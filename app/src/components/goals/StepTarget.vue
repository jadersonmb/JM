<template>
  <div class="space-y-6">
    <section class="grid gap-4 md:grid-cols-2">
      <label class="flex flex-col gap-2 text-sm font-semibold text-slate-600">
        <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.unit') }}</span>
        <select class="input" :value="unitId ?? ''" @change="emitUnit($event.target.value)">
          <option value="">{{ t('common.placeholders.select') }}</option>
          <option v-for="unit in units" :key="unit.id" :value="unit.id">
            {{ unit.displayName ?? unit.symbol ?? unit.code }}
          </option>
        </select>
      </label>
      <label class="flex flex-col gap-2 text-sm font-semibold text-slate-600">
        <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.value') }}</span>
        <input
          type="number"
          min="0"
          step="0.01"
          class="input"
          :value="targetValue ?? ''"
          @input="emitTarget($event.target.value)"
        />
      </label>
    </section>

    <section class="space-y-3">
      <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ t('goals.wizard.fields.mode') }}</p>
      <div class="inline-flex rounded-2xl border border-slate-200 bg-white p-1 shadow-sm">
        <button
          v-for="mode in modes"
          :key="mode.value"
          type="button"
          class="px-4 py-2 text-sm font-semibold transition"
          :class="mode.value === targetMode
            ? 'rounded-2xl bg-primary-500 text-white shadow'
            : 'rounded-2xl text-slate-500 hover:bg-primary-50 hover:text-primary-600'"
          @click="emitMode(mode.value)"
        >
          {{ mode.label }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  units: {
    type: Array,
    default: () => [],
  },
  unitId: {
    type: String,
    default: null,
  },
  targetValue: {
    type: [Number, String],
    default: null,
  },
  targetMode: {
    type: String,
    default: 'ABSOLUTE',
  },
});

const emit = defineEmits(['update:unitId', 'update:targetValue', 'update:targetMode']);
const { t } = useI18n();

const modes = computed(() => [
  { value: 'ABSOLUTE', label: t('goals.absolute') },
  { value: 'PER_KG', label: t('goals.perKg') },
]);

const emitUnit = (value) => emit('update:unitId', value || null);
const emitTarget = (value) => emit('update:targetValue', value !== '' ? Number(value) : null);
const emitMode = (value) => emit('update:targetMode', value);
</script>
