package com.blue.open.Code.Version.CasosDeUso;

import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Version.TareaVersionRepository;
import com.blue.open.Code.Version.TareasDeVersion;
import com.blue.open.Code.Version.Version;
import com.blue.open.Code.Version.VersionDTO;
import com.blue.open.Code.Version.VersionTareasRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * Caso de uso: quita tareas de una version sin publicar (T5.3); la asignacion queda con status -1. Exige EDITAR.
 * Dependencias externas: la base de datos.
 * Recibe: uuid de la version y las tareas. Responde: VersionDTO. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO,
 * VERSION_PUBLICADA o TAREA_DE_OTRO_PROYECTO.
 */
@Service
@RequiredArgsConstructor
public class VersionQuitarUseCase {

    private final TareasDeVersion tareasDeVersion;

    private final TareaVersionRepository tareaVersionRepository;

    @Transactional
    public VersionDTO ejecutar(String uuid, VersionTareasRequestDTO dto) {
        Version version = tareasDeVersion.sinPublicar(uuid);
        for (Tarea tarea : tareasDeVersion.tareasDelProyecto(version, dto.getTareas())) {
            tareaVersionRepository.findFirstByTareaAndVersionAndStatus(tarea, version, 0).ifPresent(asignacion -> {
                asignacion.setStatus(-1);
                asignacion.setFechaDeBorrado(new Timestamp(System.currentTimeMillis()));
                tareaVersionRepository.save(asignacion);
                tareasDeVersion.registrar(tarea, version.getNombre(), null);
            });
        }
        return tareasDeVersion.proyectar(uuid);
    }
}
