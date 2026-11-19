package com.blue.open.Code.Version;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de asignaciones de tareas a versiones.
 */
public interface TareaVersionRepository extends EntidadRepositoryBorradoLogico<TareaVersion> {

    // Asignacion vigente de la tarea a la version
    Optional<TareaVersion> findFirstByTareaAndVersionAndStatus(Tarea tarea, Version version, Integer status);

    // Asignaciones vigentes de una tarea (para clonar sus versiones)
    List<TareaVersion> findByTareaAndStatus(Tarea tarea, Integer status);

    // Tareas vigentes y terminadas de la version, por orden de tipo y numero: las lineas del changelog
    @Query("SELECT new com.blue.open.Code.Version.LineaDeChangelog(ti.nombre, ti.orden, t.clave, t.titulo) "
            + "FROM TareaVersion tv JOIN tv.tarea t JOIN t.tipo ti JOIN t.estado e "
            + "WHERE tv.version = :version AND tv.status = 0 AND t.status = 0 AND e.terminado = true "
            + "ORDER BY ti.orden, t.numero")
    List<LineaDeChangelog> lineasDeChangelog(@Param("version") Version version);
}
