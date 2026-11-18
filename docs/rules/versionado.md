# Versionado

- Después de cualquier modificación, se debe aumentar el número de versión en `<version>` de `app/pom.xml` en +0.0.1. Es la única fuente: Maven la escribe en `app-version.properties` (`app.version`) durante el build, y de ahí la leen `/info` y el header `API-Version`. No se escribe a mano en código.
- Si ya se incrementó la versión y ese cambio no fue commiteado todavía, no se debe volver a incrementar.
- La versión debe cambiar en cada push. Nunca se repite ni retrocede.
- El backend debe tener versionado independiente, visible en `/info`.

### Excepción: el template (`sm-template`)

- En el template, la versión de `app/pom.xml` queda **fija en `1.0.0`**: no se incrementa con cada modificación ni con cada push. Es la versión con la que arranca todo proyecto creado desde el template.
- Las reglas de +0.0.1 por modificación y de "cambia en cada push, nunca se repite ni retrocede" aplican a los proyectos creados desde el template, a partir de su `1.0.0` (ver `docs/arquitectura/nuevo-proyecto/README.md`).
- La versión del Core (`core.version`) no tiene excepción: en el template también se versiona con semver (ver "Versión del Core").

## Versión del Core

- El Core vive en su propio repositorio, [`SM-SOLUCIONES/sm-core-maven`](https://github.com/SM-SOLUCIONES/sm-core-maven), y se publica como repositorio Maven público en `https://sm-soluciones.github.io/sm-core-maven`. Se versiona aparte del proyecto, con semver.
- El proyecto declara la versión del Core en un solo lugar: el `<parent><version>` de `app/pom.xml` (`com.blue.open:core-parent`). De ahí salen `core.version` y la versión de todas las dependencias `core-*`, que no llevan `<version>` propia.
- Actualizar el Core es cambiar esa versión y seguir los pasos de su changelog (`sm-core-maven/changelog/{version}.md`). Nunca se baja de versión.
- Cambio que rompe compatibilidad → major. Módulo o puerto nuevo → minor. Arreglo → patch. Cómo se publica una versión: README de `sm-core-maven` → "Publicar una versión".
- La versión no se escribe a mano en código: `CoreVersion.get()` la lee de `core-version.properties`, que Maven completa al compilar el Core.
- El Core no se parchea desde un proyecto: el proyecto no tiene su código. Ver la escalera de extensión en [`sm-core-maven/changelog/README.md`](https://github.com/SM-SOLUCIONES/sm-core-maven/blob/main/changelog/README.md).

## Exposición vía Actuator

- Actuator debe estar habilitado en todos los proyectos backend.
- Se deben exponer como mínimo los endpoints `/health` e `/info`.
- `/info` debe responder la versión actual de la aplicación (`version`) y la del Core (`coreVersion`).
- Todas las respuestas HTTP deben incluir el header `API-Version` con la versión actual (formato ejemplo: `1.4.3`), coincidente con `/info`.

## Versión de build vs. versión de contrato

Todo lo de arriba es la **versión de build**: qué código está corriendo. Cambia en cada push y no le promete nada a quien consume la API.

El **versionado de contrato** (`/api/v1/...` en la URL) es otra cosa: congela la forma de un endpoint para los consumidores que no se despliegan junto con este backend. El header `API-Version` no lo reemplaza. Cuándo hace falta y cómo se implementa: `docs/criterios/versionado-de-api.md`.
