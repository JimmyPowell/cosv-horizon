<template>
  <div>
    <div class="d-flex align-center mb-4">
      <v-btn variant="text" color="primary" @click="openTemplateInfo = true">
        <v-icon start>mdi-information-outline</v-icon>
        查看 COSV 模板说明
      </v-btn>
      <v-btn
        variant="text"
        color="primary"
        :href="templateDownloadUrl"
        download="cosv_import_template.json"
        class="ml-2"
      >
        <v-icon start>mdi-download</v-icon>
        下载 COSV 模板
      </v-btn>
    </div>

    <!-- Upload & metadata (lighter, button-trigger file select) -->
    <div class="mb-6">
      <div class="d-flex align-center mb-3">
        <v-icon class="mr-2">mdi-file</v-icon>
        <span class="text-subtitle-1 font-weight-bold">上传文件与元数据</span>
      </div>
      <v-row class="align-center">
        <v-col cols="12" md="5" class="d-flex align-center">
          <input ref="hiddenFileInput" type="file" accept="application/json,.json,application/x-ndjson,.ndjson,text/plain" @change="onHiddenFileChange" style="display:none" />
          <v-btn color="primary" :loading="uploading" @click="triggerFileSelect" prepend-icon="mdi-file-upload">选择 JSON 文件</v-btn>
          <span v-if="selectedFileName" class="ml-3 text-grey">{{ selectedFileName }}</span>
        </v-col>
        <v-col cols="12" md="3">
          <v-select v-model="language" :items="languageOptions" item-title="label" item-value="value" label="语言（建议填写）" clearable />
        </v-col>
        <v-col cols="12" md="4">
          <v-select v-model="categoryCode" :items="categoryItems" item-title="nameWithCode" item-value="code" label="分类（可选）" clearable :loading="loading.categories" />
        </v-col>
        <v-col cols="12">
          <v-combobox v-model="tagCodes" :items="tagCodeSuggestions" label="标签代码（可选，支持输入）" multiple chips clearable :loading="loading.tags" />
        </v-col>
      </v-row>
      <div class="d-flex align-center mt-2">
        <v-btn color="primary" :disabled="!canParse" :loading="parsing" @click="doParse" prepend-icon="mdi-chemical-weapon">重新解析</v-btn>
        <v-chip v-if="rawFileUuid" class="ml-3" size="small" color="info" label>{{ shortUuid(rawFileUuid) }}</v-chip>
        <v-btn v-if="rawFileUuid" variant="text" size="small" class="ml-1" @click="copyUuid" :text="copying ? '已复制' : '复制标识'" />
      </div>
      <v-alert v-if="parseError" type="error" variant="tonal" class="mt-2">{{ parseError }}</v-alert>
    </div>

    <!-- Parse result (single) -->
    <v-card v-if="parseResult && !isBatch" class="mb-6">
      <v-card-title class="d-flex align-center"><v-icon class="mr-2">mdi-magnify</v-icon>预检结果</v-card-title>
      <v-divider />
      <v-card-text>
        <div class="mb-3">
          <v-chip class="mr-2" color="primary" variant="flat">建议动作：{{ parseResult.suggestedAction || '-' }}</v-chip>
          <v-chip v-if="parseResult.targetVulnerabilityUuid" class="mr-2" color="info" variant="tonal">目标漏洞：{{ parseResult.targetVulnerabilityUuid }}</v-chip>
          <v-chip v-if="parseResult.aggregatedSeverityNum != null" class="mr-2" :color="severityColor(parseResult.aggregatedSeverityNum)" variant="tonal">建议严重性：{{ parseResult.aggregatedSeverityNum }}</v-chip>
        </div>
        <div class="mb-3">
          <div class="text-subtitle-2 mb-1">冲突/提示</div>
          <div v-if="!conflicts || conflicts.length === 0" class="text-grey">无</div>
          <v-list v-else density="compact">
            <v-list-item v-for="(c, idx) in conflicts" :key="idx">
              <v-list-item-title>
                <v-chip size="x-small" class="mr-2" color="error" variant="tonal">{{ c.type }}</v-chip>
                <span v-if="c.alias">别名：{{ c.alias }}</span>
                <span v-if="c.vulnerabilityUuid" class="ml-2">已占用：{{ c.vulnerabilityUuid }}</span>
                <span v-if="c.categoryCode">分类不存在：{{ c.categoryCode }}</span>
                <span v-if="c.tagCode">标签不存在：{{ c.tagCode }}</span>
                <span v-if="c.language">语言非法：{{ c.language }}</span>
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </div>
        <v-row>
          <v-col cols="12" md="4">
            <v-select v-model="action" :items="['CREATE','UPDATE']" label="入库动作" />
          </v-col>
          <v-col cols="12" md="4" v-if="action === 'UPDATE'">
            <v-text-field v-model="targetVulnUuid" label="目标漏洞UUID（预检建议已填）" />
          </v-col>
          <v-col cols="12" md="4">
            <v-select v-model="conflictPolicy" :items="['FAIL','SKIP_ALIAS']" label="别名冲突处理" />
          </v-col>
        </v-row>
        <v-btn color="success" :disabled="ingesting || !rawFileUuid || (action==='UPDATE' && !targetVulnUuid)" :loading="ingesting" @click="doIngest" prepend-icon="mdi-database-import">确认入库</v-btn>
      </v-card-text>
    </v-card>

    <!-- Parse result (batch) -->
    <v-card v-if="isBatch && parseList.length" class="mb-6">
      <v-card-title class="d-flex align-center"><v-icon class="mr-2">mdi-magnify</v-icon>批量预检结果</v-card-title>
      <v-divider />
      <v-card-text>
        <div class="mb-3">
          <v-chip class="mr-2" color="primary" variant="tonal">总数：{{ parseSummary.total }}</v-chip>
          <v-chip class="mr-2" color="success" variant="tonal">建议更新：{{ parseList.filter(i => i.suggestedAction==='UPDATE').length }}</v-chip>
          <v-chip class="mr-2" color="error" variant="tonal">冲突条目：{{ parseSummary.conflictCount }}</v-chip>
        </div>
        <v-data-table :headers="batchHeaders" :items="parseList" item-key="index">
          <template #item.id="{ item }">
            <span class="text-grey-darken-1">{{ item.id || '-' }}</span>
          </template>
          <template #item.summary="{ item }">
            <div class="text-truncate" style="max-width: 420px;">
              {{ item.summary || '-' }}
            </div>
          </template>
          <template #item.suggestedAction="{ item }"><v-chip size="x-small" :color="item.suggestedAction==='UPDATE' ? 'info' : 'primary'" variant="flat">{{ item.suggestedAction }}</v-chip></template>
          <template #item.conflicts="{ item }"><v-chip size="x-small" color="error" variant="tonal">{{ (item.conflicts || []).length }}</v-chip></template>
        </v-data-table>
        <div class="mt-4">
          <v-row>
            <v-col cols="12" md="4"><v-select v-model="action" :items="['AUTO','CREATE','UPDATE']" label="入库动作" /></v-col>
            <v-col cols="12" md="4"><v-select v-model="conflictPolicy" :items="['FAIL','SKIP_ALIAS']" label="别名冲突处理" /></v-col>
          </v-row>
          <v-btn color="success" :disabled="ingesting || !rawFileUuid" :loading="ingesting" @click="doIngestBatch" prepend-icon="mdi-database-import">批量确认入库</v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-dialog v-model="openTemplateInfo" max-width="720">
      <v-card>
        <v-card-title>COSV 文件模板说明</v-card-title>
        <v-card-text>
          <p class="text-body-2">请使用仓库中的 <code>docs/cosv_import_template.json</code> 作为参考。上传时无需在文件内携带组织、语言、分类、标签等平台字段，这些在页面上填写即可。</p>
          <p class="text-body-2 mb-2">建议流程：上传 → 解析/预检 → 解决冲突 → 确认入库。</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="openTemplateInfo = false">关闭</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import cosvApi from '@/api/cosv';
import categoryApi from '@/api/category';
import tagApi from '@/api/tag';
import { useRouter } from 'vue-router';

const router = useRouter();
const emit = defineEmits(['success']);

// Props from parent
const props = defineProps({
  submitter: {
    type: String,
    required: true,
  },
  organizationUuid: {
    type: String,
    default: null,
  },
});

// upload
const file = ref(null);
const hiddenFileInput = ref(null);
const selectedFileName = ref('');
const uploading = ref(false);
const rawFileUuid = ref('');
const openTemplateInfo = ref(false);

const templateDownloadUrl = `${import.meta.env.BASE_URL}cosv_import_template.json`;

// metadata
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
const language = ref('');
const categoryCode = ref('');
const tagCodes = ref([]);
const categoryItems = ref([]);
const tagCodeSuggestions = ref([]);
const loading = reactive({ categories: false, tags: false });

// parse result
const parsing = ref(false);
const parseResult = ref(null); // single
const parseList = ref([]); // batch items
const parseSummary = ref({ total: 0, conflictCount: 0 });
const conflicts = computed(() => parseResult.value?.conflicts || []);
const action = ref('CREATE');
const targetVulnUuid = ref('');
const conflictPolicy = ref('FAIL');
const ingesting = ref(false);

// 是否为批量（根据解析结果自动判断）
const isBatch = computed(() => (parseList.value?.length || 0) > 1);

// 批量表头
const batchHeaders = [
  { title: '序号', key: 'index', width: 80 },
  { title: 'ID', key: 'id', width: 180 },
  { title: '摘要', key: 'summary' },
  { title: '建议动作', key: 'suggestedAction', width: 120 },
  { title: '冲突数', key: 'conflicts', width: 100 },
  { title: '目标漏洞', key: 'targetVulnerabilityUuid' },
  { title: '建议严重性', key: 'aggregatedSeverityNum', width: 140 },
];

import { useToast } from '@/stores/toast';
const toast = useToast();

function severityColor(s) {
  const v = Number(s);
  if (v >= 9) return 'error';
  if (v >= 7) return 'warning';
  if (v >= 4) return 'amber';
  return 'grey';
}

async function onFileChange() {
  console.log('[COSV] onFileChange fired, value=', file.value);
  parseResult.value = null; rawFileUuid.value = ''; parseError.value = null;
  if (!file.value || !file.value.length) { console.warn('[COSV] no files to upload'); return; }
  const sel = file.value;
  let f;
  if (Array.isArray(sel)) f = sel[0];
  else if (sel && typeof sel === 'object' && 'length' in sel) f = sel[0]; // FileList
  else f = sel;
  if (!f) { console.error('[COSV] resolved file is empty'); toast.error('未选择文件'); return; }
  const org = props.submitter === 'ORG' ? (props.organizationUuid || undefined) : undefined;
  uploading.value = true;
  try {
    console.log('[COSV] uploading', { name: f?.name, size: f?.size, type: f?.type, org });
    const res = await cosvApi.upload(f, { organizationUuid: org });
    console.log('[COSV] upload response', res?.status, res?.data);
    const uuid = res?.data?.data?.rawFileUuid;
    if (uuid) { rawFileUuid.value = uuid; console.log('[COSV] got rawFileUuid', uuid); toast.success('上传成功'); await doParse(); }
    else { toast.error('上传响应异常'); parseError.value = '上传响应异常：未返回文件标识'; }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '上传失败';
    console.error('[COSV] upload failed', e);
    toast.error(msg);
    parseError.value = msg;
  } finally {
    uploading.value = false;
    console.log('[COSV] uploading done');
  }
}

function triggerFileSelect() {
  console.log('[COSV] triggerFileSelect');
  if (hiddenFileInput.value) hiddenFileInput.value.click();
}

function onHiddenFileChange(e) {
  const files = e?.target?.files;
  console.log('[COSV] onHiddenFileChange', files ? files.length : 0);
  if (!files || !files.length) { console.warn('[COSV] no files selected'); return; }
  const picked = Array.from(files);
  file.value = picked;
  selectedFileName.value = picked[0]?.name || '';
  try { if (selectedFileName.value) { toast.info(`已选择文件：${selectedFileName.value}`); console.log('[COSV] selected', selectedFileName.value); } } catch (_) {}
  // 清空以便可重复选择同名文件
  e.target.value = '';
  onFileChange();
}

async function doParse() {
  if (!rawFileUuid.value) return;
  console.log('[COSV] doParse for', rawFileUuid.value, { language: language.value, categoryCode: categoryCode.value, tagCodes: tagCodes.value });
  parsing.value = true; parseResult.value = null; parseList.value = []; parseSummary.value = { total: 0, conflictCount: 0 }; parseError.value = null;
  try {
    // 统一使用批量解析，自动识别单/多
    const res = await cosvApi.parseBatch(rawFileUuid.value, {
      language: language.value || undefined,
      categoryCode: categoryCode.value || undefined,
      tagCodes: tagCodes.value || [],
      mode: 'AUTO',
    });
    console.log('[COSV] parse-batch response', res?.status, res?.data);
    const data = res?.data?.data || {};
    const items = Array.isArray(data?.items) ? data.items : [];
    parseList.value = items;
    parseSummary.value = { total: data?.total || items.length || 0, conflictCount: data?.conflictCount || 0 };
    if (items.length === 1) {
      // 单条视图
      parseResult.value = items[0];
      action.value = parseResult.value?.suggestedAction || 'CREATE';
      targetVulnUuid.value = parseResult.value?.targetVulnerabilityUuid || '';
    } else {
      // 批量视图，动作默认 AUTO
      parseResult.value = null;
      action.value = 'AUTO';
      targetVulnUuid.value = '';
    }
    if (!items.length) {
      parseError.value = '未解析到任何记录，请检查文件内容或重试';
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '解析失败';
    console.error('[COSV] parse failed', e);
    toast.error(msg);
    parseError.value = msg;
  } finally {
    parsing.value = false;
    console.log('[COSV] parse done');
  }
}

async function doIngest() {
  if (!rawFileUuid.value) return;
  ingesting.value = true;
  try {
    const payload = {
      action: action.value,
      targetVulnUuid: action.value === 'UPDATE' ? (targetVulnUuid.value || undefined) : undefined,
      conflictPolicy: conflictPolicy.value,
      organizationUuid: props.submitter === 'ORG' ? (props.organizationUuid || undefined) : undefined,
      language: language.value || undefined,
      categoryCode: categoryCode.value || undefined,
      tagCodes: (tagCodes.value || []).join(','),
    };
    const res = await cosvApi.ingest(rawFileUuid.value, payload);
    const v = res?.data?.data?.vulnerability || {};
    const uuid = v.uuid;
    const status = v.status;
    if (status === 'ACTIVE' && uuid) {
      toast.success('入库成功，已发布');
      emit('success');
      router.push(`/vulnerabilities/${uuid}`);
    }
    else if (uuid) {
      toast.success('入库成功，待审核');
      emit('success');
      router.push('/dashboard');
    }
    else { toast.error('入库响应异常'); }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '入库失败';
    toast.error(msg);
    parseError.value = msg;
  } finally {
    ingesting.value = false;
  }
}

async function doIngestBatch() {
  if (!rawFileUuid.value) return;
  ingesting.value = true;
  try {
    const payload = {
      action: action.value,
      conflictPolicy: conflictPolicy.value,
      organizationUuid: props.submitter === 'ORG' ? (props.organizationUuid || undefined) : undefined,
      language: language.value || undefined,
      categoryCode: categoryCode.value || undefined,
      tagCodes: (tagCodes.value || []).join(','),
    };
    const res = await cosvApi.ingestBatch(rawFileUuid.value, payload);
    const total = res?.data?.data?.total || 0;
    const failed = res?.data?.data?.failed || 0;
    if (failed === 0) {
      toast.success(`批量入库成功：${total} 条`);
      emit('success');
      router.push('/vulnerabilities');
    }
    else { toast.warning(`部分失败：成功 ${total - failed} / 失败 ${failed}`); }
  } catch (e) {
    toast.error(e?.response?.data?.message || e?.message || '批量入库失败');
  } finally {
    ingesting.value = false;
  }
}

async function loadCategories() {
  loading.categories = true;
  try { const res = await categoryApi.list({ page: 1, size: 200 }); const list = res?.data?.data?.items || []; categoryItems.value = list.map(it => ({ code: it?.code, nameWithCode: it?.name && it?.code ? `${it.name}（${it.code}）` : (it?.name || it?.code || '') })).filter(i => i.code); }
  finally { loading.categories = false; }
}
async function loadTags() {
  loading.tags = true;
  try { const res = await tagApi.list({ page: 1, size: 200 }); const list = res?.data?.data?.items || []; tagCodeSuggestions.value = list.map(t => t?.code).filter(Boolean); }
  finally { loading.tags = false; }
}

onMounted(() => { loadCategories(); loadTags(); });

// 解析错误与辅助状态
const parseError = ref(null);
const canParse = computed(() => !!rawFileUuid.value && !parsing.value);
function shortUuid(u) { if (!u) return ''; return u.length > 12 ? (u.slice(0, 6) + '…' + u.slice(-4)) : u; }
const copying = ref(false);
async function copyUuid() { try { await navigator.clipboard.writeText(String(rawFileUuid.value || '')); copying.value = true; setTimeout(() => copying.value = false, 800);} catch (_) {} }
</script>

<style scoped>
</style>
