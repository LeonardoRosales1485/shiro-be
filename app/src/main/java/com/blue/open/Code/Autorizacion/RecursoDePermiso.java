package com.blue.open.Code.Autorizacion;

/**
 * Recurso sobre el que un endpoint exige un nivel de permiso (MJ-05): el proyecto (y sus tableros), un tablero, o el
 * que corresponda segun el elemento (una tarea se mira en su tablero; un sprint, una version o un proyecto, en el
 * proyecto). Lo usa @PermisoDeRecurso para el x-permiso del contrato ("EDITAR@TABLERO").
 */
public enum RecursoDePermiso {

    PROYECTO("PROYECTO"),
    TABLERO("TABLERO"),
    PROYECTO_O_TABLERO("PROYECTO|TABLERO");

    // Como se escribe en el x-permiso del contrato
    private final String enElContrato;

    RecursoDePermiso(String enElContrato) {
        this.enElContrato = enElContrato;
    }

    // Texto del recurso en el x-permiso
    public String enElContrato() {
        return enElContrato;
    }
}
