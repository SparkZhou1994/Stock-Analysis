import type { WebSocketMessage, SubscribeRequest } from '../types/stock';

const WS_BASE_URL = 'ws://localhost:8080/api/ws-handler';

class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;
  private heartbeatInterval = 30000;
  private heartbeatTimer: number | null = null;
  private subscriptions = new Set<string>();
  private listeners = new Map<string, Set<(data: any) => void>>();

  /**
   * 连接WebSocket
   */
  connect(): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.log('WebSocket已经连接');
      return;
    }

    try {
      this.ws = new WebSocket(WS_BASE_URL);

      this.ws.onopen = this.onOpen.bind(this);
      this.ws.onmessage = this.onMessage.bind(this);
      this.ws.onerror = this.onError.bind(this);
      this.ws.onclose = this.onClose.bind(this);
    } catch (error) {
      console.error('WebSocket连接失败:', error);
      this.reconnect();
    }
  }

  /**
   * 关闭连接
   */
  disconnect(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }

    this.reconnectAttempts = 0;
    this.subscriptions.clear();
  }

  /**
   * 订阅股票/基金实时数据
   */
  subscribe(securityType: 'STOCK' | 'FUND', code: string): void {
    const key = `${securityType}:${code}`;

    if (this.subscriptions.has(key)) {
      console.log(`已经订阅了${key}`);
      return;
    }

    this.subscriptions.add(key);

    if (this.ws?.readyState === WebSocket.OPEN) {
      this.sendSubscribeMessage(securityType, code);
    }
  }

  /**
   * 取消订阅
   */
  unsubscribe(securityType: 'STOCK' | 'FUND', code: string): void {
    const key = `${securityType}:${code}`;

    if (this.subscriptions.has(key)) {
      this.subscriptions.delete(key);

      if (this.ws?.readyState === WebSocket.OPEN) {
        const message: WebSocketMessage<SubscribeRequest> = {
          type: 'UNSUBSCRIBE',
          data: { securityType, code },
          timestamp: Date.now(),
        };
        this.ws.send(JSON.stringify(message));
      }
    }
  }

  /**
   * 添加消息监听器
   */
  addListener<T = any>(type: string, callback: (data: T) => void): void {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, new Set());
    }
    this.listeners.get(type)!.add(callback);
  }

  /**
   * 移除消息监听器
   */
  removeListener<T = any>(type: string, callback: (data: T) => void): void {
    const listeners = this.listeners.get(type);
    if (listeners) {
      listeners.delete(callback);
      if (listeners.size === 0) {
        this.listeners.delete(type);
      }
    }
  }

  /**
   * 获取连接状态
   */
  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  /**
   * 获取订阅列表
   */
  getSubscriptions(): string[] {
    return Array.from(this.subscriptions);
  }

  private onOpen(): void {
    console.log('WebSocket连接成功');
    this.reconnectAttempts = 0;

    // 开始心跳
    this.startHeartbeat();

    // 重新订阅之前的订阅
    this.subscriptions.forEach((key) => {
      const [securityType, code] = key.split(':');
      this.sendSubscribeMessage(securityType as 'STOCK' | 'FUND', code);
    });

    this.notifyListeners('CONNECTED', null);
  }

  private onMessage(event: MessageEvent): void {
    try {
      const message: WebSocketMessage = JSON.parse(event.data);
      console.log('收到WebSocket消息:', message);

      this.notifyListeners(message.type, message.data);

      if (message.type === 'HEARTBEAT' && message.data === 'ping') {
        // 响应心跳
        this.sendMessage('HEARTBEAT', 'pong');
      }
    } catch (error) {
      console.error('解析WebSocket消息失败:', error);
    }
  }

  private onError(event: Event): void {
    console.error('WebSocket错误:', event);
    this.notifyListeners('ERROR', event);
  }

  private onClose(event: CloseEvent): void {
    console.log('WebSocket连接关闭:', event.code, event.reason);
    this.notifyListeners('DISCONNECTED', { code: event.code, reason: event.reason });

    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }

    // 自动重连
    if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnect();
    }
  }

  private reconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket重连次数超过最大值，停止重连');
      this.notifyListeners('RECONNECT_FAILED', null);
      return;
    }

    this.reconnectAttempts++;
    console.log(`WebSocket正在尝试重连，第${this.reconnectAttempts}次...`);

    setTimeout(() => {
      this.connect();
    }, this.reconnectInterval);
  }

  private startHeartbeat(): void {
    this.heartbeatTimer = window.setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.sendMessage('HEARTBEAT', 'ping');
      }
    }, this.heartbeatInterval);
  }

  private sendSubscribeMessage(securityType: 'STOCK' | 'FUND', code: string): void {
    const message: WebSocketMessage<SubscribeRequest> = {
      type: 'SUBSCRIBE',
      data: { securityType, code },
      timestamp: Date.now(),
    };
    this.ws?.send(JSON.stringify(message));
  }

  private sendMessage<T = any>(type: WebSocketMessage['type'], data: T): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      const message: WebSocketMessage<T> = {
        type,
        data,
        timestamp: Date.now(),
      };
      this.ws.send(JSON.stringify(message));
    }
  }

  private notifyListeners(type: string, data: any): void {
    const listeners = this.listeners.get(type);
    if (listeners) {
      listeners.forEach((callback) => callback(data));
    }

    // 同时通知全局监听器
    const globalListeners = this.listeners.get('*');
    if (globalListeners) {
      globalListeners.forEach((callback) => callback({ type, data }));
    }
  }
}

export const wsService = new WebSocketService();
export default wsService;
