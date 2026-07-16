import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由配置
 * C端(群众客户端)路由 -> /portal/*
 * B端(政府管理端)路由 -> /admin/*
 */

const routes: RouteRecordRaw[] = [
  // ===== C端 群众客户端 =====
  {
    path: '/',
    component: () => import('@/views/portal/Layout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/portal/Home.vue') },
      { path: 'search', name: 'search', component: () => import('@/views/portal/Search.vue') },
      { path: 'chat', name: 'chat', component: () => import('@/views/portal/Chat.vue') },
      { path: 'service', name: 'service', component: () => import('@/views/portal/Service.vue') },
      { path: 'interaction', name: 'interaction', component: () => import('@/views/portal/Interaction.vue') },
      { path: 'disclosure', name: 'disclosure', component: () => import('@/views/portal/Disclosure.vue') },
      { path: 'dept', name: 'dept', component: () => import('@/views/portal/Dept.vue') }
    ]
  },
  // ===== B端 政府管理端(隔离入口) =====
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/admin/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'admin-dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'stats', name: 'admin-stats', component: () => import('@/views/admin/Stats.vue') },
      { path: 'interaction', name: 'admin-interaction', component: () => import('@/views/admin/InteractionMgmt.vue') },
      { path: 'service', name: 'admin-service', component: () => import('@/views/admin/ServiceApproval.vue') },
      { path: 'disclosure', name: 'admin-disclosure', component: () => import('@/views/admin/DisclosureAudit.vue') },
      { path: 'cms', name: 'admin-cms', component: () => import('@/views/admin/ContentMgmt.vue') },
      { path: 'performance', name: 'admin-performance', component: () => import('@/views/admin/Performance.vue') },
      { path: 'system', name: 'admin-system', component: () => import('@/views/admin/System.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// B端路由守卫 - 检查Token
router.beforeEach(async (to, _from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('admin_token')
    if (!token) {
      next({ name: 'admin-login' })
    } else {
      try {
        await useUserStore().restoreSession()
        next()
      } catch {
        useUserStore().logout()
        next({ name: 'admin-login' })
      }
    }
  } else if (to.name === 'admin-login' && localStorage.getItem('admin_token')) {
    next({ name: 'admin-dashboard' })
  } else {
    next()
  }
})

export default router
