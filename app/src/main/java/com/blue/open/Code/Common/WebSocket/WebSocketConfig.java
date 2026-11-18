package com.blue.open.Code.Common.WebSocket;

import com.blue.open.Code.Common.Configuracion.UrlsDelFrontProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;

/**
 * Configuracion del broker STOMP: endpoint /websocket (con SockJS), destinos /topic, /queue y /user,
 * origenes permitidos desde front.url.websocket y autenticacion via WebSocketAuthInterceptor.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final String[] origenesDelFront;

    private final WebSocketAuthInterceptor interceptorDeAutenticacion;

    // Recibe las URLs del front (para los origenes permitidos) y el interceptor que autentica el handshake
    public WebSocketConfig(UrlsDelFrontProperties urlsDelFront, WebSocketAuthInterceptor interceptorDeAutenticacion) {
        this.origenesDelFront = urlsDelFront.getOrigenesDeWebSocket();
        this.interceptorDeAutenticacion = interceptorDeAutenticacion;
    }

    // Destinos del broker: /topic y /queue para el cliente, /app para la aplicacion, /user para mensajes por usuario
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registro) {
        registro.enableSimpleBroker("/topic", "/queue");
        registro.setApplicationDestinationPrefixes("/app");
        registro.setUserDestinationPrefix("/user");
    }

    // Autentica los mensajes entrantes con el interceptor
    @Override
    public void configureClientInboundChannel(ChannelRegistration registro) {
        registro.interceptors(interceptorDeAutenticacion);
    }

    // Publica el endpoint /websocket con SockJS para los origenes del front
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registro) {

        registro.addEndpoint("/websocket")
                .setAllowedOriginPatterns(origenesDelFront)
                .withSockJS();
    }
}
