import api from './api';

export const getRoles = () => api.get('/api/v1/roles');
export const createRole = (payload) => api.post('/api/v1/roles', payload);
export const updateRole = (id, payload) => api.put(`/api/v1/roles/${id}`, payload);
export const deleteRole = (id) => api.delete(`/api/v1/roles/${id}`);
