package com.blue.open.Code.Tarea;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.Estado.EstadoRepository;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.TipoTarea.TipoTarea;
import com.blue.open.Code.TipoTarea.TipoTareaRepository;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Core.Crud.AbstractService;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * ABM de tareas (CU-TAR-01 a CU-TAR-03) sobre los hooks de AbstractService. Al crear: la tarea va al proyecto de su
 * tablero, nace en el estado inicial del tablero, con prioridad Media y con quien la crea como informante, y recibe
 * la clave del contador del proyecto, bloqueado por fila. Al editar no cambian la clave, el estado ni el tablero.
 * Dependencias externas: la base de datos.
 */
@Service
@RequiredArgsConstructor
public class TareaService extends AbstractService<Tarea, TareaDTO, TareaDTO> {

    private static final int VIGENTE = 0;

    private static final int ELIMINADO = -1;

    private final TipoTareaRepository tipoTareaRepository;

    private final EstadoRepository estadoRepository;

    private final ProyectoRepository proyectoRepository;

    private final UsuarioRepository usuarioRepository;

    private final TareaEtiquetaRepository tareaEtiquetaRepository;

    private final TareaRepository tareaRepository;

    private final RegistradorDeHistorial historial;

    // Busca la tarea a editar con su fila bloqueada. Vacio si es un alta (sin uuid)
    @Override
    protected Optional<Tarea> hookGuardarBuscarExistente(TareaDTO dto) {
        if (dto.getUuid() == null || dto.getUuid().isEmpty())
            return Optional.empty();
        return tareaRepository.bloquearVigentePorUuid(dto.getUuid());
    }

    /*
     * Aplica el DTO con las reglas de la tarea. Recibe el DTO validado y la tarea (nueva o vigente). Resuelve el tipo
     * por codigo (TIPO_DE_TAREA_INEXISTENTE), rechaza cambiar el tablero (TABLERO_DE_TAREA_NO_SE_CAMBIA), ignora el
     * proyecto y el estado que mande el cliente, completa prioridad e informante y, en un alta, asigna estado inicial
     * y clave. Responde la tarea lista para guardar.
     */
    @Override
    protected Tarea hookGuardarAplicarDto(TareaDTO dto, Tarea modelo) {
        boolean esAlta = modelo.getId() == null;
        if (!esAlta)
            validarFechaLeida(dto, modelo);
        if (!esAlta && !modelo.getTablero().getUuid().equals(dto.getTableroUuid()))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.TABLERO_DE_TAREA_NO_SE_CAMBIA,
                    "TareaService | hookGuardarAplicarDto | tarea " + modelo.getClave() + " | tablero pedido "
                            + dto.getTableroUuid());
        TipoTarea tipo = tipoTareaRepository.findFirstByCodigoAndStatus(dto.getTipoCodigo(), VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.TIPO_DE_TAREA_INEXISTENTE,
                        "TareaService | hookGuardarAplicarDto | tipo " + dto.getTipoCodigo()));
        if (!esAlta)
            modelo.setValoresAnteriores(valoresDelHistorial(modelo, textosVigentes(modelo)));
        Proyecto proyectoActual = modelo.getProyecto();
        Estado estadoActual = modelo.getEstado();
        Usuario informanteActual = modelo.getInformante();
        PrioridadDeTarea prioridadActual = modelo.getPrioridad();
        // Proyecto y estado los decide el servidor: se descartan antes de que el Proyector asigne las relaciones
        dto.setProyectoUuid(null);
        dto.setEstadoUuid(null);

        Tarea aplicada = super.hookGuardarAplicarDto(dto, modelo);
        if (!esAlta) {
            // Antes de cualquier consulta: una consulta dispara el flush y la tarea no puede quedar sin proyecto
            aplicada.setProyecto(proyectoActual);
            aplicada.setEstado(estadoActual);
        }
        aplicada.setTipo(tipo);
        aplicada.setPrioridad(dto.getPrioridad() != null ? dto.getPrioridad()
                : esAlta ? PrioridadDeTarea.MEDIA : prioridadActual);
        if (aplicada.getInformante() == null)
            aplicada.setInformante(esAlta ? usuarioDeLaSesion() : informanteActual);
        aplicada.setEtiquetasPedidas(textosDeEtiquetas(dto));
        validarJerarquia(aplicada, esAlta);
        if (!esAlta)
            return aplicada;
        Tablero tablero = aplicada.getTablero();
        aplicada.setEstado(estadoRepository.findFirstByTipoTableroAndInicialTrueAndStatus(tablero.getTipoTablero(), VIGENTE)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorKernel.ERROR_INTERNO,
                        "TareaService | hookGuardarAplicarDto | el tablero " + tablero.getUuid() + " no tiene estado inicial")));
        // Bloquea la fila del proyecto hasta el commit: dos altas simultaneas no reciben el mismo numero (Spec 7)
        Proyecto proyecto = proyectoRepository.bloquearPorId(tablero.getProyecto().getId())
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.RELACION_NO_ENCONTRADA,
                        "TareaService | hookGuardarAplicarDto | el proyecto del tablero " + tablero.getUuid() + " no esta vigente")
                        .conDato("campo", "tableroUuid"));
        aplicada.setProyecto(proyecto);
        aplicada.numerar(proyecto.getPrefijo(), proyecto.siguienteNumero());
        return aplicada;
    }

    /*
     * Guarda la tarea y sincroniza sus etiquetas con las pedidas: crea las nuevas y deja con status -1 las que ya no
     * vienen (nada se borra). Recibe la tarea aplicada; responde la tarea guardada.
     */
    @Override
    protected Tarea hookGuardarEnBase(Tarea modelo) {
        boolean esAlta = modelo.getId() == null;
        Tarea guardada = super.hookGuardarEnBase(modelo);
        Set<String> pedidas = new LinkedHashSet<>(modelo.getEtiquetasPedidas());
        for (TareaEtiqueta etiqueta : tareaEtiquetaRepository.findByTareaAndStatus(guardada, VIGENTE)) {
            if (!pedidas.remove(etiqueta.getTexto())) {
                etiqueta.setStatus(ELIMINADO);
                etiqueta.setFechaDeBorrado(new Timestamp(System.currentTimeMillis()));
                etiqueta.setIdUsuarioDeBorrado(usuarioActual.getIdUsuarioActual());
                tareaEtiquetaRepository.save(etiqueta);
            }
        }
        for (String texto : pedidas) {
            TareaEtiqueta nueva = new TareaEtiqueta(guardada, texto);
            nueva.setIdUsuarioDeCreacion(usuarioActual.getIdUsuarioActual());
            tareaEtiquetaRepository.save(nueva);
        }
        registrarEnElHistorial(guardada, esAlta);
        return guardada;
    }

    /*
     * Escribe el historial del guardado en la misma transaccion. Alta: CREAR con la clave y, si tiene padre,
     * CREAR_SUBTAREA en el historial del padre (adenda 3 del DAF). Edicion: una fila EDITAR por campo cambiado.
     */
    private void registrarEnElHistorial(Tarea tarea, boolean esAlta) {
        if (!esAlta) {
            List<String> etiquetas = tarea.getEtiquetasPedidas().stream().sorted().toList();
            historial.registrarCambios(Historial.TAREA, tarea.getUuid(), tarea.getValoresAnteriores(),
                    valoresDelHistorial(tarea, etiquetas));
            return;
        }
        historial.registrar(Historial.TAREA, tarea.getUuid(), AccionDeHistorial.CREAR, null, null, tarea.getClave());
        if (tarea.getPadre() != null)
            historial.registrar(Historial.TAREA, tarea.getPadre().getUuid(), AccionDeHistorial.CREAR_SUBTAREA, null,
                    null, tarea.getClave());
    }

    // Valores de la tarea que sigue el historial, como texto y en orden fijo. Recibe las etiquetas ya ordenadas
    private static Map<String, String> valoresDelHistorial(Tarea tarea, List<String> etiquetas) {
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("titulo", tarea.getTitulo());
        valores.put("descripcion", tarea.getDescripcion());
        valores.put("tipo", tarea.getTipo() == null ? null : tarea.getTipo().getNombre());
        valores.put("prioridad", tarea.getPrioridad() == null ? null : tarea.getPrioridad().name());
        valores.put("puntos", tarea.getPuntos() == null ? null : tarea.getPuntos().toString());
        valores.put("vencimiento", tarea.getVencimiento() == null ? null : tarea.getVencimiento().toString());
        valores.put("responsable", tarea.getResponsable() == null ? null : tarea.getResponsable().getNombre());
        valores.put("informante", tarea.getInformante() == null ? null : tarea.getInformante().getNombre());
        valores.put("padre", tarea.getPadre() == null ? null : tarea.getPadre().getClave());
        valores.put("etiquetas", etiquetas.isEmpty() ? null : String.join(", ", etiquetas));
        return valores;
    }

    // Textos de las etiquetas vigentes de una tarea guardada, en orden alfabetico
    private List<String> textosVigentes(Tarea tarea) {
        return tareaEtiquetaRepository.findByTareaAndStatus(tarea, VIGENTE).stream().map(TareaEtiqueta::getTexto)
                .sorted().toList();
    }

    /*
     * Control de edicion simultanea (Spec 7): la fecha leida tiene que coincidir con la de la tarea, al milisegundo
     * (el JSON lleva milisegundos y la base, microsegundos). Despues deja en el DTO la fecha de la base, para que la
     * copia del DTO no la cambie. Lanza FECHA_DE_LECTURA_OBLIGATORIA o TAREA_MODIFICADA.
     */
    private static void validarFechaLeida(TareaDTO dto, Tarea modelo) {
        if (dto.getFechaDeActualizacion() == null)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.FECHA_DE_LECTURA_OBLIGATORIA,
                    "TareaService | validarFechaLeida | tarea " + modelo.getClave());
        if (dto.getFechaDeActualizacion().getTime() != modelo.getFechaDeActualizacion().getTime())
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.TAREA_MODIFICADA,
                    "TareaService | validarFechaLeida | tarea " + modelo.getClave() + " | leida "
                            + dto.getFechaDeActualizacion() + " | actual " + modelo.getFechaDeActualizacion());
        dto.setFechaDeActualizacion(modelo.getFechaDeActualizacion());
    }

    /*
     * Valida la jerarquia (Spec 7): el padre es otra tarea del mismo proyecto, de un tipo de nivel menor; al editar,
     * ninguna hija vigente queda con un nivel menor o igual al del tipo nuevo. Lanza JERARQUIA_DE_TAREA_INVALIDA o
     * PADRE_DE_OTRO_PROYECTO.
     */
    private void validarJerarquia(Tarea tarea, boolean esAlta) {
        TipoTarea tipo = tarea.getTipo();
        Tarea padre = tarea.getPadre();
        if (padre != null) {
            Long idProyecto = esAlta ? tarea.getTablero().getProyecto().getId() : tarea.getProyecto().getId();
            if (!padre.getProyecto().getId().equals(idProyecto))
                throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.PADRE_DE_OTRO_PROYECTO,
                        "TareaService | validarJerarquia | padre " + padre.getClave());
            if (padre.getId().equals(tarea.getId()) || padre.getTipo().getNivel() >= tipo.getNivel())
                throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.JERARQUIA_DE_TAREA_INVALIDA,
                        conArticulo(tipo, true) + " no puede ir dentro de " + conArticulo(padre.getTipo(), false) + ".",
                        "TareaService | validarJerarquia | padre " + padre.getClave());
        }
        if (!esAlta && tareaRepository.existsByPadreAndStatusAndTipoNivelLessThanEqual(tarea, VIGENTE, tipo.getNivel()))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.JERARQUIA_DE_TAREA_INVALIDA,
                    "La tarea tiene hijas que no pueden ir dentro de " + conArticulo(tipo, false) + ".",
                    "TareaService | validarJerarquia | tarea " + tarea.getClave() + " | tipo " + tipo.getCodigo());
    }

    // Nombre del tipo con su articulo indefinido ("Una Épica", "un Bug"), para los mensajes al usuario
    private static String conArticulo(TipoTarea tipo, boolean mayuscula) {
        String articulo = "BUG".equals(tipo.getCodigo()) ? "un" : "una";
        if (mayuscula)
            articulo = Character.toUpperCase(articulo.charAt(0)) + articulo.substring(1);
        return articulo + " " + tipo.getNombre();
    }

    // Usuario de la sesion, que es el informante de una tarea nueva. Lanza NO_AUTENTICADO si no hay sesion
    private Usuario usuarioDeLaSesion() {
        Long id = usuarioActual.getIdUsuarioActual();
        if (id == null)
            throw new ExcepcionDeApi(CodigoDeErrorKernel.NO_AUTENTICADO, "TareaService | usuarioDeLaSesion | sin sesion");
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorKernel.NO_AUTENTICADO,
                        "TareaService | usuarioDeLaSesion | no existe el usuario de la sesion"));
    }

    // Textos de las etiquetas del DTO, sin espacios en los extremos y sin repetir, en el orden recibido
    private static List<String> textosDeEtiquetas(TareaDTO dto) {
        Set<String> textos = new LinkedHashSet<>();
        for (TareaEtiquetaDTO etiqueta : dto.getEtiquetas()) {
            textos.add(etiqueta.getTexto().strip());
        }
        return List.copyOf(textos);
    }
}
