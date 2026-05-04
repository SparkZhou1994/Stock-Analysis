<template>
  <div class="search-view min-h-screen bg-gray-50 py-6">
    <div class="container mx-auto px-4">
      <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div class="flex items-center space-x-4 mb-6">
          <el-input
            v-model="keyword"
            placeholder="输入股票代码或名称搜索"
            size="large"
            class="flex-1"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon class="text-gray-400">
                <Search />
              </el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        </div>

        <div class="flex space-x-2 mb-6">
          <el-radio-group v-model="searchType" size="small">
            <el-radio label="all">全部</el-radio>
            <el-radio label="stock">股票</el-radio>
            <el-radio label="fund">基金</el-radio>
          </el-radio-group>
        </div>

        <div v-if="loading" class="flex justify-center items-center py-12">
          <el-spinner size="40" />
        </div>

        <div v-else-if="keyword && results.length === 0" class="text-center py-12">
          <el-empty description="没有找到相关结果" />
        </div>

        <div v-else-if="results.length > 0">
          <h3 class="text-lg font-medium text-gray-800 mb-4">搜索结果 ({{ results.length }})</h3>

          <el-table :data="results" stripe border style="width: 100%">
            <el-table-column prop="code" label="代码" width="120" />
            <el-table-column prop="name" label="名称" min-width="180" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.type === 'STOCK' ? 'primary' : 'success'">
                  {{ scope.row.type === 'STOCK' ? '股票' : '基金' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="addToWatchlist(scope.row)"
                  :disabled="watchlistStore.isInWatchlist(scope.row.code)"
                >
                  {{ watchlistStore.isInWatchlist(scope.row.code) ? '已添加' : '加自选' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-else class="text-center py-12 text-gray-400">
          输入股票代码或名称开始搜索
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { stockApi, fundApi } from '../services/api';
import { useWatchlistStore } from '../stores/watchlist';
import { useRealtimeStore } from '../stores/realtime';

const watchlistStore = useWatchlistStore();
const realtimeStore = useRealtimeStore();

const keyword = ref('');
const searchType = ref('all');
const loading = ref(false);
const results = ref<any[]>([]);

// 防抖搜索
let searchTimeout: number | null = null;

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }

  // 清除之前的超时
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }

  // 设置新的超时
  searchTimeout = window.setTimeout(async () => {
    loading.value = true;
    results.value = [];

    try {
      const requests = [];

      if (searchType.value === 'all' || searchType.value === 'stock') {
        requests.push(stockApi.searchStocks(keyword.value.trim()));
      }

      if (searchType.value === 'all' || searchType.value === 'fund') {
        requests.push(fundApi.searchFunds(keyword.value.trim()));
      }

      const responses = await Promise.all(requests);

      let allResults: any[] = [];

      if (responses[0]) {
        const stockResults = responses[0] as unknown as any[];
        allResults = allResults.concat(
          stockResults.map((item) => ({
            ...item,
            type: 'STOCK',
            code: item.stockCode,
            name: item.stockName,
          }))
        );
      }

      if (responses[1]) {
        const fundResults = responses[1] as unknown as any[];
        allResults = allResults.concat(
          fundResults.map((item) => ({
            ...item,
            type: 'FUND',
            code: item.fundCode,
            name: item.fundName,
          }))
        );
      }

      results.value = allResults;
    } catch (error) {
      console.error('搜索失败:', error);
      ElMessage.error('搜索失败');
    } finally {
      loading.value = false;
    }
  }, 300);
};

async function addToWatchlist(item: any) {
  try {
    await watchlistStore.addToWatchlist(item);
    // 订阅实时数据
    realtimeStore.subscribe(item.type, item.code);
    ElMessage.success('添加到自选成功');
  } catch (error) {
    ElMessage.error('添加到自选失败');
  }
}
</script>
