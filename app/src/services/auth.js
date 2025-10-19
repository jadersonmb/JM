import api from './api';

export const login = (payload) => api.post('/api/v1/auth/login', payload);
export const refreshToken = (payload) => api.post('/api/v1/auth/refresh', payload);
export const recoverPassword = (payload) => api.post('/api/v1/auth/recover-password', payload);
export const fetchProfile = () => api.get('/api/v1/auth/me');