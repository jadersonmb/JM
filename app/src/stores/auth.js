import { defineStore } from 'pinia';
import {
  login as loginRequest,
  refreshToken as refreshTokenRequest,
  recoverPassword as recoverPasswordRequest,
  fetchProfile,
} from '@/services/auth';

const STORAGE_KEY = 'jm_auth_session';

function storage() {
  return window.localStorage;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    refreshToken: '',
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
    roles: (state) => {
      if (!Array.isArray(state.user?.roles)) return [];
      return state.user.roles
        .map((role) => (role?.name ? role.name.toUpperCase() : null))
        .filter(Boolean);
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
        this.refreshToken = parsed.refreshToken ?? '';
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
        JSON.stringify({
          token: this.token,
          refreshToken: this.refreshToken,
          user: this.user,
          expiresIn: this.expiresIn,
        })
      );
    },
    async login(credentials) {
      this.loading = true;
      try {
        const { data } = await loginRequest(credentials);
        this.token = data.accessToken;
        this.refreshToken = data.refreshToken;
        this.user = data.user ?? null;
        this.expiresIn = data.expiresIn ?? 0;
        this.persist();
        if (!this.user) {
          await this.loadProfile();
        }
        return data;
      } finally {
        this.loading = false;
      }
    },
    async refresh() {
      if (!this.refreshToken) {
        throw new Error('No refresh token available');
      }
      const { data } = await refreshTokenRequest({ refreshToken: this.refreshToken });
      this.token = data.accessToken;
      this.refreshToken = data.refreshToken;
      this.expiresIn = data.expiresIn ?? 0;
      if (data.user) {
        this.user = data.user;
      }
      this.persist();
      return data;
    },
    async loadProfile() {
      if (!this.token) return null;
      const { data } = await fetchProfile();
      this.user = data;
      this.persist();
      return data;
    },
    setUser(user) {
      this.user = user;
      this.persist();
    },
    logout() {
      this.token = '';
      this.refreshToken = '';
      this.user = null;
      this.expiresIn = 0;
      storage().removeItem(STORAGE_KEY);
    },
    reset() {
      this.logout();
    },
    async recoverPassword(payload) {
      const { data } = await recoverPasswordRequest(payload);
      return data;
    },
  },
});