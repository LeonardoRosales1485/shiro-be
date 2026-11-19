package com.blue.open.Code.Common.Google;

import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Claves publicas con las que Google firma los ID tokens (JWKS). Las baja de shiro.google.jwks-url y las guarda en
 * memoria una hora; si llega un kid desconocido las vuelve a bajar, como mucho una vez por minuto (Google rota sus
 * claves). Integracion externa: la descarga va en try/catch y, si falla, responde GOOGLE_NO_DISPONIBLE.
 * Dependencias externas: Google (https://www.googleapis.com/oauth2/v3/certs).
 */
@Slf4j
@Component
public class ClavesPublicasDeGoogle {

    private static final Duration VIGENCIA = Duration.ofHours(1);

    private static final Duration ESPERA_ENTRE_DESCARGAS = Duration.ofMinutes(1);

    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final ObjectMapper lector = new ObjectMapper();

    @Value("${shiro.google.jwks-url:https://www.googleapis.com/oauth2/v3/certs}")
    private String jwksUrl;

    // Claves por kid y momento de la ultima descarga
    private Map<String, PublicKey> claves = Map.of();

    private Instant descargadas = Instant.EPOCH;

    // Clave publica del kid recibido, o vacio si Google no la publica. Lanza GOOGLE_NO_DISPONIBLE si no responde
    public synchronized Optional<PublicKey> clave(String kid) {
        Instant ahora = Instant.now();
        boolean vencidas = descargadas.plus(VIGENCIA).isBefore(ahora);
        boolean puedeReintentar = descargadas.plus(ESPERA_ENTRE_DESCARGAS).isBefore(ahora);
        if (vencidas || (!claves.containsKey(kid) && puedeReintentar)) {
            claves = descargar();
            descargadas = ahora;
        }
        return Optional.ofNullable(claves.get(kid));
    }

    // Baja el JWKS de Google y arma una clave RSA por kid. Lanza GOOGLE_NO_DISPONIBLE si falla
    private Map<String, PublicKey> descargar() {
        try {
            HttpRequest pedido = HttpRequest.newBuilder(URI.create(jwksUrl)).timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<String> respuesta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new IllegalStateException("status " + respuesta.statusCode());
            }
            Map<String, PublicKey> nuevas = new HashMap<>();
            KeyFactory fabrica = KeyFactory.getInstance("RSA");
            for (JsonNode clave : lector.readTree(respuesta.body()).get("keys")) {
                BigInteger modulo = new BigInteger(1, Base64.getUrlDecoder().decode(clave.get("n").asString()));
                BigInteger exponente = new BigInteger(1, Base64.getUrlDecoder().decode(clave.get("e").asString()));
                nuevas.put(clave.get("kid").asString(), fabrica.generatePublic(new RSAPublicKeySpec(modulo, exponente)));
            }
            log.info("Claves publicas de Google descargadas | cantidad={}", nuevas.size());
            return nuevas;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw noDisponible(e);
        } catch (Exception e) {
            throw noDisponible(e);
        }
    }

    // Error de la integracion: se loguea con la excepcion original y se traduce a GOOGLE_NO_DISPONIBLE
    private ExcepcionDeApi noDisponible(Exception causa) {
        log.error("No se pudieron descargar las claves publicas de Google | url={}", jwksUrl, causa);
        return new ExcepcionDeApi(CodigoDeErrorDelProyecto.GOOGLE_NO_DISPONIBLE,
                "ClavesPublicasDeGoogle | descargar | sin respuesta de Google", causa);
    }
}
