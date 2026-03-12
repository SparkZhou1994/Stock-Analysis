# 股票基金看板实施计划
## 问题与需求
用户需要一个股票基金看板，用于查看当天股票和基金的实时涨跌情况。具体要求包括：
- 看板是一个大页面，包含多个小格子
- 每个小格子代表一个自选的股票或基金
- 小格子里展示该股票/基金的实时涨跌情况
- 点击小格子可以查看其均线图
- 代码需要写入stock-dashboard模块
## 现有项目状态
1. 项目结构：Stock-Analysis是多模块Maven项目
2. stock-dashboard模块：独立的Spring Boot 4.0.3应用（Java 21），目前只有基本框架
3. 现有实体：StockBO和FundBO在stock-analysis-api模块中（不能修改）
4. 现有服务：FundServiceImpl和StockServiceImpl在stock-analysis-springboot模块中（不能修改）
5. 数据流：Spring Boot → RabbitMQ → Flink → 分析结果（现有架构）

## 约束条件
1. 必须使用中文回复
2. 必须使用"superpowers"插件中的工作流来执行
3. 必须遵循"关键规则"（代码组织、风格、测试、安全）
4. 不修改现有的StockBO和FundBO类
5. 不修改现有的StockServiceImpl服务
6. 所有新代码都在stock-dashboard模块中创建

## 实施方法
### 架构设计

#### 采用前后端分离架构：
- 后端：Spring Boot 4.0.3 + WebSocket + Redis + MySQL
- 前端：Vue.js 3 + TypeScript + Vite + ECharts + Element Plus
- 通信：REST API + WebSocket实时推送
- 数据源：东方财富API（主）+ 现有模拟数据（备）

##### 后端实施（stock-dashboard模块）
1. 数据模型层
位置：stock-dashboard/src/main/java/com/spark/stockdashboard/entity/dashboard/
- StockDashboardBO.java - 看板股票实体（新创建）
- FundDashboardBO.java - 看板基金实体（新创建）
- RealtimePriceBO.java - 实时价格实体（新创建）
- HistoricalDataBO.java - 历史数据实体（新创建）

关键约束：不修改现有的StockBO和FundBO，创建新的看板专用实体。

2. 数据访问层
位置：stock-dashboard/src/main/java/com/spark/stockdashboard/repository/
- StockDashboardRepository.java - 股票数据访问
- FundDashboardRepository.java - 基金数据访问
- HistoricalDataRepository.java - 历史数据访问

3. 业务服务层
位置：stock-dashboard/src/main/java/com/spark/stockdashboard/service/
- DataSyncService.java - 数据同步服务（东方财富API集成）
- RealtimeDataService.java - 实时数据服务
- TechnicalIndicatorService.java - 技术指标服务（计算MA5/10/20/30）
- WatchlistService.java - 自选管理服务
- EastMoneyApiClient.java - 东方财富API客户端封装

4. 控制器层
位置：stock-dashboard/src/main/java/com/spark/stockdashboard/controller/
- DashboardController.java - 看板数据接口（REST API）
- WatchlistController.java - 自选管理接口
- ChartDataController.java - 图表数据接口
- WebSocketController.java - WebSocket控制器

5. WebSocket层
位置：stock-dashboard/src/main/java/com/spark/stockdashboard/websocket/
- StockWebSocketHandler.java - WebSocket处理器
- WebSocketSessionManager.java - 会话管理
- StockPriceBroadcaster.java - 价格广播器

6. 定时任务
位置：stock-dashboard/src/main/java/com/spark/stockdashboard/task/
- DataSyncTask.java - 数据同步任务（分钟级更新）
- IndicatorCalculationTask.java - 指标计算任务

7. 配置类
位置：stock-dashboard/src/main/java/com/spark/stockdashboard/config/
- WebSocketConfig.java - WebSocket配置
- RedisConfig.java - Redis配置
- DataSourceConfig.java - 数据源配置
- SchedulerConfig.java - 定时任务配置

###### 前端实施（stock-dashboard-frontend）
1. 项目初始化
位置：stock-dashboard-frontend/（新建目录）
- 使用Vite创建Vue.js 3 + TypeScript项目
- 集成ECharts 5.x和Element Plus
- 配置TypeScript和ESLint
2. 组件结构
位置：stock-dashboard-frontend/src/components/
dashboard/ - 看板相关组件
- StockCard.vue - 股票卡片组件
- FundCard.vue - 基金卡片组件
- DashboardGrid.vue - 看板网格布局
- AddStockModal.vue - 添加股票弹窗
charts/ - 图表组件
- KLineChart.vue - K线图组件
- MovingAverageChart.vue - 均线图组件
- VolumeChart.vue - 成交量图组件
common/ - 公共组件
- LoadingSpinner.vue - 加载动画
- ErrorMessage.vue - 错误提示
- ConnectionStatus.vue - 连接状态
layout/ - 布局组件
- Header.vue - 头部组件
- Footer.vue - 底部组件
3. 页面视图
位置：stock-dashboard-frontend/src/views/
- DashboardView.vue - 看板主页面
- DetailView.vue - 详情页面
- SettingsView.vue - 设置页面
4. 状态管理
位置：stock-dashboard-frontend/src/stores/（使用Pinia）
- dashboardStore.ts - 看板状态
- watchlistStore.ts - 自选列表状态（使用localStorage）
- realtimeStore.ts - 实时数据状态
- websocketStore.ts - WebSocket状态
5. 服务层
位置：stock-dashboard-frontend/src/services/
api/ - API服务
- stockApi.ts - 股票API
- fundApi.ts - 基金API
- chartApi.ts - 图表API
websocket/ - WebSocket服务
- StockWebSocket.ts - 股票WebSocket
- ConnectionManager.ts - 连接管理
storage/ - 存储服务
- LocalStorageService.ts - 本地存储
- IndexedDBService.ts - IndexedDB存储
6. 类型定义
位置：stock-dashboard-frontend/src/types/
- stock.ts - 股票类型定义
- fund.ts - 基金类型定义
- chart.ts - 图表类型定义
- websocket.ts - WebSocket类型定义

### 数据库设计
1. 表结构
- stock_dashboard - 看板股票数据表
- fund_dashboard - 看板基金数据表
- realtime_price - 实时价格表
- historical_data - 历史数据表
- technical_indicators - 技术指标表
2. 索引设计
- 为股票代码、基金代码、时间戳创建索引
- 为频繁查询的字段创建复合索引
 
### API设计
#### REST API接口
1. 看板数据接口
- GET /api/dashboard/stocks - 获取所有股票数据
- GET /api/dashboard/funds - 获取所有基金数据
- GET /api/dashboard/realtime/{code} - 获取单个实时数据
- GET /api/dashboard/summary - 获取看板摘要
2. 自选管理接口
- GET /api/watchlist - 获取自选列表
- POST /api/watchlist - 添加自选
- DELETE /api/watchlist/{code} - 删除自选
- PUT /api/watchlist/order - 调整自选顺序
- GET /api/watchlist/search - 搜索股票/基金
3. 图表数据接口
- GET /api/chart/historical/{code} - 获取历史数据
- GET /api/chart/indicators/{code} - 获取技术指标
- GET /api/chart/ma/{code} - 获取均线数据
- GET /api/chart/kline/{code} - 获取K线数据
 
#### WebSocket接口
- ws://localhost:8080/ws/stocks - 股票实时数据
- ws://localhost:8080/ws/funds - 基金实时数据 

### 关键实施要点
1. 实时数据更新
- 使用WebSocket实现分钟级数据推送
- 实现断线重连和心跳检测机制
- 数据压缩和批量更新优化
2. 前端性能优化
- 虚拟滚动支持大量自选项目
- 组件懒加载和代码分割
- 数据缓存和本地存储优化
3. 错误处理和监控
- 统一的错误处理机制
- 系统健康检查和监控
- 日志记录和告警系统
4. 安全性考虑
- API访问频率限制
- 输入验证和过滤
- 敏感数据加密存储

### 测试策略
1. 单元测试
- 后端服务层单元测试（JUnit 5）
- 前端组件单元测试（Vitest）
- 工具函数单元测试
2. 集成测试
- API接口集成测试
- WebSocket连接测试
- 数据库操作测试
3. 端到端测试
- 用户操作流程测试
- 跨浏览器兼容性测试
- 性能测试
4. 测试覆盖率目标
- 代码注释率 > 20%
- 单元测试覆盖率 > 70%
- 关键流程测试覆盖率 > 80%
### 部署方案
1. 开发环境
- 本地开发：前后端独立运行
- 热重载：前端Vite热重载，后端Spring Boot DevTools
2. 生产环境
- 容器化部署：Docker + Docker Compose
- 反向代理：Nginx配置
- 监控：Spring Boot Actuator + 前端性能监控
3. 构建流程
- 后端：Maven构建 → Docker镜像
- 前端：Vite构建 → Nginx静态文件

## 实施阶段
第一阶段：基础架构搭建（1周）
1. 创建后端实体类和服务层
2. 搭建前端项目结构
3. 实现基础API接口
4. 配置数据库和Redis
第二阶段：核心功能实现（1周）
1. 实现实时数据同步（东方财富API集成）
2. 完成看板展示功能
3. 实现自选管理（本地存储）
4. 基础图表展示
第三阶段：高级功能完善（1周）
1. 实现均线图表和技术指标计算
2. 添加WebSocket实时推送
3. 优化性能和用户体验
4. 实现响应式设计
第四阶段：测试和部署（1周）
1. 单元测试和集成测试
2. 性能测试和优化
3. 部署和上线
4. 文档编写

## 关键文件路径
### 后端关键文件
1. stock-dashboard/src/main/java/com/spark/stockdashboard/entity/dashboard/StockDashboardBO.java - 核心实体
2. stock-dashboard/src/main/java/com/spark/stockdashboard/service/DataSyncService.java - 数据同步服务
3. stock-dashboard/src/main/java/com/spark/stockdashboard/controller/DashboardController.java - API控制器
4. stock-dashboard/src/main/java/com/spark/stockdashboard/websocket/StockWebSocketHandler.java - WebSocket处理器
### 前端关键文件
1. stock-dashboard-frontend/src/components/dashboard/DashboardGrid.vue - 看板网格组件
2. stock-dashboard-frontend/src/stores/watchlistStore.ts - 自选状态管理
3. stock-dashboard-frontend/src/services/websocket/StockWebSocket.ts - WebSocket服务
4. stock-dashboard-frontend/src/views/DashboardView.vue - 主页面

## 验证方法
1. 功能验证
- 启动后端服务：mvn spring-boot:run
- 启动前端服务：npm run dev
- 访问 http://localhost:3000 查看看板
- 测试实时数据更新
- 测试自选管理功能
- 测试图表展示功能
2. 测试验证
- 运行后端测试：mvn test
- 运行前端测试：npm run test
- 检查测试覆盖率报告
- 执行端到端测试
3. 性能验证
- 检查页面加载时间
- 验证实时数据更新延迟
- 测试并发用户支持
- 监控内存和CPU使用率
## 风险与缓解
1. 技术风险
- API稳定性：东方财富API可能不稳定
- 缓解：实现重试机制，添加备用数据源
- 性能问题：大量小格子渲染性能
- 缓解：虚拟滚动，分页加载，性能优化
2. 项目风险
- 进度延期：功能较多，开发周期可能延长
- 缓解：分阶段开发，优先核心功能  
- 需求变更：用户需求可能变更
- 缓解：敏捷开发，快速响应
3. 质量风险
- 代码质量：代码质量不达标
- 缓解：代码审查，测试驱动开发 
- 测试覆盖：测试覆盖率不足
- 缓解：设定明确的测试覆盖率目标
### 成功标准
1. 功能完成：实现所有核心功能需求
2. 性能达标：页面加载时间 < 3秒，数据更新延迟 < 60秒
3. 测试覆盖：单元测试覆盖率 > 70%
4. 用户体验：界面美观，操作流畅
5. 代码质量：遵循编码规范，代码注释充分
这个计划遵循了CLAUDE.md中的所有约束条件，采用前后端分离架构，不修改现有模块，所有新功能都在stock-dashboard模块中实现，同时保持了高内聚、低耦合的设计原则。 