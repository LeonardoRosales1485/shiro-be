# Arquitectura del Proyecto

Documentación de cómo funcionan las piezas transversales del proyecto (no reglas de cumplimiento obligatorio, eso está en [`docs/rules/`](../rules/); no servicios reutilizables para inyectar, eso está en [`docs/servicios/`](../servicios/); no criterios que aplican solo ante un disparador, eso está en [`docs/criterios/`](../criterios/)).

| Sistema | Documentación |
|---|---|
| Crear un proyecto nuevo desde el template | [nuevo-proyecto/README.md](nuevo-proyecto/README.md) |
| Core (módulos y puertos) | [core/README.md](core/README.md) |
| Base de datos (Liquibase) | [liquibase/README.md](liquibase/README.md) |
| Actuator (/health, /info, API-Version) | [actuator/README.md](actuator/README.md) |
| Sistema de logs (logback, @Trazar) | [logs/README.md](logs/README.md) |
| Sistema de errores (ExcepcionDeApi, handler) | [errores/README.md](errores/README.md) |
| Pruebas de contrato y Contrato de endpoint (`core-test`) | [testing/README.md](testing/README.md) |
| Comandos de Claude Code (`/test-completo`, `/auditar`) | [comandos/README.md](comandos/README.md) |
| Auditoría de cumplimiento (checklist por id, excepciones, Core publicado) | [../auditoria/README.md](../auditoria/README.md) |
| Glosario de renombres al español del Core (ADR 0002) | [glosario-espanol/README.md](glosario-espanol/README.md) |
