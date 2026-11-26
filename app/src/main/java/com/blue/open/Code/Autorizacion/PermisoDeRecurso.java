package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.ReglaPermiso.NivelDePermiso;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permiso de recurso que exige un endpoint AUTENTICADO (MJ-05 del plan de mejoras): el nivel (VER, EDITAR o ARCHIVAR)
 * sobre el proyecto o el tablero, resuelto por ResolutorDePermisosDeRecurso. Es informativa: el chequeo sigue en el
 * caso de uso o el service. Alimenta el x-permiso del contrato ("EDITAR@TABLERO") y docs/idp/catalogo-de-permisos.md.
 * PermisosDeRecursoCompletosTest falla si un endpoint AUTENTICADO no la declara (o @SoloSesion).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermisoDeRecurso {

    // Nivel que exige
    NivelDePermiso nivel();

    // Recurso sobre el que se exige
    RecursoDePermiso recurso();

    // Aclaracion para el catalogo (por ejemplo "lista solo lo visible" o "y VER en el tablero de destino")
    String detalle() default "";
}
