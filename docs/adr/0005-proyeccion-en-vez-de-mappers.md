# Proyección por defecto en vez de mappers por entidad

Hasta Core 3.0.1, `getByUuid`, `save` y `delete` cargaban la entidad completa y la convertían con un `{Entidad}Mapper` escrito a mano (`AbstractMapperComplete`), mientras que `paginate` ya proyectaba por JPQL solo las columnas del DTO. Eso traía consultas más pesadas que la respuesta y un N+1 en todo mapper que leyera una colección (`BaseUserMultiempresaMapper` hacía una consulta extra por usuario para `userCompanyRoles`). Se decide que **la proyección sea el único camino de lectura del ABM** y que la escritura sea una **copia por reflexión del DTO sobre la entidad**, resueltas ambas por un motor genérico del kernel (`Core/Kernel/Proyeccion/`, bean `Proyector`), y que `AbstractMapperComplete` y todos los mappers (de `Code` y del Core) desaparezcan. Sale como Core 3.0.2: 3.0.0 se publicó el mismo día y ningún proyecto la consume, así que no se abre un major.

## Cómo queda

- **Lectura** (`uuid`, `paginate`, respuesta de `save` y `delete`): el DTO se arma desde la consulta. Campos con el nombre de un atributo de la entidad se traen solos; `@Columna("relacion.atributo")` navega `@ManyToOne`/`@OneToOne` con `LEFT JOIN`; `@Coleccion("relacion", orden = "atributo")` sobre un `List<SubDTO>` se resuelve con **una consulta por anotación** (`IN` sobre los ids del nivel padre, partido en lotes), sin límite de niveles y con detección de ciclos al arrancar.
- **Escritura** (`save`): el Core copia por reflexión los campos del DTO sobre la entidad cargada y llama a `setAbstract(dto)`. `@Columna("relacion.uuid")` busca la relación vigente y la asigna (`RELACION_NO_ENCONTRADA` si no existe, `null` desasocia); las rutas más largas y las `@Coleccion` son de solo lectura. Después de la copia, si la entidad implementa `AplicaDto<DTO>`, se llama `aplicar(dto)` para los campos que cambian por método de negocio (`@Setter(AccessLevel.NONE)`); lo que necesita dependencias sigue en el hook del service.
- **Exclusiones simétricas**: `@NoProyectable` (antes `@NoPaginable`) y `@JsonIgnore` dejan el atributo fuera de la lectura y de la copia. `password` nunca pasa por el camino genérico: lo escribe `BaseUserService`, hasheado.
- Un caso de uso que la proyección no cubre escribe su propia consulta en el repositorio (`@Query` con `SELECT new`) o usa el `Proyector` directamente.
- Una `@Coleccion` cuyos elementos tienen borrado lógico trae solo los vigentes (`status = 0`), igual que el resto de las consultas.
- Multi-empresa: como `@Coleccion` necesita un atributo colección y el Core no puede nombrar las entidades de usuario ni de empresa del proyecto, la entidad de usuario extiende `BaseUserMultiempresa` (mapped superclass con `userCompanyRoles` de solo lectura) y `UserCompanyRole` lee la empresa por `EmpresaDeRelacion`, una entidad inmutable sobre `companies` con uuid y nombre. No cambia el esquema.

## Considered Options

- Dejar el mapper solo para la escritura (`dtoToModel`): se descartó porque mantiene una clase por entidad y el objetivo era eliminarla.
- Mudar el mapeo a un método `aplicar(DTO)` en cada entidad en vez de la copia por reflexión: se descartó como camino por defecto porque reescribe el mapper entero en la entidad; queda como complemento posterior a la copia.
- Prohibir colecciones en los DTOs del ABM: se descartó porque obliga a un caso de uso propio para cada lista (`userCompanyRoles`), que es lo más común.
- Resolver las colecciones con un hook por entidad (`hookPaginarCompletarResultados`): se mantiene como escape, pero no como default, porque cada proyecto repetiría la misma segunda consulta.
- Sincronizar colecciones al guardar: se descartó por el costo de hacerlo genérico (huérfanos, tablas intermedias, orden); una colección se escribe por hook o por caso de uso.
- Tope de niveles en `@Coleccion`: se descartó porque no resuelve el volumen multiplicativo, el tamaño del `IN` ni los ciclos; esos se atienden con lotes y detección al arrancar.
- Armar la respuesta de `save` desde la entidad sin consulta extra: se descartó para que `uuid` y `save` respondan exactamente la misma forma por un único camino.

## Consequences

- La forma del DTO pasa a ser la única fuente de verdad de qué se lee y qué se escribe; ya no hay un mapper que decida ocultar o completar campos a mano.
- `setAbstract(dto)` (transiciones de `status`) deja de depender de que cada mapper lo llame: lo hace el Core.
- Un `save` cuesta un `SELECT` más (la re-proyección), a cambio de respuesta idéntica a `uuid`.
- Un DTO con `@Coleccion` anidadas profundas puede generar consultas hoja muy grandes; el costo es de quien diseña el DTO.
- Cambian `docs/rules/consultas.md`, `entidades-y-dtos.md`, `estructura-de-carpetas.md`, `controllers-y-endpoints.md`, los docs de `core-crud`, la receta del controller y el vocabulario en `CONTEXT.md`.
