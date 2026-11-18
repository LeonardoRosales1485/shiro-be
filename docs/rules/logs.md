# Logs

Cómo se escribe una línea de log desde `Code`. Cómo está armado el sistema (Logback, `traceId`, `@Trazar`,
`SanitizadorDeLogs`, dónde terminan los archivos) está en
[`docs/arquitectura/logs/README.md`](../arquitectura/logs/README.md); esto es la regla de cumplimiento
obligatorio.

## Logger

- Toda clase que loguea lleva `@Slf4j` (ver [idioma-y-estilo.md](idioma-y-estilo.md)) y usa `log`. No se declara un
  `Logger` a mano con `LoggerFactory.getLogger(...)`.
- Está prohibido `System.out`, `System.err` y `printStackTrace()`: no pasan por Logback, no llevan `traceId` y no
  respetan los niveles.

## Formato del mensaje

- El mensaje describe **qué pasó** y después lleva los datos como `clave=valor`, separados por ` | `, igual que el
  Core (`ManejadorDeExcepcionesDeApi`, `RequestLoggingFilter`):

  ```java
  log.info("Factura emitida | uuid={} | empresaUuid={} | total={}", factura.getUuid(), empresaUuid, total);
  log.warn("Reintento de envio al proveedor | intento={} | motivo={}", intento, motivo);
  ```

- Los valores se pasan como argumentos con `{}`. Nunca se concatena (`"uuid=" + uuid`): se pierde el lazy
  evaluation y es la vía más común por la que un `toString()` de una entidad termina en el log.
- No se repite el nombre de la clase en el mensaje: el patrón de `logback-spring.xml` ya imprime `%logger{36}`.
  (El `mensajeParaDesarrollador` de `ExcepcionDeApi` tiene su propia convención `Clase | metodo | dato`, ver
  [errores.md](errores.md); esa regla no cambia.)
- El mensaje es código, no texto para el usuario final: en español, sin tildes ni eñes (ver
  [idioma-y-estilo.md](idioma-y-estilo.md)).
- Toda línea sobre un registro de negocio incluye su `uuid`, y en multiempresa el `empresaUuid`. Nunca el `id`
  interno.

## Niveles

| Nivel | Cuándo | Ejemplos |
|---|---|---|
| `ERROR` | Algo falló y alguien tiene que mirarlo: una integración externa caída, una invariante rota, un dato inconsistente en la base. Siempre con la excepción como último argumento (`log.error("...", e)`) para que salga el stacktrace | `Crypter no responde`, `El rol SYSADMIN no existe tras la sincronizacion` |
| `WARN` | Situación anómala que el sistema resolvió solo: una configuración ausente con valor por defecto, un dato sensible descartado, un reintento que salió bien, una respuesta de error al cliente | `core.identity.usuario-inicial sin configurar`, `Respuesta de error al front` |
| `INFO` | Hitos: arranque, smoke tests, inicializaciones, y las operaciones de negocio críticas (las mismas que llevan `@Trazar`: dinero, pagos, comprobantes) | `Smoke test UTC-3 validado`, `Pago acreditado` |
| `DEBUG` | Detalle para diagnosticar un problema puntual en local. En servidor está apagado (`root` en `INFO`) | Valores intermedios de un cálculo |

- No se usa `TRACE` a mano: la traza de ejecución la da `DEBUG = 1` (`TraceDeEjecucionAspect`).
- Un error que se convierte en una respuesta `4xx` al cliente (validación, registro inexistente, sin permiso) no
  es `ERROR`: el handler ya lo loguea en `WARN` con el `codigo`. `ERROR` queda para lo que el cliente no causó.

## Qué no se loguea

`SanitizadorDeLogs` solo cubre lo que pasa por Jackson: los payloads que imprime `RequestLoggingFilter`, los
argumentos y resultados de `@Trazar` y los `datos` de `ExcepcionDeApi.conDato`. **Un `log.info("contrasena={}",
dto.getContrasena())` no pasa por el sanitizador.** Por eso, a mano, nunca se loguea:

- Secretos y credenciales: contraseñas, tokens, cookies, `Authorization`, api keys, secretos de clientes, códigos de
  verificación. Es la misma lista de raíces del sanitizador, y aplica aunque el valor venga hasheado.
- Datos personales más allá del `uuid`: email, DNI, CUIT, teléfono, nombre completo. Para identificar a un usuario
  en el log alcanza su `uuid`.
- Entidades, DTOs o colecciones enteras (`log.info("usuario={}", usuario)`): el `toString()` filtra campos sensibles
  y en una entidad navega relaciones LAZY (ver "Lombok" en [idioma-y-estilo.md](idioma-y-estilo.md)). Se loguean los
  campos puntuales que hacen falta.
- Contenido de archivos, `byte[]` ni base64. El campo de un DTO que lleva el contenido de un archivo se llama
  `contenidoDelArchivo` para que el sanitizador lo tape (ver [archivos.md](archivos.md)).
- `e.getMessage()` de una librería o servicio externo interpolado en el mensaje: la excepción va como último
  argumento y el stacktrace sale completo, en una sola línea de `ERROR`.

## Cuándo no se loguea

- **No se loguea y se lanza por la misma condición.** Si el caso de uso lanza `ExcepcionDeApi`, el handler ya escribe
  la línea de `WARN` con el `codigo`, el `traceId` y el payload sanitizado: no se agrega un `log.error` antes del
  `throw`. La excepción es el `catch` de una integración externa que se traduce a `ExcepcionDeApi`: ahí sí va un
  `log.error` con la excepción original como último argumento, porque el handler no la ve.
- **No se loguea "entrando a" / "saliendo de" un método.** El inicio y fin de cada request ya lo escribe
  `RequestLoggingFilter`, y el detalle por método lo dan `@Trazar` (endpoints críticos) y `DEBUG = 1` (local).
- **No se loguea dentro de un loop por registro.** Una línea con el total y, si hace falta, la lista de `uuid`
  afectados.
- **No se loguea una excepción que se vuelve a lanzar** (`catch` + `log.error` + `throw e`): se loguea donde se
  maneja o se deja llegar al handler. Loguearla en cada nivel la duplica.

## Configuración

- `app/src/main/resources/logback-spring.xml` no se edita en un proyecto: ni el patrón de línea, ni el appender
  (`CONSOLE`), ni se agrega un `FileAppender`. Los logs salen por stdout y los captura Docker (ver "Dónde terminan
  los logs" en el README de arquitectura). Si un proyecto necesita otro formato o destino, es un issue al Core.
- El nivel de un paquete se ajusta en `application.*.properties` (`logging.level.{paquete}`), no en el XML.
- `logging.level.org.hibernate.SQL=DEBUG` se usa en `application.local.properties` para verificar consultas (ver
  [consultas.md](consultas.md)) y no se commitea.
