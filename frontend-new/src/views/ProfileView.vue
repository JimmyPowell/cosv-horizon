<template>
  <v-container class="profile-container py-8">
    <v-btn
      v-if="fromOrgId"
      variant="text"
      prepend-icon="mdi-arrow-left"
      class="mb-4"
      @click="goBack"
    >
      返回组织成员
    </v-btn>
    <v-row>
      <!-- 左侧：用户信息卡片 -->
      <v-col cols="12" md="3">
        <v-card class="user-info-card pa-6" elevation="1">
          <!-- 大头像 -->
          <div class="text-center mb-4">
            <AppAvatar :name="user.name || user.email || 'U'" :size="260" class="profile-avatar mb-4" />
            
            <!-- 用户名 -->
            <h2 class="user-name text-h5 font-weight-bold mb-1">{{ user.name || 'User' }}</h2>
            
            <!-- 邮箱 -->
            <p class="user-email text-body-2 text-grey mb-4">{{ user.email || '' }}</p>
          </div>
          
          <!-- 编辑资料按钮 -->
          <v-btn 
            v-if="!viewingOther"
            block 
            variant="outlined" 
            color="grey-darken-1"
            class="mb-4"
            to="/profile-edit"
          >
            编辑资料
          </v-btn>

          <!-- 绑定 GitHub -->
          <v-alert v-if="bindMsg" type="info" variant="tonal" class="mb-4" closable @click:close="bindMsg=null">
            {{ bindMsg }}
          </v-alert>
          <div v-if="!viewingOther" class="mb-4">
            <template v-if="isGithubBound">
              <v-alert type="success" variant="tonal" class="mb-2">
                已绑定 GitHub：
                <a :href="user.gitHub" target="_blank" class="text-decoration-none">
                  {{ getGitHubUsername(user.gitHub) }}
                </a>
              </v-alert>
            </template>
            <template v-else>
              <v-btn
                block
                variant="outlined"
                color="primary"
                @click="handleBindGithub"
              >
                绑定 GitHub
              </v-btn>
            </template>
          </div>
          
          <!-- 用户元信息 -->
          <div class="user-meta-list">
            <div v-if="user.location" class="user-meta-item">
              <v-icon size="16" class="mr-2">mdi-map-marker</v-icon>
              <span>{{ user.location }}</span>
            </div>
            
            <div v-if="user.company" class="user-meta-item">
              <v-icon size="16" class="mr-2">mdi-office-building</v-icon>
              <span>{{ user.company }}</span>
            </div>
            
            <div v-if="user.gitHub" class="user-meta-item">
              <v-icon size="16" class="mr-2">mdi-github</v-icon>
              <a :href="user.gitHub" target="_blank" class="text-decoration-none">
                {{ getGitHubUsername(user.gitHub) }}
              </a>
            </div>
            
            <div v-if="user.website" class="user-meta-item">
              <v-icon size="16" class="mr-2">mdi-link-variant</v-icon>
              <a :href="user.website" target="_blank" class="text-decoration-none">
                {{ getDomain(user.website) }}
              </a>
            </div>
          </div>

          <!-- 积分与排名 -->
          <v-divider class="my-4"></v-divider>
          <div class="d-flex align-center justify-space-between">
            <div>
              <div class="text-caption text-grey">个人积分</div>
              <div class="text-h5 font-weight-bold">{{ pointsSummary.rating || 0 }}</div>
            </div>
            <div class="text-right">
              <div class="text-caption text-grey">全站排名</div>
              <div class="text-h6 font-weight-bold">{{ pointsSummary.rank ?? '-' }}</div>
            </div>
          </div>
        </v-card>
      </v-col>
      
      <!-- 右侧：个人简介和贡献图 -->
      <v-col cols="12" md="9">
        <!-- 个人简介（Markdown 渲染） -->
        <v-card v-if="user.freeText" class="mb-4 introduction-card" elevation="1">
          <v-card-title class="text-h6 font-weight-bold">
            <v-icon class="mr-2" size="20">mdi-text-box-outline</v-icon>
            个人简介
          </v-card-title>
          <v-card-text class="markdown-content" v-html="renderedMarkdown"></v-card-text>
        </v-card>

        <!-- 贡献热力图 -->
        <v-card class="contribution-card mb-4" elevation="1">
          <v-card-title class="d-flex align-center justify-space-between">
            <span class="text-h6 font-weight-bold">
              {{ stats.totalContributions || 0 }} contributions in the last year
            </span>
            <v-btn
              size="small"
              variant="text"
              @click="showContributionSettings = !showContributionSettings"
            >
              <v-icon>mdi-cog</v-icon>
            </v-btn>
          </v-card-title>
          
          <v-card-text>
            <div v-if="loading" class="text-center py-8">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
            </div>
            
            <div v-else-if="error" class="text-center py-8 text-error">
              {{ error }}
            </div>
            
            <contribution-calendar 
              v-else
              :data="contributionData"
            />
            
            <div class="mt-4 text-caption text-grey">
              <v-icon size="14" class="mr-1">mdi-information-outline</v-icon>
              贡献包括提交的漏洞报告和其他安全相关活动
            </div>
          </v-card-text>
        </v-card>
        
        <!-- 统计信息卡片 -->
        <v-row class="mt-4">
          <v-col cols="12" sm="6">
            <v-card elevation="1" class="pa-4">
              <div class="d-flex align-center">
                <v-icon size="40" color="primary" class="mr-4">mdi-shield-bug</v-icon>
                <div>
                  <div class="text-h4 font-weight-bold">{{ stats.totalVulnerabilities || 0 }}</div>
                  <div class="text-body-2 text-grey">总提交漏洞</div>
                </div>
              </div>
            </v-card>
          </v-col>
          
          <v-col cols="12" sm="6">
            <v-card elevation="1" class="pa-4">
              <div class="d-flex align-center">
                <v-icon size="40" color="success" class="mr-4">mdi-chart-line</v-icon>
                <div>
                  <div class="text-h4 font-weight-bold">{{ stats.totalContributions || 0 }}</div>
                  <div class="text-body-2 text-grey">今年贡献</div>
                </div>
              </div>
            </v-card>
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import userApi from '@/api/user';
import pointsApi from '@/api/points';
import ContributionCalendar from '@/components/ContributionCalendar.vue';
import AppAvatar from '@/components/AppAvatar.vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';

const user = ref({
  uuid: '',
  name: '',
  email: '',
  avatar: '',
  location: '',
  company: '',
  gitHub: '',
  website: '',
  freeText: ''
});

const stats = ref({
  totalVulnerabilities: 0,
  totalContributions: 0,
  currentYear: new Date().getFullYear()
});
const pointsSummary = ref({ rating: 0, rank: null });

const contributionData = ref({});
const loading = ref(true);
const error = ref(null);
const showContributionSettings = ref(false);
const isGithubBound = computed(() => !!(user.value && user.value.gitHub));

// 配置 marked
marked.setOptions({
  breaks: true,        // 支持 GitHub 风格的换行
  gfm: true,          // 启用 GitHub Flavored Markdown
  headerIds: true,    // 为标题生成 ID
  mangle: false,      // 不混淆邮箱地址
});

// 渲染 Markdown
const renderedMarkdown = computed(() => {
  if (!user.value.freeText) return '';

  try {
    // 1. Markdown → HTML
    const rawHtml = marked.parse(user.value.freeText);

    // 2. XSS 防护
    const cleanHtml = DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: [
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'p', 'br', 'hr',
        'strong', 'em', 'del', 'code', 'pre',
        'ul', 'ol', 'li',
        'a', 'img',
        'blockquote',
        'table', 'thead', 'tbody', 'tr', 'th', 'td'
      ],
      ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class', 'target', 'rel']
    });

    return cleanHtml;
  } catch (err) {
    console.error('Markdown rendering error:', err);
    return user.value.freeText;
  }
});

const route = useRoute();
const router = useRouter();
const viewingOtherId = computed(() => route.params && route.params.id ? String(route.params.id) : null);
const viewingOther = computed(() => !!viewingOtherId.value);
const fromOrgId = computed(() => (route.query && route.query.fromOrg) ? String(route.query.fromOrg) : null);
const fromTab = computed(() => (route.query && route.query.tab) ? String(route.query.tab) : 'members');

const goBack = () => {
  if (fromOrgId.value) {
    router.push({ path: `/organizations/${fromOrgId.value}`, query: { tab: fromTab.value || 'members' } });
  } else {
    router.back();
  }
};

// 绑定 GitHub
const bindMsg = ref(null);
const authStore = useAuthStore();
const handleBindGithub = () => {
  const finishUrl = `${window.location.origin}/oauth/finish?bind=1`;
  const at = authStore.accessToken;
  const url = `/api/oauth/github/render?bind=true&redirect=${encodeURIComponent(finishUrl)}${at ? `&at=${encodeURIComponent(at)}` : ''}`;
  window.location.href = url;
};

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = viewingOther.value ? await userApi.getByUuid(viewingOtherId.value) : await userApi.getUserInfo();
    if (response.data && response.data.code === 0 && response.data.data) {
      const userData = response.data.data.user || response.data.data;
      user.value = {
        uuid: userData.uuid || '',
        name: userData.name || '',
        email: userData.email || '',
        avatar: userData.avatar || '',
        location: userData.location || '',
        company: userData.company || '',
        gitHub: userData.gitHub || '',
        website: userData.website || '',
        freeText: userData.freeText || ''
      };
    }
  } catch (err) {
    console.error('Failed to fetch user info:', err);
    error.value = '加载用户信息失败';
  }
};

// 获取用户统计信息
const fetchUserStats = async () => {
  try {
    const response = viewingOther.value ? await userApi.getStatsFor(viewingOtherId.value) : await userApi.getUserStats();
    if (response.data && response.data.code === 0 && response.data.data) {
      stats.value = response.data.data.stats || {};
    }
  } catch (err) {
    console.error('Failed to fetch user stats:', err);
  }
};

// 获取贡献数据
const fetchContributions = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    const response = viewingOther.value ? await userApi.getContributionsFor(viewingOtherId.value) : await userApi.getUserContributions();
    if (response.data && response.data.code === 0 && response.data.data) {
      const data = response.data.data.contributions || {};
      contributionData.value = data.contributionsByDate || {};
    }
  } catch (err) {
    console.error('Failed to fetch contributions:', err);
    error.value = '加载贡献数据失败';
  } finally {
    loading.value = false;
  }
};

// 从 GitHub URL 提取用户名
function getGitHubUsername(url) {
  if (!url) return '';
  const match = url.match(/github\.com\/([^\/]+)/);
  return match ? match[1] : url;
}

// 从 URL 提取域名
function getDomain(url) {
  if (!url) return '';
  try {
    const urlObj = new URL(url);
    return urlObj.hostname;
  } catch {
    return url;
  }
}

onMounted(async () => {
  await Promise.all([
    fetchUserInfo(),
    fetchUserStats(),
    fetchContributions()
  ]);
  try {
    const targetUuid = viewingOther.value ? viewingOtherId.value : user.value.uuid;
    if (targetUuid) pointsSummary.value = await pointsApi.getUserSummary(targetUuid);
  } catch (_) {}
  // 绑定回跳提示
  if (route.query && route.query.bindSuccess) {
    bindMsg.value = 'GitHub 绑定成功';
  } else if (route.query && route.query.bindError) {
    bindMsg.value = decodeURIComponent(route.query.message || 'GitHub 绑定失败');
  }
});
</script>

<style scoped>
.profile-container {
  max-width: 1280px;
  margin: 0 auto;
}

.user-info-card {
  position: sticky;
  top: 80px;
}

.profile-avatar {
  border: 1px solid #e0e0e0;
}

.user-name {
  color: #24292f;
  line-height: 1.25;
}

.user-email {
  color: #57606a;
}

.user-meta-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-meta-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #57606a;
}

.user-meta-item a {
  color: #0969da;
}

.user-meta-item a:hover {
  text-decoration: underline !important;
}

.introduction-card {
  border: 1px solid #d0d7de;
}

.contribution-card {
  border: 1px solid #d0d7de;
}

/* Markdown 内容样式 */
.markdown-content {
  line-height: 1.6;
  color: #24292f;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-content :deep(h1) {
  font-size: 2em;
  border-bottom: 1px solid #d0d7de;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid #d0d7de;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h3) {
  font-size: 1.25em;
}

.markdown-content :deep(h4) {
  font-size: 1em;
}

.markdown-content :deep(h5) {
  font-size: 0.875em;
}

.markdown-content :deep(h6) {
  font-size: 0.85em;
  color: #57606a;
}

.markdown-content :deep(p) {
  margin-bottom: 16px;
}

.markdown-content :deep(code) {
  background-color: rgba(175, 184, 193, 0.2);
  padding: 0.2em 0.4em;
  border-radius: 6px;
  font-size: 85%;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.markdown-content :deep(pre) {
  background-color: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin-bottom: 16px;
  padding-left: 2em;
}

.markdown-content :deep(li) {
  margin-bottom: 4px;
}

.markdown-content :deep(a) {
  color: #0969da;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #d0d7de;
  padding-left: 16px;
  color: #57606a;
  margin-bottom: 16px;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #d0d7de;
  padding: 8px 12px;
}

.markdown-content :deep(th) {
  background-color: #f6f8fa;
  font-weight: 600;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
}

.markdown-content :deep(hr) {
  height: 0.25em;
  padding: 0;
  margin: 24px 0;
  background-color: #d0d7de;
  border: 0;
}

.markdown-content :deep(strong) {
  font-weight: 600;
}

.markdown-content :deep(em) {
  font-style: italic;
}

.markdown-content :deep(del) {
  text-decoration: line-through;
}

/* 响应式设计 */
@media (max-width: 960px) {
  .user-info-card {
    position: relative;
    top: 0;
  }

  .profile-avatar {
    width: 200px !important;
    height: 200px !important;
  }
}
</style>
