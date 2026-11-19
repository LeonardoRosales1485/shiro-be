package com.blue.open.Code.Autorizacion;

/**
 * Un tablero vigente con el id de su proyecto: lo que necesita ResolutorDePermisosDeRecurso para calcular los
 * tableros y proyectos visibles de un usuario. Lo arma TableroRepository.listarVigentesConProyecto.
 */
public record TableroDeProyecto(Long idTablero, Long idProyecto) {
}
