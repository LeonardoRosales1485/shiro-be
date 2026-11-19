package com.blue.open.Code.Sprint;

import com.blue.open.Code.Common.Mcp.Confirmacion;
import com.blue.open.Code.Common.Mcp.RespuestaDeHerramienta;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Sprint.CasosDeUso.SprintAsignarUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintCerrarUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintIniciarUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintQuitarUseCase;
import com.blue.open.Code.Tarea.ReferenciasDeMcp;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Herramienta MCP gestionar_sprint (T5.5 y Spec 4): crear, asignar, quitar, iniciar y cerrar sprints, que se
 * identifican por proyecto y nombre. Cerrar exige confirmar: true; sin eso responde que pasaria con las tareas.
 * Usa los mismos servicios y casos de uso que la API (EDITAR en el proyecto).
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class HerramientasMcpDeSprint {

    private final ReferenciasDeMcp referencias;

    private final SprintRepository sprintRepository;

    private final TareaRepository tareaRepository;

    private final SprintService sprintService;

    private final TareasDeSprint tareasDeSprint;

    private final SprintAsignarUseCase asignarUseCase;

    private final SprintQuitarUseCase quitarUseCase;

    private final SprintIniciarUseCase iniciarUseCase;

    private final SprintCerrarUseCase cerrarUseCase;

    /*
     * Gestiona un sprint del proyecto: crear (con fechas), asignar o quitar tareas (por clave), iniciar o cerrar
     * (destino BACKLOG por defecto, o SPRINT con otro sprint planificado). Cerrar pide confirmar: true.
     */
    @McpTool(name = "gestionar_sprint", description = "Gestiona sprints de un proyecto: crear (inicio y fin aaaa-mm-dd), asignar o quitar tareas (claves), iniciar o cerrar (lo pendiente va al BACKLOG o a otro SPRINT; pide confirmar: true).")
    @Transactional
    public RespuestaDeHerramienta gestionarSprint(
            @McpToolParam(description = "crear, asignar, quitar, iniciar o cerrar", required = true) String accion,
            @McpToolParam(description = "Prefijo del proyecto", required = true) String proyecto,
            @McpToolParam(description = "Nombre del sprint", required = true) String sprint,
            @McpToolParam(description = "Inicio, aaaa-mm-dd (crear)", required = false) String inicio,
            @McpToolParam(description = "Fin, aaaa-mm-dd (crear)", required = false) String fin,
            @McpToolParam(description = "Claves de las tareas (asignar y quitar)", required = false) List<String> tareas,
            @McpToolParam(description = "Destino de lo pendiente al cerrar: BACKLOG (por defecto) o SPRINT", required = false) String destino,
            @McpToolParam(description = "Nombre del sprint de destino si destino es SPRINT", required = false) String sprintDestino,
            @McpToolParam(description = "true para cerrar; sin esto solo se muestra la vista previa", required = false) Boolean confirmar) {
        Proyecto elegido = referencias.proyecto(proyecto);
        String normalizada = accion == null ? "" : accion.trim().toLowerCase(Locale.ROOT);
        if (normalizada.equals("crear")) {
            SprintDTO dto = new SprintDTO();
            dto.setProyectoUuid(elegido.getUuid());
            dto.setNombre(sprint);
            dto.setInicio(inicio == null ? null : LocalDate.parse(inicio));
            dto.setFin(fin == null ? null : LocalDate.parse(fin));
            if (dto.getInicio() == null || dto.getFin() == null)
                throw new ExcepcionDeApi(CodigoDeErrorKernel.FORMULARIO_INVALIDO, "Para crear un sprint hacen falta inicio y fin.",
                        "HerramientasMcpDeSprint | crear");
            SprintDTO creado = sprintService.guardar(dto);
            return new RespuestaDeHerramienta("Sprint " + creado.getNombre() + " creado", creado);
        }
        Sprint elegidoSprint = sprint(elegido, sprint);
        return switch (normalizada) {
            case "asignar" -> new RespuestaDeHerramienta("Tareas asignadas a " + sprint,
                    asignarUseCase.ejecutar(elegidoSprint.getUuid(), pedido(tareas)));
            case "quitar" -> new RespuestaDeHerramienta("Tareas quitadas de " + sprint,
                    quitarUseCase.ejecutar(elegidoSprint.getUuid(), pedido(tareas)));
            case "iniciar" -> new RespuestaDeHerramienta("Sprint " + sprint + " iniciado", iniciarUseCase.ejecutar(elegidoSprint.getUuid()));
            case "cerrar" -> cerrar(elegido, elegidoSprint, destino, sprintDestino, confirmar);
            default -> throw new ExcepcionDeApi(CodigoDeErrorKernel.FORMULARIO_INVALIDO,
                    "La acción tiene que ser crear, asignar, quitar, iniciar o cerrar.", "HerramientasMcpDeSprint | accion " + accion);
        };
    }

    // Cierra el sprint o, sin confirmar, responde cuantas tareas quedan y cuantas se mueven
    private RespuestaDeHerramienta cerrar(Proyecto proyecto, Sprint sprint, String destino, String sprintDestino, Boolean confirmar) {
        SprintCerrarRequestDTO pedido = new SprintCerrarRequestDTO();
        boolean aOtroSprint = destino != null && destino.trim().equalsIgnoreCase("SPRINT");
        pedido.setDestino(aOtroSprint ? DestinoDeCierre.SPRINT : DestinoDeCierre.BACKLOG);
        pedido.setSprintDestinoUuid(aOtroSprint ? sprint(proyecto, sprintDestino).getUuid() : null);
        if (!Confirmacion.confirmada(confirmar)) {
            tareasDeSprint.sprint(sprint.getUuid());
            List<Tarea> tareas = tareaRepository.vigentesDelSprint(sprint);
            long terminadas = tareas.stream().filter(t -> t.getEstado().isTerminado()).count();
            return Confirmacion.vistaPrevia("Se cerraría el sprint " + sprint.getNombre() + ": " + terminadas
                    + " tareas terminadas quedan en el sprint y " + (tareas.size() - terminadas) + " sin terminar van "
                    + (aOtroSprint ? "al sprint " + sprintDestino : "al backlog"));
        }
        return new RespuestaDeHerramienta("Sprint " + sprint.getNombre() + " cerrado", cerrarUseCase.ejecutar(sprint.getUuid(), pedido));
    }

    // Sprint vigente del proyecto por nombre. Lanza REGISTRO_NO_ENCONTRADO
    private Sprint sprint(Proyecto proyecto, String nombre) {
        return sprintRepository.findFirstByProyectoAndNombreAndStatus(proyecto, nombre, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "No existe el sprint " + nombre + " en " + proyecto.getPrefijo() + ".", "HerramientasMcpDeSprint | sprint"));
    }

    // Pedido de tareas a partir de sus claves
    private SprintTareasRequestDTO pedido(List<String> claves) {
        SprintTareasRequestDTO pedido = new SprintTareasRequestDTO();
        pedido.setTareas(referencias.uuidsDeTareas(claves));
        return pedido;
    }
}
