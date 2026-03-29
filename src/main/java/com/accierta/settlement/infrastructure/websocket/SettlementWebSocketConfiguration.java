package com.accierta.settlement.infrastructure.websocket;

import com.accierta.shared.infrastructure.web.AcciertaWebProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class SettlementWebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final AcciertaWebProperties webProperties;

    public SettlementWebSocketConfiguration(AcciertaWebProperties webProperties) {
        this.webProperties = webProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/group");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(webProperties.allowedOriginsArray());
    }
}
