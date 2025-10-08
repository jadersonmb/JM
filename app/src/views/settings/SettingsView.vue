<template>
  <div class="space-y-8">
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
            <input
              id="first-name"
              v-model="userForm.name"
              type="text"
              class="input"
              :placeholder="t('settings.profile.fields.firstName.placeholder')"
              autocomplete="given-name"
              required
              :disabled="loadingUser || savingUser"
            />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="last-name">
              {{ t('settings.profile.fields.lastName.label') }}
            </label>
            <input
              id="last-name"
              v-model="userForm.lastName"
              type="text"
              class="input"
              :placeholder="t('settings.profile.fields.lastName.placeholder')"
              autocomplete="family-name"
              :disabled="loadingUser || savingUser"
            />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="email">
              {{ t('settings.profile.fields.email.label') }}
            </label>
            <input
              id="email"
              v-model="userForm.email"
              type="email"
              class="input"
              autocomplete="email"
              disabled
            />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="phone">
              {{ t('settings.profile.fields.phone.label') }}
            </label>
            <input
              id="phone"
              v-model="userForm.phoneNumber"
              type="tel"
              class="input"
              :placeholder="t('settings.profile.fields.phone.placeholder')"
              autocomplete="tel"
              :disabled="loadingUser || savingUser"
            />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="document">
              {{ t('settings.profile.fields.document.label') }}
            </label>
            <input
              id="document"
              v-model="userForm.documentNumber"
              type="text"
              class="input"
              :placeholder="t('settings.profile.fields.document.placeholder')"
              autocomplete="off"
              :disabled="loadingUser || savingUser"
            />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="avatar">
              {{ t('settings.profile.fields.avatar.label') }}
            </label>
            <input
              id="avatar"
              v-model="userForm.avatarUrl"
              type="url"
              class="input"
              :placeholder="t('settings.profile.fields.avatar.placeholder')"
              autocomplete="off"
              :disabled="loadingUser || savingUser"
            />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-3">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="city">
              {{ t('settings.profile.fields.city.label') }}
            </label>
            <input
              id="city"
              v-model="userForm.city"
              type="text"
              class="input"
              :placeholder="t('settings.profile.fields.city.placeholder')"
              autocomplete="address-level2"
              :disabled="loadingUser || savingUser"
            />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="state">
              {{ t('settings.profile.fields.state.label') }}
            </label>
            <input
              id="state"
              v-model="userForm.state"
              type="text"
              class="input"
              :placeholder="t('settings.profile.fields.state.placeholder')"
              autocomplete="address-level1"
              :disabled="loadingUser || savingUser"
            />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="postal-code">
              {{ t('settings.profile.fields.postalCode.label') }}
            </label>
            <input
              id="postal-code"
              v-model="userForm.postalCode"
              type="text"
              class="input"
              :placeholder="t('settings.profile.fields.postalCode.placeholder')"
              autocomplete="postal-code"
              :disabled="loadingUser || savingUser"
            />
          </div>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="street">
              {{ t('settings.profile.fields.street.label') }}
            </label>
            <input
              id="street"
              v-model="userForm.street"
              type="text"
              class="input"
              :placeholder="t('settings.profile.fields.street.placeholder')"
              autocomplete="street-address"
              :disabled="loadingUser || savingUser"
            />
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="country">
              {{ t('settings.profile.fields.country.label') }}
            </label>
            <input
              id="country"
              v-model="userForm.country"
              type="text"
              class="input"
              :placeholder="t('settings.profile.fields.country.placeholder')"
              autocomplete="country-name"
              :disabled="loadingUser || savingUser"
            />
          </div>
        </div>

        <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
          <button
            type="button"
            class="btn-secondary"
            @click="resetProfile"
            :disabled="savingUser || loadingUser || !isUserDirty"
          >
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

    <section class="card space-y-6">
      <header>
        <h2 class="text-xl font-semibold text-slate-900">{{ t('settings.sections.preferences.title') }}</h2>
        <p class="mt-1 text-sm text-slate-500">{{ t('settings.sections.preferences.subtitle') }}</p>
      </header>

      <form class="space-y-6" @submit.prevent="saveSettings">
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="language">
              {{ t('settings.preferences.fields.language.label') }}
            </label>
            <select
              id="language"
              v-model="settings.language"
              class="input"
              :disabled="loadingSettings || savingSettings"
            >
              <option v-for="option in languageOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700" for="timezone">
              {{ t('settings.preferences.fields.timezone.label') }}
            </label>
            <select
              id="timezone"
              v-model="settings.timezone"
              class="input"
              :disabled="loadingSettings || savingSettings"
            >
              <option v-for="option in timezoneOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="space-y-4">
          <p class="text-sm font-semibold text-slate-700">
            {{ t('settings.preferences.sections.notifications') }}
          </p>
          <div
            v-for="option in toggleOptions"
            :key="option.key"
            class="flex items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
          >
            <div class="flex items-start gap-3">
              <component :is="option.icon" class="h-6 w-6 text-primary-600" aria-hidden="true" />
              <div>
                <p class="text-sm font-semibold text-slate-700">{{ option.label }}</p>
                <p class="text-xs text-slate-500">{{ option.description }}</p>
              </div>
            </div>
            <button
              type="button"
              class="relative inline-flex h-6 w-11 items-center rounded-full border border-transparent transition"
              :class="settings[option.key] ? 'bg-primary-600' : 'bg-slate-200'"
              role="switch"
              :aria-checked="settings[option.key]"
              :disabled="loadingSettings || savingSettings"
              @click="toggleSetting(option.key)"
            >
              <span
                class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition"
                :class="settings[option.key] ? 'translate-x-5' : 'translate-x-1'"
              />
            </button>
          </div>
        </div>

        <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
          <button
            type="button"
            class="btn-secondary"
            @click="resetSettings"
            :disabled="savingSettings || loadingSettings || !isSettingsDirty"
          >
            {{ t('settings.common.actions.revert') }}
          </button>
          <button type="submit" class="btn-primary" :disabled="savingSettings || !isSettingsDirty">
            <span v-if="savingSettings" class="flex items-center gap-2">
              <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
              {{ t('settings.common.actions.saving') }}
            </span>
            <span v-else>{{ t('settings.preferences.actions.save') }}</span>
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
import { getUserSettings, updateUserSettings } from '@/services/settings';
import {
  MoonIcon,
  EnvelopeIcon,
  ChatBubbleLeftRightIcon,
  BellAlertIcon,
  ShieldCheckIcon,
  MegaphoneIcon,
} from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';

const auth = useAuthStore();
const notifications = useNotificationStore();
const { t } = useI18n();

const loadingUser = ref(false);
const savingUser = ref(false);
const loadingSettings = ref(false);
const savingSettings = ref(false);

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

const emptySettings = () => ({
  id: null,
  userId: auth.user?.id ?? null,
  darkMode: false,
  emailNotifications: true,
  whatsappNotifications: false,
  pushNotifications: false,
  securityAlerts: true,
  productUpdates: true,
  language: 'pt-BR',
  timezone: 'America/Sao_Paulo',
});

const userForm = reactive(emptyUser());
const settings = reactive(emptySettings());

const initialUser = ref(structuredCloneIfPossible(emptyUser()));
const initialSettings = ref(structuredCloneIfPossible(emptySettings()));

const languageOptions = computed(() => [
  { value: 'pt-BR', label: t('settings.options.languages.ptBR') },
  { value: 'en-US', label: t('settings.options.languages.enUS') },
  { value: 'es-ES', label: t('settings.options.languages.esES') },
]);

const timezoneOptions = computed(() => [
  { value: 'America/Sao_Paulo', label: t('settings.options.timezones.americaSaoPaulo') },
  { value: 'America/New_York', label: t('settings.options.timezones.americaNewYork') },
  { value: 'Europe/Lisbon', label: t('settings.options.timezones.europeLisbon') },
  { value: 'UTC', label: t('settings.options.timezones.utc') },
]);

const toggleOptions = computed(() => [
  {
    key: 'darkMode',
    label: t('settings.preferences.toggles.darkMode.label'),
    description: t('settings.preferences.toggles.darkMode.description'),
    icon: MoonIcon,
  },
  {
    key: 'emailNotifications',
    label: t('settings.preferences.toggles.emailNotifications.label'),
    description: t('settings.preferences.toggles.emailNotifications.description'),
    icon: EnvelopeIcon,
  },
  {
    key: 'whatsappNotifications',
    label: t('settings.preferences.toggles.whatsappNotifications.label'),
    description: t('settings.preferences.toggles.whatsappNotifications.description'),
    icon: ChatBubbleLeftRightIcon,
  },
  {
    key: 'pushNotifications',
    label: t('settings.preferences.toggles.pushNotifications.label'),
    description: t('settings.preferences.toggles.pushNotifications.description'),
    icon: BellAlertIcon,
  },
  {
    key: 'securityAlerts',
    label: t('settings.preferences.toggles.securityAlerts.label'),
    description: t('settings.preferences.toggles.securityAlerts.description'),
    icon: ShieldCheckIcon,
  },
  {
    key: 'productUpdates',
    label: t('settings.preferences.toggles.productUpdates.label'),
    description: t('settings.preferences.toggles.productUpdates.description'),
    icon: MegaphoneIcon,
  },
]);

const isUserDirty = computed(() =>
  JSON.stringify(userForm) !== JSON.stringify(initialUser.value ?? {})
);

const isSettingsDirty = computed(() =>
  JSON.stringify(settings) !== JSON.stringify(initialSettings.value ?? {})
);

const applyDarkMode = (enabled) => {
  if (typeof document === 'undefined') return;
  const root = document.documentElement;
  root.classList.toggle('dark', Boolean(enabled));
};

watch(
  () => settings.darkMode,
  (enabled) => applyDarkMode(enabled),
  { immediate: true }
);

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
});

const mapSettingsData = (data) => ({
  ...emptySettings(),
  ...data,
  userId: auth.user?.id ?? data?.userId ?? null,
});

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

const loadSettings = async () => {
  if (!auth.user?.id) {
    return;
  }
  loadingSettings.value = true;
  try {
    const { data } = await getUserSettings(auth.user.id);
    const mapped = mapSettingsData(data ?? {});
    Object.assign(settings, mapped);
    initialSettings.value = structuredCloneIfPossible(mapped);
  } finally {
    loadingSettings.value = false;
  }
};

const saveProfile = async () => {
  if (!auth.user?.id || !isUserDirty.value) {
    return;
  }
  savingUser.value = true;
  try {
    const payload = { ...userForm };
    const { data } = await updateUser(payload);
    const mapped = mapUserData(data ?? payload);
    Object.assign(userForm, mapped);
    initialUser.value = structuredCloneIfPossible(mapped);
    auth.user = { ...(auth.user ?? {}), ...mapped };
    if (typeof auth.persist === 'function') {
      auth.persist();
    }
    notifications.push({
      type: 'success',
      title: t('settings.profile.toast.title'),
      message: t('settings.profile.toast.message'),
    });
  } finally {
    savingUser.value = false;
  }
};

const saveSettings = async () => {
  if (!auth.user?.id || !isSettingsDirty.value) {
    return;
  }
  savingSettings.value = true;
  try {
    const payload = { ...settings, userId: auth.user.id };
    const { data } = await updateUserSettings(auth.user.id, payload);
    const mapped = mapSettingsData(data ?? payload);
    Object.assign(settings, mapped);
    initialSettings.value = structuredCloneIfPossible(mapped);
    applyDarkMode(mapped.darkMode);
    notifications.push({
      type: 'success',
      title: t('settings.preferences.toast.title'),
      message: t('settings.preferences.toast.message'),
    });
  } finally {
    savingSettings.value = false;
  }
};

const resetProfile = () => {
  if (!initialUser.value) {
    return;
  }
  Object.assign(userForm, structuredCloneIfPossible(initialUser.value));
};

const resetSettings = () => {
  if (!initialSettings.value) {
    return;
  }
  Object.assign(settings, structuredCloneIfPossible(initialSettings.value));
  applyDarkMode(settings.darkMode);
};

const toggleSetting = (key) => {
  if (loadingSettings.value || savingSettings.value) {
    return;
  }
  settings[key] = !settings[key];
};

onMounted(async () => {
  await Promise.all([loadUser(), loadSettings()]);
});
</script>
