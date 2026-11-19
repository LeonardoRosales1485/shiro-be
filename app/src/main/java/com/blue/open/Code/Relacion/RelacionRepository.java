package com.blue.open.Code.Relacion;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRelacionesResponseDTO;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
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
    // El nombre del sentido (BLOQUEADA_POR, ...) lo arma el constructor de TareaRelacionesResponseDTO
    @Query("SELECT new com.blue.open.Code.Tarea.TareaRelacionesResponseDTO(r.uuid, r.tipo, "
            + "CASE WHEN o.uuid = :uuid THEN true ELSE false END, "
            + "CASE WHEN o.uuid = :uuid THEN d.uuid ELSE o.uuid END, "
            + "CASE WHEN o.uuid = :uuid THEN d.clave ELSE o.clave END, "
            + "CASE WHEN o.uuid = :uuid THEN d.titulo ELSE o.titulo END, "
            + "CASE WHEN o.uuid = :uuid THEN ed.nombre ELSE eo.nombre END, "
            + "CASE WHEN o.uuid = :uuid THEN d.status ELSE o.status END) "
            + "FROM Relacion r JOIN r.origen o JOIN r.destino d JOIN o.estado eo JOIN d.estado ed "
            + "WHERE r.status = 0 AND (o.uuid = :uuid OR d.uuid = :uuid) ORDER BY r.id")
    List<TareaRelacionesResponseDTO> listarDeTarea(@Param("uuid") String uuid);

    // Archiva en un solo UPDATE las relaciones vigentes de las tareas vigentes del proyecto, con la operacion recibida (archivar un proyecto)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Relacion x SET x.status = -1, x.fechaDeBorrado = :fecha, x.idUsuarioDeBorrado = :usuario, x.idOperacionArchivo = :operacion, x.fechaDeActualizacion = :fecha "
            + "WHERE x.status = 0 AND x.origen IN (SELECT t FROM Tarea t WHERE t.proyecto = :proyecto AND t.status = 0)")
    int archivarDeProyecto(@Param("proyecto") Proyecto proyecto, @Param("operacion") Long operacion,
            @Param("usuario") Long usuario, @Param("fecha") Timestamp fecha);

    // Restaura en un solo UPDATE lo que archivo la operacion recibida (restaurar un proyecto)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Relacion x SET x.status = 0, x.fechaDeBorrado = NULL, x.idUsuarioDeBorrado = NULL, x.idOperacionArchivo = NULL, x.fechaDeActualizacion = :fecha, x.idUsuarioDeActualizacion = :usuario WHERE x.idOperacionArchivo = :operacion AND x.status = -1")
    int restaurarPorOperacion(@Param("operacion") Long operacion, @Param("usuario") Long usuario,
            @Param("fecha") Timestamp fecha);
}
