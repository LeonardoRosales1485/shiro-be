# Herramientas MCP

## Disparador
Crear o modificar un método anotado con `@McpTool` (Spring AI MCP), o la clase `HerramientasMcpDe{Entidad}` que lo contiene.

## Qué NO dispara esto
- Los endpoints REST que la herramienta reutiliza por debajo: siguen `controllers-y-endpoints.md` sin cambios.
- La configuración del transporte MCP (`Code/Common/Mcp/`: filtro, token de acceso), que es infraestructura transversal.

## Norma
- Una herramienta MCP **no es un endpoint de controller**: no lleva `@Contrato` y `InterceptorDeContrato` no la ve. La autorización que en la API va en `@Contrato` (rol o permiso fijo, por ejemplo "solo administradores") la exige la propia herramienta, al principio del método o de la rama, antes de leer o cambiar nada.
- La autorización que depende del registro (nivel VER, EDITAR o ARCHIVAR sobre un proyecto o tablero) se resuelve después de identificar el registro, con `ResolutorDePermisosDeRecurso.exigir(...)` o dentro del `UseCase` que la herramienta reutiliza, igual que en la API.
- Es la única excepción a "Prohibido autorizar con un `if` sobre un permiso fijo en el cuerpo del método" (`controllers-y-endpoints.md` → "Autorización: `@Contrato`"). La comprobación va en **un solo método privado** por clase (por ejemplo `exigirAdministrador()`), que delega en el resolutor de permisos del proyecto (`ResolutorDePermisosDeRecurso`) y lanza `ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO)`. Nunca un `if` repetido por herramienta.
- La herramienta exige **el mismo permiso** que el endpoint REST equivalente. Si cambia uno, cambia el otro en el mismo commit.
- La lógica no se duplica: la herramienta llama al `UseCase` o al `Service` que usa el endpoint REST, que ya aplica la autorización que depende de los datos (alcance por proyecto o tablero, dueño, etc).
- Exigen `confirmar: true` las acciones de la lista aprobada en `docs/README.md` (herramientas MCP, adenda 19 de `docs/adendas-propuestas.md`): archivar un proyecto o un usuario, asignar grupos, archivar un grupo, crear o archivar reglas de permiso, cerrar un sprint y publicar una versión. Sin `confirmar: true` responden la vista previa sin cambiar nada (`Confirmacion.confirmada`). Archivar o restaurar una tarea y restaurar en general no lo piden: se deshacen con la acción inversa. Una acción nueva que cambie permisos o afecte a muchos registros se suma a esa lista.
- Los errores se lanzan con `ExcepcionDeApi` y los códigos de siempre: el canal MCP los traduce a texto para el agente.

## Cómo se implementa
- `HerramientasMcpDe{Entidad}` vive en `Code/{Entidad}/` (regla 2 de `estructura-de-carpetas.md`): depende de esa entidad.
- El usuario es el del token MCP (`FiltroDeMcp`), así que `AuthService.getActualObligatorio()` funciona igual que en la API.
- Cada herramienta tiene prueba del caso feliz; las que exigen un rol fijo, además, del caso sin permiso; y las que exigen `confirmar`, del caso sin confirmar.

## Qué recomendarle al programador
- Si una herramienta nueva necesita un permiso que la API no tiene, conviene agregarlo primero a la API (con su `@Contrato`) y después reutilizarlo desde MCP, para no tener dos políticas.

## Cuándo preguntar
- Si la herramienta no tiene un endpoint REST equivalente y no está claro qué permiso exigir.
- Si una acción nueva podría sumarse a la lista de las que exigen `confirmar` y no está claro.
