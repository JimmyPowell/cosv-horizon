import apiClient from './index';

export default {
  // 我的组织列表
  listMine() {
    return apiClient.get('/orgs/me');
  },

  // 创建组织（支持设置可见性与策略）
  create({ name, description, avatar, freeText, isPublic, allowJoinRequest, allowInviteLink }) {
    const body = { name, description, avatar };
    if (typeof freeText === 'string') body.freeText = freeText;
    if (typeof isPublic === 'boolean') body.isPublic = isPublic;
    if (typeof allowJoinRequest === 'boolean') body.allowJoinRequest = allowJoinRequest;
    if (typeof allowInviteLink === 'boolean') body.allowInviteLink = allowInviteLink;
    return apiClient.post('/orgs', body);
  },

  // 公开组织搜索
  searchPublicOrgs({ q, page = 1, size = 20, withTotal = true } = {}) {
    const params = { page, size, withTotal };
    if (q && q.trim()) params.q = q.trim();
    return apiClient.get('/orgs/search', { params });
  },

  // 组织详情（uuid）
  getByUuid(uuid) {
    return apiClient.get(`/orgs/${uuid}`);
  },

  // 更新组织（管理员）
  update(uuid, patch) {
    return apiClient.patch(`/orgs/${uuid}`, patch);
  },

  // 成员列表
  getMembers(uuid) {
    return apiClient.get(`/orgs/${uuid}/members`);
  },
  // 公开成员精简列表（公开组织）
  getPublicMembers(uuid) {
    return apiClient.get(`/orgs/${uuid}/members/public`);
  },

  // 移除成员（管理员）
  removeMember(uuid, memberUuid) {
    return apiClient.post(`/orgs/${uuid}/members/remove`, { memberUuid });
  },

  // 变更成员角色（管理员）
  changeMemberRole(uuid, memberUuid, role) {
    return apiClient.post(`/orgs/${uuid}/members/change-role`, { memberUuid, role });
  },

  // 发起邀请（管理员）
  invite(uuid, loginOrEmail) {
    return apiClient.post(`/orgs/${uuid}/invitations/invite`, { loginOrEmail });
  },

  // 我的邀请列表（被邀请者）
  myInvitations(params = {}) {
    return apiClient.get('/orgs/invitations/mine', { params });
  },

  // 组织内邀请列表（管理员）
  orgInvitations(uuid, params = {}) {
    return apiClient.get(`/orgs/${uuid}/invitations`, { params });
  },

  // 接受/拒绝邀请
  acceptInvite(inviteUuid) {
    return apiClient.post(`/orgs/invitations/${inviteUuid}/accept`);
  },
  rejectInvite(inviteUuid) {
    return apiClient.post(`/orgs/invitations/${inviteUuid}/reject`);
  },

  // ===== Join Requests =====
  // 提交加入申请（公开组织且允许申请）
  submitJoinRequest(uuid, message) {
    const body = message ? { message } : {};
    return apiClient.post(`/orgs/${uuid}/join-requests`, body);
  },
  // 加入申请列表（管理员）
  listJoinRequests(uuid, params = {}) {
    return apiClient.get(`/orgs/${uuid}/join-requests`, { params });
  },
  // 审批加入申请（管理员）
  approveJoinRequest(requestUuid) {
    return apiClient.post(`/orgs/join-requests/${requestUuid}/approve`);
  },
  rejectJoinRequest(requestUuid) {
    return apiClient.post(`/orgs/join-requests/${requestUuid}/reject`);
  },

  // ===== Invite Links / 邀请码 =====
  // 生成邀请链接/邀请码（管理员）
  createInviteLink(uuid, { expiresInDays } = {}) {
    const body = (expiresInDays || expiresInDays === 0) ? { expiresInDays } : {};
    return apiClient.post(`/orgs/${uuid}/invite-links`, body);
  },
  // 邀请链接列表（管理员）
  listInviteLinks(uuid) {
    return apiClient.get(`/orgs/${uuid}/invite-links`);
  },
  // 撤销邀请链接（管理员）
  revokeInviteLink(linkUuid) {
    return apiClient.post(`/orgs/invite-links/${linkUuid}/revoke`);
  },
  // 使用邀请码申请加入（登录用户）
  applyJoinByCode({ code, message }) {
    return apiClient.post('/orgs/invite-links/apply', { code, message });
  },

  // 解散组织（管理员）
  disband(uuid) {
    return apiClient.post(`/orgs/${uuid}/actions/disband`);
  },

  // 暂停组织（管理员）
  suspend(uuid) {
    return apiClient.post(`/orgs/${uuid}/actions/suspend`);
  },

  // 恢复组织（管理员）
  restore(uuid) {
    return apiClient.post(`/orgs/${uuid}/actions/restore`);
  },

  // 删除组织（软删除，仅暂停后可删除）
  delete(uuid) {
    return apiClient.post(`/orgs/${uuid}/actions/delete`);
  },

  // ===== Points policy (org override) =====
  getPointsPolicy(uuid) {
    return apiClient.get(`/orgs/${uuid}/settings/points`).then(r => r.data?.data?.settings || {});
  },
  updatePointsPolicy(uuid, payload = {}) {
    return apiClient.put(`/orgs/${uuid}/settings/points`, payload).then(r => r.data?.data?.settings || {});
  },
  previewPointsPolicy(uuid, payload = {}) {
    return apiClient.post(`/orgs/${uuid}/settings/points/preview`, payload).then(r => r.data?.data?.result || {});
  },
};
