<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.pathologies.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.pathologies.description') }}</p>

    <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
      <label v-for="item in pathologies" :key="item.id" class="flex items-center gap-3 rounded-xl border border-slate-200 p-3 text-sm font-medium text-slate-600 shadow-sm">
        <input type="checkbox" class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
        <span>{{ item.name }}</span>
      </label>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
  pathologies: {
    type: Array,
    default: () => [],
  },
});

const form = props.form;
const { t, locale  } = useI18n();
const pathologies = computed(() => props.pathologies ?? []);
</script>
