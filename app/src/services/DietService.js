import api from '@/plugins/axios';

export default {
  list(params) {
    return api.get('/api/v1/diets', { params });
  },
  get(id) {
    return api.get(`/api/v1/diets/${id}`);
  },
  create(data) {
    return api.post('/api/v1/diets', data);
  },
  update(data) {
    return api.put('/api/v1/diets', data);
  },
  remove(id) {
    return api.delete(`/api/v1/diets/${id}`);
  },
  listUnits(params) {
    return api.get('/api/v1/units', { params });
  },
  listFoods(params) {
    return api.get('/api/v1/foods', { params });
  },
  listOwners(params) {
    return api.get('/api/v1/diets/owners', { params });
  },
};
