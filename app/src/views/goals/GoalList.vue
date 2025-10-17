<template>
  <div class="space-y-6 rounded-3xl bg-gray-50 p-6 shadow-sm md:p-8">
      <header
        class="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white px-6 py-4 shadow-sm lg:flex-row lg:items-center lg:justify-between"
      >
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">{{ t('goals.title') }}</h1>
        <p class="mt-1 max-w-2xl text-sm text-slate-500">{{ t('goals.description') }}</p>
      </div>
    </header>

    <DataTable :columns="tableColumns" :rows="goals.items" :loading="loading" :selectable="false"
      :pagination="goals.meta" :empty-state="t('goals.list.empty')" @change:page="handlePage"
      @change:per-page="handlePerPage" @refresh="fetchGoals">
      <template #toolbar>
        <button type="button" class="btn-primary" @click="createGoal">
          <PlusIcon class="h-4 w-4" />
          <span>{{ t('common.actions.create') }}</span>
        </button>
      </template>
      <template #filters>
        <label class="flex flex-col gap-1 text-sm font-semibold text-slate-600">
          <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.list.filters.type') }}</span>
          <select class="input" v-model="filters.type">
            <option value="all">{{ t('common.placeholders.select') }}</option>
            <option v-for="option in typeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </label>
        <label class="flex flex-col gap-1 text-sm font-semibold text-slate-600">
          <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.list.filters.periodicity') }}</span>
          <select class="input" v-model="filters.periodicity">
            <option value="all">{{ t('common.placeholders.select') }}</option>
            <option v-for="option in periodicityOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </label>
        <label class="flex flex-col gap-1 text-sm font-semibold text-slate-600">
          <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.list.filters.status') }}</span>
          <select class="input" v-model="filters.status">
            <option value="all">{{ t('goals.status.all') }}</option>
            <option value="active">{{ t('goals.status.active') }}</option>
            <option value="inactive">{{ t('goals.status.inactive') }}</option>
          </select>
        </label>
      </template>

      <template #cell:target="{ row }">
        <div class="flex flex-col">
          <span class="font-semibold text-slate-800">{{ row.target }}</span>
          <span class="text-xs text-slate-400">{{ row.unit }}</span>
        </div>
      </template>

      <template #cell:active="{ row }">
        <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold"
          :class="row.active ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-500'">
          {{ row.active ? t('goals.status.active') : t('goals.status.inactive') }}
        </span>
      </template>

      <template #actions="{ row }">
        <div class="flex items-center gap-2">
          <button type="button"
            class="rounded-xl border border-transparent bg-blue-50 p-2 text-blue-600 transition hover:border-blue-200 hover:bg-blue-100"
            @click="() => editGoal(row)">
            <PencilSquareIcon class="h-3 w-3" />
            <span class="sr-only">{{ t('common.actions.edit') }}</span>
          </button>
          <button type="button"
            class="rounded-xl border border-transparent bg-red-50 p-2 text-red-600 transition hover:border-red-200 hover:bg-red-100"
            @click="() => confirmRemoval(row)">
            <TrashIcon class="h-3 w-3" />
            <span class="sr-only">{{ t('common.actions.delete') }}</span>
          </button>
        </div>
      </template>
    </DataTable>

    <ConfirmDialog v-model="confirmOpen" :title="t('goals.list.confirmDelete.title')"
      :message="t('goals.list.confirmDelete.message')" :confirm-label="t('common.actions.delete')"
      @confirm="removeGoal" />
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import DataTable from '@/components/DataTable.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import GoalService from '@/services/GoalService';
import { useNotificationStore } from '@/stores/notifications';
import { PencilSquareIcon, TrashIcon, PlusIcon } from '@heroicons/vue/24/outline';

const { t } = useI18n();
const router = useRouter();
const notifications = useNotificationStore();

const filters = reactive({
  type: 'all',
  periodicity: 'all',
  status: 'all',
  page: 0,
  perPage: 10,
});

const goals = reactive({
  items: [],
  meta: { page: 1, perPage: 10, total: 0, lastPage: 1, from: 0, to: 0 },
});

const loading = ref(false);
const confirmOpen = ref(false);
const removalId = ref(null);

const typeOptions = computed(() => [
  { value: 'PROTEIN', label: t('goals.types.PROTEIN') },
  { value: 'CARBOHYDRATE', label: t('goals.types.CARBOHYDRATE') },
  { value: 'FAT', label: t('goals.types.FAT') },
  { value: 'WATER', label: t('goals.types.WATER') },
  { value: 'FIBER', label: t('goals.types.FIBER') },
  { value: 'ENERGY', label: t('goals.types.ENERGY') },
  { value: 'MICRONUTRIENTS', label: t('goals.types.MICRONUTRIENTS') },
  { value: 'OTHER', label: t('goals.types.OTHER') },
]);

const periodicityOptions = computed(() => [
  { value: 'DAILY', label: t('goals.periodicityOptions.DAILY') },
  { value: 'WEEKLY', label: t('goals.periodicityOptions.WEEKLY') },
  { value: 'MONTHLY', label: t('goals.periodicityOptions.MONTHLY') },
  { value: 'CUSTOM', label: t('goals.periodicityOptions.CUSTOM') },
]);

const tableColumns = computed(() => [
  { key: 'type', label: t('goals.list.columns.type'), sortable: false, visible: true },
  { key: 'target', label: t('goals.list.columns.target'), sortable: false, visible: true },
  { key: 'periodicity', label: t('goals.list.columns.periodicity'), sortable: false, visible: true },
  { key: 'active', label: t('goals.list.columns.active'), sortable: false, visible: true },
  { key: 'owner', label: t('goals.list.columns.owner'), sortable: false, visible: true },
]);

const fetchGoals = async () => {
  loading.value = true;
  try {
    const params = {
      page: filters.page,
      size: filters.perPage,
      type: filters.type !== 'all' ? filters.type : undefined,
      periodicity: filters.periodicity !== 'all' ? filters.periodicity : undefined,
      active: filters.status === 'all' ? undefined : filters.status === 'active',
    };
    const { data } = await GoalService.list(params);
    const content = data?.content ?? data?.data ?? [];
    const number = data?.number ?? data?.page ?? filters.page;
    const size = data?.size ?? filters.perPage;
    const totalElements = data?.totalElements ?? data?.total ?? content.length;
    const totalPages = data?.totalPages ?? Math.max(1, Math.ceil(totalElements / size));

    goals.items = content.map((item) => ({
      id: item.id,
      type: t(`goals.types.${item.type}`) ?? item.type,
      target: formatTarget(item),
      unit: item.unitName ?? item.unitSymbol ?? '',
      periodicity: t(`goals.periodicityOptions.${item.periodicity}`) ?? item.periodicity,
      active: item.active !== false,
      owner: item.createdByUserName ?? '',
      raw: item,
    }));

    goals.meta = {
      page: number + 1,
      perPage: size,
      total: totalElements,
      lastPage: totalPages,
      from: content.length ? number * size + 1 : 0,
      to: content.length ? number * size + content.length : 0,
    };
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to load goals.',
    });
    goals.items = [];
  } finally {
    loading.value = false;
  }
};

const formatTarget = (item) => {
  if (item.targetValue == null) {
    return t('common.placeholders.empty');
  }
  const value = Number(item.targetValue).toLocaleString(undefined, {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  });
  const mode = item.targetMode === 'PER_KG' ? t('goals.perKg') : t('goals.absolute');
  return `${value} ${item.unitSymbol ?? ''} (${mode})`;
};

const handlePage = (page) => {
  filters.page = page - 1;
  fetchGoals();
};

const handlePerPage = (perPage) => {
  filters.perPage = perPage;
  filters.page = 0;
  fetchGoals();
};

const createGoal = () => {
  router.push({ name: 'goals-new' });
};

const editGoal = (row) => {
  router.push({ name: 'goals-edit', params: { id: row.id } });
};

const confirmRemoval = (row) => {
  removalId.value = row.id;
  confirmOpen.value = true;
};

const removeGoal = async () => {
  if (!removalId.value) {
    return;
  }
  try {
    await GoalService.remove(removalId.value);
    notifications.push({
      type: 'success',
      title: t('goals.title'),
      message: t('goals.deleted'),
    });
    fetchGoals();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to delete goal.',
    });
  } finally {
    removalId.value = null;
    confirmOpen.value = false;
  }
};

watch(
  () => [filters.type, filters.periodicity, filters.status],
  () => {
    filters.page = 0;
    fetchGoals();
  }
);

watch(
  () => filters.perPage,
  () => {
    filters.page = 0;
  }
);

fetchGoals();
</script>
