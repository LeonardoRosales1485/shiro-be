# La autorización de cada endpoint se declara una sola vez, en `@Contrato`

Hasta Core 2.5.1, el permiso de un endpoint vivía en tres lugares que podían contradecirse:

- `authService.valid(...)` en el cuerpo del método, que autorizaba.
- `@Contrato(permiso)`, que solo documentaba.
- En los endpoints heredados, `GuardiaDeEndpoint` + `hookXxxAuth`, que autorizaban, y `MetadatosDeOperacion` en el test, que documentaba.

Las pruebas de contrato detectaban un endpoint que no exigía lo declarado, pero no un permiso distinto. Ahora `@Contrato.permiso` es obligatorio y autoriza. Lo hace cumplir `InterceptorDeContrato`, a través del puerto `AutorizadorDeEndpoint`, antes de ejecutar el método. La aplicación no arranca si un endpoint no lo declara o nombra un rol o permiso inexistente.

`AbstractController` se elimina y pasa a ser una receta (`core-crud/docs/receta-controller.md`). Un controller heredado escondía sus endpoints, que no podían llevar `@Contrato`, y obligaba a un segundo mecanismo de autorización. `AbstractService` se conserva: es lógica real (paginado, borrado, auditoría) que no conviene regenerar en cada proyecto.

`tienePermisos(...)` sobrevive como única API programática, para la autorización que depende de los datos: alta vs. edición en el mismo `/save`, alcance por empresa, una configuración privada. Esas reglas no se pueden expresar con una anotación estática.

## Considered Options

- **`@PreAuthorize` de Spring Security.** Es SpEL que habría que parsear para documentar, exige poblar el `SecurityContext` (hoy `AuthService` resuelve el usuario por protocolos) y obliga a dos anotaciones por endpoint.
- **Una anotación propia `@Permiso` aparte de `@Contrato`.** Son dos anotaciones que hay que mantener alineadas: es el mismo problema que se quería resolver.
- **Mantener `AbstractController` con `@Contrato` en los métodos heredados.** El permiso quedaría fijo en el Core para todas las entidades, o habría que volver a declararlo en el test.
- **Prohibir también `tienePermisos`.** Los casos que dependen de los datos tendrían que partirse en endpoints artificiales, como la configuración pública o privada por clave.
