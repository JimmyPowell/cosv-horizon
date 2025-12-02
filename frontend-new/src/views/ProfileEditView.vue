<template>
  <v-container class="my-8">
    <h1 class="text-h4 mb-8">个人资料设置</h1>
    <v-row>
      <v-col cols="12" md="4">
        <!-- Left Side: Avatar -->
        <v-card class="text-center pa-6 elevation-2">
          <AppAvatar :name="profile.name || profile.email || 'U'" :size="150" class="mb-4 elevation-2" />
          <h3 class="text-h6">{{ profile.name }}</h3>
          <p class="text-grey">{{ profile.email }}</p>
          <v-btn class="mt-4" disabled title="暂不支持">
            更换头像（暂不支持）
          </v-btn>
        </v-card>
      </v-col>
      <v-col cols="12" md="8">
        <!-- Right Side: Tabs for Info and Password -->
        <v-card class="elevation-2">
          <v-tabs v-model="tab" color="primary" bg-color="white">
            <v-tab value="info">基本信息</v-tab>
          </v-tabs>
          <v-card-text class="pa-6 form-content">
            <v-window v-model="tab">
              <v-window-item value="info">
                <v-form @submit.prevent="handleUpdateProfile">
                  <v-row class="mt-0">
                    <v-col cols="12" sm="6">
                      <v-text-field v-model="profile.name" label="用户名" variant="outlined"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field v-model="profile.realName" label="真实姓名" variant="outlined"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field v-model="profile.company" label="公司" variant="outlined"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field v-model="profile.location" label="所在地" variant="outlined"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field v-model="profile.gitHub" label="GitHub" variant="outlined"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field v-model="profile.website" label="个人网站" variant="outlined"></v-text-field>
                    </v-col>
                    <v-col cols="12">
                      <div class="markdown-editor-section">
                        <label class="text-subtitle-2 mb-2 d-block">
                          个人简介（支持 Markdown 格式）
                          <v-tooltip location="top">
                            <template v-slot:activator="{ props }">
                              <v-icon v-bind="props" size="16" class="ml-1">mdi-information-outline</v-icon>
                            </template>
                            <div style="max-width: 300px;">
                              支持 Markdown 语法：<br>
                              # 标题、**粗体**、*斜体*<br>
                              [链接](url)、`代码`<br>
                              - 列表、> 引用等
                            </div>
                          </v-tooltip>
                        </label>

                        <v-tabs v-model="markdownTab" class="mb-2">
                          <v-tab value="edit">
                            <v-icon class="mr-1">mdi-pencil</v-icon>
                            编辑
                          </v-tab>
                          <v-tab value="preview">
                            <v-icon class="mr-1">mdi-eye</v-icon>
                            预览
                          </v-tab>
                        </v-tabs>

                        <v-window v-model="markdownTab">
                          <v-window-item value="edit">
                            <v-textarea
                              v-model="profile.freeText"
                              variant="outlined"
                              rows="10"
                              placeholder="在这里输入你的个人简介，支持 Markdown 格式..."
                              class="markdown-textarea"
                            ></v-textarea>
                          </v-window-item>

                          <v-window-item value="preview">
                            <v-card variant="outlined" class="markdown-preview pa-4" min-height="280">
                              <div v-if="profile.freeText" class="markdown-content" v-html="previewMarkdown"></div>
                              <div v-else class="text-grey text-center py-8">
                                <v-icon size="48" class="mb-2">mdi-text-box-outline</v-icon>
                                <p>暂无内容，请在编辑标签页输入</p>
                              </div>
                            </v-card>
                          </v-window-item>
                        </v-window>

                        <div class="text-caption text-grey mt-2">
                          <v-icon size="14">mdi-lightbulb-outline</v-icon>
                          提示：使用 Markdown 可以让你的简介更加丰富和美观
                        </div>
                      </div>
                    </v-col>
                  </v-row>

                  <!-- 操作按钮 -->
                  <div class="mt-4">
                    <v-btn type="submit" color="primary" :loading="loading" class="mr-4">保存更改</v-btn>
                    <v-btn @click="cancelEdit" variant="outlined">取消</v-btn>
                  </div>
                </v-form>
              </v-window-item>
            </v-window>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import userApi from '@/api/user';
import authApi from '@/api/auth';
import AppAvatar from '@/components/AppAvatar.vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';

const route = useRoute();
const router = useRouter();

const tab = ref('info');
const fileInput = ref(null);
const avatarPreview = ref(null);
const uploadingAvatar = ref(false);
const loading = ref(false);
const markdownTab = ref('edit');
let originalProfile = {};

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true,
  headerIds: true,
  mangle: false,
});

// Markdown 预览
const previewMarkdown = computed(() => {
  if (!profile.value.freeText) return '';

  try {
    const rawHtml = marked.parse(profile.value.freeText);
    const cleanHtml = DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: [
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'p', 'br', 'hr',
        'strong', 'em', 'del', 'code', 'pre',
        'ul', 'ol', 'li',
        'a', 'img',
        'blockquote',
        'table', 'thead', 'tbody', 'tr', 'th', 'td'
      ],
      ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class', 'target', 'rel']
    });
    return cleanHtml;
  } catch (err) {
    console.error('Markdown rendering error:', err);
    return profile.value.freeText;
  }
});

const profile = ref({
  name: '', realName: '', email: '', company: '',
  location: '', gitHub: '', website: '', freeText: '', avatar: '',
});

const canChangePassword = computed(() => {
  const email = profile.value?.email || '';
  if (!email) return false;
  return !/noreply\.github\.com$/i.test(email.trim());
});

const passwordData = reactive({ code: '', newPassword: '', confirmPassword: '' });
const snackbar = reactive({ show: false, text: '', color: 'success' });

const fetchUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo();
    if (response.data && response.data.code === 0 && response.data.data) {
      const userData = response.data.data.user || response.data.data;
      
      // To ensure reactivity, we create a new object with all the fields
      // and assign it to the .value of the ref.
      profile.value = {
        name: userData.name || '',
        realName: userData.realName || '',
        email: userData.email || '',
        company: userData.company || '',
        location: userData.location || '',
        gitHub: userData.gitHub || '',
        website: userData.website || '',
        freeText: userData.freeText || '',
        avatar: userData.avatar || '',
      };

      // Set the initial state for the cancel functionality
      originalProfile = JSON.parse(JSON.stringify(profile.value));
    } else {
      showSnackbar(response.data?.message || '获取用户信息失败', 'error');
    }
  } catch (err) {
    console.error('Fetch user info error:', err);
    showSnackbar(err.response?.data?.message || '获取用户信息失败', 'error');
  }
};

onMounted(fetchUserInfo);

const triggerFileUpload = () => fileInput.value.click();

const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (e) => avatarPreview.value = e.target.result;
  reader.readAsDataURL(file);

  uploadingAvatar.value = true;
  try {
    const response = await userApi.uploadAvatar(file);
    if (response.data.code === "200") {
      profile.value.avatar = response.data.data.avatarUrl;
      showSnackbar('头像上传成功', 'success');
    } else {
      showSnackbar(response.data.message || '头像上传失败', 'error');
      avatarPreview.value = null;
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '头像上传失败', 'error');
    avatarPreview.value = null;
  } finally {
    uploadingAvatar.value = false;
  }
};

const handleUpdateProfile = async () => {
  loading.value = true;
  try {
    const response = await userApi.updateUserInfo(profile.value);
    if (response.data.code === 0) {
      showSnackbar('个人信息更新成功', 'success');
      // 更新原始数据
      originalProfile = JSON.parse(JSON.stringify(profile.value));
      // 延迟返回个人中心页面
      setTimeout(() => {
        router.push('/profile');
      }, 1500);
    } else {
      showSnackbar(response.data.message || '更新失败', 'error');
    }
  } catch (err) {
    console.error('Update profile error:', err);
    showSnackbar(err.response?.data?.message || '更新失败', 'error');
  } finally {
    loading.value = false;
  }
};

const cancelEdit = () => {
  // 恢复原始数据
  profile.value = JSON.parse(JSON.stringify(originalProfile));
  // 返回个人中心页面
  router.push('/profile');
};

// 使用后端提供的“忘记密码”流程在个人中心完成密码重置（基于邮箱验证码）
const pwdRequestId = ref(null);
const pwdResetSession = ref(null);

const sendPasswordCode = async () => {
  if (!profile.value.email) {
    showSnackbar('当前账户未获取到邮箱，无法发送验证码', 'error');
    return;
  }
  loading.value = true;
  try {
    const response = await authApi.requestPasswordCode(profile.value.email);
    if (response.data.code === 0) {
      pwdRequestId.value = response.data.data.requestId;
      showSnackbar('验证码已发送至邮箱', 'success');
    } else {
      showSnackbar(response.data.message || '发送验证码失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '发送验证码失败', 'error');
  } finally {
    loading.value = false;
  }
};

const handleChangePassword = async () => {
  if (passwordData.newPassword !== passwordData.confirmPassword) {
    showSnackbar('新密码两次输入不一致', 'error');
    return;
  }
  if (!pwdRequestId.value) {
    showSnackbar('请先发送验证码到邮箱', 'error');
    return;
  }
  if (!passwordData.code) {
    showSnackbar('请输入邮箱验证码', 'error');
    return;
  }
  loading.value = true;
  try {
    // 先验证验证码获取 resetSession
    if (!pwdResetSession.value) {
      const verifyResp = await authApi.verifyPasswordCode({
        email: profile.value.email,
        code: passwordData.code,
        requestId: pwdRequestId.value,
      });
      if (verifyResp.data.code !== 0) {
        showSnackbar(verifyResp.data.message || '验证码验证失败', 'error');
        loading.value = false;
        return;
      }
      pwdResetSession.value = verifyResp.data.data.resetSession;
    }
    // 提交新密码
    const resetResp = await authApi.resetPassword({
      resetSession: pwdResetSession.value,
      newPassword: passwordData.newPassword,
    });
    if (resetResp.data.code === 0) {
      showSnackbar('密码修改成功', 'success');
      passwordData.newPassword = '';
      passwordData.confirmPassword = '';
      passwordData.code = '';
      pwdResetSession.value = null;
      pwdRequestId.value = null;
    } else {
      showSnackbar(resetResp.data.message || '密码修改失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '密码修改失败', 'error');
  } finally {
    loading.value = false;
  }
};

const showSnackbar = (text, color = 'success') => {
  snackbar.text = text;
  snackbar.color = color;
  snackbar.show = true;
};
</script>

<style scoped>
/* 确保表单内容区域有足够的对比度和空间 */
.form-content {
  background-color: #ffffff;
  overflow: visible !important;
}

/* 修复 v-row 的负 margin 导致的 label 被裁剪问题 */
:deep(.v-row) {
  margin-top: 0 !important;
}

/* 确保输入框有足够的空间显示 label */
:deep(.v-text-field),
:deep(.v-textarea) {
  margin-bottom: 4px;
}

/* 确保 v-window 和 v-window-item 不会裁剪内容 */
:deep(.v-window),
:deep(.v-window-item) {
  overflow: visible !important;
}

/* 确保 v-card-text 不会裁剪溢出的内容（如浮动的 label） */
:deep(.v-card-text) {
  overflow: visible !important;
}

/* 确保输入框的 label 清晰可见 - 这是最重要的修复 */
:deep(.v-text-field .v-label),
:deep(.v-textarea .v-label),
:deep(.v-field .v-label) {
  color: rgba(0, 0, 0, 0.87) !important;
  opacity: 1 !important;
  font-weight: 500 !important;
}

/* 聚焦时的 label 颜色 */
:deep(.v-field--focused .v-label) {
  color: #1867C0 !important;
}

/* 确保输入框内的文字清晰 */
:deep(.v-field__field) {
  color: #000000;
}

:deep(.v-field__input) {
  color: rgba(0, 0, 0, 0.87);
}

/* 确保输入框的边框清晰 */
:deep(.v-field--variant-outlined .v-field__outline) {
  color: rgba(0, 0, 0, 0.38);
}

:deep(.v-field--variant-outlined.v-field--focused .v-field__outline) {
  color: #1867C0;
  --v-field-border-width: 2px;
}

/* 确保 placeholder 文本可见 */
:deep(.v-field__input::placeholder) {
  color: rgba(0, 0, 0, 0.38);
  opacity: 1;
}

/* 确保标签页有良好的对比度 */
:deep(.v-tabs) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

:deep(.v-tab) {
  color: rgba(0, 0, 0, 0.6);
  text-transform: none;
  font-weight: 500;
}

:deep(.v-tab--selected) {
  color: #1867C0;
  font-weight: 600;
}

/* 确保非编辑模式下的文本也清晰 */
.text-body-1 p {
  color: rgba(0, 0, 0, 0.87);
}

.font-weight-bold {
  color: rgba(0, 0, 0, 0.87);
}

/* Markdown 编辑器样式 */
.markdown-editor-section {
  width: 100%;
}

.markdown-textarea :deep(textarea) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.markdown-preview {
  min-height: 280px;
  background-color: #fafafa;
}

/* Markdown 内容样式 */
.markdown-content {
  line-height: 1.6;
  color: #24292f;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-content :deep(h1) {
  font-size: 2em;
  border-bottom: 1px solid #d0d7de;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid #d0d7de;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h3) {
  font-size: 1.25em;
}

.markdown-content :deep(p) {
  margin-bottom: 16px;
}

.markdown-content :deep(code) {
  background-color: rgba(175, 184, 193, 0.2);
  padding: 0.2em 0.4em;
  border-radius: 6px;
  font-size: 85%;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.markdown-content :deep(pre) {
  background-color: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin-bottom: 16px;
  padding-left: 2em;
}

.markdown-content :deep(li) {
  margin-bottom: 4px;
}

.markdown-content :deep(a) {
  color: #0969da;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #d0d7de;
  padding-left: 16px;
  color: #57606a;
  margin-bottom: 16px;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #d0d7de;
  padding: 8px 12px;
}

.markdown-content :deep(th) {
  background-color: #f6f8fa;
  font-weight: 600;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
}

.markdown-content :deep(strong) {
  font-weight: 600;
}

.markdown-content :deep(em) {
  font-style: italic;
}

.markdown-content :deep(del) {
  text-decoration: line-through;
}
</style>
