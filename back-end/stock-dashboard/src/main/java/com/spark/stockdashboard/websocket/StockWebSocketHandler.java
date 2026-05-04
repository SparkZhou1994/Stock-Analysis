package com.spark.stockdashboard.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.stockdashboard.websocket.dto.SubscribeRequest;
import com.spark.stockdashboard.websocket.dto.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket处理器
 * 处理WebSocket连接、消息收发、断开连接等
 */
@Slf4j
@Component
public class StockWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService heartbeatScheduler;

    /**
     * 心跳间隔（秒）
     */
    private static final int HEARTBEAT_INTERVAL = 30;

    /**
     * 构造函数
     *
     * @param sessionManager WebSocket会话管理器
     * @param objectMapper JSON对象映射器
     */
    public StockWebSocketHandler(WebSocketSessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        startHeartbeatTask();
    }

    /**
     * 连接建立后调用
     *
     * @param session WebSocket会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            // 添加会话到管理器
            sessionManager.addSession(session);

            // 发送连接成功消息
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.CONNECTED)
                    .data("连接成功，欢迎使用股票实时数据服务")
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, message);

            log.info("WebSocket连接建立成功，会话ID: {}, 远程地址: {}",
                    session.getId(), session.getRemoteAddress());
        } catch (Exception e) {
            log.error("WebSocket连接建立异常，会话ID: {}", session.getId(), e);
        }
    }

    /**
     * 处理收到的文本消息
     *
     * @param session WebSocket会话
     * @param message 文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("收到WebSocket消息，会话ID: {}, 消息内容: {}", session.getId(), payload);

            // 解析消息
            WebSocketMessage wsMessage = objectMapper.readValue(payload, WebSocketMessage.class);

            // 根据消息类型处理
            switch (wsMessage.getType()) {
                case HEARTBEAT:
                    handleHeartbeat(session, wsMessage);
                    break;
                case SUBSCRIBE_SUCCESS:
                    // 客户端确认订阅成功，无需处理
                    break;
                case UNSUBSCRIBE_SUCCESS:
                    // 客户端确认取消订阅成功，无需处理
                    break;
                default:
                    log.warn("未知的消息类型: {}", wsMessage.getType());
                    sendErrorMessage(session, "未知的消息类型: " + wsMessage.getType());
            }

        } catch (IOException e) {
            log.error("解析WebSocket消息失败，会话ID: {}", session.getId(), e);
            sendErrorMessage(session, "消息格式错误");
        } catch (Exception e) {
            log.error("处理WebSocket消息异常，会话ID: {}", session.getId(), e);
            sendErrorMessage(session, "处理消息时发生异常");
        }
    }

    /**
     * 处理心跳消息
     *
     * @param session WebSocket会话
     * @param message 心跳消息
     */
    private void handleHeartbeat(WebSocketSession session, WebSocketMessage message) {
        // 发送心跳响应
        WebSocketMessage response = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.HEARTBEAT)
                .data("pong")
                .timestamp(System.currentTimeMillis())
                .build();

        sendMessage(session, response);
        log.debug("处理心跳消息，会话ID: {}", session.getId());
    }

    /**
     * 连接关闭后调用
     *
     * @param session WebSocket会话
     * @param status 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            // 从管理器移除会话
            sessionManager.removeSession(session);

            log.info("WebSocket连接关闭，会话ID: {}, 关闭状态: {}, 原因: {}",
                    session.getId(), status.getCode(), status.getReason());
        } catch (Exception e) {
            log.error("WebSocket连接关闭异常，会话ID: {}", session.getId(), e);
        }
    }

    /**
     * 传输错误时调用
     *
     * @param session WebSocket会话
     * @param exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误，会话ID: {}", session.getId(), exception);

        try {
            // 发送错误消息
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.ERROR)
                    .data("传输错误: " + exception.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, message);

            // 关闭连接
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.error("关闭WebSocket连接失败，会话ID: {}", session.getId(), e);
        }
    }

    /**
     * 订阅证券
     *
     * @param session WebSocket会话
     * @param request 订阅请求
     * @return 是否订阅成功
     */
    public boolean subscribe(WebSocketSession session, SubscribeRequest request) {
        try {
            boolean success = sessionManager.subscribe(session, request);

            if (success) {
                // 发送订阅成功消息
                WebSocketMessage message = WebSocketMessage.builder()
                        .type(WebSocketMessage.MessageType.SUBSCRIBE_SUCCESS)
                        .data(request)
                        .timestamp(System.currentTimeMillis())
                        .build();

                sendMessage(session, message);
                log.info("订阅成功，会话ID: {}, 证券类型: {}, 代码: {}",
                        session.getId(), request.getSecurityType(), request.getCode());
            } else {
                sendErrorMessage(session, "订阅失败，可能已订阅该证券");
            }

            return success;
        } catch (Exception e) {
            log.error("订阅证券异常，会话ID: {}, 请求: {}", session.getId(), request, e);
            sendErrorMessage(session, "订阅时发生异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 取消订阅证券
     *
     * @param session WebSocket会话
     * @param request 订阅请求
     * @return 是否取消订阅成功
     */
    public boolean unsubscribe(WebSocketSession session, SubscribeRequest request) {
        try {
            boolean success = sessionManager.unsubscribe(session, request);

            if (success) {
                // 发送取消订阅成功消息
                WebSocketMessage message = WebSocketMessage.builder()
                        .type(WebSocketMessage.MessageType.UNSUBSCRIBE_SUCCESS)
                        .data(request)
                        .timestamp(System.currentTimeMillis())
                        .build();

                sendMessage(session, message);
                log.info("取消订阅成功，会话ID: {}, 证券类型: {}, 代码: {}",
                        session.getId(), request.getSecurityType(), request.getCode());
            } else {
                sendErrorMessage(session, "取消订阅失败，可能未订阅该证券");
            }

            return success;
        } catch (Exception e) {
            log.error("取消订阅证券异常，会话ID: {}, 请求: {}", session.getId(), request, e);
            sendErrorMessage(session, "取消订阅时发生异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送实时价格消息
     *
     * @param securityType 证券类型
     * @param code 证券代码
     * @param priceData 价格数据
     * @return 成功发送的会话数量
     */
    public int sendRealtimePrice(SubscribeRequest.SecurityType securityType, String code, Object priceData) {
        try {
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.REALTIME_PRICE)
                    .data(priceData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String jsonMessage = objectMapper.writeValueAsString(message);
            return sessionManager.broadcastToCode(securityType, code, jsonMessage);
        } catch (Exception e) {
            log.error("发送实时价格消息异常，证券类型: {}, 代码: {}", securityType, code, e);
            return 0;
        }
    }

    /**
     * 发送消息到会话
     *
     * @param session WebSocket会话
     * @param message 消息对象
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            sessionManager.sendMessage(session, jsonMessage);
        } catch (Exception e) {
            log.error("发送WebSocket消息异常，会话ID: {}", session.getId(), e);
        }
    }

    /**
     * 发送错误消息
     *
     * @param session WebSocket会话
     * @param errorMessage 错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.ERROR)
                    .data(errorMessage)
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, message);
        } catch (Exception e) {
            log.error("发送错误消息异常，会话ID: {}", session.getId(), e);
        }
    }

    /**
     * 启动心跳检测任务
     */
    private void startHeartbeatTask() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                sessionManager.cleanupInvalidSessions();

                int activeSessions = sessionManager.getActiveSessionCount();
                int activeSubscriptions = sessionManager.getActiveSubscriptionCount();

                log.debug("心跳检测 - 活跃会话: {}, 活跃订阅: {}",
                        activeSessions, activeSubscriptions);
            } catch (Exception e) {
                log.error("心跳检测任务异常", e);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        log.info("WebSocket心跳检测任务已启动，间隔: {}秒", HEARTBEAT_INTERVAL);
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        try {
            heartbeatScheduler.shutdown();
            if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                heartbeatScheduler.shutdownNow();
            }
            log.info("WebSocket处理器已关闭");
        } catch (InterruptedException e) {
            heartbeatScheduler.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("关闭WebSocket处理器时被中断", e);
        }
    }
}