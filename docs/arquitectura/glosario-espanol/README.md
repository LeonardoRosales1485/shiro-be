# Glosario de renombres al español (Core 3.3.0)

Glosario cerrado viejo → nuevo de todo lo que el Core (`sm-core-maven/core-*`) y lo que `Code` hereda de él dejan de
nombrar en inglés, según el [ADR 0002](../../adr/0002-codigo-y-contrato-en-espanol.md) y la lista cerrada de
"Qué queda en inglés" de [`idioma-y-estilo.md`](../../rules/idioma-y-estilo.md). Es la **fuente única** de los
tickets de la iniciativa: ningún ticket inventa un
nombre que no esté acá, y un nombre que falte se agrega acá antes de usarlo.

Estado: **aprobado** (2026-10-09). Las filas marcadas con `[Dn]` dependen de un caso dudoso de la última
sección; el usuario aprobó la opción recomendada en todos, así que las filas ya están escritas con la decisión final.

## Cómo leer las tablas

- **Ticket**: el ticket que aplica el renombre (`02` entidad base y auditoría, `03` contrato del ABM, `04` resto
  del kernel e integraciones, `05` roles y permisos, `06` usuarios y flujos públicos, `07` monoempresa y
  multiempresa, `08` protocolos, `09` cierre: ruta del borrado y barrido de lo que no estaba listado; `revisión 3.3.0`: correcciones de la revisión
  final, ver su sección). `—` = no cambia.
- **queda**: el nombre se mantiene; la columna "Por qué" cita la fila de excepciones que lo cubre.
- Un renombre de clase arrastra su archivo, sus imports y los usos en el resto del Core, en `Code`, en los tests,
  en los seeds y en las docs. Los getters y setters que genera Lombok siguen al campo (`getName()` →
  `getNombre()`) y no se listan aparte.
- Los parámetros de los métodos siguen al campo o concepto que nombran (`idUser` → `idUsuario`) y no se listan
  aparte.
- Las tablas y columnas se renombran con changesets nuevos (`renameTable` para tablas; `sql` con
  `RENAME COLUMN` para columnas, nunca `renameColumn`; ver "Excepcion: renombrar una columna" en
  [`liquibase/README.md`](../liquibase/README.md)), con precondición y rollback, en el changelog del módulo
  dueño de la tabla. Ningún changeset aplicado se edita.

## Convenciones que aplican a todo el glosario

| Patrón | Viejo | Nuevo |
|---|---|---|
| Prefijo `Base` de las clases base del Core | `BaseUser`, `BaseUserService`, `BaseCompanyDTO` | Pasa a sufijo del sustantivo, antes del sufijo técnico: `UsuarioBase`, `UsuarioBaseService`, `EmpresaBaseDTO` |
| Auditoría | `idUserCreated`, `dateCreated`, ... | `idUsuarioDeCreacion`, `fechaDeCreacion`, ... (ver kernel) |
| Clave foránea (Java) | `idUser`, `idCompany` | `idUsuario`, `idEmpresa` |
| Clave foránea (columna) | `user_id`, `company_id`, `role_id`, `permission_id` | `id_usuario`, `id_empresa`, `id_rol`, `id_permiso` |
| Relación en DTO | `companyUuid`, `userUuid` | `empresaUuid`, `usuarioUuid` (`{OtraEntidad}Uuid`, `entidades-y-dtos.md`) |
| Prefijos JavaBeans | `get`, `set`, `is` | **quedan** (convención de código, `idioma-y-estilo.md`); se traduce lo que sigue: `getUser()` → `getUsuario()`, `isConfigured()` → `estaConfigurada()` (verbo propio, no getter de campo) |
| Métodos derivados de Spring Data | `findFirstByNameAndIdCompany` | **Quedan** las palabras clave de Spring Data (`find`, `First`, `By`, `And`, `Or`, `Not`, `IsNull`, `In`, `OrderBy`, `Desc`, `exists`, `delete`), que el framework interpreta; se traducen las propiedades: `findFirstByNombreAndIdEmpresa` |
| Métodos de repositorio con `@Query` | `findAllByUuidInWithPermissions` | Nombre libre en español: `buscarPorUuidsConPermisos` `[D14]` |
| Hooks de `AbstractService` | `hook{Operación}{Paso}` en inglés | `hook{Operación}{Paso}` en español, con `hook` y `Auth` reservados: `hookGuardarAuth`, `hookPaginarAntesDeConsultar` |
| Overrides de Spring y de terceros | `doFilterInternal`, `preHandle`, `afterSingletonsInstantiated`, `contribute`, `addInterceptors`, `handleMethodArgumentNotValid` | **quedan** (nombres de framework) |
| Valores persistidos | `SYSADMIN`, `ADMIN`, `configuration.view`, `user.view`, códigos de error | **quedan**; cambia solo el nombre de la constante Java cuando no es el valor persistido |
| Constraints, índices y FKs de la base | `uk_auth_users_uuid`, `fk_security_code_user` | **quedan** `[D15]` |

## core-kernel

### Clases, interfaces, enums y anotaciones

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `Entity/AbstractEntity` | `Entidad/EntidadBase` | 02 | `[D7]` |
| `Entity/EntidadConBorradoLogico` | `Entidad/EntidadConBorradoLogico` | 02 | Solo cambia el paquete `[D7]` |
| `DTO/AbstractEntityDTO` | `DTO/EntidadBaseDTO` | 02 | `[D7]` |
| `Repository/EntityRepository` | `Repository/EntidadRepository` | 03 | `[D7]` |
| `Repository/EntityRepositoryBase` | `Repository/EntidadRepositoryBase` | 03 | `[D7]`. `Application.java` la nombra en `repositoryBaseClass` |
| `Repository/EntityRepositoryBorradoLogico` | `Repository/EntidadRepositoryBorradoLogico` | 03 | `[D7]` |
| `Repository/PaginateAutomatico` | `Repository/PaginadoAutomatico` | 03 | |
| `Repository/RepositoryImpl` | `Repository/MotorDeConsultas` | 03 | `Impl` no es sufijo reservado. El campo que lo guarda ya se llama `motorDePaginado` → `motorDeConsultas` |
| `DTO/PaginateList` | `DTO/PedidoDePaginado` | 03 | |
| `DTO/PaginateFiltro` | `DTO/FiltroDePaginado` | 03 | |
| `DTO/PaginateOrden` | `DTO/OrdenDePaginado` | 03 | |
| `DTO/PaginateParametro` | `DTO/ParametroDePaginado` | 03 | |
| `DTO/PaginateCondicion` | `DTO/CondicionDePaginado` | 03 | |
| `Page<T>` (Spring) en las firmas del paginado | `DTO/Pagina<T>` (nueva, del kernel) | 03 | `[D10]` |
| `DTO/ApiResponseDTO` | `DTO/RespuestaApiDTO` | 04 | `[D4]` |
| `DTO/DeleteArchivoRequest` | `DTO/BorrarArchivoRequest` | 04 | `Request` reservado |
| `DTO/KeyValue` | (se borra) | 04 | `[D2]` |
| `DTO/SimpleValue` | (se borra) | 04 | `[D3]` |
| `DTO/ControllerValidaciones` | (se borra) | 04 | `[D17]` |
| `Excepciones/CustomException` | `Excepciones/ExcepcionDeApi` | 04 | `[D1]` |
| `Excepciones/CustomRestExceptionHandler` | `Excepciones/ManejadorDeExcepcionesDeApi` | 04 | `[D1]` |
| `Excepciones/ErrorResponseDTO` | queda | — | `Response` reservado `[D9]` |
| `Excepciones/ExceptionUtils` | `Excepciones/ExcepcionUtils` | 04 | `Utils` sufijo técnico |
| `Excepciones/SubError` | queda | — | Ya está en español |
| `Excepciones/CodigoDeError`, `CodigoDeErrorKernel` | quedan | — | |
| `Notificacion/MailNotification` | `Notificacion/NotificacionPorMail` | 04 | `Mail` sufijo técnico |
| `Configuracion/ApiVersionHeaderFilter` | `Configuracion/EncabezadoApiVersionFilter` | 04 | `API-Version` es identificador fijo; `Filter` sufijo técnico |
| `Configuracion/DateTimeConfig` | `Configuracion/FechaHoraConfig` | 04 | |
| `Configuracion/JacksonConfig` | queda | — | Tercero + sufijo técnico |
| `Configuracion/VersionInfoContributor` | queda | — | `InfoContributor` de Spring Actuator + `Version` fijo |
| `Configuracion/KernelConfiguracion` | queda | — | |
| `CoreVersion` | queda | — | `Core` y `Version` son identificadores fijos |
| `SmokeTest/IntegracionesSmokeTest` | queda | — | `smoke test` reservado `[D8]` |
| `SmokeTest/TimeZoneSmokeTestService` | `SmokeTest/ZonaHorariaSmokeTestService` | 04 | `[D8]` |
| `Trazabilidad/ExecutionTraceAspect` | `Trazabilidad/TraceDeEjecucionAspect` | 04 | `Trace` reservado, `Aspect` sufijo |
| `Trazabilidad/GlobalTraceIdInitializer` | `Trazabilidad/InicializadorDeTraceIdGlobal` | 04 | |
| `Trazabilidad/LogDataSanitizer` | `Trazabilidad/SanitizadorDeLogs` | 04 | Las reglas (`errores.md`, `idioma-y-estilo.md`) lo nombran: se actualizan en el mismo ticket |
| `Trazabilidad/RequestLoggingFilter` | queda | — | `Request`, `Logging`, `Filter` reservados |
| `Trazabilidad/TraceContext` | queda | — | `Trace`, `Context` reservados |
| `Trazabilidad/TrazabilidadAspect`, `Trazar` | quedan | — | |
| `Util/ThymeleafUtil`, `Util/Utils` | quedan | — | Tercero + sufijo técnico |
| `Contrato/*`, `Paginado/*`, `Proyeccion/*`, `Puertos/*` | quedan | — | Ya están en español |

### Métodos públicos

| Clase (nueva) | Viejo | Nuevo | Ticket |
|---|---|---|---|
| `EntidadBase` | `onCreate()` | `alCrear()` | 02 |
| `EntidadBase` | `setAbstract(dto)` | `aplicarCamposBase(dto)` | 02 |
| `EntidadBase` | `generateUuidV7()` | `generarUuidV7()` | 02 |
| `EntidadConBorradoLogico` | `onCreateStatus()` | `alCrearStatus()` | 02 |
| `EntidadConBorradoLogico` | `setAbstract(dto)` | `aplicarCamposBase(dto)` | 02 |
| `EntidadBaseDTO` | `setAbstract(entidad)` | `aplicarCamposBase(entidad)` | 02 |
| `EntidadRepository` | `findFirstByUuid(uuid)` (`@Query`) | `buscarPorUuid(uuid)` | 03 `[D14]` |
| `EntidadRepository` | `findFirstById(id)` (`@Query`) | `buscarPorId(id)` | 03 `[D14]` |
| `EntidadRepositoryBorradoLogico` | `findFirstByUuidWithStatusIsActive(uuid)` | `buscarVigentePorUuid(uuid)` | 03 `[D14]` |
| `EntidadRepositoryBorradoLogico` | `findFirstByIdWithStatusIsActive(id)` | `buscarVigentePorId(id)` | 03 `[D14]` |
| `PaginadoAutomatico`, `EntidadRepositoryBase` | `paginate(paginateList, dtoClass)` | `paginar(pedido, claseDto)` | 03 |
| `MotorDeConsultas` | `paginar`, `porUuid`, `completarColecciones` | quedan | — |
| `PedidoDePaginado` | `getParametroByNombre(nombre)` | `getParametroPorNombre(nombre)` | 03 |
| `PedidoDePaginado` | `addParametro`, `addFiltro`, `addCondicion` | `agregarParametro`, `agregarFiltro`, `agregarCondicion` | 03 |
| `ConsultaDePaginado` | `getSelect()`, `getCount()` | `getConsulta()`, `getConsultaDeConteo()` | 03 |
| `ExcepcionDeApi` | `newSubError(name, message)` | `agregarSubError(nombre, mensaje)` | 04 |
| `ExcepcionDeApi` | `getLocalTime()`, `getUserMessage()`, `getDeveloperMessage()` | `getFechaHora()`, `getMensajeParaUsuario()`, `getMensajeParaDesarrollador()` | 04 |
| `ExcepcionDeApi` | `conDato`, `getCodigo`, `getStatus`, `getErrores`, `getDatos`, `getTraceId` | quedan | — |
| `ManejadorDeExcepcionesDeApi` | `handleCustomException(...)` | `manejarExcepcionDeApi(...)` | 04 |
| `ManejadorDeExcepcionesDeApi` | `handleAll(...)` | `manejarCualquierExcepcion(...)` | 04 |
| `ManejadorDeExcepcionesDeApi` | `handleMethodArgumentNotValid`, `handleHandlerMethodValidationException`, `handleHttpMessageNotReadable`, `handleHttpRequestMethodNotSupported`, `handleHttpMediaTypeNotSupported` | quedan (overrides de Spring) | — |
| `ErrorResponseDTO` | `getLocalTime()`, `getUserMessage()` | `getFechaHora()`, `getMensajeParaUsuario()` | 04 |
| `ExcepcionUtils` | `buildDeveloperMessage(context, ex)` | `construirMensajeParaDesarrollador(contexto, ex)` | 04 |
| `NotificacionPorMail` | `getSubject()`, `getBody()` | `getAsunto()`, `getCuerpo()` | 04 |
| `EnviadorDeMail` | `sendEmail(to, subject, body[, attachments])` | `enviar(para, asunto, cuerpo[, adjuntos])` | 04 |
| `IntegracionExterna` | `isConfigured()` | `estaConfigurada()` | 04 |
| `IntegracionExterna` | `getNombreIntegracion()` | queda | — |
| `ProveedorDeVersion`, `VersionPorPropiedad` | `getVersion()` | queda (`Version` fijo) | — |
| `FechaHoraConfig` | `forceDefaultTimeZone()` | `forzarZonaHorariaPorDefecto()` | 04 |
| `FechaHoraConfig` | `nowLocalDateTime()`, `nowTimestamp()`, `nowDate()` | `ahoraLocalDateTime()`, `ahoraTimestamp()`, `ahoraDate()` | 04 |
| `FechaHoraConfig` | `APPLICATION_ZONE_ID`, `APPLICATION_TIME_ZONE`, `APPLICATION_ZONE` | `ID_DE_ZONA`, `ZONA_HORARIA`, `ZONA` | 04 |
| `IntegracionesSmokeTest` | `logIntegrationsStatus()` | `registrarEstadoDeIntegraciones()` | 04 |
| `ZonaHorariaSmokeTestService` | `validateTimeZones()` | `validarZonasHorarias()` | 04 |
| `TraceDeEjecucionAspect`, `TrazabilidadAspect` | `trace(...)` | `trazar(...)` | 04 |
| `TrazabilidadAspect` | `traceService(...)` | `trazarService(...)` | 04 |
| `SanitizadorDeLogs` | `sanitizePayload`, `sanitizeHeader`, `sanitizeValue` | `sanitizarCuerpo`, `sanitizarEncabezado`, `sanitizarValor` | 04 |
| `SanitizadorDeLogs` | `esClaveSensible` | queda | — |
| `TraceContext` | `resolve(...)`, `generate()` | `resolver(...)`, `generar()` | 04 |
| `TraceContext` | `TRACE_ID_HEADER`, `TRACE_ID_KEY` | `TRACE_ID_ENCABEZADO`, `TRACE_ID_CLAVE` | 04 |
| `EncabezadoApiVersionFilter` | `HEADER_NAME` | `NOMBRE_DEL_ENCABEZADO` | 04 |
| `ThymeleafUtil` | `getProcessedHtml(model, templateName)` | `procesarHtml(modelo, plantilla)` | 04 |
| `Utils` | `isValidString(str)` | `esTextoValido(texto)` | 04 |
| `CoreVersion` | `get()` | queda | — |

### Campos y JSON

| Clase (nueva) | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| `EntidadBase` | `idUserCreated` | `idUsuarioDeCreacion` | 02 | |
| `EntidadBase` | `idUserUpdated` | `idUsuarioDeActualizacion` | 02 | |
| `EntidadBase` | `dateCreated` | `fechaDeCreacion` | 02 | |
| `EntidadBase` | `dateUpdated` | `fechaDeActualizacion` | 02 | |
| `EntidadBase` | `id`, `uuid` | quedan | — | `uuid` reservado; `id` es español |
| `EntidadConBorradoLogico` | `idUserDeleted` | `idUsuarioDeBorrado` | 02 | |
| `EntidadConBorradoLogico` | `dateDeleted` | `fechaDeBorrado` | 02 | |
| `EntidadConBorradoLogico` | `status` | queda | — | Reservado |
| `EntidadBaseDTO` (JSON de todo DTO) | `dateCreated` | `fechaDeCreacion` | 02 | Filtros y orden del paginado usan el nombre nuevo |
| `EntidadBaseDTO` | `uuid`, `status` | quedan | — | Reservados |
| `PedidoDePaginado` (JSON) | `page` | `pagina` | 03 | |
| `PedidoDePaginado` (JSON) | `size` | `tamano` | 03 | |
| `PedidoDePaginado` | `entityClass` | `claseDeEntidad` | 03 | `@JsonIgnore` |
| `PedidoDePaginado` (JSON) | `filtros`, `ordenes`, `parametros`, `condiciones` | quedan | — | |
| `FiltroDePaginado`, `OrdenDePaginado`, `ParametroDePaginado`, `CondicionDePaginado` | `campo`, `operacion`, `valor`, `direccion`, `nombre`, `jpql`, `parametros` | quedan | — | `jpql`: nombre del lenguaje |
| `Pagina<T>` (JSON de la respuesta del paginado) | `content` | `contenido` | 03 | `[D10]` |
| `Pagina<T>` | `totalElements` | `totalDeElementos` | 03 | `[D10]` |
| `Pagina<T>` | `totalPages` | `totalDePaginas` | 03 | `[D10]` |
| `Pagina<T>` | `size` | `tamano` | 03 | `[D10]` |
| `Pagina<T>` | `number` | `numero` | 03 | `[D10]` |
| `Pagina<T>` | `numberOfElements` | `cantidadDeElementos` | 03 | `[D10]` |
| `Pagina<T>` | `first`, `last`, `empty` | `primera`, `ultima`, `vacia` | 03 | `[D10]` |
| `Pagina<T>` | `pageable`, `sort` | (no se publican) | 03 | `[D10]` |
| `ErrorResponseDTO` (JSON de error) | `localTime` | `fechaHora` | 04 | |
| `ErrorResponseDTO` (JSON de error) | `userMessage` | `mensajeParaUsuario` | 04 | `errores.md` se actualiza |
| `ErrorResponseDTO` (JSON de error) | `codigo`, `status`, `datos`, `errores`, `traceId` | quedan | — | `traceId`: `Trace` reservado |
| `ExcepcionDeApi` | `localTime`, `userMessage`, `developerMessage` | `fechaHora`, `mensajeParaUsuario`, `mensajeParaDesarrollador` | 04 | `developerMessage` no viaja en la respuesta |
| `SubError` (JSON) | `name`, `message` | `nombre`, `mensaje` | 04 | |
| `RespuestaApiDTO` (JSON) | `status`, `message`, `data` | `status`, `mensaje`, `datos` | 04 | `[D4]` |
| `BorrarArchivoRequest` | `url` | queda | — | Lo consume la API de SM3 (tercero) |
| `SanitizadorDeLogs` | claves sensibles | se agregan las nuevas (`contrasena` ya está; `secreto`, `codigodeverificacion`) | 04/06 | Sin quitar las inglesas: siguen llegando en headers y en APIs externas |
| MDC / logback | `traceId`, `spanId`, `spanDepth` | quedan | — | `Trace` reservado; `span` término de OpenTelemetry |

### Rutas y encabezados

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `X-Trace-Id` | queda | — | `Trace` reservado |
| `API-Version` | queda | — | Identificador fijo (`versionado.md`) |
| `/contratos/**` (publicación del contrato) | queda | — | |

### Propiedades

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `app.version` | queda | — | Identificador fijo (`versionado.md`) |
| `contratos.publicar`, `contratos.*` | quedan | — | |

## core-crud

### Clases

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `AbstractService<T, DTO, PAGINATEDTO>` | `AbstractService<T, DTO, PAGINADODTO>` | 03 | Cambia solo el parámetro de tipo |
| `Borrado/*`, `ValidadorDeProyecciones`, `CodigoDeErrorCrud` | quedan | — | Ya están en español |

### Métodos públicos y protegidos de `AbstractService`

| Viejo | Nuevo | Ticket |
|---|---|---|
| `getByUuid(uuid)` | `obtenerPorUuid(uuid)` | 03 |
| `paginate(paginateList)` | `paginar(pedido)` | 03 |
| `saveOrCreate(dto)` | `guardar(dto)` | 03 |
| `delete(uuid)` | `borrar(uuid)` | 03 |
| `getEntityClass()`, `getDtoClass()`, `getPaginateDtoClass()` | `getClaseDeEntidad()`, `getClaseDeDto()`, `getClaseDePaginadoDto()` | 03 |
| `hookUuidAuth` | `hookObtenerAuth` | 03 |
| `hookUuidFindDatabase` | `hookObtenerBuscarEnBase` | 03 |
| `hookPaginateAuth` | `hookPaginarAuth` | 03 |
| `hookPaginateFilterStatus` | `hookPaginarFiltrarStatus` | 03 |
| `hookPaginatePreQuery` | `hookPaginarAntesDeConsultar` | 03 |
| `hookPaginateQueryExecute` | `hookPaginarEjecutarConsulta` | 03 |
| `hookPaginateCompletarResultados` | `hookPaginarCompletarResultados` | 03 |
| `hookPaginateFilterResults` | `hookPaginarFiltrarResultados` | 03 |
| `hookPaginatePageImplCreate` | `hookPaginarCrearPagina` | 03 |
| `hookSaveAuth` | `hookGuardarAuth` | 03 |
| `hookSaveGetExistRegister` | `hookGuardarBuscarExistente` | 03 |
| `hookSaveCreateNewEmptyModel` | `hookGuardarCrearEntidadVacia` | 03 |
| `hookSaveSetUserCreated` | `hookGuardarAsignarUsuarioDeCreacion` | 03 |
| `hookSaveAplicarDto` | `hookGuardarAplicarDto` | 03 |
| `hookSaveSetUserUpdated` | `hookGuardarAsignarUsuarioDeActualizacion` | 03 |
| `hookSaveDatabaseSave` | `hookGuardarEnBase` | 03 |
| `hookSaveProyectarRespuesta` | `hookGuardarProyectarRespuesta` | 03 |
| `hookDeleteAuth` | `hookBorrarAuth` | 03 |
| `hookDeleteFindByUuid` | `hookBorrarBuscarPorUuid` | 03 |
| `hookDeleteAplicarBorrado` | `hookBorrarAplicarBorrado` | 03 |
| `hookDeleteProyectar` | `hookBorrarProyectar` | 03 |
| `setRepository`, `setProyector`, `setUsuarioActual`, `setSelectorDeBorrado`, `borrado()`, `proyectar(uuid)` | quedan | — |

Los overrides de estos hooks en identity, multiempresa y `Code` (`RolService`, `EmpresaService`, `UsuarioService`)
se renombran en el mismo ticket 03.

### Rutas del ABM (receta del controller y todos los controllers de `Code`)

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `GET /{controller}/uuid/{uuid}` | queda | — | `uuid` reservado |
| `POST /{controller}/paginate` | `POST /{controller}/paginar` | 03 | La vieja responde 404 |
| `POST /{controller}/save` | `POST /{controller}/guardar` | 03 | La vieja responde 404 |
| `DELETE /{controller}/{uuid}` | `DELETE /{controller}/borrar/{uuid}` | 09 | Decisión del usuario. `borrar` sigue a `AbstractService.borrar` y a `/usuario/borrar-token/{uuid}`. La vieja responde 404: con un segmento variable, toda ruta vieja de un segmento (`/paginate`, `/login`, `/empresa/context`) respondía 405 |
| Métodos del controller `getByUuid`, `paginate`, `saveOrCreate`, `delete` | `obtenerPorUuid`, `paginar`, `guardar`, `borrar` | 03 | Receta y `UsuarioController`, `EmpresaController`, `RolController`, `PermisoController` |

## core-identity

El nombre del módulo (`core-identity`, paquete `Identity`) depende de `[D11]`; con la recomendación queda.

### Clases, interfaces y enums

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `Auth/BasePermissionEnum` | `Auth/PermisoBaseEnum` | 05 | `Auth` reservado |
| `Auth/BaseRoleEnum` | `Auth/RolBaseEnum` | 05 | |
| `Auth/IPermission` | `Auth/IPermiso` | 05 | |
| `Auth/IRole` | `Auth/IRol` | 05 | |
| `Auth/PermissionRegistry` | `Auth/RegistroDePermisos` | 05 | |
| `Entity/Permission` | `Entidad/Permiso` | 05 | Paquete `[D7]` |
| `Entity/Role` | `Entidad/Rol` | 05 | |
| `Entity/BaseUser` | `Entidad/UsuarioBase` | 06 | |
| `Entity/SecurityCode` | `Entidad/CodigoDeSeguridad` | 06 | |
| `DTO/PermissionDTO` | `DTO/PermisoDTO` | 05 | |
| `DTO/RoleDTO` | `DTO/RolDTO` | 05 | |
| `DTO/UserRolDTO` | (se borra) | 05 | Sin uso `[D17]` |
| `DTO/BaseUserDTO` | `DTO/UsuarioBaseDTO` | 06 | |
| `DTO/ChangePasswordDTO` | `DTO/CambiarContrasenaDTO` | 06 | |
| `DTO/UserAndPasswordDTO` | `DTO/UsuarioYContrasenaDTO` | 06 | Lo usa también el login de `core-protocolo-jwt` |
| `Repository/PermissionRepository` | `Repository/PermisoRepository` | 05 | |
| `Repository/RoleRepository` | `Repository/RolRepository` | 05 | |
| `Repository/SecurityCodeRepository` | `Repository/CodigoDeSeguridadRepository` | 06 | |
| `Repository/UsuarioRepositorio`, `UsuarioRepositorioJpa` | quedan | — | |
| `Service/PermissionService` | `Service/PermisoService` | 05 | |
| `Service/BaseRoleService` | `Service/RolBaseService` | 05 | |
| `Service/DatabaseInitializerService` | `Service/InicializadorDeBaseService` | 05 | Sincroniza permisos, roles y el usuario inicial |
| `Service/BaseUserService<U, DTO>` | `Service/UsuarioBaseService<U, DTO>` | 06 | |
| `Service/AuthService` | queda | — | `Auth` reservado |
| `Service/AuthBasicService` | queda | — | `Auth` reservado; `Basic` es el esquema HTTP (tercero) |
| `Service/AutorizadorPorRolesYPermisos` | queda | — | |
| `Configuracion/IdentityConfiguracion` | queda | — | `[D11]` |
| `Configuracion/SeguridadWebConfig` | queda | — | |
| `Excepciones/CodigoDeErrorIdentity` | queda | — | `[D11]` |
| `Puertos/*` | quedan | — | Ya están en español |

### Métodos públicos

| Clase (nueva) | Viejo | Nuevo | Ticket |
|---|---|---|---|
| `IPermiso` | `getName()` | `getNombre()` | 05 |
| `IPermiso`, `IRol` | `name()` | queda (`Enum.name()` de Java) | — |
| `IRol`, `RolBaseEnum` | `getPermissions()` | `getPermisos()` | 05 |
| `RegistroDePermisos` | `register(permissions)` | `registrar(permisos)` | 05 |
| `RegistroDePermisos` | `getAdditionalPermissions()` | `getPermisosAdicionales()` | 05 |
| `CatalogoDeAutorizacion` | `getRoles()`, `getPermisos()` | quedan | — |
| `PermisoRepository` | `findFirstByName` | `findFirstByNombre` | 05 |
| `RolRepository` | `findFirstByName` | `findFirstByNombre` | 05 |
| `RolRepository` | `findAllByUuidInWithPermissions` (`@Query`) | `buscarPorUuidsConPermisos` | 05 `[D14]` |
| `RolRepository` | `findFirstByNameAndIdCompanyIsNull` | `findFirstByNombreAndIdEmpresaIsNull` | 05 |
| `RolRepository` | `findFirstByNameAndIdCompany` | `findFirstByNombreAndIdEmpresa` | 05 |
| `RolRepository` | `findFirstByIdCompanyAndIsDefault` | `findFirstByIdEmpresaAndPorDefecto` | 05 |
| `RolRepository` | `findAllByIdCompanyAndIsDefault` | `findAllByIdEmpresaAndPorDefecto` | 05 |
| `RolBaseService` | `setRoleRepository`, `setPermissionRepository` | `setRolRepository`, `setPermisoRepository` | 05 |
| `RolBaseService` | `canAssignRole(role, currentUser, requesterPerms)` | `puedeAsignarRol(rol, usuarioActual, permisosDelSolicitante)` | 05 |
| `RolBaseService`, `UsuarioBaseService`, `EmpresaBaseService` | `setAuthService` | queda | — |
| `InicializadorDeBaseService` | `init()` | `inicializar()` | 05 |
| `CodigoDeSeguridadRepository` | `findFirstByIdUserAndStatusOrderByCreatedAtDesc` | `findFirstByIdUsuarioAndStatusOrderByFechaDeEmisionDesc` | 06 |
| `CodigoDeSeguridadRepository` | `findFirstByCodeAndIdUser` | `findFirstByCodigoAndIdUsuario` | 06 |
| `UsuarioRepositorio` | `porUsernameActivo(username)` | `porNombreDeUsuarioActivo(nombreDeUsuario)` | 06 |
| `UsuarioRepositorio` | `porUuid`, `porId`, `porEmailActivo`, `porEmailNoBorrado`, `porSsoUuidActivo`, `porMalvinasLegajoActivo`, `guardar`, `nuevo`, `getClaseDeUsuario` | quedan | — |
| `EstrategiaDeAutenticacion` (y todas sus implementaciones) | `getUser()` | `getUsuario()` | 06 |
| `EstrategiaDeAutenticacion` | `getProtocolo()` | queda | — |
| `AuthService` | `validProtocolBoolean(protocol)` | `esProtocoloValido(protocolo)` | 06 |
| `AuthService` | `getActualOrNull()` | `getActualONull()` | 06 |
| `AuthService` | `getActualOrThrow()` | `getActualObligatorio()` | 06 |
| `AuthService` | `isSysAdmin()`, `isSysAdmin(user)` | `esSysAdmin()`, `esSysAdmin(usuario)` | 06 |
| `AuthService` | `getAggregatedPermissions(user)` | `getPermisosAgregados(usuario)` | 06 |
| `AuthService` | `autenticar`, `tieneAlgunRolOPermisoPorNombre`, `getIdUsuarioActual`, `tienePermisos` | quedan | — |
| `UsuarioBaseService` | `PURPOSE_FORGOT_PASSWORD`, `PURPOSE_REGISTRATION` | `PROPOSITO_RECUPERAR_CONTRASENA`, `PROPOSITO_REGISTRO` | 06 (valores `[D18]`) |
| `UsuarioBaseService` | `setSecurityCodeRepository` | `setCodigoDeSeguridadRepository` | 06 |
| `UsuarioBaseService` | `setPasswordEncoder` | queda (tipo de Spring Security) | — |
| `UsuarioBaseService` | `verifyCode(uuid, code)` | `verificarCodigo(uuid, codigo)` | 06 |
| `UsuarioBaseService` | `resendCode(uuid, email)` | `reenviarCodigo(uuid, email)` | 06 |
| `UsuarioBaseService` | `forgotPassword(email)` | `recuperarContrasena(email)` | 06 |
| `UsuarioBaseService` | `changePassword(dto)` | `cambiarContrasena(dto)` | 06 |
| `UsuarioBaseService` | `sendVerificationEmail(user)` | `enviarMailDeVerificacion(usuario)` | 06 |
| `UsuarioBaseService` | `sendForgotPasswordEmail(user, code)` | `enviarMailDeRecuperacion(usuario, codigo)` | 06 |
| `UsuarioBaseService` | `generateAndSetVerificationCode(user)` | `generarCodigoDeVerificacion(usuario)` | 06 |
| `UsuarioBaseService` | `generateRandomAlphanumeric(length)` | `generarAlfanumericoAleatorio(largo)` | 06 |
| `UsuarioBaseService` | `hookGetMailNotification(purpose, user, code)` | `hookObtenerNotificacionPorMail(proposito, usuario, codigo)` | 06 |
| `IdentityConfiguracion`, `SeguridadWebConfig` | `passwordEncoder()`, `filterChain()`, `corsConfigurationSource()` | quedan (beans de Spring) | — |

### Campos y JSON

| Clase (nueva) | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| `PermisoBaseEnum` | `PERMISSION_VIEW/CREATE/EDIT/DELETE` | `PERMISO_VER/CREAR/EDITAR/BORRAR` | 05 | El valor (`permission.view`, ...) queda: persistido. `@Contrato(permiso = "PERMISSION_VIEW")` de `PermisoController` pasa a `"PERMISO_VER"` |
| `PermisoBaseEnum` | `COMPANY_NOTNEED` | `EMPRESA_NO_REQUERIDA` | 05 | Valor `company.notneed` queda |
| `PermisoBaseEnum` | `COMPANY_VIEW/CREATE/EDIT/DELETE` | `EMPRESA_VER/CREAR/EDITAR/BORRAR` | 05 | Valores `company.*` quedan |
| `PermisoBaseEnum` | `USER_VIEW/CREATE/EDIT/DELETE` | `USUARIO_VER/CREAR/EDITAR/BORRAR` | 05 | Valores `user.*` quedan |
| `PermisoBaseEnum` | `ROLE_VIEW/CREATE/EDIT/DELETE` | `ROL_VER/CREAR/EDITAR/BORRAR` | 05 | Valores `role.*` quedan |
| `PermisoBaseEnum` | `name` | `nombre` | 05 | Como `PermisoEnum` de `Code` |
| `RolBaseEnum` | `SYSADMIN`, `ADMIN` | quedan | — | Persistidos (`auth_roles.name` guarda el `name()`) |
| `RolBaseEnum` | `permissions` | `permisos` | 05 | |
| `Permiso` / `PermisoDTO` (JSON) | `name`, `description` | `nombre`, `descripcion` | 05 | El DTO solo publica `nombre` |
| `Rol` / `RolDTO` (JSON) | `name` | `nombre` | 05 | |
| `Rol` / `RolDTO` (JSON) | `isDefault` | `porDefecto` | 05 | |
| `Rol` / `RolDTO` (JSON) | `permissions` | `permisos` | 05 | |
| `Rol` | `idCompany` | `idEmpresa` | 05 | |
| `UsuarioBase` / `UsuarioBaseDTO` (JSON) | `username` | `nombreDeUsuario` | 06 | |
| `UsuarioBase` / `UsuarioBaseDTO` (JSON) | `password` | `contrasena` | 06 | `SanitizadorDeLogs` ya tapa `contrasena` |
| `UsuarioBase` / `UsuarioBaseDTO` (JSON) | `email` | queda | — | `[D8]` |
| `UsuarioBase` / `UsuarioBaseDTO` (JSON) | `phone` | `telefono` | 06 | |
| `UsuarioBase` | `name` | `nombre` | 06 | |
| `UsuarioBase` | `departmentName` | `nombreDeDepartamento` | 06 | |
| `UsuarioBase` | `groupName` | `nombreDeGrupo` | 06 | |
| `UsuarioBase` | `ssoUuid`, `malvinasLegajo` | quedan | — | `SSO` tercero, `uuid` reservado, `Malvinas` nombre propio |
| `UsuarioBase` | `verificationCode` | `codigoDeVerificacion` | 06 | |
| `UsuarioBase` | `verificationCodeExpiry` | `vencimientoDelCodigoDeVerificacion` | 06 | |
| `UsuarioBase` | `verificationCodeLastSent` | `ultimoEnvioDelCodigoDeVerificacion` | 06 | |
| `CodigoDeSeguridad` | `code` | `codigo` | 06 | |
| `CodigoDeSeguridad` | `createdAt` | `fechaDeEmision` | 06 | No se llama `fechaDeCreacion` para no chocar con la auditoría |
| `CodigoDeSeguridad` | `purpose` | `proposito` | 06 | |
| `CodigoDeSeguridad` | `used` | `usado` | 06 | |
| `CodigoDeSeguridad` | `idUser` | `idUsuario` | 06 | |
| `CambiarContrasenaDTO` (JSON) | `code`, `email`, `password` | `codigo`, `email`, `contrasena` | 06 | |
| `UsuarioYContrasenaDTO` (JSON) | `username`, `password` | `nombreDeUsuario`, `contrasena` | 06 | Body del login JWT |
| `AuthService` | `protocols` (`@Value`) | `protocolos` | 06 | |

### Tablas y columnas (changelog `core-identity.yaml`)

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| Columnas de auditoría de `auth_permissions`, `auth_users`, `auth_roles`, `security_code` | `id_usuario_de_creacion`, `id_usuario_de_actualizacion`, `id_usuario_de_borrado`, `fecha_de_creacion`, `fecha_de_actualizacion`, `fecha_de_borrado` | 02 | Con los nombres de tabla viejos: el renombre de tablas es posterior |
| `auth_permissions` | `auth_permisos` | 05 | `[D12]` |
| `auth_permissions.name`, `.description` | `nombre`, `descripcion` | 05 | |
| `auth_roles` | queda | — | `roles` es español; `auth_` `[D12]` |
| `auth_roles.name`, `.is_default`, `.company_id` | `nombre`, `por_defecto`, `id_empresa` | 05 | La FK `fk_auth_roles_company` la crea `core-multiempresa`: su nombre queda `[D15]` |
| `auth_roles_permissions` | `auth_roles_permisos` | 05 | |
| `auth_roles_permissions.role_id`, `.permission_id` | `id_rol`, `id_permiso` | 05 | |
| `auth_users` | `auth_usuarios` | 06 | La `@Table` vive en `Code/Usuario/Usuario.java` |
| `auth_users.username`, `.password`, `.name`, `.phone` | `nombre_de_usuario`, `contrasena`, `nombre`, `telefono` | 06 | |
| `auth_users.department_name`, `.group_name` | `nombre_de_departamento`, `nombre_de_grupo` | 06 | |
| `auth_users.verification_code`, `.verification_code_expiry`, `.verification_code_last_sent` | `codigo_de_verificacion`, `vencimiento_del_codigo_de_verificacion`, `ultimo_envio_del_codigo_de_verificacion` | 06 | |
| `auth_users.email`, `.sso_uuid`, `.malvinas_legajo` | quedan | — | `[D8]` para `email` |
| `security_code` | `codigos_de_seguridad` | 06 | Sin prefijo, como hoy `[D12]` |
| `security_code.code`, `.purpose`, `.used`, `.created_at`, `.user_id` | `codigo`, `proposito`, `usado`, `fecha_de_emision`, `id_usuario` | 06 | Valores de `proposito` `[D18]` |

### Rutas (`Code/Autorizacion`, `Code/Usuario`)

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `/permission/uuid/{uuid}`, `/permission/paginate` | `/permiso/uuid/{uuid}`, `/permiso/paginar` | 05 | |
| `/rol/*` | queda la base `/rol` | — | ABM según core-crud |
| `GET /usuario/login` | `GET /usuario/sesion` | 06 | `[D8]` |
| `POST /usuario/public/verify` | `POST /usuario/publico/verificar` | 06 | Body `{uuid, code}` → `{uuid, codigo}` |
| `POST /usuario/public/resend-code` | `POST /usuario/publico/reenviar-codigo` | 06 | |
| `POST /usuario/public/forgot-password` | `POST /usuario/publico/recuperar-contrasena` | 06 | Respuesta `{message}` → `{mensaje}` |
| `POST /usuario/public/change-password` | `POST /usuario/publico/cambiar-contrasena` | 06 | Respuesta `[D4]` |
| `POST /usuario/public/register` | `POST /usuario/publico/registro` | 07 | Alta con empresa (multiempresa) |
| `POST /usuario/public/register-client` | `POST /usuario/publico/registro-cliente` | 07 | |
| `POST /usuario/public/link/{userUuid}` | `POST /usuario/publico/vincular/{usuarioUuid}` | 07 | Requiere sesión; conserva el prefijo `publico/` por compatibilidad |
| Métodos `login`, `verifyCode`, `resendCode`, `forgotPassword`, `changePassword` de `UsuarioController` | `sesion`, `verificarCodigo`, `reenviarCodigo`, `recuperarContrasena`, `cambiarContrasena` | 06 | |

### Propiedades

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `auth.protocols` | `auth.protocolos` | 06 | Los valores (`jwt`, `basic`, `tokens`, `sso`, `malvinas`) quedan `[D19]` |
| `core.identity.usuario-inicial.username` | `core.identity.usuario-inicial.nombre-de-usuario` | 06 | Segmento `identity` `[D11]` |
| `core.identity.usuario-inicial.password` | `core.identity.usuario-inicial.contrasena` | 06 | |

## core-monoempresa

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Clase | `Entity/UserRole` | `Entidad/UsuarioRol` | 07 | |
| Clase | `Repository/UserRoleRepository` | `Repository/UsuarioRolRepository` | 07 | |
| Clase | `Service/InicializadorMonoempresa`, `ResolutorDePermisosMonoempresa` | quedan | — | |
| Método | `findAllByIdUserAndStatus` | `findAllByIdUsuarioAndStatus` | 07 | |
| Método | `existsRoleForUser` (`@Query`) | `existeRolParaUsuario` | 07 `[D14]` |
| Método | `existsPermissionForUser` (`@Query`) | `existePermisoParaUsuario` | 07 `[D14]` |
| Método | `findAggregatedPermissionNames` (`@Query`) | `buscarNombresDePermisosAgregados` | 07 `[D14]` |
| Método | `existsByIdUserAndRoleUuidAndStatus` | `existsByIdUsuarioAndRolUuidAndStatus` | 07 |
| Campo | `UserRole.idUser`, `.role` | `UsuarioRol.idUsuario`, `.rol` | 07 |
| Campo | `userRoleRepository` (inyección) | `usuarioRolRepository` | 07 |
| Tabla | auditoría de `auth_user_roles` | nombres de auditoría nuevos | 02 |
| Tabla | `auth_user_roles` | `auth_usuarios_roles` | 07 | `[D12]` |
| Columna | `auth_user_roles.user_id`, `.role_id` | `id_usuario`, `id_rol` | 07 |

## core-multiempresa

### Clases

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `Entity/BaseCompany` | `Entidad/EmpresaBase` | 07 | |
| `Entity/BaseUserMultiempresa` | `Entidad/UsuarioBaseMultiempresa` | 07 | |
| `Entity/UserCompanyRole` | `Entidad/UsuarioEmpresa` | 07 | Es la pertenencia del usuario a la empresa, con sus roles |
| `Entity/EmpresaDeRelacion` | queda | — | |
| `DTO/BaseCompanyDTO` | `DTO/EmpresaBaseDTO` | 07 | |
| `DTO/BaseUserMultiempresaDTO` | `DTO/UsuarioBaseMultiempresaDTO` | 07 | |
| `DTO/CompanyContextDTO<CDTO>` | `DTO/ContextoDeEmpresaDTO<EDTO>` | 07 | |
| `DTO/PublicRegisterRequest<UDTO, CDTO>` | `DTO/RegistroPublicoRequest<UDTO, EDTO>` | 07 | `Request` reservado |
| `DTO/UserCompanyRoleDTO` | `DTO/UsuarioEmpresaDTO` | 07 | |
| `Repository/UserCompanyRoleRepository` | `Repository/UsuarioEmpresaRepository` | 07 | |
| `Service/BaseCompanyService<C, DTO>` | `Service/EmpresaBaseService<E, DTO>` | 07 | |
| `Service/BaseUserServiceMultiempresa<U, DTO>` | `Service/UsuarioBaseMultiempresaService<U, DTO>` | 07 | |
| `Service/BaseRoleServiceMultiempresa` | `Service/RolBaseMultiempresaService` | 05 | Va con roles |
| `Excepciones/CodigoDeErrorMultiempresa`, `Repository/EmpresaRepositorio*`, `Service/AltaDeEmpresa*`, `ContextoDeEmpresa`, `Inicializador*`, `Resolutor*`, `Sincronizador*`, `Validador*` | quedan | — | Ya están en español |

### Métodos públicos

| Clase (nueva) | Viejo | Nuevo | Ticket |
|---|---|---|---|
| `EmpresaBaseService` | `getContext()` | `obtenerContexto()` | 07 |
| `UsuarioBaseMultiempresaService` | `publicRegister(request)` | `registroPublico(request)` | 07 |
| `UsuarioBaseMultiempresaService` | `publicRegisterClient(companyUuid, userDTO)` | `registroPublicoDeCliente(empresaUuid, usuarioDTO)` | 07 |
| `UsuarioBaseMultiempresaService` | `publicLink(companyUuid, userUuid)` | `vincular(empresaUuid, usuarioUuid)` | 07 |
| `UsuarioBaseMultiempresaService` | `hookCanAssignRole(role, user, companyUuid)` | `hookPuedeAsignarRol(rol, usuario, empresaUuid)` | 07 |
| `UsuarioBaseMultiempresaService` | `hookPublicRegisterPreSave(user, company)` | `hookRegistroPublicoAntesDeGuardar(usuario, empresa)` | 07 |
| `UsuarioBaseMultiempresaService` | `hookPublicRegisterPreCreateCompany(userDTO, companyDTO)` | `hookRegistroPublicoAntesDeCrearEmpresa(usuarioDTO, empresaDTO)` | 07 |
| `UsuarioBaseMultiempresaService` | `setUserCompanyRoleRepository` | `setUsuarioEmpresaRepository` | 07 |
| `UsuarioBaseMultiempresaService`, `RolBaseMultiempresaService` | `setRoleRepository` | `setRolRepository` | 07 / 05 |
| `UsuarioBaseMultiempresaService` | `esAdminDeEmpresa`, `sincronizarRoles`, `setEmpresaRepositorio`, `setContextoDeEmpresa`, `setAltaDeEmpresa` | quedan | — |
| `ContextoDeEmpresa` | `HEADER_EMPRESA` (valor `X-Company-Uuid`) | `ENCABEZADO_EMPRESA` (valor `X-Empresa-Uuid`) | 07 `[D20]` |
| `ContextoDeEmpresa` | `isMultiTenantEnabled()` | `isMultiempresaHabilitada()` | 07 |
| `ContextoDeEmpresa` | `resolveCompanyUuid(companyUuid)` | `resolverUuidDeEmpresa(uuid)` | 07 |
| `ContextoDeEmpresa` | `getIdEmpresaActualOrNull()` | `getIdEmpresaActualONull()` | 07 |
| `ContextoDeEmpresa` | `getCurrentCompanyOrThrow()` | `getEmpresaActualObligatoria()` | 07 |
| `ContextoDeEmpresa` | `getIdEmpresaDeSistema`, `getUuidEmpresaPorDefecto`, `getUuidDeLaRequest`, `getUuidCrudoDeLaRequest`, `getEmpresaPorDefecto` | quedan | — |
| `EmpresaRepositorio` | `porUuid`, `porUuidActiva`, `porId`, `porNombreActiva`, `guardar`, `nueva` | quedan | — |
| `AltaDeEmpresa` | `crear` | queda | — |
| `UsuarioEmpresaRepository` | `existsByCompanyAndUser` (`@Query`) | `existeUsuarioEnEmpresa` | 07 `[D14]` |
| `UsuarioEmpresaRepository` | `existsRoleForUser` (`@Query`) | `existeRolParaUsuario` | 07 `[D14]` |
| `UsuarioEmpresaRepository` | `existsPermissionForUser` (`@Query`) | `existePermisoParaUsuario` | 07 `[D14]` |
| `UsuarioEmpresaRepository` | `isSysAdmin` (`@Query`) | `esSysAdmin` | 07 `[D14]` |
| `UsuarioEmpresaRepository` | `findAggregatedPermissionNames` (`@Query`) | `buscarNombresDePermisosAgregados` | 07 `[D14]` |
| `UsuarioEmpresaRepository` | `findByUserAndCompany` (`@Query`) | `buscarPorUsuarioYEmpresa` | 07 `[D14]` |
| `UsuarioEmpresaRepository` | `findAllByIdUserAndStatus` | `findAllByIdUsuarioAndStatus` | 07 |

### Campos y JSON

| Clase (nueva) | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| `EmpresaBase` / `EmpresaBaseDTO` (JSON) | `name` | `nombre` | 07 | |
| `EmpresaBase` / `EmpresaBaseDTO` (JSON) | `description` | `descripcion` | 07 | |
| `EmpresaBase` / `EmpresaBaseDTO` (JSON) | `path` | `ruta` | 07 | Slug para URLs |
| `EmpresaBase` / `EmpresaBaseDTO` (JSON) | `code` | `codigo` | 07 | |
| `EmpresaBase` | `balance` | `saldo` | 07 | |
| `EmpresaDeRelacion` | `name` | `nombre` | 07 | |
| `UsuarioBaseMultiempresa` / `UsuarioBaseMultiempresaDTO` (JSON) | `userCompanyRoles` | `rolesPorEmpresa` | 07 | `@Coleccion("rolesPorEmpresa")` |
| `UsuarioEmpresa` | `idUser`, `idCompany`, `company` | `idUsuario`, `idEmpresa`, `empresa` | 07 | |
| `UsuarioEmpresa` / `UsuarioEmpresaDTO` (JSON) | `roles` | queda | — | Español |
| `UsuarioEmpresaDTO` (JSON) | `companyUuid`, `companyName` | `empresaUuid`, `empresaNombre` | 07 | `@Columna("empresa.uuid")`, `@Columna("empresa.nombre")` |
| `ContextoDeEmpresaDTO` (JSON) | `multiTenantEnabled` | `multiempresaHabilitada` | 07 | |
| `ContextoDeEmpresaDTO` (JSON) | `defaultCompanyUuid` | `uuidEmpresaPorDefecto` | 07 | |
| `ContextoDeEmpresaDTO` (JSON) | `companyHeaderRequired` | `encabezadoDeEmpresaRequerido` | 07 | |
| `ContextoDeEmpresaDTO` (JSON) | `requestCompanyUuid` | `uuidEmpresaDelRequest` | 07 | `Request` reservado |
| `ContextoDeEmpresaDTO` (JSON) | `resolvedCompanyUuid` | `uuidEmpresaResuelta` | 07 | |
| `ContextoDeEmpresaDTO` (JSON) | `currentCompany` | `empresaActual` | 07 | |
| `RegistroPublicoRequest` (JSON) | `user`, `company` | `usuario`, `empresa` | 07 | |
| `ContextoDeEmpresa` | `multiTenantEnabled` (`@Value`) | `multiempresaHabilitada` | 07 | |
| Inyecciones | `userCompanyRoleRepository`, `roleRepository` | `usuarioEmpresaRepository`, `rolRepository` | 07 | |
| `CodigoDeErrorMultiempresa` | `COMPANY_REQUERIDA`, `COMPANY_NO_ENCONTRADA` | quedan | — | `[D13]`; su `userMessage` ("Company requerida/no encontrada") sí pasa a "Empresa ..." en 07 |

### Tablas y columnas (changelog `core-multiempresa.yaml`; `cuit` en el changelog del proyecto)

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| Auditoría de `companies`, `auth_users_companies` | nombres de auditoría nuevos | 02 | |
| `companies` | `empresas` | 07 | La `@Table` vive en `Code/Empresa/Empresa.java` y en `EmpresaDeRelacion` |
| `companies.name`, `.description`, `.path`, `.code`, `.balance` | `nombre`, `descripcion`, `ruta`, `codigo`, `saldo` | 07 | |
| `companies.cuit` | queda (en `empresas`) | — | Columna del proyecto: si hiciera falta tocarla va en el changelog del proyecto |
| `auth_users_companies` | `auth_usuarios_empresas` | 07 | `[D12]` |
| `auth_users_companies.user_id`, `.company_id` | `id_usuario`, `id_empresa` | 07 | |
| `auth_user_company_roles` | `auth_usuarios_empresas_roles` | 07 | |
| `auth_user_company_roles.user_company_role_id`, `.role_id` | `id_usuario_empresa`, `id_rol` | 07 | |

### Rutas y encabezados

| Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|
| `GET /empresa/context` | `GET /empresa/contexto` | 07 | Método `getContext` → `obtenerContexto` |
| `X-Company-Uuid` | `X-Empresa-Uuid` | 07 | `[D20]` |
| Altas públicas `/usuario/public/register*` y `link` | ver core-identity → Rutas | 07 | |

### Propiedades

| Viejo | Nuevo | Ticket |
|---|---|---|
| `core.multi-company.enabled` | `core.multiempresa.habilitada` | 07 |
| `core.multi-company.system-uuid` | `core.multiempresa.uuid-de-sistema` | 07 |
| `core.multi-company.default-uuid` | `core.multiempresa.uuid-por-defecto` | 07 |
| `core.multi-company.system-name` | `core.multiempresa.nombre-de-sistema` | 07 |
| `core.multi-company.default-name` | `core.multiempresa.nombre-por-defecto` | 07 |

## core-protocolo-apikey

El nombre del módulo (`core-protocolo-apikey`, paquete `ApiKey`, `CodigoDeErrorApiKey`) depende de `[D11]`; con
la recomendación queda.

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Clase | `UserAccessToken` | `AccessToken` | 08 | `AccessToken` reservado |
| Clase | `UserAccessTokenDTO` | `AccessTokenDTO` | 08 | |
| Clase | `UserAccessTokenRepository` | `AccessTokenRepository` | 08 | |
| Clase | `AccessTokenController`, `AuthTokenService` | quedan | — | Reservados; `token` `[D8]` |
| Método | `AccessTokenController.createToken(dto)` | `crearToken(dto)` | 08 | |
| Método | `AccessTokenController.deleteToken(uuid)` | `borrarToken(uuid)` | 08 | |
| Método | `AccessTokenController.getTokens()` | `listarTokens()` | 08 | |
| Método | `AuthTokenService.createAccessToken(user, name)` | `crearAccessToken(usuario, nombre)` | 08 | |
| Método | `AuthTokenService.deleteAccessToken(user, uuid)` | `borrarAccessToken(usuario, uuid)` | 08 | |
| Método | `AuthTokenService.getTokensByUser(user)` | `getTokensDelUsuario(usuario)` | 08 | |
| Método | `AuthTokenService.proyectar`, `proyectarVigentes`, `getProtocolo` | quedan | — | |
| Método | `findFirstByCode` | `findFirstByCodigo` | 08 | |
| Método | `findFirstByNameAndIdUserAndStatus` | `findFirstByNombreAndIdUsuarioAndStatus` | 08 | |
| Método | `findAllByIdUserAndStatus` | `findAllByIdUsuarioAndStatus` | 08 | |
| Método | `buscarDtoPorUuid`, `listarDtosVigentesPorUsuario` | quedan | — | |
| Campo / JSON | `AccessTokenDTO.userUuid` | `usuarioUuid` | 08 | |
| Campo / JSON | `name`, `code`, `active` | `nombre`, `codigo`, `activo` | 08 | Entidad y DTO |
| Campo | `AccessToken.idUser` | `idUsuario` | 08 | |
| Tabla | auditoría de `auth_user_access_tokens` | nombres de auditoría nuevos | 02 | |
| Tabla | `auth_user_access_tokens` | `auth_access_tokens` | 08 | `[D12]` |
| Columna | `name`, `code`, `active`, `user_id` | `nombre`, `codigo`, `activo`, `id_usuario` | 08 | |
| Ruta | `POST /{path}/create-token` | `POST /{ruta}/crear-token` | 08 | `{ruta}` = `usuario` en el template |
| Ruta | `DELETE /{path}/delete-token/{uuid}` | `DELETE /{ruta}/borrar-token/{uuid}` | 08 | |
| Ruta | `GET /{path}/tokens` | queda | — | `[D8]` |
| Propiedad | `core.protocolo.apikey.path` (default `user`) | `core.protocolo.apikey.ruta` (default `usuario`) | 08 | |
| Valor de `auth.protocolos` | `tokens` | queda | — | `[D19]` |

## core-protocolo-jwt

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Clase | `AuthJwtService`, `JwtLoginController`, `JwtRefreshToken`, `JwtRefreshTokenRepository`, `JwtTokenDTO`, `CodigoDeErrorJwt` | quedan | — | `Auth`, `RefreshToken` reservados; JWT tercero. `Login` en el nombre de la clase `[D8]` |
| Método | `AuthJwtService.generateToken(user)` | `generarToken(usuario)` | 08 | |
| Método | `AuthJwtService.refreshToken(refreshToken)` | `renovarToken(refreshToken)` | 08 | |
| Método | `AuthJwtService.getUserFromToken(token)` | `getUsuarioDelToken(token)` | 08 | |
| Método | `AuthJwtService.getUserAndPassword(body)` | `autenticarConContrasena(body)` | 08 | |
| Método | `JwtLoginController.loginJWT(body)` | `iniciarSesion(body)` | 08 | `[D8]` |
| Método | `JwtLoginController.refreshToken(request)` | `renovarToken(request)` | 08 | |
| Método | `JwtRefreshTokenRepository.findByToken` | queda | — | |
| Método | `JwtRefreshTokenRepository.deleteByIdUser` | `deleteByIdUsuario` | 08 | |
| Campo | `JwtRefreshToken.idUser` | `idUsuario` | 08 | |
| Campo | `JwtRefreshToken.expiryDate` | `fechaDeVencimiento` | 08 | |
| Campo | `JwtRefreshToken.token` | queda | — | `[D8]` |
| Campo | `AuthJwtService.jwtSecret`, `tokenExpirationMs`, `refreshTokenExpirationMs` | `secretoJwt`, `vencimientoDelTokenMs`, `vencimientoDelRefreshTokenMs` | 08 | Privados, se listan por la propiedad |
| JSON | `JwtTokenDTO.token` | queda | — | `[D8]` |
| JSON | `JwtTokenDTO.refresh` | `refreshToken` | 08 | `RefreshToken` reservado |
| JSON | Body del login (`UsuarioYContrasenaDTO`) | ver core-identity | 06 | |
| Claims del JWT | `sub`, `type` = `access` | quedan | — | `[D19]`: un token emitido antes de migrar sigue validando |
| Tabla | auditoría de `auth_jwt_refresh_tokens` | nombres de auditoría nuevos | 02 | |
| Tabla | `auth_jwt_refresh_tokens` | queda | — | JWT + RefreshToken; `auth_` `[D12]` |
| Columna | `expiry_date`, `user_id` | `fecha_de_vencimiento`, `id_usuario` | 08 | |
| Columna | `token` | queda | — | `[D8]` |
| Ruta | `POST /{path}/login` | `POST /{ruta}/iniciar-sesion` | 08 | `[D8]` |
| Ruta | `POST /{path}/refresh-token` | queda | — | `RefreshToken` reservado |
| Propiedad | `core.protocolo.jwt.path` (default `user`) | `core.protocolo.jwt.ruta` (default `usuario`) | 08 | |
| Propiedad | `jwt.secret` | `jwt.secreto` | 08 | |
| Propiedad | `jwt.expiration` | (se borra de los `.properties`) | 08 | Ningún código la lee: el vencimiento está fijo en `AuthJwtService` |

## core-protocolo-sso

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Clase | `AuthSsoService`, `MalvinasJwtDecoderConfig`, `UsuarioSSO`, `CodigoDeErrorSso` | quedan | — | Reservados, terceros y nombre propio |
| Clase | `AuthMalvinasService` | queda | — | `[D6]` |
| Clase | `SSOAuthService` | (se borra) | 08 | `[D5]` |
| Método | `AuthSsoService.mapperUserBySSO(usuarioSSO)` | `usuarioDesdeSso(usuarioSSO)` | 08 | `Mapper` ya no es sufijo reservado |
| Método | `obtenerUsuariosAplicacion`, `getProtocolo`, `getNombreIntegracion` | quedan | — | |
| Campo / JSON | `UsuarioSSO.uuid`, `username`, `email`, `nombre`, `telefono`, `cuentaVerificada`, `cuentaBloqueada`, `roles` | quedan | — | Los publica la API del SSO (tercero) |
| Propiedad | `auth.sso.url`, `auth.malvinas.url` | quedan | — | |
| Propiedad | `auth.sso.clientId` | queda | — | `client_id` es término de OAuth (tercero) |
| Propiedad | `sso.url`, `sso.clientId` | (se borran con `SSOAuthService`) | 08 | `[D5]` |
| Valor | `CONTEXTO_EXTERNO` (`sso`, `malvinas`), claim `urn:malvinas:legajo` | quedan | — | Valores externos |

## core-captcha

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Clase | `CaptchaService`, `CodigoDeErrorCaptcha` | quedan | — | hCaptcha tercero |
| Método | `verify(token)` | `verificar(token)` | 04 | |
| Método | `isConfigured()` | `estaConfigurada()` | 04 | Sigue a `IntegracionExterna` |
| Campo | `secretKey`, `enabled` (`@Value`) | `claveSecreta`, `habilitado` | 04 | |
| Propiedad | `captcha.secret` | `captcha.secreto` | 04 | |
| Propiedad | `captcha.enabled` | `captcha.habilitado` | 04 | |
| Encabezado | `X-Captcha-Token` | queda | — | `[D8]` |
| JSON externo | parámetros de `https://hcaptcha.com/siteverify` | quedan | — | Tercero |

## core-crypter

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Clase | `CrypterService`, `ImagenCrypterDTO`, `CodigoDeErrorCrypter` | quedan | — | Crypter tercero |
| Método | `getFiles(resourcekeys)` | `getArchivos(resourceKeys)` | 04 | |
| Método | `getFile(resourcekey)` | `getArchivo(resourceKey)` | 04 | |
| Método | `putFile(...)` (5 sobrecargas) | `guardarArchivo(...)` | 04 | También en `CrypterEnMemoria` (`core-test`) |
| Método | `ImagenCrypterDTO.getJsonString()` | `getComoJson()` | 04 | |
| Método | `isConfigured()` | `estaConfigurada()` | 04 | |
| Campo | `crypter`, `user`, `pass` (`@Value`) | `url`, `usuario`, `contrasena` | 04 | |
| Campo / JSON externo | `ImagenCrypterDTO.*` (`idLote`, `operation`, `name`, `parentKey`, `resourceKey`, `source`, `dest`, `timeInit`, `timeEnd`, `idCrypt`, `result`, `errMessage`, `duration`, `fileSize`, `validation`) | quedan | — | Los publica la API de Crypter (tercero) |
| Propiedad | `crypter.user`, `crypter.pass` | `crypter.usuario`, `crypter.contrasena` | 04 | |
| Propiedad | `crypter.url`, `crypter.tipo.documento` | quedan | — | |

## core-mail

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Clase | `JavaMailSender` | `EnviadorDeMailSmtp` | 04 | Hoy repite el nombre de la interfaz de Spring |
| Clase | `CodigoDeErrorMail` | queda | — | |
| Método | `sendEmail(...)` | `enviar(...)` | 04 | Sigue a `EnviadorDeMail` |
| Método | `isConfigured()` | `estaConfigurada()` | 04 | |
| Propiedad | `spring.mail.*` | quedan | — | Spring |
| Propiedad | `mail.option` | `mail.opcion` | 04 | |

## core-sm3

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Clase | `SM3Service`, `CodigoDeErrorSm3` | quedan | — | SM3 tercero |
| Método | `upload(file)` | `subir(archivo)` | 04 | |
| Método | `delete(request)` | `borrar(request)` | 04 | Recibe `BorrarArchivoRequest` |
| Método | `isConfigured()` | `estaConfigurada()` | 04 | |
| Campo | `crypter`, `user`, `pass` (`@Value`) | `url`, `usuario`, `contrasena` | 04 | `crypter` está mal nombrado: es la URL de SM3 |
| Propiedad | `sm3.user`, `sm3.pass` | `sm3.usuario`, `sm3.contrasena` | 04 | |
| Propiedad | `sm3.url`, `sm3.proyecto` | quedan | — | |
| Multipart externo | `file`, `proyecto` | quedan | — | Tercero |

## core-test

El nombre del módulo (`core-test`, paquete `Test`) depende de `[D11]`; los sufijos `*Test`/`*IT` de las clases de
prueba quedan (convención de JUnit/Surefire).

| Tipo | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|
| Ruta | `ContratoDeAbmTest.OperacionAbm.PAGINAR` → `/paginate` | `/paginar` | 03 | |
| Ruta | `ContratoDeAbmTest.OperacionAbm.GUARDAR` → `/save` | `/guardar` | 03 | |
| Método | `ContratoDeAbmTest.bodyValido()`, `bodyInvalido()` | `cuerpoValido()`, `cuerpoInvalido()` | 03 | Los implementan los `{Entidad}ContratoTest` de `Code` |
| Método | `Peticion.param(nombre, valor)` | `parametro(nombre, valor)` | 04 | |
| Método | `Peticion.header(nombre, valor)` | `encabezado(nombre, valor)` | 04 | |
| Método | `Peticion.get/post/put/patch/delete/json` | quedan | — | `[D16]` |
| Método | `EnviadorDeMailEnMemoria.sendEmail` | `enviar` | 04 | Sigue a `EnviadorDeMail` |
| Método | `CrypterEnMemoria.putFile/getFiles/getFile` | `guardarArchivo/getArchivos/getArchivo` | 04 | Sigue a `CrypterService` |
| Método | `isConfigured()` de los dobles | `estaConfigurada()` | 04 | |
| Schema | Propiedades de `Page` en `GeneradorDeSchemas.pagina` | las de `Pagina<T>` | 03 | `[D10]` |
| Clase | `ActoresDePrueba.token(rol)` | queda | — | `[D8]` |
| Clase | `TestConBaseDeDatos`, `PruebaDeContrato`, `VerificacionDeContratos`, `Contrato/*`, `Dobles/*`, `Peticion`, `Respuesta` | quedan | — | `TestConBaseDeDatos` `[D11]` |
| Propiedad | `contratos.*` | quedan | — | |

## Code (lo que hereda o implementa del Core)

Además de las filas de cada módulo (hooks, rutas, DTOs y tablas), estos nombres propios de `Code` cambian porque
implementan un nombre del Core:

| Archivo | Viejo | Nuevo | Ticket |
|---|---|---|---|
| `Autorizacion/PermisoEnum`, `RolEnum` | `implements IPermission` / `IRole`; `getName()`, `getPermissions()` | `implements IPermiso` / `IRol`; `getNombre()`, `getPermisos()` | 05 |
| `Autorizacion/PermisoEnum` | `PermissionRegistry.register(values())` | `RegistroDePermisos.registrar(values())` | 05 |
| `Autorizacion/RolService` | `canAssignRole`, `hookUuidFindDatabase`, `hookSaveAplicarDto`, `permissionRepository` | `puedeAsignarRol`, `hookObtenerBuscarEnBase`, `hookGuardarAplicarDto`, `permisoRepository` | 03 / 05 |
| `Autorizacion/PermisoController` | `@RequestMapping("permission")`, `permiso = "PERMISSION_VIEW"` | `@RequestMapping("permiso")`, `permiso = "PERMISO_VER"` | 05 |
| `Empresa/Empresa`, `EmpresaDTO`, `EmpresaService` | `extends BaseCompany`, `BaseCompanyDTO`, `BaseCompanyService`; `@Table(name = "companies")` | `extends EmpresaBase`, `EmpresaBaseDTO`, `EmpresaBaseService`; `@Table(name = "empresas")` | 07 |
| `Usuario/Usuario`, `UsuarioDTO`, `UsuarioService` | `extends BaseUserMultiempresa`, `BaseUserMultiempresaDTO`, `BaseUserServiceMultiempresa`; `@Table(name = "auth_users")` | `extends UsuarioBaseMultiempresa`, `UsuarioBaseMultiempresaDTO`, `UsuarioBaseMultiempresaService`; `@Table(name = "auth_usuarios")` | 06 / 07 |
| `Usuario/UsuarioService` | `hookCanAssignRole`, `hookPublicRegisterPreCreateCompany`, `hookGetMailNotification`, `PURPOSE_*` | `hookPuedeAsignarRol`, `hookRegistroPublicoAntesDeCrearEmpresa`, `hookObtenerNotificacionPorMail`, `PROPOSITO_*` | 06 / 07 |
| `Usuario/Notificaciones/*Mail` | `implements MailNotification`; `getSubject()`, `getBody()`; `usuario.getUsername()` | `implements NotificacionPorMail`; `getAsunto()`, `getCuerpo()`; `usuario.getNombreDeUsuario()` | 04 / 06 |
| `Usuario/UsuarioController` | `publicRegister`, `publicLink`, `publicRegisterClient`; `captchaService.verify` | `registroPublico`, `vincular`, `registroPublicoDeCliente`; `captchaService.verificar` | 07 / 04 |
| `Configuracion/*` | `findFirstByUuidWithStatusIsActive`, `EntityRepositoryBorradoLogico`, `EntidadConBorradoLogico` (paquete) | ver kernel | 02 / 03 |
| `Application.java` | `repositoryBaseClass = EntityRepositoryBase.class` | `EntidadRepositoryBase.class` | 03 |
| `db/changelog` del proyecto | auditoría de `configuraciones` | nombres de auditoría nuevos | 02 |
| `application.*.properties` | propiedades listadas en cada módulo | sus nombres nuevos | 04–08 |

## Ticket 09: nombres internos que no estaban listados

El barrido del ticket 09 encontró en inglés constantes, helpers privados, variables locales, parámetros y nombres de
test que las filas anteriores no listaban. Se agregaron acá antes de usarlos. Son internos: no cambian el contrato
HTTP ni la base. Quedan en inglés, por las excepciones de [`idioma-y-estilo.md`](../../rules/idioma-y-estilo.md), los
overrides y parámetros de framework (`doFilterInternal`, `filterChain`, `handleMethodArgumentNotValid`), las variables
que llevan el nombre de su tipo de framework (`objectMapper`, `restTemplate`, `entityManager`, `mailSender`,
`handler`, `mapping`, `builder`, `helper`, `entity`, `requestEntity`, `responseEntity`, `joinPoint`), los términos de
terceros (`claims` y `bearer` de JWT/OAuth, `jwkSetURL`, `keySelector`, `clientId`, `resourceKey`) y los mensajes y
claves de log (`TRACE_START`, `parentSpanId`, `durationMs`, `[REDACTED]`).

### Código (Core y `Code`)

| Módulo | Clase | Viejo | Nuevo | Ticket |
|---|---|---|---|---|
| `core-kernel` | `CoreVersion` | `in` | `entrada` | 09 |
| `core-kernel` | `EntidadBase` | `UUID_V7_RANDOM` | `UUID_V7_ALEATORIO` | 09 |
| `core-kernel` | `EntidadBase` | `value` | `bytes` | 09 |
| `core-kernel` | `EntidadBase` | `timestamp` | `milisegundos` | 09 |
| `core-kernel` | `ExcepcionDeApi` | `cause` | `causa` | 09 |
| `core-kernel` | `InicializadorDeTraceIdGlobal` | `parentContext` | `contextoPadre` | 09 |
| `core-kernel` | `InicializadorDeTraceIdGlobal` | `previousContext` | `contextoAnterior` | 09 |
| `core-kernel` | `ManejadorDeExcepcionesDeApi` | `buildErrorResponse` | `construirRespuestaDeError` | 09 |
| `core-kernel` | `ManejadorDeExcepcionesDeApi` | `fieldName` | `nombreDelCampo` | 09 |
| `core-kernel` | `ManejadorDeExcepcionesDeApi` | `getRequestPayload` | `getCuerpoDelRequest` | 09 |
| `core-kernel` | `ManejadorDeExcepcionesDeApi` | `payload` | `contenido` | 09 |
| `core-kernel` | `ManejadorDeExcepcionesDeApi` | `body` | `cuerpo` | 09 |
| `core-kernel` | `ManejadorDeExcepcionesDeApi` | `wrapper` | `requestCacheado` | 09 |
| `core-kernel` | `MotorDeConsultas` | `queryCount` | `consultaDeConteo` | 09 |
| `core-kernel` | `MotorDeConsultas` | `querySelect` | `consultaDeFilas` | 09 |
| `core-kernel` | `MotorDeConsultas` | `query` | `consultaJpa` | 09 |
| `core-kernel` | `MotorDeConsultas` | `dtoClass` | `claseDto` | 09 |
| `core-kernel` | `MotorDeConsultas` | `entityClass` | `claseDeEntidad` | 09 |
| `core-kernel` | `ProyeccionDeDto` | `dtoClass` | `claseDto` | 09 |
| `core-kernel` | `Proyector` | `dtoClass` | `claseDto` | 09 |
| `core-kernel` | `Proyector` | `entityClass` | `claseDeEntidad` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `RESOLVING_DEBUG` | `RESOLVIENDO_DEBUG` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `REQUEST_CACHE_LIMIT` | `LIMITE_DE_CACHE_DEL_REQUEST` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `previousTraceId` | `traceIdAnterior` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `startNanos` | `inicioNanos` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `isDebugEnabled` | `debugHabilitado` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `isDebug` | `conDebug` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `cachedRequest` | `requestCacheado` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `cachedResponse` | `responseCacheado` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `method` | `metodo` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `shouldCacheResponse` | `debeCachearResponse` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `logRequestBody` | `registrarCuerpoDelRequest` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `logResponse` | `registrarResponse` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `logRequestEnd` | `registrarFinDelRequest` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `debugValue` | `valorDeDebug` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `body` | `cuerpo` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `payload` | `contenido` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `responseBody` | `cuerpoDelResponse` | 09 |
| `core-kernel` | `RequestLoggingFilter` | `durationMs` | `duracionMs` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `MAX_PAYLOAD_LOG_LENGTH` | `LARGO_MAXIMO_EN_LOG` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `REDACTED` | `TAPADO` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `OMITTED` | `OMITIDO` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `SENSITIVE_KEYS` | `CLAVES_SENSIBLES` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `payload` | `cuerpo` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `contentType` | `tipoDeContenido` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `isJsonContentType` | `esContenidoJson` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `sanitized` | `sanitizado` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `sanitizeNode` | `sanitizarNodo` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `result` | `resultado` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `exception` | `excepcion` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `headerName` | `nombreDelEncabezado` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `headerValue` | `valorDelEncabezado` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `normalizedName` | `nombreNormalizado` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `normalizeKey` | `normalizarClave` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `isSensitiveKey` | `esClaveNormalizadaSensible` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `value` | `valor` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `additionalSensitiveKeys` | `clavesSensiblesAdicionales` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `additionalKeys` | `clavesAdicionales` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `key` | `clave` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `node` | `nodo` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `sanitizedArray` | `listaSanitizada` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `child` | `hijo` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `sanitizedObject` | `objetoSanitizado` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `field` | `campo` | 09 |
| `core-kernel` | `SanitizadorDeLogs` | `normalizedKey` | `claveNormalizada` | 09 |
| `core-kernel` | `TraceContext` | `SAFE_TRACE_ID` | `TRACE_ID_SEGURO` | 09 |
| `core-kernel` | `TraceContext` | `requestedTraceId` | `traceIdPedido` | 09 |
| `core-kernel` | `TraceDeEjecucionAspect` | `DEPTH` | `PROFUNDIDAD` | 09 |
| `core-kernel` | `TraceDeEjecucionAspect` | `isDebugEnabled` | `debugHabilitado` | 09 |
| `core-kernel` | `TraceDeEjecucionAspect` | `debugValue` | `valorDeDebug` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `SPAN_ID_KEY` | `SPAN_ID_CLAVE` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `SPAN_DEPTH_KEY` | `SPAN_PROFUNDIDAD_CLAVE` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `TRACE_ENABLED_KEY` | `TRACE_HABILITADO_CLAVE` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `TRACE_EXCLUDED_FIELDS_KEY` | `TRACE_CAMPOS_EXCLUIDOS_CLAVE` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `traceOperation` | `trazarOperacion` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `activateTrace` | `activarTrace` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `previousContext` | `contextoAnterior` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `excludedFields` | `camposExcluidos` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `parentSpanId` | `spanIdPadre` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `generateSpanId` | `generarSpanId` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `depth` | `profundidad` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `parseDepth` | `leerProfundidad` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `operation` | `operacion` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `resolveOperation` | `resolverOperacion` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `startNanos` | `inicioNanos` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `valueOrDash` | `valorOGuion` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `sanitizeArguments` | `sanitizarArgumentos` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `result` | `resultado` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `durationMs` | `duracionMs` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `elapsedMillis` | `milisTranscurridos` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `sanitizeResult` | `sanitizarResultado` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `restoreContext` | `restaurarContexto` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `className` | `nombreDeClase` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `parameterNames` | `nombresDeParametros` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `arguments` | `argumentos` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `values` | `valores` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `index` | `indice` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `parameterName` | `nombreDeParametro` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `value` | `valor` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `response` | `respuesta` | 09 |
| `core-kernel` | `TrazabilidadAspect` | `exception` | `excepcion` | 09 |
| `core-kernel` | `ZonaHorariaSmokeTestService` | `EXPECTED_OFFSET_SECONDS` | `DESFASE_ESPERADO_EN_SEGUNDOS` | 09 |
| `core-kernel` | `ZonaHorariaSmokeTestService` | `TOLERANCE_SECONDS` | `TOLERANCIA_EN_SEGUNDOS` | 09 |
| `core-kernel` | `ZonaHorariaSmokeTestService` | `validateServerTimeZone` | `validarZonaHorariaDelServidor` | 09 |
| `core-kernel` | `ZonaHorariaSmokeTestService` | `validateDatabaseTimeZone` | `validarZonaHorariaDeLaBase` | 09 |
| `core-kernel` | `ZonaHorariaSmokeTestService` | `offsetSeconds` | `desfaseEnSegundos` | 09 |
| `core-kernel` | `ZonaHorariaSmokeTestService` | `consultaOffsetSegundos` | `consultaDeDesfaseEnSegundos` | 09 |
| `core-crud` | `AbstractService` | `dtoList` | `dtos` | 09 |
| `core-crud` | `AbstractService` | `model` | `modelo` | 09 |
| `core-crud` | `BorradoFisico` | `entityClass` | `claseDeEntidad` | 09 |
| `core-crud` | `BorradoFisico` | `model` | `modelo` | 09 |
| `core-crud` | `BorradoLogico` | `entityClass` | `claseDeEntidad` | 09 |
| `core-crud` | `BorradoLogico` | `model` | `modelo` | 09 |
| `core-crud` | `EstrategiaDeBorrado` | `entityClass` | `claseDeEntidad` | 09 |
| `core-crud` | `EstrategiaDeBorrado` | `model` | `modelo` | 09 |
| `core-crud` | `SelectorDeBorrado` | `entityClass` | `claseDeEntidad` | 09 |
| `core-identity` | `AuthBasicService` | `authHeader` | `encabezadoAuth` | 09 |
| `core-identity` | `AuthBasicService` | `base64Credentials` | `credencialesBase64` | 09 |
| `core-identity` | `AuthBasicService` | `decodedBytes` | `bytesDecodificados` | 09 |
| `core-identity` | `AuthBasicService` | `decoded` | `decodificado` | 09 |
| `core-identity` | `AuthBasicService` | `hasBasicAuthorizationHeader` | `tieneEncabezadoBasic` | 09 |
| `core-identity` | `AuthBasicService` | `user` | `usuario` | 09 |
| `core-identity` | `AuthService` | `user` | `usuario` | 09 |
| `core-identity` | `AuthService` | `roleNames` | `nombresDeRoles` | 09 |
| `core-identity` | `AuthService` | `permissionNames` | `nombresDePermisos` | 09 |
| `core-identity` | `AuthService` | `required` | `requeridos` | 09 |
| `core-identity` | `AuthService` | `req` | `requerido` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `syncPermissions` | `sincronizarPermisos` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `syncRoles` | `sincronizarRoles` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `syncInitialUser` | `sincronizarUsuarioInicial` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `permissions` | `permisos` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `permEnum` | `permisoDelEnum` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `permission` | `permiso` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `roleEnum` | `rolDelEnum` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `role` | `rol` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `enumPermissions` | `permisosDelEnum` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `dbPermissions` | `permisosDeLaBase` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `seenPermissionNames` | `nombresDePermisosVistos` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `user` | `usuario` | 09 |
| `core-identity` | `InicializadorDeBaseService` | `sysAdminRole` | `rolSysadmin` | 09 |
| `core-identity` | `PermisoService` | `currentUser` | `usuarioActual` | 09 |
| `core-identity` | `PermisoService` | `requesterPerms` | `permisosDelSolicitante` | 09 |
| `core-identity` | `PermisoService` | `dtoList` | `dtos` | 09 |
| `core-identity` | `RolBaseEnum` | `all` | `todos` | 09 |
| `core-identity` | `RolBaseEnum` | `exclude` | `excluir` | 09 |
| `core-identity` | `RolBaseEnum` | `allPermissions` | `todosLosPermisos` | 09 |
| `core-identity` | `RolBaseEnum` | `exclusions` | `exclusiones` | 09 |
| `core-identity` | `RolBaseEnum` | `temp` | `resultado` | 09 |
| `core-identity` | `RolBaseService` | `currentUser` | `usuarioActual` | 09 |
| `core-identity` | `RolBaseService` | `isCreate` | `esAlta` | 09 |
| `core-identity` | `RolBaseService` | `requiredPerm` | `permisoRequerido` | 09 |
| `core-identity` | `RolBaseService` | `requesterPerms` | `permisosDelSolicitante` | 09 |
| `core-identity` | `RolBaseService` | `permName` | `nombreDePermiso` | 09 |
| `core-identity` | `RolBaseService` | `perm` | `permiso` | 09 |
| `core-identity` | `RolBaseService` | `role` | `rol` | 09 |
| `core-identity` | `RolBaseService` | `dbPermissions` | `permisosDeLaBase` | 09 |
| `core-identity` | `RolBaseService` | `existingDefaults` | `rolesPorDefectoExistentes` | 09 |
| `core-identity` | `RolBaseService` | `model` | `modelo` | 09 |
| `core-identity` | `RolBaseService` | `dtoList` | `dtos` | 09 |
| `core-identity` | `UsuarioBaseService` | `isCreate` | `esAlta` | 09 |
| `core-identity` | `UsuarioBaseService` | `requiredPerm` | `permisoRequerido` | 09 |
| `core-identity` | `UsuarioBaseService` | `model` | `modelo` | 09 |
| `core-identity` | `UsuarioBaseService` | `cooldownEnd` | `finDeLaEspera` | 09 |
| `core-identity` | `UsuarioBaseService` | `chars` | `caracteres` | 09 |
| `core-identity` | `UsuarioBaseService` | `random` | `aleatorio` | 09 |
| `core-identity` | `UsuarioBaseService` | `prevCode` | `codigoAnterior` | 09 |
| `core-identity` | `UsuarioBaseService` | `fiveMinutesAgo` | `haceCincoMinutos` | 09 |
| `core-identity` | `UsuarioBaseService` | `minutesAgo` | `haceQuinceMinutos` | 09 |
| `core-identity` | `UsuarioBaseService` | `code` | `codigoDeSeguridad` | 09 |
| `core-identity` | `UsuarioBaseService` | `user` | `usuario` | 09 |
| `core-identity` | `UsuarioBaseService` | `usuario (Optional de recuperarContrasena)` | `encontrado` | 09 |
| `core-identity` | `UsuarioRepositorioJpa` | `where` | `condicion` | 09 |
| `core-identity` | `UsuarioRepositorioJpa` | `query` | `consulta` | 09 |
| `core-multiempresa` | `AltaDeEmpresaPorProyector` | `model` | `modelo` | 09 |
| `core-multiempresa` | `ContextoDeEmpresa` | `company` | `empresa` | 09 |
| `core-multiempresa` | `ContextoDeEmpresa` | `user` | `usuario` | 09 |
| `core-multiempresa` | `RolBaseMultiempresaService` | `currentUser` | `usuarioActual` | 09 |
| `core-multiempresa` | `RolBaseMultiempresaService` | `currentCompany` | `empresaActual` | 09 |
| `core-multiempresa` | `RolBaseMultiempresaService` | `currentIsGlobal` | `usuarioEsGlobal` | 09 |
| `core-multiempresa` | `RolBaseMultiempresaService` | `existing` | `existente` | 09 |
| `core-multiempresa` | `RolBaseMultiempresaService` | `role` | `rol` | 09 |
| `core-multiempresa` | `RolBaseMultiempresaService` | `isSystemRole` | `esRolDeSistema` | 09 |
| `core-multiempresa` | `RolBaseMultiempresaService` | `model` | `modelo` | 09 |
| `core-multiempresa` | `SincronizadorDeRolesExternosMultiempresa` | `company` | `empresaDelContexto` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `currentUser` | `usuarioActual` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `headerCompany` | `empresaDelEncabezado` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `currentIsGlobal` | `usuarioEsGlobal` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `currentIsLocal` | `usuarioEsAdminLocal` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `isCreate` | `esAlta` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `requiredPerm` | `permisoRequerido` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `rolesByUuid` | `rolesPorUuid` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `belongsToCompany` | `perteneceALaEmpresa` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `requesterPerms` | `permisosDelSolicitante` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `ucrDto` | `relacionDto` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `rDto` | `rolDto` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `dbRole` | `rolDeLaBase` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `perm` | `permiso` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `roleUuids` | `uuidsDeRoles` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `roleDto` | `rolDto` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `role` | `rol` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `user` | `usuario` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `model` | `modelo` | 09 |
| `core-multiempresa` | `UsuarioBaseMultiempresaService` | `managerRole` | `rolAdmin` | 09 |
| `core-crypter` | `CrypterService` | `validateConfiguration` | `validarConfiguracion` | 09 |
| `core-crypter` | `CrypterService` | `isBlank` | `estaVacio` | 09 |
| `core-crypter` | `CrypterService` | `valueOrNull` | `valorONull` | 09 |
| `core-crypter` | `CrypterService` | `maskSecret` | `enmascararSecreto` | 09 |
| `core-crypter` | `CrypterService` | `value` | `valor` | 09 |
| `core-crypter` | `CrypterService` | `getHeaders` | `getEncabezados` | 09 |
| `core-crypter` | `CrypterService` | `headers` | `encabezados` | 09 |
| `core-crypter` | `CrypterService` | `encodedAuth` | `authCodificado` | 09 |
| `core-crypter` | `CrypterService` | `authHeader` | `encabezadoAuth` | 09 |
| `core-crypter` | `CrypterService` | `requestBodyList` | `cuerposDelRequest` | 09 |
| `core-crypter` | `CrypterService` | `item` | `elemento` | 09 |
| `core-crypter` | `CrypterService` | `requestBody` | `cuerpoDelRequest` | 09 |
| `core-crypter` | `CrypterService` | `body` | `cuerpo` | 09 |
| `core-crypter` | `CrypterService` | `putFileToCrypter` | `guardarArchivoEnCrypter` | 09 |
| `core-crypter` | `CrypterService` | `today` | `hoy` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `host` | `servidor` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `port` | `puerto` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `username` | `usuario` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `password` | `contrasena` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `protocol` | `protocolo` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `mailOption` | `opcion` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `validateConfiguration` | `validarConfiguracion` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `isBlank` | `estaVacio` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `valueOrNull` | `valorONull` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `maskSecret` | `enmascararSecreto` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `value` | `valor` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `getMailSender` | `crearMailSender` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `props` | `propiedades` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `message` | `mensaje` | 09 |
| `core-mail` | `EnviadorDeMailSmtp` | `entry` | `adjunto` | 09 |
| `core-captcha` | `CaptchaService` | `validateConfiguration` | `validarConfiguracion` | 09 |
| `core-captcha` | `CaptchaService` | `logAndBuildException` | `registrarYCrearExcepcion` | 09 |
| `core-captcha` | `CaptchaService` | `body` | `cuerpo` | 09 |
| `core-sm3` | `SM3Service` | `validateConfiguration` | `validarConfiguracion` | 09 |
| `core-sm3` | `SM3Service` | `getHeaders` | `getEncabezados` | 09 |
| `core-sm3` | `SM3Service` | `headers` | `encabezados` | 09 |
| `core-sm3` | `SM3Service` | `encodedAuth` | `authCodificado` | 09 |
| `core-sm3` | `SM3Service` | `authHeader` | `encabezadoAuth` | 09 |
| `core-sm3` | `SM3Service` | `fileResource` | `recursoDelArchivo` | 09 |
| `core-sm3` | `SM3Service` | `body` | `cuerpo` | 09 |
| `core-protocolo-apikey` | `AuthTokenService` | `CHARACTERS` | `CARACTERES` | 09 |
| `core-protocolo-apikey` | `AuthTokenService` | `random` | `ALEATORIO` | 09 |
| `core-protocolo-apikey` | `AuthTokenService` | `isJwtFormat` | `esFormatoJwt` | 09 |
| `core-protocolo-apikey` | `AuthTokenService` | `generateRandomToken` | `generarTokenAleatorio` | 09 |
| `core-protocolo-apikey` | `AuthTokenService` | `length` | `largo` | 09 |
| `core-protocolo-apikey` | `AuthTokenService` | `model` | `modelo` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `getSigningKey` | `getClaveDeFirma` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `now` | `ahora` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `tokenExpiryDate` | `vencimientoDelToken` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `requestRefreshToken` | `refreshTokenRecibido` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `newRefreshToken` | `nuevoRefreshToken` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `getTokenFromRequest` | `getTokenDelRequest` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `resolveUserFromAccessToken` | `resolverUsuarioDelAccessToken` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `context` | `contexto` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `type` | `tipo` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `isJwtFormat` | `esFormatoJwt` | 09 |
| `core-protocolo-jwt` | `AuthJwtService` | `user` | `usuario` | 09 |
| `core-protocolo-sso` | `AuthMalvinasService` | `malvinasUrl` | `urlDeMalvinas` | 09 |
| `core-protocolo-sso` | `AuthMalvinasService` | `isJwtFormat` | `esFormatoJwt` | 09 |
| `core-protocolo-sso` | `AuthMalvinasService` | `user` | `usuario` | 09 |
| `core-protocolo-sso` | `AuthSsoService` | `ssoUrl` | `urlDelSso` | 09 |
| `core-protocolo-sso` | `AuthSsoService` | `ssoclientId` | `clientIdDelSso` | 09 |
| `core-protocolo-sso` | `AuthSsoService` | `validateConfiguration` | `validarConfiguracion` | 09 |
| `core-protocolo-sso` | `AuthSsoService` | `requestBody` | `cuerpoDelRequest` | 09 |
| `core-protocolo-sso` | `AuthSsoService` | `headers` | `encabezados` | 09 |
| `core-protocolo-sso` | `AuthSsoService` | `user` | `usuario` | 09 |
| `core-protocolo-sso` | `MalvinasJwtDecoderConfig` | `malvinasUrl` | `urlDeMalvinas` | 09 |
| `core-test` | `ContratoDeAbmTest` | `guardarBodyInvalido` | `guardarCuerpoInvalido` | 09 |
| `core-test` | `ContratoDeAbmTest` | `path() / OperacionAbm.path` | `ruta() / OperacionAbm.ruta` | 09 |
| `core-test` | `EnsambladorDeContrato` | `ejemplosDelBody` | `ejemplosDelCuerpo` | 09 |
| `core-test` | `EnsambladorDeContrato` | `asMap` | `comoMapa` | 09 |
| `app (Code)` | `ConfiguracionGuardarUseCase` | `model` | `modelo` | 09 |
| `app (Code)` | `EmpresaService` | `model` | `modelo` | 09 |
| `app (Code)` | `RolService` | `model` | `modelo` | 09 |
| `app (Code)` | `UsuarioController` | `currentUser` | `usuarioActual` | 09 |
| `app (Code)` | `UsuarioController` | `userDTO` | `usuarioDTO` | 09 |
| `app (Code)` | `UsuarioController` | `body` | `cuerpo` | 09 |

### Tests (Core y `app`)

Se aplican en todos los tests donde aparecen.

| Viejo | Nuevo | Ticket |
|---|---|---|
| `body` | `cuerpo` | 09 |
| `payload` | `contenido` | 09 |
| `result` | `resultado` | 09 |
| `role` | `rol` | 09 |
| `currentUser` | `usuarioActual` | 09 |
| `roleService` | `rolService` | 09 |
| `companyUuid` | `empresaUuid` | 09 |
| `roleUuid` | `rolUuid` | 09 |
| `setUp` | `preparar` | 09 |
| `param` | `parametro` | 09 |
| `sanitizer` | `sanitizador` | 09 |
| `target` | `objetivo` | 09 |
| `resolved` | `resuelto` | 09 |
| `username` | `nombreDeUsuario` | 09 |
| `password` | `contrasena` | 09 |
| `USERNAME` | `NOMBRE_DE_USUARIO` | 09 |
| `DEMO_USERNAME` | `DEMO_NOMBRE_DE_USUARIO` | 09 |
| `dtoClass` | `claseDto` | 09 |
| `sanitizarCuerpo_redactsSensitiveNestedFieldsAndArrays` | `sanitizarCuerpo_tapaCamposSensiblesAnidadosYListas` | 09 |
| `sanitizarEncabezado_redactsCredentials` | `sanitizarEncabezado_tapaCredenciales` | 09 |
| `sanitizarValor_appliesAdditionalExcludedFields` | `sanitizarValor_aplicaLosCamposExcluidosAdicionales` | 09 |
| `resolver_acceptsSafeIncomingTraceId` | `resolver_aceptaUnTraceIdEntranteSeguro` | 09 |
| `resolver_replacesUnsafeIncomingTraceId` | `resolver_reemplazaUnTraceIdEntranteInseguro` | 09 |
| `traceAutomaticallyInterceptsServiceWhenTraceIsActive` | `trazarInterceptaLosServicesMientrasHayUnaTrazaActiva` | 09 |
| `trace_propagatesTraceIdAndRestoresMdcAfterSuccess` | `trazar_propagaElTraceIdYRestauraElMdcAlTerminarBien` | 09 |
| `trace_restoresMdcAndRethrowsOriginalException` | `trazar_restauraElMdcYRelanzaLaExcepcionOriginal` | 09 |
| `desde_tomaStatusYUserMessagePorDefectoDelCodigo` | `desde_tomaStatusYMensajeParaUsuarioPorDefectoDelCodigo` | 09 |
| `errorResponseDTO_noExponeDeveloperMessageNiCampoDeThrowable` | `errorResponseDTO_noExponeMensajeParaDesarrolladorNiCampoDeThrowable` | 09 |
| `subError_noExponeValue` | `subError_noExponeElValor` | 09 |

### Documentación

| Dónde | Viejo | Nuevo | Ticket |
|---|---|---|---|
| Ejemplos de hooks en `core-crud/docs`, `core-kernel/docs/proyeccion.md` y las reglas | `model`, `currentUser`, `dtoList` | `modelo`, `usuarioActual`, `dtos` | 09 |
| `liquibase/nombres-constraints.md` | `fk_<tabla>_<columna_sin_sufijo_id>` (`company_id` → `fk_invoices_company`) | `fk_<tabla>_<columna_sin_prefijo_id>` (`id_empresa` → `fk_facturas_empresa`) | 09 |
| Ejemplos de `liquibase/*.md` | `invoices`, `currencies`, `example_table`, `name`, `code`, `description`, `active`, `company_id`, `user_id` | `facturas`, `monedas`, `tabla_de_ejemplo`, `nombre`, `codigo`, `descripcion`, `activo`, `id_empresa`, `id_usuario` | 09 |

## Revisión 3.3.0: correcciones de la revisión final

Nombres que la revisión final de la iniciativa encontró en inglés o que hizo falta crear para corregirla. Se agregaron
acá antes de usarlos. Las filas marcadas "revisión 3.3.0" son las únicas que cambian algo después del ticket 09.

### Código (Core y `Code`)

| Módulo | Clase | Viejo | Nuevo | Ticket | Nota |
|---|---|---|---|---|---|
| `core-multiempresa` | `EmpresaRepositorioJpa` | `where` (parámetro de `primera`) | `condicion` | revisión 3.3.0 | Igual que su gemelo `UsuarioRepositorioJpa` |
| `core-multiempresa` | `EmpresaRepositorioJpa` | `query` | `consulta` | revisión 3.3.0 | Igual que `UsuarioRepositorioJpa` |
| `core-kernel` | `ConsultaDePaginado` | `where` (variable y método) | `clausulaDeCondiciones` | revisión 3.3.0 | Arma el `WHERE` del JPQL |
| `core-kernel` | `ConsultaDePaginado` | `orderBy` (variable y método) | `clausulaDeOrden` | revisión 3.3.0 | Arma el `ORDER BY` del JPQL |
| `core-kernel` | `ConsultaDePaginado` | `groupBy` | `columnasDeAgrupacion` | revisión 3.3.0 | |
| `core-protocolo-jwt` | `AuthJwtService.autenticarConContrasena`, `JwtLoginController.iniciarSesion` | `body` (parámetro) | `cuerpo` | revisión 3.3.0 | Igual que `UsuarioController` (ticket 09) |
| `core-test` | `Peticion` | `path` (campo y parámetro de `get/post/put/patch/delete`) | `ruta` | revisión 3.3.0 | Igual que `ContratoDeAbmTest.ruta()` (ticket 09) |
| `core-test` | `Intercambio` | `path` (componente del record) | `ruta` | revisión 3.3.0 | |
| `core-test` | `PruebaDeContrato` | `path` (variable local) | `ruta` | revisión 3.3.0 | |
| `core-test` | `RegistroDeContratos` | `path`, `query`, `header` (ubicación del parámetro y la clave `"path"` del contrato de la operación) | quedan | — | Son los valores de `in` y la clave `paths` de OpenAPI (tercero). `context-path` queda: propiedad de Spring |
| `app (Code)` | `UsuarioController.verificarCodigo` | body `Map<String, String>` | `Usuario/UsuarioVerificarCodigoRequestDTO` | revisión 3.3.0 | Hereda de `EntidadBaseDTO`. Mismo JSON: `{uuid, codigo}` |
| `app (Code)` | `UsuarioController.recuperarContrasena` | body `Map<String, String>` | `Usuario/UsuarioRecuperarContrasenaRequestDTO` | revisión 3.3.0 | Hereda de `EntidadBaseDTO`. Mismo JSON: `{email}` |
| `core-kernel` | `SanitizadorDeLogs.CLAVES_SENSIBLES` | `contenido` | `contenidodelarchivo` | revisión 3.3.0 | `contenido` chocaba con `Pagina<T>.contenido` (ticket 03), que se tapaba en los logs. `contenidoarchivo` queda |
| Todo DTO (Core y `Code`) | campo / JSON con el contenido de un archivo | `contenido` | `contenidoDelArchivo` | revisión 3.3.0 | Convención para que el log lo tape. Hoy ningún DTO del Core ni del template lo tiene: los de Crypter (`ImagenCrypterDTO`) y SM3 (multipart `file`) usan los nombres del tercero |

## Casos dudosos (resueltos: se aplica la opción recomendada en todos)

Cada caso se resuelve con el usuario antes de ejecutar el ticket que lo toca. Las filas del glosario están
escritas con la opción recomendada.

### D1. `CustomException` y `CustomRestExceptionHandler` (ticket 04)

"Custom" y "Exception"/"Handler" no están en la lista cerrada. Son los nombres más usados del proyecto (reglas,
docs, todo `Code`).

- **a)** Traducir: `ExcepcionDeApi` y `ManejadorDeExcepcionesDeApi`.
- **b)** Traducir literal: `ExcepcionPersonalizada` y `ManejadorDeExcepcionesRest`.
- **c)** Mantener ambos como excepción nombrada en la lista cerrada.

Recomendación: **a**. Describe qué es (la excepción que termina en una respuesta de la API con su código) mejor
que "personalizada", y la regla de idioma no deja otra excepción para nombres propios del Core.

### D2. `KeyValue` (ticket 04)

Sin ningún uso en el Core ni en `Code`.

- **a)** Borrarla.
- **b)** Traducir a `ClaveValor` (`clave`, `valor`).
- **c)** Mantenerla.

Recomendación: **a**. Código muerto; si algún proyecto la necesita, la escribe en español.

### D3. `SimpleValue` (ticket 04)

Sin ningún uso en el Core ni en `Code`.

- **a)** Borrarla.
- **b)** Traducir a `ValorSimple` (`texto`, `entero`, `fecha`, `fechaHora`).
- **c)** Mantenerla.

Recomendación: **a**, por lo mismo que D2.

### D4. `ApiResponseDTO` (ticket 04)

Solo la usa la respuesta de `cambiar-contrasena` (`{status, message, data}`), mientras que `recuperar-contrasena`
responde un mapa `{message}`.

- **a)** Traducir a `RespuestaApiDTO` con `status`, `mensaje`, `datos`.
- **b)** Borrarla y que `cambiar-contrasena` responda `200` sin cuerpo (o el mismo `{mensaje}` que
  `recuperar-contrasena`).
- **c)** Mantenerla.

Recomendación: **a**. Es el cambio mínimo de contrato que cumple la regla; unificar las dos respuestas es otro
cambio que se puede pedir aparte.

### D5. `SSOAuthService` (ticket 08)

Integración vieja, duplicada por `AuthSsoService`: no la usa nadie, lee `sso.url`/`sso.clientId` en vez de
`auth.sso.*` y autoriza con `valid(...)`/`validBoolean(...)` por rol, que `controllers-y-endpoints.md` prohíbe
(la autorización va en `@Contrato`).

- **a)** Borrarla junto con las propiedades `sso.url` y `sso.clientId`.
- **b)** Renombrarla a `IntegracionSsoService` y traducir sus métodos (`validar`, `esValido`).
- **c)** Mantenerla.

Recomendación: **a**.

### D6. `AuthMalvinasService` (ticket 08)

`Auth` es término reservado y `Malvinas` es nombre propio del IDP, así que encaja; la duda es si conviene
alinearlo con `AuthSsoService` o hacer explícito que es un IDP.

- **a)** Mantener `AuthMalvinasService`.
- **b)** `AuthMalvinasIdpService`.
- **c)** `AuthPorMalvinasService`.

Recomendación: **a**. No tiene ninguna palabra fuera de las excepciones.

### D7. "Entity" en las clases base y paquetes del Core (tickets 02 y 03)

`AbstractEntity`, `AbstractEntityDTO`, `EntityRepository`, `EntityRepositoryBase`, `EntityRepositoryBorradoLogico`
y los paquetes `Entity/` de kernel, identity, monoempresa y multiempresa. `Abstract*` es sufijo reservado;
`Entity` no figura en la lista, aunque es el nombre de la anotación de JPA.

- **a)** Traducir: `EntidadBase`, `EntidadBaseDTO`, `EntidadRepository`, `EntidadRepositoryBase`,
  `EntidadRepositoryBorradoLogico`, paquetes `Entidad/`.
- **b)** Agregar `Entity` a la lista como nombre de framework (JPA) y dejar todo como está.
- **c)** Traducir manteniendo `Abstract`: `AbstractEntidad`, `AbstractEntidadDTO`.

Recomendación: **a**. Ya conviven `EntidadConBorradoLogico` y `EntityRepositoryBorradoLogico`; con (a) el kernel
queda uniforme y `@Entity` sigue siendo solo la anotación de JPA.

### D8. Anglicismos de uso corriente: `token`, `email`, `login`, `smoke test` (tickets 04, 06, 08)

No están en la lista cerrada, pero el proyecto ya los usa como palabras sueltas (`TOKEN_INVALIDO`,
`EMAIL_YA_REGISTRADO`, "smoke test" en las reglas).

- **a)** Agregar `token`, `email`, `login` y `smoke test` a los términos reservados.
- **b)** Traducir todos: `credencial`/`ficha`, `correo`, `inicio de sesión`, `prueba de humo`.
- **c)** Mixto: reservar `token`, `email` y `smoke test`; traducir `login` (`POST /login` →
  `/iniciar-sesion`, `GET /usuario/login` → `/usuario/sesion`, `loginJWT` → `iniciarSesion`).

Recomendación: **c**. `token` y `email` no tienen traducción natural en el dominio y ya están en códigos de error
publicados; `login` sí tiene una traducción clara. `JwtLoginController` queda por ser nombre de clase del
protocolo, o pasa a `JwtSesionController` si el usuario prefiere ser estricto.

### D9. `Response` como sufijo (ticket 04)

`entidades-y-dtos.md` define la convención `{Entidad}{CasoDeUso}ResponseDTO` y el kernel tiene
`ErrorResponseDTO`, pero la lista cerrada solo reserva `Request`.

- **a)** Agregar `Response` a los términos reservados, junto a `Request`.
- **b)** Traducir: `{Entidad}{CasoDeUso}RespuestaDTO` y `RespuestaDeErrorDTO`.
- **c)** Mantener `Response` solo en `ErrorResponseDTO` y traducir la convención.

Recomendación: **a**. Es el par de `Request`, ya está en las reglas y `ResponseEntity` (Spring) convive en
todos los controllers.

### D10. Forma de la respuesta del paginado (ticket 03)

Hoy `POST /paginate` responde el `Page` de Spring Data serializado tal cual (`content`, `totalElements`,
`pageable`, `sort`, ...). El ticket 03 pide la respuesta con los nombres del glosario.

- **a)** DTO propio del kernel `Pagina<T>` con `contenido`, `totalDeElementos`, `totalDePaginas`, `tamano`,
  `numero`, `cantidadDeElementos`, `primera`, `ultima`, `vacia` (sin `pageable` ni `sort`), y las firmas del
  paginado devuelven `Pagina<T>`.
- **b)** Mantener `Page` de Spring como nombre de framework (la respuesta queda en inglés).
- **c)** `PagedModel` de Spring Data (`VIA_DTO`): más estable que `PageImpl`, pero también en inglés.

Recomendación: **a**. Es lo que pide el ticket 03 y además fija la forma del JSON, que hoy depende de la versión
de Spring Data.

### D11. Nombres de módulos y paquetes de módulo (todos)

El ADR deja `Kernel` y `Crud` como nombres de módulo. Quedan en duda `core-identity`/`Identity`
(`IdentityConfiguracion`, `CodigoDeErrorIdentity`, `core.identity.*`), `core-protocolo-apikey`/`ApiKey`
(`CodigoDeErrorApiKey`) y `core-test`/`Test` (`TestConBaseDeDatos`).

- **a)** Tratarlos como nombres de módulo y mantenerlos, igual que `Kernel` y `Crud`.
- **b)** Traducirlos: `core-identidad`/`Identidad`, `core-protocolo-clave-api`/`ClaveApi`, `core-prueba`/`Prueba`
  (cambia el `artifactId` que declara cada proyecto en su `app/pom.xml`).
- **c)** Mantener los `artifactId` y traducir solo paquetes y clases.

Recomendación: **a**. Renombrar un `artifactId` obliga a tocar el `pom.xml` de cada proyecto sin ganancia para el
contrato, y deja la regla simple: "los nombres de módulo del Core quedan".

### D12. Prefijo `auth_` de las tablas (tickets 05 a 08)

- **a)** Mantener `auth_` (término reservado) y traducir el resto: `auth_usuarios`, `auth_permisos`,
  `auth_roles_permisos`, `auth_usuarios_roles`, `auth_usuarios_empresas`, `auth_usuarios_empresas_roles`,
  `auth_access_tokens`; `security_code` → `codigos_de_seguridad` y `companies` → `empresas` siguen sin prefijo.
- **b)** Quitar el prefijo: `usuarios`, `permisos`, `roles`, `roles_permisos`, ...
- **c)** Unificar todo con prefijo: también `auth_codigos_de_seguridad` y `auth_empresas`.

Recomendación: **a**. Es el cambio mínimo: el prefijo ya agrupa las tablas de autenticación y es término
reservado.

### D13. Códigos de error publicados con palabras en inglés (tickets 04 y 07)

`COMPANY_REQUERIDA`, `COMPANY_NO_ENCONTRADA` (`CodigoDeErrorMultiempresa`) y `MEDIA_TYPE_NO_SOPORTADO`
(`CodigoDeErrorKernel`). `errores.md` regla 3: un código publicado no se renombra.

- **a)** Mantener los códigos (valor persistido/publicado) y traducir solo su `userMessage`.
- **b)** Renombrarlos en este corte (`EMPRESA_REQUERIDA`, `EMPRESA_NO_ENCONTRADA`, `TIPO_DE_CONTENIDO_NO_SOPORTADO`),
  aprovechando que el front se actualiza en el mismo release.
- **c)** Agregar los códigos nuevos y mantener los viejos hasta un release posterior.

Recomendación: **a**. La regla 3 de `errores.md` es explícita y el front discrimina por `codigo`.

### D14. Nombres de los métodos de repositorio con `@Query` (tickets 03, 05, 07)

Los métodos derivados conservan las palabras clave de Spring Data porque el framework las interpreta. Los que
tienen `@Query` admiten cualquier nombre.

- **a)** Traducirlos a español: `buscarVigentePorUuid`, `existeRolParaUsuario`, `buscarPorUsuarioYEmpresa`.
- **b)** Mantener el estilo `find…By…` traduciendo solo las propiedades: `findFirstVigenteByUuid`,
  `existsRolForUsuario`.
- **c)** Convertir los que se puedan a métodos derivados.

Recomendación: **a**. `buscarVigentePorUuid` es el mismo nombre que ya usa `EstrategiaDeBorrado`, y el estilo
`find…By…` queda reservado a lo que de verdad interpreta Spring Data.

### D15. Nombres de constraints, índices y FKs (tickets 02 a 08)

`uk_auth_users_uuid`, `fk_security_code_user`, `fk_auth_roles_company`, etc.

- **a)** Mantenerlos: no son contrato, y renombrarlos exige `dropForeignKeyConstraint`/`addForeignKeyConstraint`
  por cada uno.
- **b)** Renombrarlos en changesets nuevos para que coincidan con las tablas nuevas.
- **c)** Renombrar solo los de las tablas que cambian de nombre.

Recomendación: **a**.

### D16. Verbos HTTP y `json` en el DSL de pruebas (ticket 04)

`Peticion.get/post/put/patch/delete/json` en `core-test`.

- **a)** Mantenerlos: son los nombres del protocolo HTTP y del formato.
- **b)** Traducirlos (`obtener`, `enviar`, `reemplazar`, `modificar`, `borrar`, `cuerpoJson`).

Recomendación: **a**.

### D17. Otras clases sin uso: `ControllerValidaciones` y `UserRolDTO` (tickets 04 y 05)

- **a)** Borrarlas.
- **b)** Traducirlas (`ValidacionesDeController`, `UsuarioRolDTO`).

Recomendación: **a**.

### D18. Valores persistidos de `proposito` en los códigos de seguridad (ticket 06)

`security_code.purpose` guarda `FORGOT_PASSWORD` y `REGISTRATION`. La fila "Valores persistidos" de la lista
cerrada nombra roles, permisos y códigos de error, no estos.

- **a)** Mantener los valores; solo cambian las constantes Java (`PROPOSITO_RECUPERAR_CONTRASENA`,
  `PROPOSITO_REGISTRO`).
- **b)** Migrar los valores con un changeset `update` (`RECUPERAR_CONTRASENA`, `REGISTRO`).

Recomendación: **a**. Son valores de vida corta y no viajan en el contrato; cambiarlos solo agrega una migración
de datos.

### D19. Claims del JWT y valores de `auth.protocolos` (tickets 06 y 08)

Claim `type` = `access` del JWT emitido y valores `jwt`, `basic`, `tokens`, `sso`, `malvinas` de la propiedad.

- **a)** Mantenerlos: un token emitido antes de migrar sigue validando y los `.properties` de los ambientes no
  cambian de valor.
- **b)** Traducirlos (`tipo` = `acceso`; `basico`, `tokens`) invalidando los tokens vigentes.

Recomendación: **a**. Cumple el check del ticket 08 ("un token emitido antes de migrar sigue validando").

### D20. Encabezado `X-Company-Uuid` (ticket 07)

- **a)** Traducir a `X-Empresa-Uuid`.
- **b)** Mantener `X-Company-Uuid` por convención de encabezados HTTP en inglés.

Recomendación: **a**. Es parte del contrato y no es nombre de tercero; el front se actualiza en el mismo release.
`X-Captcha-Token`, `X-Trace-Id`, `API-Version` y `Authorization` quedan por las excepciones existentes.
