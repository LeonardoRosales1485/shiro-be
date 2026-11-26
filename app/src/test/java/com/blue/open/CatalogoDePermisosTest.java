package com.blue.open;

import com.blue.open.Code.Autorizacion.PermisoDeRecurso;
import com.blue.open.Code.Autorizacion.RecursoDePermiso;
import com.blue.open.Code.Autorizacion.SoloSesion;
import com.blue.open.Code.ReglaPermiso.NivelDePermiso;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generador del catalogo de permisos (MJ-05): completa x-permiso en las operaciones del contrato que no lo traen (las
 * AUTENTICADO y PUBLICO, que core-test deja sin x-permiso), igual en openapi.yaml y en el JSON de index.html, sin
 * tocar las que ya lo tienen, y arma la tabla endpoint, permiso y caso de uso. No depende de datos externos.
 */
class CatalogoDePermisosTest {

    private static final String YAML = """
            paths:
              /tarea/uuid/{uuid}:
                get:
                  operationId: "TareaController.obtenerPorUuid"
                  summary: "Obtiene una tarea"
                  x-caso-de-uso: "CU-TAR-01"
                  security:
                  - bearer: []
              /usuario/guardar:
                post:
                  operationId: "UsuarioController.guardar"
                  summary: "Crea o edita un usuario"
                  x-caso-de-uso: "CU-USR-03"
                  x-permiso: "SYSADMIN, ADMIN"
                  security:
                  - bearer: []
              /configuracion/listar:
                get:
                  operationId: "ConfiguracionController.listar"
                  summary: "Lista las configuraciones"
                  x-caso-de-uso: "CU-CON-01"
                  parameters: []
              /usuario/sesion:
                get:
                  operationId: "UsuarioController.sesion"
                  summary: "Usuario de la sesion"
                  x-caso-de-uso: "CU-USR-05"
                  security:
                  - bearer: []
            """;

    private static final Map<String, CatalogoDePermisos.Permiso> DECLARADOS = Map.of(
            "TareaController.obtenerPorUuid", new CatalogoDePermisos.Permiso("VER@TABLERO", "lista solo lo visible"),
            "UsuarioController.sesion", new CatalogoDePermisos.Permiso("AUTENTICADO", ""));

    @Test
    void completaLasOperacionesSinXPermisoYRespetaLasQueLoTienen() {
        String yaml = CatalogoDePermisos.completarYaml(YAML, DECLARADOS);

        assertTrue(yaml.contains("x-caso-de-uso: \"CU-TAR-01\"\n      x-permiso: \"VER@TABLERO\"\n"), yaml);
        assertTrue(yaml.contains("x-caso-de-uso: \"CU-CON-01\"\n      x-permiso: \"PUBLICO\"\n"), yaml);
        assertTrue(yaml.contains("x-caso-de-uso: \"CU-USR-05\"\n      x-permiso: \"AUTENTICADO\"\n"), yaml);
        assertEquals(1, yaml.split("x-permiso: \"SYSADMIN, ADMIN\"", -1).length - 1);
        assertEquals(yaml, CatalogoDePermisos.completarYaml(yaml, DECLARADOS), "Correrlo dos veces no cambia nada");
    }

    @Test
    void elJsonDelIndexQuedaIgualQueElYaml() {
        String html = """
                <script>
                    Redoc.init({
                  "paths" : {
                    "/tarea/uuid/{uuid}" : {
                      "get" : {
                        "operationId" : "TareaController.obtenerPorUuid",
                        "summary" : "Obtiene una tarea",
                        "x-caso-de-uso" : "CU-TAR-01",
                        "security" : [ ]
                """;

        String completo = CatalogoDePermisos.completarHtml(html, Map.of("TareaController.obtenerPorUuid", "VER@TABLERO"));

        assertTrue(completo.contains("\"x-caso-de-uso\" : \"CU-TAR-01\",\n          \"x-permiso\" : \"VER@TABLERO\",\n"), completo);
        assertEquals(completo, CatalogoDePermisos.completarHtml(completo, Map.of("TareaController.obtenerPorUuid", "VER@TABLERO")));
    }

    @Test
    void elCatalogoTieneUnaFilaPorOperacion() {
        String catalogo = CatalogoDePermisos.catalogo(CatalogoDePermisos.completarYaml(YAML, DECLARADOS), DECLARADOS, "1.2.3");

        assertTrue(catalogo.contains("| GET | `/tarea/uuid/{uuid}` | `VER@TABLERO` | CU-TAR-01 | Obtiene una tarea | lista solo lo visible |"), catalogo);
        assertTrue(catalogo.contains("| POST | `/usuario/guardar` | `SYSADMIN, ADMIN` | CU-USR-03 |"), catalogo);
        assertTrue(catalogo.contains("| GET | `/configuracion/listar` | `PUBLICO` | CU-CON-01 |"), catalogo);
        assertTrue(catalogo.contains("versión 1.2.3"), catalogo);
    }

    @Test
    void elPermisoDeUnMetodoSaleDeSuAnotacion() throws Exception {
        assertEquals(new CatalogoDePermisos.Permiso("EDITAR@TABLERO", "y VER en el destino"),
                CatalogoDePermisos.permisoDe(Ejemplo.class.getDeclaredMethod("conRecurso")));
        assertEquals(new CatalogoDePermisos.Permiso("ARCHIVAR@PROYECTO|TABLERO", ""),
                CatalogoDePermisos.permisoDe(Ejemplo.class.getDeclaredMethod("deCualquierRecurso")));
        assertEquals(new CatalogoDePermisos.Permiso("AUTENTICADO", "sus propios datos"),
                CatalogoDePermisos.permisoDe(Ejemplo.class.getDeclaredMethod("conSesion")));
    }

    // Metodos de ejemplo con las anotaciones de MJ-05
    static class Ejemplo {

        @PermisoDeRecurso(nivel = NivelDePermiso.EDITAR, recurso = RecursoDePermiso.TABLERO, detalle = "y VER en el destino")
        void conRecurso() {
        }

        @PermisoDeRecurso(nivel = NivelDePermiso.ARCHIVAR, recurso = RecursoDePermiso.PROYECTO_O_TABLERO)
        void deCualquierRecurso() {
        }

        @SoloSesion("sus propios datos")
        void conSesion() {
        }
    }
}
