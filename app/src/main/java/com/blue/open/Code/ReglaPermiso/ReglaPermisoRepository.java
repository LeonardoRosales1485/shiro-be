package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Code.Autorizacion.ReglaEvaluable;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Tablero.Tablero;
import com.blue.open.Code.Usuario.Usuario;
import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Kernel.Repository.EntidadRepositoryBorradoLogico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de reglas de permiso.
 */
public interface ReglaPermisoRepository extends EntidadRepositoryBorradoLogico<ReglaPermiso> {

    // Reglas vigentes del usuario y de sus grupos vigentes (auth_usuarios_roles de core-monoempresa), reducidas a lo
    // que necesita el evaluador. Una consulta por request (la cachea ResolutorDePermisosDeRecurso)
    @Query("SELECT new com.blue.open.Code.Autorizacion.ReglaEvaluable("
            + "CASE WHEN u.id IS NOT NULL THEN true ELSE false END, p.id, t.id, r.nivel, r.efecto) "
            + "FROM ReglaPermiso r LEFT JOIN r.usuario u LEFT JOIN r.rol ro LEFT JOIN r.proyecto p LEFT JOIN r.tablero t "
            + "WHERE r.status = 0 AND (u.id = :usuario OR ro.id IN "
            + "(SELECT ur.rol.id FROM UsuarioRol ur WHERE ur.idUsuario = :usuario AND ur.status = 0))")
    List<ReglaEvaluable> reglasDelUsuario(@Param("usuario") Long usuario);

    // Si ya hay otra regla vigente con el mismo sujeto, recurso y nivel (id distinto del recibido; -1 en un alta)
    boolean existsByRolAndUsuarioAndProyectoAndTableroAndNivelAndStatusAndIdNot(Rol rol, Usuario usuario,
            Proyecto proyecto, Tablero tablero, NivelDePermiso nivel, Integer status, Long id);

    // Regla por uuid con el status recibido
    Optional<ReglaPermiso> findFirstByUuidAndStatus(String uuid, Integer status);
}
