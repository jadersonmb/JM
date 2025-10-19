<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/70 px-4">
    <div class="w-full max-w-xl rounded-2xl bg-white p-6 shadow-xl">
      <header class="flex items-center justify-between border-b border-slate-200 pb-4">
        <div>
          <h2 class="text-xl font-semibold text-slate-900">{{ user.name }}</h2>
          <p class="text-sm text-slate-500">{{ user.email }}</p>
        </div>
        <button
          type="button"
          class="text-slate-400 hover:text-slate-600"
          @click="close"
        >
          ✕
        </button>
      </header>

      <section class="mt-4">
        <p class="text-sm text-slate-600">{{ $t('roles.manage') }}</p>
        <div v-if="loading" class="mt-4 flex items-center gap-2 text-sm text-slate-500">
          <span class="h-4 w-4 animate-spin rounded-full border-2 border-emerald-200 border-t-emerald-600"></span>
          {{ $t('common.actions.loading') || 'Loading...' }}
        </div>
        <div v-else class="mt-4 space-y-3 max-h-64 overflow-y-auto pr-2">
          <label
            v-for="role in roles"
            :key="role.id"
            class="flex cursor-pointer items-start gap-3 rounded-lg border border-slate-200 p-3 hover:border-emerald-500"
          >
            <input
              type="checkbox"
              class="mt-1 h-4 w-4 rounded border-slate-300 text-emerald-600 focus:ring-emerald-500"
              :value="role.id"
              v-model="selected"
            />
            <div>
              <p class="font-medium text-slate-900">{{ role.name }}</p>
              <p class="text-xs text-slate-500">{{ role.description }}</p>
            </div>
          </label>
        </div>
        <p v-if="error" class="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{{ error }}</p>
      </section>

      <footer class="mt-6 flex justify-end gap-3">
        <button
          type="button"
          class="rounded-lg border border-slate-200 px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
          @click="close"
        >
          {{ $t('common.actions.cancel') }}
        </button>
        <button
          type="button"
          class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300"
          :disabled="saving"
          @click="save"
        >
          <span v-if="saving" class="flex items-center gap-2">
            <span class="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white"></span>
            {{ $t('common.actions.saving') || 'Saving...' }}
          </span>
          <span v-else>{{ $t('common.actions.save') }}</span>
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import { getRoles } from '@/services/roles';
import { updateUserRoles } from '@/services/users';

const props = defineProps({
  user: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['close', 'updated']);

const roles = ref([]);
const selected = ref([]);
const loading = ref(false);
const saving = ref(false);
const error = ref('');

const load = async () => {
  loading.value = true;
  error.value = '';
  try {
    const { data } = await getRoles();
    roles.value = data ?? [];
    selected.value = props.user.roles?.map((role) => role.id) ?? [];
  } catch (err) {
    error.value = err.response?.data?.details ?? err.message ?? 'Unable to load roles.';
  } finally {
    loading.value = false;
  }
};

const save = async () => {
  saving.value = true;
  error.value = '';
  try {
    await updateUserRoles(props.user.id, { roleIds: selected.value });
    emit('updated');
    close();
  } catch (err) {
    error.value = err.response?.data?.details ?? err.message ?? 'Unable to update roles.';
  } finally {
    saving.value = false;
  }
};

const close = () => emit('close');

watch(
  () => props.user,
  (user) => {
    if (user) {
      selected.value = user.roles?.map((role) => role.id) ?? [];
    }
  },
  { immediate: true }
);

onMounted(load);
</script>
