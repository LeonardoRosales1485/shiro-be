package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.HistorialRepository;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-PRY-04 y CU-PRY-05: archivar y restaurar un proyecto (adenda 4 del DAF). Archivar arrastra todo lo que hay
 * dentro (tableros, tareas en todos los niveles, comentarios y relaciones) con el mismo id de operacion; restaurar
 * devuelve lo de esa operacion. Lo archivan el super admin y los Administradores.
 */
class ProyectoArchivarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    @Autowired
    private HistorialRepository historialRepository;

    @Test
    void archivarArrastraTodoYRestaurarLoDevuelve() {
        JsonNode proyecto = como(ADMIN).post("/proyecto/guardar").json(Map.of("nombre", "Archivable", "prefijo", "ARV"))
                .preparacion().ejecutar().esperar(200).json();
        String proyectoUuid = proyecto.get("uuid").asString();
        String tablero = proyecto.get("tableros").get(0).get("uuid").asString();
        String historia = tarea(tablero, "HISTORIA", "Historia", null);
        String subtarea = tarea(tablero, "SUBTAREA", "Subtarea", historia);
        String otra = tarea(tablero, "TAREA", "Otra", null);
        String archivadaAntes = tarea(tablero, "TAREA", "Archivada antes", null);
        como(ADMIN).post("/tarea/{uuid}/archivar", archivadaAntes).preparacion().ejecutar().esperar(200);
        como(ADMIN).post("/relacion/crear").json(Map.of("origenUuid", historia, "destinoUuid", otra, "tipo", "BLOQUEA"))
                .preparacion().ejecutar().esperar(200);
        como(ADMIN).post("/comentario/guardar").json(Map.of("tareaUuid", historia, "cuerpo", "Comentario"))
                .preparacion().ejecutar().esperar(200);

        assertEquals(-1, archivar(proyectoUuid).esperar(200).json().get("status").asInt());

        for (String uuid : List.of(historia, subtarea, otra)) {
            mover(uuid).esperarError(400, "REGISTRO_NO_ENCONTRADO");
        }
        assertEquals(0, relaciones(historia).size());
        assertEquals(0, comentarios(historia));
        como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA", "titulo", "Nueva"))
                .ejecutar().esperarError(400, "RELACION_NO_ENCONTRADA");

        assertEquals(0, restaurar(proyectoUuid).esperar(200).json().get("status").asInt());

        for (String uuid : List.of(historia, subtarea, otra)) {
            mover(uuid).esperar(200);
        }
        mover(archivadaAntes).esperarError(400, "REGISTRO_NO_ENCONTRADO");
        assertEquals(1, relaciones(historia).size());
        assertEquals(1, comentarios(historia));
        List<String> acciones = historialRepository.findByEntidadAndUuidEntidadOrderByIdAsc(Historial.PROYECTO, proyectoUuid)
                .stream().map(fila -> fila.getAccion().name()).toList();
        assertEquals(List.of("CREAR", "ARCHIVAR", "RESTAURAR"), acciones);
    }

    @Test
    void unProyectoVigenteNoSeRestauraYUnoArchivadoNoSeArchivaDosVeces() {
        restaurar(ProyectosDeSeed.SHIRO).esperarError(400, "REGISTRO_NO_ENCONTRADO");
        archivar(ProyectosDeSeed.ARCHIVADO).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // POST /proyecto/{uuid}/archivar como Administrador
    private Respuesta archivar(String uuid) {
        return como(ADMIN).post("/proyecto/{uuid}/archivar", uuid).ejecutar();
    }

    // POST /proyecto/{uuid}/restaurar como Administrador
    private Respuesta restaurar(String uuid) {
        return como(ADMIN).post("/proyecto/{uuid}/restaurar", uuid).ejecutar();
    }

    // Crea una tarea (hija del padre si no es null) y responde su uuid (paso de preparacion)
    private String tarea(String tablero, String tipo, String titulo, String padreUuid) {
        Map<String, Object> cuerpo = new HashMap<>(Map.of("tableroUuid", tablero, "tipoCodigo", tipo,
                "titulo", titulo));
        if (padreUuid != null)
            cuerpo.put("padreUuid", padreUuid);
        return como(ADMIN).post("/tarea/guardar").json(cuerpo).preparacion().ejecutar().esperar(200).json()
                .get("uuid").asString();
    }

    // POST /tarea/{uuid}/mover a En curso: solo funciona con una tarea vigente
    private Respuesta mover(String uuid) {
        return como(ADMIN).post("/tarea/{uuid}/mover", uuid).json(Map.of("estadoUuid", EN_CURSO)).ejecutar();
    }

    // Relaciones vigentes de la tarea
    private JsonNode relaciones(String uuid) {
        return como(ADMIN).get("/tarea/{uuid}/relaciones", uuid).preparacion().ejecutar().esperar(200).json();
    }

    // Cantidad de comentarios vigentes de la tarea
    private int comentarios(String uuid) {
        return como(ADMIN).post("/comentario/paginar").json(Map.of("pagina", 1, "tamano", 10, "filtros",
                List.of(List.of(Map.of("campo", "tareaUuid", "operacion", "=", "valor", uuid))))).preparacion()
                .ejecutar().esperar(200).json().get("contenido").size();
    }
}
