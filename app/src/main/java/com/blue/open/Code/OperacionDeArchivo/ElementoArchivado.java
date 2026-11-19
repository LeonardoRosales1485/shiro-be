package com.blue.open.Code.OperacionDeArchivo;

import java.sql.Timestamp;

/**
 * Un elemento archivado de cualquier tipo con lo que necesita la busqueda de archivados: tipo, uuid, nombre
 * visible, proyecto (prefijo e id), tablero (id, solo las tareas) y fecha de borrado. Lo arman las consultas de
 * OperacionDeArchivoRepository.
 */
public record ElementoArchivado(String tipo, String uuid, String nombre, String proyectoPrefijo, Long idProyecto,
        Long idTablero, Timestamp fechaDeBorrado) {
}
