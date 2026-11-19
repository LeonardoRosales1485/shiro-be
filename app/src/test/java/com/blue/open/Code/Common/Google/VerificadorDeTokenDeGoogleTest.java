package com.blue.open.Code.Common.Google;

import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Validacion del ID token de Google (T2.2 y Spec 2): firma con las claves publicas de Google, emisor, audiencia (el
 * client id configurado), vencimiento, email_verified y hd (el dominio configurado). No depende de servicios externos:
 * las claves salen de TokensDeGoogleDePrueba.
 */
class VerificadorDeTokenDeGoogleTest {

    private VerificadorDeTokenDeGoogle verificador;

    @BeforeEach
    void preparar() {
        ClavesPublicasDeGoogle claves = mock(ClavesPublicasDeGoogle.class);
        when(claves.clave(TokensDeGoogleDePrueba.KID)).thenReturn(Optional.of(TokensDeGoogleDePrueba.clavePublica()));
        verificador = new VerificadorDeTokenDeGoogle(claves);
        ReflectionTestUtils.setField(verificador, "clientId", TokensDeGoogleDePrueba.CLIENT_ID);
        ReflectionTestUtils.setField(verificador, "dominio", TokensDeGoogleDePrueba.DOMINIO);
    }

    @Test
    void unTokenValidoDevuelveCorreoYNombre() {
        IdentidadDeGoogle identidad = verificador.verificar(
                TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos("ana@example.com")));

        assertEquals("ana@example.com", identidad.email());
        assertEquals("Persona de Prueba", identidad.nombre());
    }

    @Test
    void elEmisorSinHttpsTambienEsValido() {
        Map<String, Object> claims = TokensDeGoogleDePrueba.claimsValidos("ana@example.com");
        claims.put("iss", "accounts.google.com");

        assertEquals("ana@example.com", verificador.verificar(TokensDeGoogleDePrueba.firmar(claims)).email());
    }

    @Test
    void rechazaOtraAudienciaOtroEmisorVencidoSinVerificarYOtraFirma() {
        Map<String, Object> otraAudiencia = TokensDeGoogleDePrueba.claimsValidos("ana@example.com");
        otraAudiencia.put("aud", "otra-aplicacion");
        Map<String, Object> otroEmisor = TokensDeGoogleDePrueba.claimsValidos("ana@example.com");
        otroEmisor.put("iss", "https://otro.example.com");
        Map<String, Object> sinVerificar = TokensDeGoogleDePrueba.claimsValidos("ana@example.com");
        sinVerificar.put("email_verified", false);

        for (String token : new String[] {TokensDeGoogleDePrueba.firmar(otraAudiencia),
                TokensDeGoogleDePrueba.firmar(otroEmisor), TokensDeGoogleDePrueba.firmar(sinVerificar),
                TokensDeGoogleDePrueba.firmarVencido(TokensDeGoogleDePrueba.claimsValidos("ana@example.com")),
                TokensDeGoogleDePrueba.firmarConOtraClave(TokensDeGoogleDePrueba.claimsValidos("ana@example.com")),
                "no-es-un-token"}) {
            ExcepcionDeApi error = assertThrows(ExcepcionDeApi.class, () -> verificador.verificar(token));
            assertEquals("TOKEN_DE_GOOGLE_INVALIDO", error.getCodigo().codigo());
        }
    }

    @Test
    void rechazaUnaCuentaDeOtroDominio() {
        Map<String, Object> otroDominio = TokensDeGoogleDePrueba.claimsValidos("ana@gmail.com");
        otroDominio.remove("hd");

        ExcepcionDeApi error = assertThrows(ExcepcionDeApi.class,
                () -> verificador.verificar(TokensDeGoogleDePrueba.firmar(otroDominio)));

        assertEquals("DOMINIO_DE_GOOGLE_NO_PERMITIDO", error.getCodigo().codigo());
    }

    @Test
    void sinClientIdConfiguradoNoHayIngresoConGoogle() {
        ReflectionTestUtils.setField(verificador, "clientId", "");

        ExcepcionDeApi error = assertThrows(ExcepcionDeApi.class, () -> verificador.verificar("cualquiera"));

        assertEquals("GOOGLE_NO_CONFIGURADO", error.getCodigo().codigo());
    }
}
