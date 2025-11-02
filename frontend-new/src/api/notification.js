import apiClient from './index';

export default {
  list(params = {}) {
    return apiClient.get('/notifications', { params });
  },
  unreadCount() {
    return apiClient.get('/notifications/unread-count');
  },
  markRead(uuid) {
    return apiClient.post('/notifications/read', { uuid });
  },
  markAllRead() {
    return apiClient.post('/notifications/mark-all-read');
  },
  remove(uuid) {
    return apiClient.delete(`/notifications/${uuid}`);
  },
};
