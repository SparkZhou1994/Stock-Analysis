package com.spark.stockdashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 重试机制配置类
 * 配置API调用的重试策略
 */
@Configuration
public class RetryConfig {

    @Value("${eastmoney.api.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${eastmoney.api.retry.backoff-delay:1000}")
    private long initialInterval;

    @Value("${eastmoney.api.retry.backoff-multiplier:2.0}")
    private double multiplier;

    @Value("${eastmoney.api.retry.max-backoff-delay:10000}")
    private long maxInterval;

    /**
     * 配置重试模板
     */
    @Bean
    public RetryTemplate apiRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 配置重试策略
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(org.springframework.web.client.ResourceAccessException.class, true);
        retryableExceptions.put(java.net.SocketTimeoutException.class, true);
        retryableExceptions.put(java.net.ConnectException.class, true);
        retryableExceptions.put(org.springframework.web.client.HttpServerErrorException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 配置退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 设置重试监听器
        retryTemplate.registerListener(new ApiRetryListener());

        return retryTemplate;
    }

    /**
     * 重试监听器
     */
    private static class ApiRetryListener implements org.springframework.retry.RetryListener {
        @Override
        public <T, E extends Throwable> boolean open(org.springframework.retry.RetryContext context,
                                                     org.springframework.retry.RetryCallback<T, E> callback) {
            // 重试开始
            context.setAttribute("startTime", System.currentTimeMillis());
            return true;
        }

        @Override
        public <T, E extends Throwable> void close(org.springframework.retry.RetryContext context,
                                                   org.springframework.retry.RetryCallback<T, E> callback,
                                                   Throwable throwable) {
            // 重试结束
            Long startTime = (Long) context.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                context.setAttribute("duration", duration);
            }
        }

        @Override
        public <T, E extends Throwable> void onError(org.springframework.retry.RetryContext context,
                                                     org.springframework.retry.RetryCallback<T, E> callback,
                                                     Throwable throwable) {
            // 重试错误
            int retryCount = context.getRetryCount();
            String exceptionName = throwable.getClass().getSimpleName();
            String exceptionMessage = throwable.getMessage();

            // 记录重试日志
            System.out.printf("API调用重试 - 第%d次重试，异常: %s，消息: %s%n",
                    retryCount, exceptionName, exceptionMessage);
        }
    }

    /**
     * 获取重试配置信息
     */
    public String getRetryConfigInfo() {
        return String.format(
                "重试配置 - 最大重试次数: %d, 初始间隔: %dms, 退避乘数: %.1f, 最大间隔: %dms",
                maxAttempts, initialInterval, multiplier, maxInterval
        );
    }

    /**
     * 判断异常是否可重试
     */
    public boolean isRetryableException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        // 网络相关异常可重试
        if (throwable instanceof org.springframework.web.client.ResourceAccessException ||
            throwable instanceof java.net.SocketTimeoutException ||
            throwable instanceof java.net.ConnectException) {
            return true;
        }

        // HTTP 5xx错误可重试
        if (throwable instanceof org.springframework.web.client.HttpServerErrorException) {
            return true;
        }

        // HTTP 429（限流）错误可重试
        if (throwable instanceof org.springframework.web.client.HttpClientErrorException) {
            org.springframework.web.client.HttpClientErrorException httpException =
                    (org.springframework.web.client.HttpClientErrorException) throwable;
            if (httpException.getStatusCode().value() == 429) {
                return true;
            }
        }

        return false;
    }
}