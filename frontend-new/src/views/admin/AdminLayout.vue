<template>
  <div class="admin-layout">
    <div class="admin-sidebar-wrapper" :class="{ 'is-collapsed': collapsed }">
      <admin-sidebar :collapsed="collapsed" @toggle="toggleCollapsed" />
    </div>
    <div class="admin-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import AdminSidebar from '@/components/admin/layout/AdminSidebar.vue';
import { ref, watch } from 'vue';

const collapsed = ref(localStorage.getItem('adminSidebarCollapsed') === '1');
function toggleCollapsed() {
  collapsed.value = !collapsed.value;
}
watch(collapsed, (v) => localStorage.setItem('adminSidebarCollapsed', v ? '1' : '0'));
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: calc(100vh - 64px);
  width: 100%;
  overflow: hidden;
}

.admin-sidebar-wrapper {
  flex: 0 0 240px;
  width: 240px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  height: 100%;
}

.admin-sidebar-wrapper.is-collapsed {
  flex: 0 0 72px;
  width: 72px;
}

.admin-content {
  flex: 1;
  padding: 24px;
  height: 100%;
  overflow: auto; /* 仅右侧内容区域滚动 */
}
</style>
