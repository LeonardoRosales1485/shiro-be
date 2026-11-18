# El Core se publica como artefactos Maven y deja de copiarse en cada proyecto

**Estado: aceptado e implementado en Core 4.0.0.** Ver "Decisión" al final.

## Contexto

Desde Core 2.0.0 el Core son módulos Maven versionados juntos, pero cada proyecto los lleva **copiados** en su
`core/` y los compila en el mismo reactor que `app`. Eso hace que editar el Core desde un proyecto sea trivial: es
un archivo más del repo. La regla "el Core no se modifica desde un proyecto" existe, con su escalera de extensión
(configuración → hook → puerto → no usar el módulo → issue al Core), pero nada la hace cumplir, y en los sistemas
grandes el Core terminó parcheado por proyecto. Con eso se pierde lo que el Core garantiza: un solo formato de logs,
una sola forma de guardar archivos, un solo manejo de errores, y la posibilidad de actualizar todos los proyectos a
la vez.

## Propuesta original

Publicar cada módulo `core-*` como artefacto en un repositorio Maven privado de la empresa (GitHub Packages del
org, o un Nexus/Artifactory propio), con su `core.version`. Un proyecto declara en `app/pom.xml` las dependencias
que hoy declara, con la misma `core.version`, y **el directorio `core/` deja de existir en el repositorio del
proyecto**: solo existe en el repositorio del Core (este template, o uno separado).

Consecuencias:

- Editar el Core desde un proyecto pasa a ser imposible, no prohibido: la escalera de extensión es el único camino.
- Actualizar el Core es cambiar `core.version` y leer el changelog, como cualquier otra dependencia.
- `IntegridadDelCoreTest` y `core/integridad.sha256` dejan de ser necesarios y se borran.
- Las reglas (`core/docs/`) tienen que seguir llegando al proyecto: se publican como un artefacto más
  (`core-docs`, un zip que el build descomprime en `target/core-docs/`), o el template las deja en el proyecto
  en una carpeta `docs/core/` que `IntegridadDelCoreTest` sigue verificando contra la versión declarada.
  Este punto hay que decidirlo antes de implementar: `CLAUDE.md` y `/auditar` las leen por ruta.
- Un proyecto que necesita un cambio del Core sigue el flujo actual (issue, versión nueva, changelog); mientras
  tanto no puede parchear. Para que eso sea viable, el Core tiene que publicar versiones con más frecuencia que
  hoy, y el flujo "copiado desde {proyecto}" que aparece en el historial se formaliza: el cambio se hace en el
  repositorio del Core, se publica, y el proyecto sube de versión.

## Opciones consideradas

- **Dejar el Core copiado y confiar en la regla.** Es lo que hay, y no alcanzó.
- **Manifiesto de integridad (lo vigente).** Detecta la modificación en la suite y la hace visible en el PR, pero
  no la impide: regenerar el manifiesto es un comando. Sirve como puente.
- **Git submodule para `core/`.** Impide editar sin querer, pero cualquier `git` dentro del submodule lo permite, y
  los submodules complican el deploy y a los agentes. Descartado.
- **Artefactos Maven (esta propuesta).** Es lo que el README del Core ya anuncia como "paso siguiente" en
  "Distribución". Requiere infraestructura (registry y credenciales en `deploy/` y en CI) y decidir cómo viajan las
  reglas.

## Decisión

Implementado en Core 4.0.0 (ver `sm-core-maven/changelog/4.0.0.md`):

- El Core vive en su propio repositorio, [`SM-SOLUCIONES/sm-core-maven`](https://github.com/SM-SOLUCIONES/sm-core-maven),
  y se publica en un repositorio Maven **público** servido por GitHub Pages
  (`https://sm-soluciones.github.io/sm-core-maven`) en vez de GitHub Packages: GitHub Packages exige token para
  descargar paquetes Maven aunque sean públicos, y con Pages ningún desarrollador, CI ni build Docker necesita
  credenciales. El código del Core no tiene credenciales ni datos de clientes; el único default sensible (`jwt.secreto`)
  se quitó en la misma versión.
- **Cómo viajan las reglas**: la documentación general (reglas, arquitectura, auditoría, criterios, servicios y estos
  ADRs) pasa a `docs/` del proyecto, que es dueño de ella; el template es la referencia. En `sm-core-maven` queda solo
  el código de cada módulo y su documentación de implementación (`core-*/docs/`), más el changelog.
- `IntegridadDelCoreTest`, `core/integridad.py` y `core/integridad.sha256` se borraron. CORE-01 pasa a
  `CorePublicadoTest`: el proyecto no tiene `core/` ni clases en `com.blue.open.Core`.
