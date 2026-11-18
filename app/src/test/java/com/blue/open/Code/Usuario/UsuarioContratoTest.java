package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.ContratoDeAbmTest;

import java.util.List;
import java.util.Map;

/**
 * Contrato del ABM de /usuario (CU-USR-01 a CU-USR-04) con sus casos comunes: obtener, uuid
 * inexistente, paginar, alta, body invalido, eliminar, eliminar inexistente y doble borrado. Los casos propios de cada
 * caso de uso estan en Usuario{CasoDeUso}ContratoTest.
 */
class UsuarioContratoTest extends ContratoDeAbmTest {

    @Override
    protected Class<?> controlador() {
        return UsuarioController.class;
    }

    @Override
    protected String rol() {
        return RolBaseEnum.SYSADMIN.name();
    }

    @Override
    protected String uuidVigente() {
        return UsuariosDeSeed.DEMO;
    }

    @Override
    protected Object cuerpoValido() {
        return EscenarioDeUsuario.alta("usuario.contrato.heredado");
    }

    @Override
    protected Object cuerpoInvalido() {
        return Map.of("nombreDeUsuario", "", "email", "no-es-un-email");
    }

    // Primera pagina de usuarios con email: un usuario sin email no cumpliria el schema de UsuarioDTO (email
    // obligatorio)
    @Override
    protected Object paginado() {
        return Map.of("pagina", 1, "tamano", 5,
                "filtros", List.of(List.of(Map.of("campo", "email", "operacion", "is not null", "valor", ""))));
    }

}
