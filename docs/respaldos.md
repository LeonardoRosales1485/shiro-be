# Respaldos y restauración

MJ-01 del plan de mejoras y decisión D-1. «Nada se borra» protege contra borrados lógicos, no contra perder la base (disco, error de migración, volumen borrado). Este documento dice qué se respalda y cómo se restaura paso a paso.

## Qué se respalda y cuándo (D-1)

| Qué | Cómo | Frecuencia y retención | Dónde |
|---|---|---|---|
| La base de Shiro (`shiro` en st7 y st8) | `pg_dump -Fc` (formato propio de PostgreSQL, comprimido) | Diario; se guardan 14 diarios y 8 semanales | Fuera del servidor de la app |

- El job y el destino los arma el devops (fuera del repo). Este documento no los fija.
- `pg_dump` tiene que ser de la misma versión mayor que el servidor (PostgreSQL 18) o mayor. Lo más simple es correrlo dentro del contenedor de la base, que ya trae el de su versión.
- El respaldo es de una base: no incluye los roles del servidor. El rol de la aplicación (`SHIRO_DB_USUARIO_APP`, sin DELETE sobre `shr_`) no hace falta respaldarlo: la app lo crea y le vuelve a dar los permisos al arrancar (changeset `runAlways` de `2026-11-18-usuario-de-la-app.yaml`).
- El archivo tiene datos personales y credenciales cifradas: se guarda con el mismo cuidado que la base.

## Hacer un respaldo a mano

Con el nombre del contenedor de Postgres de ese ambiente (en local: `docker ps`):

```bash
docker exec <contenedor-postgres> pg_dump -U <postgres_user> -Fc -f /tmp/shiro.dump <postgres_db>
```

```bash
docker cp <contenedor-postgres>:/tmp/shiro.dump ./shiro-AAAA-MM-DD.dump
```

## Restaurar paso a paso

1. **Frenar la app** para que nadie escriba durante la restauración (el contenedor del backend del ambiente).
2. **Dejar una base vacía.** Si el servidor se perdió, levantar Postgres nuevo con el mismo `postgres_user` y `postgres_db` (en local, `python deploy/deploy.py --local local shiro-be --delete-db` deja la base vacía). Si la base existe con datos rotos, borrarla y crearla de nuevo:

   ```bash
   docker exec <contenedor-postgres> dropdb -U <postgres_user> <postgres_db>
   ```

   ```bash
   docker exec <contenedor-postgres> createdb -U <postgres_user> <postgres_db>
   ```

3. **Restaurar el archivo.** `--no-owner --no-privileges` evita que falle si el servidor nuevo todavía no tiene el rol de la aplicación; la app le vuelve a dar los permisos al arrancar. `--exit-on-error` corta en el primer error en vez de dejar una base a medias:

   ```bash
   docker cp ./shiro-AAAA-MM-DD.dump <contenedor-postgres>:/tmp/shiro.dump
   ```

   ```bash
   docker exec <contenedor-postgres> pg_restore -U <postgres_user> -d <postgres_db> --no-owner --no-privileges --exit-on-error /tmp/shiro.dump
   ```

4. **Contar antes de arrancar**, para comparar con lo que había (si se conoce) o con el respaldo anterior:

   ```bash
   docker exec <contenedor-postgres> psql -U <postgres_user> -d <postgres_db> -At -c "SELECT 'tareas', count(*) FROM shr_tarea UNION ALL SELECT 'historial', count(*) FROM shr_historial UNION ALL SELECT 'usuarios', count(*) FROM auth_usuarios"
   ```

5. **Arrancar la app** como siempre (en local, `python deploy/deploy.py --local`). Liquibase ve los changesets ya aplicados, crea el rol de la aplicación si falta y le aplica los permisos. Si la app es de una versión más nueva que el respaldo, corre solo los changesets que faltan.
6. **Verificar** con el smoke del ambiente y entrando a la app:

   ```bash
   python deploy/smoke.py <ambiente>
   ```

## Prueba de restauración en local (26/11/2026)

Hecha con la versión 1.0.60, `postgres:18.4` y el jar del backend corriendo contra la base:

1. Base A nueva, la app la migró, y por la API se creó un proyecto y tres tareas.
2. Conteo: 3 tareas, 4 filas de historial, 1 usuario, 1 proyecto. `pg_dump -Fc` (142 KB).
3. Se borró el contenedor de la base A (simula perder el servidor, incluido el rol de la app).
4. Servidor B nuevo y vacío, sin el rol de la app: `pg_restore --no-owner --no-privileges --exit-on-error` terminó sin errores y el conteo dio igual.
5. La app arrancó contra B (recreó el rol y los permisos), `python deploy/smoke.py local` dio OK con la versión 1.0.60, y la API listó las 3 tareas. El conteo final coincidió con el del paso 2.

Falta el paso 5 del plan: el simulacro en st8 con la base importada de Jira, antes del corte.
