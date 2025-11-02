import apiClient from '@/api/index';

export default {
  list({ q, role, status, page = 1, size = 20, withTotal = true } = {}) {
    const sp = new URLSearchParams();
    if (q) sp.set('q', q);
    if (role) sp.set('role', role);
    if (status) sp.set('status', status);
    sp.set('page', page);
    sp.set('size', size);
    sp.set('withTotal', withTotal);
    return apiClient.get(`/admin/users?${sp.toString()}`).then(r => {
      const d = r.data?.data || {};
      return { items: d.items || [], total: d.total ?? 0, page: d.page, size: d.size };
    });
  },
  get(uuid) {
    return apiClient.get(`/admin/users/${uuid}`).then(r => r.data?.data?.user);
  },
  update(uuid, { role, status }) {
    return apiClient.patch(`/admin/users/${uuid}`, { role, status }).then(r => r.data?.data?.user);
  },
  updateProfile(uuid, payload = {}) {
    return apiClient.patch(`/admin/users/${uuid}/profile`, payload).then(r => r.data?.data?.user);
  },
  resetPassword(uuid, newPassword) {
    return apiClient.post(`/admin/users/${uuid}/reset-password`, { newPassword }).then(r => r.data?.code === 0);
  },
};
