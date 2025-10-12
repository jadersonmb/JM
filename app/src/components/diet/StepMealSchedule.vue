<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h3 class="text-lg font-semibold text-slate-800">{{ t('diet.wizard.steps.schedule.title') }}</h3>
      <button
        type="button"
        class="btn-secondary"
        :disabled="disabled"
        @click="addMeal"
      >
        {{ t('common.actions.addMeal') }}
      </button>
    </div>

    <div v-if="!meals.length" class="rounded-2xl border border-dashed border-slate-300 p-6 text-center text-slate-500">
      {{ t('diet.validation.atLeastOneMeal') }}
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="(meal, index) in meals"
        :key="meal.id ?? index"
        class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div class="flex flex-col gap-4 md:flex-row md:items-center md:gap-6">
            <label class="flex flex-col text-sm font-medium text-slate-600">
              <span class="mb-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
                {{ t('diet.list.filters.mealType') }}
              </span>
              <select
                class="input w-40"
                :disabled="disabled"
                :value="meal.mealType"
                @change="updateMeal(index, { mealType: $event.target.value })"
              >
                <option v-for="option in mealTypeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
            <label class="flex flex-col text-sm font-medium text-slate-600">
              <span class="mb-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
                {{ t('diet.wizard.schedule.timeLabel') }}
              </span>
              <input
                type="time"
                class="input w-32"
                :disabled="disabled"
                :value="meal.scheduledTime"
                @change="updateMeal(index, { scheduledTime: $event.target.value })"
              />
            </label>
          </div>
          <button
            v-if="!disabled"
            type="button"
            class="btn-ghost text-red-500"
            @click="removeMeal(index)"
          >
            {{ t('common.actions.delete') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  mealTypeOptions: { type: Array, default: () => [] },
  disabled: { type: Boolean, default: false },
  createMeal: {
    type: Function,
    default: () => ({ mealType: 'BREAKFAST', scheduledTime: '07:30', items: [] }),
  },
});

const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();

const meals = computed(() => props.modelValue ?? []);

const addMeal = () => {
  if (props.disabled) return;
  const template = props.createMeal();
  const mealType = template.mealType ?? props.mealTypeOptions[0]?.value ?? 'BREAKFAST';
  const scheduledTime = template.scheduledTime ?? '07:30';
  const newMeal = {
    id: crypto.randomUUID ? crypto.randomUUID() : Date.now(),
    mealType,
    scheduledTime,
    items: Array.isArray(template.items) ? [...template.items] : [],
  };
  emit('update:modelValue', [...meals.value, newMeal]);
};

const updateMeal = (index, changes) => {
  const updated = meals.value.map((meal, idx) => (idx === index ? { ...meal, ...changes } : meal));
  emit('update:modelValue', updated);
};

const removeMeal = (index) => {
  const updated = meals.value.filter((_, idx) => idx !== index);
  emit('update:modelValue', updated);
};
</script>
