# Checklist de auditoría

Una fila por regla verificable. Cómo se usa, qué significa cada columna y cómo se agrega una fila:
[`README.md`](README.md). La regla completa está en el archivo que encabeza cada sección; si hay contradicción,
manda la regla.

Severidad: **B** = bloqueante, **A** = advertencia. Verificada por: `suite (Test)`, `arranque` o `agente`.

## CORE — Alcance de modificación ([`rules/controllers-y-endpoints.md`](../rules/controllers-y-endpoints.md), [`arquitectura/core/README.md`](../arquitectura/core/README.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| CORE-01 | B | El Core llega solo como dependencia publicada: el proyecto no tiene su código | Un directorio `core/` en el proyecto, o una clase en `com.blue.open.Core` dentro de `app/src` | suite (`CorePublicadoTest`) |
| CORE-02 | B | El Core nunca depende de `com.blue.open.Code` | Clases de `..Core..` con dependencias hacia `..Code..` | suite (`ArquitecturaTest`) |
| CORE-03 | B | Una necesidad del proyecto se resuelve por la escalera (configuración → hook → puerto → no usar el módulo → issue al Core), no copiando o reimplementando una clase del Core en `Code` | Clase en `Code` que duplica el nombre o el cuerpo de una del Core (`AbstractService`, `AuthService`, un `Proyector` propio, un handler de excepciones propio) | agente |
| CORE-04 | A | Antes de crear un service, integración o utilitario se reutiliza lo que el Core trae | `RestClient`/`HttpClient` propio hacia SM3, Crypter, mail o captcha; un `Utils` propio con helpers que ya existen en `Utils` del kernel | agente |

## EJE — Ejecución ([`rules/ejecucion-local.md`](../rules/ejecucion-local.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| EJE-01 | B | Existen `deploy/` y `deploy.yml` y no se modificó `deploy/` | `git diff` con cambios bajo `deploy/` sin pedido explícito | agente |
| EJE-02 | A | Un `Dockerfile` heredado no copia `target/*.jar` (el jar está en `app/target/`) | `COPY target/` en un Dockerfile del proyecto | agente |

## GIT — Git ([`rules/git.md`](../rules/git.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| GIT-01 | A | Commits chicos con mensaje que explica qué cambió | Un commit que toca más de una entidad o mezcla regla + funcionalidad; mensajes tipo `fix`, `cambios` | agente |
| GIT-02 | B | `docs/api` regenerado y commiteado antes de `develop → staging` | `docs/api/openapi.yaml` con `info.version` distinta a `app/pom.xml`, o endpoints del controller ausentes del contrato | agente |

## VER — Versionado ([`rules/versionado.md`](../rules/versionado.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| VER-01 | B | Toda modificación incrementa `<version>` de `app/pom.xml` en +0.0.1 (no aplica al template, que queda en `1.0.0`) | `git diff` con cambios en `app/src` y sin cambio en `app/pom.xml` `<version>` | agente |
| VER-02 | B | La versión no se escribe a mano en código | Literal de versión (`"1.4.3"`) en Java o properties fuera de `pom.xml` | agente |
| VER-03 | B | La versión del Core solo cambia a una versión publicada en `sm-core-maven`, hacia adelante y siguiendo su changelog | `<parent><version>` de `core-parent` en `app/pom.xml` que es `-SNAPSHOT`, menor que la anterior, o que cambia sin aplicar los pasos del changelog | agente |
| VER-04 | B | Actuator expone `/health` e `/info` con `version` y `coreVersion`; toda respuesta lleva `API-Version` | `ActuatorTest` | suite (`ActuatorTest`) |

## IDF — Identificadores fijos ([`rules/identificadores-fijos.md`](../rules/identificadores-fijos.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| IDF-01 | B | `groupId` `com.blue.open`, `artifactId` y `<name>` `open-data-proyect`, paquete raíz `com.blue.open` | Cualquier otro valor en `app/pom.xml` o un paquete fuera de `com.blue.open` | suite (`EstiloDeCodigoTest`) |

## IDI — Idioma y estilo ([`rules/idioma-y-estilo.md`](../rules/idioma-y-estilo.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| IDI-01 | B | El código se escribe en español | Clases, métodos, campos, rutas, columnas o tablas en inglés que no estén en la lista cerrada "Qué queda en inglés" (`User`, `Company`, `findAll`, `/save`, `created_at`) | agente |
| IDI-02 | B | Sin tildes ni eñes en identificadores, comentarios ni strings que no vea el usuario final | Carácter no ASCII en un `.java` de `app/` fuera de un string literal | suite (`EstiloDeCodigoTest`) |
| IDI-03 | B | `@RequiredArgsConstructor` en toda clase concreta con dependencias, `@Slf4j` en toda clase que loguea | Constructor escrito a mano solo para asignar `final`; `LoggerFactory.getLogger` | agente |
| IDI-04 | B | Lombok prohibido: `@Data`, `lombok.Value`, `@SneakyThrows` en cualquier lado; `@ToString` y `@EqualsAndHashCode` en `@Entity`; `@AllArgsConstructor` en un bean | Presencia de la anotación en un `.java` de `app/` | suite (`EstiloDeCodigoTest`) |
| IDI-05 | A | Comentario obligatorio en toda función, clase, DTO, entidad y columna ambigua: qué hace, de qué depende, qué recibe, qué responde | Método público o clase sin comentario encima | agente |
| IDI-06 | B | `@Descripcion` en todo campo de DTO expuesto y en todo `@PathVariable`, `@RequestParam` y `@RequestHeader` | La prueba de contrato falla si falta | suite (pruebas de contrato) |

## EST — Estructura de carpetas ([`rules/estructura-de-carpetas.md`](../rules/estructura-de-carpetas.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| EST-01 | B | `Code` se organiza por entidad, nunca por tipo | Paquetes `Code.Controller`, `Code.Service`, `Code.Repository`, `Code.Entity`, `Code.DTO` o equivalentes | suite (`ArquitecturaTest`) |
| EST-02 | B | Dentro de `Code/{Entidad}/` todo va plano; solo `CasosDeUso/` y `Notificaciones/` como subcarpetas | Subpaquete de una entidad con otro nombre | suite (`ArquitecturaTest`) |
| EST-03 | B | Un archivo vive en la carpeta de la entidad de la que depende, no de quien lo llama; un caso de uso multi-entidad vive en la entidad principal (la de la URL) | `UseCase` cuyo controller está en otra entidad; validador de un campo de `X` en la carpeta de `Y` | agente |
| EST-04 | B | Roles, permisos y política de asignación en `Code/Autorizacion/`; infraestructura transversal en `Code/Common/{Concern}/` | `RolEnum`/`PermisoEnum` fuera de `Autorizacion`; una `@Configuration` en la carpeta de una entidad | agente |
| EST-05 | B | Los tests espejan exactamente la estructura de `main` | Paquete de test que no existe en `main` | suite (`ArquitecturaTest`) |
| EST-06 | A | No se crea la capa de agrupación por dominio ni un módulo Maven nuevo sin coordinarlo con el usuario | Carpeta intermedia nueva entre `Code` y la entidad; `pom.xml` nuevo | agente |

## ENT — Entidades y DTOs ([`rules/entidades-y-dtos.md`](../rules/entidades-y-dtos.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| ENT-01 | B | Se parte de los casos de uso, no de la entidad: toda entidad tiene solo los archivos que algún caso de uso necesita | Controller/Service/DTO de una entidad hija que solo se modifica desde el caso de uso de otra | agente |
| ENT-02 | B | Toda `@Entity` de `Code` hereda de `EntidadConBorradoLogico` (default) o `EntidadBase` (borrado físico) | `@Entity` que no es asignable a `EntidadBase` | suite (`ArquitecturaTest`) |
| ENT-03 | B | Todo DTO hereda de `EntidadBaseDTO` | Clase `*DTO` de `Code` no asignable a `EntidadBaseDTO` | suite (`ArquitecturaTest`) |
| ENT-04 | B | Los endpoints nunca responden el `id` interno: el front conoce solo el `uuid` | Campo `id` (o `{Entidad}Id`) en un DTO; relación como `{OtraEntidad}Id` en vez de `{OtraEntidad}Uuid` | agente |
| ENT-05 | B | Todo DTO de escritura tiene validaciones de Bean Validation con mensaje por campo, derivadas de los `NOT NULL` del schema y de `docs/README.md` | Campo obligatorio en el schema sin `@NotNull`/`@NotBlank`; anotación sin `message`; `@RequestBody` sin `@Valid` | agente |
| ENT-06 | B | Fechas en DTOs con `@JsonFormat(... pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")` | Campo `Timestamp`/`Date`/`LocalDateTime` en un DTO sin esa anotación | suite (`ArquitecturaTest`) |
| ENT-07 | B | Convención `{Entidad}{CasoDeUso}RequestDTO` / `ResponseDTO`; un solo DTO si tienen los mismos campos; DTOs iguales se comparten | Dos DTOs con los mismos campos; nombre que no sigue la convención | agente |
| ENT-08 | B | Campos sensibles marcados `@NoProyectable` (o `@JsonIgnore`) | Campo `contrasena`, `codigo`, `secreto` en una entidad sin la anotación | agente |
| ENT-09 | B | Un campo que solo cambia por un método de negocio lleva `@Setter(AccessLevel.NONE)`; con concurrencia, `@Version` | Setter público sobre `stock`, `saldo`, `estado` que un método de la entidad también modifica | agente |

## CTR — Controllers, Services y Endpoints ([`rules/controllers-y-endpoints.md`](../rules/controllers-y-endpoints.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| CTR-01 | B | Todo endpoint lleva `@Contrato` con `permiso` válido | La aplicación no arranca | arranque |
| CTR-02 | B | El permiso fijo va solo en `@Contrato`; la autorización que depende de datos va en el `UseCase`/hook con `tienePermisos` y `SIN_PERMISO` | `if` sobre un rol/permiso fijo en el cuerpo de un método; `authService.valid` | agente |
| CTR-03 | B | Los controllers no heredan del Core; el ABM se escribe con la receta de `core-crud`, solo los endpoints que la entidad necesita | Controller con `extends`; endpoint del ABM con otra ruta (`/save`, `/list`) | suite (`ArquitecturaTest`) + agente |
| CTR-04 | B | Un caso de uso que no es ABM completo lleva `{Entidad}{CasoDeUso}UseCase` en `CasosDeUso/`, `@Service`, un único método público `ejecutar` | Clase `*UseCase` con más de un método público o con otro nombre; método de caso de uso agregado al `Service` de la entidad | suite (`ArquitecturaTest`) + agente |
| CTR-05 | B | El `Service` de la entidad no crece con casos de uso: solo hooks del ABM | Método público en un `*Service` que extiende `AbstractService` y no es un `hook*` | suite (`ArquitecturaTest`) |
| CTR-06 | B | Rutas de caso de uso: `GET /{uuid}/{caso-de-uso}`, `POST /{uuid}/{caso-de-uso}` (update parcial, valida `status != -1`), `POST /{caso-de-uso}` (alta parcial) | Ruta que no sigue el esquema; `PUT`/`PATCH`; update parcial sin validar eliminado | agente |
| CTR-07 | B | `@Trazar` solo en endpoints críticos (dinero, pagos, comprobantes), con `excluir` de campos sensibles | `@Trazar` en ABM, lecturas, login, configuración; `@Trazar` sin `excluir` sobre un DTO con datos sensibles | agente |
| CTR-08 | B | Imports arriba, nunca referencias inline con nombre completo | `com.blue.open.` dentro del cuerpo de un método o de un cast | suite (`EstiloDeCodigoTest`) |
| CTR-09 | A | Lógica en la entidad solo si la usan 2+ casos de uso o es una invariante; sin dependencias, sin relaciones LAZY no cargadas, sin cruzar módulos; con test unitario | Método de negocio en una entidad usado por un solo caso de uso; entidad que recibe un repository | agente |
| CTR-10 | B | Lectura y escritura del caso de uso con Proyección (`Proyector` o `SELECT new`), nunca convirtiendo la entidad a mano | Clase `*Mapper`, método `toDto`/`aDto`/`fromEntity`; `new XDTO()` seguido de setters desde una entidad | agente |

## DI — Inyección de dependencias ([`rules/inyeccion-de-dependencias.md`](../rules/inyeccion-de-dependencias.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| DI-01 | B | Prohibido `@Autowired` sobre un campo (en `Code` y en el Core) | Campo anotado `@Autowired` fuera de un `@SpringBootTest` | suite (`ArquitecturaTest`) |
| DI-02 | B | Clase concreta: `private final` + `@RequiredArgsConstructor`; sin `@Autowired` en el constructor | Campo de dependencia no `final` o no `private`; constructor con `@Autowired` | agente |
| DI-03 | B | Inyección por setter solo en clases base del Core; en `Code` no hay clases base con dependencias | Setter `@Autowired` en una clase de `Code` | suite (`ArquitecturaTest`) |
| DI-04 | B | Dependencia opcional como `ObjectProvider<T>`, no `@Autowired(required = false)` | `required = false` | suite (`EstiloDeCodigoTest`) |
| DI-05 | A | Un ciclo se rompe moviendo la lógica a un tercer bean; `@Lazy` solo si cruza un módulo del Core, con comentario | `@Lazy` en `Code` sin comentario que explique el ciclo | agente |
| DI-06 | B | En tests, la clase bajo prueba se construye con `new`; sin `@InjectMocks` ni `ReflectionTestUtils.setField` salvo para `@Value` | `@InjectMocks`; `setField` sobre un campo que no es `@Value` | suite (`EstiloDeCodigoTest`) + agente |

## BD — Base de datos ([`rules/base-de-datos.md`](../rules/base-de-datos.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| BD-01 | B | Todo cambio de esquema va por Liquibase; `ddl-auto` nunca crea ni actualiza | `spring.jpa.hibernate.ddl-auto` distinto de `none`/`validate`; columna nueva en una entidad sin changeset | agente |
| BD-02 | B | Un changeset publicado no se edita ni se borra | `git diff` que modifica un changeset ya existente en `develop` | agente |
| BD-03 | B | Renombrar una columna es `sql` con `ALTER TABLE ... RENAME COLUMN`, nunca `renameColumn` | `renameColumn` en un changeset nuevo del diff (uno ya publicado no se edita, BD-02: se repara con un changeset puente, como `2026-10-09-restaurar-not-null-configuraciones.yaml` del template) | agente |
| BD-04 | B | Toda entidad o funcionalidad nueva incluye seeds de ejemplo | Tabla nueva sin entrada en `db/changelog/seeds/` | agente |
| BD-05 | B | Columnas del proyecto sobre tablas del Core van en el changelog del proyecto; el master solo incluye los módulos declarados en `pom.xml` | `include` de un `core-*.yaml` cuyo módulo no está en `app/pom.xml`, o al revés | agente |

## CON — Consultas ([`rules/consultas.md`](../rules/consultas.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| CON-01 | B | Toda relación JPA con `fetch = FetchType.LAZY` | `@ManyToOne`/`@OneToOne` sin `fetch = LAZY` (son EAGER por defecto) | suite (`ArquitecturaTest`) |
| CON-02 | B | Sin N+1: una relación de muchos registros se trae con `JOIN FETCH`, `@EntityGraph` o `@Coleccion` | Loop sobre una lista de entidades que lee una relación o llama a un repository por elemento | agente |
| CON-03 | B | Lectura por Proyección: solo las columnas del DTO | `findAll`/`findBy` que devuelve la entidad para después armar el DTO; `SELECT e FROM` donde alcanza `SELECT new` | agente |
| CON-04 | B | Método derivado para búsquedas simples; `@Query` JPQL para lo no trivial; nativo solo con comentario que diga por qué | `nativeQuery = true` sin comentario; `findByAAndBAndCOrderByD...` ilegible | suite (`EstiloDeCodigoTest`) + agente |
| CON-05 | B | Parámetros con nombre, nunca concatenados en la consulta | `"... WHERE x = " + valor` o `createQuery` con string armado | agente |
| CON-06 | B | Consultas sobre entidades con borrado lógico filtran `status = 0` | `@Query` sobre una entidad con `status` sin la condición, sin comentario que explique por qué | agente |
| CON-07 | B | Un repository nunca devuelve `null`: `Optional<T>`, `List<T>`, `boolean`, `long`, `Pagina<T>`; nunca `Optional<List<T>>`; sin `Optional` en campos ni parámetros | Firma de repository que devuelve `T` para 0-o-1; `Optional<List>`; campo `Optional` | suite (`ArquitecturaTest`) |
| CON-08 | B | Sin `.get()` sin `isPresent()` previo ni `.orElse(null)` sin contrato documentado | `.get()` sobre un `Optional` sin guard; `.orElse(null)` sin comentario | agente |
| CON-09 | A | Al crear o modificar un endpoint que consulta, se revisó el SQL generado | Sin evidencia en el PR; `logging.level.org.hibernate.SQL` commiteado | agente |

## ERR — Errores ([`rules/errores.md`](../rules/errores.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| ERR-01 | B | Todo error es `ExcepcionDeApi` con código; sin `ResponseEntity` de error a mano ni otras excepciones | `new ResponseEntity<>(..., HttpStatus.BAD_REQUEST)`; `throw new RuntimeException`/`IllegalArgumentException` en `Code` | suite (`EstiloDeCodigoTest`) |
| ERR-02 | B | Códigos del proyecto en un único enum `Code/Common/Errores/CodigoDeErrorDelProyecto`; un código por situación, no por `throw`; nombre `MAYUSCULAS_CON_GUION_BAJO` que describe la situación | Segundo enum que implementa `CodigoDeError` en `Code`; constante con nombre de lugar (`SERVICE_X_METODO_Y`) | suite (`ArquitecturaTest`) + agente |
| ERR-03 | B | Toda constante lleva `@Descripcion`; los valores de `codigo()` son únicos | `CodigosDeErrorTest` | suite (`CodigosDeErrorTest`) |
| ERR-04 | B | `mensajeParaDesarrollador` sin secretos, sin `e.getMessage()` de terceros, sin distinguir usuario inexistente de credencial inválida | Interpolación de `e.getMessage()`; dos códigos distintos en un flujo de login | agente |
| ERR-05 | B | `datos` con claves `camelCase` en español y valores simples, nunca una entidad o DTO | `.conDato("x", entidad)` | agente |
| ERR-06 | B | `NO_AUTENTICADO` (401) por falta de sesión; `SIN_PERMISO` (403) solo para un autenticado sin alcance | `SIN_PERMISO` lanzado donde `getActualONull()` es `null` | agente |
| ERR-07 | B | Cada caso de error tiene prueba de contrato con `esperarError(status, codigo)` | Código del proyecto que ninguna prueba espera | agente |

## INT — Integraciones y entorno ([`rules/integraciones-y-entorno.md`](../rules/integraciones-y-entorno.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| INT-01 | B | Fechas del servidor en UTC-3, validado al iniciar | `ZonaHorariaSmokeTestService` del Core, no desactivado | arranque |
| INT-02 | B | Toda integración externa se valida al iniciar (smoke test) y se ejecuta en `try/catch` que traduce a `ExcepcionDeApi` | Cliente HTTP nuevo sin smoke test de arranque; llamada externa sin `catch` | agente |
| INT-03 | B | Toda variable de entorno admite `null`, se valida con mensaje propio y corta el arranque si es necesaria | `@Value("${x}")` sin default ni validación en un bean de arranque | agente |

## TST — Testing ([`rules/testing.md`](../rules/testing.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| TST-01 | B | Cada endpoint tiene prueba de contrato `{Entidad}{CasoDeUso}ContratoTest` en el paquete espejo | `ContratosCompletosTest` | suite (`ContratosCompletosTest`) |
| TST-02 | B | Los tests viven en el módulo del código que prueban; mocks por constructor o setter | `@InjectMocks`; test de `Code` en un módulo del Core | suite (`EstiloDeCodigoTest`) |
| TST-03 | B | Test unitario solo para lógica sin datos externos (y obligatorio para métodos de negocio de la entidad y procesamiento de archivos) | Método de negocio en una entidad sin test; test unitario con `@SpringBootTest` para lógica pura | agente |
| TST-04 | B | Los datos de un test se crean por los endpoints reales (`EscenarioDe{Entidad}`), nunca con SQL a mano | `jdbcTemplate`/`@Sql` en una prueba de contrato | suite (`EstiloDeCodigoTest`) |
| TST-05 | B | Contra un ambiente desplegado solo se corre `deploy/smoke.py` | Test que apunta a una URL de servidor | agente |

## DOC — Documentación ([`rules/documentacion.md`](../rules/documentacion.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| DOC-01 | B | `docs/api` no se escribe a mano | Diff manual en `docs/api/openapi.yaml` sin corrida de la suite | agente |
| DOC-02 | A | `docs/README.md` (negocio) se actualiza cuando cambia una regla de negocio que decide validaciones | Validación nueva en un DTO sin su regla en `docs/README.md` | agente |

## LOG — Logs ([`rules/logs.md`](../rules/logs.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| LOG-01 | B | `@Slf4j`; sin `System.out/err`, `printStackTrace`, `LoggerFactory.getLogger` | Presencia en un `.java` de `app/src/main` | suite (`EstiloDeCodigoTest`) |
| LOG-02 | B | Mensaje `Que paso \| clave=valor`, argumentos con `{}`, sin concatenación, sin tildes, con `uuid` del registro | `log.x("..." + variable)`; mensaje sin `uuid` sobre un registro | suite (`EstiloDeCodigoTest`) + agente |
| LOG-03 | B | Nivel correcto: `ERROR` con excepción como último argumento y solo para lo que el cliente no causó; `WARN` resuelto solo; `INFO` hitos; `DEBUG` local | `log.error` sin excepción; `log.error` antes de un `throw new ExcepcionDeApi` | agente |
| LOG-04 | B | Nunca se loguean secretos, datos personales más allá del `uuid`, entidades/DTOs enteros, contenido de archivos ni `e.getMessage()` de terceros | Argumento de `log.x` que es un DTO/entidad, un campo sensible, un `byte[]` o `getMessage()` | agente |
| LOG-05 | B | No se loguea y se lanza por la misma condición; no "entrando/saliendo"; no dentro de un loop por registro; no se re-loguea una excepción relanzada | `log` + `throw` consecutivos; `log` dentro de un `for` sobre registros | agente |
| LOG-06 | B | `logback-spring.xml` no se edita; niveles por properties; `org.hibernate.SQL=DEBUG` no se commitea | Diff en `logback-spring.xml`; `logging.level.org.hibernate.SQL=DEBUG` en un properties del diff | agente |

## ARC — Archivos e imágenes ([`rules/archivos.md`](../rules/archivos.md))

| Id | Sev | Regla | Cómo detectarla | Verificada por |
|---|---|---|---|---|
| ARC-01 | B | Público → SM3 (`{campo}Url`); privado → Crypter (`{campo}ResourceKey`); ante la duda, privado | Documento/comprobante/DNI subido a SM3; URL de Crypter persistida en vez del `resourceKey` | agente |
| ARC-02 | B | Nunca al filesystem del contenedor ni a la base | `new File`, `Files.write`, `FileOutputStream` en `Code`; columna `BLOB`/`bytea`/`@Lob`; campo `byte[]` en una entidad | suite (`EstiloDeCodigoTest`) |
| ARC-03 | B | Entra como `MultipartFile`, nunca base64 en JSON; límite global del Core y límite por caso de uso con `ARCHIVO_DEMASIADO_GRANDE` | Campo `String` base64 en un `RequestDTO`; caso de uso sin validar tamaño cuando lo pide | agente |
| ARC-04 | B | Lista cerrada de tipos por caso de uso, validada por extensión, `Content-Type` y decodificación; `TIPO_DE_ARCHIVO_NO_PERMITIDO` | `UseCase` que sube sin validar tipo; validación solo por extensión | agente |
| ARC-05 | B | El nombre original no arma rutas ni identifica; la subida va después de las validaciones y se limpia si la transacción falla | `getOriginalFilename()` usado en una ruta o clave; subida antes de validar | agente |
| ARC-06 | B | Procesamiento antes de subir, en `UseCase`/service de la entidad, con test unitario | `ImageIO`/escalado en un controller; sin test | agente |
| ARC-07 | B | El DTO responde URL utilizable; Crypter resuelto con `getArchivos` en una llamada; el `resourceKey` no viaja al front ni se loguea | `resourceKey` en un `ResponseDTO`; `getArchivo` dentro de un loop | agente |
| ARC-08 | B | Reemplazo: subir, persistir, borrar el anterior; borrado lógico no borra; físico sí | Borrado del archivo antes de persistir; `hookBorrar` con borrado lógico que borra el archivo | agente |
| ARC-09 | B | Prueba de contrato con archivo real (feliz, tipo no permitido, tamaño); SM3/Crypter mockeados | Endpoint multipart sin prueba con `partes`; test que llama al servicio real | agente |
