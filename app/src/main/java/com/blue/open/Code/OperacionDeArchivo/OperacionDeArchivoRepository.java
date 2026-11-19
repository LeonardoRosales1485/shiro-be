package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Core.Kernel.Repository.EntidadRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio de operaciones de archivo. Tambien lee lo archivado de cada tipo para la busqueda de archivados.
 */
public interface OperacionDeArchivoRepository extends EntidadRepository<OperacionDeArchivo> {

    // Tareas archivadas, las mas recientes primero (hasta 1000)
    @Query("SELECT new com.blue.open.Code.OperacionDeArchivo.ElementoArchivado('TAREA', t.uuid, CONCAT(t.clave, ' ', t.titulo), "
            + "p.prefijo, p.id, tb.id, t.fechaDeBorrado, ub.nombre) FROM Tarea t JOIN t.proyecto p JOIN t.tablero tb LEFT JOIN Usuario ub ON ub.id = t.idUsuarioDeBorrado "
            + "WHERE t.status = -1 ORDER BY t.fechaDeBorrado DESC LIMIT 1000")
    List<ElementoArchivado> tareasArchivadas();

    // Proyectos archivados
    @Query("SELECT new com.blue.open.Code.OperacionDeArchivo.ElementoArchivado('PROYECTO', p.uuid, CONCAT(p.prefijo, ' ', p.nombre), "
            + "p.prefijo, p.id, CAST(NULL AS Long), p.fechaDeBorrado, ub.nombre) FROM Proyecto p LEFT JOIN Usuario ub ON ub.id = p.idUsuarioDeBorrado WHERE p.status = -1 "
            + "ORDER BY p.fechaDeBorrado DESC LIMIT 1000")
    List<ElementoArchivado> proyectosArchivados();

    // Sprints archivados
    @Query("SELECT new com.blue.open.Code.OperacionDeArchivo.ElementoArchivado('SPRINT', s.uuid, s.nombre, p.prefijo, p.id, "
            + "CAST(NULL AS Long), s.fechaDeBorrado, ub.nombre) FROM Sprint s JOIN s.proyecto p LEFT JOIN Usuario ub ON ub.id = s.idUsuarioDeBorrado WHERE s.status = -1 "
            + "ORDER BY s.fechaDeBorrado DESC LIMIT 1000")
    List<ElementoArchivado> sprintsArchivados();

    // Versiones archivadas
    @Query("SELECT new com.blue.open.Code.OperacionDeArchivo.ElementoArchivado('VERSION', v.uuid, v.nombre, p.prefijo, p.id, "
            + "CAST(NULL AS Long), v.fechaDeBorrado, ub.nombre) FROM Version v JOIN v.proyecto p LEFT JOIN Usuario ub ON ub.id = v.idUsuarioDeBorrado WHERE v.status = -1 "
            + "ORDER BY v.fechaDeBorrado DESC LIMIT 1000")
    List<ElementoArchivado> versionesArchivadas();

    // Usuarios archivados
    @Query("SELECT new com.blue.open.Code.OperacionDeArchivo.ElementoArchivado('USUARIO', u.uuid, u.nombreDeUsuario, "
            + "CAST(NULL AS String), CAST(NULL AS Long), CAST(NULL AS Long), u.fechaDeBorrado, ub.nombre) FROM Usuario u LEFT JOIN Usuario ub ON ub.id = u.idUsuarioDeBorrado "
            + "WHERE u.status = -1 ORDER BY u.fechaDeBorrado DESC LIMIT 1000")
    List<ElementoArchivado> usuariosArchivados();
}
