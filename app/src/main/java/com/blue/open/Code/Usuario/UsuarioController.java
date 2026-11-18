package com.blue.open.Code.Usuario;

import com.blue.open.Code.Empresa.EmpresaDTO;
import com.blue.open.Core.Identity.DTO.CambiarContrasenaDTO;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Excepciones.CodigoDeErrorIdentity;
import com.blue.open.Core.Identity.Service.AuthService;
import com.blue.open.Core.Integracion.Captcha.CaptchaService;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.RespuestaApiDTO;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Multiempresa.DTO.RegistroPublicoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints de usuarios: ABM (receta de sm-core-maven/core-crud/docs/receta-controller.md), sesion actual, verificacion,
 * recuperacion de contrasena y altas publicas. La logica vive en UsuarioService (UsuarioBaseMultiempresaService).
 * Login JWT y tokens personales los publican core-protocolo-jwt y core-protocolo-apikey bajo el mismo prefijo
 * /usuario (core.protocolo.*.ruta).
 * Dependencias externas: hCaptcha (altas publicas) y la base de datos.
 */
@RestController
@RequestMapping("usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    private final AuthService authService;

    private final CaptchaService captchaService;

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

    // Crea (sin uuid) o edita un usuario. El service exige USUARIO_CREAR o USUARIO_EDITAR y el alcance por empresa
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

    // Usuario de la sesion actual (valida tambien la empresa del header). Responde el UsuarioDTO
    @GetMapping("sesion")
    @Contrato(casoDeUso = "CU-USR-05", resumen = "Obtiene el usuario de la sesión actual (requiere sesión, sin permiso puntual)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<UsuarioDTO> sesion() {
        UsuarioBase usuarioActual = authService.autenticar();
        return ResponseEntity.ok(service.obtenerPorUuid(usuarioActual.getUuid()));
    }

    // Verifica el codigo de confirmacion de un usuario pendiente. Recibe uuid y codigo
    @PostMapping("publico/verificar")
    @Contrato(casoDeUso = "CU-USR-06", resumen = "Verifica el código de confirmación de un usuario pendiente y lo activa",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<Void> verificarCodigo(@Valid @RequestBody UsuarioVerificarCodigoRequestDTO cuerpo) {
        service.verificarCodigo(cuerpo.getUuid(), cuerpo.getCodigo());
        return ResponseEntity.ok().build();
    }

    // Reenvia por email un nuevo codigo de verificacion. Recibe uuid y email
    @PostMapping("publico/reenviar-codigo")
    @Contrato(casoDeUso = "CU-USR-07", resumen = "Reenvía por email un nuevo código de verificación",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<Void> reenviarCodigo(@RequestBody UsuarioDTO usuarioDTO) {
        if (usuarioDTO.getUuid() == null || usuarioDTO.getEmail() == null) {
            throw new ExcepcionDeApi(CodigoDeErrorIdentity.DATOS_INCOMPLETOS,
                    "UsuarioController | reenviarCodigo | faltan el uuid o el email");
        }

        service.reenviarCodigo(usuarioDTO.getUuid(), usuarioDTO.getEmail());
        return ResponseEntity.ok().build();
    }

    // Envia por email un codigo para recuperar la contrasena. Recibe el email
    @PostMapping("publico/recuperar-contrasena")
    @Contrato(casoDeUso = "CU-USR-08", resumen = "Envía por email un código para recuperar la contraseña",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<Map<String, String>> recuperarContrasena(
            @Valid @RequestBody UsuarioRecuperarContrasenaRequestDTO cuerpo) {
        service.recuperarContrasena(cuerpo.getEmail());
        return ResponseEntity.ok(Map.of("mensaje", "Mail enviado correctamente"));
    }

    // Cambia la contrasena con el codigo de recuperacion recibido por email
    @PostMapping("publico/cambiar-contrasena")
    @Contrato(casoDeUso = "CU-USR-09", resumen = "Cambia la contraseña con el código de recuperación recibido por email",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<RespuestaApiDTO> cambiarContrasena(@Valid @RequestBody CambiarContrasenaDTO cuerpo) {
        service.cambiarContrasena(cuerpo);
        return ResponseEntity.ok(new RespuestaApiDTO(HttpStatus.OK.value(), "Contraseña cambiada correctamente", null));
    }

    // Registra un usuario con su empresa (queda pendiente de verificacion). Valida el captcha (hCaptcha)
    @PostMapping("publico/registro")
    @Contrato(casoDeUso = "CU-USR-12", resumen = "Registra un usuario con su empresa; queda pendiente de verificación por email",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<UsuarioDTO> registroPublico(
            @Valid @RequestBody RegistroPublicoRequest<UsuarioDTO, EmpresaDTO> request,
            @Descripcion("Token de hCaptcha. Obligatorio si el proyecto valida captcha")
            @RequestHeader(name = "X-Captcha-Token", required = false) String captchaToken) {
        captchaService.verificar(captchaToken);
        return ResponseEntity.ok(service.registroPublico(request));
    }

    // Vincula al usuario logeado a la empresa del header con el rol por defecto de esa empresa.
    // Conserva el prefijo "publico/" por compatibilidad de ruta, pero requiere sesion (desde Core 2.0.1)
    @PostMapping("publico/vincular/{usuarioUuid}")
    @Contrato(casoDeUso = "CU-USR-13",
            resumen = "Vincula al usuario de la sesión a la empresa del header con su rol por defecto (requiere sesión)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<UsuarioDTO> vincular(
            @Descripcion("Uuid de la empresa a la que se vincula el usuario")
            @RequestHeader(value = "X-Empresa-Uuid", required = false) String empresaUuid,
            @Descripcion("Uuid del usuario logeado") @PathVariable String usuarioUuid) {
        return ResponseEntity.ok(service.vincular(empresaUuid, usuarioUuid));
    }

    // Registra un cliente en la empresa del header con su rol por defecto. Valida el captcha (hCaptcha)
    @PostMapping("publico/registro-cliente")
    @Contrato(casoDeUso = "CU-USR-14", resumen = "Registra un cliente en la empresa del header con su rol por defecto",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<UsuarioDTO> registroPublicoDeCliente(
            @Descripcion("Uuid de la empresa en la que se registra el cliente")
            @RequestHeader(value = "X-Empresa-Uuid", required = false) String empresaUuid,
            @Valid @RequestBody UsuarioDTO usuarioDTO,
            @Descripcion("Token de hCaptcha. Obligatorio si el proyecto valida captcha")
            @RequestHeader(name = "X-Captcha-Token", required = false) String captchaToken) {
        captchaService.verificar(captchaToken);
        return ResponseEntity.ok(service.registroPublicoDeCliente(empresaUuid, usuarioDTO));
    }
}
