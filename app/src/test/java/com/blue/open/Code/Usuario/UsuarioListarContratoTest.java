package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-02: listado paginado de usuarios (POST /usuario/paginar). Migrado de bruno/User (02 Paginate,
 * Paginate - campo no permitido y Paginate - operacion invalida). El caso feliz suma el filtro "email is not null":
 * un usuario sin email no cumpliria el schema de UsuarioDTO (email obligatorio).
 */
class UsuarioListarContratoTest extends PruebaDeContrato {

    @Test
    void listaLosUsuariosActivos() {
        Map<String, Object> cuerpo = Map.of("pagina", 1, "tamano", 10,
                "filtros", List.of(List.of(Map.of("campo", "status", "operacion", "=", "valor", "0")),
                        List.of(Map.of("campo", "email", "operacion", "is not null", "valor", ""))),
                "ordenes", List.of(Map.of("campo", "id", "direccion", "desc")),
                "parametros", List.of());

        JsonNode pagina = paginar(cuerpo).esperar(200).json();

        assertTrue(pagina.has("totalDePaginas"));
        assertFalse(pagina.has("pageable"));
        assertFalse(pagina.get("contenido").isEmpty());
        for (JsonNode usuario : pagina.get("contenido")) {
            assertTrue(usuario.hasNonNull("uuid"));
            assertTrue(usuario.hasNonNull("nombreDeUsuario"));
            assertTrue(usuario.hasNonNull("email"));
            assertEquals(0, usuario.get("status").asInt());
            assertFalse(usuario.hasNonNull("contrasena"));
        }
    }

    @Test
    void listaLasEmpresasYRolesDeCadaUsuario() {
        Map<String, Object> cuerpo = Map.of("pagina", 1, "tamano", 10, "filtros", List.of(List.of(
                Map.of("campo", "uuid", "operacion", "=", "valor", UsuariosDeSeed.DEMO))));

        JsonNode pagina = paginar(cuerpo).esperar(200).json();

        assertEquals(1, pagina.get("totalDeElementos").asInt());
        UsuariosDeSeed.verificarEmpresasYRolesDelDemo(pagina.get("contenido").get(0));
    }

    @Test
    void campoNoPermitido() {
        Map<String, Object> cuerpo = Map.of("pagina", 1, "tamano", 10, "filtros", List.of(List.of(
                Map.of("campo", "contrasena", "operacion", "like", "valor", "$2a$%"))), "ordenes", List.of());

        JsonNode error = paginar(cuerpo).esperarError(400, "PAGINADO_INVALIDO").json();

        assertEquals("El campo 'contrasena' no se puede usar en el paginado", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void operacionInvalida() {
        Map<String, Object> cuerpo = Map.of("pagina", 1, "tamano", 10, "filtros", List.of(List.of(
                Map.of("campo", "nombreDeUsuario", "operacion", "= 'x' OR 1=1 OR t.nombreDeUsuario =", "valor", "x"))),
                "ordenes", List.of());

        JsonNode error = paginar(cuerpo).esperarError(400, "PAGINADO_INVALIDO").json();

        assertEquals("La operacion '= 'x' OR 1=1 OR t.nombreDeUsuario =' no es valida", error.get("mensajeParaUsuario").asString());
    }

    // POST /usuario/paginar como SYSADMIN
    private Respuesta paginar(Map<String, Object> cuerpo) {
        return como("SYSADMIN").post("/usuario/paginar").json(cuerpo).ejecutar();
    }
}
