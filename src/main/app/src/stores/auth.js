import { defineStore } from 'pinia';
import { login, fetchProfile, updateProfile as updateProfileApi } from '@/services/auth';
import { useNotificationStore } from '@/stores/notifications';

const STORAGE_KEY = 'jm_auth_token';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(STORAGE_KEY) || '',
    user: null,
    loading: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.user),
    initials: (state) => {
      if (!state.user?.name) return '';
      return state.user.name
        .split(' ')
        .map((part) => part[0])
        .join('')
        .substring(0, 2)
        .toUpperCase();
    },
  },
  actions: {
    async login(credentials) {
      this.loading = true;
      try {
        const notification = useNotificationStore();
        const { data } = await login(credentials);
        if (data?.token) {
          this.token = data.token;
          localStorage.setItem(STORAGE_KEY, data.token);
        }
        if (data?.user) {
          this.user = data.user;
        } else {
          await this.loadProfile();
        }
        notification.push({ type: 'success', title: 'Welcome', message: 'Login successful.' });
        return true;
      } catch (error) {
        this.reset();
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async loadProfile() {      const { data } = await fetchProfile();      this.user = data;      return data;    },
    logout() {      this.reset();    },    async updateProfile(payload) {      if (!this.user) return;      const formData = payload instanceof FormData ? payload : new FormData();      if (!(payload instanceof FormData)) {        Object.entries(payload).forEach(([key, value]) => {          if (value !== undefined && value !== null) {            formData.append(key, value);          }        });      }      const { data } = await updateProfileApi(formData);      this.user = data ?? this.user;      return this.user;    },
    reset() {
      this.token = '';
      this.user = null;
      localStorage.removeItem(STORAGE_KEY);
    },
  },
});