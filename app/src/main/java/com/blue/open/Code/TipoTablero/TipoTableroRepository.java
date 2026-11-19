package com.blue.open.Code.TipoTablero;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio del catalogo de tipos de tablero. Solo lee registros vigentes (status = 0).
 */
public interface TipoTableroRepository extends EntidadRepositoryBorradoLogico<TipoTablero> {

    // Tipos de tablero vigentes en orden de pestana, sin sus estados (los completa el Proyector)
    @Query("SELECT new com.blue.open.Code.TipoTablero.TipoTableroDTO(t.uuid, t.status, t.fechaDeCreacion, t.codigo, "
            + "t.nombre, t.orden) FROM TipoTablero t WHERE t.status = 0 ORDER BY t.orden")
    List<TipoTableroDTO> listarVigentes();

    // Tipos de tablero con el status recibido, en orden de pestana (alta de proyecto: un tablero por cada uno)
    List<TipoTablero> findByStatusOrderByOrden(Integer status);
}
