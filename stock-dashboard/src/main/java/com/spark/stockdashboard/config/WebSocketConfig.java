package com.spark.stockdashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 配置WebSocket端点、消息代理等
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     *
     * @param registry 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单的内存消息代理，将消息发送到以"/topic"为前缀的目的地
        registry.enableSimpleBroker("/topic", "/queue");

        // 设置应用程序前缀，客户端发送消息时需要以"/app"为前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 设置用户目的地前缀
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     *
     * @param registry STOMP端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，允许跨域访问
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 注册备用端点，不使用SockJS
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}