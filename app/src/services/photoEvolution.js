import api from '@/plugins/axios';

export default {
  list(params) {
    return api.get('/api/v1/photo-evolutions', { params });
  },
  get(id) {
    return api.get(`/api/v1/photo-evolutions/${id}`);
  },
  create(data) {
    return api.post('/api/v1/photo-evolutions', data);
  },
  update(id, data) {
    return api.put(`/api/v1/photo-evolutions/${id}`, data);
  },
  remove(id) {
    return api.delete(`/api/v1/photo-evolutions/${id}`);
  },
  listOwners(params) {
    return api.get('/api/v1/photo-evolutions/owners', { params });
  },
};
