package com.blue.open.Code.Tarea;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.List;

/**
 * Repositorio de etiquetas de tareas.
 */
public interface TareaEtiquetaRepository extends EntidadRepositoryBorradoLogico<TareaEtiqueta> {

    // Etiquetas de la tarea con el status recibido (0: vigentes)
    List<TareaEtiqueta> findByTareaAndStatus(Tarea tarea, Integer status);
}
