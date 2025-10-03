<template>
  <div class="grid gap-8 xl:grid-cols-[320px_1fr]">
    <section class="card space-y-6">
      <div class="text-center">
        <div class="relative mx-auto h-28 w-28 overflow-hidden rounded-3xl border border-slate-200 bg-primary-50 text-primary-600">
          <img v-if="preview" :src="preview" alt="Avatar" class="h-full w-full object-cover" />
          <div v-else class="flex h-full items-center justify-center text-2xl font-semibold">
            {{ initials(auth.user?.name) }}
          </div>
          <label class="absolute bottom-2 left-1/2 flex -translate-x-1/2 cursor-pointer items-center gap-1 rounded-full bg-white px-3 py-1 text-xs font-semibold text-primary-600 shadow">
            <PhotoIcon class="h-3.5 w-3.5" />
            Change
            <input type="file" accept="image/*" class="hidden" @change="onFileChange" />
          </label>
        </div>
        <h2 class="mt-4 text-xl font-semibold text-slate-900">{{ auth.user?.name }}</h2>
        <p class="text-sm text-slate-500">{{ auth.user?.email }}</p>
      </div>

      <dl class="space-y-4 text-sm">
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Role</dt>
          <dd class="mt-1 font-semibold capitalize text-slate-700">{{ auth.user?.role }}</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Status</dt>
          <dd class="mt-1 font-semibold capitalize text-slate-700">{{ auth.user?.status }}</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Member since</dt>
          <dd class="mt-1 font-semibold text-slate-700">{{ formatDate(auth.user?.created_at || auth.user?.createdAt) }}</dd>
        </div>
      </dl>
    </section>

    <section class="card space-y-6">
      <header>
        <h2 class="text-xl font-semibold text-slate-900">Profile settings</h2>
        <p class="mt-1 text-sm text-slate-500">Update your personal information and password.</p>
      </header>

      <form class="space-y-6" @submit.prevent="submit">
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Full name</label>
            <input v-model="form.name" type="text" class="input" required />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Email</label>
            <input v-model="form.email" type="email" class="input" required />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">New password</label>
            <input v-model="form.password" type="password" class="input" placeholder="Leave blank to keep current" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Confirm password</label>
            <input v-model="form.passwordConfirmation" type="password" class="input" />
          </div>
        </div>

        <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
          <button type="button" class="btn-secondary" @click="reset">Reset</button>
          <button type="submit" class="btn-primary" :disabled="saving">
            <span v-if="saving" class="flex items-center gap-2">
              <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
              Saving...
            </span>
            <span v-else>Save changes</span>
          </button>
        </div>
        <p v-if="errors.password" class="text-sm text-red-600">{{ errors.password }}</p>
      </form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { PhotoIcon } from '@heroicons/vue/24/outline';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';

const auth = useAuthStore();
const notifications = useNotificationStore();

const form = reactive({
  name: auth.user?.name ?? '',
  email: auth.user?.email ?? '',
  password: '',
  passwordConfirmation: '',
});

const errors = reactive({ password: '' });
const file = ref(null);
const preview = ref(auth.user?.avatarUrl ?? auth.user?.avatar ?? '');

watch(
  () => auth.user,
  (user) => {
    if (!user) return;
    form.name = user.name ?? '';
    form.email = user.email ?? '';
    preview.value = user.avatarUrl ?? user.avatar ?? preview.value;
  },
  { immediate: true }
);
const saving = ref(false);

const initials = (name = '') =>
  name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .substring(0, 2)
    .toUpperCase();

const onFileChange = (event) => {
  const selected = event.target.files?.[0];
  if (!selected) return;
  file.value = selected;
  const reader = new FileReader();
  reader.onload = () => {
    preview.value = reader.result;
  };
  reader.readAsDataURL(selected);
};

const formatDate = (value) => {
  if (!value) return 'N/A';
  try {
    return new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

const reset = () => {
  form.name = auth.user?.name ?? '';
  form.email = auth.user?.email ?? '';
  form.password = '';
  form.passwordConfirmation = '';
  file.value = null;
  preview.value = auth.user?.avatarUrl ?? auth.user?.avatar ?? '';
  errors.password = '';
};

const submit = async () => {
  errors.password = '';
  if (form.password && form.password !== form.passwordConfirmation) {
    errors.password = 'Passwords do not match.';
    return;
  }
  const payload = new FormData();
  payload.append('name', form.name);
  payload.append('email', form.email);
  if (form.password) {
    payload.append('password', form.password);
    payload.append('password_confirmation', form.passwordConfirmation);
  }
  if (file.value) {
    payload.append('avatar', file.value);
  }

  saving.value = true;
  try {
    await auth.updateProfile(payload);
    notifications.push({ type: 'success', title: 'Profile updated', message: 'Your account details were saved.' });
    form.password = '';
    form.passwordConfirmation = '';
    file.value = null;
  } catch (error) {
    const message = error.response?.data?.message ?? 'Unable to update profile.';
    notifications.push({ type: 'error', title: 'Update failed', message });
  } finally {
    saving.value = false;
  }
};
</script>