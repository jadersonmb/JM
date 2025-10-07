import { defineStore } from 'pinia';
import i18n, { DEFAULT_LOCALE, SUPPORTED_LOCALES, persistLocale } from '@/plugins/i18n';

const STORAGE_KEY = 'jm_locale';

export const usePreferencesStore = defineStore('preferences', {
  state: () => ({
    locale: DEFAULT_LOCALE,
  }),
  actions: {
    initialize() {
      if (typeof window === 'undefined') {
        return;
      }
      const saved = window.localStorage.getItem(STORAGE_KEY);
      if (saved && SUPPORTED_LOCALES.includes(saved)) {
        this.locale = saved;
        i18n.global.locale.value = saved;
      } else {
        this.locale = i18n.global.locale.value;
      }
    },
    setLocale(locale) {
      if (!SUPPORTED_LOCALES.includes(locale)) {
        return;
      }
      this.locale = locale;
      i18n.global.locale.value = locale;
      persistLocale(locale);
    },
  },
});
