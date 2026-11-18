package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-10: iniciar sesion con usuario y contrasena (POST /usuario/iniciar-sesion, JwtLoginController de core-protocolo-jwt, publico).
 * Migrado de bruno/User (05 Login (JWT) y Login - credenciales invalidas). El JWT es fijo en las pruebas
 * (AuthJwtServiceDePrueba). El login tambien setea las cookies HttpOnly access_token (Path=/) y refresh_token (Path de la
 * ruta del login), y un JWT en la cookie access_token autentica un endpoint sin header Authorization.
 */
class UsuarioIniciarSesionContratoTest extends PruebaDeContrato {

    @Autowired
    private AuthJwtServiceDePrueba authJwtService;

    @Test
    void respondeElJwtYElRefreshToken() {
        JsonNode tokens = iniciar(Map.of("nombreDeUsuario", UsuariosDeSeed.DEMO_NOMBRE_DE_USUARIO, "contrasena", "123")).esperar(200)
                .json();

        assertEquals(AuthJwtServiceDePrueba.JWT, tokens.get("token").asString());
        assertFalse(tokens.get("refreshToken").asString().isEmpty());
    }

    // El login tambien setea el access token y el refresh token como cookies HttpOnly
    @Test
    void seteaLasCookiesDeSesion() {
        Respuesta respuesta = iniciar(Map.of("nombreDeUsuario", UsuariosDeSeed.DEMO_NOMBRE_DE_USUARIO, "contrasena", "123"))
                .esperar(200);

        List<String> cookies = respuesta.resultado().getResponse().getHeaders("Set-Cookie");
        assertTrue(cookies.stream().anyMatch(c -> c.startsWith("access_token=") && c.contains("HttpOnly")
                && c.contains("SameSite=Lax") && c.contains("Path=/;")));
        // El refresh token solo viaja a la ruta del login (refresh-token y cerrar-sesion)
        assertTrue(cookies.stream().anyMatch(c -> c.startsWith("refresh_token=") && c.contains("HttpOnly")
                && c.contains("Path=/api/usuario;")));
    }

    // El JWT en la cookie access_token (sin header Authorization) autentica un endpoint
    @Test
    void laCookieDeAccesoAutenticaUnEndpoint() {
        JsonNode usuario = sinAutenticacion().get("/usuario/sesion")
                .cookie("access_token", authJwtService.jwtFirmado(UsuariosDeSeed.DEMO)).ejecutar().esperar(200).json();

        assertEquals(UsuariosDeSeed.DEMO, usuario.get("uuid").asString());
    }

    @Test
    void credencialesInvalidas() {
        JsonNode error = iniciar(Map.of("nombreDeUsuario", UsuariosDeSeed.DEMO_NOMBRE_DE_USUARIO, "contrasena", "password-incorrecto"))
                .esperarError(401, "CREDENCIALES_INVALIDAS").json();

        assertEquals("Usuario o contraseña incorrectos", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void sinContrasena() {
        iniciar(Map.of("nombreDeUsuario", UsuariosDeSeed.DEMO_NOMBRE_DE_USUARIO)).esperarError(400, "FORMULARIO_INVALIDO");
    }

    // POST /usuario/login se renombro a POST /usuario/iniciar-sesion (glosario, ticket 08 [D8]): ya no existe
    @Test
    void rutaViejaNoExiste() {
        sinAutenticacion().post("/usuario/login").json(Map.of("nombreDeUsuario", UsuariosDeSeed.DEMO_NOMBRE_DE_USUARIO, "contrasena", "123"))
                .ejecutar().esperar(404);
    }

    // POST /usuario/iniciar-sesion sin autenticacion
    private Respuesta iniciar(Map<String, Object> cuerpo) {
        return sinAutenticacion().post("/usuario/iniciar-sesion").json(cuerpo).ejecutar();
    }
}
