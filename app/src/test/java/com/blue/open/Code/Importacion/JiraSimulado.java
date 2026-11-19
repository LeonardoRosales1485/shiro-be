package com.blue.open.Code.Importacion;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Jira simulado para las pruebas del importador: un servidor HTTP local que responde como la API de Jira Cloud un
 * proyecto JSIM con dos tableros (Desarrollo y QA), estados, tipos, dos usuarios (uno existe en Shiro), cinco tareas
 * (una epica, una historia, un bug, una subtarea y una tarea de QA), comentarios, un sprint activo y una version
 * publicada. Exige autenticacion basic con el correo y el token de prueba.
 */
public final class JiraSimulado implements AutoCloseable {

    public static final String PROYECTO = "JSIM";

    public static final String CORREO = "importador@example.com";

    public static final String TOKEN = "token-de-jira-simulado";

    // Ids de Jira de tableros, estados y tipos
    public static final String TABLERO_DESARROLLO = "11";

    public static final String TABLERO_QA = "12";

    private static final String ISSUES_DESARROLLO = "{\"startAt\":0,\"maxResults\":50,\"total\":4,\"issues\":["
            + issue("10001", "JSIM-1", "Importador", "10000", "1", "Medium", null, null)
            + "," + issue("10002", "JSIM-2", "Importar sprints", "10001", "3", "High", "JSIM-1", "demo@example.local")
            + "," + issue("10003", "JSIM-4", "El token no se revoca", "10003", "1", "Highest", null, "nadie@example.com")
            + "," + issue("10004", "JSIM-7", "Mapear estados", "10004", "3", "Low", "JSIM-2", null)
            + "]}";

    private static final String ISSUES_QA = "{\"startAt\":0,\"maxResults\":50,\"total\":1,\"issues\":["
            + issue("10005", "JSIM-9", "Probar la importación", "10002", "21", "Medium", null, null) + "]}";

    private final HttpServer servidor;

    private JiraSimulado(HttpServer servidor) {
        this.servidor = servidor;
    }

    // Levanta el Jira simulado en un puerto libre
    public static JiraSimulado iniciar() throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", JiraSimulado::responder);
        servidor.start();
        return new JiraSimulado(servidor);
    }

    // URL base del Jira simulado
    public String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @Override
    public void close() {
        servidor.stop(0);
    }

    private static void responder(HttpExchange intercambio) throws IOException {
        String esperado = "Basic " + Base64.getEncoder().encodeToString((CORREO + ":" + TOKEN).getBytes(StandardCharsets.UTF_8));
        if (!esperado.equals(intercambio.getRequestHeaders().getFirst("Authorization"))) {
            enviar(intercambio, 401, "{\"errorMessages\":[\"No autorizado\"]}");
            return;
        }
        String ruta = intercambio.getRequestURI().getPath();
        String cuerpo = switch (ruta) {
            case "/rest/api/3/project/JSIM" -> "{\"id\":\"10000\",\"key\":\"JSIM\",\"name\":\"Proyecto simulado\"}";
            case "/rest/agile/1.0/board" -> "{\"values\":[{\"id\":11,\"name\":\"Desarrollo\",\"type\":\"scrum\"},"
                    + "{\"id\":12,\"name\":\"QA\",\"type\":\"kanban\"}]}";
            case "/rest/api/3/project/JSIM/statuses" -> "[{\"id\":\"10001\",\"name\":\"Story\",\"statuses\":["
                    + "{\"id\":\"1\",\"name\":\"To Do\"},{\"id\":\"3\",\"name\":\"Done\"},{\"id\":\"21\",\"name\":\"Testing\"}]}]";
            case "/rest/api/3/issuetype/project" -> "[{\"id\":\"10000\",\"name\":\"Epic\"},{\"id\":\"10001\",\"name\":\"Story\"},"
                    + "{\"id\":\"10002\",\"name\":\"Task\"},{\"id\":\"10003\",\"name\":\"Bug\"},{\"id\":\"10004\",\"name\":\"Sub-task\"}]";
            case "/rest/api/3/user/assignable/search" -> "[{\"accountId\":\"a1\",\"displayName\":\"Demo\",\"emailAddress\":\"demo@example.local\"},"
                    + "{\"accountId\":\"a2\",\"displayName\":\"Nadie\",\"emailAddress\":\"nadie@example.com\"}]";
            case "/rest/agile/1.0/board/11/issue" -> ISSUES_DESARROLLO;
            case "/rest/agile/1.0/board/12/issue" -> ISSUES_QA;
            case "/rest/agile/1.0/board/11/sprint" -> "{\"values\":[{\"id\":5,\"name\":\"Sprint simulado\",\"state\":\"active\","
                    + "\"startDate\":\"2026-11-01T10:00:00.000Z\",\"endDate\":\"2026-11-14T10:00:00.000Z\"}]}";
            case "/rest/agile/1.0/board/12/sprint" -> "{\"values\":[]}";
            case "/rest/api/3/project/JSIM/versions" -> "[{\"id\":\"700\",\"name\":\"0.9\",\"released\":true,\"releaseDate\":\"2026-10-30\"}]";
            case "/rest/api/3/issue/JSIM-2/comment" -> "{\"comments\":[{\"id\":\"9001\",\"author\":{\"emailAddress\":\"demo@example.local\","
                    + "\"displayName\":\"Demo\"},\"created\":\"2026-11-02T12:30:00.000-0300\",\"body\":" + adf("Empiezo por los sprints") + "}]}";
            default -> ruta.startsWith("/rest/api/3/issue/") && ruta.endsWith("/comment") ? "{\"comments\":[]}" : null;
        };
        if (cuerpo == null) {
            enviar(intercambio, 404, "{\"errorMessages\":[\"No existe " + ruta + "\"]}");
            return;
        }
        enviar(intercambio, 200, cuerpo);
    }

    private static String issue(String id, String clave, String titulo, String tipo, String estado, String prioridad,
            String padre, String responsable) {
        return "{\"id\":\"" + id + "\",\"key\":\"" + clave + "\",\"fields\":{\"summary\":\"" + titulo + "\","
                + "\"issuetype\":{\"id\":\"" + tipo + "\"},\"status\":{\"id\":\"" + estado + "\"},"
                + "\"priority\":{\"name\":\"" + prioridad + "\"},\"labels\":[\"importada\"],"
                + "\"description\":" + adf("Descripción de " + clave) + ","
                + (padre == null ? "" : "\"parent\":{\"key\":\"" + padre + "\"},")
                + (responsable == null ? "" : "\"assignee\":{\"emailAddress\":\"" + responsable + "\"},")
                + "\"fixVersions\":" + ("JSIM-2".equals(clave) ? "[{\"id\":\"700\"}]" : "[]") + ","
                + "\"sprint\":" + ("JSIM-2".equals(clave) || "JSIM-4".equals(clave) ? "{\"id\":5}" : "null") + "}}";
    }

    private static String adf(String texto) {
        return "{\"type\":\"doc\",\"version\":1,\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\""
                + texto + "\"}]}]}";
    }

    private static void enviar(HttpExchange intercambio, int status, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json");
        intercambio.sendResponseHeaders(status, bytes.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(bytes);
        }
    }
}
