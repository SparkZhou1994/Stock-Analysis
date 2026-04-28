package com.spark.stockdashboard.service;

import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import com.spark.stockdashboard.entity.dashboard.FundDashboardBO;
import com.spark.stockdashboard.entity.dashboard.HistoricalDataBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 东方财富API客户端
 * 封装对东方财富API的调用，获取股票和基金的实时数据、历史数据
 */
@Component
@Slf4j
@CacheConfig(cacheNames = "eastmoney-api")
public class EastMoneyApiClient {

    private final RestTemplate restTemplate;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${eastmoney.api.base-url:https://push2.eastmoney.com}")
    private String baseUrl;

    @Value("${eastmoney.api.timeout:5000}")
    private int timeout;

    @Value("${eastmoney.api.retry.max-attempts:3}")
    private int retryCount;

    public EastMoneyApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取股票实时数据
     * @param stockCode 股票代码（带市场前缀，如：sh600000, sz000001）
     * @return 股票实时数据
     */
    @Timed(value = "eastmoney.api.request.duration", description = "东方财富API请求耗时",
            extraTags = {"endpoint", "stock.realtime"})
    @Counted(value = "eastmoney.api.request.count", description = "东方财富API请求次数",
            extraTags = {"endpoint", "stock.realtime"})
    @Cacheable(key = "'stock:realtime:' + #stockCode", unless = "#result == null")
    @CircuitBreaker(name = "eastmoney-api", fallbackMethod = "fallbackStockRealtimeData")
    @Retryable(
            value = {
                    org.springframework.web.client.ResourceAccessException.class,
                    java.net.SocketTimeoutException.class,
                    java.net.ConnectException.class,
                    org.springframework.web.client.HttpServerErrorException.class
            },
            maxAttemptsExpression = "${eastmoney.api.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${eastmoney.api.retry.backoff-delay:1000}",
                    multiplierExpression = "${eastmoney.api.retry.backoff-multiplier:2.0}",
                    maxDelayExpression = "${eastmoney.api.retry.max-backoff-delay:10000}"
            )
    )
    public StockDashboardBO fetchStockRealtimeData(String stockCode) {
        try {
            log.debug("开始获取股票实时数据，股票代码：{}", stockCode);

            // 构建API URL
            String url = buildStockRealtimeUrl(stockCode);

            // 发送HTTP请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("获取股票实时数据失败，股票代码：{}，状态码：{}", stockCode, response.getStatusCode());
                return null;
            }

            // 解析响应数据
            Map<String, Object> responseData = response.getBody();
            StockDashboardBO stockData = parseStockRealtimeResponse(responseData, stockCode);

            log.debug("获取股票实时数据成功，股票代码：{}，股票名称：{}", stockCode, stockData.getStockName());
            return stockData;
        } catch (Exception e) {
            log.error("获取股票实时数据异常，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取基金实时数据
     * @param fundCode 基金代码
     * @return 基金实时数据
     */
    @Timed(value = "eastmoney.api.request.duration", description = "东方财富API请求耗时",
            extraTags = {"endpoint", "fund.realtime"})
    @Counted(value = "eastmoney.api.request.count", description = "东方财富API请求次数",
            extraTags = {"endpoint", "fund.realtime"})
    @Cacheable(key = "'fund:realtime:' + #fundCode", unless = "#result == null")
    @CircuitBreaker(name = "eastmoney-api", fallbackMethod = "fallbackFundRealtimeData")
    @Retryable(
            value = {
                    org.springframework.web.client.ResourceAccessException.class,
                    java.net.SocketTimeoutException.class,
                    java.net.ConnectException.class,
                    org.springframework.web.client.HttpServerErrorException.class
            },
            maxAttemptsExpression = "${eastmoney.api.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${eastmoney.api.retry.backoff-delay:1000}",
                    multiplierExpression = "${eastmoney.api.retry.backoff-multiplier:2.0}",
                    maxDelayExpression = "${eastmoney.api.retry.max-backoff-delay:10000}"
            )
    )
    public FundDashboardBO fetchFundRealtimeData(String fundCode) {
        try {
            log.debug("开始获取基金实时数据，基金代码：{}", fundCode);

            // 构建API URL
            String url = buildFundRealtimeUrl(fundCode);

            // 发送HTTP请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("获取基金实时数据失败，基金代码：{}，状态码：{}", fundCode, response.getStatusCode());
                return null;
            }

            // 解析响应数据
            Map<String, Object> responseData = response.getBody();
            FundDashboardBO fundData = parseFundRealtimeResponse(responseData, fundCode);

            log.debug("获取基金实时数据成功，基金代码：{}，基金名称：{}", fundCode, fundData.getFundName());
            return fundData;
        } catch (Exception e) {
            log.error("获取基金实时数据异常，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取股票历史数据
     * @param stockCode 股票代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 历史数据列表
     */
    @Timed(value = "eastmoney.api.request.duration", description = "东方财富API请求耗时",
            extraTags = {"endpoint", "stock.historical"})
    @Counted(value = "eastmoney.api.request.count", description = "东方财富API请求次数",
            extraTags = {"endpoint", "stock.historical"})
    @Cacheable(key = "'stock:historical:' + #stockCode + ':' + #startDate + ':' + #endDate", unless = "#result.isEmpty()")
    @CircuitBreaker(name = "eastmoney-api", fallbackMethod = "fallbackStockHistoricalData")
    @Retryable(
            value = {
                    org.springframework.web.client.ResourceAccessException.class,
                    java.net.SocketTimeoutException.class,
                    java.net.ConnectException.class,
                    org.springframework.web.client.HttpServerErrorException.class
            },
            maxAttemptsExpression = "${eastmoney.api.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${eastmoney.api.retry.backoff-delay:1000}",
                    multiplierExpression = "${eastmoney.api.retry.backoff-multiplier:2.0}",
                    maxDelayExpression = "${eastmoney.api.retry.max-backoff-delay:10000}"
            )
    )
    public List<HistoricalDataBO> fetchStockHistoricalData(String stockCode, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("开始获取股票历史数据，股票代码：{}，日期范围：{} - {}", stockCode, startDate, endDate);

            // 构建API URL
            String url = buildStockHistoricalUrl(stockCode, startDate, endDate);

            // 发送HTTP请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("获取股票历史数据失败，股票代码：{}，状态码：{}", stockCode, response.getStatusCode());
                return Collections.emptyList();
            }

            // 解析响应数据
            Map<String, Object> responseData = response.getBody();
            List<HistoricalDataBO> historicalData = parseStockHistoricalResponse(responseData, stockCode);

            log.debug("获取股票历史数据成功，股票代码：{}，数据数量：{}", stockCode, historicalData.size());
            return historicalData;
        } catch (Exception e) {
            log.error("获取股票历史数据异常，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取基金历史数据
     * @param fundCode 基金代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 历史数据列表
     */
    @Timed(value = "eastmoney.api.request.duration", description = "东方财富API请求耗时",
            extraTags = {"endpoint", "fund.historical"})
    @Counted(value = "eastmoney.api.request.count", description = "东方财富API请求次数",
            extraTags = {"endpoint", "fund.historical"})
    @Cacheable(key = "'fund:historical:' + #fundCode + ':' + #startDate + ':' + #endDate", unless = "#result.isEmpty()")
    @CircuitBreaker(name = "eastmoney-api", fallbackMethod = "fallbackFundHistoricalData")
    @Retryable(
            value = {
                    org.springframework.web.client.ResourceAccessException.class,
                    java.net.SocketTimeoutException.class,
                    java.net.ConnectException.class,
                    org.springframework.web.client.HttpServerErrorException.class
            },
            maxAttemptsExpression = "${eastmoney.api.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${eastmoney.api.retry.backoff-delay:1000}",
                    multiplierExpression = "${eastmoney.api.retry.backoff-multiplier:2.0}",
                    maxDelayExpression = "${eastmoney.api.retry.max-backoff-delay:10000}"
            )
    )
    public List<HistoricalDataBO> fetchFundHistoricalData(String fundCode, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("开始获取基金历史数据，基金代码：{}，日期范围：{} - {}", fundCode, startDate, endDate);

            // 构建API URL
            String url = buildFundHistoricalUrl(fundCode, startDate, endDate);

            // 发送HTTP请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("获取基金历史数据失败，基金代码：{}，状态码：{}", fundCode, response.getStatusCode());
                return Collections.emptyList();
            }

            // 解析响应数据
            Map<String, Object> responseData = response.getBody();
            List<HistoricalDataBO> historicalData = parseFundHistoricalResponse(responseData, fundCode);

            log.debug("获取基金历史数据成功，基金代码：{}，数据数量：{}", fundCode, historicalData.size());
            return historicalData;
        } catch (Exception e) {
            log.error("获取基金历史数据异常，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量获取股票实时数据
     * @param stockCodes 股票代码列表
     * @return 股票实时数据列表
     */
    public List<StockDashboardBO> batchFetchStockRealtimeData(List<String> stockCodes) {
        try {
            log.debug("开始批量获取股票实时数据，数量：{}", stockCodes.size());

            List<StockDashboardBO> result = new ArrayList<>();
            for (String stockCode : stockCodes) {
                try {
                    StockDashboardBO stockData = fetchStockRealtimeData(stockCode);
                    if (stockData != null) {
                        result.add(stockData);
                    }
                } catch (Exception e) {
                    log.error("批量获取股票实时数据失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
                }
            }

            log.debug("批量获取股票实时数据完成，成功：{}，总数：{}", result.size(), stockCodes.size());
            return result;
        } catch (Exception e) {
            log.error("批量获取股票实时数据异常，错误：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量获取基金实时数据
     * @param fundCodes 基金代码列表
     * @return 基金实时数据列表
     */
    public List<FundDashboardBO> batchFetchFundRealtimeData(List<String> fundCodes) {
        try {
            log.debug("开始批量获取基金实时数据，数量：{}", fundCodes.size());

            List<FundDashboardBO> result = new ArrayList<>();
            for (String fundCode : fundCodes) {
                try {
                    FundDashboardBO fundData = fetchFundRealtimeData(fundCode);
                    if (fundData != null) {
                        result.add(fundData);
                    }
                } catch (Exception e) {
                    log.error("批量获取基金实时数据失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
                }
            }

            log.debug("批量获取基金实时数据完成，成功：{}，总数：{}", result.size(), fundCodes.size());
            return result;
        } catch (Exception e) {
            log.error("批量获取基金实时数据异常，错误：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 搜索股票
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    public List<Map<String, Object>> searchStocks(String keyword) {
        try {
            log.debug("开始搜索股票，关键词：{}", keyword);

            // 构建搜索URL
            String url = buildStockSearchUrl(keyword);

            // 发送HTTP请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("搜索股票失败，关键词：{}，状态码：{}", keyword, response.getStatusCode());
                return Collections.emptyList();
            }

            // 解析响应数据
            Map<String, Object> responseData = response.getBody();
            List<Map<String, Object>> searchResults = parseStockSearchResponse(responseData);

            log.debug("搜索股票成功，关键词：{}，结果数量：{}", keyword, searchResults.size());
            return searchResults;
        } catch (Exception e) {
            log.error("搜索股票异常，关键词：{}，错误：{}", keyword, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 搜索基金
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    public List<Map<String, Object>> searchFunds(String keyword) {
        try {
            log.debug("开始搜索基金，关键词：{}", keyword);

            // 构建搜索URL
            String url = buildFundSearchUrl(keyword);

            // 发送HTTP请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("搜索基金失败，关键词：{}，状态码：{}", keyword, response.getStatusCode());
                return Collections.emptyList();
            }

            // 解析响应数据
            Map<String, Object> responseData = response.getBody();
            List<Map<String, Object>> searchResults = parseFundSearchResponse(responseData);

            log.debug("搜索基金成功，关键词：{}，结果数量：{}", keyword, searchResults.size());
            return searchResults;
        } catch (Exception e) {
            log.error("搜索基金异常，关键词：{}，错误：{}", keyword, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 构建股票实时数据URL
     */
    private String buildStockRealtimeUrl(String stockCode) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/qt/stock/get")
                .queryParam("secid", stockCode)
                .queryParam("fields", "f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f57,f58,f59,f60,f84,f85,f86,f169,f170")
                .queryParam("invt", "2")
                .build()
                .toUriString();
    }

    /**
     * 构建基金实时数据URL
     */
    private String buildFundRealtimeUrl(String fundCode) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/qt/fund/get")
                .queryParam("secid", "f." + fundCode)
                .queryParam("fields", "f43,f44,f45,f46,f47,f48,f49,f50,f57,f58,f59,f60,f86,f169,f170")
                .queryParam("invt", "2")
                .build()
                .toUriString();
    }

    /**
     * 构建股票历史数据URL
     */
    private String buildStockHistoricalUrl(String stockCode, LocalDate startDate, LocalDate endDate) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/qt/stock/kline/get")
                .queryParam("secid", stockCode)
                .queryParam("klt", "101")  // 日K线
                .queryParam("fqt", "1")    // 前复权
                .queryParam("beg", startDate.format(dateFormatter))
                .queryParam("end", endDate.format(dateFormatter))
                .queryParam("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13")
                .build()
                .toUriString();
    }

    /**
     * 构建基金历史数据URL
     */
    private String buildFundHistoricalUrl(String fundCode, LocalDate startDate, LocalDate endDate) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/qt/fund/kline/get")
                .queryParam("secid", "f." + fundCode)
                .queryParam("klt", "101")  // 日K线
                .queryParam("fqt", "1")    // 前复权
                .queryParam("beg", startDate.format(dateFormatter))
                .queryParam("end", endDate.format(dateFormatter))
                .queryParam("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13")
                .build()
                .toUriString();
    }

    /**
     * 构建股票搜索URL
     */
    private String buildStockSearchUrl(String keyword) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/qt/stock/search")
                .queryParam("key", keyword)
                .queryParam("pageSize", "20")
                .queryParam("pageIndex", "1")
                .build()
                .toUriString();
    }

    /**
     * 构建基金搜索URL
     */
    private String buildFundSearchUrl(String keyword) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/qt/fund/search")
                .queryParam("key", keyword)
                .queryParam("pageSize", "20")
                .queryParam("pageIndex", "1")
                .build()
                .toUriString();
    }

    /**
     * 创建HTTP请求实体
     */
    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.set("Connection", "keep-alive");
        return new HttpEntity<>(headers);
    }

    /**
     * 解析股票实时数据响应
     */
    private StockDashboardBO parseStockRealtimeResponse(Map<String, Object> responseData, String stockCode) {
        try {
            Map<String, Object> data = (Map<String, Object>) responseData.get("data");
            if (data == null) {
                return null;
            }

            StockDashboardBO stock = new StockDashboardBO();
            stock.setStockCode(stockCode);
            stock.setStockName((String) data.get("f58"));  // 股票名称
            stock.setCurrentPrice(parseBigDecimal(data.get("f43")));  // 当前价格
            stock.setChangeAmount(parseBigDecimal(data.get("f170"))); // 涨跌额
            stock.setChangePercent(parseBigDecimal(data.get("f44"))); // 涨跌幅
            stock.setOpenPrice(parseBigDecimal(data.get("f46")));     // 开盘价
            stock.setHighPrice(parseBigDecimal(data.get("f44")));     // 最高价
            stock.setLowPrice(parseBigDecimal(data.get("f45")));      // 最低价
            stock.setPreviousClose(parseBigDecimal(data.get("f60"))); // 昨收
            stock.setVolume(parseLong(data.get("f47")));              // 成交量
            stock.setTurnover(parseBigDecimal(data.get("f48")));      // 成交额
            stock.setTurnoverRate(parseBigDecimal(data.get("f168"))); // 换手率
            stock.setPeRatio(parseBigDecimal(data.get("f162")));      // 市盈率
            stock.setPbRatio(parseBigDecimal(data.get("f167")));      // 市净率
            stock.setMarketCap(parseBigDecimal(data.get("f116")));    // 总市值
            stock.setCirculatingMarketCap(parseBigDecimal(data.get("f117"))); // 流通市值
            stock.setUpdateTime(LocalDateTime.now());
            stock.setDataSource("东方财富");

            return stock;
        } catch (Exception e) {
            log.error("解析股票实时数据响应失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析基金实时数据响应
     */
    private FundDashboardBO parseFundRealtimeResponse(Map<String, Object> responseData, String fundCode) {
        try {
            Map<String, Object> data = (Map<String, Object>) responseData.get("data");
            if (data == null) {
                return null;
            }

            FundDashboardBO fund = new FundDashboardBO();
            fund.setFundCode(fundCode);
            fund.setFundName((String) data.get("f58"));  // 基金名称
            fund.setCurrentNetValue(parseBigDecimal(data.get("f43")));  // 当前净值
            fund.setDailyChangeAmount(parseBigDecimal(data.get("f170"))); // 日涨跌额
            fund.setDailyChangePercent(parseBigDecimal(data.get("f44"))); // 日涨跌幅
            fund.setAccumulatedNetValue(parseBigDecimal(data.get("f60"))); // 累计净值
            fund.setUpdateTime(LocalDateTime.now());
            fund.setDataSource("东方财富");

            return fund;
        } catch (Exception e) {
            log.error("解析基金实时数据响应失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析股票历史数据响应
     */
    private List<HistoricalDataBO> parseStockHistoricalResponse(Map<String, Object> responseData, String stockCode) {
        try {
            List<Map<String, Object>> klines = (List<Map<String, Object>>) responseData.get("data");
            if (klines == null || klines.isEmpty()) {
                return Collections.emptyList();
            }

            List<HistoricalDataBO> historicalData = new ArrayList<>();
            for (Map<String, Object> kline : klines) {
                HistoricalDataBO data = new HistoricalDataBO();
                data.setCode(stockCode);
                data.setType("STOCK");
                data.setTradeDate(parseDate((String) kline.get("f1")));  // 交易日期
                data.setOpenPrice(parseBigDecimal(kline.get("f2")));     // 开盘价
                data.setClosePrice(parseBigDecimal(kline.get("f3")));    // 收盘价
                data.setHighPrice(parseBigDecimal(kline.get("f4")));     // 最高价
                data.setLowPrice(parseBigDecimal(kline.get("f5")));      // 最低价
                data.setVolume(parseLong(kline.get("f6")));              // 成交量
                data.setTurnover(parseBigDecimal(kline.get("f7")));      // 成交额
                data.setChangePercent(parseBigDecimal(kline.get("f8"))); // 涨跌幅
                data.setChangeAmount(parseBigDecimal(kline.get("f9")));  // 涨跌额
                data.setTurnoverRate(parseBigDecimal(kline.get("f10"))); // 换手率
                data.setAdjustmentType("FORWARD");                       // 前复权

                historicalData.add(data);
            }

            return historicalData;
        } catch (Exception e) {
            log.error("解析股票历史数据响应失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析基金历史数据响应
     */
    private List<HistoricalDataBO> parseFundHistoricalResponse(Map<String, Object> responseData, String fundCode) {
        try {
            List<Map<String, Object>> klines = (List<Map<String, Object>>) responseData.get("data");
            if (klines == null || klines.isEmpty()) {
                return Collections.emptyList();
            }

            List<HistoricalDataBO> historicalData = new ArrayList<>();
            for (Map<String, Object> kline : klines) {
                HistoricalDataBO data = new HistoricalDataBO();
                data.setCode(fundCode);
                data.setType("FUND");
                data.setTradeDate(parseDate((String) kline.get("f1")));  // 交易日期
                data.setOpenPrice(parseBigDecimal(kline.get("f2")));     // 开盘价
                data.setClosePrice(parseBigDecimal(kline.get("f3")));    // 收盘价
                data.setHighPrice(parseBigDecimal(kline.get("f4")));     // 最高价
                data.setLowPrice(parseBigDecimal(kline.get("f5")));      // 最低价
                data.setChangePercent(parseBigDecimal(kline.get("f8"))); // 涨跌幅
                data.setChangeAmount(parseBigDecimal(kline.get("f9")));  // 涨跌额
                data.setAdjustmentType("FORWARD");                       // 前复权

                historicalData.add(data);
            }

            return historicalData;
        } catch (Exception e) {
            log.error("解析基金历史数据响应失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析股票搜索响应
     */
    private List<Map<String, Object>> parseStockSearchResponse(Map<String, Object> responseData) {
        try {
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseData.get("data");
            if (results == null) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> searchResults = new ArrayList<>();
            for (Map<String, Object> result : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("code", result.get("f12"));      // 股票代码
                item.put("name", result.get("f14"));      // 股票名称
                item.put("market", result.get("f13"));    // 市场
                item.put("type", "STOCK");
                searchResults.add(item);
            }

            return searchResults;
        } catch (Exception e) {
            log.error("解析股票搜索响应失败，错误：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析基金搜索响应
     */
    private List<Map<String, Object>> parseFundSearchResponse(Map<String, Object> responseData) {
        try {
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseData.get("data");
            if (results == null) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> searchResults = new ArrayList<>();
            for (Map<String, Object> result : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("code", result.get("f12"));      // 基金代码
                item.put("name", result.get("f14"));      // 基金名称
                item.put("type", result.get("f3"));       // 基金类型
                item.put("market", "FUND");
                searchResults.add(item);
            }

            return searchResults;
        } catch (Exception e) {
            log.error("解析基金搜索响应失败，错误：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析BigDecimal
     */
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else if (value instanceof String) {
                String str = (String) value;
                if (str.isEmpty() || "null".equals(str)) {
                    return null;
                }
                return new BigDecimal(str);
            }
            return null;
        } catch (Exception e) {
            log.warn("解析BigDecimal失败，值：{}，错误：{}", value, e.getMessage());
            return null;
        }
    }

    /**
     * 解析Long
     */
    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                String str = (String) value;
                if (str.isEmpty() || "null".equals(str)) {
                    return null;
                }
                return Long.parseLong(str);
            }
            return null;
        } catch (Exception e) {
            log.warn("解析Long失败，值：{}，错误：{}", value, e.getMessage());
            return null;
        }
    }

    /**
     * 解析日期
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, dateFormatter);
        } catch (Exception e) {
            log.warn("解析日期失败，日期字符串：{}，错误：{}", dateStr, e.getMessage());
            return null;
        }
    }

    /**
     * 重试失败恢复方法 - 获取股票实时数据
     */
    @Recover
    public StockDashboardBO recoverStockRealtimeData(Exception e, String stockCode) {
        log.error("获取股票实时数据重试{}次后仍然失败，股票代码：{}，错误：{}",
                retryCount, stockCode, e.getMessage(), e);
        return null;
    }

    /**
     * 重试失败恢复方法 - 获取基金实时数据
     */
    @Recover
    public FundDashboardBO recoverFundRealtimeData(Exception e, String fundCode) {
        log.error("获取基金实时数据重试{}次后仍然失败，基金代码：{}，错误：{}",
                retryCount, fundCode, e.getMessage(), e);
        return null;
    }

    /**
     * 重试失败恢复方法 - 获取股票历史数据
     */
    @Recover
    public List<HistoricalDataBO> recoverStockHistoricalData(Exception e, String stockCode,
                                                              LocalDate startDate, LocalDate endDate) {
        log.error("获取股票历史数据重试{}次后仍然失败，股票代码：{}，日期范围：{} - {}，错误：{}",
                retryCount, stockCode, startDate, endDate, e.getMessage(), e);
        return Collections.emptyList();
    }

    /**
     * 重试失败恢复方法 - 获取基金历史数据
     */
    @Recover
    public List<HistoricalDataBO> recoverFundHistoricalData(Exception e, String fundCode,
                                                             LocalDate startDate, LocalDate endDate) {
        log.error("获取基金历史数据重试{}次后仍然失败，基金代码：{}，日期范围：{} - {}，错误：{}",
                retryCount, fundCode, startDate, endDate, e.getMessage(), e);
        return Collections.emptyList();
    }

    /**
     * 熔断器降级方法 - 获取股票实时数据
     */
    public StockDashboardBO fallbackStockRealtimeData(String stockCode, Exception e) {
        log.warn("熔断器触发，获取股票实时数据降级，股票代码：{}，错误：{}", stockCode, e.getMessage());
        // 返回空值或者从缓存获取数据
        return null;
    }

    /**
     * 熔断器降级方法 - 获取基金实时数据
     */
    public FundDashboardBO fallbackFundRealtimeData(String fundCode, Exception e) {
        log.warn("熔断器触发，获取基金实时数据降级，基金代码：{}，错误：{}", fundCode, e.getMessage());
        // 返回空值或者从缓存获取数据
        return null;
    }

    /**
     * 熔断器降级方法 - 获取股票历史数据
     */
    public List<HistoricalDataBO> fallbackStockHistoricalData(String stockCode, LocalDate startDate,
                                                               LocalDate endDate, Exception e) {
        log.warn("熔断器触发，获取股票历史数据降级，股票代码：{}，日期范围：{} - {}，错误：{}",
                stockCode, startDate, endDate, e.getMessage());
        // 返回空列表或者从缓存获取数据
        return Collections.emptyList();
    }

    /**
     * 熔断器降级方法 - 获取基金历史数据
     */
    public List<HistoricalDataBO> fallbackFundHistoricalData(String fundCode, LocalDate startDate,
                                                              LocalDate endDate, Exception e) {
        log.warn("熔断器触发，获取基金历史数据降级，基金代码：{}，日期范围：{} - {}，错误：{}",
                fundCode, startDate, endDate, e.getMessage());
        // 返回空列表或者从缓存获取数据
        return Collections.emptyList();
    }

    /**
     * 测试API连接
     * @return 是否连接成功
     */
    public boolean testConnection() {
        try {
            String testUrl = baseUrl + "/api/qt/stock/get?secid=sh000001&fields=f43";
            ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("测试API连接失败，错误：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取API状态
     * @return API状态信息
     */
    public Map<String, Object> getApiStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("baseUrl", baseUrl);
        status.put("timeout", timeout);
        status.put("retryCount", retryCount);
        status.put("connectionTest", testConnection());
        status.put("timestamp", LocalDateTime.now());
        return status;
    }

    /**
     * 获取API运行时指标
     * @return 指标信息
     */
    public Map<String, Object> getApiMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("cache.enabled", true);
        metrics.put("circuit.breaker.enabled", true);
        metrics.put("retry.enabled", true);
        metrics.put("metrics.enabled", true);
        // 更多指标可以通过Micrometer的MeterRegistry获取
        return metrics;
    }
}