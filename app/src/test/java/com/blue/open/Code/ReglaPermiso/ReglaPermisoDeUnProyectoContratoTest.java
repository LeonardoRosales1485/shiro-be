package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-RPE-02 y CU-RPE-03 como los usa la pestaña Permisos de la administracion (T6.5): las reglas de un proyecto y de
 * sus tableros en un solo pedido (dos condiciones en el mismo grupo de filtros, que se combinan con O) y cambiar el
 * efecto de una regla existente.
 */
class ReglaPermisoDeUnProyectoContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Test
    void listaLasReglasDelProyectoYDeSusTablerosYCambiaElEfecto() {
        JsonNode deTablero = como(ADMIN).post("/regla-permiso/guardar").json(regla()).preparacion().ejecutar().esperar(200).json();

        JsonNode reglas = como(ADMIN).post("/regla-permiso/paginar").json(Map.of("pagina", 1, "tamano", 500, "filtros", List.of(List.of(
                Map.of("campo", "proyecto.uuid", "operacion", "=", "valor", ReglaPermisoContratoTest.PROYECTO_SHIRO),
                Map.of("campo", "tablero.proyecto.uuid", "operacion", "=", "valor", ReglaPermisoContratoTest.PROYECTO_SHIRO)))))
                .ejecutar().esperar(200).json().get("contenido");

        boolean conProyecto = false;
        boolean conTablero = false;
        for (JsonNode regla : reglas) {
            conProyecto |= "PROYECTO".equals(regla.get("tipoRecurso").asString());
            conTablero |= deTablero.get("uuid").asString().equals(regla.get("uuid").asString());
        }
        assertTrue(conProyecto && conTablero, reglas::toString);

        Map<String, Object> negada = regla();
        negada.put("uuid", deTablero.get("uuid").asString());
        negada.put("efecto", "NEGAR");
        assertEquals("NEGAR", como(ADMIN).post("/regla-permiso/guardar").json(negada).ejecutar().esperar(200).json()
                .get("efecto").asString());
    }

    // Regla del grupo CLIENTE que permite ver el tablero QA del proyecto Shiro
    private static Map<String, Object> regla() {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tipoSujeto", "ROL");
        cuerpo.put("rolUuid", ReglaPermisoContratoTest.ROL_CLIENTE);
        cuerpo.put("tipoRecurso", "TABLERO");
        cuerpo.put("tableroUuid", ReglaPermisoContratoTest.TABLERO_QA);
        cuerpo.put("nivel", "VER");
        cuerpo.put("efecto", "PERMITIR");
        return cuerpo;
    }
}
