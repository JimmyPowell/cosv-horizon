<template>
  <v-container>
    <h1 class="text-h4 mb-4">通知中心</h1>

  <v-card>
    <v-toolbar flat>
      <v-toolbar-title>我的通知</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn color="primary" class="mr-2" @click="markAllAsRead">全部标为已读</v-btn>
      <v-btn color="error" @click="clearAll">清空所有通知</v-btn>
    </v-toolbar>

    <v-list lines="two">
      <v-list-item
        v-for="n in notifications"
        :key="n.uuid"
        :class="{ 'unread-notification': !n.isRead }"
        @click="openNotification(n)"
      >
        <template #prepend>
          <v-icon v-if="!n.isRead" color="blue">mdi-circle-medium</v-icon>
        </template>
        <v-list-item-title class="font-weight-medium">{{ n.title }}</v-list-item-title>
        <v-list-item-subtitle class="text-medium-emphasis">{{ n.content }}</v-list-item-subtitle>
        <template #append>
          <span class="text-caption text-grey">{{ fmtTime(n.createTime) }}</span>
        </template>
      </v-list-item>

      <v-list-item v-if="notifications.length === 0">
        <v-list-item-title class="text-center text-grey">暂无通知</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-card>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import notificationApi from '@/api/notification';
import { useRouter } from 'vue-router';

const router = useRouter();
const notifications = ref([]);

const load = async () => {
  const res = await notificationApi.list({ page: 1, size: 50 });
  const items = res?.data?.data?.items || [];
  notifications.value = items.map(x => ({
    uuid: x.uuid,
    type: x.type,
    title: x.title,
    content: x.content,
    isRead: x.isRead,
    createTime: x.createTime,
    actionUrl: x.actionUrl,
    status: x.status,
  }));
};

const markAllAsRead = async () => {
  await notificationApi.markAllRead();
  await load();
};

const clearAll = async () => {
  // 逐条删除（小批量场景足够）
  for (const n of notifications.value) {
    try { await notificationApi.remove(n.uuid); } catch {}
  }
  await load();
};

const openNotification = async (n) => {
  if (!n.isRead) {
    try { await notificationApi.markRead(n.uuid); } catch {}
    n.isRead = true;
  }
  if (n.actionUrl) {
    router.push(n.actionUrl);
  }
};

const fmtTime = (ts) => (ts ? new Date(ts).toLocaleString() : '');

onMounted(load);
</script>

<style scoped>
.unread-notification {
  background-color: #e3f2fd; /* A light blue background for unread items */
}
</style>
