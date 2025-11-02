import apiClient from './index';

export default {
  list({ q, page = 1, size = 50, withTotal = false } = {}) {
    const params = { page, size, withTotal };
    if (q && q.trim()) params.q = q.trim();
    return apiClient.get('/tags', { params });
  },
};

