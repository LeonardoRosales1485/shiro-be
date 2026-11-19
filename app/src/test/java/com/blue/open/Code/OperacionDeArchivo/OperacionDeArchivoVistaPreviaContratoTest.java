package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-ARC-02: vista previa de un archivado (GET /archivo/{tipo}/{uuid}/vista-previa, propuesta 49 resuelta el 19/11):
 * que es, donde estaba, que se restaura con el (lo archivado en la misma operacion) y que queda archivado (lo
 * archivado por separado). En una tarea avisa si su padre sigue archivado. Exige ARCHIVAR sobre su recurso, como la
 * busqueda de archivados.
 */
class OperacionDeArchivoVistaPreviaContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    @Test
    void unaTareaDiceQueSeRestauraYQueQuedaArchivado() {
        String padre = tarea("TAREA", "Exportar a CSV", null);
        String separada = tarea("SUBTAREA", "Exportar a Excel", padre);
        String junta = tarea("SUBTAREA", "Armar el CSV", padre);
        como(ADMIN).post("/tarea/{uuid}/archivar", separada).preparacion().ejecutar().esperar(200);
        como(ADMIN).post("/tarea/{uuid}/archivar", padre).preparacion().ejecutar().esperar(200);

        JsonNode vista = como(ADMIN).get("/archivo/{tipo}/{uuid}/vista-previa", "TAREA", padre).ejecutar().esperar(200).json();

        assertTrue(vista.get("nombre").asString().endsWith("Exportar a CSV"));
        assertEquals("Principal · Por hacer", vista.get("ubicacion").asString());
        assertFalse(vista.get("padreArchivado").asBoolean());
        assertEquals(List.of(junta), uuids(vista.get("seRestauran")));
        assertEquals(1, vista.get("cantidadQueSeRestaura").asInt());
        assertEquals(List.of(separada), uuids(vista.get("quedanArchivadas")));
        assertFalse(vista.get("quedanArchivadas").get(0).get("fechaDeBorrado").isNull());
    }

    @Test
    void unaSubtareaConElPadreArchivadoLoAvisa() {
        String padre = tarea("TAREA", "Padre", null);
        String hija = tarea("SUBTAREA", "Hija", padre);
        como(ADMIN).post("/tarea/{uuid}/archivar", hija).preparacion().ejecutar().esperar(200);
        como(ADMIN).post("/tarea/{uuid}/archivar", padre).preparacion().ejecutar().esperar(200);

        JsonNode vista = como(ADMIN).get("/archivo/{tipo}/{uuid}/vista-previa", "TAREA", hija).ejecutar().esperar(200).json();

        assertTrue(vista.get("padreArchivado").asBoolean());
    }

    @Test
    void unProyectoCuentaSusTareas() {
        String proyecto = como(ADMIN).post("/proyecto/guardar").json(Map.of("nombre", "Proyecto AVP", "prefijo", "AVP")).preparacion()
                .ejecutar().esperar(200).json().get("uuid").asString();
        String tablero = como(ADMIN).get("/proyecto/uuid/{uuid}", proyecto).preparacion().ejecutar().esperar(200).json()
                .get("tableros").get(0).get("uuid").asString();
        como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA", "titulo", "Una")).preparacion().ejecutar().esperar(200);
        como(ADMIN).post("/proyecto/{uuid}/archivar", proyecto).preparacion().ejecutar().esperar(200);

        JsonNode vista = como(ADMIN).get("/archivo/{tipo}/{uuid}/vista-previa", "PROYECTO", proyecto).ejecutar().esperar(200).json();

        assertEquals(1, vista.get("cantidadQueSeRestaura").asInt());
    }

    @Test
    void sinArchivarSobreElRecursoNoSeVe() {
        String tarea = tarea("TAREA", "Ajena", null);
        como(ADMIN).post("/tarea/{uuid}/archivar", tarea).preparacion().ejecutar().esperar(200);

        como("CLIENTE").get("/archivo/{tipo}/{uuid}/vista-previa", "TAREA", tarea).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    @Test
    void unaTareaVigenteNoTieneVistaPrevia() {
        como(ADMIN).get("/archivo/{tipo}/{uuid}/vista-previa", "TAREA", "b3000000-0000-4000-8000-000000000001").ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    private String tarea(String tipo, String titulo, String padre) {
        Map<String, Object> cuerpo = new java.util.HashMap<>(Map.of("tableroUuid", TABLERO_PRINCIPAL, "tipoCodigo", tipo, "titulo", titulo));
        if (padre != null)
            cuerpo.put("padreUuid", padre);
        return como(ADMIN).post("/tarea/guardar").json(cuerpo).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }

    private static List<String> uuids(JsonNode elementos) {
        List<String> uuids = new ArrayList<>();
        elementos.forEach(elemento -> uuids.add(elemento.get("uuid").asString()));
        return uuids;
    }
}
