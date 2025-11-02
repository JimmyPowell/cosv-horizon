import apiClient from '@/api/index';

export default {
  getSite() {
    return apiClient.get('/settings/site').then(r => r.data?.data?.settings || {});
  },
};

