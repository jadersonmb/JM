<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.clinical.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.clinical.description') }}</p>

    <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.clinical.fields.mucosa') }}</label>
        <input v-model="form.mucosa" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.clinical.fields.limbs') }}</label>
        <input v-model="form.membros" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.clinical.fields.surgeries') }}</label>
        <input v-model="form.cirurgias" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div class="md:col-span-2">
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.clinical.fields.notes') }}</label>
        <textarea v-model="form.avaliacaoObservacoes" rows="3"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
});

const form = props.form;
const { t } = useI18n();
</script>
