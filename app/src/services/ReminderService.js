import api from '@/plugins/axios';

export default {
  list(params) {
    return api.get('/api/v1/reminders', { params });
  },
  get(id) {
    return api.get(`/api/v1/reminders/${id}`);
  },
  create(data) {
    return api.post('/api/v1/reminders', data);
  },
  update(id, data) {
    return api.put(`/api/v1/reminders/${id}`, data);
  },
  remove(id) {
    return api.delete(`/api/v1/reminders/${id}`);
  },
  toggleActive(id, active) {
    return api.patch(`/api/v1/reminders/${id}/active`, null, { params: { active } });
  },
  toggleCompleted(id, completed) {
    return api.patch(`/api/v1/reminders/${id}/completed`, null, { params: { completed } });
  },
  listTargets(params) {
    return api.get('/api/v1/reminders/targets', { params });
  },
};
