# Documentación de negocio

Reglas de negocio del proyecto. Junto con los `NOT NULL` del schema de Liquibase, es la fuente para decidir las validaciones de cada DTO (ver [`rules/entidades-y-dtos.md`](rules/entidades-y-dtos.md)). Si el schema y este documento se contradicen, se pregunta antes de decidir.

Este archivo describe lo que trae el template. Cada proyecto lo reemplaza o amplía con su propio negocio.

## Empresas (`Empresa`)

- El modelo es multi-empresa (`core-multiempresa`): cada usuario pertenece a una o más empresas y tiene roles en cada una.
- Hay dos empresas fijas, creadas por Liquibase:
  - **Sistema** (uuid `1`): sus roles valen en todas las empresas. Nadie puede entrar a ella por un alta pública.
  - **BlueOpenData** (uuid `2`): empresa operativa por defecto. Con `core.multiempresa.habilitada=false` (valor del template) es la que se usa cuando la request no manda `X-Empresa-Uuid`.
- Campos:
  - `nombre`: obligatorio.
  - `descripcion`, `ruta`, `codigo`: opcionales.
  - `cuit`: opcional. Si viene, 11 dígitos (con o sin guiones) y dígito verificador válido.

## Usuarios (`Usuario`)

- `nombreDeUsuario` obligatorio. `email` único entre usuarios activos (`0`) y pendientes de verificación (`-2`).
- Alta pública (`/usuario/publico/registro`): crea el usuario y su empresa; el usuario queda en `-2` hasta verificar el código que recibe por email, y queda como `ADMIN` de la empresa nueva.
- Alta pública en una empresa existente (`/usuario/publico/registro-cliente`): el usuario recibe el rol por defecto de esa empresa.
- Vinculación a otra empresa (`/usuario/publico/vincular/{usuarioUuid}`): solo el propio usuario logeado, con el rol por defecto de la empresa.
- Recuperación de contraseña por código enviado por email.

## Roles y permisos

- Los permisos se declaran en código (`PermisoBaseEnum` del Core y `PermisoEnum` del proyecto) y se sincronizan al arrancar. No se crean, editan ni borran por API.
- Roles base: `SYSADMIN` (todos los permisos) y `ADMIN` (todos salvo los de gestión de empresas y permisos). Los roles propios del proyecto van en `RolEnum`.
- Un usuario no puede crear un rol ni asignar a otro usuario un rol con permisos que él mismo no tiene, ni gestionar roles o usuarios de otra empresa (salvo `SYSADMIN`).
- Cada empresa puede tener un único rol por defecto.

## Configuraciones (`Configuracion`)

- Pares clave/valor para cambiar parámetros sin recompilar. `nombre` y `valor` obligatorios.
- `global = true`: pública, se puede leer sin sesión. `global = false`: privada, requiere el permiso `CONFIGURACION_VER`.
- Crear o editar requiere `CONFIGURACION_CREAR`.
- Una key que no existe se lee como pública con `valor` nulo: el código que la consume siempre tiene que tener un valor por defecto.
- La key `DEBUG` la usa la trazabilidad del Core.
