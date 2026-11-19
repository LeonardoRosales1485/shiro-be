package com.blue.open.Code.Common.Google;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ID tokens con la forma de los de Google, firmados con una clave RSA generada para las pruebas. La clave publica la
 * entrega un doble de ClavesPublicasDeGoogle: ninguna prueba llama a Google.
 */
public final class TokensDeGoogleDePrueba {

    // Configuracion de las pruebas (application-test.yml)
    public static final String CLIENT_ID = "shiro-pruebas.apps.googleusercontent.com";

    public static final String DOMINIO = "example.com";

    public static final String KID = "clave-de-prueba";

    private static final KeyPair CLAVES = generar();

    private static final KeyPair OTRAS_CLAVES = generar();

    private TokensDeGoogleDePrueba() {
    }

    // Clave publica con la que se verifican los tokens de prueba
    public static PublicKey clavePublica() {
        return CLAVES.getPublic();
    }

    // Claims de un token valido de Google para el correo recibido, que se pueden pisar en cada prueba
    public static Map<String, Object> claimsValidos(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "https://accounts.google.com");
        claims.put("aud", CLIENT_ID);
        claims.put("sub", "google-" + email);
        claims.put("email", email);
        claims.put("email_verified", true);
        claims.put("hd", DOMINIO);
        claims.put("name", "Persona de Prueba");
        return claims;
    }

    // Token firmado con la clave de prueba, vigente por una hora
    public static String firmar(Map<String, Object> claims) {
        return firmar(claims, CLAVES.getPrivate(), new Date(System.currentTimeMillis() + 3_600_000));
    }

    // Token firmado con otra clave (la firma no verifica)
    public static String firmarConOtraClave(Map<String, Object> claims) {
        return firmar(claims, OTRAS_CLAVES.getPrivate(), new Date(System.currentTimeMillis() + 3_600_000));
    }

    // Token vencido hace una hora
    public static String firmarVencido(Map<String, Object> claims) {
        return firmar(claims, CLAVES.getPrivate(), new Date(System.currentTimeMillis() - 3_600_000));
    }

    private static String firmar(Map<String, Object> claims, PrivateKey clave, Date vencimiento) {
        return Jwts.builder().setHeaderParam("kid", KID).addClaims(claims).setExpiration(vencimiento)
                .signWith(clave, SignatureAlgorithm.RS256).compact();
    }

    private static KeyPair generar() {
        try {
            KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
            generador.initialize(2048);
            return generador.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
