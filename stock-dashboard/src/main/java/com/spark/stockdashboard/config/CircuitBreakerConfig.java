package com.spark.stockdashboard.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 熔断器配置类
 * 配置Resilience4j熔断器
 */
@Configuration
public class CircuitBreakerConfig {

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
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
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
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
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
     * 熔断器事件监听器
     */
    @Bean
    public CircuitBreaker.EventListener circuitBreakerEventListener() {
        return new CircuitBreaker.EventListener() {
            @Override
            public void onSuccess(CircuitBreaker.OnSuccessEvent event) {
                // 成功事件
                System.out.printf("熔断器成功事件 - 名称: %s, 持续时间: %dms%n",
                        event.getCircuitBreakerName(), event.getElapsedDuration().toMillis());
            }

            @Override
            public void onError(CircuitBreaker.OnErrorEvent event) {
                // 错误事件
                System.out.printf("熔断器错误事件 - 名称: %s, 异常: %s, 持续时间: %dms%n",
                        event.getCircuitBreakerName(),
                        event.getThrowable().getClass().getSimpleName(),
                        event.getElapsedDuration().toMillis());
            }

            @Override
            public void onStateTransition(CircuitBreaker.OnStateTransitionEvent event) {
                // 状态转换事件
                System.out.printf("熔断器状态转换 - 名称: %s, 从 %s 转换到 %s%n",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState());
            }

            @Override
            public void onReset(CircuitBreaker.OnResetEvent event) {
                // 重置事件
                System.out.printf("熔断器重置 - 名称: %s%n", event.getCircuitBreakerName());
            }

            @Override
            public void onIgnoredError(CircuitBreaker.OnIgnoredErrorEvent event) {
                // 忽略错误事件
                System.out.printf("熔断器忽略错误 - 名称: %s, 异常: %s%n",
                        event.getCircuitBreakerName(),
                        event.getThrowable().getClass().getSimpleName());
            }

            @Override
            public void onCallNotPermitted(CircuitBreaker.OnCallNotPermittedEvent event) {
                // 调用不允许事件
                System.out.printf("熔断器调用不允许 - 名称: %s%n", event.getCircuitBreakerName());
            }

            @Override
            public void onSlowCallRateExceeded(CircuitBreaker.OnSlowCallRateExceededEvent event) {
                // 慢调用率超过阈值事件
                System.out.printf("熔断器慢调用率超过阈值 - 名称: %s, 慢调用率: %.1f%%%n",
                        event.getCircuitBreakerName(), event.getSlowCallRate());
            }

            @Override
            public void onFailureRateExceeded(CircuitBreaker.OnFailureRateExceededEvent event) {
                // 失败率超过阈值事件
                System.out.printf("熔断器失败率超过阈值 - 名称: %s, 失败率: %.1f%%%n",
                        event.getCircuitBreakerName(), event.getFailureRate());
            }
        };
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