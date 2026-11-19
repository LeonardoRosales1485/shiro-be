package com.blue.open.Code.Importacion;

/**
 * Credenciales para llamar a la API de Jira: URL base, correo y token de API (ya descifrado). No se loguea ni se
 * serializa: el toString no muestra el token.
 *
 * @param url    URL base del sitio, sin barra final
 * @param correo correo de la cuenta
 * @param token  token de API de la cuenta
 */
public record CredencialesDeJira(String url, String correo, String token) {

    // Credenciales con la URL sin barra final
    public CredencialesDeJira {
        url = url == null ? null : url.trim().replaceAll("/+$", "");
    }

    // Sin el token, para que nunca termine en un log
    @Override
    public String toString() {
        return "CredencialesDeJira[url=" + url + "]";
    }
}
