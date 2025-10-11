<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnesis.steps.habits.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnesis.steps.habits.description') }}</p>

    <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.mealPreparation') }}</label>
        <input v-model="form.mealPreparation" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.mealLocation') }}</label>
        <input v-model="form.mealLocation" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.workSchedule') }}</label>
        <input v-model="form.workSchedule" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.studySchedule') }}</label>
        <input v-model="form.studySchedule" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.appetite') }}</label>
        <input v-model="form.appetite" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.waterIntake') }}</label>
        <input v-model="form.waterIntake" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.physicalActivity') }}</label>
        <input v-model="form.physicalActivity" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.activityFrequency') }}</label>
        <input v-model="form.activityFrequency" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.activityDuration') }}</label>
        <input v-model="form.activityDuration" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div></div>
      <div class="flex items-center gap-3 rounded-xl border border-slate-200 p-4">
        <input v-model="form.smokes" type="checkbox"
          class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
        <span class="text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.smokes') }}</span>
      </div>
      <div class="flex items-center gap-3 rounded-xl border border-slate-200 p-4">
        <input v-model="form.drinksAlcohol" type="checkbox"
          class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
        <span class="text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.drinksAlcohol') }}</span>
      </div>
      <div class="md:col-span-2">
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.supplements') }}</label>
        <input v-model="form.supplements" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.sleepQuality') }}</label>
        <input v-model="form.sleepQuality" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.chewingQuality') }}</label>
        <input v-model="form.chewingQuality" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div class="md:col-span-2">
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnesis.steps.habits.fields.habitNotes') }}</label>
        <textarea v-model="form.habitNotes" rows="3"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
});

const form = props.form;
const { t } = useI18n();
</script>
