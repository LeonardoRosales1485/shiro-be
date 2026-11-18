package com.blue.open.Code.Common.WebSocket;

import com.blue.open.Core.Identity.Entidad.UsuarioBase;
import com.blue.open.Core.Protocolo.Jwt.AuthJwtService;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Protocolo.Jwt.Excepciones.CodigoDeErrorJwt;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Autentica el CONNECT de STOMP con el JWT del header Authorization ("Bearer ...").
 * Dependencias externas: core-protocolo-jwt (AuthJwtService). Recibe: el mensaje STOMP.
 * Responde: el mensaje con el StompPrincipal del usuario, o 401 si el token falta o es invalido.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final AuthJwtService authJwtService;

    // Solo actua en el CONNECT: valida el token y asocia el usuario a la sesion STOMP
    @Override
    public Message<?> preSend(@NonNull Message<?> mensaje, @NonNull MessageChannel canal) {
        StompHeaderAccessor accesor = MessageHeaderAccessor.getAccessor(mensaje, StompHeaderAccessor.class);

        if (accesor == null || !StompCommand.CONNECT.equals(accesor.getCommand())) {
            return mensaje;
        }

        List<String> autorizacion = accesor.getNativeHeader("Authorization");

        String token = obtenerToken(autorizacion);

        UsuarioBase usuario = authJwtService.getUsuarioDelToken(token);
        if (usuario == null) {
            throw new ExcepcionDeApi(CodigoDeErrorJwt.TOKEN_INVALIDO,
                    "Token inválido o expirado",
                    "WebSocketAuthInterceptor | preSend | usuario inexistente o token invalido");
        }

        accesor.setUser(new StompPrincipal(usuario.getUuid()));
        return mensaje;
    }

    // Extrae el token del header Authorization ("Bearer ..."). Lanza 401 si falta o tiene otro formato
    private @NonNull String obtenerToken(List<String> autorizacion) {
        if (autorizacion == null || autorizacion.isEmpty()) {
            throw new ExcepcionDeApi(CodigoDeErrorJwt.TOKEN_REQUERIDO,
                    "Falta el token de autorización",
                    "WebSocketAuthInterceptor | preSend | header Authorization vacio");
        }

        String tokenBearer = autorizacion.get(0);
        if (tokenBearer == null || !tokenBearer.startsWith("Bearer ")) {
            throw new ExcepcionDeApi(CodigoDeErrorJwt.TOKEN_INVALIDO,
                    "Formato de token inválido",
                    "WebSocketAuthInterceptor | preSend | formato Bearer invalido");
        }

        return tokenBearer.substring(7);
    }
}
