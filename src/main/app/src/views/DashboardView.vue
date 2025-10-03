<template>
  <div class="space-y-8">
    <section class="grid gap-6 md:grid-cols-2 xl:grid-cols-4">
      <div v-for="metric in metrics" :key="metric.label" class="card">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-xs uppercase tracking-wide text-slate-400">{{ metric.label }}</p>
            <p class="mt-3 text-3xl font-semibold text-slate-900">{{ metric.value }}</p>
          </div>
          <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary-50 text-primary-600">
            <component :is="metric.icon" class="h-6 w-6" />
          </div>
        </div>
        <p class="mt-4 text-xs text-slate-400">{{ metric.hint }}</p>
      </div>
    </section>

    <section class="card">
      <header class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-slate-900">System health</h2>
          <p class="text-sm text-slate-500">Quick overview of user growth and environment stability.</p>
        </div>
        <button type="button" class="btn-secondary" @click="refresh">Refresh</button>
      </header>

      <div class="mt-6 grid gap-4 lg:grid-cols-3">
        <div class="rounded-2xl bg-gradient-to-br from-primary-500 to-primary-700 p-6 text-white">
          <p class="text-sm uppercase tracking-wide text-white/80">Active sessions</p>
          <p class="mt-3 text-4xl font-semibold">1,248</p>
          <p class="mt-2 text-sm text-white/80">+18% compared to last week</p>
        </div>
        <div class="rounded-2xl border border-slate-200 bg-white p-6">
          <p class="text-sm font-semibold text-slate-500">Pending invitations</p>
          <p class="mt-3 text-2xl font-semibold text-slate-900">32</p>
          <p class="mt-2 text-xs text-slate-400">Respond within 24 hours to maintain activation rates.</p>
        </div>
        <div class="rounded-2xl border border-slate-200 bg-white p-6">
          <p class="text-sm font-semibold text-slate-500">Support tickets</p>
          <p class="mt-3 text-2xl font-semibold text-slate-900">5 open</p>
          <p class="mt-2 text-xs text-slate-400">Average response time 1h 12m.</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import {
  BoltIcon,
  ChartBarSquareIcon,
  UsersIcon,
  ShieldCheckIcon,
} from '@heroicons/vue/24/outline';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();

const metrics = computed(() => [
  { label: 'Active users', value: '—', hint: 'Live data will appear once connected.', icon: UsersIcon },
  { label: 'New signups', value: '—', hint: 'Pulled from /api/users analytics endpoint.', icon: ChartBarSquareIcon },
  { label: 'System uptime', value: '99.9%', hint: 'Managed via platform monitoring.', icon: ShieldCheckIcon },
  { label: 'Your role', value: auth.user?.role ?? '—', hint: auth.user?.email ?? 'Pending login', icon: BoltIcon },
]);

const refresh = () => {
  window.dispatchEvent(new CustomEvent('dashboard:refresh'));
};
</script>