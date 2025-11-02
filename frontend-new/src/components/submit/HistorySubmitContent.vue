<template>
  <div>
    <!-- Filters -->
    <v-card class="mb-4" elevation="0">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3">
            <v-select
              v-model="filterStatus"
              :items="statusOptions"
              label="状态"
              clearable
              density="comfortable"
              hide-details
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="filterLanguage"
              :items="languageOptions"
              item-title="label"
              item-value="value"
              label="语言"
              clearable
              density="comfortable"
              hide-details
            />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field
              v-model="searchQuery"
              label="搜索编号或摘要"
              prepend-inner-icon="mdi-magnify"
              clearable
              density="comfortable"
              hide-details
            />
          </v-col>
          <v-col cols="12" md="2" class="d-flex align-center">
            <v-btn
              color="primary"
              variant="outlined"
              prepend-icon="mdi-refresh"
              @click="loadVulnerabilities"
              :loading="loading"
              block
            >
              刷新
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Data Table -->
    <v-card>
      <v-data-table
        :headers="headers"
        :items="items"
        :loading="loading"
        :items-per-page="size"
        :page="page"
        hide-default-footer
        class="elevation-1"
      >
        <template #item.identifier="{ item }">
          <router-link
            :to="`/vulnerabilities/${item.uuid}`"
            class="text-primary text-decoration-none font-weight-medium"
          >
            {{ item.identifier }}
          </router-link>
        </template>

        <template #item.summary="{ item }">
          <div class="text-truncate" style="max-width: 300px;">
            {{ item.summary }}
          </div>
        </template>

        <template #item.severityNum="{ item }">
          <v-chip
            size="small"
            :color="severityColor(item.severityNum)"
            variant="tonal"
          >
            {{ item.severityNum }}
          </v-chip>
        </template>

        <template #item.language="{ item }">
          <v-chip size="small" color="info" variant="tonal">
            {{ item.language }}
          </v-chip>
        </template>

        <template #item.status="{ item }">
          <v-chip
            size="small"
            :color="statusColor(item.status)"
            variant="flat"
          >
            {{ statusLabel(item.status) }}
          </v-chip>
        </template>

        <template #item.submitterType="{ item }">
          <v-chip
            size="small"
            :color="item.submitterType === 'ORG' ? 'purple' : 'blue'"
            variant="outlined"
          >
            {{ item.submitterType === 'ORG' ? '组织' : '个人' }}
          </v-chip>
        </template>

        <template #item.submitted="{ item }">
          {{ formatDate(item.submitted) }}
        </template>

        <template #item.actions="{ item }">
          <div class="d-flex gap-2">
            <v-btn size="small" color="primary" variant="text" :to="`/vulnerabilities/${item.uuid}`">查看</v-btn>
            <v-btn size="small" color="warning" variant="text" :to="`/vulnerabilities/${item.uuid}/edit`">编辑</v-btn>
            <v-btn size="small" color="error" variant="text" @click="confirmDelete(item)">删除</v-btn>
          </div>
        </template>

        <template #no-data>
          <div class="text-center py-8">
            <v-icon size="64" color="grey-lighten-1">mdi-database-off</v-icon>
            <p class="text-h6 text-grey mt-4">暂无数据</p>
          </div>
        </template>
      </v-data-table>

      <!-- Pagination -->
      <v-divider />
      <div class="d-flex align-center justify-end pa-4">
        <v-pagination
          v-model="page"
          :length="totalPages"
          :total-visible="7"
          @update:model-value="loadVulnerabilities"
        />
        <v-select
          v-model="size"
          :items="[10, 20, 30, 50]"
          density="compact"
          hide-details
          class="ml-4"
          style="max-width: 120px"
          @update:model-value="onSizeChange"
        />
      </div>
    </v-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useToast } from '@/stores/toast';
import vulnerabilityApi from '@/api/vulnerability';

// Table headers
const headers = [
  { title: '编号', key: 'identifier', sortable: false },
  { title: '摘要', key: 'summary', sortable: false },
  { title: '严重性', key: 'severityNum', sortable: false },
  { title: '语言', key: 'language', sortable: false },
  { title: '状态', key: 'status', sortable: false },
  { title: '提交身份', key: 'submitterType', sortable: false },
  { title: '提交时间', key: 'submitted', sortable: false },
  { title: '操作', key: 'actions', sortable: false, align: 'center' },
];

// Language options
const languageOptions = [
  { label: 'Java', value: 'JAVA' },
  { label: 'Python', value: 'PYTHON' },
  { label: 'JavaScript', value: 'JAVASCRIPT' },
  { label: 'PHP', value: 'PHP' },
  { label: 'Go', value: 'GO' },
  { label: 'Rust', value: 'RUST' },
  { label: 'C', value: 'C' },
  { label: 'C++', value: 'CPP' },
  { label: '其他', value: 'OTHER' },
];

// Status options
const statusOptions = [
  { title: '待审核', value: 'PENDING' },
  { title: '已发布', value: 'ACTIVE' },
  { title: '已拒绝', value: 'REJECTED' },
  { title: '已修复', value: 'FIXED' },
];

// Data
const items = ref([]);
const loading = ref(false);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const toast = useToast();

// Filters
const filterStatus = ref(null);
const filterLanguage = ref(null);
const searchQuery = ref('');

// Computed
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));

// Methods
async function loadVulnerabilities() {
  loading.value = true;
  try {
    const params = {
      page: page.value,
      size: size.value,
      withTotal: true,
    };
    // 仅查看“我的提交”（个人提交 + 我是管理员的组织名义提交）
    params.mine = true;

    if (filterStatus.value) params.status = filterStatus.value;
    if (filterLanguage.value) params.language = filterLanguage.value;
    if (searchQuery.value) params.identifierPrefix = searchQuery.value;

    const res = await vulnerabilityApi.list(params);
    const data = res?.data?.data || {};
    items.value = data.items || [];
    total.value = data.total || 0;
  } catch (e) {
    console.error('Failed to load vulnerabilities:', e);
    items.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function onSizeChange() {
  page.value = 1;
  loadVulnerabilities();
}

function severityColor(severity) {
  if (severity >= 9.0) return 'error';
  if (severity >= 7.0) return 'warning';
  if (severity >= 4.0) return 'info';
  return 'success';
}

function statusColor(status) {
  const colors = {
    PENDING: 'warning',
    ACTIVE: 'success',
    REJECTED: 'error',
    FIXED: 'info',
  };
  return colors[status] || 'grey';
}

function statusLabel(status) {
  const labels = {
    PENDING: '待审核',
    ACTIVE: '已发布',
    REJECTED: '已拒绝',
    FIXED: '已修复',
  };
  return labels[status] || status;
}

function formatDate(dateString) {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

// Watch filters
watch([filterStatus, filterLanguage, searchQuery], () => {
  page.value = 1;
  loadVulnerabilities();
});

// Load on mount
onMounted(() => {
  loadVulnerabilities();
});

// Delete logic
async function confirmDelete(item) {
  const ok = window.confirm(`确认删除 ${item.identifier} 吗？此操作不可恢复。`);
  if (!ok) return;
  try {
    await vulnerabilityApi.delete(item.uuid);
    toast.success('删除成功');
    // 回到提交页面：当前即在“提交漏洞”页->我的提交 Tab，刷新列表即可
    await loadVulnerabilities();
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || '删除失败');
  }
}
</script>

<style scoped>
.gap-2 {
  gap: 8px;
}
</style>
