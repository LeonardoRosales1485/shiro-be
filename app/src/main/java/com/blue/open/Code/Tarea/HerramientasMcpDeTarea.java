package com.blue.open.Code.Tarea;

import com.blue.open.Code.Comentario.ComentarioDTO;
import com.blue.open.Code.Comentario.ComentarioService;
import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Code.Common.Mcp.RespuestaDeHerramienta;
import com.blue.open.Code.Historial.HistorialDTO;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Relacion.CasosDeUso.RelacionCrearUseCase;
import com.blue.open.Code.Relacion.RelacionCrearRequestDTO;
import com.blue.open.Code.Relacion.RelacionDTO;
import com.blue.open.Code.Relacion.TipoDeRelacion;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.Tarea.CasosDeUso.TareaClonarUseCase;
import com.blue.open.Code.Tarea.CasosDeUso.TareaHistorialUseCase;
import com.blue.open.Code.Tarea.CasosDeUso.TareaMoverUseCase;
import com.blue.open.Core.Kernel.DTO.FiltroDePaginado;
import com.blue.open.Core.Kernel.DTO.OrdenDePaginado;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.ParametroDePaginado;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Herramientas MCP de tareas (T3.2 y Spec 4): buscar_tareas, obtener_tarea, ver_historial, crear_tarea,
 * editar_tarea, mover_tarea, clonar_tarea, comentar_tarea y relacionar_tareas. Las tareas se identifican por clave
 * (SHI-123). Cada herramienta llama al mismo servicio o caso de uso que el endpoint REST, asi que tiene los mismos
 * permisos, validaciones e historial (con canal MCP, que marca FiltroDeMcp).
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class HerramientasMcpDeTarea {

    private static final int TAMANO = 50;

    private final TareaService tareaService;

    private final TareaMoverUseCase moverUseCase;

    private final TareaHistorialUseCase historialUseCase;

    private final TareaClonarUseCase clonarUseCase;

    private final ComentarioService comentarioService;

    private final RelacionCrearUseCase relacionCrearUseCase;

    private final ReferenciasDeMcp referencias;

    // Busca tareas vigentes por texto (titulo o clave) y filtros. Responde una pagina de 50 y un resumen
    @McpTool(name = "buscar_tareas", description = "Busca tareas por texto en español (clave, título y descripción) o por clave, con filtros opcionales. 50 por página.")
    public RespuestaDeHerramienta buscarTareas(
            @McpToolParam(description = "Texto a buscar (clave, título o descripción), o una clave como SHI-12", required = false) String texto,
            @McpToolParam(description = "Prefijo del proyecto, por ejemplo SHI", required = false) String proyecto,
            @McpToolParam(description = "Tablero: PRINCIPAL, QA, ANALISIS o DISENO", required = false) String tablero,
            @McpToolParam(description = "Tipo: EPICA, HISTORIA, TAREA, BUG o SUBTAREA", required = false) String tipo,
            @McpToolParam(description = "Nombre del estado, por ejemplo En curso", required = false) String estado,
            @McpToolParam(description = "Nombre de usuario del responsable", required = false) String responsable,
            @McpToolParam(description = "Número de página, desde 1. Por defecto 1", required = false) Integer pagina) throws Exception {
        PedidoDePaginado pedido = new PedidoDePaginado();
        pedido.setPagina(pagina == null ? 1 : pagina);
        pedido.setTamano(TAMANO);
        pedido.getOrdenes().add(new OrdenDePaginado("clave", "asc"));
        if (texto != null && !texto.isBlank())
            pedido.agregarParametro(new ParametroDePaginado("texto", texto.trim()));
        filtrar(pedido, "proyecto.prefijo", proyecto == null ? null : proyecto.toUpperCase(Locale.ROOT));
        filtrar(pedido, "tableroCodigo", tablero == null ? null : tablero.toUpperCase(Locale.ROOT));
        filtrar(pedido, "tipoCodigo", tipo == null ? null : tipo.toUpperCase(Locale.ROOT));
        filtrar(pedido, "estadoNombre", estado);
        filtrar(pedido, "responsable.nombreDeUsuario", responsable);
        Pagina<TareaDTO> tareas = tareaService.paginar(pedido);
        return new RespuestaDeHerramienta(tareas.getTotalDeElementos() + " tareas encontradas", tareas);
    }

    // Una tarea completa por clave. Exige VER en su tablero
    @McpTool(name = "obtener_tarea", description = "Muestra una tarea completa por su clave (por ejemplo SHI-12).")
    public RespuestaDeHerramienta obtenerTarea(@McpToolParam(description = "Clave de la tarea, por ejemplo SHI-12", required = true) String clave) {
        TareaDTO tarea = tareaService.obtenerPorUuid(referencias.tarea(clave).getUuid());
        return new RespuestaDeHerramienta(tarea.getClave() + " " + tarea.getTitulo() + " (" + tarea.getEstadoNombre() + ")", tarea);
    }

    // Historial de una tarea, lo mas nuevo primero; opcionalmente solo lo hecho por MCP
    @McpTool(name = "ver_historial", description = "Muestra el historial de cambios de una tarea, lo más nuevo primero.")
    public RespuestaDeHerramienta verHistorial(
            @McpToolParam(description = "Clave de la tarea", required = true) String clave,
            @McpToolParam(description = "true para ver solo los cambios hechos por MCP", required = false) Boolean soloMcp) {
        Tarea tarea = referencias.tarea(clave);
        List<HistorialDTO> historial = historialUseCase.ejecutar(tarea.getUuid(), Boolean.TRUE.equals(soloMcp) ? CanalDeCambio.MCP : null);
        return new RespuestaDeHerramienta(historial.size() + " cambios en " + tarea.getClave(), historial);
    }

    /*
     * Crea una tarea con tipo y titulo en el tablero indicado del proyecto (PRINCIPAL por defecto), en su primer
     * estado. Exige EDITAR en el tablero. Los demas datos son opcionales.
     */
    @McpTool(name = "crear_tarea", description = "Crea una tarea con tipo y título en un proyecto. Nace en el primer estado del tablero.")
    public RespuestaDeHerramienta crearTarea(
            @McpToolParam(description = "Prefijo del proyecto, por ejemplo SHI", required = true) String proyecto,
            @McpToolParam(description = "Tipo: EPICA, HISTORIA, TAREA, BUG o SUBTAREA (una Subtarea necesita padre)", required = true) String tipo,
            @McpToolParam(description = "Título de la tarea", required = true) String titulo,
            @McpToolParam(description = "Tablero: PRINCIPAL, QA, ANALISIS o DISENO. Por defecto PRINCIPAL", required = false) String tablero,
            @McpToolParam(description = "Descripción en Markdown", required = false) String descripcion,
            @McpToolParam(description = "Prioridad: MAS_ALTA, ALTA, MEDIA, BAJA o MAS_BAJA. Por defecto MEDIA", required = false) String prioridad,
            @McpToolParam(description = "Nombre de usuario del responsable", required = false) String responsable,
            @McpToolParam(description = "Clave de la tarea padre", required = false) String padre) {
        Proyecto elegido = referencias.proyecto(proyecto);
        Tablero destino = referencias.tablero(elegido, tablero);
        TareaDTO alta = new TareaDTO();
        alta.setTableroUuid(destino.getUuid());
        alta.setTipoCodigo(tipo.toUpperCase(Locale.ROOT));
        alta.setTitulo(titulo);
        alta.setDescripcion(descripcion);
        alta.setPrioridad(prioridad == null ? null : PrioridadDeTarea.valueOf(prioridad.toUpperCase(Locale.ROOT)));
        alta.setResponsableUuid(responsable == null ? null : referencias.usuario(responsable).getUuid());
        alta.setPadreUuid(padre == null ? null : referencias.tarea(padre).getUuid());
        TareaDTO creada = tareaService.guardar(alta);
        return new RespuestaDeHerramienta("Creada " + creada.getClave() + " en " + creada.getEstadoNombre(), creada);
    }

    // Edita los datos que vengan de una tarea; los que no vienen quedan como estan. Exige EDITAR en su tablero
    @McpTool(name = "editar_tarea", description = "Edita una tarea por su clave. Solo cambia los datos que mandes.")
    public RespuestaDeHerramienta editarTarea(
            @McpToolParam(description = "Clave de la tarea", required = true) String clave,
            @McpToolParam(description = "Título nuevo", required = false) String titulo,
            @McpToolParam(description = "Descripción nueva en Markdown", required = false) String descripcion,
            @McpToolParam(description = "Tipo nuevo: EPICA, HISTORIA, TAREA, BUG o SUBTAREA", required = false) String tipo,
            @McpToolParam(description = "Prioridad nueva: MAS_ALTA, ALTA, MEDIA, BAJA o MAS_BAJA", required = false) String prioridad,
            @McpToolParam(description = "Estimación en puntos", required = false) Integer puntos,
            @McpToolParam(description = "Nombre de usuario del responsable nuevo", required = false) String responsable,
            @McpToolParam(description = "Vencimiento nuevo, aaaa-mm-dd", required = false) String vencimiento,
            @McpToolParam(description = "Etiquetas nuevas (reemplazan las actuales)", required = false) List<String> etiquetas) {
        TareaDTO tarea = tareaService.obtenerPorUuid(referencias.tarea(clave).getUuid());
        if (titulo != null)
            tarea.setTitulo(titulo);
        if (descripcion != null)
            tarea.setDescripcion(descripcion);
        if (tipo != null)
            tarea.setTipoCodigo(tipo.toUpperCase(Locale.ROOT));
        if (prioridad != null)
            tarea.setPrioridad(PrioridadDeTarea.valueOf(prioridad.toUpperCase(Locale.ROOT)));
        if (puntos != null)
            tarea.setPuntos(puntos);
        if (responsable != null)
            tarea.setResponsableUuid(referencias.usuario(responsable).getUuid());
        if (vencimiento != null)
            tarea.setVencimiento(LocalDate.parse(vencimiento));
        if (etiquetas != null)
            tarea.setEtiquetas(etiquetasDto(etiquetas));
        TareaDTO editada = tareaService.guardar(tarea);
        return new RespuestaDeHerramienta("Editada " + editada.getClave(), editada);
    }

    // Mueve una tarea a otro estado de su tablero, por nombre. Exige EDITAR
    @McpTool(name = "mover_tarea", description = "Mueve una tarea a otro estado de su tablero, por nombre (por ejemplo En curso).")
    @Transactional
    public RespuestaDeHerramienta moverTarea(
            @McpToolParam(description = "Clave de la tarea", required = true) String clave,
            @McpToolParam(description = "Nombre del estado de destino, por ejemplo En curso", required = true) String estado) {
        Tarea tarea = referencias.tarea(clave);
        TareaMoverRequestDTO pedido = new TareaMoverRequestDTO();
        pedido.setEstadoUuid(referencias.estado(tarea.getTablero(), estado).getUuid());
        TareaDTO movida = moverUseCase.ejecutar(tarea.getUuid(), pedido);
        return new RespuestaDeHerramienta(movida.getClave() + " pasó a " + movida.getEstadoNombre(), movida);
    }

    /*
     * Clona una tarea a un tablero del mismo proyecto. copiar elige que datos se copian ademas de titulo y tipo; si no
     * viene, descripcion, prioridad y etiquetas. Exige VER en el original y EDITAR en el destino.
     */
    @McpTool(name = "clonar_tarea", description = "Clona una tarea a otro tablero del mismo proyecto, eligiendo qué datos se copian.")
    @Transactional
    public RespuestaDeHerramienta clonarTarea(
            @McpToolParam(description = "Clave de la tarea original", required = true) String clave,
            @McpToolParam(description = "Tablero de destino: PRINCIPAL, QA, ANALISIS o DISENO", required = true) String tablero,
            @McpToolParam(description = "Qué copiar además de título y tipo: DESCRIPCION, PRIORIDAD, ETIQUETAS, PUNTOS, RESPONSABLE, VERSIONES, COMENTARIOS, SUBTAREAS. Si no viene: DESCRIPCION, PRIORIDAD y ETIQUETAS", required = false) List<String> copiar) {
        Tarea original = referencias.tarea(clave);
        TareaClonarRequestDTO pedido = new TareaClonarRequestDTO();
        pedido.setTableroUuid(referencias.tablero(original.getProyecto(), tablero).getUuid());
        pedido.setCopiar(copiar == null ? null : copiar.stream().map(dato -> DatoAClonar.valueOf(dato.toUpperCase(Locale.ROOT))).toList());
        TareaDTO clon = clonarUseCase.ejecutar(original.getUuid(), pedido);
        return new RespuestaDeHerramienta("Clonada " + original.getClave() + " como " + clon.getClave(), clon);
    }

    // Comenta una tarea. Exige EDITAR en su tablero
    @McpTool(name = "comentar_tarea", description = "Agrega un comentario en Markdown a una tarea.")
    public RespuestaDeHerramienta comentarTarea(
            @McpToolParam(description = "Clave de la tarea", required = true) String clave,
            @McpToolParam(description = "Comentario en Markdown", required = true) String cuerpo) {
        ComentarioDTO comentario = new ComentarioDTO();
        comentario.setTareaUuid(referencias.tarea(clave).getUuid());
        comentario.setCuerpo(cuerpo);
        ComentarioDTO guardado = comentarioService.guardar(comentario);
        return new RespuestaDeHerramienta("Comentario agregado en " + clave.toUpperCase(Locale.ROOT), guardado);
    }

    // Relaciona dos tareas del mismo proyecto. Exige EDITAR en el tablero de origen y VER en el de destino
    @McpTool(name = "relacionar_tareas", description = "Relaciona dos tareas del mismo proyecto: BLOQUEA, RELACIONA o DUPLICA (de origen a destino).")
    @Transactional
    public RespuestaDeHerramienta relacionarTareas(
            @McpToolParam(description = "Clave de la tarea de origen", required = true) String origen,
            @McpToolParam(description = "Tipo: BLOQUEA, RELACIONA o DUPLICA", required = true) String tipo,
            @McpToolParam(description = "Clave de la tarea de destino", required = true) String destino) {
        RelacionCrearRequestDTO pedido = new RelacionCrearRequestDTO();
        pedido.setOrigenUuid(referencias.tarea(origen).getUuid());
        pedido.setDestinoUuid(referencias.tarea(destino).getUuid());
        pedido.setTipo(TipoDeRelacion.valueOf(tipo.toUpperCase(Locale.ROOT)));
        RelacionDTO relacion = relacionCrearUseCase.ejecutar(pedido);
        return new RespuestaDeHerramienta(relacion.getOrigenClave() + " " + tipo.toLowerCase(Locale.ROOT) + " "
                + relacion.getDestinoClave(), relacion);
    }

    // Agrega un filtro de igualdad si el valor vino
    private static void filtrar(PedidoDePaginado pedido, String campo, String valor) {
        if (valor != null && !valor.isBlank())
            pedido.agregarFiltro(List.of(new FiltroDePaginado(campo, "=", valor.trim())));
    }

    // Etiquetas como DTO para el guardado
    private static List<TareaEtiquetaDTO> etiquetasDto(List<String> textos) {
        List<TareaEtiquetaDTO> etiquetas = new ArrayList<>();
        for (String texto : textos) {
            TareaEtiquetaDTO etiqueta = new TareaEtiquetaDTO();
            etiqueta.setTexto(texto);
            etiquetas.add(etiqueta);
        }
        return etiquetas;
    }
}
