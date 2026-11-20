package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de UsoDeToken y de la lista de tokens del usuario con su ultimo uso.
 */
public interface UsoDeTokenRepository extends EntidadRepositoryBorradoLogico<UsoDeToken> {

    // Uso registrado del token
    Optional<UsoDeToken> findFirstByIdAccessToken(Long idAccessToken);

    // Todos los tokens del usuario (vigentes y revocados), los mas nuevos primero, con su ultimo uso
    @Query("SELECT new com.blue.open.Code.Usuario.UsuarioTokenResponseDTO(a.uuid, a.nombre, CASE WHEN a.status = 0 AND a.activo = 1 "
            + "THEN true ELSE false END, a.fechaDeCreacion, a.fechaDeBorrado, u.ultimoUso) FROM AccessToken a "
            + "LEFT JOIN UsoDeToken u ON u.idAccessToken = a.id WHERE a.idUsuario = :usuario ORDER BY a.fechaDeCreacion DESC")
    List<UsuarioTokenResponseDTO> tokensDe(@Param("usuario") Long usuario);
}
