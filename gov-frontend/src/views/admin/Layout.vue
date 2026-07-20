<template>
  <el-container style="height:100vh;">
    <el-aside width="200px" style="background:#001529;">
      <div style="color:#fff;padding:20px;font-size:16px;font-weight:bold;">政府管理端</div>
      <el-menu :default-active="$route.path" router background-color="#001529" text-color="#fff" active-text-color="#40a9ff">
        <el-menu-item index="/admin">📊 数据大屏</el-menu-item>
        <el-menu-item index="/admin/stats">📈 统计报表</el-menu-item>
        <el-menu-item index="/admin/interaction">💬 互动管理</el-menu-item>
        <el-menu-item index="/admin/service">📋 办事审批</el-menu-item>
        <el-menu-item index="/admin/service-mgmt">⚙ 服务管理</el-menu-item>
        <el-menu-item index="/admin/service-rating">⭐ 服务评价</el-menu-item>
        <el-menu-item index="/admin/disclosure">📄 依申请审核</el-menu-item>
        <el-menu-item index="/admin/cms">📝 内容管理</el-menu-item>
        <el-menu-item index="/admin/ai">🤖 AI审计</el-menu-item>
        <el-menu-item index="/admin/performance">🏆 绩效管理</el-menu-item>
        <el-menu-item index="/admin/system">⚙ 系统管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex;align-items:center;justify-content:space-between;background:#fff;border-bottom:1px solid #eee;">
        <span>{{ pageTitle }}</span>
        <span>
          {{ userStore.username }} / {{ userStore.roles.join(',') }}
          <el-button text @click="logout">退出</el-button>
        </span>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { adminLogout } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => {
  const map: Record<string,string> = {
    '/admin':'数据大屏','/admin/stats':'统计报表','/admin/interaction':'互动管理',
    '/admin/service':'办事审批','/admin/service-mgmt':'服务管理','/admin/service-rating':'服务评价','/admin/disclosure':'依申请审核','/admin/cms':'内容管理',
    '/admin/ai':'AI审计','/admin/performance':'绩效管理','/admin/system':'系统管理'
  }
  return map[route.path] || '管理端'
})

async function logout() {
  try {
    await adminLogout()
  } finally {
    userStore.clearSession('admin')
    router.replace('/admin/login')
  }
}
</script>
