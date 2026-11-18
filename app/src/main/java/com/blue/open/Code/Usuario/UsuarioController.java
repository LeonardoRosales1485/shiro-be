package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Service.AuthService;
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
 * Endpoints de usuarios: ABM (receta de sm-core-maven/core-crud/docs/receta-controller.md) y sesion actual. La logica
 * vive en UsuarioService (UsuarioBaseService).
 * Login JWT y tokens personales los publican core-protocolo-jwt y core-protocolo-apikey bajo el mismo prefijo
 * /usuario (core.protocolo.*.ruta).
 * Dependencias externas: la base de datos.
 */
@RestController
@RequestMapping("usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    private final AuthService authService;

    // Obtiene un usuario por su uuid. Responde el UsuarioDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-USR-01", resumen = "Obtiene un usuario por su uuid", permiso = "SYSADMIN")
    public ResponseEntity<UsuarioDTO> obtenerPorUuid(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista los usuarios con filtros y orden. Responde la pagina de UsuarioDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-USR-02", resumen = "Lista los usuarios con filtros y orden", permiso = "SYSADMIN")
    public ResponseEntity<Pagina<UsuarioDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea (sin uuid) o edita un usuario. El service exige USUARIO_CREAR o USUARIO_EDITAR
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-USR-03", resumen = "Crea o edita un usuario", permiso = "SYSADMIN")
    public ResponseEntity<UsuarioDTO> guardar(@Valid @RequestBody UsuarioDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Borrado logico de un usuario. Responde el usuario eliminado
    @DeleteMapping("/borrar/{uuid}")
    @Contrato(casoDeUso = "CU-USR-04", resumen = "Elimina (borrado lógico) un usuario", permiso = "SYSADMIN")
    public ResponseEntity<UsuarioDTO> borrar(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.borrar(uuid), HttpStatus.OK);
    }

    // Usuario de la sesion actual. Responde el UsuarioDTO
    @GetMapping("sesion")
    @Contrato(casoDeUso = "CU-USR-05", resumen = "Obtiene el usuario de la sesión actual (requiere sesión, sin permiso puntual)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<UsuarioDTO> sesion() {
        UsuarioBase usuarioActual = authService.autenticar();
        return ResponseEntity.ok(service.obtenerPorUuid(usuarioActual.getUuid()));
    }
}
