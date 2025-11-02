<template>
  <div class="register-container">
    <transition name="fade-zoom" appear>
      <div class="register-box">
        <h1 class="main-title">创建您的账户</h1>

        <div class="stepper">
          <div class="step" :class="{ active: step >= 1 }">
            <div class="step-number">1</div>
            <div class="step-label">邮箱验证</div>
          </div>
          <div class="step-divider"></div>
          <div class="step" :class="{ active: step >= 2 }">
            <div class="step-number">2</div>
            <div class="step-label">设置信息</div>
          </div>
          <div class="step-divider"></div>
          <div class="step" :class="{ active: step >= 3 }">
            <div class="step-number">3</div>
            <div class="step-label">完成注册</div>
          </div>
        </div>

        <div class="form-content">
          <v-alert v-if="error" type="error" variant="tonal" class="mb-4" dense closable @click:close="error = null">{{ error }}</v-alert>
          <v-alert v-if="successMsg" type="success" variant="tonal" class="mb-4" dense closable @click:close="successMsg = null">{{ successMsg }}</v-alert>

          <!-- Step 1: Email & Code -->
          <div v-if="step < 2">
            <p class="subtitle">{{ !requestId ? '输入您的邮箱以接收验证码。' : `验证码已发送至 ${email}` }}</p>
            <v-text-field v-model="email" label="邮箱" variant="outlined" :rules="[rules.required, rules.email]" :disabled="!!requestId"></v-text-field>
            <v-btn v-if="!requestId" text color="primary" @click="sendCode" :loading="loading" class="action-btn">发送验证码</v-btn>
            
            <div v-if="requestId">
              <v-text-field v-model="code" label="验证码" variant="outlined" :rules="[rules.required]" @blur="onCodeBlur"></v-text-field>
              <v-btn text color="primary" @click="verifyCode" :loading="loading" class="action-btn">验证并继续</v-btn>
            </div>
          </div>

          <!-- Step 2: Set Info -->
          <div v-if="step === 2">
            <p class="subtitle">请完善您的个人信息。</p>
            <v-row>
              <v-col cols="12" sm="6"><v-text-field v-model="profile.name" label="用户名*" variant="outlined" :rules="[rules.required]"></v-text-field></v-col>
              <v-col cols="12" sm="6"><v-text-field v-model="profile.password" label="密码*" type="password" variant="outlined" :rules="[rules.required, rules.password]"></v-text-field></v-col>
              <v-col cols="12" sm="6"><v-text-field v-model="profile.realName" label="真实姓名" variant="outlined"></v-text-field></v-col>
              <v-col cols="12" sm="6"><v-text-field v-model="profile.company" label="公司" variant="outlined"></v-text-field></v-col>
              <v-col cols="12" sm="6"><v-text-field v-model="profile.location" label="所在地" variant="outlined"></v-text-field></v-col>
              <v-col cols="12"><v-text-field v-model="profile.website" label="个人网站" variant="outlined"></v-text-field></v-col>
              <v-col cols="12"><v-textarea v-model="profile.freeText" label="个人简介" variant="outlined" rows="2"></v-textarea></v-col>
            </v-row>
            <v-btn text color="primary" @click="handleRegister" :loading="loading" class="action-btn">完成注册</v-btn>
          </div>

          <!-- Step 3: Success -->
          <div v-if="step === 3" class="text-center">
             <v-icon size="64" color="success">mdi-check-circle-outline</v-icon>
             <h2 class="text-h5 mt-4">注册成功！</h2>
             <p class="mt-2">您现在可以登录您的账户了。</p>
          </div>
        </div>

        <div class="footer-link">
          <router-link to="/login">已有账户？点击登录</router-link>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import authApi from '@/api/auth';

const step = ref(1);
const email = ref('');
const code = ref('');
const requestId = ref(null); // 来自请求验证码返回
const regSession = ref(null); // 验证码验证后返回
const loading = ref(false);
const error = ref(null);
const successMsg = ref(null);

const profile = reactive({
  name: '',
  password: '',
  realName: '',
  company: '',
  location: '',
  gitHub: '',
  website: '',
  freeText: '',
});

const rules = {
  required: value => !!value || '此字段为必填项。',
  email: value => /.+@.+\..+/.test(value) || '请输入有效的邮箱地址。',
  password: value => value.length >= 6 || '密码长度至少为6位。',
};

const sendCode = async () => {
  if (!rules.email(email.value)) {
    error.value = '请输入有效的邮箱地址。';
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    const response = await authApi.sendVerificationCode(email.value);
    if (response.data.code !== 0) {
      throw new Error(response.data.message || '发送验证码失败。');
    }
    requestId.value = response.data.data.requestId;
    successMsg.value = '验证码已发送，请检查您的邮箱。';
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '发送验证码失败。';
  } finally {
    loading.value = false;
  }
};

const verifyCode = async () => {
  // 去除空格
  if (code.value) code.value = String(code.value).replace(/\s+/g, '').trim();
  if (!code.value) {
    error.value = '请输入验证码。';
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    const response = await authApi.verifySession(requestId.value, code.value, email.value);
    if (response.data.code !== 0) {
      throw new Error(response.data.message || '验证码错误。');
    }
    regSession.value = response.data.data.regSession;
    step.value = 2;
    error.value = null;
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '验证码错误。';
  } finally {
    loading.value = false;
  }
};

const onCodeBlur = () => {
  if (code.value) code.value = String(code.value).replace(/\s+/g, '').trim();
};

const handleRegister = async () => {
  if (!profile.name || !profile.password) {
    error.value = '用户名和密码为必填项。';
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    const response = await authApi.registerComplete({
      regSession: regSession.value,
      username: profile.name,
      password: profile.password,
      realName: profile.realName,
      company: profile.company,
      location: profile.location,
    });
    if (response.data.code !== 0) {
      throw new Error(response.data.message || '注册失败。');
    }
    step.value = 3;
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '注册失败。';
  } finally {
    loading.value = false;
  }
};

watch(step, (newStep) => {
  if (newStep === 3) {
    // Clear sensitive data after registration is complete
    profile.password = '';
    requestId.value = null;
    regSession.value = null;
    code.value = '';
  }
});
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px);
  width: 100%;
  background-color: #f7f8fa;
}
.register-box {
  width: 600px; /* Widen the box for more fields */
  background-color: #ffffff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}
.main-title {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 32px;
  color: #333;
}
.stepper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 0 20px;
}
.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
  transition: color 0.3s ease;
}
.step.active {
  color: #333;
}
.step-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #ccc;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  margin-bottom: 8px;
  transition: background-color 0.3s ease;
}
.step.active .step-number {
  background-color: #1976D2;
}
.step-label {
  font-size: 14px;
}
.step-divider {
  flex-grow: 1;
  height: 2px;
  background-color: #eee;
  margin: 0 16px;
  position: relative;
  top: -12px;
}
.form-content {
  border-top: 1px solid #eee;
  padding-top: 32px;
}
.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 24px;
}
.action-btn {
  display: block;
  margin: 16px auto 0;
  font-weight: bold;
}
.footer-link {
  text-align: center;
  margin-top: 24px;
}
.footer-link a {
  color: #666;
  text-decoration: none;
  transition: color 0.3s ease;
}
.footer-link a:hover {
  color: #1976D2;
}
.fade-zoom-enter-active,
.fade-zoom-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-zoom-enter-from,
.fade-zoom-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
