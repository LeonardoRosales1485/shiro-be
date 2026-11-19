package com.blue.open.Code.Sprint.CasosDeUso;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Sprint.EstadoDeSprint;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Sprint.SprintDTO;
import com.blue.open.Code.Sprint.SprintRepository;
import com.blue.open.Code.Sprint.TareasDeSprint;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: inicia un sprint planificado (T5.2 y Spec 7). Solo puede haber un sprint activo por proyecto: si hay
 * otro, se rechaza (y el indice unico parcial de la base lo garantiza ante dos pedidos simultaneos). Exige EDITAR.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del sprint. Responde: SprintDTO. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO, SPRINT_NO_PLANIFICADO o
 * SPRINT_ACTIVO_EXISTENTE.
 */
@Service
@RequiredArgsConstructor
public class SprintIniciarUseCase {

    private final TareasDeSprint tareasDeSprint;

    private final SprintRepository sprintRepository;

    private final RegistradorDeHistorial historial;

    @Transactional
    public SprintDTO ejecutar(String uuid) {
        Sprint sprint = tareasDeSprint.sprint(uuid);
        if (sprint.getEstado() != EstadoDeSprint.PLANIFICADO)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.SPRINT_NO_PLANIFICADO, "SprintIniciarUseCase | estado " + sprint.getEstado());
        sprintRepository.findFirstByProyectoAndEstadoAndStatus(sprint.getProyecto(), EstadoDeSprint.ACTIVO, 0).ifPresent(activo -> {
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.SPRINT_ACTIVO_EXISTENTE,
                    "Ya hay un sprint activo en el proyecto: " + activo.getNombre() + ". Cerralo antes de iniciar otro.",
                    "SprintIniciarUseCase | activo " + activo.getUuid());
        });
        sprint.setEstado(EstadoDeSprint.ACTIVO);
        sprintRepository.save(sprint);
        historial.registrar(Historial.SPRINT, uuid, AccionDeHistorial.EDITAR, "estado", EstadoDeSprint.PLANIFICADO.name(),
                EstadoDeSprint.ACTIVO.name());
        return tareasDeSprint.proyectar(uuid);
    }
}
