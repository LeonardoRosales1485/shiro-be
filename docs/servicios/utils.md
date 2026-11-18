# Utils

**Clase**: `com.blue.open.Core.Kernel.Util.Utils` (módulo `core-kernel`)

Funciones estáticas de propósito general, para no repetir validaciones/helpers básicos en cada service.

- `Utils.esTextoValido(String texto)` — `true` si el texto no es `null` ni está vacío/blank.

Si se necesita un helper genérico nuevo (no específico de una entidad puntual), no se agrega al Core (no se modifica desde un proyecto): va en `Code/Common/{Concern}/` como método estático, y si sirve para todos los proyectos se propone como issue al Core.
