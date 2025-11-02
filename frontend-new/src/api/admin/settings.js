import apiClient from '@/api/index';

export default {
  getSite() {
    return apiClient.get('/admin/settings/site').then(r => r.data?.data?.settings || {});
  },
  updateSite(payload = {}) {
    return apiClient.put('/admin/settings/site', payload).then(r => r.data?.data?.settings || {});
  },
  getPoints() {
    return apiClient.get('/admin/settings/points').then(r => r.data?.data?.settings || {});
  },
  updatePoints(payload = {}) {
    return apiClient.put('/admin/settings/points', payload).then(r => r.data?.data?.settings || {});
  },
  previewPoints(payload = {}) {
    return apiClient.post('/admin/settings/points/preview', payload).then(r => r.data?.data?.result || {});
  },
};
