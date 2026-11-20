package com.blue.open.Code.Importacion;

/**
 * Estado de una Importacion (Spec 6): CONECTADA al guardar las credenciales, MAPEADA con el mapeo listo, EN_CURSO
 * mientras corre en segundo plano y TERMINADA, FALLIDA o CANCELADA (la corto un administrador) al final. Se persiste por nombre en shr_importacion.estado.
 */
public enum EstadoDeImportacion {
    CONECTADA,
    MAPEADA,
    EN_CURSO,
    TERMINADA,
    FALLIDA,
    CANCELADA
}
