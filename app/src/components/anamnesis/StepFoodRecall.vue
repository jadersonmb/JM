<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnesis.steps.foodRecall.title') }}</h2>
        <p class="mt-1 text-sm text-slate-500">{{ t('anamnesis.steps.foodRecall.description') }}</p>
      </div>
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500"
        @click="addMeal"
      >
        {{ t('common.actions.addMeal') }}
      </button>
    </div>

    <div v-if="meals.length" class="mt-6 space-y-4">
      <div
        v-for="(meal, mealIndex) in meals"
        :key="meal.__key || meal.id || mealIndex"
        class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div class="grid grid-cols-1 gap-4 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto]">
          <div>
            <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.foodRecall.fields.mealName') }}</label>
            <input
              v-model="meal.mealName"
              type="text"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.foodRecall.fields.observation') }}</label>
            <input
              v-model="meal.observation"
              type="text"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
            />
          </div>
          <div class="flex items-end justify-end">
            <button
              type="button"
              class="text-sm font-semibold text-red-600 hover:text-red-500"
              @click="removeMeal(mealIndex)"
            >
              {{ t('common.actions.remove') }}
            </button>
          </div>
        </div>

        <div class="mt-4 space-y-3">
          <div
            v-for="(item, itemIndex) in meal.items"
            :key="item.__key || item.id || itemIndex"
            class="grid grid-cols-1 gap-3 md:grid-cols-[minmax(0,1.2fr)_minmax(0,0.7fr)_minmax(0,0.5fr)_auto]"
          >
            <div>
              <label class="block text-sm font-medium text-slate-700">
                {{ t('anamnesis.steps.foodRecall.fields.food') }}
              </label>
              <select
                v-model="item.foodId"
                class="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
                @change="handleFoodChange(item)"
              >
                <option value="">{{ t('common.placeholders.select') }}</option>
                <option v-for="food in foods" :key="food.id" :value="food.id">
                  {{ food.name }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700">
                {{ t('anamnesis.steps.foodRecall.fields.unit') }}
              </label>
              <select
                v-model="item.measurementUnitId"
                class="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
                @change="handleUnitChange(item)"
              >
                <option value="">{{ t('common.placeholders.select') }}</option>
                <option v-for="unit in measurementUnits" :key="unit.id" :value="unit.id">
                  {{ unit.description || unit.symbol || unit.code }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700">
                {{ t('anamnesis.steps.foodRecall.fields.quantity') }}
              </label>
              <input
                v-model.number="item.quantity"
                type="number"
                min="0"
                step="0.1"
                class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
              />
            </div>
            <div class="flex items-end justify-end">
              <button
                type="button"
                class="text-sm font-semibold text-red-600 hover:text-red-500"
                @click="removeItem(meal, itemIndex)"
              >
                {{ t('common.actions.remove') }}
              </button>
            </div>
          </div>
        </div>
        <div class="mt-4">
          <button
            type="button"
            class="btn-secondary"
            @click="addItem(meal)"
          >
            {{ t('anamnesis.steps.foodRecall.fields.addItem') }}
          </button>
        </div>
      </div>
    </div>
    <div v-else class="mt-6 rounded-xl border border-dashed border-slate-300 bg-slate-50 p-6 text-sm text-slate-600">
      {{ t('common.empty.meals') }}
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
  foods: {
    type: Array,
    default: () => [],
  },
  measurementUnits: {
    type: Array,
    default: () => [],
  },
});

const form = props.form;
const foods = computed(() => props.foods ?? []);
const units = computed(() => props.measurementUnits ?? []);
const foodMap = computed(() => new Map((foods.value ?? []).map((food) => [food.id, food])));
const unitMap = computed(() => new Map((units.value ?? []).map((unit) => [unit.id, unit])));
const { t } = useI18n();

const ensureArray = () => {
  if (!Array.isArray(form.foodRecalls)) {
    form.foodRecalls = [];
  }
};

onMounted(ensureArray);

const meals = computed(() => form.foodRecalls ?? []);

const generateKey = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return Math.random().toString(36).slice(2);
};

const addMeal = () => {
  ensureArray();
  form.foodRecalls.push({
    id: null,
    mealName: '',
    observation: '',
    items: [],
    __key: generateKey(),
  });
};

const removeMeal = (index) => {
  ensureArray();
  form.foodRecalls.splice(index, 1);
};

const ensureItemsArray = (meal) => {
  if (!Array.isArray(meal.items)) {
    meal.items = [];
  }
};

const addItem = (meal) => {
  ensureItemsArray(meal);
  meal.items.push({
    id: null,
    foodId: '',
    foodName: '',
    measurementUnitId: '',
    measurementUnitSymbol: '',
    quantity: null,
    __key: generateKey(),
  });
};

const removeItem = (meal, index) => {
  ensureItemsArray(meal);
  meal.items.splice(index, 1);
};

const handleFoodChange = (item) => {
  const food = foodMap.value.get(item.foodId) ?? null;
  item.foodName = food?.name ?? '';
};

const handleUnitChange = (item) => {
  const unit = unitMap.value.get(item.measurementUnitId) ?? null;
  item.measurementUnitSymbol = unit?.symbol || unit?.code || unit?.description || '';
};
</script>
