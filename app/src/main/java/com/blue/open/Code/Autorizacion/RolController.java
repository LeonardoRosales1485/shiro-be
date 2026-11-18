package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.DTO.RolDTO;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de roles: ABM de la entidad Rol de core-identity (receta de sm-core-maven/core-crud/docs/receta-controller.md).
 * El permiso fijo lo declara cada @Contrato; RolService agrega las reglas que dependen de los datos: ROL_CREAR o
 * ROL_EDITAR segun sea alta o edicion, escalada de permisos y alcance por empresa.
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("rol")
@RequiredArgsConstructor
public class RolController {

    private final RolService service;

    // Obtiene un rol con los permisos que otorga. Responde el RolDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-ROL-02", resumen = "Obtiene un rol con los permisos que otorga", permiso = "SYSADMIN")
    public ResponseEntity<RolDTO> obtenerPorUuid(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista los roles vigentes con filtros y orden. Responde la pagina de RolDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-ROL-01", resumen = "Lista los roles vigentes con filtros y orden", permiso = "SYSADMIN")
    public ResponseEntity<Pagina<RolDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea (sin uuid) o edita un rol y los permisos que otorga. Responde el rol guardado
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-ROL-03", resumen = "Crea o edita un rol y los permisos que otorga", permiso = "SYSADMIN")
    public ResponseEntity<RolDTO> guardar(@Valid @RequestBody RolDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Borrado logico de un rol. Responde el rol eliminado
    @DeleteMapping("/borrar/{uuid}")
    @Contrato(casoDeUso = "CU-ROL-04", resumen = "Elimina (borrado lógico) un rol", permiso = "SYSADMIN")
    public ResponseEntity<RolDTO> borrar(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.borrar(uuid), HttpStatus.OK);
    }
}
