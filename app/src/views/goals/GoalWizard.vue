<template>
  <div class="space-y-8">
    <header class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ t('menu.title') }}</p>
        <h1 class="text-2xl font-semibold text-slate-900">{{ headerTitle }}</h1>
        <p class="mt-1 max-w-2xl text-sm text-slate-500">{{ t('goals.wizard.description') }}</p>
      </div>
      <div class="flex flex-wrap items-center gap-3">
        <button type="button" class="btn-secondary" @click="goBack">{{ t('common.actions.cancel') }}</button>
      </div>
    </header>

    <div v-if="loading" class="space-y-4">
      <div class="h-12 animate-pulse rounded-2xl bg-slate-200"></div>
      <div class="h-72 animate-pulse rounded-2xl bg-slate-200"></div>
    </div>
    <template v-else>
      <nav class="grid gap-3 sm:grid-cols-4">
        <button
          v-for="(step, index) in steps"
          :key="step.id"
          type="button"
          class="flex items-center gap-3 rounded-2xl border px-4 py-3 text-left text-sm font-semibold transition"
          :class="index === currentStepIndex
            ? 'border-primary-300 bg-primary-50 text-primary-600 shadow-sm'
            : 'border-slate-200 bg-white text-slate-500 hover:border-primary-200 hover:text-primary-600'"
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
          <button type="button" class="btn-primary" :disabled="saving" @click="saveGoal">
            <span
              v-if="saving"
              class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"
            ></span>
            <span>{{ saving ? t('common.actions.saving') : t('goals.save') }}</span>
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
import StepGoalType from '@/components/goals/StepGoalType.vue';
import StepTarget from '@/components/goals/StepTarget.vue';
import StepPeriodicity from '@/components/goals/StepPeriodicity.vue';
import StepSummary from '@/components/goals/StepSummary.vue';
import GoalService from '@/services/GoalService';
import { useNotificationStore } from '@/stores/notifications';
import { useAuthStore } from '@/stores/auth';

const { t } = useI18n();
const router = useRouter();
const route = useRoute();
const notifications = useNotificationStore();
const auth = useAuthStore();

const loading = ref(false);
const saving = ref(false);
const units = ref([]);
const templates = ref([]);
const templatesLoading = ref(false);
const owners = ref([]);
const ownersLoading = ref(false);

const form = reactive({
  id: null,
  type: 'PROTEIN',
  targetValue: null,
  unitId: null,
  targetMode: 'ABSOLUTE',
  periodicity: 'DAILY',
  customPeriodDays: null,
  startDate: null,
  endDate: null,
  notes: '',
  active: true,
  createdByUserId: null,
  createdByUserName: '',
  templateId: null,
});

const currentStepIndex = ref(0);

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');

const steps = computed(() => [
  {
    id: 'type',
    title: t('goals.wizard.steps.type.title'),
    description: t('goals.wizard.steps.type.description'),
    component: StepGoalType,
  },
  {
    id: 'target',
    title: t('goals.wizard.steps.target.title'),
    description: t('goals.wizard.steps.target.description'),
    component: StepTarget,
  },
  {
    id: 'periodicity',
    title: t('goals.wizard.steps.periodicity.title'),
    description: t('goals.wizard.steps.periodicity.description'),
    component: StepPeriodicity,
  },
  {
    id: 'summary',
    title: t('goals.wizard.steps.summary.title'),
    description: t('goals.wizard.steps.summary.description'),
    component: StepSummary,
  },
]);

const headerTitle = computed(() => (form.id ? t('goals.edit') : t('goals.new')));

const ownerName = computed(() => {
  if (form.createdByUserId) {
    const match = owners.value.find((owner) => owner.id === form.createdByUserId);
    if (match) {
      return match.displayName || match.email;
    }
  }
  return form.createdByUserName;
});

const templateName = computed(() => {
  if (!form.templateId) {
    return '';
  }
  const template = templates.value.find((item) => item.id === form.templateId);
  return template?.name ?? '';
});

const currentStep = computed(() => steps.value[currentStepIndex.value]);

const currentStepProps = computed(() => {
  switch (currentStep.value.id) {
    case 'type':
      return {
        modelValue: form.type,
        templates: templates.value,
        selectedTemplateId: form.templateId,
        loadingTemplates: templatesLoading.value,
        types: goalTypeOptions.value,
        isAdmin: isAdmin.value,
        ownerId: form.createdByUserId,
        owners: owners.value,
        loadingOwners: ownersLoading.value,
        'onUpdate:modelValue': (value) => {
          form.type = value;
        },
        'onUpdate:selectedTemplateId': (value) => applyTemplate(value),
        'onUpdate:ownerId': (value) => {
          form.createdByUserId = value || null;
        },
        'onSearch-owner': (value) => searchOwners(value),
      };
    case 'target':
      return {
        units: units.value,
        unitId: form.unitId,
        targetValue: form.targetValue,
        targetMode: form.targetMode,
        'onUpdate:unitId': (value) => {
          form.unitId = value || null;
        },
        'onUpdate:targetValue': (value) => {
          form.targetValue = value;
        },
        'onUpdate:targetMode': (value) => {
          form.targetMode = value;
        },
      };
    case 'periodicity':
      return {
        periodicity: form.periodicity,
        customPeriodDays: form.customPeriodDays,
        startDate: form.startDate,
        endDate: form.endDate,
        'onUpdate:periodicity': (value) => {
          form.periodicity = value;
          if (value !== 'CUSTOM') {
            form.customPeriodDays = null;
          }
        },
        'onUpdate:customPeriodDays': (value) => {
          form.customPeriodDays = value;
        },
        'onUpdate:startDate': (value) => {
          form.startDate = value;
        },
        'onUpdate:endDate': (value) => {
          form.endDate = value;
        },
      };
    case 'summary':
      return {
        goalType: form.type,
        unitName: currentUnitName.value,
        unitSymbol: currentUnitSymbol.value,
        targetValue: form.targetValue,
        targetMode: form.targetMode,
        periodicity: form.periodicity,
        customPeriodDays: form.customPeriodDays,
        startDate: form.startDate,
        endDate: form.endDate,
        ownerName: ownerName.value,
        templateName: templateName.value,
        notes: form.notes,
        active: form.active,
        'onUpdate:notes': (value) => {
          form.notes = value;
        },
        'onUpdate:active': (value) => {
          form.active = value;
        },
      };
    default:
      return {};
  }
});

const goalTypeOptions = computed(() => [
  { value: 'PROTEIN', label: t('goals.types.PROTEIN') },
  { value: 'CARBOHYDRATE', label: t('goals.types.CARBOHYDRATE') },
  { value: 'FAT', label: t('goals.types.FAT') },
  { value: 'WATER', label: t('goals.types.WATER') },
  { value: 'FIBER', label: t('goals.types.FIBER') },
  { value: 'ENERGY', label: t('goals.types.ENERGY') },
  { value: 'MICRONUTRIENTS', label: t('goals.types.MICRONUTRIENTS') },
  { value: 'OTHER', label: t('goals.types.OTHER') },
]);

const currentUnitName = computed(() => {
  const unit = units.value.find((item) => item.id === form.unitId);
  return unit?.displayName ?? '';
});

const currentUnitSymbol = computed(() => {
  const unit = units.value.find((item) => item.id === form.unitId);
  return unit?.symbol ?? '';
});

const goBack = () => router.push({ name: 'goals' });
const goToStep = (index) => {
  currentStepIndex.value = index;
};
const nextStep = () => {
  if (currentStepIndex.value < steps.value.length - 1) {
    currentStepIndex.value += 1;
  }
};
const previousStep = () => {
  if (currentStepIndex.value > 0) {
    currentStepIndex.value -= 1;
  }
};

const loadUnits = async () => {
  try {
    const { data } = await GoalService.listUnits();
    units.value = data ?? [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to load units.',
    });
  }
};

const loadTemplates = async () => {
  templatesLoading.value = true;
  try {
    const { data } = await GoalService.listTemplates({ size: 100 });
    const content = data?.content ?? data ?? [];
    templates.value = content;
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to load templates.',
    });
  } finally {
    templatesLoading.value = false;
  }
};

const loadGoal = async (id) => {
  if (!id) {
    return;
  }
  loading.value = true;
  try {
    const { data } = await GoalService.get(id);
    form.id = data.id;
    form.type = data.type ?? form.type;
    form.targetValue = data.targetValue;
    form.unitId = data.unitId;
    form.targetMode = data.targetMode ?? form.targetMode;
    form.periodicity = data.periodicity ?? form.periodicity;
    form.customPeriodDays = data.customPeriodDays;
    form.startDate = data.startDate;
    form.endDate = data.endDate;
    form.notes = data.notes ?? '';
    form.active = data.active !== false;
    form.createdByUserId = data.createdByUserId;
    form.createdByUserName = data.createdByUserName ?? '';
    form.templateId = data.templateId ?? null;

    if (form.createdByUserId && !owners.value.find((owner) => owner.id === form.createdByUserId)) {
      owners.value.push({
        id: form.createdByUserId,
        displayName: data.createdByUserName,
        email: '',
      });
    }
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to load nutrition goal.',
    });
    goBack();
  } finally {
    loading.value = false;
  }
};

const searchOwners = async (query) => {
  if (!isAdmin.value) {
    return;
  }
  ownersLoading.value = true;
  try {
    const params = query ? { query } : {};
    const { data } = await GoalService.listOwners(params);
    owners.value = data ?? [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to load owners.',
    });
  } finally {
    ownersLoading.value = false;
  }
};

const applyTemplate = (templateId) => {
  form.templateId = templateId || null;
  if (!templateId) {
    return;
  }
  const template = templates.value.find((item) => item.id === templateId);
  if (!template) {
    return;
  }
  form.type = template.type ?? form.type;
  form.targetValue = template.targetValue ?? form.targetValue;
  form.unitId = template.unitId ?? form.unitId;
  form.periodicity = template.periodicity ?? form.periodicity;
  form.customPeriodDays = template.customPeriodDays ?? null;
  form.targetMode = template.targetMode ?? form.targetMode;
  if (!form.notes && template.notes) {
    form.notes = template.notes;
  }
};

const validateForm = () => {
  if (!form.type || !form.unitId || form.targetValue == null || Number.isNaN(Number(form.targetValue))) {
    return false;
  }
  if (!form.periodicity) {
    return false;
  }
  if (form.periodicity === 'CUSTOM' && !form.customPeriodDays) {
    return false;
  }
  if (isAdmin.value && !form.createdByUserId) {
    return false;
  }
  return true;
};

const saveGoal = async () => {
  if (!validateForm()) {
    notifications.push({
      type: 'warning',
      title: t('notifications.validationTitle'),
      message: t('goals.validation.required'),
    });
    return;
  }

  const payload = {
    id: form.id,
    type: form.type,
    targetValue: form.targetValue != null ? Number(form.targetValue) : null,
    unitId: form.unitId,
    targetMode: form.targetMode,
    periodicity: form.periodicity,
    customPeriodDays: form.periodicity === 'CUSTOM' ? form.customPeriodDays : null,
    startDate: form.startDate,
    endDate: form.endDate,
    notes: form.notes,
    active: form.active,
    templateId: form.templateId,
    createdByUserId: form.createdByUserId,
  };

  if (!isAdmin.value) {
    payload.createdByUserId = auth.user?.id ?? null;
  }

  saving.value = true;
  try {
    const request = payload.id ? GoalService.update(payload) : GoalService.create(payload);
    await request;
    notifications.push({
      type: 'success',
      title: t('goals.title'),
      message: t('goals.wizard.notifications.saved'),
    });
    goBack();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('notifications.validationTitle'),
      message: error.response?.data?.details ?? 'Unable to save nutrition goal.',
    });
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  if (!isAdmin.value && auth.user?.id) {
    form.createdByUserId = auth.user.id;
    form.createdByUserName = `${auth.user.name ?? ''} ${auth.user.lastName ?? ''}`.trim();
  }
  await Promise.all([loadUnits(), loadTemplates()]);
  if (isAdmin.value) {
    await searchOwners('');
  }
  await loadGoal(route.params.id);
});

watch(
  () => route.params.id,
  async (id) => {
    if (!id) {
      Object.assign(form, {
        id: null,
        type: 'PROTEIN',
        targetValue: null,
        unitId: null,
        targetMode: 'ABSOLUTE',
        periodicity: 'DAILY',
        customPeriodDays: null,
        startDate: null,
        endDate: null,
        notes: '',
        active: true,
        templateId: null,
        createdByUserId: null,
        createdByUserName: '',
      });
      if (!isAdmin.value && auth.user?.id) {
        form.createdByUserId = auth.user.id;
        form.createdByUserName = `${auth.user.name ?? ''} ${auth.user.lastName ?? ''}`.trim();
      }
      return;
    }
    await loadGoal(id);
  }
);
</script>
