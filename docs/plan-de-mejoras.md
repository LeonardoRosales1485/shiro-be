# Shiro — Plan de mejoras (integridad, seguridad, escalabilidad, testing y proceso)

24/11/2026 · Leonardo Rosales (Analista Funcional) · Estado: **aprobado el 26/11/2026** (decisiones D-1 a D-6 de §5 aceptadas tal como se proponen)

Plan para llevar a Shiro las mejoras que surgieron de revisarlo con los criterios del Sistema de Faltas (skill
`bod-programador-faltas` y repos `FALTAS FINALE/faltas` y `FALTAS FINALE/faltasfe`). No cambia el alcance funcional
del DAF: las mejoras que tocan comportamiento visible (MJ-05, MJ-08, MJ-09, MJ-11) se registran primero como adenda
del plan de implementación, como se hizo con la Adenda de implementación 1.

Jerarquía vigente: DAF > Diseño técnico > Plan de implementación > este plan > código. Si una tarea de este plan
choca con un documento de más arriba, se frena y se consulta.

---

## 1. Punto de partida

Revisado el 24/11/2026 sobre `shiro-be` (`develop`, versión 1.0.52) y `shiro-fe` (0.0.11).

### Lo que ya está bien y no se toca

| Tema | Evidencia |
|---|---|
| Integración continua en los dos repos | `.github/workflows/calidad.yml`: pruebas con JaCoCo ≥ 80 %, PMD CPD / jscpd, OSV-Scanner, Semgrep (OWASP), gitleaks. Faltas no tiene integración continua. |
| Reglas y auditoría del backend | `CLAUDE.md`, `docs/rules/` (17 reglas, incluye `logs.md` y `archivos.md`, que Faltas no tiene), `docs/auditoria/checklist.md` (96 controles), `/auditar`, `docs/excepciones.md` |
| Pruebas del backend | 103 clases sobre PostgreSQL 18 embebido con el changelog real; cobertura 94,3 %; `ArquitecturaTest`, `EstiloDeCodigoTest`, `ContratosCompletosTest`; 50 creaciones concurrentes |
| Integridad en la base | La app entra como `shiro_app` (sin `DELETE` en `shr_`); `shr_historial` solo admite inserción; `ux_shr_mapeo_jira_tipo_id` evita duplicados al reimportar; índice GIN de búsqueda |
| Tokens MCP | Guardados como SHA-256 (`AuthTokenConSha256Service`), se muestran una sola vez |
| Markdown en el front | `marked` + DOMPurify (`src/componentes/Markdown.tsx`) |
| Guía local | `docs/levantar-en-local.md` con `PYTHONUTF8`, `.gitattributes` y el `.bat` desde terminal |

### Brechas encontradas

| Área | Brecha | Evidencia |
|---|---|---|
| Integridad | Sin política de respaldos ni prueba de restauración | Pendiente del traspaso; nada en `deploy/` ni `docs/` |
| Integridad | El 409 compara `fechaDeActualizacion` (precisión de timestamp), sin `@Version` | `TareaDTO.fechaDeActualizacion`, `CodigoDeErrorDelProyecto.TAREA_MODIFICADA` |
| Integridad | Invariantes de la Spec 1 que dependen solo del código Java (estado del tablero de la tarea, nivel del padre) | 17 `CHECK` en los changelogs; a relevar cuáles faltan |
| Seguridad | No hay límite de intentos de ingreso en Shiro | Sin coincidencias de `ratelimit`, `bucket4j`, `intentos`; a confirmar si el Core lo trae |
| Seguridad | `x-permiso` solo en 29 de 87 operaciones, y mezcla roles (`SYSADMIN, ADMIN`) con permisos (`CONFIGURACION_CREAR`). Los permisos por recurso (VER, EDITAR, ARCHIVAR sobre proyecto o tablero) no quedan declarados en el contrato | `docs/api/openapi.yaml` |
| Seguridad | Sin prueba que recorra todos los endpoints con cada perfil | 15 clases prueban `SIN_PERMISO`, cobertura por endpoint no medida |
| Seguridad | El front decide por `esAdministrador`, no por el permiso efectivo sobre el proyecto o tablero | `src/App.tsx` |
| Seguridad | Texto de tareas y comentarios vuelve por MCP al LLM sin marcarse como dato; sin límite de llamadas por token; tokens sin vencimiento | Spec 4, `TokensMCP` |
| Seguridad | Sin revisión de seguridad por cambio (solo `/auditar` antes del PR); sin CSP en el nginx | `.claude/commands/` |
| Escalabilidad | Tope de tamaño de página no confirmado; `shr_historial` sin plan de crecimiento; logs sin rotación ni retención | `docs/arquitectura/logs/README.md` §«Rotación y retención» |
| Testing | Sin pruebas de mutación; front con 20 archivos de prueba para 66 de código y sin pruebas de punta a punta; la imagen Docker no se construye en la integración continua; migraciones probadas solo sobre base vacía | `package.json`, `calidad.yml` |
| Proceso | `shiro-fe` sin `CLAUDE.md`, convenciones, estado ni bitácora; tipos de la API escritos a mano (`src/api/tipos.ts`); sin generadores ni verificación de contrato | Comparación con `faltasfe/scripts/` y `faltasfe/docs/roadmap/` |
| Proceso | Sin menú de tareas locales, sin `usuarios-de-prueba.md`, decisiones sin numeración propia, sin `settings.json` de permisos del agente | Comparación con `faltas/` |

---

## 2. Organización del trabajo

### Olas y fechas

El cronograma aprobado tiene validación del 4 al 15/1 y corte del 25 al 29/1/2027. Las mejoras se ordenan para que
lo que protege datos y accesos esté antes del corte.

| Ola | Fechas propuestas | Qué entra | Condición de salida |
|---|---|---|---|
| **O1. Antes de la validación: protección** | 25/11 – 11/12 | MJ-01 a MJ-07, MJ-15 | Respaldo probado con una restauración; login limitado; catálogo y matriz de permisos en verde; Docker en la integración continua |
| **O2. Antes de la validación: calidad** | 14/12 – 23/12 | MJ-08 a MJ-14, MJ-16 a MJ-19 | Contrato generado en el front; mutación medida; Playwright con los flujos principales |
| **O3. Durante la validación** | 4/1 – 15/1 | MJ-20 a MJ-27 | Front con reglas y memoria propias; menú local; verificación acotada en uso |
| **O4. Después del corte** | desde 1/2 | MJ-28 a MJ-30 | Según el uso real |

### Quién hace qué

- **Agente implementador** (Claude Code con los repos): el código, las pruebas y los docs de cada repo.
- **Leonardo**: aprobaciones, decisiones de §5, pruebas locales con Docker, push y merge.
- **Agente de pruebas locales** (Cowork, en la PC de Leonardo): verificación en local de cada ola e informe en el
  proyecto SHIRO.
- **DevOps de BOD**: respaldos (MJ-01), CSP en infra-nginx (MJ-07), Core 4.1.0 (MJ-28), protección de `develop`.
- **Líder técnico**: excepciones bloqueantes y puertos definitivos.

### Cómo se trabaja cada tarea

Igual que el plan de implementación: TDD (prueba en rojo, código, verde), +0.0.1 en `app/pom.xml` (backend) o en
`package.json` (front) por cambio, `/auditar` antes del PR, sin avanzar de ola sin aprobación. Cada tarea cierra con:
evidencia (comando y resultado), fila en el registro de tiempo y en la bitácora del programador.

---

## 3. Tareas

Formato de cada tarea: **problema**, **origen** (de dónde se toma la idea), **pasos**, **archivos**, **verifica**,
**esfuerzo** (S < 2 h, M ≈ medio día, L ≈ 1 a 2 días), **depende de**.

### Ola 1 — Protección

#### MJ-01 · Respaldos y restauración probada
- **Problema:** «nada se borra» protege contra borrados lógicos, no contra perder la base (disco, error de
  migración, borrado del volumen). Hoy no hay respaldos definidos.
- **Origen:** pendiente del traspaso y del DAF (sección 9, respaldos).
- **Pasos:**
  1. Leonardo y el devops fijan frecuencia y retención (propuesta: `pg_dump -Fc` diario, 14 diarios + 8 semanales,
     fuera del servidor de la app).
  2. El devops arma el job en st7 y st8 y el destino del archivo.
  3. El agente agrega a `docs/` un `respaldos.md` con cómo restaurar paso a paso (crear base vacía, `pg_restore`,
     arrancar la app, verificar con `deploy/smoke.py`).
  4. Prueba de restauración en local: respaldo de la base local, `--delete-db`, restauración, smoke y conteo de
     tareas antes y después.
  5. Antes del corte, simulacro en st8 con la base importada de Jira.
- **Archivos:** `docs/respaldos.md` (nuevo), configuración de infra (fuera del repo).
- **Verifica:** el conteo de `shr_tarea`, `shr_historial` y usuarios coincide antes y después de restaurar; la app
  arranca y `smoke.py` da verde.
- **Esfuerzo:** M (agente) + lo del devops. **Depende de:** decisión D-1 de §5.

#### MJ-02 · Control de edición simultánea con `@Version`
- **Problema:** el 409 compara la fecha leída con `fechaDeActualizacion`. Dos guardados en el mismo instante, o
  relojes con distinta precisión entre Java y Postgres, pueden pasar los dos.
- **Origen:** recomendación de la revisión; patrón estándar de JPA.
- **Pasos:**
  1. Prueba en rojo: dos guardados concurrentes con la misma versión leída → uno 200, otro 409 `TAREA_MODIFICADA`.
  2. Migración Liquibase: columna `version BIGINT NOT NULL DEFAULT 0` en `shr_tarea` (y en las entidades editables
     por varias personas: proyecto, sprint, versión, comentario; relevar en la tarea).
  3. `@Version` en la entidad; el DTO de edición manda `version` en lugar de (o además de) `fechaDeActualizacion`.
     Decidir en la tarea si se mantiene la fecha por compatibilidad del front un tiempo.
  4. MCP: `editar_tarea` también recibe `version` o la herramienta lee y guarda dentro de la misma transacción.
  5. Front: `DetalleEditar` y la hoja de edición del teléfono mandan `version`.
  6. Regenerar el contrato (`docs/api`).
- **Archivos:** `Code/Tarea/Tarea.java`, `TareaDTO.java`, servicio de edición, changelog nuevo, `shiro-fe`
  pantallas de edición, `src/api/tipos.ts` (o el generado de MJ-08).
- **Verifica:** prueba concurrente con `CountDownLatch` en verde; la prueba de T1.7 sigue en verde.
- **Esfuerzo:** M. **Depende de:** —.

#### MJ-03 · Invariantes de la Spec 1 en la base
- **Problema:** reglas como «el estado de la tarea es del tipo de su tablero» y «el padre es de un nivel menor» se
  validan en Java. Una migración o un import con un error las puede romper sin que la base lo impida.
- **Origen:** skill `backend-design:data-modeling-discipline`; revisión.
- **Pasos:**
  1. Relevar las invariantes de la Spec 1 y marcar cuáles ya tienen restricción (`CHECK`, `UNIQUE`, FK).
  2. Para «estado del tablero»: FK compuesta `(id_tipo_tablero, id_estado)` contra la tabla de estados, con
     `id_tipo_tablero` derivado o desnormalizado en `shr_tarea`; o un trigger si la FK compuesta complica el modelo
     (decidir con el relevamiento, anotarlo en la bitácora).
  3. Para «nivel del padre»: trigger `BEFORE INSERT OR UPDATE` en `shr_tarea` (los `CHECK` no pueden mirar otra fila).
  4. Antes de crear cada restricción: consulta que muestre que ningún dato actual la viola (en local y en st8).
  5. Pruebas que intenten violarlas por SQL directo y esperen el error de la base.
- **Archivos:** changelogs nuevos, `docs/excepciones.md` si una regla del template choca con un trigger.
- **Verifica:** cada invariante tiene su prueba de «la base lo rechaza».
- **Esfuerzo:** M. **Depende de:** —.

#### MJ-04 · Límite de intentos de ingreso
- **Problema:** con cuentas locales y tokens MCP expuestos en st7 y st8, no hay freno a la fuerza bruta.
- **Origen:** revisión de seguridad (OWASP A07).
- **Pasos:**
  1. Confirmar en el Core 4.0.0 (`core-identity`, `core-protocolo-jwt`) si existe bloqueo o límite. Si existe,
     configurarlo y saltear los pasos 2 y 3.
  2. Prueba en rojo: 5 intentos fallidos del mismo usuario en 15 minutos → el 6.º responde 429 con un código propio
     (`DEMASIADOS_INTENTOS`), aun con la clave correcta.
  3. Implementación en Shiro sin tocar el Core (escalera de extensión): filtro antes de
     `/usuario/iniciar-sesion` con contador por usuario y por IP, en memoria con caducidad (una sola instancia) o en
     una tabla `shr_intento_de_ingreso` si se prevé más de una instancia.
  4. Lo mismo para `/api/mcp` con token inválido: límite por IP.
  5. Registrar en el log de seguridad cada bloqueo (regla `logs.md`).
- **Archivos:** filtro nuevo en `Code/Common/Seguridad/`, `CodigoDeErrorDelProyecto`, pruebas de contrato.
- **Verifica:** prueba del 429; prueba de que pasada la ventana se vuelve a poder ingresar.
- **Esfuerzo:** M. **Depende de:** decisión D-2 de §5 (umbral y ventana).

#### MJ-05 · Permisos completos en el contrato y catálogo generado
- **Problema:** `x-permiso` cubre 29 de 87 operaciones y mezcla roles con permisos. Los permisos por recurso de
  `shr_regla_permiso` no aparecen en el contrato, así que no hay un lugar que diga qué exige cada endpoint.
- **Origen:** `faltas/docs/idp/catalogo-de-acl.md` (generado por la suite completa).
- **Pasos:**
  1. Definir la forma de declarar permisos de recurso en el contrato. Propuesta: `x-permiso: "EDITAR@TABLERO"`,
     `"ARCHIVAR@PROYECTO"`, `"ADMINISTRADORES"`, `"SYSADMIN"`, `"AUTENTICADO"`. Registrarlo como decisión.
  2. Anotación propia en los controllers (por ejemplo `@PermisoDeRecurso(nivel = EDITAR, recurso = TABLERO)`) que
     alimente el `x-permiso`, o extender el generador de contratos por la escalera de extensión.
  3. Que las 87 operaciones lo declaren; `ContratosCompletosTest` (o una prueba nueva) falla si una operación no
     tiene `x-permiso`.
  4. Generar `docs/idp/catalogo-de-permisos.md` en la corrida completa, igual que `docs/api`: tabla endpoint →
     método → permiso → caso de uso.
- **Archivos:** controllers, anotación nueva, generador o listener de contratos, `docs/idp/` (nuevo).
- **Verifica:** 87/87 operaciones con `x-permiso`; el catálogo se regenera y el PR lo muestra en el diff.
- **Esfuerzo:** L. **Depende de:** —. Habilita MJ-06 y MJ-08.

#### MJ-06 · Matriz de permisos: todos los endpoints contra todos los perfiles
- **Problema:** hoy se prueba `SIN_PERMISO` caso por caso. Un endpoint nuevo sin chequeo de permiso pasaría la suite.
- **Origen:** revisión (IDOR/BOLA, OWASP A01); el catálogo de MJ-05.
- **Pasos:**
  1. Fixture con un proyecto y sus tableros y actores: sin regla, VER, EDITAR, ARCHIVAR, administrador, sysadmin,
     usuario archivado, token MCP de cada uno.
  2. Prueba parametrizada que lee el catálogo de MJ-05 y, para cada operación y cada actor, espera 2xx o 403 según
     el permiso declarado. Para las operaciones con cuerpo, usa un ejemplo mínimo válido por operación.
  3. Una versión por MCP: cada herramienta con un token de cada actor.
  4. Casos de precedencia de la Spec 2 (usuario sobre rol, tablero sobre proyecto, NEGAR).
- **Archivos:** `src/test/.../Seguridad/MatrizDePermisosTest.java` (nuevo) y fixture.
- **Verifica:** la matriz completa en verde; un endpoint sin chequeo agregado a propósito la hace fallar (probarlo
  una vez y revertir).
- **Esfuerzo:** L. **Depende de:** MJ-05.

#### MJ-07 · Cabeceras de seguridad y CSP
- **Problema:** el front servido por nginx no declara CSP ni cabeceras de endurecimiento.
- **Origen:** revisión (OWASP A05).
- **Pasos:**
  1. Proponer al devops para el bloque de Shiro en infra-nginx: `Content-Security-Policy` (`default-src 'self'`,
     `img-src 'self' data:`, `style-src 'self'`, sin `unsafe-inline` si el build lo permite),
     `X-Content-Type-Options: nosniff`, `Referrer-Policy: same-origin`, `X-Frame-Options: DENY`,
     `Strict-Transport-Security`.
  2. En `vite.config.ts`, las mismas cabeceras en `preview`, para probarlas en local.
  3. Prueba de humo que lea las cabeceras de la página.
- **Archivos:** `shiro-fe/vite.config.ts`, `shiro-fe/deploy/` (fragmento nginx propuesto).
- **Verifica:** la app funciona con la CSP activa (sin errores de CSP en la consola) en las vistas principales.
- **Esfuerzo:** S + devops. **Depende de:** —.

#### MJ-15 · Build de la imagen Docker y smoke en la integración continua
- **Problema:** la imagen solo se construyó a mano en la PC de Leonardo. Un cambio en el Dockerfile o en las
  dependencias se detecta recién al desplegar.
- **Origen:** pruebas locales del 18/11 y del 20/11; `deploy/smoke.py` ya existe.
- **Pasos:**
  1. Job `imagen` en `shiro-be/.github/workflows/calidad.yml`: `docker compose` con la plantilla
     `spring-3.9-java-25-postgres-18-4`, esperar health, correr `deploy/smoke.py`, login de `sysadmin`, `eco` por MCP
     con token de seed.
  2. Job equivalente en `shiro-fe`: build de la imagen de la plantilla `vite-react-node-22`.
  3. Sumar los dos jobs a los checks obligatorios de `develop`.
- **Archivos:** `calidad.yml` de los dos repos.
- **Verifica:** el job falla si se rompe el Dockerfile a propósito (probarlo una vez en una rama).
- **Esfuerzo:** M. **Depende de:** —.

### Ola 2 — Calidad

#### MJ-08 · Contrato generado en el front
- **Problema:** `src/api/tipos.ts` (163 líneas) está escrito a mano. Si el backend cambia un DTO o un código de
  error, el front se entera en ejecución.
- **Origen:** `faltasfe/scripts/` (`generar-codigos-error.mjs`, `generar-permisos.mjs`, `verificar-contrato.mjs`,
  `lib/openapi-contrato.mjs`, `contrato.test.mjs`) y su `README.md`.
- **Pasos:**
  1. Fijar cómo llega el openapi al front: propuesta, `shiro-fe/contrato/openapi.yaml` copiado por un script desde
     `../shiro-be/docs/api/openapi.yaml`, con la versión del contrato en la primera línea. (En faltasfe el archivo no
     se versiona y la verificación no corre en la integración continua; acá conviene versionarlo para que sí corra.)
  2. Generador de tipos: `openapi-typescript` (dependencia de desarrollo) hacia `src/api/generado/esquema.ts`.
     Reemplazar `tipos.ts` por alias de lo generado.
  3. `scripts/generar-codigos-error.mjs`: constante `CODIGO_API` desde los códigos del contrato; `cliente.ts` y las
     pantallas comparan contra la constante, no contra textos sueltos.
  4. `scripts/generar-permisos.mjs`: constante `PERMISO` desde los `x-permiso` de MJ-05.
  5. Bloques generados entre marcas `// <generado:inicio>` y `// <generado:fin>`, como en faltasfe.
  6. `npm run contrato:generar`, `npm run contrato:verificar` (sale con 1 si hay diferencias) y
     `npm run contrato:probar` (prueba de los generadores con un openapi mínimo).
  7. `contrato:verificar` como paso del job de calidad del front.
- **Archivos:** `shiro-fe/scripts/`, `shiro-fe/contrato/`, `src/api/`, `package.json`, `calidad.yml`.
- **Verifica:** cambiar un campo del openapi de prueba hace fallar `contrato:verificar` y `tsc`.
- **Esfuerzo:** L. **Depende de:** MJ-05 para los permisos (los tipos y códigos pueden ir antes).

#### MJ-09 · El front decide por el permiso efectivo, no por el rol
- **Problema:** el front usa `esAdministrador`. Un miembro con VER sobre un tablero ve los botones de crear, mover o
  archivar y recibe 403 al usarlos.
- **Origen:** regla de faltasfe «autorización por `x-permiso`, nunca por rol».
- **Pasos:**
  1. Backend: endpoint `GET /proyecto/{uuid}/mis-permisos` (o campo en `ProyectoDTO` y `TableroDTO`) que devuelva los
     niveles efectivos del usuario por tablero, usando `ResolutorDePermisosDeRecurso`.
  2. Front: hook `usePermisos(proyecto)` y componente `<SiPuede nivel tablero>`; ocultar o deshabilitar acciones.
  3. Pruebas del front con los tres perfiles (VER, EDITAR, ARCHIVAR).
  4. Adenda de implementación: es comportamiento visible.
- **Archivos:** backend (endpoint y prueba de contrato), `shiro-fe/src/navegacion/`, pantallas con acciones.
- **Verifica:** con un usuario de solo VER no aparece ninguna acción de escritura; la matriz de MJ-06 sigue en verde.
- **Esfuerzo:** M. **Depende de:** MJ-05.

#### MJ-10 · Pruebas de mutación
- **Problema:** 94 % de cobertura dice qué líneas se ejecutan, no si una prueba falla cuando el código está mal.
- **Origen:** `faltasfe/scripts/mutar.mjs` y el agente `especialista-mutaciones` (StrykerJS con Vitest).
- **Pasos:**
  1. Backend: PIT (`pitest-maven` con `pitest-junit5-plugin`) acotado a `Code/**/*Service*` y al resolutor de
     permisos. Medir la línea de base.
  2. Front: StrykerJS con el runner de Vitest, acotado a `src/api/`, `src/navegacion/` y la lógica de
     `src/pantallas/**` (no estilos).
  3. Fijar el umbral después de medir (propuesta: no bajar de la línea de base; objetivo 70 % en servicios).
  4. Correrlo en un workflow semanal y a pedido, no en cada push (es lento).
- **Archivos:** `app/pom.xml` (perfil `mutacion`), `shiro-fe/stryker.config.json`, workflow `mutacion.yml`.
- **Verifica:** informe de mutantes sobrevivientes en los servicios críticos, con pruebas agregadas para los que
  importan.
- **Esfuerzo:** M (configuración) + L (cerrar sobrevivientes). **Depende de:** —.

#### MJ-11 · Pruebas de punta a punta con Playwright
- **Problema:** el front prueba componentes, pero nada recorre un flujo completo contra el backend real.
- **Origen:** revisión; `qa-visual` de faltasfe como referencia de qué medir.
- **Pasos:**
  1. `@playwright/test` en `shiro-fe/e2e/`.
  2. Flujos: ingreso; crear proyecto; crear tarea en modo rápido; mover por arrastre; editar y forzar el 409 con dos
     contextos; archivar y restaurar; clonar eligiendo qué se copia; generar un token MCP; usuario con solo VER.
  3. Cada flujo en computadora (1280×800) y teléfono (390×844).
  4. Job de la integración continua que levanta el backend con la imagen de MJ-15 y los seeds, y corre los flujos.
- **Archivos:** `shiro-fe/e2e/`, `playwright.config.ts`, `calidad.yml`.
- **Verifica:** los nueve flujos en verde en los dos tamaños.
- **Esfuerzo:** L. **Depende de:** MJ-15.

#### MJ-12 · Migraciones probadas sobre datos de la versión anterior
- **Problema:** Liquibase se prueba sobre base vacía. Después del corte, cada migración corre sobre datos reales.
- **Origen:** skill `backend-design:migration-safety`.
- **Pasos:**
  1. Job que restaura un volcado chico de la última versión publicada (generado por los seeds y algunos datos de
     prueba, sin datos reales), corre la app nueva y verifica health y smoke.
  2. Regla en `docs/rules/base-de-datos.md` (o criterio situacional): cambios destructivos en dos pasos
     (agregar → migrar datos → quitar en otra versión).
- **Archivos:** workflow, `docs/criterios/migraciones-sobre-datos.md` (nuevo).
- **Verifica:** el job detecta una migración que rompe datos existentes (probar una vez).
- **Esfuerzo:** M. **Depende de:** MJ-15.

#### MJ-13 · Herramientas MCP: contenido de terceros y límite de llamadas
- **Problema:** la descripción y los comentarios de una tarea los escribe otra persona y llegan al LLM del usuario
  como parte de la respuesta. Un texto con instrucciones puede influir en un agente con permisos de escritura.
- **Origen:** criterio `docs/criterios/herramientas-mcp.md` del propio repo.
- **Pasos:**
  1. En las herramientas que devuelven texto libre, envolverlo en un campo explícito (por ejemplo
     `contenidoDeUsuario`) y agregar a la descripción de la herramienta que ese campo es dato, no instrucciones.
  2. Límite de llamadas por token (propuesta: 120 por minuto) con 429.
  3. Revisar que todas las acciones de alto impacto exijan `confirmar: true` (archivar, restaurar, cambiar permisos,
     publicar versión, cerrar sprint).
  4. Sumarlo al criterio `herramientas-mcp.md`.
- **Archivos:** `Code/Mcp/`, `docs/criterios/herramientas-mcp.md`.
- **Verifica:** pruebas de MCP del 429 y de `confirmar`.
- **Esfuerzo:** M. **Depende de:** decisión D-3 de §5.

#### MJ-14 · Vencimiento de tokens MCP
- **Problema:** un token filtrado sirve hasta que alguien lo revoca.
- **Pasos:** campo `vence` al crear (propuesta: 90 días por defecto, elegible 30, 90 o 180); el filtro rechaza
  vencidos; la pantalla de tokens muestra el vencimiento y avisa a 7 días.
- **Verifica:** prueba con un token vencido → 401.
- **Esfuerzo:** S. **Depende de:** decisión D-3 de §5.

#### MJ-16 · Tope de tamaño de página
- **Problema:** no está confirmado que los endpoints de paginar limiten el tamaño. Un pedido por MCP podría traer
  todas las tareas.
- **Pasos:** confirmar en core-crud; si no hay tope, aplicar uno en Shiro (propuesta: 100) y responder 400 por encima;
  prueba de contrato.
- **Esfuerzo:** S. **Depende de:** —.

#### MJ-17 · Agente `auditor-seguridad` en el flujo de cada cambio
- **Origen:** `faltasfe/.claude/agents/auditor-seguridad.md` y la regla de §3 de
  `faltasfe/docs/roadmap/verificacion-acotada.md`.
- **Pasos:**
  1. Adaptar el agente a los dos repos de Shiro: Spring Security, permisos de recurso, MCP, tokens, Liquibase y
     React (storage, Markdown, sesión con cookie).
  2. Disparadores: el cambio toca autenticación, permisos, MCP, tokens, CORS, cabeceras, dependencias, secretos o
     configuración, datos personales, Markdown o archivos. Ante la duda, se audita.
  3. Semáforo: VERDE sigue; AMARILLO se repara y se informa; ROJO frena el commit.
  4. Mencionarlo en el `CLAUDE.md` de cada repo.
- **Archivos:** `.claude/agents/auditor-seguridad.md` en `shiro-be` y `shiro-fe`.
- **Esfuerzo:** S. **Depende de:** —.

#### MJ-18 · Controles chicos del front tomados de faltasfe
- **Origen:** `faltasfe/scripts/test-con-tope.mjs`, `verificar-environments.test.mjs`, `.prettierrc`,
  `.editorconfig`, `.git-blame-ignore-revs`.
- **Pasos:**
  1. `scripts/test-con-tope.mjs`: corta `vitest run` si pasa de 10 minutos y sale con 124.
  2. `scripts/verificar-configuracion.test.mjs`: falla si un archivo versionado de configuración o de pruebas trae
     claves del seed fuera de `src/pruebas/`.
  3. Prettier + `.editorconfig`; un commit de formato aislado y su hash en `.git-blame-ignore-revs`.
  4. `format:check` en la integración continua.
- **Esfuerzo:** S. **Depende de:** —.

#### MJ-19 · Pruebas en dos niveles
- **Origen:** opción B de `bod-programador-faltas` (24/11/2026).
- **Pasos:** documentar en `docs/rules/testing.md` (o como excepción, si se aparta de la regla del template):
  antes de cada commit, las pruebas de lo tocado (`-Dtest=` con las clases que usan lo cambiado, buscadas con grep);
  antes del PR, la suite completa, que es la que regenera `docs/api` y el catálogo de permisos. La integración
  continua sigue corriendo todo.
- **Esfuerzo:** S. **Depende de:** —.

### Ola 3 — Proceso y documentación

#### MJ-20 · `CLAUDE.md` y convenciones de `shiro-fe`
- **Problema:** el front no tiene reglas propias: cada sesión de agente parte de cero.
- **Origen:** `faltasfe/CLAUDE.md` y `faltasfe/docs/convenciones-frontend.md`.
- **Pasos:**
  1. `shiro-fe/CLAUDE.md`: mismas reglas generales que `shiro-be` (preguntar antes, no improvisar, citar la regla),
     protocolo de sesión (leer → implementar → probar → seguridad si corresponde → documentar → reportar) y enlaces.
  2. `docs/convenciones-frontend.md`, a partir de lo que el código ya hace: capas (`api/` → `pantallas/` →
     `componentes/`), estilos con los tokens del design system, permisos por MJ-09, errores por `codigo`, fechas,
     responsive (los dos tamaños del prototipo), accesibilidad, Markdown solo por `markdownSeguro`.
  3. Leerlo contra el código y marcar lo que no cumple como deuda, no corregirlo en esta tarea.
- **Esfuerzo:** M. **Depende de:** —.

#### MJ-21 · Estado, bitácora y errores recurrentes en el front
- **Origen:** `faltasfe/docs/roadmap/ESTADO.md`, `BITACORA.md`, `errores.md`, y `/inicio-sesion` y `/cerrar-sesion`.
- **Pasos:**
  1. `docs/ESTADO.md`: dónde quedó el trabajo, qué falta, qué espera aprobación (una página).
  2. `docs/errores.md`: formato `E-NN · fecha · síntoma` con causa, solución y cómo evitarlo; solo errores que pueden
     repetirse.
  3. Comandos `.claude/commands/inicio-sesion.md` (lee ESTADO, errores, `git status`, último commit) y
     `cerrar-sesion.md` (actualiza ESTADO y bitácora, versión, registro de tiempo).
  4. Lo mismo en `shiro-be`, reutilizando su `bitacora-del-programador.md`.
- **No se trae:** fichas por ítem, cola ni paralelización (le quedan grandes a Shiro).
- **Esfuerzo:** S. **Depende de:** MJ-20.

#### MJ-22 · Decisiones numeradas
- **Origen:** `faltas/backlog/decisions` y `faltasfe/docs/roadmap/decisiones.md`.
- **Pasos:** `docs/decisiones.md` en `shiro-be` con `D-NN · fecha · decisión · motivo · quién`; pasar las decisiones
  que hoy están en la bitácora, las adendas y las resoluciones 49 a 58 (con referencia a su origen); citar el id en
  commits y pruebas. Sin sumar Backlog.md (Shiro usa `.scratch/`).
- **Esfuerzo:** S. **Depende de:** —.

#### MJ-23 · `docs/usuarios-de-prueba.md`
- **Origen:** `faltas/docs/usuarios-de-prueba.md`.
- **Pasos:** tabla usuario → clave local → grupos → permisos sobre el proyecto de ejemplo → token de seed → para qué
  sirve (por ejemplo «probar que solo VER no ve acciones»). Agregar los usuarios que hagan falta para la matriz de
  MJ-06.
- **Esfuerzo:** S. **Depende de:** MJ-06.

#### MJ-24 · Menú de tareas locales para Windows
- **Origen:** `FALTAS FINALE/faltas-menu.bat` (14 opciones).
- **Pasos:** `SHIRO/shiro-menu.bat`, que funcione en `cmd` (sin PowerShell), con `PYTHONUTF8=1` adentro y la salida
  de cada opción guardada en `.scratch/` para que el agente de pruebas la lea:
  1. Levantar o actualizar backend · 2. Cargar seeds · 3. Ver logs del backend · 4. Smoke (health, versión, login,
  eco) · 5. Borrar la base y levantar de cero · 6. Levantar el front · 7. Pruebas del front · 8. Pruebas críticas del
  backend (con Docker) · 9. Suite completa del backend · 10. Estado de los dos repos · 11. Traer novedades (fetch y
  pull) · 0. Salir.
- **Esfuerzo:** S. **Depende de:** —.

#### MJ-25 · Verificación acotada
- **Origen:** `faltasfe/docs/roadmap/verificacion-acotada.md` §2 y §3, y `bod-programador-faltas` (modos COMPLETO,
  COMPACTO, OPCIONAL).
- **Pasos:** una tabla en el `CLAUDE.md` de cada repo con qué se vuelve a correr según lo que tocó un cambio:
  - Estilos o textos de una pantalla: build, lint y pruebas de esa pantalla.
  - Lógica de una pantalla o de un servicio: sus pruebas, mutación de lo tocado (MJ-10), build y lint.
  - `componentes/` compartidos, `api/`, navegación, sesión, dependencias, migraciones o seguridad: suite completa y
    `auditor-seguridad`.
  - Ante la duda, la fila más amplia.
- **No se trae:** el clasificador automático ni los snapshots con hashes (le quedan grandes a Shiro).
- **Esfuerzo:** S. **Depende de:** MJ-17.

#### MJ-26 · Agentes de QA del front
- **Origen:** `faltasfe/.claude/agents/qa-tester.md` y `qa-visual.md`.
- **Pasos:**
  1. `qa-tester`: build, lint, tipos, pruebas, mutación de lo tocado; veredicto con riesgos.
  2. `qa-visual`: compara la pantalla tocada con la vista del prototipo «Shiro — vistas v1» en computadora y
     teléfono; lista diferencias; no aprueba si no midió todo. El OK final lo da Leonardo.
- **Esfuerzo:** M. **Depende de:** MJ-20.

#### MJ-27 · Permisos del agente en `.claude/settings.json`
- **Origen:** `faltas/.claude/settings.json`.
- **Pasos:** en los dos repos, `allow` para `git add`, `commit`, `checkout`, `switch`, `branch` y las pruebas;
  `ask` para `git push`, `push --force`, `merge` a `develop`, `staging` o `main`, y cualquier borrado.
- **Esfuerzo:** S. **Depende de:** —.

### Ola 4 — Después del corte

#### MJ-28 · Retirar `AuthTokenConSha256Service` con el Core 4.1.0
- **Pasos:** cuando el devops publique 4.1.0: subir `core-parent`, borrar `AuthTokenConSha256Service` y
  `AuthTokenConSha256Config`, verificar que `UsuarioTokenConHashContratoTest` siga en verde y que los tokens
  existentes sigan autenticando (mismo algoritmo de hash, confirmarlo antes).
- **Esfuerzo:** S. **Depende de:** DevOps.

#### MJ-29 · Crecimiento de `shr_historial`
- **Pasos:** medir filas por mes después de la importación de Jira; si pasa de unos 5 millones, particionar por mes
  (`PARTITION BY RANGE (fecha)`) en una migración planificada; mantener `ix_shr_historial_entidad`; política de
  retención solo si el negocio la pide (el historial es la auditoría).
- **Esfuerzo:** M, solo si hace falta.

#### MJ-30 · Rotación y retención de logs
- **Origen:** `docs/arquitectura/logs/README.md` (puntos 1 y 4 de «Pendientes»).
- **Pasos:** `logging driver` de Docker con `max-size` y `max-file` en las plantillas de st7 y st8, o
  `RollingFileAppender` si los logs van al bind mount; retención acordada con el devops.
- **Esfuerzo:** S + devops.

---

## 4. Resumen

| Id | Mejora | Área | Ola | Esfuerzo | Depende de |
|---|---|---|---|---|---|
| MJ-01 | Respaldos y restauración probada | Integridad | O1 | M + devops | D-1 |
| MJ-02 | `@Version` en el 409 | Integridad | O1 | M | — |
| MJ-03 | Invariantes de la Spec 1 en la base | Integridad | O1 | M | — |
| MJ-04 | Límite de intentos de ingreso | Seguridad | O1 | M | D-2 |
| MJ-05 | `x-permiso` completo y catálogo generado | Seguridad | O1 | L | — |
| MJ-06 | Matriz de permisos | Seguridad / Testing | O1 | L | MJ-05 |
| MJ-07 | Cabeceras de seguridad y CSP | Seguridad | O1 | S + devops | — |
| MJ-15 | Imagen Docker y smoke en la integración continua | Testing | O1 | M | — |
| MJ-08 | Contrato generado en el front | Proceso / Testing | O2 | L | MJ-05 |
| MJ-09 | Front por permiso efectivo | Seguridad | O2 | M | MJ-05 |
| MJ-10 | Pruebas de mutación | Testing | O2 | M + L | — |
| MJ-11 | Playwright de punta a punta | Testing | O2 | L | MJ-15 |
| MJ-12 | Migraciones sobre datos anteriores | Integridad / Testing | O2 | M | MJ-15 |
| MJ-13 | MCP: contenido de terceros y límite | Seguridad | O2 | M | D-3 |
| MJ-14 | Vencimiento de tokens | Seguridad | O2 | S | D-3 |
| MJ-16 | Tope de página | Escalabilidad | O2 | S | — |
| MJ-17 | Agente `auditor-seguridad` | Proceso | O2 | S | — |
| MJ-18 | Controles chicos del front | Testing | O2 | S | — |
| MJ-19 | Pruebas en dos niveles | Testing | O2 | S | — |
| MJ-20 | `CLAUDE.md` y convenciones del front | Proceso | O3 | M | — |
| MJ-21 | Estado, bitácora y errores | Proceso | O3 | S | MJ-20 |
| MJ-22 | Decisiones numeradas | Proceso | O3 | S | — |
| MJ-23 | Usuarios de prueba | Proceso | O3 | S | MJ-06 |
| MJ-24 | Menú local para Windows | Proceso | O3 | S | — |
| MJ-25 | Verificación acotada | Proceso | O3 | S | MJ-17 |
| MJ-26 | Agentes de QA del front | Proceso | O3 | M | MJ-20 |
| MJ-27 | Permisos del agente | Proceso | O3 | S | — |
| MJ-28 | Retirar el servicio de hash con Core 4.1.0 | Seguridad | O4 | S | DevOps |
| MJ-29 | Crecimiento de `shr_historial` | Escalabilidad | O4 | M | Uso real |
| MJ-30 | Rotación de logs | Escalabilidad | O4 | S + devops | DevOps |

Estimación total del agente: unos 12 a 15 días de trabajo, más lo que corresponde al devops.

---

## 5. Decisiones que necesito de Leonardo antes de empezar

- [x] **D-1 Respaldos:** frecuencia, retención y destino (propuesta: diario, 14 + 8 semanales, fuera del servidor).
- [x] **D-2 Intentos de ingreso:** umbral y ventana (propuesta: 5 fallidos en 15 minutos, bloqueo de 15 minutos).
- [x] **D-3 Tokens MCP:** vencimiento por defecto (propuesta: 90 días) y límite de llamadas (propuesta: 120 por
      minuto).
- [x] **D-4 Contrato en el front:** versionar una copia del openapi en `shiro-fe` (propuesta) o leerlo de
      `../shiro-be` como faltasfe.
- [x] **D-5 Alcance de las olas:** aprobar O1 y O2 antes de la validación del 4/1, o mover parte de O2 a enero.
- [x] **D-6 Adenda:** MJ-05, MJ-08, MJ-09 y MJ-11 se registran como Adenda de implementación 2 del plan.

---

## 6. Riesgos

| Riesgo | Mitigación |
|---|---|
| O1 y O2 compiten con los ajustes pedidos en la validación | Las tareas son chicas e independientes; si hace falta, O2 se parte y MJ-10 y MJ-11 pasan a enero |
| MJ-03 encuentra datos que ya violan una invariante | Consulta previa en local y st8; corrección de datos en una migración propia antes de la restricción |
| MJ-02 rompe ediciones en curso del front | Mantener `fechaDeActualizacion` aceptada una versión más; front y backend se publican juntos |
| MJ-05 cambia la forma de `x-permiso` que usan otras herramientas de BOD | Consultar con el líder técnico si el formato propuesto choca con otros proyectos de la plantilla |
| La integración continua se vuelve lenta con Docker, Playwright y mutación | Mutación en un workflow semanal; Playwright solo en PR a `develop` |
| Exceso de proceso para el tamaño de Shiro | Se trae solo lo marcado; lo descartado está en §7 |

---

## 7. Lo que no se adopta y por qué

| De Faltas | Motivo |
|---|---|
| Los 18 agentes de faltasfe, fichas por ítem, cola, paralelización por worktrees, `analista-tiempos`, `evaluador-riesgo` | Faltas los necesita por tamaño, tres repos y cliente externo. Shiro es interno, con un implementador; alcanza con MJ-17, MJ-25 y MJ-26 |
| `limpiador-referencias` | Resuelve los IDs del DAF metidos en comentarios del código de Faltas; en Shiro no pasa |
| Backlog.md como gestor de tareas | Shiro ya usa `.scratch/` con estados (`docs/agents/issue-tracker.md`) |
| Clasificador automático de verificación y snapshots con hashes | Se trae la tabla de criterios (MJ-25), no la herramienta |
| Faltas sin integración continua | En esto Shiro va por delante y se mantiene |

---

## 8. Cómo arrancar

Mensaje sugerido para el agente implementador, con `shiro-be` y `shiro-fe` conectados:

> Sos el implementador de Shiro. Leé `docs/plan-de-mejoras.md` de shiro-be y las decisiones D-1 a
> D-6 de su §5 que ya están marcadas. Empezá por la Ola 1 en este orden: MJ-15, MJ-02, MJ-05, MJ-06, MJ-04, MJ-03,
> MJ-07 (la parte del repo) y MJ-01 (la parte del repo). TDD, +0.0.1 de versión por cambio, `/auditar` antes del PR,
> bitácora y registro de tiempo. No pases a la Ola 2 sin mi aprobación. Si una tarea choca con el DAF, la spec o el
> plan, frená y consultá.

### Verificación local de cada ola

Al cerrar cada ola, el agente de pruebas locales levanta todo en la PC de Leonardo y deja el informe en el proyecto
SHIRO. Comandos desde `shiro-be` en PowerShell:

```powershell
$env:PYTHONUTF8=1; python deploy/deploy.py --local 2>&1 | Tee-Object .scratch\deploy-local.log
```

```powershell
python deploy/deploy.py --local local shiro-be --seed 2>&1 | Tee-Object .scratch\deploy-seed.log
```

Desde `shiro-fe`:

```powershell
npm ci
npm run dev
```

(Cuando exista el menú de MJ-24, estos pasos pasan a ser opciones del menú.)
