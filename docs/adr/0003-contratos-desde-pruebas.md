# Contrato de la API generado desde pruebas de contrato, sin Bruno

Los endpoints se prueban y se documentan con **pruebas de contrato** (`core-test`): tests JUnit in-process (`@SpringBootTest` + MockMvc) sobre un PostgreSQL embebido con master + seeds de Liquibase y rollback por test. Cada request valida la respuesta real contra el schema del DTO y registra el ejemplo; la corrida completa ensambla `docs/api/openapi.yaml` + `index.html`, que se commitea. Los metadatos salen de `@Contrato` (controller) y `@Descripcion` (campos de DTO y parámetros); los casos "sin autenticación" y "sin permiso" los genera `core-test` desde el permiso declarado. `ContratosCompletosTest` falla si un endpoint publicado no tiene prueba.

Bruno deja de existir: la carpeta `bruno/` se borra, y las reglas (`testing.md`, `documentacion.md`, `git.md`, `errores.md`) pasan a exigir pruebas de contrato. Los gates de `develop → staging` y `staging → main` son la suite completa en verde más `deploy/smoke.py` contra el ambiente desplegado.

## Considered Options

- Seguir con Bruno: exigía Docker y la base local levantada, cada test de modificación dejaba datos y la sección `docs` de cada `.bru` era prosa escrita a mano, sin garantía de coincidir con la respuesta real.
- Spring REST Docs / `restdocs-api-spec`: compatibilidad incierta con Spring Boot 4.
- Anotaciones Swagger u OpenAPI escritas a mano: el contrato no quedaría probado.
