package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-14: registro publico de un cliente en la empresa del header X-Empresa-Uuid, con el rol por defecto de esa
 * empresa (POST /usuario/publico/registro-cliente, core-multiempresa). Migrado de bruno/User (13 Public Register Client).
 * El captcha esta deshabilitado en el profile test.
 */
class UsuarioRegistroClienteContratoTest extends PruebaDeContrato {

    // Empresa operativa BlueOpenData (datos requeridos), con el rol por defecto CLIENTE
    private static final String EMPRESA = "2";

    // Empresa de sistema (datos requeridos)
    private static final String EMPRESA_DE_SISTEMA = "1";

    @Test
    void registraUnClienteEnLaEmpresa() {
        JsonNode usuario = registrar(EscenarioDeUsuario.alta("cliente.registro"), EMPRESA).esperar(200).json();

        assertTrue(usuario.hasNonNull("uuid"));
        assertEquals("cliente.registro", usuario.get("nombreDeUsuario").asString());
        assertFalse(usuario.hasNonNull("contrasena"));
    }

    @Test
    void emailInvalido() {
        registrar(Map.of("nombreDeUsuario", "cliente.email.invalido", "email", "no-es-un-email"), EMPRESA)
                .esperarError(400, "FORMULARIO_INVALIDO");
    }

    @Test
    void empresaDeSistema() {
        registrar(EscenarioDeUsuario.alta("cliente.sistema"), EMPRESA_DE_SISTEMA)
                .esperarError(400, "EMPRESA_DE_SISTEMA_NO_PERMITIDA");
    }

    // La ruta en ingles se renombro a la del glosario (ticket 07): ya no existe
    @Test
    void rutaViejaNoExiste() {
        sinAutenticacion().post("/usuario/public/register-client").encabezado("X-Empresa-Uuid", EMPRESA)
                .json(EscenarioDeUsuario.alta("cliente.ruta.vieja")).ejecutar().esperar(404);
    }

    // POST /usuario/publico/registro-cliente sin autenticacion, con la empresa en el header
    private Respuesta registrar(Map<String, Object> cuerpo, String empresa) {
        return sinAutenticacion().post("/usuario/publico/registro-cliente").encabezado("X-Empresa-Uuid", empresa).json(cuerpo)
                .ejecutar();
    }
}
