package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.IPermiso;
import com.blue.open.Core.Identity.Auth.IRol;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * Roles propios del proyecto (los base SYSADMIN y ADMIN estan en RolBaseEnum del Core).
 * Se crean en la empresa de sistema al arrancar via CatalogoDelProyecto, con los permisos declarados.
 */
@AllArgsConstructor
public enum RolEnum implements IRol {
    /*
     * Ejemplo de Roles que combinan permisos del Core y del Code
     * 
     * GERENTE(new HashSet<>(Arrays.asList(
     * PermisoBaseEnum.USUARIO_VER,
     * PermisoBaseEnum.EMPRESA_VER,
     * PermisoEnum.REPORTE_VER,
     * PermisoEnum.REPORTE_EXPORTAR
     * )));
     */;

    // Usamos IPermiso para permitir tanto PermisoEnum como PermisoBaseEnum
    private final Set<? extends IPermiso> permisos;

    // Permisos del rol que exige IRol (Core)
    @Override
    public Set<? extends IPermiso> getPermisos() {
        return permisos;
    }
}
