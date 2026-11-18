package com.blue.open.Code.Proyecto;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;

/**
 * Repositorio de proyectos.
 */
public interface ProyectoRepository extends EntidadRepositoryBorradoLogico<Proyecto> {

    // Si algun proyecto, vigente o archivado, usa el prefijo: un prefijo archivado queda reservado (adenda 3 del DAF)
    boolean existsByPrefijo(String prefijo);
}
