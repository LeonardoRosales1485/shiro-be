package com.blue.open.Code.Importacion;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Estado.EstadoRepository;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.Sprint.EstadoDeSprint;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Sprint.SprintRepository;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.Tablero.TableroRepository;
import com.blue.open.Code.TipoTablero.TipoTablero;
import com.blue.open.Code.TipoTablero.TipoTableroRepository;
import com.blue.open.Code.TipoTarea.TipoTareaRepository;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Code.Version.EstadoDeVersion;
import com.blue.open.Code.Version.Version;
import com.blue.open.Code.Version.VersionRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Entidad.EntidadBase;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ejecuta una importacion de Jira (Spec 6, T4.3) en una transaccion: crea (o reusa) el proyecto con el prefijo elegido
 * al conectar y sus tableros, resuelve el mapeo, importa versiones y sprints, delega las tareas y comentarios en
 * ImportadorDeTareas y deja la importacion TERMINADA con su resumen. Cada elemento se busca en shr_mapeo_jira:
 * si ya se importo se actualiza, si no se crea. Todo va al historial con canal IMPORTACION.
 * Dependencias externas: Jira (ClienteDeJira) y la base de datos.
 */
@Component
@RequiredArgsConstructor
public class ImportadorDeJira {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    // Duracion de los sprints sin fechas en importaciones mapeadas antes de que el mapeo la pidiera
    private static final int DIAS_DE_SPRINT_SIN_FECHAS = 14;

    private final ImportacionRepository importacionRepository;

    private final MapeosDeJira mapeos;

    private final ProyectoRepository proyectoRepository;

    private final TipoTableroRepository tipoTableroRepository;

    private final TableroRepository tableroRepository;

    private final EstadoRepository estadoRepository;

    private final TipoTareaRepository tipoTareaRepository;

    private final SprintRepository sprintRepository;

    private final VersionRepository versionRepository;

    private final UsuarioRepository usuarioRepository;

    private final RegistradorDeHistorial historial;

    private final ClienteDeJira jira;

    private final CatalogoDeJira catalogo;

    private final ImportadorDeTareas importadorDeTareas;

    private final ObjectMapper lector = new ObjectMapper();

    /*
     * Importa todo lo mapeado de la importacion recibida, como el usuario recibido. Lanza REGISTRO_NO_ENCONTRADO,
     * PREFIJO_DE_PROYECTO_EN_USO, MAPEO_DE_JIRA_INVALIDO, los errores de credenciales y los de ClienteDeJira; el
     * llamador (EjecucionDeImportacion) la deja FALLIDA.
     */
    @Transactional
    public void importar(Long idImportacion, Long idUsuario) {
        Importacion importacion = importacionRepository.buscarPorId(idImportacion)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportadorDeJira | importar | " + idImportacion));
        Usuario usuario = usuarioRepository.buscarPorId(idUsuario)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "ImportadorDeJira | usuario | " + idUsuario));
        CredencialesDeJira credenciales = catalogo.credenciales(importacion);
        MapeoDeImportacion mapeo = lector.readValue(importacion.getMapeo(), MapeoDeImportacion.class);
        Proyecto proyecto = proyecto(importacion, idUsuario);
        ContextoDeImportacion contexto = new ContextoDeImportacion(proyecto, credenciales, importacion.getClaveProyectoJira(), usuario, new ResumenDeImportacion(),
                mapeo.duracionDeSprintEnDias() == null ? DIAS_DE_SPRINT_SIN_FECHAS : mapeo.duracionDeSprintEnDias());
        resolverMapeo(contexto, mapeo);
        versiones(contexto);
        sprints(contexto);
        importadorDeTareas.importar(contexto);
        importacion.setProyecto(proyecto);
        importacion.setEstado(EstadoDeImportacion.TERMINADA);
        importacion.setResumen(lector.writeValueAsString(contexto.getResumen()));
        importacion.setIdUsuarioDeActualizacion(idUsuario);
        importacionRepository.save(importacion);
    }

    /*
     * Proyecto de la importacion, con su fila bloqueada: el que ya se importo de esa clave, o uno nuevo con la clave
     * como prefijo y un tablero por tipo. Lanza PREFIJO_DE_PROYECTO_EN_USO si el prefijo lo usa otro proyecto.
     */
    private Proyecto proyecto(Importacion importacion, Long idUsuario) {
        String clave = importacion.getClaveProyectoJira();
        Proyecto proyecto = mapeos.uuid(TipoDeEntidadJira.PROYECTO, clave)
                .flatMap(uuid -> proyectoRepository.findFirstByUuidAndStatus(uuid, 0))
                .orElseGet(() -> crearProyecto(importacion, idUsuario));
        return proyectoRepository.bloquearPorId(proyecto.getId()).orElseThrow();
    }

    // Crea el proyecto con sus tableros y registra el mapeo. Lanza PREFIJO_DE_PROYECTO_EN_USO
    private Proyecto crearProyecto(Importacion importacion, Long idUsuario) {
        String clave = importacion.getClaveProyectoJira();
        String prefijo = importacion.getPrefijo() == null ? clave : importacion.getPrefijo();
        if (proyectoRepository.existsByPrefijo(prefijo))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.PREFIJO_DE_PROYECTO_EN_USO, "ImportadorDeJira | prefijo en uso: " + prefijo);
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(importacion.getNombreProyectoJira() == null ? clave : importacion.getNombreProyectoJira());
        proyecto.setPrefijo(prefijo);
        proyecto.setDescripcion("Importado de Jira (" + importacion.getUrlJira() + ")");
        proyecto.setIdUsuarioDeCreacion(idUsuario);
        Proyecto guardado = proyectoRepository.save(proyecto);
        registrar(new RegistradorDeHistorial.Cambio(Historial.PROYECTO, guardado.getUuid(), AccionDeHistorial.CREAR, null, null, prefijo), idUsuario);
        for (TipoTablero tipo : tipoTableroRepository.findByStatusOrderByOrden(0)) {
            Tablero tablero = new Tablero(guardado, tipo);
            tablero.setIdUsuarioDeCreacion(idUsuario);
            tableroRepository.save(tablero);
        }
        mapeos.registrar(TipoDeEntidadJira.PROYECTO, clave, guardado.getUuid(), idUsuario);
        return guardado;
    }

    // Pasa el mapeo guardado a entidades del proyecto. Lanza MAPEO_DE_JIRA_INVALIDO si algo ya no existe
    private void resolverMapeo(ContextoDeImportacion contexto, MapeoDeImportacion mapeo) {
        Map<String, Estado> estadosPorUuid = new HashMap<>();
        mapeo.tableros().forEach((tableroJira, tipoTableroUuid) -> {
            TipoTablero tipo = tipoTableroRepository.buscarVigentePorUuid(tipoTableroUuid).orElseThrow(() -> invalido(tipoTableroUuid));
            Tablero tablero = tableroRepository.findFirstByProyectoAndTipoTableroCodigoAndStatus(contexto.getProyecto(), tipo.getCodigo(), 0)
                    .orElseThrow(() -> invalido(tipo.getCodigo()));
            contexto.getTableros().put(tableroJira, tablero);
            Map<String, Estado> deTablero = new HashMap<>();
            mapeo.estados().getOrDefault(tableroJira, Map.of()).forEach((estadoJira, estadoUuid) -> deTablero.put(estadoJira,
                    estadosPorUuid.computeIfAbsent(estadoUuid, uuid -> estadoRepository.findFirstByUuidAndStatus(uuid, 0)
                            .orElseThrow(() -> invalido(uuid)))));
            contexto.getEstados().put(tableroJira, deTablero);
        });
        mapeo.tipos().forEach((tipoJira, tipoUuid) -> contexto.getTipos().put(tipoJira,
                tipoTareaRepository.buscarVigentePorUuid(tipoUuid).orElseThrow(() -> invalido(tipoUuid))));
    }

    // Importa las versiones del proyecto: una publicada en Shiro ya no cambia
    private void versiones(ContextoDeImportacion contexto) {
        List<DatosDeJira.Version> deJira = jira.versiones(contexto.getCredenciales(), contexto.getClaveDeJira());
        Map<String, String> yaImportadas = mapeos.uuids(TipoDeEntidadJira.VERSION, deJira.stream().map(DatosDeJira.Version::id).toList());
        for (DatosDeJira.Version version : deJira) {
            Version enShiro = Optional.ofNullable(yaImportadas.get(version.id())).flatMap(versionRepository::buscarVigentePorUuid)
                    .or(() -> versionRepository.findFirstByProyectoAndNombreAndStatus(contexto.getProyecto(), version.nombre(), 0))
                    .orElseGet(Version::new);
            boolean nueva = enShiro.getId() == null;
            if (enShiro.getEstado() != EstadoDeVersion.PUBLICADA) {
                enShiro.setProyecto(contexto.getProyecto());
                enShiro.setNombre(version.nombre());
                if (version.publicada()) {
                    enShiro.setEstado(EstadoDeVersion.PUBLICADA);
                    enShiro.setFechaDePublicacion(version.fecha() == null ? LocalDate.now(ZONA) : version.fecha());
                    enShiro.setChangelog("Versión importada de Jira.");
                }
                auditar(enShiro, nueva, contexto.idUsuario());
                enShiro = versionRepository.save(enShiro);
            }
            if (nueva)
                registrar(new RegistradorDeHistorial.Cambio(Historial.VERSION, enShiro.getUuid(), AccionDeHistorial.CREAR, null, null,
                        enShiro.getNombre()), contexto.idUsuario());
            if (!yaImportadas.containsKey(version.id()))
                mapeos.registrar(TipoDeEntidadJira.VERSION, version.id(), enShiro.getUuid(), contexto.idUsuario());
            contexto.getVersiones().put(version.id(), enShiro);
            contexto.getResumen().contarVersion();
        }
    }

    // Importa los sprints de los tableros mapeados. Solo uno queda activo por proyecto; un sprint cerrado en Shiro no cambia
    private void sprints(ContextoDeImportacion contexto) {
        Map<String, DatosDeJira.Sprint> deJira = new LinkedHashMap<>();
        for (String tablero : contexto.getTableros().keySet())
            jira.sprints(contexto.getCredenciales(), tablero).forEach(sprint -> deJira.putIfAbsent(sprint.id(), sprint));
        Map<String, String> yaImportados = mapeos.uuids(TipoDeEntidadJira.SPRINT, deJira.keySet());
        for (DatosDeJira.Sprint sprint : deJira.values()) {
            Sprint enShiro = Optional.ofNullable(yaImportados.get(sprint.id())).flatMap(sprintRepository::buscarVigentePorUuid)
                    .or(() -> sprintRepository.findFirstByProyectoAndNombreAndStatus(contexto.getProyecto(), sprint.nombre(), 0))
                    .orElseGet(Sprint::new);
            boolean nuevo = enShiro.getId() == null;
            if (enShiro.getEstado() != EstadoDeSprint.CERRADO) {
                aplicarSprint(contexto, sprint, enShiro);
                auditar(enShiro, nuevo, contexto.idUsuario());
                enShiro = sprintRepository.save(enShiro);
            }
            if (nuevo)
                registrar(new RegistradorDeHistorial.Cambio(Historial.SPRINT, enShiro.getUuid(), AccionDeHistorial.CREAR, null, null,
                        enShiro.getNombre()), contexto.idUsuario());
            if (!yaImportados.containsKey(sprint.id()))
                mapeos.registrar(TipoDeEntidadJira.SPRINT, sprint.id(), enShiro.getUuid(), contexto.idUsuario());
            contexto.getSprints().put(sprint.id(), enShiro);
            contexto.getResumen().contarSprint();
        }
    }

    // Copia nombre, fechas y estado del sprint de Jira. Sin fechas: desde hoy, los dias del mapeo. Si ya hay otro activo, queda planificado
    private void aplicarSprint(ContextoDeImportacion contexto, DatosDeJira.Sprint sprint, Sprint enShiro) {
        LocalDate inicio = sprint.inicio() == null ? LocalDate.now(ZONA) : sprint.inicio();
        LocalDate fin = sprint.fin() == null || sprint.fin().isBefore(inicio) ? inicio.plusDays(contexto.getDuracionDeSprintEnDias()) : sprint.fin();
        enShiro.setProyecto(contexto.getProyecto());
        enShiro.setNombre(sprint.nombre());
        enShiro.setInicio(inicio);
        enShiro.setFin(fin);
        EstadoDeSprint estado = switch (sprint.estado()) {
            case "active" -> EstadoDeSprint.ACTIVO;
            case "closed" -> EstadoDeSprint.CERRADO;
            default -> EstadoDeSprint.PLANIFICADO;
        };
        if (estado == EstadoDeSprint.ACTIVO && sprintRepository.findFirstByProyectoAndEstadoAndStatus(contexto.getProyecto(), EstadoDeSprint.ACTIVO, 0)
                .filter(activo -> !activo.getId().equals(enShiro.getId())).isPresent()) {
            estado = EstadoDeSprint.PLANIFICADO;
            contexto.getResumen().advertir(sprint.nombre(), "El proyecto ya tiene un sprint activo: este quedó planificado.");
        }
        enShiro.setEstado(estado);
    }

    // Completa el usuario de creacion o de actualizacion
    private static void auditar(EntidadBase entidad, boolean nueva, Long idUsuario) {
        if (nueva)
            entidad.setIdUsuarioDeCreacion(idUsuario);
        entidad.setIdUsuarioDeActualizacion(idUsuario);
    }

    // Escribe una fila de historial con canal IMPORTACION
    private void registrar(RegistradorDeHistorial.Cambio cambio, Long idUsuario) {
        historial.registrarDesdeProceso(cambio, CanalDeCambio.IMPORTACION, idUsuario);
    }

    // Error de un mapeo que apunta a algo que ya no existe
    private static ExcepcionDeApi invalido(String referencia) {
        return new ExcepcionDeApi(CodigoDeErrorDelProyecto.MAPEO_DE_JIRA_INVALIDO, "ImportadorDeJira | mapeo | no existe " + referencia);
    }
}
