<template>
  <form class="space-y-6" @submit.prevent="submit">
    <section class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
      <header class="flex items-start justify-between gap-4">
        <div class="space-y-1">
          <h3 class="text-lg font-semibold text-gray-800">{{ t('payments.recurring.selectPlanTitle') }}</h3>
          <p class="text-sm text-gray-500">{{ t('payments.recurring.selectPlanSubtitle') }}</p>
        </div>
        <span v-if="loadingPlans" class="text-xs text-gray-400">{{ t('payments.recurring.loading') }}</span>
      </header>
      <p
        v-if="planError"
        class="mt-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
      >
        {{ planError }}
      </p>
      <div v-else class="mt-4">
        <p
          v-if="!plans.length && !loadingPlans"
          class="rounded-xl border border-dashed border-gray-200 bg-gray-50 px-4 py-3 text-sm text-gray-500"
        >
          {{ t('payments.recurring.noPlans') }}
        </p>
        <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <button
            v-for="plan in plans"
            :key="plan.id"
            type="button"
            class="relative cursor-pointer rounded-xl border border-gray-200 bg-white p-4 text-left transition-all duration-300 hover:border-emerald-300 hover:shadow-md"
            :class="
              form.paymentPlanId === plan.id
                ? 'border-emerald-500 bg-emerald-50 shadow-sm ring-1 ring-emerald-400'
                : ''
            "
            @click="selectPlan(plan.id)"
          >
            <div class="space-y-2">
              <h4 class="font-semibold text-gray-800">{{ plan.name }}</h4>
              <p class="text-sm text-gray-500">
                {{ plan.description || intervalLabel(plan.interval) }}
              </p>
            </div>
            <div class="mt-4 space-y-1">
              <p class="text-2xl font-semibold text-gray-800">
                {{ formatCurrency(plan.amount, plan.currency) }}
              </p>
              <p class="text-xs uppercase tracking-wide text-gray-400">
                {{ intervalLabel(plan.interval) }}
              </p>
            </div>
            <div
              v-if="form.paymentPlanId === plan.id"
              class="absolute right-2 top-2 rounded-full bg-green-100 px-2 py-0.5 text-xs text-green-700"
            >
              ✔ {{ t('payments.recurring.planSelectedBadge') }}
            </div>
          </button>
        </div>
      </div>
    </section>

    <section class="rounded-xl border border-gray-200 bg-white p-4 text-sm text-gray-700 shadow-sm">
      <div class="flex items-center justify-between gap-4">
        <span>
          <template v-if="selectedPlan">
            {{ t('payments.recurring.selectedPlanTitle') }}:
            {{ selectedPlan.name }} · {{ formatCurrency(selectedPlan.amount, selectedPlan.currency) }} ·
            {{ intervalLabel(selectedPlan.interval) }}
          </template>
          <template v-else>
            {{ t('payments.recurring.selectedPlanEmpty') }}
          </template>
        </span>
        <span class="text-xs text-gray-400">
          {{ t('payments.recurring.methods.card.label') }}
        </span>
      </div>
    </section>

    <section class="space-y-4 rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
      <header class="flex items-center justify-between">
        <div>
          <h3 class="text-lg font-semibold text-gray-800">{{ t('payments.recurring.methodTitle') }}</h3>
          <p class="text-sm text-gray-500">
            {{ t('payments.recurring.methods.card.description') }}
          </p>
        </div>
        <span class="rounded-full bg-green-100 px-2 py-0.5 text-xs text-green-700">{{ t('common.boolean.yes') }}</span>
      </header>
      <div class="flex items-center justify-between rounded-xl border border-green-400 bg-green-50 p-4">
        <div class="flex items-center gap-3">
          <div class="h-3 w-3 rounded-full bg-green-500"></div>
          <p class="font-medium text-gray-800">{{ t('payments.recurring.methods.card.label') }}</p>
        </div>
        <p class="text-sm text-gray-600">
          {{ t('payments.recurring.methods.card.description') }}
        </p>
      </div>
    </section>

    <section
      v-if="showCardSelector"
      class="space-y-4 rounded-xl border border-gray-200 bg-white p-6 shadow-sm"
    >
      <header class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-800">{{ t('payments.recurring.cards.title') }}</h3>
        <button
          v-if="canManageCards && !props.cardEntryOnly"
          type="button"
          class="inline-flex items-center gap-2 rounded-lg border border-emerald-200 px-3 py-1 text-sm font-medium text-emerald-600 transition hover:border-emerald-300 hover:bg-emerald-50"
          @click="toggleCardForm"
        >
          <component :is="showCardForm ? XMarkIcon : PlusIcon" class="h-4 w-4" />
          <span>
            {{ showCardForm ? t('payments.cardForm.cancelAddCard') : t('payments.cardForm.addCardButton') }}
          </span>
        </button>
      </header>

      <p
        v-if="cardsError && !props.cardEntryOnly"
        class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
      >
        {{ cardsError }}
      </p>

      <div v-if="!props.cardEntryOnly">
        <div v-if="loadingCards" class="flex items-center gap-2 text-sm text-gray-500">
          <span class="inline-flex h-4 w-4 animate-spin rounded-full border-2 border-emerald-200 border-t-transparent"></span>
          <span>{{ t('payments.recurring.cards.loading') }}</span>
        </div>

        <div v-else-if="hasCards" class="space-y-3">
          <div
            v-for="card in managedCards"
            :key="card.id"
            class="flex items-center justify-between gap-3 rounded-xl border border-gray-200 bg-white p-4 transition hover:border-emerald-300 hover:shadow-md"
            :class="form.paymentMethodId === card.id ? 'border-emerald-500 bg-emerald-50' : ''"
          >
            <label class="flex flex-1 cursor-pointer items-center gap-3" :for="`recurring-card-${card.id}`">
              <input
                :id="`recurring-card-${card.id}`"
                v-model="form.paymentMethodId"
                type="radio"
                name="recurringCardId"
                :value="card.id"
                class="sr-only"
              />
              <div :class="['flex h-10 w-10 items-center justify-center rounded-full border font-semibold text-sm', brandMeta(card.brand).class]">
                {{ brandMeta(card.brand).initial }}
              </div>
              <div>
                <p class="font-medium text-gray-800">
                  {{ brandMeta(card.brand).label }} •••• {{ card.lastFour }}
                </p>
                <p class="text-xs text-gray-500">
                  {{ t('payments.recurring.cards.expires', { month: padMonth(card.expiryMonth), year: card.expiryYear }) }}
                </p>
              </div>
            </label>
            <div class="flex items-center gap-3">
              <span
                v-if="card.defaultCard"
                class="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700"
              >
                {{ t('payments.recurring.cards.defaultBadge') }}
              </span>
              <button
                v-if="canManageCards"
                type="button"
                class="rounded-full p-2 text-gray-400 transition hover:bg-rose-50 hover:text-rose-600 disabled:opacity-60"
                :disabled="deletingCardId === card.id"
                @click="removeCard(card)"
              >
                <TrashIcon class="h-5 w-5" />
                <span class="sr-only">{{ t('payments.recurring.cards.delete') }}</span>
              </button>
            </div>
          </div>
        </div>

        <p v-else class="rounded-xl border border-dashed border-gray-200 bg-gray-50 p-4 text-sm text-gray-500">
          {{ t('payments.recurring.cards.empty') }}
        </p>
      </div>

      <transition name="fade">
        <section
          v-if="cardFormVisible"
          class="space-y-4 rounded-2xl border border-emerald-100 bg-emerald-50 p-4"
        >
          <header class="space-y-1">
            <h4 class="text-sm font-semibold text-emerald-800">{{ t('payments.cardForm.tokenizationTitle') }}</h4>
            <p class="text-xs text-emerald-700">
              {{ t('payments.cardForm.tokenizationDescription') }}
            </p>
          </header>
          <div class="grid gap-4 md:grid-cols-2">
            <label class="flex flex-col gap-1 text-xs font-semibold uppercase tracking-wide text-emerald-800">
              {{ t('payments.cardForm.fields.cardholder') }}
              <input v-model="newCard.cardholder" type="text" class="rounded-lg border border-emerald-200 p-2 text-sm" />
            </label>
            <label class="flex flex-col gap-1 text-xs font-semibold uppercase tracking-wide text-emerald-800">
              {{ t('payments.cardForm.fields.number') }}
              <input
                v-model="formattedCardNumber"
                type="text"
                class="rounded-lg border border-emerald-200 p-2 font-mono text-sm"
                inputmode="numeric"
                maxlength="19"
                :placeholder="t('payments.cardForm.placeholders.number')"
                @input="onCardNumberInput"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs font-semibold uppercase tracking-wide text-emerald-800">
              {{ t('payments.cardForm.fields.expiry') }}
              <input
                v-model="formattedExpiry"
                type="text"
                class="rounded-lg border border-emerald-200 p-2 font-mono text-sm"
                inputmode="numeric"
                maxlength="5"
                :placeholder="t('payments.cardForm.placeholders.expiry')"
                @input="onExpiryInput"
              />
            </label>
            <label class="flex flex-col gap-1 text-xs font-semibold uppercase tracking-wide text-emerald-800">
              {{ t('payments.cardForm.fields.cvc') }}
              <input
                v-model="newCard.cvc"
                type="password"
                class="rounded-lg border border-emerald-200 p-2 text-sm"
                inputmode="numeric"
                maxlength="4"
              />
            </label>
          </div>
          <div class="flex items-center justify-between">
            <label class="flex items-center gap-2 text-xs font-medium text-emerald-800">
              <input
                v-model="newCard.setDefault"
                type="checkbox"
                class="h-4 w-4 rounded border-emerald-300 text-emerald-600 focus:ring-emerald-500"
              />
              {{ t('payments.cardForm.setDefault') }}
            </label>
            <button
              v-if="!props.cardEntryOnly"
              type="button"
              class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:opacity-60"
              :disabled="tokenizing || !canTokenize"
              @click="tokenizeCard"
            >
              <span
                v-if="tokenizing"
                class="inline-flex h-4 w-4 animate-spin rounded-full border-2 border-emerald-200 border-t-transparent"
              ></span>
              <span>
                {{ tokenizing ? t('payments.cardForm.tokenizing') : t('payments.cardForm.addingCard') }}
              </span>
            </button>
          </div>
          <p class="text-xs text-emerald-700">{{ t('payments.cardForm.pciNotice') }}</p>
        </section>
      </transition>
    </section>

    <section class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
      <label class="flex items-start gap-2 text-sm text-gray-600">
        <input
          v-model="form.acceptedTerms"
          type="checkbox"
          class="mt-1 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500"
          required
        />
        {{ t('payments.recurring.termsNotice') }}
      </label>
      <div class="mt-4 flex justify-end">
        <button
          type="submit"
          class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-6 py-3 font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="!canSubmit || loading"
        >
          <span
            v-if="loading"
            class="inline-flex h-4 w-4 animate-spin rounded-full border-2 border-emerald-200 border-t-transparent"
          ></span>
          <span>{{ loading ? t('payments.recurring.submitLoading') : t('payments.recurring.submit') }}</span>
        </button>
      </div>
    </section>
  </form>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { PlusIcon, TrashIcon, XMarkIcon } from '@heroicons/vue/24/outline';
import { addCard, deleteCard, listCards, listPaymentPlans } from '@/services/payments';
import { useNotificationStore } from '@/stores/notifications';

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
  selectedPlan: {
    type: Object,
    default: null,
  },
  ensureCustomer: {
    type: Function,
    default: null,
  },
  cardEntryOnly: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['create']);

const { t, locale } = useI18n();
const notifications = useNotificationStore();

const stripePublishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY ?? '';

const localeTag = computed(() => (locale.value === 'pt' ? 'pt-BR' : 'en-US'));

const plans = ref([]);
const loadingPlans = ref(false);
const planError = ref('');
const pendingPlanId = ref(null);

const internalCards = ref([]);
const loadingCards = ref(false);
const cardsError = ref('');
const deletingCardId = ref(null);
const showCardForm = ref(props.cardEntryOnly);
const tokenizing = ref(false);

const form = reactive({
  paymentPlanId: null,
  immediateCharge: true,
  paymentMethod: 'CREDIT_CARD',
  paymentMethodId: null,
  stripeCustomerId: '',
  asaasCustomerId: '',
  acceptedTerms: false,
});

const newCard = reactive({
  cardholder: '',
  cardNumber: '',
  expiryMonth: '',
  expiryYear: '',
  cvc: '',
  setDefault: true,
});

const formattedCardNumber = ref('');
const formattedExpiry = ref('');

const managedCards = computed(() =>
  props.cardEntryOnly ? [] : (props.cards?.length ? props.cards : internalCards.value),
);
const canManageCards = computed(() => typeof props.ensureCustomer === 'function' && !props.cardEntryOnly);
const hasCards = computed(() => managedCards.value.length > 0);

const brandLabels = computed(() => ({
  visa: t('payments.cardBrands.visa'),
  mastercard: t('payments.cardBrands.mastercard'),
  americanexpress: t('payments.cardBrands.amex'),
  amex: t('payments.cardBrands.amex'),
}));

const selectedPlan = computed(() => plans.value.find((plan) => plan.id === form.paymentPlanId) || null);
const showCardSelector = computed(() => form.paymentMethod === 'CREDIT_CARD');
const cardFormVisible = computed(() => (props.cardEntryOnly ? true : showCardForm.value && canManageCards.value));

const canSubmit = computed(() => {
  if (!form.paymentPlanId || !form.acceptedTerms) {
    return false;
  }
  if (!showCardSelector.value) {
    return true;
  }
  if (props.cardEntryOnly) {
    return canTokenize.value;
  }
  return Boolean(form.paymentMethodId);
});

const canTokenize = computed(
  () =>
    newCard.cardholder.trim().length > 3 &&
    newCard.cardNumber.replace(/\s/g, '').length >= 15 &&
    newCard.expiryMonth.length === 2 &&
    newCard.expiryYear.length === 2 &&
    newCard.cvc.length >= 3,
);

watch(
  () => props.selectedPlan,
  (plan) => {
    pendingPlanId.value = plan?.id ?? null;
    ensureSelectedPlan();
  },
  { immediate: true, deep: true },
);

watch(
  () => managedCards.value,
  (value) => {
    if (props.cardEntryOnly) {
      return;
    }
    if (value?.length) {
      const defaultCard = value.find((card) => card.defaultCard) ?? value[0];
      form.paymentMethodId = defaultCard.id;
    } else if (showCardSelector.value) {
      form.paymentMethodId = null;
    }
  },
  { immediate: true, deep: true },
);

watch(plans, () => {
  ensureSelectedPlan();
});

async function loadPlans() {
  loadingPlans.value = true;
  planError.value = '';
  try {
    const { data } = await listPaymentPlans();
    plans.value = data ?? [];
    if (plans.value.length) {
      const existing = plans.value.find((plan) => plan.id === form.paymentPlanId);
      if (existing) {
        selectPlan(existing.id);
      } else {
        ensureSelectedPlan();
        if (!form.paymentPlanId && plans.value.length) {
          selectPlan(plans.value[0].id);
        }
      }
    } else {
      pendingPlanId.value = null;
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
  const classes = {
    visa: 'border-emerald-200 bg-emerald-100 text-emerald-600',
    mastercard: 'border-orange-200 bg-orange-100 text-orange-600',
    americanexpress: 'border-teal-200 bg-teal-100 text-teal-600',
    amex: 'border-teal-200 bg-teal-100 text-teal-600',
  };
  const fallbackLabel = brand || t('payments.cardBrands.generic');
  const label = brandLabels.value[key] || fallbackLabel;
  const className = classes[key] || 'border-slate-200 bg-slate-100 text-slate-600';
  return {
    label,
    initial: (label || 'C').charAt(0).toUpperCase(),
    class: className,
  };
}

function padMonth(month) {
  if (!month && month !== 0) return '00';
  const value = String(month);
  return value.padStart(2, '0');
}

function selectPlan(planId) {
  form.paymentPlanId = planId;
}

function ensureSelectedPlan() {
  if (!pendingPlanId.value) return;
  const match = plans.value.find((plan) => plan.id === pendingPlanId.value);
  if (match) {
    selectPlan(match.id);
    pendingPlanId.value = null;
  }
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

async function loadCards(options = {}) {
  if (!canManageCards.value) return;
  const silent = Boolean(options?.silent);
  if (!silent) {
    cardsError.value = '';
  }

  try {
    const customer = await props.ensureCustomer?.({ silent });
    if (!customer?.id) {
      return;
    }
    loadingCards.value = true;
    const { data } = await listCards(customer.id);
    internalCards.value = data ?? [];
    if (!internalCards.value.length && !silent) {
      showCardForm.value = true;
    }
  } catch (error) {
    if (!silent) {
      if (error?.message === 'form-invalid') {
        cardsError.value = t('payments.recurring.cards.ensureCustomer');
      } else {
        cardsError.value = error?.response?.data?.message ?? t('payments.recurring.cards.error');
      }
    }
    throw error;
  } finally {
    loadingCards.value = false;
  }
}

async function toggleCardForm() {
  if (props.cardEntryOnly) {
    return;
  }
  if (showCardForm.value) {
    showCardForm.value = false;
    resetCardForm();
    return;
  }

  showCardForm.value = true;
  try {
    await loadCards({ silent: false });
  } catch (error) {
    if (error?.message === 'form-invalid') {
      cardsError.value = t('payments.recurring.cards.ensureCustomer');
    }
  }
}

function resetCardForm() {
  formattedCardNumber.value = '';
  formattedExpiry.value = '';
  newCard.cardholder = '';
  newCard.cardNumber = '';
  newCard.expiryMonth = '';
  newCard.expiryYear = '';
  newCard.cvc = '';
  newCard.setDefault = true;
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

async function tokenizeCard() {
  if (!canManageCards.value || !canTokenize.value || tokenizing.value) return;
  if (!stripePublishableKey || !window.Stripe) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.tokenizationTitle'),
      message: t('payments.notifications.stripeNotLoaded'),
    });
    return;
  }

  try {
    tokenizing.value = true;
    const customer = await props.ensureCustomer?.({ silent: false });
    if (!customer?.id) {
      return;
    }

    const stripe = window.Stripe(stripePublishableKey);
    const { token, error } = await stripe.createToken('card', {
      number: newCard.cardNumber,
      exp_month: Number(newCard.expiryMonth),
      exp_year: Number(`20${newCard.expiryYear}`),
      cvc: newCard.cvc,
      name: newCard.cardholder,
    });

    if (error) {
      throw error;
    }

    await addCard({
      customerId: customer.id,
      cardToken: token.id,
      brand: token.card.brand,
      lastFour: token.card.last4,
      expiryMonth: token.card.exp_month,
      expiryYear: token.card.exp_year,
      defaultCard: newCard.setDefault,
    });

    notifications.push({
      type: 'success',
      title: t('payments.notifications.cardSavedTitle'),
      message: t('payments.notifications.cardSavedMessage'),
    });

    await loadCards({ silent: true });
    showCardForm.value = false;
    resetCardForm();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.tokenizationFailedTitle'),
      message: error?.message ?? t('payments.notifications.tokenizationFailedMessage'),
    });
    cardsError.value = error?.message ?? t('payments.recurring.cards.tokenizationError');
  } finally {
    tokenizing.value = false;
  }
}

async function removeCard(card) {
  if (!canManageCards.value || deletingCardId.value) return;
  try {
    const customer = await props.ensureCustomer?.({ silent: false });
    if (!customer?.id) {
      return;
    }
    deletingCardId.value = card.id;
    await deleteCard(card.id, customer.id);
    notifications.push({
      type: 'success',
      title: t('payments.notifications.cardsTitle'),
      message: t('payments.notifications.cardDeletedMessage'),
    });
    await loadCards({ silent: true });
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.cardsTitle'),
      message: error?.response?.data?.message ?? t('payments.notifications.cardsLoadError'),
    });
  } finally {
    deletingCardId.value = null;
  }
}

function submit() {
  if (!canSubmit.value || !selectedPlan.value) return;
  const payload = {
    paymentPlanId: form.paymentPlanId,
    immediateCharge: form.immediateCharge,
    paymentMethod: form.paymentMethod,
    metadata: {
      stripeCustomerId: form.stripeCustomerId || undefined,
      asaasCustomerId: form.asaasCustomerId || undefined,
    },
  };

  if (showCardSelector.value) {
    if (props.cardEntryOnly) {
      payload.card = {
        cardholder: newCard.cardholder,
        cardNumber: newCard.cardNumber,
        expiryMonth: newCard.expiryMonth,
        expiryYear: newCard.expiryYear,
        cvc: newCard.cvc,
        setDefault: newCard.setDefault,
      };
    } else {
      payload.paymentMethodId = form.paymentMethodId;
    }
  }

  emit('create', payload);
}

onMounted(() => {
  loadPlans();
  if (canManageCards.value) {
    loadCards({ silent: true }).catch(() => undefined);
  }
});
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
