<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.habits.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.habits.description') }}</p>

    <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.preparoAlimentos') }}</label>
        <input v-model="form.preparoAlimentos" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.localRefeicao') }}</label>
        <input v-model="form.localRefeicao" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.horarioTrabalho') }}</label>
        <input v-model="form.horarioTrabalho" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.horarioEstudo') }}</label>
        <input v-model="form.horarioEstudo" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.apetite') }}</label>
        <input v-model="form.apetite" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.ingestaoHidrica') }}</label>
        <input v-model="form.ingestaoHidrica" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.atividadeFisica') }}</label>
        <input v-model="form.atividadeFisica" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.frequencia') }}</label>
        <input v-model="form.frequencia" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.duracao') }}</label>
        <input v-model="form.duracao" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div class="flex items-center gap-3 rounded-xl border border-slate-200 p-4">
        <input v-model="form.fuma" type="checkbox"
          class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
        <span class="text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.fuma') }}</span>
      </div>
      <div class="flex items-center gap-3 rounded-xl border border-slate-200 p-4">
        <input v-model="form.bebe" type="checkbox"
          class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
        <span class="text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.bebe') }}</span>
      </div>
      <div class="md:col-span-2">
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.suplementos') }}</label>
        <input v-model="form.suplementos" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.sono') }}</label>
        <input v-model="form.sono" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.mastigacao') }}</label>
        <input v-model="form.mastigacao" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div class="md:col-span-2">
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.habits.fields.observacoes') }}</label>
        <textarea v-model="form.habitosObservacoes" rows="3"
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
