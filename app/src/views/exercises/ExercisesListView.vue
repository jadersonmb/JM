<template>
  <div class="space-y-0">
    <DataTable
      :title="t('exercises.title')"
      :subtitle="t('exercises.subtitle')"
      :columns="tableColumns"
      :rows="rows"
      :loading="loading"
      :selected="selectedIds"
      :sort="{ field: filters.sortField, direction: filters.sortDirection }"
      :pagination="pagination"
      :empty-state="t('exercises.empty')"
      @update:selected="(value) => (selectedIds.value = value)"
      @change:sort="handleSort"
      @change:page="handlePage"
      @change:per-page="handlePerPage"
      @change:columns="persistColumns"
      @refresh="fetchExercises"
    >
      <template #toolbar="{ selected }">
        <button type="button" class="btn-primary" @click="openCreate">
          <PlusIcon class="h-4 w-4" />
          <span>{{ t('common.actions.create') }}</span>
        </button>
        <button
          type="button"
          class="btn-secondary"
          :disabled="selected.length !== 1"
          @click="openEdit(selected[0])"
        >
          <PencilSquareIcon class="h-4 w-4" />
          <span>{{ t('common.actions.edit') }}</span>
        </button>
        <button
          type="button"
          class="btn-secondary text-red-600 hover:border-red-200 hover:text-red-600"
          :disabled="selected.length === 0"
          @click="openDelete(selected)"
        >
          <TrashIcon class="h-4 w-4" />
          <span>{{ t('common.actions.delete') }}</span>
        </button>
      </template>

      <template #filters>
        <div class="md:col-span-2">
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('exercises.filters.search') }}
          </label>
          <div class="relative mt-1">
            <MagnifyingGlassIcon
              class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
            />
            <input
              v-model="searchInput"
              type="search"
              class="w-full rounded-xl border border-slate-200 px-10 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
              :placeholder="t('exercises.filters.search')"
            />
          </div>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('exercises.filters.reference') }}
          </label>
          <select v-model="filters.referenceId" class="input mt-1" @change="handleFilterChange">
            <option value="">{{ t('common.placeholders.select') }}</option>
            <option v-for="reference in references" :key="reference.id" :value="reference.id">
              {{ reference.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('exercises.filters.intensity') }}
          </label>
          <select v-model="filters.intensity" class="input mt-1" @change="handleFilterChange">
            <option value="">{{ t('common.placeholders.select') }}</option>
            <option v-for="option in intensityOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </div>
        <div v-if="isAdmin">
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {{ t('exercises.filters.user') }}
          </label>
          <select v-model="filters.userId" class="input mt-1" @change="handleFilterChange">
            <option value="">{{ t('exercises.filters.allUsers') }}</option>
            <option v-for="user in users" :key="user.id" :value="user.id">
              {{ formatUserOption(user) }}
            </option>
          </select>
        </div>
      </template>

      <template #cell:name="{ row }">
        <div>
          <p class="font-semibold text-slate-900">{{ row.displayName }}</p>
          <p v-if="row.referenceName" class="text-xs text-slate-400">{{ row.referenceName }}</p>
        </div>
      </template>

      <template #cell:intensity="{ row }">
        <span v-if="row.intensity" class="badge bg-slate-100 text-slate-600">{{ formatIntensity(row.intensity) }}</span>
        <span v-else class="text-sm text-slate-400">-</span>
      </template>

      <template v-if="isAdmin" #cell:userName="{ row }">
        <span class="text-sm text-slate-600">{{ row.userName || '-' }}</span>
      </template>

      <template #cell:durationMinutes="{ row }">
        <span class="text-sm text-slate-600">{{ formatDuration(row.durationMinutes) }}</span>
      </template>

      <template #cell:caloriesBurned="{ row }">
        <span class="text-sm text-slate-600">{{ formatCalories(row.caloriesBurned) }}</span>
      </template>

      <template #cell:createdAt="{ row }">
        <span class="text-xs text-slate-400">{{ formatDate(row.createdAt) }}</span>
      </template>

      <template #actions="{ row }">
        <div class="flex items-center gap-2">
          <button type="button"
            class="rounded-xl border border-transparent bg-blue-50 p-2 text-blue-600 transition hover:border-blue-200 hover:bg-blue-100"
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

    <ExerciseFormModal
      v-model="formModalOpen"
      :exercise="activeExercise"
      :references="references"
      :users="users"
      :is-admin="isAdmin"
      :submitting="formSubmitting"
      :intensities="intensityOptions"
      @submit="handleSubmit"
    />

    <ConfirmDialog
      v-model="confirmOpen"
      :title="t('exercises.title')"
      :message="confirmMessage"
      :confirm-label="t('common.actions.delete')"
      @confirm="handleConfirmDelete"
    />
  </div>
</template>

<script setup>
import { MagnifyingGlassIcon, PencilSquareIcon, PlusIcon, TrashIcon } from '@heroicons/vue/24/outline';
import { onMounted, reactive, ref, watch, computed } from 'vue';
import DataTable from '@/components/DataTable.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import ExerciseFormModal from '@/components/ExerciseFormModal.vue';
import { getExerciseReferences } from '@/services/reference';
import { createExercise, deleteExercise, getExercise, getExercises, updateExercise } from '@/services/exercises';
import { getUsers } from '@/services/users';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import { useI18n } from 'vue-i18n';

const notifications = useNotificationStore();
const auth = useAuthStore();
const { t, locale } = useI18n();

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');

const tableColumns = ref([]);

const buildColumns = () => {
  const columns = [
    { key: 'name', label: t('exercises.table.columns.name'), sortable: true, visible: true },
  ];

  if (isAdmin.value) {
    columns.push({ key: 'userName', label: t('exercises.table.columns.user'), sortable: false, visible: true });
  }

  columns.push(
    { key: 'intensity', label: t('exercises.table.columns.intensity'), sortable: true, visible: true },
    { key: 'durationMinutes', label: t('exercises.table.columns.duration'), sortable: true, visible: true },
    { key: 'caloriesBurned', label: t('exercises.table.columns.calories'), sortable: true, visible: true },
    { key: 'muscleGroup', label: t('exercises.table.columns.muscleGroup'), sortable: true, visible: true },
    { key: 'equipment', label: t('exercises.table.columns.equipment'), sortable: true, visible: true },
    { key: 'createdAt', label: t('exercises.table.columns.createdAt'), sortable: true, visible: true }
  );

  tableColumns.value = columns;
};

const filters = reactive({
  search: '',
  referenceId: '',
  intensity: '',
  userId: '',
  sortField: 'createdAt',
  sortDirection: 'desc',
  page: 0,
  perPage: 10,
});

const pagination = reactive({ page: 1, perPage: 10, total: 0, lastPage: 1, from: 0, to: 0 });

const rows = ref([]);
const loading = ref(false);
const references = ref([]);
const users = ref([]);
const selectedIds = ref([]);
const formModalOpen = ref(false);
const formSubmitting = ref(false);
const activeExercise = ref(null);
const confirmOpen = ref(false);
const pendingDeletion = ref([]);
const searchInput = ref('');
const isViewMounted = ref(false);

const intensityOptions = computed(() => [
  { value: 'LOW', label: t('exercises.intensity.low') },
  { value: 'MODERATE', label: t('exercises.intensity.moderate') },
  { value: 'HIGH', label: t('exercises.intensity.high') },
  { value: 'EXTREME', label: t('exercises.intensity.extreme') },
]);

const formatUserOption = (user) => {
  if (!user) return '';
  const first = user.name ?? '';
  const last = user.lastName ?? '';
  const full = `${first} ${last}`.trim();
  return full || user.email || user.id;
};

const ensureUserFilter = () => {
  if (!isAdmin.value) {
    filters.userId = auth.user?.id ?? '';
  }
};

watch(
  [() => locale.value, isAdmin],
  () => {
    buildColumns();
  },
  { immediate: true }
);

const persistColumns = (updatedColumns) => {
  tableColumns.value = updatedColumns.map((column) => ({ ...column }));
};

const mapMeta = (data, pageData) => ({
  page: (data?.number ?? filters.page) + 1,
  perPage: data?.size ?? filters.perPage,
  total: data?.totalElements ?? pageData.length,
  lastPage: data?.totalPages ?? Math.max(1, Math.ceil((data?.totalElements ?? pageData.length) / filters.perPage)),
  from: pageData.length ? ((data?.number ?? filters.page) * (data?.size ?? filters.perPage)) + 1 : 0,
  to: pageData.length ? ((data?.number ?? filters.page) * (data?.size ?? filters.perPage)) + pageData.length : 0,
});

const fetchExercises = async () => {
  loading.value = true;
  try {
    ensureUserFilter();
    const params = {
      page: filters.page,
      size: filters.perPage,
      sort: `${filters.sortField},${filters.sortDirection}`,
      search: filters.search || undefined,
      referenceId: filters.referenceId || undefined,
      intensity: filters.intensity || undefined,
      userId: filters.userId || undefined,
    };
    const { data } = await getExercises(params);
    const content = data?.content ?? data?.data ?? [];
    rows.value = content.map((item) => ({
      id: item.id,
      displayName: item.customName || item.referenceName,
      referenceName: item.referenceName,
      customName: item.customName,
      intensity: item.intensity,
      durationMinutes: item.durationMinutes,
      caloriesBurned: item.caloriesBurned,
      muscleGroup: item.muscleGroup,
      equipment: item.equipment,
      userId: item.userId,
      userName: item.userName,
      createdAt: item.createdAt,
    }));
    if (isAdmin.value && Array.isArray(content)) {
      const existingIds = new Set(users.value.map((user) => user.id));
      const extras = [];
      content.forEach((item) => {
        if (item.userId && !existingIds.has(item.userId)) {
          extras.push({ id: item.userId, name: item.userName ?? '', lastName: '', email: item.userName ?? item.userId });
          existingIds.add(item.userId);
        }
      });
      if (extras.length) {
        users.value = [...users.value, ...extras];
      }
    }
    Object.assign(pagination, mapMeta(data, content));
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? t('exercises.empty'),
    });
    rows.value = [];
    Object.assign(pagination, { page: 1, perPage: filters.perPage, total: 0, lastPage: 1, from: 0, to: 0 });
  } finally {
    loading.value = false;
  }
};

const fetchReferences = async () => {
  try {
    const { data } = await getExerciseReferences({ language: locale.value });
    references.value = data ?? [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to load exercise references.',
    });
  }
};

const fetchUsers = async () => {
  if (!isAdmin.value) return;
  try {
    const { data } = await getUsers({ size: 50, page: 0, type: 'CLIENT' });
    users.value = data?.content ?? data ?? [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? t('exercises.notifications.loadUsersError'),
    });
  }
};

const handleSort = ({ field, direction }) => {
  filters.sortField = field;
  filters.sortDirection = direction;
  fetchExercises();
};

const handlePage = (page) => {
  filters.page = page - 1;
  fetchExercises();
};

const handlePerPage = (perPage) => {
  filters.perPage = perPage;
  filters.page = 0;
  fetchExercises();
};

const handleFilterChange = () => {
  filters.page = 0;
  fetchExercises();
};

let searchTimeout;
watch(searchInput, (value) => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    filters.search = value;
    filters.page = 0;
    fetchExercises();
  }, 300);
});

watch(
  () => locale.value,
  () => {
    fetchReferences();
  }
);

watch(
  isAdmin,
  async (value) => {
    if (!isViewMounted.value) return;
    if (value) {
      filters.userId = '';
      await fetchUsers();
    } else {
      filters.userId = '';
      ensureUserFilter();
      users.value = [];
    }
    fetchExercises();
  }
);

watch(
  () => auth.user?.id,
  () => {
    if (!isViewMounted.value) return;
    ensureUserFilter();
    fetchExercises();
  }
);

const openCreate = () => {
  activeExercise.value = {
    userId: isAdmin.value ? filters.userId || '' : auth.user?.id ?? '',
  };
  formModalOpen.value = true;
};

const openEdit = async (id) => {
  if (!id) return;
  try {
    const { data } = await getExercise(id);
    activeExercise.value = data;
    formModalOpen.value = true;
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? t('exercises.empty'),
    });
  }
};

const openDelete = (ids) => {
  pendingDeletion.value = ids;
  confirmOpen.value = true;
};

const handleConfirmDelete = async () => {
  confirmOpen.value = false;
  if (!pendingDeletion.value.length) return;
  try {
    await Promise.all(pendingDeletion.value.map((id) => deleteExercise(id)));
    notifications.push({
      type: 'success',
      title: t('exercises.title'),
      message: t('exercises.notifications.deleted'),
    });
    selectedIds.value = [];
    fetchExercises();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? t('exercises.empty'),
    });
  } finally {
    pendingDeletion.value = [];
  }
};

const handleSubmit = async (payload) => {
  formSubmitting.value = true;
  try {
    const body = {
      ...payload,
      referenceId: payload.referenceId || null,
      intensity: payload.intensity || null,
      durationMinutes: payload.durationMinutes ?? null,
      caloriesBurned: payload.caloriesBurned ?? null,
      userId: isAdmin.value ? payload.userId || null : auth.user?.id ?? null,
    };
    if (payload.id) {
      await updateExercise(body);
    } else {
      await createExercise(body);
    }
    notifications.push({
      type: 'success',
      title: t('exercises.title'),
      message: t('exercises.notifications.saved'),
    });
    formModalOpen.value = false;
    activeExercise.value = null;
    fetchExercises();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? t('exercises.form.validation.name'),
    });
  } finally {
    formSubmitting.value = false;
  }
};

const formatIntensity = (value) => {
  if (!value) return '-';
  const key = value.toLowerCase();
  return t(`exercises.intensity.${key}`);
};

const formatDuration = (value) => {
  if (!value) return '-';
  return `${value} min`;
};

const formatCalories = (value) => {
  if (!value) return '-';
  return `${value} kcal`;
};

const formatDate = (value) => {
  if (!value) return '-';
  try {
    return new Intl.DateTimeFormat(locale.value, {
      year: 'numeric',
      month: 'short',
      day: '2-digit',
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

const confirmMessage = computed(() => t('exercises.notifications.confirmDelete'));

onMounted(async () => {
  ensureUserFilter();
  const tasks = [fetchReferences()];
  if (isAdmin.value) {
    tasks.push(fetchUsers());
  }
  await Promise.all(tasks);
  await fetchExercises();
  isViewMounted.value = true;
});
</script>
