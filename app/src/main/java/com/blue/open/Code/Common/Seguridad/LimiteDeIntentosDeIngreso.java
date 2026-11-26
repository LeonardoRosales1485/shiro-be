package com.blue.open.Code.Common.Seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

/**
 * Limite de intentos de ingreso (MJ-04, decision D-2 y respuestas del 26/11): por nombre de usuario (5 fallos en 15
 * minutos) y por IP (20 fallos en 15 minutos), con 15 minutos de bloqueo. Cuentan los ingresos con usuario y
 * contrasena fallidos (iniciar sesion y Basic) y los tokens invalidos en /mcp (solo para la IP). Un ingreso correcto
 * reinicia la cuenta del usuario, no la de la IP. Lo usa FiltroDeLimiteDeIngreso.
 * Dependencias externas: ninguna.
 */
@Component
public class LimiteDeIntentosDeIngreso {

    private final ContadorDeIntentos porUsuario;

    private final ContadorDeIntentos porIp;

    // Recibe los umbrales de application.properties (con los valores de D-2 por defecto) y arma los dos contadores
    public LimiteDeIntentosDeIngreso(
            @Value("${shiro.ingreso.intentos-por-usuario:5}") int intentosPorUsuario,
            @Value("${shiro.ingreso.intentos-por-ip:20}") int intentosPorIp,
            @Value("${shiro.ingreso.ventana-en-minutos:15}") long ventanaEnMinutos,
            @Value("${shiro.ingreso.bloqueo-en-minutos:15}") long bloqueoEnMinutos) {
        Duration ventana = Duration.ofMinutes(ventanaEnMinutos);
        Duration bloqueo = Duration.ofMinutes(bloqueoEnMinutos);
        Clock reloj = Clock.systemUTC();
        this.porUsuario = new ContadorDeIntentos(intentosPorUsuario, ventana, bloqueo, reloj);
        this.porIp = new ContadorDeIntentos(intentosPorIp, ventana, bloqueo, reloj);
    }

    // Motivo del bloqueo del usuario o de la IP ("usuario" o "ip"), o null si ninguno esta bloqueado
    public String motivoDeBloqueo(String nombreDeUsuario, String ip) {
        if (porUsuario.bloqueado(normalizar(nombreDeUsuario)))
            return "usuario";
        return porIp.bloqueado(ip) ? "ip" : null;
    }

    // Suma un fallo al usuario (si se conoce) y a la IP
    public void registrarFallo(String nombreDeUsuario, String ip) {
        porUsuario.registrarFallo(normalizar(nombreDeUsuario));
        porIp.registrarFallo(ip);
    }

    // Reinicia la cuenta del usuario despues de un ingreso correcto
    public void registrarExito(String nombreDeUsuario) {
        porUsuario.reiniciar(normalizar(nombreDeUsuario));
    }

    // El mismo usuario escrito con otras mayusculas o espacios cuenta igual
    private static String normalizar(String nombreDeUsuario) {
        if (nombreDeUsuario == null || nombreDeUsuario.isBlank())
            return null;
        return nombreDeUsuario.trim().toLowerCase(Locale.ROOT);
    }
}
