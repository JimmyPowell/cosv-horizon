<template>
  <v-container class="my-8">
    <h1 class="text-h4 mb-8">访问控制</h1>
    
    <v-row>
      <v-col cols="12">
        <v-card class="elevation-2">
          <v-card-title class="d-flex align-center">
            <span>API密钥</span>
            <v-spacer></v-spacer>
            <v-btn 
              color="primary" 
              prepend-icon="mdi-key-plus" 
              @click="showCreateKeyDialog = true">
              创建新密钥
            </v-btn>
          </v-card-title>
          
          <v-divider></v-divider>

      
          <v-card-text>
            <p class="text-body-2 mb-4">
              API密钥允许外部应用程序访问您的账户。请妥善保管您的密钥，如有泄露请立即撤销。
            </p>
            
            <div class="d-flex align-center mb-3" v-if="apiKeys.length > 0">
              <v-switch v-model="showRevoked" inset hide-details color="primary" :label="`显示已撤销 (${revokedCount})`" />
            </div>
            <v-table v-if="visibleKeys.length > 0">
              <thead>
                <tr>
                  <th>描述</th>
                  <th>前缀</th>
                  <th>作用域</th>
                  <th>创建日期</th>
                  <th>最后使用</th>
                  <th>过期时间</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="key in visibleKeys" :key="key.uuid">
                  <td>{{ key.description || '-' }}</td>
                  <td><code>{{ key.keyPrefix }}...</code></td>
                  <td>
                    <v-chip v-for="scope in key.scopes" :key="scope" size="small" class="mr-1 mb-1">{{ scope }}</v-chip>
                  </td>
                  <td>{{ formatDate(key.createTime) }}</td>
                  <td>{{ key.lastUsedTime ? formatDate(key.lastUsedTime) : '从未使用' }}</td>
                  <td>{{ key.expireTime ? formatDate(key.expireTime) : '永不' }}</td>
                  <td>
                    <v-chip :color="key.status === 'ACTIVE' ? 'success' : (key.status === 'REVOKED' ? 'error' : 'grey')" size="small" variant="tonal">
                      {{ key.status || '-' }}
                    </v-chip>
                  </td>
                  <td>
                    <v-btn 
                      color="primary" 
                      variant="text" 
                      density="compact"
                      @click="showUsageLogs(key)">
                      日志
                    </v-btn>
                    <v-btn 
                      color="error" 
                      variant="text" 
                      density="compact"
                      :disabled="key.status !== 'ACTIVE'"
                      @click="confirmRevokeKey(key)">
                      撤销
                    </v-btn>
                  </td>
                </tr>
              </tbody>
            </v-table>
            
            <div v-else-if="loading" class="text-center py-8">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
            </div>

            <div v-else class="text-center py-8">
              <v-icon size="64" color="grey" class="mb-4">mdi-key-outline</v-icon>
              <div class="text-body-1 text-grey">您目前没有创建任何API密钥</div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 创建新密钥对话框 -->
    <v-dialog v-model="showCreateKeyDialog" max-width="600">
      <v-card>
        <v-card-title>创建新API密钥</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newKey.description"
            label="描述"
            hint="给您的密钥一个描述性名称，例如：'GitHub集成'"
            variant="outlined"
            class="mb-4"
          ></v-text-field>

          <v-select
            v-model="newKey.organizationId"
            label="密钥类型"
            :items="organizationItems"
            item-title="name"
            item-value="id"
            variant="outlined"
            class="mb-4"
            hint="选择将密钥关联到组织或您的个人账户"
            persistent-hint
          ></v-select>

          <v-select
            v-model="newKey.scopes"
            label="权限范围 (Scopes)"
            :items="availableScopesForSelection"
            item-title="title"
            item-value="value"
            multiple
            chips
            closable-chips
            variant="outlined"
            class="mb-4"
          ></v-select>
          
          <v-select
            v-model="newKey.expireTime"
            label="过期时间"
            :items="expirationOptions"
            item-title="text"
            item-value="value"
            variant="outlined"
          ></v-select>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="showCreateKeyDialog = false">取消</v-btn>
          <v-btn 
            color="primary" 
            @click="createNewKey" 
            :loading="creatingKey"
            :disabled="!newKey.description">
            创建
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- 显示新创建的密钥对话框 -->
    <v-dialog v-model="showNewKeyDialog" max-width="500" persistent transition="fade-transition">
      <v-card>
        <v-card-title>密钥创建成功</v-card-title>
        <v-card-text>
          <p class="text-body-2 mb-4">
            请复制并保存您的API密钥。出于安全考虑，**我们不会再次显示此密钥**。
          </p>
          
          <v-text-field
            class="api-key-field"
            v-model="newKeyValue"
            readonly
            variant="outlined"
            append-inner-icon="mdi-content-copy"
            @click:append-inner="copyToClipboard"
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="closeNewKeyDialog" class="wrap-btn-text">我已保存，关闭</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 显示使用日志对话框 -->
    <v-dialog v-model="showLogsDialog" max-width="900">
      <v-card>
        <v-card-title>
          API密钥使用日志
          <span v-if="selectedKeyForLogs" class="text-subtitle-1 ml-2 text-grey">
            (<code>{{ selectedKeyForLogs.keyPrefix }}...</code>)
          </span>
        </v-card-title>
        <v-card-text>
          <v-table v-if="!loadingLogs && logs.length > 0">
            <thead>
              <tr>
                <th>时间</th>
                <th>IP地址</th>
                <th>方法</th>
                <th>路径</th>
                <th>状态码</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs" :key="log.id">
                <td>{{ formatDate(log.requestTimestamp) }}</td>
                <td>{{ log.requestIpAddress }}</td>
                <td>{{ log.requestMethod }}</td>
                <td>{{ log.requestPath }}</td>
                <td>
                  <v-chip :color="log.responseStatusCode >= 400 ? 'error' : 'success'" size="small">
                    {{ log.responseStatusCode }}
                  </v-chip>
                </td>
              </tr>
            </tbody>
          </v-table>
          <div v-else-if="loadingLogs" class="text-center py-8">
            <v-progress-circular indeterminate color="primary"></v-progress-circular>
          </div>
          <div v-else class="text-center py-8 text-grey">
            没有找到此密钥的使用记录
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="showLogsDialog = false">关闭</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 撤销密钥确认对话框 -->
    <v-dialog v-model="showRevokeDialog" max-width="520">
      <v-card>
        <v-card-title class="text-h6">撤销 API 密钥</v-card-title>
        <v-card-text>
          <div class="mb-2">确定要撤销以下密钥吗？此操作不可恢复，密钥将立即失效。</div>
          <v-list density="compact" class="mb-2">
            <v-list-item>
              <v-list-item-title>
                描述：{{ keyToRevoke?.description || '-' }}
              </v-list-item-title>
            </v-list-item>
            <v-list-item>
              <v-list-item-title>
                前缀：<code>{{ keyToRevoke?.keyPrefix }}...</code>
              </v-list-item-title>
            </v-list-item>
            <v-list-item>
              <v-list-item-title>
                状态：{{ keyToRevoke?.status || '-' }}
              </v-list-item-title>
            </v-list-item>
          </v-list>
          <v-alert type="warning" variant="tonal" density="comfortable">
            撤销后，使用该密钥的客户端将无法再访问接口。
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="showRevokeDialog = false">取消</v-btn>
          <v-btn color="error" :loading="revoking" :disabled="keyToRevoke?.status !== 'ACTIVE'" @click="doRevoke">
            确认撤销
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import apiKeyApi from '@/api/apiKey';
import organizationApi from '@/api/organization';

const loading = ref(true);
const creatingKey = ref(false);
const apiKeys = ref([]);
const organizations = ref([]);
const organizationItems = ref([{ name: '个人账户 (Personal Account)', id: null }]);

const showCreateKeyDialog = ref(false);
const showNewKeyDialog = ref(false);
const showLogsDialog = ref(false);

const logs = ref([]);
const selectedKeyForLogs = ref(null);
const loadingLogs = ref(false);

// 过滤显示：默认不展示已撤销
const showRevoked = ref(false);
const visibleKeys = computed(() => showRevoked.value ? apiKeys.value : apiKeys.value.filter(k => k.status !== 'REVOKED'));
const revokedCount = computed(() => apiKeys.value.filter(k => k.status === 'REVOKED').length);

// 撤销对话框
const showRevokeDialog = ref(false);
const keyToRevoke = ref(null);
const revoking = ref(false);

const newKey = reactive({
  description: '',
  organizationId: null, // 实际是组织UUID；提交时映射到 organizationUuid
  scopes: [],
  expireTime: '30d', // 30d/60d/90d 或 null
});
const newKeyValue = ref('');

// 与后端允许的 scopes 对齐：vuln:read, vuln:write, org:read, org:write, notification:read
const personalScopes = [
  { title: '读取漏洞', value: 'vuln:read' },
  { title: '提交/更新漏洞', value: 'vuln:write' },
  { title: '读取通知', value: 'notification:read' },
];

const organizationScopes = [
  { title: '读取组织', value: 'org:read' },
  { title: '管理组织', value: 'org:write' },
  { title: '读取漏洞', value: 'vuln:read' },
  { title: '提交/更新漏洞', value: 'vuln:write' },
  { title: '读取通知', value: 'notification:read' },
];

const availableScopesForSelection = computed(() => {
  return newKey.organizationId === null ? personalScopes : organizationScopes;
});

watch(() => newKey.organizationId, () => {
  newKey.scopes = [];
});

const expirationOptions = [
  { text: '永不过期', value: null },
  { text: '30天', value: '30d' },
  { text: '60天', value: '60d' },
  { text: '90天', value: '90d' },
];

const snackbar = reactive({
  show: false,
  text: '',
  color: 'success'
});

const fetchApiKeys = async () => {
  loading.value = true;
  try {
    const resp = await apiKeyApi.getMyApiKeys();
    if (resp.data && resp.data.code === 0) {
      const items = (resp.data.data?.items || []).map(k => ({
        ...k,
        scopes: k.scopes ? k.scopes.split(',') : [],
      }));
      apiKeys.value = items;
    } else {
      showSnackbar('获取API密钥列表失败', 'error');
    }
  } catch (error) {
    showSnackbar('获取API密钥列表失败', 'error');
  } finally {
    loading.value = false;
  }
};

const fetchOrganizations = async () => {
  try {
    const response = await organizationApi.listMine();
    if (response.data.code === 0) {
      const items = response.data.data.items || [];
      organizations.value = items;
      // Map to v-select items: name + id (uuid)
      organizationItems.value.push(...items.map(o => ({ name: o.name, id: o.uuid })));
    }
  } catch (error) {
    showSnackbar('获取组织列表失败', 'error');
  }
};

onMounted(() => {
  fetchApiKeys();
  fetchOrganizations();
});

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

const createNewKey = async () => {
  if (!newKey.description) return;
  
  creatingKey.value = true;
  try {
    const body = {
      description: newKey.description,
      scopes: newKey.scopes,
      organizationUuid: newKey.organizationId || null,
      expireTime: toExpireIso(newKey.expireTime),
    };
    const resp = await apiKeyApi.createApiKey(body);
    if (resp.data && resp.data.code === 0) {
      newKeyValue.value = resp.data.data?.apiKey || '';
      showCreateKeyDialog.value = false;
      showNewKeyDialog.value = true;
      fetchApiKeys();
    } else {
      const msg = resp.data?.message || '创建密钥失败';
      showSnackbar(msg, 'error');
    }
  } catch (error) {
    const errorMessage = error.response?.data?.message || '创建密钥失败';
    showSnackbar(errorMessage, 'error');
  } finally {
    creatingKey.value = false;
  }
};

const copyToClipboard = () => {
  navigator.clipboard.writeText(newKeyValue.value)
    .then(() => showSnackbar('API密钥已复制', 'success'))
    .catch(() => showSnackbar('复制失败', 'error'));
};

const closeNewKeyDialog = () => {
  showNewKeyDialog.value = false;
  // Reset form
  Object.assign(newKey, {
    description: '',
    organizationId: null,
    scopes: [],
    expireTime: '30d',
  });
  newKeyValue.value = '';
};

const confirmRevokeKey = (key) => {
  keyToRevoke.value = key;
  showRevokeDialog.value = true;
};

const doRevoke = async () => {
  if (!keyToRevoke.value) return;
  try {
    revoking.value = true;
    await revokeKey(keyToRevoke.value.uuid);
    showRevokeDialog.value = false;
  } finally {
    revoking.value = false;
  }
};

const revokeKey = async (uuid) => {
  try {
    const resp = await apiKeyApi.revokeApiKey(uuid);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('API密钥已撤销', 'success');
      fetchApiKeys();
    } else {
      const msg = resp.data?.message || '撤销失败';
      showSnackbar(msg, 'error');
    }
  } catch (error) {
    const errorMessage = error.response?.data?.message || '撤销失败';
    showSnackbar(errorMessage, 'error');
  }
};

const showUsageLogs = async (key) => {
  selectedKeyForLogs.value = key;
  showLogsDialog.value = true;
  loadingLogs.value = true;
  try {
    const resp = await apiKeyApi.getApiKeyUsageLogs(key.uuid);
    if (resp.data && resp.data.code === 0) {
      logs.value = resp.data.data?.items || [];
    } else {
      showSnackbar('获取使用日志失败', 'error');
      logs.value = [];
    }
  } catch (error) {
    showSnackbar('获取使用日志失败', 'error');
    logs.value = [];
  } finally {
    loadingLogs.value = false;
  }
};

const showSnackbar = (text, color = 'success') => {
  snackbar.text = text;
  snackbar.color = color;
  snackbar.show = true;
};

// 将 30d/60d/90d 转成 ISO-8601（不带时区的本地时间），或返回 null
const toExpireIso = (val) => {
  if (!val) return null; // 永不过期
  const m = /^([0-9]+)d$/.exec(val);
  if (!m) return null;
  const days = parseInt(m[1], 10);
  const dt = new Date();
  dt.setDate(dt.getDate() + days);
  const pad = (n) => String(n).padStart(2, '0');
  const yyyy = dt.getFullYear();
  const MM = pad(dt.getMonth() + 1);
  const dd = pad(dt.getDate());
  const HH = pad(dt.getHours());
  const mm = pad(dt.getMinutes());
  const ss = pad(dt.getSeconds());
  return `${yyyy}-${MM}-${dd}T${HH}:${mm}:${ss}`;
};
</script>

<style scoped>
.api-key-field :deep(.v-input__append-inner) {
  margin-left: 12px;
}

.wrap-btn-text {
  white-space: normal;
  height: auto;
  padding-top: 8px;
  padding-bottom: 8px;
}
</style>
