<template>
  <div class="space-y-6 lg:space-y-8">
    <section class="card">
      <div
        class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between"
      >
        <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-8">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('nutritionDashboard.filters.dateRange') }}
            </p>
            <div class="mt-2 flex flex-wrap gap-2">
              <button
                v-for="option in rangeOptions"
                :key="option.value"
                type="button"
                class="rounded-lg px-4 py-2 text-sm font-semibold transition"
                :class="option.value === filters.range
                  ? 'bg-primary-600 text-white shadow-soft'
                  : 'border border-slate-200 bg-white text-slate-600 hover:border-primary-200 hover:text-primary-600'"
                @click="filters.range = option.value"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <div>
            <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('nutritionDashboard.filters.groupBy') }}
            </label>
            <select
              v-model="filters.groupBy"
              class="input mt-2 w-48"
            >
              <option
                v-for="option in groupByOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-4">
          <div v-if="isAdmin" class="w-full md:w-64">
            <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('nutritionDashboard.filters.user') }}
            </label>
            <select
              v-model="filters.userId"
              class="input mt-2"
              :disabled="usersLoading"
            >
              <option value="">
                {{ t('nutritionDashboard.filters.allUsers') }}
              </option>
              <option
                v-for="user in userOptions"
                :key="user.value"
                :value="user.value"
              >
                {{ user.label }}
              </option>
            </select>
          </div>
          <button v-else
            type="button"
            class="btn-secondary self-start"
            :disabled="isRefreshing"
            @click="refresh"
          >
            <ArrowPathIcon
              class="h-4 w-4"
              :class="isRefreshing ? 'animate-spin' : ''"
            />
            <span>
              {{
                isRefreshing
                  ? t('nutritionDashboard.actions.refreshing')
                  : t('nutritionDashboard.actions.refresh')
              }}
            </span>
          </button>
        </div>
      </div>
    </section>

    <section
      class="grid gap-6 xl:grid-cols-2"
    >
      <BaseChart
        :title="t('nutritionDashboard.charts.goals.title')"
        :subtitle="t('nutritionDashboard.charts.goals.subtitle')"
        :option="goalsOption"
        :loading="chartState.goals.loading"
        :empty="goalsEmpty"
        :empty-message="t('nutritionDashboard.empty.default')"
        :height="360"
      />

      <BaseChart
        :title="t('nutritionDashboard.charts.macros.title')"
        :subtitle="t('nutritionDashboard.charts.macros.subtitle')"
        :option="macrosOption"
        :loading="chartState.macros.loading"
        :empty="macrosEmpty"
        :empty-message="t('nutritionDashboard.empty.default')"
        :height="360"
      />

      <BaseChart
        :title="t('nutritionDashboard.charts.hydration.title')"
        :subtitle="t('nutritionDashboard.charts.hydration.subtitle')"
        :option="hydrationOption"
        :loading="chartState.hydration.loading"
        :empty="hydrationEmpty"
        :empty-message="t('nutritionDashboard.empty.default')"
        :height="340"
      />

      <BaseChart
        :title="t('nutritionDashboard.charts.calories.title')"
        :subtitle="t('nutritionDashboard.charts.calories.subtitle')"
        :option="caloriesOption"
        :loading="chartState.macros.loading"
        :empty="caloriesEmpty"
        :empty-message="t('nutritionDashboard.empty.default')"
        :height="340"
      />

      <BaseChart
        :title="t('nutritionDashboard.charts.foods.title')"
        :subtitle="t('nutritionDashboard.charts.foods.subtitle')"
        :option="foodsOption"
        :loading="chartState.foods.loading"
        :empty="foodsEmpty"
        :empty-message="t('nutritionDashboard.empty.default')"
        :height="360"
      />

      <BaseChart
        class="xl:col-span-2"
        :title="t('nutritionDashboard.charts.body.title')"
        :subtitle="t('nutritionDashboard.charts.body.subtitle')"
        :option="bodyOption"
        :loading="chartState.body.loading"
        :empty="bodyEmpty"
        :empty-message="t('nutritionDashboard.empty.default')"
        :height="380"
      />
    </section>
  </div>
</template>

<script setup>
import { ArrowPathIcon } from '@heroicons/vue/24/outline';
import {
  computed,
  onMounted,
  reactive,
  ref,
  watch,
} from 'vue';
import { useI18n } from 'vue-i18n';
import BaseChart from '@/components/dashboard/BaseChart.vue';
import AnalyticsService from '@/services/AnalyticsService';
import { getUsers } from '@/services/users';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';

const METRIC_KEYS = ['protein', 'carbs', 'fat', 'water', 'fiber', 'calories'];

const auth = useAuthStore();
const notifications = useNotificationStore();
const { t, locale } = useI18n();

const isAdmin = computed(
  () => (auth.user?.type ?? '').toUpperCase() === 'ADMIN',
);

const filters = reactive({
  range: 7,
  groupBy: 'day',
  userId: '',
});

const isRefreshing = ref(false);
const usersLoading = ref(false);
const userOptions = ref([]);

const chartState = reactive({
  goals: {
    loading: false,
    data: [],
  },
  macros: {
    loading: false,
    data: [],
  },
  hydration: {
    loading: false,
    data: [],
  },
  body: {
    loading: false,
    data: [],
  },
  foods: {
    loading: false,
    data: [],
  },
});

const textColor = '#475569';
const subtleTextColor = '#64748b';
const gridLineColor = '#e2e8f0';

const rangeOptions = computed(() => ([
  { value: 7, label: t('nutritionDashboard.filters.last7Days') },
  { value: 30, label: t('nutritionDashboard.filters.last30Days') },
  { value: 90, label: t('nutritionDashboard.filters.last90Days') },
]));

const groupByOptions = computed(() => ([
  { value: 'day', label: t('nutritionDashboard.filters.groupByOptions.day') },
  { value: 'week', label: t('nutritionDashboard.filters.groupByOptions.week') },
  { value: 'month', label: t('nutritionDashboard.filters.groupByOptions.month') },
]));

const metricLabels = computed(() => METRIC_KEYS.map((key) => ({
  key,
  label: t(`nutritionDashboard.metrics.${key}`),
})));

const baseParams = computed(() => ({
  range: filters.range,
  groupBy: filters.groupBy,
  aggregation: filters.groupBy.toUpperCase(),
  userId: filters.userId || undefined,
}));

watch(
  () => auth.user,
  (user) => {
    if (!isAdmin.value && user?.id) {
      filters.userId = user.id;
    }
  },
  { immediate: true },
);

const extractArray = (payload) => {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.series)) return payload.series;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.items)) return payload.items;
  return [];
};

const getNumber = (source, key) => {
  if (!source || typeof source !== 'object') return undefined;
  const entry = Object.entries(source).find(
    ([currentKey]) => currentKey.toLowerCase() === key.toLowerCase(),
  );
  if (!entry) return undefined;
  const numeric = Number(entry[1]);
  return Number.isFinite(numeric) ? numeric : undefined;
};

const roundOne = (value) => {
  if (!Number.isFinite(value)) return 0;
  return Math.round(value * 10) / 10;
};

const formatAxisLabel = (value) => {
  if (!value) return t('nutritionDashboard.labels.unknown');
  const date = new Date(value);
  if (!Number.isNaN(date.getTime())) {
    const formatter = new Intl.DateTimeFormat(
      locale.value ?? 'en',
      filters.groupBy === 'month'
        ? { month: 'short', year: 'numeric' }
        : { month: 'short', day: 'numeric' },
    );
    return formatter.format(date);
  }
  return value;
};

const normalizeGoals = (payload) => {
  if (!payload) return [];
  const rows = extractArray(payload?.metrics ?? payload);
  if (rows.length) {
    return METRIC_KEYS.map((key) => {
      const match = rows.find((row) => {
        const identifier = String(
          row?.key ?? row?.metric ?? row?.name ?? '',
        ).toLowerCase();
        return identifier === key;
      }) ?? {};
      const target = Number(match?.target ?? match?.goal ?? match?.expected ?? 0) || 0;
      const achieved = Number(match?.achieved ?? match?.actual ?? match?.value ?? 0) || 0;
      const percentRaw = Number(match?.percent ?? match?.percentage ?? match?.adherence);
      const percent = Number.isFinite(percentRaw) ? percentRaw : (target > 0 ? (achieved / target) * 100 : 0);
      return {
        key,
        target,
        achieved,
        percent: roundOne(percent),
      };
    });
  }

  const targetSource = payload?.target ?? payload?.targets ?? payload?.goal ?? payload ?? {};
  const achievedSource = payload?.achieved ?? payload?.achievements ?? payload?.actual ?? payload?.progress ?? {};
  const percentSource = payload?.percent ?? payload?.percentages ?? payload?.adherence ?? payload?.completion ?? {};

  return METRIC_KEYS.map((key) => {
    const target = getNumber(targetSource, key) ?? 0;
    const achieved = getNumber(achievedSource, key) ?? 0;
    const percentRaw = getNumber(percentSource, key);
    const percent = percentRaw ?? (target > 0 ? (achieved / target) * 100 : 0);
    return {
      key,
      target,
      achieved,
      percent: roundOne(percent),
    };
  });
};

const normalizeMacros = (payload) => {
  const rows = extractArray(payload);
  return rows.map((row) => ({
    label: row?.label ?? row?.period ?? row?.date ?? row?.day ?? row?.week ?? row?.month ?? '',
    protein: Number(row?.protein ?? row?.proteins ?? row?.proteinGrams ?? 0) || 0,
    carbs: Number(row?.carbs ?? row?.carbohydrates ?? row?.carbGrams ?? 0) || 0,
    fat: Number(row?.fat ?? row?.fats ?? row?.lipids ?? row?.fatGrams ?? 0) || 0,
    calories: Number(row?.calories ?? row?.kcal ?? row?.energy ?? 0) || 0,
  }));
};

const normalizeHydration = (payload) => {
  const rows = extractArray(payload);
  return rows.map((row) => ({
    label: row?.label ?? row?.date ?? row?.period ?? '',
    intake: Number(row?.intake ?? row?.water ?? row?.consumed ?? row?.value ?? 0) || 0,
    target: Number(row?.target ?? row?.goal ?? row?.expected ?? 0) || 0,
  }));
};

const normalizeBody = (payload) => {
  const rows = extractArray(payload);
  return rows
    .map((row) => ({
      label: row?.label ?? row?.date ?? row?.recordedAt ?? row?.createdAt ?? '',
      weight: Number(row?.weight ?? row?.bodyWeight ?? 0) || 0,
      bmi: Number(row?.bmi ?? row?.bodyMassIndex ?? 0) || 0,
      fat: Number(row?.fat ?? row?.fatPercentage ?? row?.bodyFat ?? 0) || 0,
      muscle: Number(row?.muscle ?? row?.musclePercentage ?? row?.muscleMass ?? 0) || 0,
    }))
    .sort((a, b) => {
      const left = new Date(a.label).getTime();
      const right = new Date(b.label).getTime();
      if (Number.isNaN(left) || Number.isNaN(right)) return 0;
      return left - right;
    });
};

const normalizeFoods = (payload) => {
  const rows = extractArray(payload);
  return rows
    .map((row) => ({
      name: row?.name ?? row?.food ?? row?.item ?? '',
      quantity: Number(row?.quantity ?? row?.amount ?? row?.total ?? 0) || 0,
      unit: row?.unit ?? row?.measurement ?? row?.measure ?? '',
    }))
    .filter((row) => row.name)
    .sort((a, b) => b.quantity - a.quantity)
    .slice(0, 10);
};

const notifyError = (error) => {
  const fallback = error?.response?.data?.message ?? t('nutritionDashboard.messages.genericError');
  notifications.push({
    type: 'error',
    title: t('nutritionDashboard.messages.loadFailed'),
    message: fallback,
  });
  console.error('[NutritionDashboard]', error);
};

const fetchGoals = async () => {
  chartState.goals.loading = true;
  try {
    const { data } = await AnalyticsService.getGoalsAdherence({
      ...baseParams.value,
    });
    chartState.goals.data = normalizeGoals(data);
  } catch (error) {
    chartState.goals.data = [];
    notifyError(error);
  } finally {
    chartState.goals.loading = false;
  }
};

const fetchMacros = async () => {
  chartState.macros.loading = true;
  try {
    const { data } = await AnalyticsService.getMacros({
      ...baseParams.value,
      includeCalories: true,
    });
    chartState.macros.data = normalizeMacros(data);
  } catch (error) {
    chartState.macros.data = [];
    notifyError(error);
  } finally {
    chartState.macros.loading = false;
  }
};

const fetchHydration = async () => {
  chartState.hydration.loading = true;
  try {
    const { data } = await AnalyticsService.getHydration({
      ...baseParams.value,
    });
    chartState.hydration.data = normalizeHydration(data);
  } catch (error) {
    chartState.hydration.data = [];
    notifyError(error);
  } finally {
    chartState.hydration.loading = false;
  }
};

const fetchBody = async () => {
  chartState.body.loading = true;
  try {
    const { data } = await AnalyticsService.getBodyComposition({
      ...baseParams.value,
    });
    chartState.body.data = normalizeBody(data);
  } catch (error) {
    chartState.body.data = [];
    notifyError(error);
  } finally {
    chartState.body.loading = false;
  }
};

const fetchFoods = async () => {
  chartState.foods.loading = true;
  try {
    const { data } = await AnalyticsService.getTopFoods({
      ...baseParams.value,
      limit: 10,
    });
    chartState.foods.data = normalizeFoods(data);
  } catch (error) {
    chartState.foods.data = [];
    notifyError(error);
  } finally {
    chartState.foods.loading = false;
  }
};

const fetchUsersList = async () => {
  if (!isAdmin.value) return;
  usersLoading.value = true;
  try {
    const { data } = await getUsers({
      page: 0,
      size: 50,
      sort: 'name,asc',
    });
    const items = extractArray(data);
    userOptions.value = items.map((item) => ({
      value: item?.id ?? item?.userId ?? '',
      label: item?.name ?? item?.fullName ?? item?.email ?? `#${item?.id}`,
    })).filter((item) => item.value);
  } catch (error) {
    userOptions.value = [];
    notifyError(error);
  } finally {
    usersLoading.value = false;
  }
};

const refresh = async () => {
  if (isRefreshing.value) return;
  isRefreshing.value = true;
  await Promise.allSettled([
    fetchGoals(),
    fetchMacros(),
    fetchHydration(),
    fetchBody(),
    fetchFoods(),
  ]);
  isRefreshing.value = false;
};

watch(
  () => [filters.range, filters.groupBy, filters.userId],
  () => {
    refresh();
  },
  { immediate: true },
);

onMounted(() => {
  fetchUsersList();
});

const goalsEmpty = computed(() => {
  if (!chartState.goals.data.length) return true;
  return chartState.goals.data.every(
    (metric) => metric.target === 0 && metric.achieved === 0,
  );
});

const macrosEmpty = computed(() => {
  if (!chartState.macros.data.length) return true;
  return chartState.macros.data.every(
    (row) => row.protein === 0 && row.carbs === 0 && row.fat === 0,
  );
});

const hydrationEmpty = computed(() => {
  if (!chartState.hydration.data.length) return true;
  return chartState.hydration.data.every(
    (row) => row.intake === 0 && row.target === 0,
  );
});

const caloriesEmpty = computed(() => {
  if (!chartState.macros.data.length) return true;
  return chartState.macros.data.every(
    (row) => row.calories === 0,
  );
});

const foodsEmpty = computed(() => chartState.foods.data.length === 0);

const bodyEmpty = computed(() => {
  if (!chartState.body.data.length) return true;
  return chartState.body.data.every(
    (row) => row.weight === 0 && row.bmi === 0 && row.fat === 0 && row.muscle === 0,
  );
});

const goalsOption = computed(() => {
  const metrics = chartState.goals.data.length
    ? chartState.goals.data
    : METRIC_KEYS.map((key) => ({
      key,
      target: 0,
      achieved: 0,
      percent: 0,
    }));
  const { targetLabel, achievedLabel } = {
    targetLabel: t('nutritionDashboard.charts.goals.target'),
    achievedLabel: t('nutritionDashboard.charts.goals.achieved'),
  };

  const maxValue = metrics.reduce(
    (max, metric) => Math.max(max, metric.target, metric.achieved),
    0,
  );

  const indicators = metricLabels.value.map((metric, index) => ({
    name: metric.label,
    max: maxValue > 0 ? Math.ceil(maxValue * 1.2) : 100,
  }));

  const percentMap = metrics.reduce((acc, metric) => ({
    ...acc,
    [metric.key]: metric.percent,
  }), {});

  return {
    color: ['#93c5fd', '#2563eb'],
    tooltip: {
      trigger: 'item',
      backgroundColor: '#0f172a',
      borderColor: '#1e293b',
      formatter: () => metricLabels.value.map((metric) => {
        const values = metrics.find((item) => item.key === metric.key) ?? {};
        const percent = percentMap[metric.key] ?? 0;
        const percentText = `${roundOne(percent)}%`;
        const target = values.target?.toLocaleString();
        const achieved = values.achieved?.toLocaleString();
        return `<div style="margin-bottom:4px;">
          <strong>${metric.label}</strong><br />
          ${t('nutritionDashboard.charts.goals.target')}: ${target ?? 0}<br />
          ${t('nutritionDashboard.charts.goals.achieved')}: ${achieved ?? 0}<br />
          ${t('nutritionDashboard.labels.percentAchieved')}: ${percentText}
        </div>`;
      }).join(''),
    },
    legend: {
      data: [targetLabel, achievedLabel],
      textStyle: {
        color: textColor,
      },
    },
    radar: {
      indicator: indicators,
      splitNumber: 5,
      splitLine: {
        lineStyle: {
          color: '#cbd5f5',
        },
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(59,130,246,0.08)', 'rgba(37,99,235,0.04)'],
        },
      },
      axisLine: {
        lineStyle: {
          color: '#bfdbfe',
        },
      },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: metrics.map((metric) => metric.target),
            name: targetLabel,
            lineStyle: { color: '#a5b4fc' },
            areaStyle: { opacity: 0.3 },
          },
          {
            value: metrics.map((metric) => metric.achieved),
            name: achievedLabel,
            lineStyle: { color: '#2563eb' },
            areaStyle: { opacity: 0.2 },
          },
        ],
      },
    ],
  };
});

const macrosOption = computed(() => {
  const rows = chartState.macros.data;
  const categories = rows.map((row) => formatAxisLabel(row.label));
  const proteinLabel = t('nutritionDashboard.metrics.protein');
  const carbsLabel = t('nutritionDashboard.metrics.carbs');
  const fatLabel = t('nutritionDashboard.metrics.fat');

  return {
    color: ['#2563eb', '#38bdf8', '#fb923c'],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#0f172a',
      borderColor: '#1e293b',
      formatter: (param) => {
        if (!Array.isArray(param)) return '';
        const total = param.reduce((sum, item) => sum + (Number(item.value) || 0), 0);
        const lines = param
          .map((item) => `${item.marker} ${item.seriesName}: ${Number(item.value ?? 0).toLocaleString()} g`);
        lines.push(`<span style="display:block;margin-top:4px;">${t('nutritionDashboard.metrics.total')}: ${total.toLocaleString()} g</span>`);
        return `<strong>${param[0]?.axisValueLabel ?? ''}</strong><br />${lines.join('<br />')}`;
      },
    },
    legend: {
      data: [proteinLabel, carbsLabel, fatLabel],
      textStyle: { color: textColor },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: categories,
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
    },
    yAxis: {
      type: 'value',
      name: t('nutritionDashboard.labels.grams'),
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
      splitLine: { lineStyle: { color: gridLineColor } },
    },
    series: [
      {
        name: proteinLabel,
        type: 'line',
        smooth: true,
        stack: 'macros',
        areaStyle: { opacity: 0.18 },
        emphasis: { focus: 'series' },
        data: rows.map((row) => row.protein),
      },
      {
        name: carbsLabel,
        type: 'line',
        smooth: true,
        stack: 'macros',
        areaStyle: { opacity: 0.18 },
        emphasis: { focus: 'series' },
        data: rows.map((row) => row.carbs),
      },
      {
        name: fatLabel,
        type: 'line',
        smooth: true,
        stack: 'macros',
        areaStyle: { opacity: 0.18 },
        emphasis: { focus: 'series' },
        data: rows.map((row) => row.fat),
      },
    ],
  };
});

const hydrationOption = computed(() => {
  const rows = chartState.hydration.data;
  const categories = rows.map((row) => formatAxisLabel(row.label));
  const intakeLabel = t('nutritionDashboard.charts.hydration.intake');
  const targetLabel = t('nutritionDashboard.charts.hydration.target');

  return {
    color: ['#38bdf8', '#2563eb'],
    tooltip: {
      trigger: 'axis',
      valueFormatter: (value) => `${Number(value ?? 0).toLocaleString()} ml`,
    },
    legend: {
      data: [intakeLabel, targetLabel],
      textStyle: { color: textColor },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
    },
    yAxis: {
      type: 'value',
      name: t('nutritionDashboard.labels.milliliters'),
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
      splitLine: { lineStyle: { color: gridLineColor } },
    },
    series: [
      {
        name: intakeLabel,
        type: 'bar',
        data: rows.map((row) => row.intake),
        barWidth: '45%',
        itemStyle: { borderRadius: [6, 6, 0, 0] },
      },
      {
        name: targetLabel,
        type: 'line',
        data: rows.map((row) => row.target),
        smooth: true,
        lineStyle: { width: 3 },
        symbol: 'circle',
        symbolSize: 8,
      },
    ],
  };
});

const caloriesOption = computed(() => {
  const rows = chartState.macros.data;
  const categories = rows.map((row) => formatAxisLabel(row.label));
  const caloriesLabel = t('nutritionDashboard.metrics.calories');

  return {
    color: ['#f97316'],
    tooltip: {
      trigger: 'axis',
      valueFormatter: (value) => `${Number(value ?? 0).toLocaleString()} ${t('nutritionDashboard.labels.kcal')}`,
    },
    legend: {
      data: [caloriesLabel],
      textStyle: { color: textColor },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: categories,
      boundaryGap: false,
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
    },
    yAxis: {
      type: 'value',
      name: t('nutritionDashboard.labels.kcal'),
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
      splitLine: { lineStyle: { color: gridLineColor } },
    },
    series: [
      {
        name: caloriesLabel,
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.1 },
        lineStyle: { width: 3 },
        symbol: 'circle',
        symbolSize: 6,
        data: rows.map((row) => row.calories),
      },
    ],
  };
});

const foodsOption = computed(() => {
  const rows = chartState.foods.data;
  const categories = rows.map((row) => row.name).reverse();
  const values = rows.map((row) => row.quantity).reverse();
  const quantityLabel = t('nutritionDashboard.charts.foods.quantity');

  return {
    color: ['#22c55e'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        if (!Array.isArray(params) || !params.length) return '';
        return params.map((item) => {
          const original = rows.find((row) => row.name === item.axisValue);
          const unit = original?.unit ?? 'g';
          return `${item.marker} ${item.axisValue}: ${Number(item.value ?? 0).toLocaleString()} ${unit}`;
        }).join('<br />');
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
      splitLine: { lineStyle: { color: gridLineColor } },
    },
    yAxis: {
      type: 'category',
      data: categories,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
    },
    series: [
      {
        name: quantityLabel,
        type: 'bar',
        data: values,
        barWidth: '50%',
        itemStyle: { borderRadius: [0, 8, 8, 0] },
      },
    ],
  };
});

const bodyOption = computed(() => {
  const rows = chartState.body.data;
  const categories = rows.map((row) => formatAxisLabel(row.label));
  const weightLabel = t('nutritionDashboard.charts.body.weight');
  const bmiLabel = t('nutritionDashboard.charts.body.bmi');
  const fatLabel = t('nutritionDashboard.charts.body.fat');
  const muscleLabel = t('nutritionDashboard.charts.body.muscle');

  return {
    color: ['#2563eb', '#38bdf8', '#f43f5e', '#22c55e'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' },
    },
    legend: {
      data: [weightLabel, bmiLabel, fatLabel, muscleLabel],
      textStyle: { color: textColor },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: gridLineColor } },
      axisLabel: { color: subtleTextColor },
    },
    yAxis: [
      {
        type: 'value',
        name: `${t('nutritionDashboard.labels.weightKg')} / ${t('nutritionDashboard.charts.body.bmi')}`,
        axisLine: { lineStyle: { color: gridLineColor } },
        axisLabel: { color: subtleTextColor },
        splitLine: { lineStyle: { color: gridLineColor } },
      },
      {
        type: 'value',
        name: t('nutritionDashboard.labels.percent'),
        axisLine: { lineStyle: { color: gridLineColor } },
        axisLabel: { color: subtleTextColor },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: weightLabel,
        type: 'line',
        smooth: true,
        data: rows.map((row) => row.weight),
        symbol: 'circle',
        symbolSize: 6,
        yAxisIndex: 0,
      },
      {
        name: bmiLabel,
        type: 'line',
        smooth: true,
        data: rows.map((row) => row.bmi),
        symbol: 'circle',
        symbolSize: 6,
        yAxisIndex: 0,
      },
      {
        name: fatLabel,
        type: 'line',
        smooth: true,
        data: rows.map((row) => row.fat),
        symbol: 'circle',
        symbolSize: 6,
        yAxisIndex: 1,
      },
      {
        name: muscleLabel,
        type: 'line',
        smooth: true,
        data: rows.map((row) => row.muscle),
        symbol: 'circle',
        symbolSize: 6,
        yAxisIndex: 1,
      },
    ],
  };
});
</script>
