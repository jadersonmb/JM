<template>
  <div class="flex min-h-screen bg-slate-100">
    <transition name="fade">
      <div v-if="mobileNavOpen" class="fixed inset-0 z-30 bg-slate-900/50 backdrop-blur-sm lg:hidden"
        @click="mobileNavOpen = false" />
    </transition>

    <aside :class="[
      'fixed inset-y-0 left-0 z-40 flex w-72 flex-col border-r border-slate-200 bg-white transition-transform duration-300 dark:border-slate-800 dark:bg-slate-900 lg:static lg:translate-x-0',
      mobileNavOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
    ]">
      <div class="flex h-20 items-center justify-between px-6">
        <AppLogo />
        <button
          class="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 lg:hidden"
          @click="mobileNavOpen = false"
        >
          <XMarkIcon class="h-6 w-6" />
        </button>
      </div>

      <nav class="flex-1 space-y-3 overflow-y-auto px-4 pb-10">
        <p class="px-2 text-xs font-semibold uppercase tracking-wide text-slate-400">
          {{ t('menu.title') }}
        </p>
        <div v-for="item in navigation" :key="item.name" class="mt-1">
          <template v-if="item.children?.length">
            <button
              type="button"
              class="group flex w-full items-center justify-between rounded-xl border border-transparent px-3 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-100 hover:text-slate-900"
              @click="toggleGroup(item.name)"
            >
              <span class="flex items-center gap-3">
                <component :is="item.icon" class="h-5 w-5"
                  :class="item.active ? 'text-primary-500' : 'text-slate-400 group-hover:text-slate-500'" />
                {{ item.label }}
              </span>
              <ChevronDownIcon
                class="h-4 w-4 text-slate-400 transition-transform"
                :class="isGroupExpanded(item) ? 'rotate-180 text-primary-500' : ''"
              />
            </button>
            <transition name="fade">
              <div v-if="isGroupExpanded(item)" class="mt-1 space-y-1 pl-9">
                <RouterLink
                  v-for="child in item.children"
                  :key="child.name"
                  :to="child.to"
                  class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-semibold transition"
                  :class="child.active
                    ? 'bg-primary-50 text-primary-600 shadow-sm'
                    : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'"
                  @click="mobileNavOpen = false"
                >
                  <span>{{ child.label }}</span>
                </RouterLink>
              </div>
            </transition>
          </template>
          <template v-else>
            <RouterLink
              :to="item.to"
              class="group flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-semibold transition"
              :class="item.active
                ? 'bg-primary-50 text-primary-600 shadow-sm'
                : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'"
              @click="mobileNavOpen = false"
            >
              <component :is="item.icon" class="h-5 w-5"
                :class="item.active ? 'text-primary-600' : 'text-slate-400 group-hover:text-slate-500'" />
              <span>{{ item.label }}</span>
            </RouterLink>
          </template>
        </div>
      </nav>

      <div class="border-t border-slate-200 p-4 dark:border-slate-800">
        <div class="rounded-xl bg-slate-50 p-4 dark:bg-slate-800/60">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-300">{{ t('layout.help.title') }}</p>
          <p class="mt-2 text-sm text-slate-600 dark:text-slate-400">{{ t('layout.help.description') }}</p>
          <a
            href="https://docs"
            class="mt-3 inline-flex items-center text-xs font-semibold text-primary-600 hover:text-primary-500"
          >
            {{ t('layout.help.action') }}
          </a>
        </div>
      </div>
    </aside>

    <div class="flex min-h-screen w-full flex-1 flex-col">
      <header class="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur dark:border-slate-800 dark:bg-slate-900/85">
        <div class="flex h-20 items-center justify-between px-4 lg:px-10">
          <div class="flex items-center gap-3">
            <button type="button"
              class="inline-flex items-center justify-center rounded-xl border border-slate-200 p-2 text-slate-500 transition hover:border-primary-200 hover:text-primary-600 dark:border-slate-700 dark:text-slate-400 dark:hover:border-primary-500 dark:hover:text-primary-300 lg:hidden"
              @click="mobileNavOpen = true">
              <Bars3BottomLeftIcon class="h-6 w-6" />
            </button>
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-slate-400 dark:text-slate-500">{{ currentSection }}</p>
              <h1 class="text-xl font-semibold text-slate-900 dark:text-slate-100">{{ currentTitle }}</h1>
            </div>
          </div>

          <div class="flex items-center gap-4">
            <div
              class="hidden items-center gap-2 md:flex"
              role="group"
              :aria-label="t('preferences.language.label')"
            >
              <button
                v-for="option in languageOptions"
                :key="option.code"
                type="button"
                class="flex h-10 w-10 items-center justify-center rounded-full border text-xl transition"
                :class="selectedLocale === option.code
                  ? 'border-primary-300 bg-primary-50 text-primary-600 dark:border-primary-500 dark:bg-primary-500/20 dark:text-primary-200'
                  : 'border-slate-200 bg-white text-slate-500 hover:border-primary-200 hover:text-primary-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400 dark:hover:border-primary-500 dark:hover:text-primary-300'"
                @click="selectedLocale = option.code"
              >
                <img aria-hidden="true" :src="option.url" :alt="option.code"/>
                <span class="sr-only">{{ option.label }}</span>
              </button>
            </div>
            <button type="button"
              class="hidden items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-500 transition hover:border-primary-200 hover:text-primary-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400 dark:hover:border-primary-500 dark:hover:text-primary-300 md:flex"
              @click="router.push({ name: 'settings' })">
              <Cog6ToothIcon class="h-5 w-5" />
              <!-- <span>{{ t('layout.settings') }}</span> -->
            </button>

            <div class="relative" ref="dropdownRef">
              <button type="button"
                class="flex items-center gap-3 rounded-2xl border border-transparent bg-white px-3 py-2 text-left shadow-sm transition hover:border-primary-200 hover:shadow-md dark:bg-slate-900 dark:hover:border-primary-500"
                @click="toggleDropdown">
                <div class="h-10 w-10 rounded-2xl bg-primary-100 text-primary-600">
                  <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" alt="Profile"
                    class="h-10 w-10 rounded-2xl object-cover" />
                  <div v-else class="flex h-full w-full items-center justify-center text-sm font-semibold">
                    {{ auth.initials || 'JM' }}
                  </div>
                </div>
                <div class="hidden text-sm lg:block">
                  <p class="font-semibold text-slate-900 dark:text-slate-100">{{ auth.user?.name }}</p>
                  <p class="text-xs text-slate-400 dark:text-slate-500">{{ auth.user?.email }}</p>
                </div>
                <ChevronDownIcon class="h-4 w-4 text-slate-400 dark:text-slate-500" />
              </button>

              <transition name="dropdown">
                <div v-if="menuOpen"
                  class="absolute right-0 mt-3 w-56 rounded-xl border border-slate-200 bg-white py-2 shadow-xl dark:border-slate-700 dark:bg-slate-900">
                  <button type="button"
                    class="flex w-full items-center gap-3 px-4 py-2 text-left text-sm font-semibold text-slate-600 transition hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-800"
                    @click="router.push({ name: 'profile' })">
                    <UserCircleIcon class="h-5 w-5" />
                    {{ t('layout.profile') }}
                  </button>
                  <button type="button"
                    class="flex w-full items-center gap-3 px-4 py-2 text-left text-sm font-semibold text-slate-600 transition hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-800"
                    @click="router.push({ name: 'settings' })">
                    <AdjustmentsHorizontalIcon class="h-5 w-5" />
                    {{ t('layout.settings') }}
                  </button>
                  <hr class="my-2 border-slate-100 dark:border-slate-700" />
                  <button type="button"
                    class="flex w-full items-center gap-3 px-4 py-2 text-left text-sm font-semibold text-red-600 transition hover:bg-red-50 dark:hover:bg-red-500/10"
                    @click="logout">
                    <ArrowRightOnRectangleIcon class="h-5 w-5" />
                    {{ t('layout.logout') }}
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
  ClipboardDocumentListIcon,
  RectangleStackIcon,
  GlobeAltIcon,
  Squares2X2Icon,
  UserGroupIcon,
  UserCircleIcon,
  CreditCardIcon,
  XMarkIcon,
} from '@heroicons/vue/24/outline';
import AppLogo from '@/components/AppLogo.vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import { useI18n } from 'vue-i18n';
import { usePreferencesStore } from '@/stores/preferences';
import BR from '@/assets/bandeira-do-brasil.png';
import EUA from '@/assets/estados-unidos-da-america.png';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();
const notifications = useNotificationStore();

const mobileNavOpen = ref(false);
const menuOpen = ref(false);
const dropdownRef = ref(null);

const { t } = useI18n();
const preferences = usePreferencesStore();

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');

const languageOptions = computed(() => [
  { code: 'pt', label: t('preferences.language.portuguese'), flag: 'BR', url: BR },
  { code: 'en', label: t('preferences.language.english'), flag: 'US', url: EUA },
]);

const selectedLocale = computed({
  get: () => preferences.locale,
  set: (value) => preferences.setLocale(value),
});

const openGroups = ref([]);

const navigation = computed(() => {
  const baseItems = [
    {
      name: 'dashboard',
      label: t('routes.dashboard'),
      to: { name: 'dashboard' },
      icon: Squares2X2Icon,
      adminOnly: false,
    },
    {
      name: 'users',
      label: t('routes.users'),
      to: { name: 'users' },
      icon: UserGroupIcon,
      adminOnly: true,
    },
    {
      name: 'anamnese',
      label: t('menu.anamnese'),
      to: { name: 'anamnese' },
      icon: ClipboardDocumentListIcon,
      adminOnly: false,
    },
    {
      name: 'whatsapp-nutrition',
      label: t('routes.whatsappNutrition'),
      to: { name: 'whatsapp-nutrition' },
      icon: ChatBubbleLeftRightIcon,
      adminOnly: false,
    },
    {
      name: 'payments',
      label: t('routes.payments'),
      to: { name: 'payments' },
      icon: CreditCardIcon,
      adminOnly: false,
    },
    {
      name: 'settings',
      label: t('routes.settings'),
      to: { name: 'settings' },
      icon: RectangleStackIcon,
      adminOnly: false,
    },
  ];

  const filtered = baseItems
    .filter((item) => !item.adminOnly || isAdmin.value)
    .map((item) => ({
      ...item,
      active: route.name === item.name,
    }));

  if (isAdmin.value) {
    const referenceChildren = [
      { name: 'reference-countries', label: t('routes.referenceCountries') },
      { name: 'reference-cities', label: t('routes.referenceCities') },
      { name: 'reference-education-levels', label: t('routes.referenceEducationLevels') },
      { name: 'reference-professions', label: t('routes.referenceProfessions') },
    ].map((child) => ({
      ...child,
      to: { name: child.name },
      active: route.name === child.name,
    }));

    const referenceItem = {
      name: 'references',
      label: t('menu.reference'),
      icon: GlobeAltIcon,
      children: referenceChildren,
      active: referenceChildren.some((child) => child.active),
    };

    const anamneseIndex = filtered.findIndex((item) => item.name === 'anamnese');
    const insertIndex = anamneseIndex >= 0 ? anamneseIndex + 1 : filtered.length;
    filtered.splice(insertIndex, 0, referenceItem);
  }

  return filtered;
});

const isGroupExpanded = (item) => item.active || openGroups.value.includes(item.name);

const toggleGroup = (name) => {
  if (openGroups.value.includes(name)) {
    openGroups.value = openGroups.value.filter((group) => group !== name);
  } else {
    openGroups.value = [...openGroups.value, name];
  }
};

const currentTitle = computed(() => {
  if (route.meta.titleKey) {
    return t(route.meta.titleKey);
  }
  return route.meta.title ?? 'Overview';
});
const currentSection = computed(() => (route.name === 'dashboard' ? t('routes.dashboard') : t('menu.title')));

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

watch(
  () => route.name,
  (name) => {
    const group = navigation.value.find((item) => item.children?.some((child) => child.name === name));
    if (group && !openGroups.value.includes(group.name)) {
      openGroups.value = [...openGroups.value, group.name];
    }
  },
  { immediate: true },
);

watch(
  () => preferences.locale,
  () => {
    if (route.meta.titleKey) {
      document.title = `${t(route.meta.titleKey)} - JM Admin`;
    }
  },
);

const toggleDropdown = () => {
  menuOpen.value = !menuOpen.value;
};

const logout = () => {
  auth.logout();
  notifications.push({
    type: 'info',
    title: t('layout.logoutNotification.title'),
    message: t('layout.logoutNotification.message'),
  });
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