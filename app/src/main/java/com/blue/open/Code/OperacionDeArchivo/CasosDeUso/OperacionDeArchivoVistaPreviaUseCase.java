package com.blue.open.Code.OperacionDeArchivo.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.OperacionDeArchivo.ArchivoVistaPreviaResponseDTO;
import com.blue.open.Code.OperacionDeArchivo.ConteoPorOperacion;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivoRepository;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Sprint.SprintRepository;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Code.Version.VersionRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Caso de uso: vista previa de un archivado (CU-ARC-02, propuesta 49 resuelta el 19/11): que es, donde estaba y que
 * pasa al restaurarlo. En una tarea: tablero y estado, descripcion, si el padre sigue archivado, las tareas que se
 * archivaron en la misma operacion (se restauran con ella) y las hijas archivadas por separado (quedan archivadas). En
 * un proyecto: sus tareas de la operacion y las archivadas aparte. Sprints, versiones y usuarios solo traen el
 * nombre. Exige ARCHIVAR sobre el recurso (el tablero de la tarea, el proyecto en los demas); los usuarios, ser
 * administrador.
 * Dependencias externas: la base de datos.
 * Recibe: tipo y uuid del archivado. Responde: ArchivoVistaPreviaResponseDTO. Lanza REGISTRO_NO_ENCONTRADO (no esta
 * archivado o el tipo no existe) o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class OperacionDeArchivoVistaPreviaUseCase {

    private static final int ARCHIVADO = -1;

    private final OperacionDeArchivoRepository repository;

    private final TareaRepository tareaRepository;

    private final ProyectoRepository proyectoRepository;

    private final SprintRepository sprintRepository;

    private final VersionRepository versionRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional(readOnly = true)
    public ArchivoVistaPreviaResponseDTO ejecutar(String tipo, String uuid) {
        String normalizado = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        // La entidad se carga solo para exigir el permiso; la vista sale de la consulta (Proyeccion)
        return switch (normalizado) {
            case "TAREA" -> {
                Tarea tarea = archivado(tareaRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid);
                permisos.exigir(NivelDePermiso.ARCHIVAR, tarea.getTablero());
                ArchivoVistaPreviaResponseDTO vista = archivado(repository.vistaDeTarea(uuid), uuid);
                Long operacion = tarea.getIdOperacionArchivo() == null ? -1L : tarea.getIdOperacionArchivo();
                vista.setSeRestauran(repository.tareasDeLaOperacion(operacion, tarea.getId()));
                vista.setCantidadQueSeRestaura(cantidadEnLaOperacion(tarea.getIdOperacionArchivo(), 1));
                vista.setQuedanArchivadas(repository.hijasArchivadasAparte(tarea.getId(), operacion));
                yield vista;
            }
            case "PROYECTO" -> {
                Proyecto proyecto = archivado(proyectoRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid);
                permisos.exigir(NivelDePermiso.ARCHIVAR, proyecto);
                ArchivoVistaPreviaResponseDTO vista = archivado(repository.vistaDeProyecto(uuid), uuid);
                Long operacion = proyecto.getIdOperacionArchivo() == null ? -1L : proyecto.getIdOperacionArchivo();
                vista.setSeRestauran(repository.tareasDeLaOperacion(operacion, -1L));
                vista.setCantidadQueSeRestaura(cantidadEnLaOperacion(proyecto.getIdOperacionArchivo(), 0));
                vista.setQuedanArchivadas(repository.tareasDelProyectoArchivadasAparte(proyecto.getId(), operacion));
                yield vista;
            }
            case "SPRINT" -> {
                permisos.exigir(NivelDePermiso.ARCHIVAR, archivado(sprintRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid).getProyecto());
                yield archivado(repository.vistaDeSprint(uuid), uuid);
            }
            case "VERSION" -> {
                permisos.exigir(NivelDePermiso.ARCHIVAR, archivado(versionRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid).getProyecto());
                yield archivado(repository.vistaDeVersion(uuid), uuid);
            }
            case "USUARIO" -> {
                ArchivoVistaPreviaResponseDTO vista = archivado(repository.vistaDeUsuario(uuid), uuid);
                if (!permisos.esAdministrador())
                    throw new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO, "OperacionDeArchivoVistaPreviaUseCase | usuario " + uuid);
                yield vista;
            }
            default -> throw new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "OperacionDeArchivoVistaPreviaUseCase | tipo " + tipo);
        };
    }

    // Tareas archivadas en la operacion, menos las que se descuentan (la propia tarea)
    private int cantidadEnLaOperacion(Long operacion, int descontar) {
        if (operacion == null)
            return 0;
        long cantidad = repository.tareasPorOperacion(List.of(operacion)).stream().mapToLong(ConteoPorOperacion::cantidad).sum();
        return (int) Math.max(0, cantidad - descontar);
    }

    // El elemento archivado, o REGISTRO_NO_ENCONTRADO si no existe o no esta archivado
    private static <T> T archivado(Optional<T> elemento, String uuid) {
        return elemento.orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                "OperacionDeArchivoVistaPreviaUseCase | no archivado " + uuid));
    }
}
