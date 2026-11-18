# Convencion de orden de columnas

Este documento establece el orden estandar de columnas para todas las tablas en el schema de la base de datos.

## Orden de columnas

1. **Primary Key**
   - `id` (`BIGINT`, `autoIncrement: true`)

2. **Dato que identifica visualmente la entidad**
   - El dato mas importante para el usuario al ver un registro: `nombre`, `nombre_de_usuario`, `codigo`, etc.

3. **Estado**
   - `status` (`SMALLINT`, `defaultValueNumeric: 0`)
   - Indica si el registro esta activo (`0`) o eliminado (`-1`).

4. **Identificador unico**
   - `uuid` (`CHAR(36)`, `UNIQUE`)

5. **Datos especificos de la tabla**
   - Campos propios de la entidad que no pertenecen a `EntidadBase`.
   - Primero datos cortos y simples: `VARCHAR`, `INT`, `SMALLINT`, `BOOLEAN`.
   - Despues datos largos: `CLOB`/`TEXT`, `VARCHAR` grandes.

6. **Relaciones**
   - Referencias a otras tablas: `id_empresa`, `id_usuario`, etc.
   - Incluyen los indices y constraints asociados.

7. **Auditoria**
   - `id_usuario_de_creacion`
   - `id_usuario_de_actualizacion`
   - `id_usuario_de_borrado`
   - `fecha_de_creacion`
   - `fecha_de_actualizacion`
   - `fecha_de_borrado`

## Campos heredados de EntidadBase

Los siguientes campos se gestionan en la entidad Java y siguen este patron en la base de datos:

- `uuid`: se genera en Java mediante `UUID.randomUUID()` cuando no se define explicitamente.
- `status`: valor por defecto `0`.
- `fecha_de_creacion`, `fecha_de_actualizacion`: se gestionan con `@CreationTimestamp` y `@UpdateTimestamp` (el update automatico de `fecha_de_actualizacion` lo hace Hibernate en cada escritura; el changeset no necesita logica de motor especifica como triggers para esto).
- `fecha_de_borrado`: se establece al eliminar logicamente un registro.
- `idUsuarioDeCreacion`, `idUsuarioDeActualizacion`, `idUsuarioDeBorrado`: se asignan segun el contexto de operacion.

## Ejemplo

```yaml
databaseChangeLog:
  - changeSet:
      id: 2026-07-06-create-table-example-table
      author: jperez
      comment: Crea tabla tabla_de_ejemplo
      changes:
        - createTable:
            tableName: tabla_de_ejemplo
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: nombre
                  type: VARCHAR(255)
              - column:
                  name: status
                  type: SMALLINT
                  defaultValueNumeric: 0
                  constraints:
                    nullable: false
              - column:
                  name: uuid
                  type: CHAR(36)
                  constraints:
                    nullable: false
              - column:
                  name: codigo
                  type: VARCHAR(255)
              - column:
                  name: activo
                  type: INT
                  constraints:
                    nullable: false
              - column:
                  name: descripcion
                  type: CLOB
              - column:
                  name: id_empresa
                  type: BIGINT
              - column:
                  name: id_usuario_de_creacion
                  type: BIGINT
              - column:
                  name: id_usuario_de_actualizacion
                  type: BIGINT
              - column:
                  name: id_usuario_de_borrado
                  type: BIGINT
              - column:
                  name: fecha_de_creacion
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: fecha_de_actualizacion
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: fecha_de_borrado
                  type: TIMESTAMP
        - addUniqueConstraint:
            tableName: tabla_de_ejemplo
            columnNames: uuid
            constraintName: uk_tabla_de_ejemplo_uuid
        - addForeignKeyConstraint:
            baseTableName: tabla_de_ejemplo
            baseColumnNames: id_empresa
            referencedTableName: empresas
            referencedColumnNames: id
            constraintName: fk_tabla_de_ejemplo_empresa
      rollback:
        - dropTable:
            tableName: tabla_de_ejemplo
```
