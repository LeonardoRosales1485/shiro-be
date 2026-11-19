package com.blue.open.Code.Common.Google;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Smoke test de la integracion con Google al iniciar (integraciones-y-entorno.md): valida la configuracion y que las
 * claves publicas se puedan bajar. El ingreso con Google no es imprescindible (las cuentas locales siguen entrando),
 * asi que un fallo se loguea y no corta el arranque.
 * Dependencias externas: Google (claves publicas).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ComprobacionDeGoogleAlIniciar implements ApplicationRunner {

    private final ClavesPublicasDeGoogle claves;

    @Value("${shiro.google.client-id:}")
    private String idDeCliente;

    @Value("${shiro.google.dominio:}")
    private String dominio;

    // Si se comprueba al iniciar (las pruebas lo apagan: no llaman a Google)
    @Value("${shiro.google.comprobar-al-iniciar:true}")
    private boolean comprobar;

    // Valida la configuracion y baja las claves; ante un error solo loguea
    @Override
    public void run(ApplicationArguments argumentos) {
        if (idDeCliente == null || idDeCliente.isBlank() || dominio == null || dominio.isBlank()) {
            log.warn("Ingreso con Google deshabilitado | falta GOOGLE_CLIENT_ID o GOOGLE_DOMINIO");
            return;
        }
        if (!comprobar) {
            return;
        }
        try {
            claves.clave("comprobacion-al-iniciar");
            log.info("Smoke test de Google validado | dominio={}", dominio);
        } catch (RuntimeException e) {
            log.error("Smoke test de Google fallido: el ingreso con Google no va a funcionar hasta que responda", e);
        }
    }
}
