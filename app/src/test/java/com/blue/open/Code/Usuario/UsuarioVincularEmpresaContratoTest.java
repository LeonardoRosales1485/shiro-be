package com.blue.open.Code.Usuario;

import com.blue.open.Core.Identity.Auth.RolBaseEnum;
import com.blue.open.Core.Test.Peticion;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-USR-13: vincular al usuario de la sesion a la empresa del header X-Empresa-Uuid con el rol por defecto de esa
 * empresa (POST /usuario/publico/vincular/{usuarioUuid}, core-multiempresa). Conserva el prefijo publico/ pero exige sesion.
 * Migrado de bruno/User (12 Public Link, Public Link - empresa de sistema y Public Link - otro usuario).
 */
class UsuarioVincularEmpresaContratoTest extends PruebaDeContrato {

    // Empresa operativa BlueOpenData (datos requeridos), con el rol por defecto CLIENTE
    private static final String EMPRESA = "2";

    // Empresa de sistema (datos requeridos)
    private static final String EMPRESA_DE_SISTEMA = "1";

    @Test
    void vinculaAlUsuarioDeLaSesion() {
        JsonNode usuario = vincular(como(RolBaseEnum.SYSADMIN.name()), UsuariosDeSeed.SYSADMIN, EMPRESA)
                .esperar(200).json();

        assertEquals(UsuariosDeSeed.SYSADMIN, usuario.get("uuid").asString());
    }

    @Test
    void empresaDeSistema() {
        // Ni un SYSADMIN entra por esta via: no es falta de permiso sino una regla de la empresa de sistema
        vincular(como(RolBaseEnum.SYSADMIN.name()), UsuariosDeSeed.SYSADMIN, EMPRESA_DE_SISTEMA)
                .esperarError(400, "EMPRESA_DE_SISTEMA_NO_PERMITIDA");
    }

    @Test
    void otroUsuario() {
        JsonNode error = vincular(como(RolBaseEnum.SYSADMIN.name()), UsuariosDeSeed.DEMO, EMPRESA)
                .esperarError(403, "SIN_PERMISO").json();

        assertEquals("No permitido", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void sinSesion() {
        vincular(sinAutenticacion(), UsuariosDeSeed.SYSADMIN, EMPRESA).esperarError(401, "NO_AUTENTICADO");
    }

    // La ruta en ingles se renombro a la del glosario (ticket 07): ya no existe
    @Test
    void rutaViejaNoExiste() {
        como(RolBaseEnum.SYSADMIN.name()).post("/usuario/public/link/{userUuid}", UsuariosDeSeed.SYSADMIN)
                .encabezado("X-Empresa-Uuid", EMPRESA).ejecutar().esperar(404);
    }

    // POST /usuario/publico/vincular/{usuarioUuid} con la empresa en el header
    private Respuesta vincular(Peticion peticion, String usuarioUuid, String empresa) {
        return peticion.post("/usuario/publico/vincular/{usuarioUuid}", usuarioUuid).encabezado("X-Empresa-Uuid", empresa)
                .ejecutar();
    }
}
