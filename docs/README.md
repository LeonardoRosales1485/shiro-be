# Documentación de negocio

Reglas de negocio de Shiro, el gestor de trabajo que reemplaza a Jira en Blue Open Data. Junto con los `NOT NULL` del schema de Liquibase, es la fuente para decidir las validaciones de cada DTO (ver [`rules/entidades-y-dtos.md`](rules/entidades-y-dtos.md)). Si el schema y este documento se contradicen, se pregunta antes de decidir.

La fuente de verdad es el DAF v1.0.0 y la spec técnica aprobada (jerarquía: DAF > spec > plan > código). Este archivo resume solo las reglas que ya tienen código; crece con cada fase del plan.

## Usuarios (`Usuario`)

- Modelo monoempresa (`core-monoempresa`): un usuario tiene roles directos, sin empresas. Los roles hacen de grupos de Shiro.
- `nombreDeUsuario` obligatorio.
- Un usuario nuevo entra sin roles: no ve nada hasta que un administrador le asigna grupos (spec, Spec 2). Por eso no hay rol por defecto.
- Ingreso: `jwt` (sesión web), `basic` (cuentas locales) y `tokens` (MCP).
- Ingreso con Google (`POST /usuario/ingresar-con-google`, público): recibe el ID token de Google Identity Services, valida firma, emisor, audiencia (client id configurado), vencimiento, correo verificado y dominio (`hd`), busca el usuario vigente por correo o lo crea sin roles ni contraseña, y responde el JWT del Core como `iniciar-sesion`. Errores: `TOKEN_DE_GOOGLE_INVALIDO` (401), `DOMINIO_DE_GOOGLE_NO_PERMITIDO` (403), `GOOGLE_NO_CONFIGURADO` y `GOOGLE_NO_DISPONIBLE` (503). Un usuario archivado no entra. Se configura con `GOOGLE_CLIENT_ID` y `GOOGLE_DOMINIO`; sin ellos queda deshabilitado.
- Tokens de acceso (MCP): `POST /usuario/crear-token`, `GET /usuario/tokens` y `DELETE /usuario/borrar-token/{uuid}` (endpoints del Core). La base guarda solo el SHA-256 del token y autentica buscando por ese hash; el token se muestra una única vez, en la respuesta de crear-token, y el listado no lo trae. Borrar un token lo revoca: deja de autenticar al instante (`ACCESS_TOKEN_INVALIDO`). Mientras el Core no publique 4.1.0, la lógica vive en Shiro (`AuthTokenConSha256Service`, que reemplaza al bean del Core).
- Administración (T2.5): usuarios (`/usuario/uuid`, `paginar`, `guardar`) y grupos (`/rol/...`) los administran el super admin y los Administradores. Un Administrador no edita, archiva ni cambia los grupos del super admin ni de otros Administradores, y no asigna ni quita los grupos del sistema (`SYSADMIN`, `ADMIN`): eso lo hace solo el super admin (403 `SIN_PERMISO`).
- Grupos de un usuario: `GET /usuario/{uuid}/grupos` y `POST /usuario/{uuid}/grupos` (lista completa; reemplaza la anterior y archiva las asignaciones quitadas).
- Archivar un usuario (`POST /usuario/{uuid}/archivar`): ya no ingresa, sus tokens se desactivan y sus tareas conservan el responsable. Nadie se archiva a sí mismo (`USUARIO_NO_SE_ARCHIVA_A_SI_MISMO`). `POST /usuario/{uuid}/restaurar` lo devuelve; los tokens anteriores siguen desactivados.
- Grupos: el nombre no se repite (`GRUPO_REPETIDO`). `POST /rol/{uuid}/archivar` archiva un grupo; los del sistema no se archivan (`GRUPO_DEL_SISTEMA`). No hay `/borrar` de usuarios ni de grupos.
- Shiro no tiene registro público ni recuperación de contraseña por email.

## Proyectos (`Proyecto`)

- Crean y editan proyectos el super admin (`SYSADMIN`) y el grupo Administradores (`ADMIN`). Leer exige `VER` y archivar o restaurar, `ARCHIVAR` (ver "Permisos por recurso").
- `nombre` obligatorio (hasta 120 caracteres); `descripcion` opcional (hasta 2000).
- `prefijo`: de 2 a 6 letras, se guarda en mayúsculas. No se repite entre proyectos, tampoco con uno archivado (queda reservado), y no se cambia después de crear el proyecto (adenda 3 del DAF).
- Al crear un proyecto se generan sus cuatro tableros: Principal, QA, Análisis y Diseño.
- Archivar (`POST /proyecto/{uuid}/archivar`, con `ARCHIVAR` en el proyecto) arrastra todo lo que hay dentro: tableros, tareas en todos los niveles, comentarios y relaciones, con el mismo id de operación. Restaurar (`POST /proyecto/{uuid}/restaurar`) devuelve lo de esa operación; lo archivado antes por separado queda archivado (adenda 4 del DAF).
- Usuarios del proyecto (`GET /proyecto/{uuid}/usuarios`, con `VER`): el super admin, los Administradores y quienes ven el proyecto o alguno de sus tableros por sus reglas o las de sus grupos, por nombre de usuario. Cualquier usuario del proyecto elige de esta lista al responsable de una tarea (resolución de la propuesta 42).
- Nada se borra: no hay `/borrar`.

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

- Permisos por recurso: ver la sección "Permisos por recurso".
- Modo rápido: para crear alcanzan `tableroUuid` (el tablero que se está viendo), `tipoCodigo` y `titulo` (hasta 255 caracteres). El resto de los campos es opcional.
- La tarea nace en el estado inicial de su tablero, con prioridad `MEDIA` y con quien la crea como informante (si no se indica otro).
- Clave: prefijo del proyecto y número (`SHI-123`). Es única en el proyecto y puede tener saltos. La da el contador del proyecto (`ultimo_numero`), que se bloquea por fila al crear: 50 altas simultáneas reciben 50 claves distintas.
- Al editar no cambian la clave, el proyecto, el estado (se cambia con mover, T1.5) ni el tablero: mandar otro tablero responde `TABLERO_DE_TAREA_NO_SE_CAMBIA` (para llevarla a otro tablero se clona, T1.10).
- Edición simultánea: al editar se manda `fechaDeActualizacion`, la que se leyó (si falta, `FECHA_DE_LECTURA_OBLIGATORIA`). Si la tarea cambió desde entonces (otra edición, un movimiento), responde 409 `TAREA_MODIFICADA`: hay que recargar y volver a intentar. La fila de la tarea se bloquea mientras se compara, así dos guardados simultáneos con la misma fecha no pasan los dos.
- Mover (`POST /tarea/{uuid}/mover`, `estadoUuid`): pasa de cualquier estado a cualquier otro de su tablero (las transiciones estrictas quedan para después de la v1). Un estado de otro tablero responde `ESTADO_DE_OTRO_TABLERO`.
- Tipo inexistente: `TIPO_DE_TAREA_INEXISTENTE`. Tablero inexistente: `RELACION_NO_ENCONTRADA`.
- La descripción y los comentarios se escriben en Markdown; el front los dibuja sin HTML peligroso (resolución de la propuesta 43). Desde el detalle de una épica se agregan Historias y desde una historia, tarea o bug, Subtareas (propuesta 45).
- Prioridades: `MAS_ALTA`, `ALTA`, `MEDIA`, `BAJA`, `MAS_BAJA`. Puntos de 0 a 999. Vencimiento: fecha sin hora (`aaaa-mm-dd`).
- Jerarquía (`padreUuid`): Épica, después Historia, Tarea o Bug, después Subtarea. El padre es del mismo proyecto (si no, `PADRE_DE_OTRO_PROYECTO`) y de un tipo de nivel menor; al editar, el tipo nuevo tiene que seguir siendo de nivel menor que el de sus hijas. Si no, `JERARQUIA_DE_TAREA_INVALIDA`. La tarea responde sus `hijas` vigentes. El padre puede estar en cualquier tablero del mismo proyecto. Una Subtarea tiene siempre padre: al crearla, al editarla y al cambiarle el tipo (adenda 4 del DAF).
- Etiquetas: hasta 20, de hasta 40 caracteres; al guardar, la lista reemplaza a la anterior. Las quitadas quedan con status -1 (nada se borra).
- Clonar (`POST /tarea/{uuid}/clonar`, `tableroUuid` y `copiar`): a cualquier tablero del mismo proyecto (si no, `TABLERO_DE_OTRO_PROYECTO`). Título y tipo siempre. `copiar` elige entre `DESCRIPCION`, `PRIORIDAD`, `ETIQUETAS`, `PUNTOS`, `RESPONSABLE`, `VERSIONES`, `COMENTARIOS` y `SUBTAREAS`; si no viene, descripción, prioridad y etiquetas. El clon no hereda el padre del original. Al clonar una Subtarea se elige si el clon queda como tarea propia (`tipoCodigo`: Historia, Tarea o Bug, sin padre) o como subtarea de otra tarea existente (`padreUuid`); uno solo, si no `CLON_DE_SUBTAREA_SIN_DESTINO` (otro tipo: `TIPO_DE_CLON_INVALIDO`). El clon nace como un alta (clave nueva, estado inicial del tablero de destino, informante = quien clona). Los comentarios copiados conservan autor, fecha y canal; las subtareas se clonan con la misma selección como hijas del clon, en el tablero de destino. `VERSIONES` asigna el clon a las versiones sin publicar del original. Original y clon quedan vinculados (relación `CLONA`: clonada en / clonada de) y cada uno muestra el estado del otro; el clon deja una fila `CLONAR` en su historial.
- Archivar (`POST /tarea/{uuid}/archivar`): la tarea y sus hijas vigentes, en todos los niveles, pasan a status -1 con el mismo id de operación (`shr_operacion_archivo`). Cada una deja su fila `ARCHIVAR` en el historial con la clave de la tarea raíz. Lo archivado no se mueve, no se edita y no sirve como padre (`RELACION_NO_ENCONTRADA`).
- Restaurar (`POST /tarea/{uuid}/restaurar`): vuelve lo que se archivó en esa misma operación; lo archivado antes por separado queda archivado. Si el padre sigue archivado responde `PADRE_ARCHIVADO` ("Restaurá primero SHI-12."). Restaurar es una operación nueva y deja filas `RESTAURAR`.
- Nada se borra: no hay `/borrar`. Todas las tablas `shr_` tienen `id_operacion_archivo`.

## Backlog

- `GET /backlog/proyecto/{uuid}` (exige `VER` en el proyecto): las tareas vigentes del proyecto de todos los tableros y de todos los tipos (épicas, historias, tareas, bugs y subtareas), sin sprint, en un único orden, el mismo para todos. Las que nadie ordenó todavía van al final, por número.
- `POST /backlog/ordenar` (exige `EDITAR` en el proyecto): las tareas recibidas van primero, en ese orden; las demás quedan después en su orden anterior. Una tarea que no está en el backlog responde `TAREA_FUERA_DEL_BACKLOG`. Deja una fila `ORDENAR_BACKLOG` en el historial del proyecto.

## Sprints (`Sprint`)

- Son del proyecto e incluyen tareas de todos los tableros. ABM en `/sprint` (nombre y fechas de inicio y fin, sin hora; fin no anterior al inicio: `SPRINT_FECHAS_INVALIDAS`). Leer exige `VER` en el proyecto y el resto, `EDITAR`.
- Ciclo: se crea `PLANIFICADO`; `POST /sprint/{uuid}/asignar` y `/quitar` mueven historias, tareas y bugs del proyecto entre el backlog y el sprint (`TAREA_FUERA_DEL_BACKLOG` si no corresponde; `SPRINT_CERRADO` si ya cerró); `POST /sprint/{uuid}/iniciar` lo activa (uno solo activo por proyecto: `SPRINT_ACTIVO_EXISTENTE`, con índice único parcial en la base); `POST /sprint/{uuid}/cerrar` con destino `BACKLOG` o `SPRINT` (otro planificado del proyecto): lo terminado queda en el sprint cerrado y lo pendiente va al destino.
- `POST /sprint/{uuid}/archivar`: el activo no se archiva (`SPRINT_ACTIVO_NO_SE_ARCHIVA`); las tareas de uno planificado vuelven al backlog; las de uno cerrado quedan registradas en él.
- Cada movimiento de tarea deja una fila `EDITAR` (campo `sprint`) en su historial; el sprint registra sus cambios de estado.
- Seed: Sprint 1 del proyecto Shiro, planificado.

## Versiones (`Version`)

- Son del proyecto. ABM en `/version` (nombre). Leer exige `VER` en el proyecto, cambiar `EDITAR` y archivar o restaurar `ARCHIVAR`.
- `POST /version/{uuid}/asignar` y `/quitar`: tareas vigentes del mismo proyecto (`TAREA_DE_OTRO_PROYECTO`); una tarea puede estar en varias versiones y la tarea muestra sus `versiones`.
- `POST /version/{uuid}/publicar` con la fecha que elige quien publica (hoy si no viene). Una versión publicada no cambia: no se edita, no cambian sus tareas y su changelog queda guardado (`VERSION_PUBLICADA`).
- `GET /version/{uuid}/changelog` responde Markdown: título, fecha de publicación (o "Sin publicar.") y las tareas terminadas agrupadas por tipo, en el orden de los tipos.
- `POST /version/{uuid}/archivar` y `/restaurar`.
- Clonar con `VERSIONES` asigna el clon a las versiones sin publicar del original.
- Seed: versión 1.0 del proyecto Shiro, sin publicar.

- Tareas de una versión (`GET /version/{uuid}/tareas`, con `VER`): las vigentes asignadas, terminadas o no, con su tipo, tablero y estado, por orden de tipo y número. Las usa el detalle de versiones del front (T6.3).

- Cierre de un sprint (`GET /sprint/{uuid}/cierre`, con `VER`): quién lo cerró, cuándo y, por cada tarea que tenía, su estado y puntos al cerrar y su destino (`TERMINADA`, `BACKLOG` o `SPRINT` con el nombre del sprint). Restaurar un sprint archivado: `POST /sprint/{uuid}/restaurar` (con `EDITAR`), vuelve con el estado que tenía.
- `VersionDTO` trae `cantidadDeTareas` (vigentes) y `publicadaPorNombre`.

## Búsqueda

- `POST /tarea/paginar` con el parámetro `texto` (en `parametros`) es la búsqueda de tareas: texto en español sobre la clave, el título y la descripción (con raíces: "tablero" encuentra "tableros"), o la clave exacta, combinable con los filtros. Respeta los permisos como cualquier listado. Usa una columna `tsvector` generada con índice GIN.
- `POST /archivo/paginar` busca lo archivado de cualquier tipo (tareas, proyectos, sprints, versiones y usuarios), con filtro por tipo y texto: aparece lo que el usuario puede archivar (el tablero de la tarea o el proyecto); los usuarios, solo para administradores. Lo más reciente primero. Cada elemento se restaura en el endpoint de su tipo.

- Parámetros de `/tarea/paginar` además de `texto`: `version` (uuid: las tareas asignadas a esa versión) y `etiqueta` (las que tienen esa etiqueta, sin distinguir mayúsculas). Los archivados (`/archivo/paginar`) traen `archivadoPorNombre`.

## Comentarios (`Comentario`)

- ABM de `/comentario` sin borrar. El listado de una tarea es `POST /comentario/paginar` filtrado por `tareaUuid`.
- El autor es quien comenta, el canal el del request y la fecha, ahora. Los copiados al clonar o importados conservan autor y fecha originales.
- Solo el autor edita su comentario (si no, 403 `SIN_PERMISO`); al editar solo cambia el cuerpo (Markdown, hasta 20000 caracteres).
- Cada comentario deja una fila `COMENTAR` en el historial de la tarea.

## Relaciones (`Relacion`)

- `POST /relacion/crear` con origen, destino y tipo: `BLOQUEA`, `RELACIONA` o `DUPLICA`. `CLONA` solo la crea clonar (`TIPO_DE_RELACION_NO_PERMITIDO`).
- Se guarda un sentido y se lee desde las dos tareas con `GET /tarea/{uuid}/relaciones`: bloquea / bloqueada por, se relaciona con, duplica / duplicada por, clonada en / clonada de, con el estado de la otra tarea.
- Una tarea no se relaciona consigo misma (`RELACION_CONSIGO_MISMA`) y la misma relación no se repite en ningún sentido (`RELACION_YA_EXISTE`). Las dos tareas son del mismo proyecto (`RELACION_ENTRE_PROYECTOS`).
- Si una de las tareas se archiva, el vínculo queda y la muestra archivada. `POST /relacion/{uuid}/archivar` la saca de las dos tareas.
- Crear y archivar dejan una fila `RELACIONAR` en el historial de las dos tareas.

## Historial (`Historial`)

- Toda escritura deja su fila en `shr_historial` en la misma transacción: entidad, uuid, tipo de acción, campo, valor anterior y nuevo, usuario, canal (`WEB`, `MCP` o `IMPORTACION`) y fecha. El usuario de la aplicación solo puede insertar y leer en esa tabla.
- Acciones: `CREAR` (valor nuevo: la clave o el prefijo), `EDITAR` (una fila por campo cambiado), `MOVER` (campo `estado`), `CREAR_SUBTAREA` (en el historial del padre, valor nuevo: la clave de la subtarea). `ARCHIVAR` y `RESTAURAR` (con su operación). `COMENTAR`, `RELACIONAR` y `CLONAR` (en el clon, valor nuevo: la clave del original).
- Campos que sigue la tarea: título, descripción, tipo, prioridad, puntos, vencimiento, responsable, informante, padre y etiquetas. El proyecto: nombre y descripción.
- `GET /tarea/{uuid}/historial` responde lo más nuevo primero; `?canal=MCP` filtra por canal. Sirve también para tareas archivadas.
- El canal lo marca el filtro de `/mcp` (T3.1) en el atributo `shiro.canal` del request; sin marca es `WEB`.

## Permisos por recurso (`ReglaPermiso`)

- Una regla dice que un grupo (rol) o un usuario tiene permitido o negado un nivel (`VER`, `EDITAR`, `ARCHIVAR`) sobre un proyecto o un tablero. ABM en `/regla-permiso` (más `POST /regla-permiso/{uuid}/archivar`), solo para el super admin y los Administradores. No se repite el mismo sujeto, recurso y nivel (`REGLA_DE_PERMISO_REPETIDA`); el sujeto y el recurso tienen que coincidir con su tipo (`REGLA_DE_PERMISO_INVALIDA`).
- Resolución (Spec 2, `ResolutorDePermisosDeRecurso`): el super admin y los Administradores pueden todo. Si no: sobre el tablero, la regla del usuario decide; si no hay, entre las de sus grupos gana negar y, si alguna permite, permitido. Si el tablero no decide, lo mismo sobre el proyecto. Sin reglas, negado.
- `EDITAR` y `ARCHIVAR` permitidos implican `VER`; negar `EDITAR` o `ARCHIVAR` no niega `VER`. `EDITAR` y `ARCHIVAR` son independientes entre sí (la spec manda sobre el prototipo, que los mostraba acumulativos).
- Las reglas del usuario se leen una vez por request.
- Qué exige cada endpoint de F1 (T2.4): leer (uuid, historial, relaciones, comentarios) exige `VER` en el tablero o el proyecto; crear y editar tareas, moverlas, comentar y relacionar (en el tablero de origen; `VER` en el de destino) exige `EDITAR`; archivar y restaurar tareas y proyectos exige `ARCHIVAR`; clonar exige `VER` en el original y `EDITAR` en el destino. Sin el nivel: 403 `SIN_PERMISO`. Los listados (`paginar` de proyectos, tareas y comentarios) traen solo lo visible: un proyecto se ve si se ve el proyecto o alguno de sus tableros. Crear y editar proyectos, y las reglas de permiso, siguen siendo solo para el super admin y los Administradores.
- Seed: el grupo `CLIENTE` ve el proyecto Shiro.

## Importador de Jira (`Importacion`)

- Solo el super admin y los Administradores importan. Se conecta con la URL del sitio de Jira, el correo de una cuenta y un token de API de esa cuenta; el token se guarda cifrado (`SHIRO_CIFRADO_FRASE`) y nunca se devuelve. Sin frase de cifrado el importador está deshabilitado.
- La URL tiene que ser un sitio https de Jira Cloud (`*.atlassian.net`); si no, `JIRA_URL_NO_PERMITIDA`.
- El administrador elige el prefijo del proyecto en Shiro (2 a 6 letras), que no puede usar otro proyecto. Si esa clave de Jira ya se importó, se reusa el mismo proyecto y hay que usar su prefijo (`IMPORTACION_CON_OTRO_PREFIJO`).
- Al conectar, Shiro responde los tableros, estados (con si Jira los cuenta como terminados), tipos (con el nivel que tendrían en Shiro) y usuarios del proyecto de Jira. Cada usuario se vincula por correo con el de Shiro.
- El mapeo lleva cada tablero de Jira a un tipo de tablero de Shiro (principal o sub-tablero; varios pueden ir al mismo), cada estado de Jira de ese tablero a un estado de ese tipo de tablero, y cada tipo de Jira a un tipo de Shiro. Un estado o tipo sin equivalente se puede crear (`crear: true`): se suma al catálogo con el nombre de Jira (se reusa uno con el mismo nombre) y aparece en todos los proyectos. El mapeo fija también cuántos días duran los sprints de Jira sin fechas. Solo se importan las tareas de los tableros mapeados.
- La ejecución corre en segundo plano (una a la vez): crea el proyecto con sus tableros si no existe y trae versiones, sprints, tareas, subtareas, etiquetas y comentarios. Las tareas importadas llevan la clave `J-PREFIJO-número de Jira` (JSIM-7 queda `J-SIM-7`) y toman un número del contador del proyecto, así que las nuevas siguen `PREFIJO-n` sin chocar. Si la clave `J-` ya existe, se usa la clave de Jira entera (`J-SIM-OLD-7`) con una advertencia. Todo queda en el historial con canal IMPORTACION.
- Lo que no se puede importar no corta la ejecución: queda en las fallas del resumen (tipo o estado sin mapear, subtarea sin padre importado). Los cambios forzados quedan como advertencias (otra clave, sin padre, sprint activo repetido).
- Cada persona de Jira sin cuenta en Shiro queda como un usuario sin acceso (sin contraseña ni grupos) llamado «Usuario importado» y su nombre de Jira, con su correo si Jira lo muestra: es responsable de sus tareas y autor de sus comentarios. Si después entra con Google con ese correo, entra como ese usuario.
- Reimportar (volver a ejecutar, o conectar de nuevo con la misma clave) actualiza lo ya importado sin duplicar: cada elemento de Jira (también los usuarios creados) queda registrado en `shr_mapeo_jira`. Una tarea no cambia de tablero; los comentarios ya importados no se vuelven a traer.
- El resumen tiene las tareas creadas y actualizadas (y por tipo), los comentarios nuevos, los sprints, las versiones, las fallas, las advertencias y los usuarios creados sin acceso.

## Roles y permisos

- Los permisos se declaran en código (`PermisoBaseEnum` del Core y `PermisoEnum` del proyecto) y se sincronizan al arrancar. No se crean, editan ni borran por API.
- Roles base: `SYSADMIN` (super admin) y `ADMIN`. Los permisos por proyecto y tablero de Shiro (`shr_regla_permiso`) llegan en F2.
- Un usuario no puede crear un rol ni asignar a otro usuario un rol con permisos que él mismo no tiene (salvo `SYSADMIN`).

## MCP

- Servidor MCP sin estado en `/api/mcp` (Streamable HTTP). Exige `Authorization: Bearer <token de acceso personal>` (protocolo `tokens`): sin token, con un JWT o con un token revocado o de un usuario archivado responde 401 antes de llegar a las herramientas (`FiltroDeMcp`). Los cambios hechos por MCP quedan en el historial con canal `MCP`.
- Herramientas (T3.2): `listar_proyectos`, `ver_tablero`, `buscar_tareas`, `obtener_tarea`, `ver_historial`, `crear_tarea`, `editar_tarea`, `mover_tarea`, `clonar_tarea`, `comentar_tarea` y `relacionar_tareas`. Cada una llama al mismo servicio que la API (mismos permisos, validaciones e historial). Las tareas se identifican por clave (`SHI-123`), los proyectos por prefijo, los tableros por código, los estados por nombre y los usuarios por nombre de usuario, sin distinguir mayúsculas. Responden `{resumen, datos}`; las listas vienen de a 50. Los errores llegan como error de la herramienta con el mensaje para el usuario ("No tenés permiso para editar en el tablero.").
- `crear_tarea` pide el proyecto además de tipo y título (el tablero es PRINCIPAL si no se indica). `editar_tarea` cambia solo los datos que se mandan.
- Archivo y administración (T3.3): `archivar` y `restaurar` (TAREA por clave, PROYECTO por prefijo, USUARIO por nombre de usuario), `administrar_proyecto`, `administrar_usuarios`, `administrar_grupos` y `administrar_permisos` (sujeto `usuario:nombre` o `grupo:nombre`, recurso `SHI` o `SHI/QA`). La administración es solo para el super admin y los Administradores. Archivar un proyecto o un usuario, asignar grupos, archivar un grupo y crear o archivar reglas de permiso exigen `confirmar: true`; sin eso no cambian nada y responden la vista previa ("Se archivarían 1 proyecto, 4 tableros y 212 tareas. Repetí la llamada con confirmar: true para hacerlo.").
- Planificación (T5.5): `ver_backlog`, `ordenar_backlog`, `gestionar_sprint` (crear, asignar, quitar, iniciar y cerrar; los sprints se nombran por proyecto y nombre) y `gestionar_version` (crear, asignar, quitar, changelog y publicar), además de `buscar_archivados`. Cerrar un sprint y publicar una versión exigen `confirmar: true`; sin eso responden qué pasaría (cuántas tareas quedan y cuántas se mueven; cuántas entran al changelog).

## Configuraciones (`Configuracion`)

Módulo heredado de la plantilla; lo usa la trazabilidad del Core (key `DEBUG`).

- Pares clave/valor para cambiar parámetros sin recompilar. `nombre` y `valor` obligatorios.
- `global = true`: pública, se puede leer sin sesión. `global = false`: privada, requiere el permiso `CONFIGURACION_VER`.
- Crear o editar requiere `CONFIGURACION_CREAR`.
- Una key que no existe se lee como pública con `valor` nulo: el código que la consume siempre tiene que tener un valor por defecto.
