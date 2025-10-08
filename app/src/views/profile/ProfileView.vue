<template>
  <div class="grid gap-8 xl:grid-cols-[320px_1fr]">
    <section class="card space-y-6">
      <div class="text-center">
        <div
          class="relative mx-auto h-28 w-28 overflow-hidden rounded-3xl border border-slate-200 bg-primary-50 text-primary-600">
          <img v-if="preview" :src="preview" alt="Avatar" class="h-full w-full object-cover" />
          <div v-else class="flex h-full items-center justify-center text-2xl font-semibold">
            {{ initials(auth.user?.name) }}
          </div>
          <label
            class="absolute bottom-2 left-1/2 flex -translate-x-1/2 cursor-pointer items-center gap-1 rounded-full bg-white px-3 py-1 text-xs font-semibold text-primary-600 shadow">
            <PhotoIcon class="h-3.5 w-3.5" />
            Change
            <input type="file" accept="image/*" class="hidden" @change="onFileChange" />
          </label>
        </div>
        <h2 class="mt-4 text-xl font-semibold text-slate-900">{{ auth.user?.name }}</h2>
        <p class="text-sm text-slate-500">{{ auth.user?.email }}</p>
      </div>

      <dl class="space-y-4 text-sm">
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Role</dt>
          <dd class="mt-1 font-semibold capitalize text-slate-700">{{ auth.user?.role }}</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Status</dt>
          <dd class="mt-1 font-semibold capitalize text-slate-700">{{ auth.user?.status }}</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Member since</dt>
          <dd class="mt-1 font-semibold text-slate-700">{{ formatDate(auth.user?.created_at || auth.user?.createdAt) }}
          </dd>
        </div>
      </dl>
    </section>

    <section class="card space-y-6">
      <header>
        <h2 class="text-xl font-semibold text-slate-900">{{ t('settings.sections.profile.title') }}</h2>
        <p class="mt-1 text-sm text-slate-500">{{ t('settings.sections.profile.subtitle') }}</p>
      </header>

      <form class="space-y-6" @submit.prevent="saveProfile">
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="first-name">
              {{ t('settings.profile.fields.firstName.label') }}
            </label>
            <input id="first-name" v-model="userForm.name" type="text" class="input"
              :placeholder="t('settings.profile.fields.firstName.placeholder')" autocomplete="given-name" required
              :disabled="loadingUser || savingUser" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="last-name">
              {{ t('settings.profile.fields.lastName.label') }}
            </label>
            <input id="last-name" v-model="userForm.lastName" type="text" class="input"
              :placeholder="t('settings.profile.fields.lastName.placeholder')" autocomplete="family-name"
              :disabled="loadingUser || savingUser" />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="email">
              {{ t('settings.profile.fields.email.label') }}
            </label>
            <input id="email" v-model="userForm.email" type="email" class="input" autocomplete="email" disabled />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="phone">
              {{ t('settings.profile.fields.phone.label') }}
            </label>
            <input id="phone" v-model="userForm.phoneNumber" type="tel" class="input"
              :placeholder="t('settings.profile.fields.phone.placeholder')" autocomplete="tel"
              :disabled="loadingUser || savingUser" />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="document">
              {{ t('settings.profile.fields.document.label') }}
            </label>
            <input id="document" v-model="userForm.documentNumber" type="text" class="input"
              :placeholder="t('settings.profile.fields.document.placeholder')" autocomplete="off"
              :disabled="loadingUser || savingUser" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="avatar">
              {{ t('settings.profile.fields.avatar.label') }}
            </label>
            <input id="avatar" v-model="userForm.avatarUrl" type="url" class="input"
              :placeholder="t('settings.profile.fields.avatar.placeholder')" autocomplete="off"
              :disabled="loadingUser || savingUser" />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-3">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="city">
              {{ t('settings.profile.fields.city.label') }}
            </label>
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="state">
              {{ t('settings.profile.fields.state.label') }}
            </label>
            <input id="state" v-model="userForm.state" type="text" class="input"
              :placeholder="t('settings.profile.fields.state.placeholder')" autocomplete="address-level1"
              :disabled="loadingUser || savingUser" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="postal-code">
              {{ t('settings.profile.fields.postalCode.label') }}
            </label>
            <input id="postal-code" v-model="userForm.postalCode" type="text" class="input"
              :placeholder="t('settings.profile.fields.postalCode.placeholder')" autocomplete="postal-code"
              :disabled="loadingUser || savingUser" />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="street">
              {{ t('settings.profile.fields.street.label') }}
            </label>
            <input id="street" v-model="userForm.street" type="text" class="input"
              :placeholder="t('settings.profile.fields.street.placeholder')" autocomplete="street-address"
              :disabled="loadingUser || savingUser" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="country">
              {{ t('settings.profile.fields.country.label') }}
            </label>
            <input id="country" v-model="userForm.country" type="text" class="input"
              :placeholder="t('settings.profile.fields.country.placeholder')" autocomplete="country-name"
              :disabled="loadingUser || savingUser" />
          </div>
        </div>

        <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
          <button type="button" class="btn-secondary" @click="resetProfile"
            :disabled="savingUser || loadingUser || !isUserDirty">
            {{ t('settings.common.actions.revert') }}
          </button>
          <button type="submit" class="btn-primary" :disabled="savingUser || !isUserDirty">
            <span v-if="savingUser" class="flex items-center gap-2">
              <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
              {{ t('settings.common.actions.saving') }}
            </span>
            <span v-else>{{ t('settings.profile.actions.save') }}</span>
          </button>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import { getUser, updateUser } from '@/services/users';
import {
  PhotoIcon
} from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';

const auth = useAuthStore();
const notifications = useNotificationStore();
const { t } = useI18n();
const loadingUser = ref(false);
const savingUser = ref(false);


const emptyUser = () => ({
  id: '',
  name: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  documentNumber: '',
  avatarUrl: '',
  street: '',
  city: '',
  state: '',
  postalCode: '',
  country: '',
});

const userForm = reactive(emptyUser());
const initialUser = ref(structuredCloneIfPossible(emptyUser()));
const file = ref(null);
const preview = ref(auth.user?.avatarUrl ?? auth.user?.avatar ?? '');

function structuredCloneIfPossible(value) {
  try {
    return structuredClone(value);
  } catch (error) {
    return JSON.parse(JSON.stringify(value));
  }
}

const mapUserData = (data) => ({
  ...emptyUser(),
  ...data,
  userId: auth.user?.id ?? data?.userId ?? null,
});

const isUserDirty = computed(() =>
  JSON.stringify(userForm) !== JSON.stringify(initialUser.value ?? {})
);

const resetProfile = () => {
  if (!initialUser.value) {
    return;
  }
  Object.assign(userForm, structuredCloneIfPossible(initialUser.value));
};

const initials = (name = '') =>
  name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .substring(0, 2)
    .toUpperCase();

const onFileChange = (event) => {
  const selected = event.target.files?.[0];
  if (!selected) return;
  file.value = selected;
  const reader = new FileReader();
  reader.onload = () => {
    preview.value = reader.result;
  };
  reader.readAsDataURL(selected);
};

const formatDate = (value) => {
  if (!value) return 'N/A';
  try {
    return new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

const loadUser = async () => {
  if (!auth.user?.id) {
    return;
  }
  loadingUser.value = true;
  try {
    const { data } = await getUser(auth.user.id);
    const mapped = mapUserData(data ?? {});
    Object.assign(userForm, mapped);
    initialUser.value = structuredCloneIfPossible(mapped);
  } finally {
    loadingUser.value = false;
  }
};

const mapSettingsData = (data) => ({
  ...emptySettings(),
  ...data,
  userId: auth.user?.id ?? data?.userId ?? null,
});

const saveProfile = async () => {
  if (!auth.user?.id) {
    return;
  }
  savingUser.value = true;
  try {
    const payload = { ...userForm, userId: auth.user.id };
    const { data } = await updateUser(payload)
    const mapped = mapSettingsData(data ?? payload);
    notifications.push({
      type: 'success',
      title: t('settings.preferences.toast.title'),
      message: t('settings.preferences.toast.message'),
    });
  } finally {
    savingUser.value = false;
  }
};

onMounted(async () => {
  await Promise.all([loadUser()]);
});
</script>