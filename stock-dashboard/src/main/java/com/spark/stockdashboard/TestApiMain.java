package com.spark.stockdashboard;

import com.spark.stockdashboard.service.EastMoneyApiClient;
import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import org.springframework.web.client.RestTemplate;

public class TestApiMain {
    public static void main(String[] args) {
        System.out.println("测试东方财富API客户端...");

        RestTemplate restTemplate = new RestTemplate();
        EastMoneyApiClient apiClient = new EastMoneyApiClient(restTemplate);

        // 测试连接
        boolean connected = apiClient.testConnection();
        System.out.println("API连接状态: " + (connected ? "正常" : "失败"));

        if (connected) {
            // 测试获取上证指数
            StockDashboardBO shIndex = apiClient.fetchStockRealtimeData("sh000001");
            if (shIndex != null) {
                System.out.println("\n上证指数实时数据:");
                System.out.println("名称: " + shIndex.getStockName());
                System.out.println("当前价格: " + shIndex.getCurrentPrice());
                System.out.println("涨跌幅: " + shIndex.getChangePercent() + "%");
                System.out.println("涨跌额: " + shIndex.getChangeAmount());
                System.out.println("最高: " + shIndex.getHighPrice());
                System.out.println("最低: " + shIndex.getLowPrice());
                System.out.println("开盘: " + shIndex.getOpenPrice());
                System.out.println("昨收: " + shIndex.getPreviousClose());
                System.out.println("成交量: " + shIndex.getVolume());
                System.out.println("成交额: " + shIndex.getTurnover());
                System.out.println("更新时间: " + shIndex.getUpdateTime());
            } else {
                System.out.println("获取上证指数数据失败");
            }

            // 测试获取深证成指
            StockDashboardBO szIndex = apiClient.fetchStockRealtimeData("sz399001");
            if (szIndex != null) {
                System.out.println("\n深证成指实时数据:");
                System.out.println("名称: " + szIndex.getStockName());
                System.out.println("当前价格: " + szIndex.getCurrentPrice());
                System.out.println("涨跌幅: " + szIndex.getChangePercent() + "%");
            } else {
                System.out.println("获取深证成指数据失败");
            }
        }

        System.out.println("\n测试完成!");
    }
}
