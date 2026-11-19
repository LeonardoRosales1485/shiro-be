package com.blue.open.Code.Version;

/**
 * Una tarea terminada de una version, con lo que necesita el changelog: el tipo (nombre y orden de los selectores),
 * la clave y el titulo. La arma TareaVersionRepository.lineasDeChangelog.
 */
public record LineaDeChangelog(String tipo, Integer ordenDelTipo, String clave, String titulo) {
}
