package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-USR-03: alta o edicion de un usuario (POST /usuario/guardar). Migrado de bruno/User (03 Save or Create). El caso
 * "Save - sin autenticacion" lo genera core-test. El alta sin rolesPorEmpresa crea el usuario sin empresas.
 */
class UsuarioGuardarContratoTest extends PruebaDeContrato {

    private final EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);

    @Test
    void altaDeUnUsuario() {
        JsonNode creado = guardar(EscenarioDeUsuario.alta("usuario.prueba.contrato")).esperar(200).json();

        assertTrue(creado.hasNonNull("uuid"));
        assertEquals("usuario.prueba.contrato", creado.get("nombreDeUsuario").asString());
        assertEquals("usuario.prueba.contrato@example.com", creado.get("email").asString());
        assertEquals(0, creado.get("status").asInt());
        assertFalse(creado.hasNonNull("contrasena"));
    }

    @Test
    void edicionDeUnUsuario() {
        String uuid = usuarios.crearUuid("usuario.edicion.contrato");

        JsonNode editado = guardar(Map.of("uuid", uuid, "nombreDeUsuario", "usuario.edicion.contrato",
                "email", "usuario.edicion.contrato@example.com", "telefono", "3815559999")).esperar(200).json();

        assertEquals(uuid, editado.get("uuid").asString());
        assertEquals("3815559999", editado.get("telefono").asString());
        assertFalse(editado.hasNonNull("contrasena"));
    }

    @Test
    void laContrasenaSeGuardaHasheada_yNoSePisaAlEditarSinEnviarla() {
        String uuid = usuarios.crearUuid("usuario.contrasena.contrato");
        iniciarSesion("usuario.contrasena.contrato", EscenarioDeUsuario.CONTRASENA).esperar(200);

        JsonNode editado = guardar(Map.of("uuid", uuid, "nombreDeUsuario", "usuario.contrasena.contrato",
                "email", EscenarioDeUsuario.email("usuario.contrasena.contrato"))).esperar(200).json();

        assertFalse(editado.hasNonNull("contrasena"));
        iniciarSesion("usuario.contrasena.contrato", EscenarioDeUsuario.CONTRASENA).esperar(200);
    }

    @Test
    void guardarYObtenerRespondenLaMismaForma() {
        JsonNode guardado = guardar(EscenarioDeUsuario.alta("usuario.misma.forma")).esperar(200).json();

        JsonNode obtenido = como("SYSADMIN").get("/usuario/uuid/{uuid}", guardado.get("uuid").asString())
                .ejecutar().esperar(200).json();
        assertEquals(obtenido, guardado);
    }

    @Test
    void emailInvalido() {
        JsonNode error = guardar(Map.of("nombreDeUsuario", "usuario.email.invalido", "email", "no-es-un-email"))
                .esperarError(400, "FORMULARIO_INVALIDO").json();

        boolean conMensaje = false;
        for (JsonNode subError : error.get("errores")) {
            conMensaje |= "email".equals(subError.get("nombre").asString())
                    && "El email no tiene un formato valido".equals(subError.get("mensaje").asString());
        }
        assertTrue(conMensaje, "Falta el error del campo email: " + error);
    }

    // POST /usuario/iniciar-sesion sin autenticacion (paso de preparacion: verifica la contrasena guardada)
    private Respuesta iniciarSesion(String nombreDeUsuario, String contrasena) {
        return sinAutenticacion().post("/usuario/iniciar-sesion").json(Map.of("nombreDeUsuario", nombreDeUsuario, "contrasena", contrasena))
                .preparacion().ejecutar();
    }

    // POST /usuario/guardar como SYSADMIN
    private Respuesta guardar(Map<String, Object> cuerpo) {
        return como("SYSADMIN").post("/usuario/guardar").json(cuerpo).ejecutar();
    }
}
