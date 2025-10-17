<template>
  <div class="space-y-6 rounded-3xl bg-gray-50 p-6 shadow-sm md:p-8">
    <header class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <h1 class="text-3xl font-semibold text-gray-900">{{ t('reminders.title') }}</h1>
        <p class="text-gray-500">{{ t('reminders.subtitle') }}</p>
      </div>
      <div class="flex w-full flex-col gap-3 sm:flex-row sm:items-center sm:justify-end">
        <div class="relative w-full sm:w-72">
          <MagnifyingGlassIcon
            class="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400"
          />
          <input
            v-model="searchTerm"
            :placeholder="t('reminders.searchPlaceholder')"
            type="text"
            class="w-full rounded-xl border border-gray-200 bg-white py-2.5 pl-10 pr-4 text-sm text-gray-700 shadow-sm transition focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
          />
        </div>
        <button
          type="button"
          class="inline-flex items-center justify-center gap-2 rounded-xl bg-blue-500 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-200 focus:ring-offset-2"
          @click="openCreateModal"
        >
          <PlusCircleIcon class="h-5 w-5" />
          <span>{{ t('reminders.actions.new') }}</span>
        </button>
      </div>
    </header>

    <section>
      <div v-if="loading" class="grid gap-4">
        <div
          v-for="skeleton in 3"
          :key="`skeleton-${skeleton}`"
          class="h-28 animate-pulse rounded-2xl bg-white shadow-sm"
        />
      </div>
      <div v-else-if="reminders.length" class="grid gap-4">
        <article
          v-for="reminder in reminders"
          :key="reminder.id"
          class="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm transition-all duration-300 hover:border-blue-200 hover:shadow-lg"
        >
          <div class="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
            <div class="flex flex-1 items-start gap-4">
              <div
                class="flex h-14 w-14 items-center justify-center rounded-full"
                :class="[typeVisuals[reminder.type]?.bg, typeVisuals[reminder.type]?.text]"
              >
                <component
                  :is="typeVisuals[reminder.type]?.icon ?? SparklesIcon"
                  class="h-6 w-6"
                />
              </div>
              <div class="space-y-2">
                <div class="flex flex-wrap items-center gap-2">
                  <h3 class="text-lg font-semibold text-gray-800">{{ reminder.title }}</h3>
                  <span
                    v-if="reminder.completed"
                    class="inline-flex items-center rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-semibold text-emerald-700"
                  >
                    {{ t('reminders.card.completedBadge') }}
                  </span>
                  <span
                    v-else-if="!reminder.active"
                    class="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-600"
                  >
                    {{ t('reminders.card.pausedBadge') }}
                  </span>
                </div>
                <p v-if="reminder.description" class="text-sm text-gray-600">
                  {{ reminder.description }}
                </p>
                <div class="flex flex-wrap items-center gap-3 text-xs text-gray-500">
                  <span class="inline-flex items-center gap-1">
                    <ClockIcon class="h-4 w-4" />
                    <span>{{ formatScheduledAt(reminder.scheduledAt) }}</span>
                  </span>
                  <span class="inline-flex items-center gap-1">
                    <ArrowPathIcon class="h-4 w-4" />
                    <span>{{ describeRepeat(reminder) }}</span>
                  </span>
                  <span
                    v-if="isAdmin && (reminder.targetUserName || reminder.targetUserPhone)"
                    class="inline-flex items-center gap-1"
                  >
                    <span class="font-medium text-gray-600">{{ t('reminders.card.recipientLabel') }}:</span>
                    <span>{{ reminder.targetUserName || reminder.targetUserPhone }}</span>
                  </span>
                </div>
              </div>
            </div>
            <div class="flex w-full flex-col gap-4 md:w-auto md:flex-row md:items-center md:gap-6">
              <span
                class="inline-flex items-center justify-center rounded-full px-3 py-1 text-xs font-semibold"
                :class="[priorityVisuals[reminder.priority]?.bg, priorityVisuals[reminder.priority]?.text]"
              >
                {{ priorityLabel(reminder.priority) }}
              </span>
              <label class="flex items-center gap-2 text-sm text-gray-600">
                <input
                  type="checkbox"
                  class="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  :checked="reminder.completed"
                  @change="() => handleToggleCompleted(reminder)"
                />
                <span>{{ t('reminders.card.completedToggle') }}</span>
              </label>
              <div class="flex items-center gap-3">
                <span class="text-xs font-medium text-gray-500">
                  {{ reminder.active ? t('reminders.card.active') : t('reminders.card.inactive') }}
                </span>
                <button
                  type="button"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition"
                  :class="reminder.active ? 'bg-blue-500' : 'bg-gray-300'"
                  @click="() => handleToggleActive(reminder)"
                >
                  <span class="sr-only">{{ t('reminders.card.toggleAria', { title: reminder.title }) }}</span>
                  <span
                    class="absolute left-1 top-1 h-4 w-4 rounded-full bg-white shadow-sm transition"
                    :class="reminder.active ? 'translate-x-5' : 'translate-x-0'"
                  />
                </button>
              </div>
              <div class="flex items-center gap-2">
                <button
                  type="button"
                  class="rounded-xl border border-transparent bg-emerald-50 p-2 text-emerald-600 transition hover:border-emerald-200 hover:bg-emerald-100"
                  @click="() => handleTriggerTest(reminder)"
                >
                  <BoltIcon class="h-5 w-5" />
                  <span class="sr-only">{{ t('reminders.actions.test') }}</span>
                </button>
                <button
                  type="button"
                  class="rounded-xl border border-transparent bg-blue-50 p-2 text-blue-600 transition hover:border-blue-200 hover:bg-blue-100"
                  @click="() => openEditModal(reminder)"
                >
                  <PencilSquareIcon class="h-5 w-5" />
                  <span class="sr-only">{{ t('reminders.actions.edit') }}</span>
                </button>
                <button
                  type="button"
                  class="rounded-xl border border-transparent bg-red-50 p-2 text-red-600 transition hover:border-red-200 hover:bg-red-100"
                  @click="() => handleDelete(reminder)"
                >
                  <TrashIcon class="h-5 w-5" />
                  <span class="sr-only">{{ t('reminders.actions.delete') }}</span>
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>
      <div
        v-else
        class="rounded-2xl border border-dashed border-gray-300 bg-white p-12 text-center shadow-sm"
      >
        <p class="text-lg font-semibold text-gray-700">{{ t('reminders.empty.title') }}</p>
        <p class="mt-2 text-sm text-gray-500">{{ t('reminders.empty.subtitle') }}</p>
        <button
          type="button"
          class="mt-6 inline-flex items-center justify-center gap-2 rounded-xl border border-blue-200 bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-600 transition hover:bg-blue-100"
          @click="openCreateModal"
        >
          <PlusCircleIcon class="h-5 w-5" />
          <span>{{ t('reminders.actions.new') }}</span>
        </button>
      </div>
    </section>
  </div>

  <transition name="fade">
    <div
      v-if="isModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm px-4"
    >
      <div class="w-full max-w-2xl rounded-3xl bg-white p-6 shadow-2xl">
        <div class="flex items-start justify-between gap-4">
          <div>
            <h2 class="text-2xl font-semibold text-gray-900">
              {{ editingId ? t('reminders.form.editTitle') : t('reminders.form.newTitle') }}
            </h2>
            <p class="mt-1 text-sm text-gray-500">{{ t('reminders.form.subtitle') }}</p>
          </div>
          <button
            type="button"
            class="rounded-full p-2 text-gray-400 transition hover:bg-gray-100 hover:text-gray-600"
            @click="closeModal"
          >
            <XMarkIcon class="h-5 w-5" />
            <span class="sr-only">{{ t('reminders.form.close') }}</span>
          </button>
        </div>

        <form class="mt-6 space-y-4" @submit.prevent="saveReminder">
          <div class="grid gap-4 md:grid-cols-2">
            <div class="md:col-span-2">
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.title.label') }}
              </label>
              <input
                v-model="form.title"
                type="text"
                :placeholder="t('reminders.form.fields.title.placeholder')"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              />
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.description.label') }}
              </label>
              <textarea
                v-model="form.description"
                rows="3"
                :placeholder="t('reminders.form.fields.description.placeholder')"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              />
            </div>
            <div>
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.repeatMode.label') }}
              </label>
              <select
                v-model="form.repeatMode"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              >
                <option
                  v-for="option in repeatModeOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-semibold text-gray-700">
                {{ scheduledAtLabel }}
              </label>
              <input
                v-model="form.scheduledAt"
                type="datetime-local"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              />
              <p v-if="form.repeatMode !== 'DATE_TIME'" class="mt-1 text-xs text-gray-400">
                {{ scheduledAtPlaceholder }}
              </p>
            </div>
            <div v-if="needsTimeInput" class="md:col-span-1">
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.timeOfDay.label') }}
              </label>
              <input
                v-model="form.repeatTimeOfDay"
                type="time"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              />
            </div>
            <div v-if="needsWeekdaySelection" class="md:col-span-2">
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.weekdays.label') }}
              </label>
              <div class="mt-2 grid grid-cols-3 gap-2 sm:grid-cols-4">
                <button
                  v-for="option in weekdayOptions"
                  :key="option.value"
                  type="button"
                  class="rounded-lg border px-3 py-2 text-xs font-semibold transition"
                  :class="isWeekdaySelected(option.value)
                    ? 'border-blue-500 bg-blue-50 text-blue-600'
                    : 'border-gray-200 bg-white text-gray-600 hover:border-blue-200 hover:text-blue-500'"
                  @click="toggleWeekday(option.value)"
                >
                  {{ option.label }}
                </button>
              </div>
            </div>
            <div v-if="needsIntervalInput">
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.interval.label') }}
              </label>
              <input
                v-model.number="form.repeatIntervalMinutes"
                type="number"
                min="1"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              />
              <p class="mt-1 text-xs text-gray-400">
                {{ t('reminders.form.fields.interval.hint') }}
              </p>
            </div>
            <div v-if="needsCountInput">
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.count.label') }}
              </label>
              <input
                v-model.number="form.repeatCountTotal"
                type="number"
                min="1"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              />
            </div>
            <div>
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.priority.label') }}
              </label>
              <select
                v-model="form.priority"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              >
                <option
                  v-for="option in priorityOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.type.label') }}
              </label>
              <select
                v-model="form.type"
                class="mt-1 w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
              >
                <option v-for="option in typeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>
            <div v-if="isAdmin" class="md:col-span-2">
              <label class="block text-sm font-semibold text-gray-700">
                {{ t('reminders.form.fields.target.label') }}
              </label>
              <div class="mt-1 space-y-2">
                <input
                  v-model="targetSearch"
                  type="text"
                  :placeholder="t('reminders.form.fields.target.searchPlaceholder')"
                  class="w-full rounded-xl border border-gray-200 bg-white px-4 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
                />
                <select
                  v-model="form.targetUserId"
                  class="w-full rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 shadow-sm focus:border-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
                >
                  <option value="" disabled>{{ t('reminders.form.fields.target.placeholder') }}</option>
                  <option
                    v-for="target in targetOptions"
                    :key="target.id"
                    :value="target.id"
                  >
                    {{ target.displayName ?? target.phone ?? target.email ?? target.id }}
                    <template v-if="target.phone"> — {{ target.phone }}</template>
                  </option>
                </select>
                <p v-if="targetLoading" class="text-xs text-gray-400">
                  {{ t('reminders.form.fields.target.loading') }}
                </p>
              </div>
            </div>
          </div>

          <div class="flex flex-col gap-3 pt-4 md:flex-row md:justify-end">
            <button
              type="button"
              class="inline-flex items-center justify-center rounded-xl bg-gray-100 px-4 py-2.5 text-sm font-semibold text-gray-600 transition hover:bg-gray-200"
              @click="closeModal"
              :disabled="isSubmitting"
            >
              {{ t('reminders.form.actions.cancel') }}
            </button>
            <button
              type="submit"
              class="inline-flex items-center justify-center rounded-xl bg-blue-500 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-200 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-70"
              :disabled="isSubmitting"
            >
              {{ isSubmitting ? t('reminders.form.actions.saving') : t('reminders.form.actions.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {
  AcademicCapIcon,
  ArrowPathIcon,
  BoltIcon,
  BriefcaseIcon,
  CakeIcon,
  ClockIcon,
  HeartIcon,
  MagnifyingGlassIcon,
  PencilSquareIcon,
  PlusCircleIcon,
  SparklesIcon,
  TrashIcon,
  XMarkIcon,
} from '@heroicons/vue/24/solid';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useNotificationStore } from '@/stores/notifications';
import { useAuthStore } from '@/stores/auth';
import ReminderService from '@/services/ReminderService';

const { t, locale } = useI18n();
const notifications = useNotificationStore();
const auth = useAuthStore();

const reminders = ref([]);
const loading = ref(false);
const searchTerm = ref('');
const searchDebounce = ref(null);

const isModalOpen = ref(false);
const isSubmitting = ref(false);
const editingId = ref(null);

const form = reactive({
  title: '',
  description: '',
  scheduledAt: '',
  priority: 'MEDIUM',
  type: 'CUSTOM',
  targetUserId: null,
  active: true,
  repeatMode: 'DATE_TIME',
  repeatWeekdays: [],
  repeatIntervalMinutes: null,
  repeatCountTotal: null,
  repeatTimeOfDay: '',
});

const targetOptions = ref([]);
const targetSearch = ref('');
const targetLoading = ref(false);
let targetDebounce = null;

const skipModeWatch = ref(false);

const weekdayValues = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const weekdayOrder = weekdayValues.reduce((accumulator, value, index) => {
  accumulator[value] = index;
  return accumulator;
}, {});

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');

const typeVisuals = {
  HEALTH: { icon: HeartIcon, bg: 'bg-blue-100', text: 'text-blue-600' },
  STUDY: { icon: AcademicCapIcon, bg: 'bg-purple-100', text: 'text-purple-600' },
  WORK: { icon: BriefcaseIcon, bg: 'bg-amber-100', text: 'text-amber-600' },
  MEAL: { icon: CakeIcon, bg: 'bg-emerald-100', text: 'text-emerald-600' },
  CUSTOM: { icon: SparklesIcon, bg: 'bg-slate-100', text: 'text-slate-600' },
};

const priorityVisuals = {
  LOW: { bg: 'bg-green-100', text: 'text-green-700' },
  MEDIUM: { bg: 'bg-yellow-100', text: 'text-yellow-700' },
  HIGH: { bg: 'bg-red-100', text: 'text-red-700' },
};

const priorityOptions = computed(() => [
  { value: 'LOW', label: t('reminders.priority.low') },
  { value: 'MEDIUM', label: t('reminders.priority.medium') },
  { value: 'HIGH', label: t('reminders.priority.high') },
]);

const typeOptions = computed(() => [
  { value: 'HEALTH', label: t('reminders.types.health') },
  { value: 'STUDY', label: t('reminders.types.study') },
  { value: 'WORK', label: t('reminders.types.work') },
  { value: 'MEAL', label: t('reminders.types.meal') },
  { value: 'CUSTOM', label: t('reminders.types.custom') },
]);

const repeatModeOptions = computed(() => [
  { value: 'DATE_TIME', label: t('reminders.repeat.modes.dateTime') },
  { value: 'DAILY_TIME', label: t('reminders.repeat.modes.daily') },
  { value: 'WEEKLY', label: t('reminders.repeat.modes.weekly') },
  { value: 'INTERVAL', label: t('reminders.repeat.modes.interval') },
  { value: 'COUNTDOWN', label: t('reminders.repeat.modes.countdown') },
]);

const weekdayOptions = computed(() =>
  weekdayValues.map((value) => ({
    value,
    label: t(`reminders.repeat.weekdays.${value.toLowerCase()}`),
  })),
);

const priorityLabel = (priority) => {
  const key = (priority || 'MEDIUM').toString().toLowerCase();
  return t(`reminders.priority.${key}`);
};

const needsDateTimeInput = computed(() => form.repeatMode === 'DATE_TIME');
const needsTimeInput = computed(() => ['DAILY_TIME', 'WEEKLY'].includes(form.repeatMode));
const needsWeekdaySelection = computed(() => form.repeatMode === 'WEEKLY');
const needsIntervalInput = computed(() => ['INTERVAL', 'COUNTDOWN'].includes(form.repeatMode));
const needsCountInput = computed(() => form.repeatMode === 'COUNTDOWN');

const scheduledAtLabel = computed(() =>
  form.repeatMode === 'DATE_TIME'
    ? t('reminders.form.fields.scheduledAt.label')
    : t('reminders.form.fields.scheduledAt.startLabel'),
);

const scheduledAtPlaceholder = computed(() =>
  form.repeatMode === 'DATE_TIME'
    ? t('reminders.form.fields.scheduledAt.placeholder')
    : t('reminders.form.fields.scheduledAt.startPlaceholder'),
);

const sortWeekdays = (list) =>
  list
    .filter((value) => typeof value === 'string' && value.trim().length)
    .map((value) => value.trim().toUpperCase())
    .filter((value) => weekdayOrder[value] !== undefined)
    .sort((a, b) => weekdayOrder[a] - weekdayOrder[b]);

const toggleWeekday = (day) => {
  const normalized = day.toUpperCase();
  const set = new Set(form.repeatWeekdays);
  if (set.has(normalized)) {
    set.delete(normalized);
  } else {
    set.add(normalized);
  }
  form.repeatWeekdays = sortWeekdays(Array.from(set));
};

const isWeekdaySelected = (day) => form.repeatWeekdays.includes(day);

const formatTime = (value) => {
  if (!value) {
    return '';
  }
  const [hours, minutes] = value.split(':');
  const date = new Date();
  date.setHours(Number.parseInt(hours, 10) || 0, Number.parseInt(minutes, 10) || 0, 0, 0);
  return new Intl.DateTimeFormat(locale.value, {
    hour: 'numeric',
    minute: 'numeric',
  }).format(date);
};

const mapWeekdayLabel = (value) => t(`reminders.repeat.weekdays.${value.toLowerCase()}`);

const describeRepeat = (reminder) => {
  const mode = (reminder.repeatMode || 'DATE_TIME').toUpperCase();
  switch (mode) {
    case 'DAILY_TIME':
      return t('reminders.card.repeatSummary.daily', {
        time: formatTime(reminder.repeatTimeOfDay),
      });
    case 'WEEKLY': {
      const days = (reminder.repeatWeekdays || [])
        .map((value) => mapWeekdayLabel(value))
        .join(', ');
      return t('reminders.card.repeatSummary.weekly', {
        days,
        time: formatTime(reminder.repeatTimeOfDay),
      });
    }
    case 'INTERVAL':
      if (reminder.repeatIntervalMinutes) {
        return t('reminders.card.repeatSummary.interval', {
          minutes: reminder.repeatIntervalMinutes,
        });
      }
      break;
    case 'COUNTDOWN':
      if (reminder.repeatIntervalMinutes && reminder.repeatCountTotal) {
        return t('reminders.card.repeatSummary.countdown', {
          minutes: reminder.repeatIntervalMinutes,
          remaining: reminder.repeatCountRemaining ?? reminder.repeatCountTotal,
        });
      }
      break;
    default:
      break;
  }
  return t('reminders.card.repeatSummary.once');
};

const resetForm = () => {
  form.title = '';
  form.description = '';
  form.scheduledAt = '';
  form.priority = 'MEDIUM';
  form.type = 'CUSTOM';
  form.targetUserId = null;
  form.active = true;
  form.repeatMode = 'DATE_TIME';
  form.repeatWeekdays = [];
  form.repeatIntervalMinutes = null;
  form.repeatCountTotal = null;
  form.repeatTimeOfDay = '';
  targetSearch.value = '';
};

const closeModal = () => {
  isModalOpen.value = false;
  isSubmitting.value = false;
};

const applyModeDefaults = (mode) => {
  if (mode !== 'WEEKLY') {
    form.repeatWeekdays = [];
  }
  if (!['DAILY_TIME', 'WEEKLY'].includes(mode)) {
    form.repeatTimeOfDay = '';
  }
  if (!['INTERVAL', 'COUNTDOWN'].includes(mode)) {
    form.repeatIntervalMinutes = null;
  }
  if (mode !== 'COUNTDOWN') {
    form.repeatCountTotal = null;
  }
};

const ensureTargets = async (query) => {
  if (!isAdmin.value) {
    return;
  }
  targetLoading.value = true;
  try {
    const params = {};
    if (query) {
      params.query = query;
    }
    const { data } = await ReminderService.listTargets(params);
    targetOptions.value = data ?? [];
    if (!form.targetUserId && targetOptions.value.length === 1) {
      form.targetUserId = targetOptions.value[0].id;
    }
  } catch (error) {
    targetOptions.value = [];
  } finally {
    targetLoading.value = false;
  }
};

const toInputDateTime = (value) => {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value.slice(0, 16);
  }
  const offset = date.getTimezoneOffset();
  const local = new Date(date.getTime() - offset * 60000);
  return local.toISOString().slice(0, 16);
};

const toInputTime = (value) => {
  if (!value) {
    return '';
  }
  const parts = value.split(':');
  if (parts.length >= 2) {
    return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
  }
  return value;
};

const openCreateModal = () => {
  resetForm();
  editingId.value = null;
  if (isAdmin.value) {
    ensureTargets();
  }
  isModalOpen.value = true;
};

const openEditModal = (reminder) => {
  resetForm();
  editingId.value = reminder.id;
  form.title = reminder.title ?? '';
  form.description = reminder.description ?? '';
  form.scheduledAt = toInputDateTime(reminder.scheduledAt);
  form.priority = reminder.priority ?? 'MEDIUM';
  form.type = reminder.type ?? 'CUSTOM';
  form.targetUserId = reminder.targetUserId ?? null;
  form.active = reminder.active ?? true;
  skipModeWatch.value = true;
  form.repeatMode = reminder.repeatMode ?? 'DATE_TIME';
  form.repeatWeekdays = sortWeekdays(Array.isArray(reminder.repeatWeekdays) ? reminder.repeatWeekdays : []);
  form.repeatIntervalMinutes = reminder.repeatIntervalMinutes ?? null;
  form.repeatCountTotal = reminder.repeatCountTotal ?? null;
  form.repeatTimeOfDay = toInputTime(reminder.repeatTimeOfDay);
  skipModeWatch.value = false;
  applyModeDefaults(form.repeatMode);
  if (isAdmin.value) {
    if (reminder.targetUserId && !targetOptions.value.some((item) => item.id === reminder.targetUserId)) {
      targetOptions.value = [
        ...targetOptions.value,
        {
          id: reminder.targetUserId,
          displayName: reminder.targetUserName ?? reminder.targetUserPhone ?? reminder.targetUserId,
          phone: reminder.targetUserPhone,
          email: '',
        },
      ];
    }
    ensureTargets(targetSearch.value);
  }
  isModalOpen.value = true;
};

const formatScheduledAt = (value) => {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat(locale.value, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
};

const fetchReminders = async () => {
  loading.value = true;
  try {
    const params = {
      page: 0,
      size: 50,
    };
    if (searchTerm.value) {
      params.query = searchTerm.value;
    }
    const { data } = await ReminderService.list(params);
    if (Array.isArray(data)) {
      reminders.value = data;
    } else if (Array.isArray(data?.content)) {
      reminders.value = data.content;
    } else {
      reminders.value = [];
    }
  } catch (error) {
    reminders.value = [];
  } finally {
    loading.value = false;
  }
};

const validateForm = () => {
  let errorKey = null;
  if (!form.title.trim()) {
    errorKey = 'title';
  } else if (needsDateTimeInput.value && !form.scheduledAt) {
    errorKey = 'dateTime';
  } else if (needsTimeInput.value && !form.repeatTimeOfDay) {
    errorKey = 'time';
  } else if (needsWeekdaySelection.value && form.repeatWeekdays.length === 0) {
    errorKey = 'weekdays';
  } else if (
    needsIntervalInput.value
    && (!form.repeatIntervalMinutes || Number(form.repeatIntervalMinutes) <= 0)
  ) {
    errorKey = 'interval';
  } else if (
    needsCountInput.value
    && (!form.repeatCountTotal || Number(form.repeatCountTotal) <= 0)
  ) {
    errorKey = 'count';
  }

  if (errorKey) {
    notifications.push({
      type: 'warning',
      title: t('reminders.notifications.validationTitle'),
      message: t(`reminders.notifications.errors.${errorKey}`),
    });
    return false;
  }
  return true;
};

const saveReminder = async () => {
  if (!validateForm()) {
    return;
  }
  if (isAdmin.value && !form.targetUserId) {
    notifications.push({
      type: 'warning',
      title: t('reminders.notifications.validationTitle'),
      message: t('reminders.notifications.missingTarget'),
    });
    return;
  }

  const payload = {
    title: form.title.trim(),
    description: form.description?.trim() || null,
    scheduledAt: form.scheduledAt || null,
    priority: form.priority,
    type: form.type,
    active: form.active,
    repeatMode: form.repeatMode,
    repeatWeekdays: needsWeekdaySelection.value ? [...form.repeatWeekdays] : [],
    repeatIntervalMinutes: needsIntervalInput.value
      ? Number(form.repeatIntervalMinutes)
      : null,
    repeatCountTotal: needsCountInput.value ? Number(form.repeatCountTotal) : null,
    repeatTimeOfDay: needsTimeInput.value ? form.repeatTimeOfDay || null : null,
  };
  if (isAdmin.value) {
    payload.targetUserId = form.targetUserId;
  }

  isSubmitting.value = true;
  try {
    if (editingId.value) {
      await ReminderService.update(editingId.value, payload);
      notifications.push({
        type: 'success',
        title: t('reminders.notifications.updated.title'),
        message: t('reminders.notifications.updated.message'),
      });
    } else {
      await ReminderService.create(payload);
      notifications.push({
        type: 'success',
        title: t('reminders.notifications.created.title'),
        message: t('reminders.notifications.created.message'),
      });
    }
    closeModal();
    await fetchReminders();
  } finally {
    isSubmitting.value = false;
  }
};

const handleToggleActive = async (reminder) => {
  const { id, active } = reminder;
  try {
    const { data } = await ReminderService.toggleActive(id, !active);
    Object.assign(reminder, data);
    notifications.push({
      type: 'success',
      title: t('reminders.notifications.status.title'),
      message: data.active
        ? t('reminders.notifications.status.activated')
        : t('reminders.notifications.status.paused'),
    });
  } catch (error) {
    // handled by interceptor
  }
};

const handleToggleCompleted = async (reminder) => {
  const { id, completed } = reminder;
  try {
    const { data } = await ReminderService.toggleCompleted(id, !completed);
    Object.assign(reminder, data);
    notifications.push({
      type: 'success',
      title: t('reminders.notifications.status.title'),
      message: data.completed
        ? t('reminders.notifications.status.completed')
        : t('reminders.notifications.status.reopened'),
    });
  } catch (error) {
    // handled by interceptor
  }
};

const handleDelete = async (reminder) => {
  const confirmed = window.confirm(
    t('reminders.confirmDelete', { title: reminder.title }),
  );
  if (!confirmed) {
    return;
  }
  try {
    await ReminderService.remove(reminder.id);
    notifications.push({
      type: 'success',
      title: t('reminders.notifications.deleted.title'),
      message: t('reminders.notifications.deleted.message'),
    });
    reminders.value = reminders.value.filter((item) => item.id !== reminder.id);
  } catch (error) {
    // handled by interceptor
  }
};

const handleTriggerTest = async (reminder) => {
  try {
    await ReminderService.triggerTest(reminder.id);
    notifications.push({
      type: 'success',
      title: t('reminders.notifications.test.title'),
      message: t('reminders.notifications.test.message'),
    });
  } catch (error) {
    // handled globally
  }
};

watch(
  () => form.repeatMode,
  (mode) => {
    if (skipModeWatch.value) {
      return;
    }
    applyModeDefaults(mode);
  },
);

watch(searchTerm, (value) => {
  if (searchDebounce.value) {
    clearTimeout(searchDebounce.value);
  }
  searchDebounce.value = setTimeout(() => {
    fetchReminders();
  }, 400);
});

watch(targetSearch, (value) => {
  if (!isAdmin.value || !isModalOpen.value) {
    return;
  }
  if (targetDebounce) {
    clearTimeout(targetDebounce);
  }
  targetDebounce = setTimeout(() => {
    ensureTargets(value);
  }, 300);
});

onMounted(() => {
  fetchReminders();
  if (isAdmin.value) {
    ensureTargets();
  }
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

