import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const apiClient = axios.create({
  baseURL: '/api', // Use the proxy
});

// Request interceptor to add the auth token to headers
apiClient.interceptors.request.use(config => {
  const authStore = useAuthStore();
  const token = authStore.accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // If sending FormData, let the browser set multipart boundary automatically
  if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
    if (config.headers && 'Content-Type' in config.headers) {
      delete config.headers['Content-Type'];
    }
  } else {
    // Default JSON for non-FormData bodies
    if (config.method && ['post','put','patch'].includes(config.method.toLowerCase())) {
      config.headers['Content-Type'] = config.headers['Content-Type'] || 'application/json';
    }
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// Response interceptor to handle token refresh
apiClient.interceptors.response.use(response => {
  return response;
}, async error => {
  const originalRequest = error.config;
  const authStore = useAuthStore();

  // Handle 401 Unauthorized errors due to expired token.
  // Skip for auth endpoints to avoid infinite loops.
  const isAuthEndpoint = originalRequest?.url?.includes('/auth/login') || originalRequest?.url?.includes('/auth/refresh');
  if (error.response && error.response.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
    originalRequest._retry = true;
    try {
      // 若无 refresh token（如 OAuth 回跳前或未登录），跳过刷新，交由上层处理
      const hasRefresh = !!(typeof window !== 'undefined' && window.localStorage && localStorage.getItem('refreshToken'));
      if (!hasRefresh) {
        return Promise.reject(error);
      }
      await authStore.refreshToken();
      // Retry the original request with the new token
      return apiClient(originalRequest);
    } catch (refreshError) {
      // If refresh fails, log out the user
      authStore.logout();
      return Promise.reject(refreshError);
    }
  }

  return Promise.reject(error);
});

export default apiClient;
