import api from './api';

export const listActions = () => api.get('/api/v1/actions');
export const createAction = (payload) => api.post('/api/v1/actions', payload);
export const updateAction = (id, payload) => api.put(`/api/v1/actions/${id}`, payload);
export const deleteAction = (id) => api.delete(`/api/v1/actions/${id}`);

export const listObjects = () => api.get('/api/v1/objects');
export const createObject = (payload) => api.post('/api/v1/objects', payload);
export const updateObject = (id, payload) => api.put(`/api/v1/objects/${id}`, payload);
export const deleteObject = (id) => api.delete(`/api/v1/objects/${id}`);

export const listPermissions = () => api.get('/api/v1/permissions');
export const createPermission = (payload) => api.post('/api/v1/permissions', payload);
export const updatePermission = (id, payload) => api.put(`/api/v1/permissions/${id}`, payload);
export const deletePermission = (id) => api.delete(`/api/v1/permissions/${id}`);
