<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnesis.steps.pathologies.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnesis.steps.pathologies.description') }}</p>

    <div
      v-if="!pathologies.length"
      class="mt-6 rounded-xl border border-dashed border-slate-300 bg-slate-50 p-6 text-sm text-slate-500"
    >
      {{ t('anamnesis.steps.pathologies.empty') }}
    </div>

    <div v-else class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
      <label
        v-for="item in pathologies"
        :key="item.id"
        class="flex items-start gap-3 rounded-xl border border-slate-200 p-4 text-sm text-slate-700 shadow-sm transition hover:border-primary-200 hover:bg-primary-50/30"
      >
        <input
          v-model="modelValue"
          :value="item.id"
          type="checkbox"
          class="mt-1 h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500"
        />
        <div class="flex flex-col">
          <span class="font-semibold text-slate-800">{{ item.name }}</span>
          <span v-if="item.category" class="text-xs uppercase tracking-wide text-slate-400">{{ item.category }}</span>
          <p v-if="item.description" class="mt-1 text-xs text-slate-500">{{ item.description }}</p>
        </div>
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
  pathologies: {
    type: Array,
    default: () => [],
  },
});

const { t } = useI18n();

const pathologies = computed(() => props.pathologies ?? []);

const modelValue = computed({
  get: () => props.form.pathologyIds ?? [],
  set: (value) => {
    props.form.pathologyIds = Array.isArray(value) ? value : [];
  },
});
</script>
