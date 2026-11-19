package com.blue.open.Code.Common.Mcp;

/**
 * Respuesta de una herramienta MCP para agentes (Spec 4): un resumen corto en texto y los datos estructurados.
 */
public record RespuestaDeHerramienta(String resumen, Object datos) {
}
