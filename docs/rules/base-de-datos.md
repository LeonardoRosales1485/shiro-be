# Base de Datos (Liquibase)

- La base de datos nunca se edita a mano. Todo cambio de esquema se hace únicamente vía Liquibase.
- Cada proyecto debe contar con: schema básico, configuraciones para levantar la base, y seeds de ejemplo para pruebas.
- La aplicación debe poder ser completamente funcional sin tocar la DB a mano; se deben crear las reglas necesarias en liquibase para que la aplicación funcione correctamente.
- Toda entidad o funcionalidad nueva debe incluir seeds de ejemplo en liquibase, para poder mostrarla y probarla sin necesidad de cargar datos a mano.
- Se debe respetar la documentación de liquibase en [`docs/arquitectura/liquibase/README.md`](../arquitectura/liquibase/README.md).
- Un changeset publicado no se edita ni se borra. Solo se puede sacar de su archivo cuando un renombre de tabla o columna hace que ya no pueda correr en una base nueva, y se reemplaza como indica "Excepcion: sacar de su archivo un changeset ya publicado" en [`liquibase/README.md`](../arquitectura/liquibase/README.md) (o se resuelve con un changeset puente, en la misma sección).
- Para renombrar una columna no se usa `renameColumn`: se usa `sql` con `ALTER TABLE ... RENAME COLUMN ... TO ...`, porque en MySQL/MariaDB `renameColumn` borra el `NOT NULL` y el default. Ver "Excepcion: renombrar una columna" en [`docs/arquitectura/liquibase/README.md`](../arquitectura/liquibase/README.md).

## Changelogs por módulo

- Cada módulo del Core trae su propio changelog dentro del jar (`db/changelog/core-*.yaml`), con las tablas que ese módulo necesita y ninguna más.
- El `db.changelog-master.yaml` del proyecto incluye únicamente los changelogs de los módulos que el proyecto declara en su `pom.xml`. Dejar de usar un módulo es borrar su dependencia y su `include`.
- **Ningún módulo toca tablas de otro.** Si un módulo necesita extender una tabla ajena (por ejemplo la FK de `auth_roles.id_empresa` hacia `empresas`), lo hace en un changeset propio.
- Las columnas específicas del proyecto sobre una tabla del Core (por ejemplo `empresas.cuit`) van en el changelog del proyecto, nunca en el del módulo.
