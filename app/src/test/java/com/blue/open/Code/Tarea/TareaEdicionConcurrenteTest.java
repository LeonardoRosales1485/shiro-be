package com.blue.open.Code.Tarea;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * MJ-02 del plan de mejoras: dos guardados simultaneos de la misma tarea con la misma version leida. Uno responde 200
 * y el otro 409 TAREA_MODIFICADA (bloqueo optimista con @Version), aunque lleguen en el mismo instante.
 * Usa un puerto real y dos hilos, sin la transaccion del test: lo que crea queda guardado, en un proyecto propio (EDCO).
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TareaEdicionConcurrenteTest extends TestConBaseDeDatos {

    private static final int GUARDADOS = 2;

    // Token del actor SYSADMIN de los seeds (ActoresDelProyecto)
    private static final String TOKEN = "sysadmin-access-token";

    @LocalServerPort
    private int puerto;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dosGuardadosSimultaneosConLaMismaVersion() throws Exception {
        HttpClient cliente = HttpClient.newHttpClient();
        JsonNode proyecto = objectMapper.readTree(post(cliente, "/api/proyecto/guardar", Map.of("nombre", "Edicion concurrente", "prefijo", "EDCO")).body());
        String tablero = null;
        for (JsonNode t : proyecto.get("tableros"))
            if ("PRINCIPAL".equals(t.get("tipoTableroCodigo").asString()))
                tablero = t.get("uuid").asString();
        JsonNode tarea = objectMapper.readTree(post(cliente, "/api/tarea/guardar",
                Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA", "titulo", "Original")).body());
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(GUARDADOS);
        List<Future<HttpResponse<String>>> respuestas = new ArrayList<>();
        for (int i = 0; i < GUARDADOS; i++) {
            Map<String, Object> cuerpo = new HashMap<>(Map.of("uuid", tarea.get("uuid").asString(), "tableroUuid", tablero,
                    "tipoCodigo", "TAREA", "titulo", "Edicion " + i, "version", tarea.get("version").asLong()));
            respuestas.add(hilos.submit(() -> {
                largada.await();
                return post(cliente, "/api/tarea/guardar", cuerpo);
            }));
        }
        largada.countDown();

        List<Integer> estados = new ArrayList<>();
        for (Future<HttpResponse<String>> respuesta : respuestas) {
            HttpResponse<String> r = respuesta.get();
            estados.add(r.statusCode());
            if (r.statusCode() == 409)
                assertTrue(r.body().contains("TAREA_MODIFICADA"), r.body());
        }
        hilos.shutdown();

        estados.sort(null);
        assertEquals(List.of(200, 409), estados);
    }

    // POST con el token del SYSADMIN; responde la respuesta tal cual
    private HttpResponse<String> post(HttpClient cliente, String ruta, Map<String, Object> cuerpo) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(cuerpo)))
                .build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }
}
