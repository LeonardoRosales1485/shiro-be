package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Entidad.Rol;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Identity.Service.AuthService;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Monoempresa.Repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Regla de la seccion 15 del DAF: los Administradores pueden hacer todo, menos administrar al super admin y a otros
 * Administradores; solo el super admin suma personas al grupo Administradores y edita los grupos del sistema
 * (SYSADMIN y ADMIN). La usan los casos de uso de usuarios y grupos.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class ProteccionDeAdministradores {

    // Grupos del sistema: los del super admin y los Administradores
    public static final List<String> GRUPOS_DEL_SISTEMA = List.of("SYSADMIN", "ADMIN");

    private final AuthService authService;

    private final UsuarioRolRepository usuarioRolRepository;

    // Exige que el usuario de la sesion pueda administrar al usuario recibido: el super admin a cualquiera; los
    // demas, a quien no sea super admin ni Administrador, o a si mismos. Lanza SIN_PERMISO
    public void exigirQuePuedaAdministrar(UsuarioBase objetivo) {
        UsuarioBase actual = authService.getActualObligatorio();
        if (authService.esSysAdmin(actual) || actual.getId().equals(objetivo.getId()))
            return;
        if (usuarioRolRepository.existeRolParaUsuario(objetivo.getId(), GRUPOS_DEL_SISTEMA))
            throw sinPermiso("usuario " + objetivo.getUuid());
    }

    // Exige que el usuario de la sesion pueda asignar, quitar o editar el grupo: los del sistema, solo el super admin
    public void exigirQuePuedaManejar(Rol grupo) {
        if (esDelSistema(grupo) && !authService.esSysAdmin(authService.getActualObligatorio()))
            throw sinPermiso("grupo " + grupo.getNombre());
    }

    // Si el grupo es SYSADMIN o ADMIN
    public static boolean esDelSistema(Rol grupo) {
        return GRUPOS_DEL_SISTEMA.contains(grupo.getNombre());
    }

    // Error SIN_PERMISO de la proteccion de administradores, con el detalle para el desarrollador
    private static ExcepcionDeApi sinPermiso(String detalle) {
        return new ExcepcionDeApi(CodigoDeErrorKernel.SIN_PERMISO,
                "Solo el super admin administra al super admin, a los Administradores y a sus grupos.",
                "ProteccionDeAdministradores | " + detalle);
    }
}
