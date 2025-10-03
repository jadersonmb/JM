<template>
  <div class="space-y-8">
    <section class="card space-y-6">
      <header>
        <h2 class="text-xl font-semibold text-slate-900">Workspace preferences</h2>
        <p class="mt-1 text-sm text-slate-500">Configure default behaviours for the admin console.</p>
      </header>

      <form class="space-y-6" @submit.prevent="savePreferences">
        <div class="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-sm font-semibold text-slate-700">Dark mode</p>
            <p class="text-xs text-slate-500">Enable automatic switch based on user preference.</p>
          </div>
          <label class="inline-flex items-center gap-3">
            <input v-model="preferences.darkMode" type="checkbox" class="h-5 w-5 rounded border-slate-300 text-primary-600 focus:ring-primary-500" />
            <span class="text-sm font-semibold text-slate-600">Enable</span>
          </label>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700">Default user role</label>
            <select v-model="preferences.defaultRole" class="input" required>
              <option value="user">User</option>
              <option value="moderator">Moderator</option>
              <option value="admin">Administrator</option>
            </select>
          </div>
          <div>
            <label class="mb-1 block text-sm font-semibold text-slate-700">Session timeout (minutes)</label>
            <input v-model.number="preferences.sessionTimeout" type="number" min="15" step="5" class="input" />
          </div>
        </div>

        <div>
          <label class="mb-1 block text-sm font-semibold text-slate-700">Allowed domains</label>
          <textarea v-model="preferences.allowedDomains" rows="3" class="input" placeholder="acme.com, example.org"></textarea>
          <p class="mt-1 text-xs text-slate-400">Separate domains with commas to restrict self-registration.</p>
        </div>

        <div class="flex items-center justify-end gap-3 border-t border-slate-200 pt-5">
          <button type="button" class="btn-secondary" @click="reset">Reset</button>
          <button type="submit" class="btn-primary" :disabled="saving">
            <span v-if="saving" class="flex items-center gap-2">
              <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600" />
              Saving...
            </span>
            <span v-else>Save changes</span>
          </button>
        </div>
      </form>
    </section>

    <section class="card space-y-6">
      <header>
        <h2 class="text-lg font-semibold text-slate-900">API endpoints</h2>
        <p class="mt-1 text-sm text-slate-500">Current configuration targeting the Java backend.</p>
      </header>
      <dl class="grid gap-4 md:grid-cols-2">
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Auth login</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-700">POST /api/auth/login</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Users listing</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-700">GET /api/users</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Create user</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-700">POST /api/users</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Update user</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-700">PUT /api/users/{id}</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Delete user</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-700">DELETE /api/users/{id}</dd>
        </div>
        <div>
          <dt class="text-xs uppercase tracking-wide text-slate-400">Profile</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-700">GET /api/users/profile</dd>
        </div>
      </dl>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useNotificationStore } from '@/stores/notifications';

const notifications = useNotificationStore();

const initialState = {
  darkMode: false,
  defaultRole: 'user',
  sessionTimeout: 30,
  allowedDomains: 'acme.com',
};

const preferences = reactive({ ...initialState });
const saving = ref(false);

const reset = () => {
  Object.assign(preferences, initialState);
};

const savePreferences = async () => {
  saving.value = true;
  setTimeout(() => {
    notifications.push({ type: 'success', title: 'Preferences saved', message: 'Settings persisted locally. Connect API to store permanently.' });
    saving.value = false;
  }, 600);
};
</script>