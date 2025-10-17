<template>
  <div class="space-y-0">
    <DataTable v-model:selected="selectedRows" :title="t('reference.exerciseReferences.title')"
      :subtitle="t('reference.exerciseReferences.subtitle')" :columns="columns" :rows="rows" :loading="loading"
      :pagination="pagination" :sort="sort" :empty-state="t('reference.exerciseReferences.empty')"
      @change:sort="handleSort" @change:page="handlePage" @change:per-page="handlePerPage"
      @change:columns="persistColumns" @refresh="fetchExerciseReferences">
      <template #toolbar="{ selected }">
        <button type="button" class="btn-primary" @click="openCreate">
          <PlusIcon class="h-4 w-4" />
          <span>{{ t('common.actions.create') }}</span>
        </button>
        <button type="button" class="btn-secondary" :disabled="selected.length !== 1" @click="openEdit(selected[0])">
          <PencilSquareIcon class="h-4 w-4" />
          <span>{{ t('common.actions.edit') }}</span>
        </button>
        <button type="button" class="btn-secondary text-red-600 hover:border-red-200 hover:text-red-600"
          :disabled="selected.length === 0" @click="openDelete(selected)">
          <TrashIcon class="h-4 w-4" />
          <span>{{ t('common.actions.delete') }}</span>
        </button>
      </template>

      <template #filters>
        <div class="lg:col-span-2">
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('reference.exerciseReferences.filters.search') }}
          </label>
          <div class="relative mt-1">
            <MagnifyingGlassIcon
              class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input v-model="filters.search" type="search"
              class="w-full rounded-xl border border-slate-200 px-10 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
              :placeholder="t('reference.exerciseReferences.filters.searchPlaceholder')" />
          </div>
        </div>
        <!--<div>
            <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('reference.exerciseReferences.filters.language') }}
            </label>
            <input
              v-model="filters.language"
              type="text"
              class="input mt-1"
              :placeholder="t('reference.exerciseReferences.filters.languagePlaceholder')"
            />
          </div> -->
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('reference.exerciseReferences.filters.muscleGroup') }}
          </label>
          <input v-model="filters.muscleGroup" type="text" class="input mt-1"
            :placeholder="t('reference.exerciseReferences.filters.muscleGroupPlaceholder')" />
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('reference.exerciseReferences.filters.equipment') }}
          </label>
          <input v-model="filters.equipment" type="text" class="input mt-1"
            :placeholder="t('reference.exerciseReferences.filters.equipmentPlaceholder')" />
        </div>
      </template>

      <template #cell:createdAt="{ row }">
        <span class="text-xs text-slate-500">{{ formatDate(row.createdAt) }}</span>
      </template>

      <template #actions="{ row }">
        <div class="flex items-center gap-2">
          <button type="button" class="text-sm font-semibold text-primary-600" @click="openEdit(row.id)">
            {{ t('common.actions.edit') }}
          </button>
          <button type="button" class="text-sm font-semibold text-red-500" @click="openDelete([row.id])">
            {{ t('common.actions.delete') }}
          </button>
        </div>
      </template>
    </DataTable>

    <ReferenceFormModal v-model="modalOpen" :title="modalTitle" :description="t('reference.title')" :fields="formFields"
      :model="activeItem" :submitting="saving" :submit-label="t('common.actions.save')"
      :saving-label="t('common.actions.saving')" @submit="handleSubmit" />

    <ConfirmDialog v-model="confirmOpen" :title="t('reference.title')" :message="confirmMessage"
      :confirm-label="t('common.actions.delete')" @confirm="handleConfirmDelete" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import DataTable from '@/components/DataTable.vue';
import ReferenceFormModal from '@/components/ReferenceFormModal.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import {
  listExerciseReferences,
  createExerciseReference,
  updateExerciseReference,
  deleteExerciseReference,
} from '@/services/referenceAdmin';
import { useNotificationStore } from '@/stores/notifications';
import { useI18n } from 'vue-i18n';
import { MagnifyingGlassIcon, PencilSquareIcon, PlusIcon, TrashIcon } from '@heroicons/vue/24/outline';

const notifications = useNotificationStore();
const { t, locale } = useI18n();

const rows = ref([]);
const loading = ref(false);
const saving = ref(false);
const modalOpen = ref(false);
const confirmOpen = ref(false);
const activeItem = ref(null);
const pendingDeletion = ref([]);
const selectedRows = ref([]);

const pagination = reactive({ page: 1, perPage: 10, total: 0, lastPage: 1, from: 0, to: 0 });
const sort = reactive({ field: 'name', direction: 'asc' });
const filters = reactive({ page: 1, perPage: 10, search: '', language: '', muscleGroup: '', equipment: '' });
const columnsState = ref([]);

const createColumns = () => [
  { key: 'code', label: t('reference.exerciseReferences.columns.code'), sortable: true, visible: true },
  { key: 'name', label: t('reference.exerciseReferences.columns.name'), sortable: true, visible: true },
  {
    key: 'muscleGroup',
    label: t('reference.exerciseReferences.columns.muscleGroup'),
    sortable: true,
    visible: true,
  },
  { key: 'equipment', label: t('reference.exerciseReferences.columns.equipment'), sortable: true, visible: true },
  { key: 'language', label: t('reference.exerciseReferences.columns.language'), sortable: true, visible: true },
  { key: 'createdAt', label: t('reference.exerciseReferences.columns.createdAt'), sortable: true, visible: true },
];

const columns = computed(() => {
  if (!columnsState.value.length) {
    columnsState.value = createColumns();
  }
  return columnsState.value;
});

const formFields = computed(() => [
  { key: 'code', label: t('reference.exerciseReferences.form.code'), required: true },
  { key: 'name', label: t('reference.exerciseReferences.form.name'), required: true },
  { key: 'description', label: t('reference.exerciseReferences.form.description'), type: 'textarea', rows: 4 },
  { key: 'muscleGroup', label: t('reference.exerciseReferences.form.muscleGroup') },
  { key: 'equipment', label: t('reference.exerciseReferences.form.equipment') },
  {
    key: 'language',
    label: t('reference.exerciseReferences.form.language'),
    helper: t('reference.exerciseReferences.form.languageHelper'),
  },
]);

const modalTitle = computed(() =>
  activeItem.value?.id ? t('common.actions.edit') : t('common.actions.create')
);

const confirmMessage = computed(() => t('reference.exerciseReferences.deleteQuestion'));

const formatDate = (value) => {
  if (!value) return '—';
  try {
    return new Intl.DateTimeFormat(locale.value, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

const persistColumns = (updatedColumns) => {
  columnsState.value = updatedColumns.map((column) => ({ ...column }));
};

const mapMeta = (data, pageData) => ({
  page: (data?.number ?? filters.page - 1) + 1,
  perPage: data?.size ?? filters.perPage,
  total: data?.totalElements ?? pageData.length,
  lastPage: data?.totalPages ?? Math.max(1, Math.ceil((data?.totalElements ?? pageData.length) / filters.perPage)),
  from: pageData.length ? (data?.number ?? filters.page - 1) * (data?.size ?? filters.perPage) + 1 : 0,
  to: pageData.length
    ? (data?.number ?? filters.page - 1) * (data?.size ?? filters.perPage) + pageData.length
    : 0,
});

const fetchExerciseReferences = async () => {
  loading.value = true;
  try {
    const params = {
      page: filters.page - 1,
      size: filters.perPage,
      sort: `${sort.field},${sort.direction}`,
      search: filters.search || undefined,
      language: filters.language || undefined,
      muscleGroup: filters.muscleGroup || undefined,
      equipment: filters.equipment || undefined,
    };
    const { data } = await listExerciseReferences(params);
    const pageData = data?.content ?? data?.data ?? data ?? [];
    rows.value = pageData.map((item) => ({
      id: item.id,
      code: item.code,
      name: item.name,
      description: item.description,
      muscleGroup: item.muscleGroup,
      equipment: item.equipment,
      language: item.language,
      createdAt: item.createdAt,
    }));
    const meta = mapMeta(data, pageData);
    Object.assign(pagination, meta);
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.exerciseReferences.empty'),
    });
    rows.value = [];
    Object.assign(pagination, { page: filters.page, perPage: filters.perPage, total: 0, lastPage: 1, from: 0, to: 0 });
  } finally {
    loading.value = false;
  }
};

const handleSort = ({ field, direction }) => {
  sort.field = field;
  sort.direction = direction;
  fetchExerciseReferences();
};

const handlePage = (page) => {
  filters.page = page;
  fetchExerciseReferences();
};

const handlePerPage = (perPage) => {
  filters.perPage = perPage;
  filters.page = 1;
  fetchExerciseReferences();
};

const openCreate = () => {
  activeItem.value = null;
  modalOpen.value = true;
};

const openEdit = (id) => {
  const item = rows.value.find((row) => row.id === id);
  if (!item) return;
  activeItem.value = { ...item };
  modalOpen.value = true;
};

const openDelete = (ids) => {
  pendingDeletion.value = Array.isArray(ids) ? ids : [ids];
  confirmOpen.value = true;
};

const handleSubmit = async (payload) => {
  saving.value = true;
  try {
    const normalized = { ...payload };
    if (activeItem.value?.id) {
      await updateExerciseReference(activeItem.value.id, normalized);
    } else {
      await createExerciseReference(normalized);
    }
    notifications.push({
      type: 'success',
      title: t('reference.title'),
      message: t('reference.exerciseReferences.createSuccess'),
    });
    modalOpen.value = false;
    await fetchExerciseReferences();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.exerciseReferences.empty'),
    });
  } finally {
    saving.value = false;
  }
};

const handleConfirmDelete = async () => {
  if (!pendingDeletion.value.length) {
    return;
  }
  try {
    await Promise.all(pendingDeletion.value.map((id) => deleteExerciseReference(id)));
    notifications.push({
      type: 'success',
      title: t('reference.title'),
      message: t('reference.exerciseReferences.deleteSuccess'),
    });
    selectedRows.value = [];
    await fetchExerciseReferences();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.exerciseReferences.deleteQuestion'),
    });
  }
};

watch(
  () => [filters.search, filters.language, filters.muscleGroup, filters.equipment],
  () => {
    filters.page = 1;
    fetchExerciseReferences();
  }
);

watch(
  () => locale.value,
  () => {
    const visibility = columnsState.value.reduce((acc, column) => {
      acc[column.key] = column.visible !== false;
      return acc;
    }, {});
    columnsState.value = createColumns().map((column) => ({
      ...column,
      visible: visibility[column.key] ?? true,
    }));
  }
);

onMounted(() => {
  columnsState.value = createColumns();
  fetchExerciseReferences();
});
</script>
