import api from './api';

export const getUsers = (params) => api.get('/api/v1/users', { params });
export const getUser = (id) => api.get(`/api/v1/users/${id}`);
export const createUser = (payload) => api.post('/api/v1/users', payload);
export const updateUser = (payload) => api.put(`/api/v1/users`, payload);
export const deleteUser = (id) => api.delete(`/api/v1/users/${id}`);
export const getUsersWithRoles = () => api.get('/api/v1/users/with-roles');
export const updateUserRoles = (id, payload) => api.put(`/api/v1/users/${id}/roles`, payload);