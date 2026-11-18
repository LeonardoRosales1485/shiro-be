package com.blue.open.Code.Common.Mcp;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * Herramienta MCP de diagnostico: devuelve el texto que recibe. Sirve para comprobar que un agente llega al servidor
 * MCP de Shiro (T0.2 del plan). No toca la base ni depende del usuario.
 * Dependencias externas: ninguna.
 */
@Component
public class HerramientaDeEco {

    // Responde el mismo texto que recibe. Recibe un texto cualquiera; responde ese texto sin cambios
    @McpTool(name = "eco", description = "Devuelve el mismo texto que recibe. Sirve para probar la conexión con Shiro.")
    public String eco(@McpToolParam(description = "Texto a devolver", required = true) String texto) {
        return texto;
    }
}
