<template>
  <div class="space-y-6">
    <section class="space-y-3">
      <section v-if="isAdmin" class="space-y-3">
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ t('goals.wizard.fields.owner') }}</p>
        <select class="input" :value="ownerId ?? ''" @change="emitOwner($event.target.value)">
          <option value="">{{ t('common.placeholders.select') }}</option>
          <option v-for="owner in owners" :key="owner.id" :value="owner.id">
            {{ owner.displayName || owner.email }}
          </option>
        </select>
        <p v-if="loadingOwners" class="text-sm text-slate-400">{{ t('common.actions.search') }}...</p>
      </section>
      <div class="flex flex-col gap-1">
        <label class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ t('goals.wizard.fields.template')
          }}</label>
        <select class="input" :disabled="loadingTemplates || templates.length === 0" :value="selectedTemplateId ?? ''"
          @change="(event) => emitTemplate(event.target.value)">
          <option value="">{{ t('goals.wizard.fields.templatePlaceholder') }}</option>
          <option v-for="template in templates" :key="template.id" :value="template.id">
            {{ template.name }}
          </option>
        </select>
      </div>
      <button v-if="selectedTemplateId" type="button"
        class="text-sm font-semibold text-primary-600 hover:text-primary-500" @click="emitTemplate('')">
        {{ t('goals.wizard.actions.clearTemplate') }}
      </button>
    </section>

    <section class="space-y-3">
      <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">{{ t('goals.type') }}</p>
      <div class="grid gap-3 sm:grid-cols-2">
        <label v-for="option in types" :key="option.value"
          class="flex cursor-pointer items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm transition hover:border-primary-200 hover:text-primary-600">
          <div>
            <p class="font-semibold">{{ option.label }}</p>
          </div>
          <input type="radio" class="h-4 w-4 text-primary-600 focus:ring-primary-500" name="goal-type"
            :value="option.value" :checked="modelValue === option.value" @change="emitType(option.value)" />
        </label>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  types: {
    type: Array,
    default: () => [],
  },
  templates: {
    type: Array,
    default: () => [],
  },
  selectedTemplateId: {
    type: String,
    default: null,
  },
  loadingTemplates: {
    type: Boolean,
    default: false,
  },
  isAdmin: {
    type: Boolean,
    default: false,
  },
  ownerId: {
    type: String,
    default: null,
  },
  owners: {
    type: Array,
    default: () => [],
  },
  loadingOwners: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:modelValue', 'update:selectedTemplateId', 'update:ownerId', 'search-owner']);

const { t } = useI18n();
const ownerQuery = ref('');
let debounceTimeout;

watch(
  () => ownerQuery.value,
  (value) => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(() => {
      emit('search-owner', value);
    }, 300);
  }
);

const emitType = (value) => emit('update:modelValue', value);
const emitTemplate = (value) => emit('update:selectedTemplateId', value || null);
const emitOwner = (value) => emit('update:ownerId', value || null);
const triggerOwnerSearch = () => emit('search-owner', ownerQuery.value);
</script>
