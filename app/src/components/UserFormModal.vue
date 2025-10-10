<template>
  <transition name="modal">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/60 px-4 py-10 sm:py-16"
    >
      <div class="w-full max-w-3xl rounded-3xl bg-white shadow-2xl">
        <header class="flex items-start justify-between border-b border-slate-200 px-6 py-4">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">{{ isEdit ? 'Edit user' : 'Create new user' }}</h3>
            <p class="mt-1 text-sm text-slate-500">
              {{ isEdit ? 'Update user account details and permissions.' : 'Provision a new user for the workspace.' }}
            </p>
          </div>
          <button type="button" class="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600" @click="close">
            <XMarkIcon class="h-5 w-5" />
          </button>
        </header>

        <form class="grid gap-6 px-6 py-6" @submit.prevent="handleSubmit">
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label for="name" class="mb-1 block text-sm font-medium text-slate-700">First name</label>
              <input id="name" v-model="form.name" type="text" class="input" required />
              <p v-if="errors.name" class="mt-1 text-xs text-red-600">{{ errors.name }}</p>
            </div>
            <div>
              <label for="last-name" class="mb-1 block text-sm font-medium text-slate-700">Last name</label>
              <input id="last-name" v-model="form.lastName" type="text" class="input" />
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label for="email" class="mb-1 block text-sm font-medium text-slate-700">Email</label>
              <input id="email" v-model="form.email" type="email" class="input" required />
              <p v-if="errors.email" class="mt-1 text-xs text-red-600">{{ errors.email }}</p>
            </div>
            <div>
              <label for="phone" class="mb-1 block text-sm font-medium text-slate-700">Phone</label>
              <input id="phone" v-model="form.phoneNumber" type="tel" class="input" />
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label for="document" class="mb-1 block text-sm font-medium text-slate-700">Document</label>
              <input id="document" v-model="form.documentNumber" type="text" class="input" />
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Role</label>
              <select v-model="form.role" class="input" required>
                <option disabled value="">Select role</option>
                <option value="CLIENT">Client</option>
                <option value="ADMIN">Administrator</option>
              </select>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label for="password" class="mb-1 block text-sm font-medium text-slate-700">Password</label>
              <input
                id="password"
                v-model="form.password"
                type="password"
                class="input"
                :required="!isEdit"
                :placeholder="isEdit ? 'Leave blank to keep current password' : ''"
              />
              <p v-if="errors.password" class="mt-1 text-xs text-red-600">{{ errors.password }}</p>
            </div>
            <div>
              <label for="passwordConfirmation" class="mb-1 block text-sm font-medium text-slate-700">Confirm password</label>
              <input
                id="passwordConfirmation"
                v-model="form.passwordConfirmation"
                type="password"
                class="input"
                :required="!isEdit"
              />
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label for="country" class="mb-1 block text-sm font-medium text-slate-700">Country</label>
              <select
                id="country"
                v-model="form.countryId"
                class="input"
                :disabled="countries.length === 0"
                @change="handleCountryChange"
              >
                <option value="">Select country</option>
                <option v-for="country in countries" :key="country.id" :value="country.id">
                  {{ country.name }}
                </option>
              </select>
            </div>
            <div>
              <label for="city" class="mb-1 block text-sm font-medium text-slate-700">City</label>
              <select
                id="city"
                v-model="form.cityId"
                class="input"
                :disabled="loadingCities || !form.countryId || cities.length === 0"
              >
                <option value="">Select city</option>
                <option v-for="city in cities" :key="city.id" :value="city.id">{{ city.name }}</option>
              </select>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label for="state" class="mb-1 block text-sm font-medium text-slate-700">State</label>
              <input id="state" v-model="form.state" type="text" class="input" />
            </div>
            <div>
              <label for="postal-code" class="mb-1 block text-sm font-medium text-slate-700">Postal code</label>
              <input id="postal-code" v-model="form.postalCode" type="text" class="input" />
            </div>
          </div>

          <div>
            <label for="street" class="mb-1 block text-sm font-medium text-slate-700">Street</label>
            <input id="street" v-model="form.street" type="text" class="input" />
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label for="education" class="mb-1 block text-sm font-medium text-slate-700">Education</label>
              <select id="education" v-model="form.educationLevelId" class="input">
                <option value="">Select education level</option>
                <option v-for="level in educationLevels" :key="level.id" :value="level.id">{{ level.name }}</option>
              </select>
            </div>
            <div>
              <label for="profession" class="mb-1 block text-sm font-medium text-slate-700">Profession</label>
              <select id="profession" v-model="form.professionId" class="input">
                <option value="">Select profession</option>
                <option v-for="profession in professions" :key="profession.id" :value="profession.id">
                  {{ profession.name }}
                </option>
              </select>
            </div>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Avatar</label>
            <div class="flex flex-wrap items-center gap-4">
              <div class="h-16 w-16 overflow-hidden rounded-2xl border border-dashed border-slate-300 bg-slate-50">
                <img v-if="preview" :src="preview" alt="Avatar preview" class="h-full w-full object-cover" />
                <div v-else class="flex h-full items-center justify-center text-xs text-slate-400">Preview</div>
              </div>
              <label class="btn-secondary cursor-pointer">
                <PhotoIcon class="h-4 w-4" />
                <span>Upload</span>
                <input type="file" accept="image/*" class="hidden" @change="handleFile" />
              </label>
              <button
                v-if="form.avatarFile"
                type="button"
                class="text-sm font-semibold text-red-500"
                @click="removeFile"
              >
                Remove upload
              </button>
            </div>
            <p class="mt-2 text-xs text-slate-400">PNG or JPG up to 2MB.</p>
          </div>

          <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
            <button type="button" class="btn-secondary" @click="close">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              <span v-if="submitting" class="flex items-center gap-2">
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
                Saving...
              </span>
              <span v-else>{{ isEdit ? 'Update user' : 'Create user' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { PhotoIcon, XMarkIcon } from '@heroicons/vue/24/outline';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  user: { type: Object, default: null },
  submitting: { type: Boolean, default: false },
  countries: { type: Array, default: () => [] },
  cities: { type: Array, default: () => [] },
  educationLevels: { type: Array, default: () => [] },
  professions: { type: Array, default: () => [] },
  loadingCities: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue', 'submit', 'fetch-cities']);

const form = reactive({
  name: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  documentNumber: '',
  role: 'CLIENT',
  password: '',
  passwordConfirmation: '',
  countryId: '',
  cityId: '',
  state: '',
  postalCode: '',
  street: '',
  educationLevelId: '',
  professionId: '',
  avatarFile: null,
  avatarUrl: '',
});

const preview = ref('');
const errors = reactive({ name: '', email: '', password: '' });
const isEdit = computed(() => Boolean(props.user?.id));
let initializing = false;

watch(
  () => props.user,
  (user) => {
    initializing = true;
    /*resetErrors();*/
    if (!user) {
      /*resetForm();*/
      preview.value = '';
      initializing = false;
      return;
    }
    form.name = user.name ?? '';
    form.lastName = user.lastName ?? '';
    form.email = user.email ?? '';
    form.phoneNumber = user.phoneNumber ?? '';
    form.documentNumber = user.documentNumber ?? '';
    form.role = user.role ?? 'CLIENT';
    form.countryId = user.countryId ?? '';
    form.cityId = user.cityId ?? '';
    form.state = user.state ?? '';
    form.postalCode = user.postalCode ?? '';
    form.street = user.street ?? '';
    form.educationLevelId = user.educationLevelId ?? '';
    form.professionId = user.professionId ?? '';
    form.password = '';
    form.passwordConfirmation = '';
    form.avatarFile = null;
    form.avatarUrl = user.avatarUrl ?? '';
    preview.value = user.avatarUrl ?? '';
    if (user.countryId) {
      emit('fetch-cities', user.countryId);
    }
    initializing = false;
  },
  { immediate: true }
);

watch(
  () => props.cities,
  (cities) => {
    if (initializing || !form.cityId) return;
    if (!cities.some((city) => city.id === form.cityId)) {
      form.cityId = '';
    }
  }
);

watch(
  () => props.modelValue,
  (open) => {
    if (!open) {
      resetForm();
      preview.value = '';
      resetErrors();
    }
  }
);

const resetErrors = () => {
  errors.name = '';
  errors.email = '';
  errors.password = '';
};

const resetForm = () => {
  form.name = '';
  form.lastName = '';
  form.email = '';
  form.phoneNumber = '';
  form.documentNumber = '';
  form.role = 'CLIENT';
  form.password = '';
  form.passwordConfirmation = '';
  form.countryId = '';
  form.cityId = '';
  form.state = '';
  form.postalCode = '';
  form.street = '';
  form.educationLevelId = '';
  form.professionId = '';
  form.avatarFile = null;
  form.avatarUrl = '';
};

const close = () => {
  emit('update:modelValue', false);
};

const handleFile = (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  form.avatarFile = file;
  const reader = new FileReader();
  reader.onload = () => {
    preview.value = reader.result;
  };
  reader.readAsDataURL(file);
};

const removeFile = () => {
  form.avatarFile = null;
  preview.value = form.avatarUrl ?? '';
};

const handleCountryChange = () => {
  if (initializing) return;
  const countryId = form.countryId || '';
  form.cityId = '';
  emit('fetch-cities', countryId);
};

const handleSubmit = () => {
  resetErrors();
  if (form.password && form.password !== form.passwordConfirmation) {
    errors.password = 'Passwords do not match.';
    return;
  }
  const payload = {
    name: form.name,
    lastName: form.lastName,
    email: form.email,
    role: form.role,
    password: form.password,
    documentNumber: form.documentNumber,
    phoneNumber: form.phoneNumber,
    street: form.street,
    state: form.state,
    postalCode: form.postalCode,
    countryId: form.countryId,
    cityId: form.cityId,
    educationLevelId: form.educationLevelId,
    professionId: form.professionId,
    avatarFile: form.avatarFile,
    avatarUrl: form.avatarUrl,
  };
  emit('submit', payload);
};
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.25s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
