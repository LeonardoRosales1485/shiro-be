package com.blue.open.Code.Tarea;

import com.blue.open.Code.Usuario.UsuariosDeSeed;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-TAR-09: clonar una tarea a otro tablero del mismo proyecto eligiendo que se copia (POST /tarea/{uuid}/clonar).
 * T1.10 del plan y adenda 1 del DAF: titulo y tipo siempre; descripcion, prioridad y etiquetas por defecto;
 * estimacion, responsable, versiones, comentarios (con autor y fecha originales) y subtareas (subtareas del clon, en
 * el tablero de destino) a eleccion. El original y el clon quedan vinculados y cada uno muestra el estado del otro.
 */
class TareaClonarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String SYSADMIN = "SYSADMIN";

    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void conLaSeleccionPorDefecto() {
        JsonNode original = originalCompleta();

        JsonNode clon = clonar(uuid(original), TareasDeSeed.TABLERO_QA, null).esperar(200).json();

        assertNotEquals(original.get("clave").asString(), clon.get("clave").asString());
        assertEquals("QA", clon.get("tableroCodigo").asString());
        assertEquals("Por probar", clon.get("estadoNombre").asString());
        assertEquals("Importar sprints", clon.get("titulo").asString());
        assertEquals("HISTORIA", clon.get("tipoCodigo").asString());
        assertEquals("Trae los sprints.", clon.get("descripcion").asString());
        assertEquals("ALTA", clon.get("prioridad").asString());
        assertEquals(List.of("importador", "jira"), etiquetas(clon));
        assertTrue(clon.get("puntos").isNull());
        assertTrue(clon.get("responsableUuid").isNull());
        assertEquals(0, clon.get("hijas").size());
        assertEquals(0, comentarios(uuid(clon)).size());
    }

    @Test
    void conTodo() {
        JsonNode original = originalCompleta();
        JsonNode comentarioOriginal = comentarios(uuid(original)).get(0);

        JsonNode clon = clonar(uuid(original), TareasDeSeed.TABLERO_QA, List.of("DESCRIPCION", "PRIORIDAD", "ETIQUETAS",
                "PUNTOS", "RESPONSABLE", "VERSIONES", "COMENTARIOS", "SUBTAREAS")).esperar(200).json();

        assertEquals(5, clon.get("puntos").asInt());
        assertEquals(UsuariosDeSeed.SYSADMIN, clon.get("responsableUuid").asString());
        assertEquals(1, clon.get("hijas").size());
        JsonNode subtareaClonada = escenario.obtener(clon.get("hijas").get(0).get("uuid").asString());
        assertEquals("Mapear estados", subtareaClonada.get("titulo").asString());
        assertEquals("QA", subtareaClonada.get("tableroCodigo").asString());
        assertEquals(clon.get("clave").asString(), subtareaClonada.get("padreClave").asString());
        JsonNode comentarioCopiado = comentarios(uuid(clon)).get(0);
        assertEquals("Ojo con los sprints cerrados.", comentarioCopiado.get("cuerpo").asString());
        assertEquals(comentarioOriginal.get("autorUuid").asString(), comentarioCopiado.get("autorUuid").asString());
        assertEquals(comentarioOriginal.get("fecha").asString(), comentarioCopiado.get("fecha").asString());
    }

    @Test
    void conLoMinimo() {
        JsonNode original = originalCompleta();

        JsonNode clon = clonar(uuid(original), TareasDeSeed.TABLERO_PRINCIPAL, List.of()).esperar(200).json();

        assertEquals("Importar sprints", clon.get("titulo").asString());
        assertEquals("HISTORIA", clon.get("tipoCodigo").asString());
        assertTrue(clon.get("descripcion").isNull());
        assertEquals("MEDIA", clon.get("prioridad").asString());
        assertEquals(0, clon.get("etiquetas").size());
    }

    @Test
    void elClonMuestraElEstadoDelOriginalYViceversa() {
        JsonNode original = escenario.crear(TareasDeSeed.TABLERO_QA, "BUG", "El token revocado sigue respondiendo");
        JsonNode clon = clonar(uuid(original), TareasDeSeed.TABLERO_PRINCIPAL, null).esperar(200).json();
        como(ADMIN).post("/tarea/{uuid}/mover", uuid(clon)).json(Map.of("estadoUuid", EN_CURSO)).ejecutar().esperar(200);

        JsonNode desdeElClon = relaciones(uuid(clon)).get(0);
        JsonNode desdeElOriginal = relaciones(uuid(original)).get(0);

        assertEquals("CLONADA_DE", desdeElClon.get("vinculo").asString());
        assertEquals(original.get("clave").asString(), desdeElClon.get("tareaClave").asString());
        assertEquals("Por probar", desdeElClon.get("tareaEstadoNombre").asString());
        assertEquals("CLONADA_EN", desdeElOriginal.get("vinculo").asString());
        assertEquals("En curso", desdeElOriginal.get("tareaEstadoNombre").asString());
        JsonNode historialDelClon = como(ADMIN).get("/tarea/{uuid}/historial", uuid(clon)).ejecutar().esperar(200).json();
        assertTrue(historialDelClon.toString().contains("\"accion\":\"CLONAR\""), historialDelClon::toString);
    }

    @Test
    void elClonDeUnaSubtareaPideSuDestino() {
        JsonNode subtarea = subtareaDeUnaHistoria();

        clonarSubtarea(uuid(subtarea), null, null).esperarError(400, "CLON_DE_SUBTAREA_SIN_DESTINO");
        clonarSubtarea(uuid(subtarea), "TAREA", uuid(subtarea)).esperarError(400, "CLON_DE_SUBTAREA_SIN_DESTINO");
    }

    @Test
    void elClonDeUnaSubtareaPuedeSerUnaTareaPropia() {
        JsonNode subtarea = subtareaDeUnaHistoria();

        JsonNode clon = clonarSubtarea(uuid(subtarea), "BUG", null).esperar(200).json();

        assertEquals("BUG", clon.get("tipoCodigo").asString());
        assertTrue(clon.get("padreUuid").isNull());
    }

    @Test
    void elClonDeUnaSubtareaPuedeColgarDeOtraTarea() {
        JsonNode subtarea = subtareaDeUnaHistoria();
        JsonNode otra = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Otra tarea");

        JsonNode clon = clonarSubtarea(uuid(subtarea), null, uuid(otra)).esperar(200).json();

        assertEquals("SUBTAREA", clon.get("tipoCodigo").asString());
        assertEquals(otra.get("clave").asString(), clon.get("padreClave").asString());
    }

    @Test
    void comoTareaPropiaSoloHistoriaTareaOBug() {
        JsonNode subtarea = subtareaDeUnaHistoria();

        clonarSubtarea(uuid(subtarea), "EPICA", null).esperarError(400, "TIPO_DE_CLON_INVALIDO");
        clonarSubtarea(uuid(subtarea), "SUBTAREA", null).esperarError(400, "TIPO_DE_CLON_INVALIDO");
    }

    @Test
    void elClonDeUnaHistoriaNoHeredaElPadre() {
        JsonNode epica = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "EPICA", "Épica");
        JsonNode historia = escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia", uuid(epica));

        JsonNode clon = clonar(uuid(historia), TareasDeSeed.TABLERO_PRINCIPAL, null).esperar(200).json();

        assertTrue(clon.get("padreUuid").isNull());
    }

    @Test
    void tableroDeOtroProyectoRechazado() {
        String tableroAjeno = escenario.crearProyectoYDevolverTablero("CLO", "PRINCIPAL");

        clonar(TareasDeSeed.SHI_1, tableroAjeno, null).esperarError(400, "TABLERO_DE_OTRO_PROYECTO");
    }

    @Test
    void tableroInexistenteRechazado() {
        clonar(TareasDeSeed.SHI_1, TareasDeSeed.TABLERO_INEXISTENTE, null).esperarError(400, "RELACION_NO_ENCONTRADA");
    }

    @Test
    void tareaInexistenteRechazada() {
        clonar(TareasDeSeed.TABLERO_INEXISTENTE, TareasDeSeed.TABLERO_QA, null).esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    @Test
    void sinTableroRechazado() {
        como(ADMIN).post("/tarea/{uuid}/clonar", TareasDeSeed.SHI_1).json(Map.of()).ejecutar()
                .esperarError(400, "FORMULARIO_INVALIDO");
    }

    // Historia con todos los campos, una subtarea y un comentario, en el tablero principal
    private JsonNode originalCompleta() {
        Map<String, Object> cuerpo = EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Importar sprints");
        cuerpo.put("descripcion", "Trae los sprints.");
        cuerpo.put("prioridad", "ALTA");
        cuerpo.put("puntos", 5);
        cuerpo.put("responsableUuid", UsuariosDeSeed.SYSADMIN);
        cuerpo.put("etiquetas", List.of(Map.of("texto", "importador"), Map.of("texto", "jira")));
        JsonNode original = escenario.guardar(cuerpo);
        escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Mapear estados", uuid(original));
        como(SYSADMIN).post("/comentario/guardar").json(Map.of("tareaUuid", uuid(original),
                "cuerpo", "Ojo con los sprints cerrados.")).preparacion().ejecutar().esperar(200);
        return original;
    }

    // POST /tarea/{uuid}/clonar como Administrador; copiar null usa la seleccion por defecto
    // Subtarea nueva, hija de una historia nueva, en el tablero principal
    private JsonNode subtareaDeUnaHistoria() {
        JsonNode historia = escenario.crear(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA", "Historia");
        return escenario.crearHija(TareasDeSeed.TABLERO_PRINCIPAL, "SUBTAREA", "Subtarea", uuid(historia));
    }

    // Clona una subtarea al tablero QA como tarea propia (tipo) o como subtarea de otra tarea (padre)
    private Respuesta clonarSubtarea(String uuid, String tipoCodigo, String padreUuid) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tableroUuid", TareasDeSeed.TABLERO_QA);
        cuerpo.put("tipoCodigo", tipoCodigo);
        cuerpo.put("padreUuid", padreUuid);
        return como(ADMIN).post("/tarea/{uuid}/clonar", uuid).json(cuerpo).ejecutar();
    }

    private Respuesta clonar(String uuid, String tableroUuid, List<String> copiar) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tableroUuid", tableroUuid);
        if (copiar != null)
            cuerpo.put("copiar", copiar);
        return como(ADMIN).post("/tarea/{uuid}/clonar", uuid).json(cuerpo).ejecutar();
    }

    // Comentarios vigentes de una tarea, por fecha
    private JsonNode comentarios(String tareaUuid) {
        return como(ADMIN).post("/comentario/paginar").json(Map.of("pagina", 1, "tamano", 10,
                "filtros", List.of(List.of(Map.of("campo", "tareaUuid", "operacion", "=", "valor", tareaUuid))),
                "ordenes", List.of(Map.of("campo", "fecha", "direccion", "asc")))).preparacion().ejecutar()
                .esperar(200).json().get("contenido");
    }

    // GET /tarea/{uuid}/relaciones
    private JsonNode relaciones(String uuid) {
        return como(ADMIN).get("/tarea/{uuid}/relaciones", uuid).ejecutar().esperar(200).json();
    }

    // Textos de las etiquetas de la tarea
    private static List<String> etiquetas(JsonNode tarea) {
        List<String> textos = new ArrayList<>();
        tarea.get("etiquetas").forEach(etiqueta -> textos.add(etiqueta.get("texto").asString()));
        return textos;
    }

    private static String uuid(JsonNode tarea) {
        return tarea.get("uuid").asString();
    }
}
