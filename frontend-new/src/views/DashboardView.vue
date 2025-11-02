<template>
  <div class="dashboard-container">
    <v-container fluid>
      <!-- Hero Section -->
      <v-row class="hero-section align-center mb-6">
        <v-col>
          <h1 class="text-h5 font-weight-bold text-grey-darken-3">发现可能使您面临风险的漏洞</h1>
        </v-col>
      </v-row>

      <!-- Search and Main Content -->
      <v-row>
        <!-- Left Sidebar: Filters -->
        <v-col cols="12" md="3">
          <v-card flat class="pa-4 filter-card">
            <div class="d-flex align-center mb-4">
              <v-icon class="mr-2">mdi-filter-variant</v-icon>
              <span class="font-weight-bold">筛选器</span>
            </div>

            <!-- Filters Sections -->
            <v-list v-model:opened="openSections" class="filter-list" lines="one">
              <v-list-group value="languages">
                <template v-slot:activator="{ props }">
                  <v-list-item v-bind="props" title="编程语言"></v-list-item>
                </template>
                <v-list-item v-for="lang in languages" :key="lang.name" @click="lang.checked = !lang.checked" density="compact" class="filter-item">
                  <template v-slot:prepend>
                    <div class="custom-checkbox-box" :class="{ 'checked': lang.checked }">
                      <v-icon v-if="lang.checked" size="16" color="white">mdi-check</v-icon>
                    </div>
                  </template>
                  <v-list-item-title>{{ lang.label || lang.name }}</v-list-item-title>
                  <template v-slot:append>
                    <span class="text-grey-darken-1 text-caption">{{ lang.count }}</span>
                  </template>
                </v-list-item>
              </v-list-group>

              

              <v-list-group value="severity">
                <template v-slot:activator="{ props }">
                  <v-list-item v-bind="props" title="严重程度"></v-list-item>
                </template>
                <v-list-item v-for="level in severityLevels" :key="level.name" @click="level.checked = !level.checked" density="compact" class="filter-item">
                  <template v-slot:prepend>
                     <div class="custom-checkbox-box" :class="{ 'checked': level.checked, [`border-${level.color}`]: true }">
                      <v-icon v-if="level.checked" size="16" :color="level.checked ? 'white' : level.color">mdi-check</v-icon>
                    </div>
                  </template>
                  <v-list-item-title :class="`text-${level.color}`">{{ level.name }}</v-list-item-title>
                  <template v-slot:append>
                    <span class="text-grey-darken-1 text-caption">{{ level.count }}</span>
                  </template>
                </v-list-item>
              </v-list-group>
            </v-list>
          </v-card>
        </v-col>

        <!-- Right Content: Vulnerability List -->
        <v-col cols="12" md="9">
          <v-text-field
            v-model="searchTerm"
            placeholder="按编号/CVE/名称搜索"
            variant="outlined"
            prepend-inner-icon="mdi-magnify"
            class="mb-6"
            hide-details
          ></v-text-field>

          <v-card flat class="data-table-card">
            <v-data-table-server
              :headers="headers"
              :items="vulnerabilities"
              :items-length="total"
              :loading="loading"
              :page="page"
              :items-per-page="size"
              :sort-by="sortBy"
              hover
              class="elevation-1"
              @update:page="onUpdatePage"
              @update:items-per-page="onUpdateSize"
              @update:sort-by="onUpdateSort"
            >
              <template #item.vuln="{ item }">
                <div class="py-3">
                  <div class="d-flex align-start">
                    <v-chip :color="getSeverityColorClass(item.severityColor)" label size="small" class="mr-4 mt-1 font-weight-bold flex-shrink-0">{{ item.severityColor }}</v-chip>
                    <div>
                      <router-link :to="'/vulnerabilities/' + item.id" class="text-blue-darken-2 text-decoration-none font-weight-bold text-subtitle-1">{{ item.title }}</router-link>
                      <div class="text-grey-darken-1 text-caption mt-1">{{ item.identifier }}</div>
                    </div>
                  </div>
                </div>
              </template>
              <template #item.affects="{ item }">
                <div class="py-3">
                  <div class="text-body-1 font-weight-medium">{{ item.affects }}</div>
                  <div class="text-grey-darken-1 text-caption mt-1">{{ item.version }}</div>
                </div>
              </template>
              <template #item.type="{ item }">
                <div class="py-3">
                  <v-chip size="small" color="info" variant="tonal">{{ item.type }}</v-chip>
                </div>
              </template>
              <template #item.modified="{ item }">
                <div class="py-3">{{ item.modifiedFormatted }}</div>
              </template>
              <template #no-data>
                <div class="text-center py-8 text-grey-darken-1">暂无数据</div>
              </template>
            </v-data-table-server>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import vulnerabilityApi from '@/api/vulnerability';
import { detectKeyType } from '@/utils/searchKeyType';
import { useDebouncedRef } from '@/composables/useDebouncedRef';

const searchTerm = ref('');
const filterView = ref('all');
const openSections = ref(['languages', 'severity']);

// 分页和加载状态
const loading = ref(false);
const vulnerabilities = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const headers = [
  { title: '漏洞', key: 'vuln', sortable: false, width: '40%' },
  { title: '影响', key: 'affects', sortable: false, width: '25%' },
  { title: '类型', key: 'type', sortable: false, width: '15%' },
  { title: '发布时间', key: 'modified', sortable: true, width: '20%' },
];
const sortBy = ref([{ key: 'modified', order: 'desc' }]);

// 筛选器状态
const languages = ref([
  { name: "JAVA", label: "Java", count: 0, checked: false },
  { name: "PYTHON", label: "Python", count: 0, checked: false },
  { name: "JAVASCRIPT", label: "JavaScript", count: 0, checked: false },
  { name: "CPP", label: "C/C++", count: 0, checked: false },
  { name: "GO", label: "Go", count: 0, checked: false },
  { name: "RUST", label: "Rust", count: 0, checked: false },
  { name: "PHP", label: "PHP", count: 0, checked: false },
  { name: "RUBY", label: "Ruby", count: 0, checked: false },
  { name: "C", label: "C", count: 0, checked: false },
  { name: "OTHER", label: "其他", count: 0, checked: false },
]);



const severityLevels = ref([
  { name: "严重 (Critical)", count: 0, checked: false, color: "red-darken-2", min: 9.0, max: 10.0 },
  { name: "高危 (High)", count: 0, checked: false, color: "orange-darken-2", min: 7.0, max: 8.9 },
  { name: "中危 (Medium)", count: 0, checked: false, color: "amber-darken-2", min: 4.0, max: 6.9 },
  { name: "低危 (Low)", count: 0, checked: false, color: "green-darken-2", min: 0.0, max: 3.9 },
]);

const getSeverityColor = (severityScore) => {
  const score = Number(severityScore);
  if (score >= 9.0) return 'C';
  if (score >= 7.0) return 'H';
  if (score >= 4.0) return 'M';
  return 'L';
};

const getSeverityColorClass = (severityColor) => {
  switch (severityColor) {
    case 'C': return 'red-darken-2';
    case 'H': return 'orange-darken-2';
    case 'M': return 'amber-darken-2';
    case 'L': return 'green-darken-2';
    default: return 'grey-darken-2';
  }
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
};

// 计算选中的筛选条件
const selectedLanguages = computed(() =>
  languages.value.filter(l => l.checked).map(l => l.name)
);

const selectedSeverities = computed(() =>
  severityLevels.value.filter(s => s.checked)
);

// 防抖搜索
const { debounced: debouncedSearch } = useDebouncedRef(searchTerm.value, 300);
watch(searchTerm, (newVal) => {
  debouncedSearch.value = newVal;
});

// 获取漏洞列表：智能搜索（UUID/CVE/GHSA/ID-like 走 SQL；文本走 ES；带额外筛选则优先 SQL）
async function fetchVulnerabilities() {
  loading.value = true;
  try {
    const curSort = sortBy.value?.[0] || { key: 'modified', order: 'desc' };
    const effectiveSortKey = curSort.key === 'modified' ? 'modified' : (curSort.key === 'severity' ? 'severity' : 'modified');
    const base = { page: page.value, size: size.value, withTotal: true, sortBy: effectiveSortKey, sortOrder: curSort.order || 'desc' };
    const selectedLevels = severityLevels.value.filter(s => s.checked).map(s => {
      if (s.name.includes('Critical')) return 'CRITICAL'; if (s.name.includes('High')) return 'HIGH'; if (s.name.includes('Medium')) return 'MEDIUM'; if (s.name.includes('Low')) return 'LOW'; return null;
    }).filter(Boolean);
    const qRaw = debouncedSearch.value && debouncedSearch.value.trim();
    const hasSearch = !!qRaw;
    const keyType = hasSearch ? detectKeyType(qRaw).type : 'empty';

    let data;
    // UUID 精确查找
    if (keyType === 'uuid') {
      try {
        const r = await vulnerabilityApi.getByUuid(qRaw);
        const v = r?.data?.data?.vulnerability;
        const items = (v && v.status === 'ACTIVE') ? [v] : [];
        data = { items, total: items.length };
      } catch (e) {
        data = { items: [], total: 0 };
      }
    }
    // CVE/GHSA/ID-like 走 SQL 列表（identifierPrefix）
    else if (keyType === 'cve' || keyType === 'ghsa' || keyType === 'idlike') {
      const params = { ...base, status: 'ACTIVE', identifierPrefix: qRaw };
      if (selectedLanguages.value.length > 0) params.language = selectedLanguages.value[0];
      const r = await vulnerabilityApi.list(params);
      data = r?.data?.data || { items: [], total: 0 };
    }
    // 其余情况：若存在严重度筛选（或多语言），走高级搜索；否则根据是否有搜索词选择合适端点
    else if (selectedLevels.length > 0 || selectedLanguages.value.length > 1) {
      const params = { ...base, status: 'ACTIVE' };
      if (hasSearch) params.q = qRaw;
      if (selectedLanguages.value.length > 0) params.languages = selectedLanguages.value; // CSV 在 API 层归一
      if (selectedLevels.length > 0) params.severityLevels = selectedLevels; // CSV 在 API 层归一
      const r = await vulnerabilityApi.listSearch(params);
      data = r?.data?.data || { items: [], total: 0 };
    }
    else if (hasSearch) {
      // 普通文本且无额外筛选：全文检索
      const params = { ...base, status: 'ACTIVE', q: qRaw };
      const r = await vulnerabilityApi.listSearch(params);
      data = r?.data?.data || { items: [], total: 0 };
    } else {
      const params = { ...base, status: 'ACTIVE' };
      if (selectedLanguages.value.length > 0) params.language = selectedLanguages.value[0];
      const r = await vulnerabilityApi.list(params);
      data = r?.data?.data || { items: [], total: 0 };
    }

    // 转换数据格式
    vulnerabilities.value = (data.items || []).map(item => ({
      id: item.uuid,
      identifier: item.identifier,
      title: item.summary,
      severity: getSeverityLabel(item.severityNum),
      severityScore: item.severityNum,
      severityColor: getSeverityColor(item.severityNum),
      affects: item.affectedLabel || '-',
      version: typeof item.affectedCount === 'number' && item.affectedCount > 0 ? `共 ${item.affectedCount} 个包` : '-',
      type: item.language,
      modified: item.modified || item.published,
      modifiedFormatted: formatDate(item.modified || item.published),
    }));

    total.value = typeof data.total === 'number' ? data.total : (data.items?.length || 0);
    console.log('[Dashboard] Search result total/items =', total.value, (data.items || []).length);
  } catch (error) {
    console.error('获取漏洞列表失败:', error);
  } finally {
    loading.value = false;
  }
}

function getSeverityLabel(score) {
  const s = Number(score);
  if (s >= 9.0) return 'Critical';
  if (s >= 7.0) return 'High';
  if (s >= 4.0) return 'Medium';
  return 'Low';
}

// 分页/排序事件（与管理端体验一致）
function onUpdatePage(p) {
  page.value = p;
  fetchVulnerabilities();
}
function onUpdateSize(s) {
  size.value = s;
  page.value = 1;
  fetchVulnerabilities();
}
function onUpdateSort(sb) {
  sortBy.value = sb && sb.length ? sb : [{ key: 'modified', order: 'desc' }];
  fetchVulnerabilities();
}

// 监听筛选与搜索
watch([debouncedSearch, languages, severityLevels], () => { page.value = 1; fetchVulnerabilities(); }, { deep: true });

// 监听筛选条件变化
watch([selectedLanguages, selectedSeverities, debouncedSearch], () => { page.value = 1; fetchVulnerabilities(); });

// 初始化
onMounted(() => {
  fetchVulnerabilities();
});
</script>

<style scoped>
.dashboard-container {
  background-color: #f7f8fa;
  min-height: calc(100vh - 64px);
}
.hero-section {
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  padding: 8px 16px;
}
.filter-card, .data-table-card {
  border: 1px solid #e0e0e0 !important;
  border-radius: 8px !important;
}
.filter-list .v-list-item-title {
  font-size: 0.9rem !important;
  font-weight: 500;
  text-transform: uppercase;
}
.filter-item .v-list-item-title {
  font-size: 0.9rem !important;
  text-transform: none !important;
  font-weight: 400 !important;
}
.filter-item {
  min-height: 40px !important;
  padding-inline-start: 8px !important;
  cursor: pointer;
}
.custom-checkbox-box {
  width: 18px;
  height: 18px;
  border: 2px solid #bdbdbd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s, border-color 0.2s;
  margin-right: 12px;
}
.custom-checkbox-box.checked {
  background-color: #1976d2;
  border-color: #1976d2;
}
.custom-checkbox-box.border-red-darken-2.checked { background-color: #D32F2F; border-color: #D32F2F; }
.custom-checkbox-box.border-orange-darken-2.checked { background-color: #F57C00; border-color: #F57C00; }
.custom-checkbox-box.border-amber-darken-2.checked { background-color: #FFA000; border-color: #FFA000; }
.custom-checkbox-box.border-green-darken-2.checked { background-color: #388E3C; border-color: #388E3C; }

.custom-radio-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}
.data-table th {
  background-color: #f7f8fa;
  font-weight: bold !important;
  color: #424242 !important;
  text-transform: uppercase;
  font-size: 0.75rem !important;
}
.vuln-row {
  transition: background-color 0.2s ease;
}
.vuln-row:hover {
  background-color: #f5f5f5;
}
</style>
