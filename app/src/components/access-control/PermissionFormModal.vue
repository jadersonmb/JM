<template>
  <transition name="fade">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-start justify-center bg-slate-900/70 px-4 py-10">
      <div class="w-full max-w-2xl rounded-2xl bg-white p-6 shadow-xl">
        <header class="flex items-start justify-between border-b border-slate-200 pb-4">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">{{ isEdit ? t('accessControl.sections.permissions.modal.editTitle') : t('accessControl.sections.permissions.modal.createTitle') }}</h2>
            <p class="text-sm text-slate-500">{{ t('accessControl.sections.permissions.modal.description') }}</p>
          </div>
          <button type="button" class="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600" @click="close">
            ✕
          </button>
        </header>

        <form class="mt-4 space-y-4" @submit.prevent="submit">
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">{{ t('accessControl.sections.permissions.modal.fields.code') }}</label>
              <input v-model="form.code" type="text" class="input" :placeholder="t('accessControl.sections.permissions.modal.fields.codePlaceholder')" required />
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">{{ t('accessControl.sections.permissions.modal.fields.action') }}</label>
              <select v-model="form.actionId" class="input" required>
                <option value="" disabled>{{ t('common.placeholders.select') }}</option>
                <option v-for="action in actions" :key="action.id" :value="action.id">{{ action.name }}</option>
              </select>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">{{ t('accessControl.sections.permissions.modal.fields.object') }}</label>
              <select v-model="form.objectId" class="input" required>
                <option value="" disabled>{{ t('common.placeholders.select') }}</option>
                <option v-for="object in objects" :key="object.id" :value="object.id">{{ object.name }}</option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">{{ t('accessControl.sections.permissions.modal.fields.description') }}</label>
              <input v-model="form.description" type="text" class="input" :placeholder="t('accessControl.sections.permissions.modal.fields.descriptionPlaceholder')" />
            </div>
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
  permission: { type: Object, default: null },
  actions: { type: Array, default: () => [] },
  objects: { type: Array, default: () => [] },
  submitting: { type: Boolean, default: false },
  error: { type: String, default: '' },
});

const emit = defineEmits(['update:modelValue', 'submit', 'close']);

const { t } = useI18n();

const form = reactive({
  code: '',
  description: '',
  actionId: '',
  objectId: '',
});

const isEdit = computed(() => Boolean(props.permission?.id));

watch(
  () => props.permission,
  (value) => {
    form.code = value?.code ?? '';
    form.description = value?.description ?? '';
    form.actionId = value?.action?.id ?? '';
    form.objectId = value?.object?.id ?? '';
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
