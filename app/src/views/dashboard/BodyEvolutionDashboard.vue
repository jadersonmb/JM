<template>
  <div class="space-y-8">
    <header class="space-y-2">
      <h1 class="text-2xl font-semibold text-slate-900">
        {{ t('dashboard.bodyEvolution.title') }}
      </h1>
      <p class="text-sm text-slate-500">
        {{ t('dashboard.bodyEvolution.subtitle') }}
      </p>
    </header>

    <section class="card">
      <div
        class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between"
      >
        <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-8">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('dashboard.bodyEvolution.filters.bodyPart') }}
            </p>
            <div class="mt-2 flex flex-wrap gap-2">
              <button
                v-for="option in bodyPartOptions"
                :key="option.value"
                type="button"
                @click="selectedPart = option.value"
                :class="[
                  'px-4 py-1.5 rounded-full font-medium transition-all duration-200 text-sm',
                  selectedPart === option.value
                    ? 'bg-emerald-200 text-emerald-800 shadow-sm'
                    : 'bg-white hover:bg-emerald-100 text-gray-800 border border-transparent hover:border-emerald-300'
                ]"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('dashboard.bodyEvolution.filters.period') }}
            </p>
            <div class="mt-2 flex flex-wrap gap-2">
              <button
                v-for="range in dateRanges"
                :key="range.value"
                type="button"
                @click="selectedRange = range.value"
                :class="[
                  'px-3 py-1.5 rounded-full font-medium text-sm transition-all duration-200',
                  selectedRange === range.value
                    ? 'bg-emerald-100 text-emerald-800'
                    : 'bg-white text-gray-600 hover:bg-gray-100'
                ]"
              >
                {{ range.label }}
              </button>
            </div>
          </div>
        </div>

        <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-4">
          <div v-if="isAdmin" class="w-full md:w-64">
            <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('dashboard.bodyEvolution.filters.user') }}
            </label>
            <select
              v-model="selectedUserId"
              class="input mt-2"
              :disabled="usersLoading"
            >
              <option value="">
                {{ usersLoading ? t('dashboard.bodyEvolution.filters.loadingUsers') : t('dashboard.bodyEvolution.filters.userPlaceholder') }}
              </option>
              <option
                v-for="option in userOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </div>

          <div class="w-full md:w-48">
            <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {{ t('dashboard.bodyEvolution.sort.label') }}
            </label>
            <select
              v-model="sortBy"
              class="input mt-2"
            >
              <option
                v-for="option in sortOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </section>



    <section v-if="loading" class="flex h-64 items-center justify-center rounded-3xl bg-white shadow-sm">
      <ArrowPathIcon class="h-8 w-8 animate-spin text-primary-500" />
    </section>

    <template v-else>
      <section
        v-if="shouldPromptSelection"
        class="rounded-3xl bg-white p-12 text-center shadow-sm"
      >
        <p class="text-base font-semibold text-slate-700">
          {{ t('dashboard.bodyEvolution.prompts.selectUser') }}
        </p>
      </section>

      <section v-else-if="!hasData" class="rounded-3xl bg-white p-12 text-center shadow-sm">
        <p class="text-base font-semibold text-slate-700">
          {{ t('dashboard.bodyEvolution.empty') }}
        </p>
      </section>

      <template v-else>
        <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <article
            v-for="part in displayedParts"
            :key="part.bodyPartCode"
            class="cursor-pointer rounded-3xl border border-transparent bg-white p-6 shadow-sm transition hover:shadow-md"
            :class="activePartCode === part.bodyPartCode ? 'border-primary-200 ring-2 ring-primary-200' : ''"
            @click="selectPart(part.bodyPartCode)"
          >
            <header class="flex items-start justify-between gap-2">
              <div>
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
                  {{ t('photoEvolution.part') }}
                </p>
                <h2 class="text-lg font-semibold text-slate-900">{{ part.bodyPart }}</h2>
              </div>
              <span :class="['text-sm font-semibold', variationClass(part.variation)]">
                {{ formatPercentage(part.variation) }}
              </span>
            </header>

            <dl class="mt-4 grid grid-cols-2 gap-3 text-sm text-slate-500">
              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <dt class="text-xs font-semibold uppercase tracking-wide">
                  {{ t('dashboard.bodyEvolution.metrics.initial') }}
                </dt>
                <dd class="mt-1 text-base font-semibold text-slate-800">
                  {{ formatMeasurement(part.initialMeasurement) }}
                </dd>
              </div>
              <div class="rounded-2xl bg-slate-50 px-4 py-3">
                <dt class="text-xs font-semibold uppercase tracking-wide">
                  {{ t('dashboard.bodyEvolution.metrics.current') }}
                </dt>
                <dd class="mt-1 text-base font-semibold text-slate-800">
                  {{ formatMeasurement(part.currentMeasurement) }}
                </dd>
              </div>
            </dl>

            <div class="mt-4 h-40">
              <EvolutionChart :data="part.evolution" :height="160" />
            </div>
          </article>
        </section>

        <section v-if="activePart" class="grid gap-6 lg:grid-cols-2">
          <article class="rounded-3xl bg-white p-6 shadow-sm">
            <header class="flex items-center justify-between">
              <div>
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
                  {{ t('photoEvolution.part') }}
                </p>
                <h2 class="text-xl font-semibold text-slate-900">{{ activePart.bodyPart }}</h2>
              </div>
              <span :class="['text-base font-semibold', variationClass(activePart.variation)]">
                {{ formatPercentage(activePart.variation) }}
              </span>
            </header>
            <div class="mt-6 grid gap-4 sm:grid-cols-2">
              <figure class="flex flex-col items-center gap-3">
                <p class="text-sm font-semibold text-slate-500">
                  {{ t('dashboard.bodyEvolution.metrics.initial') }}
                </p>
                <div class="flex h-64 w-full items-center justify-center overflow-hidden rounded-2xl bg-slate-100">
                  <img
                    v-if="firstImage(activePart)"
                    :src="firstImage(activePart)"
                    :alt="`${activePart.bodyPart} initial`"
                    class="h-full w-full object-cover"
                  />
                  <PhotoIcon v-else class="h-12 w-12 text-slate-300" />
                </div>
                <p class="text-sm text-slate-500">
                  {{ formatMeasurement(activePart.initialMeasurement) }}
                  <span class="ml-1 text-xs uppercase text-slate-400">{{ t('photoEvolution.measurement') }}</span>
                </p>
              </figure>
              <figure class="flex flex-col items-center gap-3">
                <p class="text-sm font-semibold text-slate-500">
                  {{ t('dashboard.bodyEvolution.metrics.current') }}
                </p>
                <div class="flex h-64 w-full items-center justify-center overflow-hidden rounded-2xl bg-slate-100">
                  <img
                    v-if="activePart.latestImage"
                    :src="activePart.latestImage"
                    :alt="`${activePart.bodyPart} current`"
                    class="h-full w-full object-cover"
                  />
                  <PhotoIcon v-else class="h-12 w-12 text-slate-300" />
                </div>
                <p class="text-sm text-slate-500">
                  {{ formatMeasurement(activePart.currentMeasurement) }}
                  <span class="ml-1 text-xs uppercase text-slate-400">{{ t('photoEvolution.measurement') }}</span>
                </p>
              </figure>
            </div>
            <p class="mt-6 text-center text-sm text-slate-500">
              {{ t('dashboard.bodyEvolution.compareHint') }}
            </p>
          </article>

          <article class="rounded-3xl bg-white p-6 shadow-sm">
            <EvolutionChart :data="activePart.evolution" :height="320" />
            <div class="mt-6 text-center text-sm text-slate-500">
              <span :class="['text-xl font-semibold', variationClass(activePart.variation)]">
                {{ formatPercentage(activePart.variation) }}
              </span>
              <span class="ml-2 text-sm text-slate-500">({{ t('photoEvolution.variation') }})</span>
            </div>
          </article>
        </section>
      </template>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { ArrowPathIcon, PhotoIcon } from '@heroicons/vue/24/outline';
import EvolutionChart from '@/components/charts/EvolutionChart.vue';
import photoEvolutionService from '@/services/photoEvolution';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import { getUsers } from '@/services/users';

const auth = useAuthStore();
const notifications = useNotificationStore();
const { t, locale } = useI18n();

const loading = ref(false);
const usersLoading = ref(false);
const userOptions = ref([]);
const selectedUserId = ref('');
const allParts = ref([]);
const selectedPart = ref('ALL');
const selectedRange = ref(30);
const activePartCode = ref('');
const sortBy = ref('date');
const activeRequestToken = ref(null);

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');

const bodyPartOptions = computed(() => [
  { value: 'ALL', label: t('bodyParts.all') },
  { value: 'ABDOMEN', label: t('bodyParts.abdomen') },
  { value: 'ARMS', label: t('bodyParts.arms') },
  { value: 'LEGS', label: t('bodyParts.legs') },
  { value: 'BACK', label: t('bodyParts.back') },
  { value: 'CHEST', label: t('bodyParts.chest') },
]);

const dateRanges = computed(() => [
  { value: 30, label: t('periods.last30') },
  { value: 90, label: t('periods.last90') },
  { value: 365, label: t('periods.lastYear') },
]);

const sortOptions = computed(() => [
  { value: 'date', label: t('dashboard.bodyEvolution.sort.date') },
  { value: 'variation', label: t('dashboard.bodyEvolution.sort.variation') },
]);

const numberFormatter = computed(() => new Intl.NumberFormat(locale.value, {
  maximumFractionDigits: 2,
  minimumFractionDigits: 0,
}));

const targetUserId = computed(() => {
  if (isAdmin.value) {
    return selectedUserId.value || null;
  }
  return auth.user?.id ?? null;
});

const shouldPromptSelection = computed(() => isAdmin.value && !targetUserId.value && !usersLoading.value);

const displayedParts = computed(() => {
  let parts = Array.isArray(allParts.value) ? [...allParts.value] : [];
  if (selectedPart.value !== 'ALL') {
    parts = parts.filter((item) => item.bodyPartCode === selectedPart.value);
  }
  if (sortBy.value === 'variation') {
    return parts.sort((a, b) => Math.abs((b.variation ?? 0)) - Math.abs((a.variation ?? 0)));
  }
  return parts.sort((a, b) => latestTimestamp(b) - latestTimestamp(a));
});

const hasData = computed(() => displayedParts.value.length > 0);

const activePart = computed(() => {
  if (!hasData.value) {
    return null;
  }
  const current = displayedParts.value.find((item) => item.bodyPartCode === activePartCode.value);
  return current ?? displayedParts.value[0] ?? null;
});

watch(displayedParts, (parts) => {
  if (!parts.length) {
    activePartCode.value = '';
    return;
  }
  if (!parts.some((item) => item.bodyPartCode === activePartCode.value)) {
    activePartCode.value = parts[0].bodyPartCode;
  }
});

const selectPart = (code) => {
  activePartCode.value = code;
};

const toggleFilters = () => {
  // abrir modal lateral de filtros avançados
};

const formatMeasurement = (value) => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '—';
  }
  return numberFormatter.value.format(value);
};

const formatPercentage = (value) => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '0%';
  }
  return `${numberFormatter.value.format(value)}%`;
};

const variationClass = (value) => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return 'text-slate-500';
  }
  if (value < 0) {
    return 'text-emerald-600';
  }
  if (value > 0) {
    return 'text-rose-500';
  }
  return 'text-slate-500';
};

const firstImage = (part) => {
  if (!part?.evolution) {
    return null;
  }
  const point = part.evolution.find((item) => item?.imageUrl);
  return point?.imageUrl ?? null;
};

const latestTimestamp = (part) => {
  if (!part?.evolution?.length) {
    return 0;
  }
  const last = part.evolution[part.evolution.length - 1];
  const date = last?.date ? new Date(last.date) : null;
  if (!(date instanceof Date) || Number.isNaN(date.valueOf())) {
    return 0;
  }
  return date.valueOf();
};

const normalizePart = (part) => ({
  bodyPart: part?.bodyPart ?? '',
  bodyPartCode: part?.bodyPartCode ?? part?.bodyPart ?? '',
  latestImage: part?.latestImage ?? null,
  initialMeasurement: toNumber(part?.initialMeasurement),
  currentMeasurement: toNumber(part?.currentMeasurement),
  variation: toNumber(part?.variation),
  evolution: Array.isArray(part?.evolution)
    ? part.evolution
        .map((item) => ({
          date: item?.date ?? null,
          measurement: toNumber(item?.measurement),
          imageUrl: item?.imageUrl ?? null,
        }))
        .filter((item) => item.date)
    : [],
});

const toNumber = (value) => {
  if (value === null || value === undefined) {
    return null;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
};

const loadEvolutionData = async (part, range) => {
  const userId = targetUserId.value;
  if (!userId) {
    activeRequestToken.value = null;
    loading.value = false;
    allParts.value = [];
    return;
  }

  loading.value = true;
  const token = Symbol('request');
  activeRequestToken.value = token;

  const params = {};
  if (part && part !== 'ALL') {
    params.bodyPart = part;
  }
  if (typeof range === 'number' && Number.isFinite(range)) {
    params.range = range;
  }

  try {
    const { data } = await photoEvolutionService.comparison(userId, params);
    const parts = Array.isArray(data?.parts) ? data.parts.map(normalizePart) : [];
    if (activeRequestToken.value !== token) {
      return;
    }
    allParts.value = parts;
  } catch (error) {
    if (activeRequestToken.value !== token) {
      return;
    }
    console.error('[BodyEvolutionDashboard]', error);
    notifications.push({
      type: 'error',
      title: t('dashboard.bodyEvolution.error'),
      message: error?.response?.data?.details ?? error?.message ?? '',
    });
    allParts.value = [];
  } finally {
    if (activeRequestToken.value === token) {
      loading.value = false;
      activeRequestToken.value = null;
    }
  }
};

const extractArray = (payload) => {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.items)) return payload.items;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload?.results)) return payload.results;
  return [];
};

const fetchUsers = async () => {
  if (!isAdmin.value) return;
  usersLoading.value = true;
  try {
    const { data } = await getUsers({
      page: 0,
      size: 50,
      sort: 'name,asc',
      type: 'CLIENT',
    });
    const rows = extractArray(data);
    userOptions.value = rows
      .map((item) => {
        const value = item?.id ?? item?.userId ?? '';
        const label = item?.name ?? item?.fullName ?? item?.email ?? (value ? `#${value}` : '');
        return { value, label };
      })
      .filter((item) => item.value);
  } catch (error) {
    console.error('[BodyEvolutionDashboard][fetchUsers]', error);
    userOptions.value = [];
    notifications.push({
      type: 'error',
      title: t('dashboard.bodyEvolution.error'),
      message: error?.response?.data?.details ?? error?.message ?? '',
    });
  } finally {
    usersLoading.value = false;
  }
};

watch(
  () => auth.user,
  (user) => {
    if (!isAdmin.value && user?.id) {
      selectedUserId.value = user.id;
    }
  },
  { immediate: true },
);

watch(
  [selectedPart, selectedRange],
  ([part, range]) => {
    if (!targetUserId.value) {
      return;
    }
    loadEvolutionData(part, range);
  },
);

watch(
  targetUserId,
  () => {
    activePartCode.value = '';
    loadEvolutionData(selectedPart.value, selectedRange.value);
  },
  { immediate: true },
);

watch(
  isAdmin,
  (value) => {
    if (value) {
      selectedUserId.value = '';
      fetchUsers();
    } else if (auth.user?.id) {
      selectedUserId.value = auth.user.id;
    }
  },
  { immediate: true },
);
</script>

<style scoped>
.input {
  @apply rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm transition focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200;
}

.shadow-soft {
  box-shadow: 0 10px 40px -15px rgb(15 23 42 / 0.35);
}
</style>
