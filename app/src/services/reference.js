import api from './api';

export const getCountries = (params = {}) => api.get('/api/v1/reference/countries', { params });

export const getCities = (countryId, params = {}) =>
  api.get(`/api/v1/reference/countries/${countryId}/cities`, { params });

export const getEducationLevels = (params = {}) =>
  api.get('/api/v1/reference/education-levels', { params });

export const getProfessions = (params = {}) =>
  api.get('/api/v1/reference/professions', { params });

export const getMeasurementUnits = (params = {}) =>
  api.get('/api/v1/reference/measurement-units', { params });

export const getFoodCategories = (params = {}) =>
  api.get('/api/v1/reference/food-categories', { params });

export const getFoods = (params = {}) => api.get('/api/v1/reference/foods', { params });

export const getMeals = (params = {}) => api.get('/api/v1/reference/meals', { params });

export const getPathologies = (params = {}) =>
  api.get('/api/v1/reference/pathologies', { params });

export const getBiochemicalExams = (params = {}) =>
  api.get('/api/v1/reference/biochemical-exams', { params });
