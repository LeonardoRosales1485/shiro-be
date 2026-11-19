package com.blue.open.Code.Sprint.CasosDeUso;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivo;
import com.blue.open.Code.OperacionDeArchivo.OperacionDeArchivoRepository;
import com.blue.open.Code.OperacionDeArchivo.TipoDeOperacionDeArchivo;
import com.blue.open.Code.Sprint.EstadoDeSprint;
import com.blue.open.Code.Sprint.Sprint;
import com.blue.open.Code.Sprint.SprintDTO;
import com.blue.open.Code.Sprint.SprintRepository;
import com.blue.open.Code.Sprint.TareasDeSprint;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Caso de uso: archiva un sprint (T5.2 y Spec 7). Un sprint activo no se archiva: se cierra primero. Las tareas de un
 * sprint planificado vuelven al backlog; las de un sprint cerrado quedan registradas en el. Exige EDITAR.
 * Dependencias externas: la base de datos.
 * Recibe: uuid del sprint. Responde: SprintDTO archivado. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO o
 * SPRINT_ACTIVO_NO_SE_ARCHIVA.
 */
@Service
@RequiredArgsConstructor
public class SprintArchivarUseCase {

    private final TareasDeSprint tareasDeSprint;

    private final SprintRepository sprintRepository;

    private final TareaRepository tareaRepository;

    private final OperacionDeArchivoRepository operaciones;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final RegistradorDeHistorial historial;

    @Transactional
    public SprintDTO ejecutar(String uuid) {
        Sprint sprint = tareasDeSprint.sprint(uuid);
        if (sprint.getEstado() == EstadoDeSprint.ACTIVO)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.SPRINT_ACTIVO_NO_SE_ARCHIVA, "SprintArchivarUseCase | " + uuid);
        if (sprint.getEstado() == EstadoDeSprint.PLANIFICADO) {
            for (Tarea tarea : tareaRepository.vigentesDelSprint(sprint)) {
                tareasDeSprint.mover(tarea, null);
            }
        }
        Long usuario = usuarioActual.getIdUsuarioActual();
        Long operacion = operaciones.save(new OperacionDeArchivo(TipoDeOperacionDeArchivo.ARCHIVAR, Historial.SPRINT, uuid, usuario)).getId();
        sprint.archivar(usuario, operacion);
        sprintRepository.save(sprint);
        historial.registrar(Historial.SPRINT, uuid, AccionDeHistorial.ARCHIVAR, null, null, sprint.getNombre(), operacion);
        return tareasDeSprint.proyectar(uuid);
    }
}
