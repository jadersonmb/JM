import api from '@/plugins/axios';

export default {
  list(params) {
    return api.get('/api/v1/photo-evolutions', { params });
  },
  get(id) {
    return api.get(`/api/v1/photo-evolutions/${id}`);
  },
  create(formData) {
    return api.post('/api/v1/photo-evolutions', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  },
  update(id, formData) {
    return api.put(`/api/v1/photo-evolutions/${id}`, formData);
  },
  remove(id) {
    return api.delete(`/api/v1/photo-evolutions/${id}`);
  },
  listOwners(params) {
    return api.get('/api/v1/photo-evolutions/owners', { params });
  },
  prefill(params) {
    return api.get('/api/v1/photo-evolutions/prefill', { params });
  },
};
