package com.spark.stockdashboard.entity.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 历史数据实体类
 * 用于存储股票和基金的历史价格数据
 */
@Data
public class HistoricalDataBO {

    /**
     * 历史数据ID
     */
    private Long id;

    /**
     * 标的代码（股票代码或基金代码）
     */
    private String code;

    /**
     * 标的类型（STOCK-股票，FUND-基金）
     */
    private String type;

    /**
     * 交易日期
     */
    private LocalDate tradeDate;

    /**
     * 开盘价
     */
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    private BigDecimal lowPrice;

    /**
     * 收盘价
     */
    private BigDecimal closePrice;

    /**
     * 涨跌额
     */
    private BigDecimal changeAmount;

    /**
     * 涨跌幅（百分比）
     */
    private BigDecimal changePercent;

    /**
     * 成交量
     */
    private Long volume;

    /**
     * 成交额
     */
    private BigDecimal turnover;

    /**
     * 换手率（百分比）
     */
    private BigDecimal turnoverRate;

    /**
     * 振幅（百分比）
     */
    private BigDecimal amplitude;

    /**
     * 市盈率
     */
    private BigDecimal peRatio;

    /**
     * 市净率
     */
    private BigDecimal pbRatio;

    /**
     * 总市值
     */
    private BigDecimal marketCap;

    /**
     * 流通市值
     */
    private BigDecimal circulatingMarketCap;

    /**
     * 调整因子（用于复权计算）
     */
    private BigDecimal adjustmentFactor;

    /**
     * 复权类型（NONE-不复权，FORWARD-前复权，BACKWARD-后复权）
     */
    private String adjustmentType;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 创建时间
     */
    private LocalDate createTime;

    /**
     * 获取涨跌颜色
     * @return 涨为红色，跌为绿色，平为灰色
     */
    public String getChangeColor() {
        if (changeAmount == null) {
            return "gray";
        }
        return changeAmount.compareTo(BigDecimal.ZERO) > 0 ? "red" :
               changeAmount.compareTo(BigDecimal.ZERO) < 0 ? "green" : "gray";
    }

    /**
     * 获取涨跌图标
     * @return 涨为↑，跌为↓，平为-
     */
    public String getChangeIcon() {
        if (changeAmount == null) {
            return "-";
        }
        return changeAmount.compareTo(BigDecimal.ZERO) > 0 ? "↑" :
               changeAmount.compareTo(BigDecimal.ZERO) < 0 ? "↓" : "-";
    }

    /**
     * 获取格式化后的涨跌幅
     * @return 带百分号的涨跌幅字符串
     */
    public String getFormattedChangePercent() {
        if (changePercent == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", changePercent);
    }

    /**
     * 获取格式化后的收盘价
     * @return 保留两位小数的价格字符串（基金保留四位）
     */
    public String getFormattedClosePrice() {
        if (closePrice == null) {
            return "0.00";
        }
        if ("FUND".equals(type)) {
            return String.format("%.4f", closePrice);
        }
        return String.format("%.2f", closePrice);
    }

    /**
     * 获取格式化后的成交量
     * @return 带单位的成交量字符串（万、亿）
     */
    public String getFormattedVolume() {
        if (volume == null) {
            return "0";
        }
        if (volume >= 100000000) {
            return String.format("%.2f亿", volume / 100000000.0);
        } else if (volume >= 10000) {
            return String.format("%.2f万", volume / 10000.0);
        }
        return volume.toString();
    }

    /**
     * 获取格式化后的成交额
     * @return 带单位的成交额字符串（万、亿）
     */
    public String getFormattedTurnover() {
        if (turnover == null) {
            return "0";
        }
        if (turnover.compareTo(new BigDecimal("100000000")) >= 0) {
            return String.format("%.2f亿", turnover.divide(new BigDecimal("100000000")));
        } else if (turnover.compareTo(new BigDecimal("10000")) >= 0) {
            return String.format("%.2f万", turnover.divide(new BigDecimal("10000")));
        }
        return turnover.toString();
    }

    /**
     * 判断是否为股票
     * @return true-股票，false-基金
     */
    public boolean isStock() {
        return "STOCK".equals(type);
    }

    /**
     * 判断是否为基金
     * @return true-基金，false-股票
     */
    public boolean isFund() {
        return "FUND".equals(type);
    }

    /**
     * 获取复权类型中文名称
     * @return 复权类型中文名称
     */
    public String getAdjustmentTypeChinese() {
        if (adjustmentType == null) {
            return "不复权";
        }
        switch (adjustmentType.toUpperCase()) {
            case "NONE":
                return "不复权";
            case "FORWARD":
                return "前复权";
            case "BACKWARD":
                return "后复权";
            default:
                return adjustmentType;
        }
    }

    /**
     * 获取交易日期字符串（YYYY-MM-DD格式）
     * @return 交易日期字符串
     */
    public String getTradeDateString() {
        if (tradeDate == null) {
            return "";
        }
        return tradeDate.toString();
    }
}