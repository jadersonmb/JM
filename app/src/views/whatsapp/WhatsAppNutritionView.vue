<template>
    <div class="space-y-6 rounded-3xl bg-gray-50 p-6 shadow-sm md:p-8">
      <header
        class="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white px-6 py-4 shadow-sm lg:flex-row lg:items-center lg:justify-between"
      >
        <div>
          <h1 class="text-2xl font-semibold text-gray-900">Nutrition Assistant</h1>
          <p class="text-sm text-gray-500">
            Monitor WhatsApp submissions and AI-powered nutrition insights.
          </p>
        </div>
        <div class="flex flex-wrap items-center gap-3">
          <div class="relative">
            <button
              type="button"
              class="flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm transition-all duration-300 hover:border-emerald-300 hover:text-emerald-600"
              @click="togglePeriodDropdown"
            >
              <span>{{ selectedPeriod.label }}</span>
              <ChevronDownIcon class="h-4 w-4 text-gray-400" />
            </button>
            <div
              v-if="periodDropdownOpen"
              class="absolute right-0 z-20 mt-2 w-44 overflow-hidden rounded-lg border border-gray-200 bg-white shadow-lg"
            >
              <button
                v-for="option in periodOptions"
                :key="option.value"
                type="button"
                class="w-full px-4 py-2 text-left text-sm text-gray-600 transition-all duration-300 hover:bg-gray-50"
                :class="option.value === selectedPeriodValue ? 'bg-emerald-50 font-medium text-emerald-600' : ''"
                @click="selectPeriod(option.value)"
              >
                {{ option.label }}
              </button>
            </div>
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm transition-all duration-300 hover:border-emerald-300 hover:text-emerald-600 disabled:cursor-not-allowed disabled:opacity-50"
            @click="refreshData"
            :disabled="loading"
          >
            <ArrowPathIcon class="h-4 w-4" :class="loading ? 'animate-spin' : ''" />
            <span v-if="loading">{{ t('whatsappNutrition.actions.refreshing') }}</span>
            <span v-else>{{ t('whatsappNutrition.actions.refresh') }}</span>
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-lg border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-medium text-rose-600 shadow-sm transition-all duration-300 hover:border-rose-300 hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-50"
            @click="confirmDelete"
            :disabled="!selectedMessageId || deleting"
          >
            <TrashIcon class="h-4 w-4" />
            <span v-if="deleting">{{ t('whatsappNutrition.actions.deleting') }}</span>
            <span v-else>{{ t('whatsappNutrition.actions.delete') }}</span>
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition-all duration-300 hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
            @click="openCreateModal"
            :disabled="createDisabled"
          >
            <PlusIcon class="h-4 w-4" />
            <span>{{ t('whatsappNutrition.actions.create') }}</span>
          </button>
          <input
            ref="dateInputRef"
            type="date"
            class="sr-only"
            :value="selectedDateInputValue"
            @change="handleDateChange"
          />
        </div>
      </header>

      <div
        v-if="isAdmin"
        class="rounded-xl border border-gray-200 bg-white px-6 py-4 shadow-sm"
      >
        <label class="flex flex-col gap-2 sm:flex-row sm:items-center">
          <span class="text-sm font-medium text-gray-700">
            {{ t('whatsappNutrition.filters.client') }}
          </span>
          <div class="flex w-full max-w-sm items-center gap-2">
            <select v-model="selectedUserId" class="input w-full" :disabled="usersLoading">
              <option value="">{{ t('whatsappNutrition.filters.allClients') }}</option>
              <option v-for="user in userOptions" :key="user.id" :value="user.id">
                {{ user.name }}
              </option>
            </select>
          </div>
        </label>
        <p v-if="usersLoading" class="mt-2 text-xs text-gray-500">
          {{ t('whatsappNutrition.states.loadingUsers') }}
        </p>
      </div>

      <div class="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,0.8fr)]">
      <section class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition-all duration-300">
        <header class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <h2 class="text-lg font-semibold text-gray-800">Feed</h2>
            <p class="text-sm text-gray-500">Latest WhatsApp submissions and meal insights.</p>
          </div>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in feedFilterOptions"
              :key="option.value"
              type="button"
              class="rounded-full border px-3 py-1 text-sm font-medium transition-all duration-300"
              :class="isFeedFilterActive(option.value)
                ? 'border-emerald-300 bg-emerald-100 text-emerald-600'
                : 'border-gray-300 bg-white text-gray-500 hover:border-emerald-200 hover:text-emerald-600'"
              @click="toggleFeedFilter(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </header>

        <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-gray-500">
          {{ t('whatsappNutrition.feed.loading') }}
        </div>
        <div
          v-else-if="shouldPromptSelection"
          class="flex h-64 items-center justify-center text-center text-sm text-gray-500"
        >
          {{ t('whatsappNutrition.feed.selectClient') }}
        </div>
        <div
          v-else-if="!filteredFeed.length"
          class="flex h-64 items-center justify-center text-sm text-gray-500"
        >
          {{ t('whatsappNutrition.feed.empty') }}
        </div>
        <div v-else class="max-h-[90rem] space-y-6 overflow-y-auto pr-2">
          <div v-for="group in groupedFeed" :key="group.key" class="space-y-3">
            <div class="flex items-center gap-3 text-gray-600">
              <span class="text-xl">{{ mealIcon(group.label) }}</span>
              <h3 class="text-base font-semibold">{{ group.label }}</h3>
            </div>
            <ul class="space-y-4">
              <li
                v-for="item in group.items"
                :key="item.id"
                :class="[
                  'flex items-center justify-between gap-4 rounded-xl border border-gray-200 bg-white p-4 shadow-sm transition-all duration-300 hover:shadow-md',
                  selectedMessageId === item.id ? 'border-emerald-500 ring-2 ring-emerald-100' : ''
                ]"
                @click="selectMessage(item)"
                @dblclick.stop="openEditModal(item)"
              >
                <div class="flex flex-1 items-center gap-4">
                  <div class="h-20 w-20 flex-shrink-0 overflow-hidden rounded-lg bg-gray-100">
                    <img
                      v-if="item.imageUrl"
                      :src="item.imageUrl"
                      :alt="item.nutrition?.foodName ?? t('whatsappNutrition.feed.alt')"
                      class="h-full w-full object-cover"
                    />
                    <div
                      v-else
                      class="flex h-full w-full items-center justify-center text-xs font-semibold text-gray-400"
                    >
                      {{ t('whatsappNutrition.feed.textPlaceholder') }}
                    </div>
                  </div>
                  <div class="flex-1 space-y-2">
                    <div class="flex flex-col gap-1">
                      <h3 class="text-base font-semibold text-gray-800">
                        {{ item.nutrition?.foodName ?? t('whatsappNutrition.feed.unknownMeal') }}
                      </h3>
                      <p class="text-xs text-gray-500">
                        {{ submissionSourceLabel(item) }}
                      </p>
                      <p
                        v-if="isAdmin && ownerName(item.ownerUserId)"
                        class="text-xs text-gray-400"
                      >
                        {{ t('whatsappNutrition.feed.owner', { name: ownerName(item.ownerUserId) }) }}
                      </p>
                    </div>
                    <p v-if="item.textContent" class="text-sm text-gray-600">
                      {{ item.textContent }}
                    </p>
                    <div v-if="item.nutrition" class="flex flex-wrap gap-2 text-xs">
                      <span class="rounded bg-purple-100 px-2 py-0.5 font-medium text-purple-700">
                        Protein {{ formatMacro(item.nutrition.protein) }}g
                      </span>
                      <span class="rounded bg-emerald-100 px-2 py-0.5 font-medium text-emerald-700">
                        Carbs {{ formatMacro(item.nutrition.carbs) }}g
                      </span>
                      <span class="rounded bg-yellow-100 px-2 py-0.5 font-medium text-yellow-700">
                        Fat {{ formatMacro(item.nutrition.fat) }}g
                      </span>
                    </div>
                    <p v-if="item.nutrition?.summary" class="text-xs text-gray-500">
                      {{ item.nutrition.summary }}
                    </p>
                  </div>
                </div>
                <div class="flex flex-col items-end gap-2">
                  <span
                    v-if="item.nutrition?.calories"
                    class="text-sm font-medium text-gray-700"
                  >
                    {{ formatNumber(item.nutrition.calories) }} kcal
                  </span>
                  <div class="flex items-center gap-2 text-xs font-semibold">
                    <span
                      v-if="item.editedEntry"
                      class="rounded-full bg-amber-100 px-2 py-0.5 uppercase text-amber-600"
                    >
                      {{ t('whatsappNutrition.feed.edited') }}
                    </span>
                    <span
                      v-else-if="item.manualEntry"
                      class="rounded-full bg-emerald-100 px-2 py-0.5 uppercase text-emerald-600"
                    >
                      {{ t('whatsappNutrition.feed.manual') }}
                    </span>
                  </div>
                  <div class="flex gap-2">
                    <button
                      type="button"
                      class="text-gray-400 transition-all duration-300 hover:text-emerald-500"
                      @click.stop="openEditModal(item)"
                    >
                      <PencilSquareIcon class="h-5 w-5" />
                    </button>
                    <button
                      type="button"
                      class="text-gray-400 transition-all duration-300 hover:text-emerald-500"
                      @click.stop="selectMessage(item)"
                    >
                      <MagnifyingGlassIcon class="h-5 w-5" />
                    </button>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <section class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition-all duration-300">
        <div class="flex flex-col gap-4">
          <div class="flex flex-col gap-2">
            <p class="text-sm font-medium text-gray-500">Total Calories</p>
            <h3 class="text-3xl font-semibold text-gray-800">
              {{ formatNumber(dashboard.totalCalories) }} kcal
            </h3>
            <span
              class="inline-flex w-fit items-center gap-2 rounded-full bg-emerald-50 px-4 py-1 text-sm font-medium text-emerald-600"
            >
              Meals Analyzed: {{ formatNumber(dashboard.mealsAnalyzed) }}
            </span>
          </div>

          <div class="flex flex-wrap gap-2">
            <button
              v-for="option in quickFilterOptions"
              :key="option.value"
              type="button"
              class="rounded-full border px-3 py-1 text-sm font-medium transition-all duration-300"
              :class="option.value === 'export'
                ? 'border-gray-300 bg-white text-gray-600 hover:border-emerald-200 hover:text-emerald-600'
                : option.value === selectedPeriodValue
                  ? 'border-emerald-300 bg-emerald-100 text-emerald-600'
                  : 'border-gray-300 bg-white text-gray-500 hover:border-emerald-200 hover:text-emerald-600'"
              @click="handleQuickFilter(option.value)"
            >
              {{ option.label }}
            </button>
          </div>

          <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-gray-500">
            {{ t('whatsappNutrition.dashboard.loading') }}
          </div>
          <div v-else class="space-y-5">
            <section class="rounded-xl border border-gray-100 bg-gray-50 p-5">
              <div class="flex items-center justify-between">
                <div>
                  <h4 class="text-base font-semibold text-gray-800">Macronutrients Distribution</h4>
                  <p class="text-xs text-gray-500">Overview of macro intake across analyzed meals.</p>
                </div>
                <span class="text-xs font-medium text-gray-400">
                  {{ macroItems.length }} metrics
                </span>
              </div>
              <div class="mt-4 space-y-5">
                <div v-for="macro in macroItems" :key="macro.key" class="space-y-2">
                  <div class="flex items-center justify-between text-sm font-medium text-gray-700">
                    <span>{{ macro.label }}</span>
                    <span class="text-sm font-semibold text-gray-800">{{ macro.amountLabel }}</span>
                  </div>
                  <div class="h-2.5 w-full rounded-full bg-white/70">
                    <div
                      class="h-full rounded-full transition-all duration-300"
                      :class="macro.color"
                      :style="{ width: macro.percentageWidth }"
                    />
                  </div>
                  <div class="flex justify-end text-xs font-medium text-gray-500">
                    {{ macro.percentageLabel }}
                  </div>
                </div>
              </div>
              <div class="mt-4 flex flex-wrap gap-3 text-xs text-gray-500">
                <span
                  v-for="macro in macroItems"
                  :key="`legend-${macro.key}`"
                  class="inline-flex items-center gap-2"
                >
                  <span class="h-2 w-2 rounded-full" :class="macro.color"></span>
                  {{ macro.label }}
                </span>
              </div>
            </section>

            <section class="rounded-xl border border-gray-200 p-5 shadow-sm">
              <div class="flex items-center justify-between">
                <h4 class="text-base font-semibold text-gray-800">Food Categories</h4>
                <span class="text-xs text-gray-400">Calories by category</span>
              </div>
              <div v-if="categoryBreakdown.length" class="mt-4 space-y-4">
                <div
                  v-for="(item, index) in categoryBreakdown"
                  :key="item.name"
                  class="space-y-2"
                >
                  <div class="flex items-center justify-between text-sm font-medium text-gray-700">
                    <span>{{ item.name }}</span>
                    <span>{{ formatNumber(item.calories) }} kcal</span>
                  </div>
                  <div class="h-2.5 w-full rounded-full bg-gray-100">
                    <div
                      class="h-full rounded-full transition-all duration-300"
                      :class="categoryColor(index)"
                      :style="{ width: categoryWidth(item.calories) }"
                    />
                  </div>
                </div>
              </div>
              <p v-else class="mt-4 text-sm text-gray-500">
                {{ t('whatsappNutrition.dashboard.categories.empty') }}
              </p>
            </section>

            <section class="rounded-xl border border-gray-200 p-5 shadow-sm max-h-[32rem] space-y-6 overflow-y-auto pr-2">
              <div class="flex items-center justify-between">
                <h4 class="text-base font-semibold text-gray-800">Recent History</h4>
                <span class="text-xs text-gray-400">Latest analyzed meals</span>
              </div>
              <div class="mt-4 space-y-2">
                <div
                  v-for="item in history"
                  :key="item.messageId"
                  class="flex items-center justify-between border-b border-gray-100 pb-2 text-sm text-gray-700 last:border-b-0 last:pb-0"
                >
                  <span class="font-medium text-gray-800">
                    {{ item.foodName ?? t('whatsappNutrition.feed.unknownMeal') }}
                  </span>
                  <div class="flex items-center gap-3 text-right text-xs text-gray-500">
                    <span>{{ formatNumber(item.calories) }} kcal</span>
                    <span class="text-gray-400">{{ formatHistoryDate(item.analyzedAt) }}</span>
                  </div>
                </div>
                <p v-if="!history.length" class="pt-2 text-sm text-gray-500">
                  {{ t('whatsappNutrition.dashboard.history.empty') }}
                </p>
                <p class="pt-2 text-center text-xs text-gray-400">No more data to display</p>
              </div>
            </section>
          </div>
        </div>
      </section>
    </div>

    <NutritionEntryModal
      v-model="entryModalOpen"
      :mode="entryModalMode"
      :entry="modalEntry"
      :energy-units="energyUnits"
      :macro-units="macroUnits"
      :liquid-units="liquidUnits"
      :meals="meals"
      :foods="foods"
      :categories="categories"
      :owner-label="modalOwnerLabel"
      :submitting="entrySubmitting"
      @save="handleEntrySave"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  fetchNutritionDashboard,
  fetchWhatsAppMessages,
  createWhatsAppNutritionEntry,
  updateWhatsAppNutritionEntry,
  deleteWhatsAppNutritionEntry,
} from '@/services/whatsapp';
import { getMeasurementUnits, getFoodCategories, getMeals, getFoods } from '@/services/reference';
import { uploadFile } from '@/services/cloudFlare';
import { getUsers } from '@/services/users';
import NutritionEntryModal from '@/views/whatsapp/components/NutritionEntryModal.vue';
import { useNotificationStore } from '@/stores/notifications';
import { useAuthStore } from '@/stores/auth';
import {
  TrashIcon,
  PlusIcon,
  ArrowPathIcon,
  ChevronDownIcon,
  PencilSquareIcon,
  MagnifyingGlassIcon,
} from '@heroicons/vue/24/outline';

const { t, locale } = useI18n();
const notifications = useNotificationStore();
const auth = useAuthStore();

const loading = ref(true);
const deleting = ref(false);
const feed = ref([]);
const dateInputRef = ref(null);
const dashboard = ref({
  totalCalories: 0,
  totalProtein: 0,
  totalCarbs: 0,
  totalFat: 0,
  totalLiquidVolume: 0,
  totalLiquidVolumeMl: 0,
  liquidUnitSymbol: 'ml',
  mealsAnalyzed: 0,
  categoryCalories: {},
  history: [],
});

const energyUnits = ref([]);
const macroUnits = ref([]);
const liquidUnits = ref([]);
const categories = ref([]);
const meals = ref([]);
const foods = ref([]);

const users = ref([]);
const usersLoading = ref(false);

const entryModalOpen = ref(false);
const entryModalMode = ref('create');
const modalEntry = ref(null);
const entrySubmitting = ref(false);

const selectedMessageId = ref(null);

const periodOptions = [
  { label: 'Last 7 days', value: 'last7' },
  { label: 'Last 30 days', value: 'last30' },
  { label: 'Custom', value: 'custom' },
];
const selectedPeriodValue = ref(periodOptions[0].value);
const selectedPeriod = computed(
  () => periodOptions.find((option) => option.value === selectedPeriodValue.value) ?? periodOptions[0],
);
const periodDropdownOpen = ref(false);
const quickFilterOptions = [...periodOptions, { label: 'Export PDF', value: 'export' }];

const feedFilterOptions = [
  { label: 'AI-detected', value: 'ai' },
  { label: 'Manual', value: 'manual' },
  { label: 'Today', value: 'today' },
];
const activeFeedFilters = ref(new Set());

const normalizeDate = (value) => {
  if (!(value instanceof Date)) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return null;
    }
    value = date;
  }
  const normalized = new Date(value.getFullYear(), value.getMonth(), value.getDate());
  return Number.isNaN(normalized.getTime()) ? null : normalized;
};

const selectedDate = ref(normalizeDate(new Date()));

const formatDateParam = (date) => {
  if (!(date instanceof Date)) {
    return '';
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const selectedUserId = ref(isAdmin.value ? '' : auth.user?.id ?? '');

const togglePeriodDropdown = () => {
  periodDropdownOpen.value = !periodDropdownOpen.value;
};

const handleQuickFilter = (value) => {
  if (value === 'export') {
    exportDashboard();
    return;
  }
  selectPeriod(value);
};

const toggleFeedFilter = (value) => {
  const next = new Set(activeFeedFilters.value);
  if (next.has(value)) {
    next.delete(value);
  } else {
    next.add(value);
  }
  activeFeedFilters.value = next;
};

const isFeedFilterActive = (value) => activeFeedFilters.value.has(value);

const selectPeriod = (value) => {
  const option = periodOptions.find((item) => item.value === value) ?? periodOptions[0];
  selectedPeriodValue.value = option.value;
  periodDropdownOpen.value = false;
  if (option.value === 'custom') {
    openDatePicker();
    return;
  }
  selectedDate.value = null;
  void loadData();
};

const exportDashboard = () => {
  notifications.push({
    type: 'info',
    title: t('whatsappNutrition.notifications.title'),
    message: t('whatsappNutrition.notifications.message'),
  });
};
const targetUserId = computed(() => (isAdmin.value ? selectedUserId.value || null : auth.user?.id ?? null));

const defaultEnergyUnitId = computed(() => {
  const byCode = energyUnits.value.find((unit) => (unit.code ?? '').toUpperCase() === 'KCAL');
  return byCode?.id ?? energyUnits.value[0]?.id ?? '';
});

const defaultMacroUnitId = computed(() => {
  const byCode = macroUnits.value.find((unit) => (unit.code ?? '').toUpperCase() === 'G');
  return byCode?.id ?? macroUnits.value[0]?.id ?? '';
});

const defaultLiquidUnitId = computed(() => {
  const byCode = liquidUnits.value.find((unit) => (unit.code ?? '').toUpperCase() === 'ML');
  return byCode?.id ?? liquidUnits.value[0]?.id ?? '';
});

const userOptions = computed(() =>
  users.value.map((user) => ({
    id: user.id,
    name: [user.name, user.lastName].filter(Boolean).join(' ') || user.email || user.id,
  })),
);

const ownerNameById = computed(() => {
  const map = new Map(userOptions.value.map((user) => [user.id, user.name]));
  if (auth.user?.id) {
    map.set(auth.user.id, auth.user.name ?? auth.user.email ?? auth.user.id);
  }
  return map;
});

const modalOwnerLabel = computed(() => {
  const ownerId = modalEntry.value?.ownerUserId ?? targetUserId.value;
  if (!ownerId) {
    return '';
  }
  return ownerNameById.value.get(ownerId) ?? '';
});

const numberFormatter = computed(() =>
  new Intl.NumberFormat(currentLocaleTag(), { maximumFractionDigits: 0 }),
);
const macroFormatter = computed(() =>
  new Intl.NumberFormat(currentLocaleTag(), { maximumFractionDigits: 1 }),
);
const hydrationFormatter = computed(() =>
  new Intl.NumberFormat(currentLocaleTag(), { maximumFractionDigits: 1 }),
);

const macroTotals = computed(() => ({
  protein: dashboard.value.totalProtein,
  carbs: dashboard.value.totalCarbs,
  fat: dashboard.value.totalFat,
}));

const macroPercentages = computed(() => {
  const totals = macroTotals.value;
  const sum = (totals.protein ?? 0) + (totals.carbs ?? 0) + (totals.fat ?? 0);
  if (!sum) {
    return { protein: 0, carbs: 0, fat: 0 };
  }
  return {
    protein: ((totals.protein ?? 0) / sum) * 100,
    carbs: ((totals.carbs ?? 0) / sum) * 100,
    fat: ((totals.fat ?? 0) / sum) * 100,
  };
});

const clampPercentage = (value) => Math.min(Math.max(value ?? 0, 0), 100);

const macroItems = computed(() => {
  const totals = macroTotals.value;
  const percentages = macroPercentages.value;
  const items = [
    {
      key: 'protein',
      label: t('whatsappNutrition.dashboard.macros.protein'),
      amount: totals.protein ?? 0,
      percentage: clampPercentage(percentages.protein),
      color: 'bg-purple-500',
    },
    {
      key: 'carbs',
      label: t('whatsappNutrition.dashboard.macros.carbs'),
      amount: totals.carbs ?? 0,
      percentage: clampPercentage(percentages.carbs),
      color: 'bg-emerald-500',
    },
    {
      key: 'fat',
      label: t('whatsappNutrition.dashboard.macros.fat'),
      amount: totals.fat ?? 0,
      percentage: clampPercentage(percentages.fat),
      color: 'bg-yellow-500',
    },
  ];

  const hydrationMl = dashboard.value.totalLiquidVolumeMl ?? 0;
  if (hydrationMl > 0) {
    const baseUnit = (dashboard.value.liquidUnitSymbol ?? 'ml').toLowerCase();
    const isMl = baseUnit === 'ml';
    const value = isMl && hydrationMl >= 1000 ? hydrationMl / 1000 : hydrationMl;
    const unit = (isMl && hydrationMl >= 1000 ? 'l' : baseUnit).toUpperCase();
    const hydrationPercentage = clampPercentage((hydrationMl / 3000) * 100);
    items.push({
      key: 'hydration',
      label: t('whatsappNutrition.dashboard.macros.hydration'),
      amount: value,
      percentage: hydrationPercentage,
      color: 'bg-cyan-500',
      unit,
    });
  }

  return items.map((item) => ({
    key: item.key,
    label: item.label,
    color: item.color,
    amountLabel:
      item.key === 'hydration'
        ? `${hydrationFormatter.value.format(item.amount ?? 0)} ${item.unit ?? ''}`.trim()
        : `${macroFormatter.value.format(item.amount ?? 0)} g`,
    percentageLabel: `${Math.round(item.percentage)}%`,
    percentageWidth: `${item.percentage}%`,
    percentage: item.percentage,
  }));
});

const categoryBreakdown = computed(() => {
  const entries = Object.entries(dashboard.value.categoryCalories ?? {});
  return entries
    .map(([name, calories]) => ({ name, calories }))
    .sort((a, b) => (b.calories ?? 0) - (a.calories ?? 0));
});

const categoryMaxCalories = computed(() =>
  categoryBreakdown.value.reduce((max, item) => Math.max(max, item.calories ?? 0, max), 0),
);

const categoryColors = ['bg-emerald-400', 'bg-emerald-500', 'bg-amber-400', 'bg-purple-400', 'bg-rose-400', 'bg-teal-400'];

const categoryColor = (index) => categoryColors[index % categoryColors.length];

const categoryWidth = (calories) => {
  const max = categoryMaxCalories.value;
  if (!max) {
    return '0%';
  }
  const percentage = Math.min(Math.max(((calories ?? 0) / max) * 100, 0), 100);
  return `${percentage}%`;
};

const history = computed(() => dashboard.value.history ?? []);

const mealMetaById = computed(() => {
  const map = new Map();
  meals.value.forEach((meal) => map.set(meal.id, meal));
  return map;
});

const filteredFeed = computed(() => {
  const showAi = activeFeedFilters.value.has('ai');
  const showManual = activeFeedFilters.value.has('manual');
  const filterToday = activeFeedFilters.value.has('today');
  let items = [...feed.value];

  if ((showAi || showManual) && !(showAi && showManual)) {
    items = items.filter((item) => (item.manualEntry ? showManual : showAi));
  }

  if (filterToday) {
    const today = normalizeDate(new Date());
    items = items.filter((item) => {
      if (!today) {
        return true;
      }
      const received = item.receivedAt ? normalizeDate(new Date(item.receivedAt)) : null;
      return received && received.getTime() === today.getTime();
    });
  }

  return items;
});

const groupedFeed = computed(() => {
  const fallbackLabel = t('whatsappNutrition.feed.groups.unknown');
  const groups = new Map();
  filteredFeed.value.forEach((item) => {
    const mealId = item.meal?.id ?? item.nutrition?.mealId ?? null;
    const meal = mealId ? mealMetaById.value.get(mealId) : null;
    const key = mealId ?? 'unassigned';
    if (!groups.has(key)) {
      groups.set(key, {
        key,
        label: meal?.name ?? fallbackLabel,
        sortOrder: meal?.sortOrder ?? Number.MAX_SAFE_INTEGER,
        items: [],
      });
    }
   groups.get(key).items.push(item);    
  });

  return Array.from(groups.values())
    .sort((a, b) => {
      if (a.sortOrder !== b.sortOrder) {
        return a.sortOrder - b.sortOrder;
      }
      return a.label.localeCompare(b.label);
    })
    .map((group) => ({
      ...group,
      items: group.items.sort((a, b) => new Date(b.receivedAt ?? 0) - new Date(a.receivedAt ?? 0)),
    }));
});

const selectedMessage = computed(() =>
  feed.value.find((item) => item.id === selectedMessageId.value) ?? null,
);

const shouldPromptSelection = computed(() => isAdmin.value && !targetUserId.value);
const createDisabled = computed(() => isAdmin.value && !targetUserId.value);

const currentLocaleTag = () => (locale.value === 'pt' ? 'pt-BR' : 'en-US');

const selectedDateParam = computed(() => {
  if (!selectedDate.value) {
    return null;
  }
  return formatDateParam(selectedDate.value);
});

const selectedDateInputValue = computed(() => selectedDateParam.value ?? '');

const selectedDateLabel = computed(() => {
  if (!selectedDate.value) {
    return '';
  }
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), { dateStyle: 'medium' }).format(selectedDate.value);
  } catch (error) {
    return formatDateParam(selectedDate.value);
  }
});

const isTodaySelected = computed(() => {
  if (!selectedDate.value) {
    return false;
  }
  const today = normalizeDate(new Date());
  return today && selectedDate.value.getTime() === today.getTime();
});

const isNextDisabled = computed(() => {
  if (!selectedDate.value) {
    return false;
  }
  const today = normalizeDate(new Date());
  return today ? selectedDate.value.getTime() >= today.getTime() : false;
});

const formatNumber = (value) => numberFormatter.value.format(value ?? 0);
const formatMacro = (value, includeUnit = false) => {
  const formatted = macroFormatter.value.format(value ?? 0);
  return includeUnit ? `${formatted} g` : formatted;
};

const setSelectedDate = (value) => {
  const normalized = normalizeDate(value ?? new Date());
  if (!normalized) {
    return;
  }
  if (!selectedDate.value || normalized.getTime() !== selectedDate.value.getTime()) {
    selectedDate.value = normalized;
  }
};

const goToPreviousDay = () => {
  const base = selectedDate.value ? new Date(selectedDate.value) : new Date();
  base.setDate(base.getDate() - 1);
  setSelectedDate(base);
};

const goToNextDay = () => {
  const base = selectedDate.value ? new Date(selectedDate.value) : new Date();
  base.setDate(base.getDate() + 1);
  const today = normalizeDate(new Date());
  if (today && base.getTime() > today.getTime()) {
    setSelectedDate(today);
    return;
  }
  setSelectedDate(base);
};

const openDatePicker = () => {
  if (dateInputRef.value?.showPicker) {
    dateInputRef.value.showPicker();
  } else {
    dateInputRef.value?.click();
  }
};

const handleDateChange = (event) => {
  const value = event?.target?.value ?? '';
  if (!value) {
    return;
  }
  const [year, month, day] = value.split('-').map((part) => Number(part));
  if ([year, month, day].some((part) => Number.isNaN(part))) {
    return;
  }
  const candidate = new Date(year, month - 1, day);
  const today = normalizeDate(new Date());
  if (today && candidate.getTime() > today.getTime()) {
    setSelectedDate(today);
    return;
  }
  setSelectedDate(candidate);
};

const formatHistoryDate = (value) => {
  if (!value) return t('whatsappNutrition.common.unknownDate');
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), {
      dateStyle: 'medium',
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

const formatTime = (value) => {
  if (!value) return '';
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), {
      hour: 'numeric',
      minute: 'numeric',
    }).format(new Date(value));
  } catch (error) {
    return '';
  }
};

const submissionSourceLabel = (item) => {
  const segments = [];
  segments.push(item.manualEntry ? 'Manual' : 'AI-detected');
  const time = formatTime(item.receivedAt);
  if (time) {
    segments.push(time);
  }
  segments.push('WhatsApp');
  return segments.filter(Boolean).join(' • ');
};

const mealIcon = (label) => {
  const normalized = String(label ?? '').toLowerCase();
  if (normalized.includes('breakfast')) return '☕';
  if (normalized.includes('snack')) return '🍎';
  if (normalized.includes('lunch')) return '🍗';
  if (normalized.includes('dinner')) return '🍝';
  return '🍽️';
};

const formatToLocalInput = (value) => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  const pad = (input) => String(input).padStart(2, '0');
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

const parseFromInputDate = (value) => {
  if (!value) return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return null;
  }
  return date.toISOString();
};

const ownerName = (ownerId) => {
  if (!ownerId) {
    return '';
  }
  return ownerNameById.value.get(ownerId) ?? '';
};

const canManageEntry = (item) => {
  if (isAdmin.value) {
    return true;
  }
  const ownerId = item.ownerUserId ?? null;
  const currentId = auth.user?.id ?? null;
  return ownerId && currentId && ownerId === currentId;
};

const toInputValue = (value) => (value === null || value === undefined ? '' : String(value));

const createEmptyEntry = () => ({
  ownerUserId: targetUserId.value ?? '',
  receivedAt: formatToLocalInput(new Date().toISOString()),
  mealId: '',
  foodId: '',
  foodName: '',
  textContent: '',
  imageUrl: '',
  imageFile: null,
  calories: '',
  caloriesUnitId: defaultEnergyUnitId.value ?? '',
  protein: '',
  proteinUnitId: defaultMacroUnitId.value ?? '',
  carbs: '',
  carbsUnitId: defaultMacroUnitId.value ?? '',
  fat: '',
  fatUnitId: defaultMacroUnitId.value ?? '',
  liquidVolume: '',
  liquidUnitId: defaultLiquidUnitId.value ?? '',
  summary: '',
});

const mapFeedItemToEntry = (item) => {
  const nutrition = item.nutrition ?? {};
  const liquids = item.liquids ?? {};
  return {
    id: item.id,
    ownerUserId: item.ownerUserId ?? targetUserId.value ?? '',
    receivedAt: formatToLocalInput(item.receivedAt),
    mealId: item.meal?.id ?? nutrition.mealId ?? '',
    foodId: nutrition.foodId ?? item.food?.id ?? '',
    foodName: nutrition.foodName ?? item.food?.name ?? '',
    textContent: item.textContent ?? '',
    imageUrl: item.imageUrl ?? item.cloudFlareImageUrl ?? '',
    imageFile: null,
    calories: toInputValue(nutrition.calories),
    caloriesUnitId: nutrition.caloriesUnitId ?? defaultEnergyUnitId.value ?? '',
    protein: toInputValue(nutrition.protein),
    proteinUnitId: nutrition.proteinUnitId ?? defaultMacroUnitId.value ?? '',
    carbs: toInputValue(nutrition.carbs),
    carbsUnitId: nutrition.carbsUnitId ?? defaultMacroUnitId.value ?? '',
    fat: toInputValue(nutrition.fat),
    fatUnitId: nutrition.fatUnitId ?? defaultMacroUnitId.value ?? '',
    liquidVolume: toInputValue(liquids.volume),
    liquidUnitId: liquids.unitId ?? defaultLiquidUnitId.value ?? '',
    summary: nutrition.summary ?? '',
  };
};

const toNullableNumber = (value) => {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  const numeric = Number(value);
  return Number.isFinite(numeric) ? numeric : null;
};

const buildPayloadFromForm = (form) => {
  const payload = {
    ownerUserId: form.ownerUserId || targetUserId.value || null,
    receivedAt: parseFromInputDate(form.receivedAt),
    mealId: form.mealId || null,
    foodId: form.foodId || null,
    foodName: form.foodName?.trim() || null,
    calories: toNullableNumber(form.calories),
    caloriesUnitId: form.caloriesUnitId || defaultEnergyUnitId.value || null,
    protein: toNullableNumber(form.protein),
    proteinUnitId: form.proteinUnitId || defaultMacroUnitId.value || null,
    carbs: toNullableNumber(form.carbs),
    carbsUnitId: form.carbsUnitId || defaultMacroUnitId.value || null,
    fat: toNullableNumber(form.fat),
    fatUnitId: form.fatUnitId || defaultMacroUnitId.value || null,
    liquidVolume: toNullableNumber(form.liquidVolume),
    liquidUnitId: form.liquidUnitId || defaultLiquidUnitId.value || null,
    summary: form.summary?.trim() || null,
    textContent: form.textContent?.trim() || null,
    imageUrl: form.imageUrl?.trim() || null,
    imageFile: form.imageFile ?? null,
  };
  if (!payload.foodName && payload.foodId) {
    const matched = foods.value.find((food) => food.id === payload.foodId);
    payload.foodName = matched?.name ?? null;
  }
  return payload;
};

const loadMeasurementUnits = async () => {
  try {
    const { data } = await getMeasurementUnits();
    const units = data ?? [];
    energyUnits.value = units.filter((unit) => (unit.unitType ?? '').toUpperCase() === 'ENERGY');
    macroUnits.value = units.filter((unit) => (unit.unitType ?? '').toUpperCase() === 'WEIGHT');
    liquidUnits.value = units.filter((unit) => (unit.unitType ?? '').toUpperCase() === 'VOLUME');
    if (!energyUnits.value.length) {
      energyUnits.value = units;
    }
    if (!macroUnits.value.length) {
      macroUnits.value = units;
    }
    if (!liquidUnits.value.length) {
      liquidUnits.value = units;
    }
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.unitsError'),
    });
  }
};

const loadCategories = async () => {
  try {
    const { data } = await getFoodCategories();
    categories.value = data ?? [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.categoriesError'),
    });
  }
};

const loadUsers = async () => {
  if (!isAdmin.value) {
    users.value = [];
    return;
  }
  usersLoading.value = true;
  try {
    const { data } = await getUsers({ size: 200, type: 'CLIENT' });
    const pageData = data?.data ?? data?.content ?? data ?? [];
    users.value = pageData;
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.usersError'),
    });
  } finally {
    usersLoading.value = false;
  }
};

const loadMeals = async () => {
  try {
    const { data } = await getMeals();
    meals.value = (data?.content ?? data ?? []).sort((a, b) => {
      const orderA = a.sortOrder ?? Number.MAX_SAFE_INTEGER;
      const orderB = b.sortOrder ?? Number.MAX_SAFE_INTEGER;
      if (orderA !== orderB) {
        return orderA - orderB;
      }
      return (a.name ?? '').localeCompare(b.name ?? '');
    });
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.mealsError'),
    });
  }
};

const loadFoods = async () => {
  try {
    const { data } = await getFoods();
    foods.value = (data ?? []).sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.foodsError'),
    });
  }
};

const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      ...(targetUserId.value ? { userId: targetUserId.value } : {}),
      ...(selectedDateParam.value ? { date: selectedDateParam.value } : {}),
    };
    const [messagesResponse, dashboardResponse] = await Promise.all([
      fetchWhatsAppMessages(params),
      fetchNutritionDashboard(params),
    ]);
    feed.value = messagesResponse.data.filter((item) => item.messageType !== 'text') ?? [];
    dashboard.value = { ...dashboard.value, ...(dashboardResponse.data ?? {}) };
    if (!feed.value.some((item) => item.id === selectedMessageId.value)) {
      selectedMessageId.value = null;
    }
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.message'),
    });
  } finally {
    loading.value = false;
  }
};

const refreshData = () => {
  void loadData();
};

const openCreateModal = () => {
  if (createDisabled.value) {
    notifications.push({
      type: 'warning',
      title: t('whatsappNutrition.notifications.title'),
      message: t('whatsappNutrition.notifications.selectClient'),
    });
    return;
  }
  entryModalMode.value = 'create';
  modalEntry.value = createEmptyEntry();
  entryModalOpen.value = true;
};

const openEditModal = (item) => {
  if (!canManageEntry(item)) {
    notifications.push({
      type: 'warning',
      title: t('whatsappNutrition.notifications.title'),
      message: t('whatsappNutrition.notifications.forbidden'),
    });
    return;
  }
  entryModalMode.value = 'edit';
  modalEntry.value = mapFeedItemToEntry(item);
  entryModalOpen.value = true;
};

const selectMessage = (item) => {
  if (!canManageEntry(item)) {
    notifications.push({
      type: 'warning',
      title: t('whatsappNutrition.notifications.title'),
      message: t('whatsappNutrition.notifications.forbidden'),
    });
    return;
  }
  selectedMessageId.value = selectedMessageId.value === item.id ? null : item.id;
};

const handleEntrySave = async (form) => {
  const payload = buildPayloadFromForm(form);
  if (!payload.ownerUserId) {
    notifications.push({
      type: 'warning',
      title: t('whatsappNutrition.notifications.title'),
      message: t('whatsappNutrition.notifications.selectClient'),
    });
    return;
  }

  entrySubmitting.value = true;
  try {
    if (form.imageFile) {
      try {
        const { data } = await uploadFile({ file: form.imageFile, userId: payload.ownerUserId });
        payload.imageUrl = data?.url ?? payload.imageUrl;
      } catch (error) {
        notifications.push({
          type: 'error',
          title: t('whatsappNutrition.notifications.title'),
          message: error.response?.data?.message ?? t('whatsappNutrition.notifications.uploadError'),
        });
        entrySubmitting.value = false;
        return;
      }
    }

    delete payload.imageFile;

    if (entryModalMode.value === 'edit' && modalEntry.value?.id) {
      await updateWhatsAppNutritionEntry(modalEntry.value.id, payload);
      notifications.push({
        type: 'success',
        title: t('whatsappNutrition.notifications.title'),
        message: t('whatsappNutrition.notifications.updated'),
      });
    } else {
      await createWhatsAppNutritionEntry(payload);
      notifications.push({
        type: 'success',
        title: t('whatsappNutrition.notifications.title'),
        message: t('whatsappNutrition.notifications.created'),
      });
    }
    entryModalOpen.value = false;
    selectedMessageId.value = null;
    await loadData();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.saveError'),
    });
  } finally {
    entrySubmitting.value = false;
  }
};

const confirmDelete = async () => {
  if (!selectedMessage.value) {
    return;
  }
  if (!canManageEntry(selectedMessage.value)) {
    notifications.push({
      type: 'warning',
      title: t('whatsappNutrition.notifications.title'),
      message: t('whatsappNutrition.notifications.forbidden'),
    });
    return;
  }
  deleting.value = true;
  try {
    await deleteWhatsAppNutritionEntry(selectedMessageId.value);
    notifications.push({
      type: 'success',
      title: t('whatsappNutrition.notifications.title'),
      message: t('whatsappNutrition.notifications.deleted'),
    });
    selectedMessageId.value = null;
    await loadData();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.deleteError'),
    });
  } finally {
    deleting.value = false;
  }
};

onMounted(async () => {
  await Promise.all([
    loadMeasurementUnits(),
    loadCategories(),
    loadUsers(),
    loadMeals(),
    loadFoods(),
  ]);
  await loadData();
});

watch(
  () => auth.user?.id,
  (value) => {
    if (!isAdmin.value) {
      selectedUserId.value = value ?? '';
    }
  },
);

watch(targetUserId, () => {
  selectedMessageId.value = null;
  void loadData();
});

watch(selectedDateParam, () => {
  selectedMessageId.value = null;
  void loadData();
});
</script>
