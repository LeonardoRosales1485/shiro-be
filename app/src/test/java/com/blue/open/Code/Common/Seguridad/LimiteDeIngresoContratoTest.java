package com.blue.open.Code.Common.Seguridad;

import com.blue.open.Code.Usuario.EscenarioDeUsuario;
import com.blue.open.Core.Test.Peticion;
import com.blue.open.Core.Test.PruebaDeContrato;
import com.blue.open.Core.Test.Respuesta;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * MJ-04 del plan de mejoras y decision D-2: limite de intentos de ingreso. Cinco fallos del mismo usuario en 15
 * minutos lo bloquean 15 minutos (429 DEMASIADOS_INTENTOS aun con la clave correcta); veinte fallos desde la misma IP
 * bloquean la IP; un Basic fallido cuenta igual que un ingreso fallido; un token invalido en /mcp cuenta para la IP.
 * Un ingreso correcto reinicia la cuenta del usuario. Cada prueba usa su propia IP (X-Real-IP, que se acepta porque el
 * pedido llega desde una IP local, como desde el nginx del servidor) y sus propios usuarios.
 * Que pasada la ventana se vuelve a poder lo prueba ContadorDeIntentosTest con un reloj manual.
 * Dependencias externas: ninguna.
 */
class LimiteDeIngresoContratoTest extends PruebaDeContrato {

    private static final String CLAVE_INCORRECTA = "clave-incorrecta";

    @Test
    void cincoFallosDelMismoUsuarioLoBloqueanAunConLaClaveCorrecta() {
        String usuario = crear("limite.usuario");
        for (int i = 0; i < 5; i++) {
            ingresar("10.66.0.1", usuario, CLAVE_INCORRECTA).preparacion().ejecutar().esperarError(401, "CREDENCIALES_INVALIDAS");
        }

        ingresar("10.66.0.1", usuario, EscenarioDeUsuario.CONTRASENA).ejecutar().esperarError(429, "DEMASIADOS_INTENTOS");
        ingresar("10.66.0.2", usuario, EscenarioDeUsuario.CONTRASENA).preparacion().ejecutar()
                .esperarError(429, "DEMASIADOS_INTENTOS");
    }

    @Test
    void unIngresoCorrectoReiniciaLaCuentaDelUsuario() {
        String usuario = crear("limite.reinicio");
        for (int i = 0; i < 4; i++) {
            ingresar("10.66.0.3", usuario, CLAVE_INCORRECTA).preparacion().ejecutar().esperar(401);
        }
        ingresar("10.66.0.3", usuario, EscenarioDeUsuario.CONTRASENA).preparacion().ejecutar().esperar(200);
        for (int i = 0; i < 4; i++) {
            ingresar("10.66.0.3", usuario, CLAVE_INCORRECTA).preparacion().ejecutar().esperar(401);
        }

        ingresar("10.66.0.3", usuario, EscenarioDeUsuario.CONTRASENA).preparacion().ejecutar().esperar(200);
    }

    @Test
    void veinteFallosDesdeUnaIpLaBloquean() {
        for (int i = 0; i < 20; i++) {
            ingresar("10.66.0.4", "limite.ip." + i, CLAVE_INCORRECTA).preparacion().ejecutar().esperar(401);
        }
        String otro = crear("limite.ip.otro");

        ingresar("10.66.0.4", otro, EscenarioDeUsuario.CONTRASENA).preparacion().ejecutar().esperarError(429, "DEMASIADOS_INTENTOS");
        ingresar("10.66.0.5", otro, EscenarioDeUsuario.CONTRASENA).preparacion().ejecutar().esperar(200);
    }

    @Test
    void unBasicFallidoCuentaComoUnIngresoFallido() {
        String usuario = crear("limite.basic");
        for (int i = 0; i < 5; i++) {
            basic("10.66.0.6", usuario, CLAVE_INCORRECTA).get("/usuario/sesion").preparacion().ejecutar().esperar(401);
        }

        basic("10.66.0.6", usuario, EscenarioDeUsuario.CONTRASENA).get("/usuario/sesion").preparacion().ejecutar()
                .esperarError(429, "DEMASIADOS_INTENTOS");
        ingresar("10.66.0.7", usuario, EscenarioDeUsuario.CONTRASENA).preparacion().ejecutar()
                .esperarError(429, "DEMASIADOS_INTENTOS");
    }

    @Test
    void veinteTokensInvalidosEnMcpBloqueanLaIp() {
        for (int i = 0; i < 20; i++) {
            mcp("10.66.0.8", "token-invalido-" + i).ejecutar().esperarError(401, "ACCESS_TOKEN_INVALIDO");
        }

        Respuesta bloqueada = mcp("10.66.0.8", "sysadmin-access-token").ejecutar();
        bloqueada.esperarError(429, "DEMASIADOS_INTENTOS");
    }

    // Crea un usuario con la contrasena del escenario y responde su nombre de usuario
    private String crear(String nombreDeUsuario) {
        new EscenarioDeUsuario(this::como).crear(nombreDeUsuario);
        return nombreDeUsuario;
    }

    // POST /usuario/iniciar-sesion desde la IP recibida
    private Peticion ingresar(String ip, String nombreDeUsuario, String contrasena) {
        return sinAutenticacion().encabezado("X-Real-IP", ip).post("/usuario/iniciar-sesion")
                .json(Map.of("nombreDeUsuario", nombreDeUsuario, "contrasena", contrasena));
    }

    // Peticion desde la IP recibida con Authorization: Basic
    private Peticion basic(String ip, String nombreDeUsuario, String contrasena) {
        String credenciales = Base64.getEncoder().encodeToString((nombreDeUsuario + ":" + contrasena)
                .getBytes(StandardCharsets.UTF_8));
        return sinAutenticacion().encabezado("X-Real-IP", ip).encabezado("Authorization", "Basic " + credenciales);
    }

    // Ping a /mcp desde la IP recibida con el token como bearer
    private Peticion mcp(String ip, String token) {
        return sinAutenticacion().encabezado("X-Real-IP", ip).encabezado("Authorization", "Bearer " + token)
                .encabezado("Accept", "application/json, text/event-stream").post("/mcp")
                .json(Map.of("jsonrpc", "2.0", "id", 1, "method", "ping")).preparacion();
    }
}
