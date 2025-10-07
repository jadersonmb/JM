<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.bioimpedance.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.bioimpedance.description') }}</p>

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
  { key: 'peso', label: t('anamnese.steps.bioimpedance.fields.peso'), type: 'number', step: '0.1' },
  { key: 'estatura', label: t('anamnese.steps.bioimpedance.fields.estatura'), type: 'number', step: '0.1' },
  { key: 'imc', label: t('anamnese.steps.bioimpedance.fields.imc'), type: 'number', step: '0.1' },
  { key: 'gorduraPercent', label: t('anamnese.steps.bioimpedance.fields.gorduraPercent'), type: 'number', step: '0.1' },
  { key: 'musculoPercent', label: t('anamnese.steps.bioimpedance.fields.musculoPercent'), type: 'number', step: '0.1' },
  { key: 'tmb', label: t('anamnese.steps.bioimpedance.fields.tmb'), type: 'number', step: '1' },
  { key: 'circunAbdomen', label: t('anamnese.steps.bioimpedance.fields.circunAbdomen'), type: 'number', step: '0.1' },
  { key: 'circunCintura', label: t('anamnese.steps.bioimpedance.fields.circunCintura'), type: 'number', step: '0.1' },
  { key: 'circunQuadril', label: t('anamnese.steps.bioimpedance.fields.circunQuadril'), type: 'number', step: '0.1' },
  { key: 'circunBraco', label: t('anamnese.steps.bioimpedance.fields.circunBraco'), type: 'number', step: '0.1' },
  { key: 'circunJoelho', label: t('anamnese.steps.bioimpedance.fields.circunJoelho'), type: 'number', step: '0.1' },
  { key: 'circunTorax', label: t('anamnese.steps.bioimpedance.fields.circunTorax'), type: 'number', step: '0.1' },
]);
</script>
