# 股票基金看板 - 前端

基于 Vue 3 + TypeScript + Element Plus 开发的股票基金实时数据看板。

## 功能特性

- 📊 实时数据展示：股票和基金的实时价格、涨跌幅等信息
- 🔍 搜索功能：支持股票和基金的代码、名称搜索
- ⭐ 自选管理：添加/删除自选股票和基金，实时推送更新
- 📈 K线图表：历史行情K线图，支持多时间周期切换
- 🌐 WebSocket 实时推送：数据实时更新，无需手动刷新
- 📱 响应式设计：完美适配桌面端和移动端
- ⚙️ 系统设置：个性化配置刷新频率、颜色方案等

## 技术栈

- **框架**: Vue 3.5 + TypeScript
- **UI 组件库**: Element Plus 2.13
- **状态管理**: Pinia 3.0
- **路由**: Vue Router 4.5
- **图表**: ECharts 6.0
- **HTTP 客户端**: Axios 1.15
- **WebSocket**: SockJS + STOMP
- **构建工具**: Vite 8.0

## 项目结构

```
src/
├── components/          # 组件库
│   ├── charts/          # 图表组件
│   │   └── KLineChart.vue    # K线图组件
│   ├── dashboard/       # 看板组件
│   │   ├── StockCard.vue     # 股票卡片
│   │   └── FundCard.vue      # 基金卡片
│   └── layout/          # 布局组件
│       └── Header.vue        # 头部导航
├── services/            # 服务层
│   ├── api.ts           # API 接口封装
│   └── websocket.ts     # WebSocket 服务
├── stores/              # 状态管理
│   ├── watchlist.ts     # 自选列表状态
│   └── realtime.ts      # 实时数据状态
├── types/               # TypeScript 类型定义
│   └── stock.ts         # 股票、基金、K线等数据类型
├── utils/               # 工具函数
│   └── format.ts        # 格式化工具
├── views/               # 页面组件
│   ├── DashboardView.vue    # 首页看板
│   ├── DetailView.vue       # 详情页面
│   ├── WatchlistView.vue    # 自选列表
│   ├── SearchView.vue       # 搜索页面
│   └── SettingsView.vue     # 设置页面
├── router/              # 路由配置
│   └── index.ts
├── App.vue              # 根组件
├── main.ts              # 入口文件
└── style.css            # 全局样式
```

## 快速开始

### 环境要求

- Node.js >= 18.0.0
- pnpm 或 npm 或 yarn

### 安装依赖

```bash
npm install
# 或
pnpm install
# 或
yarn install
```

### 开发模式

```bash
npm run dev
# 或
pnpm dev
# 或
yarn dev
```

应用将在 http://localhost:3000 启动

### 构建生产版本

```bash
npm run build
# 或
pnpm build
# 或
yarn build
```

### 预览生产构建

```bash
npm run preview
# 或
pnpm preview
# 或
yarn preview
```

## 配置说明

### 代理配置

在 `vite.config.ts` 中配置后端 API 代理：

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // 后端 API 地址
      changeOrigin: true,
      ws: true,
    },
    '/ws': {
      target: 'ws://localhost:8080', // WebSocket 地址
      ws: true,
      changeOrigin: true,
    },
  },
}
```

### 环境变量

可创建 `.env`、`.env.development`、`.env.production` 等环境变量文件：

```env
VITE_API_BASE_URL = '/api'
VITE_WS_BASE_URL = '/ws'
```

## 主要功能说明

### 实时数据推送

通过 WebSocket 连接后端服务，订阅自选股票和基金的实时数据更新：

- 连接建立后自动订阅所有自选标的
- 数据更新时自动更新页面展示
- 断开连接后自动尝试重连

### 自选管理

- 支持添加/删除股票和基金到自选列表
- 自选数据本地持久化存储
- 实时推送自选标的的价格变动

### K线图表

- 支持 1月、3月、6月、1年、全部等时间周期
- 展示开盘价、收盘价、最高价、最低价
- 包含 MA 均线指标

### 响应式设计

- 桌面端：最多 4 列卡片布局
- 平板端：2 列卡片布局
- 移动端：1 列卡片布局

## 开发规范

- 使用 TypeScript 保证类型安全
- 遵循 Vue 3 Composition API 规范
- 组件化开发，提高代码复用性
- 合理使用 Pinia 进行状态管理
- 接口请求统一封装在 services 层
- 工具函数统一放在 utils 目录

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## License

MIT

