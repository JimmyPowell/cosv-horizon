<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">组织审核</h1>
      <v-spacer></v-spacer>
    </div>

    <admin-filter-bar v-model="q" placeholder="按组织名/UUID 搜索" />

    <admin-data-table
      :headers="headers"
      :items="items"
      :total="total"
      :loading="loading"
      :page="page"
      :size="size"
      :sort-by="[]"
      @update:page="onPage"
      @update:size="onSize"
    >
      <template #item.status="{ item }">
        <v-chip size="small" color="amber" variant="tonal">{{ item.status }}</v-chip>
      </template>
      <template #item.actions="{ item }">
        <div class="d-flex justify-center ga-1">
          <v-tooltip text="查看详情" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="primary" :to="{ name: 'admin-organization-detail', params: { uuid: item.uuid } }">
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
              <v-btn v-bind="props" icon size="small" variant="text" color="error" @click="openReject(item)">
                <v-icon size="20">mdi-close-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
        </div>
      </template>
      <template #no-data>
        <div class="text-center py-12 text-grey">暂无待审核组织</div>
      </template>
    </admin-data-table>

    <v-dialog v-model="rejectOpen" max-width="520">
      <v-card>
        <v-card-title class="text-h6">拒绝组织</v-card-title>
        <v-card-text>
          <div class="mb-2">{{ current?.name }} • {{ current?.uuid }}</div>
          <v-textarea v-model="rejectReason" label="拒绝原因（可选）" hide-details rows="3" />
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="rejectOpen=false">取消</v-btn>
          <v-btn color="error" :loading="acting" @click="doReject">拒绝</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useServerTable } from '@/composables/useServerTable';
import { useDebouncedRef } from '@/composables/useDebouncedRef';
import adminOrgs from '@/api/admin/organizations';
import AdminDataTable from '@/components/admin/table/AdminDataTable.vue';
import AdminFilterBar from '@/components/admin/filters/AdminFilterBar.vue';

const headers = [
  { title: 'UUID', key: 'uuid', width: 140, sortable: false },
  { title: '名称', key: 'name', width: 200, sortable: false },
  { title: '描述', key: 'description', sortable: false },
  { title: '状态', key: 'status', width: 100, sortable: false },
  { title: '创建时间', key: 'dateCreated', width: 160, sortable: false },
  { title: '操作', key: 'actions', width: 180, sortable: false, align: 'center' },
];

const { loading, items, total, page, size, fetch } = useServerTable(async (p) => {
  const res = await adminOrgs.list({ q: debounced.value || undefined, status: 'PENDING', page: p.page, size: p.size, withTotal: true });
  return res;
});

const { source: q, debounced } = useDebouncedRef('', 300);
function onPage(p) { fetch({ page: p }); }
function onSize(s) { fetch({ size: s }); }

watch([debounced], () => fetch({ page: 1 }));
onMounted(() => fetch({ page: 1 }));

const acting = ref(false);
const current = ref(null);
const rejectOpen = ref(false);
const rejectReason = ref('');
async function approve(item) {
  acting.value = true; current.value = item;
  try { await adminOrgs.update(item.uuid, { status: 'ACTIVE' }); fetch(); } finally { acting.value = false; }
}
function openReject(item) { current.value = item; rejectReason.value=''; rejectOpen.value = true; }
async function doReject() {
  if (!current.value) return; acting.value = true;
  try { await adminOrgs.update(current.value.uuid, { status: 'REJECTED', rejectReason: rejectReason.value }); rejectOpen.value = false; fetch(); } finally { acting.value = false; }
}
</script>

<style scoped>
</style>

