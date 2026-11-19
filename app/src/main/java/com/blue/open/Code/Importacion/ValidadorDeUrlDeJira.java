package com.blue.open.Code.Importacion;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Valida la URL de Jira que escribe el administrador (resolucion A4 del 19/11): solo sitios https de Jira Cloud
 * (*.atlassian.net). Asi el backend no llama a direcciones de la red interna. shiro.jira.hosts-de-prueba suma hosts
 * exactos aceptados tambien por http, solo para el Jira simulado de las pruebas; en servidor queda vacia.
 * Dependencias externas: ninguna.
 */
@Component
public class ValidadorDeUrlDeJira {

    private static final String DOMINIO = ".atlassian.net";

    // Hosts de prueba separados por coma (vacio en servidor)
    @Value("${shiro.jira.hosts-de-prueba:}")
    private String hostsDePrueba;

    // Lanza JIRA_URL_NO_PERMITIDA si la URL no es https de *.atlassian.net (ni un host de prueba)
    public void validar(String url) {
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            throw noPermitida(url);
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        boolean deAtlassian = "https".equals(uri.getScheme()) && host.endsWith(DOMINIO) && host.length() > DOMINIO.length()
                && uri.getPort() == -1 && uri.getUserInfo() == null;
        if (!deAtlassian && !deHostsDePrueba().contains(host))
            throw noPermitida(url);
    }

    // Hosts de prueba configurados
    private List<String> deHostsDePrueba() {
        return hostsDePrueba == null || hostsDePrueba.isBlank() ? List.of()
                : Arrays.stream(hostsDePrueba.split(",")).map(String::trim).filter(h -> !h.isEmpty()).toList();
    }

    // Error de URL fuera de *.atlassian.net (o de los hosts de prueba). No interpola la url en el mensaje
    private static ExcepcionDeApi noPermitida(String url) {
        return new ExcepcionDeApi(CodigoDeErrorDelProyecto.JIRA_URL_NO_PERMITIDA, "ValidadorDeUrlDeJira | validar | url no permitida");
    }
}
