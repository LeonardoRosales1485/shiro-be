package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.IPermiso;
import com.blue.open.Core.Identity.Auth.IRol;
import com.blue.open.Core.Identity.Puertos.CatalogoDeAutorizacion;
import org.springframework.stereotype.Component;

/**
 * Declara al Core los roles y permisos propios de este proyecto para que los sincronice al arrancar.
 * Reemplaza al Class.forName("com.blue.open.Code.Auth.PermissionEnum") que hacia el Core 1.x:
 * ahora la relacion es explicita y el proyecto puede nombrar sus enums como quiera.
 * Dependencias externas: ninguna. Recibe: nada. Responde: los enums de roles y permisos.
 */
@Component
public class CatalogoDelProyecto implements CatalogoDeAutorizacion {

    // Roles propios del proyecto (RolEnum)
    @Override
    public IRol[] getRoles() {
        return RolEnum.values();
    }

    // Permisos propios del proyecto (PermisoEnum)
    @Override
    public IPermiso[] getPermisos() {
        return PermisoEnum.values();
    }
}
