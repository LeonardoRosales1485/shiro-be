package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Mcp.RespuestaDeHerramienta;
import com.blue.open.Code.Estado.Estado;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.Tarea.ReferenciasDeMcp;
import com.blue.open.Code.Tarea.TareaDTO;
import com.blue.open.Code.Tarea.TareaService;
import com.blue.open.Core.Kernel.DTO.FiltroDePaginado;
import com.blue.open.Core.Kernel.DTO.OrdenDePaginado;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Herramientas MCP de consulta de proyectos y tableros (T3.2 y Spec 4): listar_proyectos y ver_tablero. Usan los
 * mismos servicios que la API, con sus permisos: un proyecto o tablero que el usuario no ve no aparece.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class HerramientasMcpDeProyecto {

    private static final int TAMANO = 50;

    private final ProyectoService proyectoService;

    private final TareaService tareaService;

    private final ReferenciasDeMcp referencias;

    private final ResolutorDePermisosDeRecurso permisos;

    // Proyectos que el usuario ve, con sus tableros. Recibe la pagina (1 por defecto). Responde la lista y un resumen
    @McpTool(name = "listar_proyectos", description = "Lista los proyectos que podés ver, con su prefijo y sus tableros. 50 por página.")
    public RespuestaDeHerramienta listarProyectos(
            @McpToolParam(description = "Número de página, desde 1. Por defecto 1", required = false) Integer pagina) throws Exception {
        PedidoDePaginado pedido = new PedidoDePaginado();
        pedido.setPagina(pagina == null ? 1 : pagina);
        pedido.setTamano(TAMANO);
        pedido.getOrdenes().add(new OrdenDePaginado("prefijo", "asc"));
        Pagina<ProyectoDTO> proyectos = proyectoService.paginar(pedido);
        return new RespuestaDeHerramienta(proyectos.getTotalDeElementos() + " proyectos", proyectos);
    }

    /*
     * Tablero con sus columnas (estados, en orden) y las tareas vigentes de cada una. Recibe el prefijo del proyecto
     * y el codigo del tablero (PRINCIPAL por defecto). Exige VER en el tablero.
     */
    @McpTool(name = "ver_tablero", description = "Muestra un tablero de un proyecto con sus columnas (estados) y las tareas de cada una.")
    @Transactional(readOnly = true)
    public RespuestaDeHerramienta verTablero(
            @McpToolParam(description = "Prefijo del proyecto, por ejemplo SHI", required = true) String proyecto,
            @McpToolParam(description = "Tablero: PRINCIPAL, QA, ANALISIS o DISENO. Por defecto PRINCIPAL", required = false) String tablero) throws Exception {
        Tablero elegido = referencias.tablero(referencias.proyecto(proyecto), tablero);
        permisos.exigir(NivelDePermiso.VER, elegido);
        PedidoDePaginado pedido = new PedidoDePaginado();
        pedido.setPagina(1);
        pedido.setTamano(500);
        pedido.agregarFiltro(List.of(new FiltroDePaginado("tableroUuid", "=", elegido.getUuid())));
        pedido.getOrdenes().add(new OrdenDePaginado("numero", "asc"));
        List<TareaDTO> tareas = tareaService.paginar(pedido).getContenido();
        Map<String, List<Map<String, Object>>> columnas = new LinkedHashMap<>();
        for (Estado estado : referencias.estados(elegido)) {
            columnas.put(estado.getNombre(), new ArrayList<>());
        }
        for (TareaDTO tarea : tareas) {
            Map<String, Object> tarjeta = new LinkedHashMap<>();
            tarjeta.put("clave", tarea.getClave());
            tarjeta.put("titulo", tarea.getTitulo());
            tarjeta.put("tipo", tarea.getTipoCodigo());
            tarjeta.put("prioridad", tarea.getPrioridad());
            tarjeta.put("responsable", tarea.getResponsableNombre());
            columnas.computeIfAbsent(tarea.getEstadoNombre(), nombre -> new ArrayList<>()).add(tarjeta);
        }
        return new RespuestaDeHerramienta("Tablero " + elegido.getTipoTablero().getNombre() + " de " + proyecto.toUpperCase()
                + ": " + tareas.size() + " tareas", columnas);
    }
}
