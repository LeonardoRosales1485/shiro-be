package com.blue.open.Code.Version.CasosDeUso;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Version.TareaVersion;
import com.blue.open.Code.Version.TareaVersionRepository;
import com.blue.open.Code.Version.TareasDeVersion;
import com.blue.open.Code.Version.Version;
import com.blue.open.Code.Version.VersionDTO;
import com.blue.open.Code.Version.VersionTareasRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: asigna tareas del proyecto a una version sin publicar (T5.3). Una tarea puede estar en varias
 * versiones; si ya esta en esta, no se repite. Exige EDITAR en el proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la version y las tareas. Responde: VersionDTO. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO,
 * VERSION_PUBLICADA o TAREA_DE_OTRO_PROYECTO.
 */
@Service
@RequiredArgsConstructor
public class VersionAsignarUseCase {

    private final TareasDeVersion tareasDeVersion;

    private final TareaVersionRepository tareaVersionRepository;

    @Transactional
    public VersionDTO ejecutar(String uuid, VersionTareasRequestDTO dto) {
        Version version = tareasDeVersion.sinPublicar(uuid);
        for (Tarea tarea : tareasDeVersion.tareasDelProyecto(version, dto.getTareas())) {
            if (tareaVersionRepository.findFirstByTareaAndVersionAndStatus(tarea, version, 0).isEmpty()) {
                tareaVersionRepository.save(new TareaVersion(tarea, version));
                tareasDeVersion.registrar(tarea, null, version.getNombre());
            }
        }
        return tareasDeVersion.proyectar(uuid);
    }
}
