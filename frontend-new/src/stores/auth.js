import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import authApi from '@/api/auth';
import router from '@/router';
import { jwtDecode } from 'jwt-decode';

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const refreshToken = ref(localStorage.getItem('refreshToken') || null);
  const user = ref(JSON.parse(localStorage.getItem('user')) || null);

  const isLoggedIn = computed(() => !!accessToken.value);

  function setTokens(access, refresh) {
    accessToken.value = access;
    refreshToken.value = refresh;
    localStorage.setItem('accessToken', access);
    localStorage.setItem('refreshToken', refresh);
  }

  function setUser(userData) {
    user.value = userData;
    localStorage.setItem('user', JSON.stringify(userData));
  }

  function clearAuth() {
    accessToken.value = null;
    refreshToken.value = null;
    user.value = null;
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }

  async function login(credentials) {
    try {
      // Map incoming credentials to backend expectations
      const response = await authApi.login({ login: credentials.email ?? credentials.login, password: credentials.password });

      // Backend ApiResponse success code is 0
      if (response.data.code !== 0) {
        throw new Error(response.data.message || '登录失败');
      }

      const { accessToken: newAccessToken, refreshToken: newRefreshToken, user } = response.data.data;
      setTokens(newAccessToken, newRefreshToken);
      
      // Decode token to get user info
      const decoded = jwtDecode(newAccessToken);
      setUser({
        uuid: decoded.sub,
        role: decoded.role,
        // Prefer server user data if available
        ...(user || {}),
      });

      await router.push('/dashboard');
    } catch (error) {
      console.error('Login failed:', error);
      // Re-throw the error to be handled by the component
      throw error;
    }
  }

  async function logout() {
    try {
      // Call backend logout to revoke refresh token
      if (refreshToken.value) {
        await authApi.logout(refreshToken.value);
      }
    } catch (error) {
      console.error('Logout API call failed, proceeding with client-side logout.', error);
    } finally {
      clearAuth();
      await router.push('/login');
    }
  }

  async function handleRefreshToken() {
    try {
      if (!refreshToken.value) throw new Error('No refresh token');
      const response = await authApi.refreshToken(refreshToken.value);
      if (response.data.code !== 0) throw new Error(response.data.message || '刷新Token失败');
      const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data.data;
      setTokens(newAccessToken, newRefreshToken);
    } catch (error) {
      console.error('Failed to refresh token:', error);
      // If refresh fails, log out
      clearAuth();
      await router.push('/login');
      throw error;
    }
  }

  return {
    accessToken,
    refreshToken,
    user,
    isLoggedIn,
    login,
    logout,
    refreshToken: handleRefreshToken,
    // Expose internal functions if needed elsewhere, e.g. for registration
    setUser,
    setTokens,
  };
});
