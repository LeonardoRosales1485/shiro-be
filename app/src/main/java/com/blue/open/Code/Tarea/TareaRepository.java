package com.blue.open.Code.Tarea;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoBacklogResponseDTO;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de tareas. La lectura del ABM es la Proyeccion de TareaDTO (core-crud).
 */
public interface TareaRepository extends EntidadRepositoryBorradoLogico<Tarea> {

    // Tarea por uuid con el status recibido (0: vigente, -1: archivada)
    Optional<Tarea> findFirstByUuidAndStatus(String uuid, Integer status);

    // Tarea vigente con su fila bloqueada (SELECT ... FOR UPDATE) hasta el fin de la transaccion: dos ediciones
    // simultaneas se comparan con la fecha de actualizacion de a una (Spec 7)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tarea t WHERE t.uuid = :uuid AND t.status = 0")
    Optional<Tarea> bloquearVigentePorUuid(@Param("uuid") String uuid);

    // Tarea archivada con su fila bloqueada, para restaurarla
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tarea t WHERE t.uuid = :uuid AND t.status = -1")
    Optional<Tarea> bloquearArchivadaPorUuid(@Param("uuid") String uuid);

    // Hijas con el status recibido de cualquiera de las tareas: un nivel de la cascada, en una sola consulta
    List<Tarea> findByPadreInAndStatus(List<Tarea> padres, Integer status);

    // Tareas archivadas por la operacion recibida
    List<Tarea> findByIdOperacionArchivoAndStatus(Long idOperacionArchivo, Integer status);

    // Tarea por clave (SHI-123) en cualquier status: asi la nombran la gente y los agentes
    Optional<Tarea> findFirstByClave(String clave);

    // Cantidad de tareas del proyecto con el status recibido (vista previa de archivar un proyecto)
    long countByProyectoAndStatus(Proyecto proyecto, Integer status);

    // Backlog del proyecto (T5.1 y resolucion B21): tareas vigentes sin sprint de todos los tipos, en su orden unico; las que
    // nadie ordeno todavia van al final, por numero
    @Query("SELECT new com.blue.open.Code.Proyecto.ProyectoBacklogResponseDTO(t.uuid, t.clave, t.titulo, ti.codigo, e.nombre, tt.codigo, "
            + "t.prioridad, t.puntos, r.nombre) FROM Tarea t JOIN t.tipo ti JOIN t.estado e JOIN t.tablero tb "
            + "JOIN tb.tipoTablero tt LEFT JOIN t.responsable r "
            + "WHERE t.proyecto = :proyecto AND t.status = 0 AND t.sprint IS NULL "
            + "ORDER BY CASE WHEN t.ordenBacklog IS NULL THEN 1 ELSE 0 END, t.ordenBacklog, t.numero")
    List<ProyectoBacklogResponseDTO> backlogDe(@Param("proyecto") Proyecto proyecto);

    // Las mismas tareas del backlog, como entidades, en el mismo orden (para reordenarlas)
    @Query("SELECT t FROM Tarea t WHERE t.proyecto = :proyecto AND t.status = 0 AND t.sprint IS NULL "
            + "ORDER BY CASE WHEN t.ordenBacklog IS NULL THEN 1 ELSE 0 END, t.ordenBacklog, t.numero")
    List<Tarea> tareasDelBacklog(@Param("proyecto") Proyecto proyecto);

    // Tareas vigentes de un sprint con su estado y su sprint en la misma consulta (cerrar, archivar, vista previa: sin N+1)
    @Query("SELECT t FROM Tarea t JOIN FETCH t.estado JOIN FETCH t.sprint WHERE t.sprint = :sprint AND t.status = 0")
    List<Tarea> vigentesDelSprint(@Param("sprint") Sprint sprint);

    // Tareas vigentes con los uuids recibidos, con su sprint en la misma consulta (asignar y quitar de un sprint)
    @Query("SELECT t FROM Tarea t LEFT JOIN FETCH t.sprint WHERE t.uuid IN :uuids AND t.status = 0")
    List<Tarea> vigentesConSprint(@Param("uuids") Collection<String> uuids);

    // Ids de tareas cuyo texto (clave, titulo y descripcion, columna busqueda en español) coincide con el texto, o
    // cuya clave es el texto. SQL nativo: JPQL no expresa el operador @@ de tsvector ni plainto_tsquery. Hasta 1000
    @Query(value = "SELECT t.id FROM shr_tarea t WHERE t.busqueda @@ plainto_tsquery('spanish', :texto) "
            + "OR upper(t.clave) = upper(:texto) LIMIT 1000", nativeQuery = true)
    List<Long> idsPorTexto(@Param("texto") String texto);

    // Tarea por uuid en cualquier status (vigente o archivada)
    Optional<Tarea> findFirstByUuid(String uuid);

    // Si la tarea tiene alguna hija vigente cuyo tipo tiene nivel menor o igual al recibido
    boolean existsByPadreAndStatusAndTipoNivelLessThanEqual(Tarea padre, Integer status, Integer nivel);

    // Uuids de las tareas vigentes del proyecto (para el historial de archivar el proyecto)
    @Query("SELECT t.uuid FROM Tarea t WHERE t.proyecto = :proyecto AND t.status = 0")
    List<String> uuidsVigentesDeProyecto(@Param("proyecto") Proyecto proyecto);

    // Uuids de las tareas archivadas por la operacion (para el historial de restaurar)
    @Query("SELECT t.uuid FROM Tarea t WHERE t.idOperacionArchivo = :operacion AND t.status = -1")
    List<String> uuidsArchivadosPorOperacion(@Param("operacion") Long operacion);

    // Archiva en un solo UPDATE las tareas vigentes del proyecto, en todos los niveles, con la operacion recibida (archivar un proyecto)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Tarea x SET x.status = -1, x.fechaDeBorrado = :fecha, x.idUsuarioDeBorrado = :usuario, x.idOperacionArchivo = :operacion, x.fechaDeActualizacion = :fecha "
            + "WHERE x.status = 0 AND x.proyecto = :proyecto")
    int archivarDeProyecto(@Param("proyecto") Proyecto proyecto, @Param("operacion") Long operacion,
            @Param("usuario") Long usuario, @Param("fecha") Timestamp fecha);

    // Restaura en un solo UPDATE lo que archivo la operacion recibida (restaurar un proyecto)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Tarea x SET x.status = 0, x.fechaDeBorrado = NULL, x.idUsuarioDeBorrado = NULL, x.idOperacionArchivo = NULL, x.fechaDeActualizacion = :fecha, x.idUsuarioDeActualizacion = :usuario WHERE x.idOperacionArchivo = :operacion AND x.status = -1")
    int restaurarPorOperacion(@Param("operacion") Long operacion, @Param("usuario") Long usuario,
            @Param("fecha") Timestamp fecha);

    // Tareas por uuid con su tablero, tipo de tablero, tipo, estado, responsable, padre y sprint, en una consulta (importador, F4).
    // Sin filtro de status a proposito: una tarea importada que se archivo en Shiro se reconoce igual y no se duplica
    @Query("SELECT t FROM Tarea t JOIN FETCH t.tablero tb JOIN FETCH tb.tipoTablero JOIN FETCH t.tipo "
            + "JOIN FETCH t.estado LEFT JOIN FETCH t.responsable LEFT JOIN FETCH t.padre LEFT JOIN FETCH t.sprint WHERE t.uuid IN :uuids")
    List<Tarea> buscarParaImportar(@Param("uuids") Collection<String> uuids);

    // Cuales de las claves recibidas ya usa alguna tarea (importador, F4)
    @Query("SELECT t.clave FROM Tarea t WHERE t.clave IN :claves")
    List<String> clavesEnUso(@Param("claves") Collection<String> claves);

    // Tareas con los uuids y el status recibidos, en una consulta
    List<Tarea> findByUuidInAndStatus(Collection<String> uuids, Integer status);

    // Tareas con las claves recibidas (vigentes o archivadas), en una consulta (MCP las identifica por clave)
    List<Tarea> findByClaveIn(Collection<String> claves);
}
