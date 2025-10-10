<template>
  <div class="space-y-6">
    <section class="grid gap-4 md:grid-cols-2">
      <label class="flex flex-col gap-2 text-sm font-semibold text-slate-600">
        <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.periodicity') }}</span>
        <select class="input" :value="periodicity" @change="emitPeriodicity($event.target.value)">
          <option v-for="option in options" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </label>
      <label v-if="periodicity === 'CUSTOM'" class="flex flex-col gap-2 text-sm font-semibold text-slate-600">
        <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.fields.customDays') }}</span>
        <input
          type="number"
          min="1"
          class="input"
          :value="customPeriodDays ?? ''"
          @input="emitCustomDays($event.target.value)"
        />
      </label>
    </section>

    <section class="grid gap-4 md:grid-cols-2">
      <label class="flex flex-col gap-2 text-sm font-semibold text-slate-600">
        <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.fields.startDate') }}</span>
        <input type="date" class="input" :value="startDate ?? ''" @input="emitStart($event.target.value)" />
      </label>
      <label class="flex flex-col gap-2 text-sm font-semibold text-slate-600">
        <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.fields.endDate') }}</span>
        <input type="date" class="input" :value="endDate ?? ''" @input="emitEnd($event.target.value)" />
      </label>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  periodicity: {
    type: String,
    default: 'DAILY',
  },
  customPeriodDays: {
    type: [Number, String],
    default: null,
  },
  startDate: {
    type: String,
    default: null,
  },
  endDate: {
    type: String,
    default: null,
  },
});

const emit = defineEmits(['update:periodicity', 'update:customPeriodDays', 'update:startDate', 'update:endDate']);
const { t } = useI18n();

const options = computed(() => [
  { value: 'DAILY', label: t('goals.periodicityOptions.DAILY') },
  { value: 'WEEKLY', label: t('goals.periodicityOptions.WEEKLY') },
  { value: 'MONTHLY', label: t('goals.periodicityOptions.MONTHLY') },
  { value: 'CUSTOM', label: t('goals.periodicityOptions.CUSTOM') },
]);

const emitPeriodicity = (value) => emit('update:periodicity', value);
const emitCustomDays = (value) => emit('update:customPeriodDays', value !== '' ? Number(value) : null);
const emitStart = (value) => emit('update:startDate', value || null);
const emitEnd = (value) => emit('update:endDate', value || null);
</script>
