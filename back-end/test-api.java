package com.spark.test;

import com.spark.stockdashboard.service.EastMoneyApiClient;
import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import org.springframework.web.client.RestTemplate;

public class TestEastMoneyApi {
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        EastMoneyApiClient apiClient = new EastMoneyApiClient(restTemplate);

        System.out.println("测试东方财富API连接...");
        boolean connected = apiClient.testConnection();
        System.out.println("连接状态: " + connected);

        if (connected) {
            System.out.println("\n获取上证指数实时数据...");
            StockDashboardBO shIndex = apiClient.fetchStockRealtimeData("sh000001");
            if (shIndex != null) {
                System.out.println("股票名称: " + shIndex.getStockName());
                System.out.println("当前价格: " + shIndex.getCurrentPrice());
                System.out.println("涨跌额: " + shIndex.getChangeAmount());
                System.out.println("涨跌幅: " + shIndex.getChangePercent() + "%");
                System.out.println("开盘价: " + shIndex.getOpenPrice());
                System.out.println("最高价: " + shIndex.getHighPrice());
                System.out.println("最低价: " + shIndex.getLowPrice());
                System.out.println("昨收价: " + shIndex.getPreviousClose());
                System.out.println("成交量: " + shIndex.getVolume());
                System.out.println("成交额: " + shIndex.getTurnover());
                System.out.println("更新时间: " + shIndex.getUpdateTime());
            } else {
                System.out.println("获取数据失败");
            }
        }
    }
}
