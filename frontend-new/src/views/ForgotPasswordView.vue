<template>
  <v-container class="my-8" style="max-width: 760px;">
    <h1 class="text-h4 mb-6">找回密码</h1>

    <v-alert v-if="globalError" type="error" variant="tonal" class="mb-4" closable @click:close="globalError=null">
      {{ globalError }}
    </v-alert>

    <v-stepper v-model="step" flat>
      <v-stepper-header>
        <v-stepper-item :value="1" title="输入邮箱" :complete="step>1" />
        <v-divider></v-divider>
        <v-stepper-item :value="2" title="验证验证码" :complete="step>2" />
        <v-divider></v-divider>
        <v-stepper-item :value="3" title="设置新密码" />
      </v-stepper-header>

      <v-stepper-window>
        <v-stepper-window-item :value="1">
          <v-card>
            <v-card-text>
              <v-text-field v-model="email" label="邮箱" type="email" variant="outlined" hide-details class="mb-4" />
              <div class="d-flex">
                <v-spacer />
                <v-btn color="primary" :loading="loading" :disabled="!email" @click="sendCode">发送验证码</v-btn>
              </div>
              <div class="text-caption text-grey mt-2">提示：若该邮箱存在，我们将发送验证码至邮箱（不暴露账号是否存在）。</div>
            </v-card-text>
          </v-card>
        </v-stepper-window-item>

        <v-stepper-window-item :value="2">
          <v-card>
            <v-card-text>
              <v-text-field v-model="code" label="验证码" variant="outlined" hide-details class="mb-4" />
              <div class="d-flex">
                <v-btn variant="text" @click="step=1">上一步</v-btn>
                <v-spacer />
                <v-btn color="primary" :loading="loading" :disabled="!code || !requestId" @click="verifyCode">验证</v-btn>
              </div>
            </v-card-text>
          </v-card>
        </v-stepper-window-item>

        <v-stepper-window-item :value="3">
          <v-card>
            <v-card-text>
              <v-text-field v-model="newPassword" type="password" label="新密码" variant="outlined" hide-details class="mb-4" />
              <v-text-field v-model="confirmPassword" type="password" label="确认新密码" variant="outlined" hide-details class="mb-4" />
              <div class="d-flex">
                <v-btn variant="text" @click="step=2">上一步</v-btn>
                <v-spacer />
                <v-btn color="primary" :loading="loading" :disabled="!newPassword || newPassword !== confirmPassword || !resetSession" @click="resetPwd">提交</v-btn>
              </div>
            </v-card-text>
          </v-card>
        </v-stepper-window-item>
      </v-stepper-window>
    </v-stepper>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref } from 'vue';
import authApi from '@/api/auth';
import router from '@/router';

const step = ref(1);
const loading = ref(false);
const globalError = ref(null);
const snackbar = ref({ show: false, text: '', color: 'success' });

const email = ref('');
const requestId = ref(null);
const code = ref('');
const resetSession = ref(null);
const newPassword = ref('');
const confirmPassword = ref('');

const toast = (text, color='success') => { snackbar.value={ show:true, text, color }; };

async function sendCode() {
  globalError.value = null; loading.value = true;
  try {
    const resp = await authApi.requestPasswordCode(email.value);
    if (resp.data?.code === 0) {
      requestId.value = resp.data.data?.requestId || null;
      toast('验证码已发送，请查收邮箱');
      step.value = 2;
    } else {
      globalError.value = resp.data?.message || '发送失败';
    }
  } catch (e) {
    globalError.value = e?.response?.data?.message || e?.message || '发送失败';
  } finally { loading.value = false; }
}

async function verifyCode() {
  globalError.value = null; loading.value = true;
  try {
    const resp = await authApi.verifyPasswordCode({ email: email.value, code: code.value, requestId: requestId.value });
    if (resp.data?.code === 0) {
      resetSession.value = resp.data.data?.resetSession || null;
      toast('验证码验证成功');
      step.value = 3;
    } else {
      globalError.value = resp.data?.message || '验证失败';
    }
  } catch (e) {
    globalError.value = e?.response?.data?.message || e?.message || '验证失败';
  } finally { loading.value = false; }
}

async function resetPwd() {
  if (newPassword.value !== confirmPassword.value) { toast('两次密码不一致', 'error'); return; }
  globalError.value = null; loading.value = true;
  try {
    const resp = await authApi.resetPassword({ resetSession: resetSession.value, newPassword: newPassword.value });
    if (resp.data?.code === 0) {
      toast('密码重置成功，请使用新密码登录');
      setTimeout(() => router.push('/login'), 800);
    } else {
      globalError.value = resp.data?.message || '重置失败';
    }
  } catch (e) {
    globalError.value = e?.response?.data?.message || e?.message || '重置失败';
  } finally { loading.value = false; }
}
</script>

<style scoped>
</style>

