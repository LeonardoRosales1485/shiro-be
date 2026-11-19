package com.blue.open.Code.TipoTarea;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-TTA-01: catalogo de tipos de tarea (GET /tipo-tarea/listar). T1.1 del plan: los cinco tipos fijos de la v1 con su
 * nivel en la jerarquia (seccion 4 del DAF): Epica 1; Historia, Tarea y Bug 2; Subtarea 3.
 */
class TipoTareaListarContratoTest extends PruebaDeContrato {

    @Test
    void listaLosCincoTiposConSuNivel() {
        JsonNode tipos = como(RolBaseEnum.ADMIN.name()).get("/tipo-tarea/listar").ejecutar().esperar(200).json();

        Map<String, Integer> niveles = new LinkedHashMap<>();
        tipos.forEach(tipo -> niveles.put(tipo.get("codigo").asString(), tipo.get("nivel").asInt()));
        Map<String, Integer> esperados = new LinkedHashMap<>();
        esperados.put("EPICA", 1);
        esperados.put("HISTORIA", 2);
        esperados.put("TAREA", 2);
        esperados.put("BUG", 2);
        esperados.put("SUBTAREA", 3);
        assertEquals(esperados, niveles);
        assertEquals("Épica", tipos.get(0).get("nombre").asString());
    }
}
