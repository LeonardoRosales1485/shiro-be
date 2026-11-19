package com.blue.open.Code.Autorizacion;

import com.blue.open.Code.Usuario.AuthJwtServiceDePrueba;
import com.blue.open.Code.Usuario.EscenarioDeUsuario;
import com.blue.open.Core.Test.Peticion;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T2.4 del plan: el resolutor por recurso aplicado a todos los endpoints de F1. Un usuario sin permiso recibe 403 en
 * cada uno y no ve nada en los listados; el grupo CLIENTE ve el proyecto Shiro pero no edita; una regla de usuario
 * sobre un tablero habilita solo ese tablero.
 */
class PermisosPorRecursoContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String PROYECTO_SHIRO = "b1000000-0000-4000-8000-000000000001";

    private static final String PROYECTO_ARCHIVADO = "b1000000-0000-4000-8000-000000000002";

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    private static final String TABLERO_QA = "b2000000-0000-4000-8000-000000000002";

    private static final String SHI_1 = "b3000000-0000-4000-8000-000000000001";

    private static final String COMENTARIO_DE_SEED = "b4000000-0000-4000-8000-000000000001";

    private static final String EN_CURSO = "a2000000-0000-4000-8000-000000000012";

    @Autowired
    private AuthJwtServiceDePrueba authJwtService;

    // JWT de un usuario nuevo, sin grupos ni reglas
    private String sinPermisos;

    @BeforeEach
    void usuarioSinPermisos() {
        sinPermisos = authJwtService.jwtFirmado(new EscenarioDeUsuario(this::como).crearUuid("sin-permisos"));
    }

    @Test
    void sinPermisoCadaEndpointDeF1Responde403() {
        String otra = tarea(TABLERO_PRINCIPAL, "Otra");
        String archivada = tarea(TABLERO_PRINCIPAL, "Archivada");
        como(ADMIN).post("/tarea/{uuid}/archivar", archivada).preparacion().ejecutar().esperar(200);
        String relacion = como(ADMIN).post("/relacion/crear").json(Map.of("origenUuid", SHI_1, "destinoUuid", otra,
                "tipo", "RELACIONA")).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
        String fecha = como(ADMIN).get("/tarea/uuid/{uuid}", SHI_1).preparacion().ejecutar().esperar(200).json()
                .get("fechaDeActualizacion").asString();

        List<Function<Peticion, Peticion>> llamadas = List.of(
                p -> p.get("/proyecto/uuid/{uuid}", PROYECTO_SHIRO),
                p -> p.post("/proyecto/{uuid}/archivar", PROYECTO_SHIRO),
                p -> p.post("/proyecto/{uuid}/restaurar", PROYECTO_ARCHIVADO),
                p -> p.get("/tarea/uuid/{uuid}", SHI_1),
                p -> p.post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL, "tipoCodigo", "TAREA",
                        "titulo", "Sin permiso")),
                p -> p.post("/tarea/guardar").json(Map.of("uuid", SHI_1, "tableroUuid", TABLERO_PRINCIPAL,
                        "tipoCodigo", "TAREA", "titulo", "Editada sin permiso", "fechaDeActualizacion", fecha)),
                p -> p.post("/tarea/{uuid}/mover", SHI_1).json(Map.of("estadoUuid", EN_CURSO)),
                p -> p.get("/tarea/{uuid}/historial", SHI_1),
                p -> p.get("/tarea/{uuid}/relaciones", SHI_1),
                p -> p.post("/tarea/{uuid}/archivar", SHI_1),
                p -> p.post("/tarea/{uuid}/restaurar", archivada),
                p -> p.post("/tarea/{uuid}/clonar", SHI_1).json(Map.of("tableroUuid", TABLERO_QA)),
                p -> p.get("/comentario/uuid/{uuid}", COMENTARIO_DE_SEED),
                p -> p.post("/comentario/guardar").json(Map.of("tareaUuid", SHI_1, "cuerpo", "Sin permiso")),
                p -> p.post("/relacion/crear").json(Map.of("origenUuid", SHI_1, "destinoUuid", otra, "tipo", "BLOQUEA")),
                p -> p.post("/relacion/{uuid}/archivar", relacion));
        for (Function<Peticion, Peticion> llamada : llamadas) {
            llamada.apply(con(sinPermisos)).ejecutar().esperarError(403, "SIN_PERMISO");
        }
    }

    @Test
    void sinPermisoLosListadosVienenVacios() {
        assertEquals(0, paginar(con(sinPermisos), "/proyecto/paginar").size());
        assertEquals(0, paginar(con(sinPermisos), "/tarea/paginar").size());
        assertEquals(0, paginar(con(sinPermisos), "/comentario/paginar").size());
    }

    @Test
    void elGrupoClienteVeElProyectoShiroPeroNoEdita() {
        como("CLIENTE").get("/tarea/uuid/{uuid}", SHI_1).ejecutar().esperar(200);
        JsonNode proyectos = paginar(como("CLIENTE"), "/proyecto/paginar");
        assertTrue(proyectos.toString().contains(PROYECTO_SHIRO), proyectos::toString);
        assertTrue(paginar(como("CLIENTE"), "/tarea/paginar").toString().contains(SHI_1));

        como("CLIENTE").post("/tarea/{uuid}/mover", SHI_1).json(Map.of("estadoUuid", EN_CURSO)).ejecutar()
                .esperarError(403, "SIN_PERMISO");
        como("CLIENTE").post("/comentario/guardar").json(Map.of("tareaUuid", SHI_1, "cuerpo", "Hola")).ejecutar()
                .esperarError(403, "SIN_PERMISO");
    }

    @Test
    void unaReglaDeUsuarioSobreUnTableroHabilitaSoloEseTablero() {
        String uuid = new EscenarioDeUsuario(this::como).crearUuid("editor-qa");
        como(ADMIN).post("/regla-permiso/guardar").json(Map.of("tipoSujeto", "USUARIO", "usuarioUuid", uuid,
                "tipoRecurso", "TABLERO", "tableroUuid", TABLERO_QA, "nivel", "EDITAR", "efecto", "PERMITIR"))
                .preparacion().ejecutar().esperar(200);
        String jwt = authJwtService.jwtFirmado(uuid);

        con(jwt).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_QA, "tipoCodigo", "BUG", "titulo", "En QA"))
                .ejecutar().esperar(200);
        con(jwt).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL, "tipoCodigo", "TAREA",
                "titulo", "En el principal")).ejecutar().esperarError(403, "SIN_PERMISO");
        assertTrue(paginar(con(jwt), "/proyecto/paginar").toString().contains(PROYECTO_SHIRO));
    }

    // Peticion sin actor, con el JWT recibido
    private Peticion con(String jwt) {
        return sinAutenticacion().encabezado("Authorization", "Bearer " + jwt);
    }

    // Contenido de la primera pagina de un paginado
    private static JsonNode paginar(Peticion peticion, String ruta) {
        Respuesta respuesta = peticion.post(ruta).json(Map.of("pagina", 1, "tamano", 50)).ejecutar();
        return respuesta.esperar(200).json().get("contenido");
    }

    // Crea una tarea como Administrador y responde su uuid
    private String tarea(String tablero, String titulo) {
        return como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", tablero, "tipoCodigo", "TAREA",
                "titulo", titulo)).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }
}
