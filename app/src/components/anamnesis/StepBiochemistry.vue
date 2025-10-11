<template>
  <div>
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnesis.steps.biochemistry.title') }}</h2>
        <p class="mt-1 text-sm text-slate-500">
          {{ t('anamnesis.steps.biochemistry.description') }}
        </p>
      </div>
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500"
        @click="addResult"
      >
        {{ t('common.actions.addExam') }}
      </button>
    </div>

    <div v-if="records.length" class="mt-6 space-y-4">
      <div
        v-for="(record, index) in records"
        :key="record.__key || record.id || index"
        class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div class="grid grid-cols-1 gap-4 md:grid-cols-4">
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-slate-700">
              {{ t('anamnesis.steps.biochemistry.fields.exam') }}
              <span class="text-red-500">*</span>
            </label>
            <select
              v-model="record.biochemicalExamId"
              @change="handleExamChange(record)"
              class="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
              required
            >
              <option value="">{{ t('common.placeholders.select') }}</option>
              <option
                v-for="exam in biochemicalExams"
                :key="exam.id"
                :value="exam.id"
              >
                {{ exam.name }}
              </option>
            </select>
            <p v-if="record.biochemicalExamName" class="mt-2 text-xs text-slate-500">
              {{ record.biochemicalExamName }}
            </p>
            <p v-if="selectedUnit(record)" class="mt-1 text-xs text-slate-400">
              {{ t('anamnesis.steps.biochemistry.fields.unit', { unit: selectedUnit(record) }) }}
            </p>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700">
              {{ t('anamnesis.steps.biochemistry.fields.value') }}
            </label>
            <input
              v-model="record.resultValue"
              type="text"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700">
              {{ t('anamnesis.steps.biochemistry.fields.date') }}
            </label>
            <input
              v-model="record.resultDate"
              type="date"
              class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"
            />
          </div>
        </div>
        <div class="mt-4 flex justify-end">
          <button
            type="button"
            class="text-sm font-semibold text-red-600 hover:text-red-500"
            @click="removeResult(index)"
          >
            {{ t('common.actions.remove') }}
          </button>
        </div>
      </div>
    </div>
    <div v-else class="mt-6 rounded-xl border border-dashed border-slate-300 bg-slate-50 p-6 text-sm text-slate-600">
      {{ t('common.empty.exams') }}
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
  biochemicalExams: {
    type: Array,
    default: () => [],
  },
});

const form = props.form;
const biochemicalExams = computed(() => props.biochemicalExams ?? []);
const examMap = computed(() => new Map((biochemicalExams.value ?? []).map((exam) => [exam.id, exam])));
const { t } = useI18n();

const ensureArray = () => {
  if (!Array.isArray(form.biochemicalResults)) {
    form.biochemicalResults = [];
  }
};

onMounted(ensureArray);

const records = computed(() => form.biochemicalResults ?? []);

const generateKey = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return Math.random().toString(36).slice(2);
};

const addResult = () => {
  ensureArray();
  form.biochemicalResults.push({
    id: null,
    biochemicalExamId: '',
    biochemicalExamName: '',
    resultValue: '',
    resultDate: '',
    __key: generateKey(),
  });
};

const removeResult = (index) => {
  ensureArray();
  form.biochemicalResults.splice(index, 1);
};

const handleExamChange = (item) => {
  const exam = examMap.value.get(item.biochemicalExamId) ?? null;
  item.biochemicalExamName = exam?.name ?? '';
};

const selectedUnit = (item) => {
  const exam = examMap.value.get(item.biochemicalExamId) ?? null;
  if (!exam) {
    return '';
  }
  return exam.measurementUnitDescription || exam.measurementUnitSymbol || '';
};
</script>
