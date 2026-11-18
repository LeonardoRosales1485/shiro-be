# Idioma y Estilo de Código

## Idioma

- Todo el código (entidades, columnas, funciones, endpoints, variables, comentarios) se escribe en español.
- No se usan tildes, eñes ni caracteres especiales del español dentro del código. Si una palabra lleva eñe, se reemplaza por "ne" (ej: `cumpleanos`).
- Tildes, eñes y caracteres especiales solo se permiten en textos visibles por el usuario final (mensajes, labels, etc).
- Se permiten anglicismos como convención de código (ej: `getUsuarios()`).

### Qué queda en inglés

Lista cerrada, según [ADR 0002](../adr/0002-codigo-y-contrato-en-espanol.md). Si un nombre no encaja en ninguna fila, se traduce; ante la duda, preguntar al usuario. Qué nombre del Core pasa a cuál está en el [glosario de renombres](../arquitectura/glosario-espanol/README.md); sus casos dudosos ya están resueltos y reflejados en esta tabla.

| Qué | Ejemplos |
|---|---|
| Palabras reservadas de carpeta | `Common` (carpeta de infraestructura transversal de `Code`), `WebSocket` |
| Nombres de módulo del Core | `Kernel`, `Crud`, `Identity`, `ApiKey`, `Test` (y sus `artifactId`: `core-identity`, `core-protocolo-apikey`, `core-test`) |
| Términos reservados | `uuid`, `status`, `AccessToken`, `RefreshToken`, `Auth`, `Trace`, `Context`, `Request`, `Logging`, `hook`, `Response`, `token`, `email`, `smoke test` |
| Sufijos técnicos | `Controller`, `Service`, `Repository`, `DTO`, `UseCase`, `Abstract*`, `Config`, `Filter`, `Properties`, `Aspect`, `Interceptor`, `Utils`, `Mail` |
| Nombres de framework y de terceros | `WebSocket`, `Stomp`, `Jackson`, `Principal`, JWT, SSO, hCaptcha, SM3, Crypter, Malvinas (nombre propio); los verbos HTTP y `json` del DSL de pruebas (`Peticion.get/post/json`); overrides de Spring y de otros terceros (`doFilterInternal()`, `preHandle()`); las palabras clave de los métodos derivados de Spring Data (`findFirstBy`, `existsBy`, `And`, `OrderBy`); los campos JSON que publica una API externa (Crypter, SM3, SSO) |
| Identificadores fijos del deployer | Ver [identificadores-fijos.md](identificadores-fijos.md) |
| Valores persistidos | Nombres de roles (`SYSADMIN`) y de permisos (`configuration.view`), códigos de error publicados, valores de `purpose` de los códigos de seguridad (`FORGOT_PASSWORD`), claims del JWT y valores de `auth.protocolos`, el prefijo `auth_` de las tablas y los nombres de constraints, índices y FKs ya creados |

Ejemplo: `Code/Common/WebSocket/WebSocketAuthInterceptor.java` queda entero en inglés (palabra reservada + framework + término reservado + sufijo técnico); `Code/Common/Configuracion/UrlsDelFrontProperties.java` traduce todo salvo el sufijo.

Lo que el Core le hereda al proyecto no es excepción: los campos de las entidades y DTOs base (`nombreDeUsuario`, `nombre`), los hooks de `AbstractService` (`hookGuardarAplicarDto()`), los métodos de las interfaces del Core que implementa `Code` (`getAsunto()`) y las rutas del ABM (`/paginar`, `/guardar`, `/borrar/{uuid}`) se escriben en español como cualquier otro nombre (ver el [glosario](../arquitectura/glosario-espanol/README.md)).

## Lombok

Lombok está disponible en `app` (lo declara `core-parent`, el parent de `app/pom.xml`). **No es una regla usarlo "siempre que se pueda"**: sus anotaciones no son equivalentes entre sí, y algunas rompen cosas que otras reglas de este repositorio dan por sentadas. Lo que sigue es la lista cerrada: si una anotación de Lombok no aparece acá, el agente debe detenerse y preguntar antes de usarla.

### Obligatorias

| Anotación | Dónde | Por qué |
|---|---|---|
| `@RequiredArgsConstructor` | Toda clase concreta que reciba dependencias | Lo exige [inyeccion-de-dependencias.md](inyeccion-de-dependencias.md): las dependencias son `private final` y entran por constructor |
| `@Slf4j` | Toda clase que loguee | Evita declarar el `Logger` a mano y con el nombre de clase equivocado |

### Permitidas

| Anotación | Dónde |
|---|---|
| `@Getter` / `@Setter` | Entidades y DTOs, a nivel de clase o de campo. En una entidad, los campos que solo cambian por un método de negocio llevan `@Setter(AccessLevel.NONE)` (ver "Comportamiento en la entidad" en [controllers-y-endpoints.md](controllers-y-endpoints.md)) |
| `@NoArgsConstructor` | Entidades (JPA lo exige) y DTOs (Jackson lo exige para deserializar) |
| `@AllArgsConstructor` | DTOs y enums. **Nunca** en un bean de Spring |
| `@Builder` | DTOs. En una entidad hay que acompañarlo con `@NoArgsConstructor` y `@AllArgsConstructor`, o JPA no la puede instanciar |
| `@NonNull` | Parámetros de métodos y de constructores |

### Prohibidas

| Anotación | Dónde | Qué rompe |
|---|---|---|
| `@Data` | En cualquier lado | Es un atajo que arrastra `@ToString` y `@EqualsAndHashCode`, con los problemas de las dos filas siguientes. Se declara `@Getter` y `@Setter` por separado |
| `@ToString` | `@Entity` y DTOs con datos sensibles | En una entidad, el `toString()` generado navega relaciones LAZY: con `spring.jpa.open-in-view=false` ([consultas.md](consultas.md)) eso es un `LazyInitializationException` en cualquier log, o un N+1 silencioso. En un DTO filtra campos sensibles a los logs: `UsuarioBaseDTO`, `CambiarContrasenaDTO` y `UsuarioYContrasenaDTO` tienen `contrasena`, que `SanitizadorDeLogs` justamente se encarga de tapar |
| `@EqualsAndHashCode` | `@Entity` | El `equals`/`hashCode` generado compara todos los campos: fuerza la carga de las relaciones LAZY y da `false` entre dos proxies de Hibernate de la misma fila. La identidad de una entidad es su `uuid`, no su contenido |
| `@AllArgsConstructor` | Beans de Spring (`@Service`, `@Component`, `@RestController`, `@Configuration`) | Vuelve la inyección posicional: agregar o reordenar un campo cambia el constructor en silencio. Para un bean va `@RequiredArgsConstructor` sobre campos `final` |
| `lombok.Value` | En cualquier lado | Colisiona de nombre con `org.springframework.beans.factory.annotation.Value`, que sí se usa y que además `lombok.config` declara como anotación copiable. Un import equivocado es un error difícil de leer. Para un inmutable se usan campos `final` explícitos |
| `@SneakyThrows` | En cualquier lado | Lanza excepciones checked sin declararlas: el llamador no se entera de que tiene que manejarlas. Los errores se manejan con `ExcepcionDeApi` (ver [`docs/arquitectura/errores/README.md`](../arquitectura/errores/README.md)) |

### `lombok.config`

En la raíz del repositorio. Declara `@Lazy`, `@Qualifier` y `@Value` (el de Spring) como anotaciones copiables, para que `@RequiredArgsConstructor` las traslade del campo al parámetro del constructor. Sin eso, Spring no resuelve la dependencia. No se edita sin entender qué depende de cada línea (ver "Dependencias circulares" en [inyeccion-de-dependencias.md](inyeccion-de-dependencias.md)).

## Comentarios

- El estándar de colores de comentarios es la extensión de VSCode `aaron-bond.better-comments`.
- Toda función debe tener un comentario que explique: qué hace, de qué servicios externos depende, qué datos espera, qué datos responde.
- Todo DTO, interfaz y clase debe tener un comentario que explique su función dentro del proyecto.
- Toda entidad debe tener un comentario que explique para qué sirve y con qué otras entidades se relaciona.
- Toda columna/variable cuyo nombre no sea explícito o pueda resultar ambiguo debe tener un comentario que especifique su función o valor.
- Todo campo de un DTO que aparezca en un endpoint (request o response) y todo `@PathVariable`, `@RequestParam` y `@RequestHeader` lleva `@Descripcion` (`core-kernel`): qué significa el dato y sus valores especiales. Para esos campos reemplaza al comentario. No repite si es obligatorio, el tipo ni los límites, que salen de Bean Validation y del tipo Java. La prueba de contrato falla si falta (ver [`docs/arquitectura/testing/README.md`](../arquitectura/testing/README.md)).
