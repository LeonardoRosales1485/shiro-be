package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CU-USR-12: registro publico de un usuario con su empresa (POST /usuario/publico/registro, core-multiempresa). El usuario
 * queda pendiente de verificacion (status -2). Migrado de bruno/User (11 Public Register, Public Register - cuit
 * invalido y Public Register - email duplicado). El captcha esta deshabilitado en el profile test.
 */
class UsuarioRegistroPublicoContratoTest extends PruebaDeContrato {

    @Test
    void registraUnUsuarioPendienteDeVerificacion() {
        JsonNode usuario = registrar(cuerpo("cliente.publico", "20111111112")).esperar(200).json();

        assertEquals("cliente.publico", usuario.get("nombreDeUsuario").asString());
        assertEquals(-2, usuario.get("status").asInt());
        assertFalse(usuario.hasNonNull("contrasena"));
    }

    @Test
    void cuitInvalido() {
        JsonNode error = registrar(cuerpo("caso.cuit.invalido", "20111111111")).esperarError(400, "CUIT_INVALIDO").json();

        assertEquals("CUIT inválido", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void emailDuplicado() {
        sinAutenticacion().post("/usuario/publico/registro").json(cuerpo("caso.email.duplicado", null))
                .preparacion().ejecutar().esperar(200);
        Map<String, Object> repetido = cuerpo("caso.email.duplicado.2", null);
        usuario(repetido).put("email", "caso.email.duplicado@example.com");

        JsonNode error = registrar(repetido).esperarError(400, "EMAIL_YA_REGISTRADO").json();

        assertEquals("Email ya registrado", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void sinEmpresa() {
        Map<String, Object> sinEmpresa = cuerpo("caso.sin.empresa", null);
        sinEmpresa.remove("empresa");

        registrar(sinEmpresa).esperarError(400, "FORMULARIO_INVALIDO");
    }

    // La ruta en ingles se renombro a la del glosario (ticket 07): ya no existe
    @Test
    void rutaViejaNoExiste() {
        sinAutenticacion().post("/usuario/public/register").json(cuerpo("caso.ruta.vieja", null)).ejecutar().esperar(404);
    }

    // Body del registro: usuario {username} con email {username}@example.com y una empresa con el cuit recibido
    private static Map<String, Object> cuerpo(String nombreDeUsuario, String cuit) {
        Map<String, Object> empresa = new HashMap<>();
        empresa.put("nombre", "Empresa de " + nombreDeUsuario);
        empresa.put("descripcion", "Empresa creada desde registro publico");
        empresa.put("ruta", "empresa-" + nombreDeUsuario.replace('.', '-'));
        empresa.put("codigo", "EMP-" + nombreDeUsuario.length());
        empresa.put("cuit", cuit);
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("usuario", new HashMap<>(EscenarioDeUsuario.alta(nombreDeUsuario)));
        cuerpo.put("empresa", empresa);
        return cuerpo;
    }

    // Usuario del body, para modificarlo
    @SuppressWarnings("unchecked")
    private static Map<String, Object> usuario(Map<String, Object> cuerpo) {
        return (Map<String, Object>) cuerpo.get("usuario");
    }

    // POST /usuario/publico/registro sin autenticacion
    private Respuesta registrar(Map<String, Object> cuerpo) {
        return sinAutenticacion().post("/usuario/publico/registro").json(cuerpo)
                .ejecutar();
    }
}
