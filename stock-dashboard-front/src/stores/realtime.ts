import { defineStore } from 'pinia';
import { ref, reactive } from 'vue';
import type { RealtimePriceData } from '../types/stock';
import { wsService } from '../services/websocket';

interface PriceCache {
  [code: string]: {
    price: number;
    changeAmount: number;
    changePercent: number;
    timestamp: string;
    updateCount: number;
  };
}

export const useRealtimeStore = defineStore('realtime', () => {
  const connected = ref(false);
  const prices = reactive<PriceCache>({});
  const subscribedCodes = ref<string[]>([]);
  const messageCount = ref(0);
  const lastUpdateTime = ref<string | null>(null);

  /**
   * 初始化WebSocket连接
   */
  function initWebSocket(): void {
    if (!wsService.isConnected()) {
      wsService.connect();

      // 监听连接成功
      wsService.addListener('CONNECTED', () => {
        connected.value = true;
        console.log('WebSocket连接成功');
      });

      // 监听断开连接
      wsService.addListener('DISCONNECTED', () => {
        connected.value = false;
        console.log('WebSocket连接断开');
      });

      // 监听实时价格数据
      wsService.addListener<RealtimePriceData>('REALTIME_PRICE', (data) => {
        updatePrice(data);
      });

      // 监听订阅成功
      wsService.addListener<any>('SUBSCRIBE_SUCCESS', (data) => {
        const key = `${data.securityType}:${data.code}`;
        if (!subscribedCodes.value.includes(key)) {
          subscribedCodes.value.push(key);
        }
      });

      // 监听取消订阅成功
      wsService.addListener<any>('UNSUBSCRIBE_SUCCESS', (data) => {
        const key = `${data.securityType}:${data.code}`;
        const index = subscribedCodes.value.indexOf(key);
        if (index > -1) {
          subscribedCodes.value.splice(index, 1);
        }
      });
    }
  }

  /**
   * 订阅实时数据
   */
  function subscribe(securityType: 'STOCK' | 'FUND', code: string): void {
    wsService.subscribe(securityType, code);
  }

  /**
   * 取消订阅
   */
  function unsubscribe(securityType: 'STOCK' | 'FUND', code: string): void {
    wsService.unsubscribe(securityType, code);
    const key = `${securityType}:${code}`;
    const index = subscribedCodes.value.indexOf(key);
    if (index > -1) {
      subscribedCodes.value.splice(index, 1);
    }
  }

  /**
   * 更新价格数据
   */
  function updatePrice(data: RealtimePriceData): void {
    const key = `${data.type}:${data.code}`;

    if (prices[key]) {
      prices[key].price = data.price;
      prices[key].changeAmount = data.changeAmount;
      prices[key].changePercent = data.changePercent;
      prices[key].timestamp = data.timestamp;
      prices[key].updateCount++;
    } else {
      prices[key] = {
        price: data.price,
        changeAmount: data.changeAmount,
        changePercent: data.changePercent,
        timestamp: data.timestamp,
        updateCount: 1,
      };
    }

    messageCount.value++;
    lastUpdateTime.value = new Date().toLocaleTimeString('zh-CN');
  }

  /**
   * 获取价格数据
   */
  function getPrice(securityType: 'STOCK' | 'FUND', code: string) {
    const key = `${securityType}:${code}`;
    return prices[key] || null;
  }

  /**
   * 检查是否已订阅
   */
  function isSubscribed(securityType: 'STOCK' | 'FUND', code: string): boolean {
    const key = `${securityType}:${code}`;
    return subscribedCodes.value.includes(key);
  }

  /**
   * 断开连接
   */
  function disconnect(): void {
    wsService.disconnect();
    connected.value = false;
    subscribedCodes.value = [];
  }

  return {
    // 状态
    connected,
    prices,
    subscribedCodes,
    messageCount,
    lastUpdateTime,

    // 方法
    initWebSocket,
    subscribe,
    unsubscribe,
    getPrice,
    isSubscribed,
    updatePrice,
    disconnect,
  };
});
