import api from './api';

export const login = (credentials) => api.post('/api/auth/login', credentials);
export const fetchProfile = () => api.get('/api/users/profile');
export const updateProfile = (payload) => api.put('/api/users/profile', payload);