package com.blue.open.Code.Sprint.CasosDeUso;

import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Sprint.SprintDTO;
import com.blue.open.Code.Sprint.SprintTareasRequestDTO;
import com.blue.open.Code.Sprint.TareasDeSprint;
import com.blue.open.Code.Tarea.Tarea;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: asigna tareas a un sprint no cerrado (T5.2). Las tareas son historias, tareas o bugs vigentes del
 * proyecto del sprint; salen del backlog (o de otro sprint no cerrado). Exige EDITAR en el proyecto. Cada tarea deja
 * su fila EDITAR (campo sprint) en el historial.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del sprint y las tareas. Responde: SprintDTO. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO,
 * SPRINT_CERRADO o TAREA_FUERA_DEL_BACKLOG.
 */
@Service
@RequiredArgsConstructor
public class SprintAsignarUseCase {

    private final TareasDeSprint tareasDeSprint;

    @Transactional
    public SprintDTO ejecutar(String uuid, SprintTareasRequestDTO dto) {
        Sprint sprint = tareasDeSprint.sprintEditable(uuid);
        for (Tarea tarea : tareasDeSprint.tareasDelProyecto(sprint, dto.getTareas())) {
            tareasDeSprint.mover(tarea, sprint);
        }
        return tareasDeSprint.proyectar(uuid);
    }
}
