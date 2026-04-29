<template>
  <div class="detail-view min-h-screen bg-gray-50 py-6">
    <div class="container mx-auto px-4">
      <div v-if="loading" class="flex justify-center items-center py-20">
        <el-spinner size="40" />
      </div>

      <div v-else-if="!stockData && !fundData" class="text-center py-20">
        <el-empty description="加载失败，未找到相关数据" />
        <el-button type="primary" @click="$router.go(-1)" class="mt-4">返回</el-button>
      </div>

      <div v-else>
        <!-- 头部信息 -->
        <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div class="flex justify-between items-start mb-4">
            <div>
              <div class="flex items-center space-x-3 mb-1">
                <h2 class="text-2xl font-bold text-gray-800">{{ name }}</h2>
                <el-tag size="small" :type="type === 'STOCK' ? 'primary' : 'success'">
                  {{ type === 'STOCK' ? '股票' : '基金' }}
                </el-tag>
                <el-tag size="small" v-if="isWatchlist" type="warning">
                  自选
                </el-tag>
              </div>
              <p class="text-sm text-gray-500">{{ code }}</p>
            </div>
            <div class="flex items-center space-x-3">
              <el-button
                :type="isWatchlist ? 'warning' : 'primary'"
                @click="handleWatchToggle"
              >
                <template #icon><Star v-if="isWatchlist" /><Star v-else :fill="false" /></template>
                {{ isWatchlist ? '已自选' : '加自选' }}
              </el-button>
              <el-button @click="router.go(-1)">返回</el-button>
            </div>
          </div>

          <!-- 价格信息 -->
          <div class="flex items-baseline space-x-4 mb-6">
            <span class="text-4xl font-bold" :class="changePercent >= 0 ? 'text-red-600' : 'text-green-600'">
              {{ currentPrice }}
            </span>
            <span class="text-xl" :class="changePercent >= 0 ? 'text-red-500' : 'text-green-500'">
              {{ formatChangeAmount(changeAmount) }}
              {{ formatChangePercent(changePercent) }}
            </span>
            <span class="text-sm text-gray-500">
              更新时间: {{ updateTime ? formatDate(updateTime, 'HH:mm:ss') : '--' }}
            </span>
          </div>

          <!-- 基本信息卡片 -->
          <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
            <div class="bg-gray-50 rounded-lg p-3">
              <p class="text-xs text-gray-500 mb-1">今开</p>
              <p class="text-lg font-medium">{{ formatNumber(openPrice) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <p class="text-xs text-gray-500 mb-1">最高</p>
              <p class="text-lg font-medium text-red-600">{{ formatNumber(highPrice) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <p class="text-xs text-gray-500 mb-1">最低</p>
              <p class="text-lg font-medium text-green-600">{{ formatNumber(lowPrice) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3">
              <p class="text-xs text-gray-500 mb-1">昨收</p>
              <p class="text-lg font-medium">{{ formatNumber(previousClose) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3" v-if="volume !== undefined">
              <p class="text-xs text-gray-500 mb-1">成交量</p>
              <p class="text-lg font-medium">{{ formatVolume(volume) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3" v-if="turnover !== undefined">
              <p class="text-xs text-gray-500 mb-1">成交额</p>
              <p class="text-lg font-medium">{{ formatTurnover(turnover) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3" v-if="peRatio !== undefined">
              <p class="text-xs text-gray-500 mb-1">市盈率</p>
              <p class="text-lg font-medium">{{ formatNumber(peRatio) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3" v-if="pbRatio !== undefined">
              <p class="text-xs text-gray-500 mb-1">市净率</p>
              <p class="text-lg font-medium">{{ formatNumber(pbRatio) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3" v-if="marketCap !== undefined">
              <p class="text-xs text-gray-500 mb-1">总市值</p>
              <p class="text-lg font-medium">{{ formatLargeNumber(marketCap) }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3" v-if="fundSize !== undefined">
              <p class="text-xs text-gray-500 mb-1">基金规模</p>
              <p class="text-lg font-medium">{{ formatLargeNumber(fundSize) }}亿元</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3" v-if="fundManager !== undefined">
              <p class="text-xs text-gray-500 mb-1">基金经理</p>
              <p class="text-lg font-medium">{{ fundManager }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3" v-if="fundCompany !== undefined">
              <p class="text-xs text-gray-500 mb-1">基金公司</p>
              <p class="text-lg font-medium">{{ fundCompany }}</p>
            </div>
          </div>
        </div>

        <!-- K线图区域 -->
        <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-medium text-gray-800">历史行情</h3>
            <div class="flex space-x-2">
              <el-radio-group v-model="timeRange" size="small" @change="loadHistoricalData">
                <el-radio-button label="1M">1月</el-radio-button>
                <el-radio-button label="3M">3月</el-radio-button>
                <el-radio-button label="6M">6月</el-radio-button>
                <el-radio-button label="1Y">1年</el-radio-button>
                <el-radio-button label="ALL">全部</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <div v-if="klineLoading" class="flex justify-center items-center py-40">
            <el-spinner size="40" />
          </div>

          <div v-else-if="historicalData.length === 0" class="text-center py-40">
            <el-empty description="暂无历史数据" />
          </div>

          <div v-else>
            <KLineChart :data="historicalData" :height="500" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Star } from '@element-plus/icons-vue';
import KLineChart from '../components/charts/KLineChart.vue';
import { stockApi, fundApi } from '../services/api';
import type { Stock, Fund, KLineData } from '../types/stock';
import { useWatchlistStore } from '../stores/watchlist';
import { useRealtimeStore } from '../stores/realtime';
import { formatNumber, formatChangePercent, formatChangeAmount, formatVolume, formatTurnover, formatLargeNumber, formatDate } from '../utils/format';

const route = useRoute();
const router = useRouter();
const watchlistStore = useWatchlistStore();
const realtimeStore = useRealtimeStore();

const type = ref<'STOCK' | 'FUND'>(route.params.type as 'STOCK' | 'FUND');
const code = ref<string>(route.params.code as string);
const timeRange = ref('1M');

const stockData = ref<Stock | null>(null);
const fundData = ref<Fund | null>(null);
const historicalData = ref<KLineData[]>([]);
const loading = ref(false);
const klineLoading = ref(false);

const name = computed(() => {
  if (type.value === 'STOCK') return stockData.value?.stockName;
  return fundData.value?.fundName;
});

const currentPrice = computed(() => {
  if (type.value === 'STOCK') return formatNumber(stockData.value?.currentPrice);
  return formatNumber(fundData.value?.currentNetValue, 4);
});

const changePercent = computed(() => {
  if (type.value === 'STOCK') return stockData.value?.changePercent || 0;
  return fundData.value?.dailyChangePercent || 0;
});

const changeAmount = computed(() => {
  if (type.value === 'STOCK') return stockData.value?.changeAmount || 0;
  return fundData.value?.dailyChangeAmount || 0;
});

const openPrice = computed(() => {
  if (type.value === 'STOCK') return stockData.value?.openPrice;
  return undefined;
});

const highPrice = computed(() => {
  if (type.value === 'STOCK') return stockData.value?.highPrice;
  return undefined;
});

const lowPrice = computed(() => {
  if (type.value === 'STOCK') return stockData.value?.lowPrice;
  return undefined;
});

const previousClose = computed(() => {
  if (type.value === 'STOCK') return stockData.value?.previousClose;
  return fundData.value?.accumulatedNetValue;
});

const volume = computed(() => stockData.value?.volume);
const turnover = computed(() => stockData.value?.turnover);
const peRatio = computed(() => stockData.value?.peRatio);
const pbRatio = computed(() => stockData.value?.pbRatio);
const marketCap = computed(() => stockData.value?.marketCap);

const fundSize = computed(() => fundData.value?.fundSize);
const fundManager = computed(() => fundData.value?.fundManager);
const fundCompany = computed(() => fundData.value?.fundCompany);

const updateTime = computed(() => {
  if (type.value === 'STOCK') return stockData.value?.updateTime;
  return fundData.value?.updateTime;
});

const isWatchlist = computed(() => watchlistStore.isInWatchlist(code.value));

/**
 * 加载详情数据
 */
async function loadDetail() {
  loading.value = true;
  try {
    if (type.value === 'STOCK') {
      const response = await stockApi.getStockRealtime(code.value);
      stockData.value = response as unknown as Stock;
    } else {
      const response = await fundApi.getFundRealtime(code.value);
      fundData.value = response as unknown as Fund;
    }
  } catch (error) {
    console.error('加载详情失败:', error);
    ElMessage.error('加载详情失败');
  } finally {
    loading.value = false;
  }
}

/**
 * 加载历史数据
 */
async function loadHistoricalData() {
  klineLoading.value = true;
  try {
    let response;
    if (type.value === 'STOCK') {
      response = await stockApi.getStockHistory(code.value);
    } else {
      response = await fundApi.getFundHistory(code.value);
    }

    historicalData.value = response as unknown as KLineData[];

    // 按时间范围过滤
    let limit = 30;
    switch (timeRange.value) {
      case '3M':
        limit = 90;
        break;
      case '6M':
        limit = 180;
        break;
      case '1Y':
        limit = 365;
        break;
      case 'ALL':
        limit = 10000;
        break;
    }

    historicalData.value = historicalData.value.slice(-limit);
  } catch (error) {
    console.error('加载历史数据失败:', error);
    ElMessage.error('加载历史数据失败');
  } finally {
    klineLoading.value = false;
  }
}

/**
 * 切换自选状态
 */
async function handleWatchToggle() {
  if (isWatchlist.value) {
    try {
      await ElMessageBox.confirm(`确定要将${name.value}从自选列表中移除吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      });

      await watchlistStore.removeFromWatchlist(code.value);
      realtimeStore.unsubscribe(type.value, code.value);
      ElMessage.success('已移除自选');
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('移除失败');
      }
    }
  } else {
    try {
      const item = type.value === 'STOCK' ? stockData.value : fundData.value;
      if (item) {
        await watchlistStore.addToWatchlist(item);
        realtimeStore.subscribe(type.value, code.value);
        ElMessage.success('已添加到自选');
      }
    } catch (error) {
      ElMessage.error('添加到自选失败');
    }
  }
}

onMounted(async () => {
  await Promise.all([loadDetail(), loadHistoricalData()]);
  // 订阅实时数据
  realtimeStore.subscribe(type.value, code.value);
});
</script>
