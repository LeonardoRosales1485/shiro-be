package com.blue.open.Code.Historial;

/**
 * Tipo de accion de una fila del historial (adenda 3 del DAF). Se persiste por nombre en shr_historial.accion.
 */
public enum AccionDeHistorial {
    CREAR,
    EDITAR,
    MOVER,
    CREAR_SUBTAREA,
    ARCHIVAR,
    RESTAURAR,
    COMENTAR,
    RELACIONAR,
    CLONAR
}
