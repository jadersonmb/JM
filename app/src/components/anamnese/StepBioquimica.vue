<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-100">{{ t('anamnese.steps.biochemistry.title') }}</h2>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ t('anamnese.steps.biochemistry.description') }}</p>
      </div>
      <button type="button" class="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500" @click="addExame">
        {{ t('common.actions.addExam') }}
      </button>
    </div>

    <div v-if="form.examesBioquimicos.length" class="mt-6 space-y-4">
      <div v-for="(exame, index) in form.examesBioquimicos" :key="exame.__key || exame.id || index" class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-4">
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-slate-700 dark:text-slate-200">{{ t('anamnese.steps.biochemistry.fields.name') }} <span class="text-red-500">*</span></label>
            <input v-model="exame.nomeExame" type="text" required :list="`biochemical-exams-${index}`"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:focus:border-primary-500 dark:focus:ring-primary-500" />
            <datalist :id="`biochemical-exams-${index}`">
              <option v-for="option in biochemicalExams" :key="option.id ?? option.code ?? option.name" :value="option.name">
                {{ option.code ? `${option.name} (${option.code})` : option.name }}
              </option>
            </datalist>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 dark:text-slate-200">{{ t('anamnese.steps.biochemistry.fields.value') }}</label>
            <input v-model="exame.valor" type="text"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:focus:border-primary-500 dark:focus:ring-primary-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 dark:text-slate-200">{{ t('anamnese.steps.biochemistry.fields.date') }}</label>
            <input v-model="exame.dataExame" type="date"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:focus:border-primary-500 dark:focus:ring-primary-500" />
          </div>
        </div>
        <div class="mt-4 flex justify-end">
          <button type="button" class="text-sm font-semibold text-red-600 hover:text-red-500" @click="removeExame(index)">
            {{ t('common.actions.remove') }}
          </button>
        </div>
      </div>
    </div>
    <div v-else class="mt-6 rounded-xl border border-dashed border-slate-300 bg-slate-50 p-6 text-sm text-slate-600 dark:border-slate-700 dark:bg-slate-900/60 dark:text-slate-400">
      {{ t('common.empty.exams') }}
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
  biochemicalExams: {
    type: Array,
    default: () => [],
  },
});

const form = props.form;
const biochemicalExams = computed(() => props.biochemicalExams ?? []);
const { t } = useI18n();

const ensureArray = () => {
  if (!Array.isArray(form.examesBioquimicos)) {
    form.examesBioquimicos = [];
  }
};

onMounted(ensureArray);

const generateKey = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return Math.random().toString(36).slice(2);
};

const addExame = () => {
  ensureArray();
  form.examesBioquimicos.push({
    id: null,
    nomeExame: '',
    valor: '',
    dataExame: '',
    __key: generateKey(),
  });
};

const removeExame = (index) => {
  ensureArray();
  form.examesBioquimicos.splice(index, 1);
};
</script>
