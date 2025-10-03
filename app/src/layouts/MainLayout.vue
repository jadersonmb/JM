<template>
  <div class="flex min-h-screen bg-slate-100">
    <aside
      class="fixed inset-y-0 left-0 z-30 w-72 -translate-x-full border-r border-slate-200 bg-white px-6 py-8 shadow-lg transition-transform duration-300 ease-out md:relative md:translate-x-0 md:shadow-none"
      :class="{ 'translate-x-0': sidebarOpen }"
    >
      <div class="flex items-center justify-between">
        <div>
          <p class="text-lg font-bold text-slate-900">JM Admin</p>
          <p class="text-xs uppercase tracking-[0.3em] text-primary-500">Control Center</p>
        </div>
        <button class="rounded-xl p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 md:hidden" @click="sidebarOpen = false">
          <XMarkIcon class="h-5 w-5" />
        </button>
      </div>
      <nav class="mt-8 space-y-2">
        <RouterLink
          v-for="item in navigation"
          :key="item.to.name"
          :to="item.to"
          class="flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-semibold transition"
          :class="item.active ? 'bg-primary-50 text-primary-600 shadow-sm' : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'"
          @click="sidebarOpen = false"
        >
          <component :is="item.icon" class="h-5 w-5" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <div class="flex min-h-screen flex-1 flex-col">
      <header class="sticky top-0 z-20 border-b border-slate-200 bg-white/80 backdrop-blur">
        <div class="flex h-16 items-center justify-between px-4 md:px-10">
          <div class="flex items-center gap-3">
            <button class="rounded-xl border border-slate-200 p-2 text-slate-500 transition hover:border-primary-200 hover:text-primary-600 md:hidden" @click="sidebarOpen = true">
              <Bars3Icon class="h-5 w-5" />
            </button>
            <p class="text-base font-semibold text-slate-800">{{ currentTitle }}</p>
          </div>
          <div class="flex items-center gap-3">
            <div class="hidden text-right text-sm md:block">
              <p class="font-semibold text-slate-900">{{ auth.user?.name }}</p>
              <p class="text-xs uppercase tracking-wide text-slate-400">{{ auth.user?.type }}</p>
            </div>
            <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-primary-100 text-primary-600">
              {{ auth.initials || 'JM' }}
            </div>
            <button class="btn-secondary" @click="handleLogout">
              <ArrowRightOnRectangleIcon class="h-4 w-4" />
              <span>Logout</span>
            </button>
          </div>
        </div>
      </header>
      <main class="flex flex-1 flex-col px-4 py-8 md:px-10">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';
import { ArrowRightOnRectangleIcon, Bars3Icon, Squares2X2Icon, XMarkIcon } from '@heroicons/vue/24/outline';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();
const sidebarOpen = ref(false);

const navigation = computed(() => [
  {
    label: 'Dashboard',
    to: { name: 'dashboard' },
    icon: Squares2X2Icon,
    active: route.name === 'dashboard',
  },
]);

const currentTitle = computed(() => {
  const match = navigation.value.find((item) => item.active);
  return match ? match.label : 'Overview';
});

const handleLogout = () => {
  auth.logout();
  router.push({ name: 'login' });
};
</script>