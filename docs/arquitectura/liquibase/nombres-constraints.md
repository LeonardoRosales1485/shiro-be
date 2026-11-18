# Convencion de nombres de constraints e indices

Regla unica para nombrar `PRIMARY KEY`, `UNIQUE`, `FOREIGN KEY` e `INDEX` en cualquier changeset nuevo.

| Tipo | Patron | Ejemplo |
|---|---|---|
| Unique key | `uk_<tabla>_<columna>` | `uk_facturas_uuid` |
| Foreign key | `fk_<tabla>_<columna_sin_prefijo_id>` | `fk_facturas_empresa` (columna `id_empresa`) |
| Index simple | `idx_<tabla>_<columna>` | `idx_facturas_status` |
| Index compuesto | `idx_<tabla>_<columna1>_<columna2>` | `idx_facturas_empresa_status` |

Reglas adicionales:

- `<tabla>` es el nombre de tabla completo, no abreviado.
- En foreign keys, `<columna>` se escribe sin el prefijo `id_` (la columna `id_empresa` da `fk_facturas_empresa`, no `fk_facturas_id_empresa`).
- Si una tabla necesita mas de una unique key o index sobre las mismas columnas semanticas, se agrega un sufijo numerico: `uk_facturas_codigo_2`.
- No dejar que Liquibase o el motor autogeneren el nombre del constraint; siempre nombrarlo explicitamente con `constraintName` (foreign key, unique) o `indexName` (index).

Ejemplo:

```yaml
databaseChangeLog:
  - changeSet:
      id: 2026-07-06-create-table-facturas
      author: jperez
      comment: Crea tabla facturas
      changes:
        - createTable:
            tableName: facturas
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: uuid
                  type: CHAR(36)
                  constraints:
                    nullable: false
              - column:
                  name: status
                  type: SMALLINT
                  defaultValueNumeric: 0
                  constraints:
                    nullable: false
              - column:
                  name: id_empresa
                  type: BIGINT
        - addUniqueConstraint:
            tableName: facturas
            columnNames: uuid
            constraintName: uk_facturas_uuid
        - createIndex:
            tableName: facturas
            indexName: idx_facturas_status
            columns:
              - column:
                  name: status
        - createIndex:
            tableName: facturas
            indexName: idx_facturas_empresa_status
            columns:
              - column:
                  name: id_empresa
              - column:
                  name: status
        - addForeignKeyConstraint:
            baseTableName: facturas
            baseColumnNames: id_empresa
            referencedTableName: empresas
            referencedColumnNames: id
            constraintName: fk_facturas_empresa
      rollback:
        - dropForeignKeyConstraint:
            baseTableName: facturas
            constraintName: fk_facturas_empresa
        - dropTable:
            tableName: facturas
```
