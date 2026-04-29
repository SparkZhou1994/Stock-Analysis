<template>
  <div class="dashboard-view min-h-screen bg-gray-50 py-6">
    <div class="container mx-auto px-4">
      <div class="mb-6">
        <el-tabs v-model="activeTab" class="sticky top-0 bg-white z-10 rounded-lg shadow-sm">
          <el-tab-pane label="股票" name="stocks">
            <div v-if="stocksLoading" class="flex justify-center items-center py-20">
              <el-spinner size="40" />
            </div>

            <div v-else-if="stocks.length === 0" class="text-center py-20">
              <el-empty description="暂无股票数据" />
              <el-button type="primary" @click="$router.push('/search')" class="mt-4">
                去添加自选股票
              </el-button>
            </div>

            <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              <StockCard
                v-for="stock in stocks"
                :key="stock.stockCode"
                :stock="stock"
                :show-watch-btn="true"
                :is-watchlist="watchlistStore.isInWatchlist(stock.stockCode)"
                @click="goToDetail('STOCK', stock.stockCode)"
                @watch-toggle="handleWatchToggle(stock)"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="基金" name="funds">
            <div v-if="fundsLoading" class="flex justify-center items-center py-20">
              <el-spinner size="40" />
            </div>

            <div v-else-if="funds.length === 0" class="text-center py-20">
              <el-empty description="暂无基金数据" />
              <el-button type="primary" @click="$router.push('/search')" class="mt-4">
                去添加自选基金
              </el-button>
            </div>

            <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              <FundCard
                v-for="fund in funds"
                :key="fund.fundCode"
                :fund="fund"
                :show-watch-btn="true"
                :is-watchlist="watchlistStore.isInWatchlist(fund.fundCode)"
                @click="goToDetail('FUND', fund.fundCode)"
                @watch-toggle="handleWatchToggle(fund)"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import StockCard from '../components/dashboard/StockCard.vue';
import FundCard from '../components/dashboard/FundCard.vue';
import { stockApi, fundApi } from '../services/api';
import type { Stock, Fund } from '../types/stock';
import { useWatchlistStore } from '../stores/watchlist';
import { useRealtimeStore } from '../stores/realtime';

const router = useRouter();
const watchlistStore = useWatchlistStore();
const realtimeStore = useRealtimeStore();

const activeTab = ref('stocks');
const stocks = ref<Stock[]>([]);
const funds = ref<Fund[]>([]);
const stocksLoading = ref(false);
const fundsLoading = ref(false);


/**
 * 加载股票数据
 */
async function loadStocks() {
  stocksLoading.value = true;
  try {
    const response = await stockApi.getAllStocks();
    stocks.value = response as unknown as Stock[];
  } catch (error) {
    console.error('加载股票数据失败:', error);
    ElMessage.error('加载股票数据失败');
  } finally {
    stocksLoading.value = false;
  }
}

/**
 * 加载基金数据
 */
async function loadFunds() {
  fundsLoading.value = true;
  try {
    const response = await fundApi.getAllFunds();
    funds.value = response as unknown as Fund[];
  } catch (error) {
    console.error('加载基金数据失败:', error);
    ElMessage.error('加载基金数据失败');
  } finally {
    fundsLoading.value = false;
  }
}

/**
 * 跳转到详情页
 */
function goToDetail(type: 'STOCK' | 'FUND', code: string) {
  router.push({ name: 'detail', params: { type, code } });
}

/**
 * 切换自选状态
 */
async function handleWatchToggle(item: Stock | Fund) {
  const code = 'stockCode' in item ? item.stockCode : item.fundCode;
  const isWatch = watchlistStore.isInWatchlist(code);

  try {
    if (isWatch) {
      await watchlistStore.removeFromWatchlist(code);
      ElMessage.success('已取消自选');
    } else {
      await watchlistStore.addToWatchlist(item);
      ElMessage.success('已添加到自选');
    }
  } catch (error) {
    ElMessage.error(isWatch ? '取消自选失败' : '添加自选失败');
  }
}

onMounted(async () => {
  await Promise.all([loadStocks(), loadFunds(), watchlistStore.loadWatchlist()]);

  // 订阅实时数据
  watchlistStore.items.forEach((item) => {
    if ('stockCode' in item) {
      realtimeStore.subscribe('STOCK', item.stockCode);
    } else {
      realtimeStore.subscribe('FUND', item.fundCode);
    }
  });
});
</script>
