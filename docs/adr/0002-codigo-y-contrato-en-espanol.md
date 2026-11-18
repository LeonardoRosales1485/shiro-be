# Código, base de datos y contrato HTTP en español

Todo lo que define el proyecto (clases, métodos, variables, comentarios, tablas, columnas, rutas y campos JSON), tanto en `Code` como en el Core, se escribe en español, para cumplir `docs/rules/idioma-y-estilo.md`. Se mantienen en inglés los sufijos técnicos (`Controller`, `Service`, `Repository`, `DTO`, `UseCase`, `Abstract*`), los identificadores fijos del deployer (`com.blue.open`, `open-data-proyect`, `Version`), los nombres impuestos por terceros (Spring, JWT, SSO, hCaptcha, SM3, Crypter), los términos reservados (`uuid`, `status`, `AccessToken`, `RefreshToken`, `Auth`, `Trace`, `Context`, `Request`, `Logging`, `hook`), los sufijos técnicos de infraestructura (`Config`, `Filter`, `Properties`, `Aspect`, `Interceptor`, `Utils`, `Mail`), los nombres de módulo `Kernel` y `Crud`, y los valores de enums persistidos (`SYSADMIN`, nombres de permisos). `Mapper` dejó de ser sufijo técnico: desde Core 3.0.2 no existen mappers ([ADR 0005](0005-proyeccion-en-vez-de-mappers.md)).

La traducción alcanza también lo que el Core le hereda al proyecto:

- **Rutas del ABM**: `POST /paginate` pasa a `POST /paginar`, `POST /save` a `POST /guardar` y `DELETE /{uuid}` a `DELETE /borrar/{uuid}`. `GET /uuid/{uuid}` queda, porque `uuid` es término reservado. El borrado se movió a `/borrar/{uuid}` por decisión del usuario: con un único segmento variable, toda ruta vieja de un segmento (`/paginate`, `/save`, `/login`) coincidía con su patrón y respondía 405 en vez de 404.
- **Campos heredados** de las entidades y DTOs del Core (`username`, `name`, la auditoría `dateCreated`/`idUserCreated`, ...) y **overrides del Core** (`hookSaveAplicarDto` y el resto de los hooks de `AbstractService`, `getSubject()` de las notificaciones, `getName()` de los permisos): se traducen. Solo quedan en inglés los overrides de Spring y de otros terceros.

Qué se llama cómo está en el glosario cerrado viejo → nuevo [`docs/arquitectura/glosario-espanol/README.md`](../arquitectura/glosario-espanol/README.md), que es la fuente única de los renombres y registra, en su sección de casos dudosos, las decisiones tomadas con el usuario.

El cambio es incompatible: sale como **Core 3.3.0**, con corte directo del contrato HTTP (el front propio se actualiza en el mismo release) y renombres de tablas y columnas en changesets nuevos de Liquibase (`renameTable` para tablas y `sql` con `RENAME COLUMN` para columnas), sin editar los changesets ya aplicados. `versionado.md` pide major para un cambio que rompe compatibilidad; sale como minor por decisión explícita del usuario. Antes salen Core 3.1.0 (`@Descripcion` en los códigos de error) y Core 3.2.0 (401 `NO_AUTENTICADO` para requests sin sesión).

## Considered Options

- Traducir solo `Code` y dejar el Core en inglés: se descartó porque la regla de idioma aplica a todo el código.
- Traducir solo los nombres Java y dejar DB y URLs en inglés: se descartó porque el objetivo es un único idioma de punta a punta.
- Dejar en inglés las rutas del ABM y los nombres heredados del Core (`username`, `name`, hooks) como excepción: se descartó porque obligaba a mezclar idiomas dentro de cada DTO y de cada service de `Code`.
- Reescribir los changesets originales: se descartó porque rompe el checksum de las bases existentes.
- Publicarlo como Core 4.0.0 (major, como pide `versionado.md`): se descartó por decisión del usuario.
