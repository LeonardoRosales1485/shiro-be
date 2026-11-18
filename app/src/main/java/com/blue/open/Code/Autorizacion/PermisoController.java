package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.DTO.PermisoDTO;
import com.blue.open.Core.Identity.Service.PermisoService;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Consulta de permisos (entidad Permiso de core-identity). Los permisos se sincronizan desde codigo al arrancar
 * (CatalogoDelProyecto), por eso solo hay endpoints de lectura. Publica la ruta /permiso.
 * Dependencias externas: la base de datos. Recibe: uuid o filtros de paginado.
 * Responde: el permiso o la pagina de permisos.
 */
@RestController
@RequestMapping("permiso")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService service;

    // Obtiene un permiso del sistema por su uuid
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-PER-02", resumen = "Obtiene un permiso del sistema", permiso = "SYSADMIN")
    public ResponseEntity<PermisoDTO> obtenerPorUuid(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista los permisos del sistema con filtros y orden
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-PER-01", resumen = "Lista los permisos del sistema con filtros y orden",
            permiso = "PERMISO_VER")
    public ResponseEntity<Pagina<PermisoDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }
}
