<template>
  <form class="space-y-6" @submit.prevent="submit">
    <section class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
      <header class="flex items-start justify-between gap-4">
        <div>
          <h3 class="text-lg font-semibold text-gray-800">Select a subscription plan</h3>
          <p class="text-sm text-gray-500">Choose the billing cycle that matches the customer needs.</p>
        </div>
        <span v-if="loadingPlans" class="text-xs text-gray-400">Loading…</span>
      </header>
      <p v-if="planError" class="mt-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
        {{ planError }}
      </p>
      <div v-else class="mt-4">
        <p
          v-if="!plans.length && !loadingPlans"
          class="rounded-xl border border-dashed border-gray-200 bg-gray-50 px-4 py-3 text-sm text-gray-500"
        >
          {{ t('payments.recurring.noPlans') }}
        </p>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <button
            v-for="plan in plans"
            :key="plan.id"
            type="button"
            class="relative border border-gray-200 rounded-xl p-4 hover:border-blue-300 transition-all duration-300 cursor-pointer text-left bg-white hover:shadow-md"
            :class="form.paymentPlanId === plan.id
              ? 'border-blue-500 bg-blue-50 shadow-sm ring-1 ring-blue-400'
              : ''"
            @click="selectPlan(plan.id)"
          >
            <div class="space-y-2">
              <h4 class="font-semibold text-gray-800">{{ plan.name }}</h4>
              <p class="text-sm text-gray-500">{{ plan.description || intervalLabel(plan.interval) }}</p>
            </div>
            <div class="mt-4 space-y-1">
              <p class="text-2xl font-semibold text-gray-800">{{ formatCurrency(plan.amount, plan.currency) }}</p>
              <p class="text-xs text-gray-400 uppercase tracking-wide">{{ intervalLabel(plan.interval) }}</p>
            </div>
            <div
              v-if="form.paymentPlanId === plan.id"
              class="absolute top-2 right-2 text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full"
            >
              ✔ Selected
            </div>
          </button>
        </div>
      </div>
    </section>

    <section class="bg-white rounded-xl border border-gray-200 shadow-sm p-4 text-sm text-gray-700">
      <div class="flex items-center justify-between gap-4">
        <span>
          <template v-if="selectedPlan">
            Selected plan: {{ selectedPlan.name }} - {{ formatCurrency(selectedPlan.amount, selectedPlan.currency) }} -
            {{ intervalLabel(selectedPlan.interval) }}
          </template>
          <template v-else>
            {{ t('payments.recurring.selectedPlanEmpty') }}
          </template>
        </span>
        <button type="button" class="text-gray-500 text-xs border border-gray-300 rounded-full px-3 py-1 hover:bg-gray-100">
          Editable
        </button>
      </div>
    </section>

    <section class="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-4">
      <header class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-800">Payment method</h3>
        <span class="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full">Active</span>
      </header>
      <div class="rounded-xl border border-green-400 bg-green-50 p-4 flex justify-between items-center">
        <div class="flex items-center gap-3">
          <div class="w-3 h-3 bg-green-500 rounded-full"></div>
          <p class="font-medium text-gray-800">Credit card</p>
        </div>
        <p class="text-gray-600 text-sm">Bill the saved card for every cycle.</p>
      </div>
    </section>

    <section v-if="showCardSelector" class="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-4">
      <h3 class="text-lg font-semibold text-gray-800">Select card</h3>
      <div v-if="cards.length" class="space-y-3">
        <label
          v-for="card in cards"
          :key="card.id"
          class="border border-gray-200 rounded-xl p-4 flex justify-between items-center cursor-pointer transition-all duration-300 hover:border-blue-300 hover:shadow-md"
          :class="form.paymentMethodId === card.id ? 'border-blue-500 bg-blue-50' : 'bg-white'"
        >
          <div class="flex items-center gap-3">
            <input
              v-model="form.paymentMethodId"
              type="radio"
              name="recurringCardId"
              :value="card.id"
              class="sr-only"
            />
            <div
              :class="['w-10 h-10 flex items-center justify-center rounded-full font-semibold text-sm border', brandMeta(card.brand).class]"
            >
              {{ brandMeta(card.brand).initial }}
            </div>
            <div>
              <p class="font-medium text-gray-800">{{ brandMeta(card.brand).label }} •••• {{ card.lastFour }}</p>
              <p class="text-xs text-gray-500">{{ t('payments.recurring.cards.expires', { month: card.expiryMonth, year: card.expiryYear }) }}</p>
            </div>
          </div>
          <span class="text-xs text-gray-400">{{ cardBadge(card) }}</span>
        </label>
      </div>
      <p v-else class="rounded-xl border border-dashed border-gray-200 bg-gray-50 p-4 text-sm text-gray-500">
        {{ t('payments.recurring.cards.empty') }}
      </p>
    </section>

    <section class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
      <label class="flex items-start gap-2 text-sm text-gray-600">
        <input
          v-model="form.acceptedTerms"
          type="checkbox"
          class="mt-1 border-gray-300 rounded text-blue-600 focus:ring-blue-500"
          required
        />
        I confirm the customer agreed to recurring charges and understand that cancellation or refunds must follow consumer
        protection laws.
      </label>
      <p class="text-xs text-gray-400 mt-2">
        You can change plans any time. Next charge will follow the selected cycle.
      </p>
      <div class="mt-4 flex justify-end">
        <button
          type="submit"
          class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium disabled:opacity-60 disabled:cursor-not-allowed"
          :disabled="!canSubmit || loading"
        >
          <span v-if="loading" class="mr-2 inline-flex h-4 w-4 animate-spin rounded-full border-2 border-blue-200 border-t-transparent"></span>
          <span>{{ loading ? t('payments.recurring.submitLoading') : 'Payment' }}</span>
        </button>
      </div>
    </section>
  </form>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { listPaymentPlans } from '@/services/payments';

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

const { t, locale } = useI18n();

const localeTag = computed(() => (locale.value === 'pt' ? 'pt-BR' : 'en-US'));

const plans = ref([]);
const loadingPlans = ref(false);
const planError = ref('');

const form = reactive({
  paymentPlanId: null,
  immediateCharge: true,
  paymentMethod: 'CREDIT_CARD',
  paymentMethodId: null,
  stripeCustomerId: '',
  asaasCustomerId: '',
  acceptedTerms: false,
});

const selectedPlan = computed(() => plans.value.find((plan) => plan.id === form.paymentPlanId) || null);
const showCardSelector = computed(() => form.paymentMethod === 'CREDIT_CARD');

const canSubmit = computed(() =>
  Boolean(form.paymentPlanId) &&
  form.acceptedTerms &&
  (!showCardSelector.value || form.paymentMethodId)
);

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

async function loadPlans() {
  loadingPlans.value = true;
  planError.value = '';
  try {
    const { data } = await listPaymentPlans();
    plans.value = data ?? [];
    if (plans.value.length) {
      const existing = plans.value.find((plan) => plan.id === form.paymentPlanId);
      selectPlan(existing ? existing.id : plans.value[0].id);
    }
  } catch (error) {
    console.error('Failed to load plans', error);
    planError.value = error?.response?.data?.message ?? t('payments.recurring.planError');
  } finally {
    loadingPlans.value = false;
  }
}

function brandMeta(brand) {
  const key = (brand || '').toLowerCase();
  const map = {
    visa: { label: 'Visa', initial: 'V', class: 'bg-blue-100 text-blue-600 border-blue-200' },
    mastercard: { label: 'Mastercard', initial: 'M', class: 'bg-orange-100 text-orange-600 border-orange-200' },
    americanexpress: { label: 'American Express', initial: 'A', class: 'bg-teal-100 text-teal-600 border-teal-200' },
    amex: { label: 'American Express', initial: 'A', class: 'bg-teal-100 text-teal-600 border-teal-200' },
  };
  const fallbackLabel = brand || t('payments.cardForm.brandFallback');
  return map[key] || { label: fallbackLabel, initial: (fallbackLabel || 'C').charAt(0).toUpperCase(), class: 'bg-slate-100 text-slate-600 border-slate-200' };
}

function cardBadge(card) {
  if (card.defaultCard) {
    return 'Default';
  }
  const badge = card.label || card.nickname || card.metadata?.tag || card.metadata?.label;
  if (badge) {
    return badge;
  }
  return 'Personal';
}

function selectPlan(planId) {
  form.paymentPlanId = planId;
}

function intervalLabel(interval) {
  const key = (interval ?? 'unknown').toLowerCase();
  return t(`payments.intervals.${key}`);
}

function formatCurrency(amount, currencyCode) {
  return new Intl.NumberFormat(localeTag.value, {
    style: 'currency',
    currency: currencyCode || props.currency,
  }).format(amount ?? 0);
}

function submit() {
  if (!canSubmit.value || !selectedPlan.value) return;
  emit('create', {
    paymentPlanId: form.paymentPlanId,
    immediateCharge: form.immediateCharge,
    paymentMethod: form.paymentMethod,
    paymentMethodId: showCardSelector.value ? form.paymentMethodId : null,
    metadata: {
      stripeCustomerId: form.stripeCustomerId || undefined,
      asaasCustomerId: form.asaasCustomerId || undefined,
    },
  });
}

onMounted(() => {
  loadPlans();
});
</script>
