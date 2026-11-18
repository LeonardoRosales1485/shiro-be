# Reglas para Agentes

Este documento y los archivos que enlaza son de cumplimiento obligatorio. Todas las personas que trabajen con un agente en este repositorio deben seguir exactamente las mismas reglas, sin margen de interpretación distinta.

Si el usuario pide algo que va en contra de una regla, el agente no lo ejecuta de entrada: primero le informa que la petición va en contra de una regla de la empresa y le cita el archivo y la sección de la regla afectada (por ejemplo, `docs/rules/consultas.md` → "Proyección por defecto"). Solo si el usuario insiste, el agente puede proseguir.

Si una situación no está cubierta explícitamente por estas reglas, el agente debe detenerse y preguntar al usuario antes de decidir por su cuenta. No se debe improvisar ni asumir el criterio "más razonable".

Antes de empezar a trabajar, el agente debe hacer todas las preguntas necesarias para entender exactamente lo que el usuario está pidiendo. No se debe asumir el alcance, la entidad, el caso de uso o el criterio a aplicar si hay ambigüedad: se pregunta primero y se programa después.

Antes de programar, el agente debe leer todos los archivos de `docs/rules/` y tomar sus reglas como inmutables.

El Core ya trae servicios reutilizables para autenticación, envío de archivos (Crypter, SM3), envío de emails, verificación de captcha y funciones utilitarias. Antes de crear un service nuevo, una integración externa o una función utilitaria, el agente debe revisar @docs/servicios/README.md para reutilizar lo que ya esté implementado en vez de duplicarlo.

Cómo funcionan las piezas transversales del proyecto (el Core, las pruebas de contrato, Liquibase, y lo que se vaya sumando) está centralizado en @docs/arquitectura/README.md.

El Core es un conjunto de módulos Maven publicados desde el repositorio `sm-core-maven` (no está en este repositorio), del que cada proyecto usa solo los que necesita. Antes de asumir que algo del Core es obligatorio, o de querer modificarlo, leer @docs/arquitectura/core/README.md: ahí están los módulos, los puertos y la escalera de extensión.

## Reglas

- Ejecución del proyecto: @docs/rules/ejecucion-local.md
- Git: @docs/rules/git.md
- Versionado: @docs/rules/versionado.md
- Identificadores fijos (groupId, artifactId, paquete): @docs/rules/identificadores-fijos.md
- Idioma y estilo de código: @docs/rules/idioma-y-estilo.md
- Estructura de carpetas: @docs/rules/estructura-de-carpetas.md
- Entidades y DTOs: @docs/rules/entidades-y-dtos.md
- Controllers, Services y Endpoints: @docs/rules/controllers-y-endpoints.md
- Inyeccion de dependencias: @docs/rules/inyeccion-de-dependencias.md
- Base de datos (Liquibase): @docs/rules/base-de-datos.md
- Consultas a la base de datos (JPA): @docs/rules/consultas.md
- Errores: @docs/rules/errores.md
- Integraciones externas y entorno: @docs/rules/integraciones-y-entorno.md
- Testing: @docs/rules/testing.md
- Documentación: @docs/rules/documentacion.md
- Logs: @docs/rules/logs.md
- Archivos e imágenes: @docs/rules/archivos.md

## Criterios situacionales

Reglas obligatorias que aplican **solo si se da su disparador**. A diferencia de `docs/rules/`, no se leen todas antes de cada tarea: lo que se lee siempre es esta tabla. Si el disparador matchea, el archivo se abre y se cumple como cualquier regla de `docs/rules/`; si no matchea, no hace falta abrirlo.

| Si estás por... | Leé |
|---|---|
| Escribir el primer controller de un proyecto nuevo (hay que decidir si es el backend de un front propio o una API) | `docs/criterios/versionado-de-api.md` |
| Crear o modificar un endpoint cuyo consumidor no es el front propio (app móvil, integración de un tercero, otro backend, API pública) | `docs/criterios/versionado-de-api.md` |
| Entregarle a un consumidor externo la URL de un endpoint que ya existe | `docs/criterios/versionado-de-api.md` |

Cómo agregar un criterio nuevo y qué secciones debe tener: `docs/criterios/README.md`.

## Auditoría

Las reglas de arriba se leen antes de programar; después de programar se verifican. Cómo funciona está en @docs/auditoria/README.md.

- Cada regla verificable tiene un id y una severidad en `docs/auditoria/checklist.md`. Los hallazgos, las excepciones y las conversaciones sobre una regla la citan por ese id (`DI-01`, `LOG-04`).
- Lo determinístico lo verifica la suite (`CorePublicadoTest`, `ArquitecturaTest`, `EstiloDeCodigoTest`, `CodigosDeErrorTest`, `ContratosCompletosTest`): un test que falla por una regla cita su id y el archivo de la regla. No se desactiva ni se parchea el test: se corrige el código, o se registra una excepción.
- Lo que exige criterio lo verifica el comando `/auditar` (`.claude/commands/auditar.md`): antes de abrir un PR y antes de `develop → staging`. Solo informa; no corrige.
- Un desvío con motivo se registra en `docs/excepciones.md` (id, alcance, motivo, quién aprobó). El agente no agrega filas por su cuenta: las propone y el usuario confirma.
- El Core no está en el proyecto: se descarga como dependencia Maven publicada (`sm-core-maven`). No se copia ni se reimplementa en el proyecto; `CorePublicadoTest` falla si aparece `core/` o una clase en `com.blue.open.Core` (CORE-01). La salida es la escalera de extensión.

## Agent skills

### Issue tracker

Issues y specs viven como archivos markdown locales bajo `.scratch/`. Las convenciones están en @docs/agents/issue-tracker.md.

El uso de estados es obligatorio: el agente marca el ticket como `claimed` antes de empezar a trabajarlo y le cambia el estado al terminar, como indica la sección "Status" de ese archivo.

### Domain docs

Single-context: `CONTEXT.md` + `docs/adr/` en la raíz de `backend-v2` (se crean de forma perezosa con `/domain-modeling`). Ver @docs/agents/domain.md
