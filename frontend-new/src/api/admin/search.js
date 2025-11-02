import apiClient from '@/api/index';

export default {
  reindexAll() {
    return apiClient.post('/admin/search/reindex-all').then(r => r.data?.data?.count ?? 0);
  },
  indexOne(uuid) {
    return apiClient.post(`/admin/search/index/${uuid}`).then(r => r.data);
  },
  deleteOne(uuid) {
    return apiClient.delete(`/admin/search/index/${uuid}`).then(r => r.data);
  }
};

