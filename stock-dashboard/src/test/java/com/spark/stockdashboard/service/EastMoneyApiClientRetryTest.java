package com.spark.stockdashboard.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 东方财富API重试机制测试
 */
@SpringBootTest
@TestPropertySource(properties = {
        "eastmoney.api.base-url=https://invalid-url.example.com",
        "eastmoney.api.retry.max-attempts=3",
        "eastmoney.api.retry.backoff-delay=500",
        "eastmoney.api.retry.backoff-multiplier=1.0"
})
public class EastMoneyApiClientRetryTest {

    @Autowired
    private EastMoneyApiClient eastMoneyApiClient;

    @Test
    public void testStockRealtimeDataRetry() {
        long startTime = System.currentTimeMillis();
        // 调用应该会重试3次，每次间隔500ms，总共大约需要2秒
        eastMoneyApiClient.fetchStockRealtimeData("sh000001");
        long duration = System.currentTimeMillis() - startTime;

        // 验证总耗时应该大于1500ms（3次重试，每次至少500ms间隔）
        assertTrue(duration >= 1500, "重试总耗时应该大于1500ms，实际：" + duration + "ms");
        System.out.println("重试总耗时：" + duration + "ms");
    }

    @Test
    public void testFundRealtimeDataRetry() {
        long startTime = System.currentTimeMillis();
        eastMoneyApiClient.fetchFundRealtimeData("000001");
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration >= 1500, "重试总耗时应该大于1500ms，实际：" + duration + "ms");
        System.out.println("重试总耗时：" + duration + "ms");
    }
}
