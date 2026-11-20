package com.blue.open.Code.Usuario;

import com.blue.open.Code.Proyecto.ProyectoUsuarioResponseDTO;
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

    // Usuarios por uuid, en una consulta (usuarios importados de Jira, F4)
    List<Usuario> findByUuidIn(Collection<String> uuids);


    // Ids de los usuarios vigentes del grupo del super admin o de Administradores, que acceden a todo
    @Query("SELECT ur.idUsuario FROM UsuarioRol ur JOIN ur.rol r WHERE ur.status = 0 AND r.status = 0 AND r.nombre IN :grupos")
    List<Long> idsDeLosGrupos(@Param("grupos") Collection<String> grupos);

    // Usuarios vigentes y archivados para la administracion, por nombre (CU-USR-26). Sin los temporales
    @Query("SELECT new com.blue.open.Code.Usuario.UsuarioAdministracionResponseDTO(u.uuid, u.nombre, u.nombreDeUsuario, u.email, "
            + "CASE WHEN u.contrasena IS NULL THEN false ELSE true END, CASE WHEN u.status = -1 THEN true ELSE false END) "
            + "FROM Usuario u WHERE u.status IN (0, -1) ORDER BY LOWER(COALESCE(u.nombre, u.nombreDeUsuario))")
    List<UsuarioAdministracionResponseDTO> paraAdministrar();

    // Grupos vigentes asignados a cada usuario, por nombre de grupo (CU-USR-26)
    @Query("SELECT new com.blue.open.Code.Usuario.GrupoAsignado(u.uuid, r.uuid, r.nombre, ur.fechaDeCreacion) "
            + "FROM UsuarioRol ur JOIN ur.rol r, Usuario u WHERE u.id = ur.idUsuario AND ur.status = 0 AND r.status = 0 "
            + "ORDER BY r.nombre")
    List<GrupoAsignado> gruposAsignados();

    // Usuarios vigentes de la lista de ids, por nombre de usuario, armados como respuesta (usuarios del proyecto, CU-PRY-06)
    @Query("SELECT new com.blue.open.Code.Proyecto.ProyectoUsuarioResponseDTO(u.uuid, u.nombreDeUsuario, u.nombre) FROM Usuario u "
            + "WHERE u.id IN :ids AND u.status = 0 ORDER BY u.nombreDeUsuario")
    List<ProyectoUsuarioResponseDTO> vigentesParaElProyecto(@Param("ids") Collection<Long> ids);
}
