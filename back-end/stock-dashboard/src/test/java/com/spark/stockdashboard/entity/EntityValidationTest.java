package com.spark.stockdashboard.entity;

import com.spark.stockdashboard.entity.dashboard.*;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 实体类验证测试
 * 测试JPA实体类的注解和约束
 */
class EntityValidationTest {

    private final Validator validator;

    EntityValidationTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testStockDashboardBOValidation() {
        // 测试有效的StockDashboardBO
        StockDashboardBO stock = StockDashboardBO.builder()
                .stockCode("000001")
                .stockName("平安银行")
                .currentPrice(new BigDecimal("15.50"))
                .changeAmount(new BigDecimal("0.25"))
                .changePercent(new BigDecimal("1.64"))
                .createTime(LocalDateTime.now())
                .isActive(true)
                .build();

        var violations = validator.validate(stock);
        assertThat(violations).isEmpty();

        // 测试无效的StockDashboardBO（缺少必要字段）
        StockDashboardBO invalidStock = StockDashboardBO.builder()
                .stockCode(null)  // 不能为空
                .stockName(null)  // 不能为空
                .build();

        var invalidViolations = validator.validate(invalidStock);
        assertThat(invalidViolations).isNotEmpty();
    }

    @Test
    void testFundDashboardBOValidation() {
        // 测试有效的FundDashboardBO
        FundDashboardBO fund = FundDashboardBO.builder()
                .fundCode("000001")
                .fundName("华夏成长")
                .currentNetValue(new BigDecimal("1.2345"))
                .dailyChangePercent(new BigDecimal("0.56"))
                .createTime(LocalDateTime.now())
                .isActive(true)
                .build();

        var violations = validator.validate(fund);
        assertThat(violations).isEmpty();
    }

    @Test
    void testRealtimePriceBOValidation() {
        // 测试有效的RealtimePriceBO
        RealtimePriceBO price = RealtimePriceBO.builder()
                .code("000001")
                .type("STOCK")
                .price(new BigDecimal("15.50"))
                .timestamp(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();

        var violations = validator.validate(price);
        assertThat(violations).isEmpty();

        // 测试无效的RealtimePriceBO（缺少必要字段）
        RealtimePriceBO invalidPrice = RealtimePriceBO.builder()
                .code(null)  // 不能为空
                .type(null)  // 不能为空
                .build();

        var invalidViolations = validator.validate(invalidPrice);
        assertThat(invalidViolations).isNotEmpty();
    }

    @Test
    void testHistoricalDataBOValidation() {
        // 测试有效的HistoricalDataBO
        HistoricalDataBO historicalData = HistoricalDataBO.builder()
                .code("000001")
                .type("STOCK")
                .tradeDate(LocalDate.now())
                .closePrice(new BigDecimal("15.50"))
                .createTime(LocalDate.now())
                .build();

        var violations = validator.validate(historicalData);
        assertThat(violations).isEmpty();

        // 测试无效的HistoricalDataBO（缺少必要字段）
        HistoricalDataBO invalidData = HistoricalDataBO.builder()
                .code(null)  // 不能为空
                .type(null)  // 不能为空
                .tradeDate(null)  // 不能为空
                .build();

        var invalidViolations = validator.validate(invalidData);
        assertThat(invalidViolations).isNotEmpty();
    }

    @Test
    void testStockDashboardBOBusinessMethods() {
        StockDashboardBO stock = StockDashboardBO.builder()
                .stockCode("000001")
                .stockName("平安银行")
                .currentPrice(new BigDecimal("15.50"))
                .changeAmount(new BigDecimal("0.25"))
                .changePercent(new BigDecimal("1.64"))
                .createTime(LocalDateTime.now())
                .isActive(true)
                .build();

        // 测试业务方法
        assertThat(stock.getChangeColor()).isEqualTo("red");
        assertThat(stock.getChangeIcon()).isEqualTo("↑");
        assertThat(stock.getFormattedChangePercent()).isEqualTo("1.64%");
        assertThat(stock.getFormattedPrice()).isEqualTo("15.50");

        // 测试下跌情况
        stock.setChangeAmount(new BigDecimal("-0.25"));
        stock.setChangePercent(new BigDecimal("-1.64"));
        assertThat(stock.getChangeColor()).isEqualTo("green");
        assertThat(stock.getChangeIcon()).isEqualTo("↓");
        assertThat(stock.getFormattedChangePercent()).isEqualTo("-1.64%");

        // 测试平盘情况
        stock.setChangeAmount(BigDecimal.ZERO);
        stock.setChangePercent(BigDecimal.ZERO);
        assertThat(stock.getChangeColor()).isEqualTo("gray");
        assertThat(stock.getChangeIcon()).isEqualTo("-");
        assertThat(stock.getFormattedChangePercent()).isEqualTo("0.00%");
    }

    @Test
    void testRealtimePriceBOBusinessMethods() {
        RealtimePriceBO price = RealtimePriceBO.builder()
                .code("000001")
                .type("STOCK")
                .price(new BigDecimal("15.50"))
                .volume(1000000L)
                .turnover(new BigDecimal("15500000"))
                .timestamp(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();

        // 测试业务方法
        assertThat(price.getFormattedPrice()).isEqualTo("15.50");
        assertThat(price.getFormattedVolume()).isEqualTo("100.00万");
        assertThat(price.getFormattedTurnover()).isEqualTo("1550.00万");
        assertThat(price.isStock()).isTrue();
        assertThat(price.isFund()).isFalse();

        // 测试基金类型
        price.setType("FUND");
        price.setPrice(new BigDecimal("1.2345"));
        assertThat(price.getFormattedPrice()).isEqualTo("1.2345");
        assertThat(price.isStock()).isFalse();
        assertThat(price.isFund()).isTrue();
    }
}