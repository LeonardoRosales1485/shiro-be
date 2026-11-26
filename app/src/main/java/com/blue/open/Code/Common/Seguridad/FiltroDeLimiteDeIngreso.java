package com.blue.open.Code.Common.Seguridad;

import com.blue.open.Code.Common.Errores.RespuestaDeErrorDeFiltro;
import com.blue.open.Core.Identity.Excepciones.CodigoDeErrorIdentity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Filtro del limite de intentos de ingreso (MJ-04). Mira los pedidos que prueban credenciales: POST
 * /usuario/iniciar-sesion (el usuario sale del cuerpo), cualquier pedido con Authorization: Basic (el usuario sale del
 * encabezado) y /mcp (solo la IP). Si el usuario o la IP estan bloqueados responde 429 DEMASIADOS_INTENTOS sin dejar
 * pasar el pedido, aunque la clave sea correcta. Si no, deja pasar y cuenta un 401 como fallo y un 2xx como ingreso
 * correcto. La IP es la de la conexion, salvo que llegue desde una IP local (el nginx del servidor): entonces es la
 * de X-Real-IP. Lo registra LimiteDeIngresoConfig antes que el resto de los filtros.
 * Dependencias externas: ninguna.
 */
@Slf4j
@RequiredArgsConstructor
public class FiltroDeLimiteDeIngreso extends OncePerRequestFilter {

    private static final String INICIAR_SESION = "/usuario/iniciar-sesion";

    private static final String MCP = "/mcp";

    private static final String BASIC = "Basic ";

    // Cuerpo mas largo que se lee para sacar el usuario de iniciar sesion
    private static final int MAXIMO_DEL_CUERPO = 16 * 1024;

    private final LimiteDeIntentosDeIngreso limite;

    private final ObjectMapper lector = new ObjectMapper();

    // Bloquea, deja pasar y cuenta, segun el tipo de pedido
    @Override
    protected void doFilterInternal(HttpServletRequest pedido, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String ruta = pedido.getRequestURI().substring(pedido.getContextPath().length());
        String autorizacion = pedido.getHeader("Authorization");
        boolean iniciarSesion = "POST".equalsIgnoreCase(pedido.getMethod()) && ruta.equals(INICIAR_SESION);
        boolean basic = autorizacion != null && autorizacion.startsWith(BASIC);
        boolean mcp = ruta.equals(MCP) || ruta.startsWith(MCP + "/");
        if (!iniciarSesion && !basic && !mcp) {
            cadena.doFilter(pedido, respuesta);
            return;
        }
        HttpServletRequest aPasar = pedido;
        String usuario = null;
        if (iniciarSesion) {
            PeticionConCuerpoLeido conCuerpo = new PeticionConCuerpoLeido(pedido, MAXIMO_DEL_CUERPO);
            aPasar = conCuerpo;
            usuario = usuarioDelCuerpo(conCuerpo.cuerpo());
        } else if (basic) {
            usuario = usuarioDelBasic(autorizacion.substring(BASIC.length()));
        }
        String ip = ipDelCliente(pedido);
        String motivo = limite.motivoDeBloqueo(usuario, ip);
        if (motivo != null) {
            log.warn("Ingreso bloqueado por demasiados intentos | motivo={} | ip={} | ruta={}", motivo, ip, ruta);
            rechazar(respuesta);
            return;
        }
        cadena.doFilter(aPasar, respuesta);
        int status = respuesta.getStatus();
        if (status == HttpStatus.UNAUTHORIZED.value())
            limite.registrarFallo(usuario, ip);
        else if (status >= 200 && status < 300 && usuario != null)
            limite.registrarExito(usuario);
    }

    // Nombre de usuario del cuerpo de iniciar sesion, o null si no es un JSON con nombreDeUsuario
    private String usuarioDelCuerpo(byte[] cuerpo) {
        if (cuerpo == null || cuerpo.length == 0)
            return null;
        try {
            JsonNode json = lector.readTree(cuerpo);
            JsonNode nombre = json == null ? null : json.get("nombreDeUsuario");
            return nombre == null || !nombre.isString() ? null : nombre.asString();
        } catch (RuntimeException e) {
            // Un cuerpo que no es JSON lo rechaza la validacion del endpoint; aca solo no hay usuario que contar
            return null;
        }
    }

    // Nombre de usuario de un encabezado Basic (base64 de usuario:clave), o null si no se puede leer
    private static String usuarioDelBasic(String credenciales) {
        try {
            String decodificadas = new String(Base64.getDecoder().decode(credenciales.trim()), StandardCharsets.UTF_8);
            int separador = decodificadas.indexOf(':');
            return separador <= 0 ? null : decodificadas.substring(0, separador);
        } catch (IllegalArgumentException e) {
            // Base64 invalido: el Core responde 401 y el fallo cuenta solo para la IP
            return null;
        }
    }

    // IP del cliente: X-Real-IP si el pedido llega desde una IP local (el nginx del servidor), si no la de la conexion
    static String ipDelCliente(HttpServletRequest pedido) {
        String remota = pedido.getRemoteAddr();
        String real = pedido.getHeader("X-Real-IP");
        if (real != null && !real.isBlank() && esLocal(remota))
            return real.trim();
        return remota;
    }

    // Si la direccion es de loopback o de una red privada. Solo acepta direcciones literales, sin resolver nombres
    private static boolean esLocal(String direccion) {
        if (direccion == null || !direccion.matches("[0-9a-fA-F:.]+"))
            return false;
        try {
            InetAddress ip = InetAddress.getByName(direccion);
            return ip.isLoopbackAddress() || ip.isSiteLocalAddress() || ip.isLinkLocalAddress()
                    || (ip.getAddress().length == 16 && (ip.getAddress()[0] & 0xfe) == 0xfc);
        } catch (UnknownHostException e) {
            // No pasa con una direccion literal; si pasara, no se confia en X-Real-IP
            return false;
        }
    }

    // Escribe el 429 con la misma forma que el resto de la API. El codigo es el del Core (core-identity), que ya publica
    // DEMASIADOS_INTENTOS con 429
    private static void rechazar(HttpServletResponse respuesta) throws IOException {
        CodigoDeErrorIdentity codigo = CodigoDeErrorIdentity.DEMASIADOS_INTENTOS;
        RespuestaDeErrorDeFiltro.escribir(respuesta, codigo, codigo.mensajePorDefecto());
    }
}
