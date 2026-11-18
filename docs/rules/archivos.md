# Archivos e Imágenes

Cómo se reciben, dónde se guardan y cómo se devuelven los archivos (imágenes, documentos, adjuntos) desde `Code`.
Los servicios que lo resuelven están en el catálogo ([`docs/servicios/sm3.md`](../servicios/sm3.md) y
[`docs/servicios/crypter.md`](../servicios/crypter.md)); esto es la regla de cumplimiento obligatorio.

## Dónde se guarda

| El archivo es... | Se guarda con | La entidad persiste |
|---|---|---|
| **Público**: cualquiera con la URL puede verlo y no pasa nada (foto de perfil, logo de una empresa, imagen de un producto) | `SM3Service.subir(MultipartFile)` (`core-sm3`) | La URL pública, en una columna `String {campo}Url` (`fotoUrl`, `logoUrl`) |
| **Privado**: hay que controlar quién lo ve (documentos, comprobantes, DNI, contratos, cualquier cosa de un expediente) | `CrypterService.guardarArchivo(...)` (`core-crypter`) | El `resourceKey`, en una columna `String {campo}ResourceKey` (`contratoResourceKey`). `resourceKey` es el término de la API de Crypter y queda en inglés (fila "campos JSON que publica una API externa" de [idioma-y-estilo.md](idioma-y-estilo.md)) |

Ante la duda entre público y privado, es privado. Si el caso de uso no deja claro cuál es, el agente pregunta.

Está prohibido:

- **Guardar un archivo en el filesystem del contenedor** (`new File(...)`, `Files.write(...)`, una carpeta `uploads/`).
  El contenedor es efímero: el archivo se pierde en el próximo deploy y no existe en las otras réplicas.
- **Guardar el contenido en la base** (columna `BLOB`/`bytea` o base64 en un `VARCHAR`/`TEXT`). La base guarda la
  referencia (URL o `resourceKey`), nunca el contenido.
- **Guardar el contenido en una entidad o DTO** como `byte[]` o `String` base64 más allá del request que lo recibe.

## Cómo entra

- El endpoint recibe el archivo como `MultipartFile` (`consumes = MediaType.MULTIPART_FORM_DATA_VALUE`,
  `@RequestPart`), nunca como base64 dentro de un JSON: el límite de multipart no aplicaría, `413
  ARCHIVO_DEMASIADO_GRANDE` no se dispararía y el payload entero quedaría en memoria dos veces. Crypter recibe base64:
  la conversión la hace el `UseCase` justo antes de llamar a `CrypterService`, y no antes.
- El límite global es `spring.servlet.multipart.max-file-size` (20 MB en el template) y lo hace cumplir el Core con
  `413 ARCHIVO_DEMASIADO_GRANDE`. Si un caso de uso necesita un límite menor (por ejemplo 2 MB para una foto de
  perfil), lo valida el `UseCase` y lanza `ExcepcionDeApi(CodigoDeErrorKernel.ARCHIVO_DEMASIADO_GRANDE)` con el
  límite en `datos` (`limiteEnBytes`).
- **Cada caso de uso declara una lista cerrada de tipos permitidos** y valida el archivo contra ella: extensión y
  `Content-Type`, y para una imagen además que se pueda decodificar (`ImageIO.read` no devuelve `null`). Confiar solo
  en la extensión no alcanza. Un archivo fuera de la lista responde `400` con un código del proyecto
  (`TIPO_DE_ARCHIVO_NO_PERMITIDO` en `CodigoDeErrorDelProyecto`, con la lista permitida en `datos`).
- El nombre original (`getOriginalFilename()`) solo sirve para mostrarlo: si hace falta, se persiste en una columna
  `{campo}NombreOriginal`. Nunca se usa para armar una ruta ni como identificador. El nombre de almacenamiento lo
  decide SM3 o Crypter.
- Un archivo que se recibe y no se termina usando (falló la validación, falló la transacción) no se sube. La subida va
  **después** de las validaciones y lo más cerca posible del `save`: si la transacción falla después de subir, el
  `UseCase` borra lo que subió en el `catch`.

## Procesamiento

- Comprimir, recortar, redimensionar o convertir (imagen → miniatura, docx → pdf) se hace **antes de subir**, en el
  `UseCase` o en un service propio de la entidad (`Code/{Entidad}/`), nunca en el controller ni en la entidad.
- Es lógica sin datos externos: lleva test unitario (ver [testing.md](testing.md), que la nombra como ejemplo).
- Se sube una sola versión por campo. Si el caso de uso necesita el original y la miniatura, son dos campos
  (`fotoUrl`, `fotoMiniaturaUrl`), cada uno con su regla.

## Cómo sale

- El DTO responde **una URL que el front pueda usar**, nunca el contenido ni el `resourceKey`:
  - SM3: la misma URL persistida.
  - Crypter: la URL que devuelve `getArchivo(resourceKey)`. Para una lista, `getArchivos(List<String>)` en una sola
    llamada; está prohibido llamar a `getArchivo` por registro (es el N+1 de [consultas.md](consultas.md), contra una
    integración externa).
- Un archivo privado solo se resuelve a URL dentro de un caso de uso cuyo `@Contrato` y cuya verificación de alcance
  (dueño del registro, empresa) ya pasaron. El `resourceKey` no viaja al front ni se loguea.
- El campo del DTO que lleva una URL se llama igual que la columna (`fotoUrl`). El campo de un DTO que, por
  excepción, lleva el contenido de un archivo se llama `contenidoDelArchivo`, para que `SanitizadorDeLogs` lo tape
  (ver [logs.md](logs.md)).

## Reemplazo y borrado

- Reemplazar: se sube el nuevo, se persiste la referencia nueva y **después** se borra el anterior
  (`SM3Service.borrar`). Si el borrado del anterior falla, se loguea en `WARN` con la URL y no se revierte: un
  archivo huérfano es preferible a un registro sin archivo.
- Borrado lógico de la entidad (`status = -1`): el archivo **no** se borra, porque el registro es recuperable.
- Borrado físico de la entidad (hereda de `EntidadBase`): el `UseCase` o el `hookBorrar` borra el archivo después
  de que la transacción confirmó.

## Pruebas

- Cada caso de uso que recibe un archivo tiene su prueba de contrato con un archivo real (`Peticion` de `core-test`
  arma el multipart) para el caso feliz, el tipo no permitido y el tamaño excedido.
- `SM3Service` y `CrypterService` son integraciones externas: en la prueba de contrato se reemplazan por un mock
  del bean (`@MockitoBean`), nunca se llama al servicio real desde un test.
