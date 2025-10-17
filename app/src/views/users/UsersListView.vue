<template>
  <div class="space-y-0">
    <DataTable title="User management" subtitle="Search, review, and manage users connected to your Java backend."
      :columns="tableColumns" :rows="users.items" :loading="loading" :selected="selectedIds"
      :sort="{ field: filters.sortField, direction: filters.sortDirection }" :pagination="users.meta"
      empty-state="No users found with the current filters." @update:selected="(value) => (selectedIds.value = value)"
      @change:sort="handleSort" @change:page="handlePage" @change:per-page="handlePerPage"
      @change:columns="persistColumns" @refresh="fetchUsers">
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
          :disabled="selected.length === 0" @click="openBulkDelete">
          <TrashIcon class="h-4 w-4" />
          <span>{{ t('common.actions.delete')}}</span>
        </button>
      </template>

      <template #filters>
        <div class="md:col-span-2">
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">Search</label>
          <div class="relative mt-1">
            <MagnifyingGlassIcon
              class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input v-model="searchInput" type="search" class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg
           focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent  text-sm
           placeholder:text-gray-400" placeholder="Name or email" />
          </div>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">Role</label>
          <select v-model="filters.role" class="input mt-1">
            <option value="all">All roles</option>
            <option value="CLIENT">Client</option>
            <option value="ADMIN">Administrator</option>
          </select>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">Status</label>
          <select v-model="filters.status" class="input mt-1">
            <option value="all">All statuses</option>
            <option value="active">Active</option>
            <option value="inactive">Inactive</option>
          </select>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">Country</label>
          <select v-model="filters.countryId" class="input mt-1">
            <option value="">All countries</option>
            <option v-for="country in countries" :key="country.id" :value="country.id">{{ country.name }}</option>
          </select>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">City</label>
          <select v-model="filters.cityId" class="input mt-1" :disabled="!filters.countryId || cities.length === 0">
            <option value="">All cities</option>
            <option v-for="city in cities" :key="city.id" :value="city.id">{{ city.name }}</option>
          </select>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">Education</label>
          <select v-model="filters.educationLevelId" class="input mt-1">
            <option value="">All education levels</option>
            <option v-for="level in educationLevels" :key="level.id" :value="level.id">{{ level.name }}</option>
          </select>
        </div>
        <div>
          <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">Profession</label>
          <select v-model="filters.professionId" class="input mt-1">
            <option value="">All professions</option>
            <option v-for="profession in professions" :key="profession.id" :value="profession.id">
              {{ profession.name }}
            </option>
          </select>
        </div>
      </template>

      <template #cell:name="{ row }">
        <div class="flex items-center gap-3">
          <div class="h-9 w-9 overflow-hidden rounded-2xl bg-primary-50 text-primary-600">
            <img v-if="row.avatarUrl" :src="row.avatarUrl" class="h-full w-full object-cover" alt="" />
            <div v-else class="flex h-full items-center justify-center text-xs font-semibold">
              {{ initials(row.name) }}
            </div>
          </div>
          <div>
            <p class="font-semibold text-slate-900">{{ row.name }}</p>
            <p class="text-xs text-slate-400">#{{ row.id }}</p>
          </div>
        </div>
      </template>

      <template #cell:email="{ row }">
        <span class="font-medium text-slate-600">{{ row.email }}</span>
      </template>

      <template #cell:role="{ row }">
        <span class="badge bg-slate-100 text-slate-600 capitalize">{{ formatRole(row.role) }}</span>
      </template>

      <template #cell:status="{ row }">
        <span class="badge capitalize"
          :class="row.status === 'active' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'">
          {{ row.status }}
        </span>
      </template>

      <template #cell:country="{ row }">
        <span class="text-sm text-slate-600">{{ row.country || '—' }}</span>
      </template>

      <template #cell:city="{ row }">
        <span class="text-sm text-slate-600">{{ row.city || '—' }}</span>
      </template>

      <template #cell:educationLevel="{ row }">
        <span class="text-sm text-slate-600">{{ row.educationLevel || '—' }}</span>
      </template>

      <template #cell:profession="{ row }">
        <span class="text-sm text-slate-600">{{ row.profession || '—' }}</span>
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
            @click="prepareDelete(row.id)">
            <TrashIcon class="h-3 w-3" />
            <span class="sr-only">{{ t('common.actions.delete') }}</span>
          </button>
        </div>
      </template>
    </DataTable>

    <UserFormModal
      v-model="formModalOpen"
      :user="activeUser"
      :submitting="formSubmitting"
      :countries="countries"
      :cities="modalCities"
      :education-levels="educationLevels"
      :professions="professions"
      :loading-cities="modalCitiesLoading"
      @fetch-cities="handleFetchCitiesForModal"
      @submit="handleSubmit"
    />

    <ConfirmDialog v-model="confirmOpen" :title="confirmTitle" :message="confirmMessage" confirm-label="Delete"
      @confirm="handleConfirmDelete" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import {
  MagnifyingGlassIcon,
  PencilSquareIcon,
  PlusIcon,
  TrashIcon,
} from '@heroicons/vue/24/outline';
import DataTable from '@/components/DataTable.vue';
import UserFormModal from '@/components/UserFormModal.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import { useNotificationStore } from '@/stores/notifications';
import { useAuthStore } from '@/stores/auth';
import { createUser, deleteUser, getUser, getUsers, updateUser } from '@/services/users';
import { uploadFile } from '@/services/cloudFlare';
import { getUserSettings } from '@/services/settings';
import { getCountries, getCities, getEducationLevels, getProfessions } from '@/services/reference';
import { useI18n } from 'vue-i18n';

const notifications = useNotificationStore();
const auth = useAuthStore();
const { t, locale } = useI18n();

const FILTER_STORAGE_KEY = 'users.table.filters';

const filters = reactive({
  search: '',
  role: 'all',
  status: 'all',
  countryId: '',
  cityId: '',
  educationLevelId: '',
  professionId: '',
  sortField: 'name',
  sortDirection: 'desc',
  page: 0,
  perPage: 10,
});

const searchInput = ref('');
const loading = ref(false);
const users = reactive({
  items: [],
  meta: { page: 1, perPage: 10, total: 0, lastPage: 1, from: 0, to: 0 },
});

const countries = ref([]);
const cities = ref([]);
const educationLevels = ref([]);
const professions = ref([]);
const modalCities = ref([]);
const modalCitiesLoading = ref(false);
const userSettings = ref({ language: '' });
const userLanguage = computed(() => userSettings.value.language || locale.value);

let searchTimeout = null;

watch(searchInput, (value) => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    filters.search = value;
    filters.page = 0;
    fetchUsers();
  }, 300);
});

watch(
  () => [
    filters.role,
    filters.status,
    filters.countryId,
    filters.cityId,
    filters.educationLevelId,
    filters.professionId,
  ],
  () => {
    filters.page = 0;
    fetchUsers();
  }
);

watch(filters, () => persistFilters(), { deep: true });

watch(
  () => filters.countryId,
  async (countryId, previous) => {
    if (countryId !== previous) {
      filters.cityId = '';
      await loadCities(countryId);
    }
  }
);

watch(userLanguage, async () => {
  await loadReferenceData();
  await loadCities(filters.countryId, true);
});

const tableColumns = reactive(loadColumns());

const selectedIds = ref([]);
const formModalOpen = ref(false);
const confirmOpen = ref(false);
const confirmTitle = ref('Delete users');
const confirmMessage = ref('This action cannot be undone.');
const deleteQueue = ref([]);
const formSubmitting = ref(false);
const activeUser = ref(null);

onMounted(async () => {
  loadFilters();
  await loadSettings();
  await loadReferenceData();
  await loadCities(filters.countryId, true);
  await fetchUsers();
});

onBeforeUnmount(() => {
  clearTimeout(searchTimeout);
});

function loadFilters() {
  try {
    const stored = JSON.parse(localStorage.getItem(FILTER_STORAGE_KEY) ?? '{}');
    if (stored.search !== undefined) {
      filters.search = stored.search;
      searchInput.value = stored.search;
    }
    if (stored.role) {
      if (stored.role === 'all') {
        filters.role = 'all';
      } else {
        const normalizedRole = stored.role.toString().toUpperCase();
        if (['ADMIN', 'CLIENT'].includes(normalizedRole)) {
          filters.role = normalizedRole;
        }
      }
    }
    if (stored.status) filters.status = stored.status;
    if (stored.perPage) filters.perPage = stored.perPage;
    if (stored.sortField) filters.sortField = stored.sortField;
    if (stored.sortDirection) filters.sortDirection = stored.sortDirection;
    if (stored.countryId) filters.countryId = stored.countryId;
    if (stored.cityId) filters.cityId = stored.cityId;
    if (stored.educationLevelId) filters.educationLevelId = stored.educationLevelId;
    if (stored.professionId) filters.professionId = stored.professionId;
    if (stored.page) filters.page = stored.page;
  } catch {
    console.error('Failed to load filters');
  }
}

function persistFilters() {
  const snapshot = {
    search: filters.search,
    role: filters.role,
    status: filters.status,
    countryId: filters.countryId,
    cityId: filters.cityId,
    educationLevelId: filters.educationLevelId,
    professionId: filters.professionId,
    perPage: filters.perPage,
    sortField: filters.sortField,
    sortDirection: filters.sortDirection,
    page: filters.page,
  };
  localStorage.setItem(FILTER_STORAGE_KEY, JSON.stringify(snapshot));
}

async function loadSettings() {
  if (!auth.user?.id) {
    userSettings.value = { language: locale.value };
    return;
  }
  try {
    const { data } = await getUserSettings(auth.user.id);
    userSettings.value = { language: data?.language ?? locale.value };
  } catch (error) {
    console.error('Failed to load user settings', error);
    userSettings.value = { language: locale.value };
  }
}

async function loadReferenceData() {
  try {
    const params = userSettings.value;
    const [countriesResponse, educationResponse, professionResponse] = await Promise.all([
      getCountries(params),
      getEducationLevels(params),
      getProfessions(params),
    ]);
    countries.value = countriesResponse.data ?? [];
    educationLevels.value = educationResponse.data ?? [];
    professions.value = professionResponse.data ?? [];
  } catch (error) {
    console.error('Failed to load reference data', error);
  }
}

async function loadCities(countryId, preserveSelection = false) {
  if (!countryId) {
    cities.value = [];
    if (!preserveSelection) {
      filters.cityId = '';
    }
    return;
  }
  try {
    const data = await fetchCitiesByCountryId(countryId);
    cities.value = data;
    if (filters.cityId && !cities.value.some((city) => city.id === filters.cityId) && !preserveSelection) {
      filters.cityId = '';
    }
  } catch (error) {
    console.error('Failed to load cities', error);
    cities.value = [];
    if (!preserveSelection) {
      filters.cityId = '';
    }
  }
}

const resolveReferenceName = (collectionRef, id, fallback = '') => {
  if (!id) return fallback;
  const list = Array.isArray(collectionRef.value) ? collectionRef.value : [];
  const match = list.find((entry) => entry.id === id);
  return match?.name ?? fallback;
};

function normalizeUser(item = {}) {
  const role = (item.type ?? item.role ?? 'CLIENT').toString().toUpperCase();
  const countryId = item.countryId ?? item.countryDTO?.id ?? null;
  const educationLevelId = item.educationLevelId ?? item.educationLevel?.id ?? null;
  const professionId = item.professionId ?? item.profession?.id ?? null;
  const countryName = item.countryDTO?.name ?? item.countryName
    ?? resolveReferenceName(countries, countryId, '');
  const educationName = item.educationLevelName ?? item.educationLevel?.name
    ?? resolveReferenceName(educationLevels, educationLevelId, '');
  const professionName = item.professionName ?? item.profession?.name
    ?? resolveReferenceName(professions, professionId, '');
  return {
    id: item.id,
    name: item.name ?? '',
    lastName: item.lastName ?? '',
    email: item.email ?? '',
    role,
    type: role,
    status: item.status ?? 'active',
    avatarUrl: item.avatarUrl ?? item.avatar ?? null,
    createdAt: item.createdAt ?? item.created_at ?? null,
    documentNumber: item.documentNumber ?? '',
    phoneNumber: item.phoneNumber ?? '',
    street: item.street ?? '',
    state: item.state ?? '',
    postalCode: item.postalCode ?? '',
    countryId,
    country: countryName,
    cityId: item.cityId ?? item.city?.id ?? null,
    city: item.cityName ?? item.city?.name ?? '',
    educationLevelId,
    educationLevel: educationName,
    professionId,
    profession: professionName,
  };
}

async function fetchCitiesByCountryId(countryId) {
  if (!countryId) {
    return [];
  }
  try {
    const { data } = await getCities(countryId, userSettings.value);
    return data ?? [];
  } catch (error) {
    console.error('Failed to load cities', error);
    return [];
  }
}

const buildUserPayload = (payload, userId) => {
  const sanitize = (value) => (value === '' || value === null || value === undefined ? undefined : value);
  const role = (payload.role || 'CLIENT').toString().toUpperCase();
  const body = {
    id: userId,
    name: payload.name,
    lastName: payload.lastName || undefined,
    email: payload.email,
    type: role,
    documentNumber: payload.documentNumber || undefined,
    phoneNumber: payload.phoneNumber || undefined,
    street: payload.street || undefined,
    state: payload.state || undefined,
    postalCode: payload.postalCode || undefined,
    countryId: sanitize(payload.countryId),
    cityId: sanitize(payload.cityId),
    educationLevelId: sanitize(payload.educationLevelId),
    professionId: sanitize(payload.professionId),
  };
  if (!userId) {
    delete body.id;
  }
  if (payload.password) {
    body.password = payload.password;
  }
  if (payload.avatarUrl !== undefined) {
    body.avatarUrl = payload.avatarUrl || undefined;
  }
  return body;
};

async function fetchUsers() {
  loading.value = true;
  try {
    const params = {
      page: filters.page,
      size: filters.perPage,
      search: filters.search || undefined,
      type: filters.role !== 'all' ? filters.role : undefined,
      status: filters.status !== 'all' ? filters.status : undefined,
      sort: `${filters.sortField},${filters.sortDirection}`,
      countryId: filters.countryId || undefined,
      cityId: filters.cityId || undefined,
      educationLevelId: filters.educationLevelId || undefined,
      professionId: filters.professionId || undefined,
    };

    const { data } = await getUsers(params);
    const pageData = data?.data ?? data?.content ?? data ?? [];
    const meta = data?.meta ?? {
      page: data?.page ?? filters.page,
      perPage: data?.perPage ?? filters.perPage,
      total: data?.totalElements ?? data?.total ?? pageData.length,
      lastPage:
        data?.totalPages ?? Math.max(1, Math.ceil((data?.totalElements ?? pageData.length) / filters.perPage)),
      from: data?.from ?? (pageData.length ? (filters.page - 1) * filters.perPage + 1 : 0),
      to: data?.to ?? (pageData.length ? (filters.page - 1) * filters.perPage + pageData.length : 0),
    };

    users.items = pageData.map((item) => normalizeUser(item));

    users.meta = {
      page: meta.page ?? filters.page,
      perPage: meta.perPage ?? filters.perPage,
      total: meta.total ?? users.items.length,
      lastPage: meta.lastPage ?? meta.totalPages ?? 1,
      from: meta.from ?? 0,
      to: meta.to ?? users.items.length,
    };
  } catch (error) {
    notifications.push({
      type: 'error',
      title: 'Fetch failed',
      message: error.response?.data?.message ?? 'Unable to load users.',
    });
    users.items = [];
    users.meta = { page: filters.page, perPage: filters.perPage, total: 0, lastPage: 1, from: 0, to: 0 };
  } finally {
    loading.value = false;
  }
}

const handleSort = ({ field, direction }) => {
  filters.sortField = field;
  filters.sortDirection = direction;
  fetchUsers();
};

const handlePage = (page) => {
  filters.page = page;
  fetchUsers();
};

const handlePerPage = (perPage) => {
  filters.perPage = perPage;
  filters.page = 1;
  fetchUsers();
};

const persistColumns = (columns) => {
  localStorage.setItem('users.table.columns', JSON.stringify(columns));
  tableColumns.splice(0, tableColumns.length, ...columns);
};

const handleFetchCitiesForModal = async (countryId) => {
  modalCitiesLoading.value = true;
  try {
    modalCities.value = countryId ? await fetchCitiesByCountryId(countryId) : [];
  } catch (error) {
    console.error('Failed to load modal cities', error);
    modalCities.value = [];
  } finally {
    modalCitiesLoading.value = false;
  }
};

const formatRole = (value) => {
  if (!value) return 'Client';
  const normalized = value.toString().toUpperCase();
  return normalized === 'ADMIN' ? 'Admin' : 'Client';
};

function loadColumns() {
  const defaults = [
    { key: 'name', label: 'Name', sortable: true, visible: true },
    { key: 'email', label: 'Email', sortable: true, visible: true },
    { key: 'role', label: 'Role', sortable: true, visible: true },
    { key: 'status', label: 'Status', sortable: true, visible: true },
    { key: 'country', label: 'Country', sortable: false, visible: true },
    { key: 'city', label: 'City', sortable: false, visible: true },
    { key: 'educationLevel', label: 'Education', sortable: false, visible: false },
    { key: 'profession', label: 'Profession', sortable: false, visible: false },
    { key: 'createdAt', label: 'Created', sortable: true, visible: true },
  ];
  try {
    const stored = localStorage.getItem('users.table.columns');
    if (!stored) return defaults;
    const parsed = JSON.parse(stored);
    return parsed.length ? parsed : defaults;
  } catch {
    return defaults;
  }
}

const initials = (name = '') => {
  return name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .substring(0, 2)
    .toUpperCase();
};

const formatDate = (value) => {
  if (!value) return 'N/A';
  try {
    return new Intl.DateTimeFormat('en', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

const openCreate = () => {
  activeUser.value = null;
  modalCities.value = [];
  formModalOpen.value = true;
};

const findUserById = (id) => users.items.find((user) => user.id === id);

const openEdit = async (id) => {
  try {
    modalCities.value = [];
    const { data } = await getUser(id);
    const user = normalizeUser(data);
    activeUser.value = user;
    await handleFetchCitiesForModal(user.countryId ?? '');
    formModalOpen.value = true;
  } catch (error) {
    console.error('Failed to load user details', error);
    const fallback = findUserById(id);
    if (fallback) {
      activeUser.value = fallback;
      await handleFetchCitiesForModal(fallback.countryId ?? '');
      formModalOpen.value = true;
      notifications.push({
        type: 'warning',
        title: 'Partial data loaded',
        message: 'Unable to load the latest user data. Showing cached information.',
      });
    } else {
      notifications.push({
        type: 'error',
        title: 'Load failed',
        message: error.response?.data?.message ?? 'Unable to load user details.',
      });
    }
  }
};

const openBulkDelete = () => {
  if (!selectedIds.value.length) return;
  deleteQueue.value = [...selectedIds.value];
  confirmTitle.value = `Delete ${deleteQueue.value.length} users`;
  confirmMessage.value = 'Deleted users will lose access immediately. Proceed?';
  confirmOpen.value = true;
};

const prepareDelete = (id) => {
  deleteQueue.value = [id];
  confirmTitle.value = 'Delete user';
  confirmMessage.value = 'The user will be permanently removed.';
  confirmOpen.value = true;
};

const handleSubmit = async (payload) => {
  formSubmitting.value = true;
  try {
    const userId = activeUser.value?.id ?? null;
    if (userId) {
      const updateBody = buildUserPayload(payload, userId);
      await updateUser(updateBody);

      if (payload.avatarFile) {
        const params = {
          file: payload.avatarFile,
          userId,
        };
        const { data: imageDTO } = await uploadFile(params);
        if (imageDTO?.url) {
          await updateUser({ id: userId, avatarUrl: imageDTO.url });
        }
      }

      notifications.push({ type: 'success', title: 'Patient updated', message: `${payload.name} has been updated.` });
    } else {
      const createBody = buildUserPayload(payload);
      const { data } = await createUser(createBody);
      const newUserId = data?.id;

      if (payload.avatarFile && newUserId) {
        const params = {
          file: payload.avatarFile,
          userId: newUserId,
        };
        const { data: imageDTO } = await uploadFile(params);
        if (imageDTO?.url) {
          await updateUser({ id: newUserId, avatarUrl: imageDTO.url });
        }
      }

      notifications.push({ type: 'success', title: 'Patient created', message: `${payload.name} has been added.` });
    }
    formModalOpen.value = false;
    activeUser.value = null;
    selectedIds.value = [];
    await fetchUsers();
  } catch (error) {
    const message = error.response?.data?.message ?? 'Unable to save user.';
    notifications.push({ type: 'error', title: 'Save failed', message });
  } finally {
    formSubmitting.value = false;
  }
};

const handleConfirmDelete = async () => {
  if (!deleteQueue.value.length) return;
  try {
    await Promise.all(deleteQueue.value.map((id) => deleteUser(id)));
    notifications.push({
      type: 'success',
      title: 'Patient deleted',
      message: `${deleteQueue.value.length} user(s) removed successfully.`,
    });
    selectedIds.value = [];
    fetchUsers();
  } catch (error) {
    const message = error.response?.data?.message ?? 'Unable to delete patient.';
    notifications.push({ type: 'error', title: 'Delete failed', message });
  }
};
</script>
