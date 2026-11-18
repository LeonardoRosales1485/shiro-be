package com.blue.open.Code.Empresa;

import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import com.blue.open.Core.Multiempresa.DTO.ContextoDeEmpresaDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de empresas: ABM (receta de sm-core-maven/core-crud/docs/receta-controller.md) y GET /contexto, que informa el
 * modo multi-empresa y la empresa resuelta para la peticion. La logica vive en EmpresaService (EmpresaBaseService).
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService service;

    // Obtiene una empresa por su uuid. Responde el EmpresaDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-EMP-02", resumen = "Obtiene una empresa", permiso = "SYSADMIN")
    public ResponseEntity<EmpresaDTO> obtenerPorUuid(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista las empresas vigentes con filtros y orden. Responde la pagina de EmpresaDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-EMP-01", resumen = "Lista las empresas vigentes con filtros y orden", permiso = "SYSADMIN")
    public ResponseEntity<Pagina<EmpresaDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea (sin uuid) o edita una empresa. Responde la empresa guardada
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-EMP-03", resumen = "Crea o edita una empresa", permiso = "SYSADMIN")
    public ResponseEntity<EmpresaDTO> guardar(@Valid @RequestBody EmpresaDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Borrado logico de una empresa. Responde la empresa eliminada
    @DeleteMapping("/borrar/{uuid}")
    @Contrato(casoDeUso = "CU-EMP-04", resumen = "Elimina (borrado lógico) una empresa", permiso = "SYSADMIN")
    public ResponseEntity<EmpresaDTO> borrar(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.borrar(uuid), HttpStatus.OK);
    }

    // Modo multi-empresa / mono-empresa y empresa resuelta para la peticion. Si hay sesion, incluye empresaActual
    @GetMapping("/contexto")
    @Contrato(casoDeUso = "CU-EMP-05", resumen = "Informa el modo multi-empresa y la empresa resuelta para la petición",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<ContextoDeEmpresaDTO<EmpresaDTO>> obtenerContexto() {
        return ResponseEntity.ok(service.obtenerContexto());
    }
}
