package com.blue.open.Code.Common.Seguridad;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MJ-04 del plan de mejoras: contador de intentos fallidos con ventana y bloqueo (D-2: 5 en 15 minutos, 15 minutos de
 * bloqueo), con un reloj que avanza a mano.
 */
class ContadorDeIntentosTest {

    private static final Duration QUINCE_MINUTOS = Duration.ofMinutes(15);

    private final RelojManual reloj = new RelojManual();

    private final ContadorDeIntentos contador = new ContadorDeIntentos(5, QUINCE_MINUTOS, QUINCE_MINUTOS, reloj);

    @Test
    void cuatroFallosNoBloqueanYElQuintoSi() {
        fallar("lucas", 4);
        assertFalse(contador.bloqueado("lucas"));

        fallar("lucas", 1);
        assertTrue(contador.bloqueado("lucas"));
        assertFalse(contador.bloqueado("carla"));
    }

    @Test
    void pasadoElBloqueoSeVuelveAPoder() {
        fallar("lucas", 5);
        reloj.avanzar(QUINCE_MINUTOS.minusSeconds(1));
        assertTrue(contador.bloqueado("lucas"));

        reloj.avanzar(Duration.ofSeconds(1));
        assertFalse(contador.bloqueado("lucas"));
        fallar("lucas", 1);
        assertFalse(contador.bloqueado("lucas"));
    }

    @Test
    void losFallosFueraDeLaVentanaNoCuentan() {
        fallar("lucas", 4);
        reloj.avanzar(QUINCE_MINUTOS.plusSeconds(1));
        fallar("lucas", 1);

        assertFalse(contador.bloqueado("lucas"));
    }

    @Test
    void unIngresoCorrectoReiniciaLaCuenta() {
        fallar("lucas", 4);
        contador.reiniciar("lucas");
        fallar("lucas", 4);

        assertFalse(contador.bloqueado("lucas"));
    }

    @Test
    void unaClaveNulaNoCuenta() {
        fallar(null, 10);

        assertFalse(contador.bloqueado(null));
    }

    // Registra la cantidad de fallos recibida para la clave
    private void fallar(String clave, int veces) {
        for (int i = 0; i < veces; i++) {
            contador.registrarFallo(clave);
        }
    }

    // Reloj de prueba que solo avanza cuando se le pide
    private static final class RelojManual extends Clock {

        private Instant ahora = Instant.parse("2026-11-26T12:00:00Z");

        // Adelanta el reloj
        void avanzar(Duration duracion) {
            ahora = ahora.plus(duracion);
        }

        // Zona del reloj
        @Override
        public ZoneId getZone() {
            return ZoneId.of("America/Argentina/Buenos_Aires");
        }

        // El reloj manual no cambia de zona
        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        // Instante actual del reloj manual
        @Override
        public Instant instant() {
            return ahora;
        }
    }
}
