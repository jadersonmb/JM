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
        <div class="grid gap-4 md:grid-cols-3">
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
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import StepMealSchedule from '@/components/diet/StepMealSchedule.vue';
import StepMealItems from '@/components/diet/StepMealItems.vue';
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
const units = ref([]);
const foods = ref([]);
const owners = ref([]);

const form = reactive({
  id: null,
  createdByUserId: null,
  patientName: '',
  notes: '',
  active: true,
  meals: [],
});

const defaultSchedule = [
  { mealType: 'BREAKFAST', scheduledTime: '07:30' },
  { mealType: 'LUNCH', scheduledTime: '12:30' },
  { mealType: 'SNACK', scheduledTime: '17:30' },
  { mealType: 'DINNER', scheduledTime: '20:00' },
];

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

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const canEdit = computed(() => isAdmin.value || !form.id || form.createdByUserId === auth.user?.id);
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
  return {
    modelValue: form.notes,
    meals: form.meals,
    foods: foods.value,
    units: units.value,
    patientName: isAdmin.value ? form.patientName : auth.user?.name || '',
    ownerName: ownerDisplayName.value,
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

const loadDiet = async (id) => {
  loading.value = true;
  try {
    const { data } = await DietService.get(id);
    form.id = data.id;
    form.createdByUserId = data.createdByUserId ?? data.createdBy ?? null;
    form.patientName = data.patientName ?? '';
    form.notes = data.notes ?? '';
    form.active = data.active !== false;
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
      data.createdByEmail ?? ''
    );
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
    ensureOwnerOption(form.createdByUserId);
    setDefaultOwner();
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

watch(form, () => {
  persistDraft();
}, { deep: true });

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
