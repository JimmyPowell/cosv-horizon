import apiClient from '@/api/index';

export default {
  list({ q, page = 1, size = 20, withTotal = true } = {}) {
    const sp = new URLSearchParams();
    if (q) sp.set('q', q);
    sp.set('page', page);
    sp.set('size', size);
    sp.set('withTotal', withTotal);
    return apiClient.get(`/categories?${sp.toString()}`).then(r => {
      const d = r.data?.data || {};
      return { items: d.items || [], total: d.total ?? 0, page: d.page, size: d.size };
    });
  },
  create({ code, name, description }) {
    return apiClient.post('/categories', { code, name, description }).then(r => r.data?.data?.category);
  },
  update(uuid, { code, name, description }) {
    return apiClient.put(`/categories/${uuid}`, { code, name, description }).then(r => r.data?.data?.category);
  },
  delete(uuid) {
    return apiClient.delete(`/categories/${uuid}`).then(r => r.data);
  },
  forceDelete(uuid, { dryRun = false } = {}) {
    return apiClient.post(`/categories/${uuid}/actions/force-delete`, { dryRun }).then(r => r.data);
  },
  usage(uuid) {
    return apiClient.get(`/categories/${uuid}/usage`).then(r => r.data?.data);
  },
  remap(uuid, { targetUuid, dryRun = false }) {
    return apiClient.post(`/categories/${uuid}/actions/remap`, { targetUuid, dryRun }).then(r => r.data);
  }
};

