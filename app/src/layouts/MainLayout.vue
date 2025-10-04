<template>
  <div class="flex min-h-screen bg-slate-100">
    <transition name="fade">
      <div
        v-if="mobileNavOpen"
        class="fixed inset-0 z-30 bg-slate-900/50 backdrop-blur-sm lg:hidden"
        @click="mobileNavOpen = false"
      />
    </transition>

    <aside
      :class="[
        'fixed inset-y-0 left-0 z-40 flex w-72 flex-col border-r border-slate-200 bg-white transition-transform duration-300 lg:static lg:translate-x-0',
        mobileNavOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
      ]"
    >
      <div class="flex h-20 items-center justify-between px-6">
        <AppLogo />
        <button class="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 lg:hidden" @click="mobileNavOpen = false">
          <XMarkIcon class="h-6 w-6" />
        </button>
      </div>

      <nav class="flex-1 space-y-2 overflow-y-auto px-4 pb-10">
        <p class="px-2 text-xs font-semibold uppercase tracking-wide text-slate-400">Menu</p>
        <RouterLink
          v-for="item in navigation"
          :key="item.name"
          :to="item.to"
          class="group mt-1 flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-semibold transition"
          :class="[
            item.active
              ? 'bg-primary-50 text-primary-600 shadow-sm'
              : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'
          ]"
          @click="mobileNavOpen = false"
        >
          <component :is="item.icon" class="h-5 w-5" :class="item.active ? 'text-primary-600' : 'text-slate-400 group-hover:text-slate-500'" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="border-t border-slate-200 p-4">
        <div class="rounded-xl bg-slate-50 p-4">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Need help?</p>
          <p class="mt-2 text-sm text-slate-600">Read the implementation guide to integrate the Java backend endpoints.</p>
          <a href="https://docs" class="mt-3 inline-flex items-center text-xs font-semibold text-primary-600 hover:text-primary-500">View documentation ?</a>
        </div>
      </div>
    </aside>

    <div class="flex min-h-screen w-full flex-1 flex-col">
      <header class="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur">
        <div class="flex h-20 items-center justify-between px-4 lg:px-10">
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="inline-flex items-center justify-center rounded-xl border border-slate-200 p-2 text-slate-500 transition hover:border-primary-200 hover:text-primary-600 lg:hidden"
              @click="mobileNavOpen = true"
            >
              <Bars3BottomLeftIcon class="h-6 w-6" />
            </button>
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ currentSection }}</p>
              <h1 class="text-xl font-semibold text-slate-900">{{ currentTitle }}</h1>
            </div>
          </div>

          <div class="flex items-center gap-4">
            <button
              type="button"
              class="hidden items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-500 transition hover:border-primary-200 hover:text-primary-600 md:flex"
              @click="router.push({ name: 'settings' })"
            >
              <Cog6ToothIcon class="h-5 w-5" />
            </button>

            <div class="relative" ref="dropdownRef">
              <button
                type="button"
                class="flex items-center gap-3 rounded-2xl border border-transparent bg-white px-3 py-2 text-left shadow-sm transition hover:border-primary-200 hover:shadow-md"
                @click="toggleDropdown"
              >
                <div class="h-10 w-10 rounded-2xl bg-primary-100 text-primary-600">
                  <img
                    v-if="auth.user?.avatarUrl"
                    :src="auth.user.avatarUrl"
                    alt="Profile"
                    class="h-10 w-10 rounded-2xl object-cover"
                  />
                  <div v-else class="flex h-full w-full items-center justify-center text-sm font-semibold">
                    {{ auth.initials || 'JM' }}
                  </div>
                </div>
                <div class="hidden text-sm lg:block">
                  <p class="font-semibold text-slate-900">{{ auth.user?.name }}</p>
                  <p class="text-xs text-slate-400">{{ auth.user?.email }}</p>
                </div>
                <ChevronDownIcon class="h-4 w-4 text-slate-400" />
              </button>

              <transition name="dropdown">
                <div
                  v-if="menuOpen"
                  class="absolute right-0 mt-3 w-56 rounded-xl border border-slate-200 bg-white py-2 shadow-xl"
                >
                  <button
                    type="button"
                    class="flex w-full items-center gap-3 px-4 py-2 text-left text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
                    @click="router.push({ name: 'profile' })"
                  >
                    <UserCircleIcon class="h-5 w-5" />
                    Profile
                  </button>
                  <button
                    type="button"
                    class="flex w-full items-center gap-3 px-4 py-2 text-left text-sm font-semibold text-slate-600 transition hover:bg-slate-50"
                    @click="router.push({ name: 'settings' })"
                  >
                    <AdjustmentsHorizontalIcon class="h-5 w-5" />
                    Settings
                  </button>
                  <hr class="my-2 border-slate-100" />
                  <button
                    type="button"
                    class="flex w-full items-center gap-3 px-4 py-2 text-left text-sm font-semibold text-red-600 transition hover:bg-red-50"
                    @click="logout"
                  >
                    <ArrowRightOnRectangleIcon class="h-5 w-5" />
                    Logout
                  </button>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </header>

      <main class="flex-1 px-4 py-8 lg:px-10">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router';
import {
  AdjustmentsHorizontalIcon,
  ArrowRightOnRectangleIcon,
  Bars3BottomLeftIcon,
  ChevronDownIcon,
  Cog6ToothIcon,
  ChatBubbleLeftRightIcon,
  RectangleStackIcon,
  Squares2X2Icon,
  UserGroupIcon,
  UserCircleIcon,
  XMarkIcon,
} from '@heroicons/vue/24/outline';
import AppLogo from '@/components/AppLogo.vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();
const notifications = useNotificationStore();

const mobileNavOpen = ref(false);
const menuOpen = ref(false);
const dropdownRef = ref(null);

const navigation = computed(() => [
  {
    name: 'dashboard',
    label: 'Dashboard',
    to: { name: 'dashboard' },
    icon: Squares2X2Icon,
    active: route.name === 'dashboard',
  },
  {
    name: 'users',
    label: 'Users',
    to: { name: 'users' },
    icon: UserGroupIcon,
    active: route.name === 'users',
  },
  {
    name: 'whatsapp-nutrition',
    label: 'WhatsApp AI',
    to: { name: 'whatsapp-nutrition' },
    icon: ChatBubbleLeftRightIcon,
    active: route.name === 'whatsapp-nutrition',
  },
  {
    name: 'settings',
    label: 'Settings',
    to: { name: 'settings' },
    icon: RectangleStackIcon,
    active: route.name === 'settings',
  },
]);

const currentTitle = computed(() => route.meta.title ?? 'Overview');
const currentSection = computed(() => (route.name === 'dashboard' ? 'Overview' : 'Workspace'));

const handleClickOutside = (event) => {
  if (!menuOpen.value) return;
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    menuOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
});

watch(
  () => route.fullPath,
  () => {
    mobileNavOpen.value = false;
    menuOpen.value = false;
  },
);

const toggleDropdown = () => {
  menuOpen.value = !menuOpen.value;
};

const logout = () => {
  auth.logout();
  notifications.push({ type: 'info', title: 'Signed out', message: 'You have been logged out.' });
  router.push({ name: 'login' });
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.15s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>