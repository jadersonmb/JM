<template>
  <transition name="fade">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/70 px-4">
      <div class="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">
        <header class="flex items-center justify-between border-b border-slate-200 pb-4">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">{{ isEdit ? t('accessControl.sections.objects.modal.editTitle') : t('accessControl.sections.objects.modal.createTitle') }}</h2>
            <p class="text-sm text-slate-500">{{ t('accessControl.sections.objects.modal.description') }}</p>
          </div>
          <button type="button" class="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600" @click="close">
            ✕
          </button>
        </header>

        <form class="mt-4 space-y-4" @submit.prevent="submit">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">{{ t('accessControl.sections.objects.modal.fields.name') }}</label>
            <input v-model="form.name" type="text" class="input" :placeholder="t('accessControl.sections.objects.modal.fields.placeholder')" required />
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">{{ t('accessControl.sections.objects.modal.fields.description') }}</label>
            <textarea v-model="form.description" rows="3" class="input" :placeholder="t('accessControl.sections.objects.modal.fields.descriptionPlaceholder')"></textarea>
          </div>

          <p v-if="error" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{{ error }}</p>

          <footer class="flex items-center justify-end gap-3 pt-2">
            <button type="button" class="btn-secondary" @click="close">{{ t('common.actions.cancel') }}</button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              <span v-if="submitting" class="flex items-center gap-2">
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600"></span>
                {{ t('common.actions.saving') }}
              </span>
              <span v-else>{{ isEdit ? t('common.actions.update') : t('common.actions.save') }}</span>
            </button>
          </footer>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, reactive, watch } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  object: { type: Object, default: null },
  submitting: { type: Boolean, default: false },
  error: { type: String, default: '' },
});

const emit = defineEmits(['update:modelValue', 'submit', 'close']);

const { t } = useI18n();

const form = reactive({
  name: '',
  description: '',
});

const isEdit = computed(() => Boolean(props.object?.id));

watch(
  () => props.object,
  (value) => {
    form.name = value?.name ?? '';
    form.description = value?.description ?? '';
  },
  { immediate: true }
);

const submit = () => {
  emit('submit', { ...form });
};

const close = () => {
  emit('update:modelValue', false);
  emit('close');
};
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
