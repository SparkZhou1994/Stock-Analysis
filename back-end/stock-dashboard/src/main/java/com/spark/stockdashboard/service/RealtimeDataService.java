package com.spark.stockdashboard.service;

import com.spark.stockdashboard.entity.dashboard.RealtimePriceBO;
import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import com.spark.stockdashboard.entity.dashboard.FundDashboardBO;
import com.spark.stockdashboard.repository.StockDashboardRepository;
import com.spark.stockdashboard.repository.FundDashboardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 实时数据服务
 * 负责管理实时价格数据，提供实时数据查询和推送功能
 */
@Service
@Slf4j
public class RealtimeDataService {

    private final StockDashboardRepository stockDashboardRepository;
    private final FundDashboardRepository fundDashboardRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 内存缓存：code -> RealtimePriceBO
    private final Map<String, RealtimePriceBO> realtimePriceCache = new ConcurrentHashMap<>();

    // WebSocket会话管理
    private final Map<String, Set<String>> codeToSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionToCodes = new ConcurrentHashMap<>();

    @Autowired
    public RealtimeDataService(StockDashboardRepository stockDashboardRepository,
                              FundDashboardRepository fundDashboardRepository,
                              RedisTemplate<String, Object> redisTemplate) {
        this.stockDashboardRepository = stockDashboardRepository;
        this.fundDashboardRepository = fundDashboardRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取股票实时价格
     * @param stockCode 股票代码
     * @return 实时价格实体
     */
    public RealtimePriceBO getStockRealtimePrice(String stockCode) {
        try {
            // 1. 先从内存缓存获取
            RealtimePriceBO cachedPrice = realtimePriceCache.get("STOCK:" + stockCode);
            if (cachedPrice != null && isPriceFresh(cachedPrice)) {
                return cachedPrice;
            }

            // 2. 从Redis获取
            String redisKey = "realtime:stock:" + stockCode;
            RealtimePriceBO redisPrice = (RealtimePriceBO) redisTemplate.opsForValue().get(redisKey);
            if (redisPrice != null && isPriceFresh(redisPrice)) {
                // 更新内存缓存
                realtimePriceCache.put("STOCK:" + stockCode, redisPrice);
                return redisPrice;
            }

            // 3. 从数据库获取最新数据
            Optional<StockDashboardBO> stockOpt = stockDashboardRepository.findByStockCode(stockCode);
            if (stockOpt.isPresent()) {
                StockDashboardBO stock = stockOpt.get();
                RealtimePriceBO price = convertToRealtimePrice(stock);

                // 更新缓存
                updatePriceCache(price);

                return price;
            }

            return null;
        } catch (Exception e) {
            log.error("获取股票实时价格失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取基金实时价格
     * @param fundCode 基金代码
     * @return 实时价格实体
     */
    public RealtimePriceBO getFundRealtimePrice(String fundCode) {
        try {
            // 1. 先从内存缓存获取
            RealtimePriceBO cachedPrice = realtimePriceCache.get("FUND:" + fundCode);
            if (cachedPrice != null && isPriceFresh(cachedPrice)) {
                return cachedPrice;
            }

            // 2. 从Redis获取
            String redisKey = "realtime:fund:" + fundCode;
            RealtimePriceBO redisPrice = (RealtimePriceBO) redisTemplate.opsForValue().get(redisKey);
            if (redisPrice != null && isPriceFresh(redisPrice)) {
                // 更新内存缓存
                realtimePriceCache.put("FUND:" + fundCode, redisPrice);
                return redisPrice;
            }

            // 3. 从数据库获取最新数据
            Optional<FundDashboardBO> fundOpt = fundDashboardRepository.findByFundCode(fundCode);
            if (fundOpt.isPresent()) {
                FundDashboardBO fund = fundOpt.get();
                RealtimePriceBO price = convertToRealtimePrice(fund);

                // 更新缓存
                updatePriceCache(price);

                return price;
            }

            return null;
        } catch (Exception e) {
            log.error("获取基金实时价格失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 批量获取股票实时价格
     * @param stockCodes 股票代码列表
     * @return 实时价格实体列表
     */
    public List<RealtimePriceBO> getStockRealtimePrices(List<String> stockCodes) {
        try {
            List<RealtimePriceBO> result = new ArrayList<>();
            List<String> codesToFetch = new ArrayList<>();

            // 1. 先从缓存获取
            for (String stockCode : stockCodes) {
                String cacheKey = "STOCK:" + stockCode;
                RealtimePriceBO cachedPrice = realtimePriceCache.get(cacheKey);
                if (cachedPrice != null && isPriceFresh(cachedPrice)) {
                    result.add(cachedPrice);
                } else {
                    codesToFetch.add(stockCode);
                }
            }

            // 2. 批量从数据库获取剩余数据
            if (!codesToFetch.isEmpty()) {
                List<StockDashboardBO> stocks = stockDashboardRepository.findByStockCodeIn(codesToFetch);
                for (StockDashboardBO stock : stocks) {
                    RealtimePriceBO price = convertToRealtimePrice(stock);
                    result.add(price);

                    // 更新缓存
                    updatePriceCache(price);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("批量获取股票实时价格失败，错误：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量获取基金实时价格
     * @param fundCodes 基金代码列表
     * @return 实时价格实体列表
     */
    public List<RealtimePriceBO> getFundRealtimePrices(List<String> fundCodes) {
        try {
            List<RealtimePriceBO> result = new ArrayList<>();
            List<String> codesToFetch = new ArrayList<>();

            // 1. 先从缓存获取
            for (String fundCode : fundCodes) {
                String cacheKey = "FUND:" + fundCode;
                RealtimePriceBO cachedPrice = realtimePriceCache.get(cacheKey);
                if (cachedPrice != null && isPriceFresh(cachedPrice)) {
                    result.add(cachedPrice);
                } else {
                    codesToFetch.add(fundCode);
                }
            }

            // 2. 批量从数据库获取剩余数据
            if (!codesToFetch.isEmpty()) {
                List<FundDashboardBO> funds = fundDashboardRepository.findByFundCodeIn(codesToFetch);
                for (FundDashboardBO fund : funds) {
                    RealtimePriceBO price = convertToRealtimePrice(fund);
                    result.add(price);

                    // 更新缓存
                    updatePriceCache(price);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("批量获取基金实时价格失败，错误：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取自选列表实时价格
     * @return 自选股票和基金的实时价格
     */
    public Map<String, Object> getWatchlistRealtimePrices() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 获取自选股票
            List<StockDashboardBO> watchlistStocks = stockDashboardRepository.findByIsWatchlistTrueOrderByWatchlistOrderAsc();
            List<RealtimePriceBO> stockPrices = getStockRealtimePrices(
                    watchlistStocks.stream()
                            .map(StockDashboardBO::getStockCode)
                            .collect(Collectors.toList())
            );

            // 获取自选基金
            List<FundDashboardBO> watchlistFunds = fundDashboardRepository.findByIsWatchlistTrueOrderByWatchlistOrderAsc();
            List<RealtimePriceBO> fundPrices = getFundRealtimePrices(
                    watchlistFunds.stream()
                            .map(FundDashboardBO::getFundCode)
                            .collect(Collectors.toList())
            );

            result.put("stocks", stockPrices);
            result.put("funds", fundPrices);
            result.put("timestamp", LocalDateTime.now());

            return result;
        } catch (Exception e) {
            log.error("获取自选列表实时价格失败，错误：{}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 更新实时价格
     * @param price 实时价格实体
     */
    public void updateRealtimePrice(RealtimePriceBO price) {
        try {
            if (price == null || price.getCode() == null || price.getType() == null) {
                log.warn("更新实时价格失败，参数无效");
                return;
            }

            // 设置时间戳
            price.setTimestamp(LocalDateTime.now());

            // 更新缓存
            updatePriceCache(price);

            // 广播价格更新
            broadcastPriceUpdate(price);

            log.debug("更新实时价格成功，代码：{}，类型：{}，价格：{}",
                     price.getCode(), price.getType(), price.getPrice());
        } catch (Exception e) {
            log.error("更新实时价格失败，错误：{}", e.getMessage(), e);
        }
    }

    /**
     * 批量更新实时价格
     * @param prices 实时价格实体列表
     */
    public void batchUpdateRealtimePrices(List<RealtimePriceBO> prices) {
        try {
            if (prices == null || prices.isEmpty()) {
                return;
            }

            for (RealtimePriceBO price : prices) {
                try {
                    updateRealtimePrice(price);
                } catch (Exception e) {
                    log.error("批量更新实时价格失败，代码：{}，错误：{}",
                             price != null ? price.getCode() : "null", e.getMessage(), e);
                }
            }

            log.debug("批量更新实时价格完成，数量：{}", prices.size());
        } catch (Exception e) {
            log.error("批量更新实时价格失败，错误：{}", e.getMessage(), e);
        }
    }

    /**
     * 注册WebSocket会话
     * @param sessionId 会话ID
     * @param codes 关注的代码列表
     */
    public void registerSession(String sessionId, Set<String> codes) {
        try {
            // 清理旧的订阅
            unregisterSession(sessionId);

            // 注册新的订阅
            sessionToCodes.put(sessionId, codes);
            for (String code : codes) {
                codeToSessions.computeIfAbsent(code, k -> new HashSet<>()).add(sessionId);
            }

            log.debug("注册WebSocket会话成功，会话ID：{}，关注代码数：{}", sessionId, codes.size());
        } catch (Exception e) {
            log.error("注册WebSocket会话失败，会话ID：{}，错误：{}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 注销WebSocket会话
     * @param sessionId 会话ID
     */
    public void unregisterSession(String sessionId) {
        try {
            Set<String> codes = sessionToCodes.remove(sessionId);
            if (codes != null) {
                for (String code : codes) {
                    Set<String> sessions = codeToSessions.get(code);
                    if (sessions != null) {
                        sessions.remove(sessionId);
                        if (sessions.isEmpty()) {
                            codeToSessions.remove(code);
                        }
                    }
                }
            }

            log.debug("注销WebSocket会话成功，会话ID：{}", sessionId);
        } catch (Exception e) {
            log.error("注销WebSocket会话失败，会话ID：{}，错误：{}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 获取关注某个代码的所有会话
     * @param code 标的代码
     * @return 会话ID集合
     */
    public Set<String> getSessionsByCode(String code) {
        return codeToSessions.getOrDefault(code, Collections.emptySet());
    }

    /**
     * 获取会话关注的所有代码
     * @param sessionId 会话ID
     * @return 代码集合
     */
    public Set<String> getCodesBySession(String sessionId) {
        return sessionToCodes.getOrDefault(sessionId, Collections.emptySet());
    }

    /**
     * 检查价格是否新鲜（1分钟内）
     * @param price 实时价格
     * @return 是否新鲜
     */
    private boolean isPriceFresh(RealtimePriceBO price) {
        if (price == null || price.getTimestamp() == null) {
            return false;
        }
        return price.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(1));
    }

    /**
     * 更新价格缓存
     * @param price 实时价格
     */
    private void updatePriceCache(RealtimePriceBO price) {
        try {
            String cacheKey = price.getType() + ":" + price.getCode();

            // 更新内存缓存
            realtimePriceCache.put(cacheKey, price);

            // 更新Redis缓存，设置1分钟过期
            String redisKey = "realtime:" + price.getType().toLowerCase() + ":" + price.getCode();
            redisTemplate.opsForValue().set(redisKey, price, java.time.Duration.ofMinutes(1));
        } catch (Exception e) {
            log.error("更新价格缓存失败，代码：{}，错误：{}", price.getCode(), e.getMessage(), e);
        }
    }

    /**
     * 广播价格更新
     * @param price 实时价格
     */
    private void broadcastPriceUpdate(RealtimePriceBO price) {
        // 这里只是记录日志，实际广播逻辑在WebSocket处理器中实现
        log.debug("广播价格更新，代码：{}，类型：{}，价格：{}",
                 price.getCode(), price.getType(), price.getPrice());
    }

    /**
     * 将StockDashboardBO转换为RealtimePriceBO
     * @param stock 股票实体
     * @return 实时价格实体
     */
    private RealtimePriceBO convertToRealtimePrice(StockDashboardBO stock) {
        RealtimePriceBO price = new RealtimePriceBO();
        price.setCode(stock.getStockCode());
        price.setType("STOCK");
        price.setPrice(stock.getCurrentPrice());
        price.setChangeAmount(stock.getChangeAmount());
        price.setChangePercent(stock.getChangePercent());
        price.setOpenPrice(stock.getOpenPrice());
        price.setHighPrice(stock.getHighPrice());
        price.setLowPrice(stock.getLowPrice());
        price.setPreviousClose(stock.getPreviousClose());
        price.setVolume(stock.getVolume());
        price.setTurnover(stock.getTurnover());
        price.setTurnoverRate(stock.getTurnoverRate());
        price.setPeRatio(stock.getPeRatio());
        price.setPbRatio(stock.getPbRatio());
        price.setMarketCap(stock.getMarketCap());
        price.setCirculatingMarketCap(stock.getCirculatingMarketCap());
        price.setTimestamp(LocalDateTime.now());
        price.setTradingStatus("交易中");
        price.setDataSource(stock.getDataSource());
        return price;
    }

    /**
     * 将FundDashboardBO转换为RealtimePriceBO
     * @param fund 基金实体
     * @return 实时价格实体
     */
    private RealtimePriceBO convertToRealtimePrice(FundDashboardBO fund) {
        RealtimePriceBO price = new RealtimePriceBO();
        price.setCode(fund.getFundCode());
        price.setType("FUND");
        price.setPrice(fund.getCurrentNetValue());
        price.setChangeAmount(fund.getDailyChangeAmount());
        price.setChangePercent(fund.getDailyChangePercent());
        price.setTimestamp(LocalDateTime.now());
        price.setTradingStatus("交易中");
        price.setDataSource(fund.getDataSource());
        return price;
    }

    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("memoryCacheSize", realtimePriceCache.size());
        stats.put("sessionCount", sessionToCodes.size());
        stats.put("codeSubscriptionCount", codeToSessions.size());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }

    /**
     * 清理过期缓存
     */
    public void cleanupExpiredCache() {
        try {
            int beforeSize = realtimePriceCache.size();

            // 清理内存缓存中过期的价格
            realtimePriceCache.entrySet().removeIf(entry -> !isPriceFresh(entry.getValue()));

            int afterSize = realtimePriceCache.size();
            int cleanedCount = beforeSize - afterSize;

            log.debug("清理过期缓存完成，清理数量：{}，剩余数量：{}", cleanedCount, afterSize);
        } catch (Exception e) {
            log.error("清理过期缓存失败，错误：{}", e.getMessage(), e);
        }
    }
}