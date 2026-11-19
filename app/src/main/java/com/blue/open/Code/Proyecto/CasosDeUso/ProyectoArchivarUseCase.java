package com.blue.open.Code.Proyecto.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Comentario.ComentarioRepository;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivo;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivoRepository;
import com.blue.open.Code.OperacionDeArchivo.TipoDeOperacionDeArchivo;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoDTO;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Relacion.RelacionRepository;
import com.blue.open.Code.Tablero.TableroRepository;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

/**
 * Caso de uso: archiva un proyecto vigente con todo lo que hay dentro (adenda 4 del DAF): sus tableros, sus tareas
 * en todos los niveles, los comentarios y las relaciones de esas tareas, con el mismo id de operacion. Cada tabla se
 * archiva con un solo UPDATE (sin una consulta por registro). El proyecto y cada tarea dejan su fila ARCHIVAR en el
 * historial, con el prefijo del proyecto. Lo archivado antes por separado conserva su propia operacion.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del proyecto. Responde: ProyectoDTO del proyecto archivado. Lanza REGISTRO_NO_ENCONTRADO si no existe
 * o ya esta archivado.
 */
@Service
@RequiredArgsConstructor
public class ProyectoArchivarUseCase {

    private final ProyectoRepository proyectoRepository;

    private final TableroRepository tableroRepository;

    private final TareaRepository tareaRepository;

    private final ComentarioRepository comentarioRepository;

    private final RelacionRepository relacionRepository;

    private final OperacionDeArchivoRepository operacionRepository;

    private final RegistradorDeHistorial historial;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final Proyector proyector;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional
    public ProyectoDTO ejecutar(String uuid) {
        Proyecto proyecto = proyectoRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "ProyectoArchivarUseCase | ejecutar | proyecto vigente " + uuid));
        permisos.exigir(NivelDePermiso.ARCHIVAR, proyecto);
        Long usuario = usuarioActual.getIdUsuarioActual();
        Timestamp fecha = new Timestamp(System.currentTimeMillis());
        Long operacion = operacionRepository.save(new OperacionDeArchivo(TipoDeOperacionDeArchivo.ARCHIVAR,
                Historial.PROYECTO, uuid, usuario)).getId();
        String prefijo = proyecto.getPrefijo();
        List<String> tareas = tareaRepository.uuidsVigentesDeProyecto(proyecto);
        // Relaciones y comentarios primero: se eligen por las tareas que todavia estan vigentes
        relacionRepository.archivarDeProyecto(proyecto, operacion, usuario, fecha);
        comentarioRepository.archivarDeProyecto(proyecto, operacion, usuario, fecha);
        tareaRepository.archivarDeProyecto(proyecto, operacion, usuario, fecha);
        tableroRepository.archivarDeProyecto(proyecto, operacion, usuario, fecha);
        proyectoRepository.archivar(proyecto.getId(), operacion, usuario, fecha);
        historial.registrar(Historial.PROYECTO, uuid, AccionDeHistorial.ARCHIVAR, null, null, prefijo, operacion);
        for (String tarea : tareas)
            historial.registrar(Historial.TAREA, tarea, AccionDeHistorial.ARCHIVAR, null, null, prefijo, operacion);
        return proyector.porUuid(Proyecto.class, uuid, ProyectoDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "ProyectoArchivarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
