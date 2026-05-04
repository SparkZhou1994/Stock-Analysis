<template>
  <div
    class="fund-card rounded-lg p-4 shadow-md hover:shadow-lg transition-all duration-300 cursor-pointer border border-gray-100"
    @click="$emit('click', fund)"
  >
    <div class="flex justify-between items-start mb-2">
      <div>
        <h3 class="font-bold text-lg text-gray-800">{{ fund.fundName }}</h3>
        <p class="text-sm text-gray-500">{{ fund.fundCode }}</p>
        <p v-if="fund.fundType" class="text-xs text-gray-400 mt-0.5">{{ fund.fundType }}</p>
      </div>
      <div
        class="px-3 py-1 rounded-full text-sm font-medium"
        :class="fund.dailyChangePercent >= 0 ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'"
      >
        {{ formatChangePercent(fund.dailyChangePercent) }}
      </div>
    </div>

    <div class="flex items-end justify-between mb-3">
      <span class="text-2xl font-bold" :class="fund.dailyChangePercent >= 0 ? 'text-red-600' : 'text-green-600'">
        {{ formatNumber(fund.currentNetValue, 4) }}
      </span>
      <span class="text-sm" :class="fund.dailyChangePercent >= 0 ? 'text-red-500' : 'text-green-500'">
        {{ formatChangeAmount(fund.dailyChangeAmount) }}
      </span>
    </div>

    <div class="grid grid-cols-2 gap-2 text-xs text-gray-600">
      <div class="flex justify-between">
        <span>累计净值</span>
        <span>{{ formatNumber(fund.accumulatedNetValue, 4) }}</span>
      </div>
      <div class="flex justify-between">
        <span>近1周</span>
        <span :class="getChangeColorClass(fund.weeklyChangePercent)">
          {{ formatChangePercent(fund.weeklyChangePercent) }}
        </span>
      </div>
      <div class="flex justify-between">
        <span>近1月</span>
        <span :class="getChangeColorClass(fund.monthlyChangePercent)">
          {{ formatChangePercent(fund.monthlyChangePercent) }}
        </span>
      </div>
      <div class="flex justify-between">
        <span>近1年</span>
        <span :class="getChangeColorClass(fund.yearlyChangePercent)">
          {{ formatChangePercent(fund.yearlyChangePercent) }}
        </span>
      </div>
      <div v-if="fund.fundSize" class="flex justify-between col-span-2">
        <span>基金规模</span>
        <span>{{ formatLargeNumber(fund.fundSize) }}亿元</span>
      </div>
    </div>

    <div class="mt-3 flex justify-between items-center">
      <span class="text-xs text-gray-400">
        更新时间: {{ fund.updateTime ? formatDate(fund.updateTime, 'HH:mm:ss') : '--' }}
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
import type { Fund } from '../../types/stock';
import { formatNumber, formatChangePercent, formatChangeAmount, formatLargeNumber, formatDate, getChangeColorClass } from '../../utils/format';

const props = defineProps<{
  fund: Fund;
  showWatchBtn?: boolean;
  isWatchlist?: boolean;
}>();

const emit = defineEmits<{
  click: [fund: Fund];
  'watch-toggle': [fund: Fund];
}>();

function handleWatchToggle() {
  emit('watch-toggle', props.fund);
}
</script>

<style scoped>
.fund-card {
  background: white;
}

.fund-card:hover {
  transform: translateY(-2px);
}
</style>
