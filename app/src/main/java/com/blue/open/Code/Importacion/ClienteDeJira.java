package com.blue.open.Code.Importacion;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente de la API REST de Jira Cloud (v3 y Agile 1.0) para el importador: lee el proyecto, sus tableros, estados,
 * tipos, usuarios asignables, issues por tablero (paginados), sprints, versiones y comentarios. Autentica con basic
 * (correo y token de API). Integracion externa: cada llamada va en try/catch; 401 o 403 responde
 * JIRA_CREDENCIALES_INVALIDAS y un error de red o un status inesperado, JIRA_NO_DISPONIBLE. No se comprueba al
 * iniciar porque la URL la elige el administrador en cada importacion.
 * Dependencias externas: el Jira de cada importacion.
 */
@Slf4j
@Component
public class ClienteDeJira {

    private static final int TAMANO_DE_PAGINA = 50;

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    // Jira devuelve las fechas como 2026-11-02T12:30:00.000-0300 (o con Z)
    private static final DateTimeFormatter FECHA_DE_JIRA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXX");

    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER).build();

    private final ObjectMapper lector = new ObjectMapper();

    // Proyecto por su clave. Lanza JIRA_PROYECTO_NO_ENCONTRADO si no existe o las credenciales no lo ven
    public DatosDeJira.Proyecto proyecto(CredencialesDeJira credenciales, String clave) {
        JsonNode proyecto = get(credenciales, "/rest/api/3/project/" + codificar(clave), true)
                .orElseThrow(() -> new ExcepcionDeApi(CodigoDeErrorDelProyecto.JIRA_PROYECTO_NO_ENCONTRADO,
                        "ClienteDeJira | proyecto | no existe " + clave));
        return new DatosDeJira.Proyecto(proyecto.path("id").asString(), proyecto.path("key").asString(),
                proyecto.path("name").asString());
    }

    // Tableros del proyecto (id y nombre), en el orden de Jira
    public List<ElementoDeJiraDTO> tableros(CredencialesDeJira credenciales, String clave) {
        List<ElementoDeJiraDTO> tableros = new ArrayList<>();
        for (JsonNode tablero : obligatorio(credenciales, "/rest/agile/1.0/board?projectKeyOrId=" + codificar(clave)).path("values"))
            tableros.add(new ElementoDeJiraDTO(tablero.path("id").asString(), tablero.path("name").asString()));
        return tableros;
    }

    // Estados del proyecto (de todos sus tipos de issue, sin repetir), en el orden de Jira
    public List<ElementoDeJiraDTO> estados(CredencialesDeJira credenciales, String clave) {
        Map<String, ElementoDeJiraDTO> estados = new LinkedHashMap<>();
        for (JsonNode tipo : obligatorio(credenciales, "/rest/api/3/project/" + codificar(clave) + "/statuses"))
            for (JsonNode estado : tipo.path("statuses"))
                estados.putIfAbsent(estado.path("id").asString(), new ElementoDeJiraDTO(estado.path("id").asString(),
                        estado.path("name").asString()));
        return new ArrayList<>(estados.values());
    }

    // Tipos de issue del proyecto
    public List<ElementoDeJiraDTO> tipos(CredencialesDeJira credenciales, String idProyecto) {
        List<ElementoDeJiraDTO> tipos = new ArrayList<>();
        for (JsonNode tipo : obligatorio(credenciales, "/rest/api/3/issuetype/project?projectId=" + codificar(idProyecto)))
            tipos.add(new ElementoDeJiraDTO(tipo.path("id").asString(), tipo.path("name").asString()));
        return tipos;
    }

    // Usuarios asignables del proyecto
    public List<DatosDeJira.Usuario> usuarios(CredencialesDeJira credenciales, String clave) {
        List<DatosDeJira.Usuario> usuarios = new ArrayList<>();
        for (JsonNode usuario : obligatorio(credenciales, "/rest/api/3/user/assignable/search?project=" + codificar(clave)
                + "&maxResults=1000"))
            usuarios.add(usuario(usuario));
        return usuarios;
    }

    // Issues del proyecto en un tablero, todas las paginas
    public List<DatosDeJira.Issue> issues(CredencialesDeJira credenciales, String idTablero, String clave) {
        List<DatosDeJira.Issue> issues = new ArrayList<>();
        int desde = 0;
        int total;
        do {
            JsonNode pagina = obligatorio(credenciales, "/rest/agile/1.0/board/" + codificar(idTablero) + "/issue?jql="
                    + codificar("project=" + clave) + "&startAt=" + desde + "&maxResults=" + TAMANO_DE_PAGINA);
            JsonNode lote = pagina.path("issues");
            lote.forEach(issue -> issues.add(issue(issue)));
            total = pagina.path("total").asInt(0);
            desde += lote.size();
            if (lote.isEmpty())
                break;
        } while (desde < total);
        return issues;
    }

    // Sprints de un tablero. Un tablero kanban no tiene sprints: Jira responde 400 y queda vacio
    public List<DatosDeJira.Sprint> sprints(CredencialesDeJira credenciales, String idTablero) {
        List<DatosDeJira.Sprint> sprints = new ArrayList<>();
        Optional<JsonNode> respuesta = get(credenciales, "/rest/agile/1.0/board/" + codificar(idTablero) + "/sprint?maxResults=50", true);
        respuesta.ifPresent(lista -> lista.path("values").forEach(sprint -> sprints.add(new DatosDeJira.Sprint(
                sprint.path("id").asString(), sprint.path("name").asString(), sprint.path("state").asString(),
                dia(sprint.path("startDate")), dia(sprint.path("endDate"))))));
        return sprints;
    }

    // Versiones del proyecto
    public List<DatosDeJira.Version> versiones(CredencialesDeJira credenciales, String clave) {
        List<DatosDeJira.Version> versiones = new ArrayList<>();
        for (JsonNode version : obligatorio(credenciales, "/rest/api/3/project/" + codificar(clave) + "/versions")) {
            String fecha = version.path("releaseDate").asString(null);
            versiones.add(new DatosDeJira.Version(version.path("id").asString(), version.path("name").asString(),
                    version.path("released").asBoolean(false), fecha == null ? null : LocalDate.parse(fecha)));
        }
        return versiones;
    }

    // Comentarios de un issue, todas las paginas
    public List<DatosDeJira.Comentario> comentarios(CredencialesDeJira credenciales, String claveIssue) {
        List<DatosDeJira.Comentario> comentarios = new ArrayList<>();
        int desde = 0;
        int total;
        do {
            JsonNode pagina = obligatorio(credenciales, "/rest/api/3/issue/" + codificar(claveIssue) + "/comment?startAt="
                    + desde + "&maxResults=" + TAMANO_DE_PAGINA);
            JsonNode lote = pagina.path("comments");
            for (JsonNode comentario : lote)
                comentarios.add(new DatosDeJira.Comentario(comentario.path("id").asString(), usuario(comentario.path("author")),
                        OffsetDateTime.parse(comentario.path("created").asString(), FECHA_DE_JIRA),
                        TextoDeAdf.convertir(comentario.path("body"))));
            total = pagina.path("total").asInt(lote.size());
            desde += lote.size();
            if (lote.isEmpty())
                break;
        } while (desde < total);
        return comentarios;
    }

    // Issue del JSON de Jira
    private static DatosDeJira.Issue issue(JsonNode issue) {
        JsonNode campos = issue.path("fields");
        List<String> etiquetas = new ArrayList<>();
        campos.path("labels").forEach(etiqueta -> etiquetas.add(etiqueta.asString()));
        List<String> versiones = new ArrayList<>();
        campos.path("fixVersions").forEach(version -> versiones.add(version.path("id").asString()));
        JsonNode responsable = campos.path("assignee");
        return new DatosDeJira.Issue(issue.path("id").asString(), issue.path("key").asString(),
                campos.path("summary").asString(), TextoDeAdf.convertir(campos.path("description")),
                campos.path("issuetype").path("id").asString(), campos.path("status").path("id").asString(),
                campos.path("priority").path("name").asString(null), campos.path("parent").path("key").asString(null),
                responsable.isObject() ? usuario(responsable) : null, etiquetas,
                campos.path("sprint").path("id").asString(null), versiones);
    }

    // Usuario del JSON de Jira
    private static DatosDeJira.Usuario usuario(JsonNode usuario) {
        return new DatosDeJira.Usuario(usuario.path("emailAddress").asString(null), usuario.path("displayName").asString(null));
    }

    // Dia (en GMT-3) de una fecha de Jira, o null
    private static LocalDate dia(JsonNode fecha) {
        String texto = fecha.asString(null);
        return texto == null ? null : OffsetDateTime.parse(texto, FECHA_DE_JIRA).atZoneSameInstant(ZONA).toLocalDate();
    }

    // GET que tiene que responder: un 404 o 400 tambien es JIRA_NO_DISPONIBLE
    private JsonNode obligatorio(CredencialesDeJira credenciales, String ruta) {
        return get(credenciales, ruta, false).orElseThrow();
    }

    /*
     * GET a la API de Jira. Responde el JSON, o vacio si toleraAusencia y Jira responde 404 o 400. Lanza
     * JIRA_CREDENCIALES_INVALIDAS ante 401 o 403 y JIRA_NO_DISPONIBLE ante un error de red, un status inesperado o
     * una respuesta que no es JSON.
     */
    private Optional<JsonNode> get(CredencialesDeJira credenciales, String ruta, boolean toleraAusencia) {
        HttpResponse<String> respuesta;
        try {
            String basic = Base64.getEncoder().encodeToString((credenciales.correo() + ":" + credenciales.token())
                    .getBytes(StandardCharsets.UTF_8));
            HttpRequest pedido = HttpRequest.newBuilder(URI.create(credenciales.url() + ruta)).timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Basic " + basic).header("Accept", "application/json").GET().build();
            respuesta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw noDisponible(credenciales, ruta, e);
        } catch (IOException | IllegalArgumentException e) {
            throw noDisponible(credenciales, ruta, e);
        }
        int status = respuesta.statusCode();
        if (status == 401 || status == 403)
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.JIRA_CREDENCIALES_INVALIDAS, "ClienteDeJira | get | status " + status);
        if (toleraAusencia && (status == 404 || status == 400))
            return Optional.empty();
        if (status != 200) {
            log.error("Jira respondio un status inesperado | url={} | ruta={} | status={}", credenciales.url(), ruta, status);
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.JIRA_NO_DISPONIBLE, "ClienteDeJira | get | status " + status);
        }
        try {
            return Optional.of(lector.readTree(respuesta.body()));
        } catch (JacksonException e) {
            throw noDisponible(credenciales, ruta, e);
        }
    }

    // Loguea la falla de la integracion y la traduce a JIRA_NO_DISPONIBLE
    private static ExcepcionDeApi noDisponible(CredencialesDeJira credenciales, String ruta, Exception e) {
        log.error("Jira no responde | url={} | ruta={}", credenciales.url(), ruta, e);
        return new ExcepcionDeApi(CodigoDeErrorDelProyecto.JIRA_NO_DISPONIBLE, "ClienteDeJira | get | sin respuesta de Jira");
    }

    // Valor codificado para una URL
    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
