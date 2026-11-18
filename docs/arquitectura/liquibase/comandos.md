# Comandos de uso

## Changelog automatico de la aplicacion

La aplicacion ejecuta automaticamente:

```text
classpath:db/changelog/db.changelog-master.yaml
```

No hace falta pasar ningun comando especial para estructura y datos obligatorios. Requiere la dependencia `spring-boot-starter-liquibase` en `app/pom.xml`: desde Spring Boot 4 la autoconfiguracion de Liquibase esta en su propio modulo, y con solo `liquibase-core` el master no corre al arrancar.

## Ejecutar seed demo

El master incluye los changelogs de los modulos del Core (`db/changelog/core-identity.yaml`, etc.), que no estan en `app/` sino dentro del jar de cada modulo. Por eso una ejecucion manual tiene que tener esos jars en el `--search-path`. Desde la raiz del repositorio, primero se copian los jars del Core a `app/target/core-jars/` (sin la version en el nombre) y despues se corre el seed:

```bash
mvn -q -pl app dependency:copy-dependencies -DincludeGroupIds=com.blue.open -Dmdep.stripVersion=true -DoutputDirectory=target/core-jars
liquibase update \
  --search-path=app/src/main/resources,app/target/core-jars/core-identity.jar,app/target/core-jars/core-multiempresa.jar,app/target/core-jars/core-protocolo-jwt.jar,app/target/core-jars/core-protocolo-apikey.jar \
  --changelog-file=db/changelog/db.changelog-seed.yaml \
  --context-filter=seed
```

El `--search-path` tiene que listar exactamente los modulos que el proyecto incluye en `db.changelog-master.yaml` (si usa `core-monoempresa` en vez de `core-multiempresa`, cambiar ese jar).

En versiones viejas de Liquibase, `--contexts=seed` en lugar de `--context-filter=seed`.

Si se ejecuta el seed desde Maven o desde una herramienta externa, mantener la misma regla: changelog `db.changelog-seed.yaml`, contexto `seed` y los recursos de los modulos del Core en el classpath.

## Verificar antes de aplicar

Para inspeccionar SQL pendiente, el mismo comando con `update-sql` en lugar de `update`.

## Produccion

En produccion debe correr solo el master:

```text
db/changelog/db.changelog-master.yaml
```

No ejecutar `db.changelog-seed.yaml` en produccion salvo autorizacion explicita.

## Autoridad de ejecucion

El master (`db.changelog-master.yaml`) solo se ejecuta automaticamente al levantar la aplicacion via Spring Boot (`spring.liquibase.change-log`). Nadie corre `liquibase update` manual contra el master de un ambiente con la app corriendo (dev, staging o produccion); si la app arranca, el master ya corrio.

El seed (`db.changelog-seed.yaml`) es la unica excepcion: se ejecuta manualmente, siempre con `--context-filter=seed`, como se detalla arriba.
