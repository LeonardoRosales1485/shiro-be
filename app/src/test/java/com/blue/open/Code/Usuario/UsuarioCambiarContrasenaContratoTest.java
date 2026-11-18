package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-09: cambiar la contrasena con el codigo de recuperacion (POST /usuario/publico/cambiar-contrasena, publico).
 * Migrado de bruno/User (17 Public Change Password). El codigo se pide antes con forgot-password (paso de
 * preparacion) y es fijo en las pruebas (UsuarioServiceDePrueba), en lugar de leerlo del mail como el .bru.
 */
class UsuarioCambiarContrasenaContratoTest extends PruebaDeContrato {

    private static final String NOMBRE_DE_USUARIO = "usuario.cambio.contrato";

    private static final String NUEVA = "nuevaPassword123";

    private final EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);

    @Test
    void cambiaLaContrasena() {
        String email = usuarioConCodigo();

        JsonNode respuesta = cambiar(Map.of("email", email, "codigo", UsuarioServiceDePrueba.codigo(32), "contrasena", NUEVA))
                .esperar(200).json();

        assertEquals(200, respuesta.get("status").asInt());
        assertEquals("Contraseña cambiada correctamente", respuesta.get("mensaje").asString());
        assertTrue(respuesta.get("datos").isNull());
    }

    @Test
    void codigoYaUtilizado() {
        String email = usuarioConCodigo();
        Map<String, Object> cuerpo = Map.of("email", email, "codigo", UsuarioServiceDePrueba.codigo(32), "contrasena", NUEVA);
        sinAutenticacion().post("/usuario/publico/cambiar-contrasena").json(cuerpo)
                .preparacion().ejecutar().esperar(200);

        JsonNode error = cambiar(cuerpo).esperarError(400, "CODIGO_YA_UTILIZADO").json();

        assertEquals("El código ya fue utilizado", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void codigoInvalido() {
        String email = usuarioConCodigo();

        JsonNode error = cambiar(Map.of("email", email, "codigo", "CODIGOQUENOEXISTE", "contrasena", NUEVA))
                .esperarError(400, "CODIGO_INVALIDO").json();

        assertEquals("Código inválido", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void emailSinCuenta() {
        JsonNode error = cambiar(Map.of("email", "nadie.registrado@example.com", "codigo", UsuarioServiceDePrueba.codigo(32),
                "contrasena", NUEVA)).esperarError(400, "REGISTRO_NO_ENCONTRADO").json();

        assertEquals("No existe el usuario", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void sinContrasena() {
        cambiar(Map.of("email", "nadie.registrado@example.com", "codigo", UsuarioServiceDePrueba.codigo(32)))
                .esperarError(400, "FORMULARIO_INVALIDO");
    }

    // Crea un usuario, le pide el codigo de recuperacion y responde su email
    private String usuarioConCodigo() {
        usuarios.crear(NOMBRE_DE_USUARIO);
        String email = EscenarioDeUsuario.email(NOMBRE_DE_USUARIO);
        usuarios.codigoDeRecuperacion(email);
        return email;
    }

    // La ruta en ingles se renombro a la del glosario (ticket 06): ya no existe
    @Test
    void rutaViejaNoExiste() {
        sinAutenticacion().post("/usuario/public/change-password").json(Map.of("email", UsuariosDeSeed.DEMO_EMAIL, "code", "CODIGOQUENOEXISTE", "password", "nueva")).ejecutar().esperar(404);
    }

    // POST /usuario/publico/cambiar-contrasena sin autenticacion
    private Respuesta cambiar(Map<String, Object> cuerpo) {
        return sinAutenticacion().post("/usuario/publico/cambiar-contrasena").json(cuerpo)
                .ejecutar();
    }
}
