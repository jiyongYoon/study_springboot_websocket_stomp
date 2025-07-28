package dev.yoon.study_springboot_websocket_stomp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket에 연결할 때 사용할 STOMP 엔드포인트를 등록합니다.
        // `/ws` 경로로 연결되며, WebSocket을 지원하지 않는 브라우저를 위해 SockJS를 사용하도록 설정합니다.
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // `/topic`으로 시작하는 주소를 구독하는 클라이언트에게 메시지를 전달하는 간단한 메모리 기반 메시지 브로커를 활성화합니다.
        // 이제 각 채팅방은 `/topic/public/{roomId}`와 같은 고유한 주소를 갖게 됩니다.
        config.enableSimpleBroker("/topic");

        // 클라이언트가 서버로 메시지를 보낼 때 사용할 주소의 접두사(prefix)를 설정합니다.
        // 예를 들어, 클라이언트는 `/app/chat.sendMessage`와 같은 주소로 메시지를 보냅니다.
        config.setApplicationDestinationPrefixes("/app");
    }

}