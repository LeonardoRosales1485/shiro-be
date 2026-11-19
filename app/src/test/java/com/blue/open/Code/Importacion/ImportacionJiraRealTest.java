package com.blue.open.Code.Importacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prueba del importador contra un Jira real (Task 25 del plan). Solo corre si el entorno define JIRA_PRUEBA_URL,
 * JIRA_PRUEBA_CORREO, JIRA_PRUEBA_TOKEN y JIRA_PRUEBA_CLAVE; si no, JUnit la omite. Contra Jira solo hace lecturas
 * (GET). Conecta, mapea el primer tablero de Jira al tablero Principal creando todos sus estados y crea todos los
 * tipos, ejecuta y verifica que termine sin fallas con claves J-PRB-n. Todo corre en la transaccion de la prueba y se
 * revierte al terminar. No imprime credenciales.
 */
@EnabledIfEnvironmentVariable(named = "JIRA_PRUEBA_TOKEN", matches = ".+")
class ImportacionJiraRealTest extends PruebaDeContrato {

    // Prefijo de Shiro para el proyecto importado: las claves quedan J-PRB-n
    private static final String PREFIJO = "PRB";

    @Test
    void importaUnProyectoRealSinFallas() {
        Map<String, Object> credenciales = Map.of("url", System.getenv("JIRA_PRUEBA_URL"),
                "correo", System.getenv("JIRA_PRUEBA_CORREO"), "token", System.getenv("JIRA_PRUEBA_TOKEN"),
                "claveProyecto", System.getenv("JIRA_PRUEBA_CLAVE"), "prefijo", PREFIJO);
        JsonNode catalogo = como(EscenarioDeImportacion.SYSADMIN).post("/importacion/conectar").json(credenciales)
                .ejecutar().esperar(200).json();
        String importacion = catalogo.get("importacionUuid").asString();
        assertFalse(catalogo.get("tableros").isEmpty(), "El proyecto de Jira no tiene tableros");

        List<Map<String, Object>> estados = new ArrayList<>();
        catalogo.get("estados").forEach(estado -> estados.add(Map.of("idJira", estado.get("idJira").asString(), "crear", true)));
        List<Map<String, Object>> tipos = new ArrayList<>();
        catalogo.get("tipos").forEach(tipo -> tipos.add(Map.of("idJira", tipo.get("idJira").asString(), "crear", true)));
        Map<String, Object> mapeo = Map.of(
                "tableros", List.of(Map.of("idJira", catalogo.get("tableros").get(0).get("idJira").asString(),
                        "tipoTableroUuid", EscenarioDeImportacion.TIPO_TABLERO_PRINCIPAL, "estados", estados)),
                "tipos", tipos, "duracionDeSprintEnDias", 14);
        como(EscenarioDeImportacion.SYSADMIN).post("/importacion/{uuid}/mapeo", importacion).json(mapeo).ejecutar().esperar(200);

        JsonNode resultado = como(EscenarioDeImportacion.SYSADMIN).post("/importacion/{uuid}/ejecutar", importacion)
                .ejecutar().esperar(200).json();

        JsonNode resumen = EscenarioDeImportacion.resumen(resultado);
        assertEquals("TERMINADA", resultado.get("estado").asString(), "Resumen: " + resumen);
        assertEquals(0, resumen.get("fallas").size(), "Fallas: " + resumen.get("fallas"));
        assertTrue(resumen.get("tareasCreadas").asInt() > 0, "No se importo ninguna tarea");
        JsonNode pagina = como(EscenarioDeImportacion.SYSADMIN).post("/tarea/paginar").json(Map.of("pagina", 1, "tamano", 5,
                "filtros", List.of(List.of(Map.of("campo", "clave", "operacion", "like", "valor", "J-" + PREFIJO + "-%")))))
                .preparacion().ejecutar().esperar(200).json();
        assertFalse(pagina.get("contenido").isEmpty(), "No hay tareas con clave J-" + PREFIJO + "-n");
    }
}
