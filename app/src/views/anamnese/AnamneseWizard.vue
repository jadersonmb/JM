<template>
  <div class="mx-auto max-w-6xl">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">{{ t('anamnese.title') }}</h1>
        <p class="mt-1 max-w-3xl text-sm text-slate-500">
          {{ t('anamnese.subtitle') }}
        </p>
      </div>
      <div v-if="viewMode === 'form'" class="text-sm font-medium text-slate-500">
        {{ t('common.stepIndicator', { current: currentStepIndex + 1, total: steps.length }) }}
      </div>
    </div>

    <div class="mt-8 grid gap-6" :class="isAdmin ? 'lg:grid-cols-[minmax(0,1fr)_minmax(0,1.35fr)]' : ''">
      <section v-if="isAdmin" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <header class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.admin.listTitle') }}</h2>
            <p class="text-sm text-slate-500">{{ t('anamnese.admin.listSubtitle') }}</p>
          </div>
          <button type="button" class="btn-secondary" :disabled="anamnesesLoading" @click="startNewAnamnese">
            <span v-if="anamnesesLoading" class="loader h-4 w-4"></span>
            <span v-else>{{ t('anamnese.admin.new') }}</span>
          </button>
        </header>

        <div v-if="anamnesesLoading" class="mt-4 space-y-2">
          <div class="h-10 w-full animate-pulse rounded-xl bg-slate-200"></div>
          <div class="h-10 w-full animate-pulse rounded-xl bg-slate-200"></div>
          <div class="h-10 w-full animate-pulse rounded-xl bg-slate-200"></div>
        </div>
        <div v-else-if="anamneses.length" class="mt-4 overflow-hidden rounded-2xl border border-slate-200">
          <table class="min-w-full divide-y divide-slate-200 text-sm">
            <thead class="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-4 py-3 text-left">{{ t('anamnese.admin.columns.patient') }}</th>
                <th class="px-4 py-3 text-left">{{ t('anamnese.admin.columns.phone') }}</th>
                <th class="px-4 py-3 text-left">{{ t('anamnese.admin.columns.goal') }}</th>
                <th class="px-4 py-3 text-right">{{ t('anamnese.admin.columns.actions') }}</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 bg-white">
              <tr
                v-for="item in anamneses"
                :key="item.id"
                :class="selectedAnamneseId === item.id ? 'bg-primary-50/50' : ''"
              >
                <td class="px-4 py-3 font-semibold text-slate-700">{{ item.paciente || t('common.placeholders.empty') }}</td>
                <td class="px-4 py-3 text-slate-500">{{ item.telefone || t('common.placeholders.empty') }}</td>
                <td class="px-4 py-3 text-slate-500">{{ item.objetivoConsulta || t('common.placeholders.empty') }}</td>
                <td class="px-4 py-3 text-right">
                  <button type="button" class="btn-ghost text-primary-600" @click="selectAnamnese(item)">
                    {{ t('anamnese.admin.view') }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="mt-4 text-sm text-slate-500">{{ t('anamnese.admin.empty') }}</p>
      </section>

      <div class="space-y-6">
        <section v-if="shouldShowSummary" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <header class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.summary.title') }}</h2>
              <p class="text-sm text-slate-500">{{ t('anamnese.summary.subtitle') }}</p>
            </div>
            <div class="flex flex-wrap gap-2">
              <button type="button" class="btn-secondary" @click="editSelectedAnamnese">
                {{ t('common.actions.edit') }}
              </button>
              <button
                v-if="isAdmin"
                type="button"
                class="btn-ghost text-primary-600"
                @click="startNewAnamnese"
              >
                {{ t('anamnese.summary.newRecord') }}
              </button>
            </div>
          </header>

          <dl class="mt-4 grid gap-4 sm:grid-cols-2">
            <div>
              <dt class="text-xs uppercase tracking-wide text-slate-400">{{ t('anamnese.summary.fields.patient') }}</dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('paciente') }}</dd>
            </div>
            <div>
              <dt class="text-xs uppercase tracking-wide text-slate-400">{{ t('anamnese.summary.fields.phone') }}</dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('telefone') }}</dd>
            </div>
            <div>
              <dt class="text-xs uppercase tracking-wide text-slate-400">{{ t('anamnese.summary.fields.goal') }}</dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('objetivoConsulta') }}</dd>
            </div>
            <div>
              <dt class="text-xs uppercase tracking-wide text-slate-400">{{ t('anamnese.summary.fields.diagnosis') }}</dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('diagnostico') }}</dd>
            </div>
            <div class="sm:col-span-2">
              <dt class="text-xs uppercase tracking-wide text-slate-400">{{ t('anamnese.summary.fields.diet') }}</dt>
              <dd class="mt-1 text-sm text-slate-700">{{ summaryField('resumoDieta') }}</dd>
            </div>
          </dl>
        </section>

        <div v-if="viewMode === 'form'" class="space-y-6">
          <div class="h-2 rounded-full bg-slate-200">
            <div class="h-2 rounded-full bg-primary-500 transition-all" :style="{ width: `${progress}%` }"></div>
          </div>

          <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
            <button
              v-for="(step, index) in steps"
              :key="step.id"
              type="button"
              class="flex items-center gap-3 rounded-2xl border px-4 py-3 text-left text-sm font-semibold transition"
              :class="[
                index < currentStepIndex
                  ? 'border-primary-200 bg-primary-50 text-primary-600'
                  : index === currentStepIndex
                    ? 'border-primary-300 bg-white text-primary-600 shadow-sm'
                    : 'border-slate-200 bg-white text-slate-500 hover:border-primary-200 hover:text-primary-600'
              ]"
              @click="goToStep(index)"
              :disabled="index > currentStepIndex + 1"
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
          </div>

          <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <component :is="currentStep.component" v-bind="currentStepProps" />
          </div>

          <div class="flex flex-col justify-between gap-4 sm:flex-row">
            <div class="flex flex-wrap gap-3">
              <button
                type="button"
                class="inline-flex items-center rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-primary-200 hover:text-primary-600 disabled:opacity-40"
                :disabled="currentStepIndex === 0"
                @click="previousStep"
              >
                {{ t('common.actions.back') }}
              </button>
              <button
                v-if="canCancelEditing"
                type="button"
                class="inline-flex items-center rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-primary-200 hover:text-primary-600"
                @click="showSummary"
              >
                {{ t('common.actions.cancel') }}
              </button>
              <button
                type="button"
                class="inline-flex items-center rounded-xl border border-primary-200 px-4 py-2 text-sm font-semibold text-primary-600 transition hover:border-primary-300 hover:bg-primary-50"
                @click="exportPdf"
              >
                {{ t('common.actions.exportPdf') }}
              </button>
            </div>
            <div class="flex flex-wrap gap-3">
              <button
                type="button"
                class="inline-flex items-center rounded-xl border border-primary-200 px-4 py-2 text-sm font-semibold text-primary-600 transition hover:border-primary-300 hover:bg-primary-50"
                @click="nextStep"
                :disabled="currentStepIndex === steps.length - 1"
              >
                {{ t('common.actions.next') }}
              </button>
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-6 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500 disabled:opacity-50"
                :disabled="saving"
                @click="saveAnamnese"
              >
                <span v-if="saving" class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                <span>{{ saving ? t('common.actions.saving') : t('common.actions.save') }}</span>
              </button>
            </div>
          </div>
        </div>
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
const viewMode = ref('form');

const anamneses = ref([]);
const anamnesesLoading = ref(false);
const selectedAnamnese = ref(null);

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const isClient = computed(() => !isAdmin.value);

const createInitialForm = () => ({
  id: null,
  userId: isAdmin.value ? null : auth.user?.id ?? null,
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

const currentStep = computed(() => steps.value[currentStepIndex.value] ?? steps.value[0]);
const currentStepProps = computed(() => currentStep.value?.getProps?.() ?? {});
const progress = computed(() => {
  if (!steps.value.length) {
    return 0;
  }
  return ((currentStepIndex.value + 1) / steps.value.length) * 100;
});

const selectedAnamneseId = computed(() => selectedAnamnese.value?.id ?? null);
const shouldShowSummary = computed(() => viewMode.value === 'summary' && Boolean(selectedAnamnese.value));
const canCancelEditing = computed(() => viewMode.value === 'form' && Boolean(selectedAnamnese.value));

const sanitizePayload = (data) => {
  const plain = JSON.parse(JSON.stringify(data));
  plain.examesBioquimicos = (plain.examesBioquimicos || []).map(({ __key, ...item }) => item);
  plain.refeicoes24h = (plain.refeicoes24h || []).map(({ __key, ...item }) => item);
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

const rehydrateList = (items = []) => items.map((item) => ({ ...item, __key: generateKey() }));

const loadDraft = () => {
  if (typeof window === 'undefined' || selectedAnamnese.value) return;
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw);
    hydrateFormFromAnamnese(parsed.form ?? createInitialForm());
    currentStepIndex.value = parsed.step ?? 0;
  } catch (error) {
    window.localStorage.removeItem(STORAGE_KEY);
  }
};

const hydrateFormFromAnamnese = (data) => {
  const base = createInitialForm();
  Object.keys(base).forEach((key) => {
    if (key === 'examesBioquimicos' || key === 'refeicoes24h') {
      return;
    }
    form[key] = data?.[key] ?? base[key];
  });
  form.examesBioquimicos = rehydrateList(data?.examesBioquimicos || []);
  form.refeicoes24h = rehydrateList(data?.refeicoes24h || []);
};

const summaryField = (key) => {
  const value = selectedAnamnese.value?.[key];
  return value && String(value).trim().length ? value : t('common.placeholders.empty');
};

const selectAnamnese = (item) => {
  selectedAnamnese.value = item;
  hydrateFormFromAnamnese(item);
  viewMode.value = 'summary';
  currentStepIndex.value = 0;
};

const editSelectedAnamnese = () => {
  if (!selectedAnamnese.value) {
    startNewAnamnese();
    return;
  }
  hydrateFormFromAnamnese(selectedAnamnese.value);
  viewMode.value = 'form';
  currentStepIndex.value = 0;
};

const startNewAnamnese = () => {
  selectedAnamnese.value = null;
  hydrateFormFromAnamnese(createInitialForm());
  viewMode.value = 'form';
  currentStepIndex.value = 0;
  if (isClient.value && auth.user?.id) {
    form.userId = auth.user.id;
  }
};

const showSummary = () => {
  if (selectedAnamnese.value) {
    hydrateFormFromAnamnese(selectedAnamnese.value);
  }
  viewMode.value = 'summary';
  currentStepIndex.value = 0;
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

const loadAnamneses = async () => {
  if (isClient.value && !auth.user?.id) {
    return;
  }
  anamnesesLoading.value = true;
  try {
    const params = { size: 50 };
    if (isClient.value && auth.user?.id) {
      params.userId = auth.user.id;
    }
    const { data } = await AnamneseService.listAll(params);
    const items = data?.content ?? data ?? [];
    anamneses.value = items;
    if (isClient.value) {
      const existing = items[0] ?? null;
      if (existing) {
        selectedAnamnese.value = existing;
        hydrateFormFromAnamnese(existing);
        viewMode.value = 'summary';
        currentStepIndex.value = 0;
      } else if (viewMode.value === 'summary') {
        startNewAnamnese();
      }
    } else if (selectedAnamnese.value) {
      const match = items.find((item) => item.id === selectedAnamnese.value.id);
      if (match) {
        selectedAnamnese.value = match;
        if (viewMode.value === 'summary') {
          hydrateFormFromAnamnese(match);
        }
      }
    }
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('anamnese.notifications.loadTitle'),
      message: error.response?.data?.message ?? t('anamnese.notifications.loadMessage'),
    });
  } finally {
    anamnesesLoading.value = false;
  }
};

const saveAnamnese = async () => {
  if (!validateStep(steps.value.length - 1)) {
    currentStepIndex.value = steps.value.findIndex((step) => step.id === 'diagnostico');
    return;
  }
  saving.value = true;
  try {
    const payload = sanitizePayload(form);
    const response = form.id
      ? await AnamneseService.update(payload)
      : await AnamneseService.create(payload);
    const saved = response.data ?? payload;
    notifications.push({
      type: 'success',
      title: t('notifications.success.title'),
      message: t('notifications.success.message'),
    });
    selectedAnamnese.value = saved;
    hydrateFormFromAnamnese(saved);
    viewMode.value = 'summary';
    window.localStorage.removeItem(STORAGE_KEY);
    await loadAnamneses();
  } catch (error) {
    // handled globally
  } finally {
    saving.value = false;
  }
};

watch(form, saveDraft, { deep: true });
watch(currentStepIndex, () => {
  if (viewMode.value === 'form') {
    saveDraft();
  }
});

watch(
  () => auth.user?.id,
  (id) => {
    if (isClient.value) {
      form.userId = id ?? null;
    }
    if (id) {
      void loadAnamneses();
    }
  },
  { immediate: true },
);

watch(
  () => auth.user,
  (user) => {
    if (!user || isAdmin.value || selectedAnamnese.value) {
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
  { immediate: true },
);

onMounted(async () => {
  await loadAnamneses();
  if (!selectedAnamnese.value) {
    loadDraft();
  }
});
</script>
