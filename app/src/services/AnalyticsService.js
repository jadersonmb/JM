import api from './api';

export default {
  getGoalsAdherence(params) {
    return api.get('/api/v1/analytics/goals/adherence', { params });
  },
  getMacros(params) {
    return api.get('/api/v1/analytics/macros/distribution', { params });
  },
  getHydration(params) {
    return api.get('/api/v1/analytics/hydration', { params });
  },
  getBodyComposition(params) {
    return api.get('/api/v1/analytics/body/biometrics', { params });
  },
  getTopFoods(params) {
    return api.get('/api/v1/analytics/foods/top', { params });
  },
};
