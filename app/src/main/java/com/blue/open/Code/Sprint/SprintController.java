package com.blue.open.Code.Sprint;

import com.blue.open.Code.Sprint.CasosDeUso.SprintArchivarUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintAsignarUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintCerrarUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintCierreUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintIniciarUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintQuitarUseCase;
import com.blue.open.Code.Sprint.CasosDeUso.SprintRestaurarUseCase;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de sprints (Spec 3, T5.2): ABM sin borrar, asignar y quitar tareas, iniciar, cerrar y archivar. Leer
 * exige VER en el proyecto y el resto, EDITAR.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("sprint")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService service;

    private final SprintAsignarUseCase asignarUseCase;

    private final SprintQuitarUseCase quitarUseCase;

    private final SprintIniciarUseCase iniciarUseCase;

    private final SprintCerrarUseCase cerrarUseCase;

    private final SprintArchivarUseCase archivarUseCase;

    private final SprintCierreUseCase cierreUseCase;

    private final SprintRestaurarUseCase restaurarUseCase;

    // Obtiene un sprint. Responde el SprintDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-SPR-01", resumen = "Obtiene un sprint", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintDTO> obtenerPorUuid(@Descripcion("Uuid del sprint") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista sprints con filtros y orden. Responde la pagina de SprintDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-SPR-02", resumen = "Lista sprints con filtros y orden", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<Pagina<SprintDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea un sprint planificado o edita su nombre y fechas. Responde el sprint guardado
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-SPR-03", resumen = "Crea un sprint planificado o edita su nombre y fechas", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintDTO> guardar(@Valid @RequestBody SprintDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Asigna tareas del backlog al sprint. Responde el sprint
    @PostMapping("/{uuid}/asignar")
    @Contrato(casoDeUso = "CU-SPR-04", resumen = "Asigna tareas al sprint", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintDTO> asignar(@Descripcion("Uuid del sprint") @PathVariable("uuid") String uuid,
            @Valid @RequestBody SprintTareasRequestDTO dto) {
        return new ResponseEntity<>(asignarUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Quita tareas del sprint (vuelven al backlog). Responde el sprint
    @PostMapping("/{uuid}/quitar")
    @Contrato(casoDeUso = "CU-SPR-05", resumen = "Quita tareas del sprint; vuelven al backlog", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintDTO> quitar(@Descripcion("Uuid del sprint") @PathVariable("uuid") String uuid,
            @Valid @RequestBody SprintTareasRequestDTO dto) {
        return new ResponseEntity<>(quitarUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Inicia el sprint (uno solo activo por proyecto). Responde el sprint activo
    @PostMapping("/{uuid}/iniciar")
    @Contrato(casoDeUso = "CU-SPR-06", resumen = "Inicia un sprint planificado; solo puede haber uno activo por proyecto",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintDTO> iniciar(@Descripcion("Uuid del sprint") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(iniciarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Cierra el sprint activo y manda lo pendiente al backlog o a otro sprint. Responde el sprint cerrado
    @PostMapping("/{uuid}/cerrar")
    @Contrato(casoDeUso = "CU-SPR-07", resumen = "Cierra el sprint activo; lo pendiente va al backlog o a otro sprint planificado",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintDTO> cerrar(@Descripcion("Uuid del sprint") @PathVariable("uuid") String uuid,
            @Valid @RequestBody SprintCerrarRequestDTO dto) {
        return new ResponseEntity<>(cerrarUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Archiva un sprint que no esta activo. Responde el sprint archivado
    @PostMapping("/{uuid}/archivar")
    @Contrato(casoDeUso = "CU-SPR-08", resumen = "Archiva un sprint planificado o cerrado (uno activo se cierra primero)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintDTO> archivar(@Descripcion("Uuid del sprint") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(archivarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Cierre del sprint: quien lo cerro, cuando y que paso con cada tarea. Responde el cierre
    @GetMapping("/{uuid}/cierre")
    @Contrato(casoDeUso = "CU-SPR-09", resumen = "Cierre de un sprint: quién lo cerró, cuándo y el estado y destino de cada tarea al cerrar (exige VER)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintCierreResponseDTO> cierre(@Descripcion("Uuid del sprint") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(cierreUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Restaura un sprint archivado. Responde el sprint restaurado
    @PostMapping("/{uuid}/restaurar")
    @Contrato(casoDeUso = "CU-SPR-10", resumen = "Restaura un sprint archivado con el estado que tenía (exige EDITAR)", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<SprintDTO> restaurar(@Descripcion("Uuid del sprint") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(restaurarUseCase.ejecutar(uuid), HttpStatus.OK);
    }
}
