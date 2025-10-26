<template>
    <div class="space-y-6 rounded-3xl bg-gray-50 p-6 shadow-sm md:p-8">
      <header
        class="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white px-6 py-4 shadow-sm lg:flex-row lg:items-center lg:justify-between"
      >
      <div>
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
          {{ t('menu.title') }}
        </p>
        <h1 class="text-2xl font-semibold text-slate-900">{{ headerTitle }}</h1>
        <p class="mt-1 max-w-2xl text-sm text-slate-500">{{ t('diet.description') }}</p>
      </div>
      <div class="flex flex-wrap items-center gap-3">
        <button type="button" class="btn-secondary" @click="goBack">{{ t('common.actions.cancel') }}</button>
        <button
          v-if="!isReadOnly && canGenerateAi"
          type="button"
          class="btn-secondary flex items-center gap-2"
          :disabled="aiGenerating"
          @click="generateAiSuggestion"
        >
          <span
            v-if="aiGenerating"
            class="h-4 w-4 animate-spin rounded-full border-2 border-primary-600 border-t-transparent"
          ></span>
          <SparklesIcon v-else class="h-4 w-4" />
          <span>{{ t('diet.wizard.actions.generateAi') }}</span>
        </button>
        <button
          v-if="isReadOnly && canEdit"
          type="button"
          class="btn-secondary"
          @click="startEditing"
        >
          {{ t('common.actions.edit') }}
        </button>
      </div>
    </header>

    <div v-if="loading" class="space-y-4">
      <div class="h-12 animate-pulse rounded-2xl bg-slate-200"></div>
      <div class="h-72 animate-pulse rounded-2xl bg-slate-200"></div>
    </div>
    <template v-else>
      <section class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="grid gap-4 md:grid-cols-4">
          <label class="flex flex-col text-sm font-semibold text-slate-600">
            <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.wizard.fields.owner') }}</span>
            <template v-if="isAdmin">
              <select
                class="input"
                :disabled="isReadOnly"
                :value="form.createdByUserId ?? ''"
                @change="onOwnerSelect($event.target.value)"
              >
                <option value="" disabled>{{ t('diet.wizard.fields.ownerPlaceholder') }}</option>
                <option v-for="owner in owners" :key="owner.id" :value="owner.id">
                  {{ owner.displayName || owner.email || '—' }}
                </option>
              </select>
            </template>
            <template v-else>
              <input type="text" class="input" :readonly="true" :value="ownerDisplayName" />
            </template>
          </label>
          <label v-if="isAdmin" class="flex flex-col text-sm font-semibold text-slate-600">
            <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.wizard.fields.patientName') }}</span>
            <input
              type="text"
              class="input"
              :readonly="isReadOnly"
              v-model="form.patientName"
              :placeholder="t('diet.wizard.fields.patientPlaceholder')"
            />
          </label>
          <label class="flex flex-col text-sm font-semibold text-slate-600">
            <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.wizard.fields.dayOfWeek') }}</span>
            <select
              class="input"
              :disabled="isReadOnly"
              v-model="form.dayOfWeek"
            >
              <option v-for="option in dayOfWeekOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <div class="flex items-center gap-3 md:justify-end">
            <input
              id="diet-active"
              type="checkbox"
              class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500"
              :disabled="isReadOnly"
              v-model="form.active"
            />
            <label for="diet-active" class="text-sm font-semibold text-slate-600">{{ t('diet.wizard.fields.isActive') }}</label>
          </div>
        </div>
      </section>

      <nav class="grid gap-3 sm:grid-cols-3">
        <button
          v-for="(step, index) in steps"
          :key="step.id"
          type="button"
          class="flex items-center gap-3 rounded-2xl border px-4 py-3 text-left text-sm font-semibold transition"
          :class="index === currentStepIndex ? 'border-primary-300 bg-primary-50 text-primary-600 shadow-sm' : 'border-slate-200 bg-white text-slate-500 hover:border-primary-200 hover:text-primary-600'"
          @click="goToStep(index)"
        >
          <span
            class="flex h-8 w-8 items-center justify-center rounded-full border text-sm"
            :class="index <= currentStepIndex ? 'border-primary-500 bg-primary-500 text-white' : 'border-slate-200 text-slate-500'"
          >
            {{ index + 1 }}
          </span>
          <div>
            <p>{{ step.title }}</p>
            <p class="text-xs font-normal text-slate-400">{{ step.description }}</p>
          </div>
        </button>
      </nav>

      <component
        :is="currentStep.component"
        v-bind="currentStepProps"
        class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm"
      />

      <footer class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="btn-secondary"
            :disabled="currentStepIndex === 0"
            @click="previousStep"
          >
            {{ t('common.actions.back') }}
          </button>
          <button
            type="button"
            class="btn-secondary"
            :disabled="currentStepIndex >= steps.length - 1"
            @click="nextStep"
          >
            {{ t('common.actions.next') }}
          </button>
        </div>
        <div class="flex flex-wrap gap-2">
          <button
            v-if="!isReadOnly"
            type="button"
            class="btn-primary"
            :disabled="saving"
            @click="saveDiet"
          >
            <span v-if="saving" class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
            <span>{{ saving ? t('common.actions.saving') : submitLabel }}</span>
          </button>
        </div>
      </footer>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { SparklesIcon } from '@heroicons/vue/24/outline';
import StepMealSchedule from '@/components/diet/StepMealSchedule.vue';
import StepMealItems from '@/components/diet/StepMealItems.vue';
import StepFoodSubstitutions from '@/components/diet/StepFoodSubstitutions.vue';
import StepNotesReview from '@/components/diet/StepNotesReview.vue';
import DietService from '@/services/DietService';
import { useNotificationStore } from '@/stores/notifications';
import { useAuthStore } from '@/stores/auth';

const { t } = useI18n();
const router = useRouter();
const route = useRoute();
const notifications = useNotificationStore();
const auth = useAuthStore();

const STORAGE_KEY_BASE = 'diet.wizard.draft';

const loading = ref(false);
const saving = ref(false);
const aiGenerating = ref(false);
const aiJobId = ref(null);
const aiJobStatus = ref(null);
let aiJobPollHandle = null;
let aiJobPollAttempts = 0;
const MAX_AI_POLL_ATTEMPTS = 60;
const units = ref([]);
const foods = ref([]);
const owners = ref([]);

const form = reactive({
  id: null,
  createdByUserId: null,
  patientName: '',
  notes: '',
  active: true,
  dayOfWeek: 'MONDAY',
  meals: [],
});

const defaultSchedule = [
  { mealType: 'BREAKFAST', scheduledTime: '07:30' },
  { mealType: 'LUNCH', scheduledTime: '12:30' },
  { mealType: 'SNACK', scheduledTime: '17:30' },
  { mealType: 'DINNER', scheduledTime: '20:00' },
];

const substitutionSuggestions = ref([]);
const substitutionLoading = ref(false);
let substitutionRefreshHandle = null;
let substitutionRequestToken = 0;

const currentStepIndex = ref(0);
const steps = computed(() => [
  {
    id: 'schedule',
    title: t('diet.wizard.steps.schedule.title'),
    description: t('diet.wizard.steps.schedule.description'),
    component: StepMealSchedule,
  },
  {
    id: 'items',
    title: t('diet.wizard.steps.items.title'),
    description: t('diet.wizard.steps.items.description'),
    component: StepMealItems,
  },
  {
    id: 'substitutions',
    title: t('diet.wizard.steps.substitutions.title'),
    description: t('diet.wizard.steps.substitutions.description'),
    component: StepFoodSubstitutions,
  },
  {
    id: 'notes',
    title: t('diet.wizard.steps.notes.title'),
    description: t('diet.wizard.steps.notes.description'),
    component: StepNotesReview,
  },
]);

const mealTypeOptions = computed(() => [
  { value: 'BREAKFAST', label: t('diet.meal.breakfast') },
  { value: 'LUNCH', label: t('diet.meal.lunch') },
  { value: 'SNACK', label: t('diet.meal.snack') },
  { value: 'DINNER', label: t('diet.meal.dinner') },
  { value: 'SUPPER', label: t('diet.meal.supper') },
]);

const dayOfWeekOptions = computed(() => [
  { value: 'MONDAY', label: t('common.weekdays.monday') },
  { value: 'TUESDAY', label: t('common.weekdays.tuesday') },
  { value: 'WEDNESDAY', label: t('common.weekdays.wednesday') },
  { value: 'THURSDAY', label: t('common.weekdays.thursday') },
  { value: 'FRIDAY', label: t('common.weekdays.friday') },
  { value: 'SATURDAY', label: t('common.weekdays.saturday') },
  { value: 'SUNDAY', label: t('common.weekdays.sunday') },
]);

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const canEdit = computed(() => isAdmin.value || !form.id || form.createdByUserId === auth.user?.id);
const canGenerateAi = computed(() => auth.hasPermission('ROLE_DIETS_GENERATE_AI'));
const viewMode = ref(route.query.mode === 'view' ? 'view' : 'edit');
const isReadOnly = computed(() => viewMode.value === 'view');

const storageKey = computed(() => `${STORAGE_KEY_BASE}.${auth.user?.id ?? 'guest'}`);

const headerTitle = computed(() => {
  if (isReadOnly.value) {
    return t('diet.view');
  }
  return form.id ? t('diet.edit') : t('diet.new');
});

const ownerDisplayName = computed(() => {
  const selected = owners.value.find((owner) => owner.id === form.createdByUserId);
  if (selected) {
    return selected.displayName || selected.email || '';
  }
  if (!isAdmin.value) {
    return auth.user?.name || auth.user?.email || '';
  }
  return '';
});

const onOwnerSelect = (value) => {
  form.createdByUserId = value || null;
};

const ensureOwnerOption = (id, name = '', email = '') => {
  if (!id) return;
  if (owners.value.some((owner) => owner.id === id)) return;
  owners.value = [
    ...owners.value,
    {
      id,
      displayName: name || email || '',
      email,
    },
  ];
};

const setDefaultOwner = () => {
  if (form.createdByUserId) {
    ensureOwnerOption(form.createdByUserId, ownerDisplayName.value);
    return;
  }
  const firstOwner = owners.value[0];
  if (firstOwner?.id) {
    form.createdByUserId = firstOwner.id;
    return;
  }
  if (auth.user?.id) {
    form.createdByUserId = auth.user.id;
    ensureOwnerOption(auth.user.id, auth.user.name ?? '', auth.user.email ?? '');
  }
};

const submitLabel = computed(() => t('diet.save'));

const currentStep = computed(() => steps.value[currentStepIndex.value]);

const unitMap = computed(() => {
  const map = new Map();
  (units.value ?? []).forEach((unit) => {
    if (unit?.id) {
      map.set(unit.id, unit.displayName ?? unit.description ?? '');
    }
  });
  return map;
});

const substitutionCategories = computed(() => [
  { value: 'ALL', label: t('diet.wizard.substitutions.filters.all') },
  { value: 'PROTEIN', label: t('diet.wizard.substitutions.filters.protein') },
  { value: 'CARBS', label: t('diet.wizard.substitutions.filters.carbs') },
  { value: 'FAT', label: t('diet.wizard.substitutions.filters.fat') },
  { value: 'FIBER', label: t('diet.wizard.substitutions.filters.fiber') },
]);

const mealItemReferences = computed(() => {
  const references = [];
  form.meals.forEach((meal, mealIndex) => {
    (meal.items ?? []).forEach((item, itemIndex) => {
      if (!item?.foodItemId) return;
      references.push({
        reference: `${mealIndex}-${itemIndex}`,
        mealIndex,
        itemIndex,
        foodId: item.foodItemId,
      });
    });
  });
  return references;
});

const buildSubstitutionPayload = () => {
  const items = mealItemReferences.value
    .map((entry) => {
      const rawId = entry.foodId;
      if (!rawId) return null;
      const normalizedId = typeof rawId === 'string' ? rawId.trim() : rawId;
      if (!normalizedId) return null;
      return {
        reference: entry.reference,
        foodId: normalizedId,
        limit: 3,
      };
    })
    .filter((value) => value != null);
  return { items };
};

const mapSubstitutionSuggestion = (suggestion) => {
  if (!suggestion?.reference) {
    return null;
  }
  const [mealIndexRaw, itemIndexRaw] = String(suggestion.reference).split('-');
  const mealIndex = Number.parseInt(mealIndexRaw, 10);
  const itemIndex = Number.parseInt(itemIndexRaw, 10);
  if (Number.isNaN(mealIndex) || Number.isNaN(itemIndex)) {
    return null;
  }
  const meal = form.meals[mealIndex];
  const item = meal?.items?.[itemIndex];
  if (!meal || !item) {
    return null;
  }
  const original = suggestion.original ?? {};
  const portionLabel = computeItemPortionLabel(item) || original.portionLabel || '';
  return {
    id: suggestion.reference,
    mealIndex,
    itemIndex,
    mealName: translateMealLabel(meal.mealType),
    mealTime: meal.scheduledTime,
    category: original.primary || suggestion.category || 'ALL',
    original: {
      ...original,
      portionLabel,
    },
    alternatives: (suggestion.alternatives ?? []).map((alternative) => ({
      ...alternative,
    })),
  };
};

const refreshSubstitutions = async () => {
  if (!mealItemReferences.value.length) {
    substitutionSuggestions.value = [];
    substitutionLoading.value = false;
    return;
  }
  const payload = buildSubstitutionPayload();
  if (!payload.items.length) {
    substitutionSuggestions.value = [];
    substitutionLoading.value = false;
    return;
  }
  const token = ++substitutionRequestToken;
  substitutionLoading.value = true;
  try {
    const { data } = await DietService.fetchSubstitutions(payload);
    if (token !== substitutionRequestToken) {
      return;
    }
    const suggestions = (data?.suggestions ?? [])
      .map((item) => mapSubstitutionSuggestion(item))
      .filter((value) => value && (value.alternatives ?? []).length);
    substitutionSuggestions.value = suggestions;
  } catch (error) {
    if (token === substitutionRequestToken) {
      substitutionSuggestions.value = [];
      notifications.push({
        type: 'error',
        title: t('notifications.validationTitle'),
        message: error.response?.data?.details ?? t('diet.wizard.substitutions.loadError'),
      });
    }
  } finally {
    if (token === substitutionRequestToken) {
      substitutionLoading.value = false;
    }
  }
};

const scheduleSubstitutionRefresh = () => {
  if (substitutionRefreshHandle) {
    clearTimeout(substitutionRefreshHandle);
  }
  substitutionRefreshHandle = setTimeout(() => {
    refreshSubstitutions();
  }, 300);
};

const resetAiPolling = () => {
  if (aiJobPollHandle) {
    clearTimeout(aiJobPollHandle);
    aiJobPollHandle = null;
  }
  aiJobPollAttempts = 0;
};

const scheduleAiPolling = (delay = 3000) => {
  if (!aiJobId.value) {
    return;
  }
  if (aiJobPollHandle) {
    clearTimeout(aiJobPollHandle);
  }
  aiJobPollHandle = setTimeout(() => {
    pollAiJobStatus();
  }, delay);
};

const handleAiJobFailure = (message) => {
  resetAiPolling();
  aiGenerating.value = false;
  aiJobStatus.value = 'FAILED';
  const errorMessage = message || t('diet.wizard.toast.aiFailed');
  notifications.push({
    type: 'error',
    title: t('notifications.validationTitle'),
    message: errorMessage,
  });
  aiJobId.value = null;
};

const handleAiJobSuccess = (diet, job) => {
  resetAiPolling();
  aiGenerating.value = false;
  aiJobStatus.value = job?.status ?? 'COMPLETED';
  aiJobId.value = null;
  if (diet) {
    applyDietData(diet);
    viewMode.value = 'edit';
    clearDraft();
    scheduleSubstitutionRefresh();
    updateRouteAfterDietChange(diet.id ?? job?.dietPlanId ?? null);
  }
  notifications.push({
    type: 'success',
    title: t('diet.wizard.toast.title'),
    message: t('diet.wizard.toast.aiGenerated'),
  });
};

const pollAiJobStatus = async () => {
  if (!aiJobId.value) {
    return;
  }
  if (aiJobPollAttempts >= MAX_AI_POLL_ATTEMPTS) {
    handleAiJobFailure(t('diet.wizard.toast.aiTimeout'));
    return;
  }

  aiJobPollAttempts += 1;

  try {
    const { data } = await DietService.getAiSuggestionStatus(aiJobId.value);
    aiJobStatus.value = data?.status ?? null;

    if ((data?.status || '').toUpperCase() === 'COMPLETED') {
      handleAiJobSuccess(data?.diet ?? null, data);
      return;
    }

    if ((data?.status || '').toUpperCase() === 'FAILED') {
      handleAiJobFailure(data?.errorMessage ?? data?.error ?? data?.details);
      return;
    }

    scheduleAiPolling();
  } catch (error) {
    if (aiJobPollAttempts >= MAX_AI_POLL_ATTEMPTS) {
      handleAiJobFailure(error.response?.data?.details ?? error.message);
      return;
    }
    scheduleAiPolling();
  }
};

const syncSuggestionContext = () => {
  substitutionSuggestions.value = substitutionSuggestions.value.map((suggestion) => {
    const meal = form.meals[suggestion.mealIndex];
    const item = meal?.items?.[suggestion.itemIndex];
    if (!meal || !item) {
      return suggestion;
    }
    return {
      ...suggestion,
      mealName: translateMealLabel(meal.mealType),
      mealTime: meal.scheduledTime,
      original: {
        ...suggestion.original,
        portionLabel: computeItemPortionLabel(item) || suggestion.original?.portionLabel || '',
      },
    };
  });
};

const translateMealLabel = (type) => t(`diet.meal.${String(type || '').toLowerCase()}`);

const computeItemPortionLabel = (item = {}) => {
  const quantity = item.quantity != null ? Number(item.quantity) : null;
  const unitName = item.unitDisplayName || unitMap.value.get(item.unitId) || '';
  if (quantity == null || Number.isNaN(quantity) || quantity <= 0) {
    return unitName;
  }
  const hasDecimals = Math.abs(quantity % 1) > 0;
  const formattedQuantity = new Intl.NumberFormat(undefined, {
    minimumFractionDigits: hasDecimals ? 1 : 0,
    maximumFractionDigits: hasDecimals ? 1 : 0,
  }).format(quantity);
  return unitName ? `${formattedQuantity} ${unitName}` : formattedQuantity;
};

const currentStepProps = computed(() => {
  if (currentStep.value?.id === 'schedule') {
    return {
      modelValue: form.meals,
      'onUpdate:modelValue': (value) => {
        form.meals = value;
      },
      mealTypeOptions: mealTypeOptions.value,
      disabled: isReadOnly.value,
      createMeal: createMealTemplate,
    };
  }
  if (currentStep.value?.id === 'items') {
    return {
      meals: form.meals,
      foods: foods.value,
      units: units.value,
      disabled: isReadOnly.value,
      'onUpdate:meals': (value) => {
        form.meals = value;
      },
    };
  }
  if (currentStep.value?.id === 'substitutions') {
    return {
      suggestions: substitutionSuggestions.value,
      categories: substitutionCategories.value,
      loading: substitutionLoading.value,
      disabled: isReadOnly.value,
      onReplace: replaceMealItem,
    };
  }
  return {
    modelValue: form.notes,
    meals: form.meals,
    foods: foods.value,
    units: units.value,
    patientName: isAdmin.value ? form.patientName : auth.user?.name || '',
    ownerName: ownerDisplayName.value,
    dayOfWeek: form.dayOfWeek,
    disabled: isReadOnly.value,
    'onUpdate:modelValue': (value) => {
      form.notes = value;
    },
  };
});

const goBack = () => {
  router.push({ name: 'diet' });
};

const nextStep = () => {
  currentStepIndex.value = Math.min(currentStepIndex.value + 1, steps.value.length - 1);
};

const previousStep = () => {
  currentStepIndex.value = Math.max(currentStepIndex.value - 1, 0);
};

const goToStep = (index) => {
  currentStepIndex.value = index;
};

const replaceMealItem = ({ mealIndex, itemIndex, foodId, foodName }) => {
  if (isReadOnly.value || mealIndex == null || itemIndex == null || !foodId) {
    return;
  }
  const replacement = foods.value.find((food) => food.id === foodId);
  const updatedMeals = form.meals.map((meal, currentMealIndex) => {
    if (currentMealIndex !== mealIndex) return meal;
    const nextItems = (meal.items ?? []).map((item, currentItemIndex) => {
      if (currentItemIndex !== itemIndex) return item;
      return {
        ...item,
        foodItemId: replacement?.id ?? foodId,
        foodItemName: replacement?.name ?? foodName ?? item.foodItemName ?? '',
      };
    });
    return { ...meal, items: nextItems };
  });
  form.meals = updatedMeals;
};

const startEditing = () => {
  if (!canEdit.value) return;
  viewMode.value = 'edit';
  router.replace({ query: { ...route.query, mode: 'edit' } });
};

const createMealTemplate = () => {
  const index = form.meals.length % defaultSchedule.length;
  return { ...defaultSchedule[index], items: [] };
};

const normalizeTime = (value) => {
  if (!value) return null;
  if (value.length === 5) {
    return `${value}:00`;
  }
  return value;
};

const normalizeMealsForPayload = () =>
  form.meals.map((meal) => ({
    id: meal.id ?? null,
    dietPlanId: form.id ?? null,
    mealType: meal.mealType,
    scheduledTime: normalizeTime(meal.scheduledTime ?? ''),
    items: (meal.items ?? []).map((item) => ({
      id: item.id ?? null,
      dietMealId: meal.id ?? null,
      foodItemId: item.foodItemId || null,
      unitId: item.unitId || null,
      quantity: item.quantity != null ? Number(item.quantity) : null,
      notes: item.notes ?? null,
    })),
  }));

const validateForm = () => {
  if (!form.createdByUserId) {
    notifications.push({
      type: 'warning',
      title: t('notifications.validationTitle'),
      message: t('diet.validation.ownerRequired'),
    });
    return false;
  }
  if (!form.dayOfWeek) {
    notifications.push({
      type: 'warning',
      title: t('notifications.validationTitle'),
      message: t('diet.validation.dayOfWeekRequired'),
    });
    return false;
  }
  if (!form.meals.length) {
    notifications.push({
      type: 'warning',
      title: t('notifications.validationTitle'),
      message: t('diet.validation.atLeastOneMeal'),
    });
    return false;
  }
  const invalidItem = form.meals.some((meal) =>
    (meal.items ?? []).some((item) => !item.foodItemId || !item.unitId || !item.quantity || Number(item.quantity) <= 0),
  );
  if (invalidItem) {
    notifications.push({
      type: 'warning',
      title: t('notifications.validationTitle'),
      message: t('diet.validation.itemRequired'),
    });
    return false;
  }
  return true;
};

const saveDiet = async () => {
  if (isReadOnly.value || saving.value) return;
  if (!validateForm()) return;
  saving.value = true;
  try {
    const payload = {
      id: form.id,
      createdByUserId: form.createdByUserId ?? auth.user?.id ?? null,
      patientName: form.patientName,
      notes: form.notes,
      active: form.active,
      dayOfWeek: form.dayOfWeek,
      meals: normalizeMealsForPayload(),
    };

    const request = form.id ? DietService.update(payload) : DietService.create(payload);
    const { data } = await request;
    notifications.push({
      type: 'success',
      title: t('diet.wizard.toast.title'),
      message: t('diet.wizard.toast.saved'),
    });
    clearDraft();
    if (data?.id) {
      form.id = data.id;
    }
    router.push({ name: 'diet' });
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? error.message ?? 'Unable to save diet plan.',
    });
  } finally {
    saving.value = false;
  }
};

const applyDietData = (data) => {
  if (!data) return;
  form.id = data.id ?? null;
  form.createdByUserId =
    data.createdByUserId ?? data.createdBy ?? form.createdByUserId ?? auth.user?.id ?? null;
  form.patientName = data.patientName ?? '';
  form.notes = data.notes ?? '';
  form.active = data.active !== false;
  form.dayOfWeek = data.dayOfWeek ?? form.dayOfWeek ?? 'MONDAY';
  form.meals = (data.meals ?? []).map((meal) => ({
    ...meal,
    scheduledTime: meal.scheduledTime ? String(meal.scheduledTime).slice(0, 5) : '08:00',
    items: (meal.items ?? []).map((item) => ({
      ...item,
      quantity: item.quantity != null ? Number(item.quantity) : null,
    })),
  }));
  ensureOwnerOption(
    form.createdByUserId,
    data.createdByName ?? '',
    data.createdByEmail ?? '',
  );
  currentStepIndex.value = 0;
};

const updateRouteAfterDietChange = (dietId) => {
  const nextQuery = { ...route.query };
  delete nextQuery.mode;

  if (dietId) {
    router.replace({ name: 'diet-edit', params: { id: dietId }, query: nextQuery });
  } else if ('mode' in route.query) {
    router.replace({ query: nextQuery });
  }
};

const loadDiet = async (id) => {
  loading.value = true;
  try {
    const { data } = await DietService.get(id);
    applyDietData(data);
    clearDraft();
    scheduleSubstitutionRefresh();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? error.message ?? 'Unable to load diet plan.',
    });
  } finally {
    loading.value = false;
  }
};

const resolveRouteGoal = () => {
  const raw = route.query.goal;
  if (Array.isArray(raw)) {
    return raw.find((entry) => typeof entry === 'string') ?? '';
  }
  return typeof raw === 'string' ? raw : '';
};

const generateAiSuggestion = async () => {
  if (isReadOnly.value || aiGenerating.value) return;
  if (!canGenerateAi.value) {
    notifications.push({
      type: 'warning',
      title: t('notifications.validationTitle'),
      message: t('routes.unauthorized'),
    });
    return;
  }
  if (!form.createdByUserId) {
    setDefaultOwner();
  }
  if (!form.createdByUserId) {
    notifications.push({
      type: 'warning',
      title: t('notifications.validationTitle'),
      message: t('diet.validation.ownerRequired'),
    });
    return;
  }

  aiGenerating.value = true;
  try {
    const payload = {
      dietId: form.id ?? null,
      ownerId: form.createdByUserId ?? null,
      patientName: form.patientName ?? '',
      goal: resolveRouteGoal(),
      notes: form.notes ?? '',
      dayOfWeek: form.dayOfWeek ?? 'MONDAY',
      active: form.active !== false,
    };

    const { data } = await DietService.generateAiSuggestion(payload);
    const jobId = data?.id;
    if (!jobId) {
      throw new Error(t('diet.wizard.toast.aiFailed'));
    }

    resetAiPolling();
    aiJobId.value = jobId;
    aiJobStatus.value = data?.status ?? 'PROCESSING';
    aiJobPollAttempts = 0;

    notifications.push({
      type: 'info',
      title: t('diet.wizard.toast.title'),
      message: t('diet.wizard.toast.aiInProgress'),
    });

    scheduleAiPolling(1000);
  } catch (error) {
    handleAiJobFailure(error.response?.data?.details ?? error.message);
  }
};

const loadReferences = async () => {
  try {
    const [unitsResponse, foodsResponse, ownersResponse] = await Promise.all([
      DietService.listUnits({ active: true }),
      DietService.listFoods({ active: true }),
      DietService.listOwners(),
    ]);
    units.value = unitsResponse.data ?? [];
    foods.value = foodsResponse.data ?? [];
    owners.value = (ownersResponse.data ?? []).map((owner) => ({
      ...owner,
      displayName: owner.displayName || owner.email || '',
    }));
    setDefaultOwner();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to load reference data.',
    });
  }
};

const restoreDraft = () => {
  if (form.id) return;
  try {
    const raw = window.localStorage.getItem(storageKey.value);
    if (!raw) {
      if (!form.meals.length) {
        form.meals = defaultSchedule.map((item) => ({ ...item, id: crypto.randomUUID ? crypto.randomUUID() : Date.now(), items: [] }));
        scheduleSubstitutionRefresh();
      }
      return;
    }
    const parsed = JSON.parse(raw);
    Object.assign(form, {
      ...form,
      ...parsed,
      meals: (parsed.meals ?? []).map((meal) => ({
        ...meal,
        scheduledTime: meal.scheduledTime ? String(meal.scheduledTime).slice(0, 5) : '08:00',
      })),
    });
    form.dayOfWeek = parsed.dayOfWeek ?? form.dayOfWeek ?? 'MONDAY';
    ensureOwnerOption(form.createdByUserId);
    setDefaultOwner();
    scheduleSubstitutionRefresh();
    notifications.push({
      type: 'info',
      title: t('diet.wizard.toast.draftTitle'),
      message: t('diet.wizard.toast.draftRestored'),
    });
  } catch (error) {
    console.error('Failed to restore draft', error);
    form.meals = defaultSchedule.map((item) => ({ ...item, id: crypto.randomUUID ? crypto.randomUUID() : Date.now(), items: [] }));
  }
};

const persistDraft = () => {
  if (form.id || isReadOnly.value) return;
  try {
    const snapshot = JSON.stringify(form);
    window.localStorage.setItem(storageKey.value, snapshot);
  } catch (error) {
    console.warn('Failed to persist diet draft', error);
  }
};

const clearDraft = () => {
  window.localStorage.removeItem(storageKey.value);
};

watch(
  () => route.query.mode,
  (mode) => {
    viewMode.value = mode === 'view' ? 'view' : 'edit';
  },
);

watch(
  mealItemReferences,
  () => {
    scheduleSubstitutionRefresh();
  },
  { immediate: true },
);

watch(
  () => form.meals,
  () => {
    syncSuggestionContext();
  },
  { deep: true },
);

watch(form, () => {
  persistDraft();
}, { deep: true });

onBeforeUnmount(() => {
  resetAiPolling();
  if (substitutionRefreshHandle) {
    clearTimeout(substitutionRefreshHandle);
    substitutionRefreshHandle = null;
  }
});

onMounted(async () => {
  await loadReferences();
  const id = route.params.id;
  if (id) {
    await loadDiet(id);
  } else {
    restoreDraft();
  }
});
</script>
