<template>
  <div class="space-y-6">
    <header class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ t('menu.title') }}</p>
        <h1 class="text-2xl font-semibold text-slate-900">{{ t('diet.title') }}</h1>
        <p class="mt-1 max-w-2xl text-sm text-slate-500">{{ t('diet.description') }}</p>
      </div>
    </header>

    <DataTable :columns="tableColumns" :rows="diets.items" :loading="loading" :selectable="false"
      :pagination="diets.meta" :empty-state="t('diet.list.empty')" @change:page="handlePage"
      @change:per-page="handlePerPage" @refresh="fetchDiets">
      <template #toolbar>
        <button type="button" class="btn-primary" @click="createDiet">
          <PlusIcon class="h-4 w-4" />
          <span>{{ t('common.actions.create') }}</span>
        </button>
      </template>
      <template v-if="isAdmin" #filters>
        <label class="flex flex-col gap-1 text-sm font-semibold text-slate-600">
          <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.list.filters.patient') }}</span>
          <input type="text" class="input" v-model="filters.patientName"
            :placeholder="t('diet.list.filters.patientPlaceholder')" />
        </label>
        <label class="flex flex-col gap-1 text-sm font-semibold text-slate-600">
          <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.list.filters.mealType') }}</span>
          <select class="input" v-model="filters.mealType">
            <option value="all">{{ t('diet.status.all') }}</option>
            <option v-for="option in mealTypeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </label>
        <label class="flex flex-col gap-1 text-sm font-semibold text-slate-600">
          <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.list.filters.status') }}</span>
          <select class="input" v-model="filters.status">
            <option value="all">{{ t('diet.status.all') }}</option>
            <option value="active">{{ t('diet.status.active') }}</option>
            <option value="inactive">{{ t('diet.status.inactive') }}</option>
          </select>
        </label>
      </template>

      <template #cell:patient="{ row }">
        <div>
          <p class="font-semibold text-slate-800">{{ row.patientName || t('common.placeholders.empty') }}</p>
          <p v-if="row.createdByUser" class="text-xs text-slate-400">{{ row.createdByUser }}</p>
        </div>
      </template>

      <template #cell:meals="{ row }">
        <span
          class="inline-flex items-center rounded-full bg-primary-50 px-3 py-1 text-xs font-semibold text-primary-600">
          {{ row.mealCount }}
        </span>
      </template>

      <template #cell:active="{ row }">
        <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold"
          :class="row.active ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-500'">
          {{ row.active ? t('diet.status.active') : t('diet.status.inactive') }}
        </span>
      </template>

      <template #actions="{ row }">
        <div class="flex items-center gap-2">
          <button type="button"
            class="rounded-xl border border-transparent bg-blue-50 p-2 text-blue-600 transition hover:border-blue-200 hover:bg-blue-100"
            @click="() => viewDiet(row)">
            <EyeIcon class="h-3 w-3" />
            <span class="sr-only">{{ t('common.actions.view') }}</span>
          </button>
          <button type="button"
            class="rounded-xl border border-transparent bg-blue-50 p-2 text-blue-600 transition hover:border-blue-200 hover:bg-blue-100"
            @click="() => editDiet(row)">
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

    <ConfirmDialog v-model="confirmOpen" :title="t('diet.list.confirmDelete.title')"
      :message="t('diet.list.confirmDelete.message')" :confirm-label="t('diet.delete')" @confirm="removeDiet" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import DataTable from '@/components/DataTable.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import DietService from '@/services/DietService';
import { useNotificationStore } from '@/stores/notifications';
import { useAuthStore } from '@/stores/auth';
import { TrashIcon, PencilSquareIcon, EyeIcon, PlusIcon } from '@heroicons/vue/24/outline';

const { t } = useI18n();
const router = useRouter();
const notifications = useNotificationStore();
const auth = useAuthStore();
const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');

const filters = reactive({
  patientName: '',
  mealType: 'all',
  status: 'all',
  page: 0,
  perPage: 10,
});

const diets = reactive({
  items: [],
  meta: { page: 1, perPage: 10, total: 0, lastPage: 1, from: 0, to: 0 },
});

const loading = ref(false);
const confirmOpen = ref(false);
const removalId = ref(null);

const mealTypeOptions = computed(() => [
  { value: 'BREAKFAST', label: t('diet.meal.breakfast') },
  { value: 'LUNCH', label: t('diet.meal.lunch') },
  { value: 'SNACK', label: t('diet.meal.snack') },
  { value: 'DINNER', label: t('diet.meal.dinner') },
  { value: 'SUPPER', label: t('diet.meal.supper') },
]);

const tableColumns = computed(() => [
  { key: 'patient', label: t('diet.list.columns.patient'), sortable: false, visible: true },
  { key: 'mealCount', label: t('diet.list.columns.meals'), sortable: false, visible: true },
  { key: 'updatedAt', label: t('diet.list.columns.updatedAt'), sortable: false, visible: true },
  { key: 'active', label: t('diet.list.columns.active'), sortable: false, visible: true },
]);

const fetchDiets = async () => {
  loading.value = true;
  try {
    const params = {
      page: filters.page,
      size: filters.perPage,
      patientName: filters.patientName || undefined,
      mealType: filters.mealType !== 'all' ? filters.mealType : undefined,
      active: filters.status === 'all' ? undefined : filters.status === 'active',
    };
    const { data } = await DietService.list(params);
    const content = data?.content ?? data?.data ?? [];
    const number = data?.number ?? data?.page ?? filters.page;
    const size = data?.size ?? filters.perPage;
    const totalElements = data?.totalElements ?? data?.total ?? content.length;
    const totalPages = data?.totalPages ?? Math.max(1, Math.ceil(totalElements / size));

    diets.items = content.map((item) => ({
      id: item.id,
      patientName: item.patientName,
      createdByUser: item.createdByName ?? '',
      mealCount: item.meals?.length ?? 0,
      updatedAt: formatDate(item.updatedAt ?? item.createdAt),
      active: item.active !== false,
      raw: item,
    }));

    diets.meta = {
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
      message: error.response?.data?.details ?? 'Unable to load diet plans.',
    });
    diets.items = [];
  } finally {
    loading.value = false;
  }
};

let searchTimeout;

watch(
  () => filters.patientName,
  () => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      filters.page = 0;
      fetchDiets();
    }, 300);
  }
);

watch(
  () => [filters.mealType, filters.status],
  () => {
    filters.page = 0;
    fetchDiets();
  }
);

const handlePage = (page) => {
  filters.page = Math.max(page - 1, 0);
  fetchDiets();
};

const handlePerPage = (perPage) => {
  filters.perPage = perPage;
  filters.page = 0;
  fetchDiets();
};

const createDiet = () => {
  router.push({ name: 'diet-new' });
};

const viewDiet = (row) => {
  router.push({ name: 'diet-edit', params: { id: row.id }, query: { mode: 'view' } });
};

const editDiet = (row) => {
  router.push({ name: 'diet-edit', params: { id: row.id } });
};

const confirmRemoval = (row) => {
  removalId.value = row.id;
  confirmOpen.value = true;
};

const removeDiet = async () => {
  if (!removalId.value) return;
  try {
    await DietService.remove(removalId.value);
    notifications.push({
      type: 'success',
      title: t('diet.wizard.toast.title'),
      message: t('diet.wizard.toast.deleted'),
    });
    fetchDiets();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to delete diet plan.',
    });
  } finally {
    removalId.value = null;
  }
};

const formatDate = (value) => {
  if (!value) return t('common.placeholders.empty');
  try {
    return new Intl.DateTimeFormat(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

onBeforeUnmount(() => {
  clearTimeout(searchTimeout);
});

fetchDiets();
</script>
