<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">漏洞管理</h1>
      <v-spacer></v-spacer>
    </div>

    <admin-filter-bar v-model="q" placeholder="搜索漏洞（摘要/UUID/别名）">
      <template #actions>
        <v-select v-model="status" :items="statusOptions" label="状态" hide-details density="compact" clearable style="max-width: 140px" class="mr-2" />
        <v-autocomplete v-model="category" :items="categoryOptions" label="分类" hide-details density="compact" clearable style="max-width: 160px" class="mr-2" />
        <v-autocomplete v-model="tagCode" :items="tagOptions" label="标签" hide-details density="compact" clearable style="max-width: 160px" class="mr-2" />
        <v-select v-model="submitterType" :items="submitterTypeOptions" label="提交者" hide-details density="compact" clearable style="max-width: 120px" class="mr-2" />
        <v-text-field v-model="submittedFrom" label="提交起" type="date" hide-details density="compact" style="max-width: 150px" class="mr-2" />
        <v-text-field v-model="submittedTo" label="提交止" type="date" hide-details density="compact" style="max-width: 150px" class="mr-2" />
        <v-text-field v-model="modifiedFrom" label="更新起" type="date" hide-details density="compact" style="max-width: 150px" class="mr-2" />
        <v-text-field v-model="modifiedTo" label="更新止" type="date" hide-details density="compact" style="max-width: 150px" class="mr-2" />
        <v-select v-model="sortKey" :items="sortOptions" item-title="label" item-value="value" density="compact" hide-details style="max-width: 160px" class="mr-2" />
        <v-select v-model="sortDir" :items="dirOptions" density="compact" hide-details style="max-width: 110px" />
        <v-btn color="primary" class="ml-2" prepend-icon="mdi-plus" @click="openCreate = true">新建漏洞</v-btn>
      </template>
    </admin-filter-bar>

    <admin-data-table
      :headers="headers"
      :items="items"
      :total="total"
      :loading="loading"
      :page="page"
      :size="size"
      :sort-by="sortBy"
      density="comfortable"
      @update:page="onPage"
      @update:size="onSize"
      @update:sortBy="onSortBy"
    >
      <template #item.identifier="{ item }">
        <router-link :to="{ name: 'admin-vulnerability-detail', params: { uuid: item.uuid }, query: { from: 'admin-vulnerabilities' } }" class="text-primary text-decoration-none">{{ item.identifier }}</router-link>
      </template>
      <template #item.uuid="{ item }">
        <UuidCell :uuid="item.uuid" :length="10" />
      </template>
      <template #item.summary="{ item }">
        <div class="text-truncate-1">{{ item.summary }}</div>
      </template>
      <template #item.status="{ item }">
        <v-chip size="small" :color="statusChipColor(item.status)" variant="tonal">{{ item.status }}</v-chip>
      </template>
      <template #item.actions="{ item }">
        <div class="d-flex justify-center ga-1">
          <v-tooltip text="查看详情" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="primary" :to="{ name: 'admin-vulnerability-detail', params: { uuid: item.uuid }, query: { from: 'admin-vulnerabilities' } }">
                <v-icon size="20">mdi-eye-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
          <v-tooltip v-if="item.status === 'PENDING'" text="通过" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="success" @click="approve(item)">
                <v-icon size="20">mdi-check-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
          <v-tooltip v-if="item.status === 'PENDING'" text="拒绝" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="error" @click="reject(item)">
                <v-icon size="20">mdi-close-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
          <v-tooltip text="编辑" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="primary" @click="openEdit(item)">
                <v-icon size="20">mdi-pencil-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
          <v-tooltip text="删除" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="error" @click="confirmDelete(item)">
                <v-icon size="20">mdi-delete-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
        </div>
      </template>
      <template #item.severityNum="{ item }">
        <v-chip size="small" :color="severityColor(item.severityNum)" variant="tonal">{{ item.severityNum }}</v-chip>
      </template>
    </admin-data-table>

    <!-- Create Dialog -->
    <v-dialog v-model="openCreate" max-width="720">
      <v-card>
        <v-card-title class="text-h6">新建漏洞</v-card-title>
        <v-card-text>
          <v-alert type="info" variant="tonal" density="compact" class="mb-3">
            此表单页仅提供必选字段，强烈建议您使用导航栏中的漏洞提交页面进行提交。
          </v-alert>
          <v-row>
            <v-col cols="12" md="6"><v-text-field v-model="form.summary" label="摘要" hide-details required /></v-col>
            <v-col cols="12" md="3"><v-text-field v-model.number="form.severityNum" type="number" step="0.1" min="0" max="10" label="严重性" hide-details required /></v-col>
            <v-col cols="12" md="3"><v-text-field v-model="form.language" label="语言（如 Java/Python）" hide-details required /></v-col>
            <v-col cols="12"><v-textarea v-model="form.details" label="详情" hide-details rows="5" /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="form.categoryCode" label="分类代码（可选）" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="tagCodesInput" label="标签代码（逗号分隔，可选）" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="form.organizationUuid" label="组织UUID（可选）" hide-details /></v-col>
          </v-row>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="openCreate = false">取消</v-btn>
          <v-btn color="primary" :loading="creating" @click="createVuln">创建</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Reject Dialog -->
    <v-dialog v-model="openReject" max-width="520">
      <v-card>
        <v-card-title class="text-h6">拒绝漏洞</v-card-title>
        <v-card-text>
          <div class="mb-2">{{ current?.identifier }} • {{ current?.summary }}</div>
          <v-textarea v-model="rejectReason" label="拒绝原因（可选）" hide-details rows="3" />
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="openReject = false">取消</v-btn>
          <v-btn color="error" :loading="acting" @click="doReject">拒绝</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Edit Dialog -->
    <v-dialog v-model="openEditDialog" max-width="720">
      <v-card>
        <v-card-title class="text-h6">编辑漏洞</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6"><v-text-field v-model="editForm.summary" label="摘要" hide-details required /></v-col>
            <v-col cols="12" md="3"><v-text-field v-model.number="editForm.severityNum" type="number" step="0.1" min="0" max="10" label="严重性" hide-details required /></v-col>
            <v-col cols="12" md="3"><v-text-field v-model="editForm.language" label="语言" hide-details required /></v-col>
            <v-col cols="12"><v-textarea v-model="editForm.details" label="详情" hide-details rows="5" /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="editForm.categoryCode" label="分类代码（可选）" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="editForm.status" label="状态（ACTIVE/FIXED/REJECTED/PENDING）" hide-details /></v-col>
          </v-row>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="openEditDialog = false">取消</v-btn>
          <v-btn color="primary" :loading="savingEdit" @click="saveVulnEdit">保存</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
  
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue';
import { useServerTable } from '@/composables/useServerTable';
import { useDebouncedRef } from '@/composables/useDebouncedRef';
import adminVulnApi from '@/api/admin/vulnerabilities';
import AdminDataTable from '@/components/admin/table/AdminDataTable.vue';
import AdminFilterBar from '@/components/admin/filters/AdminFilterBar.vue';
import UuidCell from '@/components/admin/common/UuidCell.vue';
import adminTagApi from '@/api/admin/tags';
import adminCategoryApi from '@/api/admin/categories';
import { detectKeyType } from '@/utils/searchKeyType';

const headers = [
  { title: '编号', key: 'identifier', sortable: false, width: 140 },
  { title: 'UUID', key: 'uuid', sortable: false, width: 140 },
  { title: '摘要', key: 'summary', sortable: false },
  { title: '严重性', key: 'severityNum', sortable: true, width: 100 },
  { title: '语言', key: 'language', sortable: false, width: 100 },
  { title: '状态', key: 'status', sortable: false, width: 100 },
  { title: '更新时间', key: 'modified', sortable: true, width: 160 },
  { title: '操作', key: 'actions', sortable: false, width: 240, align: 'center' },
];

const sortOptions = [
  { label: '按更新时间', value: 'modified' },
  { label: '按严重性', value: 'severity' },
];
const dirOptions = [ 'desc', 'asc' ];

const { loading, items, total, page, size, sortBy, fetch } = useServerTable(async (p) => {
  const qRaw = debounced.value && debounced.value.trim();
  const hasQ = !!qRaw;
  const hasTime = !!(submittedFrom.value || submittedTo.value || modifiedFrom.value || modifiedTo.value);
  const hasTag = !!tagCode.value;
  const keyType = hasQ ? detectKeyType(qRaw).type : 'empty';

  const common = {
    status: status.value || undefined,
    category: category.value || undefined,
    sortBy: sortKey.value,
    sortOrder: sortDir.value,
    page: p.page,
    size: p.size,
    withTotal: true,
  };

  // 1) UUID 命中：直接精确查询
  if (keyType === 'uuid') {
    try {
      const data = await adminVulnApi.get(qRaw);
      const v = data?.vulnerability;
      let arr = [];
      if (v) {
        // 按筛选做一次前端过滤
        let ok = true;
        if (common.status && v.status !== common.status) ok = false;
        if (common.category && v.categoryCode !== common.category) ok = false;
        if (tagCode.value && !(Array.isArray(v.tagCodes) && v.tagCodes.includes(tagCode.value))) ok = false;
        if (submitterType.value && v.submitterType !== submitterType.value) ok = false;
        if (ok) arr = [v];
      }
      return { items: arr, total: arr.length, page: 1, size: arr.length || 1 };
    } catch (_) {
      return { items: [], total: 0, page: 1, size: p.size };
    }
  }

  // 2) CVE/GHSA/其他 ID-like：走 SQL 列表（identifierPrefix）
  if (keyType === 'cve' || keyType === 'ghsa' || keyType === 'idlike') {
    const res = await adminVulnApi.list({
      ...common,
      identifierPrefix: qRaw,
      tagCode: tagCode.value || undefined,
      submittedFrom: submittedFrom.value || undefined,
      submittedTo: submittedTo.value || undefined,
      modifiedFrom: modifiedFrom.value || undefined,
      modifiedTo: modifiedTo.value || undefined,
    });
    let filtered = res.items || [];
    if (submitterType.value) filtered = filtered.filter(i => i.submitterType === submitterType.value);
    return { ...res, items: filtered, total: filtered.length };
  }

  // 3) 普通文本：优先全文 ES；若包含时间/标签筛选，则走 SQL 并传 q
  if (hasQ && !hasTime && !hasTag) {
    const res = await adminVulnApi.search({ q: qRaw, ...common });
    let filtered = res.items || [];
    if (submitterType.value) filtered = filtered.filter(i => i.submitterType === submitterType.value);
    if (tagCode.value) filtered = filtered.filter(i => Array.isArray(i.tagCodes) && i.tagCodes.includes(tagCode.value));
    return { ...res, items: filtered, total: filtered.length };
  }

  // 4) 走 SQL 列表并传 q（支持更多筛选）
  const res = await adminVulnApi.list({
    ...common,
    q: hasQ ? qRaw : undefined,
    tagCode: tagCode.value || undefined,
    submittedFrom: submittedFrom.value || undefined,
    submittedTo: submittedTo.value || undefined,
    modifiedFrom: modifiedFrom.value || undefined,
    modifiedTo: modifiedTo.value || undefined,
  });
  let filtered = res.items || [];
  if (submitterType.value) filtered = filtered.filter(i => i.submitterType === submitterType.value);
  return { ...res, items: filtered, total: filtered.length };
});

const { source: q, debounced } = useDebouncedRef('', 300);
const sortKey = ref('modified');
const sortDir = ref('desc');
const statusOptions = ['PENDING','ACTIVE','REJECTED','FIXED'];
const status = ref();
const category = ref();
const categoryOptions = ref([]);
const tagCode = ref();
const tagOptions = ref([]);
const submitterType = ref();
const submitterTypeOptions = ['ORG','USER'];
const submittedFrom = ref('');
const submittedTo = ref('');
const modifiedFrom = ref('');
const modifiedTo = ref('');

function severityColor(s) {
  if (s >= 9) return 'error';
  if (s >= 7) return 'warning';
  if (s >= 4) return 'amber';
  return 'grey';
}

function statusChipColor(status) {
  const colorMap = {
    'ACTIVE': 'success',
    'PENDING': 'warning',
    'REJECTED': 'error',
    'FIXED': 'info'
  };
  return colorMap[status] || 'grey';
}

function onPage(p) { fetch({ page: p }); }
function onSize(s) { fetch({ size: s }); }
function onSortBy(sb) {
  const first = sb?.[0];
  if (first) {
    sortKey.value = first.key;
    sortDir.value = first.order || 'desc';
  }
  fetch({ sortBy: sortKey.value, sortOrder: sortDir.value });
}

watch([debounced, status, category, tagCode, submitterType, submittedFrom, submittedTo, modifiedFrom, modifiedTo, sortKey, sortDir], () => {
  fetch({ page: 1, sortBy: sortKey.value, sortOrder: sortDir.value });
});

onMounted(async () => {
  // 预取分类/标签选项
  try {
    const [cats, tags] = await Promise.all([
      adminCategoryApi.list({ page: 1, size: 200, withTotal: false }),
      adminTagApi.list({ page: 1, size: 200, withTotal: false }),
    ]);
    categoryOptions.value = (cats.items || []).map(c => c.code);
    tagOptions.value = (tags.items || []).map(t => t.code);
  } catch (e) {}
  fetch({ page: 1 });
});

// Actions
const acting = ref(false);
const current = ref(null);
async function approve(item) {
  acting.value = true; current.value = item;
  try { await adminVulnApi.updateStatus(item.uuid, { status: 'ACTIVE' }); fetch(); } finally { acting.value = false; }
}

const openReject = ref(false);
const rejectReason = ref('');
function reject(item) { current.value = item; rejectReason.value=''; openReject.value = true; }
async function doReject() {
  if (!current.value) return; acting.value = true;
  try { await adminVulnApi.updateStatus(current.value.uuid, { status: 'REJECTED', rejectReason: rejectReason.value }); openReject.value = false; fetch(); } finally { acting.value = false; }
}

// Edit & Delete
const openEditDialog = ref(false);
const editForm = ref({ summary: '', details: '', severityNum: undefined, language: '', categoryCode: '', status: '' });
const savingEdit = ref(false);
function openEdit(item) {
  current.value = item;
  editForm.value = { summary: item.summary, details: item.details, severityNum: item.severityNum, language: item.language, categoryCode: item.categoryCode || '', status: item.status };
  openEditDialog.value = true;
}
async function saveVulnEdit() {
  if (!current.value) return;
  savingEdit.value = true;
  try {
    await adminVulnApi.update(current.value.uuid, editForm.value);
    openEditDialog.value = false;
    fetch();
  } finally { savingEdit.value = false; }
}
function confirmDelete(item) {
  if (!confirm(`确认删除 ${item.identifier} ？`)) return;
  acting.value = true; current.value = item;
  adminVulnApi.delete(item.uuid).then(() => fetch()).finally(() => acting.value = false);
}

// Create
const openCreate = ref(false);
const creating = ref(false);
const form = ref({ summary: '', details: '', severityNum: 5.0, language: '', categoryCode: '', organizationUuid: '' });
const tagCodesInput = ref('');
async function createVuln() {
  creating.value = true;
  try {
    const payload = { ...form.value };
    const tagCodes = tagCodesInput.value.split(',').map(s => s.trim()).filter(Boolean);
    if (tagCodes.length) payload.tagCodes = tagCodes;
    if (!payload.organizationUuid) delete payload.organizationUuid;
    if (!payload.categoryCode) delete payload.categoryCode;
    await adminVulnApi.create(payload);
    openCreate.value = false;
    form.value = { summary: '', details: '', severityNum: 5.0, language: '', categoryCode: '', organizationUuid: '' };
    tagCodesInput.value = '';
    fetch({ page: 1 });
  } finally {
    creating.value = false;
  }
}
</script>

<style scoped>
.text-truncate-1 {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}
</style>
