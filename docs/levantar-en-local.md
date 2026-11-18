# Levantar Shiro en local

Guía para correr el backend (shiro-be), su base PostgreSQL 18 y el front (shiro-fe) en una PC de desarrollo.
Estado: F0 (base del proyecto). Los puertos son provisorios hasta que el líder técnico asigne los de Shiro.

| Pieza | Cómo corre | Puerto en tu PC |
|---|---|---|
| PostgreSQL 18.4 | Contenedor Docker (lo levanta el deployer) | 5090 |
| Backend | Contenedor Docker (lo levanta el deployer) | 7090 (`/api`) |
| Front | `npm run dev` (Vite), reenvía `/api` al backend | 7091 |

## 1. Requisitos

- Git.
- Docker Desktop (o Docker Engine con `docker compose`), encendido.
- Python 3.10 o superior (el deployer solo usa la biblioteca estándar).
- Node 22 o superior (front).
- Opcional, para correr las pruebas del backend fuera de Docker: JDK 25. Maven no hace falta: el repositorio trae `mvnw`.

La primera compilación necesita internet: baja el Core de `https://sm-soluciones.github.io/sm-core-maven` (público,
sin token) y las dependencias de Maven Central y npm.

## 2. Clonar los repositorios

La rama de trabajo de F0 es `claude/jolly-newton-jqin42` en los dos repositorios.

```bash
git clone -b claude/jolly-newton-jqin42 https://github.com/LeonardoRosales1485/shiro-be.git
```

```bash
git clone -b claude/jolly-newton-jqin42 https://github.com/LeonardoRosales1485/shiro-fe.git
```

## 3. Backend y base de datos

Desde la carpeta `shiro-be`:

```bash
python deploy/deploy.py --local
```

En Windows también sirve `deploy\local.bat`. La primera vez tarda varios minutos: construye la imagen (Maven dentro
de Docker) y crea la base. El deployer lee el bloque `local` de `deploy.yml`.

Para cargar además los datos de ejemplo (usuarios `demo`, `sysadmin-pruebas` y `cliente`, todos con clave `123`):

```bash
python deploy/deploy.py --local local shiro-be --seed
```

Comprobá que arrancó (tiene que responder `UP` y la versión):

```bash
curl http://localhost:7090/api/actuator/health
```

```bash
curl http://localhost:7090/api/actuator/info
```

Otras opciones útiles:

```bash
python deploy/deploy.py --local local shiro-be -f
```

```bash
python deploy/deploy.py --local local shiro-be --delete-db
```

La primera sigue los logs (Ctrl+C para salir). La segunda borra la base y la recrea desde cero.

### Qué pasa con la base

Liquibase corre con el usuario dueño (`shiro`/`shiro`) y crea el usuario de la aplicación `shiro_app`, que no puede
borrar filas de las tablas `shr_` (regla "nada se borra"). La aplicación se conecta con `shiro_app`. Podés conectarte
con cualquier cliente de PostgreSQL a `localhost:5090`, base `shiro`.

## 4. Front

Desde la carpeta `shiro-fe`:

```bash
npm ci
```

```bash
npm run dev
```

Abrí http://localhost:7091. Con el backend levantado tiene que decir "Backend conectado: versión 1.0.2"; si dice que
no se pudo conectar, el backend no está en el puerto 7090.

## 5. Probar a mano

Ingresar con el usuario inicial (`sysadmin`, clave `123` en local). Responde un JWT:

```bash
curl -X POST http://localhost:7090/api/usuario/iniciar-sesion -H "Content-Type: application/json" -d "{\"nombreDeUsuario\":\"sysadmin\",\"contrasena\":\"123\"}"
```

Llamar la herramienta MCP de eco (en F0 `/api/mcp` todavía no pide token; eso llega en T3.1):

```bash
curl -X POST http://localhost:7090/api/mcp -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" -d "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"eco\",\"arguments\":{\"texto\":\"hola\"}}}"
```

También podés conectar un cliente MCP (por ejemplo Claude Code) a `http://localhost:7090/api/mcp` con transporte
HTTP.

## 6. Correr las pruebas

Backend (JDK 25; no necesita Docker, usa un PostgreSQL embebido):

```bash
./mvnw verify
```

Front:

```bash
npm test
```

## 7. Apagar

```bash
docker stop shiro-be shiro-be-postgres
```

## Problemas comunes

- **Puerto ocupado (5090, 7090 o 7091):** cambiá el puerto en `deploy.yml` (backend y base) o en `vite.config.ts`
  (front) y avisá, porque los definitivos los asigna el líder técnico.
- **El build de Docker no baja dependencias:** revisá que Docker tenga salida a internet (proxy corporativo o VPN).
- **"Could not resolve placeholder 'SHIRO_DB_CONTRASENA_APP'" u otra variable:** falta esa variable en `env:` de
  `deploy.yml`. El backend no arranca sin `JWT_SECRETO`, `SHIRO_DB_USUARIO_APP` ni `SHIRO_DB_CONTRASENA_APP`.
