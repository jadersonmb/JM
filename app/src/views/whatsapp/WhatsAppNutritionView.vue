<template>
  <div class="flex flex-col gap-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-slate-900">
          {{ t('whatsappNutrition.title') }}
        </h2>
        <p class="text-sm text-slate-500">{{ t('whatsappNutrition.subtitle') }}</p>
      </div>
      <div class="flex flex-wrap items-center gap-3">
        <button class="btn-secondary" @click="refreshData" :disabled="loading">
          <span v-if="loading">{{ t('whatsappNutrition.actions.refreshing') }}</span>
          <span v-else>{{ t('whatsappNutrition.actions.refresh') }}</span>
        </button>
        <button
          class="btn-secondary text-rose-600"
          @click="confirmDelete"
          :disabled="!selectedMessageId || deleting"
        >
          <span v-if="deleting">{{ t('whatsappNutrition.actions.deleting') }}</span>
          <span v-else>{{ t('whatsappNutrition.actions.delete') }}</span>
        </button>
        <button class="btn-primary" @click="openCreateModal" :disabled="createDisabled">
          {{ t('whatsappNutrition.actions.create') }}
        </button>
      </div>
    </div>

    <div
      v-if="isAdmin"
      class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
    >
      <label class="gap-2 sm:flex-row sm:items-center">
        <span class="text-sm font-medium text-slate-700">
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
      <p v-if="usersLoading" class="mt-2 text-xs text-slate-500">
        {{ t('whatsappNutrition.states.loadingUsers') }}
      </p>
    </div>

    <div class="grid gap-6 lg:grid-cols-2">
      <section class="rounded-2xl bg-white p-6 shadow-sm">
        <header class="mb-4 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">
              {{ t('whatsappNutrition.feed.title') }}
            </h3>
            <p class="text-xs text-slate-500">
              {{ t('whatsappNutrition.feed.description') }}
            </p>
          </div>
          <span class="rounded-full bg-primary-50 px-3 py-1 text-xs font-semibold text-primary-600">
            {{ t('whatsappNutrition.feed.count', { count: feed.length }) }}
          </span>
        </header>

        <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-slate-500">
          {{ t('whatsappNutrition.feed.loading') }}
        </div>
        <div
          v-else-if="shouldPromptSelection"
          class="flex h-64 items-center justify-center text-center text-sm text-slate-500"
        >
          {{ t('whatsappNutrition.feed.selectClient') }}
        </div>
        <div v-else-if="!feed.length" class="flex h-64 items-center justify-center text-sm text-slate-500">
          {{ t('whatsappNutrition.feed.empty') }}
        </div>
        <div v-else class="max-h-[84rem] space-y-6 overflow-y-auto pr-2">
          <div v-for="group in groupedFeed" :key="group.key" class="space-y-2">
            <h4 class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ group.label }}</h4>
            <ul class="space-y-4">
              <li
                v-for="item in group.items"
                :key="item.id"
                :class="[
                  'flex gap-4 rounded-xl border p-4 transition',
                  selectedMessageId === item.id
                    ? 'border-emerald-500 bg-emerald-50 shadow'
                    : 'border-slate-200 bg-white hover:border-emerald-300 hover:bg-emerald-50/40'
                ]"
                @click="selectMessage(item)"
                @dblclick.stop="openEditModal(item)"
              >
                <div class="h-20 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-slate-100">
                  <img
                    v-if="item.imageUrl"
                    :src="item.imageUrl"
                    :alt="item.nutrition?.foodName ?? t('whatsappNutrition.feed.alt')"
                    class="h-full w-full object-cover"
                  />
                  <div v-else class="flex h-full w-full items-center justify-center text-xs font-semibold text-slate-400">
                    {{ t('whatsappNutrition.feed.textPlaceholder') }}
                  </div>
                </div>
                <div class="flex-1 space-y-2">
                  <div class="flex items-start justify-between gap-2">
                    <div>
                      <p class="text-sm font-semibold text-slate-900">
                        {{ item.nutrition?.foodName ?? t('whatsappNutrition.feed.unknownMeal') }}
                      </p>
                      <p class="text-xs text-slate-500">
                        {{ t('whatsappNutrition.feed.source', { phone: item.fromPhone, date: formatDate(item.receivedAt) }) }}
                      </p>
                      <p v-if="isAdmin && ownerName(item.ownerUserId)" class="text-xs text-slate-400">
                        {{ t('whatsappNutrition.feed.owner', { name: ownerName(item.ownerUserId) }) }}
                      </p>
                    </div>
                    <div class="flex flex-col items-end gap-1">
                      <span v-if="item.nutrition?.calories" class="text-sm font-semibold text-primary-600">
                        {{ formatNumber(item.nutrition.calories) }} kcal
                      </span>
                      <span
                        v-if="item.manualEntry"
                        class="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-semibold uppercase text-emerald-700"
                      >
                        {{ t('whatsappNutrition.feed.manual') }}
                      </span>
                    </div>
                  </div>
                  <p v-if="item.textContent" class="text-sm text-slate-600">{{ item.textContent }}</p>
                  <div v-if="item.nutrition" class="grid gap-2 sm:grid-cols-3">
                    <div class="nutrition-chip">
                      {{ t('whatsappNutrition.feed.macros.protein', { amount: formatMacro(item.nutrition.protein) }) }}
                    </div>
                    <div class="nutrition-chip">
                      {{ t('whatsappNutrition.feed.macros.carbs', { amount: formatMacro(item.nutrition.carbs) }) }}
                    </div>
                    <div class="nutrition-chip">
                      {{ t('whatsappNutrition.feed.macros.fat', { amount: formatMacro(item.nutrition.fat) }) }}
                    </div>
                  </div>
                  <p v-if="item.nutrition?.summary" class="text-xs text-slate-500">{{ item.nutrition.summary }}</p>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <section class="flex flex-col gap-4 rounded-2xl bg-white p-6 shadow-sm">
        <header>
          <h3 class="text-lg font-semibold text-slate-900">{{ t('whatsappNutrition.dashboard.title') }}</h3>
          <p class="text-xs text-slate-500">{{ t('whatsappNutrition.dashboard.description') }}</p>
        </header>

        <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-slate-500">
          {{ t('whatsappNutrition.dashboard.loading') }}
        </div>
        <div v-else class="flex flex-col gap-4">
          <div class="grid gap-4 sm:grid-cols-2">
            <StatCard
              :label="t('whatsappNutrition.dashboard.metrics.calories')"
              :value="`${formatNumber(dashboard.totalCalories)} kcal`"
            />
            <StatCard
              :label="t('whatsappNutrition.dashboard.metrics.meals')"
              :value="formatNumber(dashboard.mealsAnalyzed)"
            />
          </div>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">
              {{ t('whatsappNutrition.dashboard.macros.title') }}
            </h4>
            <p class="text-xs text-slate-500">
              {{ t('whatsappNutrition.dashboard.macros.subtitle') }}
            </p>
            <MacroBar :items="macroItems" />
          </section>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">
              {{ t('whatsappNutrition.dashboard.categories.title') }}
            </h4>
            <p class="text-xs text-slate-500">
              {{ t('whatsappNutrition.dashboard.categories.subtitle') }}
            </p>
            <ul class="mt-3 space-y-2 text-sm text-slate-600">
              <li v-for="item in categoryBreakdown" :key="item.name" class="flex justify-between">
                <span>{{ item.name }}</span>
                <span>{{ formatNumber(item.calories) }} kcal</span>
              </li>
              <li v-if="!categoryBreakdown.length" class="text-xs text-slate-500">
                {{ t('whatsappNutrition.dashboard.categories.empty') }}
              </li>
            </ul>
          </section>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">
              {{ t('whatsappNutrition.dashboard.history.title') }}
            </h4>
            <p class="text-xs text-slate-500">
              {{ t('whatsappNutrition.dashboard.history.subtitle') }}
            </p>
            <ul class="mt-3 space-y-3 text-xs text-slate-500">
              <li v-for="item in history" :key="item.messageId" class="rounded-lg bg-slate-50 p-3">
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <p class="text-sm font-semibold text-slate-800">
                      {{ item.foodName ?? t('whatsappNutrition.feed.unknownMeal') }}
                    </p>
                    <p class="mt-1 text-xs text-slate-500">{{ formatDate(item.analyzedAt) }}</p>
                  </div>
                  <div class="text-right text-xs text-slate-500">
                    <p>{{ formatNumber(item.calories) }} kcal</p>
                    <p>{{ t('whatsappNutrition.feed.macros.protein', { amount: formatMacro(item.protein, false) }) }}</p>
                  </div>
                </div>
                <p v-if="item.summary" class="mt-2 text-xs text-slate-500">{{ item.summary }}</p>
              </li>
              <li v-if="!history.length" class="text-xs text-slate-500">
                {{ t('whatsappNutrition.dashboard.history.empty') }}
              </li>
            </ul>
          </section>
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
import StatCard from '@/views/whatsapp/components/StatCard.vue';
import MacroBar from '@/views/whatsapp/components/MacroBar.vue';
import NutritionEntryModal from '@/views/whatsapp/components/NutritionEntryModal.vue';
import { useNotificationStore } from '@/stores/notifications';
import { useAuthStore } from '@/stores/auth';

const { t, locale } = useI18n();
const notifications = useNotificationStore();
const auth = useAuthStore();

const loading = ref(true);
const deleting = ref(false);
const feed = ref([]);
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

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const selectedUserId = ref(isAdmin.value ? '' : auth.user?.id ?? '');
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
  const items = [
    {
      key: 'protein',
      label: t('whatsappNutrition.dashboard.macros.protein'),
      value: macroTotals.value.protein ?? 0,
      percentage: macroPercentages.value.protein,
      color: 'bg-emerald-500',
    },
    {
      key: 'carbs',
      label: t('whatsappNutrition.dashboard.macros.carbs'),
      value: macroTotals.value.carbs ?? 0,
      percentage: macroPercentages.value.carbs,
      color: 'bg-cyan-500',
    },
    {
      key: 'fat',
      label: t('whatsappNutrition.dashboard.macros.fat'),
      value: macroTotals.value.fat ?? 0,
      percentage: macroPercentages.value.fat,
      color: 'bg-amber-500',
    },
  ].map((macro) => ({
    key: macro.key,
    label: macro.label,
    percentage: clampPercentage(macro.percentage),
    color: macro.color,
    renderAmount: () => `${macroFormatter.value.format(macro.value ?? 0)} g (${Math.round(clampPercentage(macro.percentage))}%)`,
  }));

  const hydrationMl = dashboard.value.totalLiquidVolumeMl ?? 0;
  if (hydrationMl > 0) {
    let unit = (dashboard.value.liquidUnitSymbol ?? 'ml').toLowerCase();
    let value = hydrationMl;
    if (unit === 'ml' && hydrationMl >= 1000) {
      unit = 'l';
      value = hydrationMl / 1000;
    }
    items.push({
      key: 'hydration',
      label: t('whatsappNutrition.dashboard.macros.hydration'),
      percentage: clampPercentage(value),
      color: 'bg-sky-500',
      renderAmount: () => `${hydrationFormatter.value.format(value)} ${unit.toUpperCase()}`,
    });
  }

  return items;
});

const categoryBreakdown = computed(() => {
  const entries = Object.entries(dashboard.value.categoryCalories ?? {});
  return entries
    .map(([name, calories]) => ({ name, calories }))
    .sort((a, b) => (b.calories ?? 0) - (a.calories ?? 0));
});

const history = computed(() => dashboard.value.history ?? []);

const mealMetaById = computed(() => {
  const map = new Map();
  meals.value.forEach((meal) => map.set(meal.id, meal));
  return map;
});

const groupedFeed = computed(() => {
  const fallbackLabel = t('whatsappNutrition.feed.groups.unknown');
  const groups = new Map();
  feed.value.forEach((item) => {
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

const formatNumber = (value) => numberFormatter.value.format(value ?? 0);
const formatMacro = (value, includeUnit = false) => {
  const formatted = macroFormatter.value.format(value ?? 0);
  return includeUnit ? `${formatted} g` : formatted;
};

const formatDate = (value) => {
  if (!value) return t('whatsappNutrition.common.unknownDate');
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
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
    const params = targetUserId.value ? { userId: targetUserId.value } : {};
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
</script>

<style scoped>
.nutrition-chip {
  @apply rounded-lg bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600;
}

.loader {
  border: 2px solid rgba(15, 118, 110, 0.2);
  border-top-color: rgba(15, 118, 110, 1);
  border-radius: 9999px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
