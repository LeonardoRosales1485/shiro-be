package com.blue.open.Code.Common.Errores;

import com.blue.open.Core.Kernel.Excepciones.CodigoDeError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Escribe una respuesta de error desde un filtro de servlet, que corta el pedido antes de llegar al handler de
 * excepciones del Core: misma forma que el resto de la API (codigo, mensajeParaUsuario y status). La usan FiltroDeMcp
 * y FiltroDeLimiteDeIngreso.
 * Dependencias externas: ninguna.
 */
public final class RespuestaDeErrorDeFiltro {

    private static final ObjectMapper ESCRITOR = new ObjectMapper();

    private RespuestaDeErrorDeFiltro() {
    }

    // Escribe el status del codigo y el cuerpo JSON con el codigo y el mensaje para el usuario recibido
    public static void escribir(HttpServletResponse respuesta, CodigoDeError codigo, String mensajeParaUsuario) throws IOException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("codigo", codigo.codigo());
        cuerpo.put("mensajeParaUsuario", mensajeParaUsuario);
        cuerpo.put("status", codigo.status().toString());
        respuesta.setStatus(codigo.status().value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write(ESCRITOR.writeValueAsString(cuerpo));
    }
}
