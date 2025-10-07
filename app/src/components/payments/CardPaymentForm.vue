<template>
  <form class="flex flex-col gap-6" @submit.prevent="submitPayment">
    <section class="space-y-3">
      <header class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700">{{ t('payments.cardForm.selectPlanTitle') }}</h3>
        <span v-if="loadingPlans" class="text-xs text-slate-500">{{ t('payments.cardForm.loading') }}</span>
      </header>
      <p v-if="planError" class="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ planError }}
      </p>
      <div v-else>
        <p
          v-if="!plans.length && !loadingPlans"
          class="rounded-xl border border-dashed border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-500"
        >
          {{ t('payments.cardForm.noPlans') }}
        </p>
        <div v-else class="grid gap-3 md:grid-cols-2">
          <button
            v-for="plan in plans"
            :key="plan.id"
            type="button"
            class="flex h-full flex-col justify-between rounded-2xl border px-4 py-4 text-left transition"
            :class="
              form.paymentPlanId === plan.id
                ? 'border-emerald-500 bg-emerald-50 text-emerald-700 shadow'
                : 'border-slate-200 bg-white text-slate-700 hover:border-emerald-400 hover:bg-emerald-50'
            "
            @click="selectPlan(plan.id)"
          >
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

    <section class="space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700">{{ t('payments.cardForm.storedCardsTitle') }}</h3>
        <button type="button" class="btn-text" @click="toggleNewCard">
          {{ isAddingCard ? t('payments.cardForm.cancelAddCard') : t('payments.cardForm.addCardButton') }}
        </button>
      </div>

      <div v-if="cards.length" class="grid gap-3">
        <label
          v-for="card in cards"
          :key="card.id"
          class="flex items-center justify-between rounded-xl border px-4 py-3 text-sm transition"
          :class="
            form.cardId === card.id
              ? 'border-emerald-500 bg-emerald-50 text-emerald-700'
              : 'border-slate-200 bg-white text-slate-600 hover:border-emerald-400'
          "
        >
          <div class="flex items-center gap-3">
            <input
              v-model="form.cardId"
              type="radio"
              name="cardId"
              :value="card.id"
              class="h-4 w-4 text-emerald-500"
            />
            <div class="flex flex-col">
              <div class="flex items-center gap-2">
                <span :class="['rounded-full btn-primary w-10 h-8', brandMeta(card.brand).class]">{{ brandMeta(card.brand).label }}</span>
                <span class="font-medium text-slate-700">**** {{ card.lastFour }}</span>
              </div>
              <span class="text-xs text-slate-500">{{ t('payments.cardForm.expires', { month: card.expiryMonth, year: card.expiryYear }) }}</span>
          </div>
        </div>
        <span v-if="card.defaultCard" class="rounded-full bg-emerald-100 px-2 py-1 text-xs font-medium text-emerald-700">
          {{ t('payments.cardForm.defaultBadge') }}
        </span>
      </label>
    </div>
    <p v-else class="rounded-xl border border-dashed border-slate-200 bg-slate-50 p-4 text-sm text-slate-500">
      {{ t('payments.cardForm.noCards') }}
    </p>
    </section>

    <transition name="fade">
      <section v-if="isAddingCard" class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
        <header class="mb-4 space-y-1">
          <h3 class="text-sm font-semibold text-slate-700">{{ t('payments.cardForm.tokenizationTitle') }}</h3>
          <p class="text-xs text-slate-500">
            {{ t('payments.cardForm.tokenizationDescription') }}
          </p>
        </header>
        <div class="grid gap-4 md:grid-cols-2">
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.cardForm.fields.cardholder') }}</span>
            <input v-model="newCard.cardholder" type="text" class="input" autocomplete="cc-name" required />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.cardForm.fields.number') }}</span>
            <input
              v-model="formattedCardNumber"
              type="text"
              class="input font-mono"
              inputmode="numeric"
              autocomplete="cc-number"
              maxlength="19"
              :placeholder="t('payments.cardForm.placeholders.number')"
              @input="onCardNumberInput"
              required
            />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.cardForm.fields.expiry') }}</span>
            <input
              v-model="formattedExpiry"
              type="text"
              class="input font-mono"
              inputmode="numeric"
              maxlength="5"
              :placeholder="t('payments.cardForm.placeholders.expiry')"
              autocomplete="cc-exp"
              @input="onExpiryInput"
              required
            />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('payments.cardForm.fields.cvc') }}</span>
            <input
              v-model="newCard.cvc"
              type="password"
              class="input"
              inputmode="numeric"
              maxlength="4"
              autocomplete="cc-csc"
              required
            />
          </label>
        </div>
        <div class="mt-4 flex items-center justify-between">
          <label class="flex items-center gap-2 text-sm text-slate-600">
            <input v-model="newCard.setDefault" type="checkbox" class="h-4 w-4 text-emerald-500" />
            {{ t('payments.cardForm.setDefault') }}
          </label>
          <button type="button" class="btn-primary" :disabled="tokenizing || !canTokenize" @click="tokenizeCard">
            <span v-if="tokenizing" class="loader h-4 w-4" />
            <span>{{ tokenizing ? t('payments.cardForm.tokenizing') : t('payments.cardForm.addingCard') }}</span>
          </button>
        </div>
      </section>
    </transition>

    <footer class="flex items-center justify-between">
      <p class="text-xs text-slate-500">
        {{ t('payments.cardForm.pciNotice') }}
      </p>
      <button type="submit" class="btn-primary" :disabled="!canSubmit || loading">
        <span v-if="loading" class="loader h-4 w-4" />
        <span>{{ loading ? t('payments.cardForm.processing') : t('payments.cardForm.submit') }}</span>
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
  tokenizing: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['create', 'tokenize']);

const { t, locale } = useI18n();

const plans = ref([]);
const loadingPlans = ref(false);
const planError = ref('');

const form = reactive({
  paymentPlanId: null,
  cardId: null,
});

const newCard = reactive({
  cardNumber: '',
  expiryMonth: '',
  expiryYear: '',
  cvc: '',
  cardholder: '',
  setDefault: false,
});

const isAddingCard = ref(false);
const formattedCardNumber = ref('');
const formattedExpiry = ref('');

const selectedPlan = computed(() => plans.value.find((plan) => plan.id === form.paymentPlanId) || null);
const showCardSelector = computed(() => true);
const canSubmit = computed(() => Boolean(selectedPlan.value) && Boolean(form.cardId));

watch(
  () => props.cards,
  (value) => {
    if (!form.cardId && value?.length) {
      const defaultCard = value.find((card) => card.defaultCard) ?? value[0];
      form.cardId = defaultCard.id;
    }
  },
  { immediate: true },
);

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

async function loadPlans() {
  loadingPlans.value = true;
  planError.value = '';
  try {
    const { data } = await listPaymentPlans();
    plans.value = data ?? [];
    if (plans.value.length) {
      selectPlan(plans.value[0].id);
    }
  } catch (error) {
    console.error('Failed to load payment plans', error);
    planError.value = t('payments.cardForm.planLoadError');
  } finally {
    loadingPlans.value = false;
  }
}

function selectPlan(planId) {
  form.paymentPlanId = planId;
}

const canTokenize = computed(
  () =>
    newCard.cardholder.length > 3 &&
    newCard.cardNumber.replace(/\s/g, '').length >= 15 &&
    newCard.expiryMonth.length === 2 &&
    newCard.expiryYear.length === 2 &&
    newCard.cvc.length >= 3,
);

function submitPayment() {
  if (!canSubmit.value || !selectedPlan.value) return;
  emit('create', {
    amount: Number(selectedPlan.value.amount),
    description: selectedPlan.value.description || selectedPlan.value.name,
    paymentCardId: form.cardId,
    metadata: {
      paymentPlanId: selectedPlan.value.id,
      paymentPlanCode: selectedPlan.value.code,
      paymentPlanName: selectedPlan.value.name,
    },
  });
}

function toggleNewCard() {
  isAddingCard.value = !isAddingCard.value;
  if (!isAddingCard.value) {
    resetCardForm();
  }
}

function closeNewCard() {
  isAddingCard.value = false;
  resetCardForm();
}

function resetCardForm() {
  formattedCardNumber.value = '';
  formattedExpiry.value = '';
  newCard.cardNumber = '';
  newCard.expiryMonth = '';
  newCard.expiryYear = '';
  newCard.cvc = '';
  newCard.cardholder = '';
  newCard.setDefault = false;
}

function onCardNumberInput(event) {
  const value = event.target.value.replace(/\D/g, '').slice(0, 16);
  newCard.cardNumber = value;
  formattedCardNumber.value = value.replace(/(.{4})/g, '$1 ').trim();
}

function onExpiryInput(event) {
  const value = event.target.value.replace(/\D/g, '').slice(0, 4);
  if (value.length >= 2) {
    newCard.expiryMonth = value.slice(0, 2);
    newCard.expiryYear = value.slice(2, 4);
    formattedExpiry.value = `${newCard.expiryMonth}/${newCard.expiryYear}`;
  } else {
    newCard.expiryMonth = value;
    newCard.expiryYear = '';
    formattedExpiry.value = newCard.expiryMonth;
  }
}

function tokenizeCard() {
  if (!canTokenize.value) return;
  emit('tokenize', {
    cardNumber: newCard.cardNumber,
    expiryMonth: newCard.expiryMonth,
    expiryYear: newCard.expiryYear,
    cvc: newCard.cvc,
    cardholder: newCard.cardholder,
    setDefault: newCard.setDefault,
  });
}

function intervalLabel(interval) {
  const key = (interval ?? 'unknown').toLowerCase();
  return t(`payments.intervals.${key}`);
}

function formatCurrency(amount, currencyCode) {
  const localeTag = locale.value === 'pt' ? 'pt-BR' : 'en-US';
  return new Intl.NumberFormat(localeTag, {
    style: 'currency',
    currency: currencyCode || props.currency,
  }).format(amount ?? 0);
}

onMounted(() => {
  loadPlans();
});

defineExpose({ closeNewCard });
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


