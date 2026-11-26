package com.blue.open;

import com.blue.open.Core.Test.PruebaDeContrato;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MJ-14 del plan de mejoras y decision D-3: un token de acceso vencido deja de autenticar (401 TOKEN_VENCIDO), en la
 * API y en /mcp (FiltroDeMcp usa la misma autenticacion, AuthTokenConSha256Service.getUsuario). Vive en la raiz de las pruebas porque mueve el vencimiento por SQL, y TST-04 no permite SQL en las
 * pruebas de Code/. Corre dentro de la transaccion del test: todo se revierte al terminar.
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
class TokenVencidoContratoTest extends PruebaDeContrato {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void unTokenVencidoRespondeTokenVencido() {
        JsonNode token = como("ADMIN").post("/usuario/crear-token-con-vencimiento").json(Map.of("nombre", "Por vencer", "diasDeVigencia", 30))
                .preparacion().ejecutar().esperar(200).json();
        int cambiadas = jdbc.update("UPDATE shr_vencimiento_de_token SET vence = CURRENT_TIMESTAMP - INTERVAL '1 minute' "
                + "WHERE id_access_token = (SELECT id FROM auth_access_tokens WHERE uuid = ?)", token.get("uuid").asString());
        assertEquals(1, cambiadas);
        // El vencimiento creado sigue en la sesion de JPA de la transaccion del test: se descarta para leer el de la base
        entityManager.clear();

        sinAutenticacion().get("/usuario/sesion").encabezado("Authorization", "Bearer " + token.get("codigo").asString())
                .ejecutar().esperarError(401, "TOKEN_VENCIDO");
    }

    @Test
    void losTokensQueYaExistianVencenALosNoventaDias() {
        Integer sinVencimiento = jdbc.queryForObject("SELECT COUNT(*) FROM auth_access_tokens t WHERE NOT EXISTS "
                + "(SELECT 1 FROM shr_vencimiento_de_token v WHERE v.id_access_token = t.id)", Integer.class);
        assertEquals(0, sinVencimiento);
    }
}
