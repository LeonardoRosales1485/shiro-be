package com.blue.open.Code.ReglaPermiso;

import com.blue.open.Code.Usuario.UsuariosDeSeed;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-RPE-03 y CU-RPE-04: crear, editar y archivar reglas de permiso (T2.3 del plan y Spec 2). Una regla tiene un
 * sujeto (grupo o usuario), un recurso (proyecto o tablero), un nivel (VER, EDITAR, ARCHIVAR) y un efecto
 * (PERMITIR, NEGAR); no se repite el mismo sujeto, recurso y nivel. Solo las administran el super admin y los
 * Administradores.
 */
class ReglaPermisoGuardarContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    @Test
    void reglaDeUnUsuarioSobreUnProyecto() {
        JsonNode regla = guardar(regla("USUARIO", "usuarioUuid", UsuariosDeSeed.SYSADMIN, "PROYECTO", "proyectoUuid",
                ReglaPermisoContratoTest.PROYECTO_SHIRO, "ARCHIVAR", "NEGAR")).esperar(200).json();

        assertEquals("USUARIO", regla.get("tipoSujeto").asString());
        assertEquals(UsuariosDeSeed.SYSADMIN, regla.get("usuarioUuid").asString());
        assertEquals("SHI", regla.get("proyectoPrefijo").asString());
        assertEquals("NEGAR", regla.get("efecto").asString());
    }

    @Test
    void reglaDeUnGrupoSobreUnTablero() {
        JsonNode regla = guardar(regla("ROL", "rolUuid", ReglaPermisoContratoTest.ROL_CLIENTE, "TABLERO", "tableroUuid",
                ReglaPermisoContratoTest.TABLERO_QA, "EDITAR", "PERMITIR")).esperar(200).json();

        assertEquals("CLIENTE", regla.get("rolNombre").asString());
        assertEquals("QA", regla.get("tableroCodigo").asString());
    }

    @Test
    void elSujetoYElRecursoTienenQueCoincidirConSuTipo() {
        Map<String, Object> sinUsuario = regla("USUARIO", "rolUuid", ReglaPermisoContratoTest.ROL_CLIENTE, "PROYECTO",
                "proyectoUuid", ReglaPermisoContratoTest.PROYECTO_SHIRO, "VER", "PERMITIR");
        Map<String, Object> dosRecursos = regla("ROL", "rolUuid", ReglaPermisoContratoTest.ROL_CLIENTE, "PROYECTO",
                "proyectoUuid", ReglaPermisoContratoTest.PROYECTO_SHIRO, "VER", "PERMITIR");
        dosRecursos.put("tableroUuid", ReglaPermisoContratoTest.TABLERO_QA);

        guardar(sinUsuario).esperarError(400, "REGLA_DE_PERMISO_INVALIDA");
        guardar(dosRecursos).esperarError(400, "REGLA_DE_PERMISO_INVALIDA");
    }

    @Test
    void elMismoSujetoRecursoYNivelNoSeRepite() {
        guardar(regla("ROL", "rolUuid", ReglaPermisoContratoTest.ROL_CLIENTE, "PROYECTO", "proyectoUuid",
                ReglaPermisoContratoTest.PROYECTO_SHIRO, "VER", "NEGAR")).esperarError(400, "REGLA_DE_PERMISO_REPETIDA");
    }

    @Test
    void archivarUnaRegla() {
        String uuid = guardar(regla("ROL", "rolUuid", ReglaPermisoContratoTest.ROL_CLIENTE, "TABLERO", "tableroUuid",
                ReglaPermisoContratoTest.TABLERO_QA, "ARCHIVAR", "PERMITIR")).esperar(200).json().get("uuid").asString();

        assertEquals(-1, como(ADMIN).post("/regla-permiso/{uuid}/archivar", uuid).ejecutar().esperar(200).json()
                .get("status").asInt());
        como(ADMIN).post("/regla-permiso/{uuid}/archivar", uuid).ejecutar().esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // POST /regla-permiso/guardar como Administrador
    private Respuesta guardar(Map<String, Object> cuerpo) {
        return como(ADMIN).post("/regla-permiso/guardar").json(cuerpo).ejecutar();
    }

    // Body de una regla con un campo de sujeto y un campo de recurso
    private static Map<String, Object> regla(String tipoSujeto, String campoSujeto, String sujeto, String tipoRecurso,
            String campoRecurso, String recurso, String nivel, String efecto) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("tipoSujeto", tipoSujeto);
        cuerpo.put(campoSujeto, sujeto);
        cuerpo.put("tipoRecurso", tipoRecurso);
        cuerpo.put(campoRecurso, recurso);
        cuerpo.put("nivel", nivel);
        cuerpo.put("efecto", efecto);
        return cuerpo;
    }
}
