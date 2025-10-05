<template>
  <div class="grid gap-6 xl:grid-cols-2">
    <div class="space-y-6">
      <PaymentMethodSelection v-model="currentMethod" />

      <CardPaymentForm
        v-if="currentMethod === 'CARD'"
        ref="cardFormRef"
        :cards="cards"
        :currency="currency"
        :loading="intentLoading"
        :tokenizing="tokenizing"
        @create="handleCreateCardPayment"
        @tokenize="tokenizeCard"
      />

      <div v-else-if="currentMethod === 'PIX'" class="space-y-6">
        <form class="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm" @submit.prevent="handleCreatePix">
          <header class="space-y-1">
            <h2 class="text-lg font-semibold text-slate-900">Generate PIX payment</h2>
            <p class="text-sm text-slate-500">Provide the amount, description, and the Asaas customer identifier configured in your gateway.</p>
          </header>
          <div class="grid gap-4 md:grid-cols-2">
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-600">Amount ({{ currency }})</span>
              <input v-model.number="pixForm.amount" type="number" min="1" step="0.01" required class="input" />
            </label>
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-slate-600">PIX Asaas customer ID</span>
              <input v-model="pixForm.asaasCustomerId" type="text" placeholder="cus_asaas_123" required class="input" />
            </label>
          </div>
          <label class="flex flex-col gap-1">
            <span class="text-sm font-medium text-slate-600">Description</span>
            <input v-model="pixForm.description" type="text" maxlength="140" class="input" placeholder="PIX payment description" />
          </label>
          <div class="flex justify-end">
            <button type="submit" class="btn-primary" :disabled="pixLoading">
              <span v-if="pixLoading" class="loader h-4 w-4"></span>
              <span>{{ pixLoading ? 'Generating…' : 'Generate PIX' }}</span>
            </button>
          </div>
        </form>

        <PIXPayment v-if="pixPayment" :payment="pixPayment" @refresh="refreshPix" />
      </div>

      <RecurringPayment
        v-else
        :cards="cards"
        :currency="currency"
        :loading="recurringLoading"
        @create="handleCreateSubscription"
      />
    </div>

    <div class="space-y-6">
      <PaymentsDataTable
        :rows="payments"
        :loading="paymentsLoading"
        :pagination="pagination"
        :sort="sort"
        :selected="selectedPayments"
        :filters="filters"
        @update:filters="updateFilters"
        @update:selected="(value) => (selectedPayments.value = value)"
        @change:sort="changeSort"
        @change:page="changePage"
        @change:per-page="changePerPage"
        @refresh="loadPayments"
        @export:csv="exportCsv"
        @bulk:refund="confirmBulkRefund"
        @view="openPaymentDetails"
        @refund="confirmRefund"
      />

      <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">Integration checklist</h2>
        <ul class="mt-3 space-y-2 text-sm text-slate-600">
          <li>- Configure <code class="font-mono">VITE_STRIPE_PUBLISHABLE_KEY</code> in the frontend and <code>payments.stripe.secret-key</code> in the backend.</li>
          <li>- Provide <code>asaasCustomerId</code> metadata for PIX charges and <code>stripeCustomerId</code> for card subscriptions.</li>
          <li>- Add webhook endpoints in Stripe and Asaas pointing to <code>/api/webhooks/payment</code>.</li>
        </ul>
      </section>
    </div>
  </div>

  <transition name="fade">
    <div v-if="selectedPayment" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 px-4">
      <div class="w-full max-w-2xl space-y-4 rounded-3xl bg-white p-6 shadow-2xl">
        <header class="flex items-start justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">Payment details</h3>
            <p class="text-sm text-slate-500">Gateway reference: {{ selectedPayment.paymentId ?? '—' }}</p>
          </div>
          <button type="button" class="btn-ghost" @click="selectedPayment = null">Close</button>
        </header>
        <dl class="grid gap-4 md:grid-cols-2">
          <div>
            <dt class="text-xs uppercase tracking-wide text-slate-500">Status</dt>
            <dd class="text-sm font-semibold">{{ selectedPayment.status }}</dd>
          </div>
          <div>
            <dt class="text-xs uppercase tracking-wide text-slate-500">Method</dt>
            <dd class="text-sm text-slate-700">{{ selectedPayment.paymentMethod }}</dd>
          </div>
          <div>
            <dt class="text-xs uppercase tracking-wide text-slate-500">Amount</dt>
            <dd class="text-sm text-slate-700">{{ formatCurrency(selectedPayment.amount) }}</dd>
          </div>
          <div>
            <dt class="text-xs uppercase tracking-wide text-slate-500">Created</dt>
            <dd class="text-sm text-slate-700">{{ formatDate(selectedPayment.createdAt) }}</dd>
          </div>
          <div class="md:col-span-2">
            <dt class="text-xs uppercase tracking-wide text-slate-500">Metadata</dt>
            <dd class="mt-1 max-h-40 overflow-auto rounded-xl bg-slate-50 p-3 font-mono text-xs text-slate-700">
              {{ selectedPayment.metadata ? JSON.stringify(selectedPayment.metadata, null, 2) : '—' }}
            </dd>
          </div>
        </dl>
      </div>
    </div>
  </transition>

  <ConfirmDialog
    v-model="showRefundDialog"
    title="Refund payment"
    :message="refundMessage"
    confirm-label="Refund"
    @confirm="executeRefund"
  />
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import PaymentMethodSelection from '@/components/payments/PaymentMethodSelection.vue';
import CardPaymentForm from '@/components/payments/CardPaymentForm.vue';
import PIXPayment from '@/components/payments/PIXPayment.vue';
import RecurringPayment from '@/components/payments/RecurringPayment.vue';
import PaymentsDataTable from '@/components/payments/PaymentsDataTable.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import {
  addCard,
  createPaymentIntent,
  createPixPayment,
  createSubscription,
  getPayment,
  listCards,
  listPayments,
  refundPayment,
} from '@/services/payments';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';

const auth = useAuthStore();
const notifications = useNotificationStore();

const currency = 'BRL';
const currentMethod = ref('CARD');
const cards = ref([]);
const cardFormRef = ref(null);

const intentLoading = ref(false);
const recurringLoading = ref(false);
const tokenizing = ref(false);

const pixLoading = ref(false);
const pixPayment = ref(null);
const pixForm = reactive({ amount: null, description: '', asaasCustomerId: '' });
let pixPollingHandle;

const payments = ref([]);
const paymentsLoading = ref(false);
const pagination = reactive({ page: 1, perPage: 10, total: 0, lastPage: 1, from: 0, to: 0 });
const sort = ref({ field: 'createdAt', direction: 'desc' });
const filters = reactive({ search: '', status: '', paymentMethod: '', startDate: '', endDate: '' });
let filtersTimeout;
const selectedPayments = ref([]);

const showRefundDialog = ref(false);
const refundContext = ref(null);
const selectedPayment = ref(null);

const stripePublishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;

const refundMessage = computed(() => {
  if (!refundContext.value) return 'Confirm refund?';
  if (refundContext.value.type === 'bulk') {
    return `Refund ${refundContext.value.ids.length} payments?`;
  }
  return `Refund payment ${refundContext.value.payment?.paymentId ?? refundContext.value.payment?.id}?`;
});

watch(
  () => auth.user?.id,
  (value) => {
    if (value) {
      loadCards();
      loadPayments();
    }
  },
  { immediate: true },
);

watch(
  () => currentMethod.value,
  (value) => {
    if (value !== 'PIX') {
      stopPixPolling();
    }
  },
);

function updateFilters(newFilters) {
  Object.assign(filters, newFilters);
  window.clearTimeout(filtersTimeout);
  filtersTimeout = window.setTimeout(() => {
    pagination.page = 1;
    loadPayments();
  }, 150);
}

function changeSort(value) {
  sort.value = value;
  loadPayments();
}

function changePage(page) {
  pagination.page = page;
  loadPayments();
}

function changePerPage(perPage) {
  pagination.perPage = perPage;
  pagination.page = 1;
  loadPayments();
}

async function loadCards() {
  if (!auth.user?.id) return;
  try {
    const { data } = await listCards(auth.user.id);
    cards.value = data ?? [];
  } catch (error) {
    notifications.push({ type: 'error', title: 'Cards', message: 'Unable to load payment methods.' });
  }
}

async function loadPayments() {
  if (!auth.user?.id) return;
  paymentsLoading.value = true;
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.perPage,
      sort: sort.value.field ? `${sort.value.field},${sort.value.direction}` : undefined,
      ...sanitizeFilters(filters),
    };
    const { data } = await listPayments(params);
    payments.value = data.content ?? [];
    pagination.page = (data.number ?? 0) + 1;
    pagination.perPage = data.size ?? pagination.perPage;
    pagination.total = data.totalElements ?? payments.value.length;
    pagination.lastPage = data.totalPages ?? 1;
    pagination.from = data.numberOfElements ? data.number * data.size + 1 : 0;
    pagination.to = data.numberOfElements ? pagination.from + data.numberOfElements - 1 : 0;
  } catch (error) {
    notifications.push({ type: 'error', title: 'Payments', message: 'Unable to load payments.' });
  } finally {
    paymentsLoading.value = false;
  }
}

function sanitizeFilters(filtersObj) {
  const payload = {};
  if (filtersObj.search) payload.search = filtersObj.search;
  if (filtersObj.status) payload.status = filtersObj.status;
  if (filtersObj.paymentMethod) payload.paymentMethod = filtersObj.paymentMethod;
  if (filtersObj.startDate) payload.startDate = `${filtersObj.startDate}T00:00:00Z`;
  if (filtersObj.endDate) payload.endDate = `${filtersObj.endDate}T23:59:59Z`;
  return payload;
}

async function handleCreateCardPayment(payload) {
  if (!auth.user?.id) return;
  intentLoading.value = true;
  try {
    const request = {
      customerId: auth.user.id,
      amount: payload.amount,
      description: payload.description,
      paymentMethod: 'CREDIT_CARD',
      paymentCardId: payload.paymentCardId,
    };
    const { data } = await createPaymentIntent(request);
    notifications.push({ type: 'success', title: 'Payment intent created', message: `Status: ${data.status}` });
    loadPayments();
  } catch (error) {
    notifications.push({ type: 'error', title: 'Payment intent', message: 'Unable to create payment intent.' });
  } finally {
    intentLoading.value = false;
  }
}

async function tokenizeCard(cardInput) {
  if (!auth.user?.id) return;
  if (!stripePublishableKey || !window.Stripe) {
    notifications.push({ type: 'error', title: 'Stripe tokenization', message: 'Stripe.js is not loaded or publishable key is missing.' });
    return;
  }
  tokenizing.value = true;
  try {
    const stripe = window.Stripe(stripePublishableKey);
    const { token, error } = await stripe.createToken('card', {
      number: cardInput.cardNumber,
      exp_month: Number(cardInput.expiryMonth),
      exp_year: Number(`20${cardInput.expiryYear}`),
      cvc: cardInput.cvc,
      name: cardInput.cardholder,
    });
    if (error) throw error;

    await addCard({
      customerId: auth.user.id,
      cardToken: token.id,
      brand: token.card.brand,
      lastFour: token.card.last4,
      expiryMonth: token.card.exp_month,
      expiryYear: token.card.exp_year,
      defaultCard: cardInput.setDefault,
    });
    notifications.push({ type: 'success', title: 'Card saved', message: 'Tokenized card added successfully.' });
    await loadCards();
    cardFormRef.value?.closeNewCard();
  } catch (error) {
    notifications.push({ type: 'error', title: 'Tokenization failed', message: error?.message ?? 'Unable to tokenize card.' });
  } finally {
    tokenizing.value = false;
  }
}

async function handleCreatePix() {
  if (!auth.user?.id) return;
  if (!pixForm.amount || !pixForm.asaasCustomerId) {
    notifications.push({ type: 'warning', title: 'PIX', message: 'Amount and Asaas customer ID are required.' });
    return;
  }
  pixLoading.value = true;
  try {
    const { data } = await createPixPayment({
      customerId: auth.user.id,
      amount: Number(pixForm.amount),
      description: pixForm.description,
      metadata: { asaasCustomerId: pixForm.asaasCustomerId },
    });
    pixPayment.value = data;
    notifications.push({ type: 'success', title: 'PIX generated', message: 'Share the PIX payload with the customer.' });
    startPixPolling(data.id);
    loadPayments();
  } catch (error) {
    notifications.push({ type: 'error', title: 'PIX', message: 'Unable to generate PIX payment.' });
  } finally {
    pixLoading.value = false;
  }
}

async function refreshPix() {
  if (!pixPayment.value?.id) return;
  await updatePixStatus(pixPayment.value.id);
}

function startPixPolling(paymentId) {
  stopPixPolling();
  pixPollingHandle = window.setInterval(() => updatePixStatus(paymentId), 5000);
}

async function updatePixStatus(paymentId) {
  try {
    const { data } = await getPayment(paymentId);
    pixPayment.value = mapPaymentToPix(data);
    if (!['PENDING', 'PROCESSING'].includes(data.status ?? data.paymentStatus)) {
      stopPixPolling();
      loadPayments();
    }
  } catch (error) {
    notifications.push({ type: 'error', title: 'PIX status', message: 'Unable to refresh PIX status.' });
  }
}

function stopPixPolling() {
  if (pixPollingHandle) {
    window.clearInterval(pixPollingHandle);
    pixPollingHandle = undefined;
  }
}

function mapPaymentToPix(payment) {
  const metadata = payment.metadata ?? {};
  return {
    id: payment.id,
    paymentId: payment.paymentId,
    status: payment.status ?? payment.paymentStatus,
    amount: payment.amount,
    payload: metadata.pixPayload,
    qrCodeImage: metadata.pixQrCode,
    pixKey: metadata.pixKey,
    expiresAt: metadata.expiresAt,
  };
}

async function handleCreateSubscription(payload) {
  if (!auth.user?.id) return;
  if (payload.paymentMethod === 'PIX' && !payload.metadata?.asaasCustomerId) {
    notifications.push({ type: 'warning', title: 'Subscription', message: 'Asaas customer ID is required for PIX subscriptions.' });
    return;
  }
  if (payload.paymentMethod !== 'PIX' && !payload.metadata?.stripeCustomerId) {
    notifications.push({ type: 'warning', title: 'Subscription', message: 'Stripe customer ID is required for card subscriptions.' });
    return;
  }
  recurringLoading.value = true;
  try {
    const request = {
      customerId: auth.user.id,
      planId: payload.planId,
      paymentMethodId: payload.paymentMethodId,
      interval: payload.interval,
      paymentMethod: payload.paymentMethod,
      amount: payload.amount,
      immediateCharge: payload.immediateCharge,
      metadata: {
        ...(payload.metadata?.stripeCustomerId ? { stripeCustomerId: payload.metadata.stripeCustomerId } : {}),
        ...(payload.metadata?.asaasCustomerId ? { asaasCustomerId: payload.metadata.asaasCustomerId } : {}),
      },
    };
    const { data } = await createSubscription(request);
    notifications.push({ type: 'success', title: 'Subscription created', message: `Gateway ID: ${data.subscriptionId}` });
    loadPayments();
  } catch (error) {
    notifications.push({ type: 'error', title: 'Subscription', message: 'Unable to create recurring payment.' });
  } finally {
    recurringLoading.value = false;
  }
}

function openPaymentDetails(payment) {
  selectedPayment.value = payment;
}

function confirmRefund(payment) {
  refundContext.value = { type: 'single', payment };
  showRefundDialog.value = true;
}

function confirmBulkRefund(ids) {
  if (!ids?.length) return;
  refundContext.value = { type: 'bulk', ids };
  showRefundDialog.value = true;
}

async function executeRefund() {
  if (!refundContext.value) return;
  try {
    if (refundContext.value.type === 'bulk') {
      await Promise.all(refundContext.value.ids.map((id) => refundPayment(id, {})));
      notifications.push({ type: 'success', title: 'Refunds', message: 'Bulk refund scheduled.' });
    } else if (refundContext.value.payment) {
      await refundPayment(refundContext.value.payment.id, {});
      notifications.push({ type: 'success', title: 'Refund', message: 'Payment refunded successfully.' });
    }
    loadPayments();
    selectedPayments.value = [];
  } catch (error) {
    notifications.push({ type: 'error', title: 'Refund', message: 'Unable to process refund.' });
  } finally {
    refundContext.value = null;
  }
}

function exportCsv() {
  if (!payments.value.length) {
    notifications.push({ type: 'warning', title: 'Export', message: 'No data available for export.' });
    return;
  }
  const headers = ['ID', 'Gateway ID', 'Method', 'Status', 'Amount', 'Currency', 'Created At'];
  const rows = payments.value.map((item) => [
    item.id,
    item.paymentId,
    item.paymentMethod,
    item.status,
    item.amount,
    item.currency,
    item.createdAt,
  ]);
  const csv = [headers, ...rows]
    .map((row) => row.map((value) => `"${value ?? ''}"`).join(','))
    .join('\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `payments-${Date.now()}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency }).format(value ?? 0);
}

function formatDate(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}

onMounted(() => {
  if (auth.token && !auth.user) {
    auth.loadProfile?.();
  }
});

onBeforeUnmount(() => {
  stopPixPolling();
});
</script>

