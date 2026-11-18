# Errores

Cómo se lanza y se documenta un error de la API. El mecanismo completo (`ExcepcionDeApi`,
`CodigoDeError`, `ErrorResponseDTO`, el handler) está descripto en
[`docs/arquitectura/errores/README.md`](../arquitectura/errores/README.md); esto es la regla de
cumplimiento obligatorio.

1. **Todo error lleva código.** Se lanza siempre `new ExcepcionDeApi(codigo, ...)`. No se crea una
   `ResponseEntity` de error a mano ni se lanza otra excepción esperando que el handler la convierta.
2. **El front discrimina por `codigo`, nunca por `mensajeParaUsuario`.** El texto se puede reformular sin avisar;
   el código no.
3. **Un código es estable.** Una vez publicado no se renombra, no se reutiliza para otra situación y no
   se borra mientras un cliente pueda recibirlo.
4. **Nombre del código.** Constante del enum en español, en `MAYUSCULAS_CON_GUION_BAJO`, sin tildes ni
   eñes (ver [`idioma-y-estilo.md`](idioma-y-estilo.md)). Describe la situación, no el lugar del código:
   `REGISTRO_NO_ENCONTRADO`, no `ABSTRACT_SERVICE_GET_BY_UUID`.
5. **Un código por situación que el cliente tenga que distinguir**, no uno por cada `throw`. Antes de
   crear uno nuevo, buscar si ya existe en el enum del módulo o del proyecto.
6. **Dónde vive el código.** Un error del Core va en el enum de su módulo
   (`CodigoDeErrorKernel`, `CodigoDeErrorCrud`, ...). Un error del proyecto va en
   `Code/Common/Errores/CodigoDeErrorDelProyecto.java`. El Core nunca usa un código del proyecto, y el
   proyecto no crea un enum por entidad: todos sus códigos comparten ese único enum.
7. **Cada constante lleva `@Descripcion`** (`core-kernel`) que diga cuándo recibe el front ese código, en
   vez del comentario `//`. Es la columna "Cuándo ocurre" del catálogo de errores del contrato
   (`docs/api`): se escribe para el programador del front (con tildes, es texto visible) y describe la
   situación, no el lugar del código. `CodigosDeErrorTest` (en `app`) recorre por reflection todos los
   `CodigoDeError` del classpath y falla nombrando el enum y la constante que no la tenga.
8. **`mensajeParaUsuario`** es para el usuario final: español, con tildes, sin datos internos. Si el mensaje
   por defecto del código alcanza, no se pasa otro.
9. **`mensajeParaDesarrollador` nunca viaja en la respuesta HTTP.** Es un campo interno de `ExcepcionDeApi` que
   solo se loguea (ver el README de arquitectura); `ErrorResponseDTO`, que es lo único que se serializa,
   no lo incluye. Aun así:
   - Nunca lleva secretos, tokens ni contraseñas, ni el valor que el cliente mandó si ese campo es
     sensible: pasa por `SanitizadorDeLogs` antes de escribirse en el log, pero eso es una red de
     seguridad, no un permiso.
   - Nunca interpola el mensaje crudo de una excepción de una librería o servicio externo
     (`e.getMessage()` de un cliente HTTP, un driver JDBC, una librería de cifrado) ni el body de una
     respuesta HTTP externa sin filtrar: esos mensajes suelen citar hosts, queries o estructura interna.
   - En un flujo de autenticación, nunca distingue "el usuario no existe" de "la credencial es
     inválida": ambos casos usan el mismo código y el mismo `mensajeParaUsuario`, para no facilitar
     enumeración de usuarios.
10. **`datos`** lleva solo lo que el cliente necesita para resolver el error. Claves en `camelCase`, en
    español, con valores de tipo simple (`String`, número, booleano, o colecciones/mapas de esos) — nunca
    una entidad o un DTO completo. Una clave que matchea el criterio de `SanitizadorDeLogs` se descarta
    sola (con un `warn` en el log): no hace falta filtrarla a mano, pero tampoco es un permiso para
    pasar secretos a propósito.
11. **Subclase de `ExcepcionDeApi`** solo si el error lleva datos obligatorios con tipo, o si hay que
    capturarlo por tipo. Fija su código en el constructor y vive en la carpeta de la entidad de la que
    depende (regla 2 de [`estructura-de-carpetas.md`](estructura-de-carpetas.md)).
12. **Unicidad.** El valor de `codigo()` no se repite entre ningún enum del Core ni el del proyecto:
    `CodigosDeErrorTest.ningunCodigoSeRepite` (`app/src/test/java/com/blue/open/`) recorre por reflection
    todos los `CodigoDeError` del classpath y falla nombrando el enum y la constante de cada duplicado. Por
    eso un código de una integración lleva su nombre (`CAPTCHA_NO_DISPONIBLE`, `MAIL_NO_DISPONIBLE`) en vez
    de uno genérico que otro módulo también usaría.
13. **Sesión vs. permiso.** Si la request no identifica a ningún usuario, el código es `NO_AUTENTICADO`
    (`401`), que ya lanzan `AuthService.getActualObligatorio()` y los autorizadores de `@Contrato`. `SIN_PERMISO`
    (`403`) queda reservado para un usuario autenticado al que le falta un rol, un permiso o el alcance
    (empresa, dueño del registro). Nunca se lanza `SIN_PERMISO` por falta de sesión. Ver
    [`docs/arquitectura/errores/README.md`](../arquitectura/errores/README.md) → "Autorización".
14. **Pruebas de contrato.** Cada caso de error tiene su prueba de contrato, que assertea `codigo` con
    `esperarError(status, codigo)` (ver [`testing.md`](testing.md)).
