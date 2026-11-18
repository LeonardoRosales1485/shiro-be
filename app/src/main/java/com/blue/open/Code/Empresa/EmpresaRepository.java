package com.blue.open.Code.Empresa;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de empresas del proyecto. El Core (core-multiempresa) accede a las empresas por el
 * puerto EmpresaRepositorio sin conocer esta interfaz; esta es la que usa el codigo del proyecto.
 */
@Repository
public interface EmpresaRepository extends EntidadRepositoryBorradoLogico<Empresa> {
    Optional<Empresa> findFirstByCuitAndStatus(String cuit, Integer status);

    // nombre es el nombre de la empresa (campo de EmpresaBase, del Core)
    Optional<Empresa> findFirstByNombreAndStatus(String nombre, Integer status);
}
