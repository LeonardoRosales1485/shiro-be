# Consultas a la Base de Datos (JPA / Hibernate)

Este documento define cómo se escriben las consultas desde los repositorios y casos de uso de `Code`. El objetivo es que cada consulta sea eficiente y que sea explícito qué trae de la base.

El paginado del ABM (`POST /paginar`, `AbstractService.paginar`) ya sigue estas reglas: el Core lo arma en JPQL, con joins automáticos y proyectando solo las columnas del DTO (ver [entidades-y-dtos.md](entidades-y-dtos.md) y `sm-core-maven/core-crud/docs/paginado.md`).

## Carga de relaciones

- Toda relación (`@ManyToOne`, `@OneToOne`, `@OneToMany`, `@ManyToMany`) se declara con `fetch = FetchType.LAZY`. `@ManyToOne` y `@OneToOne` son `EAGER` por defecto en JPA, así que en esas se escribe explícitamente.
- El proyecto corre con `spring.jpa.open-in-view=false`: una relación LAZY que se lee fuera de una transacción lanza `LazyInitializationException`. Si el caso de uso necesita una relación, se trae en la misma consulta (ver "N+1") o se lee dentro del método `@Transactional`.

## N+1

- Está prohibido recorrer una lista de entidades y leer una relación LAZY de cada una dentro del loop: dispara una consulta extra por registro.
- Si el caso de uso necesita la relación para todos los registros, se trae en la misma consulta con `JOIN FETCH` en un `@Query`, o con `@EntityGraph` en el método del repositorio.

```java
@Query("SELECT DISTINCT r FROM Rol r LEFT JOIN FETCH r.permisos WHERE r.uuid IN :uuids")
List<Rol> buscarPorUuidsConPermisos(@Param("uuids") List<String> uuids);
```

## Proyección por defecto

- Toda lectura arma el DTO directamente desde la consulta (Proyección), con solo las columnas que necesita la respuesta: campos, `{OtraEntidad}Uuid` por join, valores calculados. No se trae la entidad para después convertirla a mano.
- En el ABM (`AbstractService`) lo resuelve el Core con el `Proyector` (`core-kernel`) en `obtenerPorUuid`, `paginar` y en la respuesta de `guardar` y `borrar`: los campos del DTO que se llaman igual que un atributo de la entidad se completan solos y `@Columna("relacion.atributo")` navega la relación con `LEFT JOIN`. Ver `sm-core-maven/core-kernel/docs/proyeccion.md`.
- Las colecciones de un DTO (`List<SubDTO>`) se declaran con `@Coleccion("relacion", orden = "atributo")`: el Core las completa con una consulta por anotación para todos los registros (`IN` sobre los ids del nivel padre, en lotes), sin N+1, y si el elemento tiene borrado lógico trae solo los vigentes. Es la forma de traer colecciones en los DTOs del ABM; no se recorre la lista leyendo la relación de cada registro.
- En multiempresa, la entidad de usuario del proyecto extiende `UsuarioBaseMultiempresa` (`core-multiempresa`): es la que declara la colección `rolesPorEmpresa` que la Proyección de `UsuarioBaseMultiempresaDTO` lee con `@Coleccion`.
- Lo que la Proyección del ABM no cubre es un caso de uso con consulta propia: una proyección en el repositorio (`@Query` con `SELECT new ...DTO(...)`) o el `Proyector` inyectado (`porUuid`, `completarColecciones`). Esto aplica a todas las lecturas de casos de uso (ver DTOs por caso de uso en [controllers-y-endpoints.md](controllers-y-endpoints.md)):

```java
@Query("SELECT new com.blue.open.Code.Producto.ProductoStockResponseDTO(p.uuid, p.stock) "
        + "FROM Producto p WHERE p.uuid = :uuid AND p.status = 0")
Optional<ProductoStockResponseDTO> findStockByUuid(@Param("uuid") String uuid);
```

- Escrituras: el caso de uso modifica la entidad (una proyección no se puede guardar), así que carga la entidad completa y le aplica el DTO con `Proyector.aplicar` (o con sus métodos de negocio). La respuesta se arma re-proyectando el registro guardado (`Proyector.porUuid` o la consulta propia del repositorio), igual que la lectura.
- Las transformaciones que JPQL no puede expresar (por ejemplo, formatear con lógica Java) se resuelven en el caso de uso o en un hook de `AbstractService`, con un comentario que explique por qué no alcanzó la Proyección.

## Qué forma de consulta usar

Aplicar en este orden. La primera regla que matchee decide:

1. **Búsqueda simple por uno o dos campos** (`findByUuid`, `existsByEmail`, `findFirstByCodigoAndStatus`): método derivado de Spring Data. No se reescribe en JPQL.
2. **Consulta no trivial**: más de dos condiciones, joins, `JOIN FETCH`, agregaciones (`COUNT`, `SUM`, `GROUP BY`), `EXISTS`, proyecciones, o un método derivado cuyo nombre se vuelve ilegible → JPQL con `@Query` en el repositorio.
3. **SQL nativo** (`nativeQuery = true` o `createNativeQuery`): solo cuando JPQL no lo resuelve (funciones propias del motor, reportes con SQL que JPQL no soporta). Debe llevar un comentario que explique por qué no se usó JPQL.
4. Si una consulta no encaja con claridad en ninguna de las anteriores → detenerse y preguntar al usuario.

- Los parámetros siempre van con nombre (`:uuid` + `@Param`). Nunca se concatenan valores en el string de la consulta.
- Las consultas de entidades con borrado lógico filtran `status = 0` salvo que el caso de uso necesite explícitamente los eliminados o temporales.

## Ausencia de resultado: `Optional`

Un método de repositorio **nunca devuelve `null`**. El tipo de retorno tiene que dejar explícito qué pasa cuando no hay resultado, para que el compilador obligue a resolverlo en vez de dejarlo a criterio del llamador.

| Lo que devuelve el método | Tipo de retorno |
|---|---|
| 0 o 1 registro (entidad o proyección) | `Optional<T>` |
| N registros | `List<T>`, vacía si no hay ninguno. **Nunca** `Optional<List<T>>` |
| Existencia o conteo | `boolean` / `long` |
| Paginado | `Pagina<T>` (el paginado del Core) o `Page<T>` (un método de Spring Data que recibe `Pageable`) |

Aplica igual a los métodos derivados del nombre y a los `@Query`: Spring Data soporta `Optional` de forma nativa en ambos, sin tocar el JPQL.

Esto vale también para los puertos del Core que consultan la base (`UsuarioRepositorio`, `EmpresaRepositorio`, `EstrategiaDeBorrado.buscarVigentePorUuid`) y para los hooks de búsqueda de `AbstractService` (`hookObtenerBuscarEnBase`, `hookGuardarBuscarExistente`, `hookBorrarBuscarPorUuid`). Ver [`sm-core-maven/changelog/2.3.0.md`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/changelog/2.3.0.md).

### Dónde NO se usa `Optional`

- **Campos de una entidad o de un DTO.** `Optional` no es serializable y JPA no lo mapea. Un campo que puede faltar es un campo nullable y punto.
- **Parámetros de un método.** Si un argumento es opcional, se sobrecarga el método o se acepta `null` documentado.
- **`EstrategiaDeAutenticacion.getUsuario()`.** Ahí el `null` no significa "no existe el usuario" sino "esta request no usa este protocolo", y no es una consulta a la base.

### Cómo se consume

La decisión de qué hacer ante el vacío es del caso de uso, no del repositorio: **el Core no provee ningún helper tipo `...OrFail`**. Cada llamada elige su forma:

```java
// El registro tiene que existir: corta con el codigo que corresponda a ese caso de uso
Producto modelo = repository.buscarVigentePorUuid(uuid)
        .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.REGISTRO_NO_ENCONTRADO));

// Si no existe, se crea
Configuracion configuracion = repository.findFirstByNombreAndStatus(clave, ACTIVO)
        .orElseGet(() -> new Configuracion(clave, valorPorDefecto, true));

// Solo interesa si existe
if (usuarioRepository.findFirstByEmailAndStatusNot(email, -1).isPresent())
    throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.EMAIL_YA_REGISTRADO);
```

Está prohibido `.get()` sin un `isPresent()` previo, y `.orElse(null)` salvo que el método que lo contiene devuelva `null` por contrato documentado (por ejemplo `AuthService.getActualONull()`), con un comentario que lo diga.

### En los tests

Un stub de Mockito sobre un método que devuelve `Optional` se escribe `thenReturn(Optional.of(entidad))`. Para simular "no existe" no hace falta stub: Mockito ya devuelve `Optional.empty()` por defecto.

## Verificación

- Al crear o modificar un endpoint que consulta la base, revisar en local el SQL que genera Hibernate (`logging.level.org.hibernate.SQL=DEBUG` en `application.local.properties`, sin commitearlo) y confirmar que no haya N+1 ni columnas o relaciones de más.
