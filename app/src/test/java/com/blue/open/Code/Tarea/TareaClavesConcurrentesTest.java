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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * T1.3 del plan y Spec 7: 50 altas simultaneas en el mismo proyecto reciben 50 claves distintas. El contador del
 * proyecto se bloquea por fila (SELECT ... FOR UPDATE); las claves no tienen que ser consecutivas.
 * Usa un puerto real y 50 hilos, sin la transaccion del test: lo que crea queda guardado. Por eso crea, por los
 * endpoints reales, un proyecto propio (CONC) para no sumar tareas al proyecto Shiro de los seeds.
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TareaClavesConcurrentesTest extends TestConBaseDeDatos {

    private static final int ALTAS = 50;

    // Token del actor SYSADMIN de los seeds (ActoresDelProyecto)
    private static final String TOKEN = "sysadmin-access-token";

    @LocalServerPort
    private int puerto;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cincuentaAltasSimultaneasRecibenClavesDistintas() throws Exception {
        HttpClient cliente = HttpClient.newHttpClient();
        String tablero = crearProyectoYDevolverSuTableroPrincipal(cliente);
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(ALTAS);
        List<Future<JsonNode>> respuestas = new ArrayList<>();
        for (int i = 0; i < ALTAS; i++) {
            String titulo = "Alta concurrente " + i;
            respuestas.add(hilos.submit(alta(cliente, largada, tablero, titulo)));
        }
        largada.countDown();

        List<String> creadas = new ArrayList<>();
        Set<String> claves = new HashSet<>();
        for (Future<JsonNode> respuesta : respuestas) {
            JsonNode tarea = respuesta.get();
            creadas.add(tarea.get("uuid").asString());
            claves.add(tarea.get("clave").asString());
        }
        hilos.shutdown();

        assertEquals(ALTAS, creadas.size());
        assertEquals(ALTAS, claves.size(), () -> "Claves repetidas: " + claves);
    }

    // Crea el proyecto CONC por POST /proyecto/guardar y responde el uuid de su tablero principal
    private String crearProyectoYDevolverSuTableroPrincipal(HttpClient cliente) throws Exception {
        JsonNode proyecto = guardar(cliente, "/api/proyecto/guardar",
                Map.of("nombre", "Altas concurrentes", "prefijo", "CONC"));
        for (JsonNode tablero : proyecto.get("tableros")) {
            if ("PRINCIPAL".equals(tablero.get("tipoTableroCodigo").asString())) {
                return tablero.get("uuid").asString();
            }
        }
        throw new IllegalStateException("El proyecto creado no tiene tablero principal: " + proyecto);
    }

    // Tarea que espera la largada y crea una tarea en modo rapido; responde la tarea creada o lanza si no fue 200
    private Callable<JsonNode> alta(HttpClient cliente, CountDownLatch largada, String tablero, String titulo) {
        return () -> {
            largada.await();
            return guardar(cliente, "/api/tarea/guardar",
                    Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA", "titulo", titulo));
        };
    }

    // POST con el token del SYSADMIN. Responde el body como JSON; lanza IllegalStateException si no fue 200
    private JsonNode guardar(HttpClient cliente, String ruta, Map<String, Object> cuerpo) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(cuerpo)))
                .build();
        HttpResponse<String> respuesta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("POST " + ruta + " fallo " + respuesta.statusCode() + ": " + respuesta.body());
        }
        return objectMapper.readTree(respuesta.body());
    }
}
