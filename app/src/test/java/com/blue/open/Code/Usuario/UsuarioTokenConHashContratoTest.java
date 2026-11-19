package com.blue.open.Code.Usuario;

import com.blue.open.Core.Protocolo.ApiKey.AccessTokenRepository;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T2.1 del plan y Spec 2: tokens de acceso con hash. La base guarda solo el SHA-256 del token y autentica buscando
 * por ese hash; el token se muestra una unica vez, al crearlo; uno revocado deja de autenticar. Mientras el Core no
 * publique 4.1.0, la logica vive en Shiro (AuthTokenConSha256Service).
 */
class UsuarioTokenConHashContratoTest extends PruebaDeContrato {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Test
    void laBaseGuardaSoloElHashYElTokenAutenticaPorHash() throws NoSuchAlgorithmException {
        String codigo = como(UsuariosDeSeed.ROL_DEMO).post("/usuario/crear-token").json(Map.of("nombre", "Agente MCP"))
                .ejecutar().esperar(200).json().get("codigo").asString();

        assertTrue(accessTokenRepository.findFirstByCodigo(codigo).isEmpty());
        assertTrue(accessTokenRepository.findFirstByCodigo(sha256(codigo)).isPresent());
        JsonNode usuario = sinAutenticacion().get("/usuario/sesion").encabezado("Authorization", "Bearer " + codigo)
                .ejecutar().esperar(200).json();
        assertEquals(UsuariosDeSeed.DEMO, usuario.get("uuid").asString());
    }

    @Test
    void elTokenSeMuestraUnaSolaVez() {
        como(UsuariosDeSeed.ROL_DEMO).post("/usuario/crear-token").json(Map.of("nombre", "Una sola vez"))
                .ejecutar().esperar(200);

        for (JsonNode token : como(UsuariosDeSeed.ROL_DEMO).get("/usuario/tokens").ejecutar().esperar(200).json()) {
            assertTrue(token.get("codigo").isNull(), token::toString);
        }
    }

    @Test
    void losTokensDeLosSeedsTambienQuedanConHash() throws NoSuchAlgorithmException {
        assertTrue(accessTokenRepository.findFirstByCodigo(UsuariosDeSeed.TOKEN_DEMO_CODIGO).isEmpty());
        assertTrue(accessTokenRepository.findFirstByCodigo(sha256(UsuariosDeSeed.TOKEN_DEMO_CODIGO)).isPresent());
    }

    @Test
    void unTokenRevocadoDejaDeAutenticar() {
        JsonNode token = como(UsuariosDeSeed.ROL_DEMO).post("/usuario/crear-token").json(Map.of("nombre", "Revocable"))
                .ejecutar().esperar(200).json();
        como(UsuariosDeSeed.ROL_DEMO).delete("/usuario/borrar-token/{uuid}", token.get("uuid").asString())
                .ejecutar().esperar(200);

        sinAutenticacion().get("/usuario/sesion").encabezado("Authorization", "Bearer " + token.get("codigo").asString())
                .ejecutar().esperarError(401, "ACCESS_TOKEN_INVALIDO");
    }

    // SHA-256 en hexadecimal, como lo guarda la base
    private static String sha256(String texto) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8)));
    }
}
