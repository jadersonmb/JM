<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.diagnostico.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.diagnostico.description') }}</p>

    <div class="mt-6 space-y-4">
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.diagnostico.fields.diagnostico') }} <span class="text-red-500">*</span></label>
        <textarea v-model="form.diagnostico" rows="4" required
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"></textarea>
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.diagnostico.fields.resumo') }}</label>
        <textarea v-model="form.resumoDieta" rows="5"
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
