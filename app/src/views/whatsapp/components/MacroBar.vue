<template>
  <div>
    <div class="flex items-center justify-between text-xs font-semibold text-slate-600">
      <span>{{ label }}</span>
      <span>{{ formattedAmount }}</span>
    </div>
    <div class="mt-1 h-2 rounded-full bg-slate-200">
      <div :class="['h-full rounded-full transition-all', color]" :style="{ width: computedWidth }" />
    </div>
  </div>
</template>

<script setup>
import { computed, defineProps } from 'vue';

const props = defineProps({
  label: { type: String, required: true },
  color: { type: String, default: 'bg-primary-500' },
  percentage: { type: Number, default: 0 },
  amount: { type: Number, default: 0 },
});

const computedWidth = computed(() => `${Math.min(Math.max(props.percentage ?? 0, 0), 100)}%`);
const formattedAmount = computed(() => `${props.amount} g (${Math.min(Math.max(props.percentage ?? 0, 0), 100).toFixed(0)}%)`);
</script>

