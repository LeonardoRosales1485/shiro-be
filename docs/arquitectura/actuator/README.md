# Actuator: health, info y el header `API-Version`

Spring Boot Actuator es la única vía por la que el backend publica su estado y su versión.
La dependencia `spring-boot-starter-actuator` la declaran `sm-core-maven/core-kernel/pom.xml` y `app/pom.xml`:
viene con el kernel, así que está en cualquier proyecto que use el Core.

La regla de qué debe exponer está en [`docs/rules/versionado.md`](../../rules/versionado.md) ("Exposición vía Actuator").
Este documento describe cómo está implementado hoy.

## Qué se expone

| Endpoint | Path completo | Quién lo provee |
|---|---|---|
| `/health` | `GET {host}/api/actuator/health` | Spring Boot (autoconfiguración) |
| `/info` | `GET {host}/api/actuator/info` | Spring Boot + `VersionInfoContributor` del kernel |

El `/api` lo agrega `server.servlet.context-path=/api` (`app/src/main/resources/application.local.properties`
y `app/src/main/resources/application.server.properties`): los endpoints de actuator cuelgan del context path,
no de la raíz. El `/actuator` es el `base-path` por defecto de Actuator, el proyecto no lo cambia.

En servidor, nginx proxea `location /api` hacia el contenedor
(`deploy/plantillas/spring-3.9-java-25-postgres-18-4/nginx.spring-boot-api.conf`), por lo que el path queda
`https://{host}/{path}/api/actuator/health`, tal como anota el comentario de `application.server.properties`.

### Respuestas reales

`GET /api/actuator/health` — con `management.endpoint.health.show-details=never` responde solo el estado agregado:

```json
{ "status": "UP" }
```

`GET /api/actuator/info` — lo que arma `VersionInfoContributor`
(`sm-core-maven/core-kernel/src/main/java/com/blue/open/Core/Kernel/Configuracion/VersionInfoContributor.java`):

```json
{
  "version": "1.0.12",
  "coreVersion": "2.1.0"
}
```

Los valores del ejemplo son ilustrativos: `1.0.12` es el `<version>` de `app/pom.xml` y `2.1.0` es la versión
del Core (el `<parent>` de `app/pom.xml`).

## De dónde sale cada dato de `/info`

| Dato | Cadena que lo resuelve |
|---|---|
| `version` | `VersionInfoContributor` -> puerto `ProveedorDeVersion` -> default `VersionPorPropiedad` -> propiedad `app.version` -> recurso `/app-version.properties` del jar del proyecto |
| `coreVersion` | `VersionInfoContributor` -> `CoreVersion.get()` -> recurso `/core-version.properties` del jar de `core-kernel` |

- **`version`**: el kernel no conoce la versión del proyecto, la pide por el puerto `ProveedorDeVersion`
  (`sm-core-maven/core-kernel/src/main/java/com/blue/open/Core/Kernel/Puertos/ProveedorDeVersion.java`). Si el proyecto no
  publica su bean, `KernelConfiguracion` registra el default `VersionPorPropiedad`, que lee la propiedad
  `app.version` y cae en `"desconocida"` si no está. Este proyecto no publica el bean: `Application` carga
  `app/src/main/resources/app-version.properties` con `@PropertySource`, cuyo contenido es
  `app.version=@project.version@`. El bloque `<resources>` de `app/pom.xml` filtra ese archivo (y solo ese, porque
  las `application.*.properties` usan placeholders `${...}` de Spring), así que Maven lo reemplaza por el `<version>`
  del proyecto. No sirve ponerlo en `application.server.properties`: en el deploy ese archivo se monta directo del
  repo, sin pasar por Maven.
- **`coreVersion`**: `CoreVersion` no tiene la versión escrita a mano
  (`sm-core-maven/core-kernel/src/main/java/com/blue/open/Core/Kernel/CoreVersion.java`): lee
  `sm-core-maven/core-kernel/src/main/resources/core-version.properties`, cuyo contenido es `core.version=@project.version@`.
  El bloque `<resources>` de `sm-core-maven/core-kernel/pom.xml` filtra ese archivo (y solo ese) durante el build, así que
  Maven lo reemplaza por la versión del artefacto (heredada de `core-parent`). Si el recurso no aparece en el
  classpath, `CoreVersion.get()` devuelve `"desconocida"`.

Consecuencia práctica: al subir la versión solo se toca `<version>` de `app/pom.xml`
(ver [`docs/rules/versionado.md`](../../rules/versionado.md)); `coreVersion` se actualiza solo al cambiar
la versión de `core-parent` en el `<parent>` de `app/pom.xml`.

## Header `API-Version`

Lo agrega `EncabezadoApiVersionFilter`
(`sm-core-maven/core-kernel/src/main/java/com/blue/open/Core/Kernel/Configuracion/EncabezadoApiVersionFilter.java`):

- Es un `OncePerRequestFilter` anotado `@Component` y `@Order(Ordered.HIGHEST_PRECEDENCE)`.
- No sobreescribe `shouldNotFilter`: no excluye ninguna ruta, se aplica a toda request que atienda la aplicación.
- Hace `response.setHeader("API-Version", proveedorDeVersion.getVersion())` antes de continuar la cadena, así que
  el header viaja también en las respuestas de error.
- Usa **el mismo** `ProveedorDeVersion` que `/info`: el header y `info.version` no pueden diferir. Formato: el
  `<version>` de `app/pom.xml`, por ejemplo `1.0.12`.

## Configuración

| Propiedad | Valor en el repo | Archivo | Efecto |
|---|---|---|---|
| `management.endpoints.web.exposure.include` | `health,info` | `application.local.properties` y `application.server.properties` | Solo esos dos endpoints se publican por HTTP |
| `management.endpoint.health.show-details` | `never` | ambos | `/health` responde solo `status`, sin detalle por indicador |
| `server.servlet.context-path` | `/api` | ambos | Prefija también los paths de actuator |
| `app.version` | (no está seteada) | — | Default `desconocida` del kernel; irrelevante acá porque `Application` publica el bean |

Los dos archivos de properties traen idénticas las tres primeras propiedades, así que actuator se comporta igual
en local y en servidor. Cuál de los dos se monta como `/config/application.properties` lo decide el campo
`application_properties` del `deploy.yml` del proyecto (ver [`deploy/deploy.md`](../../../deploy/deploy.md)).

## Quién calcula el `status` de `/health`

Ni el Core ni `app/` declaran un `HealthIndicator` propio (no hay ninguna implementación de esa interfaz en el
repo). El estado agregado lo arma Spring Boot con los indicadores que autoconfigura según las dependencias
presentes. Con `show-details=never`, la respuesta no revela cuáles son ni el estado de cada uno.

## Cómo agregar algo

**Un dato nuevo en `/info`**: un bean que implemente `org.springframework.boot.actuate.info.InfoContributor`,
igual que `VersionInfoContributor`. Se suma a lo que ya publica el kernel, no lo reemplaza. Dónde va el archivo lo
define [`docs/rules/estructura-de-carpetas.md`](../../rules/estructura-de-carpetas.md): si es infraestructura
transversal, `Code/Common/{Concern}/`.

**Cambiar de dónde sale `version`**: no se toca el Core ni el contributor; se publica un bean propio de
`ProveedorDeVersion` (es el puerto, con default `@ConditionalOnMissingBean` en `KernelConfiguracion`). Hoy ese bean
es la lambda de `Application.java`.

**Un health indicator propio**: un bean que implemente `HealthIndicator`. Tener en cuenta que con
`show-details=never` su detalle no se ve en la respuesta: solo puede bajar el `status` agregado a `DOWN`.

Las validaciones de arranque (zona horaria, integraciones externas) **no** pasan por actuator: son los smoke tests
de `sm-core-maven/core-kernel/src/main/java/com/blue/open/Core/Kernel/SmokeTest/`, que corren al iniciar la aplicación (ver
[`docs/rules/integraciones-y-entorno.md`](../../rules/integraciones-y-entorno.md)).

## Seguridad y exposición

- **Sin autenticación.** `SeguridadWebConfig` de `core-identity`
  (`sm-core-maven/core-identity/src/main/java/com/blue/open/Core/Identity/Configuracion/SeguridadWebConfig.java`) deja pasar
  todas las rutas (`anyRequest().permitAll()`), porque la autorización real la resuelve `AuthService` dentro de cada
  endpoint. Los endpoints de actuator no pasan por `AuthService`, así que `/api/actuator/health` e
  `/api/actuator/info` son públicos.
- **Todo lo demás está cerrado.** `management.endpoints.web.exposure.include=health,info` implica que `env`,
  `beans`, `metrics`, `loggers`, `mappings`, `configprops`, `threaddump`, `heapdump` y `shutdown` no se publican por
  HTTP. Eso evita exponer properties con credenciales (datasource, mail, captcha) y acciones de administración.
- **Sin detalle de infraestructura.** `show-details=never` impide que `/health` filtre estado de base de datos, disco
  u otros indicadores a un cliente anónimo.
- `/info` sí es público e informa las versiones de app y Core; es lo que pide
  [`docs/rules/versionado.md`](../../rules/versionado.md).

## Verificación

Actuator no tiene prueba de contrato: la completitud (`ContratosCompletosTest`) excluye los endpoints de Spring. En
un ambiente desplegado lo verifica `deploy/smoke.py`: `/api/actuator/health` tiene que responder `UP` y
`/api/actuator/info` la versión esperada (ver `deploy/deploy.md`).

## Dudas

- **`application.local.properties` no se usa en el deploy local.** El `deploy.yml` del repo apunta a
  `app/src/main/resources/application.server.properties` incluso dentro del bloque `local:`, y ese es el archivo que
  `deploy/deployer/local_flow.py` monta en el contenedor. Para actuator da igual (las propiedades son idénticas en
  los dos archivos), pero queda la duda de si es intencional o un resto de configuración.
