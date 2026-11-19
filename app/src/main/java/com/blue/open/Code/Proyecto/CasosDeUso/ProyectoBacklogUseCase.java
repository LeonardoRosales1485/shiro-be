package com.blue.open.Code.Proyecto.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoBacklogResponseDTO;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: backlog de un proyecto (T5.1): sus tareas sin sprint de todos los tableros y de todos los tipos (resolucion B21),
 * en el orden unico del proyecto. Exige VER en el proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del proyecto. Responde: lista de ProyectoBacklogResponseDTO. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO.
 */
@Service
@RequiredArgsConstructor
public class ProyectoBacklogUseCase {

    private final ProyectoRepository proyectoRepository;

    private final TareaRepository tareaRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    @Transactional(readOnly = true)
    public List<ProyectoBacklogResponseDTO> ejecutar(String proyectoUuid) {
        Proyecto proyecto = proyectoRepository.findFirstByUuidAndStatus(proyectoUuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "ProyectoBacklogUseCase | ejecutar | proyecto " + proyectoUuid));
        permisos.exigir(NivelDePermiso.VER, proyecto);
        return tareaRepository.backlogDe(proyecto);
    }
}
