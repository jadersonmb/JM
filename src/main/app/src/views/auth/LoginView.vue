<template>
  <div class="relative flex min-h-screen items-center justify-center bg-gradient-to-b from-slate-900 via-slate-900 to-slate-800 px-4 py-16">
    <div class="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(59,130,246,0.35),_rgba(15,23,42,0))]" aria-hidden="true" />
    <div class="relative grid w-full max-w-5xl gap-10 rounded-3xl border border-white/5 bg-white/10 p-10 shadow-[0_20px_60px_-15px_rgba(15,23,42,0.7)] backdrop-blur-xl md:grid-cols-[1.2fr_1fr]">
      <div class="flex flex-col justify-between gap-12 text-white">
        <div>
          <AppLogo />
          <h1 class="mt-10 text-4xl font-semibold leading-tight">Welcome back to your JM Admin workspace.</h1>
          <p class="mt-4 max-w-md text-base text-slate-200">
            Stay in control of your platform with real-time insights, collaborative tools, and streamlined user management.
          </p>
        </div>
        <div class="rounded-2xl border border-white/10 bg-white/5 p-5 text-sm leading-6 text-slate-200">
          <p class="font-medium text-white">Why teams choose JM Admin</p>
          <ul class="mt-3 grid gap-2 text-slate-200/80">
            <li class="flex items-center gap-2">
              <span class="h-2 w-2 rounded-full bg-emerald-400" /> Seamless collaboration with secure access controls
            </li>
            <li class="flex items-center gap-2">
              <span class="h-2 w-2 rounded-full bg-primary-300" /> Customizable dashboards for actionable insights
            </li>
            <li class="flex items-center gap-2">
              <span class="h-2 w-2 rounded-full bg-amber-300" /> Built for modern teams with mobile-first experiences
            </li>
          </ul>
        </div>
      </div>

      <form class="card relative z-10 shadow-2xl" @submit.prevent="submit">
        <div class="mb-8">
          <h2 class="text-2xl font-semibold text-slate-900">Sign in</h2>
          <p class="mt-1 text-sm text-slate-500">Use your organization email to access the control center.</p>
        </div>

        <div class="space-y-5">
          <div>
            <label for="email" class="mb-1 block text-sm font-medium text-slate-700">Email</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              autocomplete="email"
              class="input"
              required
              placeholder="you@example.com"
            />
            <p v-if="errors.email" class="mt-1 text-xs text-red-600">{{ errors.email }}</p>
          </div>

          <div>
            <div class="flex items-center justify-between">
              <label for="password" class="mb-1 block text-sm font-medium text-slate-700">Password</label>
              <a class="text-xs font-medium text-primary-600 hover:text-primary-500" href="#">Forgot password?</a>
            </div>
            <input
              id="password"
              v-model="form.password"
              type="password"
              autocomplete="current-password"
              class="input"
              required
              placeholder="••••••••"
            />
            <p v-if="errors.password" class="mt-1 text-xs text-red-600">{{ errors.password }}</p>
          </div>

          <div class="flex items-center justify-between text-sm text-slate-500">
            <label class="inline-flex items-center gap-2">
              <input v-model="form.remember" type="checkbox" class="rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
              Remember me
            </label>
            <span class="hidden sm:block">Enterprise login enabled</span>
          </div>

          <button type="submit" class="btn-primary w-full" :disabled="auth.loading">
            <span v-if="auth.loading" class="flex items-center justify-center gap-2">
              <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
              Signing in...
            </span>
            <span v-else>Sign in</span>
          </button>

          <p v-if="errors.form" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
            {{ errors.form }}
          </p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppLogo from '@/components/AppLogo.vue';
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

const submit = async () => {
  resetErrors();
  try {
    await auth.login({
      email: form.email,
      password: form.password,
      remember: form.remember,
    });
    const redirect = route.query.redirect ?? '/dashboard';
    await router.push(redirect);
  } catch (error) {
    const { response } = error;
    if (response?.data?.errors) {
      Object.entries(response.data.errors).forEach(([field, messages]) => {
        errors[field] = Array.isArray(messages) ? messages[0] : messages;
      });
    } else {
      errors.form = response?.data?.message ?? 'Unable to sign you in. Please try again.';
    }
  }
};
</script>