package com.blue.open;

import com.blue.open.Core.Kernel.Puertos.EnviadorDeMail;
import com.blue.open.Core.Test.Dobles.EnviadorDeMailEnMemoria;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifica que en las pruebas de contrato de este proyecto el envio de mail (puerto del kernel) es el doble en memoria
 * de core-test. Shiro no declara core-crypter. Los actores (tokens de los seeds) los ejercitan las pruebas de contrato y sus casos de seguridad.
 */
class InfraestructuraDePruebasDeContratoTest extends PruebaDeContrato {

    @Autowired
    private EnviadorDeMail enviadorDeMail;

    @Test
    void elMailEsElDobleEnMemoria() throws Exception {
        assertInstanceOf(EnviadorDeMailEnMemoria.class, enviadorDeMail);

        enviadorDeMail.enviar("a@b.com", "Asunto", "<p>Hola</p>");

        assertEquals(1, mails().enviados().size());
    }
}
