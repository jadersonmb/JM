import axios from 'axios';
import i18n from '@/plugins/i18n';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
  withCredentials: true,
  /*timeout: 10000, */
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  },
});

api.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

let isRefreshing = false;
const failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue.length = 0;
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const notification = useNotificationStore();
    const auth = useAuthStore();
    const { response, config } = error;

    if (response?.status === 401 && !config._retry) {
      if (auth.refreshToken) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({
              resolve: (token) => {
                config.headers.Authorization = `Bearer ${token}`;
                resolve(api(config));
              },
              reject,
            });
          });
        }

        config._retry = true;
        isRefreshing = true;
        try {
          const { accessToken } = await auth.refresh();
          processQueue(null, accessToken);
          config.headers.Authorization = `Bearer ${accessToken}`;
          return api(config);
        } catch (refreshError) {
          processQueue(refreshError, null);
          auth.reset();
          notification.push({
            type: 'warning',
            title: i18n.global.t('auth.expired'),
            message: i18n.global.t('auth.invalid'),
          });
          if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login';
          }
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      } else {
        notification.push({
          type: 'warning',
          title: i18n.global.t('auth.expired'),
          message: i18n.global.t('auth.invalid'),
        });
        auth.reset();
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    }

    if (response) {
      const message = response.data?.message || response.data?.details || i18n.global.t('common.error');
      notification.push({ type: 'error', title: 'Request failed', message });
    } else {
      notification.push({
        type: 'error',
        title: 'Network error',
        message: 'Unable to reach the server. Check your connection.',
      });
    }
    return Promise.reject(error);
  },
);

export default api;