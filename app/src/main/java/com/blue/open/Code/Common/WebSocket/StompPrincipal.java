package com.blue.open.Code.Common.WebSocket;

import java.security.Principal;

/**
 * Principal de una sesion STOMP. Su nombre es el uuid del usuario, que es lo que usa
 * convertAndSendToUser para dirigir mensajes a /user/{uuid}/...
 */
public class StompPrincipal implements Principal {
    // Uuid del usuario autenticado
    private final String nombre;

    // Recibe el uuid del usuario autenticado
    public StompPrincipal(String nombre) {
        this.nombre = nombre;
    }

    // Nombre del principal que exige Principal: el uuid del usuario
    @Override
    public String getName() {
        return nombre;
    }
}
