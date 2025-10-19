<template>
  <transition name="fade">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-start justify-center bg-slate-900/70 px-4 py-10">
      <div class="w-full max-w-3xl rounded-2xl bg-white p-6 shadow-xl">
        <header class="flex items-start justify-between border-b border-slate-200 pb-4">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">{{ isEdit ? t('accessControl.sections.roles.modal.editTitle') : t('accessControl.sections.roles.modal.createTitle') }}</h2>
            <p class="text-sm text-slate-500">{{ t('accessControl.sections.roles.modal.description') }}</p>
          </div>
          <button type="button" class="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600" @click="close">
            ✕
          </button>
        </header>

        <form class="mt-4 space-y-5" @submit.prevent="submit">
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">{{ t('accessControl.sections.roles.modal.fields.name') }}</label>
              <input v-model="form.name" type="text" class="input" :placeholder="t('accessControl.sections.roles.modal.fields.namePlaceholder')" required />
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">{{ t('accessControl.sections.roles.modal.fields.description') }}</label>
              <input v-model="form.description" type="text" class="input" :placeholder="t('accessControl.sections.roles.modal.fields.descriptionPlaceholder')" />
            </div>
          </div>

          <div>
            <p class="text-sm font-medium text-slate-700">{{ t('accessControl.sections.roles.modal.fields.permissions') }}</p>
            <div class="mt-3 max-h-72 space-y-3 overflow-y-auto pr-2">
              <div v-for="group in groupedPermissions" :key="group.label" class="rounded-xl border border-slate-200 p-3">
                <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ group.label }}</p>
                <div class="mt-2 grid gap-2 md:grid-cols-2">
                  <label
                    v-for="permission in group.items"
                    :key="permission.id"
                    class="flex items-start gap-3 rounded-lg border border-slate-200 p-3 hover:border-primary-300"
                  >
                    <input
                      v-model="selected"
                      type="checkbox"
                      class="mt-1 h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500"
                      :value="permission.id"
                    />
                    <div>
                      <p class="font-medium text-slate-700">{{ permission.code }}</p>
                      <p class="text-xs text-slate-500">{{ permission.description || t('accessControl.sections.roles.modal.fields.permissionFallback') }}</p>
                    </div>
                  </label>
                </div>
              </div>
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
import { computed, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  role: { type: Object, default: null },
  permissions: { type: Array, default: () => [] },
  submitting: { type: Boolean, default: false },
  error: { type: String, default: '' },
});

const emit = defineEmits(['update:modelValue', 'submit', 'close']);

const { t } = useI18n();

const form = reactive({
  name: '',
  description: '',
});

const selected = ref([]);

const isEdit = computed(() => Boolean(props.role?.id));

const groupedPermissions = computed(() => {
  const groups = {};
  props.permissions.forEach((permission) => {
    const groupKey = permission.object?.name ?? t('accessControl.sections.roles.modal.fields.ungrouped');
    if (!groups[groupKey]) {
      groups[groupKey] = [];
    }
    groups[groupKey].push(permission);
  });
  return Object.entries(groups)
    .map(([label, items]) => ({ label, items: items.sort((a, b) => a.code.localeCompare(b.code)) }))
    .sort((a, b) => a.label.localeCompare(b.label));
});

watch(
  () => props.role,
  (value) => {
    form.name = value?.name ?? '';
    form.description = value?.description ?? '';
    selected.value = value?.permissions?.map((permission) => permission.id) ?? [];
  },
  { immediate: true }
);

const submit = () => {
  emit('submit', { ...form, permissionIds: [...selected.value] });
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
