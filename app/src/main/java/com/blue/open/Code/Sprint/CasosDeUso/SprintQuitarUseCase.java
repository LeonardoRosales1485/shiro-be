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
 * Caso de uso: quita tareas de un sprint no cerrado; vuelven al backlog (T5.2). Exige EDITAR en el proyecto. Las
 * tareas que no estan en el sprint se ignoran.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del sprint y las tareas. Responde: SprintDTO. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO o SPRINT_CERRADO.
 */
@Service
@RequiredArgsConstructor
public class SprintQuitarUseCase {

    private final TareasDeSprint tareasDeSprint;

    @Transactional
    public SprintDTO ejecutar(String uuid, SprintTareasRequestDTO dto) {
        Sprint sprint = tareasDeSprint.sprintEditable(uuid);
        for (Tarea tarea : tareasDeSprint.tareasDelProyecto(sprint, dto.getTareas())) {
            if (tarea.getSprint() != null && tarea.getSprint().getId().equals(sprint.getId()))
                tareasDeSprint.mover(tarea, null);
        }
        return tareasDeSprint.proyectar(uuid);
    }
}
