package com.blue.open.Code.Tarea;

import com.blue.open.Code.Usuario.AuthJwtServiceDePrueba;
import com.blue.open.Code.Usuario.EscenarioDeUsuario;
import com.blue.open.Core.Test.Peticion;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Busqueda de tareas (T5.4 del plan y Spec 3): POST /tarea/paginar con el parametro texto busca en español en la
 * clave, el titulo y la descripcion (con raices: "tablero" encuentra "tableros"), se combina con los filtros y
 * respeta los permisos.
 */
class TareaBusquedaContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Autowired
    private AuthJwtServiceDePrueba authJwtService;

    private final EscenarioDeTarea escenario = new EscenarioDeTarea(this::como);

    @Test
    void buscaEnEspanolPorTituloYDescripcion() {
        Map<String, Object> cuerpo = EscenarioDeTarea.rapida(TareasDeSeed.TABLERO_PRINCIPAL, "HISTORIA",
                "Importación de sprints desde Jira");
        cuerpo.put("descripcion", "Hay que traer los tableros con sus columnas.");
        String clave = escenario.guardar(cuerpo).get("clave").asString();

        assertTrue(claves(buscar(como(ADMIN), "tablero", null)).contains(clave));
        assertTrue(claves(buscar(como(ADMIN), "importación jira", null)).contains(clave));
        assertFalse(claves(buscar(como(ADMIN), "palabrainexistente", null)).contains(clave));
    }

    @Test
    void buscaPorClaveYSeCombinaConLosFiltros() {
        assertEquals(List.of(TareasDeSeed.SHI_1_CLAVE), claves(buscar(como(ADMIN), TareasDeSeed.SHI_1_CLAVE, null)));
        assertTrue(claves(buscar(como(ADMIN), TareasDeSeed.SHI_1_CLAVE, "BUG")).isEmpty());
    }

    @Test
    void filtraPorTableroComoElFront() {
        String enQa = escenario.crear(TareasDeSeed.TABLERO_QA, "TAREA", "Probar el tablero desde el front").get("clave").asString();
        Map<String, Object> pedido = Map.of("pagina", 1, "tamano", 500, "filtros", List.of(List.of(
                Map.of("campo", "tablero.uuid", "operacion", "=", "valor", TareasDeSeed.TABLERO_QA))),
                "ordenes", List.of(Map.of("campo", "numero", "direccion", "asc")));

        List<String> claves = claves(como(ADMIN).post("/tarea/paginar").json(pedido).ejecutar().esperar(200).json().get("contenido"));

        assertTrue(claves.contains(enQa));
        assertFalse(claves.contains(TareasDeSeed.SHI_1_CLAVE));
    }

    @Test
    void respetaLosPermisos() {
        String sinPermisos = authJwtService.jwtFirmado(new EscenarioDeUsuario(this::como).crearUuid("buscador"));

        assertTrue(claves(buscar(como("CLIENTE"), TareasDeSeed.SHI_1_CLAVE, null)).contains(TareasDeSeed.SHI_1_CLAVE));
        assertTrue(claves(buscar(sinAutenticacion().encabezado("Authorization", "Bearer " + sinPermisos),
                TareasDeSeed.SHI_1_CLAVE, null)).isEmpty());
    }

    // POST /tarea/paginar con el texto y, si no es null, el filtro por tipo
    private static JsonNode buscar(Peticion peticion, String texto, String tipo) {
        Map<String, Object> pedido = new HashMap<>(Map.of("pagina", 1, "tamano", 50,
                "parametros", List.of(Map.of("nombre", "texto", "valor", texto))));
        if (tipo != null)
            pedido.put("filtros", List.of(List.of(Map.of("campo", "tipoCodigo", "operacion", "=", "valor", tipo))));
        return peticion.post("/tarea/paginar").json(pedido).ejecutar().esperar(200).json().get("contenido");
    }

    private static List<String> claves(JsonNode tareas) {
        List<String> claves = new ArrayList<>();
        tareas.forEach(tarea -> claves.add(tarea.get("clave").asString()));
        return claves;
    }
}
