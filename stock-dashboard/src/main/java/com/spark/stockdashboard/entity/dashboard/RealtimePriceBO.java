package com.spark.stockdashboard.entity.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实时价格实体类
 * 用于存储股票和基金的实时价格数据
 * JPA实体类，对应数据库表realtime_price
 */
@Entity
@Table(name = "realtime_price", indexes = {
    @Index(name = "idx_code_type", columnList = "code, type"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_trading_status", columnList = "trading_status"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimePriceBO {

    /**
     * 价格ID
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
     * 当前价格
     */
    @Column(name = "price", precision = 10, scale = 4)
    private BigDecimal price;

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
     * 昨日收盘价
     */
    @Column(name = "previous_close", precision = 10, scale = 4)
    private BigDecimal previousClose;

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
     * 委比（百分比）
     */
    @Column(name = "commission_ratio", precision = 10, scale = 4)
    private BigDecimal commissionRatio;

    /**
     * 量比
     */
    @Column(name = "volume_ratio", precision = 10, scale = 4)
    private BigDecimal volumeRatio;

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
     * 买一价
     */
    @Column(name = "bid_price1", precision = 10, scale = 4)
    private BigDecimal bidPrice1;

    /**
     * 买一量
     */
    @Column(name = "bid_volume1")
    private Long bidVolume1;

    /**
     * 卖一价
     */
    @Column(name = "ask_price1", precision = 10, scale = 4)
    private BigDecimal askPrice1;

    /**
     * 卖一量
     */
    @Column(name = "ask_volume1")
    private Long askVolume1;

    /**
     * 时间戳（精确到秒）
     * 不能为空
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * 交易状态（交易中、停牌、休市等）
     */
    @Column(name = "trading_status", length = 50)
    private String tradingStatus;

    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 50)
    private String dataSource;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

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
     * 获取格式化后的价格
     * @return 保留两位小数的价格字符串（基金保留四位）
     */
    @Transient
    public String getFormattedPrice() {
        if (price == null) {
            return "0.00";
        }
        if ("FUND".equals(type)) {
            return String.format("%.4f", price);
        }
        return String.format("%.2f", price);
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
     * 实体保存前的回调方法
     * 设置创建时间和默认值
     */
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * 复合唯一约束：同一时间同一标的只能有一条记录
     */
    @Table(
        uniqueConstraints = @UniqueConstraint(
            name = "uk_code_timestamp",
            columnNames = {"code", "timestamp"}
        )
    )
    public class UniqueConstraintAnnotation {}
}