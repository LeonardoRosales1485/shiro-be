package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-26: listado de administracion de usuarios (GET /usuario/administracion, T6.5, prototipo "Administracion ·
 * usuarios"): todos los usuarios, tambien los archivados, con nombre, correo, si ingresan con cuenta de Shiro
 * (tienen contrasena) o con Google, si estan archivados y sus grupos con la fecha desde la que estan en cada uno.
 * Solo el super admin y los Administradores.
 */
class UsuarioListadoDeAdministracionContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private final EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);

    @Test
    void listaLosUsuariosConSuIngresoSusGruposYLosArchivados() {
        String conCuenta = usuarios.crearUuid("admin.listado.cuenta");
        Map<String, Object> deGoogle = new HashMap<>(Map.of("nombreDeUsuario", "admin.listado.google@example.com",
                "email", "admin.listado.google@example.com", "nombre", "Persona de Google"));
        String google = como(ADMIN).post("/usuario/guardar").json(deGoogle).preparacion().ejecutar().esperar(200).json()
                .get("uuid").asString();
        String archivado = usuarios.crearUuid("admin.listado.archivado");
        como(ADMIN).post("/usuario/{uuid}/grupos", conCuenta).json(Map.of("grupos", List.of(grupo("CLIENTE")))).preparacion()
                .ejecutar().esperar(200);
        como(ADMIN).post("/usuario/{uuid}/archivar", archivado).preparacion().ejecutar().esperar(200);

        JsonNode lista = como(ADMIN).get("/usuario/administracion").ejecutar().esperar(200).json();

        JsonNode cuenta = porUuid(lista, conCuenta);
        assertTrue(cuenta.get("cuentaDeShiro").asBoolean());
        assertFalse(cuenta.get("archivado").asBoolean());
        assertEquals("CLIENTE", cuenta.get("grupos").get(0).get("nombre").asString());
        assertFalse(cuenta.get("grupos").get(0).get("desde").isNull());
        assertFalse(cuenta.has("contrasena"));
        JsonNode deGoogleLeido = porUuid(lista, google);
        assertFalse(deGoogleLeido.get("cuentaDeShiro").asBoolean());
        assertEquals("Persona de Google", deGoogleLeido.get("nombre").asString());
        assertEquals(0, deGoogleLeido.get("grupos").size());
        assertTrue(porUuid(lista, archivado).get("archivado").asBoolean());
        assertEquals("SYSADMIN", porUuid(lista, UsuariosDeSeed.SYSADMIN).get("grupos").get(0).get("nombre").asString());
    }

    private String grupo(String nombre) {
        return como("SYSADMIN").post("/rol/paginar").json(Map.of("pagina", 1, "tamano", 10, "filtros",
                List.of(List.of(Map.of("campo", "nombre", "operacion", "=", "valor", nombre))))).preparacion()
                .ejecutar().esperar(200).json().get("contenido").get(0).get("uuid").asString();
    }

    private static JsonNode porUuid(JsonNode lista, String uuid) {
        for (JsonNode usuario : lista) {
            if (uuid.equals(usuario.get("uuid").asString()))
                return usuario;
        }
        throw new IllegalStateException("Sin el usuario " + uuid + " en " + lista);
    }
}
