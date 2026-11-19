package com.blue.open.Code.Tablero;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.Optional;

/**
 * Repositorio de tableros de proyecto.
 */
public interface TableroRepository extends EntidadRepositoryBorradoLogico<Tablero> {

    // Tablero por uuid con el status recibido (0: vigente)
    Optional<Tablero> findFirstByUuidAndStatus(String uuid, Integer status);
}
