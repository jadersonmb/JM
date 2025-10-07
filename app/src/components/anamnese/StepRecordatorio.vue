<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.recordatorio.title') }}</h2>
        <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.recordatorio.description') }}</p>
      </div>
      <button type="button" class="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500" @click="addRefeicao">
        {{ t('common.actions.addMeal') }}
      </button>
    </div>

    <div v-if="form.refeicoes24h.length" class="mt-6 space-y-4">
      <div v-for="(refeicao, index) in form.refeicoes24h" :key="refeicao.__key || refeicao.id || index" class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div>
            <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.recordatorio.fields.nome') }}</label>
            <input v-model="refeicao.nomeRefeicao" type="text"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
          </div>
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.recordatorio.fields.alimentos') }}</label>
            <textarea v-model="refeicao.alimentos" rows="2"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"></textarea>
          </div>
          <div class="md:col-span-3">
            <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.recordatorio.fields.quantidades') }}</label>
            <textarea v-model="refeicao.quantidades" rows="2"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"></textarea>
          </div>
        </div>
        <div class="mt-4 flex justify-end">
          <button type="button" class="text-sm font-semibold text-red-600 hover:text-red-500" @click="removeRefeicao(index)">
            {{ t('common.actions.remove') }}
          </button>
        </div>
      </div>
    </div>
    <div v-else class="mt-6 rounded-xl border border-dashed border-slate-300 bg-slate-50 p-6 text-sm text-slate-600">
      {{ t('common.empty.meals') }}
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
});

const form = props.form;
const { t } = useI18n();

const ensureArray = () => {
  if (!Array.isArray(form.refeicoes24h)) {
    form.refeicoes24h = [];
  }
};

onMounted(ensureArray);

const generateKey = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return Math.random().toString(36).slice(2);
};

const addRefeicao = () => {
  ensureArray();
  form.refeicoes24h.push({
    id: null,
    nomeRefeicao: '',
    alimentos: '',
    quantidades: '',
    __key: generateKey(),
  });
};

const removeRefeicao = (index) => {
  ensureArray();
  form.refeicoes24h.splice(index, 1);
};
</script>
