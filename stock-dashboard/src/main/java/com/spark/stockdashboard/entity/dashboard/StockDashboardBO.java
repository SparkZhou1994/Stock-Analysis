package com.spark.stockdashboard.entity.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 看板股票实体类
 * 用于展示在股票基金看板中的股票信息
 * 不修改现有的StockBO类，创建新的看板专用实体
 * JPA实体类，对应数据库表stock_dashboard
 */
@Entity
@Table(name = "stock_dashboard", indexes = {
    @Index(name = "idx_stock_code", columnList = "stock_code"),
    @Index(name = "idx_stock_type", columnList = "stock_type"),
    @Index(name = "idx_industry", columnList = "industry"),
    @Index(name = "idx_is_watchlist", columnList = "is_watchlist"),
    @Index(name = "idx_update_time", columnList = "update_time"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDashboardBO {

    /**
     * 股票ID
     * 主键，自增长
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 股票代码
     * 唯一索引，不能为空
     */
    @Column(name = "stock_code", nullable = false, length = 20, unique = true)
    private String stockCode;

    /**
     * 股票名称
     */
    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    /**
     * 当前价格
     */
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    /**
     * 涨跌额
     */
    @Column(name = "change_amount", precision = 10, scale = 2)
    private BigDecimal changeAmount;

    /**
     * 涨跌幅（百分比）
     */
    @Column(name = "change_percent", precision = 10, scale = 4)
    private BigDecimal changePercent;

    /**
     * 开盘价
     */
    @Column(name = "open_price", precision = 10, scale = 2)
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    @Column(name = "high_price", precision = 10, scale = 2)
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    @Column(name = "low_price", precision = 10, scale = 2)
    private BigDecimal lowPrice;

    /**
     * 昨日收盘价
     */
    @Column(name = "previous_close", precision = 10, scale = 2)
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
     * 行业分类
     */
    @Column(name = "industry", length = 100)
    private String industry;

    /**
     * 股票类型（A股、港股、美股等）
     */
    @Column(name = "stock_type", length = 20)
    private String stockType;

    /**
     * 是否自选
     */
    @Column(name = "is_watchlist")
    private Boolean isWatchlist;

    /**
     * 自选顺序
     */
    @Column(name = "watchlist_order")
    private Integer watchlistOrder;

    /**
     * 数据更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 是否活跃
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * 数据来源（东方财富、雪球等）
     */
    @Column(name = "data_source", length = 50)
    private String dataSource;

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
     * @return 保留两位小数的价格字符串
     */
    @Transient
    public String getFormattedPrice() {
        if (currentPrice == null) {
            return "0.00";
        }
        return String.format("%.2f", currentPrice);
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
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isWatchlist == null) {
            isWatchlist = false;
        }
    }

    /**
     * 实体更新前的回调方法
     * 更新更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}