package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-15: cerrar sesion (POST /usuario/cerrar-sesion, publico, lo publica core-protocolo-jwt). Borra el refresh
 * token recibido (del body o de la cookie refresh_token) y responde 204 con las dos cookies borradas (Max-Age=0).
 * Es idempotente: sin refresh token tambien responde 204. Despues de cerrar, el refresh token ya no renueva
 * (401 REFRESH_TOKEN_INVALIDO). Una cookie en un POST desde otro origen es 403 ORIGEN_NO_PERMITIDO.
 */
class UsuarioCerrarSesionContratoTest extends PruebaDeContrato {

    // Contrasena por defecto de los usuarios de seed (spec contrasena-por-defecto-de-seeds)
    private static final String CLAVE_DE_SEED = "123";

    @Test
    void cierraLaSesionConElBody() {
        String refreshToken = iniciarSesion();

        Respuesta respuesta = sinAutenticacion().post("/usuario/cerrar-sesion")
                .json(Map.of("refreshToken", refreshToken)).ejecutar().esperar(204);

        assertCookiesBorradas(respuesta);
        sinAutenticacion().post("/usuario/refresh-token").json(Map.of("refreshToken", refreshToken))
                .preparacion().ejecutar().esperarError(401, "REFRESH_TOKEN_INVALIDO");
    }

    @Test
    void cierraLaSesionConLaCookie() {
        String refreshToken = iniciarSesion();

        Respuesta respuesta = sinAutenticacion().post("/usuario/cerrar-sesion")
                .cookie("refresh_token", refreshToken).ejecutar().esperar(204);

        assertCookiesBorradas(respuesta);
        sinAutenticacion().post("/usuario/refresh-token").json(Map.of("refreshToken", refreshToken))
                .preparacion().ejecutar().esperarError(401, "REFRESH_TOKEN_INVALIDO");
    }

    @Test
    void sinRefreshToken() {
        assertCookiesBorradas(sinAutenticacion().post("/usuario/cerrar-sesion").ejecutar().esperar(204));
    }

    @Test
    void cookieDesdeOtroOrigen() {
        sinAutenticacion().post("/usuario/cerrar-sesion").cookie("refresh_token", iniciarSesion())
                .encabezado("Origin", "https://sitio-malicioso.com").ejecutar().esperarError(403, "ORIGEN_NO_PERMITIDO");
    }

    // Inicia sesion con el usuario demo (paso de preparacion) y responde su refresh token
    private String iniciarSesion() {
        JsonNode tokens = sinAutenticacion().post("/usuario/iniciar-sesion")
                .json(Map.of("nombreDeUsuario", UsuariosDeSeed.DEMO_NOMBRE_DE_USUARIO, "contrasena", CLAVE_DE_SEED))
                .preparacion().ejecutar().esperar(200).json();
        return tokens.get("refreshToken").asString();
    }

    // Las dos cookies de sesion vuelven con Max-Age=0 y el mismo Path con el que se emitieron
    private void assertCookiesBorradas(Respuesta respuesta) {
        List<String> cookies = respuesta.resultado().getResponse().getHeaders("Set-Cookie");
        assertTrue(cookies.stream().anyMatch(c -> c.startsWith("access_token=;") && c.contains("Max-Age=0")
                && c.contains("Path=/;")));
        assertTrue(cookies.stream().anyMatch(c -> c.startsWith("refresh_token=;") && c.contains("Max-Age=0")
                && c.contains("Path=/api/usuario;")));
    }
}
