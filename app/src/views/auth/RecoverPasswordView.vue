<template>
  <div class="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-12">
    <div class="w-full max-w-md space-y-6">
      <div class="card">
        <header class="space-y-1">
          <h1 class="text-2xl font-semibold text-slate-900">Recover password</h1>
          <p class="text-sm text-slate-500">Provide your email and a new password to regain access.</p>
        </header>
        <form class="mt-6 space-y-5" @submit.prevent="handleSubmit">
          <div :class="['floating-input', errors.email ? 'input-error' : '']">
            <input v-model="form.email" id="recover-email" type="email" required placeholder="E-mail" class="input"/>
            <p v-if="errors.email" class="text-xs text-red-500">{{ errors.email }}</p>
          </div>
          <div :class="['floating-input', errors.newPassword ? 'input-error' : '']">
            <input v-model="form.newPassword" id="recover-password" type="password" required placeholder="New password" class="input"/>
            <p v-if="errors.newPassword" class="text-xs text-red-500">{{ errors.newPassword }}</p>
          </div>
          <button type="submit" class="btn-primary w-full">Update password</button>
          <RouterLink to="/login" class="block text-center text-sm font-semibold text-primary-600">Back to login</RouterLink>
        </form>
      </div>
      <p v-if="success" class="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">Password updated successfully. You can sign in now.</p>
      <p v-if="errors.form" class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{ errors.form }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();

const form = reactive({
  email: '',
  newPassword: '',
});

const errors = reactive({
  email: '',
  newPassword: '',
  form: '',
});

const success = ref(false);

const resetErrors = () => {
  errors.email = '';
  errors.newPassword = '';
  errors.form = '';
  success.value = false;
};

const handleSubmit = async () => {
  resetErrors();
  try {
    await auth.recoverPassword({ email: form.email, newPassword: form.newPassword });
    success.value = true;
    setTimeout(() => router.push({ name: 'login' }), 1200);
  } catch (error) {
    const response = error.response;
    if (response?.data?.details) {
      errors.form = response.data.details;
    } else {
      errors.form = 'Unable to update password. Please verify your information and try again.';
    }
  }
};
</script>