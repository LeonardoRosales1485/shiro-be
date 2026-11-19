package com.blue.open.Code.Version;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-VER-01 a CU-VER-03 con los datos de resumen de una version (decision del 19/11 sobre la propuesta 47): la
 * cantidad de tareas vigentes asignadas (para la lista del front) y quien la publico.
 */
class VersionResumenContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Test
    void laListaTraeLaCantidadDeTareasYLaPublicadaQuienLaPublico() {
        String proyecto = como(ADMIN).post("/proyecto/guardar").json(Map.of("nombre", "Proyecto VRS", "prefijo", "VRS"))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
        String tablero = como(ADMIN).get("/proyecto/uuid/{uuid}", proyecto).preparacion().ejecutar().esperar(200).json()
                .get("tableros").get(0).get("uuid").asString();
        String una = tarea(tablero, "Una");
        String otra = tarea(tablero, "Otra");
        String version = como(ADMIN).post("/version/guardar").json(Map.of("proyectoUuid", proyecto, "nombre", "1.0")).ejecutar()
                .esperar(200).json().get("uuid").asString();
        como(ADMIN).post("/version/{uuid}/asignar", version).json(Map.of("tareas", List.of(una, otra))).ejecutar().esperar(200);
        como(ADMIN).post("/version/{uuid}/quitar", version).json(Map.of("tareas", List.of(otra))).ejecutar().esperar(200);

        JsonNode enLista = como(ADMIN).post("/version/paginar").json(Map.of("pagina", 1, "tamano", 10,
                "filtros", List.of(List.of(Map.of("campo", "proyecto.uuid", "operacion", "=", "valor", proyecto))))).ejecutar()
                .esperar(200).json().get("contenido").get(0);
        assertEquals(1, enLista.get("cantidadDeTareas").asInt());
        assertTrue(enLista.get("publicadaPorNombre").isNull());

        JsonNode publicada = como(ADMIN).post("/version/{uuid}/publicar", version).json(Map.of("fecha", "2026-12-20")).ejecutar()
                .esperar(200).json();
        assertFalse(publicada.get("publicadaPorNombre").isNull());
        assertEquals(1, como(ADMIN).get("/version/uuid/{uuid}", version).ejecutar().esperar(200).json().get("cantidadDeTareas").asInt());
    }

    private String tarea(String tablero, String titulo) {
        return como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA", "titulo", titulo))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }
}
