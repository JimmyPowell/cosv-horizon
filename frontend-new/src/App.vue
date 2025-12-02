<template>
  <v-app>
    <v-app-bar app flat height="64" class="glass-nav">
      <v-container fluid class="d-flex align-center py-0 px-4">
        <div class="d-flex align-center" @click="goToDashboard" style="cursor: pointer;">
          <v-icon color="primary" size="32" class="mr-2">mdi-shield-check</v-icon>
          <v-toolbar-title class="font-weight-bold text-h5">cosv-horizon</v-toolbar-title>
          <template v-if="isLoggedIn">
            <span class="text-grey-darken-1 mx-2">|</span>
            <span class="font-weight-medium text-grey-darken-2">漏洞数据中心</span>
          </template>
        </div>
        <v-spacer></v-spacer>

        <!-- Logged Out State -->
        <template v-if="!isLoggedIn">
          <v-btn variant="text" class="nav-link hidden-sm-and-down" to="/#features">服务特性</v-btn>
          <v-btn variant="text" class="nav-link hidden-sm-and-down" to="/#about">关于我们</v-btn>
          <v-btn variant="text" class="nav-link hidden-sm-and-down" to="/#contact">联系我们</v-btn>
          <v-btn variant="text" class="nav-link" to="/login">登录</v-btn>
        </template>

        <!-- Logged In State -->
        <template v-else>
          <v-btn icon class="mr-1 nav-icon-btn" @click="toggleChat">
            <v-icon>mdi-robot-happy-outline</v-icon>
          </v-btn>
          <v-btn icon class="mr-1 nav-icon-btn" @click="navigateTo('/leaderboard')">
            <v-icon>mdi-trophy-outline</v-icon>
          </v-btn>
          <v-btn v-if="isAdmin" icon class="mr-1 nav-icon-btn" @click="navigateTo('/admin')">
            <v-icon>mdi-shield-crown-outline</v-icon>
          </v-btn>
          <v-btn variant="outlined" class="mr-1 nav-icon-btn" to="/vulnerabilities/submit">
            <v-icon left class="mr-1">mdi-plus</v-icon>
            提交漏洞
          </v-btn>
          <v-badge 
            :content="unreadCount > 0 ? (unreadCount > 99 ? '99+' : unreadCount.toString()) : undefined" 
            :model-value="unreadCount > 0"
            color="red" 
            overlap
            class="notification-badge"
          >
            <v-btn icon class="mr-1 nav-icon-btn" @click="navigateTo('/notifications')">
              <v-icon>mdi-bell-outline</v-icon>
            </v-btn>
          </v-badge>
          <v-btn icon class="nav-icon-btn" @click="showUserDrawer = true">
            <AppAvatar :name="userName || userEmail || 'U'" :size="40" />
          </v-btn>
        </template>
      </v-container>
    </v-app-bar>

    <v-main>
      <router-view v-slot="{ Component }">
        <v-fade-transition mode="out-in">
          <component :is="Component" />
        </v-fade-transition>
      </router-view>
    </v-main>

    <ai-bot-chat v-model="isChatOpen" />
    
    <!-- Global Toast -->
    <GlobalToast />
    
    <!-- Global Footer (hide on admin routes) -->
    <GlobalFooter v-if="!isAdminRoute" />
    
    <!-- User Navigation Drawer -->
    <v-navigation-drawer
      v-model="showUserDrawer"
      location="right"
      temporary
      width="300"
    >
      <!-- User header -->
      <div class="px-4 py-6 bg-grey-lighten-5">
        <div class="d-flex align-center mb-2">
          <AppAvatar :name="userName || userEmail || 'U'" :size="60" class="mr-3" />
          <div>
            <div class="text-h6">{{ userName || 'User' }}</div>
            <div class="text-subtitle-2 text-grey">{{ userEmail || 'user@example.com' }}</div>
          </div>
        </div>
      </div>
      
      <v-list density="compact" nav class="py-0">
        <v-list-item @click="navigateTo('/profile'); showUserDrawer = false" rounded="lg" class="mx-2 my-1">
          <template v-slot:prepend>
            <v-icon class="mr-2" color="grey">mdi-account-circle-outline</v-icon>
          </template>
          <v-list-item-title>个人资料</v-list-item-title>
        </v-list-item>
        
        <v-list-item to="/organizations" @click="showUserDrawer = false" rounded="lg" class="mx-2 my-1">
          <template v-slot:prepend>
            <v-icon class="mr-2" color="grey">mdi-account-group</v-icon>
          </template>
          <v-list-item-title>我的组织</v-list-item-title>
        </v-list-item>
        
        <v-list-item to="/access-control" @click="showUserDrawer = false" rounded="lg" class="mx-2 my-1">
          <template v-slot:prepend>
            <v-icon class="mr-2" color="grey">mdi-shield-key</v-icon>
          </template>
          <v-list-item-title>访问控制</v-list-item-title>
        </v-list-item>
        
        <v-list-item to="/mcp" @click="showUserDrawer = false" rounded="lg" class="mx-2 my-1">
          <template v-slot:prepend>
            <v-icon class="mr-2" color="grey">mdi-application-braces-outline</v-icon>
          </template>
          <v-list-item-title>MCP 集成</v-list-item-title>
        </v-list-item>
        
        <v-list-item @click="showUserDrawer = false" rounded="lg" class="mx-2 my-1">
          <template v-slot:prepend>
            <v-icon class="mr-2" color="grey">mdi-cog-outline</v-icon>
          </template>
          <v-list-item-title>设置</v-list-item-title>
        </v-list-item>
        
        <v-divider class="my-2"></v-divider>
        
        <v-list-item @click="logout(); showUserDrawer = false" rounded="lg" class="mx-2 my-1">
          <template v-slot:prepend>
            <v-icon class="mr-2" color="grey">mdi-logout</v-icon>
          </template>
          <v-list-item-title>退出登录</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>
  </v-app>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AiBotChat from './components/AiBotChat.vue';
import AppAvatar from '@/components/AppAvatar.vue';
import GlobalToast from '@/components/GlobalToast.vue';
import GlobalFooter from '@/components/GlobalFooter.vue';
import userApi from './api/user';
import notificationApi from '@/api/notification';
import { useAuthStore } from '@/stores/auth';

const isChatOpen = ref(false);
const showUserDrawer = ref(false);
const userName = ref('');
const userEmail = ref('');

const toggleChat = () => {
  isChatOpen.value = !isChatOpen.value;
};

const route = useRoute();
const router = useRouter();

const isLoggedIn = ref(false);
const unreadCount = ref(0);

// 监听路由变化，确保登录状态正确更新
watch(
  () => route.path,
  (newPath) => {
    isLoggedIn.value = route.meta.requiresAuth === true;
    if (isLoggedIn.value) {
      fetchUserProfile();
      fetchUnreadCount();
    }
  },
  { immediate: true }
);

// 获取用户个人资料信息
const fetchUserProfile = async () => {
  try {
    const response = await userApi.getUserInfo();
    if (response.data && response.data.code === 0 && response.data.data) {
      const userData = response.data.data.user || response.data.data;
      userName.value = userData.name || '';
      userEmail.value = userData.email || '';
    }
  } catch (error) {
    console.error('Failed to fetch user profile:', error);
  }
};

onMounted(() => {
  if (isLoggedIn.value) {
    fetchUserProfile();
    fetchUnreadCount();
  }
});

const authStore = useAuthStore();
const logout = () => authStore.logout();
const isAdmin = computed(() => {
  const role = authStore.user?.role;
  const adminRoles = ['ADMIN', 'SYSTEM_ADMIN', 'SUPER_ADMIN'];
  return Array.isArray(role) ? role.some(r => adminRoles.includes(r)) : adminRoles.includes(role);
});

const isAdminRoute = computed(() => route.path.startsWith('/admin'));

const goToDashboard = () => {
  if (isLoggedIn.value) {
    router.push('/dashboard');
  } else {
    router.push('/');
  }
};

// 导航方法，用于导航栏按钮点击后立即恢复状态
const navigateTo = (path) => {
  router.push(path);
};

const fetchUnreadCount = async () => {
  try {
    const res = await notificationApi.unreadCount();
    unreadCount.value = res?.data?.data?.count || 0;
  } catch (e) {
    // ignore
  }
};
</script>

<style>
/* Global styles */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  background-color: #f9fafb;
}

.glass-nav {
  background-color: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(224, 224, 224, 0.5) !important;
}

.nav-link.v-btn--variant-text .v-btn__overlay,
.nav-link.v-btn--variant-text .v-btn__underlay {
  background: transparent !important;
}

/* 导航栏图标按钮样式 - 点击后不保持激活状态 */
.nav-icon-btn {
  transition: all 0.2s ease-in-out !important;
}

/* 点击动画效果 */
.nav-icon-btn:active {
  transform: scale(0.9);
}

/* 悬停效果 */
.nav-icon-btn:hover {
  background-color: rgba(0, 0, 0, 0.04) !important;
}

/* 移除路由激活状态的样式 */
.nav-icon-btn.v-btn--active {
  background-color: transparent !important;
}

.nav-icon-btn .v-btn__overlay {
  opacity: 0 !important;
}

/* 确保按钮点击后立即恢复 */
.nav-icon-btn:focus {
  background-color: transparent !important;
}

.nav-icon-btn:focus-visible {
  outline: 2px solid rgba(24, 103, 192, 0.3);
  outline-offset: 2px;
}

/* 通知徽章样式 */
.notification-badge .v-badge__badge {
  min-width: 18px !important;
  height: 18px !important;
  font-size: 11px !important;
  font-weight: 600 !important;
  padding: 0 4px !important;
  border-radius: 9px !important;
  line-height: 18px !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2) !important;
}
</style>
