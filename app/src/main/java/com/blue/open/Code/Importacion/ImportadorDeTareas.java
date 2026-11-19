package com.blue.open.Code.Importacion;

import com.blue.open.Code.Comentario.Comentario;
import com.blue.open.Code.Comentario.ComentarioRepository;
import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Tarea.PrioridadDeTarea;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaEtiqueta;
import com.blue.open.Code.Tarea.TareaEtiquetaRepository;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Code.TipoTarea.TipoTarea;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Version.TareaVersion;
import com.blue.open.Code.Version.TareaVersionRepository;
import com.blue.open.Code.Version.Version;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Importa las tareas, subtareas y comentarios de Jira de una ejecucion (Spec 6, T4.3), dentro de la transaccion de
 * ImportadorDeJira. Trae los issues de los tableros mapeados, descarta (como falla) los de tipo o estado sin mapear y
 * las subtareas sin padre importado. Cada tarea nueva toma el numero del contador del proyecto y la clave
 * J-PREFIJO-numero de Jira (resolucion A3). Responsables y autores se resuelven con UsuariosDeJira (B34). Una tarea
 * ya importada se actualiza y no cambia de tablero; un comentario ya importado no se vuelve a traer.
 * Dependencias externas: Jira (ClienteDeJira) y la base de datos.
 */
@Component
@RequiredArgsConstructor
public class ImportadorDeTareas {

    private static final int LARGO_DEL_TITULO = 255;

    private static final String SUBTAREA = "SUBTAREA";

    private final ClienteDeJira jira;

    private final MapeosDeJira mapeos;

    private final UsuariosDeJira usuariosDeJira;

    private final TareaRepository tareaRepository;

    private final TareaEtiquetaRepository etiquetaRepository;

    private final TareaVersionRepository tareaVersionRepository;

    private final ComentarioRepository comentarioRepository;

    private final RegistradorDeHistorial historial;

    // Importa las tareas y sus comentarios. Lo que no se puede importar queda en las fallas del resumen
    public void importar(ContextoDeImportacion contexto) {
        Map<String, IssueAImportar> aceptadas = aceptar(contexto, traer(contexto));
        usuariosDeJira.precargar(contexto, aceptadas.values().stream().map(a -> a.issue().responsable()).toList());
        Map<String, String> yaImportadas = mapeos.uuids(TipoDeEntidadJira.TAREA, aceptadas.values().stream().map(a -> a.issue().id()).toList());
        Map<String, Tarea> existentes = new HashMap<>();
        if (!yaImportadas.isEmpty())
            tareaRepository.buscarParaImportar(yaImportadas.values()).forEach(tarea -> existentes.put(tarea.getUuid(), tarea));
        Map<String, String> sufijos = sufijosDeClave(contexto, aceptadas.values(), yaImportadas.keySet());
        Map<String, Tarea> porClave = new LinkedHashMap<>();
        for (IssueAImportar aceptada : aceptadas.values()) {
            Tarea existente = existentes.get(yaImportadas.get(aceptada.issue().id()));
            porClave.put(aceptada.issue().clave(), guardar(contexto, aceptada, existente, sufijos.get(aceptada.issue().clave())));
        }
        padres(contexto, aceptadas, porClave);
        etiquetasYVersiones(contexto, aceptadas, porClave);
        comentarios(contexto, porClave);
    }

    // Issues de los tableros mapeados, por clave y sin repetir (un issue en dos tableros va al primero del mapeo)
    private Map<String, IssueAImportar> traer(ContextoDeImportacion contexto) {
        Map<String, IssueAImportar> issues = new LinkedHashMap<>();
        for (String tablero : contexto.getTableros().keySet())
            for (DatosDeJira.Issue issue : jira.issues(contexto.getCredenciales(), tablero, contexto.getClaveDeJira()))
                issues.putIfAbsent(issue.clave(), new IssueAImportar(issue, tablero, null, null));
        return issues;
    }

    /*
     * Issues que se pueden importar, con su tipo y estado de Shiro, de nivel menor a mayor: el tipo y el estado (en
     * su tablero) tienen que estar mapeados, y una subtarea necesita un padre aceptado de nivel menor. A una tarea que
     * no es subtarea se le quita un padre invalido, con una advertencia.
     */
    private Map<String, IssueAImportar> aceptar(ContextoDeImportacion contexto, Map<String, IssueAImportar> issues) {
        ResumenDeImportacion resumen = contexto.getResumen();
        List<IssueAImportar> conTipo = new ArrayList<>();
        for (IssueAImportar candidata : issues.values()) {
            DatosDeJira.Issue issue = candidata.issue();
            TipoTarea tipo = contexto.getTipos().get(issue.tipoId());
            Estado estado = contexto.getEstados().getOrDefault(candidata.tablero(), Map.of()).get(issue.estadoId());
            if (tipo == null)
                resumen.fallo(issue.clave(), "Su tipo de Jira (id " + issue.tipoId() + ") no está mapeado.");
            else if (estado == null)
                resumen.fallo(issue.clave(), "Su estado de Jira (id " + issue.estadoId() + ") no está mapeado en su tablero.");
            else
                conTipo.add(new IssueAImportar(issue, candidata.tablero(), tipo, estado));
        }
        conTipo.sort(Comparator.comparing(candidata -> candidata.tipo().getNivel()));
        Map<String, IssueAImportar> aceptadas = new LinkedHashMap<>();
        for (IssueAImportar candidata : conTipo) {
            String padre = candidata.issue().padreClave();
            boolean padreValido = padre != null && aceptadas.containsKey(padre)
                    && aceptadas.get(padre).tipo().getNivel() < candidata.tipo().getNivel();
            if (SUBTAREA.equals(candidata.tipo().getCodigo()) && !padreValido) {
                resumen.fallo(candidata.issue().clave(), "Es una subtarea y su padre no se importa.");
                continue;
            }
            if (padre != null && !padreValido)
                resumen.advertir(candidata.issue().clave(), "Su padre en Jira (" + padre + ") no se importa o no es de un nivel mayor: quedó sin padre.");
            aceptadas.put(candidata.issue().clave(), padreValido ? candidata : candidata.sinPadre());
        }
        return aceptadas;
    }

    /*
     * Sufijo de la clave J-PREFIJO-sufijo de cada tarea nueva (resolucion A3 del 19/11): el numero de su clave de Jira
     * (JSIM-7 queda J-SIM-7). Si esa clave ya la usa otra tarea, o dos issues dan el mismo numero (uno movido de otro
     * proyecto de Jira), el sufijo es la clave de Jira entera (J-SIM-OLD-7), con una advertencia.
     */
    private Map<String, String> sufijosDeClave(ContextoDeImportacion contexto, Iterable<IssueAImportar> aceptadas, Set<String> idsYaImportados) {
        String inicio = "J-" + contexto.getProyecto().getPrefijo() + "-";
        Map<String, String> sufijos = new LinkedHashMap<>();
        for (IssueAImportar aceptada : aceptadas)
            if (!idsYaImportados.contains(aceptada.issue().id())) {
                String clave = aceptada.issue().clave();
                sufijos.put(clave, clave.substring(clave.lastIndexOf('-') + 1));
            }
        Set<String> enUso = sufijos.isEmpty() ? Set.of()
                : new HashSet<>(tareaRepository.clavesEnUso(sufijos.values().stream().map(sufijo -> inicio + sufijo).toList()));
        Set<String> tomados = new HashSet<>();
        sufijos.replaceAll((clave, sufijo) -> {
            if (!enUso.contains(inicio + sufijo) && tomados.add(sufijo))
                return sufijo;
            contexto.getResumen().advertir(clave, "Su número ya lo usaba otra tarea: se importó como " + inicio + clave + ".");
            return clave;
        });
        return sufijos;
    }

    // Crea o actualiza la tarea de un issue aceptado y escribe su historial. Responde la tarea guardada
    private Tarea guardar(ContextoDeImportacion contexto, IssueAImportar aceptada, Tarea existente, String sufijoDeClave) {
        DatosDeJira.Issue issue = aceptada.issue();
        boolean nueva = existente == null;
        Tarea tarea = nueva ? new Tarea() : existente;
        Map<String, String> antes = nueva ? Map.of() : valores(tarea);
        if (nueva) {
            tarea.setProyecto(contexto.getProyecto());
            tarea.setTablero(contexto.getTableros().get(aceptada.tablero()));
            tarea.setInformante(contexto.getUsuario());
            tarea.setIdUsuarioDeCreacion(contexto.idUsuario());
            tarea.numerarImportada(contexto.getProyecto().getPrefijo(), contexto.getProyecto().siguienteNumero(), sufijoDeClave);
        }
        tarea.setTitulo(issue.titulo().length() > LARGO_DEL_TITULO ? issue.titulo().substring(0, LARGO_DEL_TITULO) : issue.titulo());
        tarea.setDescripcion(issue.descripcion());
        tarea.setTipo(aceptada.tipo());
        if (nueva || Objects.equals(aceptada.estado().getTipoTablero().getId(), tarea.getTablero().getTipoTablero().getId()))
            tarea.setEstado(aceptada.estado());
        else
            contexto.getResumen().advertir(issue.clave(), "Ya estaba importada en otro tablero: conservó su estado.");
        tarea.setPrioridad(prioridad(issue.prioridad()));
        tarea.setResponsable(usuariosDeJira.resolver(contexto, issue.responsable()));
        tarea.setSprint(issue.sprintId() == null ? null : contexto.getSprints().get(issue.sprintId()));
        tarea.setIdUsuarioDeActualizacion(contexto.idUsuario());
        Tarea guardada = tareaRepository.save(tarea);
        if (nueva) {
            registrar(contexto, guardada.getUuid(), AccionDeHistorial.CREAR, null, null, guardada.getClave());
            mapeos.registrar(TipoDeEntidadJira.TAREA, issue.id(), guardada.getUuid(), contexto.idUsuario());
        } else {
            valores(guardada).forEach((campo, nuevo) -> {
                if (!Objects.equals(antes.get(campo), nuevo))
                    registrar(contexto, guardada.getUuid(), AccionDeHistorial.EDITAR, campo, antes.get(campo), nuevo);
            });
        }
        contexto.getResumen().contarTarea(nueva, aceptada.tipo().getCodigo());
        return guardada;
    }

    // Asigna el padre de Jira a cada tarea que lo tiene (ya validado en aceptar) y registra el cambio
    private void padres(ContextoDeImportacion contexto, Map<String, IssueAImportar> aceptadas, Map<String, Tarea> porClave) {
        for (IssueAImportar aceptada : aceptadas.values()) {
            Tarea tarea = porClave.get(aceptada.issue().clave());
            Tarea padre = aceptada.issue().padreClave() == null ? null : porClave.get(aceptada.issue().padreClave());
            String anterior = tarea.getPadre() == null ? null : tarea.getPadre().getClave();
            String nuevo = padre == null ? null : padre.getClave();
            if (Objects.equals(anterior, nuevo))
                continue;
            tarea.setPadre(padre);
            tareaRepository.save(tarea);
            registrar(contexto, tarea.getUuid(), AccionDeHistorial.EDITAR, "padre", anterior, nuevo);
            if (padre != null)
                registrar(contexto, padre.getUuid(), AccionDeHistorial.CREAR_SUBTAREA, null, null, tarea.getClave());
        }
    }

    // Sincroniza las etiquetas (las que ya no estan quedan con status -1) y agrega las versiones de Jira que falten
    private void etiquetasYVersiones(ContextoDeImportacion contexto, Map<String, IssueAImportar> aceptadas, Map<String, Tarea> porClave) {
        Map<Long, List<TareaEtiqueta>> etiquetas = new HashMap<>();
        etiquetaRepository.findByTareaInAndStatus(porClave.values(), 0)
                .forEach(etiqueta -> etiquetas.computeIfAbsent(etiqueta.getTarea().getId(), id -> new ArrayList<>()).add(etiqueta));
        Map<Long, Set<Long>> versiones = new HashMap<>();
        tareaVersionRepository.findByTareaInAndStatus(porClave.values(), 0)
                .forEach(asignacion -> versiones.computeIfAbsent(asignacion.getTarea().getId(), id -> new HashSet<>()).add(asignacion.getVersion().getId()));
        for (IssueAImportar aceptada : aceptadas.values()) {
            Tarea tarea = porClave.get(aceptada.issue().clave());
            Set<String> pedidas = new LinkedHashSet<>(aceptada.issue().etiquetas().stream().map(String::trim).filter(texto -> !texto.isEmpty()).toList());
            for (TareaEtiqueta actual : etiquetas.getOrDefault(tarea.getId(), List.of())) {
                if (!pedidas.remove(actual.getTexto())) {
                    actual.setStatus(-1);
                    etiquetaRepository.save(actual);
                }
            }
            for (String texto : pedidas) {
                TareaEtiqueta etiqueta = new TareaEtiqueta(tarea, texto);
                etiqueta.setIdUsuarioDeCreacion(contexto.idUsuario());
                etiquetaRepository.save(etiqueta);
            }
            for (String idVersion : aceptada.issue().versionIds()) {
                Version version = contexto.getVersiones().get(idVersion);
                if (version != null && versiones.computeIfAbsent(tarea.getId(), id -> new HashSet<>()).add(version.getId())) {
                    TareaVersion asignacion = new TareaVersion(tarea, version);
                    asignacion.setIdUsuarioDeCreacion(contexto.idUsuario());
                    tareaVersionRepository.save(asignacion);
                }
            }
        }
    }

    /*
     * Trae los comentarios de cada tarea importada y crea los que todavia no se importaron, con su fecha original. Si
     * el autor no tiene usuario en Shiro, el autor es quien importa y el cuerpo empieza con el nombre de Jira.
     */
    private void comentarios(ContextoDeImportacion contexto, Map<String, Tarea> porClave) {
        Map<DatosDeJira.Comentario, Tarea> deJira = new LinkedHashMap<>();
        porClave.forEach((clave, tarea) -> jira.comentarios(contexto.getCredenciales(), clave).forEach(comentario -> deJira.put(comentario, tarea)));
        Map<String, String> yaImportados = mapeos.uuids(TipoDeEntidadJira.COMENTARIO, deJira.keySet().stream().map(DatosDeJira.Comentario::id).toList());
        usuariosDeJira.precargar(contexto, deJira.keySet().stream().map(DatosDeJira.Comentario::autor).toList());
        deJira.forEach((deJiraComentario, tarea) -> {
            if (yaImportados.containsKey(deJiraComentario.id()))
                return;
            Usuario autor = usuariosDeJira.resolver(contexto, deJiraComentario.autor());
            String cuerpo = deJiraComentario.cuerpo() == null ? "" : deJiraComentario.cuerpo();
            Comentario comentario = new Comentario();
            comentario.setTarea(tarea);
            comentario.setAutor(autor == null ? contexto.getUsuario() : autor);
            comentario.setCuerpo(cuerpo);
            comentario.setCanal(CanalDeCambio.IMPORTACION);
            comentario.setFecha(Timestamp.from(deJiraComentario.fecha().toInstant()));
            comentario.setIdUsuarioDeCreacion(contexto.idUsuario());
            Comentario guardado = comentarioRepository.save(comentario);
            registrar(contexto, tarea.getUuid(), AccionDeHistorial.COMENTAR, null, null, guardado.getCuerpo());
            mapeos.registrar(TipoDeEntidadJira.COMENTARIO, deJiraComentario.id(), guardado.getUuid(), contexto.idUsuario());
            contexto.getResumen().contarComentario();
        });
    }

    // Valores que sigue el historial de una tarea actualizada, como texto
    private static Map<String, String> valores(Tarea tarea) {
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("estado", tarea.getEstado() == null ? null : tarea.getEstado().getNombre());
        valores.put("titulo", tarea.getTitulo());
        valores.put("prioridad", tarea.getPrioridad() == null ? null : tarea.getPrioridad().name());
        valores.put("descripcion", tarea.getDescripcion());
        valores.put("responsable", tarea.getResponsable() == null ? null : tarea.getResponsable().getNombre());
        valores.put("tipo", tarea.getTipo() == null ? null : tarea.getTipo().getNombre());
        valores.put("sprint", tarea.getSprint() == null ? null : tarea.getSprint().getNombre());
        return valores;
    }

    // Prioridad de Shiro de la prioridad de Jira (Highest, High, Medium, Low, Lowest); MEDIA si no la reconoce
    private static PrioridadDeTarea prioridad(String deJira) {
        return switch (deJira == null ? "" : deJira) {
            case "Highest" -> PrioridadDeTarea.MAS_ALTA;
            case "High" -> PrioridadDeTarea.ALTA;
            case "Low" -> PrioridadDeTarea.BAJA;
            case "Lowest" -> PrioridadDeTarea.MAS_BAJA;
            default -> PrioridadDeTarea.MEDIA;
        };
    }

    // Escribe una fila de historial de la tarea con canal IMPORTACION
    private void registrar(ContextoDeImportacion contexto, String uuid, AccionDeHistorial accion, String campo, String anterior, String nuevo) {
        historial.registrarDesdeProceso(new RegistradorDeHistorial.Cambio(Historial.TAREA, uuid, accion, campo, anterior, nuevo),
                CanalDeCambio.IMPORTACION, contexto.idUsuario());
    }

    /**
     * Un issue de Jira con el tablero de Jira del que vino y, una vez aceptado, su tipo y estado de Shiro.
     */
    private record IssueAImportar(DatosDeJira.Issue issue, String tablero, TipoTarea tipo, Estado estado) {

        // El mismo issue sin padre (su padre no se importa o no es de un nivel mayor)
        IssueAImportar sinPadre() {
            DatosDeJira.Issue i = issue;
            return new IssueAImportar(new DatosDeJira.Issue(i.id(), i.clave(), i.titulo(), i.descripcion(), i.tipoId(), i.estadoId(),
                    i.prioridad(), null, i.responsable(), i.etiquetas(), i.sprintId(), i.versionIds()), tablero, tipo, estado);
        }
    }
}
