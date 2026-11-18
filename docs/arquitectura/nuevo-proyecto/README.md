# Crear un proyecto nuevo desde el template

Guía para una persona o un agente que parte de este repositorio (template) y necesita dejar un proyecto nuevo corriendo. Es una guía de arquitectura, no una regla: las reglas de trabajo siguen estando en [`docs/rules/`](../../rules/).

## Convención de placeholders

Todo valor que cambia de un proyecto a otro está escrito en el template con una palabra que contiene `REMPLAZAR`. Para saber qué falta configurar, buscar esa palabra en todo el repositorio (excluyendo `docs/` y `target/`): cuando no quedan resultados, el proyecto está configurado.

| Placeholder | Qué es | Quién lo define |
|---|---|---|
| `NOMBRE_REMPLAZAR` | Nombre del proyecto. Tiene que ser el mismo nombre que el del repositorio de GitHub | Quien crea el proyecto |
| `PUERTO_REMPLAZAR` | Puerto del backend | Líder técnico |
| `PUERTO_POSTGRES_REMPLAZAR` | Puerto externo de PostgreSQL | Líder técnico |

Los puertos no se eligen: los asigna el líder técnico. Si no los tenés, pedilos antes de empezar.

`NOMBRE_REMPLAZAR` se usa como nombre del servicio, de la base, del usuario y de la contraseña de PostgreSQL. El mismo valor tiene que estar en `deploy.yml` y en `application.local.properties`, o el backend local no conecta con la base.

## Qué no se modifica

`groupId`, `artifactId`, `<name>` y el paquete `com.blue.open`. El deployer los asume. Ver [`docs/rules/identificadores-fijos.md`](../../rules/identificadores-fijos.md).

## Pasos

Todas las rutas son relativas a la raíz del repositorio (`backend-v2`).

### 1. Configurar el deploy (`deploy.yml`)

Reemplazar en el bloque `local`:

- `NOMBRE_REMPLAZAR-be` → `{nombre}-be`.
- `port: PUERTO_REMPLAZAR` → puerto del backend.
- `postgres_user`, `postgres_db`, `postgres_pass`: `NOMBRE_REMPLAZAR` → `{nombre}`.
- `postgres_external_port: PUERTO_POSTGRES_REMPLAZAR` → puerto de PostgreSQL.

Antes de tocar otros campos, leer [`deploy/deploy.md`](../../../deploy/deploy.md). Los bloques de servidor (`st7`, `malvinas4`) se agregan siguiendo esa documentación.

### 2. Configurar la aplicación

- `app/src/main/resources/application.local.properties` (para correr desde el IDE):
  - `server.port=PUERTO_REMPLAZAR` → el mismo puerto de `deploy.yml`.
  - `spring.datasource.url`: `PUERTO_POSTGRES_REMPLAZAR` → puerto de PostgreSQL, `NOMBRE_REMPLAZAR` → nombre de la base.
  - `spring.datasource.username` y `spring.datasource.password`: `NOMBRE_REMPLAZAR` → `{nombre}`.
- `app/src/main/resources/application.server.properties`:
  - `front.url` y `front.url.websocket`: `NOMBRE_REMPLAZAR` → `{nombre}`, que es la ruta del front en el servidor.
- Módulos opcionales, solo si el proyecto los usa (están comentados):
  - `sm3.proyecto` (módulo `core-sm3`).
  - `auth.sso.clientId` (módulo `core-protocolo-sso`).
  - Mail (`spring.mail.*`): completar con los datos del servidor de correo del proyecto. En local se puede usar un servidor de pruebas como Mailpit (`docker run -p 1025:1025 -p 8025:8025 axllent/mailpit`); las variables `SPRING_MAIL_*` se pueden declarar en `env:` de `deploy.yml`.
- Secretos: cambiar `jwt.secreto`, `captcha.secreto` y `core.identity.usuario-inicial.contrasena` por valores propios del proyecto antes de llevarlo a un servidor.

### 3. Poner la versión en 1.0.0

El proyecto nuevo empieza en la versión `1.0.0`, la misma en la que el template mantiene fija la suya (ver "Excepción: el template" en [`docs/rules/versionado.md`](../../rules/versionado.md)). Verificar que siga así:

- `app/pom.xml`: `<version>1.0.0</version>`. Es la única fuente de la versión del proyecto (ver
  [`docs/rules/versionado.md`](../../rules/versionado.md)).

La versión del `<parent>` de `app/pom.xml` (`core-parent`) es la del Core: no se toca al crear el proyecto. A partir de acá se aplica [`docs/rules/versionado.md`](../../rules/versionado.md).

### 4. Documentar el negocio

`docs/README.md` describe el negocio del template. Reemplazarlo por el del proyecto nuevo, porque de ahí salen las validaciones de los DTO. Lo mismo con el `README.md` de la raíz (descripción de una línea y sección "Configuración específica del proyecto").

`docs/excepciones.md` (desvíos registrados de las reglas, ver [`docs/auditoria/README.md`](../../auditoria/README.md)) arranca con la tabla vacía: el template no tiene excepciones y un proyecto nuevo tampoco.

### 5. Verificar que no quedó nada sin reemplazar

Buscar `REMPLAZAR` fuera de `docs/`. No debe haber resultados salvo en los bloques de módulos opcionales que el proyecto no usa (líneas comentadas).

## Correr el proyecto

Requisitos: Docker, Python, JDK y Maven (ver `README.md`).

```bash
python deploy/deploy.py --local
```

Levanta la base PostgreSQL y el backend, y ejecuta Liquibase (schema y datos requeridos). Opciones útiles, todas solo con `--local`:

```bash
python deploy/deploy.py --local local {nombre}-be --seed       # carga los seeds de ejemplo
python deploy/deploy.py --local local {nombre}-be --delete-db  # recrea la base desde cero
python deploy/deploy.py --local local {nombre}-be -f           # sigue los logs
```

Comprobar que arrancó:

- `http://localhost:{puerto}/api/actuator/health` responde `UP`.
- `http://localhost:{puerto}/api/actuator/info` muestra `version: 1.0.0` y `coreVersion`.

Para probar los endpoints, correr las pruebas de contrato (`mvn -pl app test`, ver [`testing/README.md`](../testing/README.md)); el contrato de la API queda en `docs/api/index.html`. Usuario inicial: `sysadmin` con la contraseña de `core.identity.usuario-inicial.contrasena`.

## Checklist para agentes

Un agente que reciba "crear un proyecto nuevo" debe:

1. Preguntar al usuario el nombre del proyecto, y los puertos asignados por el líder técnico. No inventar puertos.
2. Reemplazar cada `REMPLAZAR` según los pasos 1 y 2.
3. Poner la versión en `1.0.0` (paso 3).
4. No modificar `groupId`, `artifactId`, `<name>` ni el paquete `com.blue.open`.
5. No modificar la carpeta `deploy/` (solo `deploy.yml`).
6. Verificar con la búsqueda del paso 5 y correr el proyecto.
7. Correr `mvn -pl app test -Dtest='CorePublicadoTest,ArquitecturaTest,EstiloDeCodigoTest'`: tiene que quedar en verde antes del primer commit (ver "Reglas verificadas por la suite" en [`docs/rules/testing.md`](../../rules/testing.md)).
8. Pedir confirmación antes de commitear (ver [`docs/rules/git.md`](../../rules/git.md)).
