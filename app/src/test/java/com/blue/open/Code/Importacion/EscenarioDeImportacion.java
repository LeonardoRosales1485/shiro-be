package com.blue.open.Code.Importacion;

import com.blue.open.Core.Test.Peticion;
import com.blue.open.Core.Test.Respuesta;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Builder de escenarios del importador de Jira para las pruebas de contrato: conecta con el Jira simulado, manda el
 * mapeo completo (tableros Desarrollo a Principal y QA a QA, estados y tipos) y ejecuta. Todo por los endpoints reales.
 */
public final class EscenarioDeImportacion {

    public static final String SYSADMIN = "SYSADMIN";

    // Catalogos de los seeds (2026-11-18-catalogos.yaml)
    public static final String TIPO_TABLERO_PRINCIPAL = "a1000000-0000-4000-8000-000000000001";

    public static final String TIPO_TABLERO_QA = "a1000000-0000-4000-8000-000000000002";

    public static final String POR_HACER = "a2000000-0000-4000-8000-000000000011";

    public static final String HECHO = "a2000000-0000-4000-8000-000000000015";

    public static final String POR_PROBAR = "a2000000-0000-4000-8000-000000000021";

    public static final String APROBADO = "a2000000-0000-4000-8000-000000000024";

    public static final Map<String, String> TIPOS = Map.of(
            "10000", "a3000000-0000-4000-8000-000000000001",
            "10001", "a3000000-0000-4000-8000-000000000002",
            "10002", "a3000000-0000-4000-8000-000000000003",
            "10003", "a3000000-0000-4000-8000-000000000004",
            "10004", "a3000000-0000-4000-8000-000000000005");

    private static final ObjectMapper LECTOR = new ObjectMapper();

    private final Function<String, Peticion> como;

    private final JiraSimulado jira;

    public EscenarioDeImportacion(Function<String, Peticion> como, JiraSimulado jira) {
        this.como = como;
        this.jira = jira;
    }

    // Cuerpo de POST /importacion/conectar con las credenciales del Jira simulado y la clave recibida
    public Map<String, Object> credenciales(String clave) {
        return Map.of("url", jira.url(), "correo", JiraSimulado.CORREO, "token", JiraSimulado.TOKEN, "claveProyecto", clave);
    }

    // POST /importacion/conectar
    public Respuesta conectar(Map<String, Object> cuerpo) {
        return como.apply(SYSADMIN).post("/importacion/conectar").json(cuerpo).ejecutar();
    }

    // Conecta con el proyecto JSIM y responde el uuid de la importacion
    public String conectarJsim() {
        return conectar(credenciales(JiraSimulado.PROYECTO)).esperar(200).json().get("importacionUuid").asString();
    }

    // Mapeo completo del proyecto simulado, con los tipos recibidos (id de Jira a uuid de tipo de Shiro)
    public static Map<String, Object> mapeo(Map<String, String> tipos) {
        return Map.of(
                "tableros", List.of(
                        Map.of("idJira", JiraSimulado.TABLERO_DESARROLLO, "tipoTableroUuid", TIPO_TABLERO_PRINCIPAL,
                                "estados", List.of(Map.of("idJira", "1", "estadoUuid", POR_HACER),
                                        Map.of("idJira", "3", "estadoUuid", HECHO))),
                        Map.of("idJira", JiraSimulado.TABLERO_QA, "tipoTableroUuid", TIPO_TABLERO_QA,
                                "estados", List.of(Map.of("idJira", "21", "estadoUuid", POR_PROBAR)))),
                "tipos", tipos.entrySet().stream().map(t -> Map.of("idJira", t.getKey(), "tipoTareaUuid", t.getValue())).toList());
    }

    // POST /importacion/{uuid}/mapeo
    public Respuesta mapear(String importacion, Map<String, Object> cuerpo) {
        return como.apply(SYSADMIN).post("/importacion/{uuid}/mapeo", importacion).json(cuerpo).ejecutar();
    }

    // POST /importacion/{uuid}/ejecutar
    public Respuesta ejecutar(String importacion) {
        return como.apply(SYSADMIN).post("/importacion/{uuid}/ejecutar", importacion).ejecutar();
    }

    // Conecta, mapea todo y ejecuta. Responde la importacion terminada
    public JsonNode importarJsim() {
        String importacion = conectarJsim();
        mapear(importacion, mapeo(TIPOS)).esperar(200);
        return ejecutar(importacion).esperar(200).json();
    }

    // Resumen de la importacion (viene como JSON en el campo resumen)
    public static JsonNode resumen(JsonNode importacion) {
        return LECTOR.readTree(importacion.get("resumen").asString());
    }

    // Tarea por su clave (POST /tarea/paginar filtrando por clave)
    public JsonNode tareaPorClave(String clave) {
        JsonNode pagina = como.apply(SYSADMIN).post("/tarea/paginar").json(Map.of("pagina", 1, "tamano", 10,
                "filtros", List.of(List.of(Map.of("campo", "clave", "operacion", "=", "valor", clave)))))
                .preparacion().ejecutar().esperar(200).json();
        String uuid = pagina.get("contenido").get(0).get("uuid").asString();
        return como.apply(SYSADMIN).get("/tarea/uuid/{uuid}", uuid).preparacion().ejecutar().esperar(200).json();
    }
}
