package com.spark.stockdashboard.entity.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 看板基金实体类
 * 用于展示在股票基金看板中的基金信息
 * 不修改现有的FundBO类，创建新的看板专用实体
 * JPA实体类，对应数据库表fund_dashboard
 */
@Entity
@Table(name = "fund_dashboard", indexes = {
    @Index(name = "idx_fund_code", columnList = "fund_code"),
    @Index(name = "idx_fund_type", columnList = "fund_type"),
    @Index(name = "idx_fund_company", columnList = "fund_company"),
    @Index(name = "idx_is_watchlist", columnList = "is_watchlist"),
    @Index(name = "idx_update_time", columnList = "update_time"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundDashboardBO {

    /**
     * 基金ID
     * 主键，自增长
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 基金代码
     * 唯一索引，不能为空
     */
    @Column(name = "fund_code", nullable = false, length = 20, unique = true)
    private String fundCode;

    /**
     * 基金名称
     */
    @Column(name = "fund_name", nullable = false, length = 100)
    private String fundName;

    /**
     * 基金类型（股票型、混合型、债券型等）
     */
    @Column(name = "fund_type", length = 50)
    private String fundType;

    /**
     * 当前净值
     */
    @Column(name = "current_net_value", precision = 10, scale = 4)
    private BigDecimal currentNetValue;

    /**
     * 日涨跌额
     */
    @Column(name = "daily_change_amount", precision = 10, scale = 4)
    private BigDecimal dailyChangeAmount;

    /**
     * 日涨跌幅（百分比）
     */
    @Column(name = "daily_change_percent", precision = 10, scale = 4)
    private BigDecimal dailyChangePercent;

    /**
     * 周涨跌幅
     */
    @Column(name = "weekly_change_percent", precision = 10, scale = 4)
    private BigDecimal weeklyChangePercent;

    /**
     * 月涨跌幅
     */
    @Column(name = "monthly_change_percent", precision = 10, scale = 4)
    private BigDecimal monthlyChangePercent;

    /**
     * 年涨跌幅
     */
    @Column(name = "yearly_change_percent", precision = 10, scale = 4)
    private BigDecimal yearlyChangePercent;

    /**
     * 累计净值
     */
    @Column(name = "accumulated_net_value", precision = 10, scale = 4)
    private BigDecimal accumulatedNetValue;

    /**
     * 成立日期
     */
    @Column(name = "establishment_date")
    private LocalDateTime establishmentDate;

    /**
     * 基金规模（亿元）
     */
    @Column(name = "fund_size", precision = 15, scale = 2)
    private BigDecimal fundSize;

    /**
     * 基金经理
     */
    @Column(name = "fund_manager", length = 100)
    private String fundManager;

    /**
     * 基金管理公司
     */
    @Column(name = "fund_company", length = 100)
    private String fundCompany;

    /**
     * 风险等级（低、中、高）
     */
    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    /**
     * 申购状态（开放申购、暂停申购等）
     */
    @Column(name = "purchase_status", length = 50)
    private String purchaseStatus;

    /**
     * 赎回状态（开放赎回、暂停赎回等）
     */
    @Column(name = "redemption_status", length = 50)
    private String redemptionStatus;

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
     * 数据来源（东方财富、天天基金等）
     */
    @Column(name = "data_source", length = 50)
    private String dataSource;

    /**
     * 获取涨跌颜色
     * @return 涨为红色，跌为绿色，平为灰色
     */
    @Transient
    public String getChangeColor() {
        if (dailyChangeAmount == null) {
            return "gray";
        }
        return dailyChangeAmount.compareTo(BigDecimal.ZERO) > 0 ? "red" :
               dailyChangeAmount.compareTo(BigDecimal.ZERO) < 0 ? "green" : "gray";
    }

    /**
     * 获取涨跌图标
     * @return 涨为↑，跌为↓，平为-
     */
    @Transient
    public String getChangeIcon() {
        if (dailyChangeAmount == null) {
            return "-";
        }
        return dailyChangeAmount.compareTo(BigDecimal.ZERO) > 0 ? "↑" :
               dailyChangeAmount.compareTo(BigDecimal.ZERO) < 0 ? "↓" : "-";
    }

    /**
     * 获取格式化后的日涨跌幅
     * @return 带百分号的涨跌幅字符串
     */
    @Transient
    public String getFormattedDailyChangePercent() {
        if (dailyChangePercent == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", dailyChangePercent);
    }

    /**
     * 获取格式化后的净值
     * @return 保留四位小数的净值字符串
     */
    @Transient
    public String getFormattedNetValue() {
        if (currentNetValue == null) {
            return "0.0000";
        }
        return String.format("%.4f", currentNetValue);
    }

    /**
     * 获取基金类型中文名称
     * @return 基金类型中文名称
     */
    @Transient
    public String getFundTypeChinese() {
        if (fundType == null) {
            return "未知";
        }
        switch (fundType.toLowerCase()) {
            case "stock":
                return "股票型";
            case "mixed":
                return "混合型";
            case "bond":
                return "债券型";
            case "index":
                return "指数型";
            case "monetary":
                return "货币型";
            case "qdii":
                return "QDII";
            default:
                return fundType;
        }
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