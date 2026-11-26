package com.blue.open.Code.Common.Seguridad;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cuenta intentos fallidos por clave (un nombre de usuario o una IP) dentro de una ventana y bloquea la clave cuando
 * llega al maximo (MJ-04, decision D-2). Vive en memoria: Shiro corre en una sola instancia. Una clave nula no se
 * cuenta ni se bloquea. Lo usa LimiteDeIntentosDeIngreso.
 * Dependencias externas: ninguna.
 */
public class ContadorDeIntentos {

    // Cantidad de claves a partir de la cual se limpian las que ya no tienen fallos ni bloqueo vigentes
    private static final int CLAVES_ANTES_DE_LIMPIAR = 10_000;

    // Fallos en la ventana y fin del bloqueo de una clave (null si no esta bloqueada)
    private static final class Estado {

        private final Deque<Instant> fallos = new ArrayDeque<>();

        private Instant bloqueadaHasta;
    }

    private final int maximo;

    private final Duration ventana;

    private final Duration bloqueo;

    private final Clock reloj;

    private final Map<String, Estado> estados = new ConcurrentHashMap<>();

    // Recibe el maximo de fallos, la ventana en que se cuentan, la duracion del bloqueo y el reloj
    public ContadorDeIntentos(int maximo, Duration ventana, Duration bloqueo, Clock reloj) {
        this.maximo = maximo;
        this.ventana = ventana;
        this.bloqueo = bloqueo;
        this.reloj = reloj;
    }

    // Si la clave esta bloqueada ahora. Al vencer el bloqueo la clave arranca de cero
    public boolean bloqueado(String clave) {
        Estado estado = clave == null ? null : estados.get(clave);
        if (estado == null)
            return false;
        synchronized (estado) {
            Instant ahora = reloj.instant();
            if (estado.bloqueadaHasta != null && !ahora.isBefore(estado.bloqueadaHasta)) {
                estado.bloqueadaHasta = null;
                estado.fallos.clear();
            }
            return estado.bloqueadaHasta != null;
        }
    }

    // Suma un fallo a la clave; si con este llega al maximo dentro de la ventana, la bloquea
    public void registrarFallo(String clave) {
        if (clave == null)
            return;
        if (estados.size() > CLAVES_ANTES_DE_LIMPIAR)
            limpiar();
        Estado estado = estados.computeIfAbsent(clave, c -> new Estado());
        synchronized (estado) {
            Instant ahora = reloj.instant();
            descartarViejos(estado, ahora);
            estado.fallos.addLast(ahora);
            if (estado.fallos.size() >= maximo)
                estado.bloqueadaHasta = ahora.plus(bloqueo);
        }
    }

    // Borra los fallos de la clave (por ejemplo, despues de un ingreso correcto)
    public void reiniciar(String clave) {
        if (clave != null)
            estados.remove(clave);
    }

    // Saca los fallos que quedaron fuera de la ventana
    private void descartarViejos(Estado estado, Instant ahora) {
        Instant desde = ahora.minus(ventana);
        while (!estado.fallos.isEmpty() && !estado.fallos.peekFirst().isAfter(desde)) {
            estado.fallos.removeFirst();
        }
    }

    // Quita las claves sin bloqueo vigente y sin fallos en la ventana, para que la memoria no crezca sin limite
    private void limpiar() {
        Instant ahora = reloj.instant();
        estados.entrySet().removeIf(entrada -> {
            Estado estado = entrada.getValue();
            synchronized (estado) {
                descartarViejos(estado, ahora);
                boolean sinBloqueo = estado.bloqueadaHasta == null || !ahora.isBefore(estado.bloqueadaHasta);
                return sinBloqueo && estado.fallos.isEmpty();
            }
        });
    }
}
