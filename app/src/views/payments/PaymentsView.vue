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
              <span>{{ pixLoading ? 'Generating...' : 'Generate PIX' }}</span>
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
        v-if="isAdmin"
        :rows="payments"
        :loading="paymentsLoading"
        :pagination="pagination"
        :sort="sort"
        :selected="selectedPayments"
        :filters="filters"
        @update:filters="updateFilters"
        @update:selected="onUpdateSelected"
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
        <h2 class="text-lg font-semibold text-slate-900">Recenty Activity</h2>
        <div v-if="paymentsLoading" class="mt-4 space-y-3">
          <div class="h-5 w-full animate-pulse rounded bg-slate-200"></div>
          <div class="h-5 w-full animate-pulse rounded bg-slate-200"></div>
          <div class="h-5 w-2/3 animate-pulse rounded bg-slate-200"></div>
        </div>
        <ol v-else class="mt-4 timeline">
          <li v-for="item in timelineItems" :key="item.id" class="timeline-item">
            <span :class="['timeline-marker', item.statusClass]">
              <component :is="item.icon" class="h-4 w-4" />
            </span>
            <div class="timeline-content">
              <p class="text-sm font-semibold text-slate-900">{{ item.title }}</p>
              <p v-if="item.description" class="text-sm text-slate-600">{{ item.description }}</p>
              <p class="text-xs text-slate-400">{{ item.dateLabel }}</p>
            </div>
          </li>
          <li v-if="!timelineItems.length" class="timeline-empty">
            <span class="text-sm text-slate-500">No recent activity yet.</span>
          </li>
        </ol>
      </section>
    </div>
  </div>

  <transition name="fade">
    <div v-if="selectedPayment" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 px-4">
      <div class="w-full max-w-2xl space-y-4 rounded-3xl bg-white p-6 shadow-2xl">
        <header class="flex items-start justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">Payment details</h3>
            <p class="text-sm text-slate-500">Gateway reference: {{ selectedPayment.paymentId ?? '-' }}</p>
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
              {{ selectedPayment.metadata ? JSON.stringify(selectedPayment.metadata, null, 2) : '-' }}
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
import { CheckCircleIcon, ClockIcon, XCircleIcon, ArrowPathRoundedSquareIcon, ArrowUturnLeftIcon } from '@heroicons/vue/24/outline';
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

const stripePublishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY ?? '';

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');

const refundMessage = computed(() => {
  if (!refundContext.value) return 'Confirm refund?';
  if (refundContext.value.type === 'bulk') {
    return `Refund ${refundContext.value.ids.length} payments?`;
  }
  return `Refund payment ${refundContext.value.payment?.paymentId ?? refundContext.value.payment?.id}?`;
});

const timelineItems = computed(() => {
  return payments.value
    .slice()
    .sort((a, b) => new Date(b.updatedAt ?? b.createdAt) - new Date(a.updatedAt ?? a.createdAt))
    .map((payment) => {
      const meta = timelineMeta(payment);
      return {
        id: payment.id,
        title: meta.title,
        description: meta.description,
        icon: meta.icon,
        statusClass: meta.className,
        dateLabel: formatDate(payment.updatedAt ?? payment.createdAt),
      };
    });
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

function onUpdateSelected(value) {
  selectedPayments.value = value ?? [];
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
      customerId: auth.user.id,
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
  if (!payload.metadata?.paymentPlanId) {
    notifications.push({ type: 'warning', title: 'Card payment', message: 'Select a plan before creating a payment.' });
    return;
  }
  intentLoading.value = true;
  try {
    const request = {
      customerId: auth.user.id,
      amount: payload.amount,
      description: payload.description,
      paymentMethod: 'CREDIT_CARD',
      paymentCardId: payload.paymentCardId,
      metadata: payload.metadata,
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
    if (!['PENDING', 'PROCESSING'].includes((data.status ?? data.paymentStatus) ?? '')) {
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
  if (!payload.paymentPlanId) {
    notifications.push({ type: 'warning', title: 'Subscription', message: 'Select a payment plan before continuing.' });
    return;
  }
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
      paymentPlanId: payload.paymentPlanId,
      paymentMethodId: payload.paymentMethodId,
      paymentMethod: payload.paymentMethod,
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
  if (!value) return '';
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}

function timelineMeta(payment) {
  const status = (payment.status ?? '').toUpperCase();
  const amount = formatCurrency(payment.amount);
  const description = payment.description ? ` ${payment.description}` : '';

  switch (status) {
    case 'COMPLETED':
      return {
        title: 'Payment completed',
        description: `${amount} captured.${description}`.trim(),
        icon: CheckCircleIcon,
        className: 'timeline-success',
      };
    case 'REFUNDED':
      return {
        title: 'Payment refunded',
        description: `${amount} will be returned to the customer within a few days.${description}`.trim(),
        icon: ArrowUturnLeftIcon,
        className: 'timeline-info',
      };
    case 'FAILED':
      return {
        title: 'Payment failed',
        description: `Attempted charge of ${amount}.${description}`.trim(),
        icon: XCircleIcon,
        className: 'timeline-danger',
      };
    case 'PROCESSING':
      return {
        title: 'Payment processing',
        description: `Processing ${amount}. Awaiting confirmation.${description}`.trim(),
        icon: ArrowPathRoundedSquareIcon,
        className: 'timeline-warning',
      };
    default:
      return {
        title: 'Payment initiated',
        description: `Initiated charge of ${amount}.${description}`.trim(),
        icon: ClockIcon,
        className: 'timeline-default',
      };
  }
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

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.timeline {
  position: relative;
  margin: 0;
  padding: 0 0 0 1.5rem;
  list-style: none;
  border-left: 1px solid #e2e8f0;
}

.timeline-item {
  position: relative;
  padding: 0 0 1.5rem 0.75rem;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-marker {
  position: absolute;
  left: -0.8rem;
  top: 0.15rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.1rem;
  height: 1.1rem;
  border-radius: 9999px;
  border: 2px solid #cbd5f5;
  background: #fff;
  color: #64748b;
}

.timeline-success {
  border-color: #6ee7b7 !important;
  background: #ecfdf5 !important;
  color: #047857 !important;
}

.timeline-warning {
  border-color: #fcd34d !important;
  background: #fffbeb !important;
  color: #92400e !important;
}

.timeline-danger {
  border-color: #fca5a5 !important;
  background: #fef2f2 !important;
  color: #b91c1c !important;
}

.timeline-info {
  border-color: #bfdbfe !important;
  background: #eff6ff !important;
  color: #1d4ed8 !important;
}

.timeline-default {
  border-color: #cbd5f5 !important;
  background: #f8fafc !important;
  color: #475569 !important;
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.timeline-empty {
  margin-left: -0.75rem;
  padding-left: 0.75rem;
  color: #64748b;
}
</style>
