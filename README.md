# Shiro (backend)

> Backend de Shiro, gestor de trabajo de Blue Open Data: API REST y servidor MCP sobre la misma capa de servicios.

## Stack

- Java
- Spring Boot
- Maven
- PostgreSQL 18
- Liquibase
- Docker

## Requisitos

- JDK 25, Maven, Docker y Python (para `deploy.py`). La primera compilación descarga el Core de `https://sm-soluciones.github.io/sm-core-maven` (público, sin token).
- Extensiones de VSCode: buscar `@recommended` en extensiones para ver las necesarias para debugear.

## Cómo ejecutarlo

```bash
python deploy/deploy.py --local
```

Detalle en `deploy/deploy.md` y en [`docs/rules/ejecucion-local.md`](docs/rules/ejecucion-local.md).

## Estructura del repositorio

- `app/`: código del proyecto (`Code/{Entidad}/...`). El Core no está acá: es una dependencia Maven publicada desde [`sm-core-maven`](https://github.com/SM-SOLUCIONES/sm-core-maven).
- `deploy/`: ejecución con Docker.
- `docs/`: documentación.
- `docs/api/`: contrato de la API (OpenAPI + Redoc), generado por las pruebas de contrato (`mvn -pl app test`).

## Versiones

La versión de la aplicación (`version`) y la del Core (`coreVersion`) se ven en `/info`.

## Documentación

| Qué buscás | Dónde |
|---|---|
| Reglas de trabajo (obligatorias) | [`CLAUDE.md`](CLAUDE.md) → [`docs/rules/`](docs/rules/) |
| Negocio del proyecto | [`docs/README.md`](docs/README.md) |
| Crear un proyecto nuevo desde el template | [`docs/arquitectura/nuevo-proyecto/README.md`](docs/arquitectura/nuevo-proyecto/README.md) |
| Cómo funciona el Core, las pruebas de contrato, Liquibase | [`docs/arquitectura/README.md`](docs/arquitectura/README.md) |
| Servicios reutilizables (mail, crypter, etc.) | [`docs/servicios/README.md`](docs/servicios/README.md) |
| Criterios situacionales | [`docs/criterios/README.md`](docs/criterios/README.md) |
| Auditoría de cumplimiento (checklist por id, `/auditar`, excepciones, Core publicado) | [`docs/auditoria/README.md`](docs/auditoria/README.md) · [`docs/excepciones.md`](docs/excepciones.md) |
| Changelog del Core | [`sm-core-maven/changelog/`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/changelog/README.md) |

## Trabajo con agentes

Las reglas de `CLAUDE.md` aplican por igual a todas las personas y agentes que trabajen en el repositorio.

## Configuración específica del proyecto

Cada proyecto completa esta sección con lo que sea propio: variables de entorno, integraciones externas y comandos de operación del servidor. Si crece, se mueve a `docs/` y se deja el link acá.

### Variables de entorno de Shiro

| Variable | Para qué | Si falta |
|---|---|---|
| `SHIRO_CIFRADO_FRASE` | Frase de la que sale la clave AES-256-GCM con que se cifra el token de Jira del importador | El backend arranca y el importador responde 503 `CIFRADO_NO_CONFIGURADO`. En local hay una frase de desarrollo por defecto |
| `GOOGLE_CLIENT_ID` | Client id OAuth de Google para el ingreso con Google | El ingreso con Google queda deshabilitado; se entra con cuentas de Shiro |
| `GOOGLE_DOMINIO` | Dominio de Google Workspace aceptado (por ejemplo `blueopendata.com`) | Igual que el client id |

**Cómo configurar `SHIRO_CIFRADO_FRASE`:**

1. Generar una frase larga y aleatoria, una por ambiente (no se reutiliza entre ambientes):

   ```bash
   openssl rand -base64 48
   ```

2. Cargarla como variable de entorno del contenedor del backend en el ambiente (st7, st8). En local, en `deploy.yml` (`SHIRO_CIFRADO_FRASE`) o como variable del sistema; si queda vacía, `application.local.properties` usa una frase de desarrollo.
3. No commitearla ni pegarla en tickets, chats o logs.
4. Si se cambia, los tokens de Jira guardados dejan de descifrarse (`CREDENCIAL_NO_DESCIFRABLE`): hay que volver a conectar cada importación.

### Pendiente de desarrollo

- **Client id de Google:** falta crear el cliente OAuth y cargar `GOOGLE_CLIENT_ID` y `GOOGLE_DOMINIO` en los ambientes (y `VITE_GOOGLE_CLIENT_ID` en el front). Hasta entonces el ingreso con Google está deshabilitado.
- **Puertos locales definitivos:** hoy son provisorios: backend 7090, Postgres 5090 y front 7091. Los asigna el líder técnico.

### Otros

- Si el proyecto usa `core-protocolo-sso` con Malvinas: [certificado](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-protocolo-sso/docs/certificados-malvinas.md).
