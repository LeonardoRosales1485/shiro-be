package com.blue.open.Code.Version.CasosDeUso;

import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Version.ChangelogDeVersion;
import com.blue.open.Code.Version.EstadoDeVersion;
import com.blue.open.Code.Version.TareaVersionRepository;
import com.blue.open.Code.Version.TareasDeVersion;
import com.blue.open.Code.Version.Version;
import com.blue.open.Code.Version.VersionDTO;
import com.blue.open.Code.Version.VersionPublicarRequestDTO;
import com.blue.open.Code.Version.VersionRepository;
import com.blue.open.Code.Usuario.UsuarioDeLaSesion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Caso de uso: publica una version (T5.3). Quien publica elige la fecha (hoy si no la indica); el changelog de ese
 * momento queda guardado y la version no cambia mas. Exige EDITAR en el proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la version y la fecha. Responde: VersionDTO publicada. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO o
 * VERSION_PUBLICADA.
 */
@Service
@RequiredArgsConstructor
public class VersionPublicarUseCase {

    private final TareasDeVersion tareasDeVersion;

    private final VersionRepository versionRepository;

    private final TareaVersionRepository tareaVersionRepository;

    private final RegistradorDeHistorial historial;

    private final UsuarioDeLaSesion usuarioDeLaSesion;

    @Transactional
    public VersionDTO ejecutar(String uuid, VersionPublicarRequestDTO dto) {
        Version version = tareasDeVersion.sinPublicar(uuid);
        LocalDate fecha = dto.getFecha() != null ? dto.getFecha() : LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        version.publicar(fecha, ChangelogDeVersion.armar(version.getNombre(), fecha, tareaVersionRepository.lineasDeChangelog(version)),
                usuarioDeLaSesion.obtener());
        versionRepository.save(version);
        historial.registrar(Historial.VERSION, uuid, AccionDeHistorial.EDITAR, "estado", EstadoDeVersion.SIN_PUBLICAR.name(),
                EstadoDeVersion.PUBLICADA.name() + " " + fecha);
        return tareasDeVersion.proyectar(uuid);
    }
}
