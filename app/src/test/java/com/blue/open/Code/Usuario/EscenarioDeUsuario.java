package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.Peticion;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.function.Function;

/**
 * Builder de usuarios y tokens de acceso para las pruebas de contrato: crea usuarios y crea tokens por los endpoints reales (pasos de preparacion, dentro de la transaccion del test: se
 * revierten al terminar). Equivalente de los scripts pre-request de bruno/User. Se construye con el cliente de la
 * prueba: new EscenarioDeUsuario(this::como).
 */
public final class EscenarioDeUsuario {

    // Actor de los pasos de preparacion (tiene todos los permisos)
    public static final String SYSADMIN = "SYSADMIN";

    // Contrasena de los usuarios que crea el escenario
    public static final String CONTRASENA = "123456";

    private final Function<String, Peticion> como;

    // Recibe el cliente de la prueba (rol -> peticion), normalmente this::como de una PruebaDeContrato
    public EscenarioDeUsuario(Function<String, Peticion> como) {
        this.como = como;
    }

    // Body de alta de un usuario con el nombre de usuario recibido (email {nombreDeUsuario}@example.com)
    public static Map<String, Object> alta(String nombreDeUsuario) {
        return Map.of("nombreDeUsuario", nombreDeUsuario, "contrasena", CONTRASENA, "email", email(nombreDeUsuario),
                "telefono", "3815550000");
    }

    // Email de los usuarios que crea el escenario
    public static String email(String nombreDeUsuario) {
        return nombreDeUsuario + "@example.com";
    }

    // Crea un usuario activo con el nombre de usuario recibido (POST /usuario/guardar) y responde el usuario creado
    public JsonNode crear(String nombreDeUsuario) {
        return como.apply(SYSADMIN).post("/usuario/guardar").json(alta(nombreDeUsuario))
                .preparacion().ejecutar().esperar(200).json();
    }

    // Crea un usuario activo y responde su uuid
    public String crearUuid(String nombreDeUsuario) {
        return crear(nombreDeUsuario).get("uuid").asString();
    }

    // Elimina (borrado logico) el usuario recibido (DELETE /usuario/borrar/{uuid})
    public void eliminar(String uuid) {
        como.apply(SYSADMIN).delete("/usuario/borrar/{uuid}", uuid)
                .preparacion().ejecutar().esperar(200);
    }

    // Crea un token de acceso con el nombre recibido para el usuario del rol (POST /usuario/crear-token) y responde
    // el token creado
    public JsonNode crearToken(String rol, String nombre) {
        return como.apply(rol).post("/usuario/crear-token").json(Map.of("nombre", nombre))
                .preparacion().ejecutar().esperar(200).json();
    }
}
