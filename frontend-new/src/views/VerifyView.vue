<template>
  <div class="verify-container">
    <transition name="fade-zoom" appear>
      <v-card class="pa-8" width="450" flat>
        <v-card-title class="text-h4 text-center font-weight-bold mb-2">
          输入验证码
        </v-card-title>
        <v-card-subtitle class="text-center mb-8">
          已发送至您的邮箱，请注意查收
        </v-card-subtitle>
        <v-card-text>
          <v-otp-input
            ref="otpInput"
            v-model="otp"
            length="6"
            variant="outlined"
            @update:model-value="handleOtpUpdate"
          ></v-otp-input>
          <v-btn
            block
            color="primary"
            size="x-large"
            class="mt-4"
            :disabled="otp.length < 6"
            @click="router.push(`/profile-edit?email=${email}`)"
          >
            验证
          </v-btn>
          <div class="text-center mt-4">
            <a href="#" class="text-grey-darken-1 text-decoration-none">
              没有收到？重新发送
            </a>
          </div>
        </v-card-text>
      </v-card>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { useRouter } from 'vue-router';

const otp = ref('');
const otpInput = ref(null);
const router = useRouter();
const email = ref('example@domain.com'); // 这里应该是实际获取或传入的邮箱

const handleOtpUpdate = async (newValue) => {
  const oldValue = otp.value;
  otp.value = newValue;

  // 当从满格（6位）删除到5位时，Vuetify的默认行为是聚焦到第5个输入框。
  // 我们需要覆盖这个行为，将光标重新聚焦到最后一个（第6个）输入框。
  if (oldValue.length === 6 && newValue.length === 5) {
    // 使用 nextTick 等待DOM更新和组件内部逻辑执行完毕
    await nextTick();
    const inputs = otpInput.value.$el.querySelectorAll('input');
    if (inputs.length > 5) {
      // 强制将焦点设置到最后一个输入框
      inputs[5].focus();
    }
  }
};
</script>

<style scoped>
.verify-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px); /* Subtract app bar height */
  width: 100%;
}

/* Fade + Zoom Transition */
.fade-zoom-enter-active,
.fade-zoom-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-zoom-enter-from,
.fade-zoom-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* Custom styles for v-otp-input to highlight only the active field */
:deep(.v-otp-input .v-field--focused .v-field__outline) {
  --v-field-border-width: 2px;
  --v-field-border-opacity: 1;
  color: rgb(var(--v-theme-primary));
}

:deep(.v-otp-input .v-field .v-field__outline) {
  transition: none !important;
}
</style>
