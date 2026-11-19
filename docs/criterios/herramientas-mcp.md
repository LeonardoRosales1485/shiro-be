# Herramientas MCP

## Disparador
Crear o modificar un método anotado con `@McpTool` (Spring AI MCP), o la clase `HerramientasMcpDe{Entidad}` que lo contiene.

## Qué NO dispara esto
- Los endpoints REST que la herramienta reutiliza por debajo: siguen `controllers-y-endpoints.md` sin cambios.
- La configuración del transporte MCP (`Code/Common/Mcp/`: filtro, token de acceso), que es infraestructura transversal.

## Norma
- Una herramienta MCP **no es un endpoint de controller**: no lleva `@Contrato` y `InterceptorDeContrato` no la ve. La autorización que en la API va en `@Contrato` la exige la propia herramienta, al principio del método, antes de leer o cambiar nada.
- Es la única excepción a "Prohibido autorizar con un `if` sobre un permiso fijo en el cuerpo del método" (`controllers-y-endpoints.md` → "Autorización: `@Contrato`"). La comprobación va en **un solo método privado** por clase (por ejemplo `exigirAdministrador()`), que delega en el resolutor de permisos del proyecto (`ResolutorDePermisosDeRecurso`) y lanza `ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO)`. Nunca un `if` repetido por herramienta.
- La herramienta exige **el mismo permiso** que el endpoint REST equivalente. Si cambia uno, cambia el otro en el mismo commit.
- La lógica no se duplica: la herramienta llama al `UseCase` o al `Service` que usa el endpoint REST, que ya aplica la autorización que depende de los datos (alcance por proyecto o tablero, dueño, etc).
- Toda acción destructiva o masiva (archivar, asignar grupos, cambiar reglas de permiso) exige `confirmar: true`; sin eso responde la vista previa sin cambiar nada (`Confirmacion.confirmada`).
- Los errores se lanzan con `ExcepcionDeApi` y los códigos de siempre: el canal MCP los traduce a texto para el agente.

## Cómo se implementa
- `HerramientasMcpDe{Entidad}` vive en `Code/{Entidad}/` (regla 2 de `estructura-de-carpetas.md`): depende de esa entidad.
- El usuario es el del token MCP (`FiltroDeMcp`), así que `AuthService.getActualObligatorio()` funciona igual que en la API.
- Cada herramienta tiene prueba con el caso feliz, el caso sin permiso y, si es destructiva, el caso sin confirmar.

## Qué recomendarle al programador
- Si una herramienta nueva necesita un permiso que la API no tiene, conviene agregarlo primero a la API (con su `@Contrato`) y después reutilizarlo desde MCP, para no tener dos políticas.

## Cuándo preguntar
- Si la herramienta no tiene un endpoint REST equivalente y no está claro qué permiso exigir.
- Si la acción podría considerarse destructiva y no está claro si exige `confirmar`.
