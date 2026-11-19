package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Code.Autorizacion.ReglaDeUnUsuario;
import com.blue.open.Code.Autorizacion.ReglaEvaluable;
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

    // Reglas vigentes de usuarios puntuales sobre el proyecto o sobre alguno de sus tableros
    @Query("SELECT new com.blue.open.Code.Autorizacion.ReglaDeUnUsuario(u.id, true, p.id, t.id, r.nivel, r.efecto) "
            + "FROM ReglaPermiso r JOIN r.usuario u LEFT JOIN r.proyecto p LEFT JOIN r.tablero t "
            + "WHERE r.status = 0 AND (p.id = :proyecto OR t.proyecto.id = :proyecto)")
    List<ReglaDeUnUsuario> reglasDeUsuariosDelProyecto(@Param("proyecto") Long proyecto);

    // Reglas vigentes de grupos sobre el proyecto o sobre alguno de sus tableros, una por cada integrante vigente
    @Query("SELECT new com.blue.open.Code.Autorizacion.ReglaDeUnUsuario(ur.idUsuario, false, p.id, t.id, r.nivel, r.efecto) "
            + "FROM ReglaPermiso r JOIN r.rol ro JOIN UsuarioRol ur ON ur.rol.id = ro.id LEFT JOIN r.proyecto p LEFT JOIN r.tablero t "
            + "WHERE r.status = 0 AND ur.status = 0 AND (p.id = :proyecto OR t.proyecto.id = :proyecto)")
    List<ReglaDeUnUsuario> reglasDeGruposDelProyecto(@Param("proyecto") Long proyecto);

    // Si ya hay otra regla vigente con el mismo sujeto, recurso y nivel. Recibe los ids de grupo, usuario, proyecto y
    // tablero (-1 si la regla no tiene ese dato) y el id de la regla que se guarda (-1 en un alta)
    @Query("SELECT COUNT(r) > 0 FROM ReglaPermiso r LEFT JOIN r.rol ro LEFT JOIN r.usuario us LEFT JOIN r.proyecto pr "
            + "LEFT JOIN r.tablero tb WHERE r.status = 0 AND r.id <> :id AND r.nivel = :nivel AND COALESCE(ro.id, -1) = :rol "
            + "AND COALESCE(us.id, -1) = :usuario AND COALESCE(pr.id, -1) = :proyecto AND COALESCE(tb.id, -1) = :tablero")
    boolean existeOtraIgual(@Param("rol") Long rol, @Param("usuario") Long usuario, @Param("proyecto") Long proyecto,
            @Param("tablero") Long tablero, @Param("nivel") NivelDePermiso nivel, @Param("id") Long id);

    // Regla por uuid con el status recibido
    Optional<ReglaPermiso> findFirstByUuidAndStatus(String uuid, Integer status);
}
