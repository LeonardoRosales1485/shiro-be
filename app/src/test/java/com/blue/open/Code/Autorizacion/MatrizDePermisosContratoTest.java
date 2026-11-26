package com.blue.open.Code.Autorizacion;

import com.blue.open.CatalogoDePermisos;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import com.blue.open.Code.Usuario.AuthJwtServiceDePrueba;
import com.blue.open.Code.Usuario.EscenarioDeUsuario;
import com.blue.open.Core.Test.Peticion;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MJ-06 del plan de mejoras: matriz de permisos. Cada endpoint anotado con @PermisoDeRecurso se llama con cada actor
 * (sin reglas, VER, EDITAR y ARCHIVAR sobre el proyecto Shiro, Administrador, super admin y un usuario archivado) y se
 * espera lo que dice su anotacion: 403 SIN_PERMISO para el que no tiene el nivel, cualquier respuesta que no sea
 * 401/403/5xx para el que lo tiene, 401 para el usuario archivado. Los listados no responden 403: filtran, asi que
 * el que no tiene el nivel recibe la pagina vacia y el que lo tiene, con contenido.
 * Un endpoint nuevo con @PermisoDeRecurso que no este en la matriz hace fallar cadaEndpointConPermisoEstaEnLaMatriz.
 * Todas las llamadas son pasos de preparacion: no entran como ejemplos al contrato (docs/api).
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
class MatrizDePermisosContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String SYSADMIN = "SYSADMIN";

    private static final String PROYECTO_SHIRO = "b1000000-0000-4000-8000-000000000001";

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    private static final String TABLERO_QA = "b2000000-0000-4000-8000-000000000002";

    private static final String SHI_1 = "b3000000-0000-4000-8000-000000000001";

    private static final String COMENTARIO_DE_SEED = "b4000000-0000-4000-8000-000000000001";

    private static final String SPRINT_DE_SEED = "b6000000-0000-4000-8000-000000000001";

    private static final String VERSION_DE_SEED = "b7000000-0000-4000-8000-000000000001";

    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    private static final Map<String, Object> PRIMERA_PAGINA = Map.of("pagina", 1, "tamano", 50);

    // Perfiles de la matriz
    enum Actor { ARCHIVADO, SIN_REGLA, VER, EDITAR, ARCHIVAR, ADMIN, SYSADMIN }

    // Registros del escenario de cada prueba
    record Datos(String tarea, String archivada, String relacion, String sprint, String sprintArchivado, String version,
                 String versionArchivada) {
    }

    // Llamada de cada operacion (id Controller.metodo, como core-test), con un cuerpo minimo valido
    private static final Map<String, Function<Datos, Function<Peticion, Peticion>>> LLAMADAS = new TreeMap<>();

    // Operaciones cuyo nivel efectivo es mayor que el de su anotacion, porque el cuerpo minimo toca otro recurso
    private static final Map<String, NivelDePermiso> NIVEL_EFECTIVO = Map.of(
            // clonar al mismo tablero exige EDITAR en el destino
            "TareaController.clonar", NivelDePermiso.EDITAR);

    static {
        operacion("ArchivoController.paginar", d -> p -> p.post("/archivo/paginar").json(PRIMERA_PAGINA));
        operacion("ArchivoController.vistaPrevia", d -> p -> p.get("/archivo/{tipo}/{uuid}/vista-previa", "TAREA", d.archivada()));
        operacion("ComentarioController.obtenerPorUuid", d -> p -> p.get("/comentario/uuid/{uuid}", COMENTARIO_DE_SEED));
        operacion("ComentarioController.paginar", d -> p -> p.post("/comentario/paginar").json(PRIMERA_PAGINA));
        operacion("ComentarioController.guardar", d -> p -> p.post("/comentario/guardar")
                .json(Map.of("tareaUuid", d.tarea(), "cuerpo", "Comentario de la matriz")));
        operacion("ProyectoController.obtenerPorUuid", d -> p -> p.get("/proyecto/uuid/{uuid}", PROYECTO_SHIRO));
        operacion("ProyectoController.paginar", d -> p -> p.post("/proyecto/paginar").json(PRIMERA_PAGINA));
        operacion("ProyectoController.archivar", d -> p -> p.post("/proyecto/{uuid}/archivar", PROYECTO_SHIRO));
        operacion("ProyectoController.restaurar", d -> p -> p.post("/proyecto/{uuid}/restaurar", PROYECTO_SHIRO));
        operacion("ProyectoController.backlog", d -> p -> p.get("/proyecto/{uuid}/backlog", PROYECTO_SHIRO));
        operacion("ProyectoController.ordenarBacklog", d -> p -> p.post("/proyecto/{uuid}/ordenar-backlog", PROYECTO_SHIRO)
                .json(Map.of("tareas", List.of(d.tarea()))));
        operacion("ProyectoController.usuarios", d -> p -> p.get("/proyecto/{uuid}/usuarios", PROYECTO_SHIRO));
        operacion("RelacionController.crear", d -> p -> p.post("/relacion/crear")
                .json(Map.of("origenUuid", SHI_1, "destinoUuid", d.tarea(), "tipo", "BLOQUEA")));
        operacion("RelacionController.archivar", d -> p -> p.post("/relacion/{uuid}/archivar", d.relacion()));
        operacion("SprintController.obtenerPorUuid", d -> p -> p.get("/sprint/uuid/{uuid}", d.sprint()));
        operacion("SprintController.paginar", d -> p -> p.post("/sprint/paginar").json(PRIMERA_PAGINA));
        operacion("SprintController.guardar", d -> p -> p.post("/sprint/guardar").json(Map.of("proyectoUuid", PROYECTO_SHIRO,
                "nombre", "Sprint de la matriz", "inicio", "2027-01-04", "fin", "2027-01-15")));
        operacion("SprintController.asignar", d -> p -> p.post("/sprint/{uuid}/asignar", d.sprint())
                .json(Map.of("tareas", List.of(d.tarea()))));
        operacion("SprintController.quitar", d -> p -> p.post("/sprint/{uuid}/quitar", d.sprint())
                .json(Map.of("tareas", List.of(d.tarea()))));
        operacion("SprintController.iniciar", d -> p -> p.post("/sprint/{uuid}/iniciar", d.sprint()));
        operacion("SprintController.cerrar", d -> p -> p.post("/sprint/{uuid}/cerrar", SPRINT_DE_SEED)
                .json(Map.of("destino", "BACKLOG")));
        operacion("SprintController.archivar", d -> p -> p.post("/sprint/{uuid}/archivar", d.sprint()));
        operacion("SprintController.restaurar", d -> p -> p.post("/sprint/{uuid}/restaurar", d.sprintArchivado()));
        operacion("SprintController.cierre", d -> p -> p.get("/sprint/{uuid}/cierre", SPRINT_DE_SEED));
        operacion("TareaController.obtenerPorUuid", d -> p -> p.get("/tarea/uuid/{uuid}", SHI_1));
        operacion("TareaController.paginar", d -> p -> p.post("/tarea/paginar").json(PRIMERA_PAGINA));
        operacion("TareaController.guardar", d -> p -> p.post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL,
                "tipoCodigo", "TAREA", "titulo", "Tarea de la matriz")));
        operacion("TareaController.mover", d -> p -> p.post("/tarea/{uuid}/mover", d.tarea()).json(Map.of("estadoUuid", EN_CURSO)));
        operacion("TareaController.historial", d -> p -> p.get("/tarea/{uuid}/historial", SHI_1));
        operacion("TareaController.relaciones", d -> p -> p.get("/tarea/{uuid}/relaciones", SHI_1));
        operacion("TareaController.archivar", d -> p -> p.post("/tarea/{uuid}/archivar", d.tarea()));
        operacion("TareaController.restaurar", d -> p -> p.post("/tarea/{uuid}/restaurar", d.archivada()));
        operacion("TareaController.clonar", d -> p -> p.post("/tarea/{uuid}/clonar", d.tarea())
                .json(Map.of("tableroUuid", TABLERO_PRINCIPAL)));
        operacion("VersionController.obtenerPorUuid", d -> p -> p.get("/version/uuid/{uuid}", d.version()));
        operacion("VersionController.paginar", d -> p -> p.post("/version/paginar").json(PRIMERA_PAGINA));
        operacion("VersionController.guardar", d -> p -> p.post("/version/guardar")
                .json(Map.of("proyectoUuid", PROYECTO_SHIRO, "nombre", "9.9.8")));
        operacion("VersionController.asignar", d -> p -> p.post("/version/{uuid}/asignar", d.version())
                .json(Map.of("tareas", List.of(d.tarea()))));
        operacion("VersionController.quitar", d -> p -> p.post("/version/{uuid}/quitar", d.version())
                .json(Map.of("tareas", List.of(d.tarea()))));
        operacion("VersionController.publicar", d -> p -> p.post("/version/{uuid}/publicar", d.version()).json(Map.of()));
        operacion("VersionController.changelog", d -> p -> p.get("/version/{uuid}/changelog", VERSION_DE_SEED));
        operacion("VersionController.archivar", d -> p -> p.post("/version/{uuid}/archivar", d.version()));
        operacion("VersionController.restaurar", d -> p -> p.post("/version/{uuid}/restaurar", d.versionArchivada()));
        operacion("VersionController.tareas", d -> p -> p.get("/version/{uuid}/tareas", VERSION_DE_SEED));
    }

    @Autowired
    private AuthJwtServiceDePrueba authJwtService;

    // JWT de cada actor que no es un usuario de los seeds
    private final Map<Actor, String> jwts = new EnumMap<>(Actor.class);

    private Datos datos;

    @BeforeEach
    void escenario() {
        EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);
        jwts.put(Actor.SIN_REGLA, authJwtService.jwtFirmado(usuarios.crearUuid("matriz-sin-regla")));
        jwts.put(Actor.VER, conRegla(usuarios, "matriz-ver", NivelDePermiso.VER));
        jwts.put(Actor.EDITAR, conRegla(usuarios, "matriz-editar", NivelDePermiso.EDITAR));
        jwts.put(Actor.ARCHIVAR, conRegla(usuarios, "matriz-archivar", NivelDePermiso.ARCHIVAR));
        // El archivado tenia ARCHIVAR: su JWT, firmado antes de archivarlo, ya no debe alcanzar para nada
        String archivado = usuarios.crearUuid("matriz-archivado");
        regla(archivado, NivelDePermiso.ARCHIVAR);
        jwts.put(Actor.ARCHIVADO, authJwtService.jwtFirmado(archivado));
        usuarios.eliminar(archivado);

        String tarea = tarea("Tarea de la matriz");
        String archivada = tarea("Archivada de la matriz");
        preparar(como(ADMIN).post("/tarea/{uuid}/archivar", archivada));
        String relacion = preparar(como(ADMIN).post("/relacion/crear").json(Map.of("origenUuid", SHI_1,
                "destinoUuid", tarea, "tipo", "RELACIONA"))).get("uuid").asString();
        String sprintArchivado = sprint("Sprint archivado de la matriz");
        preparar(como(ADMIN).post("/sprint/{uuid}/archivar", sprintArchivado));
        String versionArchivada = version("9.9.7");
        preparar(como(ADMIN).post("/version/{uuid}/archivar", versionArchivada));
        datos = new Datos(tarea, archivada, relacion, sprint("Sprint planificado de la matriz"), sprintArchivado,
                version("9.9.9"), versionArchivada);
    }

    @Test
    void cadaEndpointConPermisoEstaEnLaMatriz() {
        Set<String> anotados = new TreeSet<>(anotados().keySet());
        Set<String> faltan = new TreeSet<>(anotados);
        faltan.removeAll(LLAMADAS.keySet());
        Set<String> sobran = new TreeSet<>(LLAMADAS.keySet());
        sobran.removeAll(anotados);
        assertTrue(faltan.isEmpty(), () -> "Endpoints con @PermisoDeRecurso sin fila en la matriz (MJ-06): " + faltan);
        assertTrue(sobran.isEmpty(), () -> "Filas de la matriz sin endpoint con @PermisoDeRecurso: " + sobran);
    }

    // Ids de las operaciones de la matriz, para la prueba parametrizada
    static Stream<String> operaciones() {
        return LLAMADAS.keySet().stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operaciones")
    void cadaActorRecibeLoQueDiceSuPermiso(String operacion) {
        NivelDePermiso nivel = NIVEL_EFECTIVO.getOrDefault(operacion, anotados().get(operacion).nivel());
        Set<Actor> permitidos = permitidos(nivel);
        boolean listado = operacion.endsWith(".paginar");
        List<String> fallas = new ArrayList<>();
        // Restaurar el proyecto necesita un proyecto archivado sobre el que los actores tengan sus reglas
        if (operacion.equals("ProyectoController.restaurar"))
            preparar(como(ADMIN).post("/proyecto/{uuid}/archivar", PROYECTO_SHIRO));
        for (Actor actor : enOrden(permitidos)) {
            Respuesta respuesta = LLAMADAS.get(operacion).apply(datos).apply(peticion(actor)).preparacion().ejecutar();
            String falla = evaluar(actor, permitidos.contains(actor), listado, respuesta);
            if (falla != null)
                fallas.add(actor + ": " + falla);
        }
        assertTrue(fallas.isEmpty(), () -> operacion + " (" + nivel + ") no respeta su permiso:\n" + String.join("\n", fallas));
    }

    @Test
    void negarEnUnTableroGanaAPermitirEnSuProyectoSinNegarVer() {
        // Precedencia de la Spec 2 de punta a punta: la regla del tablero decide antes que la del proyecto, y negar
        // EDITAR no niega VER (los casos completos estan en EvaluadorDeReglasDePermisoTest)
        EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);
        String uuid = usuarios.crearUuid("matriz-precedencia");
        regla(uuid, NivelDePermiso.EDITAR);
        preparar(como(ADMIN).post("/regla-permiso/guardar").json(Map.of("tipoSujeto", "USUARIO", "usuarioUuid", uuid,
                "tipoRecurso", "TABLERO", "tableroUuid", TABLERO_QA, "nivel", "EDITAR", "efecto", "NEGAR")));
        String enQa = preparar(como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_QA,
                "tipoCodigo", "BUG", "titulo", "Bug en QA"))).get("uuid").asString();
        String jwt = authJwtService.jwtFirmado(uuid);

        conJwt(jwt).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL, "tipoCodigo", "TAREA",
                "titulo", "En el principal")).preparacion().ejecutar().esperar(200);
        conJwt(jwt).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_QA, "tipoCodigo", "BUG", "titulo", "En QA"))
                .preparacion().ejecutar().esperarError(403, "SIN_PERMISO");
        conJwt(jwt).get("/tarea/uuid/{uuid}", enQa).preparacion().ejecutar().esperar(200);
    }

    @Test
    void sinElChequeoUnEndpointHaceFallarLaMatriz() {
        // Control de la propia matriz: si un endpoint de EDITAR dejara pasar a un usuario de solo VER, la evaluacion
        // lo informa. Se simula con la respuesta 200 que recibiria ese usuario
        Respuesta respuesta = peticion(Actor.ADMIN).get("/tarea/uuid/{uuid}", SHI_1).preparacion().ejecutar();
        assertEquals(200, respuesta.status());
        String falla = evaluar(Actor.VER, false, false, respuesta);
        assertTrue(falla != null && falla.contains("esperaba 403"), () -> String.valueOf(falla));
    }

    // Falla de la respuesta para el actor, o null si respeta el permiso
    private static String evaluar(Actor actor, boolean permitido, boolean listado, Respuesta respuesta) {
        int status = respuesta.status();
        if (actor == Actor.ARCHIVADO)
            return status == 401 ? null : "esperaba 401 y respondio " + status + " " + cuerpo(respuesta);
        if (status >= 500)
            return "respondio " + status + " " + cuerpo(respuesta);
        if (listado) {
            if (status != 200)
                return "el listado respondio " + status + " " + cuerpo(respuesta);
            int filas = respuesta.json().get("contenido").size();
            if (permitido && filas == 0)
                return "el listado vino vacio con el permiso";
            if (!permitido && filas > 0)
                return "el listado trajo " + filas + " filas sin el permiso";
            return null;
        }
        if (permitido)
            return status == 401 || status == 403 ? "respondio " + status + " con el permiso " + cuerpo(respuesta) : null;
        if (status != 403 || !"SIN_PERMISO".equals(codigo(respuesta)))
            return "esperaba 403 SIN_PERMISO y respondio " + status + " " + cuerpo(respuesta);
        return null;
    }

    // Actores en el orden en que se prueban: el archivado, los que no tienen el nivel y despues los que lo tienen, para
    // que una escritura permitida no cambie el estado antes de que se pruebe un rechazo
    private static List<Actor> enOrden(Set<Actor> permitidos) {
        List<Actor> orden = new ArrayList<>(List.of(Actor.ARCHIVADO));
        Stream.of(Actor.values()).filter(a -> a != Actor.ARCHIVADO && !permitidos.contains(a)).forEach(orden::add);
        orden.addAll(permitidos);
        return orden;
    }

    // Actores que tienen el nivel sobre Shiro: EDITAR y ARCHIVAR permitidos implican VER (EvaluadorDeReglasDePermiso)
    private static Set<Actor> permitidos(NivelDePermiso nivel) {
        Set<Actor> permitidos = EnumSet.of(Actor.ADMIN, Actor.SYSADMIN);
        switch (nivel) {
            case VER -> permitidos.addAll(List.of(Actor.VER, Actor.EDITAR, Actor.ARCHIVAR));
            case EDITAR -> permitidos.add(Actor.EDITAR);
            case ARCHIVAR -> permitidos.add(Actor.ARCHIVAR);
        }
        return permitidos;
    }

    // Metodos con @PermisoDeRecurso, por id Controller.metodo
    private static Map<String, PermisoDeRecurso> anotados() {
        Map<String, PermisoDeRecurso> anotados = new TreeMap<>();
        for (Class<?> controller : CatalogoDePermisos.controllers()) {
            for (Method metodo : controller.getDeclaredMethods()) {
                PermisoDeRecurso permiso = metodo.getAnnotation(PermisoDeRecurso.class);
                if (permiso != null)
                    anotados.put(controller.getSimpleName() + "." + metodo.getName(), permiso);
            }
        }
        return anotados;
    }

    private static void operacion(String id, Function<Datos, Function<Peticion, Peticion>> llamada) {
        LLAMADAS.put(id, llamada);
    }

    // Peticion del actor: los de los seeds con su token, el resto con su JWT
    private Peticion peticion(Actor actor) {
        return switch (actor) {
            case ADMIN -> como(ADMIN);
            case SYSADMIN -> como(SYSADMIN);
            default -> conJwt(jwts.get(actor));
        };
    }

    // Peticion sin actor, con el JWT recibido
    private Peticion conJwt(String jwt) {
        return sinAutenticacion().encabezado("Authorization", "Bearer " + jwt);
    }

    // Crea un usuario con una regla de usuario sobre el proyecto Shiro y responde su JWT
    private String conRegla(EscenarioDeUsuario usuarios, String nombre, NivelDePermiso nivel) {
        String uuid = usuarios.crearUuid(nombre);
        regla(uuid, nivel);
        return authJwtService.jwtFirmado(uuid);
    }

    private void regla(String usuarioUuid, NivelDePermiso nivel) {
        preparar(como(ADMIN).post("/regla-permiso/guardar").json(Map.of("tipoSujeto", "USUARIO", "usuarioUuid", usuarioUuid,
                "tipoRecurso", "PROYECTO", "proyectoUuid", PROYECTO_SHIRO, "nivel", nivel.name(), "efecto", "PERMITIR")));
    }

    private String tarea(String titulo) {
        return preparar(como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL,
                "tipoCodigo", "TAREA", "titulo", titulo))).get("uuid").asString();
    }

    private String sprint(String nombre) {
        return preparar(como(ADMIN).post("/sprint/guardar").json(Map.of("proyectoUuid", PROYECTO_SHIRO, "nombre", nombre,
                "inicio", "2027-02-01", "fin", "2027-02-12"))).get("uuid").asString();
    }

    private String version(String nombre) {
        return preparar(como(ADMIN).post("/version/guardar").json(Map.of("proyectoUuid", PROYECTO_SHIRO,
                "nombre", nombre))).get("uuid").asString();
    }

    private static JsonNode preparar(Peticion peticion) {
        return peticion.preparacion().ejecutar().esperar(200).json();
    }

    private static String codigo(Respuesta respuesta) {
        JsonNode codigo = respuesta.status() >= 400 ? respuesta.json().get("codigo") : null;
        return codigo == null ? null : codigo.asString();
    }

    private static String cuerpo(Respuesta respuesta) {
        String texto = respuesta.texto();
        return texto.length() > 200 ? texto.substring(0, 200) : texto;
    }
}
