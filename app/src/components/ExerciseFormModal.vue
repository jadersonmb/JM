<template>
  <transition name="modal">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/60 px-4 py-10 sm:py-16"
    >
      <div class="w-full max-w-2xl rounded-3xl bg-white shadow-2xl">
        <header class="flex items-start justify-between border-b border-slate-200 px-6 py-4">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">
              {{ isEdit ? t('exercises.form.editTitle') : t('exercises.form.createTitle') }}
            </h3>
            <p class="mt-1 text-sm text-slate-500">
              {{ t('exercises.form.subtitle') }}
            </p>
          </div>
          <button type="button" class="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
            @click="close"
          >
            <XMarkIcon class="h-5 w-5" />
          </button>
        </header>

        <form class="grid gap-6 px-6 py-6" @submit.prevent="handleSubmit">
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">
                {{ t('exercises.form.reference') }}
              </label>
              <select v-model="form.referenceId" class="input">
                <option value="">{{ t('exercises.form.referencePlaceholder') }}</option>
                <option v-for="reference in references" :key="reference.id" :value="reference.id">
                  {{ reference.name }}
                </option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">
                {{ t('exercises.form.customName') }}
              </label>
              <input v-model="form.customName" type="text" class="input"
                :placeholder="t('exercises.form.customNamePlaceholder')"
              />
            </div>
          </div>

          <div v-if="isAdmin" class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">
                {{ t('exercises.form.user') }}
              </label>
              <select v-model="form.userId" class="input">
                <option value="">{{ t('exercises.form.userPlaceholder') }}</option>
                <option v-for="user in users" :key="user.id" :value="user.id">
                  {{ formatUser(user) }}
                </option>
              </select>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-3">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">
                {{ t('exercises.form.duration') }}
              </label>
              <input v-model.number="form.durationMinutes" type="number" min="0" class="input"
                :placeholder="t('exercises.form.durationPlaceholder')"
              />
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">
                {{ t('exercises.form.intensity') }}
              </label>
              <select v-model="form.intensity" class="input">
                <option value="">{{ t('exercises.form.intensityPlaceholder') }}</option>
                <option v-for="option in intensityOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">
                {{ t('exercises.form.calories') }}
              </label>
              <input v-model.number="form.caloriesBurned" type="number" min="0" class="input"
                :placeholder="t('exercises.form.caloriesPlaceholder')"
              />
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">
                {{ t('exercises.form.equipment') }}
              </label>
              <input v-model="form.equipment" type="text" class="input"
                :placeholder="t('exercises.form.equipmentPlaceholder')"
              />
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">
                {{ t('exercises.form.muscleGroup') }}
              </label>
              <input v-model="form.muscleGroup" type="text" class="input"
                :placeholder="t('exercises.form.muscleGroupPlaceholder')"
              />
            </div>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">
              {{ t('exercises.form.notes') }}
            </label>
            <textarea v-model="form.notes" rows="3" class="input"
              :placeholder="t('exercises.form.notesPlaceholder')"
            />
          </div>

          <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

          <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
            <button type="button" class="btn-secondary" @click="close">
              {{ t('common.actions.cancel') }}
            </button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              <span v-if="submitting" class="flex items-center gap-2">
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
                {{ t('common.actions.saving') }}
              </span>
              <span v-else>{{ isEdit ? t('common.actions.update') : t('common.actions.create') }}</span>
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

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  exercise: { type: Object, default: null },
  references: { type: Array, default: () => [] },
  users: { type: Array, default: () => [] },
  isAdmin: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  intensities: {
    type: Array,
    default: () => [
      { value: 'LOW', label: 'Low' },
      { value: 'MODERATE', label: 'Moderate' },
      { value: 'HIGH', label: 'High' },
      { value: 'EXTREME', label: 'Extreme' },
    ],
  },
});

const emit = defineEmits(['update:modelValue', 'submit']);

const { t } = useI18n();

const initialState = () => ({
  id: null,
  referenceId: '',
  userId: '',
  customName: '',
  durationMinutes: null,
  intensity: '',
  caloriesBurned: null,
  equipment: '',
  muscleGroup: '',
  notes: '',
});

const form = reactive(initialState());
const error = ref('');

const isEdit = computed(() => Boolean(form.id));
const isAdmin = computed(() => props.isAdmin);

const users = computed(() => props.users ?? []);

const intensityOptions = computed(() =>
  props.intensities.map((option) => ({
    ...option,
    label: t(`exercises.intensity.${option.value.toLowerCase()}`, option.label),
  }))
);

watch(
  () => props.exercise,
  (value) => {
    Object.assign(form, initialState(), value || {});
    form.referenceId = value?.referenceId ?? '';
    form.userId = value?.userId ?? '';
  },
  { immediate: true }
);

watch(
  () => props.modelValue,
  (open) => {
    if (!open) {
      Object.assign(form, initialState());
      error.value = '';
    }
  }
);

const close = () => {
  emit('update:modelValue', false);
};

const validate = () => {
  if (!form.referenceId && !form.customName) {
    error.value = t('exercises.form.validation.name');
    return false;
  }
  if (isAdmin.value && !form.userId) {
    error.value = t('exercises.form.validation.user');
    return false;
  }
  error.value = '';
  return true;
};

const formatUser = (user) => {
  if (!user) return '';
  const first = user.name ?? '';
  const last = user.lastName ?? '';
  const full = `${first} ${last}`.trim();
  return full || user.email || user.id;
};

const handleSubmit = () => {
  if (!validate()) {
    return;
  }
  emit('submit', { ...form });
};
</script>
