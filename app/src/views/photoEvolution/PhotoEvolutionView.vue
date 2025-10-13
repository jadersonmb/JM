<template>
  <div class="space-y-8">
    <section class="rounded-3xl bg-white p-6 shadow-sm">
      <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-slate-900">{{ t('photoEvolution.title') }}</h1>
          <p class="mt-1 max-w-2xl text-sm text-slate-500">{{ t('photoEvolution.subtitle') }}</p>
        </div>
        <div class="flex items-center gap-3 text-sm text-slate-500">
          <PhotoIcon class="h-10 w-10 text-primary-500" />
          <div class="hidden md:block">
            <p class="font-semibold text-slate-700">{{ totalEntriesLabel }}</p>
            <p v-if="filterBodyPart" class="text-xs text-slate-400">{{ bodyPartLabel(filterBodyPart) }}</p>
          </div>
        </div>
      </div>

      <div class="mt-6 grid gap-6 md:grid-cols-2">
        <div v-if="isAdmin" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.filters.searchOwner') }}</label>
            <div class="relative mt-1">
              <input
                v-model="ownerSearch"
                type="text"
                :placeholder="t('photoEvolution.filters.searchOwner')"
                class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
              />
              <ArrowPathIcon
                v-if="ownersLoading"
                class="absolute right-3 top-1/2 h-5 w-5 -translate-y-1/2 animate-spin text-primary-500"
              />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.filters.user') }}</label>
            <select
              v-model="selectedUserId"
              class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
            >
              <option :value="null">{{ t('photoEvolution.filters.noUserSelected') }}</option>
              <option
                v-for="owner in owners"
                :key="owner.id"
                :value="owner.id"
              >
                {{ owner.displayName }}
                <span v-if="owner.email" class="text-slate-400">— {{ owner.email }}</span>
              </option>
            </select>
            <p v-if="!owners.length && !ownersLoading" class="mt-1 text-xs text-slate-400">{{ t('photoEvolution.filters.noOwners') }}</p>
          </div>
        </div>

        <div v-else class="space-y-2">
          <p class="text-sm font-medium text-slate-600">{{ t('photoEvolution.filters.user') }}</p>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
            <p class="font-semibold text-slate-700">{{ auth.user?.name }}</p>
            <p class="text-xs text-slate-400">{{ auth.user?.email }}</p>
          </div>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.filters.bodyPart') }}</label>
            <select
              v-model="filterBodyPart"
              class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
            >
              <option :value="null">{{ t('photoEvolution.filters.allBodyParts') }}</option>
              <option v-for="option in bodyPartOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-2xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-primary-200 hover:text-primary-600"
              @click="loadEntries"
            >
              <ArrowPathIcon class="h-4 w-4" />
              {{ t('photoEvolution.actions.refresh') }}
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-2xl border border-transparent bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-200"
              @click="clearFilters"
            >
              <XMarkIcon class="h-4 w-4" />
              {{ t('photoEvolution.actions.clearFilters') }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="rounded-3xl bg-white p-6 shadow-sm">
      <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 class="text-xl font-semibold text-slate-900">
            {{ form.id ? t('photoEvolution.form.editTitle') : t('photoEvolution.form.title') }}
          </h2>
          <p class="text-sm text-slate-500">{{ t('photoEvolution.form.helper') }}</p>
        </div>
        <div class="flex items-center gap-3">
          <button
            v-if="form.id"
            type="button"
            class="inline-flex items-center gap-2 rounded-2xl border border-transparent bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-200"
            @click="resetForm"
          >
            <XMarkIcon class="h-4 w-4" />
            {{ t('photoEvolution.form.actions.cancel') }}
          </button>
        </div>
      </div>

      <form class="mt-6 space-y-6" @submit.prevent="saveEntry">
        <div class="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.capturedAt.label') }}</label>
            <input
              v-model="form.capturedAt"
              type="date"
              class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.bodyPart.label') }}</label>
            <select
              v-model="form.bodyPart"
              class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
            >
              <option v-for="option in bodyPartOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
          <div v-for="metric in numericFields" :key="metric.key">
            <label class="block text-sm font-medium text-slate-600">
              {{ metric.label }}
              <span v-if="metric.unit" class="font-normal text-slate-400">({{ metric.unit }})</span>
            </label>
            <input
              v-model="form[metric.key]"
              type="number"
              step="0.01"
              class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.notes.label') }}</label>
          <textarea
            v-model="form.notes"
            rows="3"
            class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
          ></textarea>
        </div>

        <div class="grid gap-6 md:grid-cols-2">
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.image.label') }}</label>
            <div class="mt-1 flex items-center gap-4">
              <div
                class="flex h-32 w-32 items-center justify-center overflow-hidden rounded-2xl border border-dashed border-slate-300 bg-slate-50"
              >
                <img v-if="form.imagePreview" :src="form.imagePreview" alt="preview" class="h-full w-full object-cover" />
                <PhotoIcon v-else class="h-12 w-12 text-slate-300" />
              </div>
              <div class="space-y-2 text-sm text-slate-500">
                <p>{{ t('photoEvolution.form.fields.image.helper') }}</p>
                <label
                  class="inline-flex cursor-pointer items-center gap-2 rounded-2xl border border-primary-200 bg-primary-50 px-4 py-2 font-semibold text-primary-600 transition hover:bg-primary-100"
                >
                  <PhotoIcon class="h-4 w-4" />
                  {{ form.imagePreview ? t('photoEvolution.form.fields.image.change') : t('photoEvolution.form.fields.image.select') }}
                  <input type="file" accept="image/*" class="hidden" @change="handleFileChange" />
                </label>
              </div>
            </div>
          </div>
          <div class="flex items-end">
            <button
              type="submit"
              :disabled="saving"
              class="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-transparent bg-primary-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <ArrowPathIcon v-if="saving" class="h-5 w-5 animate-spin" />
              <PlusIcon v-else class="h-5 w-5" />
              {{ form.id ? t('photoEvolution.form.actions.update') : t('photoEvolution.form.actions.save') }}
            </button>
          </div>
        </div>
      </form>
    </section>

    <section v-if="comparisonEntries.length" class="rounded-3xl border border-primary-100 bg-primary-50/60 p-6 shadow-sm">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h2 class="text-lg font-semibold text-primary-700">{{ t('photoEvolution.comparison.title') }}</h2>
          <p class="text-sm text-primary-600">{{ t('photoEvolution.comparison.subtitle') }}</p>
        </div>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-2xl border border-transparent bg-primary-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-primary-500"
          @click="clearComparison"
        >
          <XMarkIcon class="h-4 w-4" />
          {{ t('photoEvolution.comparison.reset') }}
        </button>
      </div>

      <div class="mt-6 grid gap-6 md:grid-cols-3">
        <div
          v-for="(entry, index) in comparisonEntries"
          :key="entry.id"
          class="rounded-2xl bg-white p-4 shadow-sm"
        >
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
            {{ index === 0 ? t('photoEvolution.comparison.first') : t('photoEvolution.comparison.second') }}
          </p>
          <p class="mt-1 text-base font-semibold text-slate-800">
            {{ formatDate(entry.capturedAt || entry.createdAt) }}
          </p>
          <p class="text-xs text-slate-400">{{ bodyPartLabel(entry.bodyPart) }}</p>
          <img
            v-if="entry.imageUrl"
            :src="entry.imageUrl"
            alt="comparison"
            class="mt-4 h-48 w-full rounded-xl object-cover"
          />
          <ul class="mt-4 space-y-2 text-sm text-slate-600">
            <li v-for="metric in comparisonMetrics(entry)" :key="metric.key" class="flex items-center justify-between">
              <span class="text-slate-500">{{ metric.label }}</span>
              <span class="font-semibold text-slate-700">
                {{ metric.value }}
                <span v-if="metric.unit" class="text-xs font-normal text-slate-400">{{ metric.unit }}</span>
              </span>
            </li>
          </ul>
        </div>

        <div class="rounded-2xl bg-white p-4 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ t('photoEvolution.comparison.difference') }}</p>
          <p class="mt-1 text-base font-semibold text-slate-800">{{ bodyPartLabel(comparisonBodyPart) }}</p>
          <ul class="mt-4 space-y-2 text-sm">
            <li
              v-for="metric in comparisonDifference"
              :key="metric.key"
              class="flex items-center justify-between"
            >
              <span class="text-slate-500">{{ metric.label }}</span>
              <span :class="metric.class">
                {{ metric.value }}
                <span v-if="metric.unit" class="text-xs font-normal text-slate-400">{{ metric.unit }}</span>
              </span>
            </li>
          </ul>
        </div>
      </div>
    </section>

    <section class="space-y-6">
      <div v-if="loadingEntries" class="flex items-center justify-center rounded-3xl bg-white p-12 shadow-sm">
        <ArrowPathIcon class="h-8 w-8 animate-spin text-primary-500" />
      </div>
      <template v-else>
        <div v-if="!entries.length" class="rounded-3xl bg-white p-12 text-center shadow-sm">
          <p class="text-lg font-semibold text-slate-700">{{ t('photoEvolution.empty.title') }}</p>
          <p class="mt-2 text-sm text-slate-500">{{ t('photoEvolution.empty.subtitle') }}</p>
        </div>
        <div v-else class="space-y-8">
          <div
            v-for="group in groupedEntries"
            :key="group.bodyPart"
            class="rounded-3xl bg-white p-6 shadow-sm"
          >
            <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
              <div>
                <h3 class="text-lg font-semibold text-slate-900">
                  {{ t('photoEvolution.table.sectionTitle', { part: group.label }) }}
                </h3>
                <p class="text-xs text-slate-400">
                  {{ t('photoEvolution.table.count', { count: group.items.length }) }}
                </p>
              </div>
            </div>

            <div class="mt-4 grid gap-6 lg:grid-cols-2 xl:grid-cols-3">
              <article
                v-for="entry in group.items"
                :key="entry.id"
                class="flex flex-col rounded-2xl border border-slate-100 bg-white p-4 shadow-sm transition hover:-translate-y-1 hover:shadow-md"
              >
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-sm font-semibold text-slate-800">
                      {{ formatDate(entry.capturedAt || entry.createdAt) }}
                    </p>
                    <p class="text-xs text-slate-400">
                      {{ entry.userDisplayName }}
                    </p>
                  </div>
                  <div class="flex gap-2">
                    <button
                      type="button"
                      class="rounded-full border border-slate-200 p-2 text-slate-400 transition hover:border-primary-200 hover:text-primary-600"
                      @click="startEdit(entry)"
                    >
                      <PencilSquareIcon class="h-4 w-4" />
                    </button>
                    <button
                      type="button"
                      class="rounded-full border border-slate-200 p-2 text-slate-400 transition hover:border-red-200 hover:text-red-500"
                      @click="deleteEntry(entry)"
                    >
                      <TrashIcon class="h-4 w-4" />
                    </button>
                    <button
                      type="button"
                      class="rounded-full border border-slate-200 p-2 text-slate-400 transition hover:border-emerald-200 hover:text-emerald-500"
                      @click="toggleComparison(entry)"
                    >
                      <ArrowsRightLeftIcon class="h-4 w-4" />
                    </button>
                  </div>
                </div>

                <div class="mt-4 overflow-hidden rounded-xl bg-slate-50">
                  <img
                    v-if="entry.imageUrl"
                    :src="entry.imageUrl"
                    alt="evolution"
                    class="h-56 w-full object-cover"
                  />
                  <div v-else class="flex h-56 items-center justify-center text-slate-400">
                    <PhotoIcon class="h-10 w-10" />
                  </div>
                </div>

                <dl class="mt-4 grid grid-cols-2 gap-3 text-xs text-slate-600">
                  <template v-for="metric in cardMetrics(entry)" :key="metric.key">
                    <div class="rounded-xl bg-slate-50 px-3 py-2">
                      <dt class="text-[10px] uppercase tracking-wide text-slate-400">{{ metric.label }}</dt>
                      <dd class="mt-1 text-sm font-semibold text-slate-700">
                        {{ metric.value }}
                        <span v-if="metric.unit" class="text-[10px] font-normal text-slate-400">{{ metric.unit }}</span>
                      </dd>
                    </div>
                  </template>
                </dl>

                <p v-if="entry.notes" class="mt-3 rounded-xl bg-slate-50 px-3 py-2 text-sm text-slate-600">
                  {{ entry.notes }}
                </p>
              </article>
            </div>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import photoEvolutionService from '@/services/photoEvolution';
import { uploadFile } from '@/services/cloudFlare';
import {
  ArrowsRightLeftIcon,
  ArrowPathIcon,
  PencilSquareIcon,
  PhotoIcon,
  PlusIcon,
  TrashIcon,
  XMarkIcon,
} from '@heroicons/vue/24/outline';

const { t, locale } = useI18n();
const auth = useAuthStore();
const notifications = useNotificationStore();

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const selectedUserId = ref(isAdmin.value ? null : auth.user?.id ?? null);
const owners = ref([]);
const ownerSearch = ref('');
const ownersLoading = ref(false);
let ownerSearchTimer;

const entries = ref([]);
const loadingEntries = ref(false);
const filterBodyPart = ref(null);
const comparisonIds = ref([]);
const comparisonBodyPart = ref(null);

const numberFormatter = computed(() => new Intl.NumberFormat(locale.value, {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
}));
const dateFormatter = computed(() => new Intl.DateTimeFormat(locale.value, { dateStyle: 'medium' }));

const bodyPartOptions = computed(() => [
  { value: 'FRONT', label: t('photoEvolution.bodyParts.FRONT') },
  { value: 'BACK', label: t('photoEvolution.bodyParts.BACK') },
  { value: 'LEFT_SIDE', label: t('photoEvolution.bodyParts.LEFT_SIDE') },
  { value: 'RIGHT_SIDE', label: t('photoEvolution.bodyParts.RIGHT_SIDE') },
  { value: 'ABDOMEN', label: t('photoEvolution.bodyParts.ABDOMEN') },
  { value: 'ARMS', label: t('photoEvolution.bodyParts.ARMS') },
  { value: 'LEGS', label: t('photoEvolution.bodyParts.LEGS') },
  { value: 'SHOULDERS', label: t('photoEvolution.bodyParts.SHOULDERS') },
  { value: 'FULL_BODY', label: t('photoEvolution.bodyParts.FULL_BODY') },
]);

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

const form = reactive({
  id: null,
  capturedAt: '',
  bodyPart: bodyPartOptions.value[0]?.value ?? 'FRONT',
  weight: '',
  bodyFatPercentage: '',
  muscleMass: '',
  visceralFat: '',
  waistCircumference: '',
  hipCircumference: '',
  chestCircumference: '',
  leftArmCircumference: '',
  rightArmCircumference: '',
  leftThighCircumference: '',
  rightThighCircumference: '',
  caloricIntake: '',
  proteinIntake: '',
  carbohydrateIntake: '',
  fatIntake: '',
  notes: '',
  imageFile: null,
  imagePreview: '',
  imageId: null,
});

const saving = ref(false);

const comparisonEntries = computed(() => comparisonIds.value
  .map((id) => entries.value.find((entry) => entry.id === id))
  .filter((entry) => Boolean(entry)));

const comparisonDifference = computed(() => {
  if (comparisonEntries.value.length < 2) {
    return [];
  }
  const [first, second] = comparisonEntries.value;
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

const groupedEntries = computed(() => {
  const groups = new Map();
  entries.value.forEach((entry) => {
    const key = entry.bodyPart ?? 'UNKNOWN';
    if (!groups.has(key)) {
      groups.set(key, []);
    }
    groups.get(key).push(entry);
  });
  return Array.from(groups.entries()).map(([bodyPart, items]) => ({
    bodyPart,
    label: bodyPartLabel(bodyPart),
    items: items.slice().sort((a, b) => compareDates(b, a)),
  }));
});

const totalEntriesLabel = computed(() => {
  if (!entries.value.length) {
    return t('photoEvolution.summary.empty');
  }
  return t('photoEvolution.summary.count', { count: entries.value.length });
});

function compareDates(a, b) {
  const dateA = new Date((a.capturedAt ?? a.createdAt ?? '') + '');
  const dateB = new Date((b.capturedAt ?? b.createdAt ?? '') + '');
  return dateA - dateB;
}

function formatDate(value) {
  if (!value) return '—';
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

function bodyPartLabel(value) {
  if (!value) {
    return t('photoEvolution.bodyParts.UNKNOWN');
  }
  const key = `photoEvolution.bodyParts.${value}`;
  const translation = t(key);
  return translation === key ? value : translation;
}

function resetForm() {
  form.id = null;
  form.capturedAt = '';
  form.bodyPart = bodyPartOptions.value[0]?.value ?? 'FRONT';
  numericFields.value.forEach((metric) => {
    form[metric.key] = '';
  });
  form.notes = '';
  form.imageFile = null;
  form.imagePreview = '';
  form.imageId = null;
}

async function handleFileChange(event) {
  const [file] = event.target.files ?? [];
  if (!file) {
    return;
  }
  form.imageFile = file;
  form.imagePreview = URL.createObjectURL(file);
}

async function loadOwners(query = '') {
  if (!isAdmin.value) {
    return;
  }
  ownersLoading.value = true;
  try {
    const { data } = await photoEvolutionService.listOwners({ query });
    owners.value = data ?? [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('photoEvolution.notifications.errorTitle'),
      message: t('photoEvolution.notifications.ownersLoadFailed'),
    });
  } finally {
    ownersLoading.value = false;
  }
}

async function loadEntries() {
  if (!isAdmin.value && !selectedUserId.value) {
    selectedUserId.value = auth.user?.id ?? null;
  }
  if (isAdmin.value && !selectedUserId.value) {
    entries.value = [];
    return;
  }
  const params = {};
  if (selectedUserId.value) {
    params.userId = selectedUserId.value;
  }
  if (filterBodyPart.value) {
    params.bodyPart = filterBodyPart.value;
  }
  loadingEntries.value = true;
  try {
    const { data } = await photoEvolutionService.list(params);
    entries.value = data ?? [];
    comparisonIds.value = comparisonIds.value.filter((id) => entries.value.some((entry) => entry.id === id));
    if (comparisonIds.value.length === 0) {
      comparisonBodyPart.value = null;
    }
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('photoEvolution.notifications.errorTitle'),
      message: t('photoEvolution.notifications.loadFailed'),
    });
  } finally {
    loadingEntries.value = false;
  }
}

function clearFilters() {
  filterBodyPart.value = null;
  loadEntries();
}

function parsePayloadNumber(value) {
  const numeric = toNumber(value);
  return numeric === null ? null : numeric;
}

async function ensureImageId() {
  if (form.imageId && !form.imageFile) {
    return form.imageId;
  }
  if (!form.imageFile) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.notifications.invalidImage'),
    });
    throw new Error('IMAGE_REQUIRED');
  }
  const { data } = await uploadFile({ file: form.imageFile, userId: selectedUserId.value });
  form.imageId = data.id;
  form.imagePreview = data.url;
  form.imageFile = null;
  return form.imageId;
}

async function saveEntry() {
  if (!selectedUserId.value) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.notifications.formMissingUser'),
    });
    return;
  }
  saving.value = true;
  try {
    const imageId = await ensureImageId();
    const payload = {
      userId: selectedUserId.value,
      imageId,
      bodyPart: form.bodyPart,
      capturedAt: form.capturedAt || null,
      notes: form.notes || null,
    };
    numericFields.value.forEach((metric) => {
      payload[metric.key] = parsePayloadNumber(form[metric.key]);
    });

    if (form.id) {
      await photoEvolutionService.update(form.id, payload);
      notifications.push({
        type: 'success',
        title: t('photoEvolution.notifications.successTitle'),
        message: t('photoEvolution.notifications.updated'),
      });
    } else {
      await photoEvolutionService.create(payload);
      notifications.push({
        type: 'success',
        title: t('photoEvolution.notifications.successTitle'),
        message: t('photoEvolution.notifications.created'),
      });
    }
    await loadEntries();
    resetForm();
  } catch (error) {
    if (error?.message !== 'IMAGE_REQUIRED') {
      notifications.push({
        type: 'error',
        title: t('photoEvolution.notifications.errorTitle'),
        message: t('photoEvolution.notifications.saveFailed'),
      });
    }
  } finally {
    saving.value = false;
  }
}

function startEdit(entry) {
  form.id = entry.id;
  form.capturedAt = entry.capturedAt ?? '';
  form.bodyPart = entry.bodyPart ?? bodyPartOptions.value[0]?.value ?? 'FRONT';
  numericFields.value.forEach((metric) => {
    form[metric.key] = entry[metric.key] ?? '';
  });
  form.notes = entry.notes ?? '';
  form.imageId = entry.imageId ?? null;
  form.imagePreview = entry.imageUrl ?? '';
  form.imageFile = null;
}

async function deleteEntry(entry) {
  const confirmed = window.confirm(t('photoEvolution.notifications.confirmDelete'));
  if (!confirmed) {
    return;
  }
  await photoEvolutionService.remove(entry.id);
  notifications.push({
    type: 'success',
    title: t('photoEvolution.notifications.successTitle'),
    message: t('photoEvolution.notifications.deleted'),
  });
  comparisonIds.value = comparisonIds.value.filter((id) => id !== entry.id);
  if (comparisonIds.value.length === 0) {
    comparisonBodyPart.value = null;
  }
  await loadEntries();
}

function toggleComparison(entry) {
  const index = comparisonIds.value.indexOf(entry.id);
  if (index >= 0) {
    comparisonIds.value.splice(index, 1);
    if (!comparisonIds.value.length) {
      comparisonBodyPart.value = null;
    }
    return;
  }
  if (comparisonBodyPart.value && comparisonBodyPart.value !== entry.bodyPart) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.notifications.comparisonMismatch'),
    });
    comparisonIds.value = [entry.id];
    comparisonBodyPart.value = entry.bodyPart;
    return;
  }
  if (comparisonIds.value.length === 2) {
    comparisonIds.value.shift();
  }
  comparisonIds.value.push(entry.id);
  comparisonBodyPart.value = entry.bodyPart;
}

function clearComparison() {
  comparisonIds.value = [];
  comparisonBodyPart.value = null;
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

function cardMetrics(entry) {
  const metrics = comparisonMetrics(entry);
  return metrics.length ? metrics : [{
    key: 'empty',
    label: t('photoEvolution.card.noMetrics'),
    value: '—',
    unit: '',
  }];
}

watch(() => auth.user?.id, (newId) => {
  if (!isAdmin.value) {
    selectedUserId.value = newId ?? null;
  }
});

watch(ownerSearch, (value) => {
  if (!isAdmin.value) {
    return;
  }
  clearTimeout(ownerSearchTimer);
  ownerSearchTimer = setTimeout(() => loadOwners(value), 300);
});

watch([selectedUserId, filterBodyPart], () => {
  loadEntries();
});

onMounted(() => {
  if (isAdmin.value) {
    loadOwners();
  } else {
    loadEntries();
  }
});
</script>
