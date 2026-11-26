package com.blue.open.Code.Common.Mcp;

import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Usuario.EscenarioDeUsuario;
import com.blue.open.Core.Test.TestConBaseDeDatos;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MJ-06 del plan de mejoras, version MCP: cada herramienta con un token de acceso de cada actor (sin reglas, VER,
 * EDITAR y ARCHIVAR sobre el proyecto MTZ, Administrador y super admin). El que no tiene el nivel recibe el error de
 * permiso; el que lo tiene, cualquier otra respuesta. Las busquedas no fallan: filtran. El token de un usuario
 * archivado no entra al MCP (401). Una herramienta nueva que no este en la matriz hace fallar
 * cadaHerramientaEstaEnLaMatriz.
 * Usa un puerto real: lo que crea queda guardado, por eso trabaja sobre su propio proyecto (MTZ).
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class McpMatrizDePermisosTest extends TestConBaseDeDatos {

    private static final String PREFIJO = "MTZ";

    // Paquete donde viven las herramientas MCP
    private static final String PAQUETE = "com.blue.open.Code";

    // Nivel de las herramientas que solo usan el super admin y los Administradores
    private static final String ADMINISTRACION = "ADMINISTRACION";

    // Nivel de las herramientas que solo exigen sesion
    private static final String SESION = "SESION";

    private static final ObjectMapper LECTOR = new ObjectMapper();

    // Sufijo unico para los nombres que crean las herramientas permitidas
    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    enum Actor { SIN_REGLA, VER, EDITAR, ARCHIVAR, ADMIN, SYSADMIN }

    // Tareas de MTZ de cada prueba (claves), creadas por el Administrador
    record Datos(String tarea, String otra, String archivada) {
    }

    // Fila de la matriz: herramienta, nivel que exige y argumentos minimos validos
    record Fila(String herramienta, String nivel, boolean busqueda, Function<Datos, Map<String, Object>> argumentos) {
    }

    private static final Map<String, Fila> FILAS = new TreeMap<>();

    static {
        fila("eco", SESION, false, d -> Map.of("texto", "hola"));
        fila("listar_proyectos", NivelDePermiso.VER.name(), true, d -> Map.of());
        fila("buscar_tareas", NivelDePermiso.VER.name(), true, d -> Map.of("texto", "Matriz MCP"));
        fila("ver_tablero", NivelDePermiso.VER.name(), false, d -> Map.of("proyecto", PREFIJO));
        fila("obtener_tarea", NivelDePermiso.VER.name(), false, d -> Map.of("clave", d.tarea()));
        fila("ver_historial", NivelDePermiso.VER.name(), false, d -> Map.of("clave", d.tarea()));
        fila("ver_backlog", NivelDePermiso.VER.name(), false, d -> Map.of("proyecto", PREFIJO));
        fila("gestionar_version:changelog", NivelDePermiso.VER.name(), false,
                d -> Map.of("accion", "changelog", "proyecto", PREFIJO, "version", "1.0.0"));
        fila("crear_tarea", NivelDePermiso.EDITAR.name(), false,
                d -> Map.of("proyecto", PREFIJO, "tipo", "tarea", "titulo", "Creada por la matriz MCP"));
        fila("editar_tarea", NivelDePermiso.EDITAR.name(), false, d -> Map.of("clave", d.tarea(), "titulo", "Editada por la matriz MCP"));
        fila("mover_tarea", NivelDePermiso.EDITAR.name(), false, d -> Map.of("clave", d.tarea(), "estado", "en curso"));
        fila("clonar_tarea", NivelDePermiso.EDITAR.name(), false, d -> Map.of("clave", d.tarea(), "tablero", "QA"));
        fila("comentar_tarea", NivelDePermiso.EDITAR.name(), false, d -> Map.of("clave", d.tarea(), "cuerpo", "Comentario de la matriz"));
        fila("relacionar_tareas", NivelDePermiso.EDITAR.name(), false,
                d -> Map.of("origen", d.tarea(), "tipo", "BLOQUEA", "destino", d.otra()));
        fila("ordenar_backlog", NivelDePermiso.EDITAR.name(), false, d -> Map.of("proyecto", PREFIJO, "tareas", List.of(d.otra(), d.tarea())));
        fila("gestionar_sprint:crear", NivelDePermiso.EDITAR.name(), false, d -> Map.of("accion", "crear", "proyecto", PREFIJO,
                "sprint", "Sprint matriz " + SECUENCIA.incrementAndGet(), "inicio", "2027-03-01", "fin", "2027-03-12"));
        fila("gestionar_version:crear", NivelDePermiso.EDITAR.name(), false, d -> Map.of("accion", "crear", "proyecto", PREFIJO,
                "version", "2.0." + SECUENCIA.incrementAndGet()));
        fila("buscar_archivados", NivelDePermiso.ARCHIVAR.name(), true, d -> Map.of("texto", "Matriz MCP"));
        fila("archivar", NivelDePermiso.ARCHIVAR.name(), false, d -> Map.of("tipo", "TAREA", "identificador", d.tarea()));
        fila("restaurar", NivelDePermiso.ARCHIVAR.name(), false, d -> Map.of("tipo", "TAREA", "identificador", d.archivada()));
        fila("administrar_proyecto", ADMINISTRACION, false, d -> Map.of("accion", "editar", "prefijo", PREFIJO,
                "nombre", "Proyecto de la matriz MCP"));
        fila("administrar_usuarios", ADMINISTRACION, false, d -> Map.of("accion", "crear",
                "nombreDeUsuario", "matriz.mcp." + SECUENCIA.incrementAndGet()));
        fila("administrar_grupos", ADMINISTRACION, false, d -> Map.of("accion", "crear", "nombre", "Grupo matriz " + SECUENCIA.incrementAndGet()));
        fila("administrar_permisos", ADMINISTRACION, false, d -> Map.of("accion", "crear", "sujeto", "grupo:Clientes",
                "recurso", PREFIJO, "nivel", "VER", "efecto", "PERMITIR"));
    }

    // Tokens de acceso de cada actor y token del usuario archivado: se crean una vez (la base no se revierte)
    private static Map<Actor, String> tokens;

    private static String tokenArchivado;

    @LocalServerPort
    private int puerto;

    private Datos datos;

    @BeforeEach
    void escenario() throws Exception {
        if (tokens == null)
            actores();
        try (McpSyncClient admin = ClienteMcpDePrueba.conectar(puerto, ClienteMcpDePrueba.TOKEN_DEMO)) {
            String tarea = crearTarea(admin, "Tarea de la Matriz MCP");
            String otra = crearTarea(admin, "Otra de la Matriz MCP");
            String archivada = crearTarea(admin, "Archivada de la Matriz MCP");
            exigirExito(admin, "archivar", Map.of("tipo", "TAREA", "identificador", archivada));
            datos = new Datos(tarea, otra, archivada);
        }
    }

    @Test
    void cadaHerramientaEstaEnLaMatriz() {
        Set<String> enLaMatriz = new TreeSet<>();
        FILAS.values().forEach(f -> enLaMatriz.add(f.herramienta()));
        Set<String> faltan = new TreeSet<>(herramientas());
        faltan.removeAll(enLaMatriz);
        assertTrue(faltan.isEmpty(), () -> "Herramientas MCP sin fila en la matriz de permisos (MJ-06): " + faltan);
    }

    @Test
    void elTokenDeUnUsuarioArchivadoNoEntra() throws Exception {
        assertEquals(401, ClienteMcpDePrueba.initializeCrudo(puerto, "Bearer " + tokenArchivado).statusCode());
    }

    // Filas de la matriz, para la prueba parametrizada
    static Stream<String> filas() {
        return FILAS.keySet().stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filas")
    void cadaActorRecibeLoQueDiceSuPermiso(String id) {
        Fila fila = FILAS.get(id);
        Set<Actor> permitidos = permitidos(fila.nivel());
        List<String> fallas = new ArrayList<>();
        for (Actor actor : enOrden(permitidos)) {
            try (McpSyncClient cliente = ClienteMcpDePrueba.conectar(puerto, tokens.get(actor))) {
                McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(cliente, fila.herramienta(),
                        new HashMap<>(fila.argumentos().apply(datos)));
                String falla = evaluar(permitidos.contains(actor), fila.busqueda(), resultado);
                if (falla != null)
                    fallas.add(actor + ": " + falla);
            }
        }
        assertTrue(fallas.isEmpty(), () -> id + " (" + fila.nivel() + ") no respeta su permiso:\n" + String.join("\n", fallas));
    }

    // Falla del resultado para el actor, o null si respeta el permiso
    private static String evaluar(boolean permitido, boolean busqueda, McpSchema.CallToolResult resultado) {
        String texto = ClienteMcpDePrueba.texto(resultado);
        boolean error = Boolean.TRUE.equals(resultado.isError());
        boolean sinPermiso = error && (texto.contains("No tenés permiso") || texto.contains("Solo el super admin"));
        if (busqueda) {
            if (error)
                return "la busqueda respondio error: " + recortar(texto);
            boolean conMtz = texto.contains(PREFIJO);
            if (permitido && !conMtz)
                return "la busqueda no trajo nada de " + PREFIJO + " con el permiso";
            if (!permitido && conMtz)
                return "la busqueda trajo datos de " + PREFIJO + " sin el permiso: " + recortar(texto);
            return null;
        }
        if (permitido)
            return sinPermiso ? "respondio sin permiso teniendolo: " + recortar(texto) : null;
        return sinPermiso ? null : "esperaba el error de permiso y respondio " + (error ? "error: " : "") + recortar(texto);
    }

    // Actores que pueden usar una herramienta del nivel: EDITAR y ARCHIVAR permitidos implican VER
    private static Set<Actor> permitidos(String nivel) {
        Set<Actor> permitidos = EnumSet.of(Actor.ADMIN, Actor.SYSADMIN);
        switch (nivel) {
            case SESION -> permitidos.addAll(EnumSet.allOf(Actor.class));
            case "VER" -> permitidos.addAll(List.of(Actor.VER, Actor.EDITAR, Actor.ARCHIVAR));
            case "EDITAR" -> permitidos.add(Actor.EDITAR);
            case "ARCHIVAR" -> permitidos.add(Actor.ARCHIVAR);
            default -> {
            }
        }
        return permitidos;
    }

    // Los que no pueden primero, para que una escritura permitida no cambie el estado antes de probar un rechazo
    private static List<Actor> enOrden(Set<Actor> permitidos) {
        List<Actor> orden = new ArrayList<>();
        Stream.of(Actor.values()).filter(a -> !permitidos.contains(a)).forEach(orden::add);
        orden.addAll(permitidos);
        return orden;
    }

    // Nombres de todas las herramientas @McpTool de los beans de Shiro
    private static Set<String> herramientas() {
        ClassPathScanningCandidateComponentProvider escaner = new ClassPathScanningCandidateComponentProvider(false);
        escaner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        Set<String> nombres = new TreeSet<>();
        escaner.findCandidateComponents(PAQUETE).forEach(definicion -> {
            try {
                for (Method metodo : Class.forName(definicion.getBeanClassName()).getDeclaredMethods()) {
                    McpTool herramienta = metodo.getAnnotation(McpTool.class);
                    if (herramienta != null)
                        nombres.add(herramienta.name());
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("No se pudo cargar " + definicion.getBeanClassName(), e);
            }
        });
        return nombres;
    }

    // Crea el proyecto MTZ, los usuarios con su regla sobre MTZ y un token de acceso para cada uno
    private void actores() throws Exception {
        JsonNode proyecto = pedir("POST", "/api/proyecto/guardar", ClienteMcpDePrueba.TOKEN_SYSADMIN,
                Map.of("nombre", "Proyecto de la matriz MCP", "prefijo", PREFIJO));
        String proyectoUuid = proyecto.get("uuid").asString();
        pedir("POST", "/api/version/guardar", ClienteMcpDePrueba.TOKEN_DEMO, Map.of("proyectoUuid", proyectoUuid, "nombre", "1.0.0"));
        Map<Actor, String> creados = new EnumMap<>(Actor.class);
        creados.put(Actor.ADMIN, ClienteMcpDePrueba.TOKEN_DEMO);
        creados.put(Actor.SYSADMIN, ClienteMcpDePrueba.TOKEN_SYSADMIN);
        usuario("matriz.mcp.sin.regla");
        creados.put(Actor.SIN_REGLA, token("matriz.mcp.sin.regla"));
        for (NivelDePermiso nivel : NivelDePermiso.values()) {
            String nombre = "matriz.mcp." + nivel.name().toLowerCase();
            regla(usuario(nombre), proyectoUuid, nivel);
            creados.put(Actor.valueOf(nivel.name()), token(nombre));
        }
        String archivado = usuario("matriz.mcp.archivado");
        regla(archivado, proyectoUuid, NivelDePermiso.ARCHIVAR);
        tokenArchivado = token("matriz.mcp.archivado");
        pedir("POST", "/api/usuario/" + archivado + "/archivar", ClienteMcpDePrueba.TOKEN_SYSADMIN, null);
        tokens = creados;
    }

    // Crea un usuario con la contrasena del escenario como super admin y responde su uuid
    private String usuario(String nombre) throws Exception {
        return pedir("POST", "/api/usuario/guardar", ClienteMcpDePrueba.TOKEN_SYSADMIN, EscenarioDeUsuario.alta(nombre))
                .get("uuid").asString();
    }

    // Crea una regla de usuario PERMITIR con el nivel sobre el proyecto
    private void regla(String usuarioUuid, String proyectoUuid, NivelDePermiso nivel) throws Exception {
        pedir("POST", "/api/regla-permiso/guardar", ClienteMcpDePrueba.TOKEN_DEMO, Map.of("tipoSujeto", "USUARIO",
                "usuarioUuid", usuarioUuid, "tipoRecurso", "PROYECTO", "proyectoUuid", proyectoUuid, "nivel", nivel.name(),
                "efecto", "PERMITIR"));
    }

    // Token de acceso del usuario, creado con su propia sesion por basic (POST /usuario/crear-token). En las pruebas el
    // codigo sale del nombre del token (AuthTokenServiceDePrueba): por eso el nombre lleva el del usuario
    private String token(String nombreDeUsuario) throws Exception {
        String basic = Base64.getEncoder().encodeToString((nombreDeUsuario + ":" + EscenarioDeUsuario.CONTRASENA)
                .getBytes(StandardCharsets.UTF_8));
        return pedirCon("POST", "/api/usuario/crear-token", "Basic " + basic, Map.of("nombre", "Matriz " + nombreDeUsuario))
                .get("codigo").asString();
    }

    // Pedido HTTP a la API con un token de acceso como bearer; exige 200 y responde el JSON
    private JsonNode pedir(String metodo, String ruta, String token, Object cuerpo) throws Exception {
        return pedirCon(metodo, ruta, "Bearer " + token, cuerpo);
    }

    // Pedido HTTP a la API con el header Authorization recibido; exige 200 y responde el JSON
    private JsonNode pedirCon(String metodo, String ruta, String authorization, Object cuerpo) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json")
                .header("Authorization", authorization)
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(LECTOR.writeValueAsString(cuerpo)))
                .build();
        HttpResponse<String> respuesta = HttpClient.newHttpClient().send(pedido, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respuesta.statusCode(), () -> metodo + " " + ruta + ": " + respuesta.body());
        return LECTOR.readTree(respuesta.body());
    }

    // Crea una tarea en MTZ por MCP y responde su clave
    private static String crearTarea(McpSyncClient admin, String titulo) {
        return exigirExito(admin, "crear_tarea", Map.of("proyecto", PREFIJO, "tipo", "tarea", "titulo", titulo))
                .get("datos").get("clave").asString();
    }

    // Llama la herramienta, exige que no sea un error y responde su JSON
    private static JsonNode exigirExito(McpSyncClient cliente, String herramienta, Map<String, Object> argumentos) {
        McpSchema.CallToolResult resultado = ClienteMcpDePrueba.llamar(cliente, herramienta, argumentos);
        assertTrue(!Boolean.TRUE.equals(resultado.isError()), () -> herramienta + ": " + resultado);
        return LECTOR.readTree(ClienteMcpDePrueba.texto(resultado));
    }

    // Registra una fila de la matriz; el id puede llevar la accion despues de ":" (gestionar_sprint:crear)
    private static void fila(String id, String nivel, boolean busqueda, Function<Datos, Map<String, Object>> argumentos) {
        FILAS.put(id, new Fila(id.contains(":") ? id.substring(0, id.indexOf(':')) : id, nivel, busqueda, argumentos));
    }

    // Texto recortado a 200 caracteres, para los mensajes de falla
    private static String recortar(String texto) {
        return texto.length() > 200 ? texto.substring(0, 200) : texto;
    }
}
