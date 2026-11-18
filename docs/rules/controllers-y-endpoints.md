# Controllers, Services y Endpoints

## Estructura basada en el Core

- Los services de una entidad extienden de `AbstractService` **cuando el CRUD genérico le sirve a esa entidad**. Si no le sirve, la entidad puede no usar `core-crud` y exponer solo endpoints propios: el Core no obliga a heredar.
- Los controllers no heredan de ninguna clase del Core: se escriben explícitos. El ABM de una entidad se escribe con la receta de `sm-core-maven/core-crud/docs/receta-controller.md` (desde Core 3.0.0 no existe `AbstractController`).
- Los repository heredan de `EntidadRepositoryBorradoLogico` (entidades con borrado lógico) o de `EntidadRepository` (borrado físico). No hace falta un `RepositoryCustom`/`RepositoryImpl` por entidad: `EntidadRepositoryBase` ya provee `paginar()`.
- No hay una clase de conversión por entidad: la lectura es Proyección y la escritura es aplicar el DTO sobre la entidad, las dos con el `Proyector` de `core-kernel` (ver [consultas.md](consultas.md) y `sm-core-maven/core-kernel/docs/proyeccion.md`).
- Los imports siempre se hacen en la parte superior del archivo. No se debe hacer referencia inline (por ejemplo, está mal hacer `this.repository = (com.blue.open.Core.Kernel.Repository.EntidadRepository<HistorialEstadoTrabajo>)`).
- Las dependencias entran por constructor (`private final` + `@RequiredArgsConstructor`). Está prohibido `@Autowired` sobre un campo. Ver [inyeccion-de-dependencias.md](inyeccion-de-dependencias.md).

`Company` y `User` tienen herencias específicas de los módulos `core-multiempresa` y `core-identity`, pero siguen siendo entidades del proyecto como cualquier otra: el Core no las conoce por nombre.

El módulo `core-crud` provee la lógica del ABM de una entidad en `AbstractService` (obtener por uuid, paginar, guardar, borrar). Los 4 endpoints (`GET /uuid/{uuid}`, `POST /paginar`, `POST /guardar`, `DELETE /borrar/{uuid}`) se escriben en el controller con la receta, que delega en el service. Documentación completa en `sm-core-maven/core-crud/docs/README.md` (ver [documentacion.md](documentacion.md)).

### Qué endpoints del ABM publica cada entidad

Solo los que la entidad necesita: un endpoint que no se escribe no existe (responde 404). No hay que declarar nada más.

### Autorización: `@Contrato`

Todo método de controller lleva `@Contrato`, y su `permiso` es **la única declaración** de quién puede usar el endpoint. `InterceptorDeContrato` lo hace cumplir antes de ejecutar el método (y antes de `@Valid`: sin sesión responde 401 `NO_AUTENTICADO` y sin permiso 403 `SIN_PERMISO`, nunca 400). La aplicación no arranca si a un endpoint le falta `@Contrato`, si el `permiso` está vacío o si nombra un rol o permiso que no existe.

| `permiso` | Qué exige |
|---|---|
| `Contrato.PUBLICO` | Nada: endpoint público |
| `Contrato.AUTENTICADO` | Sesión, sin rol ni permiso puntual. No valida la empresa del header |
| `"SYSADMIN"`, `{ "USUARIO_VER", "SYSADMIN" }` | Sesión, empresa del header válida y alguno de los roles o permisos. El nombre es el de la constante del enum |

- **Prohibido** autorizar un endpoint con `authService.valid(...)` (ya no existe) o con un `if` sobre un permiso fijo en el cuerpo del método: el permiso fijo va en `@Contrato` y en ningún otro lado.
- La autorización que **depende de los datos** del request o del registro (alta vs. edición en el mismo `/guardar`, alcance por empresa, dueño del registro, una configuración privada) no se puede declarar en `@Contrato`. Va en el `UseCase` o en el `hookXxxAuth` del `AbstractService`, con `tienePermisos(...)`, y si no se cumple se lanza `ExcepcionDeApi` con `SIN_PERMISO`:

```java
// El permiso depende del registro: no se puede declarar en @Contrato
if (!configuracion.isGlobal() && !authService.tienePermisos(List.of(PermisoEnum.CONFIGURACION_VER)))
    throw new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO);
```

### Trazabilidad: `@Trazar`

`@Trazar` registra en el log el inicio, el fin, los argumentos, el resultado y la duración del endpoint, y además abre un span por cada método de `@Service` que se ejecute adentro (ver [`docs/arquitectura/logs/README.md`](../arquitectura/logs/README.md)). Genera muchas líneas de log por request y le agrega carga al servidor, así que **no se pone en todos los endpoints: solo en los críticos**.

Un endpoint es crítico cuando un fallo puede suponer una pérdida para el negocio y hay una razón real para reconstruir después qué pasó en cada paso. Por ejemplo:

- Endpoints que trabajan con dinero (saldos, cobros, transferencias).
- Endpoints que registran pagos.
- Endpoints que generan facturas o comprobantes.

No son críticos, y por lo tanto **no llevan `@Trazar`**:

- Los endpoints del ABM (`uuid`, `paginar`, `guardar`, `borrar`), salvo que la entidad sea en sí misma una de las de la lista de arriba.
- Las lecturas y los listados.
- Login, refresh del token, cambio y recuperación de contraseña, verificación de cuenta y tokens de acceso.
- La configuración del sistema y los ABM de usuarios, empresas y roles.

Si no está claro si un endpoint es crítico, el agente se detiene y le pregunta al usuario antes de anotarlo. Al usar `@Trazar`, se declaran en `excluir` los campos sensibles del DTO que no cubre la lista fija de `SanitizadorDeLogs`, y `registrarResultado = false` si la respuesta no aporta a la trazabilidad.

Dónde vive físicamente cada uno de estos archivos (Controller, Service, Repository, Entity, DTO, UseCase) está definido en [estructura-de-carpetas.md](estructura-de-carpetas.md). Este documento asume esa organización por módulo/entidad, no por tipo de archivo.

Todo lo que sigue asume que el consumidor del endpoint es el front propio, que se despliega junto con este backend. Si el consumidor es otro (app móvil, integración de un tercero, otro backend, API pública), el endpoint además lleva versión en la URL y no puede usar la receta del ABM: ver `docs/criterios/versionado-de-api.md`.

## ABM vs. DTO por caso de uso

Los endpoints se derivan de los casos de uso listados para la funcionalidad (ver "Orden de trabajo" en [entidades-y-dtos.md](entidades-y-dtos.md)).

- Si la entidad es chica (todos sus casos de uso son un ABM que usa todos sus campos), sus endpoints son, por defecto, los del ABM escritos con la receta (uuid, paginar, guardar, borrar). Si solo algunos casos de uso son ABM, se escriben solo esos endpoints o ninguno.
- Si el caso de uso (lectura o escritura) necesita menos datos que la entidad completa, no usar `GET /uuid/{uuid}` ni `POST /guardar` con el DTO completo: se debe crear un DTO específico para ese caso de uso (convención de nombres en [entidades-y-dtos.md](entidades-y-dtos.md)) y un endpoint propio en el controller concreto:
  - `GET /api/{controller}/{uuid}/{caso-de-uso}` para lectura específica.
  - `POST /api/{controller}/{uuid}/{caso-de-uso}` para actualizar un subconjunto de campos de un registro existente. El uuid en el path implica que el registro ya existe: siempre es UPDATE, y el DTO reemplaza por completo los campos que trae. Debe validar que el registro no esté eliminado (`status = -1`), igual que `POST /guardar`.
  - `POST /api/{controller}/{caso-de-uso}` para altas con menos datos que los que pide `POST /guardar` (sin uuid).
  - Estos endpoints no se agregan a `AbstractService`. El endpoint se agrega en el `Controller` de la entidad, que inyecta una clase `UseCase` propia del caso de uso en vez de agregar el método al `Service` de la entidad.
  - Cada caso de uso se implementa en una clase `{Entidad}{CasoDeUso}UseCase`, ubicada en `Code/{Entidad}/CasosDeUso/` (ver [estructura-de-carpetas.md](estructura-de-carpetas.md)). No extiende `AbstractService`: es una clase `@Service` independiente con un único método público (`ejecutar`) que resuelve ese caso de uso específico, inyectando directamente lo que necesite (`EntidadRepository`, `AuthService`, el `Proyector`, etc).
  - El `Service` de la entidad (el que extiende `AbstractService`) nunca crece con métodos de casos de uso: queda limitado a los hooks de los 4 endpoints del ABM.
  - Las lecturas del caso de uso arman el DTO directamente en la consulta (Proyección): con el `Proyector` inyectado o con una consulta propia (`@Query` con `SELECT new`) en el repositorio (ver [consultas.md](consultas.md)). La respuesta de una escritura se arma igual, re-proyectando el registro guardado; no se convierte la entidad a mano.
  - La clase `UseCase` debe tener el comentario obligatorio de [idioma-y-estilo.md](idioma-y-estilo.md): qué hace, de qué depende, qué recibe, qué responde.
  - Si dos casos de uso necesitan exactamente los mismos datos, se debe reutilizar el mismo DTO en vez de duplicarlo.
- Solo se puede crear un endpoint fuera de este esquema (ABM + DTO por caso de uso) cuando el negocio requiere una operación que no puede resolverse con ninguno de los dos (por ejemplo, acciones custom o reportes). En ese caso, el endpoint nuevo debe reutilizar la lógica del `AbstractService` en la mayor medida posible, e implementarse también como una clase `UseCase` propia siguiendo la misma convención.

### Ejemplo de implementación

Caso de uso: actualizar el stock de un `Producto` existente (`POST /api/producto/{uuid}/actualizar-stock`; el `/api` lo agrega `server.servlet.context-path`, el controller no lo repite).

`Code/Producto/ProductoService.java` (queda mínimo, no se toca por casos de uso nuevos):

```java
package com.blue.open.Code.Producto;

@Service
public class ProductoService extends AbstractService<Producto, ProductoDTO, ProductoPaginateDTO> {
    // Sin overrides: usa los hooks default de AbstractService para obtener/paginar/guardar/borrar
}
```

`Code/Producto/CasosDeUso/ProductoActualizarStockUseCase.java` (una clase por caso de uso, no extiende `AbstractService`):

```java
package com.blue.open.Code.Producto.CasosDeUso;

import com.blue.open.Code.Producto.Producto;
import com.blue.open.Core.Kernel.Proyeccion.Proyector;

// Caso de uso: actualiza unicamente el stock de un producto existente.
// Dependencias externas: ninguna.
// Recibe: uuid del producto y ProductoActualizarStockRequestDTO con el nuevo stock.
// Responde: ProductoActualizarStockResponseDTO con el registro actualizado.
@Service
@RequiredArgsConstructor
public class ProductoActualizarStockUseCase {

    private final EntidadRepository<Producto> repository;

    private final Proyector proyector;

    @Transactional
    public ProductoActualizarStockResponseDTO ejecutar(String uuid, ProductoActualizarStockRequestDTO dto) {
        Producto modelo = repository.buscarVigentePorUuid(uuid)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.REGISTRO_NO_ENCONTRADO));
        // El stock solo cambia por metodos de Producto (ver "Comportamiento en la entidad")
        modelo.ajustarStock(dto.getStock());
        repository.save(modelo);
        // La respuesta se re-proyecta desde la base, con la misma forma que una lectura
        return proyector.porUuid(Producto.class, uuid, ProductoActualizarStockResponseDTO.class)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.REGISTRO_NO_ENCONTRADO));
    }
}
```

`Code/Producto/ProductoController.java` (inyecta el `UseCase` ademas del `service` del ABM):

```java
package com.blue.open.Code.Producto;

import com.blue.open.Code.Producto.CasosDeUso.ProductoActualizarStockUseCase;

@RestController
@RequestMapping("producto")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    private final ProductoActualizarStockUseCase actualizarStockUseCase;

    // ... los endpoints del ABM (receta de sm-core-maven/core-crud/docs/receta-controller.md)

    // Actualiza el stock de un producto existente
    @PostMapping("/{uuid}/actualizar-stock")
    @Contrato(casoDeUso = "CU-PRO-05", resumen = "Actualiza el stock de un producto existente", permiso = "SYSADMIN")
    public ResponseEntity<ProductoActualizarStockResponseDTO> actualizarStock(
            @Descripcion("Uuid del producto") @PathVariable("uuid") String uuid,
            @Valid @RequestBody ProductoActualizarStockRequestDTO dto) {
        return new ResponseEntity<>(actualizarStockUseCase.ejecutar(uuid, dto), HttpStatus.OK);
    }
}
```

`@Contrato` es obligatorio en todo endpoint: su `permiso` autoriza el request (ver "Autorización: `@Contrato`") y, junto con el resto de sus datos, alimenta el contrato de la API y los casos de seguridad automáticos de las pruebas de contrato (ver [testing.md](testing.md)).

## Comportamiento en la entidad

Por defecto, la lógica de un caso de uso vive en su `UseCase`. Un método con lógica de negocio se agrega en la propia entidad (`Code/{Entidad}/{Entidad}.java`) únicamente si cumple al menos una de estas dos condiciones:

1. **La regla la usan dos o más casos de uso.** Por ejemplo, "el stock no puede quedar negativo" se aplica en `ActualizarStock`, `RegistrarVenta` y `AjustarInventario`: se escribe una vez en `Producto` y los tres la llaman, en vez de repetir la validación en cada `UseCase`.
2. **Es una invariante del estado de la entidad**: una regla que nunca debe romperse, sin importar desde dónde se modifique el registro (transiciones de estado, límites de un valor, campos que se calculan a partir de otros). El Core ya sigue este criterio en `EntidadConBorradoLogico.aplicarCamposBase()`, que valida las transiciones de `status`.

Si no cumple ninguna, la lógica queda en el `UseCase`. No se crean métodos en la entidad "por las dudas" ni para operaciones de carga simple.

### Límites

- **Solo estado propio.** El método lee y modifica únicamente los campos de su propia entidad. Si la operación modifica dos o más entidades, la coordina el `UseCase`, que llama al método de cada una.
- **Sin dependencias.** Las entidades no son beans de Spring: el método no inyecta ni recibe repositorios, services, `AuthService` ni integraciones externas. Lo que necesite de la base o de un servicio lo resuelve el `UseCase` y se lo pasa al método como dato.
- **Sin relaciones LAZY no cargadas.** El método no navega relaciones que el `UseCase` no haya traído en la misma consulta (`JOIN FETCH` o `@EntityGraph`, ver [consultas.md](consultas.md)). Nunca se llama dentro de un loop que dispare una consulta por registro.
- **Sin cruzar módulos.** Una entidad nunca llama métodos de una entidad que esté en otro dominio o módulo Maven (ver "Cómo crecer" en [estructura-de-carpetas.md](estructura-de-carpetas.md)). Esa coordinación va en el `UseCase`.
- **Errores con `ExcepcionDeApi`.** Si la operación rompe la regla, el método lanza `ExcepcionDeApi` con el mensaje para el usuario final, igual que el Core.
- **Encapsulamiento.** Los campos que se modifican a través de un método de la entidad no generan setter público: se anotan con `@Setter(AccessLevel.NONE)`. La copia genérica del DTO (`Proyector.aplicar`, que usa `POST /guardar`) no los asigna porque no tienen setter público; solo cambian a través del método. Si el ABM tiene que cambiarlos, la entidad implementa `AplicaDto<{Entidad}DTO>` y llama a sus métodos de negocio desde `aplicar(dto)`, que corre después de la copia y no recibe dependencias; lo que necesite un service va en el hook `hookGuardarAplicarDto` del `Service`.
- **Concurrencia.** Si el campo lo pueden modificar dos requests al mismo tiempo (stock, saldos, contadores), la entidad declara un atributo `@Version` (bloqueo optimista), con su columna creada por Liquibase.
- **Comentario obligatorio.** Igual que toda función ([idioma-y-estilo.md](idioma-y-estilo.md)): qué hace, qué recibe, qué valida y qué excepción lanza.
- **Test unitario.** Como no depende de datos externos, cada método de comportamiento de la entidad tiene su test unitario (ver [testing.md](testing.md)).

### Ejemplo

`Code/Producto/Producto.java`:

```java
// Stock disponible. Solo se modifica con agregarStock/descontarStock
@Setter(AccessLevel.NONE)
private Integer stock = 0;

// Bloqueo optimista: evita que dos requests pisen el stock al mismo tiempo
@Version
private Long version;

// Suma unidades al stock. Recibe una cantidad mayor a 0.
// Lanza ExcepcionDeApi si la cantidad no es valida.
public void agregarStock(int cantidad) {
    if (cantidad <= 0)
        throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CANTIDAD_INVALIDA, "La cantidad a agregar debe ser mayor a 0",
                "Producto | agregarStock | cantidad: " + cantidad);
    this.stock += cantidad;
}

// Reemplaza el stock por un valor absoluto (por ejemplo, despues de un conteo fisico).
// Recibe un stock mayor o igual a 0. Lanza ExcepcionDeApi si es negativo.
public void ajustarStock(int nuevoStock) {
    if (nuevoStock < 0)
        throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CANTIDAD_INVALIDA, "El stock no puede ser negativo",
                "Producto | ajustarStock | nuevoStock: " + nuevoStock);
    this.stock = nuevoStock;
}

// Descuenta unidades del stock. Recibe una cantidad mayor a 0.
// Lanza ExcepcionDeApi si la cantidad no es valida o si el stock quedaria negativo.
public void descontarStock(int cantidad) {
    if (cantidad <= 0)
        throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CANTIDAD_INVALIDA, "La cantidad a descontar debe ser mayor a 0",
                "Producto | descontarStock | cantidad: " + cantidad);
    if (this.stock < cantidad)
        throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.STOCK_INSUFICIENTE)
                .conDato("stockActual", this.stock);
    this.stock -= cantidad;
}
```

`Code/Producto/CasosDeUso/ProductoRegistrarVentaUseCase.java` (coordina: carga, llama al método de la entidad, guarda y responde):

```java
@Transactional
public ProductoStockResponseDTO ejecutar(String uuid, ProductoRegistrarVentaRequestDTO dto) {
    Producto modelo = repository.buscarVigentePorUuid(uuid)
            .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.REGISTRO_NO_ENCONTRADO));
    modelo.descontarStock(dto.getCantidad());
    repository.save(modelo);
    // La respuesta se re-proyecta con la consulta propia del repositorio (ver consultas.md)
    return repository.findStockByUuid(uuid)
            .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.REGISTRO_NO_ENCONTRADO));
}
```

`ProductoActualizarStockUseCase` (ver "Ejemplo de implementación") usa `ajustarStock` de la misma forma: ningún caso de uso asigna `stock` directamente.

## Alcance de modificación

El Core (`sm-core-maven/core-*`) no se modifica desde un proyecto. No es una prohibición formal: es que no hace
falta. Cuando el proyecto necesita algo distinto de lo que el Core hace por defecto, las salidas son,
en este orden:

1. **Configuración**: una propiedad en `application.properties` (ver la tabla de propiedades en [`docs/arquitectura/core/README.md`](../arquitectura/core/README.md)).
2. **Hook**: sobreescribir uno de los `hookXxx` de `AbstractService`.
3. **Puerto**: publicar un bean propio que reemplace al del Core (catálogo completo en el mismo documento).
4. **No usar el módulo**: borrar su dependencia de `app/pom.xml` y su `include` del master de Liquibase.
5. **Issue al Core**: sale en la versión siguiente, con su entrada en `sm-core-maven/changelog/`.

Si ninguna de las cinco alcanza, es un bug del Core: falta un punto de extensión. El agente debe
detenerse y avisarle al usuario, no copiar ni reimplementar el Core en el proyecto.

Desde Core 4.0.0 el proyecto no tiene el código del Core: lo descarga como dependencia Maven publicada
(`sm-core-maven`). `CorePublicadoTest` (`app`) verifica que siga así: falla si aparece un directorio `core/`
o una clase en el paquete `com.blue.open.Core` dentro del proyecto (CORE-01 del checklist). Ver
[`docs/auditoria/README.md`](../auditoria/README.md) → "Core publicado".

- No se debe modificar la carpeta `deploy`, a menos que se pida explícitamente.
