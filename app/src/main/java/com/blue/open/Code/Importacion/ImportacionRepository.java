package com.blue.open.Code.Importacion;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repositorio de Importacion: busqueda por uuid y marca atomica de ejecucion en curso.
 */
public interface ImportacionRepository extends EntidadRepositoryBorradoLogico<Importacion> {

    // Importacion por uuid con el status pedido (0 = vigente)
    Optional<Importacion> findFirstByUuidAndStatus(String uuid, Integer status);

    // Pasa la importacion a EN_CURSO si no lo estaba. Responde 1 si la paso, 0 si ya estaba en curso: asi dos
    // pedidos simultaneos de ejecutar no corren dos veces
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Importacion i SET i.estado = com.blue.open.Code.Importacion.EstadoDeImportacion.EN_CURSO, "
            + "i.avance = null, i.cancelacionPedida = false WHERE i.id = :id AND i.estado <> com.blue.open.Code.Importacion.EstadoDeImportacion.EN_CURSO")
    int marcarEnCurso(@Param("id") Long id);

    // Deja la importacion FALLIDA con el resumen recibido (la ejecucion en segundo plano no pudo terminar)
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Importacion i SET i.estado = com.blue.open.Code.Importacion.EstadoDeImportacion.FALLIDA, "
            + "i.resumen = :resumen WHERE i.id = :id")
    int marcarFallida(@Param("id") Long id, @Param("resumen") String resumen);

    // Deja la importacion CANCELADA con el resumen recibido (un administrador la corto; no quedo nada de esa ejecucion)
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Importacion i SET i.estado = com.blue.open.Code.Importacion.EstadoDeImportacion.CANCELADA, "
            + "i.cancelacionPedida = false, i.resumen = :resumen WHERE i.id = :id")
    int marcarCancelada(@Param("id") Long id, @Param("resumen") String resumen);

    // Pide cancelar la importacion si esta en curso. Responde 1 si la marco, 0 si no estaba en curso
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Importacion i SET i.cancelacionPedida = true "
            + "WHERE i.id = :id AND i.estado = com.blue.open.Code.Importacion.EstadoDeImportacion.EN_CURSO")
    int pedirCancelacion(@Param("id") Long id);

    // Guarda el avance de la importacion (lo llama la ejecucion en su propia transaccion, para que se vea mientras corre)
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Importacion i SET i.avance = :avance WHERE i.id = :id")
    int guardarAvance(@Param("id") Long id, @Param("avance") String avance);

    // Si se pidio cancelar la importacion (lee el valor confirmado en la base, no el de la entidad cargada)
    @Query("SELECT i.cancelacionPedida FROM Importacion i WHERE i.id = :id")
    Optional<Boolean> cancelacionPedida(@Param("id") Long id);
}
