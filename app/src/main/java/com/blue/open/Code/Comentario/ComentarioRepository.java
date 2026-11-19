package com.blue.open.Code.Comentario;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * Repositorio de comentarios. El listado del front es el paginado del ABM, filtrado por tareaUuid.
 */
public interface ComentarioRepository extends EntidadRepositoryBorradoLogico<Comentario> {

    // Comentarios con el status recibido de una tarea, por fecha (lo usa clonar para copiarlos)
    List<Comentario> findByTareaAndStatusOrderByFechaAsc(Tarea tarea, Integer status);

    // Archiva en un solo UPDATE los comentarios vigentes de las tareas vigentes del proyecto, con la operacion recibida (archivar un proyecto)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Comentario x SET x.status = -1, x.fechaDeBorrado = :fecha, x.idUsuarioDeBorrado = :usuario, x.idOperacionArchivo = :operacion, x.fechaDeActualizacion = :fecha "
            + "WHERE x.status = 0 AND x.tarea IN (SELECT t FROM Tarea t WHERE t.proyecto = :proyecto AND t.status = 0)")
    int archivarDeProyecto(@Param("proyecto") Proyecto proyecto, @Param("operacion") Long operacion,
            @Param("usuario") Long usuario, @Param("fecha") Timestamp fecha);

    // Restaura en un solo UPDATE lo que archivo la operacion recibida (restaurar un proyecto)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Comentario x SET x.status = 0, x.fechaDeBorrado = NULL, x.idUsuarioDeBorrado = NULL, x.idOperacionArchivo = NULL, x.fechaDeActualizacion = :fecha, x.idUsuarioDeActualizacion = :usuario WHERE x.idOperacionArchivo = :operacion AND x.status = -1")
    int restaurarPorOperacion(@Param("operacion") Long operacion, @Param("usuario") Long usuario,
            @Param("fecha") Timestamp fecha);
}
