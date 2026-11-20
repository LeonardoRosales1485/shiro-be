package com.blue.open.Code.Sprint.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Sprint.SprintDTO;
import com.blue.open.Code.Sprint.SprintRepository;
import com.blue.open.Code.Sprint.TareasDeSprint;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: restaura un sprint archivado (CU-SPR-10, T6.4). Vuelve con el estado que tenia (planificado o
 * cerrado); las tareas de un planificado ya habian vuelto al backlog al archivarlo. Exige EDITAR, como archivar.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del sprint. Responde: SprintDTO restaurado. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class SprintRestaurarUseCase {

    private final SprintRepository sprintRepository;

    private final TareasDeSprint tareasDeSprint;

    private final ResolutorDePermisosDeRecurso permisos;

    private final RegistradorDeHistorial historial;

    @Transactional
    public SprintDTO ejecutar(String uuid) {
        Sprint sprint = sprintRepository.findFirstByUuidAndStatus(uuid, -1)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "SprintRestaurarUseCase | sprint " + uuid));
        permisos.exigir(NivelDePermiso.EDITAR, sprint.getProyecto());
        sprint.restaurar();
        sprintRepository.save(sprint);
        historial.registrar(Historial.SPRINT, uuid, AccionDeHistorial.RESTAURAR, null, null, sprint.getNombre());
        return tareasDeSprint.proyectar(uuid);
    }
}
