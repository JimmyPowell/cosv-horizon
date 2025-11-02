<template>
  <v-container class="my-8">
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">我的积分</h1>
      <v-spacer></v-spacer>
      <v-btn variant="text" prepend-icon="mdi-refresh" :loading="loading" @click="load">刷新</v-btn>
    </div>

    <v-row class="mb-4">
      <v-col cols="12" md="6">
        <v-card class="elevation-2">
          <v-card-text class="d-flex align-center justify-space-between">
            <div>
              <div class="text-caption text-grey">个人积分</div>
              <div class="text-h4 font-weight-bold">{{ summary.rating || 0 }}</div>
            </div>
            <div class="text-right">
              <div class="text-caption text-grey">全站排名</div>
              <div class="text-h6 font-weight-bold">{{ summary.rank ?? '-' }}</div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-card class="elevation-2">
      <v-card-title class="d-flex align-center">积分流水</v-card-title>
      <v-divider></v-divider>
      <v-card-text>
        <div v-if="loading" class="text-center py-8">
          <v-progress-circular indeterminate color="primary" />
        </div>
        <template v-else>
          <v-table>
            <thead>
              <tr>
                <th>时间</th>
                <th>变动</th>
                <th>原因</th>
                <th>引用</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="it in items" :key="it.uuid">
                <td>{{ formatDateTime(it.createdAt) }}</td>
                <td :class="{'text-green': it.delta>0, 'text-error': it.delta<0}">{{ it.delta }}</td>
                <td>{{ it.reason }}</td>
                <td>{{ it.refType }}: {{ it.refId }}</td>
              </tr>
            </tbody>
          </v-table>
          <div class="d-flex align-center justify-space-between mt-4">
            <span class="text-grey">共 {{ total }} 条，当前第 {{ page }} 页</span>
            <div>
              <v-btn class="mr-2" variant="outlined" :disabled="page<=1" @click="changePage(page-1)">上一页</v-btn>
              <v-btn variant="outlined" :disabled="page*size>=total" @click="changePage(page+1)">下一页</v-btn>
            </div>
          </div>
        </template>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import pointsApi from '@/api/points';

const authStore = useAuthStore();
const summary = ref({ rating: 0, rank: null });
const items = ref([]);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const loading = ref(false);

const load = async () => {
  loading.value = true;
  try {
    // summary
    const myUuid = authStore.user?.uuid;
    if (myUuid) summary.value = await pointsApi.getUserSummary(myUuid);
    // ledger
    const resp = await pointsApi.getMyPoints({ page: page.value, size: size.value });
    if (resp.data && resp.data.code === 0 && resp.data.data) {
      total.value = resp.data.data.total || 0;
      items.value = resp.data.data.items || [];
    }
  } finally { loading.value = false; }
};

const changePage = (p) => { page.value = p; load(); };
const formatDateTime = (iso) => {
  if (!iso) return '-';
  try { return new Date(iso).toLocaleString('zh-CN'); } catch { return iso; }
};

onMounted(load);
</script>

<style scoped>
</style>

