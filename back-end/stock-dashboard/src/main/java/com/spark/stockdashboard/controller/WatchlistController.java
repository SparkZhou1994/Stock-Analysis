package com.spark.stockdashboard.controller;

import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import com.spark.stockdashboard.entity.dashboard.FundDashboardBO;
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
 * 自选管理控制器
 * 提供自选股票和基金的管理接口
 */
@RestController
@RequestMapping("/api/watchlist")
@Tag(name = "自选管理", description = "自选股票和基金的管理接口")
@Slf4j
public class WatchlistController {

    private final WatchlistService watchlistService;

    @Autowired
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    /**
     * 获取自选股票列表
     */
    @GetMapping("/stocks")
    @Operation(summary = "获取自选股票列表", description = "获取用户的自选股票列表，按自选顺序排序")
    public ResponseEntity<Map<String, Object>> getWatchlistStocks() {
        try {
            log.debug("获取自选股票列表");

            List<StockDashboardBO> watchlistStocks = watchlistService.getWatchlistStocks();

            Map<String, Object> response = new HashMap<>();
            response.put("data", watchlistStocks);
            response.put("count", watchlistStocks.size());
            response.put("success", true);
            response.put("message", "获取自选股票列表成功");

            log.debug("获取自选股票列表成功，数量：{}", watchlistStocks.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取自选股票列表失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取自选股票列表失败", e));
        }
    }

    /**
     * 获取自选基金列表
     */
    @GetMapping("/funds")
    @Operation(summary = "获取自选基金列表", description = "获取用户的自选基金列表，按自选顺序排序")
    public ResponseEntity<Map<String, Object>> getWatchlistFunds() {
        try {
            log.debug("获取自选基金列表");

            List<FundDashboardBO> watchlistFunds = watchlistService.getWatchlistFunds();

            Map<String, Object> response = new HashMap<>();
            response.put("data", watchlistFunds);
            response.put("count", watchlistFunds.size());
            response.put("success", true);
            response.put("message", "获取自选基金列表成功");

            log.debug("获取自选基金列表成功，数量：{}", watchlistFunds.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取自选基金列表失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取自选基金列表失败", e));
        }
    }

    /**
     * 获取自选列表摘要
     */
    @GetMapping("/summary")
    @Operation(summary = "获取自选列表摘要", description = "获取自选列表的统计摘要信息")
    public ResponseEntity<Map<String, Object>> getWatchlistSummary() {
        try {
            log.debug("获取自选列表摘要");

            Map<String, Object> summary = watchlistService.getWatchlistSummary();

            Map<String, Object> response = new HashMap<>();
            response.put("data", summary);
            response.put("success", true);
            response.put("message", "获取自选列表摘要成功");

            log.debug("获取自选列表摘要成功，总数：{}", summary.get("totalCount"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取自选列表摘要失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取自选列表摘要失败", e));
        }
    }

    /**
     * 添加股票到自选
     */
    @PostMapping("/stocks/{stockCode}")
    @Operation(summary = "添加股票到自选", description = "将指定股票添加到自选列表")
    public ResponseEntity<Map<String, Object>> addStockToWatchlist(
            @Parameter(description = "股票代码", required = true, example = "sh600000")
            @PathVariable String stockCode) {
        try {
            log.debug("添加股票到自选，股票代码：{}", stockCode);

            boolean success = watchlistService.addStockToWatchlist(stockCode);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "添加股票到自选成功");
                response.put("stockCode", stockCode);
                log.info("添加股票到自选成功，股票代码：{}", stockCode);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "添加股票到自选失败");
                response.put("stockCode", stockCode);
                log.warn("添加股票到自选失败，股票代码：{}", stockCode);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("添加股票到自选异常，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("添加股票到自选失败", e));
        }
    }

    /**
     * 添加基金到自选
     */
    @PostMapping("/funds/{fundCode}")
    @Operation(summary = "添加基金到自选", description = "将指定基金添加到自选列表")
    public ResponseEntity<Map<String, Object>> addFundToWatchlist(
            @Parameter(description = "基金代码", required = true, example = "000001")
            @PathVariable String fundCode) {
        try {
            log.debug("添加基金到自选，基金代码：{}", fundCode);

            boolean success = watchlistService.addFundToWatchlist(fundCode);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "添加基金到自选成功");
                response.put("fundCode", fundCode);
                log.info("添加基金到自选成功，基金代码：{}", fundCode);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "添加基金到自选失败");
                response.put("fundCode", fundCode);
                log.warn("添加基金到自选失败，基金代码：{}", fundCode);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("添加基金到自选异常，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("添加基金到自选失败", e));
        }
    }

    /**
     * 从自选移除股票
     */
    @DeleteMapping("/stocks/{stockCode}")
    @Operation(summary = "从自选移除股票", description = "将指定股票从自选列表中移除")
    public ResponseEntity<Map<String, Object>> removeStockFromWatchlist(
            @Parameter(description = "股票代码", required = true, example = "sh600000")
            @PathVariable String stockCode) {
        try {
            log.debug("从自选移除股票，股票代码：{}", stockCode);

            boolean success = watchlistService.removeStockFromWatchlist(stockCode);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "从自选移除股票成功");
                response.put("stockCode", stockCode);
                log.info("从自选移除股票成功，股票代码：{}", stockCode);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "从自选移除股票失败");
                response.put("stockCode", stockCode);
                log.warn("从自选移除股票失败，股票代码：{}", stockCode);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("从自选移除股票异常，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("从自选移除股票失败", e));
        }
    }

    /**
     * 从自选移除基金
     */
    @DeleteMapping("/funds/{fundCode}")
    @Operation(summary = "从自选移除基金", description = "将指定基金从自选列表中移除")
    public ResponseEntity<Map<String, Object>> removeFundFromWatchlist(
            @Parameter(description = "基金代码", required = true, example = "000001")
            @PathVariable String fundCode) {
        try {
            log.debug("从自选移除基金，基金代码：{}", fundCode);

            boolean success = watchlistService.removeFundFromWatchlist(fundCode);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "从自选移除基金成功");
                response.put("fundCode", fundCode);
                log.info("从自选移除基金成功，基金代码：{}", fundCode);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "从自选移除基金失败");
                response.put("fundCode", fundCode);
                log.warn("从自选移除基金失败，基金代码：{}", fundCode);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("从自选移除基金异常，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("从自选移除基金失败", e));
        }
    }

    /**
     * 调整股票自选顺序
     */
    @PutMapping("/stocks/reorder")
    @Operation(summary = "调整股票自选顺序", description = "调整自选股票的显示顺序")
    public ResponseEntity<Map<String, Object>> reorderWatchlistStocks(
            @Parameter(description = "按新顺序排列的股票代码列表", required = true)
            @RequestBody List<String> stockCodes) {
        try {
            log.debug("调整股票自选顺序，数量：{}", stockCodes.size());

            boolean success = watchlistService.reorderWatchlistStocks(stockCodes);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "调整股票自选顺序成功");
                response.put("stockCodes", stockCodes);
                log.info("调整股票自选顺序成功，数量：{}", stockCodes.size());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "调整股票自选顺序失败");
                response.put("stockCodes", stockCodes);
                log.warn("调整股票自选顺序失败，数量：{}", stockCodes.size());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("调整股票自选顺序异常，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("调整股票自选顺序失败", e));
        }
    }

    /**
     * 调整基金自选顺序
     */
    @PutMapping("/funds/reorder")
    @Operation(summary = "调整基金自选顺序", description = "调整自选基金的显示顺序")
    public ResponseEntity<Map<String, Object>> reorderWatchlistFunds(
            @Parameter(description = "按新顺序排列的基金代码列表", required = true)
            @RequestBody List<String> fundCodes) {
        try {
            log.debug("调整基金自选顺序，数量：{}", fundCodes.size());

            boolean success = watchlistService.reorderWatchlistFunds(fundCodes);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "调整基金自选顺序成功");
                response.put("fundCodes", fundCodes);
                log.info("调整基金自选顺序成功，数量：{}", fundCodes.size());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "调整基金自选顺序失败");
                response.put("fundCodes", fundCodes);
                log.warn("调整基金自选顺序失败，数量：{}", fundCodes.size());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("调整基金自选顺序异常，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("调整基金自选顺序失败", e));
        }
    }

    /**
     * 搜索股票
     */
    @GetMapping("/stocks/search")
    @Operation(summary = "搜索股票", description = "根据关键词搜索股票")
    public ResponseEntity<Map<String, Object>> searchStocks(
            @Parameter(description = "搜索关键词（代码或名称）", required = true, example = "茅台")
            @RequestParam String keyword,
            @Parameter(description = "返回结果数量限制", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.debug("搜索股票，关键词：{}，限制：{}", keyword, limit);

            List<StockDashboardBO> results = watchlistService.searchStocks(keyword, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("data", results);
            response.put("count", results.size());
            response.put("keyword", keyword);
            response.put("limit", limit);
            response.put("success", true);
            response.put("message", "搜索股票成功");

            log.debug("搜索股票成功，关键词：{}，结果数量：{}", keyword, results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索股票失败，关键词：{}，错误：{}", keyword, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("搜索股票失败", e));
        }
    }

    /**
     * 搜索基金
     */
    @GetMapping("/funds/search")
    @Operation(summary = "搜索基金", description = "根据关键词搜索基金")
    public ResponseEntity<Map<String, Object>> searchFunds(
            @Parameter(description = "搜索关键词（代码或名称）", required = true, example = "华夏")
            @RequestParam String keyword,
            @Parameter(description = "返回结果数量限制", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.debug("搜索基金，关键词：{}，限制：{}", keyword, limit);

            List<FundDashboardBO> results = watchlistService.searchFunds(keyword, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("data", results);
            response.put("count", results.size());
            response.put("keyword", keyword);
            response.put("limit", limit);
            response.put("success", true);
            response.put("message", "搜索基金成功");

            log.debug("搜索基金成功，关键词：{}，结果数量：{}", keyword, results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索基金失败，关键词：{}，错误：{}", keyword, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("搜索基金失败", e));
        }
    }

    /**
     * 批量添加股票到自选
     */
    @PostMapping("/stocks/batch")
    @Operation(summary = "批量添加股票到自选", description = "批量添加多个股票到自选列表")
    public ResponseEntity<Map<String, Object>> batchAddStocksToWatchlist(
            @Parameter(description = "股票代码列表", required = true)
            @RequestBody List<String> stockCodes) {
        try {
            log.debug("批量添加股票到自选，数量：{}", stockCodes.size());

            int successCount = watchlistService.batchAddStocksToWatchlist(stockCodes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "批量添加股票到自选完成");
            response.put("totalCount", stockCodes.size());
            response.put("successCount", successCount);
            response.put("failedCount", stockCodes.size() - successCount);

            log.info("批量添加股票到自选完成，总数：{}，成功：{}", stockCodes.size(), successCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量添加股票到自选失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("批量添加股票到自选失败", e));
        }
    }

    /**
     * 批量添加基金到自选
     */
    @PostMapping("/funds/batch")
    @Operation(summary = "批量添加基金到自选", description = "批量添加多个基金到自选列表")
    public ResponseEntity<Map<String, Object>> batchAddFundsToWatchlist(
            @Parameter(description = "基金代码列表", required = true)
            @RequestBody List<String> fundCodes) {
        try {
            log.debug("批量添加基金到自选，数量：{}", fundCodes.size());

            int successCount = watchlistService.batchAddFundsToWatchlist(fundCodes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "批量添加基金到自选完成");
            response.put("totalCount", fundCodes.size());
            response.put("successCount", successCount);
            response.put("failedCount", fundCodes.size() - successCount);

            log.info("批量添加基金到自选完成，总数：{}，成功：{}", fundCodes.size(), successCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量添加基金到自选失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("批量添加基金到自选失败", e));
        }
    }

    /**
     * 清空自选列表
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空自选列表", description = "清空所有自选股票和基金")
    public ResponseEntity<Map<String, Object>> clearWatchlist() {
        try {
            log.debug("清空自选列表");

            boolean success = watchlistService.clearWatchlist();

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "清空自选列表成功");
                log.info("清空自选列表成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "清空自选列表失败");
                log.warn("清空自选列表失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("清空自选列表失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("清空自选列表失败", e));
        }
    }

    /**
     * 检查股票是否在自选列表中
     */
    @GetMapping("/stocks/{stockCode}/check")
    @Operation(summary = "检查股票是否在自选列表中", description = "检查指定股票是否在自选列表中")
    public ResponseEntity<Map<String, Object>> checkStockInWatchlist(
            @Parameter(description = "股票代码", required = true, example = "sh600000")
            @PathVariable String stockCode) {
        try {
            log.debug("检查股票是否在自选列表中，股票代码：{}", stockCode);

            boolean inWatchlist = watchlistService.isStockInWatchlist(stockCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "检查股票是否在自选列表中成功");
            response.put("stockCode", stockCode);
            response.put("inWatchlist", inWatchlist);

            log.debug("检查股票是否在自选列表中成功，股票代码：{}，结果：{}", stockCode, inWatchlist);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查股票是否在自选列表中失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("检查股票是否在自选列表中失败", e));
        }
    }

    /**
     * 检查基金是否在自选列表中
     */
    @GetMapping("/funds/{fundCode}/check")
    @Operation(summary = "检查基金是否在自选列表中", description = "检查指定基金是否在自选列表中")
    public ResponseEntity<Map<String, Object>> checkFundInWatchlist(
            @Parameter(description = "基金代码", required = true, example = "000001")
            @PathVariable String fundCode) {
        try {
            log.debug("检查基金是否在自选列表中，基金代码：{}", fundCode);

            boolean inWatchlist = watchlistService.isFundInWatchlist(fundCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "检查基金是否在自选列表中成功");
            response.put("fundCode", fundCode);
            response.put("inWatchlist", inWatchlist);

            log.debug("检查基金是否在自选列表中成功，基金代码：{}，结果：{}", fundCode, inWatchlist);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查基金是否在自选列表中失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("检查基金是否在自选列表中失败", e));
        }
    }

    /**
     * 获取自选列表统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取自选列表统计", description = "获取自选列表的详细统计信息")
    public ResponseEntity<Map<String, Object>> getWatchlistStatistics() {
        try {
            log.debug("获取自选列表统计");

            List<StockDashboardBO> watchlistStocks = watchlistService.getWatchlistStocks();
            List<FundDashboardBO> watchlistFunds = watchlistService.getWatchlistFunds();

            // 计算统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", watchlistStocks.size() + watchlistFunds.size());
            statistics.put("stockCount", watchlistStocks.size());
            statistics.put("fundCount", watchlistFunds.size());

            // 股票统计
            if (!watchlistStocks.isEmpty()) {
                double avgChangePercent = watchlistStocks.stream()
                        .filter(stock -> stock.getChangePercent() != null)
                        .mapToDouble(stock -> stock.getChangePercent().doubleValue())
                        .average()
                        .orElse(0.0);
                statistics.put("stockAvgChangePercent", avgChangePercent);
            }

            // 基金统计
            if (!watchlistFunds.isEmpty()) {
                double avgDailyChangePercent = watchlistFunds.stream()
                        .filter(fund -> fund.getDailyChangePercent() != null)
                        .mapToDouble(fund -> fund.getDailyChangePercent().doubleValue())
                        .average()
                        .orElse(0.0);
                statistics.put("fundAvgDailyChangePercent", avgDailyChangePercent);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", statistics);
            response.put("success", true);
            response.put("message", "获取自选列表统计成功");

            log.debug("获取自选列表统计成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取自选列表统计失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取自选列表统计失败", e));
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