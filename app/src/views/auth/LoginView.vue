<template>
  <div class="grid min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-800 px-4 py-12">
    <div
      class="relative mx-auto w-full max-w-5xl overflow-hidden rounded-3xl border border-white/10 bg-white/5 p-10 shadow-[0_30px_80px_-40px_rgba(15,23,42,1)] backdrop-blur-xl lg:grid lg:grid-cols-[1.2fr_1fr]">
      <div
        class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(59,130,246,0.35),_rgba(15,23,42,0))]">
      </div>
      <div class="relative flex flex-col justify-between gap-10 text-white">
        <div>
          <p class="text-sm uppercase tracking-[0.4em] text-primary-200"></p>
          <h1 class="mt-6 text-4xl font-semibold leading-tight">Securely manage your workspace.</h1>
          <p class="mt-4 max-w-md text-base text-slate-200">Sign in to access the dashboard, manage users, and stay on
            top of operations.</p>
        </div>
        <ul class="space-y-3 text-sm text-slate-200/80">
          <li class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-emerald-400"></span> OAuth 2.0
            secured endpoints</li>
          <li class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-primary-200"></span> Responsive
            dashboard experience</li>
          <li class="flex items-center gap-2"><span class="h-2 w-2 rounded-full bg-amber-300"></span> Session-based
            authentication</li>
        </ul>
      </div>
      <form class="relative mt-10 card lg:mt-0" @submit.prevent="handleSubmit">
        <header class="space-y-1">
          <h2 class="text-2xl font-semibold text-slate-900">Welcome back</h2>
          <p class="text-sm text-slate-500">Authenticate with your email and password.</p>
        </header>

        <div class="mt-8 space-y-6">
          <div :class="['floating-input', errors.email ? 'input-error' : '']">
            <input v-model="form.email" id="email" type="email" required autocomplete="email" placeholder="E-mail"
              class="input" />
            <p v-if="errors.email" class="text-xs text-red-500">{{ errors.email }}</p>
          </div>
          <div :class="['floating-input', errors.password ? 'input-error' : '']">
            <input v-model="form.password" id="password" type="password" required autocomplete="current-password"
              placeholder="Password" class="input"/>
            <p v-if="errors.password" class="text-xs text-red-500">{{ errors.password }}</p>
          </div>
          <div class="flex items-center justify-between text-xs text-slate-500">
            <label class="inline-flex items-center gap-2">
              <input v-model="form.remember" type="checkbox"
                class="rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
              Remember session
            </label>
            <RouterLink to="/recover-password" class="font-semibold text-primary-400 hover:text-primary-300">Forgot
              password?</RouterLink>
          </div>
        </div>

        <button type="submit" class="btn-primary mt-8 w-full" :disabled="auth.loading">
          <span v-if="auth.loading" class="flex w-full items-center justify-center gap-2">
            <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600"></span>
            Signing in...
          </span>
          <span v-else>Sign in</span>
        </button>

        <p v-if="errors.form" class="mt-4 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{
          errors.form }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useRouter, RouterLink, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const form = reactive({
  email: '',
  password: '',
  remember: false,
});

const errors = reactive({
  email: '',
  password: '',
  form: '',
});

const resetErrors = () => {
  errors.email = '';
  errors.password = '';
  errors.form = '';
};

const handleSubmit = async () => {
  resetErrors();
  try {
    await auth.login({ email: form.email, password: form.password });
    const redirect = route.query.redirect ?? '/dashboard';
    router.push(redirect);
  } catch (error) {
    const response = error.response;
    if (response?.data?.message) {
      errors.form = response.data.message;
    } else if (response?.data?.details) {
      errors.form = response.data.details;
    } else {
      errors.form = 'Unable to sign in. Check your credentials.';
    }
  }
};
</script>