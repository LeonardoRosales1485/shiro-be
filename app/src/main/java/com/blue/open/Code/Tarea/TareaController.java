package com.blue.open.Code.Tarea;

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
 * Endpoints de tareas: ABM (receta de core-crud/docs/receta-controller.md) sin borrar, porque nada se borra
 * (archivar y restaurar llegan en T1.8). Por ahora alcanza con sesion; los permisos por proyecto (ver, editar)
 * llegan en F2 (T2.4).
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("tarea")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService service;

    // Obtiene una tarea. Responde el TareaDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-TAR-01", resumen = "Obtiene una tarea", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<TareaDTO> obtenerPorUuid(@Descripcion("Uuid de la tarea") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista las tareas con filtros y orden. Responde la pagina de TareaDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-TAR-02", resumen = "Lista las tareas con filtros y orden", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<Pagina<TareaDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea una tarea (sin uuid; alcanza con tablero, tipo y titulo) o la edita. Responde la tarea guardada
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-TAR-03",
            resumen = "Crea una tarea con tipo y título en el primer estado de su tablero, o edita sus datos",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<TareaDTO> guardar(@Valid @RequestBody TareaDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }
}
