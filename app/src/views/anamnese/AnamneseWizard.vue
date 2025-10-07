<template>
  <div class="mx-auto max-w-6xl">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">{{ t('anamnese.title') }}</h1>
        <p class="mt-1 max-w-3xl text-sm text-slate-500">
          {{ t('anamnese.subtitle') }}
        </p>
      </div>
      <div class="text-sm font-medium text-slate-500">
        {{ t('common.stepIndicator', { current: currentStepIndex + 1, total: steps.length }) }}
      </div>
    </div>

    <div class="mt-6 h-2 rounded-full bg-slate-200">
      <div class="h-2 rounded-full bg-primary-500 transition-all" :style="{ width: `${progress}%` }"></div>
    </div>

    <div class="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
      <button v-for="(step, index) in steps" :key="step.id" type="button"
        class="flex items-center gap-3 rounded-2xl border px-4 py-3 text-left text-sm font-semibold transition"
        :class="[
          index < currentStepIndex
            ? 'border-primary-200 bg-primary-50 text-primary-600'
            : index === currentStepIndex
              ? 'border-primary-300 bg-white text-primary-600 shadow-sm'
              : 'border-slate-200 bg-white text-slate-500 hover:border-primary-200 hover:text-primary-600'
        ]"
        @click="goToStep(index)"
        :disabled="index > currentStepIndex + 1">
        <span
          class="flex h-8 w-8 items-center justify-center rounded-full border text-sm"
          :class="index <= currentStepIndex ? 'border-primary-500 bg-primary-500 text-white' : 'border-slate-200 text-slate-500'">
          {{ index + 1 }}
        </span>
        <div>
          <p>{{ step.title }}</p>
          <p class="text-xs font-normal text-slate-400">{{ step.description }}</p>
        </div>
      </button>
    </div>

    <div class="mt-8 rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
      <component :is="currentStep.component" v-bind="currentStepProps" />
    </div>

    <div class="mt-6 flex flex-col justify-between gap-4 sm:flex-row">
      <div class="flex gap-3">
        <button type="button" class="inline-flex items-center rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-primary-200 hover:text-primary-600 disabled:opacity-40"
          :disabled="currentStepIndex === 0" @click="previousStep">
          {{ t('common.actions.back') }}
        </button>
        <button type="button" class="inline-flex items-center rounded-xl border border-primary-200 px-4 py-2 text-sm font-semibold text-primary-600 transition hover:border-primary-300 hover:bg-primary-50" @click="exportPdf">
          {{ t('common.actions.exportPdf') }}
        </button>
      </div>
      <div class="flex gap-3">
        <button type="button" class="inline-flex items-center rounded-xl border border-primary-200 px-4 py-2 text-sm font-semibold text-primary-600 transition hover:border-primary-300 hover:bg-primary-50"
          @click="nextStep" :disabled="currentStepIndex === steps.length - 1">
          {{ t('common.actions.next') }}
        </button>
        <button type="button" class="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-6 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500 disabled:opacity-50"
          :disabled="saving" @click="saveAnamnese">
          <span v-if="saving" class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
          <span>{{ saving ? t('common.actions.saving') : t('common.actions.save') }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import StepDadosPessoais from '@/components/anamnese/StepDadosPessoais.vue';
import StepAvaliacaoClinica from '@/components/anamnese/StepAvaliacaoClinica.vue';
import StepPatologias from '@/components/anamnese/StepPatologias.vue';
import StepBioimpedancia from '@/components/anamnese/StepBioimpedancia.vue';
import StepBioquimica from '@/components/anamnese/StepBioquimica.vue';
import StepHabitos from '@/components/anamnese/StepHabitos.vue';
import StepPreferencias from '@/components/anamnese/StepPreferencias.vue';
import StepRecordatorio from '@/components/anamnese/StepRecordatorio.vue';
import StepDiagnostico from '@/components/anamnese/StepDiagnostico.vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import AnamneseService from '@/services/AnamneseService';
import { useI18n } from 'vue-i18n';

const STORAGE_KEY = 'jm_anamnese_wizard_draft';

const auth = useAuthStore();
const notifications = useNotificationStore();
const { t } = useI18n();

const saving = ref(false);
const currentStepIndex = ref(0);

const createInitialForm = () => ({
  id: null,
  userId: auth.user?.id ?? null,
  paciente: '',
  endereco: '',
  dataNascimento: '',
  idade: null,
  telefone: '',
  escolaridade: '',
  profissao: '',
  objetivoConsulta: '',
  mucosa: '',
  membros: '',
  cirurgias: '',
  avaliacaoObservacoes: '',
  colesterol: false,
  hipertensao: false,
  diabetes1: false,
  diabetes2: false,
  trigliceridemia: false,
  anemia: false,
  intestinal: false,
  gastrica: false,
  renal: false,
  hepatica: false,
  vesicular: false,
  peso: null,
  estatura: null,
  imc: null,
  gorduraPercent: null,
  musculoPercent: null,
  tmb: null,
  circunAbdomen: null,
  circunCintura: null,
  circunQuadril: null,
  circunBraco: null,
  circunJoelho: null,
  circunTorax: null,
  examesBioquimicos: [],
  preparoAlimentos: '',
  localRefeicao: '',
  horarioTrabalho: '',
  horarioEstudo: '',
  apetite: '',
  ingestaoHidrica: '',
  atividadeFisica: '',
  frequencia: '',
  duracao: '',
  fuma: false,
  bebe: false,
  suplementos: '',
  sono: '',
  mastigacao: '',
  habitosObservacoes: '',
  alimentosPreferidos: '',
  alimentosNaoGosta: '',
  refeicoes24h: [],
  diagnostico: '',
  resumoDieta: '',
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
    id: 'dados',
    title: t('anamnese.steps.personal.title'),
    description: t('anamnese.steps.personal.description'),
    component: StepDadosPessoais,
    getProps: () => ({ form, isAdmin: isAdmin.value }),
  },
  {
    id: 'clinica',
    title: t('anamnese.steps.clinical.title'),
    description: t('anamnese.steps.clinical.description'),
    component: StepAvaliacaoClinica,
    getProps: () => ({ form }),
  },
  {
    id: 'patologias',
    title: t('anamnese.steps.pathologies.title'),
    description: t('anamnese.steps.pathologies.description'),
    component: StepPatologias,
    getProps: () => ({ form }),
  },
  {
    id: 'bioimpedancia',
    title: t('anamnese.steps.bioimpedance.title'),
    description: t('anamnese.steps.bioimpedance.description'),
    component: StepBioimpedancia,
    getProps: () => ({ form }),
  },
  {
    id: 'bioquimica',
    title: t('anamnese.steps.biochemistry.title'),
    description: t('anamnese.steps.biochemistry.description'),
    component: StepBioquimica,
    getProps: () => ({ form }),
  },
  {
    id: 'habitos',
    title: t('anamnese.steps.habits.title'),
    description: t('anamnese.steps.habits.description'),
    component: StepHabitos,
    getProps: () => ({ form }),
  },
  {
    id: 'preferencias',
    title: t('anamnese.steps.preferences.title'),
    description: t('anamnese.steps.preferences.description'),
    component: StepPreferencias,
    getProps: () => ({ form }),
  },
  {
    id: 'recordatorio',
    title: t('anamnese.steps.recordatorio.title'),
    description: t('anamnese.steps.recordatorio.description'),
    component: StepRecordatorio,
    getProps: () => ({ form }),
  },
  {
    id: 'diagnostico',
    title: t('anamnese.steps.diagnostico.title'),
    description: t('anamnese.steps.diagnostico.description'),
    component: StepDiagnostico,
    getProps: () => ({ form }),
  },
]);

const isAdmin = computed(() => auth.user?.type === 'ADMIN');

const currentStep = computed(() => steps.value[currentStepIndex.value] ?? steps.value[0]);
const currentStepProps = computed(() => currentStep.value?.getProps?.() ?? {});
const progress = computed(() => {
  if (!steps.value.length) {
    return 0;
  }
  return ((currentStepIndex.value + 1) / steps.value.length) * 100;
});

const sanitizePayload = (data) => {
  const plain = JSON.parse(JSON.stringify(data));
  plain.examesBioquimicos = (plain.examesBioquimicos || []).map(({ __key, ...item }) => item);
  plain.refeicoes24h = (plain.refeicoes24h || []).map(({ __key, ...item }) => item);
  return plain;
};

const saveDraft = () => {
  if (typeof window === 'undefined') return;
  const snapshot = {
    step: currentStepIndex.value,
    form: sanitizePayload(form),
  };
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot));
};

const rehydrateList = (items = []) => items.map((item) => ({ ...item, __key: generateKey() }));

const loadDraft = () => {
  if (typeof window === 'undefined') return;
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw);
    const base = createInitialForm();
    Object.keys(base).forEach((key) => {
      if (key === 'examesBioquimicos' || key === 'refeicoes24h') {
        return;
      }
      form[key] = parsed.form?.[key] ?? base[key];
    });
    form.examesBioquimicos = rehydrateList(parsed.form?.examesBioquimicos || []);
    form.refeicoes24h = rehydrateList(parsed.form?.refeicoes24h || []);
    currentStepIndex.value = parsed.step ?? 0;
  } catch (error) {
    window.localStorage.removeItem(STORAGE_KEY);
  }
};

const resetForm = () => {
  const base = createInitialForm();
  Object.keys(base).forEach((key) => {
    form[key] = base[key];
  });
  form.examesBioquimicos = [];
  form.refeicoes24h = [];
  currentStepIndex.value = 0;
  if (typeof window !== 'undefined') {
    window.localStorage.removeItem(STORAGE_KEY);
  }
};

const notifyValidation = (messageKey) => {
  notifications.push({
    type: 'warning',
    title: t('notifications.validationTitle'),
    message: t(messageKey),
  });
};

const validateStep = (index = currentStepIndex.value) => {
  if (steps.value[index]?.id === 'dados') {
    if (!form.userId) {
      notifyValidation('notifications.validation.patientSelection');
      return false;
    }
    if (isAdmin.value) {
      if (!form.paciente?.trim()) {
        notifyValidation('notifications.validation.personalName');
        return false;
      }
      if (!form.telefone?.trim()) {
        notifyValidation('notifications.validation.personalPhone');
        return false;
      }
    }
  }
  if (steps.value[index]?.id === 'diagnostico') {
    if (!form.diagnostico?.trim()) {
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

const exportPdf = () => {
  notifications.push({
    type: 'info',
    title: t('notifications.export.title'),
    message: t('notifications.export.message'),
  });
};

const saveAnamnese = async () => {
  if (!validateStep(steps.value.length - 1)) {
    currentStepIndex.value = steps.value.findIndex((step) => step.id === 'diagnostico');
    return;
  }
  saving.value = true;
  try {
    const payload = sanitizePayload(form);
    await AnamneseService.create(payload);
    notifications.push({
      type: 'success',
      title: t('notifications.success.title'),
      message: t('notifications.success.message'),
    });
    resetForm();
  } catch (error) {
    // Erros são tratados pelo interceptor global
  } finally {
    saving.value = false;
  }
};

watch(form, saveDraft, { deep: true });
watch(currentStepIndex, saveDraft);

watch(
  () => auth.user?.id,
  (id) => {
    if (!form.userId && id) {
      form.userId = id;
    }
  },
  { immediate: true }
);

watch(
  () => auth.user,
  (user) => {
    if (!user || isAdmin.value) {
      return;
    }
    if (!form.paciente) {
      form.paciente = [user.name, user.lastName].filter(Boolean).join(' ');
    }
    if (!form.telefone) {
      form.telefone = user.phoneNumber || '';
    }
    if (!form.endereco) {
      form.endereco = [user.street, user.city, user.state, user.country].filter((part) => part && part.length).join(', ');
    }
    if (!form.dataNascimento && user.birthDate) {
      form.dataNascimento = user.birthDate;
    }
    if (form.idade == null && typeof user.age === 'number') {
      form.idade = user.age;
    }
    if (!form.escolaridade && user.education) {
      form.escolaridade = user.education;
    }
    if (!form.profissao && user.occupation) {
      form.profissao = user.occupation;
    }
    if (!form.objetivoConsulta && user.consultationGoal) {
      form.objetivoConsulta = user.consultationGoal;
    }
  },
  { immediate: true }
);

onMounted(() => {
  loadDraft();
});
</script>
