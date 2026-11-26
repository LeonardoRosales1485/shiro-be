# Catálogo de permisos

Generado por la suite completa del backend (`CatalogoDePermisos`, MJ-05 del plan de mejoras) a partir del contrato `docs/api/openapi.yaml`, versión 1.0.55. No se edita a mano.

- `SYSADMIN, ADMIN` u otro rol o permiso: lo exige `@Contrato` antes de entrar al endpoint.
- `NIVEL@RECURSO` (por ejemplo `EDITAR@TABLERO`): exige sesión y ese nivel sobre el proyecto o el tablero del elemento, según las reglas de permiso (`ResolutorDePermisosDeRecurso`). El super admin y los Administradores pueden todo.
- `AUTENTICADO`: alcanza con la sesión. `PUBLICO`: no pide sesión.

| Método | Ruta | Permiso | Caso de uso | Resumen | Aclaración |
| --- | --- | --- | --- | --- | --- |
| POST | `/archivo/paginar` | `ARCHIVAR@PROYECTO|TABLERO` | CU-ARC-01 | Busca lo archivado de cualquier tipo sobre lo que el usuario tiene permiso de archivo | lista solo lo archivado sobre lo que tiene ARCHIVAR; los usuarios, solo administradores |
| GET | `/archivo/{tipo}/{uuid}/vista-previa` | `ARCHIVAR@PROYECTO|TABLERO` | CU-ARC-02 | Vista previa de un archivado: qué es, dónde estaba, qué se restaura con él y qué queda archivado | el tablero de una tarea, el proyecto en los demás; un usuario, solo administradores |
| POST | `/comentario/guardar` | `EDITAR@TABLERO` | CU-COM-03 | Comenta una tarea, o edita el cuerpo de un comentario propio | el tablero de la tarea; editar, solo el autor |
| POST | `/comentario/paginar` | `VER@TABLERO` | CU-COM-02 | Lista comentarios con filtros y orden, por ejemplo los de una tarea | lista solo los comentarios de tareas visibles |
| GET | `/comentario/uuid/{uuid}` | `VER@TABLERO` | CU-COM-01 | Obtiene un comentario | el tablero de la tarea |
| POST | `/configuracion/guardar` | `CONFIGURACION_CREAR` | CU-CON-04 | Crea o edita configuraciones (busca cada una por su clave) |  |
| GET | `/configuracion/listar` | `PUBLICO` | CU-CON-01 | Lista las configuraciones visibles para quien consulta |  |
| POST | `/configuracion/obtener-varias` | `PUBLICO` | CU-CON-03 | Obtiene varias configuraciones por sus claves |  |
| GET | `/configuracion/obtener/{clave}` | `PUBLICO` | CU-CON-02 | Obtiene una configuración por su clave |  |
| POST | `/importacion/conectar` | `SYSADMIN, ADMIN` | CU-IMP-01 | Conecta con un proyecto de Jira (guarda el token cifrado) y responde sus tableros, estados, tipos y usuarios para mapear |  |
| POST | `/importacion/paginar` | `SYSADMIN, ADMIN` | CU-IMP-06 | Lista las importaciones de Jira con filtros y orden |  |
| POST | `/importacion/proyectos-de-jira` | `SYSADMIN, ADMIN` | CU-IMP-08 | Prueba la conexión con Jira y lista los proyectos visibles con esas credenciales (no guarda el token) |  |
| GET | `/importacion/uuid/{uuid}` | `SYSADMIN, ADMIN` | CU-IMP-05 | Obtiene una importación de Jira por su uuid (estado, mapeo y resumen; nunca el token) |  |
| POST | `/importacion/{uuid}/cancelar` | `SYSADMIN, ADMIN` | CU-IMP-07 | Cancela una importación en curso: se corta en el próximo control, no queda nada de esa ejecución y pasa a CANCELADA |  |
| GET | `/importacion/{uuid}/catalogo` | `SYSADMIN, ADMIN` | CU-IMP-02 | Vuelve a leer de Jira los tableros, estados, tipos y usuarios de una importación |  |
| POST | `/importacion/{uuid}/ejecutar` | `SYSADMIN, ADMIN` | CU-IMP-04 | Ejecuta la importación en segundo plano (reimportar actualiza sin duplicar); se consulta su estado hasta que termine |  |
| POST | `/importacion/{uuid}/mapeo` | `SYSADMIN, ADMIN` | CU-IMP-03 | Guarda el mapeo de tableros, estados y tipos de Jira a los de Shiro |  |
| POST | `/permiso/paginar` | `PERMISO_VER` | CU-PER-01 | Lista los permisos del sistema con filtros y orden |  |
| GET | `/permiso/uuid/{uuid}` | `SYSADMIN` | CU-PER-02 | Obtiene un permiso del sistema |  |
| POST | `/proyecto/guardar` | `SYSADMIN, ADMIN` | CU-PRY-03 | Crea un proyecto con sus cuatro tableros, o edita su nombre y descripción |  |
| POST | `/proyecto/paginar` | `VER@PROYECTO` | CU-PRY-02 | Lista los proyectos con filtros y orden | lista solo los proyectos visibles |
| GET | `/proyecto/uuid/{uuid}` | `VER@PROYECTO` | CU-PRY-01 | Obtiene un proyecto con sus tableros | o alguno de sus tableros |
| POST | `/proyecto/{uuid}/archivar` | `ARCHIVAR@PROYECTO` | CU-PRY-04 | Archiva un proyecto con sus tableros, tareas, comentarios y relaciones (exige ARCHIVAR en el proyecto) |  |
| GET | `/proyecto/{uuid}/backlog` | `VER@PROYECTO` | CU-BKL-01 | Backlog de un proyecto: tareas sin sprint de todos los tableros y tipos, en su orden único |  |
| POST | `/proyecto/{uuid}/ordenar-backlog` | `EDITAR@PROYECTO` | CU-BKL-02 | Ordena el backlog de un proyecto (un único orden para todos) |  |
| POST | `/proyecto/{uuid}/restaurar` | `ARCHIVAR@PROYECTO` | CU-PRY-05 | Restaura un proyecto archivado con lo que se archivó junto con él (exige ARCHIVAR en el proyecto) |  |
| GET | `/proyecto/{uuid}/usuarios` | `VER@PROYECTO` | CU-PRY-06 | Usuarios con acceso a un proyecto, para elegir el responsable de una tarea (exige VER en el proyecto) |  |
| POST | `/regla-permiso/guardar` | `SYSADMIN, ADMIN` | CU-RPE-03 | Crea o edita una regla de permiso de un grupo o usuario sobre un proyecto o tablero |  |
| POST | `/regla-permiso/paginar` | `SYSADMIN, ADMIN` | CU-RPE-02 | Lista reglas de permiso con filtros y orden |  |
| GET | `/regla-permiso/uuid/{uuid}` | `SYSADMIN, ADMIN` | CU-RPE-01 | Obtiene una regla de permiso |  |
| POST | `/regla-permiso/{uuid}/archivar` | `SYSADMIN, ADMIN` | CU-RPE-04 | Archiva una regla de permiso |  |
| POST | `/relacion/crear` | `EDITAR@TABLERO` | CU-REL-01 | Relaciona dos tareas: bloquea, se relaciona con o duplica | el tablero de origen, y VER en el de destino |
| POST | `/relacion/{uuid}/archivar` | `EDITAR@TABLERO` | CU-REL-02 | Archiva una relación entre tareas | el tablero de origen |
| POST | `/rol/guardar` | `SYSADMIN, ADMIN` | CU-ROL-03 | Crea o edita un rol y los permisos que otorga |  |
| POST | `/rol/paginar` | `SYSADMIN, ADMIN` | CU-ROL-01 | Lista los roles vigentes con filtros y orden |  |
| GET | `/rol/uuid/{uuid}` | `SYSADMIN, ADMIN` | CU-ROL-02 | Obtiene un rol con los permisos que otorga |  |
| POST | `/rol/{uuid}/archivar` | `SYSADMIN, ADMIN` | CU-ROL-04 | Archiva un grupo (rol); los grupos del sistema no se archivan |  |
| POST | `/sprint/guardar` | `EDITAR@PROYECTO` | CU-SPR-03 | Crea un sprint planificado o edita su nombre y fechas |  |
| POST | `/sprint/paginar` | `VER@PROYECTO` | CU-SPR-02 | Lista sprints con filtros y orden | lista solo los sprints de proyectos visibles |
| GET | `/sprint/uuid/{uuid}` | `VER@PROYECTO` | CU-SPR-01 | Obtiene un sprint |  |
| POST | `/sprint/{uuid}/archivar` | `EDITAR@PROYECTO` | CU-SPR-08 | Archiva un sprint planificado o cerrado (uno activo se cierra primero) |  |
| POST | `/sprint/{uuid}/asignar` | `EDITAR@PROYECTO` | CU-SPR-04 | Asigna tareas al sprint |  |
| POST | `/sprint/{uuid}/cerrar` | `EDITAR@PROYECTO` | CU-SPR-07 | Cierra el sprint activo; lo pendiente va al backlog o a otro sprint planificado |  |
| GET | `/sprint/{uuid}/cierre` | `VER@PROYECTO` | CU-SPR-09 | Cierre de un sprint: quién lo cerró, cuándo y el estado y destino de cada tarea al cerrar (exige VER) |  |
| POST | `/sprint/{uuid}/iniciar` | `EDITAR@PROYECTO` | CU-SPR-06 | Inicia un sprint planificado; solo puede haber uno activo por proyecto |  |
| POST | `/sprint/{uuid}/quitar` | `EDITAR@PROYECTO` | CU-SPR-05 | Quita tareas del sprint; vuelven al backlog |  |
| POST | `/sprint/{uuid}/restaurar` | `EDITAR@PROYECTO` | CU-SPR-10 | Restaura un sprint archivado con el estado que tenía (exige EDITAR) |  |
| POST | `/tarea/guardar` | `EDITAR@TABLERO` | CU-TAR-03 | Crea una tarea con tipo y título en el primer estado de su tablero, o edita sus datos |  |
| POST | `/tarea/paginar` | `VER@TABLERO` | CU-TAR-02 | Lista las tareas con filtros y orden | lista solo las tareas de tableros visibles |
| GET | `/tarea/uuid/{uuid}` | `VER@TABLERO` | CU-TAR-01 | Obtiene una tarea |  |
| POST | `/tarea/{uuid}/archivar` | `ARCHIVAR@TABLERO` | CU-TAR-06 | Archiva una tarea y, en cascada, sus hijas en todos los niveles |  |
| POST | `/tarea/{uuid}/clonar` | `VER@TABLERO` | CU-TAR-09 | Clona una tarea a un tablero del mismo proyecto eligiendo qué datos se copian | el tablero original, y EDITAR en el de destino |
| GET | `/tarea/{uuid}/historial` | `VER@TABLERO` | CU-TAR-05 | Historial de cambios de una tarea, opcionalmente de un solo canal |  |
| POST | `/tarea/{uuid}/mover` | `EDITAR@TABLERO` | CU-TAR-04 | Mueve una tarea a otro estado de su tablero |  |
| GET | `/tarea/{uuid}/relaciones` | `VER@TABLERO` | CU-TAR-08 | Relaciones de una tarea vistas desde ella, con el estado de la otra tarea |  |
| POST | `/tarea/{uuid}/restaurar` | `ARCHIVAR@TABLERO` | CU-TAR-07 | Restaura una tarea archivada con lo que se archivó en su misma operación |  |
| GET | `/tipo-tablero/listar` | `AUTENTICADO` | CU-TTB-01 | Lista los tipos de tablero con sus estados en orden de columna | catálogo común a todos los proyectos |
| GET | `/tipo-tarea/listar` | `AUTENTICADO` | CU-TTA-01 | Lista los tipos de tarea con su nivel en la jerarquía | catálogo común a todos los proyectos |
| GET | `/usuario/administracion` | `SYSADMIN, ADMIN` | CU-USR-26 | Usuarios vigentes y archivados con su forma de ingreso y sus grupos, para la administración |  |
| DELETE | `/usuario/borrar-token/{uuid}` | `AUTENTICADO` | CU-TOK-03 | Elimina un token de acceso propio (requiere sesión) |  |
| POST | `/usuario/cerrar-sesion` | `PUBLICO` | CU-USR-15 | Cierra la sesión: borra el refresh token y las cookies access_token y refresh_token |  |
| POST | `/usuario/crear-cuenta` | `SYSADMIN, ADMIN` | CU-USR-27 | Crea una cuenta de Shiro con sus grupos; la contraseña inicial la genera el backend y se responde una sola vez |  |
| POST | `/usuario/crear-token` | `AUTENTICADO` | CU-TOK-01 | Crea un token de acceso personal para el usuario de la sesión (requiere sesión) |  |
| POST | `/usuario/guardar` | `SYSADMIN, ADMIN` | CU-USR-03 | Crea o edita un usuario (un Administrador no edita al super admin ni a otros Administradores) |  |
| POST | `/usuario/ingresar-con-google` | `PUBLICO` | CU-USR-20 | Ingresa con una cuenta de Google de la organización y responde un JWT y su refresh token (también como cookies) |  |
| POST | `/usuario/iniciar-sesion` | `PUBLICO` | CU-USR-10 | Inicia sesión con usuario y contraseña y responde un JWT y su refresh token (también como cookies HttpOnly access_token y refresh_token) |  |
| GET | `/usuario/mis-tokens` | `AUTENTICADO` | CU-USR-25 | Mis tokens de acceso, también los revocados, con su creación, su revocación y su último uso | tokens del propio usuario |
| POST | `/usuario/paginar` | `SYSADMIN, ADMIN` | CU-USR-02 | Lista los usuarios con filtros y orden |  |
| POST | `/usuario/refresh-token` | `PUBLICO` | CU-USR-11 | Renueva el JWT con el refresh token, del body o de la cookie refresh_token (el refresh token anterior deja de valer) |  |
| GET | `/usuario/sesion` | `AUTENTICADO` | CU-USR-05 | Obtiene el usuario de la sesión actual (requiere sesión, sin permiso puntual) | datos del propio usuario |
| GET | `/usuario/sesion/grupos` | `AUTENTICADO` | CU-USR-24 | Grupos del usuario de la sesión y si es administrador (requiere sesión, sin permiso puntual) | datos del propio usuario |
| GET | `/usuario/tokens` | `AUTENTICADO` | CU-TOK-02 | Lista los tokens de acceso vigentes del usuario de la sesión (requiere sesión) |  |
| GET | `/usuario/uuid/{uuid}` | `SYSADMIN, ADMIN` | CU-USR-01 | Obtiene un usuario por su uuid |  |
| POST | `/usuario/{uuid}/archivar` | `SYSADMIN, ADMIN` | CU-USR-04 | Archiva un usuario: no ingresa más, sus tokens se desactivan y sus tareas conservan el responsable |  |
| GET | `/usuario/{uuid}/grupos` | `SYSADMIN, ADMIN` | CU-USR-21 | Lista los grupos de un usuario |  |
| POST | `/usuario/{uuid}/grupos` | `SYSADMIN, ADMIN` | CU-USR-22 | Reemplaza los grupos de un usuario (los del sistema solo los maneja el super admin) |  |
| POST | `/usuario/{uuid}/restaurar` | `SYSADMIN, ADMIN` | CU-USR-23 | Restaura un usuario archivado (sus tokens anteriores siguen desactivados) |  |
| POST | `/version/guardar` | `EDITAR@PROYECTO` | CU-VER-03 | Crea una versión o edita su nombre (una publicada no se edita) |  |
| POST | `/version/paginar` | `VER@PROYECTO` | CU-VER-02 | Lista versiones con filtros y orden | lista solo las versiones de proyectos visibles |
| GET | `/version/uuid/{uuid}` | `VER@PROYECTO` | CU-VER-01 | Obtiene una versión |  |
| POST | `/version/{uuid}/archivar` | `ARCHIVAR@PROYECTO` | CU-VER-08 | Archiva una versión |  |
| POST | `/version/{uuid}/asignar` | `EDITAR@PROYECTO` | CU-VER-04 | Asigna tareas del proyecto a una versión sin publicar |  |
| GET | `/version/{uuid}/changelog` | `VER@PROYECTO` | CU-VER-07 | Changelog de una versión en Markdown: tareas terminadas agrupadas por tipo |  |
| POST | `/version/{uuid}/publicar` | `EDITAR@PROYECTO` | CU-VER-06 | Publica una versión con la fecha elegida; su changelog queda fijo |  |
| POST | `/version/{uuid}/quitar` | `EDITAR@PROYECTO` | CU-VER-05 | Quita tareas de una versión sin publicar |  |
| POST | `/version/{uuid}/restaurar` | `ARCHIVAR@PROYECTO` | CU-VER-09 | Restaura una versión archivada |  |
| GET | `/version/{uuid}/tareas` | `VER@PROYECTO` | CU-VER-10 | Tareas de una versión, terminadas o no, con su tipo, tablero y estado |  |
