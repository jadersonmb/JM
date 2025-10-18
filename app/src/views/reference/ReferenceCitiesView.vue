<template>
  <div class="space-y-0">
    <DataTable
      :title="t('reference.cities.title')"
      :subtitle="t('reference.cities.subtitle')"
      :columns="columns"
      :rows="rows"
      :loading="loading"
      :pagination="pagination"
      :selected="selectedRows"
      :sort="sort"
      :empty-state="t('reference.cities.empty')"
      @update:selected="(value) => (selectedRows.value = value)"
      @change:sort="handleSort"
      @change:page="handlePage"
      @change:per-page="handlePerPage"
      @change:columns="persistColumns"
      @refresh="fetchCities"
    >
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
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('common.actions.search') }}</label>
          <div class="relative mt-1">
            <MagnifyingGlassIcon
              class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
            />
            <input
              v-model="filters.search"
              type="search"
              class="w-full rounded-xl border border-slate-200 px-10 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
              :placeholder="t('common.actions.search')"
            />
          </div>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('reference.countries.title') }}</label>
          <select v-model="filters.countryId" class="input mt-1">
            <option value="">{{ t('common.placeholders.select') }}</option>
            <option v-for="option in countryOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </div>
      </template>

      <template #cell:capital="{ row }">
        <span class="badge" :class="row.capital ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'">
          {{ row.capital ? t('common.boolean.yes') : t('common.boolean.no') }}
        </span>
      </template>

      <template #actions="{ row }">
        <div class="flex items-center gap-2">
          <button type="button"
            class="rounded-xl border border-transparent bg-emerald-50 p-2 text-emerald-600 transition hover:border-emerald-200 hover:bg-emerald-100"
             @click="openEdit(row.id)">
            <PencilSquareIcon class="h-3 w-3" />
            <span class="sr-only">{{ t('common.actions.edit') }}</span>
          </button>
          <button type="button"
            class="rounded-xl border border-transparent bg-red-50 p-2 text-red-600 transition hover:border-red-200 hover:bg-red-100"
            @click="openDelete([row.id])">
            <TrashIcon class="h-3 w-3" />
            <span class="sr-only">{{ t('common.actions.delete') }}</span>
          </button>
        </div>
      </template>
    </DataTable>

    <ReferenceFormModal
      v-model="modalOpen"
      :title="modalTitle"
      :description="t('reference.title')"
      :fields="formFields"
      :model="activeItem"
      :submitting="saving"
      :submit-label="t('common.actions.save')"
      :saving-label="t('common.actions.saving')"
      @submit="handleSubmit"
    />

    <ConfirmDialog
      v-model="confirmOpen"
      :title="t('reference.title')"
      :message="confirmMessage"
      :confirm-label="t('common.actions.delete')"
      @confirm="handleConfirmDelete"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import DataTable from '@/components/DataTable.vue';
import ReferenceFormModal from '@/components/ReferenceFormModal.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import {
  listCities,
  createCity,
  updateCity,
  deleteCity,
} from '@/services/referenceAdmin';
import { getCountries } from '@/services/reference';
import { MagnifyingGlassIcon, PencilSquareIcon, PlusIcon, TrashIcon } from '@heroicons/vue/24/outline';
import { useNotificationStore } from '@/stores/notifications';
import { useI18n } from 'vue-i18n';

const notifications = useNotificationStore();
const { t, locale } = useI18n();

const columnsState = ref([]);
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
const filters = reactive({ page: 1, perPage: 10, search: '', countryId: '' });
const countryOptions = ref([]);

const createColumns = () => [
  { key: 'name', label: t('reference.cities.columns.name'), sortable: true, visible: true },
  { key: 'stateCode', label: t('reference.cities.columns.stateCode'), sortable: true, visible: true },
  { key: 'stateName', label: t('reference.cities.columns.stateName'), sortable: true, visible: true },
  { key: 'country', label: t('reference.cities.columns.country'), sortable: true, visible: true },
  { key: 'capital', label: t('reference.cities.columns.capital'), sortable: true, visible: true },
  { key: 'timezone', label: t('reference.cities.columns.timezone'), sortable: true, visible: true },
];

const columns = computed(() => {
  if (!columnsState.value.length) {
    columnsState.value = createColumns();
  }
  return columnsState.value;
});

const formFields = computed(() => [
  {
    key: 'countryId',
    label: t('reference.cities.form.countryId'),
    type: 'select',
    required: true,
    options: countryOptions.value,
    placeholder: t('common.placeholders.select'),
  },
  { key: 'name', label: t('reference.cities.form.name'), required: true },
  { key: 'stateCode', label: t('reference.cities.form.stateCode'), required: true },
  { key: 'stateName', label: t('reference.cities.form.stateName'), required: true },
  { key: 'cityCode', label: t('reference.cities.form.cityCode') },
  { key: 'timezone', label: t('reference.cities.form.timezone') },
  { key: 'capital', label: t('reference.cities.form.capital'), type: 'checkbox', checkboxLabel: t('reference.cities.form.capital') },
]);

const modalTitle = computed(() =>
  activeItem.value?.id ? t('common.actions.edit') : t('common.actions.create')
);

const confirmMessage = computed(() => t('reference.cities.deleteQuestion'));

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

const loadCountryOptions = async () => {
  try {
    const { data } = await getCountries({});
    countryOptions.value = (data ?? []).map((item) => ({ value: item.id, label: item.name }));
  } catch (error) {
    countryOptions.value = [];
  }
};

const fetchCities = async () => {
  loading.value = true;
  try {
    const params = {
      page: filters.page - 1,
      size: filters.perPage,
      sort: `${sort.field},${sort.direction}`,
      name: filters.search || undefined,
      countryId: filters.countryId || undefined,
    };
    const { data } = await listCities(params);
    const pageData = data?.content ?? data?.data ?? data ?? [];
    rows.value = pageData.map((item) => ({
      id: item.id,
      name: item.name,
      stateCode: item.stateCode,
      stateName: item.stateName,
      country: item.countryName ?? item.country?.name,
      capital: Boolean(item.capital ?? item.isCapital),
      timezone: item.timezone,
      countryId: item.countryId,
      cityCode: item.cityCode,
    }));
    Object.assign(pagination, mapMeta(data, pageData));
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.cities.empty'),
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
  fetchCities();
};

const handlePage = (page) => {
  filters.page = page;
  fetchCities();
};

const handlePerPage = (perPage) => {
  filters.perPage = perPage;
  filters.page = 1;
  fetchCities();
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
    const normalized = { ...payload, capital: Boolean(payload.capital) };
    if (activeItem.value?.id) {
      await updateCity(activeItem.value.id, normalized);
    } else {
      await createCity(normalized);
    }
    notifications.push({
      type: 'success',
      title: t('reference.title'),
      message: t('reference.cities.createSuccess'),
    });
    modalOpen.value = false;
    await fetchCities();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.cities.empty'),
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
    await Promise.all(pendingDeletion.value.map((id) => deleteCity(id)));
    notifications.push({
      type: 'success',
      title: t('reference.title'),
      message: t('reference.cities.deleteSuccess'),
    });
    selectedRows.value = [];
    await fetchCities();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.message ?? t('reference.cities.deleteQuestion'),
    });
  }
};

watch(
  () => filters.search,
  () => {
    filters.page = 1;
    fetchCities();
  }
);

watch(
  () => filters.countryId,
  () => {
    filters.page = 1;
    fetchCities();
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
    loadCountryOptions();
  }
);

onMounted(async () => {
  columnsState.value = createColumns();
  await loadCountryOptions();
  fetchCities();
});
</script>
