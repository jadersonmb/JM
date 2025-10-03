<template>
  <div class="space-y-6">
    <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <article v-for="card in cards" :key="card.label" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <p class="text-xs uppercase tracking-wide text-slate-400">{{ card.label }}</p>
        <p class="mt-4 text-3xl font-semibold text-slate-900">{{ card.value }}</p>
        <p class="mt-2 text-xs text-slate-400">{{ card.hint }}</p>
      </article>
    </section>
    <section class="rounded-3xl border border-dashed border-slate-200 bg-white p-10 text-center text-sm text-slate-500">
      Hook up your data endpoints to visualise metrics here.
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();

const cards = computed(() => [
  {
    label: 'Signed in as',
    value: auth.user?.name ?? '',
    hint: auth.user?.email ?? '',
  },
  {
    label: 'Role',
    value: auth.user?.type ?? 'CLIENT',
    hint: 'Authorisation scope from token claims',
  },
  {
    label: 'Token expires (s)',
    value: auth.expiresIn || '�',
    hint: 'Update via backend configuration',
  },
  {
    label: 'Status',
    value: auth.isAuthenticated ? 'Authenticated' : 'Guest',
    hint: auth.isAuthenticated ? 'Session stored in secure storage' : 'Sign in required',
  },
]);
</script>