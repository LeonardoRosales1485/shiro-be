# Identificadores Fijos del Proyecto

El deployer (`deploy/`) asume estos nombres. Son iguales en todos los proyectos y no se modifican nunca, ni al crear un proyecto nuevo desde el template.

## Qué no se cambia

- `groupId` `com.blue.open` en `app/pom.xml` y en los `pom.xml` del reactor.
- `artifactId` y `<name>` `open-data-proyect` en `app/pom.xml`.
- El paquete Java raíz `com.blue.open` (incluye `Code` y `Core`).

## Qué sí se personaliza en un proyecto nuevo

El nombre del proyecto, los puertos y las credenciales van en `deploy.yml` y `application.*.properties`. La versión de `app/pom.xml` sí cambia (ver [versionado.md](versionado.md)).

Si una tarea exige cambiar alguno de los identificadores fijos, el agente no lo ejecuta de entrada: informa que va en contra de esta regla y solo procede si el usuario insiste.
