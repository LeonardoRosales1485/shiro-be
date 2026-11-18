package com.blue.open.Code.Common.Configuracion;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * URLs del frontend. {@code front.url} es la base; las propiedades especificas
 * vacias heredan ese valor.
 */
@Component
public class UrlsDelFrontProperties {

    // Obligatoria: sin ella los emails y el CORS del WebSocket quedan sin URL
    @Value("${front.url:#{null}}")
    private String urlBase;

    /** Enlaces en correos de registro y recuperacion de contrasena. */
    @Value("${front.url.mail:}")
    private String urlDeMail;

    /** Origenes CORS del endpoint WebSocket (lista separada por comas). */
    @Value("${front.url.websocket:}")
    private String urlsDeWebSocket;

    // Origenes (scheme://host[:puerto]) del front separados por coma que acepta el CORS. Vacio = el origen de front.url
    @Value("${front.url.cors:}")
    private String origenesDeCors;

    // Corta el arranque con un mensaje claro si falta front.url (regla de variables de entorno)
    @PostConstruct
    void validar() {
        if (!StringUtils.hasText(urlBase)) {
            throw new IllegalStateException("Falta la propiedad front.url (URL del frontend). "
                    + "Definirla en application.properties.");
        }
    }

    // URL base del front, sin espacios
    public String getUrlBase() {
        return urlBase != null ? urlBase.trim() : "";
    }

    // URL que va en los enlaces de los emails (front.url.mail, o front.url si esta vacia)
    public String getUrlDeMail() {
        return resolver(urlDeMail);
    }

    // Origenes del front que acepta el CORS (front.url.cors, o el origen de front.url si esta vacia), sin barra final
    public List<String> getOrigenesDeCors() {
        if (StringUtils.hasText(origenesDeCors)) {
            return Arrays.stream(separarPorComas(origenesDeCors))
                    .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                    .toList();
        }
        // front.url puede traer path (https://host/proyecto): el CORS compara solo scheme://host[:puerto]
        URI base = URI.create(getUrlBase());
        if (base.getScheme() == null || base.getHost() == null) {
            throw new IllegalStateException("front.url no es una URL valida (scheme://host): " + getUrlBase()
                    + ". Definir front.url.cors con los origenes del front.");
        }
        return List.of(base.getScheme() + "://" + base.getHost() + (base.getPort() == -1 ? "" : ":" + base.getPort()));
    }

    // Origenes permitidos para el WebSocket (front.url.websocket, o front.url si esta vacia)
    public String[] getOrigenesDeWebSocket() {
        return separarPorComas(resolver(urlsDeWebSocket));
    }

    // Devuelve la URL especifica si tiene valor; si no, la URL base
    private String resolver(String especifica) {
        if (StringUtils.hasText(especifica)) {
            return especifica.trim();
        }
        return getUrlBase();
    }

    // Separa una lista de URLs por comas, sin espacios ni elementos vacios
    private static String[] separarPorComas(String lista) {
        return Arrays.stream(lista.split(","))
                .map(s -> s.trim())
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
    }
}
