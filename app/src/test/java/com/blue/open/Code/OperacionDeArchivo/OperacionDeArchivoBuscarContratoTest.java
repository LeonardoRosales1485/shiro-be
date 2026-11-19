package com.blue.open.Code.OperacionDeArchivo;

import com.blue.open.Code.Usuario.AuthJwtServiceDePrueba;
import com.blue.open.Code.Usuario.EscenarioDeUsuario;
import com.blue.open.Core.Test.Peticion;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-ARC-01: busqueda de lo archivado (POST /archivo/paginar). T5.4 del plan y Spec 3: busca lo archivado de
 * cualquier tipo (tareas, proyectos, sprints, versiones y usuarios) segun permisos: cada elemento aparece si el
 * usuario tiene ARCHIVAR sobre su recurso (los usuarios, solo para administradores). Filtra por tipo y texto.
 */
class ArchivoBuscarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    private static final String PROYECTO_SHIRO = "b1000000-0000-4000-8000-000000000001";

    @Autowired
    private AuthJwtServiceDePrueba authJwtService;

    @Test
    void elAdministradorVeTodoLoArchivadoYFiltraPorTipoYTexto() {
        String tarea = tarea("Tarea archivada para buscar");
        como(ADMIN).post("/tarea/{uuid}/archivar", tarea).preparacion().ejecutar().esperar(200);
        String version = como(ADMIN).post("/version/guardar").json(Map.of("proyectoUuid", PROYECTO_SHIRO,
                "nombre", "Versión archivada")).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
        como(ADMIN).post("/version/{uuid}/archivar", version).preparacion().ejecutar().esperar(200);

        String todo = buscar(como(ADMIN), null, null).toString();
        assertTrue(todo.contains(tarea) && todo.contains(version), todo);
        assertFalse(buscar(como(ADMIN), "TAREA", null).toString().contains(version));
        assertTrue(buscar(como(ADMIN), null, "para buscar").toString().contains(tarea));
        assertTrue(buscar(como(ADMIN), "PROYECTO", null).toString().contains("\"ARC\""));
    }

    @Test
    void soloAparecenLosElementosConArchivarSobreSuRecurso() {
        String tarea = tarea("Archivada en Shiro");
        como(ADMIN).post("/tarea/{uuid}/archivar", tarea).preparacion().ejecutar().esperar(200);
        String uuid = new EscenarioDeUsuario(this::como).crearUuid("archivista");
        String jwt = authJwtService.jwtFirmado(uuid);

        assertFalse(buscar(como("CLIENTE"), null, null).toString().contains(tarea));
        assertFalse(buscar(con(jwt), null, null).toString().contains(tarea));

        como(ADMIN).post("/regla-permiso/guardar").json(Map.of("tipoSujeto", "USUARIO", "usuarioUuid", uuid,
                "tipoRecurso", "PROYECTO", "proyectoUuid", PROYECTO_SHIRO, "nivel", "ARCHIVAR", "efecto", "PERMITIR"))
                .preparacion().ejecutar().esperar(200);
        String visto = buscar(con(jwt), null, null).toString();
        assertTrue(visto.contains(tarea), visto);
        assertFalse(buscar(con(jwt), "USUARIO", null).toString().contains("\"tipo\":\"USUARIO\""));
    }

    // POST /archivo/paginar con el tipo y el texto si no son null; responde el contenido
    private static JsonNode buscar(Peticion peticion, String tipo, String texto) {
        Map<String, Object> pedido = new HashMap<>(Map.of("pagina", 1, "tamano", 200));
        if (tipo != null)
            pedido.put("tipo", tipo);
        if (texto != null)
            pedido.put("texto", texto);
        return peticion.post("/archivo/paginar").json(pedido).ejecutar().esperar(200).json().get("contenido");
    }

    private Peticion con(String jwt) {
        return sinAutenticacion().encabezado("Authorization", "Bearer " + jwt);
    }

    private String tarea(String titulo) {
        return como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL, "tipoCodigo", "TAREA",
                "titulo", titulo)).preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }
}
