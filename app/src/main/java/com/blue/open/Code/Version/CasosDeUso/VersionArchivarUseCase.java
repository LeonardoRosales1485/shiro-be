package com.blue.open.Code.Version.CasosDeUso;

import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivo;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivoRepository;
import com.blue.open.Code.OperacionDeArchivo.TipoDeOperacionDeArchivo;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Version.TareasDeVersion;
import com.blue.open.Code.Version.Version;
import com.blue.open.Code.Version.VersionDTO;
import com.blue.open.Code.Version.VersionRepository;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Caso de uso: archiva una version (T5.3), con su operacion de archivo. Exige ARCHIVAR en el proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la version. Responde: VersionDTO archivada. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class VersionArchivarUseCase {

    private final TareasDeVersion tareasDeVersion;

    private final VersionRepository versionRepository;

    private final OperacionDeArchivoRepository operaciones;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final RegistradorDeHistorial historial;

    @Transactional
    public VersionDTO ejecutar(String uuid) {
        Version version = tareasDeVersion.version(uuid, 0, NivelDePermiso.ARCHIVAR);
        Long usuario = usuarioActual.getIdUsuarioActual();
        Long operacion = operaciones.save(new OperacionDeArchivo(TipoDeOperacionDeArchivo.ARCHIVAR, Historial.VERSION, uuid, usuario)).getId();
        version.archivar(usuario, operacion);
        versionRepository.save(version);
        historial.registrar(Historial.VERSION, uuid, AccionDeHistorial.ARCHIVAR, null, null, version.getNombre(), operacion);
        return tareasDeVersion.proyectar(uuid);
    }
}
