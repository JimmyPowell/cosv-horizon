<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5 font-weight-bold">系统设置</h1>
      <v-spacer></v-spacer>
      <v-btn variant="outlined" prepend-icon="mdi-refresh" :loading="loading" @click="load">刷新</v-btn>
    </div>

  <v-card>
    <v-card-title>网站底部信息</v-card-title>
    <v-divider></v-divider>
    <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-text-field v-model="form.icpRecord" label="ICP 备案号" hide-details clearable />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field v-model="form.psbRecord" label="公安备案号" hide-details clearable />
          </v-col>
          <v-col cols="12">
            <v-text-field v-model="form.copyrightText" label="版权信息文本" hide-details clearable />
          </v-col>
          <v-col cols="12" md="6">
            <v-select
              v-model="form.fontFamily"
              :items="fontOptions"
              label="版权信息字体"
              hide-details
              clearable
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field v-model="form.fontSize" label="字体大小（如 12px/0.9rem，可选）" hide-details clearable />
          </v-col>
        </v-row>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="primary" :loading="saving" @click="save">保存</v-btn>
    </v-card-actions>
  </v-card>

  <v-card class="mt-4">
    <v-card-title>搜索索引</v-card-title>
    <v-divider></v-divider>
    <v-card-text>
      <div class="text-grey-darken-1 mb-2">当搜索结果异常或新增数据未被检索到时，可尝试重建索引。</div>
      <v-btn color="primary" variant="outlined" :loading="reindexing" @click="reindexAll" prepend-icon="mdi-database-refresh">全量重建索引</v-btn>
    </v-card-text>
  </v-card>

  <v-card class="mt-4">
    <v-card-title>积分管理</v-card-title>
    <v-divider></v-divider>
    <v-card-text>
      <div class="text-grey-darken-1 mb-2">设置漏洞事件的积分增量与严重度加权，并查看排名与按UUID查询积分。</div>
      <v-row>
        <v-col cols="12" md="6">
          <h3 class="text-subtitle-1 font-weight-bold mb-2">事件增量</h3>
          <v-row>
            <v-col cols="6">
              <v-text-field v-model.number="pointsForm.events.submitted.userDelta" type="number" label="提交-个人" hide-details />
            </v-col>
            <v-col cols="6">
              <v-text-field v-model.number="pointsForm.events.submitted.orgDelta" type="number" label="提交-组织" hide-details />
            </v-col>
            <v-col cols="6">
              <v-text-field v-model.number="pointsForm.events.published.userDelta" type="number" label="发布-个人" hide-details />
            </v-col>
            <v-col cols="6">
              <v-text-field v-model.number="pointsForm.events.published.orgDelta" type="number" label="发布-组织" hide-details />
            </v-col>
            <!-- 拒绝扣分禁用，不提供配置 -->
          </v-row>
        </v-col>
        <v-col cols="12" md="6">
          <h3 class="text-subtitle-1 font-weight-bold mb-2">严重度加权</h3>
          <v-select :items="severityModes" v-model="pointsForm.severity.mode" label="模式" hide-details />
          <div v-if="pointsForm.severity.mode === 'LEVEL_MULTIPLIER'">
            <v-row>
              <v-col cols="6"><v-text-field v-model.number="pointsForm.severity.levels.critical" type="number" step="0.1" label="CRITICAL 倍率" hide-details /></v-col>
              <v-col cols="6"><v-text-field v-model.number="pointsForm.severity.levels.high" type="number" step="0.1" label="HIGH 倍率" hide-details /></v-col>
              <v-col cols="6"><v-text-field v-model.number="pointsForm.severity.levels.medium" type="number" step="0.1" label="MEDIUM 倍率" hide-details /></v-col>
              <v-col cols="6"><v-text-field v-model.number="pointsForm.severity.levels.low" type="number" step="0.1" label="LOW 倍率" hide-details /></v-col>
            </v-row>
          </div>
          <div v-else-if="pointsForm.severity.mode === 'SCORE_LINEAR'">
            <v-row>
              <v-col cols="6"><v-text-field v-model.number="pointsForm.severity.linear.k" type="number" step="0.1" label="k" hide-details /></v-col>
              <v-col cols="6"><v-text-field v-model.number="pointsForm.severity.linear.b" type="number" step="0.1" label="b" hide-details /></v-col>
            </v-row>
          </div>
          <div class="mt-2">
            <v-btn size="small" variant="outlined" :loading="savingPoints" @click="savePoints">保存积分设置</v-btn>
          </div>
        </v-col>
      </v-row>

      <v-divider class="my-4"></v-divider>
      <h3 class="text-subtitle-1 font-weight-bold mb-2">预览计算</h3>
      <v-row>
        <v-col cols="12" md="3"><v-select :items="eventOptions" v-model="preview.event" label="事件" hide-details /></v-col>
        <v-col cols="12" md="3"><v-text-field v-model.number="preview.severityNum" type="number" step="0.1" label="严重度分数(可选)" hide-details /></v-col>
        <v-col cols="12" md="3"><v-select :items="levelOptions" v-model="preview.severityLevel" label="严重度等级(可选)" hide-details /></v-col>
        <v-col cols="12" md="3"><v-btn color="primary" @click="doPreview" :loading="previewing">计算</v-btn></v-col>
      </v-row>
      <div v-if="previewResult" class="mt-2 text-grey-darken-2">结果：个人 {{ previewResult.userDelta }}，组织 {{ previewResult.orgDelta }}</div>

      <v-divider class="my-4"></v-divider>
      <h3 class="text-subtitle-1 font-weight-bold mb-2">查看排名与查询</h3>
      <v-tabs v-model="lbTab" class="mb-2">
        <v-tab value="users">用户排行</v-tab>
        <v-tab value="orgs">组织排行</v-tab>
      </v-tabs>
      <v-window v-model="lbTab">
        <v-window-item value="users">
          <v-table density="comfortable"><thead><tr><th>名次</th><th>用户</th><th class="text-right">积分</th></tr></thead>
            <tbody><tr v-for="(u, idx) in lbUsers" :key="u.uuid"><td>{{ idx + 1 }}</td><td>{{ u.name }}</td><td class="text-right">{{ u.rating }}</td></tr></tbody></v-table>
        </v-window-item>
        <v-window-item value="orgs">
          <v-table density="comfortable"><thead><tr><th>名次</th><th>组织</th><th class="text-right">积分</th></tr></thead>
            <tbody><tr v-for="(o, idx) in lbOrgs" :key="o.uuid"><td>{{ idx + 1 }}</td><td>{{ o.name }}</td><td class="text-right">{{ o.rating }}</td></tr></tbody></v-table>
        </v-window-item>
      </v-window>

      <div class="d-flex align-center mt-4">
        <v-select :items="queryTypes" v-model="queryType" style="max-width:140px" hide-details />
        <v-text-field v-model="queryUuid" class="mx-2" label="UUID" hide-details style="max-width: 380px" />
        <v-btn variant="outlined" @click="doQuerySummary">查询</v-btn>
        <div class="ml-4 text-grey-darken-1" v-if="querySummary">积分：{{ querySummary.rating }}，排名：{{ querySummary.rank }}</div>
      </div>
    </v-card-text>
  </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import adminSettings from '@/api/admin/settings';
import { useToast } from '@/stores/toast';
import adminSearch from '@/api/admin/search';
import leaderboardApi from '@/api/leaderboard';
import pointsApi from '@/api/points';

const toast = useToast();
const loading = ref(false);
const saving = ref(false);
const form = ref({ icpRecord: '', psbRecord: '', copyrightText: '', fontFamily: '', fontSize: '' });
const reindexing = ref(false);

const fontOptions = [
  'system-ui, -apple-system, Segoe UI, Roboto, Noto Sans, Ubuntu, Cantarell, Helvetica Neue, Arial, Apple Color Emoji, Segoe UI Emoji, Noto Color Emoji, sans-serif',
  'Arial, Helvetica, sans-serif',
  'Helvetica Neue, Helvetica, Arial, sans-serif',
  'Roboto, Helvetica, Arial, sans-serif',
  'Noto Sans SC, PingFang SC, Microsoft YaHei, Arial, sans-serif',
  'PingFang SC, Hiragino Sans GB, Microsoft YaHei, Arial, sans-serif',
];

async function load() {
  loading.value = true;
  try {
    const s = await adminSettings.getSite();
    form.value = {
      icpRecord: s.icpRecord || '',
      psbRecord: s.psbRecord || '',
      copyrightText: s.copyrightText || '',
      fontFamily: s.fontFamily || '',
      fontSize: s.fontSize || '',
    };
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || '加载失败');
  } finally { loading.value = false; }
}

async function save() {
  saving.value = true;
  try {
    await adminSettings.updateSite(form.value);
    toast.success('保存成功');
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || '保存失败');
  } finally { saving.value = false; }
}

onMounted(load);

async function reindexAll() {
  reindexing.value = true;
  try {
    const count = await adminSearch.reindexAll();
    toast.success(`重建完成：共 ${count} 条`);
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || '重建失败');
  } finally { reindexing.value = false; }
}

// ===== Points settings state =====
const severityModes = ['NONE', 'LEVEL_MULTIPLIER', 'SCORE_LINEAR'];
const eventOptions = ['SUBMITTED', 'PUBLISHED'];
const levelOptions = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
const pointsForm = ref({
  events: {
    submitted: { userDelta: 0, orgDelta: 2 },
    published: { userDelta: 5, orgDelta: 10 },
  },
  severity: {
    mode: 'LEVEL_MULTIPLIER',
    levels: { critical: 2.0, high: 1.5, medium: 1.0, low: 0.5 },
    linear: { k: 1.0, b: 0.0 },
  }
});
const savingPoints = ref(false);
const preview = ref({ event: 'SUBMITTED', severityNum: null, severityLevel: null });
const previewResult = ref(null);
const lbUsers = ref([]);
const lbOrgs = ref([]);
const lbTab = ref('users');
const queryTypes = ['USER', 'ORG'];
const queryType = ref('USER');
const queryUuid = ref('');
const querySummary = ref(null);

async function loadPoints() {
  try {
    const s = await adminSettings.getPoints();
    // 深拷贝避免丢字段
    pointsForm.value = {
      events: {
        submitted: { userDelta: s?.events?.submitted?.userDelta ?? 0, orgDelta: s?.events?.submitted?.orgDelta ?? 2 },
        published: { userDelta: s?.events?.published?.userDelta ?? 5, orgDelta: s?.events?.published?.orgDelta ?? 10 },
      },
      severity: {
        mode: s?.severity?.mode || 'LEVEL_MULTIPLIER',
        levels: {
          critical: s?.severity?.levels?.critical ?? 2.0,
          high: s?.severity?.levels?.high ?? 1.5,
          medium: s?.severity?.levels?.medium ?? 1.0,
          low: s?.severity?.levels?.low ?? 0.5,
        },
        linear: {
          k: s?.severity?.linear?.k ?? 1.0,
          b: s?.severity?.linear?.b ?? 0.0,
        },
      },
    };
  } catch (e) {
    // ignore
  }
  try { lbUsers.value = await leaderboardApi.getUserLeaderboard(10); } catch (_) {}
  try { lbOrgs.value = await leaderboardApi.getOrganizationLeaderboard(10); } catch (_) {}
}

async function savePoints() {
  savingPoints.value = true;
  try {
    await adminSettings.updatePoints(pointsForm.value);
    toast.success('积分设置已保存');
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || '保存失败');
  } finally { savingPoints.value = false; }
}

async function doPreview() {
  previewResult.value = null;
  try {
    previewResult.value = await adminSettings.previewPoints({ ...preview.value });
  } catch (_) {}
}

async function doQuerySummary() {
  querySummary.value = null;
  const uuid = (queryUuid.value || '').trim();
  if (!uuid) return;
  try {
    querySummary.value = queryType.value === 'USER' ? await pointsApi.getUserSummary(uuid) : await pointsApi.getOrgSummary(uuid);
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || '查询失败');
  }
}

onMounted(loadPoints);
</script>

<style scoped>
</style>
