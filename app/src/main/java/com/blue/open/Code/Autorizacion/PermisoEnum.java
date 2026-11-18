package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Identity.Auth.IPermiso;
import com.blue.open.Core.Identity.Auth.RegistroDePermisos;

import lombok.AllArgsConstructor;

/**
 * Permisos propios del proyecto (los genericos estan en PermisoBaseEnum del Core).
 * Se sincronizan con auth_permisos al arrancar via CatalogoDelProyecto, y RegistroDePermisos
 * los agrega automaticamente a los roles base SYSADMIN y ADMIN.
 */
@AllArgsConstructor
public enum PermisoEnum implements IPermiso {

    /*
     * Agregue aqui nuevos permisos especificos del proyecto
     *
     * Ejemplos:
     * REPORTE_VER("reporte.ver"),
     * REPORTE_EXPORTAR("reporte.exportar");
     */

    // Ver las configuraciones privadas (global = false)
    CONFIGURACION_VER("configuration.view"),
    // Crear o editar configuraciones
    CONFIGURACION_CREAR("configuration.create");

    // Nombre del permiso tal como se guarda en auth_permisos.nombre. Es un valor persistido: no se traduce (ADR 0002)
    private final String nombre;

    static {
        RegistroDePermisos.registrar(values());
    }

    // Nombre del permiso que exige IPermiso (Core)
    @Override
    public String getNombre() {
        return nombre;
    }
}
