<template>
  <transition name="modal">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/60 px-4 py-10 sm:py-16">
      <div class="w-full max-w-xl rounded-3xl bg-white shadow-2xl">
        <header class="flex items-start justify-between border-b border-slate-200 px-6 py-4">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">{{ title }}</h3>
            <p v-if="description" class="mt-1 text-sm text-slate-500">{{ description }}</p>
          </div>
          <button type="button" class="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
            @click="close">
            <XMarkIcon class="h-5 w-5" />
          </button>
        </header>

        <form class="grid gap-5 px-6 py-6" @submit.prevent="handleSubmit">
          <div v-for="field in normalizedFields" :key="field.key" class="space-y-1">
            <label :for="field.key" class="block text-sm font-medium text-slate-700">
              {{ field.label }}
              <span v-if="field.required" class="text-red-500">*</span>
            </label>
            <template v-if="field.type === 'select'">
              <select :id="field.key" v-model="form[field.key]" class="input" :required="field.required">
                <option value="">{{ field.placeholder || 'Select an option' }}</option>
                <option v-for="option in field.options" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </template>
            <template v-else-if="field.type === 'textarea'">
              <textarea :id="field.key" v-model="form[field.key]" class="input min-h-[96px] resize-y"
                :rows="field.rows || 3" :required="field.required" />
            </template>
            <template v-else-if="field.type === 'checkbox'">
              <label class="inline-flex items-center gap-2 text-sm text-slate-600">
                <input :id="field.key" v-model="form[field.key]" type="checkbox"
                  class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
                {{ field.checkboxLabel || field.label }}
              </label>
            </template>
            <template v-else>
              <input :id="field.key" v-model="form[field.key]"
                :type="field.type === 'number' ? 'number' : field.type || 'text'"
                class="input" :required="field.required" :min="field.min" :max="field.max" :step="field.step" />
            </template>
            <p v-if="field.helper" class="text-xs text-slate-400">{{ field.helper }}</p>
          </div>

          <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
            <button type="button" class="btn-secondary" @click="close">{{ cancelLabel }}</button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              <span v-if="submitting" class="flex items-center gap-2">
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
                {{ savingLabel }}
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
import { computed, reactive, watch } from 'vue';
import { XMarkIcon } from '@heroicons/vue/24/outline';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: 'Manage record' },
  description: { type: String, default: '' },
  fields: { type: Array, default: () => [] },
  model: { type: Object, default: () => ({}) },
  submitting: { type: Boolean, default: false },
  submitLabel: { type: String, default: 'Save' },
  savingLabel: { type: String, default: 'Saving...' },
  cancelLabel: { type: String, default: 'Cancel' },
});

const emit = defineEmits(['update:modelValue', 'submit']);

const form = reactive({});

const normalizedFields = computed(() =>
  props.fields.map((field) => ({ type: 'text', required: false, options: [], ...field }))
);

watch(
  () => props.model,
  (value) => {
    normalizedFields.value.forEach((field) => {
      if (field.type === 'checkbox') {
        form[field.key] = Boolean(value?.[field.key]);
      } else {
        form[field.key] = value?.[field.key] ?? '';
      }
    });
  },
  { immediate: true }
);

watch(
  normalizedFields,
  (fields) => {
    fields.forEach((field) => {
      if (!(field.key in form)) {
        form[field.key] = field.type === 'checkbox' ? false : '';
      }
    });
  },
  { immediate: true }
);

const close = () => {
  emit('update:modelValue', false);
};

const handleSubmit = () => {
  const payload = {};
  normalizedFields.value.forEach((field) => {
    payload[field.key] = form[field.key];
  });
  emit('submit', payload);
};

</script>

<script>
export default {
  emits: ['update:modelValue', 'submit'],
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

