<template>
  <section class="card space-y-6">
    <header class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">{{ title }}</h2>
        <p class="text-sm text-slate-500">{{ subtitle }}</p>
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <slot name="toolbar" :selected="localSelected" :refresh="emitRefresh" />
        <button type="button" class="btn-secondary" @click="emitRefresh">
          <ArrowPathIcon class="h-4 w-4" />
          <span>Refresh</span>
        </button>
        <button type="button" class="btn-secondary" @click="columnPanelOpen = !columnPanelOpen">
          <AdjustmentsHorizontalIcon class="h-4 w-4" />
          <span>Columns</span>
        </button>
      </div>
    </header>

    <div class="grid gap-4 md:grid-cols-4">
      <slot name="filters" />
    </div>

    <transition name="fade">
      <div v-if="columnPanelOpen" class="rounded-xl border border-slate-200 bg-slate-50 p-4">
        <h3 class="text-sm font-semibold text-slate-700">Visible columns</h3>
        <div class="mt-3 grid gap-3 sm:grid-cols-2 md:grid-cols-3">
          <label
            v-for="column in localColumns"
            :key="column.key"
            class="flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-600 shadow-sm"
          >
            <input type="checkbox" v-model="column.visible" @change="emitColumns" />
            {{ column.label }}
          </label>
        </div>
      </div>
    </transition>

    <div class="overflow-x-auto rounded-2xl border border-slate-200">
      <table class="min-w-full divide-y divide-slate-200 text-left text-sm">
        <thead class="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
          <tr>
            <th v-if="selectable" class="w-12 px-4 py-3">
              <input
                type="checkbox"
                :checked="allVisibleSelected"
                :indeterminate="indeterminate"
                @change="toggleAll"
              />
            </th>
            <th v-for="column in visibleColumns" :key="column.key" class="px-4 py-3">
              <button
                v-if="column.sortable"
                type="button"
                class="flex items-center gap-1 text-slate-600 transition hover:text-slate-900"
                @click="toggleSort(column.key)"
              >
                {{ column.label }}
                <ChevronUpIcon
                  v-if="sort.field === column.key"
                  :class="['h-4 w-4 transition-transform', sort.direction === 'desc' ? 'rotate-180' : '']"
                />
              </button>
              <span v-else>{{ column.label }}</span>
            </th>
            <th v-if="$slots.actions" class="px-4 py-3 text-slate-500">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-200 bg-white">
          <tr v-if="loading">
            <td :colspan="tableColspan" class="px-4 py-12 text-center text-sm text-slate-500">
              <span class="inline-flex items-center gap-3 text-slate-500">
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
                Loading data...
              </span>
            </td>
          </tr>
          <template v-else>
            <tr v-if="!rows.length">
              <td :colspan="tableColspan" class="px-4 py-12 text-center text-sm text-slate-500">
                {{ emptyState }}
              </td>
            </tr>
            <tr
              v-for="row in rows"
              :key="row[idField]"
              class="transition hover:bg-slate-50"
            >
              <td v-if="selectable" class="px-4 py-3">
                <input
                  type="checkbox"
                  :value="row[idField]"
                  v-model="localSelected"
                  @change="emitSelected"
                />
              </td>
              <td v-for="column in visibleColumns" :key="column.key" class="px-4 py-3 align-top text-sm text-slate-600">
                <slot :name="`cell:${column.key}`" :row="row">
                  {{ row[column.key] ?? '-' }}
                </slot>
              </td>
              <td v-if="$slots.actions" class="px-4 py-3">
                <slot name="actions" :row="row" />
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <footer class="flex flex-col gap-4 border-t border-slate-200 pt-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="text-sm text-slate-500">
        Showing
        <span class="font-semibold text-slate-700">{{ meta.from }}-{{ meta.to }}</span>
        of
        <span class="font-semibold text-slate-700">{{ meta.total }}</span>
      </div>
      <div class="flex flex-wrap items-center gap-3">
        <label class="flex items-center gap-2 text-sm text-slate-500">
          Rows per page
          <select v-model.number="localPerPage" class="input h-9 w-24" @change="emitPerPage">
            <option v-for="size in perPageOptions" :key="size" :value="size">{{ size }}</option>
          </select>
        </label>
        <nav class="flex items-center gap-2">
          <button type="button" class="btn-secondary" :disabled="meta.page <= 1" @click="emitPage(meta.page - 1)">
            Prev
          </button>
          <span class="text-sm font-semibold text-slate-500">Page {{ meta.page }} / {{ meta.lastPage }}</span>
          <button
            type="button"
            class="btn-secondary"
            :disabled="meta.page >= meta.lastPage"
            @click="emitPage(meta.page + 1)"
          >
            Next
          </button>
        </nav>
      </div>
    </footer>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch, useSlots } from 'vue';
import {
  AdjustmentsHorizontalIcon,
  ArrowPathIcon,
  ChevronUpIcon,
} from '@heroicons/vue/24/outline';

const props = defineProps({
  title: { type: String, default: 'Data table' },
  subtitle: { type: String, default: '' },
  columns: {
    type: Array,
    default: () => [],
  },
  rows: {
    type: Array,
    default: () => [],
  },
  loading: { type: Boolean, default: false },
  selectable: { type: Boolean, default: true },
  selected: {
    type: Array,
    default: () => [],
  },
  sort: {
    type: Object,
    default: () => ({ field: '', direction: 'asc' }),
  },
  pagination: {
    type: Object,
    default: () => ({ page: 1, perPage: 10, total: 0, lastPage: 1, from: 0, to: 0 }),
  },
  emptyState: {
    type: String,
    default: 'No records found.',
  },
  idField: {
    type: String,
    default: 'id',
  },
  perPageOptions: {
    type: Array,
    default: () => [10, 25, 50, 100],
  },
});

const emit = defineEmits(['update:selected', 'change:sort', 'change:page', 'change:per-page', 'change:columns', 'refresh']);
const slots = useSlots();

const localColumns = reactive(props.columns.map((column) => ({ ...column })));
const localSelected = ref([...props.selected]);
const localPerPage = ref(props.pagination.perPage || 10);
const columnPanelOpen = ref(false);

watch(
  () => props.columns,
  (value) => {
    localColumns.splice(0, localColumns.length, ...value.map((column) => ({ ...column })));
  },
  { deep: true }
);

watch(
  () => props.selected,
  (value) => {
    localSelected.value = [...value];
  }
);

watch(
  () => props.pagination.perPage,
  (value) => {
    if (value) localPerPage.value = value;
  }
);

const visibleColumns = computed(() => localColumns.filter((column) => column.visible !== false));
const tableColspan = computed(() => visibleColumns.value.length + (props.selectable ? 1 : 0) + (slots.actions ? 1 : 0));

const meta = computed(() => ({
  page: props.pagination.page ?? 1,
  perPage: props.pagination.perPage ?? localPerPage.value,
  total: props.pagination.total ?? 0,
  lastPage: props.pagination.lastPage ?? 1,
  from: props.pagination.from ?? 0,
  to: props.pagination.to ?? 0,
}));

const allVisibleSelected = computed(() => {
  if (!props.selectable || !props.rows.length) return false;
  const ids = props.rows.map((row) => row[props.idField]);
  return ids.every((id) => localSelected.value.includes(id));
});

const indeterminate = computed(() => {
  if (!props.selectable) return false;
  const ids = props.rows.map((row) => row[props.idField]);
  const selectedCount = ids.filter((id) => localSelected.value.includes(id)).length;
  return selectedCount > 0 && selectedCount < ids.length;
});

const sort = computed(() => props.sort ?? { field: '', direction: 'asc' });

const toggleAll = (event) => {
  if (event.target.checked) {
    localSelected.value = props.rows.map((row) => row[props.idField]);
  } else {
    localSelected.value = [];
  }
  emitSelected();
};

const toggleSort = (field) => {
  let direction = 'asc';
  if (sort.value.field === field) {
    direction = sort.value.direction === 'asc' ? 'desc' : 'asc';
  }
  emit('change:sort', { field, direction });
};

const emitSelected = () => {
  emit('update:selected', [...localSelected.value]);
};

const emitColumns = () => {
  emit('change:columns', localColumns.map((column) => ({ ...column })));
};

const emitPage = (page) => {
  emit('change:page', page);
};

const emitPerPage = () => {
  emit('change:per-page', Number(localPerPage.value));
};

const emitRefresh = () => emit('refresh');
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>