# 新技能学习记录

## Spring Boot WebSocket 实时数据推送

### 学习内容
1. **Spring WebSocket架构**
   - STOMP协议在Spring中的实现
   - 消息代理（Message Broker）配置
   - WebSocket端点注册和跨域配置

2. **WebSocket会话管理**
   - 会话生命周期管理（连接、消息、关闭）
   - 会话状态跟踪和清理
   - 心跳检测机制实现

3. **实时数据推送模式**
   - 发布-订阅模式实现
   - 按代码订阅特定数据
   - 定时推送和事件驱动推送

4. **前端WebSocket集成**
   - SockJS客户端库使用
   - 原生WebSocket API
   - 连接状态管理和错误处理

### 关键技术点
1. **@EnableWebSocketMessageBroker**
   - 启用WebSocket消息代理
   - 配置消息目的地前缀
   - 设置应用程序目的地前缀

2. **WebSocketSession管理**
   - 使用ConcurrentHashMap存储会话
   - 实现线程安全的订阅管理
   - 支持按代码快速查找订阅会话

3. **心跳检测实现**
   - ScheduledExecutorService定时任务
   - 无效会话自动清理
   - 连接状态监控

4. **消息序列化**
   - Jackson ObjectMapper配置
   - 自定义消息DTO设计
   - JSON消息格式标准化

### 最佳实践
1. **代码组织**
   - 配置类、处理器、管理器分离
   - DTO对象用于消息传输
   - 服务层负责业务逻辑

2. **错误处理**
   - WebSocket传输错误处理
   - 消息解析异常处理
   - 连接中断恢复机制

3. **性能优化**
   - 使用CopyOnWriteArraySet避免并发问题
   - 消息缓冲区大小配置
   - 异步消息发送

4. **测试策略**
   - 单元测试核心管理器
   - 集成测试完整流程
   - 前端测试客户端

### 应用场景
1. **金融实时数据**
   - 股票价格实时推送
   - 基金净值更新
   - 市场行情广播

2. **即时通讯**
   - 聊天消息推送
   - 在线状态更新
   - 通知提醒

3. **物联网数据**
   - 设备状态监控
   - 实时数据采集
   - 控制指令下发

### 注意事项
1. **连接管理**
   - 控制最大连接数
   - 实现连接超时处理
   - 支持断线重连

2. **消息可靠性**
   - 重要消息确认机制
   - 消息重发策略
   - 顺序保证

3. **安全性**
   - WebSocket连接认证
   - 消息内容验证
   - 防止DoS攻击

### 扩展学习
1. **高级特性**
   - WebSocket over SSL (WSS)
   - 集群会话共享
   - 消息持久化

2. **相关技术**
   - Server-Sent Events (SSE)
   - WebRTC数据通道
   - MQTT协议

3. **监控运维**
   - 连接数监控
   - 消息吞吐量统计
   - 性能瓶颈分析

---

## Spring Boot数据库配置和JPA实体设计

### 学习日期
2026-03-09

### 学习内容

#### 1. Spring Boot数据库配置
- **数据源配置**：使用HikariCP作为连接池，配置连接超时、最大连接数、最小空闲连接等参数
- **JPA配置**：配置实体管理器工厂、事务管理器、Hibernate属性
- **多数据源支持**：通过@Primary注解指定主数据源

#### 2. Redis配置和缓存管理
- **Redis连接配置**：配置主机、端口、密码、数据库、连接池参数
- **序列化配置**：使用Jackson进行JSON序列化，支持Java 8时间类型
- **缓存管理**：配置不同缓存的过期时间，如实时数据5秒，历史数据1小时
- **缓存命名空间**：使用前缀避免缓存键冲突

#### 3. JPA实体设计最佳实践
- **实体注解**：@Entity, @Table, @Id, @GeneratedValue, @Column等
- **索引设计**：为查询频繁的字段创建索引，提高查询性能
- **约束设计**：唯一约束、非空约束、长度限制等
- **数据类型映射**：BigDecimal对应DECIMAL，LocalDateTime对应DATETIME
- **回调方法**：@PrePersist, @PreUpdate用于自动设置时间字段

#### 4. 数据库脚本设计
- **表结构设计**：合理的字段类型、长度、注释
- **索引策略**：根据查询模式设计复合索引
- **约束设计**：主键、外键、唯一约束、检查约束
- **存储过程和函数**：封装复杂业务逻辑
- **事件调度**：定时清理过期数据
- **视图设计**：简化复杂查询

#### 5. 配置管理
- **分层配置**：将不同环境的配置分离
- **连接池调优**：根据应用负载调整连接池参数
- **缓存策略**：根据数据更新频率设置不同的缓存时间
- **监控配置**：集成Spring Boot Actuator进行应用监控

### 关键技术点

#### 1. HikariCP配置优化
```properties
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
```

#### 2. JPA性能优化
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.fetch_size=100
```

#### 3. Redis序列化配置
```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerModule(new JavaTimeModule());
objectMapper.activateDefaultTyping(
    LaissezFaireSubTypeValidator.instance,
    ObjectMapper.DefaultTyping.NON_FINAL,
    JsonTypeInfo.As.PROPERTY
);
```

#### 4. 实体设计模式
```java
@Entity
@Table(name = "stock_dashboard", indexes = {
    @Index(name = "idx_stock_code", columnList = "stock_code"),
    @Index(name = "idx_update_time", columnList = "update_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDashboardBO {
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
```

### 最佳实践总结

1. **连接池配置**：根据应用并发量合理设置连接池大小
2. **索引设计**：为WHERE、JOIN、ORDER BY字段创建索引
3. **缓存策略**：高频读取、低频更新的数据适合缓存
4. **事务管理**：合理设置事务边界，避免长事务
5. **监控告警**：配置数据库连接、慢查询、死锁监控
6. **备份恢复**：定期备份，测试恢复流程
7. **版本控制**：数据库脚本纳入版本控制

### 后续学习建议

1. **性能调优**：学习数据库性能分析和调优技巧
2. **分库分表**：大数据量下的分库分表方案
3. **读写分离**：主从复制和读写分离配置
4. **数据迁移**：在线数据迁移和版本升级
5. **监控告警**：Prometheus + Grafana监控体系
6. **安全加固**：数据库访问控制和加密