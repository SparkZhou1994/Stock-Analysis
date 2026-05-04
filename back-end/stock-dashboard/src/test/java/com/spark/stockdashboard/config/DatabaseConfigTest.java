package com.spark.stockdashboard.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据库配置测试类
 * 测试数据源、JPA配置等是否正确加载
 */
@SpringBootTest
class DatabaseConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testDataSourceBeanExists() {
        // 测试数据源Bean是否存在
        assertThat(applicationContext.containsBean("dataSource")).isTrue();

        DataSource dataSource = applicationContext.getBean(DataSource.class);
        assertThat(dataSource).isNotNull();
    }

    @Test
    void testEntityManagerFactoryBeanExists() {
        // 测试实体管理器工厂Bean是否存在
        assertThat(applicationContext.containsBean("entityManagerFactory")).isTrue();
    }

    @Test
    void testTransactionManagerBeanExists() {
        // 测试事务管理器Bean是否存在
        assertThat(applicationContext.containsBean("transactionManager")).isTrue();
    }

    @Test
    void testRedisTemplateBeanExists() {
        // 测试Redis模板Bean是否存在
        assertThat(applicationContext.containsBean("redisTemplate")).isTrue();
    }

    @Test
    void testCacheManagerBeanExists() {
        // 测试缓存管理器Bean是否存在
        assertThat(applicationContext.containsBean("cacheManager")).isTrue();
    }

    @Test
    void testTaskSchedulerBeanExists() {
        // 测试定时任务调度器Bean是否存在
        assertThat(applicationContext.containsBean("taskScheduler")).isTrue();
    }
}