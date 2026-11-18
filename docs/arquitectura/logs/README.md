# Sistema de logs

Cómo se escriben, con qué formato y dónde terminan los logs del backend. La trazabilidad
(`traceId`, `@Trazar`, sanitizado) vive en `core-kernel`, así que está disponible en cualquier
proyecto que use el Core (ver [`docs/arquitectura/core/README.md`](../core/README.md)).

## Cómo está armado

| Pieza | Dónde |
|---|---|
| API de logging | SLF4J, casi siempre vía `@Slf4j` de Lombok |
| Implementación | Logback (la que trae Spring Boot) |
| Configuración | `app/src/main/resources/logback-spring.xml` |
| Trazabilidad | `sm-core-maven/core-kernel/src/main/java/com/blue/open/Core/Kernel/Trazabilidad/` |

`logback-spring.xml` declara **un solo appender: `CONSOLE`** (`ch.qos.logback.core.ConsoleAppender`),
con un `ThresholdFilter` en `INFO` y el `root` en `INFO`. **No hay `FileAppender` ni `RollingFileAppender`:**
todo sale por stdout.

### Formato de línea

Patrón exacto declarado en `logback-spring.xml`:

```
[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5level] [traceId: %X{traceId}] [spanId: %X{spanId}] [depth: %X{spanDepth}] [thread: %thread] %logger{36} - %msg%n
```

`traceId`, `spanId` y `spanDepth` salen del MDC. Si no hay una request o una operación trazada en curso,
esos corchetes aparecen vacíos.

### Niveles por paquete

Los frameworks ruidosos están bajados en `logback-spring.xml`:

| Logger | Nivel |
|---|---|
| `org.apache.catalina`, `org.apache.coyote`, `org.apache.tomcat` | `WARN` |
| `org.hibernate` | `WARN` |
| `org.springframework.web` | `WARN` |
| `org.springframework.security` | `WARN` |
| `org.springframework.boot.autoconfigure` | `WARN` |
| `root` | `INFO` |

Dos más se bajan desde properties (ver la tabla del final): `liquibase.database` y
`org.hibernate.orm.deprecation`.

## `traceId`: correlacionar las líneas de una request

`RequestLoggingFilter` (`core-kernel`, un `OncePerRequestFilter`) corre en cada request:

- Resuelve el `traceId` con `TraceContext.resolver(request.getHeader("X-Trace-Id"))`. Si el cliente mandó
  un `X-Trace-Id` que matchea `^[A-Za-z0-9._:-]{1,128}$` se reutiliza; si no, se genera un `UUID`.
- Lo deja en el MDC (`traceId`) y lo devuelve al cliente en el header `X-Trace-Id` de la respuesta.
- Al terminar, restaura el `traceId` previo del hilo (no lo borra a secas).

Siempre loguea, en `INFO`:

```
REQUEST_START - Method: {} | URI: {} | Content-Type: {} | Content-Length: {}
REQUEST_END - Status Code: {} | DurationMs: {}
```

Con `DEBUG = 1` (ver más abajo) agrega el cuerpo, ya sanitizado:

```
REQUEST - Payload: {}
RESPONSE - Status Code: {}
RESPONSE - Body: {}
```

El body de respuesta no se cachea (ni se loguea) para URIs que terminan en `/export/excel`, y el body de
request se cachea hasta 100.000 bytes.

El `traceId` también viaja a los hilos asincrónicos: `InicializadorDeTraceIdGlobal` implementa `TaskDecorator`
copiando todo el MDC del hilo padre, y `Code/Common/Configuracion/AsincronicoConfig.java` se lo pasa al
`ThreadPoolTaskExecutor` de `@Async`.

Por último, `ExcepcionDeApi` guarda el `traceId` del MDC al construirse, así que la respuesta de error que
recibe el front trae el mismo id que las líneas del log.

## `@Trazar`

`com.blue.open.Core.Kernel.Trazabilidad.Trazar` marca un método cuyo inicio, fin, argumentos y resultado
deben quedar registrados. Lo procesa `TrazabilidadAspect` (`@Order(0)`).

### Cuándo se usa

Solo en endpoints críticos: los que trabajan con dinero, registran pagos o generan facturas o comprobantes,
donde un fallo puede suponer una pérdida para el negocio. Cada request trazado imprime un par de líneas por
el endpoint y otro por cada método de `@Service` que ejecute, con argumentos y resultado serializados: es
pesado para el log y para el servidor. La regla, con lo que no lleva `@Trazar`, está en "Trazabilidad:
`@Trazar`" de [`docs/rules/controllers-y-endpoints.md`](../../rules/controllers-y-endpoints.md).

### Atributos

| Atributo | Default | Para qué |
|---|---|---|
| `nombre` | `""` | Nombre estable de la operación. Vacío ⇒ `ClaseSimple.metodo` |
| `excluir` | `{}` | Campos extra a redactar en argumentos y resultado |
| `registrarArgumentos` | `true` | `false` no imprime `arguments=` |
| `registrarResultado` | `true` | `false` no imprime `result=` |

### Qué imprime

Al entrar, en `INFO`:

```
TRACE_START - spanId={} | parentSpanId={} | depth={} | operation={} | arguments={}
```

Al salir bien, en `INFO`:

```
TRACE_END - spanId={} | depth={} | operation={} | status=SUCCESS | durationMs={} | result={}
```

Al salir con excepción, en `ERROR` (con el stacktrace como último argumento):

```
TRACE_END - spanId={} | depth={} | operation={} | status=ERROR | durationMs={} | exceptionType={}
```

- `spanId`: 16 caracteres hex (un `UUID` sin guiones, truncado).
- `parentSpanId`: el span que lo envuelve, o `-` si es el primero.
- `depth`: nivel de anidamiento, empieza en 1.
- `durationMs`: milisegundos medidos con `System.nanoTime()`.
- `result`: para un `ResponseEntity` se serializa como `{"status": ..., "body": ...}`; un `ServletRequest`
  o `ServletResponse` se reemplaza por `[respuesta servlet omitida]`.

Ejemplo de par de líneas, armado con el patrón de `logback-spring.xml` y los formatos del aspecto, para un
hipotético `@Trazar(nombre = "PagoController.registrar", excluir = { "numeroDeTarjeta" })`:

```
[2026-09-20 10:46:00.123] [INFO ] [traceId: 8f2a1c40-7f1e-4a3b-9c11-2d5e6f70a8b9] [spanId: a1b2c3d4e5f60718] [depth: 1] [thread: http-nio-9090-exec-3] c.b.o.C.K.T.TrazabilidadAspect - TRACE_START - spanId=a1b2c3d4e5f60718 | parentSpanId=- | depth=1 | operation=PagoController.registrar | arguments={"dto":{"facturaUuid":"3f1c...","monto":15000.00,"numeroDeTarjeta":"[REDACTED]"}}
[2026-09-20 10:46:00.310] [INFO ] [traceId: 8f2a1c40-7f1e-4a3b-9c11-2d5e6f70a8b9] [spanId: a1b2c3d4e5f60718] [depth: 1] [thread: http-nio-9090-exec-3] c.b.o.C.K.T.TrazabilidadAspect - TRACE_END - spanId=a1b2c3d4e5f60718 | depth=1 | operation=PagoController.registrar | status=SUCCESS | durationMs=187 | result={"status":200,"body":{"uuid":"9a7b...","estado":"ACREDITADO"}}
```

### Propagación automática a los services

Mientras hay una trazabilidad activa (la abre un método anotado, que pone `traceEnabled=true` en el MDC),
el mismo aspecto intercepta **todos los métodos públicos de clases `@Service` bajo `com.blue.open`** y les
genera su propio span anidado, con el `depth` incrementado y el `parentSpanId` del span que los llamó. Sin
un `@Trazar` arriba, los services no se trazan: el aspecto hace `proceed()` y sale.

La lista de `excluir` del `@Trazar` que abrió la traza se propaga por MDC (`traceExcludedFields`) a esos
spans automáticos.

### Dónde se usa hoy

Ningún endpoint del template ni del Core lleva `@Trazar`: ninguno es crítico en el sentido de la regla.
Hasta Core 3.0.0 estaban anotados las escrituras del ABM, el login, los tokens y la configuración; se
quitaron en [3.0.1](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/changelog/3.0.1.md).

El patrón en un proyecto es anotar el endpoint crítico y declarar lo que no debe salir en el log:

```java
@PostMapping("/registrar")
@Contrato(casoDeUso = "CU-PAG-01", resumen = "Registra un pago de una factura", permiso = "PAGO_REGISTRAR")
@Trazar(excluir = { "numeroDeTarjeta" })
public ResponseEntity<PagoRegistrarResponseDTO> registrar(@Valid @RequestBody PagoRegistrarRequestDTO dto) { ... }
```

## `TrazabilidadAspect` vs. `TraceDeEjecucionAspect`

Son dos aspectos distintos, que conviven y pueden loguear la misma llamada dos veces.

| | `TrazabilidadAspect` | `TraceDeEjecucionAspect` |
|---|---|---|
| Se activa por | La anotación `@Trazar` (y, con la traza abierta, cualquier `@Service`) | La clave de configuración `DEBUG = 1` |
| Alcance | Métodos anotados + métodos públicos de `@Service` de `com.blue.open` | `execution(* com.blue.open..*Controller.*(..))` o `..*Service.*(..)` |
| Qué imprime | `TRACE_START` / `TRACE_END` con argumentos, resultado y `durationMs` | `TRACE -> Clase.metodo()` y `TRACE <- Clase.metodo() OK (n ms)` |
| Datos | Argumentos y resultado sanitizados | Solo clase, método y duración |
| Anidamiento | `spanId`/`parentSpanId`/`depth` en el MDC | Sangría de dos espacios por nivel, en un `ThreadLocal` |
| Nivel | `INFO`, y `ERROR` cuando la operación falla | `INFO` siempre (también al fallar: `TRACE <- ... EX Clase (n ms)`) |

Líneas de `TraceDeEjecucionAspect`, con el formato exacto del archivo:

```
TRACE -> ConfiguracionController.listar()
TRACE   -> AuthService.tienePermisos()
TRACE   <- AuthService.tienePermisos() OK (2 ms)
TRACE <- ConfiguracionController.listar() OK (15 ms)
```

### La clave `DEBUG`

`TraceDeEjecucionAspect` y el logueo de payloads de `RequestLoggingFilter` se prenden con la misma condición:
`ProveedorDeConfiguracion.getValor("DEBUG", "0")` devuelve `"1"`.

En este proyecto el puerto lo implementa `Code/Configuracion/ConfiguracionDesdeBase.java` (`@Primary`), que
lee la tabla `configuraciones`; ante cualquier error devuelve el valor por defecto. Sin ese bean, el default
del kernel es `ConfiguracionDesdePropiedades`, que lee del `application.properties`.

Por defecto es `0`: la trazabilidad de `TraceDeEjecucionAspect` y los payloads están apagados. Los dos
resuelven `DEBUG` con un guard `ThreadLocal` porque la propia lectura pasa por un `@Service` del proyecto y
volvería a entrar al aspecto.

## Sanitizado: qué nunca sale en el log

`SanitizadorDeLogs` (`core-kernel`) pasa por Jackson todo payload, argumento y resultado antes de escribirlo,
y reemplaza por `[REDACTED]` cualquier clave que, normalizada (minúsculas, sin `_` ni `-`), **contenga** alguna de
estas raíces:

```
authorization, cookie, password, contrasena, token, secret, codigodeverificacion, apikey, captcha, base64
```

o **coincida exacta** con alguna de estas claves:

```
archivo, contenidodelarchivo, contenidoarchivo
```

Así se tapan `contrasenaActual`, `contrasenaNueva`, `refreshToken`, `clientSecret` o `imagenBase64` sin listarlas
una por una (también un booleano como `tieneContrasena`). La raíz `secret` no cuenta dentro de `secretari`
(`secretario`, `secretaria`): es un rol del negocio. `archivo` es exacta para no tapar `archivos` ni
`nombreDelArchivo`. `ExcepcionDeApi.conDato` usa el mismo criterio (`SanitizadorDeLogs.esClaveSensible`).

`contenido` no está en la lista: es la lista de registros de la respuesta del paginado (`Pagina<T>`). El campo de
un DTO que lleva el contenido de un archivo se llama `contenidoDelArchivo` para que el log lo tape.

Además:

- Más de 10.000 caracteres, o `Content-Type` que no sea JSON ⇒ `[payload omitido]`.
- JSON inválido ⇒ `[payload JSON invalido omitido]`.
- `byte[]` ⇒ `[contenido binario omitido]`.
- Objeto no serializable ⇒ `[valor no serializable omitido]`.
- Lo que se sume en `@Trazar(excluir = {...})` se redacta igual que las claves de la lista fija.

`ManejadorDeExcepcionesDeApi` usa el mismo sanitizador para el payload que imprime junto a cada respuesta de
error.

## Dónde terminan los logs

### Local

`python deploy/deploy.py --local` fuerza, para stacks Spring, `LOGGING_FILE_PATH=/app/logs` y
`LOGGING_FILE_NAME=""`, y reemplaza el bind mount de `/app/logs` por un `tmpfs`
(`deploy/deployer/local_flow.py`). No se escribe ningún archivo en el repositorio ni en el host. Los logs se
leen con:

```bash
docker logs -f <contenedor>
```

`LOGGING_FILE_PATH`, `LOGGING_FILE_NAME` y `LOGS_VOLUME` son variables reservadas en local: no se pueden
setear desde el bloque `env:` de `deploy.yml` (ver [`deploy/doc/01-local.md`](../../../deploy/doc/01-local.md)).

### Servidor

El `docker-compose.yml` del stack (para este proyecto,
`deploy/plantillas/spring-3.9-java-25-postgres-18-4/docker-compose.yml`, según `deploy.yml`) monta:

```yaml
- ${LOGS_VOLUME:-/var/log}/${CONTAINER_NAME}:/app/logs
```

y exporta `LOGGING_FILE_PATH=/app/logs` y `LOGGING_FILE_BASENAME=${CONTAINER_NAME}`. El `Dockerfile` del
stack crea `/app/logs` y se lo asigna al usuario `spring`. `LOGS_VOLUME` se configura por servicio en
`deploy.yml` (ver [`deploy/doc/05-env-personalizados.md`](../../../deploy/doc/05-env-personalizados.md)).

**Hoy ese directorio queda vacío:** `logback-spring.xml` no define ningún appender de archivo ni incluye el
`base.xml` de Spring Boot, y `application.server.properties` no declara `logging.file.path` ni
`logging.file.name`, así que las variables anteriores no producen archivos. En servidor los logs también se
leen con `docker logs`.

### Rotación y retención

No hay política de rotación ni de retención definida en el repositorio: no hay `RollingFileAppender`, ni
`maxHistory`, ni `totalSizeCap`, ni configuración de logging driver en los `docker-compose.yml`. Aplica lo
que haga el daemon de Docker del host.

## Cómo loguear desde código nuevo

Declarar el logger con Lombok y usar placeholders `{}` (nunca concatenar):

```java
@Slf4j
@Service
public class ProductoService { ... }
```

`MotorDeConsultas` del kernel es la excepción: usa `LoggerFactory.getLogger(...)` a mano.

### Qué nivel usar

Criterio tomado de cómo se usa hoy en el Core y en `app/`:

| Nivel | Cuándo | Ejemplo real |
|---|---|---|
| `error` | Falla una integración externa o falta algo crítico del arranque | `CaptchaService`, `CrypterService`, `EnviadorDeMailSmtp`; `"FALTA CRÍTICA: El rol SYSADMIN no se encontró tras la sincronización."` en `InicializadorDeBaseService` |
| `warn` | Algo no está configurado y degrada la funcionalidad, o se le responde un error al front | `IntegracionesSmokeTest` cuando una integración no está configurada; `"Respuesta de error al front \| request={} \| ..."` en `ManejadorDeExcepcionesDeApi` |
| `info` | Hitos de arranque y de trazabilidad | `"Smoke test UTC-3: servidor y base de datos validados correctamente"`, `"Rol sincronizado: {}"`, `TRACE_START`/`REQUEST_START` |
| `debug` / `trace` | No se usan en el repositorio. El `ThresholdFilter` del appender está en `INFO`, así que no se imprimirían aunque se agregaran | — |

`Application.main()` usa `System.out.println` para cuatro líneas de arranque (zona horaria forzada,
`java.home`, comando de importación del certificado SSO y `>>> INICIADO CORRECTAMENTE <<<`). Son previas o
ajenas al contexto de Spring y no pasan por Logback.

### Qué no loguear

- **Nunca** contraseñas, tokens, `Authorization`, cookies, secretos ni `apiKey`. El sanitizador cubre lo que
  pasa por `@Trazar` y por `RequestLoggingFilter`, pero **un `log.info("...", password)` a mano no pasa por
  ningún sanitizador**.
- Archivos, base64 ni binarios: agrandan el log y no aportan.
- Si un endpoint con `@Trazar` maneja un campo sensible con un nombre que no está en la lista fija,
  declararlo en `@Trazar(excluir = { "..." })`.
- Datos personales que no hagan falta para diagnosticar (por ejemplo un `email`): también van en `excluir`,
  aunque no sean secretos.

## Propiedades de logging

| Propiedad / variable | Valor actual | Dónde vive |
|---|---|---|
| `logging.level.liquibase.database` | `WARN` | `app/src/main/resources/application.local.properties`, `application.server.properties` |
| `logging.level.org.hibernate.orm.deprecation` | `ERROR` | `app/src/main/resources/application.local.properties`, `application.server.properties` |
| Niveles de Tomcat, Hibernate, Spring Web/Security/Autoconfigure | `WARN` | `app/src/main/resources/logback-spring.xml` |
| `root` y umbral del appender `CONSOLE` | `INFO` | `app/src/main/resources/logback-spring.xml` |
| `DEBUG` (clave de configuración, no una property de Spring) | `0` si no existe en la tabla `configuraciones` | Tabla `configuraciones`, vía `Code/Configuracion/ConfiguracionDesdeBase.java` |
| `LOGGING_FILE_PATH` | `/app/logs` | `docker-compose.yml` del stack (servidor) y `deploy/deployer/local_flow.py` (local) |
| `LOGGING_FILE_BASENAME` | `${CONTAINER_NAME}` | `docker-compose.yml` del stack (solo servidor) |
| `LOGGING_FILE_NAME` | `""` | `deploy/deployer/local_flow.py` (solo local) |
| `LOGS_VOLUME` | `/var/log` | `docker-compose.yml` del stack; se puede sobreescribir en `env:` de `deploy.yml`, solo en servidor |

## Dudas

1. **Las variables de archivo de log no tienen efecto.** El stack monta `/app/logs`, el `Dockerfile` crea el
   directorio y el compose define `LOGGING_FILE_PATH` / `LOGGING_FILE_BASENAME`, pero
   `logback-spring.xml` solo tiene el appender de consola y `application.server.properties` no declara
   `logging.file.path` ni `logging.file.name`. El ejemplo de
   `deploy/doc/ejemplos/configurar-proyecto/application-server.properties` sí las declara. Queda por definir
   si el template debe agregar esas dos properties (y un `RollingFileAppender`) o si el volumen `/app/logs`
   es un resto que ya no aplica.
2. **El comentario de `logback-spring.xml` no coincide con el archivo.** Dice
   `<!-- Salida exclusiva por consola, desde DEBUG. -->` pero el `ThresholdFilter` y el `root` están en
   `INFO`. No está claro si la intención era dejarlo en `DEBUG`.
3. **`TrazabilidadAspect` y `TraceDeEjecucionAspect` se pisan.** Con `DEBUG = 1` y un endpoint anotado con
   `@Trazar`, la misma llamada a un `@Service` de `com.blue.open` genera líneas de los dos aspectos
   (`TRACE_START`/`TRACE_END` y `TRACE ->`/`TRACE <-`). No está documentado en el código si esa duplicación
   es intencional o si uno de los dos quedó como legado.
4. **No hay política de retención.** No encontré configuración de rotación ni de `logging driver` en ningún
   compose del repositorio, así que no puedo documentar cuánto se guardan los logs en servidor.
