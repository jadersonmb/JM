import api from '@/plugins/axios';

export default {
  listAll(params) {
    return api.get('/api/v1/anamnese', { params });
  },
  getById(id) {
    return api.get(`/api/v1/anamnese/${id}`);
  },
  create(data) {
    return api.post('/api/v1/anamnese', data);
  },
  update(data) {
    return api.put('/api/v1/anamnese', data);
  },
  remove(id) {
    return api.delete(`/api/v1/anamnese/${id}`);
  },
};
