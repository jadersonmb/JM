<template>
  <div class="flex flex-col gap-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900 text-blue-600 font-bold">{{ t('anamnesis.title') }}</h1>
        <p class="mt-1 max-w-3xl text-sm text-slate-500">
          {{ t('anamnesis.subtitle') }}
        </p>
      </div>
      <div v-if="viewMode === 'form'" class="text-sm font-medium text-slate-500">
        {{ t('common.stepIndicator', { current: currentStepIndex + 1, total: steps.length }) }}
      </div>
    </div>
    <div class="mt-8 grid gap-6" :class="isAdmin ? 'lg:grid-cols-[30%_70%]' : ''">
      <section v-if="isAdmin" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <header class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnesis.admin.listTitle') }}</h2>
            <p class="text-sm text-slate-500">{{ t('anamnesis.admin.listSubtitle') }}</p>
          </div>
          <div class="flex grade gap-2">
            <button type="button" class="btn-secondary" :disabled="anamnesisLoading" @click="startNewAnamnesis">
              <span v-if="anamnesisLoading" class="loader h-4 w-4"></span>
              <span v-else>{{ t('anamnesis.admin.new') }}</span>
            </button>
            <!--<button v-if="isAdmin" type="button" class="btn-ghost text-slate-500" @click="toggleCompactSummary">
              <Bars3Icon class="h-4 w-4" /> 
            </button> -->
          </div>
        </header>

        <div v-if="anamnesisLoading" class="mt-4 space-y-2">
          <div class="h-10 w-full animate-pulse rounded-xl bg-slate-200"></div>
          <div class="h-10 w-full animate-pulse rounded-xl bg-slate-200"></div>
          <div class="h-10 w-full animate-pulse rounded-xl bg-slate-200"></div>
        </div>
        <div v-else-if="anamnesisList.length" class="mt-4 overflow-hidden rounded-2xl border border-slate-200">
          <table class="min-w-full divide-y divide-slate-200 text-sm">
            <thead class="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-4 py-3 text-left">{{ t('anamnesis.admin.columns.patient') }}</th>
                <th class="px-4 py-3 text-left">{{ t('anamnesis.admin.columns.phone') }}</th>
                <th class="px-4 py-3 text-left">{{ t('anamnesis.admin.columns.goal') }}</th>
                <th class="px-2 py-3 text-left">{{ t('anamnesis.admin.columns.actions') }}</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 bg-white">
              <tr v-for="item in anamnesisList" :key="item.id"
                :class="selectedAnamnesisId === item.id ? 'bg-primary-50/50' : ''">
                <td class="px-4 py-3 font-semibold text-slate-700">
                  {{ item.patientName || t('common.placeholders.empty') }}
                </td>
                <td class="px-4 py-3 text-slate-500">
                  {{ item.phoneNumber || t('common.placeholders.empty') }}
                </td>
                <td class="px-4 py-3 text-slate-500">
                  {{ item.consultationGoal || t('common.placeholders.empty') }}
                </td>
                <td class="px-4 py-3 text-left">
                  <button type="button" class="btn-ghost text-primary-600" @click="selectAnamnesis(item)">
                    <PencilSquareIcon class="h-4 w-4" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="mt-4 text-sm text-slate-500">{{ t('anamnesis.admin.empty') }}</p>
      </section>

      <div :class="['flex flex-col gap-6', compactSummary ? 'lg:flex-row lg:items-start' : '']">
        <section v-if="shouldShowSummary" :class="[
          'rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition-all',
          compactSummary ? 'lg:max-w-md lg:flex-shrink-0' : '',
        ]">
          <header class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h2 class="text-lg font-semibold text-slate-900 font-bold">{{ t('anamnesis.summary.title') }}</h2>
            </div>
            <div class="flex flex-wrap gap-2">
              <button type="button" class="btn-secondary" @click="editSelectedAnamnesis">
                {{ t('common.actions.edit') }}
              </button>
              <!--<button v-if="isAdmin" type="button" class="btn-ghost text-primary-600" @click="startNewAnamnesis">
                {{ t('anamnesis.summary.newRecord') }}
              </button> -->
            </div>
          </header>

          <dl class="mt-4 grid gap-4 sm:grid-cols-2">
            <div>
              <dt class="text-xs uppercase tracking-wide text-slate-400 text-blue-600 font-bold">
                {{ t('anamnesis.summary.fields.patient') }}
              </dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('patientName') }}</dd>
            </div>
            <div>
              <dt class="text-xs uppercase tracking-wide text-slate-400 text-blue-600 font-bold">
                {{ t('anamnesis.summary.fields.phone') }}
              </dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('phoneNumber') }}</dd>
            </div>
            <div>
              <dt class="text-xs uppercase tracking-wide text-slate-400 text-blue-600 font-bold">
                {{ t('anamnesis.summary.fields.goal') }}
              </dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('consultationGoal') }}</dd>
            </div>
            <div>
              <dt class="text-xs uppercase tracking-wide text-slate-400 text-blue-600 font-bold">
                {{ t('anamnesis.summary.fields.diagnosis') }}
              </dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('diagnosis') }}</dd>
            </div>
          </dl>

          <div class="mt-6 space-y-4">
            <div>
              <h3 class="text-sm font-semibold text-slate-800">
                {{ t('anamnesis.summary.sections.pathologies') }}
              </h3>
              <p v-if="!selectedPathologyNames.length" class="text-sm text-slate-500">
                {{ t('common.placeholders.empty') }}
              </p>
              <ul v-else class="mt-2 flex flex-wrap gap-2">
                <li v-for="name in selectedPathologyNames" :key="name"
                  class="rounded-full bg-primary-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-primary-600">
                  {{ name }}
                </li>
              </ul>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-slate-800">
                {{ t('anamnesis.summary.sections.preferences') }}
              </h3>
              <div v-if="!selectedFoodPreferences.length" class="text-sm text-slate-500">
                {{ t('common.placeholders.empty') }}
              </div>
              <div v-else class="mt-2 space-y-2">
                <div v-for="(group, index) in selectedFoodPreferences" :key="index">
                  <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
                    {{ t(`anamnesis.preferences.types.${group.type}`) }}
                  </p>
                  <ul class="mt-1 flex flex-wrap gap-2">
                    <li v-for="item in group.items" :key="item.key"
                      class="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-600">
                      {{ item.label }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-slate-800">
                {{ t('anamnesis.summary.sections.diet') }}
              </h3>
              <p class="text-sm text-slate-700">
                {{ summaryField('dietSummary') }}
              </p>
            </div>
          </div>
        </section>
        <div class="flex-1">
          <div v-if="viewMode === 'form'" class="space-y-6">
            <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div class="flex flex-wrap gap-2">
                <button v-for="(step, index) in steps" :key="step.id" type="button"
                  class="flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-semibold transition"
                  :class="index <= currentStepIndex ? 'border-primary-500 bg-primary-50 text-primary-600' : 'border-slate-200 text-slate-500'"
                  :disabled="index > currentStepIndex + 1" @click="goToStep(index)">
                  <span class="flex h-7 w-7 items-center justify-center rounded-full border text-xs"
                    :class="index <= currentStepIndex ? 'border-primary-500 bg-primary-500 text-white' : 'border-slate-200 text-slate-500'">
                    {{ index + 1 }}
                  </span>
                  <span class="whitespace-nowrap max-w-[7rem] truncate">{{ step.title }}</span>
                </button>
              </div>
            </div>
            <div class="flex items-center">
              <div class="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
                <div class="h-full rounded-full bg-primary-500 transition-all" :style="{ width: `${progress}%` }" />
              </div>
              <span class="text-xs font-medium text-slate-500">
                {{ Math.round(progress) }}%
              </span>
            </div>
            <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
              <component :is="currentStep.component" v-bind="currentStepProps" />
            </div>

            <div class="flex flex-col justify-between gap-4 sm:flex-row">
              <div class="flex flex-wrap gap-3">
                <button type="button"
                  class="inline-flex items-center rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-primary-200 hover:text-primary-600 disabled:opacity-40"
                  :disabled="currentStepIndex === 0" @click="previousStep">
                  {{ t('common.actions.back') }}
                </button>
                <button v-if="canCancelEditing" type="button"
                  class="inline-flex items-center rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-primary-200 hover:text-primary-600"
                  @click="showSummary">
                  {{ t('common.actions.cancel') }}
                </button>
              </div>
              <div class="flex flex-wrap gap-3">
                <button type="button"
                  class="inline-flex items-center rounded-xl border border-primary-200 px-4 py-2 text-sm font-semibold text-primary-600 transition hover:border-primary-300 hover:bg-primary-50"
                  @click="nextStep" :disabled="currentStepIndex === steps.length - 1">
                  {{ t('common.actions.next') }}
                </button>
                <button type="button"
                  class="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-6 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500 disabled:opacity-50"
                  :disabled="saving" @click="saveAnamnesis">
                  <span v-if="saving"
                    class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                  <span>{{ saving ? t('common.actions.saving') : t('common.actions.save') }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import StepPersonalInfo from '@/components/anamnesis/StepPersonalInfo.vue';
import StepClinicalAssessment from '@/components/anamnesis/StepClinicalAssessment.vue';
import StepPathologies from '@/components/anamnesis/StepPathologies.vue';
import StepBioimpedance from '@/components/anamnesis/StepBioimpedance.vue';
import StepBiochemistry from '@/components/anamnesis/StepBiochemistry.vue';
import StepHabits from '@/components/anamnesis/StepHabits.vue';
import StepFoodPreferences from '@/components/anamnesis/StepFoodPreferences.vue';
import StepFoodRecall from '@/components/anamnesis/StepFoodRecall.vue';
import StepDiagnosis from '@/components/anamnesis/StepDiagnosis.vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import AnamnesisService from '@/services/AnamnesisService';
import { useI18n } from 'vue-i18n';
import {
  getCountries,
  getCities,
  getEducationLevels,
  getProfessions,
  getPathologies,
  getBiochemicalExams,
  getFoods,
  getMeasurementUnits,
} from '@/services/reference';
import { getUserSettings } from '@/services/settings';
import { Bars3Icon, PencilSquareIcon } from '@heroicons/vue/24/outline';

const STORAGE_KEY = 'jm_anamnesis_wizard_draft';

const auth = useAuthStore();
const notifications = useNotificationStore();
const { t, locale } = useI18n();

const saving = ref(false);
const currentStepIndex = ref(0);
const viewMode = ref('form');

const anamnesisList = ref([]);
const anamnesisLoading = ref(false);
const selectedAnamnesis = ref(null);
const userSettings = ref({ language: '' });
const compactSummary = ref(false);

const referenceLoading = reactive({
  general: false,
  cities: false,
});

const referenceData = reactive({
  countries: [],
  cities: [],
  educationLevels: [],
  professions: [],
  pathologies: [],
  biochemicalExams: [],
  foods: [],
  measurementUnits: [],
});

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const isClient = computed(() => !isAdmin.value);

const referenceParams = computed(() => userSettings.value.language || locale.value);

const loadSettings = async () => {
  if (!auth.user?.id) {
    return;
  }
  try {
    const { data } = await getUserSettings(auth.user.id);
    userSettings.value = { language: data?.language ?? userSettings.value.language ?? locale.value };
  } catch (error) {
    console.error('Failed to load user settings', error);
    userSettings.value = { language: locale.value };
  }
};
const createInitialForm = () => ({
  id: null,
  userId: isAdmin.value ? null : auth.user?.id ?? null,
  countryId: null,
  cityId: null,
  educationLevelId: null,
  professionId: null,
  patientName: '',
  address: '',
  birthDate: '',
  age: null,
  phoneNumber: '',
  educationLevelName: '',
  professionName: '',
  consultationGoal: '',
  mucosa: '',
  limbs: '',
  surgicalHistory: '',
  clinicalNotes: '',
  pathologyIds: [],
  weightKg: null,
  heightCm: null,
  bodyMassIndex: null,
  bodyFatPercentage: null,
  muscleMassPercentage: null,
  basalMetabolicRate: null,
  abdominalCircumference: null,
  waistCircumference: null,
  hipCircumference: null,
  armCircumference: null,
  kneeCircumference: null,
  thoraxCircumference: null,
  biochemicalResults: [],
  mealPreparation: '',
  mealLocation: '',
  workSchedule: '',
  studySchedule: '',
  appetite: '',
  waterIntake: '',
  physicalActivity: '',
  activityFrequency: '',
  activityDuration: '',
  smokes: false,
  drinksAlcohol: false,
  supplements: '',
  sleepQuality: '',
  chewingQuality: '',
  habitNotes: '',
  foodPreferences: [],
  foodRecalls: [],
  diagnosis: '',
  dietSummary: '',
});

const form = reactive(createInitialForm());

const generateKey = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return Math.random().toString(36).slice(2);
};

const steps = computed(() => [
  {
    id: 'personal',
    title: t('anamnesis.steps.personal.title'),
    description: t('anamnesis.steps.personal.description'),
    component: StepPersonalInfo,
    getProps: () => ({
      form,
      isAdmin: isAdmin.value,
      countries: referenceData.countries,
      cities: referenceData.cities,
      educationLevels: referenceData.educationLevels,
      professions: referenceData.professions,
      loadingReference: referenceLoading,
    }),
  },
  {
    id: 'clinical',
    title: t('anamnesis.steps.clinical.title'),
    description: t('anamnesis.steps.clinical.description'),
    component: StepClinicalAssessment,
    getProps: () => ({ form }),
  },
  {
    id: 'pathologies',
    title: t('anamnesis.steps.pathologies.title'),
    description: t('anamnesis.steps.pathologies.description'),
    component: StepPathologies,
    getProps: () => ({ form, pathologies: referenceData.pathologies }),
  },
  {
    id: 'bodyComposition',
    title: t('anamnesis.steps.bodyComposition.title'),
    description: t('anamnesis.steps.bodyComposition.description'),
    component: StepBioimpedance,
    getProps: () => ({ form }),
  },
  {
    id: 'biochemistry',
    title: t('anamnesis.steps.biochemistry.title'),
    description: t('anamnesis.steps.biochemistry.description'),
    component: StepBiochemistry,
    getProps: () => ({ form, biochemicalExams: referenceData.biochemicalExams }),
  },
  {
    id: 'habits',
    title: t('anamnesis.steps.habits.title'),
    description: t('anamnesis.steps.habits.description'),
    component: StepHabits,
    getProps: () => ({ form }),
  },
  {
    id: 'preferences',
    title: t('anamnesis.steps.preferences.title'),
    description: t('anamnesis.steps.preferences.description'),
    component: StepFoodPreferences,
    getProps: () => ({ form, foods: referenceData.foods }),
  },
  {
    id: 'foodRecall',
    title: t('anamnesis.steps.foodRecall.title'),
    description: t('anamnesis.steps.foodRecall.description'),
    component: StepFoodRecall,
    getProps: () => ({
      form,
      foods: referenceData.foods,
      measurementUnits: referenceData.measurementUnits,
    }),
  },
  {
    id: 'diagnosis',
    title: t('anamnesis.steps.diagnosis.title'),
    description: t('anamnesis.steps.diagnosis.description'),
    component: StepDiagnosis,
    getProps: () => ({ form }),
  },
]);

const currentStep = computed(() => steps.value[currentStepIndex.value] ?? steps.value[0]);
const currentStepProps = computed(() => currentStep.value?.getProps?.() ?? {});
const progress = computed(() => {
  if (!steps.value.length) {
    return 0;
  }
  return ((currentStepIndex.value + 1) / steps.value.length) * 100;
});

const selectedAnamnesisId = computed(() => selectedAnamnesis.value?.id ?? null);
const shouldShowSummary = computed(() => viewMode.value === 'summary' && Boolean(selectedAnamnesis.value));
const canCancelEditing = computed(() => viewMode.value === 'form' && Boolean(selectedAnamnesis.value));
const sanitizePayload = (data) => {
  const plain = JSON.parse(JSON.stringify(data));
  plain.biochemicalResults = (plain.biochemicalResults || []).map(({ __key, biochemicalExamName, ...item }) => item);
  plain.foodPreferences = (plain.foodPreferences || []).map(({ __key, foodName, ...item }) => item);
  plain.foodRecalls = (plain.foodRecalls || []).map(({ __key, items = [], ...meal }) => ({
    ...meal,
    items: items.map(({ __key: itemKey, foodName, measurementUnitSymbol, ...item }) => item),
  }));
  plain.pathologyIds = Array.from(new Set(plain.pathologyIds || []));
  plain.userId = plain.userId || null;
  ['countryId', 'cityId', 'educationLevelId', 'professionId'].forEach((key) => {
    if (!plain[key]) {
      plain[key] = null;
    }
  });
  return plain;
};

const saveDraft = () => {
  if (typeof window === 'undefined' || viewMode.value !== 'form') return;
  const snapshot = {
    step: currentStepIndex.value,
    form: sanitizePayload(form),
  };
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot));
};

const rehydrateBiochemicalResults = (items = []) =>
  items.map((item) => ({
    id: item.id ?? null,
    biochemicalExamId: item.biochemicalExamId ?? '',
    biochemicalExamName: item.biochemicalExamName ?? '',
    resultValue: item.resultValue ?? '',
    resultDate: item.resultDate ?? '',
    __key: generateKey(),
  }));

const rehydrateFoodPreferences = (items = []) =>
  items.map((item) => ({
    id: item.id ?? null,
    foodId: item.foodId ?? '',
    foodName: item.foodName ?? '',
    preferenceType: item.preferenceType ?? 'PREFERRED',
    notes: item.notes ?? '',
    __key: generateKey(),
  }));

const rehydrateFoodRecallItems = (items = []) =>
  items.map((item) => ({
    id: item.id ?? null,
    foodId: item.foodId ?? '',
    foodName: item.foodName ?? '',
    measurementUnitId: item.measurementUnitId ?? '',
    measurementUnitSymbol: item.measurementUnitSymbol ?? '',
    quantity: item.quantity ?? null,
    __key: generateKey(),
  }));

const rehydrateFoodRecalls = (items = []) =>
  items.map((meal) => ({
    id: meal.id ?? null,
    mealName: meal.mealName ?? '',
    observation: meal.observation ?? '',
    items: rehydrateFoodRecallItems(meal.items ?? []),
    __key: generateKey(),
  }));

const loadDraft = () => {
  if (typeof window === 'undefined' || selectedAnamnesis.value) return;
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw);
    hydrateFormFromAnamnesis(parsed.form ?? createInitialForm());
    currentStepIndex.value = parsed.step ?? 0;
  } catch (error) {
    window.localStorage.removeItem(STORAGE_KEY);
  }
};

const hydrateFormFromAnamnesis = (data) => {
  const base = createInitialForm();
  Object.keys(base).forEach((key) => {
    if (['biochemicalResults', 'foodPreferences', 'foodRecalls', 'pathologyIds'].includes(key)) {
      return;
    }
    form[key] = data?.[key] ?? base[key];
  });
  form.pathologyIds = Array.isArray(data?.pathologyIds) ? [...data.pathologyIds] : [];
  form.biochemicalResults = rehydrateBiochemicalResults(data?.biochemicalResults || []);
  form.foodPreferences = rehydrateFoodPreferences(data?.foodPreferences || []);
  form.foodRecalls = rehydrateFoodRecalls(data?.foodRecalls || []);
  loadCitiesReference(form.countryId, true);
};

const summaryField = (key) => {
  const value = selectedAnamnesis.value?.[key];
  return value && String(value).trim().length ? value : t('common.placeholders.empty');
};

const selectAnamnesis = (item) => {
  selectedAnamnesis.value = item;
  hydrateFormFromAnamnesis(item);
  viewMode.value = 'summary';
  currentStepIndex.value = 0;
};

const editSelectedAnamnesis = () => {
  if (!selectedAnamnesis.value) {
    startNewAnamnesis();
    return;
  }
  hydrateFormFromAnamnesis(selectedAnamnesis.value);
  viewMode.value = 'form';
  currentStepIndex.value = 0;
};

const startNewAnamnesis = () => {
  selectedAnamnesis.value = null;
  hydrateFormFromAnamnesis(createInitialForm());
  viewMode.value = 'form';
  currentStepIndex.value = 0;
  if (isClient.value && auth.user?.id) {
    form.userId = auth.user.id;
  }
};

const showSummary = () => {
  if (selectedAnamnesis.value) {
    hydrateFormFromAnamnesis(selectedAnamnesis.value);
  }
  viewMode.value = 'summary';
  currentStepIndex.value = 0;
};

const toggleCompactSummary = () => {
  compactSummary.value = !compactSummary.value;
};

const notifyValidation = (messageKey) => {
  notifications.push({
    type: 'warning',
    title: t('notifications.validationTitle'),
    message: t(messageKey),
  });
};

const validateStep = (index = currentStepIndex.value) => {
  if (steps.value[index]?.id === 'personal') {
    if (!form.userId) {
      notifyValidation('notifications.validation.patientSelection');
      return false;
    }
    if (isAdmin.value) {
      if (!form.patientName?.trim()) {
        notifyValidation('notifications.validation.personalName');
        return false;
      }
      if (!form.phoneNumber?.trim()) {
        notifyValidation('notifications.validation.personalPhone');
        return false;
      }
    }
  }
  if (steps.value[index]?.id === 'diagnosis') {
    if (!form.diagnosis?.trim()) {
      notifyValidation('notifications.validation.diagnosis');
      return false;
    }
  }
  return true;
};

const nextStep = () => {
  if (!validateStep()) return;
  if (currentStepIndex.value < steps.value.length - 1) {
    currentStepIndex.value += 1;
  }
};

const previousStep = () => {
  if (currentStepIndex.value > 0) {
    currentStepIndex.value -= 1;
  }
};

const goToStep = (index) => {
  if (index > currentStepIndex.value + 1) return;
  currentStepIndex.value = index;
};

const loadReferenceData = async () => {
  referenceLoading.general = true;
  try {
    const params = { language: referenceParams.value };
    const [
      countriesResponse,
      educationResponse,
      professionResponse,
      pathologiesResponse,
      biochemicalResponse,
      foodsResponse,
      unitsResponse,
    ] = await Promise.all([
      getCountries(params),
      getEducationLevels(params),
      getProfessions(params),
      getPathologies(params),
      getBiochemicalExams(params),
      getFoods(params),
      getMeasurementUnits(params),
    ]);
    referenceData.countries = countriesResponse.data ?? [];
    referenceData.educationLevels = educationResponse.data ?? [];
    referenceData.professions = professionResponse.data ?? [];
    referenceData.pathologies = pathologiesResponse.data ?? [];
    referenceData.biochemicalExams = biochemicalResponse.data ?? [];
    referenceData.foods = foodsResponse.data ?? [];
    referenceData.measurementUnits = unitsResponse.data ?? [];
  } catch (error) {
    console.error('Failed to load reference data for anamnesis', error);
  } finally {
    referenceLoading.general = false;
  }
};

const loadCitiesReference = async (countryId, preserveSelection = false) => {
  if (!countryId) {
    referenceData.cities = [];
    if (!preserveSelection) {
      form.cityId = null;
    }
    return;
  }
  referenceLoading.cities = true;
  try {
    const { data } = await getCities(countryId, { language: referenceParams.value });
    referenceData.cities = data ?? [];
    if (!preserveSelection && form.cityId && !referenceData.cities.some((city) => city.id === form.cityId)) {
      form.cityId = null;
    }
  } catch (error) {
    console.error('Failed to load cities for anamnesis', error);
    referenceData.cities = [];
    if (!preserveSelection) {
      form.cityId = null;
    }
  } finally {
    referenceLoading.cities = false;
  }
};

const loadAnamnesisList = async () => {
  if (isClient.value && !auth.user?.id) {
    return;
  }
  anamnesisLoading.value = true;
  try {
    const params = { size: 50 };
    if (isClient.value && auth.user?.id) {
      params.userId = auth.user.id;
    }
    const { data } = await AnamnesisService.listAll(params);
    const items = data?.content ?? data ?? [];
    anamnesisList.value = items;
    if (isClient.value) {
      const existing = items[0] ?? null;
      if (existing) {
        selectedAnamnesis.value = existing;
        hydrateFormFromAnamnesis(existing);
        viewMode.value = 'summary';
        currentStepIndex.value = 0;
      } else if (viewMode.value === 'summary') {
        startNewAnamnesis();
      }
    } else if (selectedAnamnesis.value) {
      const match = items.find((item) => item.id === selectedAnamnesis.value.id);
      if (match) {
        selectedAnamnesis.value = match;
        if (viewMode.value === 'summary') {
          hydrateFormFromAnamnesis(match);
        }
      }
    }
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('anamnesis.notifications.loadTitle'),
      message: error.response?.data?.message ?? t('anamnesis.notifications.loadMessage'),
    });
  } finally {
    anamnesisLoading.value = false;
  }
};

const saveAnamnesis = async () => {
  if (!validateStep(steps.value.length - 1)) {
    currentStepIndex.value = steps.value.findIndex((step) => step.id === 'diagnosis');
    return;
  }
  saving.value = true;
  try {
    const payload = sanitizePayload(form);
    const response = form.id
      ? await AnamnesisService.update(payload)
      : await AnamnesisService.create(payload);
    const saved = response.data ?? payload;
    notifications.push({
      type: 'success',
      title: t('notifications.success.title'),
      message: t('notifications.success.message'),
    });
    selectedAnamnesis.value = saved;
    hydrateFormFromAnamnesis(saved);
    viewMode.value = 'summary';
    window.localStorage.removeItem(STORAGE_KEY);
    await loadAnamnesisList();
  } catch (error) {
    // handled globally
  } finally {
    saving.value = false;
  }
};

const selectedPathologyNames = computed(() => {
  if (!selectedAnamnesis.value?.pathologyIds?.length) {
    return [];
  }
  const dictionary = new Map(referenceData.pathologies.map((item) => [item.id, item.name]));
  return selectedAnamnesis.value.pathologyIds
    .map((id) => dictionary.get(id))
    .filter(Boolean);
});

const selectedFoodPreferences = computed(() => {
  if (!selectedAnamnesis.value?.foodPreferences?.length) {
    return [];
  }
  const groups = selectedAnamnesis.value.foodPreferences.reduce((acc, pref, index) => {
    const key = pref.preferenceType ?? 'PREFERRED';
    if (!acc[key]) {
      acc[key] = [];
    }
    const labelParts = [pref.foodName, pref.notes].filter((part) => part && String(part).trim().length);
    acc[key].push({
      key: `${key}-${index}`,
      label: labelParts.join(' — '),
    });
    return acc;
  }, {});
  return Object.entries(groups).map(([type, items]) => ({ type, items }));
});
watch(form, saveDraft, { deep: true });
watch(currentStepIndex, () => {
  if (viewMode.value === 'form') {
    saveDraft();
  }
});

watch(
  () => form.countryId,
  (newId, oldId) => {
    if (newId === oldId) {
      return;
    }
    loadCitiesReference(newId, true);
  },
);

watch(
  () => referenceData.cities,
  (list) => {
    if (form.cityId && !list.some((city) => city.id === form.cityId)) {
      form.cityId = null;
    }
  },
  { deep: true },
);

const syncEducationFromReference = () => {
  if (!form.educationLevelId) {
    form.educationLevelName = '';
    return;
  }
  const match = referenceData.educationLevels.find((item) => item.id === form.educationLevelId);
  if (match) {
    form.educationLevelName = match.name;
  }
};

const syncProfessionFromReference = () => {
  if (!form.professionId) {
    form.professionName = '';
    return;
  }
  const match = referenceData.professions.find((item) => item.id === form.professionId);
  if (match) {
    form.professionName = match.name;
  }
};

watch(
  () => form.educationLevelId,
  () => {
    syncEducationFromReference();
  },
  { immediate: true },
);

watch(
  () => form.professionId,
  () => {
    syncProfessionFromReference();
  },
  { immediate: true },
);

watch(
  () => referenceData.educationLevels,
  () => syncEducationFromReference(),
  { deep: true },
);

watch(
  () => referenceData.professions,
  () => syncProfessionFromReference(),
  { deep: true },
);

watch(
  () => locale.value,
  async () => {
    await loadReferenceData();
    if (form.countryId) {
      await loadCitiesReference(form.countryId, true);
    }
  },
);

watch(
  () => auth.user?.id,
  (id) => {
    if (isClient.value) {
      form.userId = id ?? null;
    }
    if (id) {
      void loadAnamnesisList();
    }
  },
  { immediate: true },
);

watch(
  () => auth.user,
  (user) => {
    if (!user || isAdmin.value || selectedAnamnesis.value) {
      return;
    }
    if (!form.patientName) {
      form.patientName = [user.name, user.lastName].filter(Boolean).join(' ');
    }
    if (!form.phoneNumber) {
      form.phoneNumber = user.phoneNumber || '';
    }
    if (!form.address) {
      const countryName = user.countryDTO?.name || user.countryName || user.country || '';
      const cityName = user.cityName || (typeof user.city === 'string' ? user.city : '');
      form.address = [user.street, cityName, user.state, countryName]
        .filter((part) => part && String(part).length)
        .join(', ');
    }
    if (!form.birthDate && user.birthDate) {
      form.birthDate = user.birthDate;
    }
    if (form.age == null && typeof user.age === 'number') {
      form.age = user.age;
    }
    if (!form.countryId && user.countryId) {
      form.countryId = user.countryId;
    }
    if (!form.cityId && user.cityId) {
      form.cityId = user.cityId;
    }
    if (!form.educationLevelId && user.educationLevelId) {
      form.educationLevelId = user.educationLevelId;
    }
    if (!form.professionId && user.professionId) {
      form.professionId = user.professionId;
    }
    if (!form.educationLevelName && user.education) {
      form.educationLevelName = user.education;
    }
    if (!form.professionName && user.occupation) {
      form.professionName = user.occupation;
    }
    if (!form.consultationGoal && user.consultationGoal) {
      form.consultationGoal = user.consultationGoal;
    }
  },
  { immediate: true },
);

onMounted(async () => {
  await loadSettings();
  await loadReferenceData();
  if (form.countryId) {
    await loadCitiesReference(form.countryId, true);
  }
  await loadAnamnesisList();
  if (!selectedAnamnesis.value) {
    loadDraft();
  }
});
</script>
