<template>
  <div class="card flex h-full flex-col">
    <header
      v-if="hasHeader"
      class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between"
    >
      <div class="space-y-1">
        <h3 v-if="title" class="text-base font-semibold text-slate-900 dark:text-slate-100">
          {{ title }}
        </h3>
        <p v-if="subtitle" class="text-sm text-slate-500 dark:text-slate-400">
          {{ subtitle }}
        </p>
      </div>
      <div v-if="$slots.actions" class="flex items-center gap-2">
        <slot name="actions" />
      </div>
    </header>

    <div class="relative mt-6 flex-1">
      <div
        v-if="loading"
        class="absolute inset-0 z-10 flex items-center justify-center rounded-xl bg-white/70 backdrop-blur-sm dark:bg-slate-900/50"
      >
        <span class="text-sm font-medium text-slate-500 dark:text-slate-300">
          {{ loadingText }}
        </span>
      </div>

      <div
        v-if="empty && !loading"
        class="flex h-full items-center justify-center rounded-xl border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-400 dark:border-slate-700 dark:bg-slate-900/40 dark:text-slate-500"
      >
        {{ emptyMessage }}
      </div>

      <VueECharts
        v-else
        :option="option"
        autoresize
        class="w-full"
        :style="{ height: chartHeight }"
      />
    </div>

    <footer
      v-if="$slots.footer"
      class="mt-6 border-t border-slate-200 pt-4 text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400"
    >
      <slot name="footer" />
    </footer>
  </div>
</template>

<script setup>
import { computed, useSlots } from 'vue';
import VueECharts from 'vue-echarts';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { BarChart, LineChart, RadarChart } from 'echarts/charts';
import {
  DatasetComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  DataZoomComponent,
  ToolboxComponent,
  RadarComponent,
  MarkLineComponent,
  MarkPointComponent,
} from 'echarts/components';

use([
  DatasetComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  DataZoomComponent,
  ToolboxComponent,
  RadarComponent,
  MarkLineComponent,
  MarkPointComponent,
  BarChart,
  LineChart,
  RadarChart,
  CanvasRenderer,
]);

const slots = useSlots();

const props = defineProps({
  title: {
    type: String,
    default: '',
  },
  subtitle: {
    type: String,
    default: '',
  },
  option: {
    type: Object,
    default: () => ({}),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  height: {
    type: [Number, String],
    default: 320,
  },
  empty: {
    type: Boolean,
    default: false,
  },
  emptyMessage: {
    type: String,
    default: 'No data available.',
  },
  loadingText: {
    type: String,
    default: 'Loading data...',
  },
});

const hasHeader = computed(
  () => Boolean(props.title || props.subtitle || slots.actions),
);

const chartHeight = computed(() => {
  if (typeof props.height === 'number') {
    return `${props.height}px`;
  }
  return props.height;
});
</script>
