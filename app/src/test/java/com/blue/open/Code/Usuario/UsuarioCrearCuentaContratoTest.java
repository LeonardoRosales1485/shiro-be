package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-27: crear una cuenta de Shiro (POST /usuario/crear-cuenta, resolucion de la propuesta 56): nombre, nombre de
 * usuario, correo y grupos. El backend genera la contrasena inicial y la responde una sola vez; despues ningun
 * endpoint la devuelve. Un Administrador no suma a nadie al grupo Administradores. Solo el super admin y los
 * Administradores.
 */
class UsuarioCrearCuentaContratoTest extends PruebaDeContrato {

    @Test
    void creaLaCuentaConContrasenaGeneradaYSusGrupos() {
        JsonNode creada = como("ADMIN").post("/usuario/crear-cuenta").json(Map.of("nombre", "Ana Externa", "nombreDeUsuario", "ana.externa",
                "email", "ana@proveedor.com", "grupos", List.of(grupo("CLIENTE")))).ejecutar().esperar(200).json();

        String contrasena = creada.get("contrasena").asString();
        assertTrue(contrasena.length() >= 16, contrasena);
        assertEquals("ana.externa", creada.get("nombreDeUsuario").asString());
        sinAutenticacion().post("/usuario/iniciar-sesion").json(Map.of("nombreDeUsuario", "ana.externa", "contrasena", contrasena))
                .preparacion().ejecutar().esperar(200);
        String uuid = creada.get("uuid").asString();
        assertEquals("CLIENTE", como("ADMIN").get("/usuario/{uuid}/grupos", uuid).ejecutar().esperar(200).json().get(0).get("nombre").asString());
        assertFalse(como("ADMIN").get("/usuario/uuid/{uuid}", uuid).ejecutar().esperar(200).json().hasNonNull("contrasena"));
    }

    @Test
    void unAdministradorNoSumaAlGrupoAdministradores() {
        como("ADMIN").post("/usuario/crear-cuenta").json(Map.of("nombre", "Otra", "nombreDeUsuario", "otra.cuenta", "email", "otra@proveedor.com",
                "grupos", List.of(grupo("ADMIN")))).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    @Test
    void faltaElNombreDeUsuario() {
        como("ADMIN").post("/usuario/crear-cuenta").json(Map.of("nombre", "Sin usuario", "email", "sin@proveedor.com", "grupos", List.of()))
                .ejecutar().esperarError(400, "VALIDACION_FALLIDA");
    }

    private String grupo(String nombre) {
        return como("SYSADMIN").post("/rol/paginar").json(Map.of("pagina", 1, "tamano", 10, "filtros",
                List.of(List.of(Map.of("campo", "nombre", "operacion", "=", "valor", nombre))))).preparacion()
                .ejecutar().esperar(200).json().get("contenido").get(0).get("uuid").asString();
    }
}
