# Documentacion de Liquibase

Este directorio define la estrategia de migraciones de base de datos del backend.
Los agentes deben usar este indice antes de crear o modificar changelogs.

## Indice

1. [Estrategia de changelogs](changelogs.md)
2. [Seed demo opcional](seed-demo.md)
3. [Comandos de uso](comandos.md)
4. [Convencion de orden de columnas](orden-columnas.md)
5. [Convencion de archivos, id y author](nombres-changesets.md)
6. [Rollback y cambios destructivos](rollback.md)
7. [Idempotencia y precondiciones](idempotencia.md)
8. [Convencion de nombres de constraints e indices](nombres-constraints.md)

## Regla principal

El changelog automatico de la aplicacion es `db/changelog/db.changelog-master.yaml`.
Ese archivo solo debe incluir estructura y datos obligatorios para produccion.

Los datos de ejemplo viven en `db/changelog/db.changelog-seed.yaml`.
Ese changelog no se incluye desde el master y solo se ejecuta con un comando explicito.

## Formato obligatorio: YAML

Todo changeset, sea migracion (`db/changelog/changes/`) o seed (`db/changelog/seeds/`), se escribe en **YAML** (`.yaml`) usando los changeTypes nativos de Liquibase (`createTable`, `addColumn`, `insert`, `addForeignKeyConstraint`, etc.). No se usa SQL plano ni el formato `--liquibase formatted sql`.

Motivo: SQL plano ata el schema a la sintaxis de un motor puntual (por ejemplo `AUTO_INCREMENT`, `ENGINE=InnoDB`, `TINYINT`, `ON UPDATE CURRENT_TIMESTAMP`, `FULLTEXT INDEX` de MySQL/MariaDB). El formato YAML con changeTypes nativos deja que Liquibase traduzca cada operacion al dialecto del motor configurado, lo cual permite correr el mismo changelog contra distintos motores (por ejemplo MariaDB y PostgreSQL) sin reescribirlo.

### Excepcion: renombrar una columna

Para renombrar una columna **no se usa `renameColumn`: se usa `sql` con `RENAME COLUMN`**, con su `rollback` tambien en `sql` y la precondicion `columnExists` con `onFail: MARK_RAN`.

Por que: en MySQL/MariaDB, Liquibase traduce `renameColumn` a `ALTER TABLE t CHANGE viejo nuevo <columnDataType>`, que redefine la columna entera con solo el tipo y le borra el `NOT NULL` y el `DEFAULT` (por ejemplo, `date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` queda `TIMESTAMP NULL`). En PostgreSQL `renameColumn` es un `RENAME COLUMN` y no pierde nada, por eso las pruebas de `app` (PostgreSQL embebido) no lo detectan. `ALTER TABLE ... RENAME COLUMN ... TO ...` solo cambia el nombre y es la misma sintaxis en PostgreSQL, MySQL 8+ y MariaDB 10.5+, asi que no ata el changeset a un motor.

```yaml
databaseChangeLog:
  - changeSet:
      id: 2026-10-09-renombrar-columna-date-created-facturas
      author: jperez
      comment: Renombra la columna date_created de facturas a fecha_de_creacion
      preConditions:
        onFail: MARK_RAN
        columnExists:
          tableName: facturas
          columnName: date_created
      changes:
        - sql:
            sql: ALTER TABLE facturas RENAME COLUMN date_created TO fecha_de_creacion
      rollback:
        - sql:
            sql: ALTER TABLE facturas RENAME COLUMN fecha_de_creacion TO date_created
```

Si un `renameColumn` ya publicado corrio en MySQL/MariaDB, no se edita: se agrega un changeset nuevo con `dbms: mysql,mariadb` que restaure la definicion original de la columna (tipo, `NOT NULL`, default) tal como la creo su `createTable`. Ejemplo: `app/src/main/resources/db/changelog/changes/2026-10-09-restaurar-not-null-configuraciones.yaml`.

## Excepcion: sacar de su archivo un changeset ya publicado

Un changeset publicado (que ya corrio en alguna base de un ambiente) no se edita ni se borra. La unica excepcion es
esta: **un renombre de tabla o columna hace que el changeset original no pueda correr en una base nueva**, porque
nombra la tabla o la columna vieja y el renombre corre antes que el (por ejemplo, el Core renombra `companies` a
`empresas` y despues el changelog del proyecto inserta en `companies`). Ningun otro motivo (estilo, nombres, datos
distintos) justifica sacarlo.

Antes de sacarlo, ver si alcanza con un changeset puente (abajo): si alcanza, el original no se toca.

### Como se reemplaza

1. **Changeset nuevo con id nuevo** (`{fecha-del-dia}-{descripcion}`, ver [nombres-changesets.md](nombres-changesets.md)),
   que hace lo mismo que el original con los nombres nuevos, en el changelog del modulo dueno de la tabla.
2. **Misma precondicion idempotente** que el original, con `onFail: MARK_RAN` (`not columnExists`, `not tableExists`,
   `sqlCheck` de que la fila no existe, ...). Asi, en una base existente, donde el original ya corrio, lo que crea ya
   existe y el nuevo queda `MARK_RAN`; en una base nueva corre el nuevo. Si el original no tenia precondicion, el
   nuevo la agrega (ver [idempotencia.md](idempotencia.md)).
3. **Se saca el original** de su archivo. Si el archivo queda vacio, se borra junto con su `include` del master o del
   seed. En las bases donde corrio, el original queda registrado en `databasechangelog` sin archivo: Liquibase no lo
   vuelve a correr ni se queja.
4. **Constancia** en dos lugares:
   - La cabecera del archivo nuevo: que original reemplaza (`original -> nuevo`, uno por linea) y por que ya no
     podia correr. Ejemplo: `app/src/main/resources/db/changelog/changes/2026-10-09-insert-empresas-required-data.yaml`.
   - El changelog del Core de esa version (`sm-core-maven/changelog/{version}.md`, seccion de base de datos), con la misma
     lista.
5. **Rollback:** el nuevo lleva el suyo, como todo changeset ([rollback.md](rollback.md)). Se asume que el original
   queda **sin rollback**: su definicion ya no existe, asi que `liquibase rollback` no puede revertirlo en las bases
   donde corrio. Se deja dicho en la cabecera del archivo nuevo.

### Changeset puente

Si la precondicion del original ya lo deja `MARK_RAN` cuando el objeto existe (por ejemplo
`not foreignKeyConstraintExists` o `not tableExists`), no hace falta sacarlo: se agrega **antes del original**, en el
mismo changelog, un changeset puente (`{fecha}-create-*` o `{fecha}-fk-*`) que crea el objeto con los nombres nuevos.

- **Base nueva:** corre el puente; despues el original encuentra el objeto creado y queda `MARK_RAN`.
- **Base existente:** el original ya corrio y el objeto existe (siguio a la tabla o columna renombrada): el puente
  queda `MARK_RAN`.
- **Rollback vacio, con su porque:** Liquibase tambien revierte los changesets `MARK_RAN`, en orden inverso. En una
  base nueva el original quedo `MARK_RAN` despues del puente, asi que se revierte antes y su rollback borra el objeto;
  si el puente tambien lo borrara, fallaria. El rollback del puente es un `output` que lo explica, y un comentario
  sobre el changeset cuenta por que existe.

```yaml
  # Roles y permisos en espanol. core-identity renombra auth_roles.company_id a id_empresa antes de que corra este
  # changelog. En una base nueva este changeset crea la FK y core-multiempresa-fk-auth-roles-company (que nombra
  # company_id) queda MARK_RAN por su precondicion. En una base existente la FK ya existe y este queda MARK_RAN.
  # Su rollback no hace nada: la FK la borra el rollback del original, que se revierte antes.
  - changeSet:
      id: 2026-10-09-fk-auth-roles-id-empresa
      author: TaielMG
      comment: Agrega la FK de auth_roles.id_empresa a companies si todavia no existe
      preConditions:
        - onFail: MARK_RAN
        - columnExists:
            tableName: auth_roles
            columnName: id_empresa
        - not:
            - foreignKeyConstraintExists:
                foreignKeyName: fk_auth_roles_company
      changes:
        - addForeignKeyConstraint:
            baseTableName: auth_roles
            baseColumnNames: id_empresa
            referencedTableName: companies
            referencedColumnNames: id
            constraintName: fk_auth_roles_company
      rollback:
        - output:
            message: Sin efecto. La FK la borra el rollback de core-multiempresa-fk-auth-roles-company
```

Ejemplos reales en `sm-core-maven/core-multiempresa/src/main/resources/db/changelog/core-multiempresa.yaml`:
`2026-10-09-create-auth-users-companies-fk-auth-usuarios` y `2026-10-09-fk-auth-roles-id-empresa`.

Ver el detalle de esta convencion en cada documento del indice: [Convencion de archivos, id y author](nombres-changesets.md), [Idempotencia y precondiciones](idempotencia.md), [Rollback y cambios destructivos](rollback.md), [Convencion de nombres de constraints e indices](nombres-constraints.md) y [Convencion de orden de columnas](orden-columnas.md).
