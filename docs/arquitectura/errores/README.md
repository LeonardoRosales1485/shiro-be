# Sistema de errores

Cómo viaja un error desde el código hasta el front. Todo el mecanismo vive en
`core-kernel`, en el paquete
[`sm-core-maven/core-kernel/src/main/java/com/blue/open/Core/Kernel/Excepciones/`](https://github.com/SM-SOLUCIONES/sm-core-maven/tree/main/core-kernel/src/main/java/com/blue/open/Core/Kernel/Excepciones):

| Archivo | Qué hace |
|---|---|
| `CodigoDeError.java` | Interfaz que implementa un enum por módulo del Core y uno por proyecto. `codigo()` es el identificador estable que viaja al cliente; `status()` y `mensajePorDefecto()` son el `HttpStatus` y el `mensajeParaUsuario` por defecto de ese código. Cada constante lleva `@Descripcion` con cuándo ocurre, que publica el catálogo de errores del contrato (`docs/api`) |
| `CodigoDeErrorKernel.java` | Códigos propios de `core-kernel`: paginado, borrado lógico, permisos y las respuestas genéricas del handler |
| `ExcepcionDeApi.java` | La excepción única del proyecto. Nunca se serializa directamente: `mensajeParaDesarrollador` y `cause` quedan internos, solo para el log |
| `ErrorResponseDTO.java` | El **body** real de la respuesta de error. Lo arma `ErrorResponseDTO.desde(excepcion)` a partir de una `ExcepcionDeApi` |
| `ManejadorDeExcepcionesDeApi.java` | `@ControllerAdvice` que convierte cualquier excepción en un `ErrorResponseDTO` y la loguea |
| `ExcepcionUtils.java` | `construirMensajeParaDesarrollador(contexto, ex)`: arma el texto técnico con el stacktrace, para el log de `manejarCualquierExcepcion` |
| `SubError.java` | Estructura `nombre` / `mensaje` para el detalle por campo de `errores` |

## Cómo responde la API ante un error

`ManejadorDeExcepcionesDeApi.construirRespuestaDeError(...)` devuelve
`new ResponseEntity<>(ErrorResponseDTO.desde(error), headers, status)`: el body es un DTO propio, no la
excepción serializada. `mensajeParaDesarrollador`, `cause` y el resto de los campos de `Throwable` (`message`,
`stackTrace`, `suppressed`, `localizedMessage`) nunca llegan al cliente, aunque existan internamente en
`ExcepcionDeApi` para el log. Ejemplo de un
`throw new ExcepcionDeApi(CodigoDeErrorCrud.REGISTRO_NO_ENCONTRADO)`:

```json
{
  "fechaHora": "2026-09-22T14:30:45.000-03:00",
  "codigo": "REGISTRO_NO_ENCONTRADO",
  "status": "BAD_REQUEST",
  "mensajeParaUsuario": "No existe el registro",
  "errores": [],
  "traceId": "9f1c2f4e-2f1a-4c9a-9f0e-2b3d4a5c6d7e"
}
```

Con `datos` (solo aparece si no está vacío):

```json
{
  "fechaHora": "2026-09-22T14:30:45.000-03:00",
  "codigo": "STOCK_INSUFICIENTE",
  "status": "BAD_REQUEST",
  "mensajeParaUsuario": "No hay stock suficiente",
  "datos": { "stockActual": 3 },
  "errores": [],
  "traceId": "9f1c2f4e-2f1a-4c9a-9f0e-2b3d4a5c6d7e"
}
```

| Campo | Origen | Significado |
|---|---|---|
| `fechaHora` | `FechaHoraConfig.ahoraLocalDateTime()` en el constructor | Momento del error en GMT-3, con el patrón `yyyy-MM-dd'T'HH:mm:ss.SSSXXX` |
| `codigo` | El `CodigoDeError` del constructor | Identificador **estable**: el front discrimina por acá, nunca por `mensajeParaUsuario` |
| `status` | `codigo.status()` | Mismo valor que el status HTTP de la respuesta, como nombre del enum |
| `mensajeParaUsuario` | `codigo.mensajePorDefecto()`, o el que pasa el call site | Mensaje para el usuario final. Es lo que muestra el front |
| `datos` | `ExcepcionDeApi.conDato(clave, valor)` | Datos que el cliente necesita para resolver el error. Se omite si está vacío. Una clave sensible (según `SanitizadorDeLogs`) se descarta sola, con un `warn` en el log |
| `errores` | `agregarSubError(nombre, mensaje)` | Lista de `SubError` (`nombre` + `mensaje`, sin `value`). La completa `handleMethodArgumentNotValid` con el detalle por campo de un `@Valid` |
| `traceId` | `MDC.get("traceId")` en el constructor | Correlaciona la respuesta con los logs. El mismo valor viaja en el header `X-Trace-Id` |

`mensajeParaDesarrollador` y `cause` existen como campos de `ExcepcionDeApi` pero son **internos**: solo se
loguean (ver "Qué se loguea"), nunca se responden. Es la razón del nombre: son para quien lee el log, no
para el cliente HTTP.

Ejemplos reales de estos bodies por endpoint del ABM de una entidad, en la documentación de `core-crud`:
[`guardar.md`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/guardar.md) y [`borrar.md`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/borrar.md).

## Qué captura el handler

`ManejadorDeExcepcionesDeApi` extiende `ResponseEntityExceptionHandler`:

| Método | Excepción | Código |
|---|---|---|
| `manejarExcepcionDeApi` | `ExcepcionDeApi` | El que trae la excepción |
| `handleMethodArgumentNotValid` (override) | `MethodArgumentNotValidException` (`@Valid`) | `CodigoDeErrorKernel.FORMULARIO_INVALIDO`, con un `SubError` por campo en `errores` |
| `handleHttpMessageNotReadable` (override) | `HttpMessageNotReadableException` (JSON ilegible, tipo incorrecto) | `CodigoDeErrorKernel.CUERPO_INVALIDO` |
| `handleHttpRequestMethodNotSupported` (override) | `HttpRequestMethodNotSupportedException` | `CodigoDeErrorKernel.METODO_NO_SOPORTADO` |
| `handleHttpMediaTypeNotSupported` (override) | `HttpMediaTypeNotSupportedException` | `CodigoDeErrorKernel.MEDIA_TYPE_NO_SOPORTADO` |
| `manejarCualquierExcepcion` | `Exception` (todo lo demás) | `CodigoDeErrorKernel.ERROR_INTERNO`. El stacktrace completo se loguea (`ExcepcionUtils.construirMensajeParaDesarrollador`), pero nunca se responde |

Con estos cinco overrides, toda excepción que puede llegar a un controller responde con `ErrorResponseDTO`
y un código: no queda ningún caso que se escape al formato por default de Spring.

Una operación del ABM que el controller no escribe responde `404`, no `403`, porque no existe (ver
[`../core/README.md`](../core/README.md)).

### Autorización

No hay `AuthenticationEntryPoint` ni `AccessDeniedHandler`: `SeguridadWebConfig` (`core-identity`) deja
pasar todas las rutas (`anyRequest().permitAll()`) y la autorización la resuelve `InterceptorDeContrato`
(`core-kernel`) con el `permiso` del `@Contrato` de cada endpoint, antes de ejecutarlo (y antes de `@Valid`),
lanzando `ExcepcionDeApi`. Lo que depende de los datos lo resuelve el código con `AuthService.tienePermisos`.
Por eso los errores de auth tienen el mismo formato que el resto.

Desde Core 3.2.0 los dos casos se distinguen por `codigo` y por status:

| Código | Status | Cuándo |
|---|---|---|
| `NO_AUTENTICADO` | `401` | La request no identifica a ningún usuario (sin token) y el endpoint exige sesión: `Contrato.AUTENTICADO`, un rol o un permiso. Lo lanzan `AuthService.getActualObligatorio()` (y por lo tanto `autenticar()`) y, en un proyecto sin `core-identity`, `AutorizadorDenegarTodo` |
| `SIN_PERMISO` | `403` | Hay un usuario autenticado, pero le falta el rol, el permiso o el alcance (empresa, dueño del registro, escalación de permisos) |

Un token vencido o inválido no cae en ninguno de los dos: responde el `401` propio de su protocolo
(`TOKEN_VENCIDO` y `TOKEN_INVALIDO` en JWT, `ACCESS_TOKEN_INVALIDO` en tokens de acceso personal). El front trata `NO_AUTENTICADO` como "iniciar sesión" y `SIN_PERMISO` como
"no tiene acceso"; nunca al revés.

## `ExcepcionDeApi`

Constructores disponibles (el `status` sale siempre de `codigo.status()`):

| Constructor | `mensajeParaUsuario` |
|---|---|
| `ExcepcionDeApi(CodigoDeError codigo)` | `codigo.mensajePorDefecto()` |
| `ExcepcionDeApi(CodigoDeError codigo, String mensajeParaDesarrollador)` | `codigo.mensajePorDefecto()` |
| `ExcepcionDeApi(CodigoDeError codigo, String mensajeParaDesarrollador, Throwable cause)` | `codigo.mensajePorDefecto()` |
| `ExcepcionDeApi(CodigoDeError codigo, String mensajeParaUsuario, String mensajeParaDesarrollador)` | El que pasa el call site |

`conDato(clave, valor)` es encadenable y agrega una entrada a `datos`. Ignora `valor == null` (la clave
ausente significa "el backend no lo pudo informar") y descarta, con un `warn` en el log, cualquier clave
sensible según el mismo criterio que usa `SanitizadorDeLogs` para los logs.

### `mensajeParaUsuario` vs. `mensajeParaDesarrollador`

- **`mensajeParaUsuario`**: lo lee una persona. Va en español, con tildes (es texto visible por el usuario final,
  la excepción que permite [`idioma-y-estilo.md`](../../rules/idioma-y-estilo.md)), sin nombres de clase ni
  datos internos. El front no discrimina por este campo: las pruebas de contrato assertean `codigo`.
- **`mensajeParaDesarrollador`**: lo lee quien depura en el log. **Nunca viaja en la respuesta HTTP.** La convención
  que sigue el código del proyecto es `Clase | metodo | detalle`, por ejemplo:

```java
// app/src/main/java/com/blue/open/Code/Empresa/EmpresaService.java
throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CUIT_INVALIDO,
        "EmpresaService | hookGuardarAplicarDto | digito verificador invalido: " + dto.getCuit());
```

Como no se responde, `mensajeParaDesarrollador` puede llevar más detalle técnico que antes (nombres de excepción,
resultados intermedios) siempre que no incluya secretos, tokens ni contraseñas: sigue pasando por
`SanitizadorDeLogs` antes de escribirse en el log, pero eso es una red de seguridad, no un permiso.

## Errores de validación `@Valid`

Las validaciones se declaran en el DTO, con mensaje por campo (ver
[`entidades-y-dtos.md`](../../rules/entidades-y-dtos.md)):

```java
// app/src/main/java/com/blue/open/Code/Configuracion/ConfiguracionGuardarRequestDTO.java
@NotBlank(message = "El nombre de la configuracion es obligatorio")
private String nombre;
```

`handleMethodArgumentNotValid` recorre `ex.getBindingResult().getAllErrors()` y agrega un `SubError` por
campo con `agregarSubError(campo, mensaje)`:

```json
{
  "codigo": "FORMULARIO_INVALIDO",
  "status": "BAD_REQUEST",
  "mensajeParaUsuario": "Error en el formulario",
  "errores": [
    { "nombre": "nombre", "mensaje": "El nombre de la configuracion es obligatorio" }
  ]
}
```

El método castea cada error a `FieldError`, así que solo contempla validaciones de campo. Cubre
`MethodArgumentNotValidException`, que es lo que lanza `@Valid @RequestBody DTO`
(el `guardar` de la receta del ABM, `UsuarioController.cambiarContrasena`, `JwtLoginController.iniciarSesion`,
`AccessTokenController.crearToken`, el registro público de `UsuarioController`). Una validación sobre los elementos de
una colección, como `@RequestBody List<@Valid ConfiguracionGuardarRequestDTO>` en
`ConfiguracionController.guardar`, Spring la lanza como `HandlerMethodValidationException`: desde Core 2.5.1
la resuelve `handleHandlerMethodValidationException`, también con `FORMULARIO_INVALIDO` y un subError por campo
cuyo `nombre` lleva el índice del elemento (`[0].valor`).

## Cómo lanzar un error desde código nuevo

1. Siempre `ExcepcionDeApi` del kernel, con un `CodigoDeError` (`import
   com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;`). No se crean excepciones propias por entidad ni
   se devuelven `ResponseEntity` de error a mano: el `@ControllerAdvice` ya arma el body.
2. Elegir el código del enum que corresponda al módulo (Core) o al proyecto
   (`CodigoDeErrorDelProyecto`). Si la situación no tiene un código todavía, ver
   [`docs/rules/errores.md`](../../rules/errores.md) antes de crear uno nuevo.
3. Pasar `mensajeParaDesarrollador` con `Clase | metodo | detalle` cuando el `mensajeParaUsuario` por defecto del código no
   alcanza para depurar. No hace falta si el mensaje de usuario ya lo dice todo.
4. Si el cliente necesita un dato puntual para resolver el error (el valor vigente de una versión, un
   límite), agregarlo con `.conDato(clave, valor)`.
5. Dónde lanzarla, según la regla de
   [`controllers-y-endpoints.md`](../../rules/controllers-y-endpoints.md):
   - **UseCase**: el registro no existe, el estado no permite la operación, el dato no pasa la regla del caso de uso (por ejemplo `ConfiguracionObtenerVariasUseCase`, que responde `403` nombrando las configuraciones privadas).
   - **Entidad**: las invariantes de su propio estado (el ejemplo `descontarStock` de esa misma regla lanza `ExcepcionDeApi`).
   - **Service que extiende `AbstractService`**: dentro de un hook, como hace `EmpresaService.hookGuardarAplicarDto`.
   - **Controller**: solo cuando el error depende de la request y no del dato.
6. Si se envuelve otra excepción, usar el constructor con `Throwable cause` o
   `ExcepcionUtils.construirMensajeParaDesarrollador(...)`: el detalle queda en el log, nunca en la respuesta.
7. Los cuatro métodos de `AbstractService` (`obtenerPorUuid`, `paginar`, `guardar`, `borrar`) envuelven su
   cuerpo en `try { ... } catch (ExcepcionDeApi e) { throw e; } catch (Exception e) { ... }`: una
   `ExcepcionDeApi` lanzada desde un hook **llega intacta** al front, mientras que cualquier otra excepción
   se reemplaza por un código genérico de `CodigoDeErrorCrud`. Es otra razón para lanzar `ExcepcionDeApi` y
   no una excepción cualquiera.
8. Probar el error: cada escenario de error va como un método de la prueba de contrato del caso de uso,
   asertando `codigo` con `esperarError(status, codigo)` (ver [`../testing/README.md`](../testing/README.md)).

## Qué se loguea

`construirRespuestaDeError` loguea **una línea WARN por respuesta de error**, sea `400` o `500`:

```
Respuesta de error al front | request={} | payload={} | status={} | codigo={} | mensajeParaUsuario={} | mensajeParaDesarrollador={} | datos={} | errores={}
```

- `payload` es el body de la request, tomado del `ContentCachingRequestWrapper` que instala
  `RequestLoggingFilter`, y pasa por `SanitizadorDeLogs.sanitizarCuerpo(...)`, que redacta secretos y binarios.
  Si la request no viene envuelta o no tiene body, queda vacío.
- `datos` y `errores` pasan por `SanitizadorDeLogs.sanitizarValor(...)` antes de loguearse, igual que el
  payload.
- Cada línea del log lleva el `traceId` del MDC por el patrón de
  `app/src/main/resources/logback-spring.xml`, que es el mismo `traceId` del body y del header `X-Trace-Id`.
- En `manejarCualquierExcepcion` el stacktrace no se pierde: `ExcepcionUtils.construirMensajeParaDesarrollador(...)` lo imprime dentro del
  `mensajeParaDesarrollador`, que se loguea en esa misma línea, pero **no** se responde al cliente.
- Aparte del handler, `TrazabilidadAspect` loguea a nivel ERROR un
  `TRACE_END ... status=ERROR ... exceptionType=...` para los métodos anotados con `@Trazar` que terminan en
  excepción.

## Errores fuera del ciclo HTTP

El `@ControllerAdvice` solo cubre requests que llegan a un controller. Fuera de eso:

### WebSocket (STOMP)

`app/src/main/java/com/blue/open/Code/Common/WebSocket/WebSocketAuthInterceptor.java` autentica el
`CONNECT` y lanza `ExcepcionDeApi` en tres casos (falta el header `Authorization`, el header no empieza
con `Bearer `, o el token es inválido o expiró). Se lanza desde un `ChannelInterceptor`, no desde un
controller: el `@ControllerAdvice` no interviene y el cliente lo recibe como un frame `ERROR` de STOMP, no
como el body JSON de arriba.

### Arranque de la aplicación

Los errores de arranque no producen respuesta: cortan el proceso o quedan en un log.

| Componente | Cuándo corre | Ante un problema |
|---|---|---|
| `ZonaHorariaSmokeTestService` (`core-kernel`) | `ApplicationReadyEvent`, `@Order(1)`, no en el profile `test` | Lanza `IllegalStateException` si el servidor o la base no están en UTC-3 |
| `IntegracionesSmokeTest` (`core-kernel`) | `ApplicationReadyEvent`, `@Order(2)` | Solo loguea: `INFO` si la integración está configurada, `WARN` si no. **Nunca corta**, las integraciones son opcionales |
| `UrlsDelFrontProperties.validar()` (`app`) | `@PostConstruct` | Lanza `IllegalStateException` si falta `front.url`, cortando el arranque |

Es la aplicación de la regla de variables de entorno e integraciones de
[`integraciones-y-entorno.md`](../../rules/integraciones-y-entorno.md): lo obligatorio corta el arranque, lo
opcional queda en un log.
