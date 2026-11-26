package com.blue.open.Code.Common.Seguridad;

import java.time.Clock;
import java.time.Duration;

/**
 * Cuenta intentos fallidos por clave (usuario o IP) dentro de una ventana y bloquea la clave al llegar al maximo
 * (MJ-04). Esqueleto de la prueba en rojo: todavia no cuenta.
 */
public class ContadorDeIntentos {

    // Recibe el maximo de fallos, la ventana en que se cuentan, la duracion del bloqueo y el reloj
    public ContadorDeIntentos(int maximo, Duration ventana, Duration bloqueo, Clock reloj) {
    }

    // Si la clave esta bloqueada ahora
    public boolean bloqueado(String clave) {
        return false;
    }

    // Suma un fallo a la clave
    public void registrarFallo(String clave) {
    }

    // Borra los fallos de la clave
    public void reiniciar(String clave) {
    }
}
