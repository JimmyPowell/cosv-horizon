import apiClient from '@/api/index';

export default {
  list({ q, page = 1, size = 20, withTotal = true } = {}) {
    const sp = new URLSearchParams();
    if (q) sp.set('q', q);
    sp.set('page', page);
    sp.set('size', size);
    sp.set('withTotal', withTotal);
    return apiClient.get(`/tags?${sp.toString()}`).then(r => {
      const d = r.data?.data || {};
      return { items: d.items || [], total: d.total ?? 0, page: d.page, size: d.size };
    });
  },
  create({ code, name }) {
    return apiClient.post('/tags', { code, name }).then(r => r.data?.data?.tag);
  },
  update(uuid, { code, name }) {
    return apiClient.put(`/tags/${uuid}`, { code, name }).then(r => r.data?.data?.tag);
  },
  delete(uuid) {
    return apiClient.delete(`/tags/${uuid}`).then(r => r.data);
  },
  forceDelete(uuid, { dryRun = false } = {}) {
    return apiClient.post(`/tags/${uuid}/actions/force-delete`, { dryRun }).then(r => r.data);
  },
  usage(uuid) {
    return apiClient.get(`/tags/${uuid}/usage`).then(r => r.data?.data);
  },
  remap(uuid, { targetUuid, dryRun = false }) {
    return apiClient.post(`/tags/${uuid}/actions/remap`, { targetUuid, dryRun }).then(r => r.data);
  }
};
