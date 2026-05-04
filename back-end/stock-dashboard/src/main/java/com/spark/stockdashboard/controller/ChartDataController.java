package com.spark.stockdashboard.controller;

import com.spark.stockdashboard.entity.dashboard.HistoricalDataBO;
import com.spark.stockdashboard.service.TechnicalIndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 图表数据控制器
 * 提供图表数据的REST API接口，包括K线图、均线图、技术指标等
 */
@RestController
@RequestMapping("/api/chart")
@Tag(name = "图表数据", description = "股票和基金图表数据接口")
@Slf4j
public class ChartDataController {

    private final TechnicalIndicatorService technicalIndicatorService;

    @Autowired
    public ChartDataController(TechnicalIndicatorService technicalIndicatorService) {
        this.technicalIndicatorService = technicalIndicatorService;
    }

    /**
     * 获取移动平均线数据
     */
    @GetMapping("/{code}/ma")
    @Operation(summary = "获取移动平均线数据", description = "获取指定标的的移动平均线数据")
    public ResponseEntity<Map<String, Object>> getMovingAverages(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code,
            @Parameter(description = "移动平均线周期列表", example = "5,10,20,30,60")
            @RequestParam(defaultValue = "5,10,20,30,60") List<Integer> periods,
            @Parameter(description = "结束日期", example = "2024-03-09")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "计算天数", example = "60")
            @RequestParam(defaultValue = "60") int days) {
        try {
            log.debug("获取移动平均线数据，代码：{}，周期：{}，结束日期：{}，天数：{}",
                     code, periods, endDate, days);

            if (endDate == null) {
                endDate = LocalDate.now();
            }

            Map<String, List<BigDecimal>> maData = technicalIndicatorService.calculateMovingAverages(
                    code, periods, endDate, days);

            Map<String, Object> response = new HashMap<>();
            response.put("data", maData);
            response.put("code", code);
            response.put("periods", periods);
            response.put("endDate", endDate);
            response.put("days", days);
            response.put("success", true);
            response.put("message", "获取移动平均线数据成功");

            log.debug("获取移动平均线数据成功，代码：{}，周期数量：{}", code, maData.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取移动平均线数据失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取移动平均线数据失败", e));
        }
    }

    /**
     * 获取指数移动平均线数据
     */
    @GetMapping("/{code}/ema")
    @Operation(summary = "获取指数移动平均线数据", description = "获取指定标的的指数移动平均线数据")
    public ResponseEntity<Map<String, Object>> getExponentialMovingAverage(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code,
            @Parameter(description = "EMA周期", example = "12")
            @RequestParam(defaultValue = "12") int period,
            @Parameter(description = "结束日期", example = "2024-03-09")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "计算天数", example = "60")
            @RequestParam(defaultValue = "60") int days) {
        try {
            log.debug("获取指数移动平均线数据，代码：{}，周期：{}，结束日期：{}，天数：{}",
                     code, period, endDate, days);

            if (endDate == null) {
                endDate = LocalDate.now();
            }

            List<BigDecimal> emaData = technicalIndicatorService.calculateExponentialMovingAverage(
                    code, period, endDate, days);

            Map<String, Object> response = new HashMap<>();
            response.put("data", emaData);
            response.put("code", code);
            response.put("period", period);
            response.put("endDate", endDate);
            response.put("days", days);
            response.put("success", true);
            response.put("message", "获取指数移动平均线数据成功");

            log.debug("获取指数移动平均线数据成功，代码：{}，数据数量：{}", code, emaData.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取指数移动平均线数据失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取指数移动平均线数据失败", e));
        }
    }

    /**
     * 获取MACD指标数据
     */
    @GetMapping("/{code}/macd")
    @Operation(summary = "获取MACD指标数据", description = "获取指定标的的MACD指标数据")
    public ResponseEntity<Map<String, Object>> getMACD(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code,
            @Parameter(description = "快线周期", example = "12")
            @RequestParam(defaultValue = "12") int fastPeriod,
            @Parameter(description = "慢线周期", example = "26")
            @RequestParam(defaultValue = "26") int slowPeriod,
            @Parameter(description = "信号线周期", example = "9")
            @RequestParam(defaultValue = "9") int signalPeriod,
            @Parameter(description = "结束日期", example = "2024-03-09")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "计算天数", example = "60")
            @RequestParam(defaultValue = "60") int days) {
        try {
            log.debug("获取MACD指标数据，代码：{}，快线：{}，慢线：{}，信号线：{}，结束日期：{}，天数：{}",
                     code, fastPeriod, slowPeriod, signalPeriod, endDate, days);

            if (endDate == null) {
                endDate = LocalDate.now();
            }

            Map<String, List<BigDecimal>> macdData = technicalIndicatorService.calculateMACD(
                    code, fastPeriod, slowPeriod, signalPeriod, endDate, days);

            Map<String, Object> response = new HashMap<>();
            response.put("data", macdData);
            response.put("code", code);
            response.put("fastPeriod", fastPeriod);
            response.put("slowPeriod", slowPeriod);
            response.put("signalPeriod", signalPeriod);
            response.put("endDate", endDate);
            response.put("days", days);
            response.put("success", true);
            response.put("message", "获取MACD指标数据成功");

            log.debug("获取MACD指标数据成功，代码：{}，DIF数量：{}", code,
                     macdData.getOrDefault("DIF", List.of()).size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取MACD指标数据失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取MACD指标数据失败", e));
        }
    }

    /**
     * 获取RSI指标数据
     */
    @GetMapping("/{code}/rsi")
    @Operation(summary = "获取RSI指标数据", description = "获取指定标的的RSI指标数据")
    public ResponseEntity<Map<String, Object>> getRSI(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code,
            @Parameter(description = "RSI周期", example = "14")
            @RequestParam(defaultValue = "14") int period,
            @Parameter(description = "结束日期", example = "2024-03-09")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "计算天数", example = "60")
            @RequestParam(defaultValue = "60") int days) {
        try {
            log.debug("获取RSI指标数据，代码：{}，周期：{}，结束日期：{}，天数：{}",
                     code, period, endDate, days);

            if (endDate == null) {
                endDate = LocalDate.now();
            }

            List<BigDecimal> rsiData = technicalIndicatorService.calculateRSI(
                    code, period, endDate, days);

            Map<String, Object> response = new HashMap<>();
            response.put("data", rsiData);
            response.put("code", code);
            response.put("period", period);
            response.put("endDate", endDate);
            response.put("days", days);
            response.put("success", true);
            response.put("message", "获取RSI指标数据成功");

            log.debug("获取RSI指标数据成功，代码：{}，数据数量：{}", code, rsiData.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取RSI指标数据失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取RSI指标数据失败", e));
        }
    }

    /**
     * 获取布林带数据
     */
    @GetMapping("/{code}/bollinger")
    @Operation(summary = "获取布林带数据", description = "获取指定标的的布林带数据")
    public ResponseEntity<Map<String, Object>> getBollingerBands(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code,
            @Parameter(description = "布林带周期", example = "20")
            @RequestParam(defaultValue = "20") int period,
            @Parameter(description = "标准差倍数", example = "2")
            @RequestParam(defaultValue = "2") int stdDev,
            @Parameter(description = "结束日期", example = "2024-03-09")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "计算天数", example = "60")
            @RequestParam(defaultValue = "60") int days) {
        try {
            log.debug("获取布林带数据，代码：{}，周期：{}，标准差倍数：{}，结束日期：{}，天数：{}",
                     code, period, stdDev, endDate, days);

            if (endDate == null) {
                endDate = LocalDate.now();
            }

            Map<String, List<BigDecimal>> bollingerData = technicalIndicatorService.calculateBollingerBands(
                    code, period, stdDev, endDate, days);

            Map<String, Object> response = new HashMap<>();
            response.put("data", bollingerData);
            response.put("code", code);
            response.put("period", period);
            response.put("stdDev", stdDev);
            response.put("endDate", endDate);
            response.put("days", days);
            response.put("success", true);
            response.put("message", "获取布林带数据成功");

            log.debug("获取布林带数据成功，代码：{}，中轨数量：{}", code,
                     bollingerData.getOrDefault("middle", List.of()).size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取布林带数据失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取布林带数据失败", e));
        }
    }

    /**
     * 获取成交量指标数据
     */
    @GetMapping("/{code}/volume")
    @Operation(summary = "获取成交量指标数据", description = "获取指定标的的成交量指标数据")
    public ResponseEntity<Map<String, Object>> getVolumeIndicators(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code,
            @Parameter(description = "成交量周期", example = "5")
            @RequestParam(defaultValue = "5") int period,
            @Parameter(description = "结束日期", example = "2024-03-09")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "计算天数", example = "30")
            @RequestParam(defaultValue = "30") int days) {
        try {
            log.debug("获取成交量指标数据，代码：{}，周期：{}，结束日期：{}，天数：{}",
                     code, period, endDate, days);

            if (endDate == null) {
                endDate = LocalDate.now();
            }

            Map<String, Object> volumeData = technicalIndicatorService.calculateVolumeIndicators(
                    code, period, endDate, days);

            Map<String, Object> response = new HashMap<>();
            response.put("data", volumeData);
            response.put("code", code);
            response.put("period", period);
            response.put("endDate", endDate);
            response.put("days", days);
            response.put("success", true);
            response.put("message", "获取成交量指标数据成功");

            log.debug("获取成交量指标数据成功，代码：{}", code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取成交量指标数据失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取成交量指标数据失败", e));
        }
    }

    /**
     * 获取技术指标摘要
     */
    @GetMapping("/{code}/indicators/summary")
    @Operation(summary = "获取技术指标摘要", description = "获取指定标的的技术指标摘要")
    public ResponseEntity<Map<String, Object>> getTechnicalIndicatorsSummary(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code) {
        try {
            log.debug("获取技术指标摘要，代码：{}", code);

            Map<String, Object> indicatorsSummary = technicalIndicatorService.getTechnicalIndicatorsSummary(code);

            Map<String, Object> response = new HashMap<>();
            response.put("data", indicatorsSummary);
            response.put("code", code);
            response.put("success", true);
            response.put("message", "获取技术指标摘要成功");

            log.debug("获取技术指标摘要成功，代码：{}，指标数量：{}", code, indicatorsSummary.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取技术指标摘要失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取技术指标摘要失败", e));
        }
    }

    /**
     * 获取K线图数据
     */
    @GetMapping("/{code}/kline")
    @Operation(summary = "获取K线图数据", description = "获取指定标的的K线图数据")
    public ResponseEntity<Map<String, Object>> getKLineData(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code,
            @Parameter(description = "开始日期", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期", example = "2024-03-09")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "K线类型", example = "day")
            @RequestParam(defaultValue = "day") String klineType) {
        try {
            log.debug("获取K线图数据，代码：{}，开始日期：{}，结束日期：{}，K线类型：{}",
                     code, startDate, endDate, klineType);

            if (endDate == null) {
                endDate = LocalDate.now();
            }
            if (startDate == null) {
                startDate = endDate.minusMonths(3); // 默认最近3个月
            }

            // 这里可以调用服务层获取K线数据
            // 暂时返回模拟数据
            List<Map<String, Object>> klineData = generateMockKLineData(code, startDate, endDate, klineType);

            Map<String, Object> response = new HashMap<>();
            response.put("data", klineData);
            response.put("code", code);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("klineType", klineType);
            response.put("count", klineData.size());
            response.put("success", true);
            response.put("message", "获取K线图数据成功");

            log.debug("获取K线图数据成功，代码：{}，数据数量：{}", code, klineData.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取K线图数据失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取K线图数据失败", e));
        }
    }

    /**
     * 获取均线图数据
     */
    @GetMapping("/{code}/moving-average")
    @Operation(summary = "获取均线图数据", description = "获取指定标的的均线图数据")
    public ResponseEntity<Map<String, Object>> getMovingAverageChartData(
            @Parameter(description = "标的代码", required = true, example = "sh600000")
            @PathVariable String code,
            @Parameter(description = "均线周期列表", example = "5,10,20,30,60")
            @RequestParam(defaultValue = "5,10,20,30,60") List<Integer> periods,
            @Parameter(description = "开始日期", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期", example = "2024-03-09")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.debug("获取均线图数据，代码：{}，周期：{}，开始日期：{}，结束日期：{}",
                     code, periods, startDate, endDate);

            if (endDate == null) {
                endDate = LocalDate.now();
            }
            if (startDate == null) {
                startDate = endDate.minusMonths(3); // 默认最近3个月
            }

            // 计算天数
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            int days = (int) daysBetween;

            Map<String, List<BigDecimal>> maData = technicalIndicatorService.calculateMovingAverages(
                    code, periods, endDate, days);

            // 这里可以调用服务层获取历史价格数据
            // 暂时返回模拟数据
            List<Map<String, Object>> priceData = generateMockPriceData(code, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("priceData", priceData);
            response.put("maData", maData);
            response.put("code", code);
            response.put("periods", periods);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("success", true);
            response.put("message", "获取均线图数据成功");

            log.debug("获取均线图数据成功，代码：{}，周期数量：{}", code, periods.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取均线图数据失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取均线图数据失败", e));
        }
    }

    /**
     * 获取技术指标对比
     */
    @PostMapping("/compare/indicators")
    @Operation(summary = "获取技术指标对比", description = "对比多个标的的技术指标")
    public ResponseEntity<Map<String, Object>> compareTechnicalIndicators(
            @Parameter(description = "标的代码列表", required = true)
            @RequestBody List<String> codes,
            @Parameter(description = "技术指标列表", example = "MA5,MA10,RSI,MACD")
            @RequestParam(defaultValue = "MA5,MA10,RSI,MACD") List<String> indicators) {
        try {
            log.debug("获取技术指标对比，标的数量：{}，指标数量：{}", codes.size(), indicators.size());

            Map<String, Object> comparisonData = new HashMap<>();

            for (String code : codes) {
                Map<String, Object> indicatorsSummary = technicalIndicatorService.getTechnicalIndicatorsSummary(code);
                comparisonData.put(code, indicatorsSummary);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", comparisonData);
            response.put("codes", codes);
            response.put("indicators", indicators);
            response.put("success", true);
            response.put("message", "获取技术指标对比成功");

            log.debug("获取技术指标对比成功，标的数量：{}", codes.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取技术指标对比失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取技术指标对比失败", e));
        }
    }

    /**
     * 获取图表配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取图表配置", description = "获取图表的默认配置信息")
    public ResponseEntity<Map<String, Object>> getChartConfig() {
        try {
            log.debug("获取图表配置");

            Map<String, Object> config = new HashMap<>();
            config.put("colors", Arrays.asList("#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de"));
            config.put("lineWidth", 2);
            config.put("symbolSize", 4);
            config.put("animationDuration", 1000);
            config.put("grid", Map.of("left", "10%", "right", "10%", "bottom", "15%", "top", "10%"));
            config.put("tooltip", Map.of("trigger", "axis", "axisPointer", Map.of("type", "cross")));
            config.put("legend", Map.of("data", Arrays.asList("价格", "MA5", "MA10", "MA20", "成交量")));

            Map<String, Object> response = new HashMap<>();
            response.put("data", config);
            response.put("success", true);
            response.put("message", "获取图表配置成功");

            log.debug("获取图表配置成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取图表配置失败，错误：{}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取图表配置失败", e));
        }
    }

    /**
     * 生成模拟K线数据
     */
    private List<Map<String, Object>> generateMockKLineData(String code, LocalDate startDate, LocalDate endDate, String klineType) {
        List<Map<String, Object>> klineData = new ArrayList<>();

        LocalDate currentDate = startDate;
        Random random = new Random(code.hashCode());

        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek().getValue() <= 5) { // 只生成工作日数据
                double open = 100 + random.nextDouble() * 50;
                double close = open + (random.nextDouble() - 0.5) * 10;
                double high = Math.max(open, close) + random.nextDouble() * 5;
                double low = Math.min(open, close) - random.nextDouble() * 5;
                long volume = 1000000 + random.nextInt(9000000);

                Map<String, Object> kline = new HashMap<>();
                kline.put("date", currentDate.toString());
                kline.put("open", open);
                kline.put("close", close);
                kline.put("high", high);
                kline.put("low", low);
                kline.put("volume", volume);
                kline.put("code", code);

                klineData.add(kline);
            }
            currentDate = currentDate.plusDays(1);
        }

        return klineData;
    }

    /**
     * 生成模拟价格数据
     */
    private List<Map<String, Object>> generateMockPriceData(String code, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> priceData = new ArrayList<>();

        LocalDate currentDate = startDate;
        Random random = new Random(code.hashCode());
        double basePrice = 100;

        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek().getValue() <= 5) { // 只生成工作日数据
                double price = basePrice + (random.nextDouble() - 0.5) * 20;
                basePrice = price;

                Map<String, Object> pricePoint = new HashMap<>();
                pricePoint.put("date", currentDate.toString());
                pricePoint.put("price", price);
                pricePoint.put("code", code);

                priceData.add(pricePoint);
            }
            currentDate = currentDate.plusDays(1);
        }

        return priceData;
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