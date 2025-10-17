import api from './api';

export const getExercises = (params = {}) => api.get('/api/v1/exercises', { params });
export const getExercise = (id) => api.get(`/api/v1/exercises/${id}`);
export const createExercise = (payload) => api.post('/api/v1/exercises', payload);
export const updateExercise = (payload) => api.put('/api/v1/exercises', payload);
export const deleteExercise = (id) => api.delete(`/api/v1/exercises/${id}`);
