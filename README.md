# API de aplicación

> Descripción de una línea de qué hace este backend.

## Stack

- Java
- Spring Boot
- Maven
- MariaDB
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

- Si el proyecto usa `core-protocolo-sso` con Malvinas: [certificado](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/core-protocolo-sso/docs/certificados-malvinas.md).
