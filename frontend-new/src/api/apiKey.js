import apiClient from './index';

export default {
  // 列出 API Key（当前用户，或按组织）
  getMyApiKeys(params = {}) {
    // 支持可选参数：{ organizationUuid }
    return apiClient.get('/api-keys', { params });
  },

  // 创建 API Key
  // body: { description, scopes: string[], organizationUuid?: string, expireTime?: string(ISO-8601) | null }
  createApiKey(body) {
    return apiClient.post('/api-keys', body);
  },

  // 更新 API Key（描述/权限/过期时间）
  // patch: { description?, scopes?: string[]|[], expireTime?: string(ISO-8601)|''|null }
  updateApiKey(uuid, patch) {
    return apiClient.patch(`/api-keys/${uuid}`, patch);
  },

  // 撤销 API Key
  revokeApiKey(uuid) {
    return apiClient.post(`/api-keys/${uuid}/revoke`);
  },

  // 使用日志
  getApiKeyUsageLogs(uuid, params = {}) {
    return apiClient.get(`/api-keys/${uuid}/usage`, { params });
  },
};
