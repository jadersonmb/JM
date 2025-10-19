<template>
  <div class="p-8">
    <div class="mb-6 flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-slate-900">{{ $t('roles.title') }}</h1>
      <button
        type="button"
        class="rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-600 hover:bg-slate-100"
        @click="loadUsers"
      >
        {{ $t('common.actions.refresh') || 'Refresh' }}
      </button>
    </div>

    <div v-if="loading" class="flex items-center gap-2 rounded-lg bg-white p-6 text-slate-500 shadow">
      <span class="h-4 w-4 animate-spin rounded-full border-2 border-emerald-200 border-t-emerald-600"></span>
      {{ $t('common.actions.loading') || 'Loading...' }}
    </div>

    <p v-else-if="error" class="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">{{ error }}</p>

    <div v-else class="space-y-4">
      <div
        v-for="user in users"
        :key="user.id"
        class="rounded-lg bg-white p-6 shadow"
      >
        <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div>
            <h3 class="font-semibold text-slate-900">{{ user.name }}</h3>
            <p class="text-sm text-slate-500">{{ user.email }}</p>
          </div>
          <button
            class="mt-3 inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 md:mt-0"
            @click="openRoleModal(user)"
          >
            {{ $t('roles.manage') }}
          </button>
        </div>
        <div class="mt-3 flex flex-wrap gap-2">
          <span
            v-for="role in user.roles"
            :key="role.id"
            class="rounded-full bg-emerald-100 px-3 py-1 text-sm text-emerald-700"
          >
            {{ role.name }}
          </span>
          <span v-if="!user.roles?.length" class="text-sm text-slate-500">{{ $t('roles.empty') }}</span>
        </div>
      </div>
    </div>

    <UserRoleModal
      v-if="showModal && selectedUser"
      :user="selectedUser"
      @close="closeModal"
      @updated="handleUpdated"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import UserRoleModal from '@/components/UserRoleModal.vue';
import { getUsersWithRoles } from '@/services/users';

const users = ref([]);
const loading = ref(false);
const error = ref('');
const showModal = ref(false);
const selectedUser = ref(null);

const loadUsers = async () => {
  loading.value = true;
  error.value = '';
  try {
    const { data } = await getUsersWithRoles();
    users.value = data ?? [];
  } catch (err) {
    error.value = err.response?.data?.details ?? err.message ?? 'Unable to load users.';
  } finally {
    loading.value = false;
  }
};

const openRoleModal = (user) => {
  selectedUser.value = user;
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  selectedUser.value = null;
};

const handleUpdated = async () => {
  await loadUsers();
  closeModal();
};

onMounted(loadUsers);
</script>
