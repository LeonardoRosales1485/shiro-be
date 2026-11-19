package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio de tareas. La lectura del ABM es la Proyeccion de TareaDTO (core-crud).
 */
public interface TareaRepository extends EntidadRepositoryBorradoLogico<Tarea> {

    // Tarea por uuid con el status recibido (0: vigente, -1: archivada)
    Optional<Tarea> findFirstByUuidAndStatus(String uuid, Integer status);

    // Tarea vigente con su fila bloqueada (SELECT ... FOR UPDATE) hasta el fin de la transaccion: dos ediciones
    // simultaneas se comparan con la fecha de actualizacion de a una (Spec 7)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tarea t WHERE t.uuid = :uuid AND t.status = 0")
    Optional<Tarea> bloquearVigentePorUuid(@Param("uuid") String uuid);

    // Si existe una tarea con el uuid, en cualquier status
    boolean existsByUuid(String uuid);

    // Si la tarea tiene alguna hija vigente cuyo tipo tiene nivel menor o igual al recibido
    boolean existsByPadreAndStatusAndTipoNivelLessThanEqual(Tarea padre, Integer status, Integer nivel);
}
