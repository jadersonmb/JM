<template>
  <div class="space-y-6">
    <section class="space-y-3">
      <h3 class="text-lg font-semibold text-slate-900">{{ t('goals.wizard.review.title') }}</h3>
      <div class="grid gap-4 rounded-3xl border border-slate-200 bg-white p-4 shadow-sm sm:grid-cols-2">
        <div class="space-y-1">
          <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.type') }}</p>
          <p class="text-sm font-semibold text-slate-700">{{ typeLabel }}</p>
        </div>
        <div class="space-y-1">
          <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.unit') }}</p>
          <p class="text-sm font-semibold text-slate-700">{{ unitDisplay }}</p>
        </div>
        <div class="space-y-1">
          <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.value') }}</p>
          <p class="text-sm font-semibold text-slate-700">{{ targetDisplay }}</p>
        </div>
        <div class="space-y-1">
          <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.review.period') }}</p>
          <p class="text-sm font-semibold text-slate-700">{{ periodicityLabel }}</p>
        </div>
        <div class="space-y-1">
          <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.review.duration') }}</p>
          <p class="text-sm font-semibold text-slate-700">{{ durationDisplay }}</p>
        </div>
        <div v-if="templateName" class="space-y-1">
          <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.review.template') }}</p>
          <p class="text-sm font-semibold text-slate-700">{{ templateName }}</p>
        </div>
        <div class="space-y-1">
          <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.review.owner') }}</p>
          <p class="text-sm font-semibold text-slate-700">{{ ownerName || t('common.placeholders.empty') }}</p>
        </div>
        <div class="space-y-1">
          <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.review.active') }}</p>
          <p class="text-sm font-semibold text-slate-700">
            {{ active ? t('common.boolean.yes') : t('common.boolean.no') }}
          </p>
        </div>
      </div>
    </section>

    <section class="space-y-3">
      <label class="flex items-center gap-3 text-sm font-semibold text-slate-600">
        <input
          type="checkbox"
          class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500"
          :checked="active"
          @change="emitActive($event.target.checked)"
        />
        <span>{{ t('goals.wizard.fields.active') }}</span>
      </label>
      <label class="flex flex-col gap-2 text-sm font-semibold text-slate-600">
        <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('goals.wizard.fields.notes') }}</span>
        <textarea
          rows="4"
          class="input"
          :value="notes ?? ''"
          @input="emitNotes($event.target.value)"
        />
      </label>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  goalType: {
    type: String,
    default: '',
  },
  unitName: {
    type: String,
    default: '',
  },
  unitSymbol: {
    type: String,
    default: '',
  },
  targetValue: {
    type: [Number, String],
    default: null,
  },
  targetMode: {
    type: String,
    default: 'ABSOLUTE',
  },
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
  ownerName: {
    type: String,
    default: '',
  },
  templateName: {
    type: String,
    default: '',
  },
  notes: {
    type: String,
    default: '',
  },
  active: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['update:notes', 'update:active']);
const { t } = useI18n();

const translateOr = (key, fallback) => {
  const translated = t(key);
  return translated === key ? fallback : translated;
};

const typeLabel = computed(() => translateOr(`goals.types.${props.goalType}`, props.goalType));
const unitDisplay = computed(() => {
  if (props.unitName) {
    return props.unitName;
  }
  if (props.unitSymbol) {
    return props.unitSymbol;
  }
  return t('common.placeholders.empty');
});
const targetDisplay = computed(() => {
  if (props.targetValue == null || props.targetValue === '') {
    return t('common.placeholders.empty');
  }
  const formatted = Number(props.targetValue).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 });
  const modeLabel = props.targetMode === 'PER_KG' ? t('goals.perKg') : t('goals.absolute');
  return `${formatted} (${modeLabel})`;
});
const periodicityLabel = computed(() => {
  const base = translateOr(`goals.periodicityOptions.${props.periodicity}`, props.periodicity);
  if (props.periodicity === 'CUSTOM' && props.customPeriodDays) {
    return `${base} (${props.customPeriodDays})`;
  }
  return base;
});
const durationDisplay = computed(() => {
  if (!props.startDate && !props.endDate) {
    return t('common.placeholders.empty');
  }
  if (props.startDate && props.endDate) {
    return `${props.startDate} → ${props.endDate}`;
  }
  return props.startDate || props.endDate || t('common.placeholders.empty');
});

const emitNotes = (value) => emit('update:notes', value);
const emitActive = (value) => emit('update:active', value);
</script>
