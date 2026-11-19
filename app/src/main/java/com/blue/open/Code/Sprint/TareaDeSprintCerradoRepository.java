package com.blue.open.Code.Sprint;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio de TareaDeSprintCerrado: las fotos de las tareas al cerrar un sprint.
 */
public interface TareaDeSprintCerradoRepository extends EntidadRepositoryBorradoLogico<TareaDeSprintCerrado> {

    // Tareas del cierre del sprint, por numero de tarea, con el nombre del sprint de destino
    @Query("SELECT new com.blue.open.Code.Sprint.SprintCierreTareaDTO(t.uuid, t.clave, t.titulo, tt.codigo, c.estadoAlCerrar, "
            + "c.terminada, c.puntos, c.destino, d.nombre) FROM TareaDeSprintCerrado c JOIN c.tarea t JOIN t.tablero tb "
            + "JOIN tb.tipoTablero tt LEFT JOIN c.sprintDestino d WHERE c.sprint = :sprint AND c.status = 0 ORDER BY t.numero")
    List<SprintCierreTareaDTO> tareasDelCierre(@Param("sprint") Sprint sprint);
}
