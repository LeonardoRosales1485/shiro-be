package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.ReglaPermiso.EfectoDePermiso;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;

/**
 * Regla de permiso que alcanza a un usuario puntual (propia o de uno de sus grupos), con el id de ese usuario. La
 * arman las consultas de ReglaPermisoRepository que resuelven quienes tienen acceso a un proyecto, y se evalua con
 * EvaluadorDeReglasDePermiso igual que las de la sesion.
 */
public record ReglaDeUnUsuario(Long idUsuario, Boolean esDelUsuario, Long idProyecto, Long idTablero, NivelDePermiso nivel,
        EfectoDePermiso efecto) {

    // La regla sin el usuario, como la evalua EvaluadorDeReglasDePermiso
    public ReglaEvaluable evaluable() {
        return new ReglaEvaluable(esDelUsuario, idProyecto, idTablero, nivel, efecto);
    }
}
