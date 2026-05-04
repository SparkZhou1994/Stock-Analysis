package com.spark.stockdashboard.service;

import com.spark.stockdashboard.websocket.StockWebSocketHandler;
import com.spark.stockdashboard.websocket.dto.SubscribeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket推送服务
 * 负责定期推送实时数据到订阅的客户端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final StockWebSocketHandler stockWebSocketHandler;
    private final RealtimeDataService realtimeDataService;

    /**
     * 推送统计
     */
    private final Map<String, AtomicInteger> pushStatistics = new ConcurrentHashMap<>();

    /**
     * 模拟的活跃股票代码（实际应该从数据库或配置中获取）
     */
    private static final String[] ACTIVE_STOCKS = {
            "000001", "000002", "000858", "600519", "300750"
    };

    /**
     * 模拟的活跃基金代码（实际应该从数据库或配置中获取）
     */
    private static final String[] ACTIVE_FUNDS = {
            "161725", "110022", "519674", "003096", "005827"
    };

    /**
     * 定期推送实时数据
     * 每3秒执行一次
     */
    @Scheduled(fixedDelay = 3000)
    @Async
    public void pushRealtimeData() {
        try {
            pushStockData();
            pushFundData();
            logStatistics();
        } catch (Exception e) {
            log.error("推送实时数据异常", e);
        }
    }

    /**
     * 推送股票数据
     */
    private void pushStockData() {
        for (String stockCode : ACTIVE_STOCKS) {
            try {
                // 获取实时数据（这里使用模拟数据，实际应该调用realtimeDataService）
                Map<String, Object> stockData = getMockStockData(stockCode);

                // 推送数据
                int sentCount = stockWebSocketHandler.sendRealtimePrice(
                        SubscribeRequest.SecurityType.STOCK,
                        stockCode,
                        stockData
                );

                // 更新统计
                updateStatistics("STOCK", sentCount);

                if (sentCount > 0) {
                    log.debug("推送股票数据成功，代码: {}, 发送到 {} 个会话", stockCode, sentCount);
                }
            } catch (Exception e) {
                log.error("推送股票数据失败，代码: {}", stockCode, e);
            }
        }
    }

    /**
     * 推送基金数据
     */
    private void pushFundData() {
        for (String fundCode : ACTIVE_FUNDS) {
            try {
                // 获取实时数据（这里使用模拟数据，实际应该调用realtimeDataService）
                Map<String, Object> fundData = getMockFundData(fundCode);

                // 推送数据
                int sentCount = stockWebSocketHandler.sendRealtimePrice(
                        SubscribeRequest.SecurityType.FUND,
                        fundCode,
                        fundData
                );

                // 更新统计
                updateStatistics("FUND", sentCount);

                if (sentCount > 0) {
                    log.debug("推送基金数据成功，代码: {}, 发送到 {} 个会话", fundCode, sentCount);
                }
            } catch (Exception e) {
                log.error("推送基金数据失败，代码: {}", fundCode, e);
            }
        }
    }

    /**
     * 获取模拟股票数据
     *
     * @param stockCode 股票代码
     * @return 股票数据
     */
    private Map<String, Object> getMockStockData(String stockCode) {
        Map<String, Object> data = new HashMap<>();

        // 基础信息
        data.put("code", stockCode);
        data.put("name", getStockName(stockCode));
        data.put("securityType", "STOCK");

        // 价格数据
        double basePrice = getBasePrice(stockCode);
        double change = (Math.random() - 0.5) * 2;
        double price = basePrice + change;

        data.put("price", round(price, 2));
        data.put("change", round(change, 2));
        data.put("changePercent", round((change / basePrice) * 100, 2));

        // 交易数据
        data.put("open", round(basePrice + (Math.random() - 0.5), 2));
        data.put("high", round(basePrice + Math.random(), 2));
        data.put("low", round(basePrice - Math.random(), 2));
        data.put("preClose", round(basePrice, 2));

        data.put("volume", (int) (Math.random() * 1000000));
        data.put("amount", round(Math.random() * 10000000, 2));

        // 时间戳
        data.put("timestamp", System.currentTimeMillis());

        return data;
    }

    /**
     * 获取模拟基金数据
     *
     * @param fundCode 基金代码
     * @return 基金数据
     */
    private Map<String, Object> getMockFundData(String fundCode) {
        Map<String, Object> data = new HashMap<>();

        // 基础信息
        data.put("code", fundCode);
        data.put("name", getFundName(fundCode));
        data.put("securityType", "FUND");

        // NAV数据
        double baseNav = getBaseNav(fundCode);
        double change = (Math.random() - 0.5) * 0.1;
        double nav = baseNav + change;

        data.put("nav", round(nav, 4));
        data.put("change", round(change, 4));
        data.put("changePercent", round((change / baseNav) * 100, 2));

        // 估值数据
        data.put("estimatedNav", round(nav + (Math.random() - 0.5) * 0.05, 4));
        data.put("estimatedChangePercent", round((Math.random() - 0.5) * 2, 2));

        // 时间戳
        data.put("timestamp", System.currentTimeMillis());

        return data;
    }

    /**
     * 获取股票名称
     *
     * @param stockCode 股票代码
     * @return 股票名称
     */
    private String getStockName(String stockCode) {
        Map<String, String> stockNames = new HashMap<>();
        stockNames.put("000001", "平安银行");
        stockNames.put("000002", "万科A");
        stockNames.put("000858", "五粮液");
        stockNames.put("600519", "贵州茅台");
        stockNames.put("300750", "宁德时代");

        return stockNames.getOrDefault(stockCode, "未知股票");
    }

    /**
     * 获取基金名称
     *
     * @param fundCode 基金代码
     * @return 基金名称
     */
    private String getFundName(String fundCode) {
        Map<String, String> fundNames = new HashMap<>();
        fundNames.put("161725", "招商中证白酒");
        fundNames.put("110022", "易方达消费行业");
        fundNames.put("519674", "银河创新成长");
        fundNames.put("003096", "中欧医疗健康");
        fundNames.put("005827", "易方达蓝筹精选");

        return fundNames.getOrDefault(fundCode, "未知基金");
    }

    /**
     * 获取基础价格
     *
     * @param stockCode 股票代码
     * @return 基础价格
     */
    private double getBasePrice(String stockCode) {
        Map<String, Double> basePrices = new HashMap<>();
        basePrices.put("000001", 10.5);
        basePrices.put("000002", 8.2);
        basePrices.put("000858", 150.0);
        basePrices.put("600519", 1700.0);
        basePrices.put("300750", 200.0);

        return basePrices.getOrDefault(stockCode, 10.0);
    }

    /**
     * 获取基础净值
     *
     * @param fundCode 基金代码
     * @return 基础净值
     */
    private double getBaseNav(String fundCode) {
        Map<String, Double> baseNavs = new HashMap<>();
        baseNavs.put("161725", 1.2);
        baseNavs.put("110022", 3.5);
        baseNavs.put("519674", 5.8);
        baseNavs.put("003096", 2.3);
        baseNavs.put("005827", 2.1);

        return baseNavs.getOrDefault(fundCode, 1.0);
    }

    /**
     * 四舍五入
     *
     * @param value 值
     * @param places 小数位数
     * @return 四舍五入后的值
     */
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /**
     * 更新统计信息
     *
     * @param type 类型
     * @param count 数量
     */
    private void updateStatistics(String type, int count) {
        pushStatistics
                .computeIfAbsent(type, k -> new AtomicInteger(0))
                .addAndGet(count);
    }

    /**
     * 记录统计信息
     */
    private void logStatistics() {
        if (log.isDebugEnabled()) {
            int totalPushes = pushStatistics.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum();

            if (totalPushes > 0) {
                log.debug("推送统计 - 总推送次数: {}", totalPushes);
                pushStatistics.forEach((type, count) -> {
                    if (count.get() > 0) {
                        log.debug("  {}: {}", type, count.get());
                    }
                });

                // 重置统计（每小时重置一次）
                if (System.currentTimeMillis() % 3600000 < 3000) {
                    pushStatistics.clear();
                    log.info("推送统计已重置");
                }
            }
        }
    }

    /**
     * 获取推送统计
     *
     * @return 推送统计
     */
    public Map<String, Integer> getPushStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        pushStatistics.forEach((type, count) -> stats.put(type, count.get()));
        return stats;
    }

    /**
     * 手动触发数据推送（用于测试）
     *
     * @param securityType 证券类型
     * @param code 证券代码
     * @return 是否成功
     */
    public boolean triggerManualPush(String securityType, String code) {
        try {
            SubscribeRequest.SecurityType type = SubscribeRequest.SecurityType.valueOf(securityType.toUpperCase());

            Map<String, Object> data;
            if (type == SubscribeRequest.SecurityType.STOCK) {
                data = getMockStockData(code);
            } else {
                data = getMockFundData(code);
            }

            int sentCount = stockWebSocketHandler.sendRealtimePrice(type, code, data);
            log.info("手动触发推送成功，类型: {}, 代码: {}, 发送到 {} 个会话", type, code, sentCount);
            return sentCount > 0;
        } catch (Exception e) {
            log.error("手动触发推送失败，类型: {}, 代码: {}", securityType, code, e);
            return false;
        }
    }
}