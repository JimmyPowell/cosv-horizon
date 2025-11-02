<template>
  <v-container class="my-8">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" to="/organizations" class="mb-4">
      返回我的组织
    </v-btn>

    <h1 class="text-h4 mb-8">创建新组织</h1>

    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card class="elevation-2">
          <v-card-title>组织信息</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="createOrganization">
              <v-text-field
                v-model="organization.name"
                label="组织名称"
                placeholder="例如：My Awesome Tech Inc."
                variant="outlined"
                class="mb-4"
                :rules="[rules.required, rules.nameLength]"
                counter="100"
              ></v-text-field>

              <v-textarea
                v-model="organization.description"
                label="组织描述"
                placeholder="简单介绍一下您的组织"
                variant="outlined"
                class="mb-4"
                :rules="[rules.maxLength(500)]"
                counter="500"
              ></v-textarea>

              <v-text-field
                v-model="organization.avatar"
                label="组织头像 URL (可选)"
                placeholder="https://example.com/avatar.png"
                variant="outlined"
                class="mb-4"
              ></v-text-field>

              <v-textarea
                v-model="organization.freeText"
                label="自由文本 (可选)"
                placeholder="可以填写更多关于组织的信息"
                variant="outlined"
                class="mb-4"
              ></v-textarea>

              <div class="mb-4">
                <v-switch v-model="organization.isPublic" label="公开可见"></v-switch>
                <v-switch v-model="organization.allowJoinRequest" :disabled="!organization.isPublic" label="允许公开申请加入"></v-switch>
                <v-switch v-model="organization.allowInviteLink" label="允许邀请链接/邀请码"></v-switch>
              </div>

              <v-btn type="submit" color="primary" :loading="loading" block size="large">
                创建组织
              </v-btn>
            </v-form>
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
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import organizationApi from '@/api/organization';

const router = useRouter();
const loading = ref(false);
const organization = reactive({
  name: '',
  description: '',
  freeText: '',
  avatar: '',
  isPublic: true,
  allowJoinRequest: false,
  allowInviteLink: true,
});
const snackbar = reactive({
  show: false,
  text: '',
  color: 'success'
});

const rules = {
  required: value => !!value || '此项为必填项',
  nameLength: value => (value && value.length >= 3) || '组织名称至少需要3个字符',
  maxLength: max => value => (value && value.length <= max) || `长度不能超过 ${max} 个字符`,
};

const createOrganization = async () => {
  if (!organization.name || organization.name.length < 3) {
    showSnackbar('请填写有效的组织名称', 'error');
    return;
  }

  loading.value = true;
  try {
    const response = await organizationApi.create({
      name: organization.name,
      description: organization.description,
      freeText: organization.freeText,
      avatar: organization.avatar,
      isPublic: organization.isPublic,
      allowJoinRequest: organization.allowJoinRequest,
      allowInviteLink: organization.allowInviteLink,
    });
    if (response.data && response.data.code === 0) {
      showSnackbar('组织创建成功，等待审核', 'success');
      router.push('/organizations');
    } else {
      showSnackbar(response.data.message || '创建失败', 'error');
    }
  } catch (error) {
    console.error('Failed to create organization:', error);
    const errorMessage = error.response?.data?.message || '创建失败';
    showSnackbar(errorMessage, 'error');
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
