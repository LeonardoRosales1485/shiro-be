package com.blue.open.Code.Sprint;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-SPR-10: restaurar un sprint archivado (POST /sprint/{uuid}/restaurar, T6.4: desde Archivados se restaura todo lo
 * que se archiva). Vuelve con el estado que tenia (planificado o cerrado); las tareas de un planificado ya habian
 * vuelto al backlog al archivarlo. Exige EDITAR, como archivar.
 */
class SprintRestaurarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Test
    void unSprintArchivadoVuelve() {
        String proyecto = como(ADMIN).post("/proyecto/guardar").json(Map.of("nombre", "Proyecto SRA", "prefijo", "SRA"))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
        String sprint = como(ADMIN).post("/sprint/guardar").json(Map.of("proyectoUuid", proyecto, "nombre", "Sprint A",
                "inicio", "2026-12-01", "fin", "2026-12-14")).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
        como(ADMIN).post("/sprint/{uuid}/archivar", sprint).preparacion().ejecutar().esperar(200);

        var restaurado = como(ADMIN).post("/sprint/{uuid}/restaurar", sprint).ejecutar().esperar(200).json();

        assertEquals(0, restaurado.get("status").asInt());
        assertEquals("PLANIFICADO", restaurado.get("estado").asString());
        como(ADMIN).get("/sprint/uuid/{uuid}", sprint).ejecutar().esperar(200);
    }

    @Test
    void unSprintVigenteNoSeRestaura() {
        como(ADMIN).post("/sprint/{uuid}/restaurar", "b6000000-0000-4000-8000-000000000001").ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    @Test
    void sinEditarNoSeRestaura() {
        como("CLIENTE").post("/sprint/{uuid}/restaurar", "b6000000-0000-4000-8000-000000000001").ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    @Test
    void sinEditarEnElProyectoNoSeRestauraUnArchivado() {
        String sprint = como(ADMIN).post("/sprint/guardar").json(Map.of("proyectoUuid", "b1000000-0000-4000-8000-000000000001", "nombre", "Sprint sin permiso",
                "inicio", "2026-12-01", "fin", "2026-12-14")).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
        como(ADMIN).post("/sprint/{uuid}/archivar", sprint).preparacion().ejecutar().esperar(200);

        como("CLIENTE").post("/sprint/{uuid}/restaurar", sprint).ejecutar().esperarError(403, "SIN_PERMISO");
    }
}
