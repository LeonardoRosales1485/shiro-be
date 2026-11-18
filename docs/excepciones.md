# Excepciones a las reglas

Desvíos registrados de este proyecto respecto de las reglas de `docs/rules/` y `docs/criterios/`. Cómo
funciona el mecanismo (quién aprueba, qué puede ser excepción y qué no) está en
[`docs/auditoria/README.md`](auditoria/README.md) → "Excepciones".

`/auditar` lee este archivo antes de informar y omite los hallazgos cubiertos: mismo id de regla y archivo dentro
del alcance. Lo que no está acá se informa.

Reglas para esta tabla:

- Una fila por regla y alcance. El alcance es un archivo, una carpeta de entidad (`Code/Factura/`) o `app/` entero;
  cuanto más chico, mejor.
- El motivo dice por qué la regla no aplica o no conviene acá, no "porque sí" ni "por tiempo".
- Una regla `bloqueante` (ver el checklist) solo se exceptúa con aprobación del líder técnico, y la columna
  "Aprobó" lo nombra.
- Una excepción se revisa al actualizar el Core: si la regla cambió o apareció un `criterio` que la cubre, la fila
  se borra.
- El agente no agrega filas por su cuenta: las propone en el informe de `/auditar` y las escribe solo cuando el
  usuario lo confirma.

| Id | Alcance | Motivo | Aprobó | Fecha |
|---|---|---|---|---|
| | | | | |

El template no tiene excepciones. Un proyecto nuevo arranca con esta tabla vacía.
