import apiClient from './index';

export default {
  upload(file, { organizationUuid, mimeType } = {}) {
    const fd = new FormData();
    fd.append('file', file);
    if (organizationUuid) fd.append('organizationUuid', organizationUuid);
    if (mimeType) fd.append('mimeType', mimeType);
    // 不手动设置 Content-Type，交由浏览器生成带 boundary 的 multipart/form-data
    return apiClient.post('/cosv/files', fd);
  },
  parse(rawFileUuid, { language, categoryCode, tagCodes, mode } = {}) {
    const params = {};
    if (language) params.language = language;
    if (categoryCode) params.categoryCode = categoryCode;
    if (Array.isArray(tagCodes) && tagCodes.length) params.tagCodes = tagCodes.join(',');
    if (mode) params.mode = mode;
    return apiClient.post(`/cosv/files/${rawFileUuid}/parse`, null, { params });
  },
  parseBatch(rawFileUuid, { language, categoryCode, tagCodes, mode } = {}) {
    const params = {};
    if (language) params.language = language;
    if (categoryCode) params.categoryCode = categoryCode;
    if (Array.isArray(tagCodes) && tagCodes.length) params.tagCodes = tagCodes.join(',');
    if (mode) params.mode = mode;
    return apiClient.post(`/cosv/files/${rawFileUuid}/parse-batch`, null, { params });
  },
  ingest(rawFileUuid, payload) {
    return apiClient.post(`/cosv/files/${rawFileUuid}/ingest`, payload);
  },
  ingestBatch(rawFileUuid, payload) {
    return apiClient.post(`/cosv/files/${rawFileUuid}/ingest-batch`, payload);
  },
};
