package com.spark.stockdashboard.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 熔断器配置类
 * 配置Resilience4j熔断器
 */
@Configuration
public class Resilience4jCircuitBreakerConfig {

    /**
     * 配置熔断器注册表
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    /**
     * 东方财富API熔断器配置
     */
    @Bean
    public CircuitBreaker eastMoneyApiCircuitBreaker(CircuitBreakerRegistry registry) {
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                // 失败率阈值，超过50%触发熔断
                .failureRateThreshold(50)
                // 慢调用率阈值，超过100%触发熔断
                .slowCallRateThreshold(100)
                // 慢调用持续时间阈值，超过2秒视为慢调用
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                // 熔断持续时间，初始60秒
                .waitDurationInOpenState(Duration.ofSeconds(60))
                // 半开状态允许的调用次数
                .permittedNumberOfCallsInHalfOpenState(5)
                // 最小调用次数，最少10次调用才计算指标
                .minimumNumberOfCalls(10)
                // 滑动窗口大小
                .slidingWindowSize(100)
                // 滑动窗口类型
                .slidingWindowType(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 记录异常
                .recordExceptions(
                        org.springframework.web.client.ResourceAccessException.class,
                        java.net.SocketTimeoutException.class,
                        java.net.ConnectException.class,
                        org.springframework.web.client.HttpServerErrorException.class
                )
                // 忽略异常（不计入失败）
                .ignoreExceptions(
                        org.springframework.web.client.HttpClientErrorException.class
                )
                // 自动从打开状态转换到半开状态
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        return registry.circuitBreaker("eastmoney-api", config);
    }

    /**
     * 获取熔断器状态信息
     */
    public String getCircuitBreakerState(CircuitBreaker circuitBreaker) {
        CircuitBreaker.State state = circuitBreaker.getState();
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        return String.format(
                "熔断器状态 - 名称: %s, 状态: %s, 失败率: %.1f%%, 慢调用率: %.1f%%, " +
                "总调用数: %d, 失败调用数: %d, 慢调用数: %d, 不可用时间: %ds",
                circuitBreaker.getName(),
                state.name(),
                metrics.getFailureRate(),
                metrics.getSlowCallRate(),
                metrics.getNumberOfBufferedCalls(),
                metrics.getNumberOfFailedCalls(),
                metrics.getNumberOfSlowCalls(),
                metrics.getNumberOfNotPermittedCalls()
        );
    }

    /**
     * 判断是否应该熔断
     */
    public boolean shouldCircuitBreak(CircuitBreaker circuitBreaker) {
        CircuitBreaker.State state = circuitBreaker.getState();
        return state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.FORCED_OPEN;
    }

    /**
     * 获取熔断器健康状态
     */
    public String getCircuitBreakerHealth(CircuitBreaker circuitBreaker) {
        CircuitBreaker.State state = circuitBreaker.getState();
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        if (state == CircuitBreaker.State.CLOSED) {
            return "健康 - 熔断器关闭，正常处理请求";
        } else if (state == CircuitBreaker.State.OPEN) {
            return String.format("不健康 - 熔断器打开，失败率: %.1f%%，慢调用率: %.1f%%",
                    metrics.getFailureRate(), metrics.getSlowCallRate());
        } else if (state == CircuitBreaker.State.HALF_OPEN) {
            return "恢复中 - 熔断器半开，测试恢复";
        } else if (state == CircuitBreaker.State.FORCED_OPEN) {
            return "强制打开 - 熔断器被强制打开";
        } else if (state == CircuitBreaker.State.DISABLED) {
            return "禁用 - 熔断器被禁用";
        } else {
            return "未知状态";
        }
    }
}
