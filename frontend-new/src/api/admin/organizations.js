import apiClient from '@/api/index';

export default {
  list({ q, status, page = 1, size = 20, withTotal = true } = {}) {
    const sp = new URLSearchParams();
    if (q) sp.set('q', q);
    if (status) sp.set('status', status);
    sp.set('page', page);
    sp.set('size', size);
    sp.set('withTotal', withTotal);
    return apiClient.get(`/admin/orgs?${sp.toString()}`).then(r => {
      const d = r.data?.data || {};
      return { items: d.items || [], total: d.total ?? 0, page: d.page, size: d.size };
    });
  },
  get(uuid) {
    return apiClient.get(`/admin/orgs/${uuid}`).then(r => r.data?.data?.organization);
  },
  update(uuid, { status, rejectReason, isVerified } = {}) {
    const body = {};
    if (status) body.status = status;
    if (rejectReason !== undefined) body.rejectReason = rejectReason;
    if (typeof isVerified === 'boolean') body.isVerified = isVerified;
    return apiClient.patch(`/admin/orgs/${uuid}`, body).then(r => r.data?.data?.organization);
  },
  updateBasic(uuid, patch = {}) {
    // Allow editing name/avatar/description/freeText/isPublic/allowJoinRequest/allowInviteLink
    return apiClient.patch(`/admin/orgs/${uuid}`, patch).then(r => r.data?.data?.organization);
  },
  getDetails(uuid, params = {}) {
    const sp = new URLSearchParams();
    if (params.page) sp.set('page', params.page);
    if (params.size) sp.set('size', params.size);
    const url = sp.toString() ? `/admin/orgs/${uuid}/details?${sp.toString()}` : `/admin/orgs/${uuid}/details`;
    return apiClient.get(url);
  },
  disband(uuid) { return apiClient.post(`/admin/orgs/${uuid}/actions/disband`).then(r => r.data); },
  suspend(uuid) { return apiClient.post(`/admin/orgs/${uuid}/actions/suspend`).then(r => r.data); },
  restore(uuid) { return apiClient.post(`/admin/orgs/${uuid}/actions/restore`).then(r => r.data); },
  delete(uuid) { return apiClient.post(`/admin/orgs/${uuid}/actions/delete`).then(r => r.data); },
};
