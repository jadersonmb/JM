<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnesis.steps.bodyComposition.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnesis.steps.bodyComposition.description') }}</p>

    <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
      <div v-for="field in campos" :key="field.key">
        <label class="block text-sm font-medium text-slate-700">{{ field.label }}</label>
        <input v-model.number="form[field.key]" :type="field.type" :step="field.step"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
});

const form = props.form;

const { t } = useI18n();

const campos = computed(() => [
  { key: 'weightKg', label: t('anamnesis.steps.bodyComposition.fields.weightKg'), type: 'number', step: '0.1' },
  { key: 'heightCm', label: t('anamnesis.steps.bodyComposition.fields.heightCm'), type: 'number', step: '0.1' },
  { key: 'bodyMassIndex', label: t('anamnesis.steps.bodyComposition.fields.bodyMassIndex'), type: 'number', step: '0.1' },
  { key: 'bodyFatPercentage', label: t('anamnesis.steps.bodyComposition.fields.bodyFatPercentage'), type: 'number', step: '0.1' },
  { key: 'muscleMassPercentage', label: t('anamnesis.steps.bodyComposition.fields.muscleMassPercentage'), type: 'number', step: '0.1' },
  { key: 'basalMetabolicRate', label: t('anamnesis.steps.bodyComposition.fields.basalMetabolicRate'), type: 'number', step: '1' },
  { key: 'abdominalCircumference', label: t('anamnesis.steps.bodyComposition.fields.abdominalCircumference'), type: 'number', step: '0.1' },
  { key: 'waistCircumference', label: t('anamnesis.steps.bodyComposition.fields.waistCircumference'), type: 'number', step: '0.1' },
  { key: 'hipCircumference', label: t('anamnesis.steps.bodyComposition.fields.hipCircumference'), type: 'number', step: '0.1' },
  { key: 'armCircumference', label: t('anamnesis.steps.bodyComposition.fields.armCircumference'), type: 'number', step: '0.1' },
  { key: 'kneeCircumference', label: t('anamnesis.steps.bodyComposition.fields.kneeCircumference'), type: 'number', step: '0.1' },
  { key: 'thoraxCircumference', label: t('anamnesis.steps.bodyComposition.fields.thoraxCircumference'), type: 'number', step: '0.1' },
]);
</script>
