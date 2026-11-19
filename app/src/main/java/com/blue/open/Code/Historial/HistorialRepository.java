package com.blue.open.Code.Historial;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Core.Kernel.Repository.EntidadRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio del historial. Solo inserta y lee.
 */
public interface HistorialRepository extends EntidadRepository<Historial> {

    String PROYECCION = "SELECT new com.blue.open.Code.Historial.HistorialDTO(h.uuid, h.fechaDeCreacion, h.accion, "
            + "h.campo, h.valorAnterior, h.valorNuevo, h.canal, u.uuid, u.nombre) "
            + "FROM Historial h LEFT JOIN Usuario u ON u.id = h.idUsuarioDeCreacion "
            + "WHERE h.entidad = :entidad AND h.uuidEntidad = :uuid ";

    // Historial de un registro, el mas nuevo primero
    @Query(PROYECCION + "ORDER BY h.id DESC")
    List<HistorialDTO> listar(@Param("entidad") String entidad, @Param("uuid") String uuid);

    // Historial de un registro por un canal, el mas nuevo primero
    @Query(PROYECCION + "AND h.canal = :canal ORDER BY h.id DESC")
    List<HistorialDTO> listarPorCanal(@Param("entidad") String entidad, @Param("uuid") String uuid,
            @Param("canal") CanalDeCambio canal);

    // Filas de un registro en el orden en que se escribieron
    List<Historial> findByEntidadAndUuidEntidadOrderByIdAsc(String entidad, String uuidEntidad);
}
