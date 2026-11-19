package com.blue.open.Code.Tablero;

import com.blue.open.Code.Autorizacion.TableroDeProyecto;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de tableros de proyecto.
 */
public interface TableroRepository extends EntidadRepositoryBorradoLogico<Tablero> {

    // Tableros vigentes con el id de su proyecto (para calcular lo visible de un usuario)
    @Query("SELECT new com.blue.open.Code.Autorizacion.TableroDeProyecto(t.id, t.proyecto.id) FROM Tablero t WHERE t.status = 0")
    List<TableroDeProyecto> listarVigentesConProyecto();

    // Tablero por uuid con el status recibido (0: vigente)
    Optional<Tablero> findFirstByUuidAndStatus(String uuid, Integer status);

    // Archiva en un solo UPDATE los tableros vigentes del proyecto, con la operacion recibida (archivar un proyecto)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Tablero x SET x.status = -1, x.fechaDeBorrado = :fecha, x.idUsuarioDeBorrado = :usuario, x.idOperacionArchivo = :operacion, x.fechaDeActualizacion = :fecha "
            + "WHERE x.status = 0 AND x.proyecto = :proyecto")
    int archivarDeProyecto(@Param("proyecto") Proyecto proyecto, @Param("operacion") Long operacion,
            @Param("usuario") Long usuario, @Param("fecha") Timestamp fecha);

    // Restaura en un solo UPDATE lo que archivo la operacion recibida (restaurar un proyecto)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Tablero x SET x.status = 0, x.fechaDeBorrado = NULL, x.idUsuarioDeBorrado = NULL, x.idOperacionArchivo = NULL, x.fechaDeActualizacion = :fecha, x.idUsuarioDeActualizacion = :usuario WHERE x.idOperacionArchivo = :operacion AND x.status = -1")
    int restaurarPorOperacion(@Param("operacion") Long operacion, @Param("usuario") Long usuario,
            @Param("fecha") Timestamp fecha);
}
