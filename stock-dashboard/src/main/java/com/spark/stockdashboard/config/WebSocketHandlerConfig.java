package com.spark.stockdashboard.config;

import com.spark.stockdashboard.websocket.StockWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket处理器配置
 * 注册WebSocket处理器和配置WebSocket容器
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandlerConfig implements WebSocketConfigurer {

    private final StockWebSocketHandler stockWebSocketHandler;

    /**
     * 注册WebSocket处理器
     *
     * @param registry WebSocket处理器注册器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，设置允许跨域访问
        registry.addHandler(stockWebSocketHandler, "/ws-handler")
                .setAllowedOriginPatterns("*");

        log.info("WebSocket处理器已注册，路径: /ws-handler");
    }

    /**
     * 配置WebSocket容器
     *
     * @return ServletServerContainerFactoryBean
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        // 设置消息缓冲区大小（字节）
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);

        // 设置会话空闲超时时间（毫秒）
        container.setMaxSessionIdleTimeout(300000L); // 5分钟

        // 设置异步发送超时时间（毫秒）
        container.setAsyncSendTimeout(10000L); // 10秒

        log.info("WebSocket容器配置完成，消息缓冲区: 8KB, 会话空闲超时: 5分钟");

        return container;
    }

    /**
     * 日志记录
     */
    private static void logInfo(String message) {
        System.out.println("[WebSocketConfig] " + message);
    }
}