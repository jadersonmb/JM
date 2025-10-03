import api from './api';

export const login = (payload) => api.post('/api/auth/login', payload);
export const recoverPassword = (payload) => api.post('/api/auth/recoverPassword', payload);