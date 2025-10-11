import api from './api';

export const listCountries = (params = {}) => api.get('/api/v1/reference-management/countries', { params });
export const createCountry = (payload) => api.post('/api/v1/reference-management/countries', payload);
export const updateCountry = (id, payload) => api.put(`/api/v1/reference-management/countries/${id}`, payload);
export const deleteCountry = (id) => api.delete(`/api/v1/reference-management/countries/${id}`);

export const listCities = (params = {}) => api.get('/api/v1/reference-management/cities', { params });
export const createCity = (payload) => api.post('/api/v1/reference-management/cities', payload);
export const updateCity = (id, payload) => api.put(`/api/v1/reference-management/cities/${id}`, payload);
export const deleteCity = (id) => api.delete(`/api/v1/reference-management/cities/${id}`);

export const listEducationLevels = (params = {}) =>
  api.get('/api/v1/reference-management/education-levels', { params });
export const createEducationLevel = (payload) =>
  api.post('/api/v1/reference-management/education-levels', payload);
export const updateEducationLevel = (id, payload) =>
  api.put(`/api/v1/reference-management/education-levels/${id}`, payload);
export const deleteEducationLevel = (id) =>
  api.delete(`/api/v1/reference-management/education-levels/${id}`);

export const listProfessions = (params = {}) => api.get('/api/v1/reference-management/professions', { params });
export const createProfession = (payload) => api.post('/api/v1/reference-management/professions', payload);
export const updateProfession = (id, payload) => api.put(`/api/v1/reference-management/professions/${id}`, payload);
export const deleteProfession = (id) => api.delete(`/api/v1/reference-management/professions/${id}`);

export const listMeals = (params = {}) => api.get('/api/v1/reference-management/meals', { params });
export const createMeal = (payload) => api.post('/api/v1/reference-management/meals', payload);
export const updateMeal = (id, payload) => api.put(`/api/v1/reference-management/meals/${id}`, payload);
export const deleteMeal = (id) => api.delete(`/api/v1/reference-management/meals/${id}`);
