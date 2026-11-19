package com.blue.open.Code.Proyecto.CasosDeUso;

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
 * Caso de uso: restaura un proyecto archivado con todo lo que se archivo en su misma operacion (adenda 4 del DAF):
 * tableros, tareas, comentarios y relaciones. Lo archivado antes por separado queda archivado. Un proyecto archivado
 * sin operacion (por ejemplo, el de los seeds) vuelve solo. El proyecto y cada tarea dejan su fila RESTAURAR.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del proyecto. Responde: ProyectoDTO del proyecto restaurado. Lanza REGISTRO_NO_ENCONTRADO si no
 * existe o no esta archivado.
 */
@Service
@RequiredArgsConstructor
public class ProyectoRestaurarUseCase {

    private final ProyectoRepository proyectoRepository;

    private final OperacionDeArchivoRepository operacionRepository;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final TareaRepository tareaRepository;

    private final TableroRepository tableroRepository;

    private final ComentarioRepository comentarioRepository;

    private final RelacionRepository relacionRepository;

    private final RegistradorDeHistorial historial;

    private final Proyector proyector;

    @Transactional
    public ProyectoDTO ejecutar(String uuid) {
        Proyecto proyecto = proyectoRepository.findFirstByUuidAndStatus(uuid, -1)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "ProyectoRestaurarUseCase | ejecutar | proyecto archivado " + uuid));
        Long usuario = usuarioActual.getIdUsuarioActual();
        Timestamp fecha = new Timestamp(System.currentTimeMillis());
        Long archivo = proyecto.getIdOperacionArchivo();
        Long operacion = operacionRepository.save(new OperacionDeArchivo(TipoDeOperacionDeArchivo.RESTAURAR,
                Historial.PROYECTO, uuid, usuario)).getId();
        String prefijo = proyecto.getPrefijo();
        List<String> tareas = archivo == null ? List.of() : tareaRepository.uuidsArchivadosPorOperacion(archivo);
        if (archivo != null) {
            tableroRepository.restaurarPorOperacion(archivo, usuario, fecha);
            tareaRepository.restaurarPorOperacion(archivo, usuario, fecha);
            comentarioRepository.restaurarPorOperacion(archivo, usuario, fecha);
            relacionRepository.restaurarPorOperacion(archivo, usuario, fecha);
        }
        proyectoRepository.restaurar(proyecto.getId(), usuario, fecha);
        historial.registrar(Historial.PROYECTO, uuid, AccionDeHistorial.RESTAURAR, null, null, prefijo, operacion);
        for (String tarea : tareas)
            historial.registrar(Historial.TAREA, tarea, AccionDeHistorial.RESTAURAR, null, null, prefijo, operacion);
        return proyector.porUuid(Proyecto.class, uuid, ProyectoDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "ProyectoRestaurarUseCase | ejecutar | no se pudo proyectar " + uuid));
    }
}
