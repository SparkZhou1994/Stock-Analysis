/**
 * 股票数据类型定义
 */
export interface Stock {
  id?: number;
  stockCode: string;
  stockName: string;
  currentPrice: number;
  changeAmount: number;
  changePercent: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  previousClose: number;
  volume: number;
  turnover: number;
  turnoverRate: number;
  peRatio: number;
  pbRatio: number;
  marketCap: number;
  circulatingMarketCap: number;
  industry?: string;
  updateTime?: string;
  dataSource?: string;
  isWatchlist?: boolean;
  watchlistOrder?: number;
}

/**
 * 基金数据类型定义
 */
export interface Fund {
  id?: number;
  fundCode: string;
  fundName: string;
  fundType?: string;
  currentNetValue: number;
  dailyChangeAmount: number;
  dailyChangePercent: number;
  weeklyChangePercent?: number;
  monthlyChangePercent?: number;
  yearlyChangePercent?: number;
  accumulatedNetValue: number;
  establishmentDate?: string;
  fundSize?: number;
  fundManager?: string;
  fundCompany?: string;
  riskLevel?: string;
  purchaseStatus?: string;
  redemptionStatus?: string;
  updateTime?: string;
  dataSource?: string;
  isWatchlist?: boolean;
  watchlistOrder?: number;
}

/**
 * 历史K线数据类型
 */
export interface KLineData {
  id?: number;
  code: string;
  type: 'STOCK' | 'FUND';
  tradeDate: string;
  openPrice: number;
  closePrice: number;
  highPrice: number;
  lowPrice: number;
  volume: number;
  turnover?: number;
  changePercent: number;
  changeAmount: number;
  turnoverRate?: number;
  adjustmentType?: string;
  ma5?: number;
  ma10?: number;
  ma20?: number;
  ma30?: number;
}

/**
 * 实时价格推送数据类型
 */
export interface RealtimePriceData {
  code: string;
  type: 'STOCK' | 'FUND';
  name: string;
  price: number;
  changeAmount: number;
  changePercent: number;
  timestamp: string;
}

/**
 * WebSocket消息类型
 */
export interface WebSocketMessage<T = any> {
  type: 'CONNECTED' | 'HEARTBEAT' | 'SUBSCRIBE' | 'UNSUBSCRIBE' | 'SUBSCRIBE_SUCCESS' | 'UNSUBSCRIBE_SUCCESS' | 'REALTIME_PRICE' | 'ERROR' | 'DISCONNECTED' | 'RECONNECT_FAILED';
  data: T;
  timestamp: number;
}

/**
 * 订阅请求类型
 */
export interface SubscribeRequest {
  securityType: 'STOCK' | 'FUND';
  code: string;
}
