<template>
  <transition name="fade">
    <div
      v-if="visible"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
    >
      <div class="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h2 class="mb-4 text-xl font-semibold text-gray-800">
          {{ $t('firstAccess.title') }}
        </h2>
        <p class="mb-6 text-sm text-gray-600">
          {{ $t('firstAccess.subtitle') }}
        </p>

        <form @submit.prevent="submit">
          <div class="space-y-4">
            <input
              v-model="form.oldPassword"
              type="password"
              class="w-full rounded-lg border p-3"
              :placeholder="$t('firstAccess.current')"
              required
            />
            <input
              v-model="form.newPassword"
              type="password"
              class="w-full rounded-lg border p-3"
              :placeholder="$t('firstAccess.new')"
              required
            />
            <input
              v-model="form.confirmPassword"
              type="password"
              class="w-full rounded-lg border p-3"
              :placeholder="$t('firstAccess.confirm')"
              required
            />
          </div>

          <p v-if="error" class="mt-3 text-sm text-red-500">{{ error }}</p>

          <button
            type="submit"
            class="mt-6 w-full rounded-lg bg-emerald-600 py-3 font-medium text-white transition hover:bg-emerald-700"
            :disabled="loading"
          >
            <span v-if="loading" class="flex items-center justify-center gap-2">
              <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
              {{ $t('firstAccess.saving') }}
            </span>
            <span v-else>
              {{ $t('firstAccess.save') }}
            </span>
          </button>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import api from '@/services/api';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  userId: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['updated']);

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const error = ref('');
const loading = ref(false);
const { t } = useI18n();

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      form.oldPassword = '';
      form.newPassword = '';
      form.confirmPassword = '';
      error.value = '';
    }
  }
);

async function submit() {
  error.value = '';

  if (form.newPassword !== form.confirmPassword) {
    error.value = t('firstAccess.mismatch');
    return;
  }

  loading.value = true;
  try {
    await api.put('/api/v1/users/change-password-first-access', {
      userId: props.userId,
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
    });
    emit('updated');
  } catch (err) {
    error.value = err.response?.data?.details || err.response?.data?.message || t('firstAccess.error');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
