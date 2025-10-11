import api from '@/plugins/axios';

export default {
  listAll(params) {
    return api.get('/api/v1/anamnesis', { params });
  },
  getById(id) {
    return api.get(`/api/v1/anamnesis/${id}`);
  },
  create(data) {
    return api.post('/api/v1/anamnesis', data);
  },
  update(data) {
    return api.put('/api/v1/anamnesis', data);
  },
  remove(id) {
    return api.delete(`/api/v1/anamnesis/${id}`);
  },
};
