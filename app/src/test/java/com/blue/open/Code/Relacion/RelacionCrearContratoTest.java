package com.blue.open.Code.Relacion;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CU-REL-01 y CU-TAR-08: relacionar dos tareas (POST /relacion/crear) y leer las relaciones de una tarea
 * (GET /tarea/{uuid}/relaciones). T1.9 del plan y seccion 4 del DAF: se guarda un sentido y la relacion se lee
 * desde las dos tareas, con el nombre del sentido de cada una; con una tarea archivada el vinculo queda y la muestra
 * archivada.
 */
class RelacionCrearContratoTest extends PruebaDeContrato {

    private static final String ADMIN = "ADMIN";

    private static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    @Test
    void laRelacionSeLeeDesdeLasDosTareas() {
        JsonNode bloqueante = tarea("Migrar la base");
        JsonNode bloqueada = tarea("Publicar la versión");

        crear(bloqueante, bloqueada, "BLOQUEA").esperar(200);

        JsonNode desdeOrigen = relaciones(bloqueante).get(0);
        assertEquals("BLOQUEA", desdeOrigen.get("vinculo").asString());
        assertEquals(bloqueada.get("clave").asString(), desdeOrigen.get("tareaClave").asString());
        assertEquals("Por hacer", desdeOrigen.get("tareaEstadoNombre").asString());
        JsonNode desdeDestino = relaciones(bloqueada).get(0);
        assertEquals("BLOQUEADA_POR", desdeDestino.get("vinculo").asString());
        assertEquals(bloqueante.get("clave").asString(), desdeDestino.get("tareaClave").asString());
        JsonNode historial = como(ADMIN).get("/tarea/{uuid}/historial", uuid(bloqueada)).ejecutar().esperar(200).json();
        assertEquals("RELACIONAR", historial.get(0).get("accion").asString());
        assertEquals("bloqueada por " + bloqueante.get("clave").asString(), historial.get(0).get("valorNuevo").asString());
    }

    @Test
    void conUnaTareaArchivadaElVinculoQuedaYLaMuestraArchivada() {
        JsonNode original = tarea("Original");
        JsonNode duplicada = tarea("Duplicada");
        crear(duplicada, original, "DUPLICA").esperar(200);
        como(ADMIN).post("/tarea/{uuid}/archivar", uuid(duplicada)).ejecutar().esperar(200);

        JsonNode relacion = relaciones(original).get(0);

        assertEquals("DUPLICADA_POR", relacion.get("vinculo").asString());
        assertTrue(relacion.get("tareaArchivada").asBoolean());
    }

    @Test
    void unaTareaNoSeRelacionaConsigoMisma() {
        JsonNode una = tarea("Sola");

        crear(una, una, "RELACIONA").esperarError(400, "RELACION_CONSIGO_MISMA");
    }

    @Test
    void laMismaRelacionNoSeRepiteEnNingunSentido() {
        JsonNode a = tarea("A");
        JsonNode b = tarea("B");
        crear(a, b, "RELACIONA").esperar(200);

        crear(a, b, "RELACIONA").esperarError(400, "RELACION_YA_EXISTE");
        crear(b, a, "RELACIONA").esperarError(400, "RELACION_YA_EXISTE");
    }

    @Test
    void laRelacionDeClonSoloLaCreaClonar() {
        crear(tarea("A"), tarea("B"), "CLONA").esperarError(400, "TIPO_DE_RELACION_NO_PERMITIDO");
    }

    @Test
    void tareaInexistenteRechazada() {
        como(ADMIN).post("/relacion/crear").json(Map.of("origenUuid", uuid(tarea("A")),
                "destinoUuid", "ffffffff-ffff-4fff-bfff-ffffffffffff", "tipo", "RELACIONA")).ejecutar()
                .esperarError(400, "RELACION_NO_ENCONTRADA");
    }

    @Test
    void sinTipoRechazado() {
        como(ADMIN).post("/relacion/crear").json(Map.of("origenUuid", uuid(tarea("A")), "destinoUuid",
                uuid(tarea("B")))).ejecutar().esperarError(400, "FORMULARIO_INVALIDO");
    }

    @Test
    void relacionesDeUnaTareaInexistente() {
        como(ADMIN).get("/tarea/{uuid}/relaciones", "ffffffff-ffff-4fff-bfff-ffffffffffff").ejecutar()
                .esperarError(400, "REGISTRO_NO_ENCONTRADO");
    }

    // Crea una tarea en el tablero principal de Shiro (paso de preparacion)
    private JsonNode tarea(String titulo) {
        return como(ADMIN).post("/tarea/guardar").json(Map.of("tableroUuid", TABLERO_PRINCIPAL, "tipoCodigo", "TAREA",
                "titulo", titulo)).preparacion().ejecutar().esperar(200).json();
    }

    // POST /relacion/crear como Administrador
    private Respuesta crear(JsonNode origen, JsonNode destino, String tipo) {
        return como(ADMIN).post("/relacion/crear").json(Map.of("origenUuid", uuid(origen), "destinoUuid", uuid(destino),
                "tipo", tipo)).ejecutar();
    }

    // GET /tarea/{uuid}/relaciones
    private JsonNode relaciones(JsonNode tarea) {
        return como(ADMIN).get("/tarea/{uuid}/relaciones", uuid(tarea)).ejecutar().esperar(200).json();
    }

    private static String uuid(JsonNode tarea) {
        return tarea.get("uuid").asString();
    }
}
