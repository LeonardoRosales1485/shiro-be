package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-25: mis tokens de acceso (GET /usuario/mis-tokens, propuesta 50 resuelta el 19/11): todos los tokens del
 * usuario de la sesion, tambien los revocados, con su fecha de creacion y su ultimo uso (lo registra la autenticacion
 * por token, a lo sumo una vez por minuto). Con sesion.
 */
class UsuarioMisTokensContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Test
    void listaLosVigentesYLosRevocadosConSuUltimoUso() {
        JsonNode usado = como(ADMIN).post("/usuario/crear-token").json(Map.of("nombre", "Token usado")).ejecutar().esperar(200).json();
        JsonNode revocado = como(ADMIN).post("/usuario/crear-token").json(Map.of("nombre", "Token revocado")).ejecutar().esperar(200).json();
        sinAutenticacion().encabezado("Authorization", "Bearer " + usado.get("codigo").asString()).get("/usuario/sesion").ejecutar().esperar(200);
        como(ADMIN).delete("/usuario/borrar-token/{uuid}", revocado.get("uuid").asString()).ejecutar().esperar(200);

        JsonNode tokens = como(ADMIN).get("/usuario/mis-tokens").ejecutar().esperar(200).json();

        JsonNode conUso = porNombre(tokens, "Token usado");
        assertTrue(conUso.get("activo").asBoolean());
        assertFalse(conUso.get("ultimoUso").isNull(), conUso::toString);
        assertFalse(conUso.get("fechaDeCreacion").isNull());
        JsonNode sinUso = porNombre(tokens, "Token revocado");
        assertFalse(sinUso.get("activo").asBoolean());
        assertTrue(sinUso.get("ultimoUso").isNull());
        assertTrue(sinUso.get("codigo") == null || sinUso.get("codigo").isNull());
    }

    private static JsonNode porNombre(JsonNode tokens, String nombre) {
        for (JsonNode token : tokens) {
            if (nombre.equals(token.get("nombre").asString()))
                return token;
        }
        throw new IllegalStateException("Sin el token " + nombre + " en " + tokens);
    }
}
