package com.blue.open.Code.Sprint.CasosDeUso;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Sprint.DestinoDeCierre;
import com.blue.open.Code.Sprint.EstadoDeSprint;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Sprint.SprintCerrarRequestDTO;
import com.blue.open.Code.Sprint.SprintDTO;
import com.blue.open.Code.Sprint.SprintRepository;
import com.blue.open.Code.Sprint.TareasDeSprint;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: cierra el sprint activo (T5.2 y adenda 3 del DAF). Las tareas en un estado terminado quedan en el
 * sprint cerrado (asi el sprint registra lo que se hizo); las demas van al backlog o a otro sprint planificado del
 * proyecto. Exige EDITAR.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del sprint y el destino. Responde: SprintDTO. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO,
 * SPRINT_NO_ACTIVO o SPRINT_DESTINO_INVALIDO.
 */
@Service
@RequiredArgsConstructor
public class SprintCerrarUseCase {

    private final TareasDeSprint tareasDeSprint;

    private final SprintRepository sprintRepository;

    private final TareaRepository tareaRepository;

    private final RegistradorDeHistorial historial;

    @Transactional
    public SprintDTO ejecutar(String uuid, SprintCerrarRequestDTO dto) {
        Sprint sprint = tareasDeSprint.sprint(uuid);
        if (sprint.getEstado() != EstadoDeSprint.ACTIVO)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.SPRINT_NO_ACTIVO, "SprintCerrarUseCase | estado " + sprint.getEstado());
        Sprint destino = dto.getDestino() == DestinoDeCierre.SPRINT ? destino(sprint, dto.getSprintDestinoUuid()) : null;
        for (Tarea tarea : tareaRepository.vigentesDelSprint(sprint)) {
            if (!tarea.getEstado().isTerminado())
                tareasDeSprint.mover(tarea, destino);
        }
        sprint.setEstado(EstadoDeSprint.CERRADO);
        sprintRepository.save(sprint);
        historial.registrar(Historial.SPRINT, uuid, AccionDeHistorial.EDITAR, "estado", EstadoDeSprint.ACTIVO.name(),
                EstadoDeSprint.CERRADO.name());
        return tareasDeSprint.proyectar(uuid);
    }

    // Sprint planificado del mismo proyecto. Lanza SPRINT_DESTINO_INVALIDO si no lo es
    private Sprint destino(Sprint cerrado, String uuid) {
        return sprintRepository.findFirstByUuidAndStatus(uuid == null ? "" : uuid, 0)
                .filter(s -> s.getProyecto().getId().equals(cerrado.getProyecto().getId()))
                .filter(s -> s.getEstado() == EstadoDeSprint.PLANIFICADO)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.SPRINT_DESTINO_INVALIDO,
                        "SprintCerrarUseCase | destino " + uuid));
    }
}
