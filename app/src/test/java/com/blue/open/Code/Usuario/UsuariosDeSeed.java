package com.blue.open.Code.Usuario;

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
}
