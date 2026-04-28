package com.spark.stockdashboard.entity.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 历史数据实体类
 * 用于存储股票和基金的历史价格数据
 * JPA实体类，对应数据库表historical_data
 */
@Entity
@Table(name = "historical_data", indexes = {
    @Index(name = "idx_code_type", columnList = "code, type"),
    @Index(name = "idx_trade_date", columnList = "trade_date"),
    @Index(name = "idx_adjustment_type", columnList = "adjustment_type"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDataBO {

    /**
     * 历史数据ID
     * 主键，自增长
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 标的代码（股票代码或基金代码）
     * 不能为空
     */
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    /**
     * 标的类型（STOCK-股票，FUND-基金）
     * 不能为空
     */
    @Column(name = "type", nullable = false, length = 10)
    private String type;

    /**
     * 交易日期
     * 不能为空
     */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    /**
     * 开盘价
     */
    @Column(name = "open_price", precision = 10, scale = 4)
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    @Column(name = "high_price", precision = 10, scale = 4)
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    @Column(name = "low_price", precision = 10, scale = 4)
    private BigDecimal lowPrice;

    /**
     * 收盘价
     */
    @Column(name = "close_price", precision = 10, scale = 4)
    private BigDecimal closePrice;

    /**
     * 涨跌额
     */
    @Column(name = "change_amount", precision = 10, scale = 4)
    private BigDecimal changeAmount;

    /**
     * 涨跌幅（百分比）
     */
    @Column(name = "change_percent", precision = 10, scale = 4)
    private BigDecimal changePercent;

    /**
     * 成交量
     */
    @Column(name = "volume")
    private Long volume;

    /**
     * 成交额
     */
    @Column(name = "turnover", precision = 20, scale = 2)
    private BigDecimal turnover;

    /**
     * 换手率（百分比）
     */
    @Column(name = "turnover_rate", precision = 10, scale = 4)
    private BigDecimal turnoverRate;

    /**
     * 振幅（百分比）
     */
    @Column(name = "amplitude", precision = 10, scale = 4)
    private BigDecimal amplitude;

    /**
     * 市盈率
     */
    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;

    /**
     * 市净率
     */
    @Column(name = "pb_ratio", precision = 10, scale = 2)
    private BigDecimal pbRatio;

    /**
     * 总市值
     */
    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;

    /**
     * 流通市值
     */
    @Column(name = "circulating_market_cap", precision = 20, scale = 2)
    private BigDecimal circulatingMarketCap;

    /**
     * 调整因子（用于复权计算）
     */
    @Column(name = "adjustment_factor", precision = 10, scale = 6)
    private BigDecimal adjustmentFactor;

    /**
     * 复权类型（NONE-不复权，FORWARD-前复权，BACKWARD-后复权）
     */
    @Column(name = "adjustment_type", length = 20)
    private String adjustmentType;

    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 50)
    private String dataSource;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDate createTime;

    /**
     * 获取涨跌颜色
     * @return 涨为红色，跌为绿色，平为灰色
     */
    @Transient
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
    @Transient
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
    @Transient
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
    @Transient
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
    @Transient
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
    @Transient
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
    @Transient
    public boolean isStock() {
        return "STOCK".equals(type);
    }

    /**
     * 判断是否为基金
     * @return true-基金，false-股票
     */
    @Transient
    public boolean isFund() {
        return "FUND".equals(type);
    }

    /**
     * 获取复权类型中文名称
     * @return 复权类型中文名称
     */
    @Transient
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
    @Transient
    public String getTradeDateString() {
        if (tradeDate == null) {
            return "";
        }
        return tradeDate.toString();
    }

    /**
     * 实体保存前的回调方法
     * 设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDate.now();
        }
    }

    /**
     * 复合唯一约束：同一标的同一交易日同一复权类型只能有一条记录
     */
    @Table(
        uniqueConstraints = @UniqueConstraint(
            name = "uk_code_trade_date_adjustment",
            columnNames = {"code", "trade_date", "adjustment_type"}
        )
    )
    public class UniqueConstraintAnnotation {}
}