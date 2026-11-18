package com.blue.open.Code.TipoTarea;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio del catalogo de tipos de tarea. Solo lee registros vigentes (status = 0).
 */
public interface TipoTareaRepository extends EntidadRepositoryBorradoLogico<TipoTarea> {

    // Tipos de tarea vigentes en el orden de los selectores
    @Query("SELECT new com.blue.open.Code.TipoTarea.TipoTareaDTO(t.uuid, t.status, t.fechaDeCreacion, t.codigo, "
            + "t.nombre, t.nivel) FROM TipoTarea t WHERE t.status = 0 ORDER BY t.orden")
    List<TipoTareaDTO> listarVigentes();
}
