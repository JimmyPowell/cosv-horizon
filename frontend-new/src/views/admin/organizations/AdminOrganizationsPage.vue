<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">组织管理</h1>
      <v-spacer></v-spacer>
    </div>

    <admin-filter-bar v-model="q" placeholder="按组织名/UUID 搜索">
      <template #actions>
        <v-select v-model="status" :items="orgStatusOptions" label="状态" hide-details density="compact" clearable style="max-width: 160px" />
      </template>
    </admin-filter-bar>

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
      <template #item.uuid="{ item }">
        <UuidCell :uuid="item.uuid" :length="10" />
      </template>
      <template #item.name="{ item }">
        <div class="font-weight-medium">{{ item.name }}</div>
      </template>
      <template #item.description="{ item }">
        <div class="text-truncate-1">{{ item.description }}</div>
      </template>
      <template #item.actions="{ item }">
        <div class="d-flex justify-center ga-1">
          <v-tooltip text="管理" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="primary" :to="{ name: 'admin-organization-detail', params: { uuid: item.uuid } }">
                <v-icon size="20">mdi-cog-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
        </div>
      </template>
      <template #item.status="{ item }">
        <v-chip size="small" :color="orgStatusColor(item.status)" variant="tonal">{{ item.status }}</v-chip>
      </template>
      <template #no-data>
        <empty-state title="暂无组织" description="试试调整搜索/筛选条件" icon="mdi-account-group-outline" />
      </template>
    </admin-data-table>

    <!-- 单一入口：管理/详情。编辑弹窗移除，统一在详情页的设置中完成。 -->
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import AdminDataTable from '@/components/admin/table/AdminDataTable.vue';
import AdminFilterBar from '@/components/admin/filters/AdminFilterBar.vue';
import EmptyState from '@/components/admin/common/EmptyState.vue';
import { useServerTable } from '@/composables/useServerTable';
import { useDebouncedRef } from '@/composables/useDebouncedRef';
import adminOrgs from '@/api/admin/organizations';
import UuidCell from '@/components/admin/common/UuidCell.vue';

const headers = [
  { title: 'UUID', key: 'uuid', width: 140, sortable: false },
  { title: '名称', key: 'name', width: 200, sortable: false },
  { title: '描述', key: 'description', sortable: false },
  { title: '状态', key: 'status', width: 100, sortable: false },
  { title: '创建时间', key: 'dateCreated', width: 160, sortable: false },
  { title: '操作', key: 'actions', width: 150, sortable: false, align: 'center' },
];

const orgStatusOptions = ['ACTIVE','PENDING','REJECTED','SUSPENDED'];

const { loading, items, total, page, size, fetch } = useServerTable(async (p) => {
  const res = await adminOrgs.list({ q: debounced.value || undefined, status: status.value || undefined, page: p.page, size: p.size, withTotal: true });
  return res;
});

const { source: q, debounced } = useDebouncedRef('', 300);
const status = ref();

function onPage(p) { fetch({ page: p }); }
function onSize(s) { fetch({ size: s }); }
function orgStatusColor(s) { return s === 'ACTIVE' ? 'success' : (s === 'PENDING' ? 'amber' : (s === 'REJECTED' ? 'error' : 'grey')); }

watch([debounced, status], () => fetch({ page: 1 }));
onMounted(() => fetch({ page: 1 }));

// 仅保留“管理”入口，编辑在详情页完成
</script>

<style scoped>
.text-truncate-1 {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}
</style>
