package com.blue.open.Code.Version.CasosDeUso;

import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Version.TareaVersionRepository;
import com.blue.open.Code.Version.TareasDeVersion;
import com.blue.open.Code.Version.Version;
import com.blue.open.Code.Version.VersionTareaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: tareas de una version (CU-VER-10, T6.3): las vigentes asignadas, terminadas o no, por orden de tipo y
 * numero, para el detalle de versiones del front. Exige VER en el proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la version. Responde: lista de VersionTareaResponseDTO. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class VersionTareasUseCase {

    private final TareasDeVersion tareasDeVersion;

    private final TareaVersionRepository tareaVersionRepository;

    @Transactional(readOnly = true)
    public List<VersionTareaResponseDTO> ejecutar(String uuid) {
        Version version = tareasDeVersion.version(uuid, 0, NivelDePermiso.VER);
        return tareaVersionRepository.tareasDe(version);
    }
}
