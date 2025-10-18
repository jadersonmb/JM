<template>
  <DataTable
    :title="t('payments.table.title')"
    :subtitle="t('payments.table.subtitle')"
    :columns="visibleColumns"
    :rows="rows"
    :loading="loading"
    :selected="localSelected"
    :selectable="true"
    :sort="sort"
    :pagination="pagination"
    :per-page-options="[10, 25, 50, 100]"
    id-field="id"
    @update:selected="(value) => emit('update:selected', value)"
    @change:sort="(value) => emit('change:sort', value)"
    @change:page="(value) => emit('change:page', value)"
    @change:per-page="(value) => emit('change:per-page', value)"
    @change:columns="updateVisibleColumns"
    @refresh="emit('refresh')"
  >
    <template #filters>
      <label class="flex flex-col gap-1">
        <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.table.filters.search') }}</span>
        <input
          v-model="filtersLocal.search"
          type="search"
          class="input"
          :placeholder="t('payments.table.filters.searchPlaceholder')"
        />
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.table.filters.status') }}</span>
        <select v-model="filtersLocal.status" class="input">
          <option value="">{{ t('payments.table.filters.statusAll') }}</option>
          <option v-for="option in statusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.table.filters.method') }}</span>
        <select v-model="filtersLocal.paymentMethod" class="input">
          <option value="">{{ t('payments.table.filters.methodAll') }}</option>
          <option v-for="option in methodOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
      </label>
      <div class="grid grid-cols-2 gap-2">
        <label class="flex flex-col gap-1">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.table.filters.from') }}</span>
          <input v-model="filtersLocal.startDate" type="date" class="input" />
        </label>
        <label class="flex flex-col gap-1">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.table.filters.to') }}</span>
          <input v-model="filtersLocal.endDate" type="date" class="input" />
        </label>
      </div>
    </template>

    <template #toolbar="{ selected, refresh }">
      <button type="button" class="btn-secondary" :disabled="!rows.length" @click="emit('export:csv')">
        <ArrowDownTrayIcon class="h-4 w-4" />
        <span>{{ t('payments.table.toolbar.export') }}</span>
      </button>
      <button
        type="button"
        class="btn-secondary"
        :disabled="!selected.length"
        @click="emit('bulk:refund', selected)"
      >
        <ArrowUturnLeftIcon class="h-4 w-4" />
        <span>{{ t('payments.table.toolbar.refundSelected') }}</span>
      </button>
      <button type="button" class="btn-secondary" @click="refresh()">
        <ArrowPathIcon class="h-4 w-4" />
        <span>{{ t('payments.table.toolbar.refresh') }}</span>
      </button>
    </template>

    <template #row="{ row }">
      <td class="whitespace-nowrap px-4 py-3 font-mono text-xs text-slate-500">{{ row.paymentId ?? '' }}</td>
      <td class="px-4 py-3 text-sm text-slate-700">{{ row.description || '' }}</td>
      <td class="px-4 py-3 text-sm text-slate-700">{{ paymentMethodLabel(row.paymentMethod) }}</td>
      <td class="px-4 py-3 text-sm text-slate-700">
        <span :class="statusBadge(row.status)" class="rounded-full px-2 py-1 text-xs font-semibold uppercase">
          {{ paymentStatusLabel(row.status) }}
        </span>
      </td>
      <td class="px-4 py-3 text-sm text-slate-700">{{ formatCurrency(row.amount) }}</td>
      <td class="px-4 py-3 text-xs text-slate-500">{{ formatDate(row.createdAt) }}</td>
      <td class="px-4 py-3 text-xs text-slate-500">{{ formatDate(row.updatedAt) }}</td>
    </template>

    <template #actions="{ row }">
      <div class="flex items-center gap-2">
        <button type="button" class="btn-ghost" :title="t('payments.table.actions.view')" @click="emit('view', row)">
          <EyeIcon class="h-4 w-4" />
        </button>
        <button
          type="button"
          class="btn-ghost text-rose-500"
          :title="t('payments.table.actions.refund')"
          @click="emit('refund', row)"
        >
          <ArrowUturnLeftIcon class="h-4 w-4" />
        </button>
      </div>
    </template>
  </DataTable>
</template>

<script setup>
import { computed, reactive, watch } from 'vue';
import {
  ArrowDownTrayIcon,
  ArrowPathIcon,
  ArrowUturnLeftIcon,
  EyeIcon,
} from '@heroicons/vue/24/outline';
import DataTable from '@/components/DataTable.vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  pagination: {
    type: Object,
    default: () => ({ page: 1, perPage: 10, total: 0, lastPage: 1, from: 0, to: 0 }),
  },
  sort: {
    type: Object,
    default: () => ({ field: 'createdAt', direction: 'desc' }),
  },
  selected: {
    type: Array,
    default: () => [],
  },
  filters: {
    type: Object,
    default: () => ({ search: '', status: '', paymentMethod: '', startDate: '', endDate: '' }),
  },
});

const emit = defineEmits([
  'update:filters',
  'update:selected',
  'change:sort',
  'change:page',
  'change:per-page',
  'change:columns',
  'refresh',
  'bulk:refund',
  'export:csv',
  'view',
  'refund',
]);

const { t, te, locale } = useI18n();

const columnDefinitions = [
  { key: 'paymentId', labelKey: 'payments.table.columns.paymentId', visible: true, sortable: true },
  { key: 'description', labelKey: 'payments.table.columns.description', visible: true, sortable: true },
  { key: 'paymentMethod', labelKey: 'payments.table.columns.method', visible: true, sortable: true },
  { key: 'status', labelKey: 'payments.table.columns.status', visible: true, sortable: true },
  { key: 'amount', labelKey: 'payments.table.columns.amount', visible: true, sortable: true },
  { key: 'createdAt', labelKey: 'payments.table.columns.createdAt', visible: true, sortable: true },
  { key: 'updatedAt', labelKey: 'payments.table.columns.updatedAt', visible: false, sortable: true },
];

const visibleColumns = reactive(
  columnDefinitions.map((column) => ({
    ...column,
    label: t(column.labelKey),
  })),
);
const filtersLocal = reactive({ ...props.filters });
const localSelected = computed(() => props.selected);

const statusValues = ['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED'];
const methodValues = ['CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BANK_SLIP'];

const statusOptions = computed(() =>
  statusValues.map((value) => ({
    value,
    label: paymentStatusLabel(value),
  })),
);

const methodOptions = computed(() =>
  methodValues.map((value) => ({
    value,
    label: paymentMethodLabel(value),
  })),
);

let debounceHandle;
watch(
  () => ({ ...filtersLocal }),
  (value) => {
    window.clearTimeout(debounceHandle);
    debounceHandle = window.setTimeout(() => {
      emit('update:filters', { ...value });
    }, 250);
  },
  { deep: true }
);

watch(
  () => props.filters,
  (value) => {
    Object.assign(filtersLocal, value);
  },
  { deep: true }
);

function updateVisibleColumns(columns) {
  const mapped = columns.map((column) => {
    const definition = columnDefinitions.find((item) => item.key === column.key);
    return {
      ...column,
      labelKey: definition?.labelKey ?? column.labelKey,
      label: definition ? t(definition.labelKey) : column.label,
    };
  });
  visibleColumns.splice(0, visibleColumns.length, ...mapped);
  emit('change:columns', columns);
}

function statusBadge(status) {
  switch ((status ?? '').toUpperCase()) {
    case 'COMPLETED':
      return 'bg-emerald-100 text-emerald-700';
    case 'FAILED':
      return 'bg-rose-100 text-rose-700';
    case 'REFUNDED':
      return 'bg-emerald-100 text-emerald-700';
    case 'PROCESSING':
      return 'bg-amber-100 text-amber-700';
    default:
      return 'bg-slate-200 text-slate-700';
  }
}

function paymentStatusLabel(status) {
  const key = `payments.statuses.${(status ?? '').toLowerCase()}`;
  return te(key) ? t(key) : status ?? '';
}

function paymentMethodLabel(method) {
  const key = `payments.methods.${(method ?? '').toLowerCase()}`;
  return te(key) ? t(key) : method ?? '';
}

function currentLocaleTag() {
  return locale.value === 'pt' ? 'pt-BR' : 'en-US';
}

function formatCurrency(value) {
  return new Intl.NumberFormat(currentLocaleTag(), {
    style: 'currency',
    currency: 'BRL',
  }).format(value ?? 0);
}

function formatDate(value) {
  if (!value) return '';
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
  } catch (error) {
    return value;
  }
}

watch(
  () => locale.value,
  () => {
    visibleColumns.forEach((column) => {
      const definition = columnDefinitions.find((item) => item.key === column.key);
      if (definition) {
        column.label = t(definition.labelKey);
      }
    });
  },
);
</script>

