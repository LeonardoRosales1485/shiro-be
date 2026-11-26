package com.blue.open;

import com.blue.open.Code.Autorizacion.PermisoDeRecurso;
import com.blue.open.Code.Autorizacion.SoloSesion;
import com.blue.open.Core.Kernel.Contrato.Contrato;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MJ-05 del plan de mejoras (docs/plan-de-mejoras.md): todo endpoint de Shiro con @Contrato(permiso =
 * Contrato.AUTENTICADO) declara que permiso de recurso exige (@PermisoDeRecurso: VER, EDITAR o ARCHIVAR sobre el
 * proyecto o el tablero) o que alcanza con la sesion (@SoloSesion). Con eso el contrato (x-permiso) y el catalogo de
 * permisos (docs/idp/catalogo-de-permisos.md) dicen que exige cada endpoint. Sin contexto de Spring.
 */
class PermisosDeRecursoCompletosTest {

    @Test
    void todoEndpointAutenticadoDeclaraSuPermisoDeRecurso() {
        List<String> sinDeclarar = new ArrayList<>();
        for (Class<?> controller : CatalogoDePermisos.controllers()) {
            for (Method metodo : controller.getDeclaredMethods()) {
                Contrato contrato = metodo.getAnnotation(Contrato.class);
                if (contrato == null || !Arrays.asList(contrato.permiso()).contains(Contrato.AUTENTICADO))
                    continue;
                if (!metodo.isAnnotationPresent(PermisoDeRecurso.class) && !metodo.isAnnotationPresent(SoloSesion.class))
                    sinDeclarar.add(controller.getSimpleName() + "." + metodo.getName());
            }
        }
        assertTrue(sinDeclarar.isEmpty(), () -> "MJ-05: estos endpoints AUTENTICADO no declaran @PermisoDeRecurso ni @SoloSesion: "
                + sinDeclarar);
    }

    @Test
    void hayControllersParaRevisar() {
        assertTrue(CatalogoDePermisos.controllers().size() > 10, "No se encontraron los controllers de com.blue.open.Code");
    }
}
