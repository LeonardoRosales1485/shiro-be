package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-USR-04 y CU-USR-23: archivar y restaurar un usuario (T2.5 del plan y Spec 7). Un usuario archivado no ingresa,
 * sus tokens se desactivan y sus tareas conservan el responsable. Nadie se archiva a si mismo.
 */
class UsuarioArchivarContratoTest extends PruebaDeContrato {

    private static final String SYSADMIN = "SYSADMIN";

    @Autowired
    private AuthJwtServiceDePrueba authJwtService;

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    @Test
    void archivarDesactivaLosTokensYConservaElResponsable() {
        String uuid = new EscenarioDeUsuario(this::como).crearUuid("a-archivar");
        String tarea = como(SYSADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL,
                "tipoCodigo", "TAREA", "titulo", "Asignada", "responsableUuid", uuid)).preparacion().ejecutar()
                .esperar(200).json().get("uuid").asString();
        String token = sinAutenticacion().post("/usuario/crear-token").json(Map.of("nombre", "Token a revocar"))
                .encabezado("Authorization", "Bearer " + authJwtService.jwtFirmado(uuid)).ejecutar().esperar(200).json()
                .get("codigo").asString();

        JsonNode archivado = como(SYSADMIN).post("/usuario/{uuid}/archivar", uuid).ejecutar().esperar(200).json();

        assertEquals(-1, archivado.get("status").asInt());
        sinAutenticacion().get("/usuario/sesion").encabezado("Authorization", "Bearer " + token).ejecutar()
                .esperarError(401, "ACCESS_TOKEN_INVALIDO");
        assertEquals(uuid, como(SYSADMIN).get("/tarea/uuid/{uuid}", tarea).ejecutar().esperar(200).json()
                .get("responsableUuid").asString());
    }

    @Test
    void restaurarDevuelveAlUsuario() {
        String uuid = new EscenarioDeUsuario(this::como).crearUuid("a-restaurar");
        como(SYSADMIN).post("/usuario/{uuid}/archivar", uuid).preparacion().ejecutar().esperar(200);

        assertEquals(0, como(SYSADMIN).post("/usuario/{uuid}/restaurar", uuid).ejecutar().esperar(200).json()
                .get("status").asInt());
        como(SYSADMIN).post("/usuario/{uuid}/restaurar", uuid).ejecutar().esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    @Test
    void nadieSeArchivaASiMismo() {
        como(SYSADMIN).post("/usuario/{uuid}/archivar", UsuariosDeSeed.SYSADMIN).ejecutar()
                .esperarError(400, "USUARIO_NO_SE_ARCHIVA_A_SI_MISMO");
    }

    @Test
    void archivarDosVecesRechazado() {
        String uuid = new EscenarioDeUsuario(this::como).crearUuid("doble");
        como(SYSADMIN).post("/usuario/{uuid}/archivar", uuid).preparacion().ejecutar().esperar(200);

        como(SYSADMIN).post("/usuario/{uuid}/archivar", uuid).ejecutar().esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }
}
