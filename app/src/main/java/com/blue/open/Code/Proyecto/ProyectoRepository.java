package com.blue.open.Code.Proyecto;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
