package com.spark.stockdashboard.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订阅请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {

    /**
     * 证券类型
     */
    private SecurityType securityType;

    /**
     * 证券代码
     */
    private String code;

    /**
     * 证券类型枚举
     */
    public enum SecurityType {
        /**
         * 股票
         */
        STOCK,

        /**
         * 基金
         */
        FUND
    }
}