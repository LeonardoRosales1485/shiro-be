package com.blue.open.Code.Version;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pasos que comparten los casos de uso de versiones (T5.3): buscar la version con su permiso, exigir que no este
 * publicada, validar las tareas del proyecto y proyectar la respuesta.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class TareasDeVersion {

    private final VersionRepository versionRepository;

    private final TareaRepository tareaRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    private final RegistradorDeHistorial historial;

    private final Proyector proyector;

    // Version con el status recibido y el nivel exigido sobre su proyecto. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO
    public Version version(String uuid, int status, NivelDePermiso nivel) {
        Version version = versionRepository.findFirstByUuidAndStatus(uuid, status)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "TareasDeVersion | version " + uuid));
        permisos.exigir(nivel, version.getProyecto());
        return version;
    }

    // Version vigente sin publicar, con EDITAR. Lanza ademas VERSION_PUBLICADA
    public Version sinPublicar(String uuid) {
        Version version = version(uuid, 0, NivelDePermiso.EDITAR);
        if (version.getEstado() == EstadoDeVersion.PUBLICADA)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.VERSION_PUBLICADA, "TareasDeVersion | publicada " + uuid);
        return version;
    }

    // Tareas vigentes del proyecto de la version. Lanza TAREA_DE_OTRO_PROYECTO si alguna no lo es
    public List<Tarea> tareasDelProyecto(Version version, List<String> uuids) {
        List<Tarea> tareas = new ArrayList<>();
        for (String uuid : uuids) {
            tareas.add(tareaRepository.findFirstByUuidAndStatus(uuid, 0)
                    .filter(t -> t.getProyecto().getId().equals(version.getProyecto().getId()))
                    .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.TAREA_DE_OTRO_PROYECTO,
                            "TareasDeVersion | tarea " + uuid).conDato("tareaUuid", uuid)));
        }
        return tareas;
    }

    // Fila EDITAR (campo version) en el historial de la tarea
    public void registrar(Tarea tarea, String anterior, String nueva) {
        historial.registrar(Historial.TAREA, tarea.getUuid(), AccionDeHistorial.EDITAR, "version", anterior, nueva);
    }

    // Proyeccion de la version (vigente o archivada)
    public VersionDTO proyectar(String uuid) {
        return proyector.porUuid(Version.class, uuid, VersionDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "TareasDeVersion | proyectar " + uuid));
    }
}
