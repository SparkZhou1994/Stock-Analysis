package com.spark.stockdashboard.service;

import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import com.spark.stockdashboard.entity.dashboard.FundDashboardBO;
import com.spark.stockdashboard.repository.StockDashboardRepository;
import com.spark.stockdashboard.repository.FundDashboardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自选管理服务
 * 负责管理用户的自选股票和基金列表
 */
@Service
@Slf4j
public class WatchlistService {

    private final StockDashboardRepository stockDashboardRepository;
    private final FundDashboardRepository fundDashboardRepository;
    private final DataSyncService dataSyncService;

    @Autowired
    public WatchlistService(StockDashboardRepository stockDashboardRepository,
                           FundDashboardRepository fundDashboardRepository,
                           DataSyncService dataSyncService) {
        this.stockDashboardRepository = stockDashboardRepository;
        this.fundDashboardRepository = fundDashboardRepository;
        this.dataSyncService = dataSyncService;
    }

    /**
     * 获取自选股票列表
     * @return 自选股票列表，按自选顺序排序
     */
    public List<StockDashboardBO> getWatchlistStocks() {
        try {
            List<StockDashboardBO> watchlistStocks = stockDashboardRepository
                    .findByIsWatchlistTrueOrderByWatchlistOrderAsc();

            log.debug("获取自选股票列表成功，数量：{}", watchlistStocks.size());
            return watchlistStocks;
        } catch (Exception e) {
            log.error("获取自选股票列表失败，错误：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取自选基金列表
     * @return 自选基金列表，按自选顺序排序
     */
    public List<FundDashboardBO> getWatchlistFunds() {
        try {
            List<FundDashboardBO> watchlistFunds = fundDashboardRepository
                    .findByIsWatchlistTrueOrderByWatchlistOrderAsc();

            log.debug("获取自选基金列表成功，数量：{}", watchlistFunds.size());
            return watchlistFunds;
        } catch (Exception e) {
            log.error("获取自选基金列表失败，错误：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取自选列表摘要
     * @return 自选列表摘要信息
     */
    public Map<String, Object> getWatchlistSummary() {
        try {
            Map<String, Object> summary = new HashMap<>();

            List<StockDashboardBO> watchlistStocks = getWatchlistStocks();
            List<FundDashboardBO> watchlistFunds = getWatchlistFunds();

            // 计算统计信息
            long totalCount = watchlistStocks.size() + watchlistFunds.size();
            long stockCount = watchlistStocks.size();
            long fundCount = watchlistFunds.size();

            // 计算涨跌统计
            long stockUpCount = watchlistStocks.stream()
                    .filter(stock -> stock.getChangeAmount() != null &&
                            stock.getChangeAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
                    .count();
            long stockDownCount = watchlistStocks.stream()
                    .filter(stock -> stock.getChangeAmount() != null &&
                            stock.getChangeAmount().compareTo(java.math.BigDecimal.ZERO) < 0)
                    .count();

            long fundUpCount = watchlistFunds.stream()
                    .filter(fund -> fund.getDailyChangeAmount() != null &&
                            fund.getDailyChangeAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
                    .count();
            long fundDownCount = watchlistFunds.stream()
                    .filter(fund -> fund.getDailyChangeAmount() != null &&
                            fund.getDailyChangeAmount().compareTo(java.math.BigDecimal.ZERO) < 0)
                    .count();

            summary.put("totalCount", totalCount);
            summary.put("stockCount", stockCount);
            summary.put("fundCount", fundCount);
            summary.put("stockUpCount", stockUpCount);
            summary.put("stockDownCount", stockDownCount);
            summary.put("fundUpCount", fundUpCount);
            summary.put("fundDownCount", fundDownCount);
            summary.put("timestamp", LocalDateTime.now());

            log.debug("获取自选列表摘要成功，股票：{}，基金：{}", stockCount, fundCount);
            return summary;
        } catch (Exception e) {
            log.error("获取自选列表摘要失败，错误：{}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 添加股票到自选
     * @param stockCode 股票代码
     * @return 是否添加成功
     */
    @Transactional
    public boolean addStockToWatchlist(String stockCode) {
        try {
            log.debug("开始添加股票到自选，股票代码：{}", stockCode);

            // 检查股票是否存在
            Optional<StockDashboardBO> stockOpt = stockDashboardRepository.findByStockCode(stockCode);
            if (stockOpt.isEmpty()) {
                // 尝试同步股票数据
                boolean syncSuccess = dataSyncService.syncSingleStockData(stockCode);
                if (!syncSuccess) {
                    log.warn("添加股票到自选失败，股票不存在且同步失败，股票代码：{}", stockCode);
                    return false;
                }

                // 重新获取股票数据
                stockOpt = stockDashboardRepository.findByStockCode(stockCode);
                if (stockOpt.isEmpty()) {
                    log.warn("添加股票到自选失败，同步后股票仍不存在，股票代码：{}", stockCode);
                    return false;
                }
            }

            StockDashboardBO stock = stockOpt.get();

            // 检查是否已经是自选
            if (Boolean.TRUE.equals(stock.getIsWatchlist())) {
                log.debug("股票已在自选列表中，股票代码：{}", stockCode);
                return true;
            }

            // 获取当前最大自选顺序
            List<StockDashboardBO> watchlistStocks = getWatchlistStocks();
            int maxOrder = watchlistStocks.stream()
                    .map(StockDashboardBO::getWatchlistOrder)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);

            // 更新股票的自选状态和顺序
            stock.setIsWatchlist(true);
            stock.setWatchlistOrder(maxOrder + 1);
            stock.setUpdateTime(LocalDateTime.now());
            stockDashboardRepository.save(stock);

            log.info("添加股票到自选成功，股票代码：{}，自选顺序：{}", stockCode, maxOrder + 1);
            return true;
        } catch (Exception e) {
            log.error("添加股票到自选失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 添加基金到自选
     * @param fundCode 基金代码
     * @return 是否添加成功
     */
    @Transactional
    public boolean addFundToWatchlist(String fundCode) {
        try {
            log.debug("开始添加基金到自选，基金代码：{}", fundCode);

            // 检查基金是否存在
            Optional<FundDashboardBO> fundOpt = fundDashboardRepository.findByFundCode(fundCode);
            if (fundOpt.isEmpty()) {
                // 尝试同步基金数据
                boolean syncSuccess = dataSyncService.syncSingleFundData(fundCode);
                if (!syncSuccess) {
                    log.warn("添加基金到自选失败，基金不存在且同步失败，基金代码：{}", fundCode);
                    return false;
                }

                // 重新获取基金数据
                fundOpt = fundDashboardRepository.findByFundCode(fundCode);
                if (fundOpt.isEmpty()) {
                    log.warn("添加基金到自选失败，同步后基金仍不存在，基金代码：{}", fundCode);
                    return false;
                }
            }

            FundDashboardBO fund = fundOpt.get();

            // 检查是否已经是自选
            if (Boolean.TRUE.equals(fund.getIsWatchlist())) {
                log.debug("基金已在自选列表中，基金代码：{}", fundCode);
                return true;
            }

            // 获取当前最大自选顺序
            List<FundDashboardBO> watchlistFunds = getWatchlistFunds();
            int maxOrder = watchlistFunds.stream()
                    .map(FundDashboardBO::getWatchlistOrder)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);

            // 更新基金的自选状态和顺序
            fund.setIsWatchlist(true);
            fund.setWatchlistOrder(maxOrder + 1);
            fund.setUpdateTime(LocalDateTime.now());
            fundDashboardRepository.save(fund);

            log.info("添加基金到自选成功，基金代码：{}，自选顺序：{}", fundCode, maxOrder + 1);
            return true;
        } catch (Exception e) {
            log.error("添加基金到自选失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从自选移除股票
     * @param stockCode 股票代码
     * @return 是否移除成功
     */
    @Transactional
    public boolean removeStockFromWatchlist(String stockCode) {
        try {
            log.debug("开始从自选移除股票，股票代码：{}", stockCode);

            Optional<StockDashboardBO> stockOpt = stockDashboardRepository.findByStockCode(stockCode);
            if (stockOpt.isEmpty()) {
                log.warn("从自选移除股票失败，股票不存在，股票代码：{}", stockCode);
                return false;
            }

            StockDashboardBO stock = stockOpt.get();

            // 检查是否在自选列表中
            if (!Boolean.TRUE.equals(stock.getIsWatchlist())) {
                log.debug("股票不在自选列表中，股票代码：{}", stockCode);
                return true;
            }

            // 更新股票的自选状态
            stock.setIsWatchlist(false);
            stock.setWatchlistOrder(null);
            stock.setUpdateTime(LocalDateTime.now());
            stockDashboardRepository.save(stock);

            // 重新排序剩余的自选股票
            reorderWatchlistStocks();

            log.info("从自选移除股票成功，股票代码：{}", stockCode);
            return true;
        } catch (Exception e) {
            log.error("从自选移除股票失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从自选移除基金
     * @param fundCode 基金代码
     * @return 是否移除成功
     */
    @Transactional
    public boolean removeFundFromWatchlist(String fundCode) {
        try {
            log.debug("开始从自选移除基金，基金代码：{}", fundCode);

            Optional<FundDashboardBO> fundOpt = fundDashboardRepository.findByFundCode(fundCode);
            if (fundOpt.isEmpty()) {
                log.warn("从自选移除基金失败，基金不存在，基金代码：{}", fundCode);
                return false;
            }

            FundDashboardBO fund = fundOpt.get();

            // 检查是否在自选列表中
            if (!Boolean.TRUE.equals(fund.getIsWatchlist())) {
                log.debug("基金不在自选列表中，基金代码：{}", fundCode);
                return true;
            }

            // 更新基金的自选状态
            fund.setIsWatchlist(false);
            fund.setWatchlistOrder(null);
            fund.setUpdateTime(LocalDateTime.now());
            fundDashboardRepository.save(fund);

            // 重新排序剩余的自选基金
            reorderWatchlistFunds();

            log.info("从自选移除基金成功，基金代码：{}", fundCode);
            return true;
        } catch (Exception e) {
            log.error("从自选移除基金失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 调整股票自选顺序
     * @param stockCodes 按新顺序排列的股票代码列表
     * @return 是否调整成功
     */
    @Transactional
    public boolean reorderWatchlistStocks(List<String> stockCodes) {
        try {
            log.debug("开始调整股票自选顺序，股票数量：{}", stockCodes.size());

            // 验证所有股票都存在且在自选列表中
            List<StockDashboardBO> stocks = stockDashboardRepository.findByStockCodeIn(stockCodes);
            if (stocks.size() != stockCodes.size()) {
                log.warn("调整股票自选顺序失败，部分股票不存在，预期：{}，实际：{}",
                         stockCodes.size(), stocks.size());
                return false;
            }

            // 更新自选顺序
            for (int i = 0; i < stockCodes.size(); i++) {
                String stockCode = stockCodes.get(i);
                int order = i + 1;

                stockDashboardRepository.updateWatchlistStatus(stockCode, true, order);
            }

            log.info("调整股票自选顺序成功，股票数量：{}", stockCodes.size());
            return true;
        } catch (Exception e) {
            log.error("调整股票自选顺序失败，错误：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 调整基金自选顺序
     * @param fundCodes 按新顺序排列的基金代码列表
     * @return 是否调整成功
     */
    @Transactional
    public boolean reorderWatchlistFunds(List<String> fundCodes) {
        try {
            log.debug("开始调整基金自选顺序，基金数量：{}", fundCodes.size());

            // 验证所有基金都存在且在自选列表中
            List<FundDashboardBO> funds = fundDashboardRepository.findByFundCodeIn(fundCodes);
            if (funds.size() != fundCodes.size()) {
                log.warn("调整基金自选顺序失败，部分基金不存在，预期：{}，实际：{}",
                         fundCodes.size(), funds.size());
                return false;
            }

            // 更新自选顺序
            for (int i = 0; i < fundCodes.size(); i++) {
                String fundCode = fundCodes.get(i);
                int order = i + 1;

                fundDashboardRepository.updateWatchlistStatus(fundCode, true, order);
            }

            log.info("调整基金自选顺序成功，基金数量：{}", fundCodes.size());
            return true;
        } catch (Exception e) {
            log.error("调整基金自选顺序失败，错误：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 搜索股票
     * @param keyword 搜索关键词（代码或名称）
     * @param limit 返回结果数量限制
     * @return 搜索结果
     */
    public List<StockDashboardBO> searchStocks(String keyword, int limit) {
        try {
            log.debug("开始搜索股票，关键词：{}，限制：{}", keyword, limit);

            List<StockDashboardBO> results = new ArrayList<>();

            // 按代码搜索
            Optional<StockDashboardBO> byCode = stockDashboardRepository.findByStockCode(keyword);
            byCode.ifPresent(results::add);

            // 按名称模糊搜索
            if (results.size() < limit) {
                List<StockDashboardBO> byName = stockDashboardRepository.findByStockNameContaining(keyword);
                for (StockDashboardBO stock : byName) {
                    if (results.size() >= limit) break;
                    if (!results.contains(stock)) {
                        results.add(stock);
                    }
                }
            }

            log.debug("股票搜索完成，关键词：{}，结果数量：{}", keyword, results.size());
            return results;
        } catch (Exception e) {
            log.error("搜索股票失败，关键词：{}，错误：{}", keyword, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 搜索基金
     * @param keyword 搜索关键词（代码或名称）
     * @param limit 返回结果数量限制
     * @return 搜索结果
     */
    public List<FundDashboardBO> searchFunds(String keyword, int limit) {
        try {
            log.debug("开始搜索基金，关键词：{}，限制：{}", keyword, limit);

            List<FundDashboardBO> results = new ArrayList<>();

            // 按代码搜索
            Optional<FundDashboardBO> byCode = fundDashboardRepository.findByFundCode(keyword);
            byCode.ifPresent(results::add);

            // 按名称模糊搜索
            if (results.size() < limit) {
                List<FundDashboardBO> byName = fundDashboardRepository.findByFundNameContaining(keyword);
                for (FundDashboardBO fund : byName) {
                    if (results.size() >= limit) break;
                    if (!results.contains(fund)) {
                        results.add(fund);
                    }
                }
            }

            log.debug("基金搜索完成，关键词：{}，结果数量：{}", keyword, results.size());
            return results;
        } catch (Exception e) {
            log.error("搜索基金失败，关键词：{}，错误：{}", keyword, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量添加股票到自选
     * @param stockCodes 股票代码列表
     * @return 成功添加的数量
     */
    @Transactional
    public int batchAddStocksToWatchlist(List<String> stockCodes) {
        try {
            log.debug("开始批量添加股票到自选，数量：{}", stockCodes.size());

            int successCount = 0;
            for (String stockCode : stockCodes) {
                try {
                    boolean success = addStockToWatchlist(stockCode);
                    if (success) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("批量添加股票失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
                }
            }

            log.info("批量添加股票到自选完成，总数：{}，成功：{}", stockCodes.size(), successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量添加股票到自选失败，错误：{}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 批量添加基金到自选
     * @param fundCodes 基金代码列表
     * @return 成功添加的数量
     */
    @Transactional
    public int batchAddFundsToWatchlist(List<String> fundCodes) {
        try {
            log.debug("开始批量添加基金到自选，数量：{}", fundCodes.size());

            int successCount = 0;
            for (String fundCode : fundCodes) {
                try {
                    boolean success = addFundToWatchlist(fundCode);
                    if (success) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("批量添加基金失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
                }
            }

            log.info("批量添加基金到自选完成，总数：{}，成功：{}", fundCodes.size(), successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量添加基金到自选失败，错误：{}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 清空自选列表
     * @return 是否清空成功
     */
    @Transactional
    public boolean clearWatchlist() {
        try {
            log.debug("开始清空自选列表");

            // 清空股票自选
            List<StockDashboardBO> watchlistStocks = getWatchlistStocks();
            for (StockDashboardBO stock : watchlistStocks) {
                stock.setIsWatchlist(false);
                stock.setWatchlistOrder(null);
                stock.setUpdateTime(LocalDateTime.now());
            }
            stockDashboardRepository.saveAll(watchlistStocks);

            // 清空基金自选
            List<FundDashboardBO> watchlistFunds = getWatchlistFunds();
            for (FundDashboardBO fund : watchlistFunds) {
                fund.setIsWatchlist(false);
                fund.setWatchlistOrder(null);
                fund.setUpdateTime(LocalDateTime.now());
            }
            fundDashboardRepository.saveAll(watchlistFunds);

            log.info("清空自选列表成功，股票：{}，基金：{}", watchlistStocks.size(), watchlistFunds.size());
            return true;
        } catch (Exception e) {
            log.error("清空自选列表失败，错误：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 重新排序自选股票（内部方法）
     */
    private void reorderWatchlistStocks() {
        try {
            List<StockDashboardBO> watchlistStocks = getWatchlistStocks();
            for (int i = 0; i < watchlistStocks.size(); i++) {
                StockDashboardBO stock = watchlistStocks.get(i);
                stock.setWatchlistOrder(i + 1);
                stock.setUpdateTime(LocalDateTime.now());
            }
            stockDashboardRepository.saveAll(watchlistStocks);

            log.debug("重新排序自选股票完成，数量：{}", watchlistStocks.size());
        } catch (Exception e) {
            log.error("重新排序自选股票失败，错误：{}", e.getMessage(), e);
        }
    }

    /**
     * 重新排序自选基金（内部方法）
     */
    private void reorderWatchlistFunds() {
        try {
            List<FundDashboardBO> watchlistFunds = getWatchlistFunds();
            for (int i = 0; i < watchlistFunds.size(); i++) {
                FundDashboardBO fund = watchlistFunds.get(i);
                fund.setWatchlistOrder(i + 1);
                fund.setUpdateTime(LocalDateTime.now());
            }
            fundDashboardRepository.saveAll(watchlistFunds);

            log.debug("重新排序自选基金完成，数量：{}", watchlistFunds.size());
        } catch (Exception e) {
            log.error("重新排序自选基金失败，错误：{}", e.getMessage(), e);
        }
    }

    /**
     * 检查股票是否在自选列表中
     * @param stockCode 股票代码
     * @return 是否在自选列表中
     */
    public boolean isStockInWatchlist(String stockCode) {
        try {
            Optional<StockDashboardBO> stockOpt = stockDashboardRepository.findByStockCode(stockCode);
            return stockOpt.isPresent() && Boolean.TRUE.equals(stockOpt.get().getIsWatchlist());
        } catch (Exception e) {
            log.error("检查股票是否在自选列表失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查基金是否在自选列表中
     * @param fundCode 基金代码
     * @return 是否在自选列表中
     */
    public boolean isFundInWatchlist(String fundCode) {
        try {
            Optional<FundDashboardBO> fundOpt = fundDashboardRepository.findByFundCode(fundCode);
            return fundOpt.isPresent() && Boolean.TRUE.equals(fundOpt.get().getIsWatchlist());
        } catch (Exception e) {
            log.error("检查基金是否在自选列表失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return false;
        }
    }
}