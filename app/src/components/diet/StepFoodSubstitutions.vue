<template>
  <div class="space-y-6">
    <header class="space-y-2">
      <h3 class="text-lg font-semibold text-slate-800">{{ t('diet.wizard.substitutions.title') }}</h3>
      <p class="text-sm text-slate-500">{{ t('diet.wizard.substitutions.description') }}</p>
    </header>

    <section class="space-y-3">
      <div class="flex flex-wrap items-center gap-3">
        <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">
          {{ t('diet.wizard.substitutions.filters.label') }}
        </span>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="option in resolvedCategories"
            :key="option.value"
            type="button"
            class="rounded-full border px-4 py-1 text-sm font-semibold transition"
            :class="
              selectedCategory === option.value
                ? 'border-primary-500 bg-primary-50 text-primary-600 shadow-sm'
                : 'border-slate-200 bg-white text-slate-500 hover:border-primary-200 hover:text-primary-600'
            "
            @click="selectCategory(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>
      <p class="text-xs text-slate-400">
        {{ t('diet.wizard.substitutions.filters.helper') }}
      </p>
    </section>

    <div v-if="loading" class="space-y-4">
      <div class="h-5 w-48 animate-pulse rounded-full bg-slate-200"></div>
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div v-for="index in 3" :key="index" class="h-44 animate-pulse rounded-2xl bg-slate-200"></div>
      </div>
    </div>

    <div
      v-else-if="!filteredSuggestions.length"
      class="rounded-3xl border border-dashed border-slate-300 p-6 text-center text-slate-500"
    >
      {{ suggestions.length ? t('diet.wizard.substitutions.emptyFiltered') : t('diet.wizard.substitutions.emptyState') }}
    </div>

    <div v-else class="space-y-8">
      <section
        v-for="suggestion in filteredSuggestions"
        :key="suggestion.id"
        class="space-y-4 rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <header class="space-y-2">
          <div class="flex flex-wrap items-center gap-3">
            <span class="rounded-full bg-primary-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-primary-600">
              {{ t('diet.wizard.substitutions.originalLabel') }}
            </span>
            <span
              class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-slate-500"
            >
              {{ suggestion.mealName }}
              <span v-if="suggestion.mealTime" class="ml-1 font-normal normal-case text-slate-400">
                • {{ formatTime(suggestion.mealTime) }}
              </span>
            </span>
          </div>
          <div>
            <h4 class="text-base font-semibold text-slate-800">
              {{ suggestion.original.name }}
              <span v-if="suggestion.original.portionLabel" class="text-sm font-normal text-slate-500">
                ({{ suggestion.original.portionLabel }})
              </span>
            </h4>
            <p class="text-xs text-slate-500">
              {{ macroSummary(suggestion.original) }}
              <span v-if="suggestion.original.calories">
                • {{ t('diet.wizard.substitutions.calorieLabel', { value: formatNumber(suggestion.original.calories) }) }}
              </span>
            </p>
          </div>
        </header>

        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <article
            v-for="alternative in suggestion.alternatives"
            :key="alternative.id"
            class="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-slate-50 p-4 shadow-sm transition hover:border-primary-200 hover:bg-white hover:shadow-md"
          >
            <div class="flex items-center gap-4">
              <div v-if="alternative.imageUrl" class="h-16 w-16 overflow-hidden rounded-xl bg-slate-100">
                <img :src="alternative.imageUrl" :alt="alternative.name" class="h-full w-full object-cover" />
              </div>
              <div v-else class="flex h-16 w-16 items-center justify-center rounded-xl bg-primary-50 text-lg font-semibold text-primary-600">
                {{ initials(alternative.name) }}
              </div>
              <div class="space-y-1">
                <h5 class="text-sm font-semibold text-slate-800">{{ alternative.name }}</h5>
                <p v-if="alternative.portionLabel" class="text-xs text-slate-500">
                  {{ t('diet.wizard.substitutions.portionLabel', { portion: alternative.portionLabel }) }}
                </p>
                <p class="text-xs text-slate-500">{{ macroSummary(alternative) }}</p>
                <p v-if="alternative.calories" class="text-xs font-semibold text-slate-600">
                  {{ t('diet.wizard.substitutions.calorieLabel', { value: formatNumber(alternative.calories) }) }}
                </p>
              </div>
            </div>

            <button
              type="button"
              class="flex items-center justify-center gap-2 rounded-xl bg-emerald-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-emerald-600 disabled:cursor-not-allowed disabled:bg-emerald-300"
              :disabled="disabled"
              @click="replaceItem(suggestion, alternative)"
            >
              {{ t('diet.wizard.substitutions.replace') }}
            </button>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, toRefs, watch } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  suggestions: { type: Array, default: () => [] },
  categories: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(['replace']);

const { t } = useI18n();

const { suggestions, loading, disabled } = toRefs(props);

const selectedCategory = ref('ALL');

const resolvedCategories = computed(() => {
  if (!props.categories.length) {
    return [
      { value: 'ALL', label: t('diet.wizard.substitutions.filters.all') },
      { value: 'PROTEIN', label: t('diet.wizard.substitutions.filters.protein') },
      { value: 'CARBS', label: t('diet.wizard.substitutions.filters.carbs') },
      { value: 'FAT', label: t('diet.wizard.substitutions.filters.fat') },
      { value: 'FIBER', label: t('diet.wizard.substitutions.filters.fiber') },
    ];
  }
  return props.categories;
});

watch(
  () => resolvedCategories.value,
  (options) => {
    if (!options.some((option) => option.value === selectedCategory.value)) {
      selectedCategory.value = options[0]?.value ?? 'ALL';
    }
  },
  { immediate: true },
);

const filteredSuggestions = computed(() => {
  const allSuggestions = suggestions.value ?? [];
  if (selectedCategory.value === 'ALL') {
    return allSuggestions;
  }
  return allSuggestions.filter((suggestion) => suggestion.category === selectedCategory.value);
});

const formatNumber = (value) => {
  if (value == null || Number.isNaN(Number(value))) return '';
  return new Intl.NumberFormat(undefined, { maximumFractionDigits: 1 }).format(Number(value));
};

const formatTime = (value) => {
  if (!value) return '';
  if (typeof value === 'string' && value.length >= 5) {
    return value.slice(0, 5);
  }
  return value;
};

const macroSummary = (food) => {
  const macros = [
    { key: 'protein', value: food.protein, label: t('diet.wizard.substitutions.macros.protein') },
    { key: 'carbs', value: food.carbs, label: t('diet.wizard.substitutions.macros.carbs') },
    { key: 'fat', value: food.fat, label: t('diet.wizard.substitutions.macros.fat') },
    { key: 'fiber', value: food.fiber, label: t('diet.wizard.substitutions.macros.fiber') },
  ];
  const parts = macros
    .filter((macro) => macro.value && Number(macro.value) > 0)
    .map((macro) => `${formatNumber(macro.value)}g ${macro.label}`);
  return parts.join(' • ');
};

const initials = (name = '') => {
  const cleaned = name.trim();
  if (!cleaned) return 'AI';
  const words = cleaned.split(/\s+/);
  if (words.length === 1) {
    return words[0].slice(0, 2).toUpperCase();
  }
  return `${words[0][0] ?? ''}${words[1][0] ?? ''}`.toUpperCase();
};

const selectCategory = (value) => {
  selectedCategory.value = value;
};

const replaceItem = (suggestion, alternative) => {
  emit('replace', {
    mealIndex: suggestion.mealIndex,
    itemIndex: suggestion.itemIndex,
    foodId: alternative.id,
    foodName: alternative.name,
  });
};
</script>
