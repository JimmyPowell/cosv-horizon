<template>
  <v-card class="mb-6" variant="outlined">
    <v-card-title class="text-h6 font-weight-bold d-flex align-center">
      <v-icon class="mr-2" color="primary">mdi-forum</v-icon>
      评论
      <v-spacer />
      <span class="text-caption text-grey-darken-1">{{ total }} 条</span>
    </v-card-title>
    <v-divider />
    <v-card-text>
      <!-- New comment -->
      <div class="mb-4">
        <v-textarea v-model="draft" rows="3" placeholder="发表你的看法（支持 Markdown）" auto-grow hide-details />
        <div class="d-flex align-center mt-2">
          <v-spacer />
          <v-btn color="primary" :disabled="!canSubmit" :loading="submitting" @click="submit">发表评论</v-btn>
        </div>
      </div>

      <v-divider class="my-4" />

      <!-- List -->
      <div v-if="loading" class="text-center py-6">
        <v-progress-circular indeterminate color="primary" />
      </div>
      <div v-else>
        <div v-if="items.length === 0" class="text-grey text-center py-6">暂无评论</div>
        <div v-else>
          <div v-for="c in items" :key="c.commentUuid" class="comment-item">
            <div class="d-flex align-start">
              <v-avatar size="32" class="mr-3">
                <img v-if="c.userAvatar" :src="c.userAvatar" alt="avatar" />
                <v-icon v-else>mdi-account-circle</v-icon>
              </v-avatar>
              <div class="flex-grow-1">
                <div class="d-flex align-center mb-1">
                  <strong class="mr-2">{{ c.userName || '用户' }}</strong>
                  <span class="text-caption text-grey">{{ fmtTime(c.createTime) }}</span>
                  <v-spacer />
                  <v-btn v-if="canDelete(c)" size="x-small" variant="text" color="error" @click="confirmDelete(c)">
                    <v-icon size="18">mdi-delete</v-icon>
                  </v-btn>
                </div>
                <div class="comment-content markdown-content" v-html="renderMarkdown(c.content)"></div>
              </div>
            </div>
            <v-divider class="my-4" />
          </div>
          <div v-if="canLoadMore" class="text-center">
            <v-btn variant="outlined" :loading="loadingMore" @click="loadMore">加载更多</v-btn>
          </div>
        </div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import vulnerabilityApi from '@/api/vulnerability';
import { marked } from 'marked';
import DOMPurify from 'dompurify';

const props = defineProps({
  vulnUuid: { type: String, required: true },
});

const auth = useAuthStore();

const items = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const loading = ref(false);
const loadingMore = ref(false);

const draft = ref('');
const submitting = ref(false);
const canSubmit = computed(() => draft.value && draft.value.trim().length > 0 && draft.value.trim().length <= 4000);

function fmtTime(s) {
  if (!s) return '-';
  try { return new Date(s).toLocaleString(); } catch { return s; }
}

function renderMarkdown(text) {
  if (!text) return '';
  try {
    const raw = marked.parse(text);
    return DOMPurify.sanitize(raw, { ALLOWED_ATTR: ['href','src','alt','title','class','target','rel'] });
  } catch {
    return text;
  }
}

const canLoadMore = computed(() => items.value.length < total.value);

async function fetchComments(initial = false) {
  if (initial) { page.value = 1; items.value = []; }
  const params = { page: page.value, size: size.value };
  try {
    if (initial) loading.value = true; else loadingMore.value = true;
    const res = await vulnerabilityApi.listComments(props.vulnUuid, params);
    const data = res?.data?.data || {};
    total.value = data.total ?? 0;
    const list = data.items || [];
    if (initial) items.value = list; else items.value = items.value.concat(list);
  } finally {
    loading.value = false; loadingMore.value = false;
  }
}

async function submit() {
  if (!canSubmit.value || submitting.value) return;
  submitting.value = true;
  try {
    const res = await vulnerabilityApi.addComment(props.vulnUuid, { content: draft.value.trim() });
    const cv = res?.data?.data?.comment;
    if (cv) {
      items.value.unshift(cv);
      total.value += 1;
      draft.value = '';
    }
  } finally {
    submitting.value = false;
  }
}

function isAdmin() {
  const role = auth.user?.role;
  const adminRoles = ['ADMIN', 'SYSTEM_ADMIN', 'SUPER_ADMIN'];
  return Array.isArray(role) ? role.some(r => adminRoles.includes(r)) : adminRoles.includes(role);
}

function canDelete(c) {
  const myUuid = auth.user?.uuid;
  return isAdmin() || (myUuid && c.userUuid === myUuid);
}

async function confirmDelete(c) {
  if (!canDelete(c)) return;
  try {
    await vulnerabilityApi.deleteComment(props.vulnUuid, c.commentUuid);
    items.value = items.value.filter(x => x.commentUuid !== c.commentUuid);
    total.value = Math.max(0, total.value - 1);
  } catch (e) {
    console.error('删除评论失败', e);
  }
}

async function loadMore() {
  if (!canLoadMore.value) return;
  page.value += 1;
  await fetchComments(false);
}

onMounted(() => fetchComments(true));
</script>

<style scoped>
.comment-item {
  margin-bottom: 8px;
}
.markdown-content :deep(pre) { background: #f7f7f7; padding: 8px; border-radius: 4px; overflow: auto; }
.markdown-content :deep(code) { background: #f0f0f0; padding: 2px 4px; border-radius: 3px; }
.markdown-content :deep(a) { color: #1976d2; }
</style>

