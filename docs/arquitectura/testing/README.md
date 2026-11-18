# Pruebas de contrato y Contrato de endpoint

Una **Prueba de contrato** ejecuta un endpoint real (MockMvc, in-process, sin Docker) sobre el PostgreSQL 18
embebido con master + seeds de Liquibase, con rollback al terminar cada test. Además de assertear la respuesta,
la valida contra el schema derivado de la clase DTO y registra el request y la respuesta reales.

El **Contrato de endpoint** es el OpenAPI 3 que se ensambla con lo que registraron las pruebas: path, verbo,
parámetros, headers, DTO que espera, DTO que responde, códigos de error y ejemplos reales. No se escribe a mano:
si la prueba falla, el contrato no se publica.

El soporte vive en el módulo `core-test` (scope test). Las anotaciones que alimentan el contrato viven en
`core-kernel`, porque las usa el código de producción.

## Anotaciones (`core-kernel`, paquete `com.blue.open.Core.Kernel.Contrato`)

### `@Contrato` — sobre todo método de controller

```java
// Actualiza el stock de un producto existente
@PostMapping("/{uuid}/actualizar-stock")
@Contrato(casoDeUso = "CU-PRO-05", resumen = "Actualiza el stock de un producto existente",
        permiso = "PRODUCTO_ADMINISTRAR")
public ResponseEntity<ProductoActualizarStockResponseDTO> actualizarStock(...)
```

| Atributo | Qué va | Ejemplo |
|---|---|---|
| `casoDeUso` | Identificador del caso de uso que resuelve la operación. Agrupa las operaciones en el contrato. | `CU-DEP-03` |
| `resumen` | Una línea, en lenguaje del usuario final, de lo que hace. Es el título de la operación en el contrato. | `Guarda una dependencia (alta o edición)` |
| `permiso` | Quién puede ejecutarla (`String[]`, obligatorio, sin default): `Contrato.PUBLICO` (sin sesión), `Contrato.AUTENTICADO` (con sesión, sin rol ni permiso puntual), o nombres de roles/permisos tal como se llama la constante del enum. Alcanza con tener uno. | `DEPENDENCIA_ADMINISTRAR`, `{ "PRODUCTO_ADMINISTRAR", "SYSADMIN" }`, `Contrato.PUBLICO` |

`permiso` **autoriza**: lo hace cumplir `InterceptorDeContrato` (`core-kernel`) antes de ejecutar el método (y antes
de `@Valid`: un request sin permiso responde 403 aunque el body sea inválido), a través del puerto
`AutorizadorDeEndpoint` (con `core-identity`, `AutorizadorPorRolesYPermisos`). El permiso fijo de un endpoint no se
repite en el cuerpo del método; lo que depende de los datos sigue en el código, con `tienePermisos` (ver
[`autenticacion.md`](../../servicios/autenticacion.md)). `core-test` usa el mismo `permiso` para generar solos los
casos "sin autenticación" y "sin permiso".

`VerificadorDeContratos` impide arrancar la aplicación si un endpoint de `com.blue.open` no tiene `@Contrato`, tiene
`permiso` vacío, combina `PUBLICO` o `AUTENTICADO` con otros valores, o nombra un rol o permiso que no existe.

Los 4 endpoints del ABM de una entidad (receta de [`sm-core-maven/core-crud/docs/receta-controller.md`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/receta-controller.md))
llevan `@Contrato` como cualquier otro.

### `@Descripcion` — sobre cada campo de DTO y cada parámetro

```java
@Descripcion("Código corto operativo. Único entre todas las dependencias, incluidas las eliminadas")
@Size(max = 8, message = "El código no puede superar los 8 caracteres")
private String codigo;
```

```java
public ResponseEntity<ActaDTO> obtener(
        @Descripcion("Uuid del acta") @PathVariable("uuid") String uuid)
```

- Va en **todos** los campos de un DTO que aparezca en algún contrato (request o response), y en cada
  `@PathVariable`, `@RequestParam` y `@RequestHeader`. Una prueba de contrato falla listando clase y campo si
  falta alguna.
- Dice qué significa el dato y sus valores especiales (`null = raíz`, `solo lectura`, etc). **No** repite si es
  obligatorio, el tipo ni los límites: eso sale solo de Bean Validation (`@NotNull`, `@Size`, ...) y del tipo
  Java.
- Opcional sobre la clase del DTO (descripción del objeto).
- Sobre el getter de un campo gana sobre la del campo (desde Core 3.5.0). Así un DTO hijo redefine la descripción de
  un campo heredado (por ejemplo `status` de `EntidadBaseDTO`) sin tocar la clase base:

```java
@Override
@Descripcion("Estado del inspector: 0 vigente, -1 dado de baja")
public int getStatus() {
    return super.getStatus();
}
```

  La verificación de completitud mira el campo y su getter: con una de las dos alcanza.
- Obligatoria sobre cada constante de un enum `CodigoDeError`, en vez del comentario `//`: dice cuándo recibe el
  front ese código y es la columna "Cuándo ocurre" del catálogo de errores del contrato (ver
  [`errores.md`](../../rules/errores.md), regla 7). `CodigosDeErrorTest` (`app`) falla nombrando el enum y la
  constante que no la tenga.

## Módulo `core-test`

Se declara en `app/pom.xml` con scope test (nada llega al jar):

```xml
<dependency>
    <groupId>com.blue.open</groupId>
    <artifactId>core-test</artifactId>
    <scope>test</scope>
</dependency>
```

Trae PostgreSQL embebido (`embedded-postgres`, binarios de la versión fijada en `core-parent`), MockMvc y
`spring-boot-starter-test`: el proyecto no los declara aparte.

| Clase | Para qué |
|---|---|
| `TestConBaseDeDatos` | Base de todo test con base de datos. PostgreSQL 18 embebido levantado una vez por JVM en UTC-3, profile `test`, master + seeds de Liquibase, `@Transactional` (rollback por test). Un único contexto de Spring por JVM. |
| `PruebaDeContrato` | Base de las pruebas de contrato. Extiende `TestConBaseDeDatos` y expone el cliente fluido. |
| `ActoresDePrueba` | Puerto que implementa el proyecto: rol → token Bearer. |
| `Dobles.*` | Dobles en memoria de los servicios externos, activos solo con el profile `test`. |

### Cómo se escribe una prueba de contrato

Una clase por caso de uso, `{Entidad}{CasoDeUso}ContratoTest`, en `app/src/test/.../Code/{Entidad}/`. Adentro, el
caso feliz y cada caso de error:

```java
class DependenciaEliminarContratoTest extends PruebaDeContrato {

    @Test
    void eliminaUnaDependencia() {
        como("ADMINISTRADOR_SISTEMA").delete("/dependencias/borrar/{uuid}", UUID_SEED)
                .ejecutar()
                .esperar(200);
    }

    @Test
    void conHijasVigentes() {
        como("SYSADMIN").delete("/dependencias/borrar/{uuid}", UUID_CON_HIJAS)
                .ejecutar()
                .esperarError(422, "PRECONDICION_VIOLADA");
    }
}
```

- `como(rol)` ejecuta con el token del actor de ese rol; `sinAutenticacion()` sin header `Authorization`.
- Operación: `get/post/put/patch/delete(ruta, variables...)`. La ruta va sin el context-path (`/dependencias/...`,
  no `/api/dependencias/...`).
- Request: `.json(objeto o String)`, `.parametro(nombre, valor)`, `.encabezado(nombre, valor)`, y para multipart
  `.archivo(parte, nombreArchivo, contentType, bytes)` y `.parteJson(parte, objeto)`.
- `.ejecutar()` corre el request con MockMvc **dentro de la transacción del test**: lo que crea se revierte al
  terminar. Responde una `Respuesta` con `status()`, `json()`, `como(Clase)`, `texto()`, `esperar(status)` y
  `esperarError(status, codigo)`.
- Todo caso de error assertea `codigo` con `esperarError` (regla de [`errores.md`](../../rules/errores.md)).
- Un dato que el caso necesita y que no está en los seeds se crea en el mismo test, por los endpoints reales
  (builders en `app/src/test/.../Code/{Entidad}/`), nunca con SQL a mano.
- Un builder que usan pruebas de otros módulos (por ejemplo el que labra un acta) es una clase pública que recibe el
  cliente de la prueba (`new EscenarioDeActa(this::como)`) y marca cada paso con `.preparacion()`. Así un escenario
  largo (acta → documento firmado → notificación → fallo) se arma componiendo los builders de cada módulo.
- Todo lo que hace el request corre en la transacción del test. Un caso de uso que abre una transacción propia
  (`REQUIRES_NEW`) no ve los datos del escenario ni se revierte: el proyecto lo reemplaza en el profile `test` por un
  doble que use la transacción en curso.

### Cómo se declara un actor

El proyecto implementa `ActoresDePrueba` como bean (`@Component` en `app/src/test`), con un usuario por rol creado
por los seeds de Liquibase y su token de acceso (protocolo `tokens`, que debe estar en `auth.protocolos` del
profile test). En este proyecto: `Code/Autorizacion/ActoresDelProyecto`.

- `token(rol)`: token del actor del rol.
- `rolSinPermiso(List<String> permisos)`: un rol que no tiene ninguno de esos roles o permisos, para el caso
  automático "sin permiso".

### Cómo se usa un doble

Con el profile `test`, `core-test` registra como `@Primary`:

| Doble | Reemplaza a | Qué permite assertear |
|---|---|---|
| `CrypterEnMemoria` | `CrypterService` (`core-crypter`) | `archivos()`, `archivosEnCarpeta(carpeta)`: cuántos archivos se subieron y a qué carpeta. `getArchivo` devuelve el base64 guardado. |
| `EnviadorDeMailEnMemoria` | `EnviadorDeMail` (`core-mail`) | `mails().enviados()`: destinatario, asunto, cuerpo y adjuntos. |

Se vacían antes de cada test. El de Crypter se inyecta con `@Autowired CrypterEnMemoria crypter;` en la prueba que
lo use (no está en la base porque `core-crypter` es opcional). Sin WireMock y sin red.

### Properties del profile `test`

`application-test.yml` del proyecto debe declarar Liquibase (master + seeds), el driver y el dialecto de
PostgreSQL con `ddl-auto=validate` y `open-in-view=false`, `auth.protocolos` con `tokens`, y las properties que la
aplicación exige para arrancar. La url, el usuario y la clave del datasource los registra `TestConBaseDeDatos`.

## Cómo se genera el contrato

Cada `ejecutar()` de una prueba de contrato, además de responder:

1. Identifica la operación que atendió el request (método del controller y patrón del path).
2. Toma sus metadatos de `@Contrato`. Sin `@Contrato`, la prueba falla.
3. Genera el JSON Schema de los DTOs de la operación desde las clases (`victools/jsonschema-generator` con los
   módulos de Jackson y Bean Validation): propiedades según Jackson, obligatorios y límites según Bean Validation,
   `description` desde `@Descripcion` (la del getter del campo, si tiene, y si no la del campo), fechas con el `pattern` ISO GMT-3. Un campo sin `@Descripcion` (ni en el campo
   ni en su getter) en una clase de `com.blue.open` hace fallar la prueba listando clase y campo.
4. Valida la respuesta real contra el schema de su status (`networknt/json-schema-validator`): el DTO del método
   para los 2xx, `ErrorResponseDTO` para los errores. Un campo que el DTO no declara, un tipo distinto o una fecha
   fuera de GMT-3 hacen fallar la prueba con el detalle del campo.
5. Registra el ejemplo real (request y respuesta) y, si es un error, su `codigo` (y `datos.motivo`).
6. Escribe el contrato parcial en `app/target/contratos/operaciones/{Controller.metodo}__{ClaseDeTest}.json`.

Un paso de preparación (crear el dato que el caso necesita) se marca con `.preparacion()`: se valida igual, pero no
se publica como ejemplo ni genera casos de seguridad. Si su operación todavía no tiene contrato (un endpoint de otro
módulo sin migrar), no se registra.

Dos métodos del controller con el mismo path y verbo que se distinguen por el content type del body (la variante
JSON y la multipart de un alta) se publican como una sola operación de OpenAPI: un `content` por content type, con
los ejemplos de cada variante, y la lista de variantes en la descripción. El `operationId` y el resumen son los del
primer método en orden alfabético.

Los ejemplos se normalizan para que dos corridas sobre el mismo código den el mismo `openapi.yaml`: uuids generados
en la corrida → uuids de ejemplo numerados (los de los seeds, con un grupo `0000`, se conservan), fechas del día →
`2026-01-01...`, `traceId`/`fechaHora` fijos, arrays recortados a 3 elementos y propiedades ordenadas. Un uuid generado que viaja dentro de otro texto (por ejemplo un QR firmado
que lo incluye) no se normaliza: la prueba que publica ese ejemplo usa un uuid fijo.

### Casos de seguridad automáticos

La primera vez que una operación que exige sesión (todo `permiso` salvo `PUBLICO`) responde 2xx,
`PruebaDeContrato` repite el mismo request:

- **sin token** → tiene que responder 401 `NO_AUTENTICADO` (desde Core 3.2.0; antes aceptaba 401 o 403);
- **como un actor sin ninguno de los roles o permisos declarados** (`ActoresDePrueba.rolSinPermiso`) → 403
  `SIN_PERMISO`. Solo si exige rol o permiso: una operación `AUTENTICADO` no tiene permiso que faltar.

Si el endpoint no exige lo declarado, la prueba falla. Ambos quedan como ejemplos de la operación. En el contrato,
una operación `AUTENTICADO` se publica como "Requiere sesión" (con security Bearer), distinta de "Pública". No se
escriben pruebas "sin autenticación" ni "sin permiso" a mano.

### ABM de una entidad

Una clase por entidad que extiende `ContratoDeAbmTest` (`{Entidad}ContratoTest`), para el controller escrito con la
receta de [`receta-controller.md`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/receta-controller.md):

```java
class DependenciaContratoTest extends ContratoDeAbmTest {
    protected Class<?> controlador() { return DependenciaController.class; }
    protected String rol() { return "ADMINISTRADOR_SISTEMA"; }
    protected String uuidVigente() { return "d0000000-0000-4000-8000-000000000001"; }
    protected Object cuerpoValido() { return Map.of("nombre", "Departamento X", "tipoActa", "COMERCIO"); }
    protected Object cuerpoInvalido() { return Map.of("tipoActa", "TRANSITO"); }
}
```

Los metadatos (caso de uso, resumen y permiso) salen del `@Contrato` de cada método del controller. Una operación que
el controller no publica (no hay método con ese verbo y path) se saltea. Cubre: obtener por uuid, uuid inexistente,
primera página, alta, body inválido (`FORMULARIO_INVALIDO`), eliminar, eliminar inexistente y doble borrado
(`REGISTRO_NO_ENCONTRADO`, con el status que declara `CodigoDeErrorCrud`). Las reglas de negocio de cada caso de uso
van en sus propias clases.

### Completitud

`VerificacionDeContratos`, que el proyecto extiende con `@Order(Integer.MAX_VALUE)` (en este proyecto
`ContratosCompletosTest`) y que corre última gracias a `junit-platform.properties`
(`junit.jupiter.testclass.order.default=org.junit.jupiter.api.ClassOrderer$OrderAnnotation`), recorre
`RequestMappingHandlerMapping` (sin los endpoints de Spring: Actuator, `/error`) y falla listando las operaciones
que ninguna prueba ejecutó. Solo corre en la corrida completa (sin `-Dtest`).

Un endpoint que no se quiere probar no existe: si no tiene sentido para la entidad, no se escribe en el controller.

## Publicación

| Dónde | Qué | Cuándo |
|---|---|---|
| `app/target/contratos/openapi.yaml` + `index.html` | Contrato ensamblado | Al final de toda corrida de tests (también con `-Dtest`: junta lo que haya en `operaciones/`) |
| `docs/api/openapi.yaml` + `index.html` (commiteado) | Lo que lee el front y se revisa en cada PR | Solo en la corrida completa y en verde. Nunca falla por "estaba viejo" |
| `BOOT-INF/classes/docs-api/` del jar → `/api/docs` | Lo que corre cada ambiente | `prepare-package` copia `docs/api` (nunca una corrida parcial o fallida) |

- El ensamblado lo hace `ListenerDeContratos` (un `LauncherSessionListener` de JUnit registrado por `core-test`):
  junta los parciales de una misma operación, mueve los schemas a `components/schemas`, agrega el esquema Bearer, los
  headers globales y el catálogo de `CodigoDeError` (por reflexión: código, HTTP, mensaje por defecto y cuándo
  ocurre, que sale del `@Descripcion` de la constante). `info.version` es la versión del proyecto.
- `index.html` es Redoc 2.5.4 empaquetado en `core-test` con el contrato embebido: se abre como archivo, sin
  servidor y sin CDN.
- `/api/docs` (redirige a `/api/docs/index.html`) se sirve sin autenticación si `contratos.publicar=true`
  (default). En producción va en `false` (`CONTRATOS_PUBLICAR=false`) y `/api/docs` no existe.
- `-Dcontratos.verificar=true`: en vez de copiar a `docs/api`, falla si el commiteado difiere del generado (para un
  CI futuro). Compara sin los fines de línea (git en Windows hace checkout con CRLF). Como `info.version` es la
  versión del proyecto, después de subir la versión hay que regenerar `docs/api` con una corrida completa.

La configuración del proyecto:

- `app/pom.xml`, en surefire: `contratos.directorio` (`target/contratos`), `contratos.destino` (`../docs/api`) y
  `contratos.version` (`${project.version}`); y la ejecución `copy-resources` de `maven-resources-plugin` en
  `prepare-package` que copia `docs/api` al jar.
- `app/src/test/resources/contratos.properties`: `contratos.titulo`, `contratos.descripcion`, `contratos.servidor`
  (`/api`) y un `contratos.encabezado.{Nombre}=descripción` por header global que el proyecto acepte.

## Comandos

```bash
mvn -pl app test                                  # todo: unitarios + contrato + completitud; publica docs/api si está en verde
mvn -pl app test -Dtest='Dependencia*'            # un módulo: no limpia, no exige completitud, no toca docs/api
mvn -pl app test -Dcontratos.verificar=true       # CI: falla si docs/api no es el contrato generado
```

Se corren desde la raíz del repositorio. La primera vez Maven descarga el Core de `https://sm-soluciones.github.io/sm-core-maven`: no hace falta instalar nada antes.
