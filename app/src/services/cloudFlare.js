import api from './api';

export const uploadFile = async ({ file, userId }) => {
  const form = new FormData();
  form.append('file', file);
  if (userId) form.append('userId', userId);
  return api.post('/api/v1/cloudflare/storage/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const downloadFile = (payload) => api.post(`/api/v1/cloudflare/storage/download/${id}/${filename}`, payload);
