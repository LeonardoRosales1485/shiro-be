# Shiro — Bitácora del programador

Desde el 18/11/2026 la bitácora vive en este archivo (antes en Claude Docs). Las filas más nuevas van arriba.

Avances de la implementación de Shiro, tarea por tarea, con su evidencia. Complementa el [plan de implementación](https://claude.ai/artifact/EmaCcmkAjPbQGSKTB3rz2s) y la [spec técnica](https://claude.ai/artifact/THf6uGiRMHyV9L2K2SkF8k).

## Estado

**F1 en curso.** T1.1 a T1.5 cerradas (versión 1.0.10). Sigue T1.6 (historial). Guía local: `shiro-be/docs/levantar-en-local.md`.

## Avances

| Fecha | Tarea | Qué se hizo | Evidencia |
| --- | --- | --- | --- |
| 18/11/2026 | T1.5 | POST /tarea/{uuid}/mover con estadoUuid (TareaMoverUseCase). Cualquier estado del tablero de la tarea; un estado de otro tablero responde ESTADO_DE_OTRO_TABLERO. La ruta sigue la regla /{uuid}/{caso-de-uso} de controllers-y-endpoints.md (la spec decía POST /mover) | e232d79 (rojo) y verde en el commit siguiente. ./mvnw verify: 267 pruebas, 0 fallas, 11 omitidas; cobertura 90,5%. Versión 1.0.10 |
| 18/11/2026 | T1.4 | Tarea padre (id_padre) con hijas vigentes en la respuesta. El padre es del mismo proyecto (PADRE_DE_OTRO_PROYECTO) y de un tipo de nivel menor; al editar, el tipo nuevo no puede igualar o superar el nivel de sus hijas (JERARQUIA_DE_TAREA_INVALIDA). Decisión propia por no estar en el DAF ni en la spec: el padre puede estar en otro tablero del mismo proyecto y una Subtarea puede no tener padre. EscenarioDeTarea para preparar datos por los endpoints. El tablero no editable ya quedó cubierto en T1.3 | 74f34e8 (rojo) y verde en el commit siguiente. ./mvnw verify: 261 pruebas, 0 fallas, 11 omitidas; cobertura 90,1%. Versión 1.0.9 |
| 18/11/2026 | T1.3 | shr\_tarea y shr\_tarea\_etiqueta. ABM de /tarea sin borrar (con sesión; permisos por proyecto en F2). Modo rápido: tablero, tipo y título. Nace en el estado inicial del tablero, prioridad MEDIA, informante = quien crea. Clave del contador del proyecto con SELECT … FOR UPDATE (Proyecto.siguienteNumero, Tarea.numerar, con tests unitarios). Al editar no cambian clave, proyecto, estado ni tablero (TABLERO\_DE\_TAREA\_NO\_SE\_CAMBIA). Etiquetas quitadas con status -1. Seed SHI-1. Hallazgo: la prueba de 50 altas concurrentes trababa el pool de Hikari porque ConfiguracionDesdeBase (template) leía DEBUG con NOT\_SUPPORTED y pedía una segunda conexión en cada llamada a un Service dentro de una transacción; pasó a SUPPORTS. La prueba concurrente crea su propio proyecto (CONC) por el endpoint, sin SQL (TST-04). Los tests unitarios de entidad se escribieron junto con el código, no antes | 4af6c02 (rojo, no compila), 0dc3ed0 (fix del pool) y 0efc3b4 (verde). ./mvnw verify: 254 pruebas, 0 fallas, 11 omitidas (casos de borrar que no aplican); cobertura de líneas 89,2%. 50 altas simultáneas: 50 claves distintas en 14 s |
| 18/11/2026 | T1.2 | shr\_proyecto y shr\_tablero. ABM de /proyecto sin borrar: leer con sesión (filtro por permisos en T2.4); guardar para SYSADMIN y ADMIN. El alta crea los cuatro tableros en la misma transacción. Prefijo de 2 a 6 letras en mayúsculas, único también contra archivados e inmutable. Seeds: Shiro (SHI) y un proyecto archivado (ARC) | 251fcb2 (rojo, no compila) y 4341c68 (verde). ./mvnw verify: 179 pruebas, 0 fallas, 8 omitidas (casos de borrar que no aplican) |
| 18/11/2026 | T1.1 | shr\_tipo\_tablero, shr\_estado (inicial y terminado) y shr\_tipo\_tarea (nivel) con datos obligatorios y uuid fijos. GET /tipo-tablero/listar y GET /tipo-tarea/listar (catálogo; no estaban en la Spec 3, sirven de verificación de T1.1 y al front). La columna id\_operacion\_archivo se agrega a todas las tablas shr\_ en T1.8, junto con shr\_operacion\_archivo | fd6b659 (rojo, 404) y ce482a4 (verde). ./mvnw verify: 162 pruebas, 0 fallas |
| 18/11/2026 | Historial de shiro-be | git filter-repo reemplazó por REMOVIDO los valores de mail, captcha, crypter, sm3 y jwt de la copia de la plantilla, en todo el historial. Se quitó la excepción de commit de .gitleaks.toml. Todos los SHA de shiro-be cambiaron: los citados antes en esta bitácora corresponden al historial viejo (por ejemplo c4f0a22, cac9d1c, 7d47e14). La rama de trabajo quedó reescrita; develop sigue con el historial viejo | Force push de la rama de trabajo (d9566ba → 8f6dd1a, versión 1.0.5). gitleaks local sobre el historial nuevo: 0 hallazgos. En CI falla gitleaks porque develop todavía trae el historial viejo |
| 18/11/2026 | T0.6 | Workflow Calidad en los dos repos. Back: mvnw verify con JaCoCo (80% de líneas) y PMD CPD (50 tokens), OSV sobre SBOM CycloneDX, Semgrep y gitleaks. Front: ESLint, tipos, Vitest con cobertura, jscpd, build, OSV, Semgrep y gitleaks. OSV encontró vulnerabilidades críticas en Tomcat y Jackson de Boot 4.1.1: se subieron los parches. Ajustes registrados al final de la adenda 1 del plan | shiro-be 7d47e14 (versión 1.0.4), shiro-fe 7d7f0f8. Local: cobertura back 83%, front 92,9%; CPD y jscpd 0; OSV 0; Semgrep 0; gitleaks 0. Un commit con secreto de prueba hace fallar gitleaks; un duplicado hace fallar CPD y jscpd |
| 18/11/2026 | Prueba en Windows | El arquitecto levantó F0 en Windows con Docker Desktop: imagen de shiro-be, Postgres 18.4, seed (17 changesets), login de los 4 usuarios, eco por MCP, app solo como shiro\_app y front conectado. Respuesta a sus observaciones: .gitattributes en los dos repos, guía con PYTHONUTF8 y local.bat desde terminal, spec corregida. deploy/ no se tocó (regla de la plantilla) | Commits locales shiro-be cfbfcef (versión 1.0.3) y shiro-fe 61592e9, sin pushear: falta confirmación del push |
| 18/11/2026 | T0.4 y T0.5 | shiro-fe: Vite 7 + React 19 + TypeScript, tokens.css generado desde tokens.json, bundle.css y Montserrat; página inicial que muestra la versión del backend; proxy /api a 7090; deploy/ y deploy.yml (vite-react-node-22). shiro-be: deploy.yml local (spring\_postgres, Postgres 18.4). Puertos provisorios 7090, 5090 y 7091. El build de imágenes no se pudo probar acá: el entorno no da internet a los builds de Docker | shiro-fe 8230581 (rojo) y 0b0cf0d: 3 pruebas, build y npm audit sin vulnerabilidades. Jar contra postgres:18.4 + vite preview: captura docs/inicio-local.png con «Backend conectado: versión 1.0.2» |
| 18/11/2026 | T0.3 | Opción A: Liquibase con el usuario dueño crea shiro\_app sin DELETE ni TRUNCATE en shr\_ (shr\_historial solo inserción; tablas del Core con DELETE) y reaplica permisos en cada arranque. La app se conecta con shiro\_app (spring.datasource.hikari.\*) | c0c11bb (rojo) y cac9d1c: 158 pruebas. Contra postgres:18.4 real: pool de la app con shiro\_app, login de sysadmin y eco por MCP OK. Versión 1.0.2 |
| 18/11/2026 | T0.2 | Spring AI 2.0.2 (SDK MCP 2.1.0) con Boot 4.1.1: servidor MCP sin estado en /api/mcp y herramienta eco. Un cliente MCP real la inicializa, lista y llama por Streamable HTTP. No hace falta el plan B. /api/mcp queda sin autenticar hasta T3.1 (solo expone eco) | Commits d2522d1 (prueba en rojo, 404) y cda618f (verde). ./mvnw verify: 154 pruebas, 0 fallas, 5 omitidas. Versión 1.0.1 |
| 18/11/2026 | T0.1 | Plantilla backend-v2 copiada en shiro-be y adaptada: Core con identity + monoempresa, jwt, basic y tokens; fuera multiempresa, sso, mail, crypter, sm3, captcha y websocket. Seed nuevo de roles y usuario-rol. Secretos del servidor a variables de entorno. Prueba ModulosDelCoreTest escrita antes que el código | Commits 1782e35 (plantilla), cab5664 (prueba en rojo), c4f0a22 (verde). ./mvnw verify: 153 pruebas, 0 fallas, 5 omitidas |
| 18/11/2026 | Preparación | Leídos el traspaso, la spec, el plan, `sm-template` (backend-v2) y el Core 4.0.0. JDK 25 instalado. Adenda de implementación 1 en el plan | Registro de tiempo, turnos 23 y 24 |

## Decisiones

| Fecha | Decisión | Quién la aprobó |
| --- | --- | --- |
| 18/11/2026 | La bitácora pasa a `shiro-be/docs/bitacora-del-programador.md`. Seguir sin esperar aprobación hasta T1.10; detenerse solo ante un bloqueo fuerte para las tareas siguientes | Arquitecto |
| 18/11/2026 | Vencimiento de la tarea como fecha sin hora (`LocalDate`, `aaaa-mm-dd`) | Arquitecto |
| 18/11/2026 | Autorización vigente para usar Claude Docs (leer y editar DAF, spec, plan y bitácora) sin pedir confirmación en cada caso | Arquitecto |
| 18/11/2026 | Adenda 3 del DAF: SSO y mail fuera de la v1 (con motivo) y reglas del prototipo para F1 (prefijo, estado terminado, cascada, historial, valores por defecto, vencimiento). Reflejada en la spec. develop de shiro-be apuntado al historial reescrito (c13a6be) | Arquitecto |
| 18/11/2026 | F0 aprobada. Se reescribe el historial de shiro-be para sacar los secretos de la plantilla (en lugar de dejar la excepción de gitleaks) | Arquitecto |
| 18/11/2026 | Autorización vigente para pushear a claude/jolly-newton-jqin42 (be y fe) al cerrar cada tarea, sin pedir confirmación en cada push. Merge y PR siguen necesitando pedido explícito | Arquitecto |
| 18/11/2026 | Plantilla backend-v2 de `sm-template` (no v3) | Arquitecto |
| 18/11/2026 | Repositorios en la cuenta personal; la empresa no permite el MCP de GitHub | Arquitecto |
| 18/11/2026 | Commits chicos en `claude/jolly-newton-jqin42`, push al cerrar cada tarea, PR a `develop` cuando se indique | Arquitecto |
| 18/11/2026 | Adenda de implementación 1: OSV-Scanner en lugar de Dependency-Check y precisiones de calidad | Arquitecto (umbrales delegados) |

## Pendientes y bloqueos

- [ ] Protección de la rama `develop` con los checks de T0.6 (la configura el arquitecto en GitHub)
- [ ] Credenciales de un proyecto de Jira de prueba (F4)
- [ ] Client id de Google (F2)
- [ ] Leer el DAF completo antes de F1
- [ ] Hallazgo plantilla: `ConfiguracionDesdeBase` con `NOT_SUPPORTED` agota el pool de conexiones con 10 transacciones concurrentes (cada llamada a un Service pide otra conexión para leer DEBUG). En Shiro se cambió a `SUPPORTS`. Reportar a quien mantiene sm-template
- [x] Confirmar con el arquitecto: el vencimiento de la tarea es `LocalDate` (`aaaa-mm-dd`, sin hora). La regla de fechas de `entidades-y-dtos.md` no cubre fechas sin hora
- [ ] Apuntar develop de shiro-be al historial reescrito (8f6dd1a). El force push lo bloqueó el control de permisos de la sesión
- [ ] Rotar los secretos de la plantilla igual: GitHub puede conservar los commits viejos accesibles por su SHA un tiempo
- [ ] Hallazgo plantilla: Semgrep marca 4 problemas en `deploy/` de sm-template (subprocess con entorno y XML sin defusedxml en `smoke.py`). Reportar a quien la mantiene
- [ ] Decidir si se reescribe el historial de shiro-be para sacar el commit `1782e35` (secretos de la plantilla) o se deja la excepción de gitleaks y se rotan los secretos
- [ ] Puertos locales del backend y de Postgres (los asigna el líder técnico; quedan `PUERTO_REMPLAZAR` en `application.local.properties` y `deploy.yml`)
- [ ] Hallazgo spec: `core-crypter` guarda archivos en un servicio externo, no cifra datos. La spec lo usa para cifrar las credenciales de Jira (Spec 2 y 6). Resolver antes de F4
- [ ] Hallazgo Core 4.0.0: en monoempresa no hay endpoint para asignar roles a un usuario ni validación de nombre de rol repetido. Se cubre en T2.5
- [ ] Hallazgo Core 4.0.0: `RolBaseService.setAuthService` no asigna el campo. Reportar al Core
- [ ] Hallazgo plantilla: `application.*.properties` de sm-template traen en comentarios una contraseña de aplicación de Gmail y un secreto de hCaptcha. En Shiro se quitaron; conviene rotarlos y avisar a quien mantiene la plantilla
- [ ] `/usuario/borrar` y `/rol/borrar` de la plantilla siguen; la spec pide archivar y restaurar (T2.5)
