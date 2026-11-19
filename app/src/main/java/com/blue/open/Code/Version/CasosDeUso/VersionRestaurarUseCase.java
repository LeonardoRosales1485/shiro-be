package com.blue.open.Code.Version.CasosDeUso;

import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Version.TareasDeVersion;
import com.blue.open.Code.Version.Version;
import com.blue.open.Code.Version.VersionDTO;
import com.blue.open.Code.Version.VersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: restaura una version archivada (T5.3). Exige ARCHIVAR en el proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la version. Responde: VersionDTO restaurada. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class VersionRestaurarUseCase {

    private final TareasDeVersion tareasDeVersion;

    private final VersionRepository versionRepository;

    private final RegistradorDeHistorial historial;

    @Transactional
    public VersionDTO ejecutar(String uuid) {
        Version version = tareasDeVersion.version(uuid, -1, NivelDePermiso.ARCHIVAR);
        version.restaurar();
        versionRepository.save(version);
        historial.registrar(Historial.VERSION, uuid, AccionDeHistorial.RESTAURAR, null, null, version.getNombre());
        return tareasDeVersion.proyectar(uuid);
    }
}
