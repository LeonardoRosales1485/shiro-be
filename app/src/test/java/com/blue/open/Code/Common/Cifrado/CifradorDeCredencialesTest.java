package com.blue.open.Code.Common.Cifrado;

import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cifrado de credenciales (F4 y Spec 2): las credenciales de Jira se guardan cifradas con AES-256-GCM y una clave que
 * sale de la frase configurada. No depende de datos externos.
 */
class CifradorDeCredencialesTest {

    private CifradorDeCredenciales con(String frase) {
        CifradorDeCredenciales cifrador = new CifradorDeCredenciales();
        ReflectionTestUtils.setField(cifrador, "frase", frase);
        return cifrador;
    }

    @Test
    void cifraYDescifra() {
        CifradorDeCredenciales cifrador = con("frase de prueba de shiro");

        String cifrado = cifrador.cifrar("token-de-jira");

        assertFalse(cifrado.contains("token-de-jira"));
        assertEquals("token-de-jira", cifrador.descifrar(cifrado));
        assertNotEquals(cifrado, cifrador.cifrar("token-de-jira"));
    }

    @Test
    void otraFraseNoDescifra() {
        String cifrado = con("frase de prueba de shiro").cifrar("token-de-jira");

        assertThrows(ExcepcionDeApi.class, () -> con("otra frase").descifrar(cifrado));
    }

    @Test
    void sinFraseNoHayCifrado() {
        ExcepcionDeApi error = assertThrows(ExcepcionDeApi.class, () -> con("").cifrar("token"));

        assertEquals("CIFRADO_NO_CONFIGURADO", error.getCodigo().codigo());
    }
}
