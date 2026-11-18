# SM3

**Clase**: `com.blue.open.Core.Integracion.Sm3.SM3Service` (módulo `core-sm3`)

Integración externa para subir/borrar archivos públicos (por ejemplo imágenes), a diferencia de [Crypter](crypter.md) que guarda archivos privados/encriptados. Devuelve una URL pública.

## Configuración

`application.properties`: `sm3.url`, `sm3.usuario`, `sm3.contrasena`, `sm3.proyecto`. `estaConfigurada()` permite chequear en un smoke test de arranque si está configurado.

## Cómo usarlo

```java
private final SM3Service sm3Service;
```

La dependencia entra por constructor: campo `private final` y `@RequiredArgsConstructor` en la clase. Ver [`docs/rules/inyeccion-de-dependencias.md`](../rules/inyeccion-de-dependencias.md).

- `subir(MultipartFile archivo)` — sube un archivo y devuelve su URL pública.
- `borrar(BorrarArchivoRequest request)` — borra un archivo por su URL pública. Devuelve `true` si se borró correctamente.
