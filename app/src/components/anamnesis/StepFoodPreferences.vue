<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnesis.steps.preferences.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnesis.steps.preferences.description') }}</p>

    <div class="mt-6 space-y-6">
      <div
        v-for="section in sections"
        :key="section.type"
        class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-sm font-semibold text-slate-800">
              {{ t(`anamnesis.preferences.labels.${section.type}`) }}
            </h3>
            <p class="text-xs text-slate-500">
              {{ t(`anamnesis.preferences.descriptions.${section.type}`) }}
            </p>
          </div>
          <button
            type="button"
            class="btn-secondary"
            @click="addPreference(section.type)"
          >
            {{ t('anamnesis.preferences.actions.add') }}
          </button>
        </div>

        <div v-if="preferencesByType(section.type).length" class="mt-4 space-y-4">
          <div
            v-for="(item, index) in preferencesByType(section.type)"
            :key="item.__key || item.id || `${section.type}-${index}`"
            class="rounded-xl border border-slate-200 p-4"
          >
            <div class="grid grid-cols-1 gap-4 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto]">
              <div>
                <label class="block text-sm font-medium text-slate-700">
                  {{ t('anamnesis.preferences.fields.food') }}
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
                  {{ t('anamnesis.preferences.fields.notes') }}
                </label>
                <input
                  v-model="item.notes"
                  type="text"
                  class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
                />
              </div>
              <div class="flex items-end justify-end">
                <button
                  type="button"
                  class="text-sm font-semibold text-red-600 hover:text-red-500"
                  @click="removePreference(item)"
                >
                  {{ t('common.actions.remove') }}
                </button>
              </div>
            </div>
          </div>
        </div>
        <p v-else class="mt-4 text-sm text-slate-500">
          {{ t('anamnesis.preferences.empty') }}
        </p>
      </div>
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
});

const form = props.form;
const { t } = useI18n();

const foods = computed(() => props.foods ?? []);
const foodMap = computed(() => new Map((foods.value ?? []).map((food) => [food.id, food])));

const ensureArray = () => {
  if (!Array.isArray(form.foodPreferences)) {
    form.foodPreferences = [];
  }
};

onMounted(ensureArray);

const sections = [
  { type: 'PREFERRED' },
  { type: 'DISLIKED' },
];

const preferencesByType = (type) =>
  (form.foodPreferences ?? []).filter((item) => (item.preferenceType ?? 'PREFERRED') === type);

const generateKey = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return Math.random().toString(36).slice(2);
};

const addPreference = (type) => {
  ensureArray();
  form.foodPreferences.push({
    id: null,
    foodId: '',
    foodName: '',
    preferenceType: type,
    notes: '',
    __key: generateKey(),
  });
};

const removePreference = (item) => {
  ensureArray();
  const index = form.foodPreferences.indexOf(item);
  if (index >= 0) {
    form.foodPreferences.splice(index, 1);
  }
};

const handleFoodChange = (item) => {
  const match = foodMap.value.get(item.foodId) ?? null;
  item.foodName = match?.name ?? '';
};
</script>
