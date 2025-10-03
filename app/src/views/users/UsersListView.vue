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
import { createUser, deleteUser, getUsers, updateUser } from '@/services/users';

const notifications = useNotificationStore();

const FILTER_STORAGE_KEY = 'users.table.filters';

const filters = reactive({
  search: '',
  role: 'all',
  status: 'all',
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

let searchTimeout = null;

watch(searchInput, (value) => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    filters.search = value;
    filters.page = 1;
    fetchUsers();
  }, 300);
});

watch(() => [filters.role, filters.status], () => {
  filters.page = 1;
  fetchUsers();
});

watch(filters, () => persistFilters(), { deep: true });

const tableColumns = reactive(loadColumns());

const selectedIds = ref([]);
const formModalOpen = ref(false);
const confirmOpen = ref(false);
const confirmTitle = ref('Delete users');
const confirmMessage = ref('This action cannot be undone.');
const deleteQueue = ref([]);
const formSubmitting = ref(false);
const activeUser = ref(null);

onMounted(() => {
  loadFilters();
  fetchUsers();
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
  } catch {
    console.error('Failed to load filters');
  }
}

function persistFilters() {
  const snapshot = {
    search: filters.search,
    role: filters.role,
    status: filters.status,
    perPage: filters.perPage,
    sortField: filters.sortField,
    sortDirection: filters.sortDirection,
  };
  localStorage.setItem(FILTER_STORAGE_KEY, JSON.stringify(snapshot));
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
}

const handlePage = (page) => {
  filters.page = page;
  fetchUsers();
}

const handlePerPage = (perPage) => {
  filters.perPage = perPage;
  filters.page = 1;
  fetchUsers();
}

const persistColumns = (columns) => {
  localStorage.setItem('users.table.columns', JSON.stringify(columns));
  tableColumns.splice(0, tableColumns.length, ...columns);
}

function loadColumns() {
  const defaults = [
    { key: 'name', label: 'Name', sortable: true, visible: true },
    { key: 'email', label: 'Email', sortable: true, visible: true },
    { key: 'role', label: 'Role', sortable: true, visible: true },
    { key: 'status', label: 'Status', sortable: true, visible: true },
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
}

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
}

const openCreate = () => {
  activeUser.value = null;
  formModalOpen.value = true;
}

const findUserById = (id) => users.items.find((user) => user.id === id);

const openEdit = (id) => {
  const user = findUserById(id);
  if (!user) return;
  activeUser.value = user;
  formModalOpen.value = true;
}

const openBulkDelete = () => {
  if (!selectedIds.value.length) return;
  deleteQueue.value = [...selectedIds.value];
  confirmTitle.value = `Delete ${deleteQueue.value.length} users`;
  confirmMessage.value = 'Deleted users will lose access immediately. Proceed?';
  confirmOpen.value = true;
}

const prepareDelete = (id) => {
  deleteQueue.value = [id];
  confirmTitle.value = 'Delete user';
  confirmMessage.value = 'The user will be permanently removed.';
  confirmOpen.value = true;
}
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
      await updateUser(formData);
      notifications.push({ type: 'success', title: 'User updated', message: `${payload.name} has been updated.` });
    } else {
      await createUser(formData);
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
}

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
}
</script>