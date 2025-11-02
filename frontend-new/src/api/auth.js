import apiClient from './index';

export default {
  // Registration: step 1 - request verification code
  sendVerificationCode(email) {
    return apiClient.post('/auth/register/request-code', { email });
  },

  // Registration: step 2 - verify code, issue regSession
  verifySession(requestId, code, email) {
    return apiClient.post('/auth/register/verify-code', { requestId, code, email });
  },

  // Registration: step 3 - complete registration (no tokens returned)
  registerComplete({ regSession, username, password, realName, company, location }) {
    return apiClient.post('/auth/register/complete', { regSession, username, password, realName, company, location });
  },

  // Login with email or username
  login({ login, password }) {
    return apiClient.post('/auth/login', { login, password });
  },

  // Refresh token
  refreshToken(refreshToken) {
    return apiClient.post('/auth/refresh', { refreshToken });
  },

  // Logout: revoke the refresh token
  logout(refreshToken) {
    return apiClient.post('/auth/logout', { refreshToken });
  },

  // Forgot password flow
  requestPasswordCode(email) {
    return apiClient.post('/auth/password/request-code', { email });
  },
  verifyPasswordCode({ email, code, requestId }) {
    return apiClient.post('/auth/password/verify-code', { email, code, requestId });
  },
  resetPassword({ resetSession, newPassword }) {
    return apiClient.post('/auth/password/reset', { resetSession, newPassword });
  },
};
