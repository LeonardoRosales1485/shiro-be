package com.blue.open.Code.TipoTarea;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio del catalogo de tipos de tarea. Solo lee registros vigentes (status = 0).
 */
public interface TipoTareaRepository extends EntidadRepositoryBorradoLogico<TipoTarea> {

    // Tipos de tarea vigentes en el orden de los selectores
    @Query("SELECT new com.blue.open.Code.TipoTarea.TipoTareaDTO(t.uuid, t.status, t.fechaDeCreacion, t.codigo, "
            + "t.nombre, t.nivel) FROM TipoTarea t WHERE t.status = 0 ORDER BY t.orden")
    List<TipoTareaDTO> listarVigentes();

    // Tipo de tarea vigente por su codigo (EPICA, HISTORIA, TAREA, BUG o SUBTAREA)
    Optional<TipoTarea> findFirstByCodigoAndStatus(String codigo, Integer status);

    // Tipos vigentes por orden (el importador los usa para reusar uno por nombre o crear el siguiente)
    List<TipoTarea> findByStatusOrderByOrden(Integer status);
}
