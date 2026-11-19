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
| BD-04 | `db/changelog/changes/2026-11-18-historial.yaml` (`shr_historial`) | Solo la escribe el sistema, en la misma transacción de cada cambio: los seeds de las demás tablas no pasan por los casos de uso, y una fila sembrada a mano mostraría un historial que no ocurrió. Se prueba creando datos por los endpoints | Arquitecto | 19/11/2026 |
| BD-04 | `db/changelog/changes/2026-11-18-operaciones-de-archivo.yaml` (`shr_operacion_archivo`) | Solo la escribe el sistema al archivar un proyecto o tablero en cascada; sembrarla exigiría sembrar también registros archivados coherentes. Se prueba archivando por los endpoints | Arquitecto | 19/11/2026 |
