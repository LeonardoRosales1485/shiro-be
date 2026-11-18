# Git

- Flujo de ramas: `develop` -> `staging` -> `main`.
- Los commits deben ser lo más chicos posibles.
- El mensaje del commit debe explicar claramente qué se cambió.
- El agente debe pedir confirmación explícita antes de: pushear o mergear. Nunca se asume autorización previa como válida para una acción posterior.
- El agente nunca debe hacer push, salvo que la tarea lo solicite explícitamente en ese momento.

## Antes de mergear `develop` -> `staging`

Preguntarles al usuario si quiere ejecutar los test.
Test:
- Suite completa en verde: `mvn -pl app test` (unitarios + pruebas de contrato + completitud + reglas verificables). Ver [testing.md](testing.md).
- `docs/api` regenerado por esa corrida y commiteado.
- `/auditar` sin hallazgos bloqueantes (o con cada uno registrado en `docs/excepciones.md`). Ver [`docs/auditoria/README.md`](../auditoria/README.md).

## Antes de pasar de `staging` -> `main`

Preguntarles al usuario si quiere ejecutar los test.
Test:
- Suite completa en verde: `mvn -pl app test`.
- Smoke del ambiente desplegado: `python deploy/smoke.py <ambiente>` (ver `deploy/deploy.md`).
