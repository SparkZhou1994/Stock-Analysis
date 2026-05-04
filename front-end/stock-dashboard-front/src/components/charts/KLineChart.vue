<template>
  <div class="kline-chart h-full w-full">
    <div ref="chartRef" class="w-full" :style="{ height: height + 'px' }"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import type { KLineData } from '../../types/stock';

const props = withDefaults(defineProps<{
  data: KLineData[];
  height?: number;
  showMA?: boolean;
}>(), {
  height: 400,
  showMA: true,
});

const chartRef = ref<HTMLDivElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

function initChart() {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);
  renderChart();
}

function renderChart() {
  if (!chartInstance || !props.data || props.data.length === 0) return;

  const dates = props.data.map(item => item.tradeDate);
  const klineData = props.data.map(item => [item.openPrice, item.closePrice, item.lowPrice, item.highPrice]);
  const volumes = props.data.map(item => item.volume);
  const ma5Data = props.data.map(item => item.ma5 || '-');
  const ma10Data = props.data.map(item => item.ma10 || '-');
  const ma20Data = props.data.map(item => item.ma20 || '-');

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
      },
    },
    grid: [
      {
        left: '3%',
        right: '4%',
        top: '5%',
        height: '60%',
      },
      {
        left: '3%',
        right: '4%',
        top: '70%',
        height: '20%',
      },
    ],
    xAxis: [
      {
        type: 'category',
        data: dates,
        boundaryGap: true,
        axisLine: { onZero: false },
        splitLine: { show: false },
        min: 'dataMin',
        max: 'dataMax',
      },
      {
        type: 'category',
        gridIndex: 1,
        data: dates,
        boundaryGap: true,
        axisLine: { onZero: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
      },
    ],
    yAxis: [
      {
        scale: true,
        splitArea: {
          show: true,
        },
      },
      {
        scale: true,
        gridIndex: 1,
        splitNumber: 2,
        axisLabel: { show: false },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
      },
    ],
    dataZoom: [
      {
        type: 'inside',
        xAxisIndex: [0, 1],
        start: 0,
        end: 100,
      },
      {
        show: true,
        xAxisIndex: [0, 1],
        type: 'slider',
        bottom: '5%',
        start: 0,
        end: 100,
      },
    ],
    series: [
      {
        name: 'K线',
        type: 'candlestick',
        data: klineData,
        itemStyle: {
          color: '#ef5350',
          color0: '#26a69a',
          borderColor: '#ef5350',
          borderColor0: '#26a69a',
        },
      },
      ...(props.showMA
        ? [
            {
              name: 'MA5',
              type: 'line' as const,
              data: ma5Data,
              smooth: true,
              lineStyle: {
                opacity: 0.5,
                width: 1,
                color: '#2196f3',
              },
              showSymbol: false,
            },
            {
              name: 'MA10',
              type: 'line' as const,
              data: ma10Data,
              smooth: true,
              lineStyle: {
                opacity: 0.5,
                width: 1,
                color: '#f44336',
              },
              showSymbol: false,
            },
            {
              name: 'MA20',
              type: 'line' as const,
              data: ma20Data,
              smooth: true,
              lineStyle: {
                opacity: 0.5,
                width: 1,
                color: '#ff9800',
              },
              showSymbol: false,
            },
          ]
        : []),
      {
        name: '成交量',
        type: 'bar',
        xAxisIndex: 1,
        yAxisIndex: 1,
        data: volumes,
        itemStyle: {
          color: (params) => {
            const index = params.dataIndex;
            const data = props.data[index];
            return data.closePrice >= data.openPrice ? '#ef5350' : '#26a69a';
          },
        },
      },
    ],
  };

  chartInstance.setOption(option, true);
}

function resizeChart() {
  chartInstance?.resize();
}

onMounted(() => {
  initChart();
  window.addEventListener('resize', resizeChart);
});

watch(
  () => props.data,
  () => {
    renderChart();
  },
  { deep: true }
);

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart);
  chartInstance?.dispose();
});
</script>
