package com.blue.open.Code.Version.CasosDeUso;

import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Version.ChangelogDeVersion;
import com.blue.open.Code.Version.TareaVersionRepository;
import com.blue.open.Code.Version.TareasDeVersion;
import com.blue.open.Code.Version.Version;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: changelog de una version en Markdown (T5.3): de una publicada, el que se guardo al publicar; de una
 * sin publicar, el de este momento. Lista las tareas terminadas agrupadas por tipo. Exige VER en el proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la version. Responde: el Markdown. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class VersionChangelogUseCase {

    private final TareasDeVersion tareasDeVersion;

    private final TareaVersionRepository tareaVersionRepository;

    @Transactional(readOnly = true)
    public String ejecutar(String uuid) {
        Version version = tareasDeVersion.version(uuid, 0, NivelDePermiso.VER);
        if (version.getChangelog() != null)
            return version.getChangelog();
        return ChangelogDeVersion.armar(version.getNombre(), null, tareaVersionRepository.lineasDeChangelog(version));
    }
}
