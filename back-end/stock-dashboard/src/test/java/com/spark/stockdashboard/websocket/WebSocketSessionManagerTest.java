package com.spark.stockdashboard.websocket;

import com.spark.stockdashboard.websocket.dto.SubscribeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * WebSocketSessionManager测试类
 */
@ExtendWith(MockitoExtension.class)
class WebSocketSessionManagerTest {

    @Mock
    private WebSocketSession session1;

    @Mock
    private WebSocketSession session2;

    private WebSocketSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new WebSocketSessionManager();

        when(session1.getId()).thenReturn("session-1");
        when(session2.getId()).thenReturn("session-2");
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);
    }

    @Test
    void testAddAndRemoveSession() {
        // 添加会话
        sessionManager.addSession(session1);
        assertEquals(1, sessionManager.getActiveSessionCount());

        sessionManager.addSession(session2);
        assertEquals(2, sessionManager.getActiveSessionCount());

        // 移除会话
        sessionManager.removeSession(session1);
        assertEquals(1, sessionManager.getActiveSessionCount());

        sessionManager.removeSession(session2);
        assertEquals(0, sessionManager.getActiveSessionCount());
    }

    @Test
    void testSubscribeAndUnsubscribe() {
        // 添加会话
        sessionManager.addSession(session1);

        // 创建订阅请求
        SubscribeRequest request = SubscribeRequest.builder()
                .securityType(SubscribeRequest.SecurityType.STOCK)
                .code("000001")
                .build();

        // 订阅
        boolean subscribeResult = sessionManager.subscribe(session1, request);
        assertTrue(subscribeResult);
        assertEquals(1, sessionManager.getActiveSubscriptionCount());

        // 获取订阅的会话
        var sessions = sessionManager.getSessionsByCode(
                SubscribeRequest.SecurityType.STOCK, "000001");
        assertEquals(1, sessions.size());
        assertTrue(sessions.contains(session1));

        // 获取会话订阅的代码
        var codes = sessionManager.getCodesBySession(session1);
        assertEquals(1, codes.size());
        assertTrue(codes.contains("STOCK:000001"));

        // 取消订阅
        boolean unsubscribeResult = sessionManager.unsubscribe(session1, request);
        assertTrue(unsubscribeResult);
        assertEquals(0, sessionManager.getActiveSubscriptionCount());
    }

    @Test
    void testBroadcastToCode() throws IOException {
        // 添加会话
        sessionManager.addSession(session1);
        sessionManager.addSession(session2);

        // 订阅
        SubscribeRequest request = SubscribeRequest.builder()
                .securityType(SubscribeRequest.SecurityType.STOCK)
                .code("000001")
                .build();

        sessionManager.subscribe(session1, request);
        sessionManager.subscribe(session2, request);

        // 广播消息
        String message = "测试消息";
        int sentCount = sessionManager.broadcastToCode(
                SubscribeRequest.SecurityType.STOCK, "000001", message);

        assertEquals(2, sentCount);

        // 验证消息发送
        verify(session1, times(1)).sendMessage(any());
        verify(session2, times(1)).sendMessage(any());
    }

    @Test
    void testBroadcastToAll() throws IOException {
        // 添加会话
        sessionManager.addSession(session1);
        sessionManager.addSession(session2);

        // 广播消息
        String message = "测试广播消息";
        int sentCount = sessionManager.broadcastToAll(message);

        assertEquals(2, sentCount);

        // 验证消息发送
        verify(session1, times(1)).sendMessage(any());
        verify(session2, times(1)).sendMessage(any());
    }

    @Test
    void testSendMessageToClosedSession() throws IOException {
        // 添加会话但标记为关闭
        when(session1.isOpen()).thenReturn(false);
        sessionManager.addSession(session1);

        // 发送消息应该失败
        boolean result = sessionManager.sendMessage(session1, "测试消息");
        assertFalse(result);

        // 验证没有发送消息
        verify(session1, never()).sendMessage(any());
    }

    @Test
    void testCleanupInvalidSessions() {
        // 添加两个会话，一个打开一个关闭
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(false);

        sessionManager.addSession(session1);
        sessionManager.addSession(session2);

        // 清理前有2个会话
        assertEquals(2, sessionManager.getActiveSessionCount());

        // 清理无效会话
        sessionManager.cleanupInvalidSessions();

        // 清理后只有1个活跃会话
        assertEquals(1, sessionManager.getActiveSessionCount());
    }

    @Test
    void testMultipleSubscriptions() {
        // 添加会话
        sessionManager.addSession(session1);

        // 订阅多个证券
        SubscribeRequest stockRequest = SubscribeRequest.builder()
                .securityType(SubscribeRequest.SecurityType.STOCK)
                .code("000001")
                .build();

        SubscribeRequest fundRequest = SubscribeRequest.builder()
                .securityType(SubscribeRequest.SecurityType.FUND)
                .code("161725")
                .build();

        sessionManager.subscribe(session1, stockRequest);
        sessionManager.subscribe(session1, fundRequest);

        // 验证订阅数量
        assertEquals(2, sessionManager.getActiveSubscriptionCount());

        // 验证会话订阅的代码
        var codes = sessionManager.getCodesBySession(session1);
        assertEquals(2, codes.size());
        assertTrue(codes.contains("STOCK:000001"));
        assertTrue(codes.contains("FUND:161725"));
    }

    @Test
    void testDuplicateSubscription() {
        // 添加会话
        sessionManager.addSession(session1);

        // 创建订阅请求
        SubscribeRequest request = SubscribeRequest.builder()
                .securityType(SubscribeRequest.SecurityType.STOCK)
                .code("000001")
                .build();

        // 第一次订阅
        boolean firstResult = sessionManager.subscribe(session1, request);
        assertTrue(firstResult);
        assertEquals(1, sessionManager.getActiveSubscriptionCount());

        // 第二次订阅相同证券（应该失败）
        boolean secondResult = sessionManager.subscribe(session1, request);
        assertFalse(secondResult);
        assertEquals(1, sessionManager.getActiveSubscriptionCount());
    }
}