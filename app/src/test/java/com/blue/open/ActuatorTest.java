package com.blue.open;

import com.blue.open.Core.Kernel.CoreVersion;
import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoints de Actuator que exige docs/rules/versionado.md (/health e /info, publicos y con el header API-Version).
 * No son pruebas de contrato: la completitud excluye los endpoints de Spring.
 */
class ActuatorTest extends TestConBaseDeDatos {

    @Autowired
    private MockMvc mockMvc;

    // <version> de app/pom.xml: surefire la recibe del pom (contratos.version), sin pasar por app-version.properties
    @Value("${contratos.version}")
    private String versionDelPom;

    @Test
    void healthRespondeUpSinAutenticacion() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void infoRespondeLaVersionDelProyectoYLaDelCore() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(versionDelPom))
                .andExpect(jsonPath("$.coreVersion").value(CoreVersion.get()))
                .andExpect(header().string("API-Version", versionDelPom));
    }
}
