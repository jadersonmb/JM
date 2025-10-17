<template>
  <div class="space-y-0">
    <DataTable v-model:selected="selectedRows" :title="t('reference.aiPrompts.title')"
      :subtitle="t('reference.aiPrompts.subtitle')" :columns="columns" :rows="rows" :loading="loading"
      :pagination="pagination" :sort="sort" :empty-state="t('reference.aiPrompts.empty')" @change:sort="handleSort"
      @change:page="handlePage" @change:per-page="handlePerPage" @change:columns="persistColumns"
      @refresh="fetchPrompts">
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
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('reference.aiPrompts.filters.search') }}
          </label>
          <div class="relative mt-1">
            <MagnifyingGlassIcon
              class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input v-model="filters.search" type="search"
              class="w-full rounded-xl border border-slate-200 px-10 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
              :placeholder="t('reference.aiPrompts.filters.searchPlaceholder')" />
          </div>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('reference.aiPrompts.filters.provider') }}
          </label>
          <select v-model="filters.provider" class="input mt-1">
            <option value="">{{ t('reference.aiPrompts.filters.allProviders') }}</option>
            <option v-for="option in providerOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
        <!-- <div>
            <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('reference.aiPrompts.filters.owner') }}
            </label>
            <input
              v-model="filters.owner"
              type="text"
              class="input mt-1"
              :placeholder="t('reference.aiPrompts.filters.ownerPlaceholder')"
            />
          </div> -->
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
  listAiPrompts,
  createAiPrompt,
  updateAiPrompt,
  deleteAiPrompt,
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
const sort = reactive({ field: 'updatedAt', direction: 'desc' });
const filters = reactive({ page: 1, perPage: 10, search: '', provider: '', owner: '' });
const columnsState = ref([]);

const providerOptions = computed(() => [
  { value: 'GEMINI', label: t('reference.aiPrompts.providers.GEMINI') },
  { value: 'OLLAMA', label: t('reference.aiPrompts.providers.OLLAMA') },
]);

const createColumns = () => [
  { key: 'code', label: t('reference.aiPrompts.columns.code'), sortable: true, visible: true },
  { key: 'name', label: t('reference.aiPrompts.columns.name'), sortable: true, visible: true },
  { key: 'providerLabel', label: t('reference.aiPrompts.columns.provider'), sortable: true, visible: true },
  { key: 'model', label: t('reference.aiPrompts.columns.model'), sortable: true, visible: true },
  { key: 'ownerDisplay', label: t('reference.aiPrompts.columns.owner'), sortable: false, visible: true },
  { key: 'activeLabel', label: t('reference.aiPrompts.columns.active'), sortable: true, visible: true },
  { key: 'updatedAt', label: t('reference.aiPrompts.columns.updatedAt'), sortable: true, visible: true },
];

const columns = computed(() => {
  if (!columnsState.value.length) {
    columnsState.value = createColumns();
  }
  return columnsState.value;
});

const formFields = computed(() => [
  { key: 'code', label: t('reference.aiPrompts.form.code'), required: true },
  { key: 'name', label: t('reference.aiPrompts.form.name'), required: true },
  {
    key: 'provider',
    label: t('reference.aiPrompts.form.provider'),
    type: 'select',
    required: true,
    options: providerOptions.value,
  },
  { key: 'model', label: t('reference.aiPrompts.form.model'), required: true },
  {
    key: 'owner',
    label: t('reference.aiPrompts.form.owner'),
    helper: t('reference.aiPrompts.form.ownerHelper'),
  },
  { key: 'description', label: t('reference.aiPrompts.form.description'), type: 'textarea' },
  {
    key: 'prompt',
    label: t('reference.aiPrompts.form.prompt'),
    type: 'textarea',
    rows: 6,
    required: true,
    helper: t('reference.aiPrompts.form.promptHelper'),
  },
  {
    key: 'active',
    label: t('reference.aiPrompts.form.active'),
    type: 'checkbox',
    checkboxLabel: t('reference.aiPrompts.form.activeLabel'),
  },
]);

const modalTitle = computed(() =>
  activeItem.value?.id ? t('common.actions.edit') : t('common.actions.create')
);

const confirmMessage = computed(() => t('reference.aiPrompts.deleteQuestion'));

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

const formatDateTime = (value) => {
  if (!value) return '—';
  try {
    return new Date(value).toLocaleString(locale.value);
  } catch (error) {
    return value;
  }
};

const fetchPrompts = async () => {
  loading.value = true;
  try {
    const params = {
      page: filters.page - 1,
      size: filters.perPage,
      sort: `${sort.field},${sort.direction}`,
      code: filters.search || undefined,
      name: filters.search || undefined,
      provider: filters.provider || undefined,
      owner: filters.owner || undefined,
    };
    const { data } = await listAiPrompts(params);
    const pageData = data?.content ?? data?.data ?? data ?? [];
    rows.value = pageData.map((item) => ({
      id: item.id,
      code: item.code,
      name: item.name,
      provider: item.provider,
      providerLabel: t(`reference.aiPrompts.providers.${item.provider}`),
      model: item.model,
      owner: item.owner ?? '',
      ownerDisplay: item.owner ?? t('reference.aiPrompts.ownerGlobal'),
      description: item.description,
      prompt: item.prompt,
      active: Boolean(item.active),
      activeLabel: item.active
        ? t('reference.aiPrompts.activeStatus.active')
        : t('reference.aiPrompts.activeStatus.inactive'),
      updatedAt: formatDateTime(item.updatedAt),
    }));
    Object.assign(pagination, mapMeta(data, pageData));
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.aiPrompts.empty'),
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
  fetchPrompts();
};

const handlePage = (page) => {
  filters.page = page;
  fetchPrompts();
};

const handlePerPage = (perPage) => {
  filters.perPage = perPage;
  filters.page = 1;
  fetchPrompts();
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
    const normalized = {
      ...payload,
      owner: payload.owner?.trim() || null,
      active: Boolean(payload.active),
    };
    if (activeItem.value?.id) {
      await updateAiPrompt(activeItem.value.id, normalized);
    } else {
      await createAiPrompt(normalized);
    }
    notifications.push({
      type: 'success',
      title: t('reference.title'),
      message: t('reference.aiPrompts.saveSuccess'),
    });
    modalOpen.value = false;
    await fetchPrompts();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.aiPrompts.empty'),
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
    await Promise.all(pendingDeletion.value.map((id) => deleteAiPrompt(id)));
    notifications.push({
      type: 'success',
      title: t('reference.title'),
      message: t('reference.aiPrompts.deleteSuccess'),
    });
    confirmOpen.value = false;
    pendingDeletion.value = [];
    selectedRows.value = [];
    await fetchPrompts();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.aiPrompts.deleteQuestion'),
    });
  }
};

watch(
  () => filters.search,
  () => {
    filters.page = 1;
    fetchPrompts();
  },
);

watch(
  () => filters.provider,
  () => {
    filters.page = 1;
    fetchPrompts();
  },
);

watch(
  () => filters.owner,
  () => {
    filters.page = 1;
    fetchPrompts();
  },
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
  },
);

onMounted(() => {
  columnsState.value = createColumns();
  fetchPrompts();
});
</script>
