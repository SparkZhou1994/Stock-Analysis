<template>
  <div
    class="stock-card rounded-lg p-4 shadow-md hover:shadow-lg transition-all duration-300 cursor-pointer border border-gray-100"
    @click="$emit('click', stock)"
  >
    <div class="flex justify-between items-start mb-2">
      <div>
        <h3 class="font-bold text-lg text-gray-800">{{ stock.stockName }}</h3>
        <p class="text-sm text-gray-500">{{ stock.stockCode }}</p>
      </div>
      <div
        class="px-3 py-1 rounded-full text-sm font-medium"
        :class="stock.changePercent >= 0 ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'"
      >
        {{ formatChangePercent(stock.changePercent) }}
      </div>
    </div>

    <div class="flex items-end justify-between mb-3">
      <span class="text-2xl font-bold" :class="stock.changePercent >= 0 ? 'text-red-600' : 'text-green-600'">
        {{ formatNumber(stock.currentPrice) }}
      </span>
      <span class="text-sm" :class="stock.changePercent >= 0 ? 'text-red-500' : 'text-green-500'">
        {{ formatChangeAmount(stock.changeAmount) }}
      </span>
    </div>

    <div class="grid grid-cols-2 gap-2 text-xs text-gray-600">
      <div class="flex justify-between">
        <span>今开</span>
        <span>{{ formatNumber(stock.openPrice) }}</span>
      </div>
      <div class="flex justify-between">
        <span>最高</span>
        <span class="text-red-500">{{ formatNumber(stock.highPrice) }}</span>
      </div>
      <div class="flex justify-between">
        <span>昨收</span>
        <span>{{ formatNumber(stock.previousClose) }}</span>
      </div>
      <div class="flex justify-between">
        <span>最低</span>
        <span class="text-green-500">{{ formatNumber(stock.lowPrice) }}</span>
      </div>
      <div class="flex justify-between">
        <span>成交量</span>
        <span>{{ formatVolume(stock.volume) }}</span>
      </div>
      <div class="flex justify-between">
        <span>成交额</span>
        <span>{{ formatTurnover(stock.turnover) }}</span>
      </div>
    </div>

    <div class="mt-3 flex justify-between items-center">
      <span class="text-xs text-gray-400">
        更新时间: {{ stock.updateTime ? formatDate(stock.updateTime, 'HH:mm:ss') : '--' }}
      </span>
      <el-button
        v-if="showWatchBtn"
        :icon="isWatchlist ? 'StarFilled' : 'Star'"
        type="warning"
        size="small"
        circle
        @click.stop="handleWatchToggle"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Stock } from '../../types/stock';
import { formatNumber, formatChangePercent, formatChangeAmount, formatVolume, formatTurnover, formatDate } from '../../utils/format';

const props = defineProps<{
  stock: Stock;
  showWatchBtn?: boolean;
  isWatchlist?: boolean;
}>();

const emit = defineEmits<{
  click: [stock: Stock];
  'watch-toggle': [stock: Stock];
}>();

function handleWatchToggle() {
  emit('watch-toggle', props.stock);
}
</script>

<style scoped>
.stock-card {
  background: white;
}

.stock-card:hover {
  transform: translateY(-2px);
}
</style>
