package com.blue.open.Code.Importacion;

/**
 * Elemento de Jira que no se importo, o que se importo distinto, y por que (ResumenDeImportacion).
 *
 * @param claveJira clave de la tarea de Jira (o el nombre del sprint)
 * @param motivo    explicacion para el administrador
 */
public record Falla(String claveJira, String motivo) {
}
