package com.blue.open.Code.Common.Mcp;

/**
 * Confirmacion de las acciones de alto impacto por MCP (Spec 4): archivar un proyecto o un usuario y cambiar
 * permisos exigen confirmar: true. Sin eso, la herramienta no cambia nada y responde que haria, para que el agente
 * se lo muestre a la persona antes de repetir la llamada.
 */
public final class Confirmacion {

    private Confirmacion() {
    }

    // Si la llamada trae confirmar: true
    public static boolean confirmada(Boolean confirmar) {
        return Boolean.TRUE.equals(confirmar);
    }

    // Respuesta de vista previa: que se haria y como confirmarlo
    public static RespuestaDeHerramienta vistaPrevia(String queSeHaria) {
        return new RespuestaDeHerramienta(queSeHaria + ". Repetí la llamada con confirmar: true para hacerlo.", null);
    }
}
