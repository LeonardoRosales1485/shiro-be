package com.blue.open.Code.Relacion;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRelacionDTO;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de relaciones entre tareas.
 */
public interface RelacionRepository extends EntidadRepositoryBorradoLogico<Relacion> {

    // Si existe una relacion con el status recibido de origen a destino con el tipo recibido
    boolean existsByOrigenAndDestinoAndTipoAndStatus(Tarea origen, Tarea destino, TipoDeRelacion tipo, Integer status);

    // Relacion por uuid con el status recibido
    Optional<Relacion> findFirstByUuidAndStatus(String uuid, Integer status);

    // Relaciones vigentes de una tarea, vistas desde ella: la otra tarea (vigente o archivada) y si esta es el origen.
    // El nombre del sentido (BLOQUEADA_POR, ...) lo arma el constructor de TareaRelacionDTO
    @Query("SELECT new com.blue.open.Code.Tarea.TareaRelacionDTO(r.uuid, r.tipo, "
            + "CASE WHEN o.uuid = :uuid THEN true ELSE false END, "
            + "CASE WHEN o.uuid = :uuid THEN d.uuid ELSE o.uuid END, "
            + "CASE WHEN o.uuid = :uuid THEN d.clave ELSE o.clave END, "
            + "CASE WHEN o.uuid = :uuid THEN d.titulo ELSE o.titulo END, "
            + "CASE WHEN o.uuid = :uuid THEN ed.nombre ELSE eo.nombre END, "
            + "CASE WHEN o.uuid = :uuid THEN d.status ELSE o.status END) "
            + "FROM Relacion r JOIN r.origen o JOIN r.destino d JOIN o.estado eo JOIN d.estado ed "
            + "WHERE r.status = 0 AND (o.uuid = :uuid OR d.uuid = :uuid) ORDER BY r.id")
    List<TareaRelacionDTO> listarDeTarea(@Param("uuid") String uuid);
}
