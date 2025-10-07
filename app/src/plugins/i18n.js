import { createI18n } from 'vue-i18n';
import messages, { DEFAULT_LOCALE, SUPPORTED_LOCALES } from '@/locales';

const STORAGE_KEY = 'jm_locale';

const getInitialLocale = () => {
  if (typeof window === 'undefined') {
    return DEFAULT_LOCALE;
  }
  const saved = window.localStorage.getItem(STORAGE_KEY);
  if (saved && SUPPORTED_LOCALES.includes(saved)) {
    return saved;
  }
  return DEFAULT_LOCALE;
};

const i18n = createI18n({
  legacy: false,
  locale: getInitialLocale(),
  fallbackLocale: DEFAULT_LOCALE,
  messages,
});

export const persistLocale = (locale) => {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(STORAGE_KEY, locale);
  }
};

export { SUPPORTED_LOCALES, DEFAULT_LOCALE };
export default i18n;
