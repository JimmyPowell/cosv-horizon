<template>
  <v-container class="my-8">
    <h1 class="text-h4 mb-8">我的组织</h1>
    
    <v-row>
      <v-col cols="12">
        <v-card class="elevation-2">
          <v-toolbar flat color="white">
            <v-toolbar-title>组织列表</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-btn class="mr-3" variant="outlined" prepend-icon="mdi-key" @click="showApplyCodeDialog = true">使用邀请码加入</v-btn>
            <v-btn class="mr-3" variant="outlined" prepend-icon="mdi-magnify" to="/organizations/explore">浏览公开组织</v-btn>
            <v-btn color="primary" prepend-icon="mdi-plus" to="/organizations/create">创建组织</v-btn>
          </v-toolbar>

          <v-tabs v-model="filterTab" grow>
            <v-tab value="all">所有组织</v-tab>
            <v-tab value="created">我创建的</v-tab>
            <v-tab value="joined">我加入的</v-tab>
          </v-tabs>
          
          <v-divider></v-divider>
          
          <v-card-text v-if="loading" class="text-center py-8">
            <v-progress-circular indeterminate color="primary"></v-progress-circular>
          </v-card-text>
          
          <v-list v-else-if="filteredOrganizations.length > 0">
            <v-list-item
              v-for="organization in filteredOrganizations"
              :key="organization.uuid"
              lines="two"
              @click="goToOrganization(organization.uuid)"
            >
              <template #prepend>
                <AppAvatar :name="organization.name || '?'" :size="40" class="mr-3" />
              </template>

              <template #title>
                <div class="d-flex align-center">
                  <span class="text-subtitle-1 font-weight-medium mr-2">{{ organization.name }}</span>
                  <v-icon v-if="organization.isVerified" color="primary" size="18" class="mr-1">mdi-check-decagram</v-icon>
                </div>
              </template>

              <template #subtitle>
                <span class="text-grey">{{ organization.description || '暂无描述' }}</span>
              </template>

              <template #append>
                <v-chip :color="getStatusColor(organization.status)" size="small" class="mr-4">
                  {{ organization.status }}
                </v-chip>
                <v-btn variant="text" icon="mdi-chevron-right"></v-btn>
              </template>
            </v-list-item>
          </v-list>
          
          <v-card-text v-else class="text-center py-8">
            <v-icon size="64" color="grey" class="mb-4">mdi-account-group-outline</v-icon>
            <div class="text-body-1 text-grey">{{ emptyStateMessage }}</div>
            <v-btn v-if="filterTab !== 'joined'" color="primary" class="mt-4" prepend-icon="mdi-plus" to="/organizations/create">创建组织</v-btn>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
  <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.text }}
    </v-snackbar>

    <!-- 使用邀请码加入 对话框 -->
    <v-dialog v-model="showApplyCodeDialog" max-width="520">
      <v-card>
        <v-card-title class="text-h6">使用邀请码加入组织</v-card-title>
        <v-card-text>
          <v-text-field v-model="inviteCode" label="邀请码" placeholder="请输入邀请码" variant="outlined" hide-details class="mb-3"></v-text-field>
          <v-textarea v-model="applyMessage" label="备注（可选）" variant="outlined" hide-details rows="3"></v-textarea>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="showApplyCodeDialog = false">取消</v-btn>
          <v-btn color="primary" :loading="applyingByCode" :disabled="!inviteCode" @click="applyByCode">提交申请</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import organizationApi from '@/api/organization';
import AppAvatar from '@/components/AppAvatar.vue';

const router = useRouter();
const loading = ref(true);
const organizations = ref([]);
const filterTab = ref('all');
const snackbar = reactive({
  show: false,
  text: '',
  color: 'success'
});

// 邀请码加入
const showApplyCodeDialog = ref(false);
const inviteCode = ref('');
const applyMessage = ref('');
const applyingByCode = ref(false);

const filteredOrganizations = computed(() => {
  if (filterTab.value === 'created') {
    // 我创建的：管理员角色
    return organizations.value.filter(org => org.role === 'ADMIN');
  }
  if (filterTab.value === 'joined') {
    // 我加入的：包含我创建的（ADMIN 也属于成员）
    return organizations.value;
  }
  return organizations.value;
});

const emptyStateMessage = computed(() => {
  switch (filterTab.value) {
    case 'created':
      return '您还没有创建任何组织';
    case 'joined':
      return '您还没有加入任何组织';
    default:
      return '您目前没有加入或创建任何组织';
  }
});

const getStatusColor = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'SUSPENDED':
      return 'warning';
    case 'BANNED':
      return 'error';
    case 'CLOSED':
    case 'REJECTED':
      return 'error';
    case 'DELETED':
      return 'grey';
    default:
      return 'grey';
  }
};

const fetchOrganizations = async () => {
  loading.value = true;
  try {
    const response = await organizationApi.listMine();
    if (response.data && response.data.code === 0) {
      const items = response.data.data.items || [];
      organizations.value = items;
    } else {
      showSnackbar(response.data.message || '获取组织列表失败', 'error');
    }
  } catch (error) {
    console.error('Failed to fetch organizations:', error);
    const errorMessage = error.response?.data?.message || '获取组织列表失败';
    showSnackbar(errorMessage, 'error');
  } finally {
    loading.value = false;
  }
};

const goToOrganization = (uuid) => {
  router.push({ name: 'organization-detail', params: { id: uuid } });
};

onMounted(fetchOrganizations);

const showSnackbar = (text, color = 'success') => {
  snackbar.text = text;
  snackbar.color = color;
  snackbar.show = true;
};

const applyByCode = async () => {
  if (!inviteCode.value) return;
  applyingByCode.value = true;
  try {
    const code = String(inviteCode.value).trim().toUpperCase();
    const resp = await organizationApi.applyJoinByCode({ code, message: applyMessage.value || undefined });
    if (resp.data && resp.data.code === 0) {
      showSnackbar('申请已提交，等待管理员审批', 'success');
      showApplyCodeDialog.value = false;
      inviteCode.value = '';
      applyMessage.value = '';
    } else {
      showSnackbar(resp.data?.message || '提交失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '提交失败', 'error');
  } finally {
    applyingByCode.value = false;
  }
};
</script>

<style scoped>
/* Add any specific styles if needed */
</style>
