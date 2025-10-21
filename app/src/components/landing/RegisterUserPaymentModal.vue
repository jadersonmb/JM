<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
    <div class="relative w-full max-w-3xl overflow-hidden rounded-2xl bg-white shadow-xl animate-fadeIn">
      <button
        type="button"
        class="absolute right-4 top-4 text-gray-500 transition hover:text-gray-700"
        @click="emit('close')"
      >
        <XCircleIcon class="h-7 w-7" />
      </button>

      <div class="max-h-[calc(100vh-4rem)] overflow-y-auto p-8">
        <h2 class="mb-2 text-2xl font-semibold text-gray-800">{{ t('register.title') }}</h2>
        <p class="mb-6 text-sm text-gray-600">
          {{ t('register.subtitle', { plan: props.selectedPlan?.name ?? t('register.planFallback') }) }}
        </p>

        <form class="space-y-6" @submit.prevent>
          <div class="space-y-4">
            <input
              v-model.trim="form.name"
              type="text"
              :placeholder="t('register.name')"
              class="w-full rounded-lg border border-gray-200 p-3 focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
              required
            />
            <input
              v-model.trim="form.email"
              type="email"
              :placeholder="t('register.email')"
              class="w-full rounded-lg border border-gray-200 p-3 focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
              required
            />
            <input
              v-model.trim="form.phoneNumber"
              type="tel"
              :placeholder="t('register.phone')"
              class="w-full rounded-lg border border-gray-200 p-3 focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
              required
            />
          </div>

          <p v-if="errorMessage" class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
            {{ errorMessage }}
          </p>

          <RecurringPayment
            :selected-plan="props.selectedPlan"
            :loading="loading"
            :ensure-customer="ensureCustomer"
            card-entry-only
            @create="handleRecurringCreate"
          />
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import axios from '@/services/api';
import { addCard, createSubscription } from '@/services/payments';
import { useNotificationStore } from '@/stores/notifications';
import RecurringPayment from '@/components/payments/RecurringPayment.vue';
import { XCircleIcon } from '@heroicons/vue/24/outline';
const props = defineProps({
  selectedPlan: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['close', 'success']);

const { t, locale } = useI18n();
const notifications = useNotificationStore();

const stripePublishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY ?? '';

const form = reactive({
  name: '',
  email: '',
  phoneNumber: '',
});

const customer = ref(null);

const loading = ref(false);
const errorMessage = ref('');

function isFormValid() {
  return form.name && form.email && form.phoneNumber;
}

function normalizePhone(value) {
  return (value || '').replace(/[^+\d]/g, '');
}

function buildUserPayload() {
  return {
    name: form.name,
    email: form.email,
    phoneNumber: normalizePhone(form.phoneNumber),
    type: 'CLIENT',
    locale: locale.value === 'pt' ? 'pt-BR' : 'en-US',
  };
}

async function createOrFetchUser() {
  const payload = buildUserPayload();
  try {
    const { data } = await axios.post('/api/v1/users', payload);
    customer.value = {
      ...data,
      phoneNumber: data.phoneNumber ?? payload.phoneNumber,
    };
    return data;
  } catch (error) {
    const status = error.response?.status;
    if (status === 400 || status === 409) {
      try {
        const { data } = await axios.get('/api/v1/users', {
          params: { email: payload.email, size: 1 },
        });
        const existing = data?.content?.[0];
        if (existing) {
          customer.value = {
            ...existing,
            phoneNumber: existing.phoneNumber ?? payload.phoneNumber,
          };
          return existing;
        }
      } catch (fetchError) {
        console.error('Failed to fetch existing user', fetchError);
      }
    }
    throw error;
  }
}

async function ensureCustomer(options = {}) {
  const silent = Boolean(options?.silent);

  if (!isFormValid()) {
    if (!silent) {
      errorMessage.value = t('register.validationError');
      notifications.push({
        type: 'warning',
        title: t('register.errorTitle'),
        message: errorMessage.value,
      });
    }
    throw new Error('form-invalid');
  }

  if (customer.value?.id) {
    return customer.value;
  }

  try {
    const user = await createOrFetchUser();
    return user;
  } catch (error) {
    if (!silent) {
      const responseMessage = error.response?.data?.details || error.response?.data?.message;
      errorMessage.value = responseMessage || t('register.errorMessage');
      notifications.push({
        type: 'error',
        title: t('register.errorTitle'),
        message: errorMessage.value,
      });
    }
    throw error;
  }
}

async function handleRecurringCreate(payload) {
  errorMessage.value = '';
  if (!isFormValid()) {
    errorMessage.value = t('register.validationError');
    return;
  }
  if (!payload?.paymentPlanId) {
    errorMessage.value = t('register.planRequired');
    return;
  }

  if (!payload?.card) {
    errorMessage.value = t('register.cardValidationError');
    return;
  }

  if (!stripePublishableKey || !window.Stripe) {
    errorMessage.value = t('register.cardTokenizationError');
    notifications.push({
      type: 'error',
      title: t('register.errorTitle'),
      message: errorMessage.value,
    });
    return;
  }

  const sanitizedCardNumber = (payload.card.cardNumber || '').replace(/\s+/g, '');
  const normalizedExpiryMonth = String(payload.card.expiryMonth || '').padStart(2, '0');
  const rawExpiryYear = String(payload.card.expiryYear || '');
  const normalizedExpiryYear = rawExpiryYear.length === 4 ? rawExpiryYear : `20${rawExpiryYear.padStart(2, '0')}`;

  loading.value = true;
  try {
    const stripe = window.Stripe(stripePublishableKey);
    const { token, error: stripeError } = await stripe.createToken('card', {
      number: sanitizedCardNumber,
      exp_month: Number(normalizedExpiryMonth),
      exp_year: Number(normalizedExpiryYear),
      cvc: payload.card.cvc,
      name: payload.card.cardholder,
    });

    if (stripeError) {
      throw new Error(stripeError.message || t('register.cardTokenizationError'));
    }

    if (!token?.id) {
      throw new Error(t('register.cardTokenizationError'));
    }

    const user = await ensureCustomer({ silent: true });
    if (!user?.id) {
      throw new Error(t('register.missingUserError'));
    }

    const { data: cardResponse } = await addCard({
      customerId: user.id,
      cardToken: token.id,
      brand: token.card?.brand,
      lastFour: token.card?.last4,
      expiryMonth: token.card?.exp_month,
      expiryYear: token.card?.exp_year,
      defaultCard: payload.card.setDefault,
    });

    const paymentMethodId = cardResponse?.id;
    if (!paymentMethodId) {
      throw new Error(t('register.cardCreationError'));
    }

    await createSubscription({
      customerId: user.id,
      paymentPlanId: payload.paymentPlanId,
      paymentMethodId,
      paymentMethod: payload.paymentMethod,
      immediateCharge: payload.immediateCharge,
      metadata: payload.metadata ?? {},
    });

    await sendWelcomeMessage(user);

    notifications.push({
      type: 'success',
      title: t('register.successTitle'),
      message: t('register.successMessage'),
    });

    emit('success');
  } catch (error) {
    console.error('Subscription flow failed', error);
    const responseMessage =
      error.response?.data?.details ||
      error.response?.data?.message ||
      error.message;
    errorMessage.value = responseMessage || t('register.errorMessage');
    notifications.push({
      type: 'error',
      title: t('register.errorTitle'),
      message: errorMessage.value,
    });
  } finally {
    loading.value = false;
  }
}

async function sendWelcomeMessage(user) {
  const phone = normalizePhone(user?.phoneNumber ?? form.phoneNumber);
  if (!phone) {
    return;
  }

  try {
    await axios.post('/public/api/v1/whatsapp/send', {
      phoneNumber: phone,
      message: t('register.welcomeMessage', { name: user?.name || form.name }),
      userId: user?.id,
    });
  } catch (error) {
    console.error('Failed to send WhatsApp welcome message', error);
  }
}
</script>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fadeIn {
  animation: fadeIn 200ms ease-out;
}
</style>
