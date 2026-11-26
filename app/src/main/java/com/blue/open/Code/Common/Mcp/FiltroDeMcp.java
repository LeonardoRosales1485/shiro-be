package com.blue.open.Code.Common.Mcp;

import com.blue.open.Code.Common.Canal.CanalDeCambio;
import com.blue.open.Code.Common.Canal.CanalDeLaPeticion;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Common.Errores.RespuestaDeErrorDeFiltro;
import com.blue.open.Code.Usuario.AuthTokenConSha256Service;
import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeError;
import com.blue.open.Core.Kernel.Excepciones.CodigoDeErrorKernel;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de /mcp (T3.1 y Spec 2): exige Authorization: Bearer con un token de acceso y lo autentica con el protocolo
 * "tokens" (AuthTokenConSha256Service). Sin token valido responde 401 antes de llegar a las herramientas. Marca el
 * request con canal MCP, para que el historial y los comentarios lo registren. Lo registra FiltroDeMcpConfig solo
 * para /mcp. Cada token puede hacer hasta 120 llamadas por minuto (MJ-13, decision D-3, LimiteDeLlamadasMcp); la
 * siguiente responde 429 DEMASIADAS_LLAMADAS_MCP con Retry-After.
 * Dependencias externas: la base de datos (tokens de acceso).
 */
@Slf4j
@RequiredArgsConstructor
public class FiltroDeMcp extends OncePerRequestFilter {

    private final AuthTokenConSha256Service tokens;

    private final LimiteDeLlamadasMcp limite;

    // Autentica el token y marca el canal MCP; si no autentica responde 401 con el codigo de error
    @Override
    protected void doFilterInternal(HttpServletRequest pedido, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = pedido.getHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith("Bearer ") || encabezado.split("\\.", -1).length == 3) {
            rechazar(respuesta, CodigoDeErrorKernel.NO_AUTENTICADO, "MCP necesita un token de acceso personal en Authorization: Bearer.");
            return;
        }
        try {
            UsuarioBase usuario = tokens.getUsuario();
            if (usuario == null || usuario.getStatus() == null || usuario.getStatus() != 0) {
                rechazar(respuesta, CodigoDeErrorKernel.NO_AUTENTICADO, "MCP necesita un token de acceso personal en Authorization: Bearer.");
                return;
            }
        } catch (ExcepcionDeApi e) {
            rechazar(respuesta, e.getCodigo(), e.getMessage());
            return;
        }
        String token = encabezado.substring("Bearer ".length()).trim();
        if (!limite.admitir(AuthTokenConSha256Service.sha256(token))) {
            respuesta.setHeader("Retry-After", String.valueOf(LimiteDeLlamadasMcp.UN_MINUTO.toSeconds()));
            rechazar(respuesta, CodigoDeErrorDelProyecto.DEMASIADAS_LLAMADAS_MCP, CodigoDeErrorDelProyecto.DEMASIADAS_LLAMADAS_MCP.mensajePorDefecto());
            return;
        }
        pedido.setAttribute(CanalDeLaPeticion.ATRIBUTO, CanalDeCambio.MCP);
        cadena.doFilter(pedido, respuesta);
    }

    // Escribe la respuesta de error con la misma forma que el resto de la API (codigo, mensaje y status)
    private void rechazar(HttpServletResponse respuesta, CodigoDeError codigo, String mensaje) throws IOException {
        log.warn("Pedido MCP rechazado | codigo={}", codigo.codigo());
        RespuestaDeErrorDeFiltro.escribir(respuesta, codigo, mensaje);
    }
}
