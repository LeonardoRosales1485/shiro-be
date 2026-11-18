package com.blue.open.Code.Common.Configuracion;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

/**
 * CORS del proyecto: reemplaza al CORS abierto del Core (SeguridadWebConfig, @ConditionalOnMissingBean) para que
 * solo los origenes del front reciban Access-Control-Allow-Credentials. Con la sesion por cookie (Core 3.6.0) un CORS
 * abierto dejaria a cualquier sitio leer los GET autenticados por cookie si la cookie fuera SameSite=None.
 * Los origenes salen de front.url.cors y tienen que coincidir con core.protocolo.cookie.origenes-permitidos.
 * Dependencias externas: ninguna.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final UrlsDelFrontProperties urlsDelFront;

    // CORS para todas las rutas: los origenes del front, cualquier metodo y header, con credenciales. Expone
    // Content-Disposition (nombre de archivo de las descargas), X-Trace-Id y API-Version para que el JavaScript del
    // front los pueda leer. El nombre del metodo es el del bean del Core: su cadena de seguridad lo busca por nombre.
    // Un origen fuera de la lista no recibe configuracion: el filtro CORS no le agrega headers (el navegador bloquea la
    // lectura de la respuesta) y tampoco corta con su 403 sin codigo; un request que modifica autenticado por cookie
    // lo rechaza CookiesDeSesion con 403 ORIGEN_NO_PERMITIDO (errores.md regla 1)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(urlsDelFront.getOrigenesDeCors());
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Content-Disposition", "X-Trace-Id", "API-Version"));
        configuration.setAllowCredentials(true);
        return request -> {
            String origen = request.getHeader(HttpHeaders.ORIGIN);
            return origen != null && configuration.checkOrigin(origen) != null ? configuration : null;
        };
    }
}
