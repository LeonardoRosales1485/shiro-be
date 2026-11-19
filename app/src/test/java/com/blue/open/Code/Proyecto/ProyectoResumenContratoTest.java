package com.blue.open.Code.Proyecto;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-PRY-01 y CU-PRY-02 con la cantidad de tareas vigentes del proyecto (T6.5, prototipo "Administracion ·
 * proyectos"): la muestra la pestaña Proyectos de la administracion. Las archivadas no cuentan.
 */
class ProyectoResumenContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Test
    void obtenerYPaginarTraenLaCantidadDeTareasVigentes() {
        String proyecto = como(ADMIN).post("/proyecto/guardar").json(Map.of("nombre", "Proyecto PRS", "prefijo", "PRS"))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
        String tablero = como(ADMIN).get("/proyecto/uuid/{uuid}", proyecto).preparacion().ejecutar().esperar(200).json()
                .get("tableros").get(0).get("uuid").asString();
        tarea(tablero, "Una");
        String archivada = tarea(tablero, "Archivada");
        como(ADMIN).post("/tarea/{uuid}/archivar", archivada).preparacion().ejecutar().esperar(200);

        assertEquals(1, como(ADMIN).get("/proyecto/uuid/{uuid}", proyecto).ejecutar().esperar(200).json()
                .get("cantidadDeTareas").asInt());
        JsonNode enLista = como(ADMIN).post("/proyecto/paginar").json(Map.of("pagina", 1, "tamano", 10,
                "filtros", List.of(List.of(Map.of("campo", "prefijo", "operacion", "=", "valor", "PRS"))))).ejecutar()
                .esperar(200).json().get("contenido").get(0);
        assertEquals(1, enLista.get("cantidadDeTareas").asInt());
    }

    private String tarea(String tablero, String titulo) {
        return como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA", "titulo", titulo))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }
}
