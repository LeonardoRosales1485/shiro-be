package com.blue.open.Code.Common.Configuracion;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CORS del proyecto (CorsConfig): solo los origenes del front (front.url.cors; en el profile test, el origen de
 * front.url) reciben Access-Control-Allow-Origin y Access-Control-Allow-Credentials. Otro origen no recibe headers
 * CORS (el navegador bloquea la lectura); el 403 con codigo de un POST por cookie lo cubren las pruebas de
 * ORIGEN_NO_PERMITIDO de /usuario. No son pruebas de contrato: el CORS lo resuelve
 * el filtro de Spring Security antes de llegar a un controller.
 */
class CorsConfigTest extends TestConBaseDeDatos {

    private static final String FRONT = "http://localhost:4200";

    private static final String OTRO_ORIGEN = "https://sitio-malicioso.com";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightDelFront_permiteElOrigenConCredenciales() throws Exception {
        mockMvc.perform(options("/usuario/iniciar-sesion")
                        .header("Origin", FRONT)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONT))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void preflightDeOtroOrigen_noRecibeCredenciales() throws Exception {
        mockMvc.perform(options("/usuario/iniciar-sesion")
                        .header("Origin", OTRO_ORIGEN)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void getDeOtroOrigen_noRecibeCredenciales() throws Exception {
        mockMvc.perform(get("/actuator/health").header("Origin", OTRO_ORIGEN))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void getDelFront_exponeLosHeadersDeLasDescargas() throws Exception {
        mockMvc.perform(get("/actuator/health").header("Origin", FRONT))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONT))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("Content-Disposition")));
    }
}
