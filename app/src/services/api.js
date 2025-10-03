import axios from 'axios';
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

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const notification = useNotificationStore();
    if (error.response) {
      const { status, data } = error.response;
      const message = data?.message || 'An unexpected error occurred.';

      if (status === 401) {
        notification.push({ type: 'warning', title: 'Session expired', message });
        const auth = useAuthStore();
        auth.reset();
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
      } else {
        notification.push({ type: 'error', title: 'Request failed', message });
      }
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