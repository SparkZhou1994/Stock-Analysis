package com.spark.stockdashboard.service;

import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import com.spark.stockdashboard.entity.dashboard.FundDashboardBO;
import com.spark.stockdashboard.entity.dashboard.RealtimePriceBO;
import com.spark.stockdashboard.entity.dashboard.HistoricalDataBO;
import com.spark.stockdashboard.repository.StockDashboardRepository;
import com.spark.stockdashboard.repository.FundDashboardRepository;
import com.spark.stockdashboard.repository.HistoricalDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 数据同步服务
 * 负责从东方财富API同步股票和基金数据到本地数据库
 */
@Service
@Slf4j
public class DataSyncService {

    private final EastMoneyApiClient eastMoneyApiClient;
    private final StockDashboardRepository stockDashboardRepository;
    private final FundDashboardRepository fundDashboardRepository;
    private final HistoricalDataRepository historicalDataRepository;

    @Autowired
    public DataSyncService(EastMoneyApiClient eastMoneyApiClient,
                          StockDashboardRepository stockDashboardRepository,
                          FundDashboardRepository fundDashboardRepository,
                          HistoricalDataRepository historicalDataRepository) {
        this.eastMoneyApiClient = eastMoneyApiClient;
        this.stockDashboardRepository = stockDashboardRepository;
        this.fundDashboardRepository = fundDashboardRepository;
        this.historicalDataRepository = historicalDataRepository;
    }

    /**
     * 同步股票实时数据
     * @param stockCodes 股票代码列表，为空则同步所有股票
     * @return 同步的股票数量
     */
    @Transactional
    public int syncStockRealtimeData(List<String> stockCodes) {
        try {
            log.info("开始同步股票实时数据，股票数量：{}", stockCodes != null ? stockCodes.size() : "全部");

            List<StockDashboardBO> stocksToSync;
            if (stockCodes == null || stockCodes.isEmpty()) {
                // 同步所有活跃股票
                stocksToSync = stockDashboardRepository.findByIsActive(true);
            } else {
                // 同步指定股票
                stocksToSync = stockDashboardRepository.findByStockCodeIn(stockCodes);
            }

            int syncedCount = 0;
            for (StockDashboardBO stock : stocksToSync) {
                try {
                    boolean success = syncSingleStockData(stock.getStockCode());
                    if (success) {
                        syncedCount++;
                    }
                } catch (Exception e) {
                    log.error("同步股票数据失败，股票代码：{}，错误：{}", stock.getStockCode(), e.getMessage(), e);
                }
            }

            log.info("股票实时数据同步完成，成功同步：{} 个，总数：{}", syncedCount, stocksToSync.size());
            return syncedCount;
        } catch (Exception e) {
            log.error("同步股票实时数据失败：{}", e.getMessage(), e);
            throw new RuntimeException("同步股票实时数据失败", e);
        }
    }

    /**
     * 同步单个股票数据
     * @param stockCode 股票代码
     * @return 是否同步成功
     */
    @Transactional
    public boolean syncSingleStockData(String stockCode) {
        try {
            log.debug("开始同步单个股票数据，股票代码：{}", stockCode);

            // 从东方财富API获取股票实时数据
            StockDashboardBO stockData = eastMoneyApiClient.fetchStockRealtimeData(stockCode);
            if (stockData == null) {
                log.warn("从API获取股票数据失败，股票代码：{}", stockCode);
                return false;
            }

            // 保存或更新股票数据
            Optional<StockDashboardBO> existingStock = stockDashboardRepository.findByStockCode(stockCode);
            if (existingStock.isPresent()) {
                // 更新现有数据
                StockDashboardBO stockToUpdate = existingStock.get();
                updateStockData(stockToUpdate, stockData);
                stockDashboardRepository.save(stockToUpdate);
                log.debug("更新股票数据成功，股票代码：{}", stockCode);
            } else {
                // 插入新数据
                stockData.setCreateTime(LocalDateTime.now());
                stockData.setIsActive(true);
                stockDashboardRepository.save(stockData);
                log.debug("插入股票数据成功，股票代码：{}", stockCode);
            }

            // 同步历史数据
            syncStockHistoricalData(stockCode);

            return true;
        } catch (Exception e) {
            log.error("同步单个股票数据失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步基金实时数据
     * @param fundCodes 基金代码列表，为空则同步所有基金
     * @return 同步的基金数量
     */
    @Transactional
    public int syncFundRealtimeData(List<String> fundCodes) {
        try {
            log.info("开始同步基金实时数据，基金数量：{}", fundCodes != null ? fundCodes.size() : "全部");

            List<FundDashboardBO> fundsToSync;
            if (fundCodes == null || fundCodes.isEmpty()) {
                // 同步所有活跃基金
                fundsToSync = fundDashboardRepository.findByIsActive(true);
            } else {
                // 同步指定基金
                fundsToSync = fundDashboardRepository.findByFundCodeIn(fundCodes);
            }

            int syncedCount = 0;
            for (FundDashboardBO fund : fundsToSync) {
                try {
                    boolean success = syncSingleFundData(fund.getFundCode());
                    if (success) {
                        syncedCount++;
                    }
                } catch (Exception e) {
                    log.error("同步基金数据失败，基金代码：{}，错误：{}", fund.getFundCode(), e.getMessage(), e);
                }
            }

            log.info("基金实时数据同步完成，成功同步：{} 个，总数：{}", syncedCount, fundsToSync.size());
            return syncedCount;
        } catch (Exception e) {
            log.error("同步基金实时数据失败：{}", e.getMessage(), e);
            throw new RuntimeException("同步基金实时数据失败", e);
        }
    }

    /**
     * 同步单个基金数据
     * @param fundCode 基金代码
     * @return 是否同步成功
     */
    @Transactional
    public boolean syncSingleFundData(String fundCode) {
        try {
            log.debug("开始同步单个基金数据，基金代码：{}", fundCode);

            // 从东方财富API获取基金实时数据
            FundDashboardBO fundData = eastMoneyApiClient.fetchFundRealtimeData(fundCode);
            if (fundData == null) {
                log.warn("从API获取基金数据失败，基金代码：{}", fundCode);
                return false;
            }

            // 保存或更新基金数据
            Optional<FundDashboardBO> existingFund = fundDashboardRepository.findByFundCode(fundCode);
            if (existingFund.isPresent()) {
                // 更新现有数据
                FundDashboardBO fundToUpdate = existingFund.get();
                updateFundData(fundToUpdate, fundData);
                fundDashboardRepository.save(fundToUpdate);
                log.debug("更新基金数据成功，基金代码：{}", fundCode);
            } else {
                // 插入新数据
                fundData.setCreateTime(LocalDateTime.now());
                fundData.setIsActive(true);
                fundDashboardRepository.save(fundData);
                log.debug("插入基金数据成功，基金代码：{}", fundCode);
            }

            // 同步历史数据
            syncFundHistoricalData(fundCode);

            return true;
        } catch (Exception e) {
            log.error("同步单个基金数据失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步股票历史数据
     * @param stockCode 股票代码
     */
    @Transactional
    public void syncStockHistoricalData(String stockCode) {
        try {
            log.debug("开始同步股票历史数据，股票代码：{}", stockCode);

            // 获取最近一年的历史数据
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);

            List<HistoricalDataBO> historicalData = eastMoneyApiClient.fetchStockHistoricalData(stockCode, startDate, endDate);
            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("从API获取股票历史数据失败或为空，股票代码：{}", stockCode);
                return;
            }

            // 批量保存历史数据
            List<HistoricalDataBO> dataToSave = new ArrayList<>();
            for (HistoricalDataBO data : historicalData) {
                // 检查是否已存在
                Optional<HistoricalDataBO> existingData = historicalDataRepository.findByCodeAndTradeDate(stockCode, data.getTradeDate());
                if (existingData.isEmpty()) {
                    data.setCode(stockCode);
                    data.setType("STOCK");
                    data.setCreateTime(LocalDate.now());
                    dataToSave.add(data);
                }
            }

            if (!dataToSave.isEmpty()) {
                historicalDataRepository.saveAll(dataToSave);
                log.debug("保存股票历史数据成功，股票代码：{}，新增记录数：{}", stockCode, dataToSave.size());
            }
        } catch (Exception e) {
            log.error("同步股票历史数据失败，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
        }
    }

    /**
     * 同步基金历史数据
     * @param fundCode 基金代码
     */
    @Transactional
    public void syncFundHistoricalData(String fundCode) {
        try {
            log.debug("开始同步基金历史数据，基金代码：{}", fundCode);

            // 获取最近一年的历史数据
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);

            List<HistoricalDataBO> historicalData = eastMoneyApiClient.fetchFundHistoricalData(fundCode, startDate, endDate);
            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("从API获取基金历史数据失败或为空，基金代码：{}", fundCode);
                return;
            }

            // 批量保存历史数据
            List<HistoricalDataBO> dataToSave = new ArrayList<>();
            for (HistoricalDataBO data : historicalData) {
                // 检查是否已存在
                Optional<HistoricalDataBO> existingData = historicalDataRepository.findByCodeAndTradeDate(fundCode, data.getTradeDate());
                if (existingData.isEmpty()) {
                    data.setCode(fundCode);
                    data.setType("FUND");
                    data.setCreateTime(LocalDate.now());
                    dataToSave.add(data);
                }
            }

            if (!dataToSave.isEmpty()) {
                historicalDataRepository.saveAll(dataToSave);
                log.debug("保存基金历史数据成功，基金代码：{}，新增记录数：{}", fundCode, dataToSave.size());
            }
        } catch (Exception e) {
            log.error("同步基金历史数据失败，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
        }
    }

    /**
     * 定时同步任务 - 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scheduledSyncRealtimeData() {
        try {
            log.info("开始定时同步实时数据");

            // 同步自选股票和基金
            List<StockDashboardBO> watchlistStocks = stockDashboardRepository.findByIsWatchlistTrueOrderByWatchlistOrderAsc();
            List<FundDashboardBO> watchlistFunds = fundDashboardRepository.findByIsWatchlistTrueOrderByWatchlistOrderAsc();

            List<String> stockCodes = watchlistStocks.stream()
                    .map(StockDashboardBO::getStockCode)
                    .toList();
            List<String> fundCodes = watchlistFunds.stream()
                    .map(FundDashboardBO::getFundCode)
                    .toList();

            int stockCount = syncStockRealtimeData(stockCodes);
            int fundCount = syncFundRealtimeData(fundCodes);

            log.info("定时同步完成，股票：{} 个，基金：{} 个", stockCount, fundCount);
        } catch (Exception e) {
            log.error("定时同步任务失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 定时同步历史数据 - 每天收盘后执行
     */
    @Scheduled(cron = "0 30 16 * * ?")
    public void scheduledSyncHistoricalData() {
        try {
            log.info("开始定时同步历史数据");

            // 同步所有活跃股票的历史数据
            List<StockDashboardBO> activeStocks = stockDashboardRepository.findByIsActive(true);
            for (StockDashboardBO stock : activeStocks) {
                try {
                    syncStockHistoricalData(stock.getStockCode());
                } catch (Exception e) {
                    log.error("同步股票历史数据失败，股票代码：{}，错误：{}", stock.getStockCode(), e.getMessage(), e);
                }
            }

            // 同步所有活跃基金的历史数据
            List<FundDashboardBO> activeFunds = fundDashboardRepository.findByIsActive(true);
            for (FundDashboardBO fund : activeFunds) {
                try {
                    syncFundHistoricalData(fund.getFundCode());
                } catch (Exception e) {
                    log.error("同步基金历史数据失败，基金代码：{}，错误：{}", fund.getFundCode(), e.getMessage(), e);
                }
            }

            log.info("定时同步历史数据完成，股票：{} 个，基金：{} 个", activeStocks.size(), activeFunds.size());
        } catch (Exception e) {
            log.error("定时同步历史数据任务失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 更新股票数据
     * @param existing 现有股票数据
     * @param newData 新股票数据
     */
    private void updateStockData(StockDashboardBO existing, StockDashboardBO newData) {
        existing.setStockName(newData.getStockName());
        existing.setCurrentPrice(newData.getCurrentPrice());
        existing.setChangeAmount(newData.getChangeAmount());
        existing.setChangePercent(newData.getChangePercent());
        existing.setOpenPrice(newData.getOpenPrice());
        existing.setHighPrice(newData.getHighPrice());
        existing.setLowPrice(newData.getLowPrice());
        existing.setPreviousClose(newData.getPreviousClose());
        existing.setVolume(newData.getVolume());
        existing.setTurnover(newData.getTurnover());
        existing.setTurnoverRate(newData.getTurnoverRate());
        existing.setPeRatio(newData.getPeRatio());
        existing.setPbRatio(newData.getPbRatio());
        existing.setMarketCap(newData.getMarketCap());
        existing.setCirculatingMarketCap(newData.getCirculatingMarketCap());
        existing.setIndustry(newData.getIndustry());
        existing.setStockType(newData.getStockType());
        existing.setUpdateTime(LocalDateTime.now());
        existing.setDataSource(newData.getDataSource());
    }

    /**
     * 更新基金数据
     * @param existing 现有基金数据
     * @param newData 新基金数据
     */
    private void updateFundData(FundDashboardBO existing, FundDashboardBO newData) {
        existing.setFundName(newData.getFundName());
        existing.setFundType(newData.getFundType());
        existing.setCurrentNetValue(newData.getCurrentNetValue());
        existing.setDailyChangeAmount(newData.getDailyChangeAmount());
        existing.setDailyChangePercent(newData.getDailyChangePercent());
        existing.setWeeklyChangePercent(newData.getWeeklyChangePercent());
        existing.setMonthlyChangePercent(newData.getMonthlyChangePercent());
        existing.setYearlyChangePercent(newData.getYearlyChangePercent());
        existing.setAccumulatedNetValue(newData.getAccumulatedNetValue());
        existing.setEstablishmentDate(newData.getEstablishmentDate());
        existing.setFundSize(newData.getFundSize());
        existing.setFundManager(newData.getFundManager());
        existing.setFundCompany(newData.getFundCompany());
        existing.setRiskLevel(newData.getRiskLevel());
        existing.setPurchaseStatus(newData.getPurchaseStatus());
        existing.setRedemptionStatus(newData.getRedemptionStatus());
        existing.setUpdateTime(LocalDateTime.now());
        existing.setDataSource(newData.getDataSource());
    }

    /**
     * 初始化股票数据
     * @param stockCodes 股票代码列表
     */
    @Transactional
    public void initializeStockData(List<String> stockCodes) {
        log.info("开始初始化股票数据，数量：{}", stockCodes.size());

        for (String stockCode : stockCodes) {
            try {
                // 检查是否已存在
                Optional<StockDashboardBO> existingStock = stockDashboardRepository.findByStockCode(stockCode);
                if (existingStock.isEmpty()) {
                    // 同步股票数据
                    boolean success = syncSingleStockData(stockCode);
                    if (success) {
                        log.info("初始化股票数据成功，股票代码：{}", stockCode);
                    } else {
                        log.warn("初始化股票数据失败，股票代码：{}", stockCode);
                    }
                } else {
                    log.info("股票数据已存在，跳过初始化，股票代码：{}", stockCode);
                }
            } catch (Exception e) {
                log.error("初始化股票数据异常，股票代码：{}，错误：{}", stockCode, e.getMessage(), e);
            }
        }

        log.info("股票数据初始化完成");
    }

    /**
     * 初始化基金数据
     * @param fundCodes 基金代码列表
     */
    @Transactional
    public void initializeFundData(List<String> fundCodes) {
        log.info("开始初始化基金数据，数量：{}", fundCodes.size());

        for (String fundCode : fundCodes) {
            try {
                // 检查是否已存在
                Optional<FundDashboardBO> existingFund = fundDashboardRepository.findByFundCode(fundCode);
                if (existingFund.isEmpty()) {
                    // 同步基金数据
                    boolean success = syncSingleFundData(fundCode);
                    if (success) {
                        log.info("初始化基金数据成功，基金代码：{}", fundCode);
                    } else {
                        log.warn("初始化基金数据失败，基金代码：{}", fundCode);
                    }
                } else {
                    log.info("基金数据已存在，跳过初始化，基金代码：{}", fundCode);
                }
            } catch (Exception e) {
                log.error("初始化基金数据异常，基金代码：{}，错误：{}", fundCode, e.getMessage(), e);
            }
        }

        log.info("基金数据初始化完成");
    }
}