package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-28 (MJ-14 del plan de mejoras y decision D-3): crear un token de acceso eligiendo su vigencia (30, 90 o 180
 * dias). Responde el token una unica vez y su vencimiento. Un token creado por el endpoint del Core (crear-token)
 * vence a los 90 dias. "Mis tokens" muestra el vencimiento de cada uno. Que un token vencido deja de autenticar lo
 * prueba TokenVencidoContratoTest (necesita mover la fecha por SQL).
 */
class UsuarioCrearTokenConVencimientoContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Test
    void creaElTokenConLaVigenciaElegida() {
        JsonNode token = como(ADMIN).post("/usuario/crear-token-con-vencimiento").json(Map.of("nombre", "Vence en 30", "diasDeVigencia", 30))
                .ejecutar().esperar(200).json();

        assertEquals("Vence en 30", token.get("nombre").asString());
        assertTrue(token.get("codigo").asString().length() > 10);
        assertCerca(30, token.get("vence").asString());
        sinAutenticacion().get("/usuario/sesion").encabezado("Authorization", "Bearer " + token.get("codigo").asString())
                .preparacion().ejecutar().esperar(200);
        assertCerca(30, porNombre(misTokens(), "Vence en 30").get("vence").asString());
    }

    @Test
    void unTokenDelEndpointDelCoreVenceALosNoventaDias() {
        como(ADMIN).post("/usuario/crear-token").json(Map.of("nombre", "Del Core")).preparacion().ejecutar().esperar(200);

        assertCerca(90, porNombre(misTokens(), "Del Core").get("vence").asString());
    }

    @Test
    void soloAceptaTreintaNoventaOCientoOchentaDias() {
        como(ADMIN).post("/usuario/crear-token-con-vencimiento").json(Map.of("nombre", "Vigencia rara", "diasDeVigencia", 45))
                .ejecutar().esperarError(400, "FORMULARIO_INVALIDO");
    }

    @Test
    void elNombreNoSeRepite() {
        como(ADMIN).post("/usuario/crear-token-con-vencimiento").json(Map.of("nombre", "Repetido", "diasDeVigencia", 90))
                .preparacion().ejecutar().esperar(200);

        como(ADMIN).post("/usuario/crear-token-con-vencimiento").json(Map.of("nombre", "Repetido", "diasDeVigencia", 90))
                .ejecutar().esperarError(400, "TOKEN_NOMBRE_DUPLICADO");
    }

    // Mis tokens del ADMIN
    private JsonNode misTokens() {
        return como(ADMIN).get("/usuario/mis-tokens").preparacion().ejecutar().esperar(200).json();
    }

    // Token de la lista con ese nombre
    private static JsonNode porNombre(JsonNode tokens, String nombre) {
        for (JsonNode token : tokens) {
            if (nombre.equals(token.get("nombre").asString())) {
                return token;
            }
        }
        throw new AssertionError("No esta el token " + nombre + " en " + tokens);
    }

    // El vencimiento esta a esa cantidad de dias de ahora (con una hora de margen)
    private static void assertCerca(int dias, String vence) {
        Duration diferencia = Duration.between(OffsetDateTime.now().plusDays(dias), OffsetDateTime.parse(vence)).abs();
        assertTrue(diferencia.toHours() < 1, "Vence " + vence + ", se esperaban " + dias + " dias desde ahora");
    }
}
