package com.blue.open.Code.Usuario;

import com.blue.open.Code.Usuario.CasosDeUso.UsuarioArchivarUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioAsignarGruposUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioCrearCuentaUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioGruposUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioIngresarConGoogleUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioListadoDeAdministracionUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioMisTokensUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioRestaurarUseCase;
import com.blue.open.Code.Usuario.CasosDeUso.UsuarioSesionGruposUseCase;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Service.AuthService;
import com.blue.open.Core.Identity.Service.CookiesDeSesion;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import com.blue.open.Core.Kernel.Contrato.Descripcion;
import com.blue.open.Core.Kernel.DTO.Pagina;
import com.blue.open.Core.Kernel.DTO.PedidoDePaginado;
import com.blue.open.Core.Protocolo.Jwt.AuthJwtService;
import com.blue.open.Core.Protocolo.Jwt.JwtTokenDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // Path de la cookie refresh_token: el mismo que usa el Core para /usuario (context-path /api + ruta usuario)
    private static final String RUTA_DEL_REFRESH_TOKEN = "/api/usuario";

    private final UsuarioService service;

    private final AuthService authService;

    private final UsuarioIngresarConGoogleUseCase ingresarConGoogleUseCase;

    private final AuthJwtService authJwtService;

    private final CookiesDeSesion cookiesDeSesion;

    private final UsuarioArchivarUseCase archivarUseCase;

    private final UsuarioRestaurarUseCase restaurarUseCase;

    private final UsuarioGruposUseCase gruposUseCase;

    private final UsuarioAsignarGruposUseCase asignarGruposUseCase;

    private final UsuarioSesionGruposUseCase sesionGruposUseCase;

    private final UsuarioMisTokensUseCase misTokensUseCase;

    private final UsuarioListadoDeAdministracionUseCase listadoDeAdministracionUseCase;

    private final UsuarioCrearCuentaUseCase crearCuentaUseCase;

    // Obtiene un usuario por su uuid. Responde el UsuarioDTO
    @GetMapping("/uuid/{uuid}")
    @Contrato(casoDeUso = "CU-USR-01", resumen = "Obtiene un usuario por su uuid", permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<UsuarioDTO> obtenerPorUuid(@Descripcion("Uuid del registro") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(service.obtenerPorUuid(uuid), HttpStatus.OK);
    }

    // Lista los usuarios con filtros y orden. Responde la pagina de UsuarioDTO
    @PostMapping("/paginar")
    @Contrato(casoDeUso = "CU-USR-02", resumen = "Lista los usuarios con filtros y orden", permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<Pagina<UsuarioDTO>> paginar(@RequestBody PedidoDePaginado pedido) throws Exception {
        return new ResponseEntity<>(service.paginar(pedido), HttpStatus.OK);
    }

    // Crea (sin uuid) o edita un usuario. Un Administrador no edita al super admin ni a otros Administradores
    @PostMapping("/guardar")
    @Contrato(casoDeUso = "CU-USR-03", resumen = "Crea o edita un usuario (un Administrador no edita al super admin ni a otros Administradores)",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<UsuarioDTO> guardar(@Valid @RequestBody UsuarioDTO dto) {
        return new ResponseEntity<>(service.guardar(dto), HttpStatus.OK);
    }

    // Archiva un usuario: no ingresa mas y sus tokens se desactivan. Responde el usuario archivado
    @PostMapping("/{uuid}/archivar")
    @Contrato(casoDeUso = "CU-USR-04", resumen = "Archiva un usuario: no ingresa más, sus tokens se desactivan y sus tareas conservan el responsable",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<UsuarioDTO> archivar(@Descripcion("Uuid del usuario") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(archivarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Restaura un usuario archivado. Responde el usuario restaurado
    @PostMapping("/{uuid}/restaurar")
    @Contrato(casoDeUso = "CU-USR-23", resumen = "Restaura un usuario archivado (sus tokens anteriores siguen desactivados)",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<UsuarioDTO> restaurar(@Descripcion("Uuid del usuario") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(restaurarUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Grupos de un usuario. Responde la lista de grupos
    @GetMapping("/{uuid}/grupos")
    @Contrato(casoDeUso = "CU-USR-21", resumen = "Lista los grupos de un usuario", permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<List<GrupoDeUsuarioDTO>> grupos(@Descripcion("Uuid del usuario") @PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(gruposUseCase.ejecutar(uuid), HttpStatus.OK);
    }

    // Reemplaza los grupos de un usuario. Responde los grupos que quedaron
    @PostMapping("/{uuid}/grupos")
    @Contrato(casoDeUso = "CU-USR-22", resumen = "Reemplaza los grupos de un usuario (los del sistema solo los maneja el super admin)",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<List<GrupoDeUsuarioDTO>> asignarGrupos(@Descripcion("Uuid del usuario") @PathVariable("uuid") String uuid,
            @Valid @RequestBody UsuarioAsignarGruposRequestDTO dto) {
        return new ResponseEntity<>(asignarGruposUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }

    // Usuario de la sesion actual. Responde el UsuarioDTO
    @GetMapping("sesion")
    @Contrato(casoDeUso = "CU-USR-05", resumen = "Obtiene el usuario de la sesión actual (requiere sesión, sin permiso puntual)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<UsuarioDTO> sesion() {
        UsuarioBase usuarioActual = authService.autenticar();
        return ResponseEntity.ok(service.obtenerPorUuid(usuarioActual.getUuid()));
    }

    // Grupos del usuario de la sesion y si es administrador. Responde UsuarioSesionGruposResponseDTO
    @GetMapping("sesion/grupos")
    @Contrato(casoDeUso = "CU-USR-24", resumen = "Grupos del usuario de la sesión y si es administrador (requiere sesión, sin permiso puntual)",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<UsuarioSesionGruposResponseDTO> sesionGrupos() {
        return ResponseEntity.ok(sesionGruposUseCase.ejecutar());
    }

    // Ingresa con el ID token de Google. Publico. Responde el JWT y su refresh token, como iniciar-sesion, y los
    // setea como cookies si estan habilitadas
    @PostMapping("ingresar-con-google")
    @Contrato(casoDeUso = "CU-USR-20", resumen = "Ingresa con una cuenta de Google de la organización y responde un JWT y su refresh token (también como cookies)",
            permiso = Contrato.PUBLICO)
    public ResponseEntity<JwtTokenDTO> ingresarConGoogle(@Valid @RequestBody UsuarioIngresarConGoogleRequestDTO cuerpo) {
        JwtTokenDTO tokens = ingresarConGoogleUseCase.ejecutar(cuerpo.getIdToken());
        ResponseEntity.BodyBuilder respuesta = ResponseEntity.ok();
        if (cookiesDeSesion.isHabilitada()) {
            respuesta.header(HttpHeaders.SET_COOKIE, cookiesDeSesion.crear(CookiesDeSesion.ACCESS_TOKEN, tokens.getToken(),
                    authJwtService.getVigenciaDelToken()));
            respuesta.header(HttpHeaders.SET_COOKIE, cookiesDeSesion.crear(CookiesDeSesion.REFRESH_TOKEN,
                    tokens.getRefreshToken(), authJwtService.getVigenciaDelRefreshToken(), RUTA_DEL_REFRESH_TOKEN));
        }
        return respuesta.body(tokens);
    }

    // Mis tokens de acceso, tambien los revocados, con su ultimo uso. Responde la lista, los mas nuevos primero
    @GetMapping("mis-tokens")
    @Contrato(casoDeUso = "CU-USR-25", resumen = "Mis tokens de acceso, también los revocados, con su creación, su revocación y su último uso",
            permiso = Contrato.AUTENTICADO)
    public ResponseEntity<List<UsuarioTokenResponseDTO>> misTokens() {
        return new ResponseEntity<>(misTokensUseCase.ejecutar(), HttpStatus.OK);
    }

    // Listado de administracion: usuarios vigentes y archivados con su ingreso y sus grupos. Responde la lista por nombre
    @GetMapping("administracion")
    @Contrato(casoDeUso = "CU-USR-26", resumen = "Usuarios vigentes y archivados con su forma de ingreso y sus grupos, para la administración",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<List<UsuarioAdministracionResponseDTO>> listadoDeAdministracion() {
        return new ResponseEntity<>(listadoDeAdministracionUseCase.ejecutar(), HttpStatus.OK);
    }

    // Crea una cuenta de Shiro con contrasena generada y sus grupos. Responde la cuenta con la contrasena, una sola vez
    @PostMapping("crear-cuenta")
    @Contrato(casoDeUso = "CU-USR-27", resumen = "Crea una cuenta de Shiro con sus grupos; la contraseña inicial la genera el backend y se responde una sola vez",
            permiso = {"SYSADMIN", "ADMIN"})
    public ResponseEntity<UsuarioCrearCuentaResponseDTO> crearCuenta(@Valid @RequestBody UsuarioCrearCuentaRequestDTO dto) {
        return new ResponseEntity<>(crearCuentaUseCase.ejecutar(dto), HttpStatus.OK);
    }
}
