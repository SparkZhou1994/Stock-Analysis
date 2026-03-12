package com.spark.stockdashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 异步和调度配置
 * 启用Spring的异步执行和定时任务调度
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * 配置说明：
     * 1. @EnableAsync - 启用Spring的异步执行支持
     * 2. @EnableScheduling - 启用Spring的定时任务调度支持
     *
     * 相关配置在application.properties中：
     * - spring.task.execution.* - 异步任务执行配置
     * - spring.task.scheduling.* - 调度任务配置
     */
}