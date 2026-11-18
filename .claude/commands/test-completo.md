---
description: Instala el Core, corre la suite completa (mvn -pl app test) y resume el resultado
---

# Test completo

Todos los comandos se ejecutan desde la raíz del repositorio. Son largos: usar el timeout máximo o correrlos en segundo plano y esperar a que terminen.

## 1. Instalar el Core

```bash
mvn install -DskipTests -q
```

Si falla, la suite no corre: saltar al resumen con el error de compilación (módulo, archivo y línea) como único fallo.

## 2. Correr la suite

```bash
mvn -pl app test > app/target/test-completo.log 2>&1
```

Sin `-Dtest`: la corrida completa es la que ejecuta `ContratosCompletosTest` y regenera `docs/api` (ver `docs/arquitectura/testing/README.md`, "Publicación").

Completo cuando el log termina en `BUILD SUCCESS` o `BUILD FAILURE`.

## 3. Resumir

Del final de `app/target/test-completo.log`:

- **Totales**: la línea `Tests run: N, Failures: F, Errors: E, Skipped: S` de la sección `Results:` (la agregada, no la de cada clase).
- **Duración**: la línea `Total time:`.
- **Fallos**: las secciones `[ERROR] Failures:` y `[ERROR] Errors:` listan `Clase.metodo:linea`. La causa de cada uno está en `app/target/surefire-reports/{paquete}.{Clase}.txt`.

Si el build cortó antes de los tests (compilación, arranque de Maven), no hay totales: reportar ese error.

Responder con este formato, con cada test fallido en la tabla:

```
**Resultado:** BUILD SUCCESS | BUILD FAILURE
**Tests:** N corridos · F fallados · E con error · S salteados
**Duración:** mm:ss

| Test | Causa |
|---|---|
| `Clase.metodo` | primera línea útil del assert o de la excepción |
```

Sin fallos, la tabla se omite.
