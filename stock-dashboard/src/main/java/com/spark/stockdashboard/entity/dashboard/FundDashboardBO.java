package com.spark.stockdashboard.entity.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 看板基金实体类
 * 用于展示在股票基金看板中的基金信息
 * 不修改现有的FundBO类，创建新的看板专用实体
 */
@Data
public class FundDashboardBO {

    /**
     * 基金ID
     */
    private Long id;

    /**
     * 基金代码
     */
    private String fundCode;

    /**
     * 基金名称
     */
    private String fundName;

    /**
     * 基金类型（股票型、混合型、债券型等）
     */
    private String fundType;

    /**
     * 当前净值
     */
    private BigDecimal currentNetValue;

    /**
     * 日涨跌额
     */
    private BigDecimal dailyChangeAmount;

    /**
     * 日涨跌幅（百分比）
     */
    private BigDecimal dailyChangePercent;

    /**
     * 周涨跌幅
     */
    private BigDecimal weeklyChangePercent;

    /**
     * 月涨跌幅
     */
    private BigDecimal monthlyChangePercent;

    /**
     * 年涨跌幅
     */
    private BigDecimal yearlyChangePercent;

    /**
     * 累计净值
     */
    private BigDecimal accumulatedNetValue;

    /**
     * 成立日期
     */
    private LocalDateTime establishmentDate;

    /**
     * 基金规模（亿元）
     */
    private BigDecimal fundSize;

    /**
     * 基金经理
     */
    private String fundManager;

    /**
     * 基金管理公司
     */
    private String fundCompany;

    /**
     * 风险等级（低、中、高）
     */
    private String riskLevel;

    /**
     * 申购状态（开放申购、暂停申购等）
     */
    private String purchaseStatus;

    /**
     * 赎回状态（开放赎回、暂停赎回等）
     */
    private String redemptionStatus;

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
     * 数据来源（东方财富、天天基金等）
     */
    private String dataSource;

    /**
     * 获取涨跌颜色
     * @return 涨为红色，跌为绿色，平为灰色
     */
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
}