package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repositorio de Importacion.
 */
public interface ImportacionRepository extends EntidadRepositoryBorradoLogico<Importacion> {

    Optional<Importacion> findFirstByUuidAndStatus(String uuid, Integer status);

    // Pasa la importacion a EN_CURSO si no lo estaba. Responde 1 si la paso, 0 si ya estaba en curso: asi dos
    // pedidos simultaneos de ejecutar no corren dos veces
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Importacion i SET i.estado = com.blue.open.Code.Importacion.EstadoDeImportacion.EN_CURSO "
            + "WHERE i.id = :id AND i.estado <> com.blue.open.Code.Importacion.EstadoDeImportacion.EN_CURSO")
    int marcarEnCurso(@Param("id") Long id);

    // Deja la importacion FALLIDA con el resumen recibido (la ejecucion en segundo plano no pudo terminar)
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Importacion i SET i.estado = com.blue.open.Code.Importacion.EstadoDeImportacion.FALLIDA, "
            + "i.resumen = :resumen WHERE i.id = :id")
    int marcarFallida(@Param("id") Long id, @Param("resumen") String resumen);
}
