package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-11: renovar el JWT con el refresh token (POST /usuario/refresh-token, JwtLoginController de core-protocolo-jwt,
 * publico). Migrado de bruno/User (07 Refresh Token). Usa el refresh token del usuario demo de los seeds; el JWT es
 * fijo en las pruebas (AuthJwtServiceDePrueba). Sin refresh token en el body se usa el de la cookie HttpOnly
 * refresh_token, y la renovacion tambien setea las cookies nuevas; una cookie en un POST desde otro origen es 403
 * ORIGEN_NO_PERMITIDO.
 */
class UsuarioRenovarSesionContratoTest extends PruebaDeContrato {

    @Test
    void renuevaElJwt() {
        JsonNode tokens = renovar(Map.of("refreshToken", UsuariosDeSeed.REFRESH_TOKEN_DEMO)).esperar(200).json();

        assertEquals(AuthJwtServiceDePrueba.JWT, tokens.get("token").asString());
        assertNotEquals(UsuariosDeSeed.REFRESH_TOKEN_DEMO, tokens.get("refreshToken").asString());
    }

    @Test
    void elRefreshTokenAnteriorDejaDeValer() {
        sinAutenticacion().post("/usuario/refresh-token").json(Map.of("refreshToken", UsuariosDeSeed.REFRESH_TOKEN_DEMO))
                .preparacion().ejecutar().esperar(200);

        renovar(Map.of("refreshToken", UsuariosDeSeed.REFRESH_TOKEN_DEMO)).esperarError(401, "REFRESH_TOKEN_INVALIDO");
    }

    @Test
    void renuevaElJwtConLaCookie() {
        Respuesta respuesta = sinAutenticacion().post("/usuario/refresh-token")
                .cookie("refresh_token", UsuariosDeSeed.REFRESH_TOKEN_DEMO).ejecutar().esperar(200);

        assertEquals(AuthJwtServiceDePrueba.JWT, respuesta.json().get("token").asString());
        assertTrue(cookiesSeteadas(respuesta).stream().anyMatch(c -> c.startsWith("access_token=") && c.contains("HttpOnly")));
        assertTrue(cookiesSeteadas(respuesta).stream().anyMatch(c -> c.startsWith("refresh_token=") && c.contains("HttpOnly")
                && c.contains("Path=/api/usuario;")));
    }

    @Test
    void cookieDesdeOtroOrigen() {
        sinAutenticacion().post("/usuario/refresh-token").cookie("refresh_token", UsuariosDeSeed.REFRESH_TOKEN_DEMO)
                .encabezado("Origin", "https://sitio-malicioso.com").ejecutar().esperarError(403, "ORIGEN_NO_PERMITIDO");
    }

    @Test
    void sinRefreshToken() {
        renovar(Map.of()).esperarError(400, "REFRESH_TOKEN_FALTANTE");
    }

    // Valores de los Set-Cookie de la respuesta
    private List<String> cookiesSeteadas(Respuesta respuesta) {
        return respuesta.resultado().getResponse().getHeaders("Set-Cookie");
    }

    // POST /usuario/refresh-token sin autenticacion
    private Respuesta renovar(Map<String, Object> cuerpo) {
        return sinAutenticacion().post("/usuario/refresh-token").json(cuerpo)
                .ejecutar();
    }
}
