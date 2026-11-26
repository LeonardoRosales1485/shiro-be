package com.blue.open.Code.Common.Mcp;

import com.blue.open.Code.Common.Seguridad.ContadorDeIntentos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

/**
 * Limite de llamadas a /mcp por token de acceso (MJ-13, decision D-3): hasta 120 por minuto; pasado el limite el token
 * queda frenado hasta que termina el minuto. Cuenta cada llamada con ContadorDeIntentos (el mismo de MJ-04: aca cada
 * llamada cuenta como un "intento"). En memoria: Shiro corre en una sola instancia. Lo usa FiltroDeMcp.
 * Dependencias externas: ninguna.
 */
@Component
public class LimiteDeLlamadasMcp {

    // Duracion de la ventana y del freno
    public static final Duration UN_MINUTO = Duration.ofMinutes(1);

    private final ContadorDeIntentos llamadas;

    // Recibe el limite de application.properties (120 por defecto, decision D-3)
    public LimiteDeLlamadasMcp(@Value("${shiro.mcp.llamadas-por-minuto:120}") int llamadasPorMinuto) {
        this.llamadas = new ContadorDeIntentos(llamadasPorMinuto, UN_MINUTO, UN_MINUTO, Clock.systemUTC());
    }

    /*
     * Registra una llamada del token (identificado por el hash que guarda la base, nunca el token en claro).
     * Responde false si el token ya paso el limite del minuto: esa llamada no se cuenta ni se atiende.
     */
    public boolean admitir(String hashDelToken) {
        if (llamadas.bloqueado(hashDelToken))
            return false;
        llamadas.registrarFallo(hashDelToken);
        return true;
    }
}
