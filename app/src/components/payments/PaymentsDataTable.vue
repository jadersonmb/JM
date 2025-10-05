<template>
  <DataTable
    title="Payments"
    subtitle="Monitor real-time transactions across card, PIX and recurring billing."
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
        <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">Search</span>
        <input v-model="filtersLocal.search" type="search" class="input" placeholder="Payment ID, description…" />
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">Status</span>
        <select v-model="filtersLocal.status" class="input">
          <option value="">All statuses</option>
          <option v-for="status in statusOptions" :key="status" :value="status">{{ status }}</option>
        </select>
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">Method</span>
        <select v-model="filtersLocal.paymentMethod" class="input">
          <option value="">Any method</option>
          <option v-for="method in methodOptions" :key="method" :value="method">{{ method }}</option>
        </select>
      </label>
      <div class="grid grid-cols-2 gap-2">
        <label class="flex flex-col gap-1">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">From</span>
          <input v-model="filtersLocal.startDate" type="date" class="input" />
        </label>
        <label class="flex flex-col gap-1">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">To</span>
          <input v-model="filtersLocal.endDate" type="date" class="input" />
        </label>
      </div>
    </template>

    <template #toolbar="{ selected, refresh }">
      <button type="button" class="btn-secondary" :disabled="!rows.length" @click="emit('export:csv')">
        <ArrowDownTrayIcon class="h-4 w-4" />
        <span>Export CSV</span>
      </button>
      <button
        type="button"
        class="btn-secondary"
        :disabled="!selected.length"
        @click="emit('bulk:refund', selected)"
      >
        <ArrowUturnLeftIcon class="h-4 w-4" />
        <span>Refund selected</span>
      </button>
      <button type="button" class="btn-secondary" @click="refresh()">
        <ArrowPathIcon class="h-4 w-4" />
        <span>Sync</span>
      </button>
    </template>

    <template #row="{ row }">
      <td class="whitespace-nowrap px-4 py-3 font-mono text-xs text-slate-500">{{ row.paymentId ?? '—' }}</td>
      <td class="px-4 py-3 text-sm text-slate-700">{{ row.description || '—' }}</td>
      <td class="px-4 py-3 text-sm text-slate-700">{{ row.paymentMethod }}</td>
      <td class="px-4 py-3 text-sm text-slate-700">
        <span :class="statusBadge(row.status)" class="rounded-full px-2 py-1 text-xs font-semibold uppercase">
          {{ row.status }}
        </span>
      </td>
      <td class="px-4 py-3 text-sm text-slate-700">{{ formatCurrency(row.amount) }}</td>
      <td class="px-4 py-3 text-xs text-slate-500">{{ formatDate(row.createdAt) }}</td>
      <td class="px-4 py-3 text-xs text-slate-500">{{ formatDate(row.updatedAt) }}</td>
    </template>

    <template #actions="{ row }">
      <div class="flex items-center gap-2">
        <button type="button" class="btn-ghost" @click="emit('view', row)">
          <EyeIcon class="h-4 w-4" />
        </button>
        <button type="button" class="btn-ghost text-rose-500" @click="emit('refund', row)">
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

const initialColumns = [
  { key: 'paymentId', label: 'Payment ID', visible: true, sortable: true },
  { key: 'description', label: 'Description', visible: true, sortable: true },
  { key: 'paymentMethod', label: 'Method', visible: true, sortable: true },
  { key: 'status', label: 'Status', visible: true, sortable: true },
  { key: 'amount', label: 'Amount', visible: true, sortable: true },
  { key: 'createdAt', label: 'Created at', visible: true, sortable: true },
  { key: 'updatedAt', label: 'Updated at', visible: false, sortable: true },
];

const visibleColumns = reactive(initialColumns.map((column) => ({ ...column })));
const filtersLocal = reactive({ ...props.filters });
const localSelected = computed(() => props.selected);

const statusOptions = ['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED'];
const methodOptions = ['CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BANK_SLIP'];

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
  visibleColumns.splice(0, visibleColumns.length, ...columns);
  emit('change:columns', columns);
}

function statusBadge(status) {
  switch ((status ?? '').toUpperCase()) {
    case 'COMPLETED':
      return 'bg-emerald-100 text-emerald-700';
    case 'FAILED':
      return 'bg-rose-100 text-rose-700';
    case 'REFUNDED':
      return 'bg-blue-100 text-blue-700';
    case 'PROCESSING':
      return 'bg-amber-100 text-amber-700';
    default:
      return 'bg-slate-200 text-slate-700';
  }
}

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value ?? 0);
}

function formatDate(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}
</script>

