# Estrategia de changelogs

## Changelog master

El master productivo es:

```text
app/src/main/resources/db/changelog/db.changelog-master.yaml
```

Spring Boot usa este changelog automaticamente mediante `spring.liquibase.change-log`.
Todo lo incluido en el master debe ser seguro para produccion.

El master contiene:

- estructura de tablas, indices y claves foraneas;
- datos obligatorios para que la aplicacion arranque correctamente;
- catalogos tecnicos requeridos por el codigo, como roles base, empresas base y usuario inicial.
- los permisos y las asociaciones rol-permiso no se cargan por Liquibase; se sincronizan desde Java al iniciar la aplicacion.

El master no contiene:

- datos de ejemplo;
- datos de pruebas;
- usuarios demo;
- registros pensados solo para presentaciones.

## Changelog de seed

El changelog de seed es:

```text
app/src/main/resources/db/changelog/db.changelog-seed.yaml
```

Este archivo incluye primero el master y despues los seeds opcionales. Esto permite ejecutarlo sobre una base vacia o sobre una base donde el master ya corrio.

Los changesets de seed deben tener `context:@seed`. Ese prefijo obliga a ejecutar Liquibase con el contexto `seed`; un `update` sin contexto no debe insertar datos demo.

## Regla para nuevos cambios

- Si el dato es necesario en produccion, va en un archivo bajo `db/changelog/changes/` y se incluye en el master.
- Si el dato es solo para pruebas, demos o desarrollo local, va en un archivo bajo `db/changelog/seeds/` y se incluye solo en `db.changelog-seed.yaml`.
- No editar changesets ya aplicados. Agregar un archivo nuevo y referenciarlo desde el changelog correspondiente.
- Nombre de archivo, id y author: ver [Convencion de archivos, id y author](nombres-changesets.md).
- Idempotencia de DDL: ver [Idempotencia y precondiciones](idempotencia.md). Los `insert` siguen siendo idempotentes con una precondicion (`sqlCheck` u otro check nativo) o apoyandose en una clave unica.
- Rollback: todo changeset lo requiere, sin excepcion. Ver [Rollback y cambios destructivos](rollback.md).
- Nombres de constraints e indices: ver [Convencion de nombres de constraints e indices](nombres-constraints.md).
- Liquibase no debe insertar permisos ni registros en `auth_roles_permisos`, porque esa sincronizacion corresponde a `InicializadorDeBaseService`.
