<template>
  <div class="space-y-6">
    <header class="space-y-2">
      <h3 class="text-lg font-semibold text-slate-800">{{ t('diet.wizard.steps.notes.title') }}</h3>
      <p class="text-sm text-slate-500">{{ t('diet.wizard.steps.notes.description') }}</p>
    </header>

    <label class="flex flex-col gap-2 text-sm font-semibold text-slate-600">
      <span class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.wizard.fields.notes') }}</span>
      <textarea
        rows="4"
        class="input min-h-[120px]"
        :readonly="disabled"
        :value="modelValue ?? ''"
        @input="onInput($event.target.value)"
      />
    </label>

    <section class="grid gap-3 sm:grid-cols-3">
      <div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
        <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.wizard.review.ownerLabel') }}</p>
        <p class="mt-1 text-sm font-semibold text-slate-700">{{ props.ownerName || t('common.placeholders.empty') }}</p>
      </div>
      <div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
        <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.wizard.review.patientLabel') }}</p>
        <p class="mt-1 text-sm font-semibold text-slate-700">{{ props.patientName || t('common.placeholders.empty') }}</p>
      </div>
      <div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
        <p class="text-xs uppercase tracking-wide text-slate-400">{{ t('diet.wizard.review.mealCountLabel') }}</p>
        <p class="mt-1 text-sm font-semibold text-slate-700">{{ mealCount }}</p>
      </div>
    </section>

    <section class="space-y-4">
      <h4 class="text-base font-semibold text-slate-800">{{ t('diet.wizard.review.title') }}</h4>
      <div class="grid gap-4 lg:grid-cols-2">
        <article
          v-for="(meal, index) in meals"
          :key="meal.id ?? index"
          class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
        >
          <header class="flex items-center justify-between">
            <div>
              <h5 class="text-sm font-semibold text-slate-800">{{ mealLabel(meal.mealType) }}</h5>
              <p class="text-xs uppercase tracking-wide text-slate-400">{{ formatTime(meal.scheduledTime) }}</p>
            </div>
          </header>
          <ul v-if="meal.items?.length" class="mt-4 space-y-2 text-sm text-slate-600">
            <li
              v-for="(item, itemIndex) in meal.items"
              :key="item.id ?? itemIndex"
              class="rounded-xl border border-slate-100 bg-slate-50 px-3 py-2"
            >
              <p class="font-semibold text-slate-700">
                {{ item.foodItemName || resolveFoodName(item.foodItemId) || t('common.placeholders.empty') }}
              </p>
              <p class="text-xs text-slate-500">
                {{ formatQuantity(item.quantity) }}
                <span v-if="item.unitDisplayName || resolveUnitName(item.unitId)">
                  {{ item.unitDisplayName || resolveUnitName(item.unitId) }}
                </span>
              </p>
              <p v-if="item.notes" class="text-xs text-slate-500">{{ item.notes }}</p>
            </li>
          </ul>
          <p v-else class="mt-4 text-sm text-slate-500">{{ t('diet.wizard.review.emptyItems') }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  modelValue: { type: String, default: '' },
  meals: { type: Array, default: () => [] },
  foods: { type: Array, default: () => [] },
  units: { type: Array, default: () => [] },
  patientName: { type: String, default: '' },
  ownerName: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();

const meals = computed(() => props.meals ?? []);
const mealCount = computed(() => meals.value.length ?? 0);

const onInput = (value) => {
  emit('update:modelValue', value);
};

const mealLabel = (type) => t(`diet.meal.${String(type || '').toLowerCase()}`);

const formatTime = (value) => {
  if (!value) return '--:--';
  if (typeof value === 'string' && value.length >= 5) {
    return value.slice(0, 5);
  }
  return value;
};

const formatQuantity = (value) => {
  if (!value && value !== 0) return '';
  return Number(value).toString();
};

const resolveFoodName = (id) => props.foods.find((food) => food.id === id)?.name ?? '';
const resolveUnitName = (id) => props.units.find((unit) => unit.id === id)?.displayName ?? '';
</script>
