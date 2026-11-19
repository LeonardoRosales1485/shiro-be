package com.blue.open.Code.Importacion;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mapeo guardado de una importacion (columna mapeo, en JSON): tipo de tablero por tablero de Jira, estado por tablero
 * y estado de Jira, y tipo de tarea por tipo de Jira. Todo por uuid de Shiro e id de Jira.
 *
 * @param tableros uuid de tipo de tablero por id de tablero de Jira, en el orden del mapeo
 * @param estados  por id de tablero de Jira, uuid de estado por id de estado de Jira
 * @param tipos    uuid de tipo de tarea por id de tipo de Jira
 */
public record MapeoDeImportacion(LinkedHashMap<String, String> tableros, Map<String, Map<String, String>> estados,
        Map<String, String> tipos) {
}
