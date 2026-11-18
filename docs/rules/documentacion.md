# Documentación

- La documentación del negocio está en [`docs/README.md`](../README.md).
- El contrato de la API es `docs/api/openapi.yaml` (más `docs/api/index.html`, que lo muestra con Redoc). No se escribe a mano: lo generan las pruebas de contrato en la corrida completa y se commitea (ver [`docs/arquitectura/testing/README.md`](../arquitectura/testing/README.md)).
- Documentar un endpoint es anotarlo y probarlo:
  - `@Contrato` en el método del controller: caso de uso, resumen (qué hace) y permiso.
  - `@Descripcion` en cada campo de los DTOs que recibe y responde y en cada parámetro (qué espera y qué devuelve).
  - Una prueba de contrato por caso de uso, con el caso feliz y cada caso de error (sus casos de uso y mensajes de error). Ver [testing.md](testing.md).

## Documentación del Core

El módulo `core-crud` resuelve en `AbstractService` la lógica de los 4 endpoints del ABM de una entidad, que el controller escribe con la receta de `sm-core-maven/core-crud/docs/receta-controller.md` (solo los que la entidad necesita). La documentación completa se encuentra en:

**Índice principal**: `sm-core-maven/core-crud/docs/README.md`

Qué módulos del Core existen, qué puertos expone cada uno y cómo se elige usarlos está en [`docs/arquitectura/core/README.md`](../arquitectura/core/README.md).

1. **GET /uuid/{uuid}** — Obtener registro por UUID.
   - Documentación: `sm-core-maven/core-crud/docs/obtener.md`
   - Operación read-only, sin filtros.
   - Retorna un DTO individual.

2. **POST /paginar** — Búsqueda avanzada con paginación.
   - Documentación: `sm-core-maven/core-crud/docs/paginado.md`
   - Soporta filtros complejos, ordenamiento, JOINs, GROUP BY.
   - Retorna `Pagina<PAGINADODTO>` paginado.
   - Filtros con 11+ operadores (`=`, `>`, `<`, `like`, `in`, `is null`, etc.).

3. **POST /guardar** — Crear o actualizar registro.
   - Documentación: `sm-core-maven/core-crud/docs/guardar.md`
   - CREATE automático si no hay UUID, UPDATE si existe.
   - Auditoría completa (usuario, timestamps).
   - No se pueden reactivar registros eliminados.

4. **DELETE /borrar/{uuid}** — Borrado lógico o físico, según de qué clase herede la entidad.
   - Documentación: `sm-core-maven/core-crud/docs/borrar.md`
   - Entidad que extiende `EntidadConBorradoLogico`: marca `status = -1`, guarda quién y cuándo, recuperable.
   - Entidad que extiende solo `EntidadBase`: `DELETE` real, sin columna `status`.

Cómo usar la documentación del Core:
- Comenzar por `README.md` para entender la arquitectura general.
- Consultar el archivo específico del endpoint que se necesite usar.
- Cada archivo contiene ejemplos, casos de uso, hooks y mejores prácticas.
- Los hooks permiten personalizar comportamiento en subclases.
