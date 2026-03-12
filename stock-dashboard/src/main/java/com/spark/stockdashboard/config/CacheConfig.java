package com.spark.stockdashboard.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 配置Caffeine缓存管理器
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${eastmoney.api.cache.realtime-ttl:30}")
    private int realtimeTtl;

    @Value("${eastmoney.api.cache.historical-ttl:3600}")
    private int historicalTtl;

    @Value("${eastmoney.api.cache.search-ttl:300}")
    private int searchTtl;

    @Value("${eastmoney.api.cache.maximum-size:1000}")
    private int maximumSize;

    @Value("${eastmoney.api.cache.expire-after-write:3600}")
    private int expireAfterWrite;

    @Value("${eastmoney.api.cache.consistency.random-ttl-offset:0.1}")
    private double randomTtlOffset;

    /**
     * 实时数据缓存配置
     */
    @Bean
    public Caffeine<Object, Object> realtimeCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(getRandomTtl(realtimeTtl), TimeUnit.SECONDS)
                .recordStats()
                .removalListener(new CacheRemovalListener("realtime"));
    }

    /**
     * 历史数据缓存配置
     */
    @Bean
    public Caffeine<Object, Object> historicalCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(getRandomTtl(historicalTtl), TimeUnit.SECONDS)
                .recordStats()
                .removalListener(new CacheRemovalListener("historical"));
    }

    /**
     * 搜索数据缓存配置
     */
    @Bean
    public Caffeine<Object, Object> searchCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(getRandomTtl(searchTtl), TimeUnit.SECONDS)
                .recordStats()
                .removalListener(new CacheRemovalListener("search"));
    }

    /**
     * 主缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(realtimeCacheBuilder());
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "stockRealtime", "fundRealtime",
                "stockHistorical", "fundHistorical",
                "stockSearch", "fundSearch"
        ));
        return cacheManager;
    }

    /**
     * 实时数据缓存管理器
     */
    @Bean
    public CacheManager realtimeCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(realtimeCacheBuilder());
        cacheManager.setCacheNames(java.util.Arrays.asList("stockRealtime", "fundRealtime"));
        return cacheManager;
    }

    /**
     * 历史数据缓存管理器
     */
    @Bean
    public CacheManager historicalCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(historicalCacheBuilder());
        cacheManager.setCacheNames(java.util.Arrays.asList("stockHistorical", "fundHistorical"));
        return cacheManager;
    }

    /**
     * 搜索数据缓存管理器
     */
    @Bean
    public CacheManager searchCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(searchCacheBuilder());
        cacheManager.setCacheNames(java.util.Arrays.asList("stockSearch", "fundSearch"));
        return cacheManager;
    }

    /**
     * 获取随机TTL，防止缓存雪崩
     */
    private int getRandomTtl(int baseTtl) {
        if (randomTtlOffset <= 0) {
            return baseTtl;
        }
        int offset = (int) (baseTtl * randomTtlOffset);
        return baseTtl + (int) (Math.random() * offset * 2) - offset;
    }

    /**
     * 缓存移除监听器
     */
    private static class CacheRemovalListener implements RemovalListener<Object, Object> {
        private final String cacheType;

        public CacheRemovalListener(String cacheType) {
            this.cacheType = cacheType;
        }

        @Override
        public void onRemoval(@Nullable Object key, @Nullable Object value, RemovalCause cause) {
            // 记录缓存移除事件
            String causeStr = cause.name();
            String keyStr = key != null ? key.toString() : "null";

            // 这里可以记录到日志系统
            System.out.printf("缓存移除 - 类型: %s, 键: %s, 原因: %s%n", cacheType, keyStr, causeStr);
        }
    }

    /**
     * 获取缓存配置信息
     */
    public String getCacheConfigInfo() {
        return String.format(
                "缓存配置 - 实时数据TTL: %ds, 历史数据TTL: %ds, 搜索数据TTL: %ds, 最大大小: %d, 随机偏移: %.1f%%",
                realtimeTtl, historicalTtl, searchTtl, maximumSize, randomTtlOffset * 100
        );
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats(CacheManager cacheManager, String cacheName) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
            com.github.benmanes.caffeine.cache.stats.CacheStats stats = nativeCache.stats();

            return String.format(
                    "缓存统计 - %s: 命中率: %.2f%%, 加载次数: %d, 加载成功: %d, 加载失败: %d, 总加载时间: %dms",
                    cacheName,
                    stats.hitRate() * 100,
                    stats.loadCount(),
                    stats.loadSuccessCount(),
                    stats.loadFailureCount(),
                    stats.totalLoadTime()
            );
        }
        return String.format("缓存统计 - %s: 无统计信息", cacheName);
    }
}