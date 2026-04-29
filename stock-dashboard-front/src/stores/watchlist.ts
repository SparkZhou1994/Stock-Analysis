import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Stock, Fund } from '../types/stock';
import { watchlistApi } from '../services/api';

type WatchlistItem = Stock | Fund;

export const useWatchlistStore = defineStore('watchlist', () => {
  const items = ref<WatchlistItem[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  const stockItems = computed(() => items.value.filter(item => 'stockCode' in item) as Stock[]);
  const fundItems = computed(() => items.value.filter(item => 'fundCode' in item) as Fund[]);
  const totalCount = computed(() => items.value.length);

  /**
   * 加载自选列表
   */
  async function loadWatchlist(): Promise<void> {
    loading.value = true;
    error.value = null;

    try {
      const response = await watchlistApi.getWatchlist();
      items.value = response as unknown as WatchlistItem[];
    } catch (err) {
      console.error('加载自选列表失败:', err);
      error.value = '加载自选列表失败';
    } finally {
      loading.value = false;
    }
  }

  /**
   * 添加到自选
   */
  async function addToWatchlist(item: WatchlistItem): Promise<boolean> {
    try {
      const code = 'stockCode' in item ? item.stockCode : item.fundCode;
      const type = 'stockCode' in item ? 'STOCK' : 'FUND';
      const name = 'stockName' in item ? item.stockName : item.fundName;

      await watchlistApi.addToWatchlist({ code, type, name });

      // 检查是否已存在
      const exists = items.value.some(i => {
        const iCode = 'stockCode' in i ? i.stockCode : i.fundCode;
        return iCode === code;
      });

      if (!exists) {
        item.isWatchlist = true;
        item.watchlistOrder = items.value.length + 1;
        items.value.push(item);
      }

      return true;
    } catch (err) {
      console.error('添加到自选失败:', err);
      error.value = '添加到自选失败';
      return false;
    }
  }

  /**
   * 从自选移除
   */
  async function removeFromWatchlist(code: string): Promise<boolean> {
    try {
      await watchlistApi.removeFromWatchlist(code);
      items.value = items.value.filter(item => {
        const itemCode = 'stockCode' in item ? item.stockCode : item.fundCode;
        return itemCode !== code;
      });
      return true;
    } catch (err) {
      console.error('从自选移除失败:', err);
      error.value = '从自选移除失败';
      return false;
    }
  }

  /**
   * 更新排序
   */
  async function updateOrder(orders: { code: string; order: number }[]): Promise<boolean> {
    try {
      await watchlistApi.updateWatchlistOrder(orders);

      // 更新本地顺序
      items.value.sort((a, b) => {
        const aCode = 'stockCode' in a ? a.stockCode : a.fundCode;
        const bCode = 'stockCode' in b ? b.stockCode : b.fundCode;
        const aOrder = orders.find(o => o.code === aCode)?.order || 0;
        const bOrder = orders.find(o => o.code === bCode)?.order || 0;
        return aOrder - bOrder;
      });

      return true;
    } catch (err) {
      console.error('更新排序失败:', err);
      error.value = '更新排序失败';
      return false;
    }
  }

  /**
   * 检查是否在自选列表中
   */
  function isInWatchlist(code: string): boolean {
    return items.value.some(item => {
      const itemCode = 'stockCode' in item ? item.stockCode : item.fundCode;
      return itemCode === code;
    });
  }

  /**
   * 更新实时价格
   */
  function updateRealtimePrice(code: string, price: number, changeAmount: number, changePercent: number): void {
    const item = items.value.find(item => {
      const itemCode = 'stockCode' in item ? item.stockCode : item.fundCode;
      return itemCode === code;
    });

    if (item) {
      if ('currentPrice' in item) {
        item.currentPrice = price;
      }
      if ('currentNetValue' in item) {
        item.currentNetValue = price;
      }
      if ('changeAmount' in item) {
        item.changeAmount = changeAmount;
      }
      if ('dailyChangeAmount' in item) {
        item.dailyChangeAmount = changeAmount;
      }
      if ('changePercent' in item) {
        item.changePercent = changePercent;
      }
      if ('dailyChangePercent' in item) {
        item.dailyChangePercent = changePercent;
      }
    }
  }

  /**
   * 清空自选列表
   */
  async function clearWatchlist(): Promise<boolean> {
    try {
      await watchlistApi.clearWatchlist();
      items.value = [];
      return true;
    } catch (err) {
      console.error('清空自选列表失败:', err);
      error.value = '清空自选列表失败';
      return false;
    }
  }

  /**
   * 清除错误
   */
  function clearError(): void {
    error.value = null;
  }

  return {
    // 状态
    items,
    loading,
    error,
    stockItems,
    fundItems,
    totalCount,

    // 方法
    loadWatchlist,
    addToWatchlist,
    removeFromWatchlist,
    updateOrder,
    isInWatchlist,
    updateRealtimePrice,
    clearWatchlist,
    clearError,
  };
});
