package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Tarea.TareasDeSeed;
import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CU-PRY-06: usuarios con acceso a un proyecto (GET /proyecto/{uuid}/usuarios), para elegir el responsable de una
 * tarea (decision del 19/11 sobre la adenda propuesta 42: cualquier usuario del proyecto elige al responsable, sea
 * administrador o no). Tienen acceso el super admin, los Administradores y quienes pueden ver el proyecto o alguno de
 * sus tableros por sus reglas o las de sus grupos. Exige VER en el proyecto.
 */
class ProyectoUsuariosContratoTest extends PruebaDeContrato {

    private static final String PROYECTO_SHIRO = "b1000000-0000-4000-8000-000000000001";

    @Test
    void listaALosQueVenElProyecto() {
        List<String> usuarios = nombres(como("CLIENTE").get("/proyecto/{uuid}/usuarios", PROYECTO_SHIRO).ejecutar().esperar(200).json());

        assertTrue(usuarios.contains("cliente"), usuarios::toString);
        assertTrue(usuarios.contains("demo"), usuarios::toString);
        assertTrue(usuarios.contains("sysadmin-pruebas"), usuarios::toString);
        assertFalse(usuarios.contains("pendiente-verificacion"), usuarios::toString);
    }

    @Test
    void enUnProyectoSinReglasSoloEstanLosAdministradores() {
        String proyecto = crearProyecto("USR");

        List<String> usuarios = nombres(como("ADMIN").get("/proyecto/{uuid}/usuarios", proyecto).ejecutar().esperar(200).json());

        assertTrue(usuarios.contains("demo"), usuarios::toString);
        assertFalse(usuarios.contains("cliente"), usuarios::toString);
    }

    @Test
    void sinVerElProyectoNoSeListan() {
        String proyecto = crearProyecto("USN");

        como("CLIENTE").get("/proyecto/{uuid}/usuarios", proyecto).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    @Test
    void proyectoInexistente() {
        como("ADMIN").get("/proyecto/{uuid}/usuarios", TareasDeSeed.TABLERO_INEXISTENTE).ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // Crea un proyecto sin reglas de permiso y responde su uuid
    private String crearProyecto(String prefijo) {
        return como("SYSADMIN").post("/proyecto/guardar").json(Map.of("nombre", "Proyecto " + prefijo, "prefijo", prefijo))
                .preparacion().ejecutar().esperar(200).json().get("uuid").asString();
    }

    // Nombres de usuario de la respuesta
    private static List<String> nombres(JsonNode usuarios) {
        List<String> nombres = new ArrayList<>();
        usuarios.forEach(usuario -> nombres.add(usuario.get("nombreDeUsuario").asString()));
        return nombres;
    }
}
