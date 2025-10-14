<template>
  <div class="space-y-6">
    <header class="space-y-1">
      <h1 class="text-2xl font-semibold text-slate-900">
        {{ t('reference.title') }}
      </h1>
      <p class="text-sm text-slate-500">
        {{ t('reference.countries.subtitle') }}
      </p>
    </header>

    <nav class="flex flex-wrap gap-2">
      <RouterLink
        v-for="link in links"
        :key="link.name"
        :to="{ name: link.name }"
        class="rounded-full px-4 py-2 text-sm font-semibold transition"
        :class="isActive(link.name)
          ? 'bg-primary-600 text-white shadow-sm'
          : 'border border-slate-200 text-slate-600 hover:border-primary-300 hover:text-primary-600'
        "
      >
        {{ link.label }}
      </RouterLink>
    </nav>

    <RouterView />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { RouterView, RouterLink, useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const { t } = useI18n();

const links = computed(() => [
  { name: 'reference-countries', label: t('routes.referenceCountries') },
  { name: 'reference-ai-prompts', label: t('routes.referenceAiPrompts') },
  { name: 'reference-cities', label: t('routes.referenceCities') },
  { name: 'reference-education-levels', label: t('routes.referenceEducationLevels') },
  { name: 'reference-meals', label: t('routes.referenceMeals') },
  { name: 'reference-professions', label: t('routes.referenceProfessions') },
]);

const isActive = (name) => route.name === name;
</script>
