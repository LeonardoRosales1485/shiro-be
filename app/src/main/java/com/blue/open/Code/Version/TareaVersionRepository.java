package com.blue.open.Code.Version;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Repositorio de asignaciones de tareas a versiones.
 */
public interface TareaVersionRepository extends EntidadRepositoryBorradoLogico<TareaVersion> {



    // Versiones vigentes y sin publicar de una tarea, en una consulta (clonar con VERSIONES)
    @Query("SELECT tv.version FROM TareaVersion tv WHERE tv.tarea = :tarea AND tv.status = 0 AND tv.version.status = 0 "
            + "AND tv.version.estado = com.blue.open.Code.Version.EstadoDeVersion.SIN_PUBLICAR")
    List<Version> versionesSinPublicarDe(@Param("tarea") Tarea tarea);

    // Tareas vigentes y terminadas de la version, por orden de tipo y numero: las lineas del changelog
    @Query("SELECT new com.blue.open.Code.Version.LineaDeChangelog(ti.nombre, ti.orden, t.clave, t.titulo) "
            + "FROM TareaVersion tv JOIN tv.tarea t JOIN t.tipo ti JOIN t.estado e "
            + "WHERE tv.version = :version AND tv.status = 0 AND t.status = 0 AND e.terminado = true "
            + "ORDER BY ti.orden, t.numero")
    List<LineaDeChangelog> lineasDeChangelog(@Param("version") Version version);

    // Tareas vigentes de la version, terminadas o no, por orden de tipo y numero (detalle de versiones del front)
    @Query("SELECT new com.blue.open.Code.Version.VersionTareaResponseDTO(t.uuid, t.clave, t.titulo, ti.codigo, ti.nombre, tt.codigo, "
            + "e.nombre, e.terminado) FROM TareaVersion tv JOIN tv.tarea t JOIN t.tipo ti JOIN t.estado e JOIN t.tablero tb "
            + "JOIN tb.tipoTablero tt WHERE tv.version = :version AND tv.status = 0 AND t.status = 0 ORDER BY ti.orden, t.numero")
    List<VersionTareaResponseDTO> tareasDe(@Param("version") Version version);

    // Asignaciones vigentes de varias tareas, en una consulta (importador, F4)
    List<TareaVersion> findByTareaInAndStatus(Collection<Tarea> tareas, Integer status);

    // Asignaciones con el status recibido de una version para varias tareas, en una consulta
    List<TareaVersion> findByVersionAndTareaInAndStatus(Version version, Collection<Tarea> tareas, Integer status);
}
