package com.blue.open.Code.ReglaPermiso;

/**
 * Nivel de una regla de permiso (Spec 2): ver, editar o archivar. EDITAR y ARCHIVAR implican VER, pero son
 * independientes entre si. Se persiste por nombre.
 */
public enum NivelDePermiso {
    VER,
    EDITAR,
    ARCHIVAR
}
