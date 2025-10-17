<template>
  <form class="flex flex-col gap-6" @submit.prevent="submit">
    <section class="space-y-3">
      <header class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700">{{ t('payments.recurring.selectPlanTitle') }}</h3>
        <span v-if="loadingPlans" class="text-xs text-slate-500">{{ t('payments.recurring.loading') }}</span>
      </header>
      <p v-if="planError" class="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ planError }}
      </p>
      <div v-else>
        <p
          v-if="!plans.length && !loadingPlans"
          class="rounded-xl border border-dashed border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-500"
        >
          {{ t('payments.recurring.noPlans') }}
        </p>
        <div v-else class="grid gap-3 md:grid-cols-2">
          <button v-for="plan in plans" :key="plan.id" type="button"
            class="flex h-full flex-col justify-between rounded-2xl border px-4 py-4 text-left transition" :class="form.paymentPlanId === plan.id
                ? 'border-emerald-500 bg-emerald-50 text-emerald-700 shadow'
                : 'border-slate-200 bg-white text-slate-700 hover:border-emerald-400 hover:bg-emerald-50'
              " @click="selectPlan(plan.id)">
            <div class="space-y-2">
              <p class="text-sm font-semibold">{{ plan.name }}</p>
              <p class="text-xs text-slate-500">{{ plan.description || intervalLabel(plan.interval) }}</p>
            </div>
            <div class="mt-4">
              <p class="text-2xl font-semibold text-slate-900">{{ formatCurrency(plan.amount, plan.currency) }}</p>
              <p class="text-xs uppercase tracking-wide text-slate-500">{{ intervalLabel(plan.interval) }}</p>
            </div>
          </button>
        </div>
      </div>
    </section>

    <section class="grid gap-4 md:grid-cols-1">
      <!--<label class="flex flex-col gap-1">
        <span class="text-sm font-medium text-slate-600">{{ t('payments.recurring.chargeTimingLabel') }}</span>
        <select v-model="form.immediateCharge" class="input">
          <option :value="true">{{ t('payments.recurring.chargeTimingImmediate') }}</option>
          <option :value="false">{{ t('payments.recurring.chargeTimingLater') }}</option>
        </select>
      </label> -->
      <div class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
        <p class="font-semibold text-slate-700">{{ t('payments.recurring.selectedPlanTitle') }}</p>
        <p v-if="selectedPlan" class="mt-1 text-slate-900">
          {{ selectedPlan.name }} - {{ formatCurrency(selectedPlan.amount, selectedPlan.currency) }} - {{
            intervalLabel(selectedPlan.interval) }}
        </p>
        <p v-else class="mt-1 text-xs text-slate-500">{{ t('payments.recurring.selectedPlanEmpty') }}</p>
      </div>
    </section>

    <section class="space-y-3">
      <h3 class="text-sm font-semibold text-slate-700">{{ t('payments.recurring.methodTitle') }}</h3>
      <div class="grid gap-3 md:grid-cols-1">
        <label v-for="method in supportedMethods" :key="method.value"
          class="flex items-center gap-3 rounded-xl border px-4 py-3 text-sm transition" :class="form.paymentMethod === method.value
              ? 'border-emerald-500 bg-emerald-50 text-emerald-700'
              : 'border-slate-200 bg-white text-slate-600 hover:border-emerald-400'
            ">
          <input v-model="form.paymentMethod" type="radio" name="recurringPaymentMethod" :value="method.value"
            class="h-4 w-4 text-emerald-500" />
          <div class="flex flex-col">
            <span class="font-semibold">{{ method.label }}</span>
            <span class="text-xs text-slate-500">{{ method.description }}</span>
          </div>
        </label>
      </div>
    </section>

    <section v-if="showCardSelector" class="space-y-3">
      <h3 class="text-sm font-semibold text-slate-700">{{ t('payments.recurring.cards.title') }}</h3>
      <div v-if="cards.length" class="grid gap-3">
        <label v-for="card in cards" :key="card.id"
          class="flex items-center justify-between rounded-xl border px-4 py-3 text-sm transition" :class="form.paymentMethodId === card.id
              ? 'border-emerald-500 bg-emerald-50 text-emerald-700'
              : 'border-slate-200 bg-white text-slate-600 hover:border-emerald-400'
            ">
          <div class="flex items-center gap-3">
            <input v-model="form.paymentMethodId" type="radio" name="recurringCardId" :value="card.id"
              class="h-4 w-4 text-emerald-500" />
                        <div class="flex flex-col">
              <div class="flex items-center gap-2">
                <span :class="['rounded-full btn-primary w-10 h-8', brandMeta(card.brand).class]">{{ brandMeta(card.brand).label }}</span>
                <span class="font-medium text-slate-700">**** {{ card.lastFour }}</span>
              </div>
              <span class="text-xs text-slate-500">{{
                t('payments.recurring.cards.expires', { month: card.expiryMonth, year: card.expiryYear })
              }}</span>
            </div>
          </div>
          <span v-if="card.defaultCard"
            class="rounded-full bg-emerald-100 px-2 py-1 text-xs font-medium text-emerald-700">
            {{ t('payments.recurring.cards.defaultBadge') }}
          </span>
        </label>
      </div>
      <p v-else class="rounded-xl border border-dashed border-slate-200 bg-slate-50 p-4 text-sm text-slate-500">
        {{ t('payments.recurring.cards.empty') }}
      </p>
    </section>

    <!-- <section class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
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
    </section> -->

    <label class="flex items-start gap-3 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
      <input v-model="form.acceptedTerms" type="checkbox" class="mt-1 h-4 w-4 text-emerald-500" required />
      <span>
        {{ t('payments.recurring.termsNotice') }}
      </span>
    </label>

    <footer class="flex justify-end">
      <button type="submit" class="btn-primary" :disabled="!canSubmit || loading">
        <span v-if="loading" class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
        <span>{{ loading ? t('payments.recurring.submitLoading') : t('payments.recurring.submit') }}</span>
      </button>
    </footer>
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

const supportedMethods = computed(() => [
  {
    value: 'CREDIT_CARD',
    label: t('payments.recurring.methods.card.label'),
    description: t('payments.recurring.methods.card.description'),
  },
  /*{
    value: 'PIX',
    label: t('payments.recurring.methods.pix.label'),
    description: t('payments.recurring.methods.pix.description'),
  },*/
]);

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
    visa: { label: 'Visa', class: 'bg-blue-100 text-blue-700 border border-blue-200' },
    mastercard: { label: 'Mastercard', class: 'bg-orange-100 text-orange-700 border border-orange-200' },
    americanexpress: { label: 'Amex', class: 'bg-teal-100 text-teal-700 border border-teal-200' },
    amex: { label: 'Amex', class: 'bg-teal-100 text-teal-700 border border-teal-200' },
  };
  return map[key] || {
    label: brand || t('payments.cardForm.brandFallback'),
    class: 'bg-slate-100 text-slate-600 border border-slate-200',
  };
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
