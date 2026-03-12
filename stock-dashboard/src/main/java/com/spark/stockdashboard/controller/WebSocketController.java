package com.spark.stockdashboard.controller;

import com.spark.stockdashboard.service.RealtimeDataService;
import com.spark.stockdashboard.websocket.StockWebSocketHandler;
import com.spark.stockdashboard.websocket.WebSocketSessionManager;
import com.spark.stockdashboard.websocket.dto.SubscribeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * WebSocket控制器
 * 提供WebSocket测试接口和管理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketController {

    private final StockWebSocketHandler stockWebSocketHandler;
    private final WebSocketSessionManager sessionManager;
    private final RealtimeDataService realtimeDataService;

    /**
     * 获取WebSocket连接状态
     *
     * @return 连接状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("activeSessions", sessionManager.getActiveSessionCount());
        status.put("activeSubscriptions", sessionManager.getActiveSubscriptionCount());
        status.put("service", "WebSocket实时数据服务");
        status.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(status);
    }

    /**
     * 测试WebSocket连接
     *
     * @return 测试结果
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 模拟发送测试消息
            int broadcastCount = sessionManager.broadcastToAll("{\"type\":\"TEST\",\"data\":\"WebSocket服务正常\",\"timestamp\":" + System.currentTimeMillis() + "}");

            result.put("success", true);
            result.put("message", "WebSocket连接测试成功");
            result.put("broadcastCount", broadcastCount);
            result.put("timestamp", System.currentTimeMillis());

            log.info("WebSocket连接测试成功，广播消息到 {} 个会话", broadcastCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "WebSocket连接测试失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());

            log.error("WebSocket连接测试失败", e);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 手动触发实时价格推送（测试用）
     *
     * @param securityType 证券类型
     * @param code 证券代码
     * @return 推送结果
     */
    @PostMapping("/push/{securityType}/{code}")
    public ResponseEntity<Map<String, Object>> pushRealtimePrice(
            @PathVariable String securityType,
            @PathVariable String code) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 验证证券类型
            SubscribeRequest.SecurityType type;
            try {
                type = SubscribeRequest.SecurityType.valueOf(securityType.toUpperCase());
            } catch (IllegalArgumentException e) {
                result.put("success", false);
                result.put("message", "无效的证券类型，支持: STOCK, FUND");
                return ResponseEntity.badRequest().body(result);
            }

            // 获取实时数据
            Object priceData = getRealtimePriceData(type, code);
            if (priceData == null) {
                result.put("success", false);
                result.put("message", "获取实时数据失败");
                return ResponseEntity.ok(result);
            }

            // 发送实时价格
            int sentCount = stockWebSocketHandler.sendRealtimePrice(type, code, priceData);

            result.put("success", true);
            result.put("message", "实时价格推送成功");
            result.put("securityType", type);
            result.put("code", code);
            result.put("sentCount", sentCount);
            result.put("timestamp", System.currentTimeMillis());

            log.info("手动触发实时价格推送，证券类型: {}, 代码: {}, 发送到 {} 个会话",
                    type, code, sentCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "实时价格推送失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());

            log.error("实时价格推送失败，证券类型: {}, 代码: {}", securityType, code, e);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取实时价格数据
     *
     * @param securityType 证券类型
     * @param code 证券代码
     * @return 实时价格数据
     */
    private Object getRealtimePriceData(SubscribeRequest.SecurityType securityType, String code) {
        try {
            // 这里应该调用实际的实时数据服务
            // 目前返回模拟数据
            Map<String, Object> priceData = new HashMap<>();
            priceData.put("code", code);
            priceData.put("securityType", securityType.name());
            priceData.put("price", Math.random() * 100);
            priceData.put("change", (Math.random() - 0.5) * 5);
            priceData.put("changePercent", (Math.random() - 0.5) * 10);
            priceData.put("volume", (int) (Math.random() * 1000000));
            priceData.put("amount", Math.random() * 10000000);
            priceData.put("timestamp", System.currentTimeMillis());

            return priceData;
        } catch (Exception e) {
            log.error("获取实时价格数据失败，证券类型: {}, 代码: {}", securityType, code, e);
            return null;
        }
    }

    /**
     * 清理无效会话
     *
     * @return 清理结果
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupSessions() {
        Map<String, Object> result = new HashMap<>();

        try {
            int beforeCount = sessionManager.getActiveSessionCount();
            sessionManager.cleanupInvalidSessions();
            int afterCount = sessionManager.getActiveSessionCount();

            result.put("success", true);
            result.put("message", "会话清理完成");
            result.put("cleanedCount", beforeCount - afterCount);
            result.put("remainingSessions", afterCount);
            result.put("timestamp", System.currentTimeMillis());

            log.info("WebSocket会话清理完成，清理了 {} 个无效会话，剩余 {} 个活跃会话",
                    beforeCount - afterCount, afterCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "会话清理失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());

            log.error("WebSocket会话清理失败", e);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取订阅统计信息
     *
     * @return 订阅统计信息
     */
    @GetMapping("/subscriptions/stats")
    public ResponseEntity<Map<String, Object>> getSubscriptionStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 这里可以添加更详细的统计信息
            stats.put("activeSessions", sessionManager.getActiveSessionCount());
            stats.put("activeSubscriptions", sessionManager.getActiveSubscriptionCount());
            stats.put("timestamp", System.currentTimeMillis());

            // 添加服务状态
            stats.put("serviceStatus", "RUNNING");
            stats.put("heartbeatEnabled", true);
            stats.put("realtimePushEnabled", true);

            log.debug("获取WebSocket订阅统计信息");
        } catch (Exception e) {
            stats.put("error", "获取统计信息失败: " + e.getMessage());
            stats.put("timestamp", System.currentTimeMillis());

            log.error("获取WebSocket订阅统计信息失败", e);
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        health.put("status", "UP");
        health.put("component", "WebSocketService");
        health.put("activeSessions", sessionManager.getActiveSessionCount());
        health.put("timestamp", System.currentTimeMillis());

        // 检查关键组件
        health.put("sessionManager", "OK");
        health.put("webSocketHandler", "OK");
        health.put("realtimeDataService", realtimeDataService != null ? "OK" : "NOT_AVAILABLE");

        return ResponseEntity.ok(health);
    }

    /**
     * 获取WebSocket连接指南
     *
     * @return 连接指南
     */
    @GetMapping("/guide")
    public ResponseEntity<Map<String, Object>> getConnectionGuide() {
        Map<String, Object> guide = new HashMap<>();

        guide.put("websocketEndpoints", new String[]{
                "ws://localhost:8080/ws",
                "ws://localhost:8080/ws-native"
        });

        guide.put("messageTypes", new String[]{
                "CONNECTED - 连接建立",
                "HEARTBEAT - 心跳检测",
                "SUBSCRIBE_SUCCESS - 订阅成功",
                "UNSUBSCRIBE_SUCCESS - 取消订阅成功",
                "REALTIME_PRICE - 实时价格",
                "ERROR - 错误消息",
                "DISCONNECTED - 断开连接"
        });

        guide.put("subscriptionExample", new HashMap<String, Object>() {{
            put("type", "SUBSCRIBE");
            put("data", new HashMap<String, Object>() {{
                put("securityType", "STOCK");
                put("code", "000001");
            }});
        }});

        guide.put("heartbeatExample", new HashMap<String, Object>() {{
            put("type", "HEARTBEAT");
            put("data", "ping");
            put("timestamp", System.currentTimeMillis());
        }});

        guide.put("notes", new String[]{
                "建议使用SockJS客户端库",
                "心跳间隔建议30秒",
                "支持股票(STOCK)和基金(FUND)类型",
                "自动重连机制已内置"
        });

        return ResponseEntity.ok(guide);
    }
}