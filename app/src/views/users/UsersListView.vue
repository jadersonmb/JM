<template>
  <div class="space-y-8">
    <DataTable title="User management" subtitle="Search, review, and manage users connected to your Java backend."
      :columns="tableColumns" :rows="users.items" :loading="loading" :selected="selectedIds"
      :sort="{ field: filters.sortField, direction: filters.sortDirection }" :pagination="users.meta"
      empty-state="No users found with the current filters." @update:selected="(value) => (selectedIds.value = value)"
      @change:sort="handleSort" @change:page="handlePage" @change:per-page="handlePerPage"
      @change:columns="persistColumns" @refresh="fetchUsers">
      <template #toolbar="{ selected }">
        <button type="button" class="btn-primary" @click="openCreate">
          <PlusIcon class="h-4 w-4" />
          <span>New user</span>
        </button>
        <button type="button" class="btn-secondary" :disabled="selected.length !== 1" @click="openEdit(selected[0])">
          <PencilSquareIcon class="h-4 w-4" />
          <span>Edit</span>
        </button>
        <button type="button" class="btn-secondary text-red-600 hover:border-red-200 hover:text-red-600"
          :disabled="selected.length === 0" @click="openBulkDelete">
          <TrashIcon class="h-4 w-4" />
          <span>Delete</span>
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
            <option value="admin">Administrator</option>
            <option value="moderator">Moderator</option>
            <option value="user">User</option>
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
        <span class="badge bg-slate-100 text-slate-600 capitalize">{{ row.role }}</span>
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
          <button type="button" class="text-sm font-semibold text-primary-600" @click="openEdit(row.id)">
            Edit
          </button>
          <button type="button" class="text-sm font-semibold text-red-500" @click="prepareDelete(row.id)">
            Delete
          </button>
        </div>
      </template>
    </DataTable>

    <UserFormModal v-model="formModalOpen" :user="activeUser" :submitting="formSubmitting" @submit="handleSubmit" />

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
import { createUser, deleteUser, getUsers, updateUser } from '@/services/users';
import { uploadFile } from '@/services/cloudFlare';
import { getUserSettings } from '@/services/settings';
import { getCountries, getCities, getEducationLevels, getProfessions } from '@/services/reference';
import { useI18n } from 'vue-i18n';

const notifications = useNotificationStore();
const auth = useAuthStore();
const { locale } = useI18n();

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
  page: 1,
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
const userSettings = ref({ language: '' });
const userLanguage = computed(() => userSettings.value.language || locale.value);

let searchTimeout = null;

watch(searchInput, (value) => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    filters.search = value;
    filters.page = 1;
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
    filters.page = 1;
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

const referenceParams = computed(() => (userLanguage.value ? { language: userLanguage.value } : {}));

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
    if (stored.role) filters.role = stored.role;
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
    const params = referenceParams.value;
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
    const { data } = await getCities(countryId, referenceParams.value);
    cities.value = data ?? [];
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

async function fetchUsers() {
  loading.value = true;
  try {
    const params = {
      page: filters.page,
      size: filters.perPage,
      search: filters.search || undefined,
      role: filters.role !== 'all' ? filters.role : undefined,
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

    users.items = pageData.map((item) => ({
      id: item.id,
      name: item.name,
      email: item.email,
      role: item.role,
      status: item.status,
      avatarUrl: item.avatarUrl ?? item.avatar ?? null,
      createdAt: item.createdAt ?? item.created_at,
      country: item.countryDTO?.name ?? item.countryName ?? '',
      city: item.cityName ?? '',
      educationLevel: item.educationLevelName ?? '',
      profession: item.professionName ?? '',
    }));

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
  formModalOpen.value = true;
};

const findUserById = (id) => users.items.find((user) => user.id === id);

const openEdit = (id) => {
  const user = findUserById(id);
  if (!user) return;
  activeUser.value = user;
  formModalOpen.value = true;
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
    const formData = new FormData();
    formData.append('name', payload.name);
    formData.append('email', payload.email);
    formData.append('role', payload.role);
    formData.append('status', payload.status);
    if (payload.password) {
      formData.append('password', payload.password);
    }
    if (payload.avatarFile) {
      formData.append('avatar', payload.avatarFile);
    }

    if (activeUser.value?.id) {
      formData.append('id', activeUser.value?.id);
      if (payload.avatarFile != null) {
        const params = {
          file: payload.avatarFile,
          userId: activeUser.value?.id,
        };
        await uploadFile(params);
      }
      await updateUser(formData);
      notifications.push({ type: 'success', title: 'User updated', message: `${payload.name} has been updated.` });
    } else {
      const { data } = await createUser(formData);
      if (payload.avatarFile != null) {
        const params = {
          file: payload.avatarFile,
          userId: data?.id,
        };

        const { data: imageDTO } = await uploadFile(params);
        formData.append('avatarUrl', imageDTO?.url);
        formData.append('id', data?.id);
        await updateUser(formData);
      }
      notifications.push({ type: 'success', title: 'User created', message: `${payload.name} has been added.` });
    }
    formModalOpen.value = false;
    fetchUsers();
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
      title: 'Users deleted',
      message: `${deleteQueue.value.length} user(s) removed successfully.`,
    });
    selectedIds.value = [];
    fetchUsers();
  } catch (error) {
    const message = error.response?.data?.message ?? 'Unable to delete users.';
    notifications.push({ type: 'error', title: 'Delete failed', message });
  }
};
</script>
