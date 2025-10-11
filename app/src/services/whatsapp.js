import api from './api';

export const fetchWhatsAppMessages = (params) => api.get('/public/api/v1/whatsapp/messages', { params });
export const fetchNutritionDashboard = (params) => api.get('/public/api/v1/whatsapp/dashboard', { params });
export const createWhatsAppNutritionEntry = (payload) => api.post('/api/v1/whatsapp/messages', payload);
export const updateWhatsAppNutritionEntry = (id, payload) =>
  api.put(`/api/v1/whatsapp/messages/${id}`, payload);
export const deleteWhatsAppNutritionEntry = (id) => api.delete(`/api/v1/whatsapp/messages/${id}`);
