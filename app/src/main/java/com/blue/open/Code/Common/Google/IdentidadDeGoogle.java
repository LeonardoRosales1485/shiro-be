package com.blue.open.Code.Common.Google;

/**
 * Lo que Shiro toma de un ID token de Google ya validado: el correo verificado y el nombre visible.
 */
public record IdentidadDeGoogle(String email, String nombre) {
}
