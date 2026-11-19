package com.blue.open.Code.Estado;

import com.blue.open.Code.TipoTablero.TipoTablero;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.Optional;

/**
 * Repositorio de estados de tablero. Lo usan las tareas para validar y mover estados (T1.3 y T1.5).
 */
public interface EstadoRepository extends EntidadRepositoryBorradoLogico<Estado> {

    // Estado inicial vigente de un tipo de tablero: el estado en el que nace una tarea nueva
    Optional<Estado> findFirstByTipoTableroAndInicialTrueAndStatus(TipoTablero tipoTablero, Integer status);
}
