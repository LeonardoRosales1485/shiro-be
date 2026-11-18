package com.blue.open;

import com.blue.open.Core.Integracion.Crypter.CrypterService;
import com.blue.open.Core.Kernel.Puertos.EnviadorDeMail;
import com.blue.open.Core.Test.Dobles.CrypterEnMemoria;
import com.blue.open.Core.Test.Dobles.EnviadorDeMailEnMemoria;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifica que en las pruebas de contrato de este proyecto los servicios externos son los dobles en memoria de
 * core-test. Los actores (tokens de los seeds) los ejercitan las pruebas de contrato y sus casos de seguridad.
 */
class InfraestructuraDePruebasDeContratoTest extends PruebaDeContrato {

    @Autowired
    private CrypterService crypterService;

    @Autowired
    private EnviadorDeMail enviadorDeMail;

    @Test
    void crypterEsElDobleEnMemoria() {
        CrypterEnMemoria doble = assertInstanceOf(CrypterEnMemoria.class, crypterService);

        String resourceKey = crypterService.guardarArchivo("aG9sYQ==", true, "carpeta-1");

        assertEquals("aG9sYQ==", crypterService.getArchivo(resourceKey));
        assertEquals(1, doble.archivosEnCarpeta("carpeta-1").size());
    }

    @Test
    void elMailEsElDobleEnMemoria() throws Exception {
        assertInstanceOf(EnviadorDeMailEnMemoria.class, enviadorDeMail);

        enviadorDeMail.enviar("a@b.com", "Asunto", "<p>Hola</p>");

        assertEquals(1, mails().enviados().size());
    }
}
