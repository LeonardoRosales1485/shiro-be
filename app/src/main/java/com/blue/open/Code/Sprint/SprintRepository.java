package com.blue.open.Code.Sprint;

import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

import java.util.Optional;

/**
 * Repositorio de sprints.
 */
public interface SprintRepository extends EntidadRepositoryBorradoLogico<Sprint> {

    // Sprint por uuid con el status recibido
    Optional<Sprint> findFirstByUuidAndStatus(String uuid, Integer status);

    // Sprint del proyecto por nombre con el status recibido (asi lo nombran los agentes en MCP)
    Optional<Sprint> findFirstByProyectoAndNombreAndStatus(Proyecto proyecto, String nombre, Integer status);

    // Sprint del proyecto en el estado recibido (para el activo: hay uno como mucho)
    Optional<Sprint> findFirstByProyectoAndEstadoAndStatus(Proyecto proyecto, EstadoDeSprint estado, Integer status);
}
