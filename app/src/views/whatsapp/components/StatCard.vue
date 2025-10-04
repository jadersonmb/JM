<template>
  <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
    <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ label }}</p>
    <p class="mt-2 text-2xl font-semibold text-slate-900">{{ value }}</p>
    <p v-if="description" class="mt-1 text-xs text-slate-500">{{ description }}</p>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  description: { type: String, default: '' },
});
</script>
