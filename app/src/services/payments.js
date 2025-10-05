import api from './api';

export function createPaymentIntent(payload) {
  return api.post('/api/payments/create-intent', payload);
}

export function confirmPayment(payload) {
  return api.post('/api/payments/confirm', payload);
}

export function createPixPayment(payload) {
  return api.post('/api/payments/pix', payload);
}

export function createSubscription(payload) {
  return api.post('/api/payments/subscription', payload);
}

export function getPayment(id) {
  return api.get(`/api/payments/${id}`);
}

export function refundPayment(id, payload) {
  return api.post(`/api/payments/${id}/refund`, payload);
}

export function listPayments(params) {
  return api.get('/api/payments', { params });
}

export function addCard(payload) {
  return api.post('/api/payment-methods/card', payload);
}

export function listCards(customerId) {
  return api.get('/api/payment-methods', { params: { customerId } });
}

export function deleteCard(id, customerId) {
  return api.delete(`/api/payment-methods/${id}`, { params: { customerId } });
}

export function sendWebhook(payload) {
  return api.post('/api/webhooks/payment', payload);
}
