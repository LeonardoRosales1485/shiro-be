package com.blue.open.Code.Proyecto;

import com.blue.open.Code.Historial.Historial;
import com.blue.open.Code.Historial.HistorialRepository;
import com.blue.open.Core.Identity.Auth.RolBaseEnum;
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
 * CU-PRY-03: crear o editar un proyecto (POST /proyecto/guardar). T1.2 del plan y adenda 3 del DAF: al crear se generan
 * los cuatro tableros; el prefijo tiene de 2 a 6 letras, se guarda en mayusculas, no se repite (tampoco con un proyecto
 * archivado) y no se cambia despues de crear el proyecto. Crean y editan el super admin y los Administradores.
 */
class ProyectoGuardarContratoTest extends PruebaDeContrato {

    @Autowired
    private HistorialRepository historialRepository;

    @Test
    void alCrearSeGeneranLosCuatroTableros() {
        JsonNode proyecto = guardar(RolBaseEnum.SYSADMIN.name(), alta("Portal de Trámites", "PTR")).esperar(200).json();

        assertEquals("PTR", proyecto.get("prefijo").asString());
        assertEquals(List.of("PRINCIPAL", "QA", "ANALISIS", "DISENO"), codigosDeTableros(proyecto));
    }

    @Test
    void unAdministradorTambienCreaProyectos() {
        JsonNode proyecto = guardar(RolBaseEnum.ADMIN.name(), alta("Mesa de Ayuda", "MDA")).esperar(200).json();

        assertEquals("MDA", proyecto.get("prefijo").asString());
    }

    @Test
    void elPrefijoSeGuardaEnMayusculas() {
        JsonNode proyecto = guardar(RolBaseEnum.SYSADMIN.name(), alta("Recursos Humanos", "rrhh")).esperar(200).json();

        assertEquals("RRHH", proyecto.get("prefijo").asString());
    }

    @Test
    void prefijoRepetidoRechazado() {
        JsonNode error = guardar(RolBaseEnum.SYSADMIN.name(), alta("Otro Shiro", "shi"))
                .esperarError(400, "PREFIJO_DE_PROYECTO_EN_USO").json();

        assertEquals("El prefijo SHI ya lo usa otro proyecto. Elegí otro.", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void elPrefijoDeUnProyectoArchivadoQuedaReservado() {
        guardar(RolBaseEnum.SYSADMIN.name(), alta("Nuevo", ProyectosDeSeed.ARCHIVADO_PREFIJO))
                .esperarError(400, "PREFIJO_DE_PROYECTO_EN_USO");
    }

    @Test
    void prefijoConFormatoInvalidoRechazado() {
        for (String prefijo : List.of("P", "PROYEC1", "P2", "ABCDEFG", "SH-I")) {
            guardar(RolBaseEnum.SYSADMIN.name(), alta("Proyecto", prefijo)).esperarError(400, "FORMULARIO_INVALIDO");
        }
    }

    @Test
    void editarCambiaElNombreYConservaPrefijoYTableros() {
        JsonNode proyecto = guardar(RolBaseEnum.SYSADMIN.name(), Map.of("uuid", ProyectosDeSeed.SHIRO,
                "nombre", "Shiro (gestor de trabajo)", "prefijo", ProyectosDeSeed.SHIRO_PREFIJO)).esperar(200).json();

        assertEquals("Shiro (gestor de trabajo)", proyecto.get("nombre").asString());
        assertEquals(ProyectosDeSeed.SHIRO_PREFIJO, proyecto.get("prefijo").asString());
        assertEquals(List.of("PRINCIPAL", "QA", "ANALISIS", "DISENO"), codigosDeTableros(proyecto));
    }

    @Test
    void elPrefijoNoSeCambia() {
        JsonNode error = guardar(RolBaseEnum.SYSADMIN.name(), Map.of("uuid", ProyectosDeSeed.SHIRO,
                "nombre", "Shiro", "prefijo", "SHR")).esperarError(400, "PREFIJO_DE_PROYECTO_NO_SE_CAMBIA").json();

        assertEquals("El prefijo no se cambia: lo usan las claves de las tareas existentes.",
                error.get("mensajeParaUsuario").asString());
    }

    @Test
    void crearYEditarDejanSuFilaEnElHistorial() {
        String uuid = guardar(RolBaseEnum.SYSADMIN.name(), alta("Historial", "HIS")).esperar(200).json()
                .get("uuid").asString();
        guardar(RolBaseEnum.SYSADMIN.name(), Map.of("uuid", uuid, "nombre", "Historial editado", "prefijo", "HIS"))
                .esperar(200);

        List<String> filas = new ArrayList<>();
        for (Historial fila : historialRepository.findByEntidadAndUuidEntidadOrderByIdAsc("PROYECTO", uuid)) {
            filas.add(fila.getAccion() + " " + fila.getCampo() + " " + fila.getValorNuevo());
        }

        assertEquals(List.of("CREAR null HIS", "EDITAR nombre Historial editado"), filas);
    }

    // POST /proyecto/guardar como el actor del rol
    private Respuesta guardar(String rol, Map<String, Object> cuerpo) {
        return como(rol).post("/proyecto/guardar").json(cuerpo).ejecutar();
    }

    // Body de alta con nombre y prefijo
    private static Map<String, Object> alta(String nombre, String prefijo) {
        return Map.of("nombre", nombre, "prefijo", prefijo);
    }

    // Codigos de los tipos de tablero del proyecto, en el orden en que llegan
    private static List<String> codigosDeTableros(JsonNode proyecto) {
        List<String> codigos = new ArrayList<>();
        proyecto.get("tableros").forEach(tablero -> codigos.add(tablero.get("tipoTableroCodigo").asString()));
        return codigos;
    }
}
