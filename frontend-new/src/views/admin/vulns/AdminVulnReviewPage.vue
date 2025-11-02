<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">漏洞审核</h1>
      <v-spacer></v-spacer>
    </div>

    <admin-filter-bar v-model="q" placeholder="按编号前缀搜索 (identifierPrefix)">
      <template #actions>
        <v-select v-model="sortKey" :items="sortOptions" item-title="label" item-value="value" density="compact" hide-details style="max-width: 200px" class="mr-2" />
        <v-select v-model="sortDir" :items="dirOptions" density="compact" hide-details style="max-width: 120px" />
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
      @update:page="onPage"
      @update:size="onSize"
      @update:sortBy="onSortBy"
    >
      <template #item.identifier="{ item }">
        <router-link :to="{ name: 'admin-vulnerability-detail', params: { uuid: item.uuid }, query: { from: 'admin-vuln-review' } }" class="text-primary text-decoration-none">{{ item.identifier }}</router-link>
      </template>
      <template #item.severityNum="{ item }">
        <v-chip size="small" :color="severityColor(item.severityNum)" variant="tonal">{{ item.severityNum }}</v-chip>
      </template>
      <template #item.actions="{ item }">
        <div class="d-flex justify-center ga-1">
          <v-tooltip text="查看详情" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="primary" :to="{ name: 'admin-vulnerability-detail', params: { uuid: item.uuid }, query: { from: 'admin-vuln-review' } }">
                <v-icon size="20">mdi-eye-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
          <v-tooltip text="通过审核" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="success" @click="approve(item)">
                <v-icon size="20">mdi-check-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
          <v-tooltip text="拒绝" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="error" @click="reject(item)">
                <v-icon size="20">mdi-close-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
        </div>
      </template>
      <template #no-data>
        <div class="text-center py-12 text-grey">暂无待审核漏洞</div>
      </template>
    </admin-data-table>

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
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useToast } from '@/stores/toast';
import { useServerTable } from '@/composables/useServerTable';
import { useDebouncedRef } from '@/composables/useDebouncedRef';
import adminVulnApi from '@/api/admin/vulnerabilities';
import AdminDataTable from '@/components/admin/table/AdminDataTable.vue';
import AdminFilterBar from '@/components/admin/filters/AdminFilterBar.vue';

const headers = [
  { title: '编号', key: 'identifier', sortable: false, width: 140 },
  { title: '摘要', key: 'summary', sortable: false },
  { title: '严重性', key: 'severityNum', sortable: true, width: 100 },
  { title: '语言', key: 'language', sortable: false, width: 100 },
  { title: '提交时间', key: 'submitted', sortable: true, width: 160 },
  { title: '操作', key: 'actions', sortable: false, width: 180, align: 'center' },
];

const sortOptions = [
  { label: '按更新时间', value: 'modified' },
  { label: '按严重性', value: 'severity' },
];
const dirOptions = [ 'desc', 'asc' ];

const { loading, items, total, page, size, sortBy, fetch } = useServerTable(async (p) => {
  const res = await adminVulnApi.list({
    identifierPrefix: debounced.value || undefined,
    status: 'PENDING',
    sortBy: sortKey.value,
    sortOrder: sortDir.value,
    page: p.page,
    size: p.size,
    withTotal: true,
  });
  return res;
});

const { source: q, debounced } = useDebouncedRef('', 300);
const sortKey = ref('modified');
const sortDir = ref('desc');

function severityColor(s) {
  if (s >= 9) return 'error';
  if (s >= 7) return 'warning';
  if (s >= 4) return 'amber';
  return 'grey';
}

function onPage(p) { fetch({ page: p }); }
function onSize(s) { fetch({ size: s }); }
function onSortBy(sb) {
  const first = sb?.[0];
  if (first) { sortKey.value = first.key; sortDir.value = first.order || 'desc'; }
  fetch({ sortBy: sortKey.value, sortOrder: sortDir.value });
}

watch([debounced, sortKey, sortDir], () => { fetch({ page: 1, sortBy: sortKey.value, sortOrder: sortDir.value }); });
onMounted(() => fetch({ page: 1 }));

// Actions
const acting = ref(false);
const toast = useToast();
const current = ref(null);
const openReject = ref(false);
const rejectReason = ref('');
async function approve(item) {
  acting.value = true; current.value = item;
  try {
    await adminVulnApi.updateStatus(item.uuid, { status: 'ACTIVE' });
    toast.success('已通过审核');
    fetch();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '操作失败';
    toast.error(msg);
  } finally { acting.value = false; }
}
function reject(item) { current.value = item; rejectReason.value=''; openReject.value = true; }
async function doReject() {
  if (!current.value) return; acting.value = true;
  try {
    await adminVulnApi.updateStatus(current.value.uuid, { status: 'REJECTED', rejectReason: rejectReason.value });
    openReject.value = false;
    toast.success('已拒绝该漏洞');
    fetch();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '操作失败';
    toast.error(msg);
  } finally { acting.value = false; }
}
</script>

<style scoped>
</style>
