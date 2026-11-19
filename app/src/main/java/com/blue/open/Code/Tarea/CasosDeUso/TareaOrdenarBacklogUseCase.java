package com.blue.open.Code.Tarea.CasosDeUso;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Tarea.Tarea;
import com.blue.open.Code.Tarea.TareaDeBacklogDTO;
import com.blue.open.Code.Tarea.TareaOrdenarBacklogRequestDTO;
import com.blue.open.Code.Tarea.TareaRepository;
import com.blue.open.Core.Crud.Excepciones.CodigoDeErrorCrud;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso: ordena el backlog de un proyecto (T5.1). Las tareas recibidas van primero, en ese orden; las demas
 * quedan despues, en su orden anterior. El orden es uno solo para todos. Exige EDITAR en el proyecto y deja una fila
 * ORDENAR_BACKLOG en el historial del proyecto.
 * Dependencias externas: la base de datos.
 * Recibe: TareaOrdenarBacklogRequestDTO. Responde: el backlog ordenado. Lanza REGISTRO_NO_ENCONTRADO, SIN_PERMISO o
 * TAREA_FUERA_DEL_BACKLOG.
 */
@Service
@RequiredArgsConstructor
public class TareaOrdenarBacklogUseCase {

    private final ProyectoRepository proyectoRepository;

    private final TareaRepository tareaRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    private final RegistradorDeHistorial historial;

    @Transactional
    public List<TareaDeBacklogDTO> ejecutar(TareaOrdenarBacklogRequestDTO dto) {
        Proyecto proyecto = proyectoRepository.findFirstByUuidAndStatus(dto.getProyectoUuid(), 0)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO,
                        "TareaOrdenarBacklogUseCase | ejecutar | proyecto " + dto.getProyectoUuid()));
        permisos.exigir(NivelDePermiso.EDITAR, proyecto);
        Map<String, Tarea> pendientes = new LinkedHashMap<>();
        for (Tarea tarea : tareaRepository.tareasDelBacklog(proyecto)) {
            pendientes.put(tarea.getUuid(), tarea);
        }
        List<Tarea> orden = new ArrayList<>();
        for (String uuid : dto.getTareas()) {
            Tarea tarea = pendientes.remove(uuid);
            if (tarea == null)
                throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.TAREA_FUERA_DEL_BACKLOG,
                        "TareaOrdenarBacklogUseCase | ejecutar | tarea " + uuid).conDato("tareaUuid", uuid);
            orden.add(tarea);
        }
        orden.addAll(pendientes.values());
        for (int i = 0; i < orden.size(); i++) {
            orden.get(i).setOrdenBacklog(i + 1L);
        }
        tareaRepository.saveAll(orden);
        historial.registrar(Historial.PROYECTO, proyecto.getUuid(), AccionDeHistorial.ORDENAR_BACKLOG, "backlog", null,
                String.join(", ", orden.stream().limit(20).map(Tarea::getClave).toList()));
        return tareaRepository.backlogDe(proyecto);
    }
}
