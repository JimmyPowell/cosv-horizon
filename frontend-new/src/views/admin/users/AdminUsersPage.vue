<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">用户管理</h1>
      <v-spacer></v-spacer>
    </div>

    <admin-filter-bar v-model="q" placeholder="按用户名/邮箱搜索">
      <template #actions>
        <v-select v-model="role" :items="roleOptions" label="角色" hide-details density="compact" clearable style="max-width: 160px" class="mr-2" />
        <v-select v-model="status" :items="userStatusOptions" label="状态" hide-details density="compact" clearable style="max-width: 160px" />
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
      <template #item.actions="{ item }">
        <div class="d-flex justify-center ga-1">
          <v-tooltip text="设置角色/状态" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="primary" @click="openEdit(item)">
                <v-icon size="20">mdi-shield-account-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
          <v-tooltip text="编辑资料" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="primary" @click="openProfile(item)">
                <v-icon size="20">mdi-account-edit-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
          <v-tooltip text="重置密码" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon size="small" variant="text" color="error" @click="openReset(item)">
                <v-icon size="20">mdi-lock-reset</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
        </div>
      </template>
      <template #item.role="{ item }">
        <v-chip size="small" :color="item.role === 'ADMIN' ? 'primary' : (item.role === 'MODERATOR' ? 'amber' : 'grey')" variant="tonal">{{ item.role }}</v-chip>
      </template>
      <template #item.status="{ item }">
        <v-chip size="small" :color="statusColor(item.status)" variant="tonal">{{ item.status }}</v-chip>
      </template>
      
      <template #no-data>
        <empty-state title="暂无用户" description="试试调整搜索/筛选条件" icon="mdi-account-off-outline" />
      </template>
    </admin-data-table>

    <v-dialog v-model="editOpen" max-width="520">
      <v-card>
        <v-card-title class="text-h6">更新用户角色/状态</v-card-title>
        <v-card-text>
          <div class="mb-2 d-flex align-center">
            <v-avatar size="28" class="mr-2" v-if="current?.avatar"><img :src="current.avatar" alt="" /></v-avatar>
            <span>{{ current?.name }} • {{ current?.email }}</span>
          </div>
          <v-select v-model="edit.role" :items="roleOptions" label="角色" hide-details density="comfortable" class="mb-3" />
          <v-select v-model="edit.status" :items="userStatusOptions" label="状态" hide-details density="comfortable" />
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="editOpen = false">取消</v-btn>
          <v-btn color="primary" :loading="saving" @click="saveEdit">保存</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="profileOpen" max-width="640">
      <v-card>
        <v-card-title class="text-h6">编辑资料</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6"><v-text-field v-model="profile.name" label="用户名" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="profile.email" label="邮箱" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="profile.avatar" label="头像URL" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="profile.company" label="公司" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="profile.location" label="所在地" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="profile.gitHub" label="GitHub" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="profile.website" label="网站" hide-details /></v-col>
            <v-col cols="12"><v-textarea v-model="profile.freeText" label="简介（可选）" rows="3" hide-details /></v-col>
            <v-col cols="12" md="6"><v-text-field v-model="profile.realName" label="实名（可选）" hide-details /></v-col>
          </v-row>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="profileOpen = false">取消</v-btn>
          <v-btn color="primary" :loading="savingProfile" @click="saveProfile">保存</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="resetOpen" max-width="480">
      <v-card>
        <v-card-title class="text-h6">重置密码</v-card-title>
        <v-card-text>
          <div class="mb-2">{{ current?.name }} • {{ current?.email }}</div>
          <v-text-field v-model="newPassword" label="新密码" type="password" hide-details />
          <v-alert type="warning" variant="tonal" class="mt-3">重置后将吊销该用户所有已签发令牌。</v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="resetOpen = false">取消</v-btn>
          <v-btn color="error" :loading="resetting" @click="doReset">重置</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import AdminDataTable from '@/components/admin/table/AdminDataTable.vue';
import AdminFilterBar from '@/components/admin/filters/AdminFilterBar.vue';
import EmptyState from '@/components/admin/common/EmptyState.vue';
import { useServerTable } from '@/composables/useServerTable';
import { useDebouncedRef } from '@/composables/useDebouncedRef';
import adminUsers from '@/api/admin/users';
import UuidCell from '@/components/admin/common/UuidCell.vue';

const headers = [
  { title: 'UUID', key: 'uuid', width: 140, sortable: false },
  { title: '用户名', key: 'name', width: 180, sortable: false },
  { title: '邮箱', key: 'email', sortable: false },
  { title: '角色', key: 'role', width: 100, sortable: false },
  { title: '状态', key: 'status', width: 100, sortable: false },
  { title: '创建时间', key: 'createDate', width: 160, sortable: false },
  { title: '操作', key: 'actions', width: 200, sortable: false, align: 'center' },
];
const roleOptions = ['ADMIN','USER','MODERATOR'];
const userStatusOptions = ['CREATED','ACTIVE','INACTIVE','SUSPENDED'];

const { loading, items, total, page, size, fetch } = useServerTable(async (p) => {
  const res = await adminUsers.list({ q: debounced.value || undefined, role: role.value || undefined, status: status.value || undefined, page: p.page, size: p.size, withTotal: true });
  return res;
});

const { source: q, debounced } = useDebouncedRef('', 300);
const role = ref();
const status = ref();

function onPage(p) { fetch({ page: p }); }
function onSize(s) { fetch({ size: s }); }
function statusColor(s) { return s === 'ACTIVE' ? 'success' : (s === 'SUSPENDED' ? 'error' : 'grey'); }

watch([debounced, role, status], () => fetch({ page: 1 }));
onMounted(() => fetch({ page: 1 }));

// Edit dialog
const editOpen = ref(false);
const current = ref(null);
const edit = ref({ role: undefined, status: undefined });
const saving = ref(false);
function openEdit(item) {
  current.value = item;
  edit.value = { role: item.role, status: item.status };
  editOpen.value = true;
}
async function saveEdit() {
  if (!current.value) return;
  saving.value = true;
  try {
    await adminUsers.update(current.value.uuid, { role: edit.value.role, status: edit.value.status });
    editOpen.value = false;
    fetch();
  } finally {
    saving.value = false;
  }
}

// Profile dialog
const profileOpen = ref(false);
const profile = ref({});
const savingProfile = ref(false);
function openProfile(item) {
  current.value = item;
  profile.value = { name: item.name, email: item.email, avatar: item.avatar, company: item.company, location: item.location, gitHub: item.gitHub, website: item.website, freeText: item.freeText, realName: item.realName };
  profileOpen.value = true;
}
async function saveProfile() {
  if (!current.value) return;
  savingProfile.value = true;
  try {
    await adminUsers.updateProfile(current.value.uuid, profile.value);
    profileOpen.value = false;
    fetch();
  } finally {
    savingProfile.value = false;
  }
}

// Reset password
const resetOpen = ref(false);
const newPassword = ref('');
const resetting = ref(false);
function openReset(item) {
  current.value = item; newPassword.value = ''; resetOpen.value = true;
}
async function doReset() {
  if (!current.value || !newPassword.value) return;
  resetting.value = true;
  try {
    await adminUsers.resetPassword(current.value.uuid, newPassword.value);
    resetOpen.value = false;
  } finally { resetting.value = false; }
}
</script>

<style scoped>
/* Styles handled by global and component styles */
</style>
