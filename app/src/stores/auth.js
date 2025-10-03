import { defineStore } from 'pinia';
import { login as loginRequest, recoverPassword } from '@/services/auth';

const STORAGE_KEY = 'jm_auth_token';

function storage() {
  return window.sessionStorage;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null,
    expiresIn: 0,
    isHydrated: false,
    loading: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
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
    hydrate() {
      const raw = storage().getItem(STORAGE_KEY);
      if (!raw) {
        this.isHydrated = true;
        return;
      }
      try {
        const parsed = JSON.parse(raw);
        this.token = parsed.token ?? '';
        this.user = parsed.user ?? null;
        this.expiresIn = parsed.expiresIn ?? 0;
      } catch (error) {
        storage().removeItem(STORAGE_KEY);
      }
      this.isHydrated = true;
    },
    persist() {
      storage().setItem(
        STORAGE_KEY,
        JSON.stringify({ token: this.token, user: this.user, expiresIn: this.expiresIn })
      );
    },
    async login(credentials) {
      this.loading = true;
      try {
        const { data } = await loginRequest(credentials);
        this.token = data.access_token;
        this.user = data.user;
        this.expiresIn = data.expires_in;
        this.persist();
        return data;
      } finally {
        this.loading = false;
      }
    },
    logout() {
      this.token = '';
      this.user = null;
      this.expiresIn = 0;
      storage().removeItem(STORAGE_KEY);
    },
    async recoverPassword(payload) {
      const { data } = await recoverPassword(payload);
      return data;
    }  },
});