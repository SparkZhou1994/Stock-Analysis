package com.spark.stockdashboard.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 消息内容
     */
    private Object data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /**
         * 连接建立
         */
        CONNECTED,

        /**
         * 心跳检测
         */
        HEARTBEAT,

        /**
         * 订阅成功
         */
        SUBSCRIBE_SUCCESS,

        /**
         * 取消订阅成功
         */
        UNSUBSCRIBE_SUCCESS,

        /**
         * 实时价格
         */
        REALTIME_PRICE,

        /**
         * 错误消息
         */
        ERROR,

        /**
         * 断开连接
         */
        DISCONNECTED
    }
}