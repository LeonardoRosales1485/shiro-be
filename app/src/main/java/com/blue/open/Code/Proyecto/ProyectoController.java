package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoArchivarUseCase;
import com.blue.open.Code.Proyecto.CasosDeUso.ProyectoRestaurarUseCase;
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
 * Endpoints de proyectos: ABM (receta de core-crud/docs/receta-controller.md) sin borrar, porque nada se borra: se
 * archiva con todo lo que contiene y se restaura (adenda 4 del DAF). Crear y editar: super admin y Administradores (seccion 4 del DAF). Leer: con
 * sesion; el filtro por permisos de proyecto llega en F2 (T2.4).
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("proyecto")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService service;

    private final ProyectoArchivarUseCase archivarUseCase;

    private final ProyectoRestaurarUseCase restaurarUseCase;

    // Obtiene un proyecto con sus tableros. Responde el ProyectoDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-PRY-01", resumen = "Obtiene un proyecto con sus tableros", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<ProyectoDTO> obtenerPorUuid(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista los proyectos con filtros y orden. Responde la pagina de ProyectoDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-PRY-02", resumen = "Lista los proyectos con filtros y orden", permiso = Contrato.AUTENTICADO)
    public ResponseEntity<Pagina<ProyectoDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea (sin uuid, con sus cuatro tableros) o edita un proyecto. Responde el proyecto guardado
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-PRY-03", resumen = "Crea un proyecto con sus cuatro tableros, o edita su nombre y descripción",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<ProyectoDTO> guardar(@Valid @RequestBody ProyectoDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Archiva un proyecto con todo lo que contiene. Responde el proyecto archivado
    @PostMapping("/{uuid}/archivar")
    @Contrato(casoDeUso = "CU-PRY-04", resumen = "Archiva un proyecto con sus tableros, tareas, comentarios y relaciones",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<ProyectoDTO> archivar(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(archivarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Restaura un proyecto archivado con lo que se archivo en su misma operacion. Responde el proyecto restaurado
    @PostMapping("/{uuid}/restaurar")
    @Contrato(casoDeUso = "CU-PRY-05", resumen = "Restaura un proyecto archivado con lo que se archivó junto con él",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<ProyectoDTO> restaurar(@Descripcion("Uuid del proyecto") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(restaurarUseCase.ejecutar(uuid), HttpStatus.OK);
    }
}
