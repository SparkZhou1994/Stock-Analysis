package com.spark.stockdashboard.websocket;

import com.spark.stockdashboard.websocket.dto.SubscribeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket会话管理器
 * 管理WebSocket会话，支持按代码订阅
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 存储所有活跃的WebSocket会话
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 存储订阅关系：证券代码 -> 会话ID集合
     */
    private final Map<String, Set<String>> codeToSessionIds = new ConcurrentHashMap<>();

    /**
     * 存储会话订阅关系：会话ID -> 证券代码集合
     */
    private final Map<String, Set<String>> sessionIdToCodes = new ConcurrentHashMap<>();

    /**
     * 添加会话
     *
     * @param session WebSocket会话
     */
    public void addSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionIdToCodes.put(sessionId, new CopyOnWriteArraySet<>());
        log.info("WebSocket会话添加成功，会话ID: {}", sessionId);
    }

    /**
     * 移除会话
     *
     * @param session WebSocket会话
     */
    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();

        // 清理订阅关系
        Set<String> subscribedCodes = sessionIdToCodes.remove(sessionId);
        if (subscribedCodes != null) {
            for (String code : subscribedCodes) {
                Set<String> sessionIds = codeToSessionIds.get(code);
                if (sessionIds != null) {
                    sessionIds.remove(sessionId);
                    if (sessionIds.isEmpty()) {
                        codeToSessionIds.remove(code);
                    }
                }
            }
        }

        // 移除会话
        sessions.remove(sessionId);
        log.info("WebSocket会话移除成功，会话ID: {}", sessionId);
    }

    /**
     * 订阅证券
     *
     * @param session WebSocket会话
     * @param request 订阅请求
     * @return 是否订阅成功
     */
    public boolean subscribe(WebSocketSession session, SubscribeRequest request) {
        String sessionId = session.getId();
        String code = buildCodeKey(request.getSecurityType(), request.getCode());

        // 添加到会话订阅关系
        Set<String> sessionCodes = sessionIdToCodes.get(sessionId);
        if (sessionCodes == null) {
            sessionCodes = new CopyOnWriteArraySet<>();
            sessionIdToCodes.put(sessionId, sessionCodes);
        }

        // 添加到代码订阅关系
        Set<String> sessionIds = codeToSessionIds.get(code);
        if (sessionIds == null) {
            sessionIds = new CopyOnWriteArraySet<>();
            codeToSessionIds.put(code, sessionIds);
        }

        boolean addedToSession = sessionCodes.add(code);
        boolean addedToCode = sessionIds.add(sessionId);

        if (addedToSession && addedToCode) {
            log.info("订阅成功，会话ID: {}, 证券代码: {}", sessionId, code);
            return true;
        }

        return false;
    }

    /**
     * 取消订阅证券
     *
     * @param session WebSocket会话
     * @param request 订阅请求
     * @return 是否取消订阅成功
     */
    public boolean unsubscribe(WebSocketSession session, SubscribeRequest request) {
        String sessionId = session.getId();
        String code = buildCodeKey(request.getSecurityType(), request.getCode());

        // 从会话订阅关系移除
        Set<String> sessionCodes = sessionIdToCodes.get(sessionId);
        boolean removedFromSession = sessionCodes != null && sessionCodes.remove(code);

        // 从代码订阅关系移除
        Set<String> sessionIds = codeToSessionIds.get(code);
        boolean removedFromCode = sessionIds != null && sessionIds.remove(sessionId);

        // 清理空集合
        if (sessionIds != null && sessionIds.isEmpty()) {
            codeToSessionIds.remove(code);
        }

        if (removedFromSession && removedFromCode) {
            log.info("取消订阅成功，会话ID: {}, 证券代码: {}", sessionId, code);
            return true;
        }

        return false;
    }

    /**
     * 获取订阅特定证券的所有会话
     *
     * @param securityType 证券类型
     * @param code 证券代码
     * @return 订阅该证券的会话集合
     */
    public Set<WebSocketSession> getSessionsByCode(SubscribeRequest.SecurityType securityType, String code) {
        String codeKey = buildCodeKey(securityType, code);
        Set<String> sessionIds = codeToSessionIds.get(codeKey);

        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<WebSocketSession> result = new HashSet<>();
        for (String sessionId : sessionIds) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                result.add(session);
            }
        }

        return result;
    }

    /**
     * 获取会话订阅的所有证券代码
     *
     * @param session WebSocket会话
     * @return 会话订阅的证券代码集合
     */
    public Set<String> getCodesBySession(WebSocketSession session) {
        String sessionId = session.getId();
        Set<String> codes = sessionIdToCodes.get(sessionId);
        return codes != null ? new HashSet<>(codes) : Collections.emptySet();
    }

    /**
     * 向特定会话发送消息
     *
     * @param session WebSocket会话
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            return false;
        }

        try {
            synchronized (session) {
                session.sendMessage(new org.springframework.web.socket.TextMessage(message));
            }
            return true;
        } catch (IOException e) {
            log.error("发送WebSocket消息失败，会话ID: {}", session.getId(), e);
            return false;
        }
    }

    /**
     * 向订阅特定证券的所有会话广播消息
     *
     * @param securityType 证券类型
     * @param code 证券代码
     * @param message 消息内容
     * @return 成功发送的会话数量
     */
    public int broadcastToCode(SubscribeRequest.SecurityType securityType, String code, String message) {
        Set<WebSocketSession> sessions = getSessionsByCode(securityType, code);
        int successCount = 0;

        for (WebSocketSession session : sessions) {
            if (sendMessage(session, message)) {
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * 向所有活跃会话广播消息
     *
     * @param message 消息内容
     * @return 成功发送的会话数量
     */
    public int broadcastToAll(String message) {
        int successCount = 0;

        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen() && sendMessage(session, message)) {
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * 获取活跃会话数量
     *
     * @return 活跃会话数量
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
                .filter(WebSocketSession::isOpen)
                .count();
    }

    /**
     * 获取活跃订阅数量（按证券代码统计）
     *
     * @return 活跃订阅数量
     */
    public int getActiveSubscriptionCount() {
        return codeToSessionIds.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * 构建证券代码键
     *
     * @param securityType 证券类型
     * @param code 证券代码
     * @return 构建后的代码键
     */
    private String buildCodeKey(SubscribeRequest.SecurityType securityType, String code) {
        return securityType.name() + ":" + code;
    }

    /**
     * 清理无效会话
     */
    public void cleanupInvalidSessions() {
        List<String> invalidSessionIds = new ArrayList<>();

        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (!entry.getValue().isOpen()) {
                invalidSessionIds.add(entry.getKey());
            }
        }

        for (String sessionId : invalidSessionIds) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null) {
                removeSession(session);
            }
        }

        if (!invalidSessionIds.isEmpty()) {
            log.info("清理了 {} 个无效WebSocket会话", invalidSessionIds.size());
        }
    }
}