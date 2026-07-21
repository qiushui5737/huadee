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
      { path: 'interaction/write', name: 'write-letter', component: () => import('@/views/portal/WriteLetter.vue') },
      { path: 'interaction/suggest', name: 'submit-suggest', component: () => import('@/views/portal/SubmitSuggest.vue') },
      { path: 'disclosure', name: 'disclosure', component: () => import('@/views/portal/Disclosure.vue') },
      { path: 'disclosure/my-apply', name: 'disclosure-my-apply', component: () => import('@/views/portal/DisclosureMyApply.vue') },
      { path: 'disclosure-catalog', name: 'disclosure-catalog', component: () => import('@/views/portal/DisclosureCatalog.vue') },
      { path: 'questionnaire', name: 'questionnaire', component: () => import('@/views/portal/Questionnaire.vue') },
      { path: 'dept', name: 'dept', component: () => import('@/views/portal/Dept.vue') },
      { path: 'profile', name: 'profile', component: () => import('@/views/portal/Profile.vue'), meta: { requiresPortalAuth: true } },
      // 政民互动子页面
      { path: 'interaction/letters', name: 'public-letters', component: () => import('@/views/portal/PublicLetters.vue') },
      { path: 'interaction/letter/:id', name: 'letter-detail', component: () => import('@/views/portal/LetterDetail.vue') },
      { path: 'interaction/consultation', name: 'consultation', component: () => import('@/views/portal/Consultation.vue') },
      { path: 'interaction/consultation/:id', name: 'consultation-detail', component: () => import('@/views/portal/ConsultationDetail.vue') },
      { path: 'interaction/complaint', name: 'submit-complaint', component: () => import('@/views/portal/SubmitComplaint.vue') },
      { path: 'interaction/opinion', name: 'submit-opinion', component: () => import('@/views/portal/SubmitOpinion.vue') },
      { path: 'interaction/collection', name: 'collection-list', component: () => import('@/views/portal/CollectionList.vue') },
      { path: 'interaction/collection/:id', name: 'collection-detail', component: () => import('@/views/portal/CollectionDetail.vue') },
      { path: 'interaction/suggestion', name: 'suggestion-list', component: () => import('@/views/portal/SuggestionList.vue') },
      { path: 'interaction/suggestion/:id', name: 'suggestion-detail', component: () => import('@/views/portal/SuggestionDetail.vue') },
      { path: 'interaction/collection/:id/feedback', name: 'collection-feedback', component: () => import('@/views/portal/CollectionFeedback.vue') },
      // 办事服务子页面
      { path: 'service/apply', name: 'service-apply', component: () => import('@/views/portal/ServiceApply.vue') }
    ]
  },
  {
    path: '/login',
    name: 'portal-login',
    component: () => import('@/views/admin/Login.vue')
  },
  {
    path: '/register',
    name: 'portal-register',
    component: () => import('@/views/admin/Register.vue')
  },
  // ===== B端 政府管理端(隔离入口) =====
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/admin/Login.vue')
  },
  {
    path: '/admin/register',
    name: 'admin-register',
    component: () => import('@/views/admin/Register.vue')
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
      { path: 'questionnaire', name: 'admin-questionnaire', component: () => import('@/views/admin/QuestionnaireMgmt.vue') },
      { path: 'consultation', name: 'admin-consultation', component: () => import('@/views/admin/ConsultationMgmt.vue') },
      { path: 'suggestion', name: 'admin-suggestion', component: () => import('@/views/admin/SuggestionMgmt.vue') },
      { path: 'complaint', name: 'admin-complaint', component: () => import('@/views/admin/ComplaintMgmt.vue') },
      { path: 'collection', name: 'admin-collection', component: () => import('@/views/admin/CollectionMgmt.vue') },
      { path: 'disclosure-file', name: 'admin-disclosure-file', component: () => import('@/views/admin/DisclosureFileMgmt.vue') },
      { path: 'cms', name: 'admin-cms', component: () => import('@/views/admin/ContentMgmt.vue') },
      { path: 'ai', name: 'admin-ai', component: () => import('@/views/admin/AiAudit.vue') },
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
        await useUserStore().restoreSession('admin')
        next()
      } catch {
        useUserStore().clearSession('admin')
        next({ name: 'admin-login' })
      }
    }
  } else if (to.meta.requiresPortalAuth) {
    if (!localStorage.getItem('c_token')) {
      next({ name: 'portal-login', query: { redirect: to.fullPath } })
    } else {
      try {
        await useUserStore().restoreSession('portal')
        next()
      } catch {
        useUserStore().clearSession('portal')
        next({ name: 'portal-login' })
      }
    }
  } else if ((to.name === 'admin-login' || to.name === 'admin-register') && localStorage.getItem('admin_token')) {
    next({ name: 'admin-dashboard' })
  } else if ((to.name === 'portal-login' || to.name === 'portal-register') && localStorage.getItem('c_token')) {
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
