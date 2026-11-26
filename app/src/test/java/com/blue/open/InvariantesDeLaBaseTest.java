package com.blue.open;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MJ-03 del plan de mejoras: las invariantes de la tarea (Spec 1, Spec 7 y adenda 4 del DAF) las garantiza tambien la
 * base, no solo Java, para que una migracion o un import con un error no las rompa. Cada prueba intenta violar una por
 * SQL directo y espera el error de la base:
 * - la tarea es del proyecto de su tablero (FK compuesta fk_shr_tarea_tablero_proyecto);
 * - el estado es del tipo de tablero de su tablero (trigger tg_shr_tarea_estado_del_tablero);
 * - la jerarquia: el padre es otra tarea del mismo proyecto y de un nivel menor, y cambiar el tipo no deja hijas
 *   vigentes de nivel menor o igual (trigger tg_shr_tarea_jerarquia);
 * - una Subtarea tiene padre (trigger diferido tg_shr_tarea_subtarea_con_padre, que la prueba pasa a inmediato).
 * Corre dentro de la transaccion del test: todo se revierte al terminar.
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
class InvariantesDeLaBaseTest extends TestConBaseDeDatos {

    // SQLSTATE de PostgreSQL: violacion de FK y de CHECK (los triggers usan check_violation)
    private static final String FK = "23503";

    private static final String CHECK = "23514";

    private static final String SHI_1 = "b3000000-0000-4000-8000-000000000001";

    @Autowired
    private JdbcTemplate jdbc;

    // Id de SHI-1 (una Tarea del tablero principal de Shiro)
    private long shi1;

    @BeforeEach
    void tareaDeSeed() {
        shi1 = jdbc.queryForObject("SELECT id FROM shr_tarea WHERE uuid = ?", Long.class, SHI_1);
    }

    @Test
    void laTareaEsDelProyectoDeSuTablero() {
        long proyectoDeShi1 = jdbc.queryForObject("SELECT id_proyecto FROM shr_tarea WHERE id = ?", Long.class, shi1);
        long otroProyecto = copiarFila("shr_proyecto", proyectoDeShi1, Map.of("prefijo", "INVA", "nombre", "Otro proyecto"));

        esperarRechazo(FK, "fk_shr_tarea_tablero_proyecto",
                "UPDATE shr_tarea SET id_proyecto = ? WHERE id = ?", otroProyecto, shi1);
    }

    @Test
    void elEstadoEsDelTipoDeSuTablero() {
        long estadoDeQa = jdbc.queryForObject("SELECT e.id FROM shr_estado e JOIN shr_tipo_tablero t ON t.id = e.id_tipo_tablero"
                + " WHERE t.codigo = 'QA' ORDER BY e.orden LIMIT 1", Long.class);

        esperarRechazo(CHECK, "estado", "UPDATE shr_tarea SET id_estado = ? WHERE id = ?", estadoDeQa, shi1);
    }

    @Test
    void unaTareaNoEsSuPropioPadre() {
        esperarRechazo(CHECK, "padre", "UPDATE shr_tarea SET id_padre = id WHERE id = ?", shi1);
    }

    @Test
    void elPadreEsDeUnNivelMenor() {
        long otraTarea = copia("TAREA", null);

        esperarRechazo(CHECK, "nivel", "UPDATE shr_tarea SET id_padre = ? WHERE id = ?", otraTarea, shi1);
    }

    @Test
    void elPadreEsDelMismoProyecto() {
        long proyectoDeShi1 = jdbc.queryForObject("SELECT id_proyecto FROM shr_tarea WHERE id = ?", Long.class, shi1);
        long tableroDeShi1 = jdbc.queryForObject("SELECT id_tablero FROM shr_tarea WHERE id = ?", Long.class, shi1);
        long otroProyecto = copiarFila("shr_proyecto", proyectoDeShi1, Map.of("prefijo", "INVB", "nombre", "Otro proyecto"));
        long tableroDeOtroProyecto = copiarFila("shr_tablero", tableroDeShi1, Map.of("id_proyecto", otroProyecto));
        long epicaDeOtroProyecto = copiarFila("shr_tarea", shi1, Map.of("id_tipo_tarea", tipo("EPICA"), "id_proyecto", otroProyecto,
                "id_tablero", tableroDeOtroProyecto, "numero", 1L, "clave", "INVB-1"));

        esperarRechazo(CHECK, "proyecto", "UPDATE shr_tarea SET id_padre = ? WHERE id = ?", epicaDeOtroProyecto, shi1);
    }

    @Test
    void unaSubtareaTienePadre() {
        long subtarea = tipo("SUBTAREA");
        // Esta regla se verifica al confirmar (el importador asigna el padre en una segunda pasada): se pasa a inmediata
        jdbc.execute("SET CONSTRAINTS tg_shr_tarea_subtarea_con_padre IMMEDIATE");

        esperarRechazo(CHECK, "Subtarea", "UPDATE shr_tarea SET id_tipo_tarea = ?, id_padre = NULL WHERE id = ?", subtarea, shi1);
    }

    @Test
    void cambiarElTipoNoDejaHijasDeNivelMenorOIgual() {
        long epica = copia("EPICA", null);
        jdbc.update("UPDATE shr_tarea SET id_padre = ? WHERE id = ?", epica, shi1);

        esperarRechazo(CHECK, "hijas", "UPDATE shr_tarea SET id_tipo_tarea = ? WHERE id = ?", tipo("HISTORIA"), epica);
    }

    @Test
    void lasTareasValidasSeGuardan() {
        long epica = copia("EPICA", null);
        long historia = copia("HISTORIA", epica);
        long subtarea = copia("SUBTAREA", historia);

        jdbc.update("UPDATE shr_tarea SET id_padre = ? WHERE id = ?", epica, shi1);
        jdbc.update("UPDATE shr_tarea SET titulo = 'Editada' WHERE id = ?", subtarea);
        assertEquals(historia, jdbc.queryForObject("SELECT id_padre FROM shr_tarea WHERE id = ?", Long.class, subtarea));
    }

    // Ejecuta la sentencia y exige que la base la rechace con el SQLSTATE y un mensaje que mencione el texto
    private void esperarRechazo(String sqlState, String texto, String sql, Object... parametros) {
        DataAccessException error = assertThrows(DataAccessException.class, () -> jdbc.update(sql, parametros));
        SQLException causa = assertInstanceOf(SQLException.class, NestedExceptionUtils.getMostSpecificCause(error), error::toString);
        assertEquals(sqlState, causa.getSQLState(), causa::getMessage);
        assertTrue(causa.getMessage().contains(texto), causa::getMessage);
    }

    // Id del tipo de tarea con el codigo recibido
    private long tipo(String codigo) {
        return jdbc.queryForObject("SELECT id FROM shr_tipo_tarea WHERE codigo = ?", Long.class, codigo);
    }

    // Copia SHI-1 como una tarea nueva del tipo y con el padre recibidos, y responde su id
    private long copia(String tipoCodigo, Long padre) {
        long numero = jdbc.queryForObject("SELECT COALESCE(MAX(numero), 0) + 1000 FROM shr_tarea", Long.class);
        Map<String, Object> valores = new HashMap<>(Map.of("numero", numero, "clave", "INV-" + numero,
                "id_tipo_tarea", tipo(tipoCodigo)));
        valores.put("id_padre", padre);
        return copiarFila("shr_tarea", shi1, valores);
    }

    // Copia la fila de la tabla con un uuid nuevo y los valores recibidos en lugar de los originales; responde el id
    private long copiarFila(String tabla, long id, Map<String, Object> valores) {
        List<String> copiadas = new ArrayList<>(jdbc.queryForList("SELECT column_name FROM information_schema.columns"
                + " WHERE table_name = ? AND is_generated = 'NEVER' AND column_name NOT IN ('id', 'uuid') ORDER BY ordinal_position",
                String.class, tabla));
        copiadas.removeAll(valores.keySet());
        List<String> reemplazadas = new ArrayList<>(valores.keySet());
        String uuid = UUID.randomUUID().toString();
        List<Object> parametros = new ArrayList<>(List.of(uuid));
        reemplazadas.forEach(columna -> parametros.add(valores.get(columna)));
        parametros.add(id);
        String marcas = String.join("", Collections.nCopies(reemplazadas.size(), ", ?"));
        String columnasCopiadas = copiadas.isEmpty() ? "" : ", " + String.join(", ", copiadas);
        jdbc.update("INSERT INTO " + tabla + " (uuid" + (reemplazadas.isEmpty() ? "" : ", " + String.join(", ", reemplazadas))
                + columnasCopiadas + ") SELECT ?" + marcas + columnasCopiadas + " FROM " + tabla + " WHERE id = ?",
                parametros.toArray());
        return jdbc.queryForObject("SELECT id FROM " + tabla + " WHERE uuid = ?", Long.class, uuid);
    }
}
