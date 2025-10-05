<template>
  <div class="flex flex-col gap-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
    <header class="flex items-start justify-between gap-3">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">PIX payment</h2>
        <p class="text-sm text-slate-500">Scan the QR code or copy the PIX key to complete the payment.</p>
      </div>
      <span :class="statusClass" class="rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide">
        {{ payment?.status ?? 'PENDING' }}
      </span>
    </header>

    <div class="grid gap-6 md:grid-cols-2">
      <div class="flex flex-col items-center gap-4">
        <div class="flex h-48 w-48 items-center justify-center rounded-xl border border-dashed border-slate-300 bg-slate-50">
          <img v-if="payment?.qrCodeImage" :src="payment.qrCodeImage" alt="PIX QR Code" class="h-44 w-44 object-cover" />
          <QrCodeIcon v-else class="h-20 w-20 text-slate-400" />
        </div>
        <button type="button" class="btn-secondary" @click="emit('refresh')">
          <ArrowPathIcon class="h-4 w-4" />
          <span>Refresh status</span>
        </button>
      </div>
      <div class="flex flex-col gap-4">
        <div class="rounded-xl bg-slate-50 p-4">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">PIX payload</span>
          <pre class="mt-2 max-h-32 overflow-auto whitespace-pre-wrap break-all text-sm text-slate-700">{{ payment?.payload ?? 'PIX payload will appear here after generation.' }}</pre>
          <div class="mt-3 flex justify-end">
            <button type="button" class="btn-primary" @click="copyPayload" :disabled="!payment?.payload">
              <span v-if="copied" class="text-emerald-100">Copied!</span>
              <span v-else>Copy PIX code</span>
            </button>
          </div>
        </div>
        <div class="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          <div>
            <p class="font-semibold">PIX key</p>
            <p class="font-mono text-xs">{{ payment?.pixKey ?? '—' }}</p>
          </div>
          <div class="text-right">
            <p class="text-xs uppercase tracking-wide text-emerald-600">Expires in</p>
            <p class="font-semibold">{{ remainingTime }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { ArrowPathIcon, QrCodeIcon } from '@heroicons/vue/24/outline';

const props = defineProps({
  payment: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['refresh']);

const countdown = ref('00:00');
const copied = ref(false);
let timerHandle;

const statusClass = computed(() => {
  const status = (props.payment?.status ?? 'PENDING').toUpperCase();
  switch (status) {
    case 'COMPLETED':
      return 'bg-emerald-100 text-emerald-700';
    case 'FAILED':
      return 'bg-rose-100 text-rose-700';
    default:
      return 'bg-amber-100 text-amber-700';
  }
});

const remainingTime = computed(() => countdown.value);

watch(
  () => props.payment?.expiresAt,
  (value) => {
    clearTimer();
    if (!value) return;
    const expireAt = new Date(value);
    timerHandle = window.setInterval(() => updateCountdown(expireAt), 1000);
    updateCountdown(expireAt);
  },
  { immediate: true },
);

async function copyPayload() {
  if (!props.payment?.payload) return;
  await navigator.clipboard.writeText(props.payment.payload);
  copied.value = true;
  window.setTimeout(() => (copied.value = false), 1500);
}

function updateCountdown(expireAt) {
  const now = new Date();
  const diff = expireAt.getTime() - now.getTime();
  if (diff <= 0) {
    countdown.value = 'Expired';
    clearTimer();
    return;
  }
  const minutes = Math.floor(diff / 60000)
    .toString()
    .padStart(2, '0');
  const seconds = Math.floor((diff % 60000) / 1000)
    .toString()
    .padStart(2, '0');
  countdown.value = `${minutes}:${seconds}`;
}

function clearTimer() {
  if (timerHandle) {
    window.clearInterval(timerHandle);
    timerHandle = undefined;
  }
}

onMounted(() => {
  if (props.payment?.expiresAt) {
    const expireAt = new Date(props.payment.expiresAt);
    updateCountdown(expireAt);
    timerHandle = window.setInterval(() => updateCountdown(expireAt), 1000);
  }
});

onBeforeUnmount(() => {
  clearTimer();
});
</script>
