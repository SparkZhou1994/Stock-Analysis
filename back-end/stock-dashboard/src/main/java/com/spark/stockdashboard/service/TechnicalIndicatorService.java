package com.spark.stockdashboard.service;

import com.spark.stockdashboard.entity.dashboard.HistoricalDataBO;
import com.spark.stockdashboard.repository.HistoricalDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 技术指标服务
 * 负责计算股票和基金的技术指标，如移动平均线、MACD、RSI等
 */
@Service
@Slf4j
public class TechnicalIndicatorService {

    private final HistoricalDataRepository historicalDataRepository;

    @Autowired
    public TechnicalIndicatorService(HistoricalDataRepository historicalDataRepository) {
        this.historicalDataRepository = historicalDataRepository;
    }

    /**
     * 计算移动平均线（MA）
     * @param code 标的代码
     * @param periods 周期列表，如[5, 10, 20, 30, 60]
     * @param endDate 结束日期
     * @param days 计算天数
     * @return 移动平均线数据
     */
    public Map<String, List<BigDecimal>> calculateMovingAverages(String code, List<Integer> periods, LocalDate endDate, int days) {
        try {
            log.debug("开始计算移动平均线，代码：{}，周期：{}，结束日期：{}，天数：{}",
                     code, periods, endDate, days);

            // 获取历史数据
            LocalDate startDate = endDate.minusDays(days + Collections.max(periods));
            List<HistoricalDataBO> historicalData = historicalDataRepository
                    .findByCodeAndTradeDateBetween(code, startDate, endDate);

            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("没有找到历史数据，代码：{}，日期范围：{} - {}", code, startDate, endDate);
                return Collections.emptyMap();
            }

            // 按日期排序
            historicalData.sort(Comparator.comparing(HistoricalDataBO::getTradeDate));

            // 提取收盘价序列
            List<BigDecimal> closePrices = historicalData.stream()
                    .map(HistoricalDataBO::getClosePrice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (closePrices.size() < Collections.max(periods)) {
                log.warn("历史数据不足，无法计算移动平均线，代码：{}，需要数据：{}，实际数据：{}",
                         code, Collections.max(periods), closePrices.size());
                return Collections.emptyMap();
            }

            // 计算各个周期的移动平均线
            Map<String, List<BigDecimal>> result = new HashMap<>();
            for (int period : periods) {
                List<BigDecimal> maValues = calculateSimpleMovingAverage(closePrices, period);
                result.put("MA" + period, maValues);
            }

            log.debug("移动平均线计算完成，代码：{}，结果数量：{}", code, result.size());
            return result;
        } catch (Exception e) {
            log.error("计算移动平均线失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 计算简单移动平均线（SMA）
     * @param prices 价格序列
     * @param period 周期
     * @return 移动平均值序列
     */
    private List<BigDecimal> calculateSimpleMovingAverage(List<BigDecimal> prices, int period) {
        List<BigDecimal> maValues = new ArrayList<>();

        if (prices.size() < period) {
            return maValues;
        }

        for (int i = period - 1; i < prices.size(); i++) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = i - period + 1; j <= i; j++) {
                sum = sum.add(prices.get(j));
            }
            BigDecimal ma = sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
            maValues.add(ma);
        }

        return maValues;
    }

    /**
     * 计算指数移动平均线（EMA）
     * @param code 标的代码
     * @param period 周期
     * @param endDate 结束日期
     * @param days 计算天数
     * @return 指数移动平均值序列
     */
    public List<BigDecimal> calculateExponentialMovingAverage(String code, int period, LocalDate endDate, int days) {
        try {
            log.debug("开始计算指数移动平均线，代码：{}，周期：{}，结束日期：{}，天数：{}",
                     code, period, endDate, days);

            // 获取历史数据
            LocalDate startDate = endDate.minusDays(days + period * 2);
            List<HistoricalDataBO> historicalData = historicalDataRepository
                    .findByCodeAndTradeDateBetween(code, startDate, endDate);

            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("没有找到历史数据，代码：{}，日期范围：{} - {}", code, startDate, endDate);
                return Collections.emptyList();
            }

            // 按日期排序
            historicalData.sort(Comparator.comparing(HistoricalDataBO::getTradeDate));

            // 提取收盘价序列
            List<BigDecimal> closePrices = historicalData.stream()
                    .map(HistoricalDataBO::getClosePrice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (closePrices.size() < period) {
                log.warn("历史数据不足，无法计算指数移动平均线，代码：{}，需要数据：{}，实际数据：{}",
                         code, period, closePrices.size());
                return Collections.emptyList();
            }

            // 计算EMA
            List<BigDecimal> emaValues = calculateEMA(closePrices, period);

            log.debug("指数移动平均线计算完成，代码：{}，结果数量：{}", code, emaValues.size());
            return emaValues;
        } catch (Exception e) {
            log.error("计算指数移动平均线失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 计算指数移动平均线（EMA）
     * @param prices 价格序列
     * @param period 周期
     * @return EMA序列
     */
    private List<BigDecimal> calculateEMA(List<BigDecimal> prices, int period) {
        List<BigDecimal> emaValues = new ArrayList<>();

        if (prices.size() < period) {
            return emaValues;
        }

        // 计算平滑系数
        BigDecimal multiplier = BigDecimal.valueOf(2.0)
                .divide(BigDecimal.valueOf(period + 1), 8, RoundingMode.HALF_UP);

        // 第一个EMA使用SMA
        BigDecimal firstSMA = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            firstSMA = firstSMA.add(prices.get(i));
        }
        firstSMA = firstSMA.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        emaValues.add(firstSMA);

        // 计算后续EMA
        for (int i = period; i < prices.size(); i++) {
            BigDecimal currentPrice = prices.get(i);
            BigDecimal previousEMA = emaValues.get(emaValues.size() - 1);
            BigDecimal currentEMA = currentPrice.subtract(previousEMA)
                    .multiply(multiplier)
                    .add(previousEMA)
                    .setScale(4, RoundingMode.HALF_UP);
            emaValues.add(currentEMA);
        }

        return emaValues;
    }

    /**
     * 计算MACD指标
     * @param code 标的代码
     * @param fastPeriod 快线周期（默认12）
     * @param slowPeriod 慢线周期（默认26）
     * @param signalPeriod 信号线周期（默认9）
     * @param endDate 结束日期
     * @param days 计算天数
     * @return MACD指标数据
     */
    public Map<String, List<BigDecimal>> calculateMACD(String code, int fastPeriod, int slowPeriod,
                                                      int signalPeriod, LocalDate endDate, int days) {
        try {
            log.debug("开始计算MACD指标，代码：{}，快线：{}，慢线：{}，信号线：{}，结束日期：{}，天数：{}",
                     code, fastPeriod, slowPeriod, signalPeriod, endDate, days);

            // 获取历史数据
            LocalDate startDate = endDate.minusDays(days + slowPeriod * 2);
            List<HistoricalDataBO> historicalData = historicalDataRepository
                    .findByCodeAndTradeDateBetween(code, startDate, endDate);

            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("没有找到历史数据，代码：{}，日期范围：{} - {}", code, startDate, endDate);
                return Collections.emptyMap();
            }

            // 按日期排序
            historicalData.sort(Comparator.comparing(HistoricalDataBO::getTradeDate));

            // 提取收盘价序列
            List<BigDecimal> closePrices = historicalData.stream()
                    .map(HistoricalDataBO::getClosePrice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (closePrices.size() < slowPeriod) {
                log.warn("历史数据不足，无法计算MACD，代码：{}，需要数据：{}，实际数据：{}",
                         code, slowPeriod, closePrices.size());
                return Collections.emptyMap();
            }

            // 计算快线EMA和慢线EMA
            List<BigDecimal> fastEMA = calculateEMA(closePrices, fastPeriod);
            List<BigDecimal> slowEMA = calculateEMA(closePrices, slowPeriod);

            // 计算DIF（快线EMA - 慢线EMA）
            List<BigDecimal> difValues = new ArrayList<>();
            int minLength = Math.min(fastEMA.size(), slowEMA.size());
            for (int i = 0; i < minLength; i++) {
                BigDecimal dif = fastEMA.get(i).subtract(slowEMA.get(i));
                difValues.add(dif);
            }

            // 计算DEA（DIF的EMA）
            List<BigDecimal> deaValues = calculateEMA(difValues, signalPeriod);

            // 计算MACD柱（DIF - DEA）
            List<BigDecimal> macdHistogram = new ArrayList<>();
            int macdMinLength = Math.min(difValues.size(), deaValues.size());
            for (int i = 0; i < macdMinLength; i++) {
                BigDecimal macd = difValues.get(i).subtract(deaValues.get(i));
                macdHistogram.add(macd);
            }

            // 构建结果
            Map<String, List<BigDecimal>> result = new HashMap<>();
            result.put("DIF", difValues);
            result.put("DEA", deaValues);
            result.put("MACD", macdHistogram);

            log.debug("MACD指标计算完成，代码：{}，DIF数量：{}，DEA数量：{}，MACD数量：{}",
                     code, difValues.size(), deaValues.size(), macdHistogram.size());
            return result;
        } catch (Exception e) {
            log.error("计算MACD指标失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 计算相对强弱指标（RSI）
     * @param code 标的代码
     * @param period RSI周期（默认14）
     * @param endDate 结束日期
     * @param days 计算天数
     * @return RSI值序列
     */
    public List<BigDecimal> calculateRSI(String code, int period, LocalDate endDate, int days) {
        try {
            log.debug("开始计算RSI指标，代码：{}，周期：{}，结束日期：{}，天数：{}",
                     code, period, endDate, days);

            // 获取历史数据
            LocalDate startDate = endDate.minusDays(days + period * 2);
            List<HistoricalDataBO> historicalData = historicalDataRepository
                    .findByCodeAndTradeDateBetween(code, startDate, endDate);

            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("没有找到历史数据，代码：{}，日期范围：{} - {}", code, startDate, endDate);
                return Collections.emptyList();
            }

            // 按日期排序
            historicalData.sort(Comparator.comparing(HistoricalDataBO::getTradeDate));

            // 提取收盘价序列
            List<BigDecimal> closePrices = historicalData.stream()
                    .map(HistoricalDataBO::getClosePrice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (closePrices.size() < period + 1) {
                log.warn("历史数据不足，无法计算RSI，代码：{}，需要数据：{}，实际数据：{}",
                         code, period + 1, closePrices.size());
                return Collections.emptyList();
            }

            // 计算价格变化
            List<BigDecimal> priceChanges = new ArrayList<>();
            for (int i = 1; i < closePrices.size(); i++) {
                BigDecimal change = closePrices.get(i).subtract(closePrices.get(i - 1));
                priceChanges.add(change);
            }

            // 计算RSI
            List<BigDecimal> rsiValues = new ArrayList<>();
            for (int i = period; i < priceChanges.size(); i++) {
                BigDecimal avgGain = BigDecimal.ZERO;
                BigDecimal avgLoss = BigDecimal.ZERO;

                // 计算初始平均值
                for (int j = i - period; j < i; j++) {
                    BigDecimal change = priceChanges.get(j);
                    if (change.compareTo(BigDecimal.ZERO) > 0) {
                        avgGain = avgGain.add(change);
                    } else {
                        avgLoss = avgLoss.add(change.abs());
                    }
                }

                avgGain = avgGain.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
                avgLoss = avgLoss.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);

                // 计算RSI
                BigDecimal rs = avgLoss.compareTo(BigDecimal.ZERO) == 0 ?
                        BigDecimal.valueOf(100) :
                        avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
                BigDecimal rsi = BigDecimal.valueOf(100)
                        .subtract(BigDecimal.valueOf(100)
                                .divide(BigDecimal.ONE.add(rs), 4, RoundingMode.HALF_UP));

                rsiValues.add(rsi);
            }

            log.debug("RSI指标计算完成，代码：{}，结果数量：{}", code, rsiValues.size());
            return rsiValues;
        } catch (Exception e) {
            log.error("计算RSI指标失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 计算布林带（Bollinger Bands）
     * @param code 标的代码
     * @param period 周期（默认20）
     * @param stdDev 标准差倍数（默认2）
     * @param endDate 结束日期
     * @param days 计算天数
     * @return 布林带数据
     */
    public Map<String, List<BigDecimal>> calculateBollingerBands(String code, int period, int stdDev,
                                                                LocalDate endDate, int days) {
        try {
            log.debug("开始计算布林带，代码：{}，周期：{}，标准差倍数：{}，结束日期：{}，天数：{}",
                     code, period, stdDev, endDate, days);

            // 获取历史数据
            LocalDate startDate = endDate.minusDays(days + period * 2);
            List<HistoricalDataBO> historicalData = historicalDataRepository
                    .findByCodeAndTradeDateBetween(code, startDate, endDate);

            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("没有找到历史数据，代码：{}，日期范围：{} - {}", code, startDate, endDate);
                return Collections.emptyMap();
            }

            // 按日期排序
            historicalData.sort(Comparator.comparing(HistoricalDataBO::getTradeDate));

            // 提取收盘价序列
            List<BigDecimal> closePrices = historicalData.stream()
                    .map(HistoricalDataBO::getClosePrice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (closePrices.size() < period) {
                log.warn("历史数据不足，无法计算布林带，代码：{}，需要数据：{}，实际数据：{}",
                         code, period, closePrices.size());
                return Collections.emptyMap();
            }

            // 计算中轨（SMA）
            List<BigDecimal> middleBand = calculateSimpleMovingAverage(closePrices, period);

            // 计算标准差和上下轨
            List<BigDecimal> upperBand = new ArrayList<>();
            List<BigDecimal> lowerBand = new ArrayList<>();

            for (int i = period - 1; i < closePrices.size(); i++) {
                // 计算标准差
                BigDecimal mean = middleBand.get(i - period + 1);
                BigDecimal variance = BigDecimal.ZERO;

                for (int j = i - period + 1; j <= i; j++) {
                    BigDecimal diff = closePrices.get(j).subtract(mean);
                    variance = variance.add(diff.multiply(diff));
                }

                BigDecimal stdDeviation = BigDecimal.valueOf(Math.sqrt(
                        variance.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP).doubleValue()
                ));

                // 计算上下轨
                BigDecimal upper = mean.add(stdDeviation.multiply(BigDecimal.valueOf(stdDev)));
                BigDecimal lower = mean.subtract(stdDeviation.multiply(BigDecimal.valueOf(stdDev)));

                upperBand.add(upper.setScale(4, RoundingMode.HALF_UP));
                lowerBand.add(lower.setScale(4, RoundingMode.HALF_UP));
            }

            // 构建结果
            Map<String, List<BigDecimal>> result = new HashMap<>();
            result.put("middle", middleBand);
            result.put("upper", upperBand);
            result.put("lower", lowerBand);

            log.debug("布林带计算完成，代码：{}，中轨数量：{}，上轨数量：{}，下轨数量：{}",
                     code, middleBand.size(), upperBand.size(), lowerBand.size());
            return result;
        } catch (Exception e) {
            log.error("计算布林带失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 计算成交量指标
     * @param code 标的代码
     * @param period 周期
     * @param endDate 结束日期
     * @param days 计算天数
     * @return 成交量指标数据
     */
    public Map<String, Object> calculateVolumeIndicators(String code, int period, LocalDate endDate, int days) {
        try {
            log.debug("开始计算成交量指标，代码：{}，周期：{}，结束日期：{}，天数：{}",
                     code, period, endDate, days);

            // 获取历史数据
            LocalDate startDate = endDate.minusDays(days + period);
            List<HistoricalDataBO> historicalData = historicalDataRepository
                    .findByCodeAndTradeDateBetween(code, startDate, endDate);

            if (historicalData == null || historicalData.isEmpty()) {
                log.warn("没有找到历史数据，代码：{}，日期范围：{} - {}", code, startDate, endDate);
                return Collections.emptyMap();
            }

            // 按日期排序
            historicalData.sort(Comparator.comparing(HistoricalDataBO::getTradeDate));

            // 提取成交量和收盘价
            List<Long> volumes = historicalData.stream()
                    .map(HistoricalDataBO::getVolume)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<BigDecimal> closePrices = historicalData.stream()
                    .map(HistoricalDataBO::getClosePrice)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (volumes.size() < period || closePrices.size() < period) {
                log.warn("历史数据不足，无法计算成交量指标，代码：{}，需要数据：{}，实际数据：{}",
                         code, period, Math.min(volumes.size(), closePrices.size()));
                return Collections.emptyMap();
            }

            // 计算成交量移动平均
            List<BigDecimal> volumeMA = new ArrayList<>();
            for (int i = period - 1; i < volumes.size(); i++) {
                long sum = 0;
                for (int j = i - period + 1; j <= i; j++) {
                    sum += volumes.get(j);
                }
                BigDecimal avg = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(period), 0, RoundingMode.HALF_UP);
                volumeMA.add(avg);
            }

            // 计算量比（当前成交量 / 过去N日平均成交量）
            List<BigDecimal> volumeRatio = new ArrayList<>();
            for (int i = period; i < volumes.size(); i++) {
                long currentVolume = volumes.get(i);
                long pastAvgVolume = 0;
                for (int j = i - period; j < i; j++) {
                    pastAvgVolume += volumes.get(j);
                }
                pastAvgVolume /= period;

                BigDecimal ratio = pastAvgVolume == 0 ?
                        BigDecimal.ZERO :
                        BigDecimal.valueOf(currentVolume).divide(BigDecimal.valueOf(pastAvgVolume), 4, RoundingMode.HALF_UP);
                volumeRatio.add(ratio);
            }

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("volumeMA", volumeMA);
            result.put("volumeRatio", volumeRatio);
            result.put("volumes", volumes.subList(period - 1, volumes.size()));

            log.debug("成交量指标计算完成，代码：{}，成交量MA数量：{}，量比数量：{}",
                     code, volumeMA.size(), volumeRatio.size());
            return result;
        } catch (Exception e) {
            log.error("计算成交量指标失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 获取技术指标摘要
     * @param code 标的代码
     * @return 技术指标摘要
     */
    public Map<String, Object> getTechnicalIndicatorsSummary(String code) {
        try {
            LocalDate endDate = LocalDate.now();
            Map<String, Object> summary = new HashMap<>();

            // 计算移动平均线
            Map<String, List<BigDecimal>> maResults = calculateMovingAverages(
                    code, Arrays.asList(5, 10, 20, 30, 60), endDate, 60);
            if (!maResults.isEmpty()) {
                for (Map.Entry<String, List<BigDecimal>> entry : maResults.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        summary.put(entry.getKey(), entry.getValue().get(entry.getValue().size() - 1));
                    }
                }
            }

            // 计算RSI
            List<BigDecimal> rsiValues = calculateRSI(code, 14, endDate, 30);
            if (!rsiValues.isEmpty()) {
                summary.put("RSI", rsiValues.get(rsiValues.size() - 1));
            }

            // 计算MACD
            Map<String, List<BigDecimal>> macdResults = calculateMACD(code, 12, 26, 9, endDate, 30);
            if (!macdResults.isEmpty()) {
                for (Map.Entry<String, List<BigDecimal>> entry : macdResults.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        summary.put(entry.getKey(), entry.getValue().get(entry.getValue().size() - 1));
                    }
                }
            }

            summary.put("code", code);
            summary.put("calculationDate", endDate);

            log.debug("获取技术指标摘要完成，代码：{}，指标数量：{}", code, summary.size());
            return summary;
        } catch (Exception e) {
            log.error("获取技术指标摘要失败，代码：{}，错误：{}", code, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}