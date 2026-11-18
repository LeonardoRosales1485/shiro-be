package com.blue.open;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Modulos del Core que usa Shiro (spec tecnica, "Que depende de core-parent" y Spec 2): identity con monoempresa
 * (los grupos son roles directos del usuario), los protocolos jwt, basic y tokens, y ningun modulo que la v1 no usa
 * (multiempresa, sso, mail, sm3, captcha, crypter). No depende de servicios
 * externos ni del contexto de Spring: revisa el classpath y las properties.
 */
class ModulosDelCoreTest {

    // Una clase representativa de cada modulo que tiene que estar en el classpath
    private static final List<String> CLASES_DE_MODULOS_REQUERIDOS = List.of(
            "com.blue.open.Core.Identity.Service.AuthService",
            "com.blue.open.Core.Monoempresa.Service.ResolutorDePermisosMonoempresa",
            "com.blue.open.Core.Protocolo.Jwt.AuthJwtService",
            "com.blue.open.Core.Protocolo.ApiKey.AuthTokenService");

    // Una clase representativa de cada modulo que no tiene que estar en el classpath
    private static final List<String> CLASES_DE_MODULOS_EXCLUIDOS = List.of(
            "com.blue.open.Core.Multiempresa.Service.ContextoDeEmpresa",
            "com.blue.open.Core.Integracion.Mail.EnviadorDeMailSmtp",
            "com.blue.open.Core.Integracion.Captcha.CaptchaService",
            "com.blue.open.Core.Integracion.Sm3.SM3Service",
            "com.blue.open.Core.Protocolo.Sso.AuthSsoService",
            "com.blue.open.Core.Integracion.Crypter.CrypterService");

    @Test
    void estanLosModulosDeLaSpec() {
        List<String> faltantes = new ArrayList<>();
        for (String clase : CLASES_DE_MODULOS_REQUERIDOS) {
            if (!existe(clase)) {
                faltantes.add(clase);
            }
        }
        assertTrue(faltantes.isEmpty(), "Faltan modulos del Core que la spec exige en app/pom.xml: " + faltantes);
    }

    @Test
    void noEstanLosModulosQueLaV1NoUsa() {
        List<String> sobrantes = new ArrayList<>();
        for (String clase : CLASES_DE_MODULOS_EXCLUIDOS) {
            if (existe(clase)) {
                sobrantes.add(clase);
            }
        }
        assertTrue(sobrantes.isEmpty(), "Sobran modulos del Core en app/pom.xml que Shiro v1 no usa: " + sobrantes);
    }

    @Test
    void elServidorHabilitaLosProtocolosDeLaSpec() throws IOException {
        Properties propiedades = new Properties();
        try (InputStream entrada = getClass().getResourceAsStream("/application.server.properties")) {
            propiedades.load(entrada);
        }
        // Google se suma en F2 (T2.2) como estrategia propia de Shiro
        assertEquals("jwt,basic,tokens", propiedades.getProperty("auth.protocolos"));
    }

    // Si la clase esta en el classpath. Recibe el nombre completo; no lanza excepciones
    private static boolean existe(String clase) {
        try {
            Class.forName(clase, false, ModulosDelCoreTest.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
