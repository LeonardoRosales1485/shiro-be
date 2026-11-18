# Rollback y cambios destructivos

## Rollback obligatorio

Todo changeset, sin excepcion, debe incluir su bloque `rollback:` correspondiente. No se mergea un changeset sin rollback definido, sea el cambio aditivo (`createTable`, `addColumn`) o destructivo (`dropColumn`, `dropTable`).

Ejemplo:

```yaml
databaseChangeLog:
  - changeSet:
      id: 2026-07-06-add-column-status-facturas
      author: jperez
      comment: Agrega columna status a facturas para soft delete
      changes:
        - addColumn:
            tableName: facturas
            columns:
              - column:
                  name: status
                  type: SMALLINT
                  defaultValueNumeric: 0
                  constraints:
                    nullable: false
      rollback:
        - dropColumn:
            tableName: facturas
            columnName: status
```

Si un changeset tiene varios elementos en `changes`, el `rollback:` cubre la reversion completa de todos ellos, en orden inverso.

## Cambios destructivos (dropColumn, dropTable, cambio de tipo con perdida de datos)

No tienen un proceso especial: se revisan como cualquier otro changeset, con el mismo PR y el mismo reviewer. La unica exigencia extra es la misma regla general de rollback obligatorio de arriba.

Es responsabilidad de quien escribe el changeset y de quien revisa el PR evaluar si el cambio destructivo es seguro para el estado actual de produccion.
