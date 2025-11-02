import apiClient from './index';

export default {
  // Match backend: GET /users/me -> { code:0, data: { user: {...} } }
  getUserInfo() {
    return apiClient.get('/users/me');
  },

  // 公共用户资料（精简）
  getByUuid(uuid) {
    return apiClient.get(`/users/${uuid}`);
  },

  // Match backend: PATCH /users/me
  updateUserInfo(userData) {
    return apiClient.patch('/users/me', userData);
  },

  // Get user statistics
  getUserStats() {
    return apiClient.get('/users/me/stats');
  },

  // Get user contributions calendar data
  getUserContributions(year) {
    return apiClient.get('/users/me/contributions', {
      params: year ? { year } : {}
    });
  },

  // Public: other user's stats
  getStatsFor(uuid) {
    return apiClient.get(`/users/${uuid}/stats`);
  },
  // Public: other user's contributions
  getContributionsFor(uuid, year) {
    return apiClient.get(`/users/${uuid}/contributions`, { params: year ? { year } : {} });
  },

  // Placeholder: UI uses this, backend currently exposes password reset via /auth/password/*
  // Keep endpoint here for future integration.
  changePassword(passwords) {
    return Promise.reject(new Error('暂未对接：请使用“忘记密码”流程'));
  },

  // Placeholder: backend暂无直传头像端点（可改为上传到对象存储并PATCH avatar URL）
  uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);
    return Promise.reject(new Error('暂未对接：请上传图片到存储后PATCH avatar URL'));
  },
};
