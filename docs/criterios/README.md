# Criterios Situacionales

Reglas de cumplimiento obligatorio que **aplican solo si se da un disparador concreto**. Se diferencian de [`docs/rules/`](../rules/), que aplica siempre a todo el código, y de [`docs/arquitectura/`](../arquitectura/), que describe cómo funcionan las piezas del proyecto sin obligar a nada.

El índice de disparadores vive en [`CLAUDE.md`](../../CLAUDE.md), sección "Criterios situacionales", y **ese índice sí es de lectura obligatoria antes de programar**. Si el disparador matchea, el archivo de este directorio se lee y se cumple como cualquier regla de `docs/rules/`. Si no matchea, no hace falta abrirlo.

Razón de la separación: `docs/rules/` se lee entero antes de cada tarea. Un criterio que aplica una vez cada veinte tareas no puede vivir ahí sin obligar al agente a leer veinte documentos irrelevantes en cada cambio.

## Índice

| Criterio | Disparador |
|---|---|
| [versionado-de-api.md](versionado-de-api.md) | Escribir el primer controller de un proyecto nuevo; crear o modificar un endpoint cuyo consumidor no es el front propio; entregarle a un consumidor externo la URL de un endpoint que ya existe |

## Plantilla

Todo archivo de este directorio usa estas secciones, en este orden. El agente puede asumir que están:

```markdown
# {Nombre del criterio}

## Disparador
La condición exacta que activa este criterio, sin ambigüedad.

## Qué NO dispara esto
Los falsos positivos. Evita que el criterio se aplique de más.

## Norma
Qué se hace, en imperativo. Es la parte obligatoria.

## Cómo se implementa
El detalle técnico, con el criterio para elegir entre las alternativas posibles.

## Qué recomendarle al programador
Lo que el agente debe plantear aunque la decisión no sea suya (costos, riesgos,
consecuencias a futuro). El agente lo dice; el usuario decide.

## Cuándo preguntar
Los casos en los que el agente se detiene en vez de decidir por su cuenta.
```

## Agregar un criterio nuevo

1. Crear el archivo en este directorio con la plantilla de arriba.
2. Agregarlo al índice de este README.
3. Agregar la fila correspondiente a la tabla de "Criterios situacionales" de [`CLAUDE.md`](../../CLAUDE.md). **Sin este paso el criterio no existe**: el agente nunca abre el archivo porque no sabe que está.
4. Si el criterio cruza con una regla de `docs/rules/`, enlazarlo desde esa regla también.
