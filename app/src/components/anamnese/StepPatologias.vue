<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.pathologies.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.pathologies.description') }}</p>

    <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
      <label v-for="item in pathologies" :key="item.id" class="flex items-center gap-3 rounded-xl border border-slate-200 p-3 text-sm font-medium text-slate-600 shadow-sm">
        <input v-model="form[item.name]" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
        <span>{{ item.name }}</span>
      </label>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { getPathologies } from '@/services/reference';
import { getUserSettings } from '@/services/settings';
import { useAuthStore } from '@/stores/auth';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
});

const form = props.form;
const { t, locale  } = useI18n();
const userSettings = ref({ language: '' });
const auth = useAuthStore();

const referenceParams = computed(() => userSettings.value.language || locale.value);
const loadSettings = async () => {
  if (!auth.user?.id) {
    return;
  }
  try {
    const { data } = await getUserSettings(auth.user.id);
    userSettings.value = { language: data?.language ?? userSettings.value.language ?? locale.value };
  } catch (error) {
    console.error('Failed to load user settings', error);
    userSettings.value = { language: locale.value };
  }
};

onMounted(() => {
  loadSettings();
  load();
});

const pathologies = ref([]);

async function load() { 
  try {
    const param = { language: referenceParams.value};
    const { data } = await getPathologies(param);
    pathologies.value = data;
  } catch (error) {
    console.error('Failed to load user settings', error);
    userSettings.value = { language: locale.value };
  }
}

</script>
