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
 * CU-USR-07: reenviar el codigo de verificacion por email (POST /usuario/publico/reenviar-codigo, publico). Migrado de
 * bruno/User (15 Public Resend Code). El codigo es fijo en las pruebas (UsuarioServiceDePrueba).
 */
class UsuarioReenviarCodigoContratoTest extends PruebaDeContrato {

    @Test
    void reenviaElCodigo() {
        reenviar(Map.of("uuid", UsuariosDeSeed.DEMO, "email", UsuariosDeSeed.DEMO_EMAIL)).esperar(200);

        List<MailEnviado> enviados = mails().enviados();
        assertEquals(1, enviados.size());
        assertEquals(UsuariosDeSeed.DEMO_EMAIL, enviados.get(0).para());
        assertEquals("Código de Verificación - Blue Open Data", enviados.get(0).asunto());
        assertTrue(enviados.get(0).cuerpo().contains(UsuarioServiceDePrueba.codigo(6)));
    }

    @Test
    void antesDeTresMinutos() {
        Map<String, Object> cuerpo = Map.of("uuid", UsuariosDeSeed.DEMO, "email", UsuariosDeSeed.DEMO_EMAIL);
        sinAutenticacion().post("/usuario/publico/reenviar-codigo").json(cuerpo)
                .preparacion().ejecutar().esperar(200);

        JsonNode error = reenviar(cuerpo).esperarError(429, "DEMASIADOS_INTENTOS").json();

        assertEquals("Por favor espere unos minutos antes de reenviar el código", error.get("mensajeParaUsuario").asString());
        assertEquals(1, mails().enviados().size());
    }

    @Test
    void sinEmail() {
        JsonNode error = reenviar(Map.of("uuid", UsuariosDeSeed.DEMO)).esperarError(400, "DATOS_INCOMPLETOS").json();

        assertEquals("Datos incompletos", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void usuarioInexistente() {
        JsonNode error = reenviar(Map.of("uuid", UsuariosDeSeed.INEXISTENTE, "email", "nadie@example.com"))
                .esperarError(400, "REGISTRO_NO_ENCONTRADO").json();

        assertEquals("Usuario no encontrado", error.get("mensajeParaUsuario").asString());
    }

    // La ruta en ingles se renombro a la del glosario (ticket 06): ya no existe
    @Test
    void rutaViejaNoExiste() {
        sinAutenticacion().post("/usuario/public/resend-code").json(Map.of("uuid", UsuariosDeSeed.DEMO, "email", UsuariosDeSeed.DEMO_EMAIL)).ejecutar().esperar(404);
    }

    // POST /usuario/publico/reenviar-codigo sin autenticacion
    private Respuesta reenviar(Map<String, Object> cuerpo) {
        return sinAutenticacion().post("/usuario/publico/reenviar-codigo").json(cuerpo)
                .ejecutar();
    }
}
