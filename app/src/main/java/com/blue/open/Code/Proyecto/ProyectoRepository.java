package com.blue.open.Code.Proyecto;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Repositorio de proyectos.
 */
public interface ProyectoRepository extends EntidadRepositoryBorradoLogico<Proyecto> {

    // Si algun proyecto, vigente o archivado, usa el prefijo: un prefijo archivado queda reservado (adenda 3 del DAF)
    boolean existsByPrefijo(String prefijo);

    // Proyecto vigente con su fila bloqueada (SELECT ... FOR UPDATE) hasta el fin de la transaccion. Lo usa el alta de
    // tareas para avanzar el contador de claves sin repetir numeros (Spec 7)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Proyecto p WHERE p.id = :id AND p.status = 0")
    Optional<Proyecto> bloquearPorId(@Param("id") Long id);

    // Proyecto por prefijo con el status recibido
    Optional<Proyecto> findFirstByPrefijoAndStatus(String prefijo, Integer status);

    // Proyecto por uuid en cualquier status
    Optional<Proyecto> findFirstByUuid(String uuid);

    // Proyecto por uuid con el status recibido (0: vigente, -1: archivado)
    Optional<Proyecto> findFirstByUuidAndStatus(String uuid, Integer status);

    // Archiva el proyecto vigente con la operacion recibida
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Proyecto x SET x.status = -1, x.fechaDeBorrado = :fecha, x.idUsuarioDeBorrado = :usuario, "
            + "x.idOperacionArchivo = :operacion, x.fechaDeActualizacion = :fecha WHERE x.id = :id AND x.status = 0")
    int archivar(@Param("id") Long id, @Param("operacion") Long operacion, @Param("usuario") Long usuario,
            @Param("fecha") Timestamp fecha);

    // Restaura el proyecto archivado y limpia los datos de borrado y la operacion
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Proyecto x SET x.status = 0, x.fechaDeBorrado = NULL, x.idUsuarioDeBorrado = NULL, "
            + "x.idOperacionArchivo = NULL, x.fechaDeActualizacion = :fecha, x.idUsuarioDeActualizacion = :usuario "
            + "WHERE x.id = :id AND x.status = -1")
    int restaurar(@Param("id") Long id, @Param("usuario") Long usuario, @Param("fecha") Timestamp fecha);
}
