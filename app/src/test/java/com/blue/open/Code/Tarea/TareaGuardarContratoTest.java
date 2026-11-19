package com.blue.open.Code.Tarea;

import com.blue.open.Code.Usuario.UsuariosDeSeed;
import com.blue.open.Core.Identity.Auth.RolBaseEnum;
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
 * CU-TAR-03: crear o editar una tarea (POST /tarea/guardar). T1.3 del plan, adenda 2 y adenda 3 del DAF: se crea solo
 * con tablero, tipo y titulo (modo rapido) o con todos los datos; nace en el primer estado de su tablero, con prioridad
 * Media y con quien la crea como informante; recibe una clave unica del proyecto (puede tener saltos). Al editar no
 * cambian la clave, el estado ni el tablero.
 */
class TareaGuardarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = RolBaseEnum.ADMIN.name();

    @Test
    void creaEnModoRapidoSoloConTipoYTitulo() {
        JsonNode tarea = guardar(ADMIN, rapida(TareasDeSeed.TABLERO_QA, "BUG", "El token revocado sigue respondiendo"))
                .esperar(200).json();

        assertTrue(tarea.get("clave").asString().matches("SHI-\\d+"), tarea::toString);
        assertEquals("BUG", tarea.get("tipoCodigo").asString());
        assertEquals("Por probar", tarea.get("estadoNombre").asString());
        assertEquals("QA", tarea.get("tableroCodigo").asString());
        assertEquals("MEDIA", tarea.get("prioridad").asString());
        assertEquals(UsuariosDeSeed.DEMO, tarea.get("informanteUuid").asString());
    }

    @Test
    void creaConTodosLosDatos() {
        Map<String, Object> cuerpo = new HashMap<>(rapida(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA",
                "Importar sprints desde Jira"));
        cuerpo.put("descripcion", "Trae los **sprints** y sus fechas.");
        cuerpo.put("responsableUuid", UsuariosDeSeed.SYSADMIN);
        cuerpo.put("prioridad", "ALTA");
        cuerpo.put("puntos", 5);
        cuerpo.put("vencimiento", "2026-12-15");
        cuerpo.put("etiquetas", List.of(Map.of("texto", "importador"), Map.of("texto", "jira")));

        JsonNode tarea = guardar(ADMIN, cuerpo).esperar(200).json();

        assertEquals("Trae los **sprints** y sus fechas.", tarea.get("descripcion").asString());
        assertEquals(UsuariosDeSeed.SYSADMIN, tarea.get("responsableUuid").asString());
        assertEquals("ALTA", tarea.get("prioridad").asString());
        assertEquals(5, tarea.get("puntos").asInt());
        assertEquals("2026-12-15", tarea.get("vencimiento").asString());
        assertEquals(List.of("importador", "jira"), etiquetas(tarea));
        assertEquals("Por hacer", tarea.get("estadoNombre").asString());
    }

    @Test
    void cadaTareaRecibeUnaClaveDistintaDelProyecto() {
        String primera = guardar(ADMIN, rapida(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Primera")).esperar(200)
                .json().get("clave").asString();
        String segunda = guardar(ADMIN, rapida(TareasDeSeed.TABLERO_QA, "BUG", "Segunda")).esperar(200)
                .json().get("clave").asString();

        assertNotEquals(primera, segunda);
        assertNotEquals(TareasDeSeed.SHI_1_CLAVE, primera);
        assertTrue(numero(segunda) > numero(primera), primera + " / " + segunda);
    }

    @Test
    void sinTituloOSinTipoRechazado() {
        guardar(ADMIN, Map.of("tableroUuid", TareasDeSeed.TABLERO_PRINCIPAL, "tipoCodigo", "TAREA"))
                .esperarError(400, "FORMULARIO_INVALIDO");
        guardar(ADMIN, Map.of("tableroUuid", TareasDeSeed.TABLERO_PRINCIPAL, "titulo", "Sin tipo"))
                .esperarError(400, "FORMULARIO_INVALIDO");
    }

    @Test
    void tipoInexistenteRechazado() {
        guardar(ADMIN, rapida(TareasDeSeed.TABLERO_PRINCIPAL, "SPIKE", "Investigar"))
                .esperarError(400, "TIPO_DE_TAREA_INEXISTENTE");
    }

    @Test
    void tableroInexistenteRechazado() {
        guardar(ADMIN, rapida(TareasDeSeed.TABLERO_INEXISTENTE, "TAREA", "Sin tablero"))
                .esperarError(400, "RELACION_NO_ENCONTRADA");
    }

    @Test
    void editarCambiaLosCamposYConservaClaveEstadoYTablero() {
        JsonNode creada = guardar(ADMIN, conEtiquetas(rapida(TareasDeSeed.TABLERO_PRINCIPAL, "TAREA", "Borrador"),
                "a", "b")).esperar(200).json();

        Map<String, Object> edicion = conEtiquetas(rapida(TareasDeSeed.TABLERO_PRINCIPAL, "BUG", "Definitivo"), "b", "c");
        edicion.put("uuid", creada.get("uuid").asString());
        edicion.put("fechaDeActualizacion", creada.get("fechaDeActualizacion").asString());
        edicion.put("prioridad", "BAJA");
        JsonNode editada = guardar(ADMIN, edicion).esperar(200).json();

        assertEquals(creada.get("clave").asString(), editada.get("clave").asString());
        assertEquals("Definitivo", editada.get("titulo").asString());
        assertEquals("BUG", editada.get("tipoCodigo").asString());
        assertEquals("BAJA", editada.get("prioridad").asString());
        assertEquals("Por hacer", editada.get("estadoNombre").asString());
        assertEquals(List.of("b", "c"), etiquetas(editada));
    }

    @Test
    void elTableroNoSeCambia() {
        Map<String, Object> edicion = rapida(TareasDeSeed.TABLERO_QA, "TAREA", "Mover de tablero");
        edicion.put("uuid", TareasDeSeed.SHI_1);
        edicion.put("fechaDeActualizacion", como(ADMIN).get("/tarea/uuid/{uuid}", TareasDeSeed.SHI_1).ejecutar()
                .esperar(200).json().get("fechaDeActualizacion").asString());

        JsonNode error = guardar(ADMIN, edicion).esperarError(400, "TABLERO_DE_TAREA_NO_SE_CAMBIA").json();

        assertEquals("Una tarea no cambia de tablero. Para llevarla a otro tablero, clonala.",
                error.get("mensajeParaUsuario").asString());
    }

    // POST /tarea/guardar como el actor del rol
    private Respuesta guardar(String rol, Map<String, Object> cuerpo) {
        return como(rol).post("/tarea/guardar").json(cuerpo).ejecutar();
    }

    // Body del modo rapido: tablero, tipo y titulo
    private static Map<String, Object> rapida(String tableroUuid, String tipoCodigo, String titulo) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tableroUuid", tableroUuid);
        cuerpo.put("tipoCodigo", tipoCodigo);
        cuerpo.put("titulo", titulo);
        return cuerpo;
    }

    // El mismo body con las etiquetas recibidas
    private static Map<String, Object> conEtiquetas(Map<String, Object> cuerpo, String... textos) {
        List<Map<String, String>> etiquetas = new ArrayList<>();
        for (String texto : textos) {
            etiquetas.add(Map.of("texto", texto));
        }
        cuerpo.put("etiquetas", etiquetas);
        return cuerpo;
    }

    // Textos de las etiquetas de la tarea, en el orden en que llegan
    private static List<String> etiquetas(JsonNode tarea) {
        List<String> textos = new ArrayList<>();
        tarea.get("etiquetas").forEach(etiqueta -> textos.add(etiqueta.get("texto").asString()));
        return textos;
    }

    // Numero de una clave (SHI-12 -> 12)
    private static int numero(String clave) {
        return Integer.parseInt(clave.substring(clave.indexOf('-') + 1));
    }
}
