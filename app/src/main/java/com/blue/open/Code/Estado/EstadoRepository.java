package com.blue.open.Code.Estado;

import com.blue.open.Code.TipoTablero.TipoTablero;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de estados de tablero. Lo usan las tareas para validar y mover estados (T1.3 y T1.5).
 */
public interface EstadoRepository extends EntidadRepositoryBorradoLogico<Estado> {

    // Estado por uuid con el status recibido (0: vigente)
    Optional<Estado> findFirstByUuidAndStatus(String uuid, Integer status);

    // Estados vigentes de un tipo de tablero, en orden de columna
    List<Estado> findByTipoTableroAndStatusOrderByOrden(TipoTablero tipoTablero, Integer status);

    // Estado inicial vigente de un tipo de tablero: el estado en el que nace una tarea nueva
    Optional<Estado> findFirstByTipoTableroAndInicialTrueAndStatus(TipoTablero tipoTablero, Integer status);
}
