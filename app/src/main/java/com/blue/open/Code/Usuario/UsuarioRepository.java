package com.blue.open.Code.Usuario;

import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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

    // Usuarios vigentes con alguno de los correos recibidos, en una consulta (vincular usuarios de Jira, F4)
    List<Usuario> findByEmailInAndStatus(Collection<String> emails, Integer status);

    // Usuario por uuid con el status recibido (0: vigente, -1: archivado)
    Optional<Usuario> findFirstByUuidAndStatus(String uuid, Integer status);

    // Usuario por uuid en cualquier status
    Optional<Usuario> findFirstByUuid(String uuid);

    // Grupos vigentes del usuario (auth_usuarios_roles de core-monoempresa), por nombre
    @Query("SELECT new com.blue.open.Code.Usuario.GrupoDeUsuarioDTO(r.uuid, r.nombre) FROM UsuarioRol ur JOIN ur.rol r "
            + "WHERE ur.idUsuario = :usuario AND ur.status = 0 AND r.status = 0 ORDER BY r.nombre")
    List<GrupoDeUsuarioDTO> gruposDe(@Param("usuario") Long usuario);
}
