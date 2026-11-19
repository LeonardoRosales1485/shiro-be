package com.blue.open.Code.Common.Google;

import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Descarga de las claves publicas de Google (JWKS) contra un servidor HTTP local que publica la clave de
 * TokensDeGoogleDePrueba: arma la clave RSA por kid, devuelve vacio para un kid desconocido y, si el servidor no
 * responde, GOOGLE_NO_DISPONIBLE. No llama a Google.
 */
class ClavesPublicasDeGoogleTest {

    private HttpServer servidor;

    @AfterEach
    void apagar() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void armaLaClaveDelKidPublicado() throws IOException {
        ClavesPublicasDeGoogle claves = conUrl(levantar(200, jwks()));

        assertEquals(TokensDeGoogleDePrueba.clavePublica(), claves.clave(TokensDeGoogleDePrueba.KID).orElseThrow());
        assertTrue(claves.clave("otro-kid").isEmpty());
    }

    @Test
    void sinRespuestaDeGoogleNoDisponible() throws IOException {
        ClavesPublicasDeGoogle claves = conUrl(levantar(500, "{}"));

        ExcepcionDeApi error = assertThrows(ExcepcionDeApi.class, () -> claves.clave(TokensDeGoogleDePrueba.KID));

        assertEquals("GOOGLE_NO_DISPONIBLE", error.getCodigo().codigo());
    }

    // Servidor local que responde siempre el status y el cuerpo recibidos; devuelve su URL
    private String levantar(int status, String cuerpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/certs", intercambio -> {
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(status, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/certs";
    }

    // ClavesPublicasDeGoogle apuntando a la URL recibida
    private static ClavesPublicasDeGoogle conUrl(String url) {
        ClavesPublicasDeGoogle claves = new ClavesPublicasDeGoogle();
        ReflectionTestUtils.setField(claves, "jwksUrl", url);
        return claves;
    }

    // JWKS con la clave publica de prueba, en el formato de Google
    private static String jwks() {
        RSAPublicKey clave = (RSAPublicKey) TokensDeGoogleDePrueba.clavePublica();
        return "{\"keys\":[{\"kid\":\"" + TokensDeGoogleDePrueba.KID + "\",\"kty\":\"RSA\",\"alg\":\"RS256\",\"n\":\""
                + base64(clave.getModulus()) + "\",\"e\":\"" + base64(clave.getPublicExponent()) + "\"}]}";
    }

    private static String base64(BigInteger numero) {
        byte[] bytes = numero.toByteArray();
        if (bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
