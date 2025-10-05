<template>
  <div class="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
    <header class="flex flex-col gap-2">
      <h2 class="text-lg font-semibold text-slate-900">Select a payment method</h2>
      <p class="text-sm text-slate-500">
        Toggle between card, PIX or recurring billing. Each option updates the form details on the right side.
      </p>
    </header>
    <div class="grid gap-3 sm:grid-cols-3">
      <button
        v-for="option in options"
        :key="option.value"
        type="button"
        class="flex items-start gap-3 rounded-xl border px-4 py-3 text-left transition"
        :class="
          modelValue === option.value
            ? 'border-emerald-500 bg-emerald-50 text-emerald-700 shadow'
            : 'border-slate-200 bg-white text-slate-700 hover:border-emerald-400 hover:bg-emerald-50'
        "
        @click="$emit('update:modelValue', option.value)"
      >
        <span class="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-100 text-emerald-700">
          <component :is="option.icon" class="h-5 w-5" />
        </span>
        <span class="flex flex-col">
          <span class="text-sm font-semibold">{{ option.label }}</span>
          <span class="text-xs text-slate-500">{{ option.description }}</span>
        </span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import {
  CreditCardIcon,
  QrCodeIcon,
  ArrowPathRoundedSquareIcon,
} from '@heroicons/vue/24/outline';

const props = defineProps({
  modelValue: {
    type: String,
    default: 'CARD',
  },
});

const options = computed(() => [
  {
    value: 'CARD',
    label: 'Card Payment',
    description: 'Accept Visa, Mastercard, Elo and international cards with 3-D secure.',
    icon: CreditCardIcon,
  },
  {
    value: 'PIX',
    label: 'PIX Instant',
    description: 'Generate QR codes and copy-and-paste keys with realtime confirmation.',
    icon: QrCodeIcon,
  },
  {
    value: 'RECURRING',
    label: 'Recurring Billing',
    description: 'Configure subscriptions with automated retries and dunning.',
    icon: ArrowPathRoundedSquareIcon,
  },
]);
</script>
