# Autenticación

**Clase**: `com.blue.open.Core.Identity.Service.AuthService` (módulo `core-identity`)

Punto de entrada único para autenticación/autorización. No usar directamente los servicios de cada protocolo (`AuthJwtService`, `AuthTokenService`, `AuthSsoService`, etc.): cada módulo `core-protocolo-*` registra una `EstrategiaDeAutenticacion` y `AuthService` recorre las habilitadas en `auth.protocolos` (ej. `jwt,basic,tokens`; los valores no se traducen). Cada estrategia resuelve el usuario con `getUsuario()`.

La empresa actual no la resuelve `AuthService` sino `ContextoDeEmpresa` (módulo `core-multiempresa`).

## Cómo usarlo

```java
@Service
@RequiredArgsConstructor
public class MiUseCase {

    private final AuthService authService;
}
```

La dependencia entra por constructor: campo `private final` y `@RequiredArgsConstructor` en la clase. Ver [`docs/rules/inyeccion-de-dependencias.md`](../rules/inyeccion-de-dependencias.md).

## Permiso de un endpoint: `@Contrato`

El permiso fijo de un endpoint no se valida con `AuthService`: se declara en el `permiso` de su `@Contrato` (`Contrato.PUBLICO`, `Contrato.AUTENTICADO` o nombres de roles/permisos) y lo hace cumplir `InterceptorDeContrato` antes de ejecutar el método. Con `core-identity`, el puerto `AutorizadorDeEndpoint` lo implementa `AutorizadorPorRolesYPermisos`, que usa `AuthService`. Ver [`docs/arquitectura/testing/README.md`](../arquitectura/testing/README.md) y la [receta del controller](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/receta-controller.md).

```java
@PostMapping("/guardar")
@Contrato(casoDeUso = "CU-CON-04", resumen = "Crea o edita configuraciones", permiso = "CONFIGURACION_CREAR")
```

## Métodos principales

- `getActualONull()` / `getActualObligatorio()` — usuario logeado actual, o `null` / `401 NO_AUTENTICADO` ("Usuario no identificado") si no hay sesión.
- `autenticar()` — igual que `getActualObligatorio()`, y además ejecuta el `ValidadorDeContexto` (con `core-multiempresa`: que el usuario pertenezca a la empresa del header `X-Empresa-Uuid` o a la empresa de sistema). Lo usa `AutorizadorPorRolesYPermisos` antes de chequear un rol o permiso; un endpoint `AUTENTICADO` que necesita el usuario con el contexto validado lo llama en su cuerpo.
- `tienePermisos(List<Enum<?>> required)` — `true`/`false` sin lanzar excepción, también sin sesión (devuelve `false`). Es la única API programática de autorización: solo para reglas que dependen de los datos (alta vs. edición, alcance por empresa, dueño del registro) o para endpoints que responden distinto según el permiso. Si corta, el llamador lanza la excepción:
  ```java
  if (!authService.tienePermisos(List.of(PermisoBaseEnum.USUARIO_EDITAR)))
      throw new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO);
  ```
- `tieneAlgunRolOPermisoPorNombre(UsuarioBase user, List<String> roles, List<String> permisos)` — como `tienePermisos`, pero por nombre y para un usuario dado. Lo usa el autorizador de `@Contrato`.
- `esSysAdmin()` / `esSysAdmin(UsuarioBase usuario)` — si el usuario tiene el rol SYSADMIN en la empresa de sistema.
- `getPermisosAgregados(UsuarioBase usuario)` — set de nombres de permisos del usuario en la empresa del contexto (más los de la empresa de sistema).

## Empresa actual (`core-multiempresa`)

```java
private final ContextoDeEmpresa contextoDeEmpresa;
```

- `getEmpresaActualObligatoria()` — empresa del header `X-Empresa-Uuid`, validando que el usuario pertenezca a ella o a la empresa de sistema.
- `getIdEmpresaActualONull()` / `getUuidDeLaRequest()` — sin validar acceso.
- `isMultiempresaHabilitada()` — valor de `core.multiempresa.habilitada`.

## Tokens y JWT

- Tokens de acceso personales: `AuthTokenService` (`core-protocolo-apikey`: `crearAccessToken`, `borrarAccessToken`, `getTokensDelUsuario`), expuestos por `AccessTokenController` (prefijo `core.protocolo.apikey.ruta`, default `usuario`). Entidad `AccessToken` (tabla `auth_access_tokens`: `nombre`, `codigo` (`@NoProyectable`), `activo`, `id_usuario`), repositorio `AccessTokenRepository`.
- JWT: `AuthJwtService` (`core-protocolo-jwt`: `generarToken`, `renovarToken`, `getUsuarioDelToken`, `autenticarConContrasena`), expuesto por `JwtLoginController` (prefijo `core.protocolo.jwt.ruta`, default `usuario`). La clave de firma es la propiedad `jwt.secreto`. Refresh tokens: entidad `JwtRefreshToken` (tabla `auth_jwt_refresh_tokens`: `token`, `fecha_de_vencimiento`, `id_usuario`).
- El JWT emitido lleva los claims `sub` (uuid del usuario) y `type` = `access`, y los valores de `auth.protocolos` (`jwt`, `basic`, `tokens`, `sso`, `malvinas`) no se traducen: un JWT, un token de acceso o un refresh token emitidos antes de pasar al español siguen validando, siempre que `jwt.secreto` conserve el valor que tenía `jwt.secret`.
- SSO: `AuthSsoService` (`core-protocolo-sso`, propiedades `auth.sso.url` y `auth.sso.clientId`) resuelve el usuario local con `usuarioDesdeSso(usuarioSSO)`; `AuthMalvinasService` hace lo mismo para Malvinas (`auth.malvinas.url`). La integración vieja `SSOAuthService` (propiedades `sso.url` y `sso.clientId`) ya no existe.
- Convivencia de JWT propio y Malvinas (desde Core 3.4.0): con `auth.protocolos=malvinas,jwt,tokens` los dos llegan como `Authorization: Bearer`. Cada estrategia lee el `alg` de la cabecera sin verificarla y solo resuelve el suyo: `AuthJwtService` el `HS256` (`getUsuario` y `getUsuarioDelToken`) y `AuthMalvinasService` el `ES256`. Con otro `alg`, o una cabecera ilegible, la estrategia responde `null` y no lanza error, así el orden de `auth.protocolos` no importa. Un `HS256` con otra firma o vencido sigue respondiendo `TOKEN_INVALIDO` / `TOKEN_VENCIDO`. Un Bearer que ninguna estrategia reconoce responde `401 NO_AUTENTICADO`.
- Política de acceso de Malvinas: antes de crear o guardar el usuario, `AuthMalvinasService` llama a `PoliticaDeAccesoExterno.validar("malvinas", legajo, usuarioVigente)` (`core-identity`), con el usuario vigente de ese legajo o `null` si todavía no existe. Si la política lanza `ExcepcionDeApi`, el error sale tal cual (no se envuelve en `SSO_AUTENTICACION_FALLIDA`) y no se crea ni se modifica ninguna fila. La implementación por defecto no valida nada; el proyecto la reemplaza publicando su bean.
- `POST /usuario/refresh-token` solo renueva el token de un usuario en `status = 0`: un usuario dado de baja o inexistente recibe `REFRESH_TOKEN_INVALIDO`.

| Ruta | Body | Respuesta |
|---|---|---|
| `POST /usuario/iniciar-sesion` | `UsuarioYContrasenaDTO`: `{nombreDeUsuario, contrasena}` | `JwtTokenDTO`: `{token, refreshToken}` |
| `POST /usuario/refresh-token` | `{refreshToken}` (opcional si viene la cookie `refresh_token`) | `JwtTokenDTO`: `{token, refreshToken}` |
| `POST /usuario/cerrar-sesion` | `{refreshToken}` opcional | `204`; borra el refresh token y las cookies |
| `POST /usuario/crear-token` | `{nombre}` | `AccessTokenDTO`: `{uuid, status, fechaDeCreacion, usuarioUuid, nombre, codigo, activo}` (única vez que se ve el `codigo` al crear) |
| `GET /usuario/tokens` | — | lista de `AccessTokenDTO` vigentes del usuario de la sesión |
| `DELETE /usuario/borrar-token/{uuid}` | — | `AccessTokenDTO` del token borrado |

### Cookies de sesión (desde Core 3.6.0)

- El login y el refresh de `core-protocolo-jwt` mandan además `Set-Cookie` de `access_token` y `refresh_token` (`HttpOnly`, `Secure`, `SameSite=Lax`); `access_token` con `Path=/` y `refresh_token` con el `Path` del controller de login (`server.servlet.context-path` + `core.protocolo.jwt.ruta`, por ejemplo `/api/usuario`, o `core.protocolo.cookie.path-refresh-token` si un proxy agrega un prefijo), para que solo viaje a `refresh-token` y `cerrar-sesion`. El body sigue igual.
- `AuthJwtService` y `AuthMalvinasService` leen el token de la cookie `access_token` solo si el request no trae el header `Authorization` (con cualquier esquema). Cada uno sigue resolviendo solo su `alg` (`HS256` / `ES256`), así que comparten el nombre de la cookie sin pisarse. Malvinas solo lee: el IDP emite el token.
- `POST /usuario/refresh-token` y `POST /usuario/cerrar-sesion` aceptan el refresh token del body o, si no viene, de la cookie `refresh_token`.
- Protección CSRF: una cookie en un POST, PUT, PATCH o DELETE desde un `Origin` distinto del host del request y que no está en `core.protocolo.cookie.origenes-permitidos` responde `403 ORIGEN_NO_PERMITIDO`. Con el header `Authorization` no se valida.
- El front en otro host manda `credentials: 'include'`, y el proyecto publica su `CorsConfigurationSource` con sus orígenes (el CORS por defecto acepta cualquiera). Propiedades en [`docs/arquitectura/core/README.md`](../arquitectura/core/README.md).

Las rutas viejas (`POST /usuario/login`, `/usuario/create-token`, `/usuario/delete-token/{uuid}`) ya no existen y responden `404`.

## Roles y permisos

`tienePermisos(List<Enum<?>> required)` acepta una lista mezclando roles (`RolBaseEnum`/`RolEnum`, implementan `IRol`) y permisos (`PermisoBaseEnum`/`PermisoEnum`, implementan `IPermiso`). Alcanza con que el usuario cumpla uno de los dos tipos, no hace falta que cumpla todos.

- Un permiso implementa `IPermiso`: `getNombre()` devuelve el valor persistido (`user.view`, `configuration.view`), que no se traduce. Un rol implementa `IRol`: `name()` es el nombre persistido (`SYSADMIN`, `ADMIN`) y `getPermisos()` los permisos que otorga.
- `PermisoBaseEnum` trae los permisos del Core: `PERMISO_VER/CREAR/EDITAR/BORRAR`, `EMPRESA_NO_REQUERIDA`, `EMPRESA_VER/CREAR/EDITAR/BORRAR`, `USUARIO_VER/CREAR/EDITAR/BORRAR` y `ROL_VER/CREAR/EDITAR/BORRAR`. En `@Contrato(permiso = ...)` se usa el nombre de la constante (`"USUARIO_VER"`), no el valor (`user.view`).
- Los permisos propios del proyecto se registran con `RegistroDePermisos.registrar(values())` (ver `Code/Autorizacion/PermisoEnum`): así `RolBaseEnum.SYSADMIN` y `ADMIN` los suman a los suyos.
- Al arrancar, `InicializadorDeBaseService.inicializar()` sincroniza en la base los permisos y roles declarados en código (los del Core y los del `CatalogoDeAutorizacion` del proyecto) y crea el usuario inicial.
- Entidades: `Permiso` (tabla `auth_permisos`: `nombre`, `descripcion`) y `Rol` (tabla `auth_roles`: `nombre`, `por_defecto`, `id_empresa`), relacionadas por `auth_roles_permisos` (`id_rol`, `id_permiso`). Las lee y escribe `RolBaseService` (con `core-multiempresa`, `RolBaseMultiempresaService`) y `PermisoService`; `Code/Autorizacion` las publica en `/rol` y `/permiso` (`/permiso/paginar`, `/permiso/uuid/{uuid}`), con el JSON `nombre`, `porDefecto` y `permisos`.
- `RolBaseService<D extends RolDTO>` y `RolBaseMultiempresaService<D extends RolDTO>` son genéricos sobre el DTO (desde Core 3.5.0): un proyecto que publica un DTO propio (por ejemplo `RolDTO` más un dato calculado) declara `extends RolBaseService<SuDTO>` y obtiene, pagina, guarda y borra con esa clase, sin cast. Sin DTO propio, `extends RolBaseService<RolDTO>`.
- `RolBaseService.puedeAsignarRol(rol, usuarioActual, permisosDelSolicitante)` decide qué roles ve y puede asignar un usuario; el proyecto lo amplía sobreescribiéndolo (ver `Code/Autorizacion/RolService`).

## Usuarios y flujos públicos

- Entidad base: `UsuarioBase` (`core-identity`, `@MappedSuperclass`). El proyecto la extiende con su `@Entity` sobre la tabla `auth_usuarios` (`Code/Usuario/Usuario`). Campos: `nombreDeUsuario`, `contrasena` (`@NoProyectable`), `nombre`, `email`, `telefono`, `nombreDeDepartamento`, `nombreDeGrupo`, `ssoUuid`, `malvinasLegajo` y el código de verificación (`codigoDeVerificacion`, `vencimientoDelCodigoDeVerificacion`, `ultimoEnvioDelCodigoDeVerificacion`, los tres `@NoProyectable`). Columnas: `nombre_de_usuario`, `contrasena`, `nombre`, `email`, `telefono`, `nombre_de_departamento`, `nombre_de_grupo`, `codigo_de_verificacion`, `vencimiento_del_codigo_de_verificacion`, `ultimo_envio_del_codigo_de_verificacion`.
- DTO base: `UsuarioBaseDTO`, con el JSON `nombreDeUsuario`, `contrasena` (solo se envía al guardar; nunca se responde), `email` y `telefono`. `SanitizadorDeLogs` tapa en los logs toda clave que contenga `contrasena` (también `contrasenaActual` y `contrasenaNueva`) y `codigoDeVerificacion`.
- Service base: `UsuarioBaseService` (con `core-multiempresa`, `UsuarioBaseMultiempresaService`). Hashea la contraseña al guardar y resuelve los flujos públicos: `verificarCodigo(uuid, codigo)`, `reenviarCodigo(uuid, email)`, `recuperarContrasena(email)` y `cambiarContrasena(CambiarContrasenaDTO)`. El proyecto arma los mails sobreescribiendo `hookObtenerNotificacionPorMail(proposito, usuario, codigo)`, con `proposito` = `PROPOSITO_REGISTRO` o `PROPOSITO_RECUPERAR_CONTRASENA` (ver `Code/Usuario/UsuarioService`).
- Códigos de recuperación: entidad `CodigoDeSeguridad` (tabla `codigos_de_seguridad`: `codigo`, `proposito`, `usado`, `fecha_de_emision`, `id_usuario`), repositorio `CodigoDeSeguridadRepository`. Los valores de `proposito` (`FORGOT_PASSWORD`, `REGISTRATION`) quedan en inglés: son valores persistidos.
- Puerto `UsuarioRepositorio` (`porNombreDeUsuarioActivo`, `porEmailActivo`, ...): el Core busca usuarios sin conocer la entidad del proyecto.
- Usuario inicial: `core.identity.usuario-inicial.nombre-de-usuario` y `core.identity.usuario-inicial.contrasena` (sin las dos, no se crea).

Rutas de `Code/Usuario/UsuarioController`:

| Ruta | Body | Respuesta |
|---|---|---|
| `GET /usuario/sesion` | — | `UsuarioDTO` del usuario de la sesión |
| `POST /usuario/publico/verificar` | `{uuid, codigo}` | `200` sin cuerpo |
| `POST /usuario/publico/reenviar-codigo` | `{uuid, email}` | `200` sin cuerpo |
| `POST /usuario/publico/recuperar-contrasena` | `{email}` | `{mensaje}` |
| `POST /usuario/publico/cambiar-contrasena` | `CambiarContrasenaDTO`: `{codigo, email, contrasena}` | `RespuestaApiDTO` (`{status, mensaje, datos}`) |

Las rutas viejas (`GET /usuario/login` como sesión actual y `/usuario/public/verify`, `resend-code`, `forgot-password`, `change-password`) ya no existen y responden `404`.

## Empresas, roles por empresa y altas públicas (`core-multiempresa`)

- Entidad base: `EmpresaBase` (`@MappedSuperclass`). El proyecto la extiende con su `@Entity` sobre la tabla `empresas` (`Code/Empresa/Empresa`, que agrega `cuit`). Campos y JSON de `EmpresaBaseDTO`: `nombre`, `descripcion`, `ruta` (slug para URLs) y `codigo`; la entidad suma `saldo`. El service base es `EmpresaBaseService`, con `obtenerContexto()` detrás de `GET /empresa/contexto`.
- `GET /empresa/contexto` responde `ContextoDeEmpresaDTO`: `multiempresaHabilitada`, `uuidEmpresaPorDefecto`, `encabezadoDeEmpresaRequerido`, `uuidEmpresaDelRequest`, `uuidEmpresaResuelta` y `empresaActual` (con sesión).
- Pertenencia del usuario a una empresa: `UsuarioEmpresa` (tabla `auth_usuarios_empresas`: `id_usuario`, `id_empresa`), con sus roles en `auth_usuarios_empresas_roles` (`id_usuario_empresa`, `id_rol`). La entidad de usuario extiende `UsuarioBaseMultiempresa` y el DTO `UsuarioBaseMultiempresaDTO`, que publica `rolesPorEmpresa` (lista de `UsuarioEmpresaDTO`: `empresaUuid`, `empresaNombre`, `roles`).
- Service base de usuarios: `UsuarioBaseMultiempresaService`. Hooks: `hookPuedeAsignarRol(rol, usuario, empresaUuid)`, `hookRegistroPublicoAntesDeCrearEmpresa(usuarioDTO, empresaDTO)` y `hookRegistroPublicoAntesDeGuardar(usuario, empresa)`.
- Encabezado de la empresa: `X-Empresa-Uuid` (`ContextoDeEmpresa.ENCABEZADO_EMPRESA`). Propiedades: `core.multiempresa.habilitada`, `uuid-de-sistema`, `uuid-por-defecto`, `nombre-de-sistema` y `nombre-por-defecto`.

| Ruta | Body | Respuesta |
|---|---|---|
| `POST /usuario/publico/registro` | `RegistroPublicoRequest`: `{usuario, empresa}` | `UsuarioDTO` pendiente de verificación |
| `POST /usuario/publico/registro-cliente` | `UsuarioDTO`, empresa en `X-Empresa-Uuid` | `UsuarioDTO` pendiente de verificación |
| `POST /usuario/publico/vincular/{usuarioUuid}` | —, empresa en `X-Empresa-Uuid`; requiere sesión | `UsuarioDTO` |

Las rutas viejas (`/usuario/public/register`, `register-client`, `link/{userUuid}` y `GET /empresa/context`) ya no existen y responden `404`.

Con `core-monoempresa` no hay empresas: el usuario tiene roles directos en `UsuarioRol` (tabla `auth_usuarios_roles`: `id_usuario`, `id_rol`).
