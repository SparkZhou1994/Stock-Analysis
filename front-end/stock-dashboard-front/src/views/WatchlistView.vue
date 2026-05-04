<template>
  <div class="watchlist-view min-h-screen bg-gray-50 py-6">
    <div class="container mx-auto px-4">
      <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-xl font-bold text-gray-800">我的自选</h2>
          <div class="flex items-center space-x-2">
            <el-button type="primary" @click="$router.push('/search')">
              <template #icon><Plus /></template>
              添加
            </el-button>
          </div>
        </div>

        <div v-if="watchlistStore.loading" class="flex justify-center items-center py-20">
          <el-spinner size="40" />
        </div>

        <div v-else-if="watchlistStore.totalCount === 0" class="text-center py-20">
          <el-empty description="暂无自选股票和基金" />
          <el-button type="primary" @click="$router.push('/search')" class="mt-4">
            去添加
          </el-button>
        </div>

        <div v-else>
          <el-tabs v-model="activeTab" class="mb-6">
            <el-tab-pane label="全部" name="all">
              <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                <StockCard
                  v-for="stock in watchlistStore.stockItems"
                  :key="stock.stockCode"
                  :stock="stock"
                  :show-watch-btn="true"
                  :is-watchlist="true"
                  @click="goToDetail('STOCK', stock.stockCode)"
                  @watch-toggle="handleWatchToggle(stock)"
                />
                <FundCard
                  v-for="fund in watchlistStore.fundItems"
                  :key="fund.fundCode"
                  :fund="fund"
                  :show-watch-btn="true"
                  :is-watchlist="true"
                  @click="goToDetail('FUND', fund.fundCode)"
                  @watch-toggle="handleWatchToggle(fund)"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane label="股票" name="stocks">
              <div v-if="watchlistStore.stockItems.length === 0" class="text-center py-12">
                <el-empty description="暂无自选股票" />
              </div>
              <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                <StockCard
                  v-for="stock in watchlistStore.stockItems"
                  :key="stock.stockCode"
                  :stock="stock"
                  :show-watch-btn="true"
                  :is-watchlist="true"
                  @click="goToDetail('STOCK', stock.stockCode)"
                  @watch-toggle="handleWatchToggle(stock)"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane label="基金" name="funds">
              <div v-if="watchlistStore.fundItems.length === 0" class="text-center py-12">
                <el-empty description="暂无自选基金" />
              </div>
              <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                <FundCard
                  v-for="fund in watchlistStore.fundItems"
                  :key="fund.fundCode"
                  :fund="fund"
                  :show-watch-btn="true"
                  :is-watchlist="true"
                  @click="goToDetail('FUND', fund.fundCode)"
                  @watch-toggle="handleWatchToggle(fund)"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import StockCard from '../components/dashboard/StockCard.vue';
import FundCard from '../components/dashboard/FundCard.vue';
import type { Stock, Fund } from '../types/stock';
import { useWatchlistStore } from '../stores/watchlist';
import { useRealtimeStore } from '../stores/realtime';

const router = useRouter();
const watchlistStore = useWatchlistStore();
const realtimeStore = useRealtimeStore();

const activeTab = ref('all');

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
  const name = 'stockName' in item ? item.stockName : item.fundName;

  try {
    await ElMessageBox.confirm(`确定要将${name}从自选列表中移除吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    await watchlistStore.removeFromWatchlist(code);
    realtimeStore.unsubscribe('stockCode' in item ? 'STOCK' : 'FUND', code);
    ElMessage.success('已移除自选');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败');
    }
  }
}

onMounted(async () => {
  await watchlistStore.loadWatchlist();
});
</script>
