package com.blue.open.Code.Tarea;

import com.blue.open.Core.Test.Peticion;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Builder de proyectos y tareas para las pruebas de contrato: los crea por los endpoints reales (pasos de
 * preparacion, dentro de la transaccion del test: se revierten al terminar). Se construye con el cliente de la
 * prueba: new EscenarioDeTarea(this::como).
 */
public final class EscenarioDeTarea {

    // Actor de los pasos de preparacion
    public static final String SYSADMIN = "SYSADMIN";

    private final Function<String, Peticion> como;

    // Recibe el cliente de la prueba (rol -> peticion), normalmente this::como de una PruebaDeContrato
    public EscenarioDeTarea(Function<String, Peticion> como) {
        this.como = como;
    }

    // Body del modo rapido: tablero, tipo y titulo
    public static Map<String, Object> rapida(String tableroUuid, String tipoCodigo, String titulo) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tableroUuid", tableroUuid);
        cuerpo.put("tipoCodigo", tipoCodigo);
        cuerpo.put("titulo", titulo);
        return cuerpo;
    }

    // Crea una tarea en modo rapido (POST /tarea/guardar) y responde la tarea creada
    public JsonNode crear(String tableroUuid, String tipoCodigo, String titulo) {
        return guardar(rapida(tableroUuid, tipoCodigo, titulo));
    }

    // Crea una tarea hija del padre recibido y responde la tarea creada
    public JsonNode crearHija(String tableroUuid, String tipoCodigo, String titulo, String padreUuid) {
        Map<String, Object> cuerpo = rapida(tableroUuid, tipoCodigo, titulo);
        cuerpo.put("padreUuid", padreUuid);
        return guardar(cuerpo);
    }

    // Guarda la tarea del body (POST /tarea/guardar) y responde la tarea guardada
    public JsonNode guardar(Map<String, Object> cuerpo) {
        return como.apply(SYSADMIN).post("/tarea/guardar").json(cuerpo).preparacion().ejecutar().esperar(200).json();
    }

    // Obtiene la tarea (GET /tarea/uuid/{uuid})
    public JsonNode obtener(String uuid) {
        return como.apply(SYSADMIN).get("/tarea/uuid/{uuid}", uuid).preparacion().ejecutar().esperar(200).json();
    }

    // Crea un proyecto con el prefijo recibido (POST /proyecto/guardar) y responde el uuid de su tablero del tipo
    public String crearProyectoYDevolverTablero(String prefijo, String codigoDeTablero) {
        JsonNode proyecto = como.apply(SYSADMIN).post("/proyecto/guardar")
                .json(Map.of("nombre", "Proyecto " + prefijo, "prefijo", prefijo))
                .preparacion().ejecutar().esperar(200).json();
        for (JsonNode tablero : proyecto.get("tableros")) {
            if (codigoDeTablero.equals(tablero.get("tipoTableroCodigo").asString())) {
                return tablero.get("uuid").asString();
            }
        }
        throw new IllegalStateException("El proyecto " + prefijo + " no tiene tablero " + codigoDeTablero);
    }
}
