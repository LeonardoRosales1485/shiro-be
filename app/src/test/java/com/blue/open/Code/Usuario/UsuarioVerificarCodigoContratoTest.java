package com.blue.open.Code.Usuario;

import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CU-USR-06: verificar el codigo de confirmacion de un usuario (POST /usuario/publico/verificar, publico). Migrado de
 * bruno/User (14 Public Verify y Public Verify - codigo invalido). Los usuarios pendientes (status -2) con un codigo
 * conocido, vigente y vencido, salen de 2026-10-09-insert-usuarios-seed.yaml.
 */
class UsuarioVerificarCodigoContratoTest extends PruebaDeContrato {

    @Test
    void activaUnUsuarioPendiente() {
        verificar(Map.of("uuid", UsuariosDeSeed.PENDIENTE, "codigo", UsuariosDeSeed.CODIGO_PENDIENTE)).esperar(200);

        // Ya activo: un segundo intento responde 200 sin validar el codigo
        verificar(Map.of("uuid", UsuariosDeSeed.PENDIENTE, "codigo", "000000")).esperar(200);
    }

    @Test
    void codigoInvalido() {
        JsonNode error = verificar(Map.of("uuid", UsuariosDeSeed.PENDIENTE, "codigo", "000000"))
                .esperarError(400, "CODIGO_INVALIDO").json();

        assertEquals("Código inválido", error.get("mensajeParaUsuario").asString());
    }

    @Test
    void codigoVencido() {
        verificar(Map.of("uuid", UsuariosDeSeed.PENDIENTE_VENCIDO, "codigo", UsuariosDeSeed.CODIGO_PENDIENTE))
                .esperarError(400, "CODIGO_VENCIDO");
    }

    @Test
    void usuarioYaActivo() {
        verificar(Map.of("uuid", UsuariosDeSeed.DEMO, "codigo", "000000")).esperar(200);
    }

    @Test
    void sinCodigo() {
        JsonNode error = verificar(Map.of("uuid", UsuariosDeSeed.DEMO)).esperarError(400, "FORMULARIO_INVALIDO").json();

        assertEquals("codigo", error.get("errores").get(0).get("nombre").asString());
        assertEquals("El codigo es obligatorio", error.get("errores").get(0).get("mensaje").asString());
    }

    @Test
    void sinUuid() {
        JsonNode error = verificar(Map.of("codigo", "000000")).esperarError(400, "FORMULARIO_INVALIDO").json();

        assertEquals("uuid", error.get("errores").get(0).get("nombre").asString());
        assertEquals("El uuid del usuario es obligatorio", error.get("errores").get(0).get("mensaje").asString());
    }

    @Test
    void usuarioInexistente() {
        JsonNode error = verificar(Map.of("uuid", UsuariosDeSeed.INEXISTENTE, "codigo", "000000"))
                .esperarError(400, "REGISTRO_NO_ENCONTRADO").json();

        assertEquals("Usuario no encontrado", error.get("mensajeParaUsuario").asString());
    }

    // La ruta en ingles se renombro a la del glosario (ticket 06): ya no existe
    @Test
    void rutaViejaNoExiste() {
        sinAutenticacion().post("/usuario/public/verify").json(Map.of("uuid", UsuariosDeSeed.PENDIENTE, "code", UsuariosDeSeed.CODIGO_PENDIENTE)).ejecutar().esperar(404);
    }

    // POST /usuario/publico/verificar sin autenticacion
    private Respuesta verificar(Map<String, Object> cuerpo) {
        return sinAutenticacion().post("/usuario/publico/verificar").json(cuerpo)
                .ejecutar();
    }
}
