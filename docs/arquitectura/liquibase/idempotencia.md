# Idempotencia y precondiciones

## Regla unica

Todo changeset de DDL (`createTable`, `addColumn`, `createIndex`, `addForeignKeyConstraint`, etc.) usa una precondicion nativa de Liquibase con `onFail: MARK_RAN`.

Esto significa: si la tabla/columna/indice/constraint que el changeset va a crear ya existe, Liquibase marca el changeset como ejecutado y sigue arrancando la aplicacion sin fallar. Liquibase no debe frenar el arranque solo porque el objeto ya existe.

Las precondiciones se escriben con los checks nativos de Liquibase (`tableExists`, `columnExists`, `indexExists`, `foreignKeyConstraintExists`, `primaryKeyExists`, `uniqueConstraintExists`), nunca contra tablas de catalogo especificas de un motor (`information_schema`, `pg_catalog`, etc.). Un check nativo es lo que permite que la misma precondicion funcione igual en MariaDB y en PostgreSQL; una consulta manual a `information_schema` u otro catalogo interno ata el changeset a un motor puntual, que es justo lo que este formato busca evitar.

La app no confia en Liquibase para detectar si el schema real quedo distinto al esperado por el codigo: eso lo valida Hibernate con `spring.jpa.hibernate.ddl-auto=validate` al levantar el contexto. Si el schema no coincide con las entidades, el arranque falla ahi, con un mensaje claro de que columna/tabla no coincide. Liquibase no debe duplicar esa validacion ni abortar el arranque por su cuenta cuando el objeto ya existe.

Los changesets de datos (`insert`/`update`) siguen siendo idempotentes por si mismos: con una precondicion `sqlCheck`/`rowCountGreaterThan` que verifica si el registro ya existe, o apoyandose en una clave unica de la tabla. Cuando el valor de una columna depende de otra tabla (por ejemplo, resolver el `id` de una empresa a partir de su `uuid`), se usa `valueComputed` con una subconsulta dentro del `insert` nativo, en vez de recurrir a SQL plano.

## Ejemplo: crear tabla

```yaml
databaseChangeLog:
  - changeSet:
      id: 2026-07-06-create-table-facturas
      author: jperez
      comment: Crea tabla facturas
      preConditions:
        onFail: MARK_RAN
        not:
          - tableExists:
              tableName: facturas
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
      rollback:
        - dropTable:
            tableName: facturas
```

## Ejemplo: agregar columna

```yaml
databaseChangeLog:
  - changeSet:
      id: 2026-07-06-add-column-status-facturas
      author: jperez
      comment: Agrega columna status a facturas
      preConditions:
        onFail: MARK_RAN
        not:
          - columnExists:
              tableName: facturas
              columnName: status
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

## Ejemplo: insert idempotente

```yaml
databaseChangeLog:
  - changeSet:
      id: 2026-07-06-insert-default-currency
      author: jperez
      comment: Inserta la moneda por defecto
      preConditions:
        onFail: MARK_RAN
        sqlCheck:
          expectedResult: 0
          sql: SELECT COUNT(*) FROM monedas WHERE codigo = 'ARS'
      changes:
        - insert:
            tableName: monedas
            columns:
              - column:
                  name: codigo
                  value: ARS
              - column:
                  name: nombre
                  value: Peso argentino
      rollback:
        - delete:
            tableName: monedas
            where: codigo = 'ARS'
```

## Por que este esquema

`onFail: MARK_RAN` es el unico modo de precondicion que cumple las dos condiciones a la vez: no revierte ni aborta el `update` cuando el objeto ya existe, y sigue dejando que la validacion real (si el schema coincide con el codigo) ocurra en `ddl-auto=validate`. Usar `HALT` (el default) rompe el arranque ante un objeto ya existente aunque sea identico; no usar precondicion obliga a que cada `createTable`/`addColumn` este envuelto en un chequeo condicional propio de cada motor. Los checks nativos (`tableExists`, `columnExists`, etc.) son los que permiten correr el mismo changelog contra MariaDB o PostgreSQL sin reescribirlo.
