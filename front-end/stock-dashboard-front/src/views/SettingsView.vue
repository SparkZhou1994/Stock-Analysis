<template>
  <div class="settings-view min-h-screen bg-gray-50 py-6">
    <div class="container mx-auto px-4">
      <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <h2 class="text-xl font-bold text-gray-800 mb-6">系统设置</h2>

        <el-form label-width="120px">
          <el-form-item label="刷新频率">
            <el-radio-group v-model="refreshInterval" size="small">
              <el-radio :label="3">3秒</el-radio>
              <el-radio :label="5">5秒</el-radio>
              <el-radio :label="10">10秒</el-radio>
              <el-radio :label="30">30秒</el-radio>
            </el-radio-group>
            <div class="text-xs text-gray-500 mt-1">实时数据刷新间隔时间</div>
          </el-form-item>

          <el-form-item label="涨跌颜色">
            <el-radio-group v-model="colorScheme" size="small">
              <el-radio label="red-up">红涨绿跌</el-radio>
              <el-radio label="green-up">绿涨红跌</el-radio>
            </el-radio-group>
            <div class="text-xs text-gray-500 mt-1">设置价格涨跌的显示颜色</div>
          </el-form-item>

          <el-form-item label="数据缓存">
            <el-switch v-model="enableCache" />
            <div class="text-xs text-gray-500 mt-1">开启后将缓存历史数据，提升加载速度</div>
          </el-form-item>

          <el-form-item label="通知设置">
            <el-switch v-model="enableNotification" />
            <div class="text-xs text-gray-500 mt-1">开启价格异动等系统通知</div>
          </el-form-item>

          <el-form-item label="默认K线周期">
            <el-select v-model="defaultKlinePeriod" size="small" style="width: 120px">
              <el-option label="1月" value="1M" />
              <el-option label="3月" value="3M" />
              <el-option label="6月" value="6M" />
              <el-option label="1年" value="1Y" />
              <el-option label="全部" value="ALL" />
            </el-select>
            <div class="text-xs text-gray-500 mt-1">详情页默认显示的K线时间范围</div>
          </el-form-item>

          <el-divider />

          <el-form-item label="数据管理">
            <div class="space-y-2">
              <el-button type="warning" size="small" @click="handleClearCache">
                清除缓存数据
              </el-button>
              <el-button type="danger" size="small" @click="handleClearWatchlist">
                清空自选列表
              </el-button>
            </div>
          </el-form-item>

          <el-divider />

          <el-form-item label="关于系统">
            <div class="text-sm text-gray-600">
              <p>股票基金看板 v1.0.0</p>
              <p class="mt-1">数据来源：东方财富网</p>
              <p class="mt-1">更新时间：2026-04-28</p>
            </div>
          </el-form-item>
        </el-form>

        <div class="mt-8 flex justify-end">
          <el-button type="primary" @click="handleSave">保存设置</el-button>
          <el-button @click="handleReset" class="ml-2">恢复默认</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useWatchlistStore } from '../stores/watchlist';

const watchlistStore = useWatchlistStore();

const refreshInterval = ref(5);
const colorScheme = ref('red-up');
const enableCache = ref(true);
const enableNotification = ref(true);
const defaultKlinePeriod = ref('1M');

/**
 * 加载设置
 */
function loadSettings() {
  const settings = localStorage.getItem('appSettings');
  if (settings) {
    const parsed = JSON.parse(settings);
    refreshInterval.value = parsed.refreshInterval || 5;
    colorScheme.value = parsed.colorScheme || 'red-up';
    enableCache.value = parsed.enableCache ?? true;
    enableNotification.value = parsed.enableNotification ?? true;
    defaultKlinePeriod.value = parsed.defaultKlinePeriod || '1M';
  }
}

/**
 * 保存设置
 */
function handleSave() {
  const settings = {
    refreshInterval: refreshInterval.value,
    colorScheme: colorScheme.value,
    enableCache: enableCache.value,
    enableNotification: enableNotification.value,
    defaultKlinePeriod: defaultKlinePeriod.value,
  };

  localStorage.setItem('appSettings', JSON.stringify(settings));
  ElMessage.success('设置已保存');
}

/**
 * 恢复默认设置
 */
function handleReset() {
  refreshInterval.value = 5;
  colorScheme.value = 'red-up';
  enableCache.value = true;
  enableNotification.value = true;
  defaultKlinePeriod.value = '1M';
  localStorage.removeItem('appSettings');
  ElMessage.success('已恢复默认设置');
}

/**
 * 清除缓存
 */
function handleClearCache() {
  ElMessageBox.confirm('确定要清除所有缓存数据吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    localStorage.removeItem('stockDataCache');
    localStorage.removeItem('fundDataCache');
    localStorage.removeItem('historicalDataCache');
    ElMessage.success('缓存已清除');
  }).catch(() => {});
}

/**
 * 清空自选列表
 */
function handleClearWatchlist() {
  ElMessageBox.confirm('确定要清空所有自选股票和基金吗？此操作不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await watchlistStore.clearWatchlist();
    ElMessage.success('自选列表已清空');
  }).catch(() => {});
}

onMounted(() => {
  loadSettings();
});
</script>
