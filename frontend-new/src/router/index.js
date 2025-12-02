import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import RegisterView from '../views/RegisterView.vue'
import VerifyView from '../views/VerifyView.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'

const defaultTitle = 'cosv-horizon - 开源漏洞数据中心'
const defaultDescription = 'cosv-horizon 是基于 COSV Schema 的开源漏洞数据中心与协作平台，支持漏洞提交、审核、检索与共享，提供组织与个人积分榜、API 接口与自动导入能力，帮助构建安全透明的开源供应链。cosv-horizon is an open-source vulnerability collaboration platform built on the COSV schema.'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: {
        title: defaultTitle,
        description: 'cosv-horizon 是基于 COSV Schema 的开源漏洞数据中心，聚合开源漏洞信息，提供标准化上报与共享能力，支持组织与个人协作治理开源供应链安全。'
      }
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('../views/ForgotPasswordView.vue'),
      meta: {
        title: '找回密码 - cosv-horizon',
        description: '通过邮件验证重置你的 cosv-horizon 登录密码，保障账户安全。'
      }
    },
    {
      path: '/my/points',
      name: 'my-points',
      component: () => import('../views/UserPointsView.vue'),
      meta: {
        requiresAuth: true,
        title: '我的积分 - cosv-horizon',
        description: '查看你在 cosv-horizon 平台上通过漏洞报告与治理获得的积分记录和奖励情况。'
      }
    },
    {
      path: '/vulnerabilities',
      name: 'vulnerabilities',
      component: () => import('../views/VulnerabilitiesListView.vue'),
      meta: {
        requiresAuth: true,
        title: '漏洞列表 - cosv-horizon',
        description: '在 cosv-horizon 漏洞列表中按 COSV Schema 浏览和检索开源漏洞，支持按语言、组件、标签等条件筛选，为安全研究人员和平台方提供统一视图。'
      }
    },
    {
      path: '/vulnerabilities/submit',
      name: 'vulnerability-submit',
      component: () => import('../views/VulnerabilitySubmitView.vue'),
      meta: {
        requiresAuth: true,
        title: '提交漏洞 - cosv-horizon',
        description: '在 cosv-horizon 提交符合 COSV Schema 的开源漏洞报告，支持时间线、CWE、影响范围等关键字段，帮助社区快速响应并修复安全问题。'
      }
    },
    {
      path: '/vulnerabilities/:uuid/edit',
      name: 'vulnerability-edit',
      component: () => import('../views/VulnerabilityEditView.vue'),
      meta: {
        requiresAuth: true,
        title: '编辑漏洞 - cosv-horizon',
        description: '编辑已有开源漏洞条目，更新摘要、详情、受影响范围、补丁等信息，保持漏洞数据的准确性和完整性。'
      }
    },
    {
      path: '/mcp',
      name: 'mcp',
      component: () => import('../views/McpInfoView.vue'),
      meta: {
        requiresAuth: true,
        title: 'MCP 集成说明 - cosv-horizon',
        description: '了解如何在 AI 代理和工具链中集成 cosv-horizon MCP 服务，自动化查询与管理开源漏洞数据。'
      }
    },
    {
      path: '/users/:id/profile',
      name: 'user-profile',
      component: () => import('../views/ProfileView.vue'),
      meta: {
        requiresAuth: true,
        title: '用户公开档案 - cosv-horizon',
        description: '查看某个用户在 cosv-horizon 上的公开资料和漏洞贡献概况。'
      }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: {
        title: '注册 - cosv-horizon',
        description: '注册 cosv-horizon 账户，加入开源漏洞治理社区，共同提升供应链安全。'
      }
    },
    {
      path: '/verify',
      name: 'verify',
      component: VerifyView,
      meta: {
        title: '邮箱验证 - cosv-horizon',
        description: '完成邮件验证以激活你的 cosv-horizon 账户，保障平台使用安全。'
      }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        title: '登录 - cosv-horizon',
        description: '登录 cosv-horizon，开始管理和协作处理开源漏洞数据。'
      }
    },
    {
      path: '/oauth/finish',
      name: 'oauth-finish',
      component: () => import('../views/OAuthFinishView.vue'),
      meta: {
        title: '第三方登录完成 - cosv-horizon',
        description: '完成 GitHub 等第三方 OAuth 登录并回到 cosv-horizon，继续你的安全协作工作。'
      }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: {
        title: '关于我们 - cosv-horizon',
        description: '了解 cosv-horizon 背后的设计理念、COSV Schema 标准以及平台在开源供应链安全治理中的定位。'
      }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: {
        requiresAuth: true,
        title: '个人资料 - cosv-horizon',
        description: '查看与管理你的 cosv-horizon 账户信息、个人资料和安全偏好设置。'
      }
    },
    {
      path: '/profile-edit',
      name: 'profile-edit',
      component: () => import('../views/ProfileEditView.vue'),
      meta: {
        requiresAuth: true,
        title: '编辑个人资料 - cosv-horizon',
        description: '编辑你的 cosv-horizon 个人资料，包括昵称、联系方式等信息。'
      }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: {
        requiresAuth: true,
        title: '控制台 - cosv-horizon',
        description: '在控制台总览你的漏洞提交、审核进度、积分与通知，快速进入日常开源漏洞治理工作。'
      } // 标记为需要登录
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('../views/NotificationsView.vue'),
      meta: {
        requiresAuth: true,
        title: '通知中心 - cosv-horizon',
        description: '查看 cosv-horizon 平台发送的漏洞变更、审核结果、组织邀请等通知。'
      }
    },
    {
      path: '/organizations',
      name: 'organizations',
      component: () => import('../views/OrganizationsView.vue'),
      meta: {
        requiresAuth: true,
        title: '我的组织 - cosv-horizon',
        description: '管理你在 cosv-horizon 中的安全组织，查看组织成员、漏洞资产与协作情况。'
      }
    },
    {
      path: '/organizations/explore',
      name: 'organizations-explore',
      component: () => import('../views/PublicOrganizationsView.vue'),
      meta: {
        requiresAuth: true,
        title: '发现组织 - cosv-horizon',
        description: '浏览和搜索在 cosv-horizon 上的公开组织，了解社区协作情况。'
      }
    },
    {
      path: '/organizations/create',
      name: 'organization-create',
      component: () => import('../views/OrganizationCreateView.vue'),
      meta: {
        requiresAuth: true,
        title: '创建组织 - cosv-horizon',
        description: '在 cosv-horizon 创建新的安全组织，邀请成员共同管理开源漏洞。'
      }
    },
    {
      path: '/organizations/:id',
      name: 'organization-detail',
      component: () => import('../views/OrganizationDetailView.vue'),
      meta: {
        requiresAuth: true,
        title: '组织详情 - cosv-horizon',
        description: '查看特定组织在 cosv-horizon 上的漏洞资产、成员与安全治理活动概况。'
      }
    },
    {
      path: '/access-control',
      name: 'access-control',
      component: () => import('../views/AccessControlView.vue'),
      meta: {
        requiresAuth: true,
        title: '访问控制 - cosv-horizon',
        description: '管理 cosv-horizon 的 API Key、访问令牌等访问控制配置，保障数据安全。'
      }
    },
    {
      path: '/leaderboard',
      name: 'leaderboard',
      component: () => import('../views/LeaderboardView.vue'),
      meta: {
        requiresAuth: true,
        title: '贡献榜 - cosv-horizon',
        description: '查看 cosv-horizon 平台中的用户与组织漏洞贡献排行榜，了解社区在开源漏洞报告与治理方面的活跃度和影响力。'
      }
    },
    {
      path: '/org-invitations',
      name: 'org-invitations',
      component: () => import('../views/OrgInvitationsView.vue'),
      meta: {
        requiresAuth: true,
        title: '组织邀请 - cosv-horizon',
        description: '查看和处理你在 cosv-horizon 中收到的组织邀请。'
      }
    },
    {
      path: '/vulnerabilities/:id',
      name: 'vulnerability-detail',
      component: () => import('../views/VulnerabilityDetailView.vue'),
      meta: {
        requiresAuth: true,
        title: '漏洞详情 - cosv-horizon',
        description: '查看单条开源漏洞的 COSV 详情，包括摘要、时间线、受影响范围、修复信息和参考链接。'
      }
    },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
        title: '后台管理 - cosv-horizon',
        description: 'cosv-horizon 管理后台，用于管理用户、组织、漏洞数据、分类标签以及平台全局配置。'
      },
      children: [
        { path: '', redirect: '/admin/users' },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/admin/users/AdminUsersPage.vue'),
          meta: { title: '用户管理 - cosv-horizon' }
        },
        {
          path: 'organizations',
          name: 'admin-organizations',
          component: () => import('../views/admin/organizations/AdminOrganizationsPage.vue'),
          meta: { title: '组织管理 - cosv-horizon' }
        },
        {
          path: 'organizations/review',
          name: 'admin-organizations-review',
          component: () => import('../views/admin/organizations/AdminOrgReviewPage.vue'),
          meta: { title: '组织审核 - cosv-horizon' }
        },
        {
          path: 'organizations/:uuid',
          name: 'admin-organization-detail',
          component: () => import('../views/admin/organizations/AdminOrganizationDetailView.vue'),
          meta: { title: '组织详情（后台） - cosv-horizon' }
        },
        {
          path: 'vulnerabilities',
          name: 'admin-vulnerabilities',
          component: () => import('../views/admin/vulns/AdminVulnerabilitiesPage.vue'),
          meta: { title: '漏洞管理 - cosv-horizon' }
        },
        {
          path: 'vulnerabilities/:uuid',
          name: 'admin-vulnerability-detail',
          component: () => import('../views/admin/vulns/AdminVulnerabilityDetailView.vue'),
          meta: { title: '漏洞详情（后台） - cosv-horizon' }
        },
        {
          path: 'vulnerabilities/review',
          name: 'admin-vuln-review',
          component: () => import('../views/admin/vulns/AdminVulnReviewPage.vue'),
          meta: { title: '漏洞审核 - cosv-horizon' }
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('../views/admin/categories/AdminCategoriesPage.vue'),
          meta: { title: '漏洞分类管理 - cosv-horizon' }
        },
        {
          path: 'tags',
          name: 'admin-tags',
          component: () => import('../views/admin/tags/AdminTagsPage.vue'),
          meta: { title: '标签管理 - cosv-horizon' }
        },
        {
          path: 'settings',
          name: 'admin-settings',
          component: () => import('../views/admin/settings/AdminSettingsPage.vue'),
          meta: { title: '平台设置 - cosv-horizon' }
        },
      ]
    },
    {
      path: '/403',
      name: 'forbidden',
      component: () => import('../views/ForbiddenView.vue'),
      meta: {
        title: '无权限访问 - cosv-horizon',
        description: '你没有访问该 cosv-horizon 页面所需的权限，如有疑问请联系管理员。'
      }
    },
  ],
})

import { useAuthStore } from '@/stores/auth';

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);

  if (requiresAuth && !authStore.isLoggedIn) {
    next('/login');
    return;
  }

  if (requiresAdmin) {
    const user = authStore.user;
    const role = user?.role;
    const adminRoles = ['ADMIN', 'SYSTEM_ADMIN', 'SUPER_ADMIN'];
    const isAdmin = Array.isArray(role)
      ? role.some(r => adminRoles.includes(r))
      : adminRoles.includes(role);

    if (!isAdmin) {
      next('/403');
      return;
    }
  }

  next();
});

router.afterEach((to) => {
  if (typeof document === 'undefined') {
    return;
  }

  const matched = [...to.matched].reverse();
  const nearestWithTitle = matched.find(record => record.meta && record.meta.title);
  const nearestWithDescription = matched.find(record => record.meta && record.meta.description);

  document.title = nearestWithTitle ? nearestWithTitle.meta.title : defaultTitle;

  const description = nearestWithDescription ? nearestWithDescription.meta.description : defaultDescription;
  let descriptionTag = document.querySelector('meta[name="description"]');
  if (!descriptionTag) {
    descriptionTag = document.createElement('meta');
    descriptionTag.setAttribute('name', 'description');
    document.head.appendChild(descriptionTag);
  }
  descriptionTag.setAttribute('content', description);
});

export default router
