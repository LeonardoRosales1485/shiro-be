package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.ReglaPermiso.EfectoDePermiso;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;

/**
 * Regla de permiso reducida a lo que necesita EvaluadorDeReglasDePermiso: si es del usuario (o de uno de sus grupos),
 * el recurso (id de proyecto o de tablero; el otro es null), el nivel y el efecto. La arma
 * ReglaPermisoRepository.reglasDelUsuario con una proyeccion.
 */
public record ReglaEvaluable(Boolean esDelUsuario, Long idProyecto, Long idTablero, NivelDePermiso nivel,
        EfectoDePermiso efecto) {
}
