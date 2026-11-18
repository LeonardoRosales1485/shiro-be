package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.Dobles.EnviadorDeMailEnMemoria.MailEnviado;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-08: pedir un codigo de recuperacion de contrasena por email (POST /usuario/publico/recuperar-contrasena, publico).
 * Migrado de bruno/User (16 Public Forgot Password). Usa un usuario nuevo: el demo tiene un codigo de seguridad en
 * los seeds y su cooldown de 5 minutos dependeria de cuando corrio Liquibase. El codigo es fijo en las pruebas
 * (UsuarioServiceDePrueba).
 */
class UsuarioRecuperarContrasenaContratoTest extends PruebaDeContrato {

    private static final String NOMBRE_DE_USUARIO = "usuario.recupero.contrato";

    private final EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);

    @Test
    void enviaElCodigoPorEmail() {
        usuarios.crear(NOMBRE_DE_USUARIO);
        String email = EscenarioDeUsuario.email(NOMBRE_DE_USUARIO);

        JsonNode respuesta = recuperar(Map.of("email", email)).esperar(200).json();

        assertEquals("Mail enviado correctamente", respuesta.get("mensaje").asString());
        List<MailEnviado> enviados = mails().enviados();
        assertEquals(1, enviados.size());
        assertEquals(email, enviados.get(0).para());
        assertEquals("Recuperación de Contraseña - Blue Open Data", enviados.get(0).asunto());
        assertTrue(enviados.get(0).cuerpo().contains(UsuarioServiceDePrueba.codigo(32)));
    }

    @Test
    void emailSinCuentaNoRevelaQueNoExiste() {
        JsonNode respuesta = recuperar(Map.of("email", "nadie.registrado@example.com")).esperar(200).json();

        assertEquals("Mail enviado correctamente", respuesta.get("mensaje").asString());
        assertEquals(0, mails().enviados().size());
    }

    @Test
    void antesDeCincoMinutos() {
        usuarios.crear(NOMBRE_DE_USUARIO);
        String email = EscenarioDeUsuario.email(NOMBRE_DE_USUARIO);
        usuarios.codigoDeRecuperacion(email);

        JsonNode error = recuperar(Map.of("email", email)).esperarError(429, "DEMASIADOS_INTENTOS").json();

        assertEquals("Ya te enviamos un mail. Por favor, espera 5 minutos para solicitar otro.",
                error.get("mensajeParaUsuario").asString());
    }

    @Test
    void sinEmail() {
        JsonNode error = recuperar(Map.of()).esperarError(400, "FORMULARIO_INVALIDO").json();

        assertEquals("email", error.get("errores").get(0).get("nombre").asString());
        assertEquals("El email es obligatorio", error.get("errores").get(0).get("mensaje").asString());
    }

    // La ruta en ingles se renombro a la del glosario (ticket 06): ya no existe
    @Test
    void rutaViejaNoExiste() {
        sinAutenticacion().post("/usuario/public/forgot-password").json(Map.of("email", UsuariosDeSeed.DEMO_EMAIL)).ejecutar().esperar(404);
    }

    // POST /usuario/publico/recuperar-contrasena sin autenticacion
    private Respuesta recuperar(Map<String, Object> cuerpo) {
        return sinAutenticacion().post("/usuario/publico/recuperar-contrasena").json(cuerpo)
                .ejecutar();
    }
}
