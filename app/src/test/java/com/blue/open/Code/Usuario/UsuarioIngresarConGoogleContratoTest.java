package com.blue.open.Code.Usuario;

import com.blue.open.Code.Common.Google.ClavesPublicasDeGoogle;
import com.blue.open.Code.Common.Google.TokensDeGoogleDePrueba;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * CU-USR-20: ingresar con Google (POST /usuario/ingresar-con-google). T2.2 del plan y Spec 2: el front manda el ID
 * token de Google; el backend lo valida, busca o crea el usuario por correo y responde el JWT del Core. Un usuario
 * nuevo entra sin roles. Las claves publicas de Google son un doble: la prueba no llama a Google.
 */
class UsuarioIngresarConGoogleContratoTest extends PruebaDeContrato {

    @MockitoBean
    private ClavesPublicasDeGoogle clavesPublicasDeGoogle;

    @BeforeEach
    void clavesDePrueba() {
        when(clavesPublicasDeGoogle.clave(TokensDeGoogleDePrueba.KID))
                .thenReturn(Optional.of(TokensDeGoogleDePrueba.clavePublica()));
    }

    @Test
    void unUsuarioNuevoEntraSinRoles() {
        JsonNode sesion = ingresar(TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos("nueva@example.com")))
                .esperar(200).json();

        String jwt = sesion.get("token").asString();
        JsonNode usuario = sinAutenticacion().get("/usuario/sesion").encabezado("Authorization", "Bearer " + jwt)
                .ejecutar().esperar(200).json();
        assertEquals("nueva@example.com", usuario.get("email").asString());
        assertEquals("Persona de Prueba", usuario.get("nombre").asString());
        sinAutenticacion().post("/proyecto/guardar").encabezado("Authorization", "Bearer " + jwt)
                .json(Map.of("nombre", "Sin permiso", "prefijo", "SPE")).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    @Test
    void elMismoCorreoEntraComoElMismoUsuario() {
        String token = TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos("repetida@example.com"));

        String primero = uuidDeLaSesion(ingresar(token).esperar(200).json().get("token").asString());
        String segundo = uuidDeLaSesion(ingresar(token).esperar(200).json().get("token").asString());

        assertEquals(primero, segundo);
    }

    @Test
    void unaCuentaDeOtroDominioRechazada() {
        Map<String, Object> claims = TokensDeGoogleDePrueba.claimsValidos("alguien@gmail.com");
        claims.remove("hd");

        ingresar(TokensDeGoogleDePrueba.firmar(claims)).esperarError(403, "DOMINIO_DE_GOOGLE_NO_PERMITIDO");
    }

    @Test
    void unTokenInvalidoRechazado() {
        ingresar(TokensDeGoogleDePrueba.firmarConOtraClave(TokensDeGoogleDePrueba.claimsValidos("ana@example.com")))
                .esperarError(401, "TOKEN_DE_GOOGLE_INVALIDO");
    }

    @Test
    void unUsuarioArchivadoNoEntra() {
        EscenarioDeUsuario escenario = new EscenarioDeUsuario(this::como);
        String uuid = escenario.crearUuid("archivada");
        escenario.eliminar(uuid);

        ingresar(TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos(EscenarioDeUsuario.email("archivada"))))
                .esperarError(401, "NO_AUTENTICADO");
    }

    @Test
    void sinTokenRechazado() {
        sinAutenticacion().post("/usuario/ingresar-con-google").json(Map.of()).ejecutar()
                .esperarError(400, "FORMULARIO_INVALIDO");
    }

    // POST /usuario/ingresar-con-google sin sesion
    private Respuesta ingresar(String idToken) {
        return sinAutenticacion().post("/usuario/ingresar-con-google").json(Map.of("idToken", idToken)).ejecutar();
    }

    // Uuid del usuario del JWT recibido
    private String uuidDeLaSesion(String jwt) {
        return sinAutenticacion().get("/usuario/sesion").encabezado("Authorization", "Bearer " + jwt).ejecutar()
                .esperar(200).json().get("uuid").asString();
    }
}
