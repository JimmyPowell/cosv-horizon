<template>
  <v-container class="my-6">
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">漏洞列表</h1>
      <v-spacer />
      <v-btn color="primary" variant="flat" prepend-icon="mdi-plus" :to="{ name: 'vulnerability-submit' }">提交漏洞</v-btn>
    </div>

    <v-card class="mb-4">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="4">
            <v-text-field v-model="q" label="按编号前缀搜索 (identifierPrefix)" hide-details clearable density="comfortable" prepend-inner-icon="mdi-magnify" />
          </v-col>
          <v-col cols="12" md="3">
            <v-select v-model="status" :items="statusOptions" label="状态" hide-details clearable density="comfortable" />
          </v-col>
          <v-col cols="12" md="3">
            <v-select v-model="language" :items="languageOptions" item-title="label" item-value="value" label="语言" hide-details clearable density="comfortable" />
          </v-col>
          <v-col cols="12" md="2">
            <v-select v-model="category" :items="categoryItems" item-title="nameWithCode" item-value="code" label="分类" hide-details clearable density="comfortable" :loading="loading.categories" />
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-card>
      <v-data-table
        :headers="headers"
        :items="items"
        :loading="loadingTable"
        :page.sync="page"
        :items-per-page.sync="size"
        :items-length="total"
        item-key="uuid"
        class="elevation-1"
      >
        <template #item.identifier="{ item }">
          <router-link :to="`/vulnerabilities/${item.uuid}`" class="text-primary text-decoration-none">{{ item.identifier }}</router-link>
        </template>
        <template #item.severityNum="{ item }">
          <v-chip size="x-small" :color="severityColor(item.severityNum)" variant="tonal">{{ item.severityNum }}</v-chip>
        </template>
        <template #item.language="{ item }">
          <v-chip size="x-small" color="info" variant="tonal">{{ item.language }}</v-chip>
        </template>
        <template #item.status="{ item }">
          <v-chip size="x-small" :color="statusColor(item.status)" variant="flat">{{ item.status }}</v-chip>
        </template>
        <template #bottom>
          <div class="d-flex align-center justify-end pa-4">
            <v-pagination v-model="page" :length="Math.max(1, Math.ceil(total / size))" total-visible="7" />
            <v-select class="ml-4" style="max-width: 120px" v-model="size" :items="[10,20,30,50]" density="compact" hide-details />
          </div>
        </template>
      </v-data-table>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import vulnerabilityApi from '@/api/vulnerability';
import categoryApi from '@/api/category';
import { useDebouncedRef } from '@/composables/useDebouncedRef';

const headers = [
  { title: '编号', key: 'identifier' },
  { title: '摘要', key: 'summary' },
  { title: '严重性', key: 'severityNum' },
  { title: '语言', key: 'language' },
  { title: '状态', key: 'status' },
  { title: '更新时间', key: 'modified' },
];

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

const statusOptions = ['PENDING','ACTIVE','REJECTED','FIXED'];

// Table state
const items = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const loadingTable = ref(false);

// Filters
const { source: q, debounced: qd } = useDebouncedRef('', 300);
// 默认只显示 ACTIVE 状态
const status = ref('ACTIVE');
const language = ref();
const category = ref();

// Category options
const categoryItems = ref([]);
const loading = ref({ categories: false });

function severityColor(s) {
  const v = Number(s);
  if (v >= 9) return 'error';
  if (v >= 7) return 'warning';
  if (v >= 4) return 'amber';
  return 'grey';
}
function statusColor(st) {
  switch (st) {
    case 'ACTIVE': return 'success';
    case 'PENDING': return 'warning';
    case 'REJECTED': return 'error';
    case 'FIXED': return 'info';
    default: return 'grey';
  }
}

async function fetchList() {
  loadingTable.value = true;
  try {
    const params = {
      page: page.value,
      size: size.value,
      withTotal: true,
    };
    if (qd.value && qd.value.trim()) params.identifierPrefix = qd.value.trim();
    if (status.value) params.status = status.value;
    if (language.value) params.language = language.value;
    if (category.value) params.category = category.value;
    const res = await vulnerabilityApi.list(params);
    const data = res?.data?.data || {};
    items.value = data.items || [];
    total.value = typeof data.total === 'number' ? data.total : 0;
  } finally {
    loadingTable.value = false;
  }
}

async function loadCategories() {
  loading.value.categories = true;
  try {
    const res = await categoryApi.list({ page: 1, size: 200, withTotal: false });
    const list = res?.data?.data?.items || [];
    categoryItems.value = list.map(it => ({ code: it?.code, nameWithCode: it?.name && it?.code ? `${it.name}（${it.code}）` : (it?.name || it?.code || '') })).filter(i => i.code);
  } finally {
    loading.value.categories = false;
  }
}

onMounted(() => { loadCategories(); fetchList(); });

watch([qd, status, language, category, page, size], () => { fetchList(); });
</script>

<style scoped>
</style>
