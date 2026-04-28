package com.spark.stockdashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定时任务配置类
 * 配置定时任务线程池，支持并发执行定时任务
 * 避免单线程执行导致的阻塞问题
 */
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    /**
     * 定时任务线程池配置
     * 配置核心线程数、最大线程数、队列容量等
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 配置线程池参数
        scheduler.setPoolSize(10);  // 核心线程数
        scheduler.setThreadNamePrefix("scheduled-task-");  // 线程名前缀
        scheduler.setAwaitTerminationSeconds(60);  // 等待任务完成时间
        scheduler.setWaitForTasksToCompleteOnShutdown(true);  // 关闭时等待任务完成
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // 拒绝策略

        // 配置线程池队列
        scheduler.setRemoveOnCancelPolicy(true);  // 取消时移除任务

        return scheduler;
    }

    /**
     * 配置定时任务执行器
     * 使用自定义的线程池执行定时任务
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    /**
     * 定时任务执行器Bean
     * 用于在其他组件中注入使用
     */
    @Bean
    public Executor scheduledTaskExecutor() {
        return taskScheduler();
    }

    /**
     * 定时任务线程池监控配置
     * 提供线程池状态监控信息
     */
    @Bean
    public SchedulerMonitor schedulerMonitor() {
        return new SchedulerMonitor(taskScheduler());
    }

    /**
     * 定时任务线程池监控类
     * 提供线程池状态信息
     */
    public static class SchedulerMonitor {
        private final ThreadPoolTaskScheduler scheduler;

        public SchedulerMonitor(ThreadPoolTaskScheduler scheduler) {
            this.scheduler = scheduler;
        }

        /**
         * 获取线程池活动线程数
         */
        public int getActiveThreadCount() {
            return scheduler.getActiveCount();
        }

        /**
         * 获取线程池队列大小
         */
        public int getQueueSize() {
            return scheduler.getScheduledThreadPoolExecutor().getQueue().size();
        }

        /**
         * 获取线程池已完成任务数
         */
        public long getCompletedTaskCount() {
            return scheduler.getScheduledThreadPoolExecutor().getCompletedTaskCount();
        }

        /**
         * 获取线程池状态信息
         */
        public String getStatus() {
            return String.format(
                "Scheduler Status - Active: %d, Pool: %d, Queue: %d, Completed: %d",
                getActiveThreadCount(),
                scheduler.getPoolSize(),
                getQueueSize(),
                getCompletedTaskCount()
            );
        }
    }

    /**
     * 定时任务异常处理器
     * 处理定时任务执行过程中的异常
     */
    @Bean
    public SchedulerExceptionHandler schedulerExceptionHandler() {
        return new SchedulerExceptionHandler();
    }

    /**
     * 定时任务异常处理类
     * 记录定时任务执行异常
     */
    public static class SchedulerExceptionHandler {

        /**
         * 处理定时任务异常
         */
        public void handleException(Runnable task, Throwable exception) {
            // 记录异常日志，这里可以集成到日志系统中
            System.err.println("Scheduled task failed: " + task.getClass().getName());
            System.err.println("Exception: " + exception.getMessage());
            exception.printStackTrace();

            // 这里可以添加告警逻辑，如发送邮件、短信等
            // sendAlert(task, exception);
        }

        /**
         * 发送告警（示例方法）
         */
        private void sendAlert(Runnable task, Throwable exception) {
            // 实现告警逻辑
            // 例如：发送邮件、短信、钉钉消息等
        }
    }
}