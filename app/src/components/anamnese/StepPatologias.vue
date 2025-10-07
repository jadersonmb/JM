<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.pathologies.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.pathologies.description') }}</p>

    <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
      <label v-for="item in patologias" :key="item.key" class="flex items-center gap-3 rounded-xl border border-slate-200 p-3 text-sm font-medium text-slate-600 shadow-sm">
        <input v-model="form[item.key]" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
        <span>{{ item.label }}</span>
      </label>
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

const patologias = computed(() => [
  { key: 'colesterol', label: t('anamnese.steps.pathologies.items.colesterol') },
  { key: 'hipertensao', label: t('anamnese.steps.pathologies.items.hipertensao') },
  { key: 'diabetes1', label: t('anamnese.steps.pathologies.items.diabetes1') },
  { key: 'diabetes2', label: t('anamnese.steps.pathologies.items.diabetes2') },
  { key: 'trigliceridemia', label: t('anamnese.steps.pathologies.items.trigliceridemia') },
  { key: 'anemia', label: t('anamnese.steps.pathologies.items.anemia') },
  { key: 'intestinal', label: t('anamnese.steps.pathologies.items.intestinal') },
  { key: 'gastrica', label: t('anamnese.steps.pathologies.items.gastrica') },
  { key: 'renal', label: t('anamnese.steps.pathologies.items.renal') },
  { key: 'hepatica', label: t('anamnese.steps.pathologies.items.hepatica') },
  { key: 'vesicular', label: t('anamnese.steps.pathologies.items.vesicular') },
]);
</script>
