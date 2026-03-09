package com.spark.stockdashboard.entity.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 看板股票实体类
 * 用于展示在股票基金看板中的股票信息
 * 不修改现有的StockBO类，创建新的看板专用实体
 */
@Data
public class StockDashboardBO {

    /**
     * 股票ID
     */
    private Long id;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 涨跌额
     */
    private BigDecimal changeAmount;

    /**
     * 涨跌幅（百分比）
     */
    private BigDecimal changePercent;

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
     * 昨日收盘价
     */
    private BigDecimal previousClose;

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
     * 行业分类
     */
    private String industry;

    /**
     * 股票类型（A股、港股、美股等）
     */
    private String stockType;

    /**
     * 是否自选
     */
    private Boolean isWatchlist;

    /**
     * 自选顺序
     */
    private Integer watchlistOrder;

    /**
     * 数据更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否活跃
     */
    private Boolean isActive;

    /**
     * 数据来源（东方财富、雪球等）
     */
    private String dataSource;

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
     * 获取格式化后的价格
     * @return 保留两位小数的价格字符串
     */
    public String getFormattedPrice() {
        if (currentPrice == null) {
            return "0.00";
        }
        return String.format("%.2f", currentPrice);
    }
}