package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Code.ReglaPermiso.CasosDeUso.ReglaPermisoArchivarUseCase;
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
 * Endpoints de reglas de permiso (Spec 3): ABM (receta de core-crud/docs/receta-controller.md) sin borrar, y
 * archivar. Solo para el super admin y los Administradores.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("regla-permiso")
@RequiredArgsConstructor
public class ReglaPermisoController {

    private final ReglaPermisoService service;

    private final ReglaPermisoArchivarUseCase archivarUseCase;

    // Obtiene una regla de permiso. Responde el ReglaPermisoDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-RPE-01", resumen = "Obtiene una regla de permiso", permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<ReglaPermisoDTO> obtenerPorUuid(@Descripcion("Uuid de la regla") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista reglas de permiso con filtros y orden. Responde la pagina de ReglaPermisoDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-RPE-02", resumen = "Lista reglas de permiso con filtros y orden", permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<Pagina<ReglaPermisoDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea o edita una regla de permiso. Responde la regla guardada
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-RPE-03", resumen = "Crea o edita una regla de permiso de un grupo o usuario sobre un proyecto o tablero",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<ReglaPermisoDTO> guardar(@Valid @RequestBody ReglaPermisoDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Archiva una regla de permiso. Responde la regla archivada
    @PostMapping("/{uuid}/archivar")
    @Contrato(casoDeUso = "CU-RPE-04", resumen = "Archiva una regla de permiso", permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<ReglaPermisoDTO> archivar(@Descripcion("Uuid de la regla") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(archivarUseCase.ejecutar(uuid), HttpStatus.OK);
    }
}
