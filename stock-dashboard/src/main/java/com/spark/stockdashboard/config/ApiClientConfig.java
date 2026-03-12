package com.spark.stockdashboard.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * API客户端配置类
 * 配置HTTP连接池和RestTemplate
 */
@Configuration
@EnableRetry
public class ApiClientConfig {

    @Value("${eastmoney.api.http.max-total-connections:100}")
    private int maxTotalConnections;

    @Value("${eastmoney.api.http.max-per-route:20}")
    private int maxPerRoute;

    @Value("${eastmoney.api.http.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${eastmoney.api.http.socket-timeout:10000}")
    private int socketTimeout;

    @Value("${eastmoney.api.http.connection-request-timeout:2000}")
    private int connectionRequestTimeout;

    @Value("${eastmoney.api.http.validate-after-inactivity:30000}")
    private int validateAfterInactivity;

    /**
     * 配置HTTP连接池
     */
    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.of(socketTimeout, TimeUnit.MILLISECONDS))
                .build();

        return PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(maxTotalConnections)
                .setMaxConnPerRoute(maxPerRoute)
                .setDefaultSocketConfig(socketConfig)
                .setValidateAfterInactivity(Timeout.of(validateAfterInactivity, TimeUnit.MILLISECONDS))
                .build();
    }

    /**
     * 配置HTTP客户端
     */
    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager) {
        return HttpClients.custom()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.of(30, TimeUnit.SECONDS))
                .build();
    }

    /**
     * 配置RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(Timeout.of(connectTimeout, TimeUnit.MILLISECONDS));
        requestFactory.setConnectionRequestTimeout(Timeout.of(connectionRequestTimeout, TimeUnit.MILLISECONDS));

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // 添加默认的请求拦截器
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // 添加User-Agent头部，模拟浏览器访问
            request.getHeaders().add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            request.getHeaders().add("Accept", "application/json");
            request.getHeaders().add("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            request.getHeaders().add("Connection", "keep-alive");
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    /**
     * 连接池监控信息
     */
    public String getConnectionPoolStats(PoolingHttpClientConnectionManager connectionManager) {
        return String.format(
                "连接池状态 - 总连接数: %d, 活跃连接数: %d, 空闲连接数: %d, 等待队列: %d",
                connectionManager.getTotalStats().getMax(),
                connectionManager.getTotalStats().getLeased(),
                connectionManager.getTotalStats().getAvailable(),
                connectionManager.getTotalStats().getPending()
        );
    }
}