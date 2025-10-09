<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h3 class="text-lg font-semibold text-slate-800">{{ t('diet.wizard.steps.items.title') }}</h3>
      <p class="text-sm text-slate-500 max-w-2xl">{{ t('diet.wizard.steps.items.description') }}</p>
    </div>

    <div v-if="!meals.length" class="rounded-2xl border border-dashed border-slate-300 p-6 text-center text-slate-500">
      {{ t('diet.validation.atLeastOneMeal') }}
    </div>

    <div v-else class="space-y-6">
      <section
        v-for="(meal, mealIndex) in meals"
        :key="meal.id ?? mealIndex"
        class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <header class="mb-4 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div>
            <h4 class="text-base font-semibold text-slate-800">
              {{ mealLabel(meal.mealType) }}
            </h4>
            <p class="text-xs uppercase tracking-wide text-slate-400">
              {{ formatTime(meal.scheduledTime) }}
            </p>
          </div>
          <button
            type="button"
            class="btn-secondary"
            :disabled="disabled"
            @click="addItem(mealIndex)"
          >
            {{ t('diet.wizard.items.addItem') }}
          </button>
        </header>

        <div v-if="!meal.items?.length" class="rounded-xl border border-dashed border-slate-200 p-4 text-center text-slate-500">
          {{ t('common.empty.meals') }}
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="(item, itemIndex) in meal.items"
            :key="item.id ?? itemIndex"
            class="rounded-xl border border-slate-200 p-4"
          >
            <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <label class="flex flex-col text-sm font-semibold text-slate-600">
                <span class="mb-1 text-xs uppercase tracking-wide text-slate-400">
                  {{ t('diet.wizard.items.foodLabel') }}
                </span>
                <select
                  class="input"
                  :disabled="disabled"
                  :value="item.foodItemId ?? ''"
                  @change="updateItem(mealIndex, itemIndex, { foodItemId: $event.target.value })"
                >
                  <option value="" disabled>{{ t('common.placeholders.select') }}</option>
                  <option v-for="food in foods" :key="food.id" :value="food.id">
                    {{ food.name }}
                  </option>
                </select>
              </label>

              <label class="flex flex-col text-sm font-semibold text-slate-600">
                <span class="mb-1 text-xs uppercase tracking-wide text-slate-400">
                  {{ t('diet.wizard.items.unitLabel') }}
                </span>
                <select
                  class="input"
                  :disabled="disabled"
                  :value="item.unitId ?? ''"
                  @change="updateItem(mealIndex, itemIndex, { unitId: $event.target.value })"
                >
                  <option value="" disabled>{{ t('common.placeholders.select') }}</option>
                  <option v-for="unit in units" :key="unit.id" :value="unit.id">
                    {{ unit.displayName }}
                  </option>
                </select>
              </label>

              <label class="flex flex-col text-sm font-semibold text-slate-600">
                <span class="mb-1 text-xs uppercase tracking-wide text-slate-400">
                  {{ t('diet.wizard.items.quantityLabel') }}
                </span>
                <input
                  type="number"
                  step="0.1"
                  min="0"
                  class="input"
                  :disabled="disabled"
                  :value="item.quantity ?? ''"
                  @change="updateItem(mealIndex, itemIndex, { quantity: parseFloat($event.target.value) || null })"
                />
              </label>

              <label class="flex flex-col text-sm font-semibold text-slate-600 sm:col-span-2 lg:col-span-1">
                <span class="mb-1 text-xs uppercase tracking-wide text-slate-400">
                  {{ t('diet.wizard.items.notesLabel') }}
                </span>
                <input
                  type="text"
                  class="input"
                  :disabled="disabled"
                  :value="item.notes ?? ''"
                  @input="updateItem(mealIndex, itemIndex, { notes: $event.target.value })"
                />
              </label>
            </div>
            <div class="mt-3 flex justify-end">
              <button
                type="button"
                class="btn-ghost text-red-500"
                :disabled="disabled"
                @click="removeItem(mealIndex, itemIndex)"
              >
                {{ t('common.actions.remove') }}
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  meals: { type: Array, default: () => [] },
  foods: { type: Array, default: () => [] },
  units: { type: Array, default: () => [] },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(['update:meals']);

const { t } = useI18n();

const meals = computed(() => props.meals ?? []);

const addItem = (mealIndex) => {
  if (props.disabled) return;
  const updated = meals.value.map((meal, index) => {
    if (index !== mealIndex) return meal;
    const items = Array.isArray(meal.items) ? [...meal.items] : [];
    items.push({
      id: crypto.randomUUID ? crypto.randomUUID() : Date.now(),
      foodItemId: '',
      unitId: '',
      quantity: null,
      notes: '',
    });
    return { ...meal, items };
  });
  emit('update:meals', updated);
};

const updateItem = (mealIndex, itemIndex, changes) => {
  const updated = meals.value.map((meal, mIndex) => {
    if (mIndex !== mealIndex) return meal;
    const items = meal.items?.map((item, idx) => {
      if (idx !== itemIndex) return item;
      const nextItem = { ...item, ...changes };
      if (changes.foodItemId) {
        const foundFood = props.foods.find((food) => food.id === changes.foodItemId);
        if (foundFood) {
          nextItem.foodItemName = foundFood.name;
        }
      }
      if (changes.unitId) {
        const foundUnit = props.units.find((unit) => unit.id === changes.unitId);
        if (foundUnit) {
          nextItem.unitDisplayName = foundUnit.displayName;
        }
      }
      return nextItem;
    }) ?? [];
    return { ...meal, items };
  });
  emit('update:meals', updated);
};

const removeItem = (mealIndex, itemIndex) => {
  const updated = meals.value.map((meal, mIndex) => {
    if (mIndex !== mealIndex) return meal;
    const items = meal.items?.filter((_, idx) => idx !== itemIndex) ?? [];
    return { ...meal, items };
  });
  emit('update:meals', updated);
};

const mealLabel = (type) => t(`diet.meal.${String(type || '').toLowerCase()}`);

const formatTime = (value) => {
  if (!value) return '--:--';
  if (typeof value === 'string' && value.length >= 5) {
    return value.slice(0, 5);
  }
  return value;
};
</script>
