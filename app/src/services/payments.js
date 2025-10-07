import api from './api';

export function createPaymentIntent(payload) {
  return api.post('/v1/api/payments/create-intent', payload);
}

export function confirmPayment(payload) {
  return api.post('/v1/api/payments/confirm', payload);
}

export function createPixPayment(payload) {
  return api.post('/v1/api/payments/pix', payload);
}

export function createSubscription(payload) {
  return api.post('/v1/api/payments/subscription', payload);
}

export function getPayment(id) {
  return api.get(`/v1/api/payments/${id}`);
}

export function refundPayment(id, payload) {
  return api.post(`/v1/api/payments/${id}/refund`, payload);
}

export function listPayments(params) {
  return api.get('/v1/api/payments', { params });
}

export function listSubscriptions(params) {
  return api.get('/v1/api/payments/subscription', { params });
}

export function cancelSubscription(id) {
  return api.delete(`/v1/api/payments/subscription/${id}`);
}

export function addCard(payload) {
  return api.post('/v1/api/payment-methods/card', payload);
}

export function listCards(customerId) {
  return api.get('/v1/api/payment-methods', { params: { customerId } });
}

export function deleteCard(id, customerId) {
  return api.delete(`/v1/api/payment-methods/${id}`, { params: { customerId } });
}

export function sendWebhook(payload) {
  return api.post('/v1/api/webhooks/payment', payload);
}

export function listPaymentPlans() {
  return api.get('/v1/api/payment-plans');
}
