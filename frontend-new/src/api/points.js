import apiClient from './index';

export default {
  // My user points ledger
  getMyPoints({ page = 1, size = 20 } = {}) {
    return apiClient.get('/users/me/points', { params: { page, size } });
  },
  // Org points ledger (admin only)
  getOrgPoints(orgUuid, { page = 1, size = 20 } = {}) {
    return apiClient.get(`/orgs/${orgUuid}/points`, { params: { page, size } });
  },
  // Summaries (rating + rank)
  getUserSummary(uuid) {
    return apiClient.get(`/points/users/${uuid}/summary`).then(r => r.data?.data?.summary || {});
  },
  getOrgSummary(uuid) {
    return apiClient.get(`/points/orgs/${uuid}/summary`).then(r => r.data?.data?.summary || {});
  },
};

