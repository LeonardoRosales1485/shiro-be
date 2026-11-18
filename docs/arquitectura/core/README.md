# Core: módulos, puertos y cómo elegir qué usar

El Core es un conjunto de módulos Maven versionados juntos (`com.blue.open:core-*`), publicados desde su propio
repositorio, [`SM-SOLUCIONES/sm-core-maven`](https://github.com/SM-SOLUCIONES/sm-core-maven). El proyecto no tiene su
código: lo declara como dependencia en `app/pom.xml`, con la versión del `<parent>` (`core-parent`). Un proyecto
declara **solo los módulos que necesita**.

Historial de versiones: [`sm-core-maven/changelog/`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/changelog/README.md).

## Las dos reglas

> **1. El Core nunca importa `com.blue.open.Code`.** Si el Core necesita algo del proyecto, lo declara
> como puerto (una interfaz) y el proyecto lo implementa.
>
> **2. El Core no decide, ofrece.** Toda política tiene un valor por defecto reemplazable sin tocar el Core.

La primera se verifica con:

```bash
grep -rn "com.blue.open.Code" core --include=*.java
```

Solo deben aparecer menciones en comentarios. Un import real es un bug.

## Módulos

| Módulo | Para qué | Tablas que crea |
|---|---|---|
| `core-kernel` | Obligatorio. Paginado, entidades base, [Proyección](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-kernel/docs/proyeccion.md) (`Proyector`: lectura del DTO desde la consulta y aplicar un DTO sobre la entidad), excepciones, fechas GMT-3, trazabilidad, `API-Version`, smoke tests, `@Contrato` (autorización de endpoints), puertos | — |
| `core-crud` | La lógica del ABM de una entidad (`AbstractService`: `obtenerPorUuid`, `paginar`, `guardar`, `borrar`). Los endpoints los escribe el proyecto con la [receta](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/receta-controller.md) | — |
| `core-identity` | Usuarios, roles, permisos, login por contraseña, recuperación de contraseña | `auth_usuarios`, `auth_roles`, `auth_permisos`, `auth_roles_permisos`, `codigos_de_seguridad` |
| `core-monoempresa` | Scoping simple: un usuario tiene roles y punto | `auth_usuarios_roles` |
| `core-multiempresa` | Scoping por empresa, encabezado `X-Empresa-Uuid`, registro público con alta de empresa | `empresas`, `auth_usuarios_empresas`, `auth_usuarios_empresas_roles` |
| `core-protocolo-jwt` | Inicio de sesión JWT + refresh | `auth_jwt_refresh_tokens` |
| `core-protocolo-apikey` | Tokens de acceso personales | `auth_access_tokens` |
| `core-protocolo-sso` | SSO externo y Malvinas ([certificado](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-protocolo-sso/docs/certificados-malvinas.md)) | — |
| `core-mail` | Envío de emails | — |
| `core-crypter` | Archivos encriptados | — |
| `core-sm3` | Archivos públicos | — |
| `core-captcha` | Verificación de hCaptcha | — |
| `core-test` | Scope test. Pruebas de contrato: PostgreSQL embebido, MockMvc, dobles en memoria y emisor del Contrato de endpoint ([testing](../testing/README.md)) | — |

**`core-monoempresa` y `core-multiempresa` son excluyentes.** Se usa uno, el otro, o ninguno
(usuarios autenticados sin roles).

## Cómo arrancar un proyecto

1. En `app/pom.xml`, dejar solo las dependencias `core-*` que el proyecto va a usar.
2. En `db.changelog-master.yaml`, incluir el changelog de cada uno de esos módulos, en el mismo orden.
3. `Application.java` escanea `com.blue.open.Code` y `com.blue.open.Core`: los módulos presentes se
   registran solos y los ausentes no existen. No hay que tocar esa clase al agregar o quitar un módulo.

Quitar un módulo = borrar su dependencia + borrar su `include` del master. Nada más.

## Catálogo de puertos

Todos tienen implementación por defecto. Para reemplazar uno, el proyecto publica un bean propio
(anotado `@Primary` si el default no es `@ConditionalOnMissingBean`).

### Kernel

| Puerto | Qué resuelve | Default |
|---|---|---|
| `ProveedorDeUsuarioActual` | Quién es el usuario logeado, para la auditoría y los permisos | `UsuarioActualAnonimo`. Con `core-identity`, `AuthService` |
| `AutorizadorDeEndpoint` | Si el usuario actual cumple el `permiso` del `@Contrato` del endpoint (lo consulta `InterceptorDeContrato`) | `AutorizadorDenegarTodo` (solo deja pasar `PUBLICO`). Con `core-identity`, `AutorizadorPorRolesYPermisos` |
| `ProveedorDeConfiguracion` | Configuración dinámica (la clave `DEBUG` de la trazabilidad) | Lee del `application.properties` |
| `ProveedorDeVersion` | La versión del proyecto para `/info` y el header `API-Version` | Lee `app.version` |
| `EnviadorDeMail` | Enviar emails | `core-mail`. Sin ese módulo, error explícito |
| `IntegracionExterna` | Que el smoke test de arranque descubra las integraciones presentes | La implementa cada integración |

Además, el kernel publica el bean `Proyector` ([Proyección](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-kernel/docs/proyeccion.md)): no es un puerto para reemplazar sino el motor que usan el ABM y cualquier caso de uso para proyectar (`porUuid`, `completarColecciones`), aplicar un DTO sobre una entidad (`aplicar`) y validar una proyección (`validar`).

### Crud

| Puerto | Qué resuelve | Default |
|---|---|---|
| `EstrategiaDeBorrado` | Si el `DELETE` es lógico o físico | Por tipo de entidad: `EntidadConBorradoLogico` → lógico; si no, físico |

Además, `core-crud` publica `ValidadorDeProyecciones`: al arrancar valida con el `Proyector` los DTOs de todos los `AbstractService` contra su entidad (rutas de `@Columna`/`@Coleccion` inexistentes, ciclos de DTOs) y corta el arranque si alguno es inválido.

### Identity

| Puerto | Qué resuelve | Default |
|---|---|---|
| `ResolutorDePermisos` | Qué roles y permisos tiene un usuario | Sin roles. Lo implementan mono/multiempresa |
| `ValidadorDeContexto` | Validación extra en cada request autenticado | No-op. `core-multiempresa` exige pertenencia a la empresa |
| `EstrategiaDeAutenticacion` | Un protocolo de login | Una por cada `core-protocolo-*` presente |
| `CatalogoDeAutorizacion` | Los roles y permisos del proyecto | Vacío. El proyecto publica sus enums |
| `InicializadorDeAutorizacion` | Datos iniciales que dependen del scoping | No-op. Lo implementan mono/multiempresa |
| `SincronizadorDeRolesExternos` | Aplicar los roles que declara un SSO | No-op. `core-multiempresa` lo implementa |
| `PoliticaDeAccesoExterno` | Si una identidad externa (el legajo de Malvinas) puede entrar. `AuthMalvinasService` la consulta después de buscar el usuario vigente y antes de crearlo o guardarlo; si lanza `ExcepcionDeApi`, el error sale tal cual | No valida nada (`@ConditionalOnMissingBean`). El proyecto publica la suya |
| `UsuarioRepositorio` | Acceso a usuarios sin nombrar la clase concreta del proyecto | `UsuarioRepositorioJpa` (descubre la entidad en el metamodelo JPA) |

### Multiempresa

| Puerto | Qué resuelve | Default |
|---|---|---|
| `EmpresaRepositorio` | Acceso a empresas sin nombrar la clase concreta | `EmpresaRepositorioJpa` |
| `AltaDeEmpresa` | Crear la empresa en el registro público | `AltaDeEmpresaPorProyector`: instancia la entidad de empresa del proyecto, le aplica el DTO con el `Proyector` y la guarda |

## Controllers: el Core no trae controllers base

Desde 3.0.0 ([changelog](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/changelog/3.0.0.md)) ningún módulo trae un controller para heredar: los de
usuarios, roles, permisos y empresas viven en `Code`. El ABM de una entidad se escribe explícito con la
[receta](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/receta-controller.md) y delega en `AbstractService`. Una operación que la
entidad no necesita no se escribe: responde 404, no 403. Los únicos controllers del Core son los de
protocolo (`JwtLoginController`, `AccessTokenController`), presentes solo si se declara su módulo.

Todo endpoint lleva `@Contrato` con su `permiso`, que autoriza el request a través del puerto
`AutorizadorDeEndpoint` (ver [testing](../testing/README.md)).

Un proyecto grande puede no depender de `core-crud` y quedarse solo con el kernel: conserva el paginado,
las excepciones y la trazabilidad, con endpoints propios.

## Borrado lógico vs. físico

Lo decide de qué clase hereda la entidad:

| Hereda de | Borrado | Columna `status` | Filtro en el paginado |
|---|---|---|---|
| `EntidadConBorradoLogico` | `status = -1` + fecha y usuario | Sí | `status = 0` |
| `EntidadBase` | `DELETE` real | No | Ninguno |

El repositorio de una entidad con borrado lógico extiende `EntidadRepositoryBorradoLogico` (agrega los
finders que filtran por `status = 0`); el de una entidad física, `EntidadRepository`.

No hace falta `{Entidad}RepositoryCustom` ni `{Entidad}RepositoryImpl`: `EntidadRepositoryBase` provee
`paginar(pedido, claseDto)` para todos los repositorios. Desde 2.0.2 el paginado es JPQL (MariaDB y
PostgreSQL por igual) y responde directamente el DTO pedido, trayendo solo sus columnas: ver
[`sm-core-maven/core-crud/docs/PAGINADO.md`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/PAGINADO.md).

## Propiedades del Core

| Propiedad | Default | Módulo |
|---|---|---|
| `auth.protocolos` | (vacío) | identity — protocolos habilitados, separados por coma. Hasta 3.2.0: `auth.protocols` |
| `core.multiempresa.habilitada` | `true` | multiempresa — `false` usa siempre la empresa por defecto. Hasta 3.2.0, todas las `core.multiempresa.*` eran `core.multi-company.*` (`enabled`, `system-uuid`, `default-uuid`, `system-name`, `default-name`) |
| `core.multiempresa.uuid-de-sistema` | `1` | multiempresa — empresa cuyos roles valen en todas |
| `core.multiempresa.uuid-por-defecto` | `2` | multiempresa — empresa operativa por defecto |
| `core.multiempresa.nombre-de-sistema` / `nombre-por-defecto` | `Sistema` / `Principal` | multiempresa |
| `core.identity.usuario-inicial.nombre-de-usuario` / `.contrasena` | (sin default desde 2.0.1) | identity — sin las dos, no se crea el usuario inicial. Hasta 3.2.0: `.username` / `.password` |
| `core.protocolo.jwt.ruta` | `usuario` | jwt — prefijo de `/iniciar-sesion` y `/refresh-token`. Hasta 3.2.0: `core.protocolo.jwt.path` (default `user`) |
| `core.protocolo.cookie.habilitada` | `true` | identity — `false` apaga la lectura y la emisión de las cookies `access_token` y `refresh_token` (jwt y malvinas). Desde 3.6.0 |
| `core.protocolo.cookie.secure` / `.same-site` / `.dominio` | `true` / `Lax` / (vacío) | identity — atributos de las cookies emitidas. `secure=false` solo en desarrollo local sin HTTPS |
| `core.protocolo.cookie.origenes-permitidos` | (vacío) | identity — orígenes (`scheme://host[:puerto]`, separados por coma) desde los que se acepta una cookie en un POST, PUT, PATCH o DELETE, además del mismo host |
| `core.protocolo.cookie.path-refresh-token` | (vacío) | protocolo-jwt — `Path` público de la cookie `refresh_token`; vacío = `server.servlet.context-path` + `core.protocolo.jwt.ruta`. Se define si un proxy agrega un prefijo. Desde 3.6.1 |
| `core.protocolo.apikey.ruta` | `usuario` | apikey — prefijo de los endpoints de tokens. Hasta 3.2.0: `core.protocolo.apikey.path` (default `user`) |
| `jwt.secreto` | (sin default desde 4.0.0: obligatoria) | jwt — clave HS256 con la que se firman y validan los JWT; sin ella la aplicación no arranca. Hasta 3.2.0: `jwt.secret`; conservar el mismo valor para que los JWT emitidos antes sigan validando |
| `app.version` | `desconocida` | kernel — solo si el proyecto no publica un `ProveedorDeVersion` |
| `captcha.secreto` | (sin default) | captcha — clave secreta de hCaptcha. Hasta 3.2.0: `captcha.secret` |
| `captcha.habilitado` | `true` | captcha — `false` no verifica el token. Hasta 3.2.0: `captcha.enabled` |
| `crypter.url` / `crypter.tipo.documento` | (sin default) | crypter |
| `crypter.usuario` / `crypter.contrasena` | (sin default) | crypter — Basic Auth del webservice. Hasta 3.2.0: `crypter.user` / `crypter.pass` |
| `sm3.url` / `sm3.proyecto` | (sin default) | sm3 |
| `sm3.usuario` / `sm3.contrasena` | (sin default) | sm3 — Basic Auth del webservice. Hasta 3.2.0: `sm3.user` / `sm3.pass` |
| `mail.opcion` | `smtp` | mail — `notify` todavía no está implementado. Hasta 3.2.0: `mail.option`. El servidor SMTP se configura con `spring.mail.*` |

## Distribución

Desde 4.0.0 cada módulo `core-*` y `core-parent` se publican en el repositorio Maven público
`https://sm-soluciones.github.io/sm-core-maven` (GitHub Pages del repositorio `sm-core-maven`), sin token para
descargarlos. `app/pom.xml` declara ese repositorio en `<repositories>` y a `core-parent` como `<parent>`:

```xml
<parent>
    <groupId>com.blue.open</groupId>
    <artifactId>core-parent</artifactId>
    <version>4.0.0</version>
    <relativePath/>
</parent>
```

- **Actualizar el Core** es cambiar esa versión y seguir los pasos del changelog de la versión nueva.
- **Qué hereda `app` de `core-parent`**: la versión de cada `core-*` (por eso las dependencias no llevan
  `<version>`), Lombok, el compilador en Java 25 y la configuración de surefire y failsafe.
- **Documentación**: la de implementación de cada módulo (receta del controller, paginado, Proyección, certificado
  de Malvinas) vive con el módulo en `sm-core-maven/core-*/docs/`. La general (reglas, arquitectura, auditoría,
  criterios, servicios, ADRs) es del proyecto y vive en `docs/`.
- **Que el proyecto no tenga el Core** lo verifica `CorePublicadoTest` (CORE-01). Ver
  [`docs/auditoria/README.md`](../../auditoria/README.md) → "Core publicado" y
  [ADR 0006](../../adr/0006-core-publicado-como-artefacto.md).
