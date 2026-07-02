<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'

use([CanvasRenderer, BarChart, LineChart, GridComponent, LegendComponent, TooltipComponent])

const props = defineProps({
  stats: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: 'growth', // 'growth' or 'revenue'
  },
})

const emit = defineEmits(['retry'])


function formatMonth(dateString) {
  const parts = dateString.split('-')
  if (parts.length === 3) {
    const [year, month, day] = parts
    return `${day}/${month}`
  } else if (parts.length === 2) {
    const [year, month] = parts
    return `T${Number(month)}/${year}`
  }
  return dateString
}

function formatCompactValue(value) {
  if (Math.abs(value) >= 1000) {
    return new Intl.NumberFormat('vi-VN', {
      notation: 'compact',
      compactDisplay: 'short',
      maximumFractionDigits: 1,
    }).format(value)
  }

  return value.toLocaleString('vi-VN')
}

const chartOption = computed(() => {
  const axisLabels = props.stats.map((item) => formatMonth(item.month))

  const isRevenue = props.type === 'revenue'
  const colors = isRevenue ? ['#10b981'] : ['#18181b', '#71717a', '#0ea5e9']

  let series = []
  if (isRevenue) {
    series = [
      {
        name: 'Doanh thu',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        areaStyle: { opacity: 0.03 },
        data: props.stats.map((item) => item.revenue || 0),
      }
    ]
  } else {
    series = [
      {
        name: 'Người dùng',
        type: 'bar',
        barMaxWidth: 12,
        barGap: '30%',
        itemStyle: { borderRadius: [4, 4, 0, 0] },
        data: props.stats.map((item) => item.newUsers),
      },
      {
        name: 'Sách mới',
        type: 'bar',
        barMaxWidth: 12,
        itemStyle: { borderRadius: [4, 4, 0, 0] },
        data: props.stats.map((item) => item.newBooks),
      },
      {
        name: 'Bình luận',
        type: 'bar',
        barMaxWidth: 12,
        itemStyle: { borderRadius: [4, 4, 0, 0] },
        data: props.stats.map((item) => item.comments),
      },
    ]
  }

  return {
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    color: colors,
    grid: {
      left: 0,
      right: 0,
      top: 40,
      bottom: 0,
      containLabel: true,
    },
    legend: {
      top: 0,
      left: 0,
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        color: '#71717a',
        fontFamily: 'Satoshi, sans-serif',
        fontSize: 12,
        fontWeight: 500
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.05)',
          width: 2,
          type: 'solid'
        },
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: 'rgba(226, 232, 240, 0.8)',
      borderWidth: 1,
      padding: [12, 16],
      textStyle: {
        color: '#09090b',
        fontFamily: 'Satoshi, sans-serif',
        fontSize: 13,
        fontWeight: 500
      },
      borderRadius: 12,
      extraCssText: 'box-shadow: 0 10px 30px -10px rgba(0,0,0,0.1);',
      valueFormatter: (value) => {
        if (typeof value !== 'number') return String(value)
        return isRevenue ? value.toLocaleString('vi-VN') + ' VNĐ' : value.toLocaleString('vi-VN')
      },
    },
    xAxis: {
      type: 'category',
      boundaryGap: true,
      data: axisLabels,
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: {
        color: '#71717a',
        fontFamily: 'Satoshi, sans-serif',
        fontSize: 12,
        fontWeight: 500,
        margin: 16
      },
    },
    yAxis: {
      type: 'value',
      splitNumber: 4,
      axisLabel: {
        color: '#a1a1aa',
        fontFamily: 'Satoshi, sans-serif',
        fontSize: 12,
        formatter: (value) => formatCompactValue(value),
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.04)',
          type: 'dashed'
        },
      },
    },
    series,
  }
})
</script>

<template>
  <div class="dashboard-bar-chart">
    <div v-if="loading" class="dashboard-bar-chart__loading" aria-live="polite">
      <div class="dashboard-bar-chart__skeleton dashboard-bar-chart__skeleton--header"></div>
      <div class="dashboard-bar-chart__skeleton dashboard-bar-chart__skeleton--plot"></div>
    </div>

    <div v-else-if="error" class="dashboard-bar-chart__state dashboard-bar-chart__state--error">
      <p>{{ error }}</p>
      <button type="button" @click="emit('retry')">Thử lại</button>
    </div>

    <div v-else-if="!stats.length" class="dashboard-bar-chart__state">
      <p>Không có dữ liệu</p>
    </div>

    <div v-else class="chart-container">
      <VChart
        class="dashboard-bar-chart__canvas"
        :option="chartOption"
        autoresize
      />
    </div>
  </div>
</template>

<style scoped>
.dashboard-bar-chart {
  min-height: 24rem;
  width: 100%;
  height: 100%;
}

.chart-container {
  position: relative;
  width: 100%;
  height: 24rem; /* Ensures the container has a fixed height for ECharts */
}

.dashboard-bar-chart__canvas {
  width: 100%;
  height: 100%;
}

.dashboard-bar-chart__loading {
  display: grid;
  gap: 1rem;
}

.dashboard-bar-chart__skeleton {
  border-radius: 1.5rem;
  background:
    linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent),
    #f4f4f5;
  background-size: 200% 100%;
  animation: chart-shimmer 1.5s infinite;
}

.dashboard-bar-chart__skeleton--header {
  height: 2.5rem;
  width: 40%;
  border-radius: 99px;
}

.dashboard-bar-chart__skeleton--plot {
  min-height: 20rem;
}

.dashboard-bar-chart__state {
  display: grid;
  place-items: center;
  gap: 1rem;
  min-height: 24rem;
  padding: 2rem;
  border: 1px dashed rgba(226, 232, 240, 0.8);
  border-radius: 2rem;
  background: #fafafa;
  text-align: center;
  color: #71717a;
  font-family: 'Satoshi', sans-serif;
  font-weight: 500;
}

.dashboard-bar-chart__state--error {
  background: #fef2f2;
  border-color: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.dashboard-bar-chart__state button {
  padding: 0.75rem 1.25rem;
  border-radius: 99px;
  background: #18181b;
  color: white;
  font-weight: 600;
  font-family: inherit;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.dashboard-bar-chart__state button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(0,0,0,0.2);
}

@keyframes chart-shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
</style>
