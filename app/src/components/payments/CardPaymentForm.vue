<template>
  <form class="flex flex-col gap-6" @submit.prevent="submitPayment">
    <section class="grid gap-4 md:grid-cols-2">
      <label class="flex flex-col gap-1">
        <span class="text-sm font-medium text-slate-600">Amount ({{ currency }})</span>
        <input
          v-model.number="form.amount"
          type="number"
          min="1"
          step="0.01"
          placeholder="0.00"
          class="input"
          required
        />
      </label>
      <label class="flex flex-col gap-1">
        <span class="text-sm font-medium text-slate-600">Description</span>
        <input
          v-model="form.description"
          type="text"
          maxlength="140"
          placeholder="What is this payment for?"
          class="input"
        />
      </label>
    </section>

    <section class="space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700">Stored cards</h3>
        <button type="button" class="btn-text" @click="toggleNewCard">
          {{ isAddingCard ? 'Cancel' : 'Add new card' }}
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
        No saved cards. Add a new one below using secure tokenization.
      </p>
    </section>

    <transition name="fade">
      <section v-if="isAddingCard" class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
        <header class="mb-4 space-y-1">
          <h3 class="text-sm font-semibold text-slate-700">Secure card tokenization</h3>
          <p class="text-xs text-slate-500">
            Card details are sent directly to the payment gateway and never touch our servers. Complete the
            fields and click "Tokenize card".
          </p>
        </header>
        <div class="grid gap-4 md:grid-cols-2">
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">Cardholder name</span>
            <input v-model="newCard.cardholder" type="text" class="input" autocomplete="cc-name" required />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">Card number</span>
            <input
              v-model="formattedCardNumber"
              type="text"
              class="input font-mono"
              inputmode="numeric"
              autocomplete="cc-number"
              maxlength="19"
              placeholder="0000 0000 0000 0000"
              @input="onCardNumberInput"
              required
            />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">Expiry (MM/YY)</span>
            <input
              v-model="formattedExpiry"
              type="text"
              class="input font-mono"
              inputmode="numeric"
              maxlength="5"
              placeholder="12/29"
              autocomplete="cc-exp"
              @input="onExpiryInput"
              required
            />
          </label>
          <label class="flex flex-col gap-1">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">CVC</span>
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
            Set as default payment method
          </label>
          <button type="button" class="btn-primary" :disabled="tokenizing || !canTokenize" @click="tokenizeCard">
            <span v-if="tokenizing" class="loader h-4 w-4" />
            <span>{{ tokenizing ? 'Tokenizing…' : 'Tokenize card' }}</span>
          </button>
        </div>
      </section>
    </transition>

    <footer class="flex items-center justify-between">
      <p class="text-xs text-slate-500">
        By confirming the payment you agree to process the transaction according to PCI DSS.
      </p>
      <button type="submit" class="btn-primary" :disabled="!canSubmit || loading">
        <span v-if="loading" class="loader h-4 w-4" />
        <span>{{ loading ? 'Processing…' : 'Create payment intent' }}</span>
      </button>
    </footer>
  </form>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';

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

const form = reactive({
  amount: 0,
  description: '',
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

const canSubmit = computed(() => form.amount > 0 && Boolean(form.cardId));
const canTokenize = computed(
  () =>
    newCard.cardholder.length > 3 &&
    newCard.cardNumber.replace(/\s/g, '').length >= 15 &&
    newCard.expiryMonth.length === 2 &&
    newCard.expiryYear.length === 2 &&
    newCard.cvc.length >= 3,
);

function submitPayment() {
  if (!canSubmit.value) return;
  emit('create', {
    amount: Number(form.amount),
    description: form.description,
    paymentCardId: form.cardId,
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

