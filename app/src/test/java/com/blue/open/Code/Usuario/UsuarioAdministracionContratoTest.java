package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Administracion de usuarios y grupos (T2.5 del plan, Spec 2 y seccion 15 del DAF): los Administradores administran
 * usuarios y grupos, menos al super admin y a otros Administradores; solo el super admin suma personas al grupo
 * Administradores. Los grupos de un usuario se asignan con POST /usuario/{uuid}/grupos (CU-USR-22) y se leen con
 * GET /usuario/{uuid}/grupos (CU-USR-21).
 */
class UsuarioAdministracionContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String SYSADMIN = "SYSADMIN";

    @Autowired
    private AuthJwtServiceDePrueba authJwtService;

    private final EscenarioDeUsuario usuarios = new EscenarioDeUsuario(this::como);

    @Test
    void unAdministradorNoModificaAlSuperAdmin() {
        como(ADMIN).post("/usuario/guardar").json(Map.of("uuid", UsuariosDeSeed.SYSADMIN, "nombreDeUsuario",
                "sysadmin-pruebas", "email", "otro@example.com")).ejecutar().esperarError(403, "SIN_PERMISO");
        como(ADMIN).post("/usuario/{uuid}/archivar", UsuariosDeSeed.SYSADMIN).ejecutar().esperarError(403, "SIN_PERMISO");
        asignar(ADMIN, UsuariosDeSeed.SYSADMIN, List.of()).esperarError(403, "SIN_PERMISO");
    }

    @Test
    void unAdministradorAdministraUsuariosComunes() {
        String uuid = usuarios.crearUuid("comun");
        String cliente = uuidDeGrupo("CLIENTE");

        JsonNode grupos = asignar(ADMIN, uuid, List.of(cliente)).esperar(200).json();

        assertEquals(List.of("CLIENTE"), nombres(grupos));
        assertEquals(List.of("CLIENTE"), nombres(como(ADMIN).get("/usuario/{uuid}/grupos", uuid).ejecutar().esperar(200).json()));
        como(ADMIN).post("/usuario/guardar").json(Map.of("uuid", uuid, "nombreDeUsuario", "comun",
                "email", "comun-editado@example.com")).ejecutar().esperar(200);
    }

    @Test
    void soloElSuperAdminSumaPersonasAlGrupoAdministradores() {
        String uuid = usuarios.crearUuid("futuro-admin");
        String administradores = uuidDeGrupo("ADMIN");

        asignar(ADMIN, uuid, List.of(administradores)).esperarError(403, "SIN_PERMISO");
        asignar(SYSADMIN, uuid, List.of(administradores)).esperar(200);

        String jwt = authJwtService.jwtFirmado(uuid);
        sinAutenticacion().post("/proyecto/guardar").encabezado("Authorization", "Bearer " + jwt)
                .json(Map.of("nombre", "Del nuevo admin", "prefijo", "DNA")).ejecutar().esperar(200);
        como(ADMIN).post("/usuario/{uuid}/archivar", uuid).ejecutar().esperarError(403, "SIN_PERMISO");
    }

    @Test
    void asignarReemplazaLosGruposYRechazaUnoInexistente() {
        String uuid = usuarios.crearUuid("con-grupos");
        asignar(ADMIN, uuid, List.of(uuidDeGrupo("CLIENTE"))).esperar(200);

        assertEquals(0, asignar(ADMIN, uuid, List.of()).esperar(200).json().size());
        asignar(ADMIN, uuid, List.of("ffffffff-ffff-4fff-bfff-ffffffffffff")).esperarError(400, "RELACION_NO_ENCONTRADA");
    }

    @Test
    void gruposDeUnUsuarioInexistente() {
        como(ADMIN).get("/usuario/{uuid}/grupos", "ffffffff-ffff-4fff-bfff-ffffffffffff").ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // POST /usuario/{uuid}/grupos como el actor del rol
    private Respuesta asignar(String rol, String uuid, List<String> grupos) {
        return como(rol).post("/usuario/{uuid}/grupos", uuid).json(Map.of("grupos", grupos)).ejecutar();
    }

    // Uuid del grupo (rol) con el nombre recibido
    private String uuidDeGrupo(String nombre) {
        JsonNode pagina = como(SYSADMIN).post("/rol/paginar").json(Map.of("pagina", 1, "tamano", 10, "filtros",
                List.of(List.of(Map.of("campo", "nombre", "operacion", "=", "valor", nombre))))).preparacion()
                .ejecutar().esperar(200).json();
        return pagina.get("contenido").get(0).get("uuid").asString();
    }

    private static List<String> nombres(JsonNode grupos) {
        List<String> nombres = new ArrayList<>();
        grupos.forEach(grupo -> nombres.add(grupo.get("nombre").asString()));
        return nombres;
    }
}
