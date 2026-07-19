<template>
  <div class="portal-shell">
    <header class="site-header">
      <div class="utility">
        <span>遂宁市人民政府门户网站</span>
        <span class="utility-actions">
          <button type="button" @click="accessibility.toggleLanguage">{{ accessibility.language === 'zh-CN' ? '繁體中文' : '简体中文' }}</button>
          <i aria-hidden="true"></i>
          <button type="button" @click="accessibility.toolbarOpen = true">无障碍浏览</button>
        </span>
      </div>
      <div class="identity">
        <router-link to="/" class="gov-brand"><span class="seal">遂</span><span><b>遂宁市人民政府</b><small>THE PEOPLE'S GOVERNMENT OF SUINING</small></span></router-link>
        <div class="service-brand"><span class="service-mark">政</span><span><small>全国一体化在线政务服务平台</small><b>遂宁政务服务网</b></span></div>
        <div v-if="!userStore.isLoggedIn" class="account">
          <router-link to="/login">群众登录</router-link><router-link class="register" to="/register">注册</router-link>
        </div>
        <el-dropdown v-else class="user-menu" trigger="click" @command="handleUserCommand">
          <button class="avatar-button" type="button" aria-label="打开用户菜单">
            <el-avatar :size="42">{{ avatarText }}</el-avatar>
            <span>{{ userStore.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile"><el-icon><User /></el-icon>个人中心</el-dropdown-item>
              <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <nav class="main-nav">
        <router-link to="/">首页</router-link>
        <div class="service-menu">
          <router-link to="/service">办事服务</router-link>
          <div class="mega-menu">
            <router-link v-for="item in serviceItems" :key="item.name" :to="item.path"><component :is="item.icon"/><span>{{ item.name }}</span></router-link>
          </div>
        </div>
        <router-link to="/chat">智能问答</router-link><router-link to="/disclosure">在线依申请</router-link>
        <router-link to="/interaction">政民互动</router-link><router-link to="/search">政务公开</router-link>
        <a href="#help">帮助中心</a><router-link to="/dept">直通部门</router-link>
      </nav>
    </header>
    <main><router-view /></main>
    <footer id="help"><b>遂宁市人民政府</b><span>网站标识码：5109000001　蜀ICP备政务示例号</span><span>联系电话：0825-12345</span><router-link class="admin-entry" to="/admin/login">进入管理端</router-link></footer>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { School, House, FirstAidKit, Briefcase, UserFilled, Van, Wallet, Document, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { adminLogout } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import { useAccessibilityStore } from '@/stores/accessibility'
const router = useRouter()
const userStore = useUserStore()
const accessibility = useAccessibilityStore()
const avatarText = computed(() => (userStore.username || '用户').slice(0, 1))
onMounted(async () => {
  if (!localStorage.getItem('c_token')) return
  try { await userStore.restoreSession('portal') } catch { userStore.clearSession('portal') }
})
async function handleUserCommand(command: string) {
  if (command === 'profile') return router.push('/profile')
  if (command === 'logout') {
    try { await adminLogout() } finally {
      userStore.clearSession('portal')
      ElMessage.success('已退出登录')
      router.replace('/')
    }
  }
}
const serviceItems = [
  {name:'教育服务',icon:School,path:'/service?category=education'},{name:'住房保障',icon:House,path:'/service?category=housing'},
  {name:'医疗卫生',icon:FirstAidKit,path:'/service?category=health'},{name:'就业创业',icon:Briefcase,path:'/service?category=employment'},
  {name:'社会保障',icon:UserFilled,path:'/service?category=social'},{name:'交通出行',icon:Van,path:'/service?category=traffic'},
  {name:'税费办理',icon:Wallet,path:'/service?category=tax'},{name:'证件办理',icon:Document,path:'/service?category=certificate'}
]
</script>
<style scoped>
.portal-shell{min-height:100vh;color:#242424;background:#fff}.site-header{background:#fff}.utility{height:34px;padding:0 5vw;display:flex;align-items:center;justify-content:space-between;background:#f5f7fa;color:#777;font-size:13px;border-bottom:1px solid #eee}.identity{height:118px;padding:0 5vw;display:flex;align-items:center;gap:46px}.gov-brand,.service-brand{display:flex;align-items:center;gap:13px;text-decoration:none}.seal{width:64px;height:64px;border-radius:50%;display:grid;place-items:center;background:#c8171e;color:#ffd45b;border:4px solid #f0c643;font-size:24px;font-weight:700}.gov-brand b{display:block;color:#bd1d22;font-size:29px;letter-spacing:2px}.gov-brand small{color:#777;font-size:9px;letter-spacing:1px}.service-brand{padding-left:36px;border-left:1px solid #ccc}.service-mark{width:55px;height:55px;border-radius:50%;background:#0c8b75;color:#fff;display:grid;place-items:center;font-size:24px}.service-brand small,.service-brand b{display:block}.service-brand small{color:#fff;background:#c8242a;padding:2px 5px;font-size:10px}.service-brand b{font-size:25px;margin-top:5px}.account{margin-left:auto;display:flex;border:1px solid #1684d5}.account a{padding:11px 18px;color:#1269a9;text-decoration:none}.account .register{background:#1684d5;color:#fff}.main-nav{height:70px;display:flex;align-items:stretch;justify-content:center;gap:2.7vw;border-top:1px solid #eee;border-bottom:1px solid #dce5ed;position:relative;z-index:20}.main-nav>a,.service-menu>a{display:flex;align-items:center;color:#222;text-decoration:none;font-size:18px;white-space:nowrap;border-bottom:4px solid transparent}.main-nav>a.router-link-exact-active,.service-menu:hover>a{color:#096ab1;border-bottom-color:#1675bd}.service-menu{display:flex}.mega-menu{display:none;position:absolute;top:70px;left:50%;transform:translateX(-50%);width:min(1040px,90vw);padding:22px;background:#fff;border-top:3px solid #1473b8;box-shadow:0 12px 25px rgba(25,61,91,.18);grid-template-columns:repeat(4,1fr);gap:8px}.service-menu:hover .mega-menu{display:grid}.mega-menu a{display:flex;align-items:center;gap:12px;padding:17px;color:#333;text-decoration:none;background:#f6f9fc;border:1px solid #e6edf3}.mega-menu a:hover{color:#096ab1;background:#eef7ff}.mega-menu svg{width:23px}footer{min-height:110px;background:#273849;color:#dbe5ee;display:flex;justify-content:center;align-items:center;gap:48px;font-size:13px}footer b{font-size:19px;color:#fff}@media(max-width:900px){.utility,.service-brand{display:none}.identity{height:88px;padding:0 18px}.gov-brand b{font-size:20px}.seal{width:48px;height:48px}.account a{padding:8px}.main-nav{overflow-x:auto;justify-content:flex-start;padding:0 15px;gap:24px}.main-nav>a,.service-menu>a{font-size:15px}.mega-menu{grid-template-columns:repeat(2,1fr)}footer{flex-direction:column;gap:8px;padding:25px}}
.admin-entry{padding:9px 16px;border:1px solid #9fb5c6;color:#fff;text-decoration:none;background:#1c2d3c}.admin-entry:hover,.admin-entry:focus-visible{background:#fff;color:#1f4f72;border-color:#fff;outline:none}
.user-menu{margin-left:auto}.avatar-button{height:48px;display:flex;align-items:center;gap:10px;padding:3px 10px 3px 4px;border:1px solid #d8e3ec;background:#fff;color:#263746;cursor:pointer}.avatar-button:hover{border-color:#1684d5;color:#1269a9}.avatar-button .el-avatar{background:#176ead;color:#fff}.avatar-button span{max-width:120px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.utility-actions{display:flex;align-items:center;gap:10px}.utility-actions i{height:13px;border-left:1px solid #b8bec4}.utility-actions button{padding:2px;border:0;background:transparent;color:inherit;font:inherit;cursor:pointer}.utility-actions button:hover,.utility-actions button:focus-visible{color:#0869aa;text-decoration:underline}
</style>
