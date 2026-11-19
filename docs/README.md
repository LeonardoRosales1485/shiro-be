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

## Tareas (`Tarea`)

- Por ahora crear, editar y leer requiere sesión; los permisos por proyecto (ver, editar) llegan en F2.
- Modo rápido: para crear alcanzan `tableroUuid` (el tablero que se está viendo), `tipoCodigo` y `titulo` (hasta 255 caracteres). El resto de los campos es opcional.
- La tarea nace en el estado inicial de su tablero, con prioridad `MEDIA` y con quien la crea como informante (si no se indica otro).
- Clave: prefijo del proyecto y número (`SHI-123`). Es única en el proyecto y puede tener saltos. La da el contador del proyecto (`ultimo_numero`), que se bloquea por fila al crear: 50 altas simultáneas reciben 50 claves distintas.
- Al editar no cambian la clave, el proyecto, el estado (se cambia con mover, T1.5) ni el tablero: mandar otro tablero responde `TABLERO_DE_TAREA_NO_SE_CAMBIA` (para llevarla a otro tablero se clona, T1.10).
- Edición simultánea: al editar se manda `fechaDeActualizacion`, la que se leyó (si falta, `FECHA_DE_LECTURA_OBLIGATORIA`). Si la tarea cambió desde entonces (otra edición, un movimiento), responde 409 `TAREA_MODIFICADA`: hay que recargar y volver a intentar. La fila de la tarea se bloquea mientras se compara, así dos guardados simultáneos con la misma fecha no pasan los dos.
- Mover (`POST /tarea/{uuid}/mover`, `estadoUuid`): pasa de cualquier estado a cualquier otro de su tablero (las transiciones estrictas quedan para después de la v1). Un estado de otro tablero responde `ESTADO_DE_OTRO_TABLERO`.
- Tipo inexistente: `TIPO_DE_TAREA_INEXISTENTE`. Tablero inexistente: `RELACION_NO_ENCONTRADA`.
- Prioridades: `MAS_ALTA`, `ALTA`, `MEDIA`, `BAJA`, `MAS_BAJA`. Puntos de 0 a 999. Vencimiento: fecha sin hora (`aaaa-mm-dd`).
- Jerarquía (`padreUuid`): Épica, después Historia, Tarea o Bug, después Subtarea. El padre es del mismo proyecto (si no, `PADRE_DE_OTRO_PROYECTO`) y de un tipo de nivel menor; al editar, el tipo nuevo tiene que seguir siendo de nivel menor que el de sus hijas. Si no, `JERARQUIA_DE_TAREA_INVALIDA`. La tarea responde sus `hijas` vigentes. El padre puede estar en otro tablero del mismo proyecto y una Subtarea puede no tener padre (la spec no lo exige).
- Etiquetas: hasta 20, de hasta 40 caracteres; al guardar, la lista reemplaza a la anterior. Las quitadas quedan con status -1 (nada se borra).
- Nada se borra: no hay `/borrar`; archivar y restaurar llegan en T1.8.

## Historial (`Historial`)

- Toda escritura deja su fila en `shr_historial` en la misma transacción: entidad, uuid, tipo de acción, campo, valor anterior y nuevo, usuario, canal (`WEB`, `MCP` o `IMPORTACION`) y fecha. El usuario de la aplicación solo puede insertar y leer en esa tabla.
- Acciones: `CREAR` (valor nuevo: la clave o el prefijo), `EDITAR` (una fila por campo cambiado), `MOVER` (campo `estado`), `CREAR_SUBTAREA` (en el historial del padre, valor nuevo: la clave de la subtarea). `ARCHIVAR`, `RESTAURAR`, `COMENTAR`, `RELACIONAR` y `CLONAR` llegan con T1.8 a T1.10.
- Campos que sigue la tarea: título, descripción, tipo, prioridad, puntos, vencimiento, responsable, informante, padre y etiquetas. El proyecto: nombre y descripción.
- `GET /tarea/{uuid}/historial` responde lo más nuevo primero; `?canal=MCP` filtra por canal. Sirve también para tareas archivadas.
- El canal lo marca el filtro de `/mcp` (T3.1) en el atributo `shiro.canal` del request; sin marca es `WEB`.

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
