package com.blue.open.Code.Sprint.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Sprint.SprintCierreResponseDTO;
import com.blue.open.Code.Sprint.SprintRepository;
import com.blue.open.Code.Sprint.TareaDeSprintCerradoRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: cierre de un sprint (CU-SPR-09, propuesta 46 resuelta el 19/11): quien lo cerro, cuando y la foto de
 * cada tarea al cerrar. Exige VER en el proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del sprint. Responde: SprintCierreResponseDTO (sin fecha ni tareas si no esta cerrado). Lanza
 * REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class SprintCierreUseCase {

    private final SprintRepository sprintRepository;

    private final TareaDeSprintCerradoRepository cierres;

    private final ResolutorDePermisosDeRecurso permisos;

    private final Proyector proyector;

    @Transactional(readOnly = true)
    public SprintCierreResponseDTO ejecutar(String uuid) {
        Sprint sprint = sprintRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "SprintCierreUseCase | sprint " + uuid));
        permisos.exigir(NivelDePermiso.VER, sprint.getProyecto());
        // Quien y cuando salen de la proyeccion; las tareas, de la foto del cierre
        SprintCierreResponseDTO cierre = proyector.porUuid(Sprint.class, uuid, SprintCierreResponseDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "SprintCierreUseCase | proyeccion " + uuid));
        cierre.setTareas(cierres.tareasDelCierre(sprint));
        return cierre;
    }
}
