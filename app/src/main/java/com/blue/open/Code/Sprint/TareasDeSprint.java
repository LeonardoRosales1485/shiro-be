package com.blue.open.Code.Sprint;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pasos que comparten los casos de uso del ciclo de un sprint (T5.2): buscar el sprint con su permiso, validar las
 * tareas que se le asignan y mover una tarea de sprint escribiendo su historial.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class TareasDeSprint {

    private final SprintRepository sprintRepository;

    private final TareaRepository tareaRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    private final RegistradorDeHistorial historial;

    private final Proyector proyector;

    // Sprint vigente con EDITAR en su proyecto. Lanza REGISTRO_NO_ENCONTRADO o SIN_PERMISO
    public Sprint sprint(String uuid) {
        Sprint sprint = sprintRepository.findFirstByUuidAndStatus(uuid, 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "TareasDeSprint | sprint " + uuid));
        permisos.exigir(NivelDePermiso.EDITAR, sprint.getProyecto());
        return sprint;
    }

    // Sprint vigente y no cerrado, con EDITAR. Lanza ademas SPRINT_CERRADO
    public Sprint sprintEditable(String uuid) {
        Sprint sprint = sprint(uuid);
        if (sprint.getEstado() == EstadoDeSprint.CERRADO)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.SPRINT_CERRADO, "TareasDeSprint | sprint cerrado " + uuid);
        return sprint;
    }

    // Tareas vigentes del proyecto del sprint, de cualquier tipo. Lanza TAREA_FUERA_DEL_BACKLOG si alguna no lo es
    public List<Tarea> tareasDelProyecto(Sprint sprint, List<String> uuids) {
        List<Tarea> tareas = new ArrayList<>();
        for (String uuid : uuids) {
            Tarea tarea = tareaRepository.findFirstByUuidAndStatus(uuid, 0)
                    .filter(t -> t.getProyecto().getId().equals(sprint.getProyecto().getId()))
                    .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.TAREA_FUERA_DEL_BACKLOG,
                            "TareasDeSprint | tarea " + uuid).conDato("tareaUuid", uuid));
            tareas.add(tarea);
        }
        return tareas;
    }

    // Lleva la tarea al sprint recibido (null: al backlog) y escribe la fila EDITAR (campo sprint)
    public void mover(Tarea tarea, Sprint destino) {
        String anterior = tarea.getSprint() == null ? null : tarea.getSprint().getNombre();
        tarea.setSprint(destino);
        tareaRepository.save(tarea);
        historial.registrar(Historial.TAREA, tarea.getUuid(), AccionDeHistorial.EDITAR, "sprint", anterior,
                destino == null ? null : destino.getNombre());
    }

    // Proyeccion del sprint (vigente o archivado)
    public SprintDTO proyectar(String uuid) {
        return proyector.porUuid(Sprint.class, uuid, SprintDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO, "TareasDeSprint | proyectar " + uuid));
    }
}
