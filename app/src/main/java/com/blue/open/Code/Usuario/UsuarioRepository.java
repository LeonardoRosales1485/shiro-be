package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de usuarios del proyecto. El Core ya no declara este repositorio: cada proyecto
 * publica el suyo sobre su propia entidad de usuario. Los nombres de los metodos derivados usan
 * los campos de UsuarioBase (Core).
 */
@Repository
public interface UsuarioRepository extends EntidadRepositoryBorradoLogico<Usuario> {

    Optional<Usuario> findFirstByNombreDeUsuarioIgnoringCase(String nombreDeUsuario);

    Optional<Usuario> findFirstByNombreDeUsuarioAndStatus(String nombreDeUsuario, Integer status);

    Optional<Usuario> findFirstByEmailAndStatus(String email, Integer status);

    Optional<Usuario> findFirstByEmailAndStatusNot(String email, Integer status);
}
