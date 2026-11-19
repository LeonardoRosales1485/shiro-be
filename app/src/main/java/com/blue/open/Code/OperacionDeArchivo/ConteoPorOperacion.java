package com.blue.open.Code.OperacionDeArchivo;

/**
 * Cantidad de tareas archivadas en una operacion de archivo. Lo arma OperacionDeArchivoRepository.tareasPorOperacion
 * para la columna "Incluye" de Archivados.
 */
public record ConteoPorOperacion(Long idOperacion, Long cantidad) {
}
