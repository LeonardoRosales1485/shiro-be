package com.blue.open;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que la base de los tests ({@link TestConBaseDeDatos}) es la que arma Liquibase: el contexto levanta con
 * ddl-auto=validate (las entidades coinciden con el schema), la base esta en UTC-3 y se cargan los seeds.
 */
class BaseDeDatosDeTestTest extends TestConBaseDeDatos {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void laBaseEsPostgreSql18() {
        String version = jdbcTemplate.queryForObject("SHOW server_version", String.class);

        assertTrue(version.startsWith("18."), "Version de PostgreSQL: " + version);
    }

    @Test
    void laBaseEstaEnUtcMenos3() {
        Integer offsetSegundos = jdbcTemplate.queryForObject("SELECT EXTRACT(TIMEZONE FROM NOW())::int", Integer.class);

        assertEquals(-3 * 3600, offsetSegundos);
    }

    @Test
    void seCarganLosSeeds() {
        Integer usuariosDemo = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM auth_usuarios WHERE nombre_de_usuario = 'demo'", Integer.class);

        assertEquals(1, usuariosDemo);
    }
}
