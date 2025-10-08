<template>
  <div>
    <h2 class="text-lg font-semibold text-slate-900">{{ t('anamnese.steps.personal.title') }}</h2>
    <p class="mt-1 text-sm text-slate-500">{{ t('anamnese.steps.personal.description') }}</p>

    <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
      <div class="md:col-span-2" v-if="isAdmin">
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.selectLabel') }}</label>
        <select v-model="selectedUserId" :disabled="loading"
          class="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200">
          <option value="">{{ t('anamnese.steps.personal.placeholder') }}</option>
          <option v-for="user in users" :key="user.id" :value="user.id">
            {{ formatUserName(user) }}
          </option>
        </select>
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">
          {{ t('anamnese.steps.personal.fields.patient') }} <span class="text-red-500">*</span>
        </label>
        <input v-model="form.paciente" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.address') }}</label>
        <input v-model="form.endereco" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.country') }}</label>
        <select v-model="selectedCountryId" :disabled="referenceLoading.general"
          class="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200">
          <option value="">{{ t('common.placeholders.select') }}</option>
          <option v-for="country in countries" :key="country.id" :value="country.id">{{ country.name }}</option>
        </select>
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.city') }}</label>
        <select v-model="selectedCityId" :disabled="!selectedCountryId || referenceLoading.cities"
          class="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200">
          <option value="">{{ t('common.placeholders.select') }}</option>
          <option v-for="city in cities" :key="city.id" :value="city.id">{{ city.name }}</option>
        </select>
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.birthDate') }}</label>
        <input v-model="form.dataNascimento" type="date"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.age') }}</label>
        <input v-model.number="form.idade" type="number" min="0"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">
          {{ t('anamnese.steps.personal.fields.phone') }} <span class="text-red-500">*</span>
        </label>
        <input v-model="form.telefone" type="tel"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.education') }}</label>
        <select v-model="selectedEducationLevelId" :disabled="referenceLoading.general"
          class="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200">
          <option value="">{{ t('common.placeholders.select') }}</option>
          <option v-for="level in educationLevels" :key="level.id" :value="level.id">{{ level.name }}</option>
        </select>
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.occupation') }}</label>
        <select v-model="selectedProfessionId" :disabled="referenceLoading.general"
          class="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200">
          <option value="">{{ t('common.placeholders.select') }}</option>
          <option v-for="profession in professions" :key="profession.id" :value="profession.id">{{ profession.name }}</option>
        </select>
      </div>
      <div class="md:col-span-2">
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.goal') }}</label>
        <textarea v-model="form.objetivoConsulta" rows="3"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200"></textarea>
      </div>
    </div>

   <!-- <div v-else class="mt-6 rounded-xl border border-dashed border-slate-300 bg-slate-50 p-6 text-sm text-slate-600">
      {{ t('common.adminOnly') }}
    </div> -->
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { getUsers } from '@/services/users';

const props = defineProps({
  form: {
    type: Object,
    required: true,
  },
  isAdmin: {
    type: Boolean,
    default: false,
  },
  countries: {
    type: Array,
    default: () => [],
  },
  cities: {
    type: Array,
    default: () => [],
  },
  educationLevels: {
    type: Array,
    default: () => [],
  },
  professions: {
    type: Array,
    default: () => [],
  },
  loadingReference: {
    type: Object,
    default: () => ({ general: false, cities: false }),
  },
});

const form = props.form;
const isAdmin = computed(() => props.isAdmin);
const users = ref([]);
const loading = ref(false);
const { t } = useI18n();

const countries = computed(() => props.countries ?? []);
const cities = computed(() => props.cities ?? []);
const educationLevels = computed(() => props.educationLevels ?? []);
const professions = computed(() => props.professions ?? []);
const referenceLoading = computed(() => ({
  general: Boolean(props.loadingReference?.general),
  cities: Boolean(props.loadingReference?.cities),
}));

const formatUserName = (user) => {
  return [user.name, user.lastName].filter(Boolean).join(' ');
};

const hydrateFromUser = (user) => {
  if (!user) return;
  form.paciente = formatUserName(user);
  form.telefone = user.phoneNumber || '';
  const countryName = user.countryDTO?.name || user.countryName || user.country || '';
  const cityName = user.cityName || (typeof user.city === 'string' ? user.city : '');
  form.endereco = [user.street, cityName, user.state, countryName]
    .filter((part) => part && String(part).length)
    .join(', ');
  form.dataNascimento = user.birthDate || '';
  form.idade = user.age ?? null;
  form.countryId = user.countryId || user.countryDTO?.id || null;
  form.cityId = user.cityId || null;
  form.educationLevelId = user.educationLevelId || null;
  form.professionId = user.professionId || null;
  form.escolaridade = user.educationLevelName || user.education || '';
  form.profissao = user.professionName || user.occupation || '';
  form.objetivoConsulta = user.consultationGoal || '';
};

const selectedUserId = computed({
  get: () => form.userId || '',
  set: (value) => {
    form.userId = value || null;
    if (!value) {
      return;
    }
    const user = users.value.find((item) => item.id === value);
    if (user) {
      hydrateFromUser(user);
    }
  },
});

const selectedCountryId = computed({
  get: () => form.countryId || '',
  set: (value) => {
    form.countryId = value || null;
    if (!value) {
      form.cityId = null;
    }
  },
});

const selectedCityId = computed({
  get: () => form.cityId || '',
  set: (value) => {
    form.cityId = value || null;
  },
});

const selectedEducationLevelId = computed({
  get: () => form.educationLevelId || '',
  set: (value) => {
    form.educationLevelId = value || null;
    if (!value) {
      form.escolaridade = '';
    }
  },
});

const selectedProfessionId = computed({
  get: () => form.professionId || '',
  set: (value) => {
    form.professionId = value || null;
    if (!value) {
      form.profissao = '';
    }
  },
});

const fetchUsers = async () => {
  if (!isAdmin.value) return;
  loading.value = true;
  try {
    const { data } = await getUsers({ size: 50, page: 0 });
    users.value = data?.content ?? [];
    if (form.userId) {
      const existing = users.value.find((item) => item.id === form.userId);
      if (existing) {
        hydrateFromUser(existing);
      }
    }
  } catch (error) {
    // handled globally
  } finally {
    loading.value = false;
  }
};

onMounted(fetchUsers);

const syncEducationName = () => {
  if (!form.educationLevelId) {
    return;
  }
  const match = educationLevels.value.find((item) => item.id === form.educationLevelId);
  if (match) {
    form.escolaridade = match.name;
  }
};

const syncProfessionName = () => {
  if (!form.professionId) {
    return;
  }
  const match = professions.value.find((item) => item.id === form.professionId);
  if (match) {
    form.profissao = match.name;
  }
};

watch(() => form.educationLevelId, syncEducationName, { immediate: true });
watch(educationLevels, syncEducationName, { deep: true });

watch(() => form.professionId, syncProfessionName, { immediate: true });
watch(professions, syncProfessionName, { deep: true });

watch(countries, (list) => {
  if (form.countryId && !list.some((item) => item.id === form.countryId)) {
    form.countryId = null;
  }
});

watch(cities, (list) => {
  if (form.cityId && !list.some((item) => item.id === form.cityId)) {
    form.cityId = null;
  }
});

watch(
  () => form.userId,
  (newId, oldId) => {
    if (!newId || newId === oldId) {
      return;
    }
    const user = users.value.find((item) => item.id === newId);
    if (user) {
      hydrateFromUser(user);
    }
  }
);

watch(users, (list) => {
  if (!form.userId || !list.length) {
    return;
  }
  const user = list.find((item) => item.id === form.userId);
  if (user && !form.paciente) {
    hydrateFromUser(user);
  }
});
</script>
