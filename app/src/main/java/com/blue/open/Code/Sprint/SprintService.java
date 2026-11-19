package com.blue.open.Code.Sprint;

import com.blue.open.Code.Autorizacion.ResolutorDePermisosDeRecurso;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Historial.AccionDeHistorial;
import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.RegistradorDeHistorial;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Core.Crud.AbstractService;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ABM de sprints (CU-SPR-01 a CU-SPR-03) sobre los hooks de AbstractService. Leer exige VER en el proyecto y crear o
 * editar, EDITAR. El estado no se cambia por guardar (lo cambian iniciar y cerrar) y el proyecto no cambia. Valida
 * las fechas y escribe el historial.
 * Dependencias externas: la base de datos.
 */
@Service
@RequiredArgsConstructor
public class SprintService extends AbstractService<Sprint, SprintDTO, SprintDTO> {

    private final SprintRepository sprintRepository;

    private final ResolutorDePermisosDeRecurso permisos;

    private final RegistradorDeHistorial historial;

    // Exige VER en el proyecto del sprint
    @Override
    protected void hookObtenerAuth(String uuid) {
        sprintRepository.findFirstByUuidAndStatus(uuid, 0).ifPresent(sprint -> permisos.exigir(NivelDePermiso.VER, sprint.getProyecto()));
    }

    // El listado trae solo los sprints de proyectos visibles
    @Override
    protected PedidoDePaginado hookPaginarAntesDeConsultar(PedidoDePaginado pedido) {
        permisos.restringirAProyectosVisibles(pedido, "t.proyecto.id");
        return super.hookPaginarAntesDeConsultar(pedido);
    }

    /*
     * Aplica el DTO: conserva proyecto y estado de un sprint existente, valida fin >= inicio
     * (SPRINT_FECHAS_INVALIDAS) y exige EDITAR en el proyecto. Recuerda los valores anteriores para el historial.
     */
    @Override
    protected Sprint hookGuardarAplicarDto(SprintDTO dto, Sprint modelo) {
        if (dto.getFin().isBefore(dto.getInicio()))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.SPRINT_FECHAS_INVALIDAS, "SprintService | fechas " + dto.getInicio() + " " + dto.getFin());
        boolean esAlta = modelo.getId() == null;
        Proyecto proyectoActual = modelo.getProyecto();
        EstadoDeSprint estadoActual = esAlta ? EstadoDeSprint.PLANIFICADO : modelo.getEstado();
        modelo.setValoresAnteriores(esAlta ? Map.of() : valores(modelo));
        if (!esAlta)
            dto.setProyectoUuid(proyectoActual.getUuid());
        dto.setEstado(estadoActual);
        Sprint aplicado = super.hookGuardarAplicarDto(dto, modelo);
        permisos.exigir(NivelDePermiso.EDITAR, aplicado.getProyecto());
        return aplicado;
    }

    // Guarda el sprint y escribe su historial (CREAR o EDITAR por campo)
    @Override
    protected Sprint hookGuardarEnBase(Sprint modelo) {
        boolean esAlta = modelo.getId() == null;
        Sprint guardado = super.hookGuardarEnBase(modelo);
        if (esAlta)
            historial.registrar(Historial.SPRINT, guardado.getUuid(), AccionDeHistorial.CREAR, null, null, guardado.getNombre());
        else
            historial.registrarCambios(Historial.SPRINT, guardado.getUuid(), modelo.getValoresAnteriores(), valores(guardado));
        return guardado;
    }

    // Valores del sprint que sigue el historial
    private static Map<String, String> valores(Sprint sprint) {
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("nombre", sprint.getNombre());
        valores.put("inicio", sprint.getInicio() == null ? null : sprint.getInicio().toString());
        valores.put("fin", sprint.getFin() == null ? null : sprint.getFin().toString());
        return valores;
    }
}
