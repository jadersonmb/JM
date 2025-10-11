<template>
  <div class="space-y-3">
    <div v-for="item in items" :key="item.key" class="space-y-1">
      <div class="flex items-center justify-between text-xs font-semibold text-slate-600">
        <span>{{ item.label }}</span>
        <span>{{ formatAmount(item) }}</span>
      </div>
      <div v-if="hasPercentage(item)" class="h-2 rounded-full bg-slate-200">
        <div
          :class="['h-full rounded-full transition-all', item.color || 'bg-primary-500']"
          :style="{ width: normalizedPercentage(item) }"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  items: { type: Array, default: () => [] },
});

const clamp = (value) => {
  const numeric = Number.isFinite(value) ? value : 0;
  return Math.min(Math.max(numeric, 0), 100);
};

const hasPercentage = (item) => item.percentage !== undefined && item.percentage !== null;

const normalizedPercentage = (item) => `${clamp(item.percentage)}%`;

const formatAmount = (item) => {
  const amount = item.display ?? item.amount ?? 0;
  const unit = item.unit ?? '';
  if (item.renderAmount) {
    return item.renderAmount(item);
  }
  if (unit) {
    return `${amount} ${unit}`;
  }
  return amount;
};
</script>
