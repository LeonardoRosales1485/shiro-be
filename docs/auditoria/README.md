# Auditoría de cumplimiento

Cómo se verifica que el código de un proyecto cumple las reglas de la empresa **después** de escrito. Las reglas
viven en [`docs/rules/`](../rules/) y [`docs/criterios/`](../criterios/) y se leen antes de programar; esto es la
otra mitad: compararlas contra el código ya escrito, detectar desvíos y proponer cómo volver.

## Tres capas

| Capa | Qué verifica | Cuándo corre | Falla con |
|---|---|---|---|
| **Arranque** | `@Contrato` en todo endpoint, permisos que existen, proyecciones del ABM válidas | Al levantar la aplicación | La aplicación no arranca |
| **Suite de tests** (`mvn -pl app test`) | Lo que se puede verificar de forma determinística: que el Core llegue solo como dependencia publicada, dependencias entre paquetes, inyección por constructor, relaciones LAZY, Lombok prohibido, identificadores sin tildes, `@Descripcion` en códigos de error, completitud del contrato | En cada corrida de la suite y en CI | `BUILD FAILURE` |
| **Agente** (`/auditar`) | Lo que exige criterio: si un endpoint debía ser ABM o caso de uso, si una lógica debía ser un hook y no un parche al Core, si un log filtra datos, si un DTO expone de más, si un archivo va a SM3 o a Crypter | Antes de abrir un PR y antes de `develop → staging` (ver [`rules/git.md`](../rules/git.md)) | Un informe con hallazgos por id de regla |

El agente **no repite lo que ya verifica la suite**: lo da por hecho si la suite está en verde y se concentra en las
reglas marcadas como `agente` en el checklist. Lo que se puede volver determinístico se mueve a la suite, no al
agente.

## El checklist

[`checklist.md`](checklist.md) es la lista de reglas auditables, una por fila, con:

- **Id** estable (`DI-01`, `LOG-03`): es lo que cita el informe y lo que se registra en una excepción. No se renombra
  ni se reutiliza; una regla que deja de existir se marca como derogada.
- **Severidad**: `bloqueante` (no se mergea a `staging` con un hallazgo abierto) o `advertencia` (se corrige o se
  registra una excepción, pero no frena el merge).
- **Cómo detectarla**: la señal concreta en el código. Es lo que convierte una regla escrita para generar código en
  una regla que se puede verificar.
- **Verificada por**: `suite ({Test})`, `arranque` o `agente`.

Cada fila es un resumen: la regla completa, con su razón y sus ejemplos, sigue estando en el archivo de `rules/` que
la fila referencia. Si el checklist y la regla se contradicen, manda la regla y se corrige el checklist.

### Agregar o cambiar una regla

1. Escribir o modificar la regla en `docs/rules/` (o `docs/criterios/` si tiene disparador).
2. Agregar su fila al checklist, con id nuevo (el siguiente del prefijo), severidad y cómo detectarla.
3. Si se puede verificar de forma determinística, escribir el test en `app/src/test/java/com/blue/open/` (ver
   `ArquitecturaTest`, `EstiloDeCodigoTest`) y marcar la fila como `suite`.
4. Versionar el Core (ver [`rules/versionado.md`](../rules/versionado.md)): las reglas viajan con `core.version`, y un
   proyecto se audita con las reglas de la versión del Core que usa, no con las del template más nuevo.

## Excepciones

Un sistema grande se va a desviar de alguna regla con motivo. Para que el auditor no marque siempre lo mismo y la
gente no deje de leer el informe, la excepción se **registra**, no se ignora:

- Vive en [`docs/excepciones.md`](../excepciones.md), en la raíz del proyecto (fuera del Core: es
  propia de cada proyecto).
- Cada fila dice: id de la regla, alcance (archivo, carpeta o entidad), motivo, quién la aprobó y fecha.
- El auditor la lee antes de informar y omite los hallazgos cubiertos. Un hallazgo fuera del alcance registrado se
  informa igual.
- Una excepción no se registra para una regla `bloqueante` sin aprobación del líder técnico. El agente no la
  registra por su cuenta: la propone en el informe y la escribe solo cuando el usuario lo confirma.
- Las excepciones son señal para el Core: si varios proyectos registran la misma, la regla está mal o le falta un
  `criterio`. Al actualizar el Core se revisan.

## Core publicado

Desde Core 4.0.0 el proyecto no tiene el código del Core: `app/pom.xml` lo declara como dependencia (`core-parent` y
los `core-*`) y Maven lo descarga del repositorio público `https://sm-soluciones.github.io/sm-core-maven`. La regla
"el Core no se modifica desde un proyecto" ([`rules/controllers-y-endpoints.md`](../rules/controllers-y-endpoints.md)
→ "Alcance de modificación") deja de depender de la disciplina: no hay archivos del Core para editar.

`CorePublicadoTest` (`app`) verifica que siga así (CORE-01): falla si aparece un directorio `core/` en el proyecto o
una clase en el paquete `com.blue.open.Core` dentro de `app/src`. Copiar el Core al proyecto, o reimplementar una
clase suya con el mismo paquete, no es una salida: lo es la escalera de extensión (configuración, hook, puerto, no
usar el módulo, o issue al Core). Ver [ADR 0006](../adr/0006-core-publicado-como-artefacto.md).

Hasta 3.7.0 esto se verificaba con un manifiesto de hashes (`core/integridad.sha256` e `IntegridadDelCoreTest`), que
se eliminó en 4.0.0.

## Calibración

Antes de volver bloqueante el informe de `/auditar` en un proyecto que ya existe:

1. Correrlo sobre todo el proyecto (`/auditar todo`) y leer cada hallazgo.
2. Un falso positivo es un bug del checklist (la señal "cómo detectarla" está mal escrita) o de la regla (es
   ambigua): se corrige en el Core, no se registra como excepción.
3. Un hallazgo real que el proyecto no va a corregir ahora se registra en `docs/excepciones.md` con su motivo.
4. Recién con el informe limpio se agrega al gate de `develop → staging`.
