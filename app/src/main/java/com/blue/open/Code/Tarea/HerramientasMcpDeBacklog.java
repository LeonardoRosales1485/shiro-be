package com.blue.open.Code.Tarea;

import com.blue.open.Code.Common.Mcp.RespuestaDeHerramienta;
import com.blue.open.Code.Tarea.CasosDeUso.TareaBacklogUseCase;
import com.blue.open.Code.Tarea.CasosDeUso.TareaOrdenarBacklogUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Herramientas MCP del backlog (T5.5 y Spec 4): ver_backlog y ordenar_backlog. Usan los mismos casos de uso que la
 * API (VER para leer y EDITAR en el proyecto para ordenar).
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class HerramientasMcpDeBacklog {

    private final ReferenciasDeMcp referencias;

    private final TareaBacklogUseCase backlogUseCase;

    private final TareaOrdenarBacklogUseCase ordenarUseCase;

    // Backlog del proyecto en su orden unico
    @McpTool(name = "ver_backlog", description = "Muestra el backlog de un proyecto: las tareas sin sprint de todos los tipos, en su orden único.")
    public RespuestaDeHerramienta verBacklog(@McpToolParam(description = "Prefijo del proyecto", required = true) String proyecto) {
        List<TareaDeBacklogDTO> backlog = backlogUseCase.ejecutar(referencias.proyecto(proyecto).getUuid());
        return new RespuestaDeHerramienta(backlog.size() + " tareas en el backlog de " + proyecto.toUpperCase(), backlog);
    }

    // Ordena el backlog: las tareas recibidas van primero, en ese orden
    @McpTool(name = "ordenar_backlog", description = "Ordena el backlog de un proyecto: las tareas que mandes van primero, en ese orden; el resto queda después.")
    @Transactional
    public RespuestaDeHerramienta ordenarBacklog(
            @McpToolParam(description = "Prefijo del proyecto", required = true) String proyecto,
            @McpToolParam(description = "Claves de las tareas en el orden nuevo", required = true) List<String> tareas) {
        TareaOrdenarBacklogRequestDTO pedido = new TareaOrdenarBacklogRequestDTO();
        pedido.setProyectoUuid(referencias.proyecto(proyecto).getUuid());
        pedido.setTareas(tareas.stream().map(clave -> referencias.tarea(clave).getUuid()).toList());
        List<TareaDeBacklogDTO> backlog = ordenarUseCase.ejecutar(pedido);
        return new RespuestaDeHerramienta("Backlog de " + proyecto.toUpperCase() + " ordenado", backlog);
    }
}
