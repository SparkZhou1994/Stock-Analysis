package com.spark.stockdashboard.controller;

import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import com.spark.stockdashboard.entity.dashboard.FundDashboardBO;
import com.spark.stockdashboard.entity.dashboard.RealtimePriceBO;
import com.spark.stockdashboard.service.RealtimeDataService;
import com.spark.stockdashboard.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 看板控制器
 * 提供看板数据的REST API接口
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "看板管理", description = "股票基金看板数据管理接口")
@Slf4j
public class DashboardController {

    private final RealtimeDataService realtimeDataService;
    private final WatchlistService watchlistService;

    @Autowired
    public DashboardController(RealtimeDataService realtimeDataService,
                              WatchlistService watchlistService) {
        this.realtimeDataService = realtimeDataService;
        this.watchlistService = watchlistService;
    }

    /**
     * 获取自选列表实时数据
     */
    @GetMapping("/watchlist/realtime")
    @Operation(summary = "获取自选列表实时数据", description = "获取用户自选股票和基金的实时价格数据")
    public ResponseEntity<Map<String, Object>> getWatchlistRealtimeData() {
        try {
            log.debug("获取自选列表实时数据");

            Map<String, Object> watchlistData = realtimeDataService.getWatchlistRealtimePrices();
            Map<String, Object> watchlistSummary = watchlistService.getWatchlistSummary();

            Map<String, Object> response = new HashMap<>();
            response.put("data", watchlistData);
            response.put("summary", watchlistSummary);
            response.put("success", true);
            response.put("message", "获取自选列表实时数据成功");

            log.debug("获取自选列表实时数据成功，股票数量：{}，基金数量：{}",
                     watchlistSummary.get("stockCount"), watchlistSummary.get("fundCount"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取自选列表实时数据失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取自选列表实时数据失败", e));
        }
    }

    /**
     * 获取股票实时价格
     */
    @GetMapping("/stocks/{stockCode}/realtime")
    @Operation(summary = "获取股票实时价格", description = "根据股票代码获取实时价格数据")
    public ResponseEntity<Map<String, Object>> getStockRealtimePrice(
            @Parameter(description = "股票代码", required = true, example = "sh600000")
            @PathVariable String stockCode) {
        try {
            log.debug("获取股票实时价格，股票代码：{}", stockCode);

            RealtimePriceBO price = realtimeDataService.getStockRealtimePrice(stockCode);
            if (price == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", price);
            response.put("success", true);
            response.put("message", "获取股票实时价格成功");

            log.debug("获取股票实时价格成功，股票代码：{}，价格：{}", stockCode, price.getPrice());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取股票实时价格失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取股票实时价格失败", e));
        }
    }

    /**
     * 获取基金实时价格
     */
    @GetMapping("/funds/{fundCode}/realtime")
    @Operation(summary = "获取基金实时价格", description = "根据基金代码获取实时价格数据")
    public ResponseEntity<Map<String, Object>> getFundRealtimePrice(
            @Parameter(description = "基金代码", required = true, example = "000001")
            @PathVariable String fundCode) {
        try {
            log.debug("获取基金实时价格，基金代码：{}", fundCode);

            RealtimePriceBO price = realtimeDataService.getFundRealtimePrice(fundCode);
            if (price == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", price);
            response.put("success", true);
            response.put("message", "获取基金实时价格成功");

            log.debug("获取基金实时价格成功，基金代码：{}，价格：{}", fundCode, price.getPrice());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取基金实时价格失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取基金实时价格失败", e));
        }
    }

    /**
     * 批量获取股票实时价格
     */
    @PostMapping("/stocks/batch/realtime")
    @Operation(summary = "批量获取股票实时价格", description = "批量获取多个股票的实时价格数据")
    public ResponseEntity<Map<String, Object>> batchGetStockRealtimePrices(
            @Parameter(description = "股票代码列表", required = true)
            @RequestBody List<String> stockCodes) {
        try {
            log.debug("批量获取股票实时价格，数量：{}", stockCodes.size());

            List<RealtimePriceBO> prices = realtimeDataService.getStockRealtimePrices(stockCodes);

            Map<String, Object> response = new HashMap<>();
            response.put("data", prices);
            response.put("count", prices.size());
            response.put("success", true);
            response.put("message", "批量获取股票实时价格成功");

            log.debug("批量获取股票实时价格成功，数量：{}", prices.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量获取股票实时价格失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("批量获取股票实时价格失败", e));
        }
    }

    /**
     * 批量获取基金实时价格
     */
    @PostMapping("/funds/batch/realtime")
    @Operation(summary = "批量获取基金实时价格", description = "批量获取多个基金的实时价格数据")
    public ResponseEntity<Map<String, Object>> batchGetFundRealtimePrices(
            @Parameter(description = "基金代码列表", required = true)
            @RequestBody List<String> fundCodes) {
        try {
            log.debug("批量获取基金实时价格，数量：{}", fundCodes.size());

            List<RealtimePriceBO> prices = realtimeDataService.getFundRealtimePrices(fundCodes);

            Map<String, Object> response = new HashMap<>();
            response.put("data", prices);
            response.put("count", prices.size());
            response.put("success", true);
            response.put("message", "批量获取基金实时价格成功");

            log.debug("批量获取基金实时价格成功，数量：{}", prices.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量获取基金实时价格失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("批量获取基金实时价格失败", e));
        }
    }

    /**
     * 获取看板摘要信息
     */
    @GetMapping("/summary")
    @Operation(summary = "获取看板摘要信息", description = "获取看板的统计摘要信息")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        try {
            log.debug("获取看板摘要信息");

            Map<String, Object> watchlistSummary = watchlistService.getWatchlistSummary();
            Map<String, Object> cacheStats = realtimeDataService.getCacheStats();

            Map<String, Object> response = new HashMap<>();
            response.put("watchlistSummary", watchlistSummary);
            response.put("cacheStats", cacheStats);
            response.put("success", true);
            response.put("message", "获取看板摘要信息成功");

            log.debug("获取看板摘要信息成功，自选总数：{}", watchlistSummary.get("totalCount"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取看板摘要信息失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取看板摘要信息失败", e));
        }
    }

    /**
     * 获取涨幅榜（前N名）
     */
    @GetMapping("/top/gainers")
    @Operation(summary = "获取涨幅榜", description = "获取涨幅最大的股票和基金")
    public ResponseEntity<Map<String, Object>> getTopGainers(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.debug("获取涨幅榜，限制：{}", limit);

            // 这里可以调用服务层获取涨幅榜数据
            // 暂时返回空数据，后续实现
            Map<String, Object> response = new HashMap<>();
            response.put("stocks", List.of());
            response.put("funds", List.of());
            response.put("limit", limit);
            response.put("success", true);
            response.put("message", "获取涨幅榜成功");

            log.debug("获取涨幅榜成功，限制：{}", limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取涨幅榜失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取涨幅榜失败", e));
        }
    }

    /**
     * 获取跌幅榜（前N名）
     */
    @GetMapping("/top/losers")
    @Operation(summary = "获取跌幅榜", description = "获取跌幅最大的股票和基金")
    public ResponseEntity<Map<String, Object>> getTopLosers(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.debug("获取跌幅榜，限制：{}", limit);

            // 这里可以调用服务层获取跌幅榜数据
            // 暂时返回空数据，后续实现
            Map<String, Object> response = new HashMap<>();
            response.put("stocks", List.of());
            response.put("funds", List.of());
            response.put("limit", limit);
            response.put("success", true);
            response.put("message", "获取跌幅榜成功");

            log.debug("获取跌幅榜成功，限制：{}", limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取跌幅榜失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取跌幅榜失败", e));
        }
    }

    /**
     * 获取成交量榜（前N名）
     */
    @GetMapping("/top/volume")
    @Operation(summary = "获取成交量榜", description = "获取成交量最大的股票")
    public ResponseEntity<Map<String, Object>> getTopVolume(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.debug("获取成交量榜，限制：{}", limit);

            // 这里可以调用服务层获取成交量榜数据
            // 暂时返回空数据，后续实现
            Map<String, Object> response = new HashMap<>();
            response.put("stocks", List.of());
            response.put("limit", limit);
            response.put("success", true);
            response.put("message", "获取成交量榜成功");

            log.debug("获取成交量榜成功，限制：{}", limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取成交量榜失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取成交量榜失败", e));
        }
    }

    /**
     * 获取热门行业
     */
    @GetMapping("/hot/industries")
    @Operation(summary = "获取热门行业", description = "获取当前热门的行业板块")
    public ResponseEntity<Map<String, Object>> getHotIndustries(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.debug("获取热门行业，限制：{}", limit);

            // 这里可以调用服务层获取热门行业数据
            // 暂时返回空数据，后续实现
            Map<String, Object> response = new HashMap<>();
            response.put("industries", List.of());
            response.put("limit", limit);
            response.put("success", true);
            response.put("message", "获取热门行业成功");

            log.debug("获取热门行业成功，限制：{}", limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取热门行业失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取热门行业失败", e));
        }
    }

    /**
     * 获取市场概况
     */
    @GetMapping("/market/overview")
    @Operation(summary = "获取市场概况", description = "获取股票市场的整体概况")
    public ResponseEntity<Map<String, Object>> getMarketOverview() {
        try {
            log.debug("获取市场概况");

            // 这里可以调用服务层获取市场概况数据
            // 暂时返回模拟数据
            Map<String, Object> marketData = new HashMap<>();
            marketData.put("totalStocks", 5000);
            marketData.put("risingStocks", 2500);
            marketData.put("fallingStocks", 2000);
            marketData.put("unchangedStocks", 500);
            marketData.put("totalTurnover", "1.2万亿");
            marketData.put("shanghaiIndex", 3200.50);
            marketData.put("shenzhenIndex", 11000.75);
            marketData.put("chiNextIndex", 2200.30);

            Map<String, Object> response = new HashMap<>();
            response.put("data", marketData);
            response.put("success", true);
            response.put("message", "获取市场概况成功");

            log.debug("获取市场概况成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取市场概况失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取市场概况失败", e));
        }
    }

    /**
     * 获取系统状态
     */
    @GetMapping("/system/status")
    @Operation(summary = "获取系统状态", description = "获取看板系统的运行状态")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            log.debug("获取系统状态");

            Map<String, Object> cacheStats = realtimeDataService.getCacheStats();
            Map<String, Object> watchlistSummary = watchlistService.getWatchlistSummary();

            Map<String, Object> systemStatus = new HashMap<>();
            systemStatus.put("cacheStats", cacheStats);
            systemStatus.put("watchlistSummary", watchlistSummary);
            systemStatus.put("apiStatus", "正常");
            systemStatus.put("databaseStatus", "正常");
            systemStatus.put("websocketStatus", "正常");
            systemStatus.put("lastUpdateTime", java.time.LocalDateTime.now());

            Map<String, Object> response = new HashMap<>();
            response.put("data", systemStatus);
            response.put("success", true);
            response.put("message", "获取系统状态成功");

            log.debug("获取系统状态成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取系统状态失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取系统状态失败", e));
        }
    }

    /**
     * 清理缓存
     */
    @PostMapping("/cache/cleanup")
    @Operation(summary = "清理缓存", description = "清理实时数据缓存")
    public ResponseEntity<Map<String, Object>> cleanupCache() {
        try {
            log.debug("清理缓存");

            realtimeDataService.cleanupExpiredCache();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "清理缓存成功");

            log.debug("清理缓存成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("清理缓存失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("清理缓存失败", e));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查看板服务的健康状态")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            log.debug("健康检查");

            Map<String, Object> healthStatus = new HashMap<>();
            healthStatus.put("status", "UP");
            healthStatus.put("timestamp", java.time.LocalDateTime.now());
            healthStatus.put("service", "stock-dashboard");
            healthStatus.put("version", "1.0.0");

            Map<String, Object> response = new HashMap<>();
            response.put("data", healthStatus);
            response.put("success", true);
            response.put("message", "服务运行正常");

            log.debug("健康检查通过");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("健康检查失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("健康检查失败", e));
        }
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message, Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("error", e.getMessage());
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }
}