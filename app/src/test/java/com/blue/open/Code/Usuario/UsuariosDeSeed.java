package com.blue.open.Code.Usuario;

import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Datos de los usuarios de los seeds de Liquibase que usan las pruebas de contrato de /usuario. El usuario demo
 * (2026-10-09-insert-usuarios-seed.yaml) es el actor del rol ADMIN en ActoresDelProyecto.
 */
public final class UsuariosDeSeed {

    // Rol del actor demo en ActoresDelProyecto
    public static final String ROL_DEMO = "ADMIN";

    // Usuario demo
    public static final String DEMO = "90000000-0000-4000-8000-000000000002";

    public static final String DEMO_NOMBRE_DE_USUARIO = "demo";

    public static final String DEMO_EMAIL = "demo@example.local";

    public static final String DEMO_TELEFONO = "1122334455";
    // Relacion empresa-roles del usuario demo (auth_usuarios_empresas): empresa BlueOpenData con el rol ADMIN
    public static final String DEMO_RELACION = "90000000-0000-4000-8000-000000000005";
    public static final String DEMO_EMPRESA = "2";
    public static final String DEMO_EMPRESA_NOMBRE = "BlueOpenData";

    // Token de acceso del usuario demo (auth_access_tokens)
    public static final String TOKEN_DEMO = "90000000-0000-4000-8000-000000000007";

    public static final String TOKEN_DEMO_NOMBRE = "Token demo";

    public static final String TOKEN_DEMO_CODIGO = "demo-access-token";

    // Refresh token JWT del usuario demo (auth_jwt_refresh_tokens), vigente hasta 2099
    public static final String REFRESH_TOKEN_DEMO = "demo-refresh-token";

    // Usuario sysadmin-pruebas, actor del rol SYSADMIN (2026-10-09-insert-usuarios-seed.yaml)
    public static final String SYSADMIN = "90000000-0000-4000-8000-000000000012";

    // Usuario pendiente de verificacion (status -2) con el codigo vigente (2026-10-09-insert-usuarios-seed.yaml)
    public static final String PENDIENTE = "90000000-0000-4000-8000-000000000018";

    // Usuario pendiente de verificacion (status -2) con el codigo vencido
    public static final String PENDIENTE_VENCIDO = "90000000-0000-4000-8000-000000000019";

    // Codigo de verificacion de los dos usuarios pendientes
    public static final String CODIGO_PENDIENTE = "ABC123";

    // Uuid que no existe en ninguna tabla
    public static final String INEXISTENTE = "ffffffff-ffff-4fff-bfff-ffffffffffff";

    private UsuariosDeSeed() {
    }

    // Verifica que el usuario demo responda su empresa con el rol ADMIN y los permisos del rol (por @Coleccion)
    static void verificarEmpresasYRolesDelDemo(JsonNode usuario) {
        JsonNode relaciones = usuario.get("rolesPorEmpresa");
        assertEquals(1, relaciones.size(), relaciones::toString);
        JsonNode relacion = relaciones.get(0);
        assertEquals(DEMO_RELACION, relacion.get("uuid").asString());
        // uuid es CHAR(36): el "2" de los seeds vuelve completado con espacios
        assertEquals(DEMO_EMPRESA, relacion.get("empresaUuid").asString().trim());
        assertEquals(DEMO_EMPRESA_NOMBRE, relacion.get("empresaNombre").asString());
        assertEquals(1, relacion.get("roles").size(), relacion::toString);
        JsonNode rol = relacion.get("roles").get(0);
        assertEquals(ROL_DEMO, rol.get("nombre").asString());
        assertFalse(rol.get("permisos").isEmpty(), "El rol ADMIN responde sus permisos");
        assertTrue(rol.get("permisos").get(0).hasNonNull("nombre"));
    }
}
