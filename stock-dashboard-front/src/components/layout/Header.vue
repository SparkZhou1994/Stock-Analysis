<template>
  <el-header class="header bg-white shadow-sm border-b border-gray-200">
    <div class="container mx-auto px-4 h-full flex items-center justify-between">
      <div class="flex items-center space-x-2">
        <el-icon class="text-2xl text-blue-600" size="24">
          <TrendCharts />
        </el-icon>
        <h1 class="text-xl font-bold text-gray-800">股票基金看板</h1>
      </div>

      <div class="flex items-center space-x-1">
        <el-button
          @click="$router.push('/')"
          :type="$router.currentRoute.value.name === 'dashboard' ? 'primary' : 'default'"
          plain
        >
          <template #icon><Monitor /></template>
          看板
        </el-button>
        <el-button
          @click="$router.push('/watchlist')"
          :type="$router.currentRoute.value.name === 'watchlist' ? 'primary' : 'default'"
          plain
        >
          <template #icon><Star /></template>
          自选
        </el-button>
        <el-button
          @click="$router.push('/search')"
          :type="$router.currentRoute.value.name === 'search' ? 'primary' : 'default'"
          plain
        >
          <template #icon><Search /></template>
          搜索
        </el-button>
        <el-button
          @click="$router.push('/settings')"
          :type="$router.currentRoute.value.name === 'settings' ? 'primary' : 'default'"
          plain
        >
          <template #icon><Setting /></template>
          设置
        </el-button>
      </div>

      <div class="flex items-center space-x-4">
        <div class="flex items-center" :class="wsConnected ? 'text-green-600' : 'text-red-600'">
          <el-icon size="14">
            <Connection v-if="wsConnected" />
            <Connection v-else style="opacity: 0.5;" />
          </el-icon>
          <span class="text-xs ml-1">{{ wsConnected ? '实时连接' : '未连接' }}</span>
        </div>

        <div class="text-xs text-gray-500" v-if="lastUpdateTime">
          最后更新: {{ lastUpdateTime }}
        </div>
      </div>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { TrendCharts, Monitor, Star, Search, Setting, Connection } from '@element-plus/icons-vue';
import { useRealtimeStore } from '../../stores/realtime';

const realtimeStore = useRealtimeStore();

const wsConnected = computed(() => realtimeStore.connected);
const lastUpdateTime = computed(() => realtimeStore.lastUpdateTime);
</script>

<style scoped>
.header {
  height: 60px !important;
}
</style>
