package com.blue.open.Code.Tarea;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
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

    // Backlog del proyecto (T5.1): tareas vigentes de nivel 2 (historias, tareas y bugs) en su orden unico; las que
    // nadie ordeno todavia van al final, por numero
    @Query("SELECT new com.blue.open.Code.Tarea.TareaDeBacklogDTO(t.uuid, t.clave, t.titulo, ti.codigo, e.nombre, tt.codigo, "
            + "t.prioridad, t.puntos, r.nombre) FROM Tarea t JOIN t.tipo ti JOIN t.estado e JOIN t.tablero tb "
            + "JOIN tb.tipoTablero tt LEFT JOIN t.responsable r "
            + "WHERE t.proyecto = :proyecto AND t.status = 0 AND ti.nivel = 2 AND t.sprint IS NULL "
            + "ORDER BY CASE WHEN t.ordenBacklog IS NULL THEN 1 ELSE 0 END, t.ordenBacklog, t.numero")
    List<TareaDeBacklogDTO> backlogDe(@Param("proyecto") Proyecto proyecto);

    // Las mismas tareas del backlog, como entidades, en el mismo orden (para reordenarlas)
    @Query("SELECT t FROM Tarea t JOIN t.tipo ti WHERE t.proyecto = :proyecto AND t.status = 0 AND ti.nivel = 2 AND t.sprint IS NULL "
            + "ORDER BY CASE WHEN t.ordenBacklog IS NULL THEN 1 ELSE 0 END, t.ordenBacklog, t.numero")
    List<Tarea> tareasDelBacklog(@Param("proyecto") Proyecto proyecto);

    // Tareas del sprint con el status recibido
    List<Tarea> findBySprintAndStatus(Sprint sprint, Integer status);

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
}
