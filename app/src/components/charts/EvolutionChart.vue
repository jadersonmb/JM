<template>
  <VueECharts
    v-if="points.length"
    :option="option"
    autoresize
    class="w-full"
    :style="{ height: chartHeight }"
  />
  <div v-else class="flex h-full items-center justify-center text-sm text-slate-400">
    {{ emptyMessage }}
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { use } from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import VueECharts from 'vue-echarts';

use([GridComponent, TooltipComponent, LegendComponent, LineChart, CanvasRenderer]);

const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
  height: {
    type: [Number, String],
    default: 240,
  },
  accentColor: {
    type: String,
    default: '#0ea5e9',
  },
  emptyMessage: {
    type: String,
    default: 'No data available.',
  },
});

const { locale } = useI18n();

const points = computed(() => {
  if (!Array.isArray(props.data)) {
    return [];
  }
  return props.data
    .map((item) => ({
      date: item?.date ? new Date(item.date) : null,
      measurement: typeof item?.measurement === 'number' ? item.measurement : null,
    }))
    .filter((item) => item.date instanceof Date && !Number.isNaN(item.date?.valueOf()));
});

const chartHeight = computed(() => {
  if (typeof props.height === 'number') {
    return `${props.height}px`;
  }
  return props.height;
});

const numberFormatter = computed(() => new Intl.NumberFormat(locale.value, {
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
}));

const dateFormatter = computed(() => new Intl.DateTimeFormat(locale.value, {
  month: 'short',
  day: 'numeric',
}));

const option = computed(() => {
  const categories = points.value.map((item) => dateFormatter.value.format(item.date));
  const values = points.value.map((item) => item.measurement);

  return {
    grid: { left: 16, right: 16, top: 24, bottom: 16, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#0f172a',
      borderColor: '#0f172a',
      textStyle: { color: '#f8fafc' },
      formatter: (params) => {
        if (!Array.isArray(params) || !params.length) {
          return '';
        }
        const [{ axisValue, data }] = params;
        if (data == null) {
          return axisValue;
        }
        return `${axisValue}<br/>${numberFormatter.value.format(data)}`;
      },
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: categories,
      axisLine: { lineStyle: { color: '#cbd5f5' } },
      axisLabel: { color: '#475569', fontSize: 12 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#475569', fontSize: 12 },
      splitLine: { lineStyle: { color: '#e2e8f0', type: 'dashed' } },
    },
    series: [
      {
        name: 'measurement',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        data: values,
        lineStyle: { width: 3, color: props.accentColor },
        itemStyle: { color: props.accentColor },
        areaStyle: {
          opacity: 0.15,
          color: props.accentColor,
        },
      },
    ],
  };
});
</script>
