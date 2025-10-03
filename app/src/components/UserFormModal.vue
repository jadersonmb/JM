<template>
  <transition name="modal">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 px-4 py-10">
      <div class="w-full max-w-2xl rounded-3xl bg-white shadow-2xl">
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
              <label for="name" class="mb-1 block text-sm font-medium text-slate-700">Name</label>
              <input id="name" v-model="form.name" type="text" class="input" required />
              <p v-if="errors.name" class="mt-1 text-xs text-red-600">{{ errors.name }}</p>
            </div>
            <div>
              <label for="email" class="mb-1 block text-sm font-medium text-slate-700">Email</label>
              <input id="email" v-model="form.email" type="email" class="input" required />
              <p v-if="errors.email" class="mt-1 text-xs text-red-600">{{ errors.email }}</p>
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
              <label class="mb-1 block text-sm font-medium text-slate-700">Role</label>
              <select v-model="form.role" class="input" required>
                <option disabled value="">Select role</option>
                <option value="admin">Administrator</option>
                <option value="moderator">Moderator</option>
                <option value="user">User</option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Status</label>
              <div class="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-3 text-sm">
                <label class="inline-flex items-center gap-2 text-slate-600">
                  <input type="radio" value="active" v-model="form.status" /> Active
                </label>
                <label class="inline-flex items-center gap-2 text-slate-600">
                  <input type="radio" value="inactive" v-model="form.status" /> Inactive
                </label>
              </div>
            </div>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Avatar</label>
            <div class="flex items-center gap-4">
              <div class="h-16 w-16 overflow-hidden rounded-2xl border border-dashed border-slate-300 bg-slate-50">
                <img v-if="preview" :src="preview" alt="Avatar preview" class="h-full w-full object-cover" />
                <div v-else class="flex h-full items-center justify-center text-xs text-slate-400">Preview</div>
              </div>
              <label class="btn-secondary cursor-pointer">
                <PhotoIcon class="h-4 w-4" />
                <span>Upload</span>
                <input type="file" accept="image/*" class="hidden" @change="handleFile" />
              </label>
              <button v-if="form.avatarFile" type="button" class="text-sm font-semibold text-red-500" @click="removeFile">
                Remove
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
import { computed, reactive, watch, ref } from 'vue';
import { PhotoIcon, XMarkIcon } from '@heroicons/vue/24/outline';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  user: { type: Object, default: null },
  submitting: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue', 'submit']);

const form = reactive({
  name: '',
  email: '',
  password: '',
  passwordConfirmation: '',
  role: 'user',
  status: 'active',
  avatarFile: null,
});

const preview = ref('');
const errors = reactive({ name: '', email: '', password: '' });

const isEdit = computed(() => Boolean(props.user?.id));

watch(
  () => props.user,
  (user) => {
    if (!user) {
      resetForm();
      return;
    }
    form.name = user.name ?? '';
    form.email = user.email ?? '';
    form.role = user.role ?? 'user';
    form.status = user.status ?? 'active';
    preview.value = user.avatarUrl ?? '';
    form.password = '';
    form.passwordConfirmation = '';
    form.avatarFile = null;
  },
  { immediate: true }
);

const resetErrors = () => {
  errors.name = '';
  errors.email = '';
  errors.password = '';
};

const resetForm = () => {
  form.name = '';
  form.email = '';
  form.role = 'user';
  form.status = 'active';
  form.password = '';
  form.passwordConfirmation = '';
  form.avatarFile = null;
  preview.value = '';
  resetErrors();
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
  preview.value = props.user?.avatarUrl ?? '';
};

const handleSubmit = () => {
  resetErrors();
  if (form.password && form.password !== form.passwordConfirmation) {
    errors.password = 'Passwords do not match.';
    return;
  }
  const payload = {
    name: form.name,
    email: form.email,
    role: form.role,
    status: form.status,
    password: form.password,
    avatarFile: form.avatarFile,
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