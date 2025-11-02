<template>
  <div class="login-container">
    <transition name="fade-zoom" appear>
      <div class="login-content">
        <h1 class="text-h4 text-center font-weight-bold mb-8">
          登录
        </h1>
        
        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-4"
          closable
          @click:close="error = null"
        >
          {{ error }}
        </v-alert>

        <div class="login-form">
          <!-- 邮箱密码登录表单 -->
          <form @submit.prevent="handleLogin">
            <div class="input-group mb-6">
              <label class="input-label">邮箱</label>
              <div class="input-wrapper">
                <input 
                  v-model="email"
                  type="email"
                  class="custom-input"
                  placeholder=""
                />
              </div>
            </div>
            
            <div class="input-group mb-2">
              <label class="input-label">密码</label>
              <div class="input-wrapper">
                <input 
                  v-model="password"
                  type="password"
                  class="custom-input"
                  placeholder=""
                />
              </div>
            </div>
            
            <div class="d-flex justify-end mb-6">
              <router-link to="/forgot-password" class="text-decoration-none text-grey-darken-1">忘记密码？</router-link>
            </div>
            
            <v-btn
              :loading="loading"
              :disabled="loading"
              block
              color="primary"
              size="x-large"
              type="submit"
              class="mb-6"
            >
              登录
            </v-btn>
          </form>
          
          <!-- 分隔线 -->
          <div class="divider mb-6">
            <span class="divider-text">或</span>
          </div>
          
          <!-- GitHub 登录按钮 -->
          <v-btn
            block
            variant="outlined"
            size="x-large"
            class="mb-4"
            @click="handleGithubLogin"
          >
            <v-icon left class="mr-2">mdi-github</v-icon>
            GITHUB 授权登录
          </v-btn>
          
          <!-- 注册链接 -->
          <div class="text-center mt-6">
            <span class="text-grey-darken-1">还没有账号？</span>
            <router-link to="/register" class="text-decoration-none ml-2">立即注册</router-link>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const email = ref('');
const password = ref('');
const loading = ref(false);
const error = ref(null);
const route = useRoute();

const handleLogin = async () => {
  loading.value = true;
  error.value = null;
  try {
    await authStore.login({ email: email.value, password: password.value });
    // The store handles redirection on success
  } catch (err) {
    error.value = err.message || '登录失败，请检查您的凭据或网络连接。';
  } finally {
    loading.value = false;
  }
};

const handleGithubLogin = () => {
  const finishUrl = `${window.location.origin}/oauth/finish`;
  window.location.href = `/api/oauth/github/render?redirect=${encodeURIComponent(finishUrl)}`;
};

onMounted(() => {
  // 如果从 OAuth 完成页带回错误信息，则在此展示
  const oauthError = route.query.oauthError || route.query.error;
  const msg = route.query.message;
  if (oauthError) {
    error.value = typeof msg === 'string' ? msg : '授权登录失败，请改用邮箱密码或稍后再试。';
  }
});
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px); /* 减去应用栏高度 */
  width: 100%;
  background-color: #ffffff;
}

.login-content {
  width: 450px;
  padding: 32px;
}

.login-form {
  width: 100%;
}

/* 输入框样式 */
.input-group {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  margin-bottom: 6px;
  color: #333;
  font-size: 14px;
}

.input-wrapper {
  position: relative;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.custom-input {
  width: 100%;
  padding: 12px 16px;
  border: none;
  background-color: #f5f7fa;
  outline: none;
  font-size: 16px;
  box-sizing: border-box;
}

.custom-input:focus {
  background-color: #e8f0fe;
}


/* 淡入+缩放过渡效果 */
.fade-zoom-enter-active,
.fade-zoom-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-zoom-enter-from,
.fade-zoom-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* 分隔线样式 */
.divider {
  position: relative;
  text-align: center;
  height: 1px;
  background-color: rgba(0, 0, 0, 0.12);
  margin: 24px 0;
}

.divider-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  padding: 0 12px;
  color: rgba(0, 0, 0, 0.6);
}
</style>
