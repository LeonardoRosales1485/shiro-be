# Integraciones Externas, Entorno y Arranque

## Fechas del servidor

- Todas las fechas se manejan en UTC-3, forzado siempre y validado al iniciar la aplicación.
- Se debe validar que la base de datos y el servidor estén en UTC-3 dentro de los smoke test.

## Integraciones externas

- Toda integración externa que dependa de otro contenedor, servidor o servicio (que pueda estar apagado o fallar) debe:
  - Validarse al iniciar la aplicación como smoke test.
  - Ejecutarse dentro de un `try/catch`.

## Variables de entorno

- Toda variable de entorno usada debe admitir `null` y tener una validación manual con mensaje de error personalizado.
  - Si la variable es necesaria para el correcto funcionamiento de la aplicación debe cortar la ejecución del java.

## Validación de conexiones al iniciar

- Al iniciar la aplicación se debe validar el estado de las conexiones externas (base de datos, microservicios, APIs, etc).
- Ante un error de conexión se debe manejar con logs y alertas.
  - Si es necesario para el correcto funcionamiento de la aplicación debe cortar la ejecución del java.
