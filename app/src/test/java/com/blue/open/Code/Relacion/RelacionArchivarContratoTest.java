package com.blue.open.Code.Relacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-REL-02: archivar una relacion (POST /relacion/{uuid}/archivar). T1.9 del plan: deja de leerse desde las dos
 * tareas y queda en el historial de las dos.
 */
class RelacionArchivarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    @Test
    void laRelacionArchivadaDejaDeLeerseYQuedaEnElHistorial() {
        String a = tarea("A");
        String b = tarea("B");
        String relacion = como(ADMIN).post("/relacion/crear").json(Map.of("origenUuid", a, "destinoUuid", b,
                "tipo", "BLOQUEA")).preparacion().ejecutar().esperar(200).json().get("uuid").asString();

        archivar(relacion).esperar(200);

        assertEquals(0, como(ADMIN).get("/tarea/{uuid}/relaciones", a).ejecutar().esperar(200).json().size());
        assertEquals(0, como(ADMIN).get("/tarea/{uuid}/relaciones", b).ejecutar().esperar(200).json().size());
        JsonNode ultima = como(ADMIN).get("/tarea/{uuid}/historial", a).ejecutar().esperar(200).json().get(0);
        assertEquals("RELACIONAR", ultima.get("accion").asString());
        assertEquals(true, ultima.get("valorNuevo").isNull());
    }

    @Test
    void archivarDosVecesRechazado() {
        String relacion = como(ADMIN).post("/relacion/crear").json(Map.of("origenUuid", tarea("A"),
                "destinoUuid", tarea("B"), "tipo", "RELACIONA")).preparacion().ejecutar().esperar(200).json()
                .get("uuid").asString();
        archivar(relacion).esperar(200);

        archivar(relacion).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // POST /relacion/{uuid}/archivar como Administrador
    private Respuesta archivar(String uuid) {
        return como(ADMIN).post("/relacion/{uuid}/archivar", uuid).ejecutar();
    }

    // Crea una tarea en el tablero principal de Shiro y responde su uuid (paso de preparacion)
    private String tarea(String titulo) {
        return como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL, "tipoCodigo", "TAREA",
                "titulo", titulo)).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }
}
