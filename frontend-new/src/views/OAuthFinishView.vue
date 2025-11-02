<template>
  <div class="finish-container">
    <div class="content">正在完成登录，请稍候…</div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';
import apiClient from '@/api';

function readParams() {
  const hash = window.location.hash?.startsWith('#') ? window.location.hash.slice(1) : '';
  const search = window.location.search?.startsWith('?') ? window.location.search.slice(1) : '';
  const src = hash || search;
  return new URLSearchParams(src);
}

onMounted(async () => {
  const params = readParams();
  const error = params.get('error');
  const message = params.get('message');
  const bindFlag = params.get('bind');
  const accessToken = params.get('accessToken');
  const refreshToken = params.get('refreshToken');
  const target = params.get('redirect') || '/dashboard';

  // 绑定场景（不下发token）
  if (bindFlag) {
    const success = params.get('success');
    const q = new URLSearchParams();
    if (success) {
      q.set('bindSuccess', '1');
    } else {
      q.set('bindError', error || '1');
      if (message) q.set('message', message);
    }
    await router.replace(`/profile?${q.toString()}`);
    return;
  }

  // 登录场景（必须有token）
  if (error || !accessToken || !refreshToken) {
    const q = new URLSearchParams();
    q.set('oauthError', error || '1');
    if (message) q.set('message', message);
    await router.replace(`/login?${q.toString()}`);
    return;
  }

  const auth = useAuthStore();
  auth.setTokens(accessToken, refreshToken);
  try {
    const me = await apiClient.get('/users/me');
    if (me?.data?.data?.user) auth.setUser(me.data.data.user);
  } catch (e) {}
  await router.replace(target);
});
</script>

<style scoped>
.finish-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
}
.content {
  padding: 24px;
  color: #666;
}
</style>
