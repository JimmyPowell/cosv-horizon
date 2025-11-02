<template>
  <v-container class="my-8">
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5">发现公开组织</h1>
      <v-spacer></v-spacer>
      <v-text-field v-model="q" placeholder="搜索组织名或UUID" variant="outlined" density="comfortable" hide-details clearable style="max-width: 360px" @keydown.enter="search" />
      <v-btn class="ml-3" color="primary" @click="search">搜索</v-btn>
    </div>

    <v-card class="elevation-2">
      <v-card-text>
        <div v-if="loading" class="text-center py-8">
          <v-progress-circular indeterminate color="primary"></v-progress-circular>
        </div>

        <v-list v-else-if="items.length > 0">
          <v-list-item v-for="org in items" :key="org.uuid" lines="two" @click="go(org.uuid)">
            <template #prepend>
              <AppAvatar :name="org.name || '?'" :size="40" class="mr-3" />
            </template>
            <template #title>
              <div class="d-flex align-center">
                <span class="text-subtitle-1 font-weight-medium mr-2">{{ org.name }}</span>
                <v-icon v-if="org.isVerified" color="primary" size="18" class="mr-1">mdi-check-decagram</v-icon>
                <v-chip v-if="org.status && org.status !== 'ACTIVE'" size="x-small" :color="statusColor(org.status)" label class="ml-1">{{ org.status }}</v-chip>
              </div>
            </template>
            <template #subtitle>
              <span class="text-grey">{{ org.description || '暂无描述' }}</span>
            </template>
            <template #append>
              <v-chip size="small" :color="org.isPublic ? 'info' : 'grey'">{{ org.isPublic ? '公开' : '私有' }}</v-chip>
              <v-btn icon variant="text"><v-icon>mdi-chevron-right</v-icon></v-btn>
            </template>
          </v-list-item>
        </v-list>

        <div v-else class="text-center py-8 text-grey">暂无结果</div>

        <div class="d-flex align-center justify-space-between mt-4">
          <span class="text-grey">共 {{ total }} 条，当前第 {{ page }} 页</span>
          <div>
            <v-btn class="mr-2" variant="outlined" :disabled="page<=1" @click="changePage(page-1)">上一页</v-btn>
            <v-btn variant="outlined" :disabled="page*size>=total" @click="changePage(page+1)">下一页</v-btn>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import organizationApi from '@/api/organization';
import AppAvatar from '@/components/AppAvatar.vue';

const router = useRouter();
const q = ref('');
const loading = ref(false);
const items = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const snackbar = ref({ show: false, text: '', color: 'success' });

const fetch = async () => {
  loading.value = true;
  try {
    const resp = await organizationApi.searchPublicOrgs({ q: q.value, page: page.value, size: size.value, withTotal: true });
    if (resp.data && resp.data.code === 0) {
      items.value = resp.data.data.items || [];
      total.value = resp.data.data.total ?? items.value.length;
    } else {
      show(resp.data?.message || '搜索失败', 'error');
    }
  } catch (err) {
    show(err.response?.data?.message || '搜索失败', 'error');
  } finally { loading.value = false; }
};

const search = () => { page.value = 1; fetch(); };
const changePage = (p) => { page.value = p; fetch(); };
const go = (uuid) => router.push(`/organizations/${uuid}`);
const show = (text, color='success') => { snackbar.value = { show: true, text, color }; };

function statusColor(st) {
  switch (st) {
    case 'ACTIVE': return 'success';
    case 'SUSPENDED': return 'warning';
    case 'BANNED': return 'error';
    case 'PENDING': return 'warning';
    case 'REJECTED': return 'error';
    case 'DELETED': return 'grey';
    default: return 'grey';
  }
}

fetch();
</script>

<style scoped>
</style>
