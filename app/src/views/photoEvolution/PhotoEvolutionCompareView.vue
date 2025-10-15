<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <button
        type="button"
        class="inline-flex items-center gap-2 text-sm font-semibold text-primary-600 transition hover:text-primary-500"
        @click="goBack"
      >
        <ArrowLeftIcon class="h-4 w-4" />
        {{ t('photoEvolution.comparison.goBack') }}
      </button>
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">{{ t('photoEvolution.comparison.fullTitle') }}</h1>
        <p class="mt-1 text-sm text-slate-500">{{ t('photoEvolution.comparison.fullSubtitle') }}</p>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center rounded-3xl bg-white p-12 shadow-sm">
      <ArrowPathIcon class="h-8 w-8 animate-spin text-primary-500" />
    </div>

    <template v-else>
      <div v-if="!canCompare" class="rounded-3xl bg-white p-12 text-center shadow-sm">
        <p class="text-lg font-semibold text-slate-700">
          {{ t('photoEvolution.comparison.missingSelection') }}
        </p>
        <p v-if="isMismatch" class="mt-2 text-sm text-slate-500">
          {{ t('photoEvolution.comparison.mismatch') }}
        </p>
      </div>

      <div v-else class="space-y-6">
        <div class="grid gap-6 lg:grid-cols-2">
          <article
            v-for="(entry, index) in entries"
            :key="entry.id"
            class="rounded-3xl bg-white p-6 shadow-sm"
          >
            <p class="text-xs font-semibold uppercase tracking-wide text-primary-500">
              {{ index === 0 ? t('photoEvolution.comparison.first') : t('photoEvolution.comparison.second') }}
            </p>
            <div class="mt-1 flex flex-col gap-1 text-sm text-slate-500">
              <span class="text-base font-semibold text-slate-900">
                {{ formatDate(entry.capturedAt || entry.createdAt) }}
              </span>
              <span>{{ entry.userDisplayName }}</span>
              <span>{{ bodyPartLabel(entry.bodyPart) }}</span>
            </div>
            <div class="mt-4 overflow-hidden rounded-2xl bg-slate-50">
              <img
                v-if="entry.imageUrl"
                :src="entry.imageUrl"
                alt="comparison photo"
                class="h-full w-full object-cover"
              />
              <div v-else class="flex h-96 items-center justify-center text-slate-300">
                <PhotoIcon class="h-12 w-12" />
              </div>
            </div>
            <div class="mt-4 grid gap-3 sm:grid-cols-2">
              <div
                v-for="metric in comparisonMetrics(entry)"
                :key="metric.key"
                class="rounded-2xl bg-slate-50 px-4 py-3"
              >
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
                  {{ metric.label }}
                </p>
                <p class="mt-1 text-base font-semibold text-slate-800">
                  {{ metric.value }}
                  <span v-if="metric.unit" class="text-xs font-normal text-slate-400">{{ metric.unit }}</span>
                </p>
              </div>
            </div>
            <p v-if="entry.notes" class="mt-4 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600">
              {{ entry.notes }}
            </p>
          </article>
        </div>

        <section class="rounded-3xl bg-white p-6 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
            {{ t('photoEvolution.comparison.difference') }}
          </p>
          <p class="mt-1 text-base font-semibold text-slate-800">
            {{ bodyPartLabel(comparisonBodyPart) }}
          </p>
          <ul class="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            <li
              v-for="metric in comparisonDifference"
              :key="metric.key"
              class="rounded-2xl bg-slate-50 px-4 py-3"
            >
              <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
                {{ metric.label }}
              </p>
              <p :class="['mt-1 text-base', metric.class]">
                {{ metric.value }}
                <span v-if="metric.unit" class="text-xs font-normal text-slate-400">{{ metric.unit }}</span>
              </p>
            </li>
          </ul>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import photoEvolutionService from '@/services/photoEvolution';
import { useNotificationStore } from '@/stores/notifications';
import {
  ArrowLeftIcon,
  ArrowPathIcon,
  PhotoIcon,
} from '@heroicons/vue/24/outline';

const router = useRouter();
const route = useRoute();
const { t, locale } = useI18n();
const notifications = useNotificationStore();

const loading = ref(false);
const entries = ref([]);

const selectionIds = computed(() => {
  const ids = [];
  const collect = (value) => {
    if (!value) {
      return;
    }
    if (Array.isArray(value)) {
      value.forEach(collect);
      return;
    }
    ids.push(String(value));
  };
  collect(route.query.first);
  collect(route.query.second);
  return [...new Set(ids)].slice(0, 2);
});

const numericFields = computed(() => [
  { key: 'weight', label: t('photoEvolution.form.fields.weight.label'), unit: t('photoEvolution.form.fields.weight.unit') },
  { key: 'bodyFatPercentage', label: t('photoEvolution.form.fields.bodyFatPercentage.label'), unit: t('photoEvolution.form.fields.bodyFatPercentage.unit') },
  { key: 'muscleMass', label: t('photoEvolution.form.fields.muscleMass.label'), unit: t('photoEvolution.form.fields.muscleMass.unit') },
  { key: 'visceralFat', label: t('photoEvolution.form.fields.visceralFat.label'), unit: t('photoEvolution.form.fields.visceralFat.unit') },
  { key: 'waistCircumference', label: t('photoEvolution.form.fields.waistCircumference.label'), unit: t('photoEvolution.form.fields.waistCircumference.unit') },
  { key: 'hipCircumference', label: t('photoEvolution.form.fields.hipCircumference.label'), unit: t('photoEvolution.form.fields.hipCircumference.unit') },
  { key: 'chestCircumference', label: t('photoEvolution.form.fields.chestCircumference.label'), unit: t('photoEvolution.form.fields.chestCircumference.unit') },
  { key: 'leftArmCircumference', label: t('photoEvolution.form.fields.leftArmCircumference.label'), unit: t('photoEvolution.form.fields.leftArmCircumference.unit') },
  { key: 'rightArmCircumference', label: t('photoEvolution.form.fields.rightArmCircumference.label'), unit: t('photoEvolution.form.fields.rightArmCircumference.unit') },
  { key: 'leftThighCircumference', label: t('photoEvolution.form.fields.leftThighCircumference.label'), unit: t('photoEvolution.form.fields.leftThighCircumference.unit') },
  { key: 'rightThighCircumference', label: t('photoEvolution.form.fields.rightThighCircumference.label'), unit: t('photoEvolution.form.fields.rightThighCircumference.unit') },
  { key: 'caloricIntake', label: t('photoEvolution.form.fields.caloricIntake.label'), unit: t('photoEvolution.form.fields.caloricIntake.unit') },
  { key: 'proteinIntake', label: t('photoEvolution.form.fields.proteinIntake.label'), unit: t('photoEvolution.form.fields.proteinIntake.unit') },
  { key: 'carbohydrateIntake', label: t('photoEvolution.form.fields.carbohydrateIntake.label'), unit: t('photoEvolution.form.fields.carbohydrateIntake.unit') },
  { key: 'fatIntake', label: t('photoEvolution.form.fields.fatIntake.label'), unit: t('photoEvolution.form.fields.fatIntake.unit') },
]);

const numberFormatter = computed(() => new Intl.NumberFormat(locale.value, {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
}));

const dateFormatter = computed(() => new Intl.DateTimeFormat(locale.value, { dateStyle: 'medium' }));

const isMismatch = computed(() => {
  if (entries.value.length < 2) {
    return false;
  }
  const [first, second] = entries.value;
  return first.bodyPart !== second.bodyPart;
});

const canCompare = computed(() => entries.value.length === 2 && !isMismatch.value);

const comparisonBodyPart = computed(() => (entries.value[0]?.bodyPart ?? null));

const comparisonDifference = computed(() => {
  if (!canCompare.value) {
    return [];
  }
  const [first, second] = entries.value;
  return numericFields.value.map((metric) => {
    const initial = toNumber(first[metric.key]);
    const latest = toNumber(second[metric.key]);
    if (initial === null || latest === null) {
      return {
        key: metric.key,
        label: metric.label,
        unit: metric.unit,
        value: '—',
        class: 'text-slate-500',
      };
    }
    const diff = latest - initial;
    const formatted = `${diff > 0 ? '+' : diff < 0 ? '' : ''}${numberFormatter.value.format(diff)}`;
    const diffClass = diff > 0 ? 'text-emerald-600 font-semibold'
      : diff < 0 ? 'text-red-500 font-semibold'
        : 'text-slate-600';
    return {
      key: metric.key,
      label: metric.label,
      unit: metric.unit,
      value: formatted,
      class: diffClass,
    };
  });
});

function formatDate(value) {
  if (!value) {
    return '—';
  }
  try {
    const date = value.length === 10 ? new Date(`${value}T00:00:00`) : new Date(value);
    return dateFormatter.value.format(date);
  } catch (error) {
    return value;
  }
}

function formatNumber(value) {
  if (value === null || value === undefined || value === '') {
    return '—';
  }
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return '—';
  }
  return numberFormatter.value.format(numeric);
}

function toNumber(value) {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
}

function comparisonMetrics(entry) {
  return numericFields.value
    .map((metric) => ({
      key: metric.key,
      label: metric.label,
      value: formatNumber(entry[metric.key]),
      unit: metric.unit,
    }))
    .filter((metric) => metric.value !== '—');
}

function bodyPartLabel(value) {
  if (!value) {
    return t('photoEvolution.bodyParts.UNKNOWN');
  }
  const key = `photoEvolution.bodyParts.${value}`;
  const translation = t(key);
  return translation === key ? value : translation;
}

function buildComparisonQuery(ids) {
  const [first, second] = ids;
  const query = {};
  if (first) {
    query.first = first;
  }
  if (second) {
    query.second = second;
  }
  return query;
}

async function loadComparisonEntries() {
  const ids = selectionIds.value;
  if (!ids.length) {
    entries.value = [];
    return;
  }
  loading.value = true;
  try {
    const responses = await Promise.all(ids.map((id) => photoEvolutionService.get(id)
      .then((response) => response.data)
      .catch(() => null)));
    const valid = responses.filter((item) => Boolean(item));
    if (valid.length !== ids.length) {
      notifications.push({
        type: 'error',
        title: t('photoEvolution.notifications.errorTitle'),
        message: t('photoEvolution.notifications.loadFailed'),
      });
    }
    entries.value = valid.slice(0, 2);
  } catch (error) {
    entries.value = [];
    notifications.push({
      type: 'error',
      title: t('photoEvolution.notifications.errorTitle'),
      message: t('photoEvolution.notifications.loadFailed'),
    });
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push({ name: 'photo-evolution', query: buildComparisonQuery(selectionIds.value) });
}

watch(selectionIds, () => {
  loadComparisonEntries();
});

onMounted(() => {
  loadComparisonEntries();
});
</script>
