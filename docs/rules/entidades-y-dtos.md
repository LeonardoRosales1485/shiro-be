# Entidades y DTOs

## Orden de trabajo: primero los casos de uso

El código se plantea desde los casos de uso, no desde las entidades. Al crear una funcionalidad nueva:

1. **Listar los casos de uso**: cada acción que el usuario o el sistema necesita hacer (una pantalla, un botón, un reporte, una integración). Cada caso de uso define qué datos recibe y qué datos responde.
2. **Derivar de cada caso de uso su endpoint y sus DTOs** (ver [controllers-y-endpoints.md](controllers-y-endpoints.md)).
3. **Diseñar la entidad** como el modelo de persistencia que cubre lo que piden esos casos de uso: sus campos, sus relaciones y sus invariantes.

Una entidad solo está obligada a tener su `@Entity` y su `Repository`. El Controller, el Service y los DTOs existen únicamente si algún caso de uso (o un endpoint del ABM publicado) los necesita. Por ejemplo, una entidad hija (`ItemFactura`) que solo se modifica desde un caso de uso de `Factura` no tiene controller, service ni DTOs propios.

### Entidad chica: ABM con la receta

Si todos los casos de uso listados para una entidad son un ABM que usa todos sus campos (alta, edición, baja, obtención y listado del registro completo), la entidad es **chica** y sus endpoints son, por defecto, los del ABM, escritos con la receta de `sm-core-maven/core-crud/docs/receta-controller.md` (ver [controllers-y-endpoints.md](controllers-y-endpoints.md)). Si algún caso de uso necesita menos datos, otra forma o una acción que no es ABM, ese caso de uso lleva su propio `UseCase` y DTO.

## Entities

- Una entidad con **borrado lógico** hereda de `EntidadConBorradoLogico` (que a su vez hereda de `EntidadBase`). Es el caso por defecto.
- Una entidad con **borrado físico** hereda directamente de `EntidadBase`: no tiene columna `status`, el `DELETE` saca la fila y el paginado no le aplica filtro de vigencia.
- Elegir uno u otro es la única decisión: el Core resuelve solo cómo borrar según de qué clase hereda la entidad.
- Las entidades con borrado lógico tienen un `status`, por defecto 0.
  - `0`: activo.
  - `-1`: eliminado.
  - `-2`: temporal. Se usa cuando una entidad depende de otra que se carga en varios pasos (por ejemplo, una tarea que requiere fotos: se crea la tarea en `-2`, se cargan las fotos una por una, y al finalizar correctamente la carga la tarea pasa a `0`).

## DTOs

Reglas comunes a todos los DTOs, sean de caso de uso o del ABM:

- Todos los DTO deben heredar de `EntidadBaseDTO`.
- Los DTO de entidades que tengan relación con otras entidades deben tener `{OtraEntidad}Uuid`. Por ejemplo, si `Vehiculo` tiene asociado `TipoVehiculo`, el DTO de `Vehiculo` debe tener `TipoVehiculoUuid`.
- Los endpoints no deben responder el `id` interno, el front únicamente debe conocer el `uuid`. Esto es por seguridad, para evitar enumeración.

### Validaciones

- Todos los DTO que sirvan para guardar datos deben tener validaciones usando `@Valid`. Cada campo debe tener su validación y su mensaje de error específico.
  - Las validaciones se determinan combinando dos fuentes: los `NOT NULL` del schema de la base de datos y las reglas de negocio documentadas en `docs\README.md`.
  - Si ambas fuentes entran en conflicto (por ejemplo, el schema permite NULL pero el negocio dice que el campo es obligatorio, o viceversa), el agente debe detenerse y preguntar al usuario antes de decidir.
- Ante un dato inválido se debe responder status `400` con un mensaje de error personalizado por variable.

### Fechas

- Todas las fechas (`Timestamp`, `Date`, `LocalDateTime`) en DTOs deben tener la anotación:
  ```java
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "America/Argentina/Buenos_Aires")
  ```
- Las fechas se serializan como strings ISO 8601 en GMT-3. Ejemplo: `2026-08-06T10:46:00.123-03:00`.
- Las fechas se trabajan siempre en GMT-3, nunca en una zona horaria distinta ni implícita del host.
- El frontend recibe y envía siempre fechas en GMT-3.

## DTOs por caso de uso

Ver en [controllers-y-endpoints.md](controllers-y-endpoints.md) cuándo un caso de uso usa un endpoint del ABM y cuándo lleva su propio endpoint y DTO.

- Convención de nombres: `{Entidad}{CasoDeUso}RequestDTO` y `{Entidad}{CasoDeUso}ResponseDTO`. Si Request y Response tienen los mismos campos, se usa un único DTO para ambos.
- Si el caso de uso es de solo lectura, no hace falta RequestDTO: el uuid viaja por el path.
- Si varios casos de uso necesitan exactamente los mismos datos, deben compartir el mismo DTO en vez de duplicarlo.
- Los DTOs de caso de uso se ubican en el mismo paquete que el resto de los DTOs de la entidad. Si el caso de uso toca varias entidades, van en la carpeta de la entidad principal (ver [estructura-de-carpetas.md](estructura-de-carpetas.md)).

## DTOs del ABM

Solo existen si la entidad publica el ABM (ver "Entidad chica: ABM con la receta").

- `{Entidad}DTO`: lo usan `GET /uuid/{uuid}`, `POST /guardar` y `DELETE /borrar/{uuid}`, con todos los campos de la entidad. La respuesta de `POST /guardar` tiene exactamente la misma forma que la de `GET /uuid/{uuid}`: el Core re-proyecta el registro guardado. Si alcanza, se comparte también para el paginado.
- `{Entidad}PaginateDTO`: se crea cuando el listado necesita solo una parte de los campos de `{Entidad}DTO`, con únicamente los campos útiles para la paginación.
- Lectura (Proyección): el Core trae de la base únicamente las columnas del DTO (no hace "select *" ni convierte la entidad a mano). Los campos del DTO que se llaman igual que un atributo de la entidad se completan solos; los que vienen de una entidad relacionada se declaran con `@Columna("relacion.atributo")` (por ejemplo `@Columna("tipoVehiculo.uuid") String tipoVehiculoUuid`). Ver `sm-core-maven/core-kernel/docs/proyeccion.md` y `sm-core-maven/core-crud/docs/paginado.md`.
- Listas de sub-DTOs: un campo `List<SubDTO>` que sale de una colección de la entidad (`@OneToMany`/`@ManyToMany`) se declara con `@Coleccion("relacion", orden = "atributo")`. Se resuelve sin N+1 (ver [consultas.md](consultas.md)) y es de solo lectura: al guardar se ignora.
- Escritura (aplicar un DTO): `POST /guardar` copia por reflexión sobre la entidad los campos del DTO con el mismo nombre que un atributo con setter público, y asigna las relaciones declaradas con `@Columna("relacion.uuid")` buscando el registro vigente (si no existe responde `400` con `RELACION_NO_ENCONTRADA`; `null` desasocia). Las rutas más largas que `relacion.uuid` y las `@Coleccion` no se copian. No hace falta escribir ninguna clase de conversión.
- Los atributos que nunca deben poder filtrarse, ordenarse, mostrarse ni copiarse al guardar (contraseñas, códigos, secretos) se marcan con `@NoProyectable`: quedan fuera de la lectura y de la escritura. `@JsonIgnore` tiene el mismo efecto.
