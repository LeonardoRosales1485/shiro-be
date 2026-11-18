# Documentación de negocio

Reglas de negocio de Shiro, el gestor de trabajo que reemplaza a Jira en Blue Open Data. Junto con los `NOT NULL` del schema de Liquibase, es la fuente para decidir las validaciones de cada DTO (ver [`rules/entidades-y-dtos.md`](rules/entidades-y-dtos.md)). Si el schema y este documento se contradicen, se pregunta antes de decidir.

La fuente de verdad es el DAF v1.0.0 y la spec técnica aprobada (jerarquía: DAF > spec > plan > código). Este archivo resume solo las reglas que ya tienen código; crece con cada fase del plan.

## Usuarios (`Usuario`)

- Modelo monoempresa (`core-monoempresa`): un usuario tiene roles directos, sin empresas. Los roles hacen de grupos de Shiro.
- `nombreDeUsuario` obligatorio.
- Un usuario nuevo entra sin roles: no ve nada hasta que un administrador le asigna grupos (spec, Spec 2). Por eso no hay rol por defecto.
- Ingreso: `jwt` (sesión web), `basic` (cuentas locales) y `tokens` (MCP). El ingreso con Google se agrega en F2.
- Shiro no tiene registro público ni recuperación de contraseña por email.

## Proyectos (`Proyecto`)

- Crean y editan proyectos el super admin (`SYSADMIN`) y el grupo Administradores (`ADMIN`). Leer requiere sesión; el filtro por permisos de proyecto llega en F2.
- `nombre` obligatorio (hasta 120 caracteres); `descripcion` opcional (hasta 2000).
- `prefijo`: de 2 a 6 letras, se guarda en mayúsculas. No se repite entre proyectos, tampoco con uno archivado (queda reservado), y no se cambia después de crear el proyecto (adenda 3 del DAF).
- Al crear un proyecto se generan sus cuatro tableros: Principal, QA, Análisis y Diseño.
- Nada se borra: no hay `/borrar`; archivar y restaurar llegan en T1.8.

## Tableros, estados y tipos de tarea

Catálogos sembrados por Liquibase (`2026-11-18-catalogos.yaml`), de solo lectura por API (`GET /tipo-tablero/listar` y `GET /tipo-tarea/listar`).

| Tablero | Estados, en orden de columna | Terminado |
|---|---|---|
| Principal | Por hacer, En curso, Bloqueado, En revisión, Hecho | Hecho |
| QA | Por probar, Probando, Bloqueado, Aprobado, Rechazado | Aprobado |
| Análisis | Por analizar, Analizando, Bloqueado, En validación, Aprobado | Aprobado |
| Diseño | Por diseñar, Diseñando, Bloqueado, En revisión, Aprobado | Aprobado |

- El primer estado de cada tablero es el inicial: ahí nace una tarea.
- Una tarea en un estado terminado cuenta para el changelog y como terminada al cerrar un sprint (adenda 3 del DAF).
- Tipos de tarea y nivel en la jerarquía: Épica 1; Historia, Tarea y Bug 2; Subtarea 3. El padre de una tarea tiene nivel menor.

## Roles y permisos

- Los permisos se declaran en código (`PermisoBaseEnum` del Core y `PermisoEnum` del proyecto) y se sincronizan al arrancar. No se crean, editan ni borran por API.
- Roles base: `SYSADMIN` (super admin) y `ADMIN`. Los permisos por proyecto y tablero de Shiro (`shr_regla_permiso`) llegan en F2.
- Un usuario no puede crear un rol ni asignar a otro usuario un rol con permisos que él mismo no tiene (salvo `SYSADMIN`).

## Configuraciones (`Configuracion`)

Módulo heredado de la plantilla; lo usa la trazabilidad del Core (key `DEBUG`).

- Pares clave/valor para cambiar parámetros sin recompilar. `nombre` y `valor` obligatorios.
- `global = true`: pública, se puede leer sin sesión. `global = false`: privada, requiere el permiso `CONFIGURACION_VER`.
- Crear o editar requiere `CONFIGURACION_CREAR`.
- Una key que no existe se lee como pública con `valor` nulo: el código que la consume siempre tiene que tener un valor por defecto.
