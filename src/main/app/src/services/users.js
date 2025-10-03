import api from './api';

export const getUsers = (params) => api.get('/api/v1/users', { params });
export const createUser = (payload) => api.post('/api/v1/users', payload);
export const updateUser = (id, payload) => api.put(`/api/v1/users${id}`, payload);
export const deleteUser = (id) => api.delete(`/api/v1/users${id}`);