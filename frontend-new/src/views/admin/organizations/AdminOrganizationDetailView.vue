<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">组织详情</h1>
      <v-spacer></v-spacer>
      <v-btn variant="outlined" @click="fetchDetails" :loading="loading" prepend-icon="mdi-refresh">刷新</v-btn>
    </div>

    <div v-if="loading" class="text-center py-12">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <div v-else>
      <v-row>
        <v-col cols="12" md="8">
          <v-card class="mb-4">
            <v-card-title class="d-flex align-center"><v-icon class="mr-2">mdi-information-outline</v-icon>组织信息</v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <div class="mb-2"><strong>名称：</strong>{{ org.name }}</div>
              <div class="mb-2"><strong>UUID：</strong><code>{{ org.uuid }}</code></div>
              <div class="mb-2"><strong>状态：</strong><v-chip size="small" :color="orgStatusColor(org.status)" variant="tonal">{{ org.status }}</v-chip></div>
              <div class="mb-2"><strong>认证：</strong><v-chip size="small" :color="org.isVerified ? 'success' : 'grey'" variant="tonal">{{ org.isVerified ? '已认证' : '未认证' }}</v-chip></div>
              <div class="mb-2"><strong>描述：</strong>{{ org.description || '-' }}</div>
              <div v-if="org.freeText" class="mt-4">
                <div class="text-subtitle-2 mb-1">详细介绍（Markdown）</div>
                <div v-html="renderedFreeText" class="markdown-body"></div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="4">
          <v-card class="mb-4">
            <v-card-title>统计</v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <div class="mb-2"><strong>成员数：</strong>{{ stats.memberCount }}</div>
              <div class="mb-2"><strong>漏洞数：</strong>{{ stats.vulnCount }}</div>
            </v-card-text>
          </v-card>
          <v-card>
            <v-card-title>设置</v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <v-select v-model="edit.status" :items="orgStatusOptions" label="状态" hide-details density="comfortable" class="mb-3" />
              <v-switch v-model="edit.isVerified" inset color="primary" label="已认证"></v-switch>
              <v-switch v-model="edit.isPublic" inset color="secondary" label="公开组织"></v-switch>
              <v-switch v-model="edit.allowJoinRequest" inset color="secondary" label="允许加入申请"></v-switch>
              <v-switch v-model="edit.allowInviteLink" inset color="secondary" label="允许邀请链接"></v-switch>
              <v-textarea v-model="edit.rejectReason" label="拒绝原因（可选）" hide-details rows="2" class="mt-2" />
              <div class="mt-3">
                <v-btn color="primary" :loading="saving" @click="save">保存</v-btn>
              </div>
            </v-card-text>
          </v-card>
          <v-card class="mt-4">
            <v-card-title>组织操作</v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <div class="d-flex flex-wrap gap-2">
                <v-btn color="warning" variant="tonal" class="mr-2 mb-2" @click="doSuspend" :loading="acting">暂停</v-btn>
                <v-btn color="success" variant="tonal" class="mr-2 mb-2" @click="doRestore" :loading="acting">恢复</v-btn>
                <v-btn color="orange-darken-2" variant="tonal" class="mr-2 mb-2" @click="doDisband" :loading="acting">解散</v-btn>
                <v-btn color="error" variant="tonal" class="mb-2" @click="doDelete" :loading="acting">删除</v-btn>
              </div>
              <div class="text-caption text-grey-darken-1 mt-2">暂停/解散将撤销邀请链接并过期待处理邀请/申请；删除需在暂停后执行。</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="6">
          <v-card>
            <v-card-title>成员列表</v-card-title>
            <v-divider></v-divider>
            <v-table>
              <thead><tr><th>姓名</th><th>邮箱</th><th>角色</th></tr></thead>
              <tbody>
                <tr v-for="m in members" :key="m.uuid"><td>{{ m.name || '-' }}</td><td>{{ m.email || '-' }}</td><td>{{ m.role }}</td></tr>
                <tr v-if="members.length===0"><td colspan="3" class="text-center text-grey py-4">暂无成员</td></tr>
              </tbody>
            </v-table>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card>
            <v-card-title>漏洞列表</v-card-title>
            <v-divider></v-divider>
            <v-table>
              <thead><tr><th>标识符</th><th>摘要</th><th>严重性</th><th>状态</th><th>更新时间</th></tr></thead>
              <tbody>
                <tr v-for="v in vulns" :key="v.uuid">
                  <td><router-link :to="`/vulnerabilities/${v.uuid}`">{{ v.identifier }}</router-link></td>
                  <td>{{ v.summary }}</td>
                  <td><v-chip size="x-small" :color="severityColor(v.severityNum)" variant="tonal">{{ v.severityNum }}</v-chip></td>
                  <td>{{ v.status }}</td>
                  <td>{{ formatDateTime(v.modified) }}</td>
                </tr>
                <tr v-if="vulns.length===0"><td colspan="5" class="text-center text-grey py-4">暂无漏洞</td></tr>
              </tbody>
            </v-table>
          </v-card>
        </v-col>
      </v-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import adminOrgs from '@/api/admin/organizations';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import AdminDataTable from '@/components/admin/table/AdminDataTable.vue';

const route = useRoute();
// 初始即为 loading，避免首屏闪现默认 false 的开关导致误解/误保存
const loading = ref(true);
const org = ref({});
const stats = ref({ memberCount: 0, vulnCount: 0 });
const members = ref([]);
const vulns = ref([]);
const edit = ref({ status: undefined, isVerified: false, rejectReason: '', isPublic: false, allowJoinRequest: false, allowInviteLink: false });
const saving = ref(false);
const acting = ref(false);

const orgStatusOptions = ['ACTIVE','PENDING','REJECTED','SUSPENDED'];
const orgStatusColor = (s) => s==='ACTIVE'?'success':(s==='PENDING'?'amber':(s==='REJECTED'?'error':'grey'));
const severityColor = (s) => s>=9?'error':(s>=7?'warning':(s>=4?'amber':'grey'));
const formatDateTime = (iso) => { if (!iso) return '-'; try { return new Date(iso).toLocaleString('zh-CN'); } catch { return iso; } };

const renderedFreeText = computed(() => {
  const text = org.value?.freeText || '';
  if (!text.trim()) return '';
  const raw = marked.parse(text);
  const clean = DOMPurify.sanitize(raw, {
    ALLOWED_TAGS: [
      'h1','h2','h3','h4','h5','h6',
      'p','br','hr',
      'strong','em','del','code','pre',
      'ul','ol','li',
      'a','img',
      'blockquote',
      'table','thead','tbody','tr','th','td'
    ],
    ALLOWED_ATTR: ['href','src','alt','title','class','target','rel']
  });
  return clean;
});

async function fetchDetails() {
  loading.value = true;
  try {
    // 同时获取“聚合详情”和“基础组织信息”（基础信息包含 isPublic/allowJoinRequest/allowInviteLink 等布尔字段）
    const [detailsResp, baseOrg] = await Promise.all([
      adminOrgs.getDetails(route.params.uuid),
      adminOrgs.get(route.params.uuid)
    ]);
    const d = detailsResp?.data?.data || detailsResp?.data || {};
    const fromDetails = d.organization || {};
    const fromBase = baseOrg || {};
    // 以基础组织信息为兜底，合并聚合详情（后者可能缺失某些布尔字段）
    const o = { ...fromBase, ...fromDetails };
    org.value = o;
    stats.value = d.statistics || { memberCount: 0, vulnCount: 0 };
    members.value = d.members || [];
    vulns.value = d.vulnerabilities || [];
    edit.value = {
      status: o.status,
      isVerified: Boolean(o.isVerified),
      rejectReason: '',
      isPublic: Boolean(o.isPublic),
      allowJoinRequest: Boolean(o.allowJoinRequest),
      allowInviteLink: Boolean(o.allowInviteLink),
    };
  } finally { loading.value = false; }
}

async function save() {
  saving.value = true;
  try {
    await adminOrgs.updateBasic(org.value.uuid, { status: edit.value.status, rejectReason: edit.value.rejectReason || undefined, isVerified: edit.value.isVerified, isPublic: edit.value.isPublic, allowJoinRequest: edit.value.allowJoinRequest, allowInviteLink: edit.value.allowInviteLink });
    await fetchDetails();
  } finally { saving.value = false; }
}

async function doSuspend() { acting.value = true; try { await adminOrgs.suspend(org.value.uuid); await fetchDetails(); } finally { acting.value = false; } }
async function doDisband() { acting.value = true; try { await adminOrgs.disband(org.value.uuid); await fetchDetails(); } finally { acting.value = false; } }
async function doRestore() { acting.value = true; try { await adminOrgs.restore(org.value.uuid); await fetchDetails(); } finally { acting.value = false; } }
async function doDelete() { if (!confirm('确认删除该组织？需先暂停。')) return; acting.value = true; try { await adminOrgs.delete(org.value.uuid); await fetchDetails(); } finally { acting.value = false; } }

onMounted(fetchDetails);
</script>

<style scoped>
.markdown-body {
  line-height: 1.7;
  color: #1f2937;
}
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  font-weight: 600;
  margin: 1.2em 0 0.6em;
  line-height: 1.3;
}
.markdown-body :deep(h1) { font-size: 1.8rem; }
.markdown-body :deep(h2) { font-size: 1.6rem; }
.markdown-body :deep(h3) { font-size: 1.4rem; }
.markdown-body :deep(p) { margin: 0.6em 0; }
.markdown-body :deep(code) {
  background: #f3f4f6;
  padding: 0.15em 0.4em;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}
.markdown-body :deep(pre) {
  background: #0b1020;
  color: #e5e7eb;
  padding: 12px 14px;
  border-radius: 6px;
  overflow: auto;
}
.markdown-body :deep(pre code) {
  background: transparent;
  padding: 0;
}
.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 1.4em;
}
.markdown-body :deep(li) { margin: 0.3em 0; }
.markdown-body :deep(a) { color: #2563eb; text-decoration: none; }
.markdown-body :deep(a:hover) { text-decoration: underline; }
.markdown-body :deep(blockquote) {
  border-left: 3px solid #e5e7eb;
  padding-left: 10px;
  color: #6b7280;
  margin: 0.8em 0;
}
.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0.8em 0;
}
.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 6px 8px;
}
.markdown-body :deep(th) { background: #f9fafb; }
.markdown-body :deep(img) { max-width: 100%; border-radius: 4px; }
.markdown-body :deep(strong) { font-weight: 600; }
.markdown-body :deep(em) { font-style: italic; }
.markdown-body :deep(del) { text-decoration: line-through; }
</style>
