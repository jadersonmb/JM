import api from './api';

export const fetchWhatsAppMessages = (params) => api.get('/public/api/v1/whatsapp/messages', { params });
export const fetchNutritionDashboard = () => api.get('/public/api/v1/whatsapp/dashboard');
