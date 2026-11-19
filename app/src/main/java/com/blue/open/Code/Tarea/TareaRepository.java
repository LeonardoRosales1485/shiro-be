package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.Optional;

/**
 * Repositorio de tareas. La lectura del ABM es la Proyeccion de TareaDTO (core-crud).
 */
public interface TareaRepository extends EntidadRepositoryBorradoLogico<Tarea> {

    // Tarea por uuid con el status recibido (0: vigente, -1: archivada)
    Optional<Tarea> findFirstByUuidAndStatus(String uuid, Integer status);

    // Si la tarea tiene alguna hija vigente cuyo tipo tiene nivel menor o igual al recibido
    boolean existsByPadreAndStatusAndTipoNivelLessThanEqual(Tarea padre, Integer status, Integer nivel);
}
