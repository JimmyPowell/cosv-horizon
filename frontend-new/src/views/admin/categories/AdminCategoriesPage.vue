<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">分类管理</h1>
      <v-spacer></v-spacer>
    </div>

    <admin-filter-bar v-model="q" placeholder="搜索分类 (code/name)">
      <template #actions>
        <v-btn color="primary" @click="openCreate = true" prepend-icon="mdi-plus">新建分类</v-btn>
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
      <template #item.actions="{ item }">
        <div class="d-flex justify-center">
          <v-tooltip text="编辑" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="primary" @click="startEdit(item)">
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
      <template #item.description="{ item }">
        <div class="text-truncate-1">{{ item.description }}</div>
      </template>
    </admin-data-table>

    <confirm-dialog v-model="showConfirm" title="删除分类" :message="confirmMsg" @confirm="doDelete" />

    <v-dialog v-model="openCreate" max-width="520">
      <v-card>
        <v-card-title class="text-h6">新建分类</v-card-title>
        <v-card-text>
          <v-text-field v-model="form.code" label="代码" hide-details required />
          <v-text-field v-model="form.name" label="名称" hide-details required />
          <v-textarea v-model="form.description" label="描述" hide-details />
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="openCreate = false">取消</v-btn>
          <v-btn color="primary" :loading="creating" @click="createCategory">创建</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="openEdit" max-width="520">
      <v-card>
        <v-card-title class="text-h6">编辑分类</v-card-title>
        <v-card-text>
          <v-text-field v-model="editForm.code" label="代码" hide-details required />
          <v-text-field v-model="editForm.name" label="名称" hide-details required />
          <v-textarea v-model="editForm.description" label="描述" hide-details />
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="openEdit = false">取消</v-btn>
          <v-btn color="primary" :loading="updating" @click="updateCategory">保存</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useServerTable } from '@/composables/useServerTable';
import { useDebouncedRef } from '@/composables/useDebouncedRef';
import adminCategoryApi from '@/api/admin/categories';
import AdminDataTable from '@/components/admin/table/AdminDataTable.vue';
import AdminFilterBar from '@/components/admin/filters/AdminFilterBar.vue';
import ConfirmDialog from '@/components/admin/common/ConfirmDialog.vue';
import { useToast } from '@/stores/toast';

const headers = [
  { title: '代码', key: 'code', width: 180, sortable: false },
  { title: '名称', key: 'name', width: 200, sortable: false },
  { title: '描述', key: 'description', sortable: false },
  { title: '操作', key: 'actions', width: 100, sortable: false, align: 'center' },
];

const { loading, items, total, page, size, fetch } = useServerTable(async (p) => {
  return adminCategoryApi.list({ q: debounced.value || undefined, page: p.page, size: p.size, withTotal: true });
});

const { source: q, debounced } = useDebouncedRef('', 300);

function onPage(p) { fetch({ page: p }); }
function onSize(s) { fetch({ size: s }); }

watch(debounced, () => fetch({ page: 1 }));
onMounted(() => fetch({ page: 1 }));

// Create
const openCreate = ref(false);
const creating = ref(false);
const form = ref({ code: '', name: '', description: '' });
async function createCategory() {
  if (!form.value.code || !form.value.name) return;
  creating.value = true;
  try {
    await adminCategoryApi.create(form.value);
    openCreate.value = false;
    form.value = { code: '', name: '', description: '' };
    fetch({ page: 1 });
  } finally {
    creating.value = false;
  }
}

// Delete
const toDelete = ref(null);
const showConfirm = ref(false);
const confirmMsg = ref('确认删除该分类？');
function confirmDelete(item) { toDelete.value = item; confirmMsg.value = `确认删除分类 ${item.name} (${item.code})？`; showConfirm.value = true; }
async function doDelete() {
  if (!toDelete.value) return;
  const res = await adminCategoryApi.delete(toDelete.value.uuid);
  if (res?.code === 1013) {
    // need force delete
    await adminCategoryApi.forceDelete(toDelete.value.uuid, { dryRun: false });
  }
  toDelete.value = null;
  fetch();
}

// Edit
const openEdit = ref(false);
const updating = ref(false);
const editForm = ref({ uuid: '', code: '', name: '', description: '' });
const toast = useToast();
function startEdit(item) {
  editForm.value = { uuid: item.uuid, code: item.code, name: item.name, description: item.description || '' };
  openEdit.value = true;
}
async function updateCategory() {
  if (!editForm.value.uuid) return;
  updating.value = true;
  try {
    await adminCategoryApi.update(editForm.value.uuid, { code: editForm.value.code, name: editForm.value.name, description: editForm.value.description });
    openEdit.value = false;
    toast.success('分类已更新');
    fetch();
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '更新失败';
    toast.error(msg);
  } finally {
    updating.value = false;
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
