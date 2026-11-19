package com.blue.open.Code.OperacionDeArchivo.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.OperacionDeArchivo.ArchivoVistaPreviaResponseDTO;
import com.blue.open.Code.OperacionDeArchivo.ConteoPorOperacion;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivoRepository;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Sprint.SprintRepository;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Code.Usuario.UsuarioRepository;
import com.blue.open.Code.Version.Version;
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

    private final UsuarioRepository usuarioRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional(readOnly = true)
    public ArchivoVistaPreviaResponseDTO ejecutar(String tipo, String uuid) {
        String normalizado = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        ArchivoVistaPreviaResponseDTO vista = switch (normalizado) {
            case "TAREA" -> deTarea(archivado(tareaRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid));
            case "PROYECTO" -> deProyecto(archivado(proyectoRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid));
            case "SPRINT" -> {
                Sprint sprint = archivado(sprintRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid);
                permisos.exigir(NivelDePermiso.ARCHIVAR, sprint.getProyecto());
                yield basica(sprint.getNombre());
            }
            case "VERSION" -> {
                Version version = archivado(versionRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid);
                permisos.exigir(NivelDePermiso.ARCHIVAR, version.getProyecto());
                yield basica(version.getNombre());
            }
            case "USUARIO" -> {
                Usuario usuario = archivado(usuarioRepository.findFirstByUuidAndStatus(uuid, ARCHIVADO), uuid);
                if (!permisos.esAdministrador())
                    throw new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO, "OperacionDeArchivoVistaPreviaUseCase | usuario " + uuid);
                yield basica(usuario.getNombreDeUsuario());
            }
            default -> throw new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "OperacionDeArchivoVistaPreviaUseCase | tipo " + tipo);
        };
        vista.setTipo(normalizado);
        vista.setUuid(uuid);
        return vista;
    }

    // Tarea: tablero y estado, descripcion, padre archivado, lo que se restaura con ella y sus hijas archivadas aparte
    private ArchivoVistaPreviaResponseDTO deTarea(Tarea tarea) {
        permisos.exigir(NivelDePermiso.ARCHIVAR, tarea.getTablero());
        ArchivoVistaPreviaResponseDTO vista = basica(tarea.getClave() + " " + tarea.getTitulo());
        vista.setUbicacion(tarea.getTablero().getTipoTablero().getNombre() + " · " + tarea.getEstado().getNombre());
        vista.setDescripcion(tarea.getDescripcion());
        vista.setPadreArchivado(tarea.getPadre() != null && tarea.getPadre().getStatus() == ARCHIVADO);
        Long operacion = tarea.getIdOperacionArchivo() == null ? -1L : tarea.getIdOperacionArchivo();
        vista.setSeRestauran(repository.tareasDeLaOperacion(operacion, tarea.getId()));
        vista.setCantidadQueSeRestaura(cantidadEnLaOperacion(tarea.getIdOperacionArchivo(), 1));
        vista.setQuedanArchivadas(repository.hijasArchivadasAparte(tarea.getId(), operacion));
        return vista;
    }

    // Proyecto: sus tareas de la operacion (se restauran) y las archivadas aparte (quedan archivadas)
    private ArchivoVistaPreviaResponseDTO deProyecto(Proyecto proyecto) {
        permisos.exigir(NivelDePermiso.ARCHIVAR, proyecto);
        ArchivoVistaPreviaResponseDTO vista = basica(proyecto.getPrefijo() + " " + proyecto.getNombre());
        Long operacion = proyecto.getIdOperacionArchivo() == null ? -1L : proyecto.getIdOperacionArchivo();
        vista.setSeRestauran(repository.tareasDeLaOperacion(operacion, -1L));
        vista.setCantidadQueSeRestaura(cantidadEnLaOperacion(proyecto.getIdOperacionArchivo(), 0));
        vista.setQuedanArchivadas(repository.tareasDelProyectoArchivadasAparte(proyecto.getId(), operacion));
        return vista;
    }

    // Tareas archivadas en la operacion, menos las que se descuentan (la propia tarea)
    private int cantidadEnLaOperacion(Long operacion, int descontar) {
        if (operacion == null)
            return 0;
        long cantidad = repository.tareasPorOperacion(List.of(operacion)).stream().mapToLong(ConteoPorOperacion::cantidad).sum();
        return (int) Math.max(0, cantidad - descontar);
    }

    // Vista con solo el nombre
    private static ArchivoVistaPreviaResponseDTO basica(String nombre) {
        ArchivoVistaPreviaResponseDTO vista = new ArchivoVistaPreviaResponseDTO();
        vista.setNombre(nombre);
        return vista;
    }

    // El elemento archivado, o REGISTRO_NO_ENCONTRADO si no existe o no esta archivado
    private static <T> T archivado(Optional<T> elemento, String uuid) {
        return elemento.orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                "OperacionDeArchivoVistaPreviaUseCase | no archivado " + uuid));
    }
}
