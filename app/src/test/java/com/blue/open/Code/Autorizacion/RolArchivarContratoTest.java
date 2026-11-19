package com.blue.open.Code.Autorizacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-ROL-04 y reglas de grupos (T2.5 del plan): un grupo se archiva (nada se borra); el nombre de un grupo no se
 * repite; los grupos SYSADMIN y ADMIN no se archivan, y un Administrador no los edita.
 */
class RolArchivarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private final EscenarioDeAutorizacion autorizacion = new EscenarioDeAutorizacion(this::como);

    @Test
    void archivarUnGrupo() {
        String uuid = autorizacion.crearRol("Grupo a archivar");

        JsonNode rol = como(ADMIN).post("/rol/{uuid}/archivar", uuid).ejecutar().esperar(200).json();

        assertEquals(-1, rol.get("status").asInt());
        como(ADMIN).post("/rol/{uuid}/archivar", uuid).ejecutar().esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    @Test
    void elNombreDeUnGrupoNoSeRepite() {
        autorizacion.crearRol("Desarrollo");

        como(ADMIN).post("/rol/guardar").json(EscenarioDeAutorizacion.rol("Desarrollo")).ejecutar()
                .esperarError(400, "GRUPO_REPETIDO");
    }

    @Test
    void losGruposDelSistemaNoSeArchivanNiLosEditaUnAdministrador() {
        String administradores = uuidDeGrupo("ADMIN");

        como("SYSADMIN").post("/rol/{uuid}/archivar", administradores).ejecutar().esperarError(400, "GRUPO_DEL_SISTEMA");
        como(ADMIN).post("/rol/guardar").json(Map.of("uuid", administradores, "nombre", "ADMIN")).ejecutar()
                .esperarError(403, "SIN_PERMISO");
    }

    // Uuid del grupo (rol) con el nombre recibido
    private String uuidDeGrupo(String nombre) {
        return como("SYSADMIN").post("/rol/paginar").json(Map.of("pagina", 1, "tamano", 10, "filtros",
                List.of(List.of(Map.of("campo", "nombre", "operacion", "=", "valor", nombre))))).preparacion()
                .ejecutar().esperar(200).json().get("contenido").get(0).get("uuid").asString();
    }
}
