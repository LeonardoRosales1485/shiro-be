package com.blue.open.Code.Common.Canal;

/**
 * Canal por el que entro un cambio (Spec 1): la web, un agente por MCP o el importador de Jira. Se persiste por
 * nombre en el historial y en los comentarios.
 */
public enum CanalDeCambio {
    WEB,
    MCP,
    IMPORTACION
}
