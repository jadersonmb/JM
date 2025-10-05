<template>
  <form class="flex flex-col gap-6" @submit.prevent="submit">
    <section class="grid gap-4 md:grid-cols-2">
      <label class="flex flex-col gap-1">
        <span class="text-sm font-medium text-slate-600">Plan ID</span>
        <input v-model="form.planId" type="text" class="input" placeholder="price_monthly_001" required />
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-sm font-medium text-slate-600">Amount ({{ currency }})</span>
        <input v-model.number="form.amount" type="number" min="1" step="0.01" class="input" required />
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-sm font-medium text-slate-600">Billing interval</span>
        <select v-model="form.interval" class="input">
          <option v-for="option in intervalOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-sm font-medium text-slate-600">Charge timing</span>
        <select v-model="form.immediateCharge" class="input">
          <option :value="true">Start immediately</option>
          <option :value="false">Charge on next billing date</option>
        </select>
      </label>
    </section>

    <section class="space-y-3">
      <h3 class="text-sm font-semibold text-slate-700">Payment method</h3>
      <div class="grid gap-3 md:grid-cols-2">
        <label
          v-for="method in supportedMethods"
          :key="method.value"
          class="flex items-center gap-3 rounded-xl border px-4 py-3 text-sm transition"
          :class="
            form.paymentMethod === method.value
              ? 'border-emerald-500 bg-emerald-50 text-emerald-700'
              : 'border-slate-200 bg-white text-slate-600 hover:border-emerald-400'
          "
        >
          <input
            v-model="form.paymentMethod"
            type="radio"
            name="recurringPaymentMethod"
            :value="method.value"
            class="h-4 w-4 text-emerald-500"
          />
          <div class="flex flex-col">
            <span class="font-semibold">{{ method.label }}</span>
            <span class="text-xs text-slate-500">{{ method.description }}</span>
          </div>
        </label>
      </div>
    </section>

    <section v-if="showCardSelector" class="space-y-3">
      <h3 class="text-sm font-semibold text-slate-700">Select card</h3>
      <div v-if="cards.length" class="grid gap-3">
        <label
          v-for="card in cards"
          :key="card.id"
          class="flex items-center justify-between rounded-xl border px-4 py-3 text-sm transition"
          :class="
            form.paymentMethodId === card.id
              ? 'border-emerald-500 bg-emerald-50 text-emerald-700'
              : 'border-slate-200 bg-white text-slate-600 hover:border-emerald-400'
          "
        >
          <div class="flex items-center gap-3">
            <input
              v-model="form.paymentMethodId"
              type="radio"
              name="recurringCardId"
              :value="card.id"
              class="h-4 w-4 text-emerald-500"
            />
            <div class="flex flex-col">
              <span class="font-medium">{{ card.brand }} ···· {{ card.lastFour }}</span>
              <span class="text-xs text-slate-500">Expires {{ card.expiryMonth }}/{{ card.expiryYear }}</span>
            </div>
          </div>
          <span v-if="card.defaultCard" class="rounded-full bg-emerald-100 px-2 py-1 text-xs font-medium text-emerald-700">
            Default
          </span>
        </label>
      </div>
      <p v-else class="rounded-xl border border-dashed border-slate-200 bg-slate-50 p-4 text-sm text-slate-500">
        Add a card first to create recurring charges.
      </p>
    </section>

    <section class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
      <details class="group">
        <summary class="flex cursor-pointer items-center justify-between text-sm font-semibold text-slate-700">
          Advanced gateway metadata
          <span class="text-xs text-slate-500 group-open:hidden">(optional)</span>
        </summary>
        <div class="mt-3 grid gap-3 md:grid-cols-2">
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">Stripe customer ID</span>
            <input v-model="form.stripeCustomerId" type="text" class="input" placeholder="cus_123" />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">Asaas customer ID</span>
            <input v-model="form.asaasCustomerId" type="text" class="input" placeholder="cus_abc" />
          </label>
        </div>
      </details>
    </section>

    <label class="flex items-start gap-3 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
      <input v-model="form.acceptedTerms" type="checkbox" class="mt-1 h-4 w-4 text-emerald-500" required />
      <span>
        I confirm the customer has agreed to recurring charges and understand that cancellation or refunds must
        be offered according to Brazilian consumer protection laws.
      </span>
    </label>

    <footer class="flex justify-end">
      <button type="submit" class="btn-primary" :disabled="!canSubmit || loading">
        <span v-if="loading" class="loader h-4 w-4" />
        <span>{{ loading ? 'Creating…' : 'Create subscription' }}</span>
      </button>
    </footer>
  </form>
</template>

<script setup>
import { computed, reactive, watch } from 'vue';

const props = defineProps({
  cards: {
    type: Array,
    default: () => [],
  },
  currency: {
    type: String,
    default: 'BRL',
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['create']);

const intervalOptions = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'YEARLY', label: 'Yearly' },
  { value: 'WEEKLY', label: 'Weekly' },
  { value: 'DAILY', label: 'Daily' },
];

const supportedMethods = [
  {
    value: 'CREDIT_CARD',
    label: 'Credit card',
    description: 'Stripe handles retries, dunning and SCA compliance.',
  },
  {
    value: 'PIX',
    label: 'PIX recurring',
    description: 'Generate PIX charges automatically via Asaas.',
  },
];

const form = reactive({
  planId: '',
  amount: 0,
  interval: 'MONTHLY',
  immediateCharge: true,
  paymentMethod: 'CREDIT_CARD',
  paymentMethodId: null,
  stripeCustomerId: '',
  asaasCustomerId: '',
  acceptedTerms: false,
});

watch(
  () => props.cards,
  (value) => {
    if (!form.paymentMethodId && value?.length) {
      const defaultCard = value.find((card) => card.defaultCard) ?? value[0];
      form.paymentMethodId = defaultCard.id;
    }
  },
  { immediate: true },
);

const showCardSelector = computed(() => form.paymentMethod === 'CREDIT_CARD');
const canSubmit = computed(
  () =>
    form.planId &&
    form.amount > 0 &&
    form.acceptedTerms &&
    (!showCardSelector.value || form.paymentMethodId)
);

function submit() {
  if (!canSubmit.value) return;
  emit('create', {
    planId: form.planId,
    amount: Number(form.amount),
    interval: form.interval,
    immediateCharge: form.immediateCharge,
    paymentMethod: form.paymentMethod,
    paymentMethodId: showCardSelector.value ? form.paymentMethodId : null,
    metadata: {
      stripeCustomerId: form.stripeCustomerId || undefined,
      asaasCustomerId: form.asaasCustomerId || undefined,
    },
  });
}
</script>
