<template>
  <transition name="fade">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 px-4">
      <div class="w-full max-w-md rounded-3xl bg-white p-6 shadow-2xl dark:bg-slate-900">
        <header class="flex items-center gap-3">
          <ExclamationTriangleIcon class="h-10 w-10 text-amber-500" />
          <div>
            <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-100">{{ title }}</h2>
            <p class="text-sm text-slate-500 dark:text-slate-400">{{ message }}</p>
          </div>
        </header>
        <footer class="mt-6 flex items-center justify-end gap-3">
          <button type="button" class="btn-secondary" @click="emit('update:modelValue', false)">Cancel</button>
          <button type="button" class="btn-primary bg-red-600 hover:bg-red-500 focus:ring-red-400" @click="confirm">
            {{ confirmLabel }}
          </button>
        </footer>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: 'Are you sure?' },
  message: { type: String, default: 'This action cannot be undone.' },
  confirmLabel: { type: String, default: 'Confirm' },
});

const emit = defineEmits(['confirm', 'update:modelValue']);

const confirm = () => {
  emit('confirm');
  emit('update:modelValue', false);
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
