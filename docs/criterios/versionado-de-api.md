# Versionado de API en la URL

## Disparador

Cualquiera de estas dos situaciones:

1. **Arrancar un proyecto nuevo desde el template.** Hay que decidir de qué tipo es (ver "Tipo de proyecto"). Se decide una sola vez, al principio.
2. **Crear o modificar un endpoint cuyo consumidor no es el front propio** desplegado junto con este backend.

Son consumidores externos:

- Una aplicación móvil (queda instalada en el dispositivo: no se actualiza junto con el backend).
- Una integración de un tercero.
- Otro backend, de otro equipo o con su propio ciclo de deploy.
- Un front que vive en otro repositorio y se despliega por separado.
- Una API pública o semi-pública, aunque hoy no tenga ningún consumidor dado de alta.

El criterio real es uno solo: **¿puede existir, en algún momento, un cliente corriendo contra una versión del contrato distinta de la que está desplegada?** Si la respuesta es sí, el endpoint se versiona.

## Tipo de proyecto

Se decide al arrancar el proyecto y no se vuelve a discutir por endpoint. Son dos:

**a) Backend de un front propio.** La superficie es mayormente interna: la consume un front que se despliega junto con este backend, y los consumidores externos son la excepción. Es el caso del template y el que asume **todo el resto de este documento**. Se versiona únicamente la superficie que toca un consumidor externo; el resto no lleva versión.

**b) API.** El consumidor principal es externo y no hay un front acoplado: el producto *es* la API. Acá sí se versiona toda la superficie del proyecto desde el primer commit, y `/v1` significa algo porque todo está efectivamente congelado. Condiciones:

- Todos los controllers del proyecto arrancan en `v1/...`.
- Los endpoints de los módulos `core-protocolo-*` se mueven con su property (`core.protocolo.jwt.ruta`, `core.protocolo.apikey.ruta`), no editando el Core.
- `PermissionController` (`core-identity`) tiene el path fijo `permission` y no se puede mover por configuración. Las salidas son no exponerlo hacia afuera o abrir un issue al Core (ver "Alcance de modificación" en [`docs/rules/controllers-y-endpoints.md`](../rules/controllers-y-endpoints.md)). **No se edita el Core desde el proyecto.**
- Las reglas de "Qué cambios crean una versión nueva" aplican a toda la superficie a la vez: un cambio incompatible en un solo endpoint obliga a una versión nueva de todo. Es el costo de este modelo y hay que asumirlo antes de elegirlo.
- **Consecuencia fuerte: el proyecto casi no usa los endpoints del ABM de la entidad ([receta](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/receta-controller.md)) hacia afuera.** Si toda la superficie es un contrato congelado, ningún endpoint externo puede responder el DTO completo de la entidad (punto 4 de la Norma). En la práctica un proyecto de tipo (b) es casi todo `UseCase` + DTO propio, y los controllers no escriben los endpoints del ABM. Hay que tenerlo en cuenta al estimar: es bastante más trabajo por entidad que un proyecto de tipo (a).

**Por qué no se versiona todo en el caso (a):** una versión que nunca se incrementa no es un contrato, es decoración. Si los endpoints del front propio llevan `/v1` pero se siguen rompiendo en cada release —que es lo correcto para un front acoplado—, el prefijo deja de distinguir lo congelado de lo que no, y el día que un tercero ve `/v1` asume una estabilidad que no existe. Además, los endpoints del ABM de una entidad responden el DTO completo de la entidad, que cambia cada vez que cambia la entidad: versionarlos sería poner un número de contrato sobre algo que por diseño no se puede congelar.

Si no está claro cuál de los dos tipos es el proyecto, **preguntar antes de escribir el primer controller**. Pasar de (a) a (b) después obliga a mover todas las URLs.

## Qué NO dispara esto

- El front propio que se despliega junto con el backend. Un breaking change se coordina en el mismo release y no necesita dos contratos vivos. Es el caso de todos los endpoints actuales del template.
- Los endpoints de infraestructura: `/health`, `/info` y el resto de Actuator (ver [`docs/arquitectura/actuator/README.md`](../arquitectura/actuator/README.md)).
- Un endpoint que solo consumen las pruebas de contrato.
- El header `API-Version`. **No es versionado de contrato**: informa qué build está corriendo (`1.4.3`) y cambia en cada push. Ver [`docs/rules/versionado.md`](../rules/versionado.md). Que una respuesta traiga `API-Version: 1.4.3` no dice nada sobre qué versión del contrato se está usando.

## Norma

1. **La versión va en la URL desde el primer commit del endpoint.** No se agrega "más adelante, cuando haga falta": pasar de `/producto` a `/v1/producto` es en sí mismo un breaking change, y para ese entonces ya hay clientes que no lo pueden absorber.
2. **Nunca se le entrega a un consumidor externo una URL sin versión.** Ni para una prueba, ni "por ahora", ni aunque el endpoint ya exista y haga exactamente lo que el consumidor necesita. Si pide acceso a algo que hoy solo usa el front propio, primero se promueve el endpoint (ver "Promover un endpoint existente a un consumidor externo") y recién después se entrega la URL. Este es el punto de control real del criterio: el riesgo no es olvidarse el `/v1` al crear un endpoint, es regalar por apuro una URL que después no se puede mover.
3. **El prefijo es `/v{n}`, arranca en `/v1` y va después del context path.** El `/api` lo agrega `server.servlet.context-path`; el controller declara `v1/producto` y la URL final queda `/api/v1/producto`.
4. **Un endpoint versionado no usa la receta del ABM de una entidad tal cual.** Los endpoints del ABM (`/uuid/{uuid}`, `/paginar`, `/guardar`, `DELETE /borrar/{uuid}`, ver [`core-crud/docs/receta-controller.md`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-crud/docs/receta-controller.md)) responden con el DTO completo de la entidad: ese DTO cambia cada vez que cambia la entidad, así que el contrato no se puede congelar. Un endpoint versionado es siempre un caso de uso con su DTO propio, declarado como endpoint propio con su path versionado y su `@Contrato` (ver [`docs/rules/controllers-y-endpoints.md`](../rules/controllers-y-endpoints.md)).
5. **Los endpoints versionados y los del front propio conviven.** No se versiona todo el backend por tener un consumidor externo: se versiona la superficie que ese consumidor toca. `/api/producto` (front propio) y `/api/v1/producto` (app móvil) son dos controllers distintos sobre la misma entidad.
6. **Una versión publicada en `main` no se modifica de forma incompatible.** Qué se puede cambiar sobre una versión viva está en "Qué cambios crean una versión nueva".

## Cómo se implementa

### Dónde vive cada archivo

Se respeta [`docs/rules/estructura-de-carpetas.md`](../rules/estructura-de-carpetas.md): todo sigue plano dentro de `Code/{Entidad}/`. La versión se escribe en el nombre de **lo que el contrato congela** (controller y DTOs), nunca en el caso de uso, que es compartido entre versiones:

```
Code/Producto/
├── ProductoController.java                      # front propio, sin version
├── ProductoV1Controller.java                    # @RequestMapping("v1/producto")
├── ProductoDTO.java
├── ProductoDetalleV1ResponseDTO.java            # contrato congelado de v1
└── CasosDeUso/
    └── ProductoDetalleUseCase.java              # SIN version: lo comparten v1 y v2
```

### La escalera: routing, adaptador, implementación separada

Aplicar en este orden. La primera que alcance es la que se usa.

**1. Routing (caso por defecto).** Dos controllers, cada uno con sus DTOs, los dos llaman al mismo `UseCase`. Se usa cuando `v1` y `v2` hacen exactamente lo mismo y solo cambia la forma del contrato: nombres de campos, tipos, campos que se agregan o se sacan de la respuesta, formato de las fechas.

```java
@RestController
@RequestMapping("v1/producto")
@RequiredArgsConstructor
public class ProductoV1Controller {

    private final ProductoDetalleUseCase detalleUseCase;

    // Detalle de un producto en el contrato v1
    @GetMapping("/{uuid}")
    public ResponseEntity<ProductoDetalleV1ResponseDTO> detalle(@PathVariable("uuid") String uuid) {
        return new ResponseEntity<>(detalleUseCase.ejecutarV1(uuid), HttpStatus.OK);
    }
}
```

El `UseCase` expone un método por versión (`ejecutarV1`, `ejecutarV2`), cada uno con su proyección al DTO de esa versión (ver [`docs/rules/consultas.md`](../rules/consultas.md): la lectura arma el DTO en la consulta, con la Proyección). La lógica de negocio compartida queda en un método privado del mismo `UseCase`.

**2. Adaptador.** El `UseCase` tiene un único método, el de la versión nueva, y la versión vieja traduce su salida. Se usa cuando la traducción tiene lógica propia: `v1` necesita campos que `v2` ya no produce, valores por defecto para lo que antes era obligatorio, o recombinar datos que `v2` devuelve con otra forma.

```java
// Adaptador: v2 dejo de devolver "stockTotal"; v1 lo sigue exponiendo como la suma de los depositos.
// Dependencias externas: ninguna. Depende de ProductoDetalleUseCase (contrato v2).
// Recibe: uuid del producto. Responde: ProductoDetalleV1ResponseDTO.
@Service
@RequiredArgsConstructor
public class ProductoDetalleV1Adapter {

    private final ProductoDetalleUseCase detalleUseCase;

    public ProductoDetalleV1ResponseDTO ejecutar(String uuid) {
        ProductoDetalleV2ResponseDTO v2 = detalleUseCase.ejecutar(uuid);
        ProductoDetalleV1ResponseDTO dto = new ProductoDetalleV1ResponseDTO();
        dto.setUuid(v2.getUuid());
        dto.setStockTotal(v2.getDepositos().stream().mapToInt(DepositoDTO::getStock).sum());
        return dto;
    }
}
```

El adaptador es una clase aparte, en `Code/{Entidad}/CasosDeUso/`, con el comentario obligatorio de [`docs/rules/idioma-y-estilo.md`](../rules/idioma-y-estilo.md). No se mete la traducción dentro del controller.

**3. Implementación separada.** Dos `UseCase` distintos, uno por versión. Es el escalón caro: duplica la lógica y obliga a arreglar cada bug dos veces. Solo aplica cuando **el comportamiento cambió**, no solo la respuesta: `v1` tiene que seguir calculando, validando o guardando como antes. **Requiere pedido explícito del usuario**; el agente no lo elige por su cuenta.

### Cómo elegir

| Pregunta | Respuesta | Escalón |
|---|---|---|
| ¿`v1` y `v2` hacen lo mismo y solo cambia la forma del contrato? | Sí | Routing |
| ¿`v1` necesita datos que `v2` ya no produce, o armados de otra forma? | Sí | Adaptador |
| ¿`v1` tiene que **comportarse** distinto de `v2` (cálculo, validación, escritura)? | Sí | Implementación separada (preguntar) |

### Promover un endpoint existente a un consumidor externo

Aparece un consumidor externo para algo que hoy usa el front propio. **No es una migración**: el endpoint del front no se toca, no se renombra y no se mueve. Se agrega una superficie nueva al lado.

1. Crear `{Entidad}V1Controller` en `Code/{Entidad}/`, con `@RequestMapping("v1/{entidad}")` y solo los endpoints que el consumidor externo necesita.
2. Crear el DTO congelado del caso de uso (`{Entidad}{CasoDeUso}V1ResponseDTO`). **No se reutiliza el DTO del front propio**: ese sigue evolucionando con la entidad y el congelado no.
3. Apuntar al `UseCase` que ya existe (escalón 1, routing), agregándole el método `ejecutarV1` con la proyección al DTO nuevo. La lógica no se duplica.
4. Crear las pruebas de contrato de ese endpoint (`{Entidad}V1{CasoDeUso}ContratoTest`), separadas de las del endpoint del front (ver [`docs/arquitectura/testing/README.md`](../arquitectura/testing/README.md)).
5. Recién ahí se le entrega la URL al consumidor.

Desde ese momento los dos endpoints evolucionan por separado: el del front propio se sigue rompiendo libremente en cada release, el `/v1` queda congelado bajo las reglas de la sección siguiente. **Esa divergencia es el objetivo, no un efecto colateral.**

## Qué cambios crean una versión nueva

Hacen falta **las dos condiciones** a la vez:

1. El cambio es incompatible (está en la lista de abajo), y
2. la versión actual ya está mergeada a `main`.

Si falta cualquiera de las dos, **se modifica la versión actual en el lugar**: mientras el cambio no llegó a `main` no hay ningún cliente que pueda depender de él, y crear un `/v2` que nadie consumió es deuda pura.

| ¿Proyecto en producción? | ¿Versión actual en `main`? | Ante un cambio incompatible |
|---|---|---|
| No | No | Modificar la versión actual |
| No | Sí | Modificar la versión actual, avisando a los consumidores conocidos que estén integrando |
| Sí | No | Modificar la versión actual (el cambio todavía no salió) |
| Sí | Sí | **Crear la versión siguiente** |

Un proyecto que todavía se está armando no crea versiones nuevas: `/v1` se modifica libremente hasta el primer release a producción. Lo que congela el contrato es tener consumidores reales; el merge a `main` es el momento en que eso se vuelve posible.

### Sí crean versión nueva

- Sacar o renombrar un campo de la respuesta.
- Sacar un endpoint, o cambiar su path o su método HTTP.
- Cambiar el tipo de un campo (`string` a `number`, escalar a objeto, objeto a lista).
- Hacer obligatorio un campo del request que era opcional.
- Agregar una validación nueva: un request que antes pasaba ahora responde `400`.
- Cambiar el significado de un valor ya existente (por ejemplo, qué representa un `status`).
- Sacar un valor de un enum que la respuesta puede devolver.
- Cambiar el código HTTP de una respuesta exitosa.
- Cambiar la estructura del cuerpo de error (ver [`docs/arquitectura/errores/README.md`](../arquitectura/errores/README.md)).
- Cambiar el formato o la zona horaria de una fecha (ver [`docs/rules/entidades-y-dtos.md`](../rules/entidades-y-dtos.md)).
- Cambiar la forma de la respuesta paginada o el tamaño de página por defecto.

### No crean versión nueva

- Agregar un campo a la respuesta.
- Agregar un campo **opcional** al request.
- Agregar un endpoint nuevo.
- Cambiar el texto de un mensaje de error, manteniendo el código y la estructura.
- Cualquier cambio interno: refactor, queries, índices, changesets de Liquibase que no cambien la respuesta.
- Cambios de performance.

### Casos grises: preguntar antes de decidir

- **Agregar un valor a un enum de la respuesta.** Es compatible en el papel, pero rompe a cualquier cliente que haga un switch exhaustivo.
- **Arreglar un bug del que ya depende un consumidor**: el contrato documentado decía una cosa y la API hacía otra.
- **Hacer más laxa una validación.** No rompe a nadie técnicamente, pero cambia el comportamiento observable.

## Qué recomendarle al programador

El agente plantea esto aunque la decisión no sea suya:

- **Si el endpoint todavía no tiene un consumidor externo pero es plausible que lo tenga**, decirlo y dejar que el usuario decida. Versionar desde el día uno cuesta un prefijo en la URL; agregarlo después cuesta una migración coordinada con cada cliente.
- **Cada versión viva tiene costo fijo**: sus pruebas de contrato, su documentación y su mantenimiento (ver [`docs/rules/testing.md`](../rules/testing.md) y [`docs/rules/documentacion.md`](../rules/documentacion.md)). Al crear una versión nueva, proponer en el mismo momento una fecha de baja para la anterior. Una versión sin fecha de baja no se da de baja nunca.
- **Más de dos versiones vivas es señal de que no se está dando de baja nada.** Vale la pena decirlo en vez de seguir sumando.
- **`API-Version` no reemplaza al `/v1`.** Si alguien propone usar el header como versionado de contrato, aclarar que ese header cambia en cada push y no identifica un contrato.
- **Al crear `/v2` hay que crear también sus pruebas de contrato** y mantener las de `/v1` mientras la versión siga viva, no reemplazarlas.
- **Si alguien propone versionar todos los endpoints "por las dudas"** en un proyecto de tipo (a), explicar el costo: un `/v1` que nunca se incrementa no evita ninguna migración —los endpoints versionados y los del front conviven, no se migra nada— y a cambio vuelve indistinguible lo congelado de lo que no. La respuesta al miedo de fondo es la regla 2 de la Norma más la receta de promoción, no el prefijo global.

## Cuándo preguntar

El agente se detiene y consulta al usuario si:

- No está claro si el proyecto es de tipo (a) o (b). Se pregunta **antes de escribir el primer controller**.
- No está claro si el consumidor del endpoint es externo o el front propio.
- El cambio cae en la lista de casos grises.
- Haría falta llegar al escalón 3 (implementación separada).
- Hay que dar de baja una versión, o decidir su fecha de baja.
- Ya hay dos versiones vivas y el cambio pediría una tercera.
- El endpoint versionado necesitaría exponer uno de los endpoints del ABM de la entidad (receta de `core-crud`) con el DTO completo.
