import apiClient from './index';

export default {
  getUserLeaderboard(limit = 10) {
    return apiClient.get(`/leaderboard/users`, { params: { limit, withTotal: false } })
      .then(r => r.data?.data?.items || []);
  },
  getOrganizationLeaderboard(limit = 10) {
    return apiClient.get(`/leaderboard/organizations`, { params: { limit, withTotal: false } })
      .then(r => r.data?.data?.items || []);
  }
};
