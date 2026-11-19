package com.blue.open.Code.Version;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.Optional;

/**
 * Repositorio de versiones.
 */
public interface VersionRepository extends EntidadRepositoryBorradoLogico<Version> {

    // Version por uuid con el status recibido
    Optional<Version> findFirstByUuidAndStatus(String uuid, Integer status);
}
