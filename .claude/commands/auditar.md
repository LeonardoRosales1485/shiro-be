---
description: Compara el código del proyecto contra las reglas de la empresa (checklist de auditoría) y reporta los desvíos con propuesta de corrección
---

# Auditar

Verifica que el código cumple las reglas de `docs/rules/` y `docs/criterios/` **después** de escrito. No
corrige nada: informa, y el usuario decide. Cómo funciona el sistema completo (capas, severidades, excepciones):
`docs/auditoria/README.md`.

## Alcance

El argumento del comando decide qué se audita:

| Argumento | Alcance |
|---|---|
| (vacío) | Los archivos que cambian en `git diff develop...HEAD` más los no commiteados (`git status`). Si la rama es `develop`, los cambios desde el último merge a `staging` |
| `todo` | Todo `app/`, `docs/`, `deploy.yml` y `pom.xml`. Es lento y caro: se usa al calibrar un proyecto existente (ver "Calibración" en el README de auditoría) |
| una ruta (`app/src/main/java/com/blue/open/Code/Factura`) | Solo esa carpeta o archivo |

Nunca se audita el Core: llega como dependencia publicada (`sm-core-maven`) y se da por correcto. Que el proyecto no lo copie lo verifica `CorePublicadoTest`.

## Pasos

### 1. Leer, en este orden

1. `docs/auditoria/checklist.md` entero. Es la lista de lo que se verifica, con id y severidad.
2. La tabla "Criterios situacionales" de `CLAUDE.md`. Si algún disparador matchea con el alcance (por ejemplo un
   endpoint cuyo consumidor no es el front propio), abrir ese criterio y sumarlo al checklist de esta corrida.
3. `docs/excepciones.md`. Cada fila cubre un id de regla en un alcance: un hallazgo cubierto no se informa.
4. `docs/README.md` (negocio del proyecto) y `CONTEXT.md` (vocabulario): hacen falta para ENT-05 e IDI-01.
5. Para cada fila del checklist que aplique y cuya señal no quede clara con el resumen, abrir el archivo de
   `docs/rules/` que encabeza su sección. La regla completa manda sobre el checklist.

### 2. Correr lo determinístico

Si el alcance incluye código Java o changelogs:

```bash
mvn -pl app test -Dtest='CorePublicadoTest,ArquitecturaTest,EstiloDeCodigoTest,CodigosDeErrorTest' -q
```

Es rápido (sin contexto de Spring). Cada fallo se informa tal cual, con el id que el mensaje del test cita. Las filas
del checklist marcadas `suite` **no se vuelven a revisar a mano**: si la suite pasó, se dan por cumplidas. Si el
comando falla por compilación o por Maven, se informa eso como único hallazgo y se sigue con el paso 3 igual.

### 3. Revisar lo que exige criterio

Para cada archivo del alcance, recorrer las filas del checklist marcadas `agente` que apliquen a ese tipo de archivo
(un controller no se revisa contra las de Liquibase). Para cada hallazgo:

- Confirmarlo abriendo el archivo y la regla completa. Un hallazgo que no se puede señalar en una línea concreta no
  se informa como hallazgo: va en "Dudas".
- Verificar que no esté cubierto por `docs/excepciones.md` (mismo id y alcance que incluye el archivo).
- Escribir la propuesta: qué cambiar para cumplir la regla, citando la sección de la regla y, cuando exista, el
  mecanismo del Core que la resuelve (un hook, el `Proyector`, `@Coleccion`, `SM3Service`, etc.).

Prioridad cuando el alcance es grande: primero CORE-*, después las `bloqueante`, después las `advertencia`.

### 4. Informar

Responder exactamente con este formato:

```
**Alcance:** {qué se auditó: rama/diff, `todo` o la ruta} · {N} archivos
**Suite determinística:** PASÓ | FALLÓ ({M} tests) | NO CORRIDA (motivo)
**Hallazgos:** {B} bloqueantes · {A} advertencias · {E} cubiertos por excepción (omitidos)

| Id | Sev | Archivo:línea | Qué está mal | Propuesta |
|---|---|---|---|---|
| DI-01 | B | `Code/Factura/FacturaService.java:21` | `@Autowired` sobre el campo `repository` | `private final` + `@RequiredArgsConstructor` (inyeccion-de-dependencias.md → "Caso por defecto") |

**Dudas** (no son hallazgos; el usuario decide):
- ...

**Excepciones propuestas** (solo si el usuario pide registrar una; no se escriben sin confirmación):
- ...
```

Orden de la tabla: CORE-* primero, después bloqueantes, después advertencias; dentro de cada grupo, por archivo.
Sin hallazgos, la tabla se omite y se dice "Sin hallazgos". Un mismo problema repetido en varios archivos es una
fila por archivo (el usuario tiene que poder ir a cada uno), salvo que sean más de 10: entonces una fila con la
lista de archivos.

## Qué no hace este comando

- No modifica código ni `docs/excepciones.md`. Si el usuario pide corregir un hallazgo, es otra tarea, con las
  reglas de siempre (incluida la de versionado).
- No audita el Core ni propone cambiarlo. Si un hallazgo solo se resuelve tocando el Core, la propuesta es "issue al
  Core" con el punto de extensión que falta (ver "Alcance de modificación" en `controllers-y-endpoints.md`).
- No reemplaza a `/test-completo`: la suite completa (pruebas de contrato incluidas) sigue siendo el gate de
  `develop → staging`. Este comando corre solo los tests determinísticos para no tardar.
