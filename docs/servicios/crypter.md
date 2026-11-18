# Crypter

**Clase**: `com.blue.open.Core.Integracion.Crypter.CrypterService` (módulo `core-crypter`)

Integración externa para guardar y obtener archivos de forma privada, con opción de encriptarlos. Devuelve/recibe un `resourceKey` con el cual se referencia el archivo (ese es el valor que se persiste en la entidad/DTO, nunca el archivo en sí).

## Configuración

`application.properties`: `crypter.url`, `crypter.usuario`, `crypter.contrasena`, `crypter.tipo.documento`.

`estaConfigurada()` permite chequear en un smoke test de arranque si la integración está configurada, sin lanzar excepción.

## Cómo usarlo

```java
private final CrypterService crypterService;
```

La dependencia entra por constructor: campo `private final` y `@RequiredArgsConstructor` en la clase. Ver [`docs/rules/inyeccion-de-dependencias.md`](../rules/inyeccion-de-dependencias.md).

- `guardarArchivo(String base64)` — guarda un archivo encriptado. Devuelve el `resourceKey`.
- `guardarArchivo(String base64, boolean encriptar)` — permite guardar sin encriptar (más liviano/rápido, para info no confidencial o previews).
- `guardarArchivo(String base64, boolean encriptar, String nombreCarpeta)` — agrupa el archivo en una carpeta (debug: uuid, nº de expediente, dni, etc.). Sin especificar, usa la fecha del día.
- `guardarArchivo(String base64, boolean encriptar, String nombreCarpeta, String nombreColeccion)` — agrupa además en una colección (debug: año, área/oficina, etc.). Sin especificar, usa el año.
- `guardarArchivo(String base64, boolean encriptar, String nombreCarpeta, String nombreColeccion, String nombreArchivo)` — además guarda el nombre original del archivo (debug).
- `getArchivo(String resourceKey)` — obtiene la URL de un archivo por su `resourceKey`.
- `getArchivos(List<String> resourceKeys)` — obtiene varias URLs en una sola llamada.

Las carpetas no deberían tener más de ~2.000 archivos.
