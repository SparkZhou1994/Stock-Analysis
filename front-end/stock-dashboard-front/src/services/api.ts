import axios from 'axios';
import type { Stock, Fund, KLineData } from '../types/stock';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    console.error('API请求错误:', error);
    return Promise.reject(error);
  }
);

/**
 * 股票相关API
 */
export const stockApi = {
  // 获取所有股票列表
  getAllStocks: () => api.get<Stock[]>('/dashboard/stocks'),

  // 获取单个股票实时数据
  getStockRealtime: (code: string) => api.get<Stock>(`/dashboard/stocks/${code}`),

  // 获取股票历史数据
  getStockHistory: (code: string, startDate?: string, endDate?: string) =>
    api.get<KLineData[]>(`/chart/historical/${code}`, {
      params: { startDate, endDate }
    }),

  // 获取股票均线数据
  getStockMA: (code: string, days: number) => api.get<number[]>(`/chart/ma/${code}`, { params: { days } }),

  // 搜索股票
  searchStocks: (keyword: string) => api.get<Stock[]>('/watchlist/stocks/search', { params: { keyword } }),
};

/**
 * 基金相关API
 */
export const fundApi = {
  // 获取所有基金列表
  getAllFunds: () => api.get<Fund[]>('/dashboard/funds'),

  // 获取单个基金实时数据
  getFundRealtime: (code: string) => api.get<Fund>(`/dashboard/funds/${code}`),

  // 获取基金历史数据
  getFundHistory: (code: string, startDate?: string, endDate?: string) =>
    api.get<KLineData[]>(`/chart/historical/${code}`, {
      params: { startDate, endDate }
    }),

  // 搜索基金
  searchFunds: (keyword: string) => api.get<Fund[]>('/watchlist/funds/search', { params: { keyword } }),
};

/**
 * 自选列表相关API
 */
export const watchlistApi = {
  // 获取自选列表
  getWatchlist: async () => {
    // 同时请求股票和基金自选列表
    const [stockResponse, fundResponse] = await Promise.all([
      api.get('/watchlist/stocks'),
      api.get('/watchlist/funds')
    ]);

    // 合并结果
    const stockItems = (stockResponse.data as any[]).map(item => ({ ...item, type: 'STOCK' }));
    const fundItems = (fundResponse.data as any[]).map(item => ({ ...item, type: 'FUND' }));

    return [...stockItems, ...fundItems];
  },

  // 添加到自选
  addToWatchlist: (item: { code: string; type: 'STOCK' | 'FUND'; name: string }) =>
    item.type === 'STOCK' ? api.post(`/watchlist/stocks/${item.code}`) : api.post(`/watchlist/funds/${item.code}`),

  // 从自选移除
  removeFromWatchlist: (code: string) => {
    // 尝试识别是股票还是基金代码
    if (/^(sh|sz|bj)/i.test(code)) {
      return api.delete(`/watchlist/stocks/${code}`);
    } else {
      return api.delete(`/watchlist/funds/${code}`);
    }
  },

  // 更新自选排序
  updateWatchlistOrder: (orders: { code: string; order: number }[]) =>
    api.put('/watchlist/order', { orders }),

  // 清空自选列表
  clearWatchlist: () => api.delete('/watchlist'),
};

export default api;
