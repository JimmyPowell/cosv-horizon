<template>
  <v-container class="my-8">
    <h1 class="text-h5 mb-6">我的组织邀请</h1>

    <v-card class="elevation-2">
      <v-card-text>
        <div v-if="loading" class="text-center py-8">
          <v-progress-circular indeterminate color="primary"></v-progress-circular>
        </div>

        <v-table v-else-if="invitations.length > 0">
          <thead>
            <tr>
              <th>组织</th>
              <th>邀请人</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="inv in invitations" :key="inv.inviteUuid">
              <td>{{ inv.orgName || '-' }}</td>
              <td>{{ inv.inviterName || '-' }}</td>
              <td>{{ inv.status }}</td>
              <td>
                <v-btn size="small" color="primary" variant="text" class="mr-2" :loading="actingId === inv.inviteUuid" @click="accept(inv.inviteUuid)">接受</v-btn>
                <v-btn size="small" color="error" variant="text" :loading="actingId === inv.inviteUuid" @click="reject(inv.inviteUuid)">拒绝</v-btn>
              </td>
            </tr>
          </tbody>
        </v-table>

        <div v-else class="text-center py-8 text-grey">暂无邀请</div>
      </v-card-text>
    </v-card>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import organizationApi from '@/api/organization';

const loading = ref(true);
const invitations = ref([]);
const actingId = ref(null);
const snackbar = reactive({ show: false, text: '', color: 'success' });

const fetchInvitations = async () => {
  loading.value = true;
  try {
    const resp = await organizationApi.myInvitations({ withTotal: false });
    if (resp.data && resp.data.code === 0) {
      invitations.value = resp.data.data.items || [];
    } else {
      show('获取邀请失败', 'error');
    }
  } catch (err) {
    show(err.response?.data?.message || '获取邀请失败', 'error');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchInvitations);

const accept = async (uuid) => {
  actingId.value = uuid;
  try {
    const resp = await organizationApi.acceptInvite(uuid);
    if (resp.data && resp.data.code === 0) {
      show('已接受邀请', 'success');
      fetchInvitations();
    } else {
      show(resp.data.message || '操作失败', 'error');
    }
  } catch (err) {
    show(err.response?.data?.message || '操作失败', 'error');
  } finally {
    actingId.value = null;
  }
};

const reject = async (uuid) => {
  actingId.value = uuid;
  try {
    const resp = await organizationApi.rejectInvite(uuid);
    if (resp.data && resp.data.code === 0) {
      show('已拒绝邀请', 'success');
      fetchInvitations();
    } else {
      show(resp.data.message || '操作失败', 'error');
    }
  } catch (err) {
    show(err.response?.data?.message || '操作失败', 'error');
  } finally {
    actingId.value = null;
  }
};

const show = (text, color = 'success') => {
  snackbar.text = text; snackbar.color = color; snackbar.show = true;
};
</script>

<style scoped>
</style>
