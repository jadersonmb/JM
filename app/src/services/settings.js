import api from './api';

export const getUserSettings = (userId) => api.get(`/api/v1/users/${userId}/settings`);
export const updateUserSettings = (userId, payload) =>
  api.put(`/api/v1/users/${userId}/settings`, payload);
