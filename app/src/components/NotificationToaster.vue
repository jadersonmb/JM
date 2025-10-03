<template>
  <div class="fixed inset-x-0 top-4 z-50 mx-auto flex w-full max-w-md flex-col gap-3 px-4 sm:right-6 sm:top-6 sm:ml-auto sm:mr-0 sm:w-80 sm:px-0">
    <transition-group name="toast" tag="div">
      <article
        v-for="notification in notifications"
        :key="notification.id"
        class="flex items-start gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-lg"
        :class="typeClasses(notification.type)"
      >
        <component :is="typeIcon(notification.type)" class="mt-0.5 h-5 w-5 flex-shrink-0" />
        <div class="flex-1">
          <p v-if="notification.title" class="text-sm font-semibold leading-5">{{ notification.title }}</p>
          <p class="text-sm text-slate-600">{{ notification.message }}</p>
        </div>
        <button
          type="button"
          class="text-slate-400 transition hover:text-slate-600"
          @click="dismiss(notification.id)"
        >
          <XMarkIcon class="h-4 w-4" />
        </button>
      </article>
    </transition-group>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import {
  CheckCircleIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XCircleIcon,
  XMarkIcon,
} from '@heroicons/vue/24/outline';
import { useNotificationStore } from '@/stores/notifications';

const store = useNotificationStore();
const notifications = computed(() => store.items);

const dismiss = (id) => store.remove(id);

const typeClasses = (type) => {
  switch (type) {
    case 'success':
      return 'border-emerald-200 bg-emerald-50 text-emerald-700';
    case 'error':
      return 'border-red-200 bg-red-50 text-red-700';
    case 'warning':
      return 'border-amber-200 bg-amber-50 text-amber-700';
    default:
      return 'border-slate-200 bg-white text-slate-700';
  }
};

const typeIcon = (type) => {
  switch (type) {
    case 'success':
      return CheckCircleIcon;
    case 'error':
      return XCircleIcon;
    case 'warning':
      return ExclamationTriangleIcon;
    default:
      return InformationCircleIcon;
  }
};
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}
</style>