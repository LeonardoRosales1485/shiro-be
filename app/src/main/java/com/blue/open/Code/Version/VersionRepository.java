package com.blue.open.Code.Version;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.Optional;

/**
 * Repositorio de versiones.
 */
public interface VersionRepository extends EntidadRepositoryBorradoLogico<Version> {

    // Version del proyecto por nombre con el status recibido (asi la nombran los agentes en MCP)
    Optional<Version> findFirstByProyectoAndNombreAndStatus(Proyecto proyecto, String nombre, Integer status);

    // Version por uuid con el status recibido
    Optional<Version> findFirstByUuidAndStatus(String uuid, Integer status);
}
