import apiClient from '@/api/index';

export default {
  get(uuid) {
    return apiClient.get(`/vulns/${uuid}`).then(r => r.data?.data);
  },
  list(params = {}) {
    const {
      q,
      language,
      status,
      identifierPrefix,
      tagCode,
      category,
      submittedFrom,
      submittedTo,
      modifiedFrom,
      modifiedTo,
      sortBy,
      sortOrder,
      page = 1,
      size = 20,
      withTotal = true,
    } = params;

    const search = new URLSearchParams();
    if (q) search.set('q', q);
    if (language) search.set('language', language);
    if (status) search.set('status', status);
    if (identifierPrefix) search.set('identifierPrefix', identifierPrefix);
    if (tagCode) search.set('tagCode', tagCode);
    if (category) search.set('category', category);
    if (submittedFrom) search.set('submittedFrom', submittedFrom);
    if (submittedTo) search.set('submittedTo', submittedTo);
    if (modifiedFrom) search.set('modifiedFrom', modifiedFrom);
    if (modifiedTo) search.set('modifiedTo', modifiedTo);
    if (sortBy) search.set('sortBy', sortBy);
    if (sortOrder) search.set('sortOrder', sortOrder);
    search.set('page', page);
    search.set('size', size);
    search.set('withTotal', withTotal);

    return apiClient.get(`/vulns?${search.toString()}`).then(r => {
      const d = r.data?.data || {};
      return { items: d.items || [], total: d.total ?? 0, page: d.page, size: d.size };
    });
  },
  search(params = {}) {
    const {
      q,
      status,
      identifierPrefix,
      organizationUuid,
      category,
      sortBy = 'modified',
      sortOrder = 'desc',
      page = 1,
      size = 20,
      withTotal = true,
    } = params;

    const search = new URLSearchParams();
    if (q) search.set('q', q);
    if (status) search.set('status', status);
    if (identifierPrefix) search.set('identifierPrefix', identifierPrefix);
    if (organizationUuid) search.set('organizationUuid', organizationUuid);
    if (category) search.set('category', category);
    search.set('page', page);
    search.set('size', size);
    search.set('withTotal', withTotal);
    search.set('sortBy', sortBy);
    search.set('sortOrder', sortOrder);

    return apiClient.get(`/vulns/search?${search.toString()}`).then(r => {
      const d = r.data?.data || {};
      return { items: d.items || [], total: d.total ?? 0, page: d.page, size: d.size };
    });
  },
  create(payload) {
    return apiClient.post('/vulns', payload).then(r => r.data?.data?.vulnerability);
  },
  updateStatus(uuid, { status, rejectReason }) {
    return apiClient.patch(`/admin/vulns/${uuid}/status`, { status, rejectReason }).then(r => r.data?.data?.vulnerability);
  },
  update(uuid, payload) {
    return apiClient.patch(`/vulns/${uuid}`, payload).then(r => r.data?.data?.vulnerability);
  },
  delete(uuid) {
    return apiClient.delete(`/admin/vulns/${uuid}`).then(r => r.data);
  },
  addTag(uuid, { code, name }) {
    return apiClient.post(`/vulns/${uuid}/tags`, { code, name }).then(r => r.data);
  },
  removeTag(uuid, nameOrCode) {
    return apiClient.delete(`/vulns/${uuid}/tags/${encodeURIComponent(nameOrCode)}`).then(r => r.data);
  }
};
