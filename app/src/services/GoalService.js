import api from '@/plugins/axios';

export default {
  list(params) {
    return api.get('/api/v1/goals', { params });
  },
  get(id) {
    return api.get(`/api/v1/goals/${id}`);
  },
  create(data) {
    return api.post('/api/v1/goals', data);
  },
  update(data) {
    return api.put('/api/v1/goals', data);
  },
  remove(id) {
    return api.delete(`/api/v1/goals/${id}`);
  },
  listTemplates(params) {
    return api.get('/api/v1/goal-templates', { params });
  },
  listUnits() {
    return api.get('/api/v1/units');
  },
  listOwners(params) {
    return api.get('/api/v1/goals/owners', { params });
  },
};
