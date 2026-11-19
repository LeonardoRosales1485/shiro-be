package com.blue.open.Code.Comentario;

import com.blue.open.Code.Usuario.UsuariosDeSeed;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-COM-02 y CU-COM-03: comentar una tarea y listar sus comentarios (T1.9 del plan y adenda 3 del DAF). El autor
 * es quien comenta y el canal, el del request; solo el autor edita su comentario; cada comentario queda en el
 * historial de la tarea.
 */
class ComentarioGuardarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String SYSADMIN = "SYSADMIN";

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    @Test
    void comentarGuardaAutorCanalYFechaYQuedaEnElHistorial() {
        JsonNode comentario = guardar(ADMIN, nuevo(ComentarioContratoTest.SHI_1, "Lo tomo yo")).esperar(200).json();

        assertEquals(UsuariosDeSeed.DEMO, comentario.get("autorUuid").asString());
        assertEquals("WEB", comentario.get("canal").asString());
        assertEquals(ComentarioContratoTest.SHI_1, comentario.get("tareaUuid").asString());
        assertEquals(false, comentario.get("fecha").isNull());
        JsonNode ultima = como(ADMIN).get("/tarea/{uuid}/historial", ComentarioContratoTest.SHI_1).ejecutar()
                .esperar(200).json().get(0);
        assertEquals("COMENTAR", ultima.get("accion").asString());
        assertEquals("Lo tomo yo", ultima.get("valorNuevo").asString());
    }

    @Test
    void elAutorEditaSuComentario() {
        JsonNode comentario = guardar(ADMIN, nuevo(ComentarioContratoTest.SHI_1, "Primera versión")).esperar(200).json();
        Map<String, Object> edicion = nuevo(ComentarioContratoTest.SHI_1, "Segunda versión");
        edicion.put("uuid", comentario.get("uuid").asString());

        JsonNode editado = guardar(ADMIN, edicion).esperar(200).json();

        assertEquals("Segunda versión", editado.get("cuerpo").asString());
        assertEquals(UsuariosDeSeed.DEMO, editado.get("autorUuid").asString());
        assertEquals(comentario.get("fecha").asString(), editado.get("fecha").asString());
    }

    @Test
    void otroUsuarioNoEditaElComentario() {
        JsonNode comentario = guardar(SYSADMIN, nuevo(ComentarioContratoTest.SHI_1, "Del super admin")).esperar(200).json();
        Map<String, Object> edicion = nuevo(ComentarioContratoTest.SHI_1, "Editado por otro");
        edicion.put("uuid", comentario.get("uuid").asString());

        guardar(ADMIN, edicion).esperarError(403, "SIN_PERMISO");
    }

    @Test
    void tareaInexistenteRechazada() {
        guardar(ADMIN, nuevo("ffffffff-ffff-4fff-bfff-ffffffffffff", "Hola")).esperarError(400, "RELACION_NO_ENCONTRADA");
    }

    @Test
    void sinCuerpoRechazado() {
        guardar(ADMIN, Map.of("tareaUuid", ComentarioContratoTest.SHI_1)).esperarError(400, "FORMULARIO_INVALIDO");
    }

    @Test
    void listaLosComentariosDeUnaTarea() {
        String tarea = como(SYSADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL,
                "tipoCodigo", "TAREA", "titulo", "Con comentarios")).preparacion().ejecutar().esperar(200).json()
                .get("uuid").asString();
        guardar(ADMIN, nuevo(tarea, "Uno")).esperar(200);
        guardar(ADMIN, nuevo(tarea, "Dos")).esperar(200);

        JsonNode pagina = como(ADMIN).post("/comentario/paginar").json(Map.of("pagina", 0, "tamano", 10,
                "filtros", List.of(List.of(Map.of("campo", "tareaUuid", "operacion", "=", "valor", tarea))),
                "ordenes", List.of(Map.of("campo", "fecha", "direccion", "asc")))).ejecutar().esperar(200).json();

        assertEquals(2, pagina.get("contenido").size());
        assertEquals("Uno", pagina.get("contenido").get(0).get("cuerpo").asString());
    }

    // POST /comentario/guardar como el actor del rol
    private Respuesta guardar(String rol, Map<String, Object> cuerpo) {
        return como(rol).post("/comentario/guardar").json(cuerpo).ejecutar();
    }

    // Body de un comentario nuevo
    private static Map<String, Object> nuevo(String tareaUuid, String cuerpo) {
        Map<String, Object> comentario = new HashMap<>();
        comentario.put("tareaUuid", tareaUuid);
        comentario.put("cuerpo", cuerpo);
        return comentario;
    }
}
