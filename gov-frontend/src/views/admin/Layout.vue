<template>
  <el-container style="height:100vh;">
    <el-aside width="220px" style="background:#001529;">
      <div class="admin-logo">
        <div class="logo-icon">🏛</div>
        <div class="logo-text"><div>管理平台</div><span>管理端</span></div>
      </div>
      <el-menu :default-active="$route.path" router background-color="#001529" text-color="rgba(255,255,255,.7)" active-text-color="#fff">
        <el-menu-item index="/admin">📊 数据大屏</el-menu-item>
        <el-menu-item index="/admin/stats">📈 统计报表</el-menu-item>
        <el-menu-item index="/admin/cms">📝 内容管理</el-menu-item>
        <el-menu-item index="/admin/disclosure">📄 依申请审核</el-menu-item>
        <el-menu-item index="/admin/service">📋 办事审批</el-menu-item>
        <el-menu-item index="/admin/interaction">💬 互动管理</el-menu-item>
        <el-menu-item index="/admin/performance">🏆 绩效管理</el-menu-item>
        <el-menu-item index="/admin/system">⚙ 系统管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
          
        </div>
        <div class="header-right">
          <el-tag size="small" type="danger" effect="plain" style="margin-right:12px;">政务内网</el-tag>
          <span class="user-info"><el-icon><User /></el-icon> {{ userStore.username || "管理员" }}</span>
          <el-button text @click="logout" style="color:rgba(0,0,0,.45);">退出</el-button>
        </div>
      </el-header>
      <el-main style="background:#f0f2f5;padding:20px;"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useUserStore } from "@/stores/user"

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => {
  const map: Record<string,string> = {
    "/admin":"数据大屏","/admin/stats":"统计报表","/admin/cms":"内容管理",
    "/admin/service":"办事审批","/admin/interaction":"互动管理","/admin/disclosure":"依申请审核",
    "/admin/performance":"绩效管理","/admin/system":"系统管理"
  }
  return map[route.path] || "管理端"
})

function logout() {
  userStore.logout()
  router.push("/admin/login")
}
</script>

<style scoped>
.admin-logo { padding:18px 16px; display:flex; align-items:center; gap:10px; border-bottom:1px solid rgba(255,255,255,.08); }
.logo-icon { font-size:28px; }
.logo-text { color:#fff; }
.logo-text div { font-size:16px; font-weight:bold; letter-spacing:1px; }
.logo-text span { font-size:11px; opacity:.5; }
.admin-header { display:flex; align-items:center; justify-content:space-between; background:#fff; border-bottom:1px solid #e8e8e8; padding:0 24px; height:56px; }
.header-left { display:flex; align-items:center; gap:16px; }
.page-title { font-size:16px; font-weight:600; color:#1a1a1a; }
.header-breadcrumb { font-size:12px; color:#999; }
.header-right { display:flex; align-items:center; gap:8px; }
.user-info { font-size:13px; color:#333; display:flex; align-items:center; gap:4px; }
</style>