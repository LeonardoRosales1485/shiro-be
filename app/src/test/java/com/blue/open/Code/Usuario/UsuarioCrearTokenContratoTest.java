package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Peticion;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-TOK-01: crear un token de acceso personal (POST /usuario/crear-token, AccessTokenController de
 * core-protocolo-apikey). Migrado de bruno/User (08 Create Token y Create Token - nombre duplicado). Exige sesion pero
 * ningun permiso, asi que el caso "sin sesion" se prueba aca. El codigo del token es fijo en las pruebas
 * (AuthTokenServiceDePrueba).
 */
class UsuarioCrearTokenContratoTest extends PruebaDeContrato {

    private final EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);

    @Test
    void creaUnTokenPropio() {
        JsonNode token = crear(como(UsuariosDeSeed.ROL_DEMO), "Token de prueba").esperar(200).json();

        assertTrue(token.hasNonNull("uuid"));
        assertEquals("Token de prueba", token.get("nombre").asString());
        assertEquals(AuthTokenServiceDePrueba.codigo("Token de prueba"), token.get("codigo").asString());
        assertEquals(1, token.get("activo").asInt());
        assertEquals(UsuariosDeSeed.DEMO, token.get("usuarioUuid").asString());
    }

    @Test
    void nombreDuplicado() {
        usuarios.crearToken(UsuariosDeSeed.ROL_DEMO, "Token Duplicado Caso");

        JsonNode error = crear(como(UsuariosDeSeed.ROL_DEMO), "Token Duplicado Caso")
                .esperarError(400, "TOKEN_NOMBRE_DUPLICADO").json();

        assertEquals("Ya existe un token con el nombre: Token Duplicado Caso", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void sinNombre() {
        crear(como(UsuariosDeSeed.ROL_DEMO), "").esperarError(400, "FORMULARIO_INVALIDO");
    }

    @Test
    void sinSesion() {
        crear(sinAutenticacion(), "Token sin sesion").esperarError(401, "NO_AUTENTICADO");
    }

    // La ruta en ingles se renombro a la del glosario (ticket 08): ya no existe
    @Test
    void rutaViejaNoExiste() {
        como(UsuariosDeSeed.ROL_DEMO).post("/usuario/create-token").json(Map.of("name", "Token ruta vieja")).ejecutar()
                .esperar(404);
    }

    // POST /usuario/crear-token con el nombre recibido
    private Respuesta crear(Peticion peticion, String nombre) {
        return peticion.post("/usuario/crear-token").json(Map.of("nombre", nombre))
                .ejecutar();
    }
}
