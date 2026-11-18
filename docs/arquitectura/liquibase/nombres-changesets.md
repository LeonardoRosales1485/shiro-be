# Convencion de archivos, id y author

Regla unica, sin excepciones, para todo archivo nuevo bajo `db/changelog/changes/` y `db/changelog/seeds/`.

## Nombre de archivo

```text
YYYY-MM-DD-descripcion-corta-en-ingles-o-espanol-sin-acentos.yaml
```

Ejemplos:

```text
2026-07-06-add-column-status-facturas.yaml
2026-07-06-create-table-facturas.yaml
```

- La fecha es la fecha en que se crea el archivo, no la fecha estimada de release.
- Si dos archivos del mismo dia tocan lo mismo, se diferencian agregando un sufijo `-2`, `-3`, etc: `2026-07-06-add-column-status-facturas-2.yaml`.
- No reutilizar ni renombrar un archivo ya mergeado a `main`, aunque el cambio no se haya liberado todavia.

## Formato

Todo changeset nuevo se escribe en **YAML** usando los changeTypes nativos de Liquibase (`createTable`, `addColumn`, `insert`, `addForeignKeyConstraint`, etc.). No se usa el formato `--liquibase formatted sql` ni SQL plano en archivos nuevos: ver [Formato obligatorio: YAML](README.md#formato-obligatorio-yaml).

## Granularidad

Un archivo puede contener varios changesets (`changeSet`) solo si pertenecen al mismo cambio logico, por ejemplo: crear una tabla junto con sus indices y su FK, o agregar una columna junto con el backfill de esa columna.

No mezclar en el mismo archivo cambios que no dependen entre si (ej: una migracion de `facturas` y otra de `auth_usuarios` en el mismo archivo).

## id y author

- `author` es el usuario git de quien escribe el changeset (ej: `jperez`).
- `id` es el mismo nombre de archivo, sin la extension `.yaml` (ej: `2026-07-06-add-column-status-facturas`).
- Si el archivo tiene mas de un changeset, cada uno agrega un sufijo numerico al id: `2026-07-06-create-table-facturas-1`, `2026-07-06-create-table-facturas-2`.

Ejemplo completo:

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

## Por que este esquema

- La fecha en el nombre evita colisiones de numeracion entre ramas paralelas de un equipo grande: dos desarrolladores pueden crear un changeset el mismo dia sin coordinarse, y con un sufijo numerico si hace falta.
- Usar el mismo string para nombre de archivo e `id` elimina la ambiguedad de "que id le pongo": es una unica decision, no dos.
- El `author` real (no un author generico de equipo) permite rastrear a quien preguntar sobre un changeset especifico.
- YAML con changeTypes nativos, en vez de SQL plano, es lo que permite correr el mismo changelog contra distintos motores de base de datos (por ejemplo MariaDB y PostgreSQL) sin reescribirlo: ver [Formato obligatorio: YAML](README.md#formato-obligatorio-yaml).
