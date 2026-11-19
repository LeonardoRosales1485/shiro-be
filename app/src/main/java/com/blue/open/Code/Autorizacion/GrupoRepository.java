package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Entidad.Rol;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Consultas de Shiro sobre los grupos (roles de core-identity). Extiende Repository y no EntidadRepository para no
 * competir con el RolRepository del Core, que RolService recibe por tipo.
 */
public interface GrupoRepository extends Repository<Rol, Long> {

    // Si hay otro grupo con el status recibido y el mismo nombre (uuid distinto del recibido; "" en un alta)
    boolean existsByNombreAndStatusAndUuidNot(String nombre, Integer status, String uuid);

    // Grupos con los uuids y el status recibidos
    List<Rol> findByUuidInAndStatus(Collection<String> uuids, Integer status);

    // Grupos con los nombres y el status recibidos
    List<Rol> findByNombreInAndStatus(Collection<String> nombres, Integer status);

    // Grupo por uuid con el status recibido
    Optional<Rol> findFirstByUuidAndStatus(String uuid, Integer status);
}
