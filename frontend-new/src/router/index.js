import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import RegisterView from '../views/RegisterView.vue'
import VerifyView from '../views/VerifyView.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('../views/ForgotPasswordView.vue')
    },
    {
      path: '/my/points',
      name: 'my-points',
      component: () => import('../views/UserPointsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vulnerabilities',
      name: 'vulnerabilities',
      component: () => import('../views/VulnerabilitiesListView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vulnerabilities/submit',
      name: 'vulnerability-submit',
      component: () => import('../views/VulnerabilitySubmitView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vulnerabilities/:uuid/edit',
      name: 'vulnerability-edit',
      component: () => import('../views/VulnerabilityEditView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/mcp',
      name: 'mcp',
      component: () => import('../views/McpInfoView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/users/:id/profile',
      name: 'user-profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/verify',
      name: 'verify',
      component: VerifyView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/oauth/finish',
      name: 'oauth-finish',
      component: () => import('../views/OAuthFinishView.vue')
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile-edit',
      name: 'profile-edit',
      component: () => import('../views/ProfileEditView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true } // 标记为需要登录
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('../views/NotificationsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/organizations',
      name: 'organizations',
      component: () => import('../views/OrganizationsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/organizations/explore',
      name: 'organizations-explore',
      component: () => import('../views/PublicOrganizationsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/organizations/create',
      name: 'organization-create',
      component: () => import('../views/OrganizationCreateView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/organizations/:id',
      name: 'organization-detail',
      component: () => import('../views/OrganizationDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/access-control',
      name: 'access-control',
      component: () => import('../views/AccessControlView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/leaderboard',
      name: 'leaderboard',
      component: () => import('../views/LeaderboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/org-invitations',
      name: 'org-invitations',
      component: () => import('../views/OrgInvitationsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vulnerabilities/:id',
      name: 'vulnerability-detail',
      component: () => import('../views/VulnerabilityDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/users' },
        { path: 'users', name: 'admin-users', component: () => import('../views/admin/users/AdminUsersPage.vue') },
        { path: 'organizations', name: 'admin-organizations', component: () => import('../views/admin/organizations/AdminOrganizationsPage.vue') },
        { path: 'organizations/review', name: 'admin-organizations-review', component: () => import('../views/admin/organizations/AdminOrgReviewPage.vue') },
        { path: 'organizations/:uuid', name: 'admin-organization-detail', component: () => import('../views/admin/organizations/AdminOrganizationDetailView.vue') },
        { path: 'vulnerabilities', name: 'admin-vulnerabilities', component: () => import('../views/admin/vulns/AdminVulnerabilitiesPage.vue') },
        { path: 'vulnerabilities/:uuid', name: 'admin-vulnerability-detail', component: () => import('../views/admin/vulns/AdminVulnerabilityDetailView.vue') },
        { path: 'vulnerabilities/review', name: 'admin-vuln-review', component: () => import('../views/admin/vulns/AdminVulnReviewPage.vue') },
        { path: 'categories', name: 'admin-categories', component: () => import('../views/admin/categories/AdminCategoriesPage.vue') },
        { path: 'tags', name: 'admin-tags', component: () => import('../views/admin/tags/AdminTagsPage.vue') },
        { path: 'settings', name: 'admin-settings', component: () => import('../views/admin/settings/AdminSettingsPage.vue') },
      ]
    },
    {
      path: '/403',
      name: 'forbidden',
      component: () => import('../views/ForbiddenView.vue')
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

export default router
