# Servicios Reutilizables del Core

Catálogo de servicios e integraciones externas que trae el Core. Antes de crear un service nuevo, una integración externa o una función utilitaria, revisar acá si ya existe algo para reutilizar.

Cada uno vive en su propio módulo Maven: un servicio solo está disponible si el proyecto declara ese módulo en `app/pom.xml`. Ver [`docs/arquitectura/core/README.md`](../arquitectura/core/README.md).

Esto es un catálogo de referencia (qué ya existe), no una regla de cumplimiento obligatorio. Las reglas de trabajo están en [`docs/rules/`](../rules/).

| Servicio | Módulo | Para qué sirve |
|---|---|---|
| [Autenticación](autenticacion.md) | `core-identity` (+ `core-protocolo-*`) | Usuario logeado, roles/permisos, empresa actual, tokens de acceso, JWT |
| [Crypter](crypter.md) | `core-crypter` | Guardar/obtener archivos de forma encriptada y privada |
| [Mail](mail.md) | `core-mail` | Enviar emails (con o sin adjuntos) |
| [SM3](sm3.md) | `core-sm3` | Subir/borrar archivos públicos (imágenes, etc.) |
| [Captcha](captcha.md) | `core-captcha` | Verificar un token de hCaptcha |
| [Utils](utils.md) | `core-kernel` | Funciones utilitarias estáticas de propósito general |
