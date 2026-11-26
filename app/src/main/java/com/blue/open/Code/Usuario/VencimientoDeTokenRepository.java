package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.Optional;

/**
 * Repositorio de VencimientoDeToken.
 */
public interface VencimientoDeTokenRepository extends EntidadRepositoryBorradoLogico<VencimientoDeToken> {

    // Vencimiento del token
    Optional<VencimientoDeToken> findFirstByIdAccessToken(Long idAccessToken);
}
