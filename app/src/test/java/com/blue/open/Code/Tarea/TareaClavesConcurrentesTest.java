package com.blue.open.Code.Tarea;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * Usa un puerto real y 50 hilos: corre sin la transaccion del test, y al terminar borra como duenio de la base las
 * tareas que creo (el usuario de la aplicacion no puede borrar en tablas shr_).
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
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    private final List<String> creadas = new ArrayList<>();

    @Test
    void cincuentaAltasSimultaneasRecibenClavesDistintas() throws Exception {
        HttpClient cliente = HttpClient.newHttpClient();
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(ALTAS);
        List<Future<JsonNode>> respuestas = new ArrayList<>();
        for (int i = 0; i < ALTAS; i++) {
            String titulo = "Alta concurrente " + i;
            respuestas.add(hilos.submit(alta(cliente, largada, titulo)));
        }
        largada.countDown();

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

    // Borra como duenio de la base las tareas creadas y sus etiquetas
    @AfterEach
    void borrarTareasCreadas() {
        for (String uuid : creadas) {
            jdbc.update("DELETE FROM shr_tarea_etiqueta WHERE id_tarea = (SELECT id FROM shr_tarea WHERE uuid = ?)", uuid);
            jdbc.update("DELETE FROM shr_tarea WHERE uuid = ?", uuid);
        }
    }

    // Tarea que espera la largada y crea una tarea en modo rapido; responde la tarea creada o lanza si no fue 200
    private Callable<JsonNode> alta(HttpClient cliente, CountDownLatch largada, String titulo) {
        return () -> {
            String cuerpo = objectMapper.writeValueAsString(Map.of("tableroUuid",
                    TareasDeSeed.TABLERO_PRINCIPAL, "tipoCodigo", "TAREA", "titulo", titulo));
            HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/tarea/guardar"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                    .build();
            largada.await();
            HttpResponse<String> respuesta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new IllegalStateException("Alta fallida " + respuesta.statusCode() + ": " + respuesta.body());
            }
            return objectMapper.readTree(respuesta.body());
        };
    }
}
