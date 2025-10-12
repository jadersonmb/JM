<template>
  <transition name="modal">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/60 px-4 py-10 sm:py-16">
      <div class="w-full max-w-2xl rounded-3xl bg-white shadow-2xl">
        <header class="flex items-start justify-between border-b border-slate-200 px-6 py-4">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">{{ title }}</h3>
            <p class="mt-1 text-sm text-slate-500">{{ subtitle }}</p>
            <p v-if="ownerLabel" class="mt-2 text-xs font-medium text-slate-500">
              {{ t('whatsappNutrition.modal.owner', { owner: ownerLabel }) }}
            </p>
          </div>
          <button
            type="button"
            class="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
            @click="close"
          >
            <XMarkIcon class="h-5 w-5" />
          </button>
        </header>

        <form class="grid gap-5 px-6 py-6" @submit.prevent="handleSubmit">
          <div class="grid gap-3 md:grid-cols-2">
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.receivedAt') }}
              </span>
              <input v-model="form.receivedAt" type="datetime-local" class="input" required />
            </label>
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.meal') }}
              </span>
              <select v-model="form.mealId" class="input">
                <option value="">{{ t('whatsappNutrition.modal.options.noMeal') }}</option>
                <option v-for="meal in mealOptions" :key="meal.value" :value="meal.value">
                  {{ meal.label }}
                </option>
              </select>
            </label>
          </div>

          <div class="grid gap-3 md:grid-cols-2">
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.food') }}
              </span>
              <select v-model="selectedFood" class="input">
                <option value="">{{ t('whatsappNutrition.modal.options.noFood') }}</option>
                <option v-for="option in foodOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
            <label v-if="useCustomFood" class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.customFood') }}
              </span>
              <input v-model="form.foodName" type="text" class="input" />
            </label>
          </div>

          <div class="grid gap-3 md:grid-cols-2">
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.calories') }}
              </span>
              <div class="grid gap-2 sm:grid-cols-[1fr,minmax(90px,auto)]">
                <input v-model="form.calories" type="number" step="0.01" min="0" class="input" />
                <select v-model="form.caloriesUnitId" class="input">
                  <option v-for="unit in energyUnits" :key="unit.id" :value="unit.id">
                    {{ unit.symbol ?? unit.code ?? unit.name }}
                  </option>
                </select>
              </div>
            </label>
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.liquidVolume') }}
              </span>
              <div class="grid gap-2 sm:grid-cols-[1fr,minmax(90px,auto)]">
                <input v-model="form.liquidVolume" type="number" step="0.01" min="0" class="input" />
                <select v-model="form.liquidUnitId" class="input">
                  <option v-for="unit in liquidUnits" :key="unit.id" :value="unit.id">
                    {{ unit.symbol ?? unit.code ?? unit.name }}
                  </option>
                </select>
              </div>
            </label>
          </div>

          <div class="grid gap-3 md:grid-cols-3">
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.protein') }}
              </span>
              <div class="grid gap-2 sm:grid-cols-[1fr,minmax(90px,auto)]">
                <input v-model="form.protein" type="number" step="0.01" min="0" class="input" />
                <select v-model="form.proteinUnitId" class="input">
                  <option v-for="unit in macroUnits" :key="unit.id" :value="unit.id">
                    {{ unit.symbol ?? unit.code ?? unit.name }}
                  </option>
                </select>
              </div>
            </label>
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.carbs') }}
              </span>
              <div class="grid gap-2 sm:grid-cols-[1fr,minmax(90px,auto)]">
                <input v-model="form.carbs" type="number" step="0.01" min="0" class="input" />
                <select v-model="form.carbsUnitId" class="input">
                  <option v-for="unit in macroUnits" :key="unit.id" :value="unit.id">
                    {{ unit.symbol ?? unit.code ?? unit.name }}
                  </option>
                </select>
              </div>
            </label>
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.fat') }}
              </span>
              <div class="grid gap-2 sm:grid-cols-[1fr,minmax(90px,auto)]">
                <input v-model="form.fat" type="number" step="0.01" min="0" class="input" />
                <select v-model="form.fatUnitId" class="input">
                  <option v-for="unit in macroUnits" :key="unit.id" :value="unit.id">
                    {{ unit.symbol ?? unit.code ?? unit.name }}
                  </option>
                </select>
              </div>
            </label>
          </div>

          <label class="flex flex-col gap-1">
            <span class="text-sm font-medium text-slate-700">
              {{ t('whatsappNutrition.modal.fields.summary') }}
            </span>
            <textarea v-model="form.summary" rows="3" class="input min-h-[96px] resize-y"></textarea>
          </label>

          <label class="flex flex-col gap-1">
            <span class="text-sm font-medium text-slate-700">
              {{ t('whatsappNutrition.modal.fields.textContent') }}
            </span>
            <textarea v-model="form.textContent" rows="3" class="input min-h-[96px] resize-y"></textarea>
          </label>

          <div class="space-y-3">
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-700">
                {{ t('whatsappNutrition.modal.fields.imageUrl') }}
              </span>
              <input v-model="form.imageUrl" type="url" class="input" placeholder="https://..." />
            </label>
            <div class="flex items-start gap-4">
              <div v-if="preview" class="h-20 w-20 overflow-hidden rounded-xl border border-slate-200">
                <img :src="preview" alt="preview" class="h-full w-full object-cover" />
              </div>
              <div class="flex flex-col gap-2">
                <input ref="fileInput" type="file" class="hidden" accept="image/*" @change="handleFile" />
                <button type="button" class="btn-secondary" @click="triggerFile">
                  {{ t('whatsappNutrition.modal.actions.upload') }}
                </button>
                <button
                  v-if="preview"
                  type="button"
                  class="btn-ghost text-red-500"
                  @click="removeFile"
                >
                  {{ t('whatsappNutrition.modal.actions.removeImage') }}
                </button>
              </div>
            </div>
          </div>

          <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
            <button type="button" class="btn-secondary" @click="close">
              {{ t('common.actions.cancel') }}
            </button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              <span v-if="submitting" class="flex items-center gap-2">
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
                {{ t('common.actions.saving') }}
              </span>
              <span v-else>{{ submitLabel }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { XMarkIcon } from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';

const CUSTOM_FOOD_VALUE = '__custom__';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'create' },
  entry: { type: Object, default: () => ({}) },
  energyUnits: { type: Array, default: () => [] },
  macroUnits: { type: Array, default: () => [] },
  liquidUnits: { type: Array, default: () => [] },
  meals: { type: Array, default: () => [] },
  foods: { type: Array, default: () => [] },
  ownerLabel: { type: String, default: '' },
  submitting: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue', 'save']);
const { t } = useI18n();

const form = reactive({
  ownerUserId: '',
  receivedAt: '',
  mealId: '',
  foodId: '',
  foodName: '',
  textContent: '',
  imageUrl: '',
  imageFile: null,
  calories: '',
  caloriesUnitId: '',
  protein: '',
  proteinUnitId: '',
  carbs: '',
  carbsUnitId: '',
  fat: '',
  fatUnitId: '',
  liquidVolume: '',
  liquidUnitId: '',
  summary: '',
});

const preview = ref('');
const useCustomFood = ref(false);
const fileInput = ref(null);

const defaultEnergyUnitId = computed(() => {
  const byCode = props.energyUnits.find((unit) => (unit.code ?? '').toUpperCase() === 'KCAL');
  return byCode?.id ?? props.energyUnits[0]?.id ?? '';
});

const defaultMacroUnitId = computed(() => {
  const byCode = props.macroUnits.find((unit) => (unit.code ?? '').toUpperCase() === 'G');
  return byCode?.id ?? props.macroUnits[0]?.id ?? '';
});

const defaultLiquidUnitId = computed(() => {
  const byCode = props.liquidUnits.find((unit) => (unit.code ?? '').toUpperCase() === 'ML');
  return byCode?.id ?? props.liquidUnits[0]?.id ?? '';
});

const mealOptions = computed(() =>
  props.meals.map((meal) => ({ value: meal.id, label: meal.name ?? meal.code ?? meal.id })),
);

const foodOptions = computed(() => {
  const base = props.foods.map((food) => ({ value: food.id, label: food.name ?? food.code ?? food.id }));
  return [
    ...base,
    { value: CUSTOM_FOOD_VALUE, label: t('whatsappNutrition.modal.options.customFood') },
  ];
});

const selectedFood = computed({
  get: () => (useCustomFood.value ? CUSTOM_FOOD_VALUE : form.foodId || ''),
  set: (value) => {
    if (value === CUSTOM_FOOD_VALUE) {
      useCustomFood.value = true;
      form.foodId = '';
    } else {
      useCustomFood.value = false;
      form.foodId = value || '';
      if (value) {
        const match = props.foods.find((food) => food.id === value);
        form.foodName = match?.name ?? form.foodName;
      }
    }
  },
});

const title = computed(() =>
  props.mode === 'edit'
    ? t('whatsappNutrition.modal.title.edit')
    : t('whatsappNutrition.modal.title.create'),
);

const subtitle = computed(() =>
  props.mode === 'edit'
    ? t('whatsappNutrition.modal.subtitle.edit')
    : t('whatsappNutrition.modal.subtitle.create'),
);

const submitLabel = computed(() =>
  props.mode === 'edit'
    ? t('whatsappNutrition.modal.actions.update')
    : t('whatsappNutrition.modal.actions.create'),
);

const resetForm = () => {
  form.ownerUserId = props.entry?.ownerUserId ?? '';
  form.receivedAt = props.entry?.receivedAt ?? '';
  form.mealId = props.entry?.mealId ?? '';
  form.foodId = props.entry?.foodId ?? '';
  form.foodName = props.entry?.foodName ?? '';
  form.textContent = props.entry?.textContent ?? '';
  form.imageUrl = props.entry?.imageUrl ?? '';
  form.imageFile = null;
  form.calories = props.entry?.calories ?? '';
  form.caloriesUnitId = props.entry?.caloriesUnitId ?? defaultEnergyUnitId.value;
  form.protein = props.entry?.protein ?? '';
  form.proteinUnitId = props.entry?.proteinUnitId ?? defaultMacroUnitId.value;
  form.carbs = props.entry?.carbs ?? '';
  form.carbsUnitId = props.entry?.carbsUnitId ?? defaultMacroUnitId.value;
  form.fat = props.entry?.fat ?? '';
  form.fatUnitId = props.entry?.fatUnitId ?? defaultMacroUnitId.value;
  form.liquidVolume = props.entry?.liquidVolume ?? '';
  form.liquidUnitId = props.entry?.liquidUnitId ?? defaultLiquidUnitId.value;
  form.summary = props.entry?.summary ?? '';
  preview.value = form.imageUrl ?? '';
  useCustomFood.value = !form.foodId && !!form.foodName;
};

watch(
  () => props.entry,
  (value) => {
    if (value) {
      resetForm();
    } else {
      Object.assign(form, {
        ownerUserId: '',
        receivedAt: '',
        mealId: '',
        foodId: '',
        foodName: '',
        textContent: '',
        imageUrl: '',
        imageFile: null,
        calories: '',
        caloriesUnitId: defaultEnergyUnitId.value,
        protein: '',
        proteinUnitId: defaultMacroUnitId.value,
        carbs: '',
        carbsUnitId: defaultMacroUnitId.value,
        fat: '',
        fatUnitId: defaultMacroUnitId.value,
        liquidVolume: '',
        liquidUnitId: defaultLiquidUnitId.value,
        summary: '',
      });
      preview.value = '';
      useCustomFood.value = false;
    }
  },
  { immediate: true },
);

watch(
  () => props.modelValue,
  (open) => {
    if (!open) {
      form.imageFile = null;
      preview.value = '';
      useCustomFood.value = false;
    }
  },
);

watch(
  () => form.imageUrl,
  (value) => {
    if (!form.imageFile) {
      preview.value = value || '';
    }
  },
);

const close = () => {
  emit('update:modelValue', false);
};

const handleSubmit = () => {
  const payload = {
    ...form,
    mealId: form.mealId || null,
    foodId: useCustomFood.value ? null : form.foodId || null,
    foodName: form.foodName?.trim() || null,
  };
  emit('save', payload);
};

const triggerFile = () => {
  fileInput.value?.click();
};

const handleFile = (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  form.imageFile = file;
  const reader = new FileReader();
  reader.onload = () => {
    preview.value = reader.result;
  };
  reader.readAsDataURL(file);
};

const removeFile = () => {
  form.imageFile = null;
  preview.value = form.imageUrl ?? '';
};
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
