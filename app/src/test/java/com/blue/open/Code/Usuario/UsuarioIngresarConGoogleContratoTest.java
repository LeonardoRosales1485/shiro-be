package com.blue.open.Code.Usuario;

import org.springframework.test.util.ReflectionTestUtils;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Code.Common.Google.VerificadorDeTokenDeGoogle;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Common.Google.ClavesPublicasDeGoogle;
import com.blue.open.Code.Common.Google.TokensDeGoogleDePrueba;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * CU-USR-20: ingresar con Google (POST /usuario/ingresar-con-google). T2.2 del plan y Spec 2: el front manda el ID
 * token de Google; el backend lo valida, busca o crea el usuario por correo y responde el JWT del Core. Un usuario
 * nuevo entra sin roles. Las claves publicas de Google son un doble: la prueba no llama a Google. En el profile test
 * el JWT de la respuesta es fijo (AuthJwtServiceDePrueba); para usar la sesion se pide uno firmado.
 */
class UsuarioIngresarConGoogleContratoTest extends PruebaDeContrato {

    @MockitoBean
    private ClavesPublicasDeGoogle clavesPublicasDeGoogle;

    @Autowired
    private AuthJwtServiceDePrueba authJwtService;

    @Autowired
    private VerificadorDeTokenDeGoogle verificador;

    @BeforeEach
    void clavesDePrueba() {
        when(clavesPublicasDeGoogle.clave(TokensDeGoogleDePrueba.KID))
                .thenReturn(Optional.of(TokensDeGoogleDePrueba.clavePublica()));
    }

    @Test
    void unUsuarioNuevoEntraSinRoles() {
        JsonNode sesion = ingresar(TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos("nueva@example.com")))
                .esperar(200).json();

        assertEquals(AuthJwtServiceDePrueba.JWT, sesion.get("token").asString());
        JsonNode creados = usuariosConCorreo("nueva@example.com");
        assertEquals(1, creados.size());
        String jwt = authJwtService.jwtFirmado(creados.get(0).get("uuid").asString());
        JsonNode usuario = sinAutenticacion().get("/usuario/sesion").encabezado("Authorization", "Bearer " + jwt)
                .ejecutar().esperar(200).json();
        assertEquals("nueva@example.com", usuario.get("email").asString());
        assertEquals("nueva@example.com", usuario.get("nombreDeUsuario").asString());
        sinAutenticacion().post("/proyecto/guardar").encabezado("Authorization", "Bearer " + jwt)
                .json(Map.of("nombre", "Sin permiso", "prefijo", "SPE")).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    @Test
    void elMismoCorreoEntraComoElMismoUsuario() {
        String token = TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos("repetida@example.com"));

        ingresar(token).esperar(200);
        ingresar(token).esperar(200);

        assertEquals(1, usuariosConCorreo("repetida@example.com").size());
    }

    @Test
    void unaCuentaDeOtroDominioRechazada() {
        Map<String, Object> claims = TokensDeGoogleDePrueba.claimsValidos("alguien@gmail.com");
        claims.remove("hd");

        ingresar(TokensDeGoogleDePrueba.firmar(claims)).esperarError(403, "DOMINIO_DE_GOOGLE_NO_PERMITIDO");
    }

    @Test
    void unTokenInvalidoRechazado() {
        ingresar(TokensDeGoogleDePrueba.firmarConOtraClave(TokensDeGoogleDePrueba.claimsValidos("ana@example.com")))
                .esperarError(401, "TOKEN_DE_GOOGLE_INVALIDO");
    }

    @Test
    void unUsuarioArchivadoNoEntra() {
        EscenarioDeUsuario escenario = new EscenarioDeUsuario(this::como);
        String uuid = escenario.crearUuid("archivada");
        escenario.eliminar(uuid);

        ingresar(TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos(EscenarioDeUsuario.email("archivada"))))
                .esperarError(401, "NO_AUTENTICADO");
    }

    @Test
    void sinTokenRechazado() {
        sinAutenticacion().post("/usuario/ingresar-con-google").json(Map.of()).ejecutar()
                .esperarError(400, "FORMULARIO_INVALIDO");
    }

    @Test
    void googleNoResponde() {
        when(clavesPublicasDeGoogle.clave(TokensDeGoogleDePrueba.KID))
                .thenThrow(new ExcepcionDeApi(CodigoDeErrorDelProyecto.GOOGLE_NO_DISPONIBLE));

        ingresar(TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos("ana@example.com")))
                .esperarError(503, "GOOGLE_NO_DISPONIBLE");
    }

    @Test
    void sinConfiguracionDeGoogle() {
        Object idDeCliente = ReflectionTestUtils.getField(verificador, "idDeCliente");
        ReflectionTestUtils.setField(verificador, "idDeCliente", "");
        try {
            ingresar(TokensDeGoogleDePrueba.firmar(TokensDeGoogleDePrueba.claimsValidos("ana@example.com")))
                    .esperarError(503, "GOOGLE_NO_CONFIGURADO");
        } finally {
            ReflectionTestUtils.setField(verificador, "idDeCliente", idDeCliente);
        }
    }

    // POST /usuario/ingresar-con-google sin sesion
    private Respuesta ingresar(String idToken) {
        return sinAutenticacion().post("/usuario/ingresar-con-google").json(Map.of("idToken", idToken)).ejecutar();
    }

    // Usuarios vigentes con el correo recibido (POST /usuario/paginar como super admin)
    private JsonNode usuariosConCorreo(String email) {
        return como("SYSADMIN").post("/usuario/paginar").json(Map.of("pagina", 1, "tamano", 10, "filtros",
                List.of(List.of(Map.of("campo", "email", "operacion", "=", "valor", email))))).preparacion().ejecutar()
                .esperar(200).json().get("contenido");
    }
}
