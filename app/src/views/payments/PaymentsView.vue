<template>
  <div
    v-if="isAdmin || activeSubscription?.status !== 'ACTIVE'"
    class="grid grid-cols-1 lg:grid-cols-[minmax(0,1.4fr)_minmax(0,0.8fr)] gap-6 p-6 bg-gray-50 min-h-screen"
  >
    <div class="space-y-6">
      <!-- <PaymentMethodSelection v-model="currentMethod" /> -->

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
        <form
          class="flex flex-col gap-4 bg-white rounded-xl border border-gray-200 shadow-sm p-6"
          @submit.prevent="handleCreatePix"
        >
          <header class="space-y-1">
            <h2 class="text-lg font-semibold text-gray-800">{{ t('payments.pixForm.title') }}</h2>
            <p class="text-sm text-gray-500">{{ t('payments.pixForm.description') }}</p>
          </header>
          <div class="grid gap-4 md:grid-cols-2">
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-gray-600">{{ t('payments.pixForm.amountLabel', { currency }) }}</span>
              <input v-model.number="pixForm.amount" type="number" min="1" step="0.01" required class="input" />
            </label>
            <label class="flex flex-col gap-1">
              <span class="text-sm font-medium text-gray-600">{{ t('payments.pixForm.customerLabel') }}</span>
              <input
                v-model="pixForm.asaasCustomerId"
                type="text"
                :placeholder="t('payments.pixForm.customerPlaceholder')"
                required
                class="input"
              />
            </label>
          </div>
          <label class="flex flex-col gap-1">
            <span class="text-sm font-medium text-gray-600">{{ t('payments.pixForm.descriptionLabel') }}</span>
            <input
              v-model="pixForm.description"
              type="text"
              maxlength="140"
              class="input"
              :placeholder="t('payments.pixForm.descriptionPlaceholder')"
            />
          </label>
          <div class="flex justify-end">
            <button type="submit" class="btn-primary" :disabled="pixLoading">
              <span v-if="pixLoading" class="loader h-4 w-4"></span>
              <span>{{ pixLoading ? t('payments.pixForm.generating') : t('payments.pixForm.generate') }}</span>
            </button>
          </div>
        </form>

        <PIXPayment v-if="pixPayment" :payment="pixPayment" @refresh="refreshPix" />
      </div>

      <section v-else class="bg-transparent">
        <RecurringPayment
          :cards="cards"
          :currency="currency"
          :loading="recurringLoading"
          @create="handleCreateSubscription"
        />
      </section>
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

      <section class="bg-white rounded-xl border border-gray-200 shadow-sm p-5">
        <header class="flex flex-col gap-1">
          <h2 class="text-xl font-semibold text-gray-800">Payment History</h2>
          <p class="text-sm text-gray-500">All payments made through this subscription.</p>
        </header>

        <div class="mt-6 space-y-8">
          <div v-if="recurringHistoryItems.length" class="space-y-4">
            <h3 class="text-xs font-semibold uppercase tracking-wide text-gray-500">{{ t('payments.subscription.recurringList') }}</h3>
            <div class="divide-y divide-gray-100">
              <article
                v-for="item in recurringHistoryItems"
                :key="item.id"
                class="flex flex-col gap-3 py-4 md:flex-row md:items-center md:justify-between"
              >
                <div class="space-y-2">
                  <p class="text-sm font-semibold text-gray-800">{{ item.title }}</p>
                  <p class="text-xs text-gray-500">{{ item.subtitle }}</p>
                  <div class="flex items-center gap-2 text-xs">
                    <span :class="['px-2 py-0.5 rounded', item.statusClass]">{{ item.statusLabel }}</span>
                  </div>
                </div>
                <div class="text-right">
                  <p class="text-sm font-semibold text-gray-800">{{ item.amount }}</p>
                  <p class="text-xs text-gray-500">{{ item.nextBilling }}</p>
                </div>
              </article>
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between">
              <h3 class="text-xs font-semibold uppercase tracking-wide text-gray-500">{{ t('payments.subscription.recentPayments') }}</h3>
              <span v-if="paymentsLoading" class="text-xs text-gray-400">{{ t('common.loading') }}</span>
            </div>

            <div v-if="paymentsLoading" class="mt-4 space-y-4">
              <div class="h-16 w-full animate-pulse rounded-lg bg-gray-200"></div>
              <div class="h-16 w-full animate-pulse rounded-lg bg-gray-200"></div>
              <div class="h-16 w-full animate-pulse rounded-lg bg-gray-200"></div>
            </div>

            <div v-else-if="paymentHistoryItems.length" class="mt-4 divide-y divide-gray-100">
              <article
                v-for="item in paymentHistoryItems"
                :key="item.id"
                class="flex flex-col gap-3 py-4 md:flex-row md:items-start md:justify-between"
              >
                <div>
                  <p class="text-sm font-medium text-gray-800">{{ item.date }} · {{ item.description }}</p>
                  <div class="mt-2 flex flex-wrap items-center gap-3 text-xs">
                    <span :class="['px-2 py-0.5 rounded', item.statusClass]">{{ item.statusLabel }}</span>
                    <button
                      v-for="action in item.actions"
                      :key="action.type"
                      type="button"
                      :disabled="action.disabled"
                      :class="[
                        'font-medium transition hover:underline',
                        action.disabled
                          ? 'cursor-not-allowed text-gray-400 hover:text-gray-400 hover:no-underline'
                          : 'text-blue-600 hover:text-blue-700'
                      ]"
                      @click="handleHistoryAction(item, action.type)"
                    >
                      {{ action.label }}
                    </button>
                  </div>
                </div>
                <p class="text-sm font-semibold text-gray-800 md:text-right">{{ item.amount }}</p>
              </article>
            </div>

            <div v-else class="mt-4 rounded-xl bg-gray-50 p-4 text-sm text-gray-600">
              <p>{{ t('payments.recentActivity.empty') }}</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>

  <div
    v-else
    class="grid grid-cols-1 lg:grid-cols-[minmax(0,1.4fr)_minmax(0,0.8fr)] gap-6 p-6 bg-gray-50 min-h-screen"
  >
    <div class="space-y-6">
      <section class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
        <header class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <p class="text-sm font-medium text-blue-600">{{ t('payments.subscription.title') }}</p>
            <h2 class="text-xl font-semibold text-gray-800">{{ t('payments.subscription.subtitle') }}</h2>
          </div>
          <span
            v-if="activeSubscription"
            :class="[
              'inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-semibold uppercase',
              subscriptionBadgeClass(activeSubscription.status),
            ]"
          >
            {{ subscriptionStatusLabel(activeSubscription.status) }}
          </span>
        </header>

        <div v-if="subscriptionsLoading" class="mt-6 space-y-4">
          <div class="h-16 w-full animate-pulse rounded-lg bg-gray-200"></div>
          <div class="h-16 w-full animate-pulse rounded-lg bg-gray-200"></div>
          <div class="h-16 w-2/3 animate-pulse rounded-lg bg-gray-200"></div>
        </div>

        <div v-else-if="activeSubscription" class="mt-6 space-y-6">
          <div class="grid gap-4 sm:grid-cols-2">
            <article class="relative rounded-xl border border-blue-100 bg-blue-50 p-5">
              <h3 class="text-sm font-semibold text-blue-700">
                {{ activeSubscription.plan?.name ?? t('payments.subscription.planUnknown') }}
              </h3>
              <p class="mt-2 text-xs uppercase tracking-wide text-blue-500">
                {{ subscriptionIntervalLabel(activeSubscription.interval) }}
              </p>
              <p class="mt-3 text-2xl font-semibold text-blue-900">
                {{ formatCurrency(activeSubscription.amount) }}
              </p>
              <p class="mt-1 text-xs text-blue-600">
                {{ t('payments.subscription.nextBilling') }}:
                {{ formatSubscriptionDate(activeSubscription.nextBillingDate) }}
              </p>
            </article>

            <article class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
              <h3 class="text-sm font-semibold text-gray-800">{{ t('payments.subscription.card') }}</h3>
              <p class="mt-2 text-sm text-gray-600">{{ primaryCardLabel }}</p>
              <p class="mt-4 text-xs text-gray-400">
                {{ subscriptionIntervalLabel(activeSubscription.interval) }}
              </p>
            </article>
          </div>

          <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
            <h3 class="text-sm font-semibold text-gray-800">{{ t('payments.subscription.details') }}</h3>
            <dl class="mt-4 grid gap-4 sm:grid-cols-2">
              <div class="space-y-1">
                <dt class="text-xs uppercase tracking-wide text-gray-500">{{ t('payments.subscription.plan') }}</dt>
                <dd class="text-sm font-semibold text-gray-800">
                  {{ activeSubscription.plan?.name ?? t('payments.subscription.planUnknown') }}
                </dd>
              </div>
              <div class="space-y-1">
                <dt class="text-xs uppercase tracking-wide text-gray-500">{{ t('payments.subscription.statusLabel') }}</dt>
                <dd class="text-sm font-semibold text-gray-800">
                  {{ subscriptionStatusLabel(activeSubscription.status) }}
                </dd>
              </div>
              <div class="space-y-1">
                <dt class="text-xs uppercase tracking-wide text-gray-500">{{ t('payments.subscription.amount') }}</dt>
                <dd class="text-sm font-semibold text-gray-800">{{ formatCurrency(activeSubscription.amount) }}</dd>
              </div>
              <div class="space-y-1">
                <dt class="text-xs uppercase tracking-wide text-gray-500">{{ t('payments.subscription.nextBilling') }}</dt>
                <dd class="text-sm font-semibold text-gray-800">
                  {{ formatSubscriptionDate(activeSubscription.nextBillingDate) }}
                </dd>
              </div>
            </dl>

            <div class="mt-6 flex flex-wrap items-center justify-between gap-3">
              <p class="text-xs text-gray-500">
                {{ t('payments.subscription.manageHint') }}
              </p>
              <button
                type="button"
                class="rounded-lg bg-red-100 px-4 py-2 text-sm font-medium text-red-700 transition-colors hover:bg-red-200 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="cancelingSubscription"
                aria-label="Cancel Subscription"
                @click="handleCancelSubscription"
              >
                <span v-if="cancelingSubscription" class="flex items-center justify-center gap-2">
                  <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
                  {{ t('payments.subscription.cancelling') }}
                </span>
                <span v-else>{{ t('payments.subscription.cancel') }}</span>
              </button>
            </div>
          </div>
        </div>

        <div v-else class="mt-6 rounded-xl bg-gray-50 p-4 text-sm text-gray-600">
          <p class="font-semibold text-gray-700">{{ t('payments.subscription.emptyTitle') }}</p>
          <p class="mt-1 text-gray-500">{{ t('payments.subscription.emptyDescription') }}</p>
        </div>
      </section>
    </div>

    <div class="space-y-6">
      <section class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
        <header class="flex flex-col gap-1">
          <h2 class="text-xl font-semibold text-gray-800">{{ t('payments.subscription.paymentHisotry') }}</h2>
          <p class="text-sm text-gray-500">{{ t('payments.subscription.paymentHisotryDescription') }}</p>
        </header>

        <div class="mt-6 space-y-8">
          <div v-if="recurringHistoryItems.length" class="space-y-4">
            <h3 class="text-xs font-semibold uppercase tracking-wide text-gray-500">{{ t('payments.subscription.recurringList') }}</h3>
            <div class="divide-y divide-gray-100">
              <article
                v-for="item in recurringHistoryItems"
                :key="item.id"
                class="flex flex-col gap-3 py-4 md:flex-row md:items-center md:justify-between"
              >
                <div class="space-y-2">
                  <p class="text-sm font-semibold text-gray-800">{{ item.title }}</p>
                  <p class="text-xs text-gray-500">{{ item.subtitle }}</p>
                  <div class="flex items-center gap-2 text-xs">
                    <span :class="['px-2 py-0.5 rounded', item.statusClass]">{{ item.statusLabel }}</span>
                  </div>
                </div>
                <div class="text-right">
                  <p class="text-sm font-semibold text-gray-800">{{ item.amount }}</p>
                  <p class="text-xs text-gray-500">{{ item.nextBilling }}</p>
                </div>
              </article>
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between">
              <h3 class="text-xs font-semibold uppercase tracking-wide text-gray-500">{{ t('payments.subscription.recentPayments') }}</h3>
              <span v-if="paymentsLoading" class="text-xs text-gray-400">{{ t('common.loading') }}</span>
            </div>

            <div v-if="paymentsLoading" class="mt-4 space-y-4">
              <div class="h-16 w-full animate-pulse rounded-lg bg-gray-200"></div>
              <div class="h-16 w-full animate-pulse rounded-lg bg-gray-200"></div>
              <div class="h-16 w-full animate-pulse rounded-lg bg-gray-200"></div>
            </div>

            <div v-else-if="paymentHistoryItems.length" class="mt-4 divide-y divide-gray-100">
              <article
                v-for="item in paymentHistoryItems"
                :key="item.id"
                class="flex flex-col gap-3 py-4 md:flex-row md:items-start md:justify-between"
              >
                <div>
                  <p class="text-sm font-medium text-gray-800">{{ item.date }} · {{ item.description }}</p>
                  <div class="mt-2 flex flex-wrap items-center gap-3 text-xs">
                    <span :class="['px-2 py-0.5 rounded', item.statusClass]">{{ item.statusLabel }}</span>
                    <button
                      v-for="action in item.actions"
                      :key="action.type"
                      type="button"
                      :disabled="action.disabled"
                      :class="[
                        'font-medium transition hover:underline',
                        action.disabled
                          ? 'cursor-not-allowed text-gray-400 hover:text-gray-400 hover:no-underline'
                          : 'text-blue-600 hover:text-blue-700'
                      ]"
                      @click="handleHistoryAction(item, action.type)"
                    >
                      {{ action.label }}
                    </button>
                  </div>
                </div>
                <p class="text-sm font-semibold text-gray-800 md:text-right">{{ item.amount }}</p>
              </article>
            </div>

            <div v-else class="mt-4 rounded-xl bg-gray-50 p-4 text-sm text-gray-600">
              <p>{{ t('payments.recentActivity.empty') }}</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>

  <transition name="fade">
    <div v-if="selectedPayment" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 px-4">
      <div class="w-full max-w-2xl space-y-4 rounded-3xl bg-white p-6 shadow-2xl">
        <header class="flex items-start justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">{{ t('payments.details.title') }}</h3>
            <p class="text-sm text-slate-500">{{ t('payments.details.reference') }}: {{ selectedPayment.paymentId ?? '-'
            }}</p>
          </div>
          <button type="button" class="btn-ghost" @click="selectedPayment = null">{{ t('common.actions.close')
          }}</button>
        </header>
        <dl class="grid gap-4 md:grid-cols-2">
          <div>
            <dt class="text-xs uppercase tracking-wide text-slate-500">{{ t('payments.details.status') }}</dt>
            <dd class="text-sm font-semibold">{{ selectedPayment.status }}</dd>
          </div>
          <div>
            <dt class="text-xs uppercase tracking-wide text-slate-500">{{ t('payments.details.method') }}</dt>
            <dd class="text-sm text-slate-700">{{ selectedPayment.paymentMethod }}</dd>
          </div>
          <div>
            <dt class="text-xs uppercase tracking-wide text-slate-500">{{ t('payments.details.amount') }}</dt>
            <dd class="text-sm text-slate-700">{{ formatCurrency(selectedPayment.amount) }}</dd>
          </div>
          <div>
            <dt class="text-xs uppercase tracking-wide text-slate-500">{{ t('payments.details.createdAt') }}</dt>
            <dd class="text-sm text-slate-700">{{ formatDate(selectedPayment.createdAt) }}</dd>
          </div>
          <div class="md:col-span-2">
            <dt class="text-xs uppercase tracking-wide text-slate-500">{{ t('payments.details.metadata') }}</dt>
            <dd class="mt-1 max-h-40 overflow-auto rounded-xl bg-slate-50 p-3 font-mono text-xs text-slate-700">
              {{ selectedPayment.metadata ? JSON.stringify(selectedPayment.metadata, null, 2) : '-' }}
            </dd>
          </div>
        </dl>
      </div>
    </div>
  </transition>

  <ConfirmDialog v-if="isAdmin" v-model="showRefundDialog" :title="t('payments.refund.dialogTitle')"
    :message="refundMessage" :confirm-label="t('payments.refund.confirmLabel')" @confirm="executeRefund" />
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
  listSubscriptions,
  cancelSubscription,
  refundPayment,
} from '@/services/payments';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import { useI18n } from 'vue-i18n';

const auth = useAuthStore();
const notifications = useNotificationStore();
const { t, locale } = useI18n();

const currency = 'BRL';
const currentMethod = ref('RECURING');
const cards = ref([]);
const cardFormRef = ref(null);

const primaryCard = computed(() => {
  if (!cards.value?.length) return null;
  return cards.value.find((card) => card.defaultCard) ?? cards.value[0];
});

const primaryCardLabel = computed(() => {
  if (!primaryCard.value) {
    return t('payments.cardForm.noCards');
  }
  const brand = (primaryCard.value.brand ?? '').toUpperCase() || 'CARD';
  const lastFour = primaryCard.value.lastFour ?? '••••';
  return `${brand} •••• ${lastFour}`;
});

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

const subscriptions = ref([]);
const subscriptionsLoading = ref(false);
const cancelingSubscription = ref(false);

const stripePublishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY ?? '';

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const isClient = computed(() => !isAdmin.value);

const activeSubscription = computed(() => {
  return subscriptions.value.find((subscription) => {
    const status = (subscription.status ?? '').toUpperCase();
    return ['ACTIVE', 'PAUSED'].includes(status);
  }) || subscriptions.value[0] || null;
});

const refundMessage = computed(() => {
  if (!refundContext.value) return t('payments.refund.confirmDefault');
  if (refundContext.value.type === 'bulk') {
    return t('payments.refund.confirmBulk', { count: refundContext.value.ids.length });
  }
  return t('payments.refund.confirmSingle', {
    id: refundContext.value.payment?.paymentId ?? refundContext.value.payment?.id ?? '-'
  });
});

const paymentStatusMeta = {
  PAID: { label: 'Paid', className: 'bg-green-100 text-green-700', showInvoice: true },
  COMPLETED: { label: 'Paid', className: 'bg-green-100 text-green-700', showInvoice: true },
  REFUNDED: { label: 'Refunded', className: 'bg-amber-100 text-amber-700', showInvoice: true, negative: true },
  FAILED: { label: 'Failed', className: 'bg-red-100 text-red-700', showInvoice: false, negative: true },
  DECLINED: { label: 'Failed', className: 'bg-red-100 text-red-700', showInvoice: false, negative: true },
  PROCESSING: { label: 'Processing', className: 'bg-gray-100 text-gray-600', showInvoice: false },
  DEFAULT: { label: 'Processing', className: 'bg-gray-100 text-gray-600', showInvoice: false },
};

const paymentHistoryItems = computed(() => {
  return payments.value
    .slice()
    .sort((a, b) => new Date(b.createdAt ?? 0) - new Date(a.createdAt ?? 0))
    .map((payment) => {
      const status = (payment.status ?? payment.paymentStatus ?? '').toUpperCase();
      const meta = paymentStatusMeta[status] ?? paymentStatusMeta.DEFAULT;
      const amount = Number(payment.amount ?? 0);
      const signedAmount = meta.negative ? -Math.abs(amount) : amount;
      const invoiceUrl = resolveInvoiceUrl(payment.metadata);
      const actions = [
        {
          type: 'invoice',
          label: t('payments.history.actions.invoice'),
          disabled: !meta.showInvoice || !invoiceUrl,
        },
        { type: 'details', label: t('payments.history.actions.details'), disabled: false },
      ];
      return {
        id: payment.id ?? payment.paymentId,
        date: formatHistoryDate(payment.createdAt ?? payment.updatedAt),
        description: payment.description ?? activeSubscription.value?.plan?.name ?? t('payments.subscription.planUnknown'),
        statusLabel: meta.label,
        statusClass: meta.className,
        amount: formatCurrency(signedAmount),
        invoiceUrl,
        actions,
        payment,
      };
    });
});

const recurringHistoryItems = computed(() => {
  if (!subscriptions.value?.length) {
    return [];
  }
  return subscriptions.value
    .slice()
    .sort((a, b) => new Date(b.nextBillingDate ?? 0) - new Date(a.nextBillingDate ?? 0))
    .map((subscription) => {
      const intervalLabel = subscriptionIntervalLabel(subscription.interval);
      const statusLabel = subscriptionStatusLabel(subscription.status);
      return {
        id: subscription.id,
        title: subscription.plan?.name ?? t('payments.subscription.planUnknown'),
        subtitle: intervalLabel,
        statusLabel,
        statusClass: subscriptionBadgeClass(subscription.status),
        amount: formatCurrency(subscription.amount),
        nextBilling: subscription.nextBillingDate
          ? `${t('payments.subscription.nextBilling')}: ${formatSubscriptionDate(subscription.nextBillingDate)}`
          : t('payments.subscription.noNextBilling'),
      };
    });
});

const timelineItems = computed(() => {
  return subscriptions.value
    .slice()
    .sort((a, b) => a.status.localeCompare(b.status) || new Date(b.createdAt) - new Date(a.createdAt))
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
      if (isClient.value) {
        loadSubscriptions();
      } else {
        subscriptions.value = [];
      }
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

watch(
  () => isAdmin.value,
  (value) => {
    if (!value && auth.user?.id) {
      loadSubscriptions();
    }
    if (value) {
      subscriptions.value = [];
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
    notifications.push({
      type: 'error',
      title: t('payments.notifications.cardsTitle'),
      message: t('payments.notifications.cardsLoadError'),
    });
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
      customerId: isAdmin.value ? undefined : auth.user.id,
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
    notifications.push({
      type: 'error',
      title: t('payments.notifications.paymentsTitle'),
      message: t('payments.notifications.paymentsLoadError'),
    });
  } finally {
    paymentsLoading.value = false;
  }
}

async function loadSubscriptions() {
  if (!auth.user?.id || !isClient.value) return;
  subscriptionsLoading.value = true;
  try {
    const { data } = await listSubscriptions({ customerId: auth.user.id });
    subscriptions.value = data ?? [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.subscriptionTitle'),
      message: error?.response?.data?.message ?? t('payments.notifications.subscriptionLoadError'),
    });
  } finally {
    subscriptionsLoading.value = false;
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
    notifications.push({
      type: 'warning',
      title: t('payments.notifications.cardTitle'),
      message: t('payments.notifications.cardMissingPlan'),
    });
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
    notifications.push({
      type: 'success',
      title: t('payments.notifications.intentTitle'),
      message: t('payments.notifications.intentCreated', { status: data.status }),
    });
    loadPayments();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.intentErrorTitle'),
      message: t('payments.notifications.intentError'),
    });
  } finally {
    intentLoading.value = false;
  }
}

async function handleCancelSubscription() {
  if (!activeSubscription.value?.id || cancelingSubscription.value) return;
  cancelingSubscription.value = true;
  try {
    await cancelSubscription(activeSubscription.value.id);
    notifications.push({
      type: 'success',
      title: t('payments.notifications.subscriptionCancelledTitle'),
      message: t('payments.notifications.subscriptionCancelledMessage'),
    });
    await loadSubscriptions();
    await loadPayments();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.subscriptionTitle'),
      message: error?.response?.data?.message ?? t('payments.notifications.subscriptionCancelError'),
    });
  } finally {
    cancelingSubscription.value = false;
  }
}

async function tokenizeCard(cardInput) {
  if (!auth.user?.id) return;
  if (!stripePublishableKey || !window.Stripe) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.tokenizationTitle'),
      message: t('payments.notifications.stripeNotLoaded'),
    });
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
    notifications.push({
      type: 'success',
      title: t('payments.notifications.cardSavedTitle'),
      message: t('payments.notifications.cardSavedMessage'),
    });
    await loadCards();
    cardFormRef.value?.closeNewCard();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.tokenizationFailedTitle'),
      message: error?.message ?? t('payments.notifications.tokenizationFailedMessage'),
    });
  } finally {
    tokenizing.value = false;
  }
}

async function handleCreatePix() {
  if (!auth.user?.id) return;
  if (!pixForm.amount || !pixForm.asaasCustomerId) {
    notifications.push({
      type: 'warning',
      title: t('payments.notifications.pixTitle'),
      message: t('payments.notifications.pixMissingFields'),
    });
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
    notifications.push({
      type: 'success',
      title: t('payments.notifications.pixSuccessTitle'),
      message: t('payments.notifications.pixSuccessMessage'),
    });
    startPixPolling(data.id);
    loadPayments();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.pixTitle'),
      message: t('payments.notifications.pixError'),
    });
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
    notifications.push({
      type: 'error',
      title: t('payments.notifications.pixStatusTitle'),
      message: t('payments.notifications.pixStatusError'),
    });
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
    notifications.push({
      type: 'warning',
      title: t('payments.notifications.subscriptionTitle'),
      message: t('payments.notifications.subscriptionMissingPlan'),
    });
    return;
  }
  if (payload.paymentMethod === 'PIX' && !payload.metadata?.asaasCustomerId) {
    notifications.push({
      type: 'warning',
      title: t('payments.notifications.subscriptionTitle'),
      message: t('payments.notifications.subscriptionMissingAsaas'),
    });
    return;
  }
  if (payload.paymentMethod !== 'PIX' && !auth.user.id) {
    notifications.push({
      type: 'warning',
      title: t('payments.notifications.subscriptionTitle'),
      message: t('payments.notifications.subscriptionMissingStripe'),
    });
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
    notifications.push({
      type: 'success',
      title: t('payments.notifications.subscriptionCreatedTitle'),
      message: t('payments.notifications.subscriptionCreatedMessage', { id: data.subscriptionId }),
    });
    loadPayments();
    if (isClient.value) {
      await loadSubscriptions();
    }
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.subscriptionTitle'),
      message: t('payments.notifications.subscriptionError'),
    });
  } finally {
    recurringLoading.value = false;
  }
}

function handleHistoryAction(item, actionType) {
  const action = item.actions?.find((entry) => entry.type === actionType);
  if (!action || action.disabled) {
    if (actionType === 'invoice') {
      viewInvoice(item.payment, item.invoiceUrl);
    }
    return;
  }
  if (actionType === 'invoice') {
    viewInvoice(item.payment, item.invoiceUrl);
    return;
  }
  if (actionType === 'details') {
    openPaymentDetails(item.payment);
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
      notifications.push({
        type: 'success',
        title: t('payments.notifications.refundsTitle'),
        message: t('payments.notifications.refundBulkMessage'),
      });
    } else if (refundContext.value.payment) {
      await refundPayment(refundContext.value.payment.id, {});
      notifications.push({
        type: 'success',
        title: t('payments.notifications.refundTitle'),
        message: t('payments.notifications.refundSuccess'),
      });
    }
    loadPayments();
    selectedPayments.value = [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('payments.notifications.refundTitle'),
      message: t('payments.notifications.refundError'),
    });
  } finally {
    refundContext.value = null;
  }
}

function exportCsv() {
  if (!payments.value.length) {
    notifications.push({
      type: 'warning',
      title: t('payments.notifications.exportTitle'),
      message: t('payments.notifications.exportEmpty'),
    });
    return;
  }
  const headers = [
    t('payments.export.headers.id'),
    t('payments.export.headers.gateway'),
    t('payments.export.headers.method'),
    t('payments.export.headers.status'),
    t('payments.export.headers.amount'),
    t('payments.export.headers.currency'),
    t('payments.export.headers.createdAt'),
  ];
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

function currentLocaleTag() {
  return locale.value === 'pt' ? 'pt-BR' : 'en-US';
}

function formatCurrency(value) {
  return new Intl.NumberFormat(currentLocaleTag(), { style: 'currency', currency }).format(value ?? 0);
}

function viewInvoice(payment, directUrl) {
  const url = directUrl || resolveInvoiceUrl(payment?.metadata);
  if (url && typeof window !== 'undefined') {
    window.open(url, '_blank', 'noopener');
    return;
  }
  notifications.push({
    type: 'warning',
    title: t('payments.notifications.invoiceTitle'),
    message: t('payments.notifications.invoiceUnavailable'),
  });
}

function resolveInvoiceUrl(metadata) {
  if (!metadata) return null;
  const candidates = [
    'invoiceUrl',
    'invoice_url',
    'hostedInvoiceUrl',
    'hosted_invoice_url',
    'invoice',
    'invoicePdf',
    'invoice_pdf',
    'receiptUrl',
    'receipt_url',
    'url',
  ];
  for (const key of candidates) {
    const value = metadata?.[key];
    if (typeof value === 'string' && /^https?:\/\//i.test(value)) {
      return value;
    }
  }
  return null;
}

function formatDate(value) {
  if (!value) return '';
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
  } catch (error) {
    return value;
  }
}

function formatHistoryDate(value) {
  if (!value) return '';
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), { dateStyle: 'medium' }).format(new Date(value));
  } catch (error) {
    return value;
  }
}

function timelineMeta(payment) {
  const status = (payment.status ?? '').toUpperCase();
  const amount = formatCurrency(payment.amount);
  const extraDetails = payment.description ? ` ${payment.description}` : '';

  const baseMeta = {
    COMPLETED: {
      title: t('payments.timeline.completed.title'),
      description: t('payments.timeline.completed.description', { amount }),
      icon: CheckCircleIcon,
      className: 'timeline-success',
    },
    ACTIVE: {
      title: t('payments.timeline.completed.title'),
      description: t('payments.timeline.completed.description', { amount }),
      icon: CheckCircleIcon,
      className: 'timeline-success',
    },
    REFUNDED: {
      title: t('payments.timeline.refunded.title'),
      description: t('payments.timeline.refunded.description', { amount }),
      icon: ArrowUturnLeftIcon,
      className: 'timeline-info',
    },
    FAILED: {
      title: t('payments.timeline.failed.title'),
      description: t('payments.timeline.failed.description', { amount }),
      icon: XCircleIcon,
      className: 'timeline-danger',
    },
    CANCELLED: {
      title: t('payments.timeline.failed.title'),
      description: t('payments.timeline.failed.description', { amount }),
      icon: XCircleIcon,
      className: 'timeline-danger',
    },
    PROCESSING: {
      title: t('payments.timeline.processing.title'),
      description: t('payments.timeline.processing.description', { amount }),
      icon: ArrowPathRoundedSquareIcon,
      className: 'timeline-warning',
    },
    RECURRING: {
      title: t('payments.timeline.recurring.title'),
      description: t('payments.timeline.recurring.description', { amount }),
      icon: ArrowPathRoundedSquareIcon,
      className: 'timeline-info',
    },
    DEFAULT: {
      title: t('payments.timeline.default.title'),
      description: t('payments.timeline.default.description', { amount }),
      icon: ClockIcon,
      className: 'timeline-default',
    },
  };

  const meta = baseMeta[status] ?? baseMeta.DEFAULT;
  return {
    ...meta,
    description: `${meta.description}${extraDetails}`.trim(),
  };
}

function subscriptionStatusLabel(status) {
  const key = (status ?? 'unknown').toLowerCase();
  return t(`payments.subscription.status.${key}`);
}

function subscriptionBadgeClass(status) {
  switch ((status ?? '').toUpperCase()) {
    case 'ACTIVE':
      return 'bg-emerald-100 text-emerald-700';
    case 'PAUSED':
      return 'bg-amber-100 text-amber-700';
    case 'CANCELLED':
      return 'bg-rose-100 text-rose-700';
    case 'EXPIRED':
      return 'bg-slate-200 text-slate-700';
    default:
      return 'bg-slate-200 text-slate-700';
  }
}

function subscriptionIntervalLabel(interval) {
  const key = (interval ?? 'unknown').toLowerCase();
  return t(`payments.subscription.interval.${key}`);
}

function formatSubscriptionDate(value) {
  if (!value) {
    return t('payments.subscription.noNextBilling');
  }
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), { dateStyle: 'medium' }).format(new Date(value));
  } catch (error) {
    return value;
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
