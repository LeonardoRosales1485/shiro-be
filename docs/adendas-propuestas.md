# Adendas propuestas (pendientes de revisión)

Decisiones que el programador tomó mientras el arquitecto no estaba disponible y que deberían pasar como adenda al
DAF, la spec o el plan. Desde el 19/11/2026 no se escriben en Claude Docs: quedan acá hasta que el arquitecto las
revise y se agreguen a los documentos. Cada una dice qué documento afecta.

| # | Fecha | Tarea | Documento | Decisión | Motivo |
| --- | --- | --- | --- | --- | --- |
| 1 | 19/11/2026 | Adenda 4 | DAF | El clon de una Subtarea conserva el padre del original (excepción a "el clon no hereda el padre") | Una Subtarea no puede quedar sin padre (adenda 4). Ya figura en la adenda 4 como "a confirmar" |
| 2 | 19/11/2026 | T2.1 | Spec 2, plan | Tokens con hash en Shiro: `AuthTokenConSha256Service` extiende el `AuthTokenService` del Core y `AuthTokenConSha256Config` quita del contexto el bean original. Se retira cuando el Core publique 4.1.0 | El repositorio Maven del Core solo tiene 4.0.0 (consultado el 19/11/2026). La spec ya preveía hacerlo en Shiro; esto fija cómo |
| 3 | 19/11/2026 | T2.1 | Spec 2 | Un token revocado responde 401 `ACCESS_TOKEN_INVALIDO` (código del Core), no `NO_AUTENTICADO` | Es el código que ya publica core-protocolo-apikey |
| 4 | 19/11/2026 | T2.2 | Spec 2 y 3 | El ingreso con Google es `POST /api/usuario/ingresar-con-google` (no `POST /api/auth/google`) y no se agrega `google` a `auth.protocolos` | Sigue la convención de `/usuario/iniciar-sesion` del Core. El ID token de Google se cambia por el JWT del Core, que es lo que viaja después en cada request: no hace falta una estrategia por request |
| 5 | 19/11/2026 | T2.2 | Spec 2 | El usuario que se crea al entrar con Google tiene como nombre de usuario su correo, sin contraseña y sin roles. Un correo de un usuario archivado no entra (401 `NO_AUTENTICADO`) | La spec dice "busca o crea el usuario por correo" sin más detalle |
| 6 | 19/11/2026 | T2.2 | Spec 2 | Sin `GOOGLE_CLIENT_ID` o `GOOGLE_DOMINIO` el backend arranca igual, con el ingreso con Google deshabilitado (503 `GOOGLE_NO_CONFIGURADO`); el smoke test de Google al iniciar solo loguea | Las cuentas locales siguen funcionando: Google no es imprescindible para arrancar (regla de integraciones) |
| 7 | 19/11/2026 | T2.3 | Spec 2 | Niveles independientes: `EDITAR` y `ARCHIVAR` permitidos implican `VER`, pero negar `EDITAR` o `ARCHIVAR` no niega `VER`, y entre las reglas del mismo usuario sobre un recurso gana negar | La spec dice que implican VER y son independientes; el prototipo los mostraba acumulativos. Manda la spec. Faltaba definir qué pasa con una regla que niega |
| 8 | 19/11/2026 | T2.3 | Spec 3 | `/regla-permiso`: uuid, paginar, guardar y `POST /{uuid}/archivar`, todos solo para super admin y Administradores. Cada guardado y archivado deja su fila en el historial | La spec lista "paginar, guardar, archivar"; uuid sale de la receta del ABM |
| 9 | 19/11/2026 | T2.4 | Spec 2 | Niveles por endpoint: leer = VER; crear, editar, mover, comentar = EDITAR en el tablero; relacionar = EDITAR en el origen y VER en el destino; archivar y restaurar tareas y proyectos = ARCHIVAR; clonar = VER en el original y EDITAR en el destino. Un proyecto aparece en el listado si se ve el proyecto o alguno de sus tableros | La spec define VER, EDITAR y ARCHIVAR, y que crear y comentar exigen EDITAR; no detallaba relacionar, clonar ni la visibilidad de un proyecto con permiso solo sobre un tablero |
| 10 | 19/11/2026 | T2.4 | Spec 3 | Archivar y restaurar un proyecto exige ARCHIVAR en el proyecto (no solo ser Administrador) | La spec dice en Spec 3 "Archivar: permiso ARCHIVAR en el proyecto" y en otra parte "crear y archivar para administradores"; se tomó la regla de Spec 3. Los Administradores lo tienen siempre |

