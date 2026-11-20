package com.blue.open.Code.Usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registra el ultimo uso de un token de acceso (propuesta 50 resuelta el 19/11) a lo sumo una vez por minuto por
 * token, para no escribir en cada pedido del MCP.
 * Dependencias externas: la base de datos (shr_uso_de_token).
 */
@Component
@RequiredArgsConstructor
public class RegistroDeUsoDeToken {

    private static final long UN_MINUTO = 60_000L;

    // Ultima escritura de cada token en este proceso (id del token -> milisegundos)
    private final Map<Long, Long> ultimaEscritura = new ConcurrentHashMap<>();

    private final UsoDeTokenRepository repository;

    // Guarda ahora como ultimo uso del token, salvo que ya se haya guardado en el ultimo minuto
    @Transactional
    public void registrar(Long idAccessToken) {
        long ahora = System.currentTimeMillis();
        Long anterior = ultimaEscritura.get(idAccessToken);
        if (anterior != null && ahora - anterior < UN_MINUTO)
            return;
        ultimaEscritura.put(idAccessToken, ahora);
        Timestamp momento = new Timestamp(ahora);
        UsoDeToken uso = repository.findFirstByIdAccessToken(idAccessToken).orElseGet(() -> new UsoDeToken(idAccessToken, momento));
        uso.usar(momento);
        repository.save(uso);
    }
}
