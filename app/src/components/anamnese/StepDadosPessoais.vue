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
        <input v-model="form.escolaridade" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
      </div>
      <div>
        <label class="block text-sm font-medium text-slate-700">{{ t('anamnese.steps.personal.fields.occupation') }}</label>
        <input v-model="form.profissao" type="text"
          class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-200" />
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
import { computed, isShallow, onMounted, ref, watch } from 'vue';
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
});

const form = props.form;
const isAdmin = computed(() => props.isAdmin);
const users = ref([]);
const loading = ref(false);
const { t } = useI18n();

const formatUserName = (user) => {
  return [user.name, user.lastName].filter(Boolean).join(' ');
};

const hydrateFromUser = (user) => {
  if (!user) return;
  form.paciente = formatUserName(user);
  form.telefone = user.phoneNumber || '';
  form.endereco = [user.street, user.city, user.state, user.country].filter((part) => part && part.length).join(', ');
  form.dataNascimento = user.birthDate || '';
  form.idade = user.age ?? null;
  form.escolaridade = user.education || '';
  form.profissao = user.occupation || '';
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
