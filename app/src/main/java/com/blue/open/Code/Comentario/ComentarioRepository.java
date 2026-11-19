package com.blue.open.Code.Comentario;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.List;

/**
 * Repositorio de comentarios. El listado del front es el paginado del ABM, filtrado por tareaUuid.
 */
public interface ComentarioRepository extends EntidadRepositoryBorradoLogico<Comentario> {

    // Comentarios con el status recibido de una tarea, por fecha (lo usa clonar para copiarlos)
    List<Comentario> findByTareaAndStatusOrderByFechaAsc(Tarea tarea, Integer status);
}
