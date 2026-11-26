package com.blue.open.Code.Autorizacion;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Endpoint AUTENTICADO que no exige permiso de recurso: alcanza con la sesion (MJ-05). El valor explica por que (por
 * ejemplo "datos del propio usuario" o "catalogo comun a todos los proyectos"). Su x-permiso es "AUTENTICADO".
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SoloSesion {

    // Por que alcanza con la sesion
    String value();
}
