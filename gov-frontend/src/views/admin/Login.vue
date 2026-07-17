<template>
  <div class="gov-page">
    <header class="gov-header">
      <router-link to="/" class="brand"><span class="emblem">政</span><span><b>政府网站集约化平台</b><small>GOVERNMENT SERVICE PLATFORM</small></span></router-link>
      <nav><router-link to="/">首页</router-link><i />简体<i />无障碍服务</nav>
    </header>
    <main class="content">
      <section class="login-panel">
        <div class="intro">
          <div class="intro-title">
            <span class="tag">统一身份认证</span>
            <h1>{{ isAdminEntry ? '政务管理端' : '政务服务用户端' }}</h1>
          </div>
        </div>
        <div class="login-form">
          <h2>账号登录</h2><div class="title-line" />
          <label>用户名</label>
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" :prefix-icon="User" />
          <label>密码</label>
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" @keyup.enter="onLogin" />
          <div class="form-links"><span>还没有账号？<router-link :to="isAdminEntry ? '/admin/register' : '/register'">立即注册</router-link></span><span>账号密码登录</span></div>
          <el-button class="primary-btn" type="primary" :loading="loading" @click="onLogin">登 录</el-button>
          <p class="notice">请妥善保管账号信息，请勿向他人泄露密码。</p>
        </div>
      </section>
    </main>
    <footer>主办单位：政府网站集约化平台　｜　技术支持：政务信息中心</footer>
  </div>
</template>
<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/admin'
import { useUserStore } from '@/stores/user'
const router = useRouter(); const route = useRoute(); const userStore = useUserStore(); const loading = ref(false)
const isAdminEntry = computed(() => route.path.startsWith('/admin'))
const form = reactive({ username: '', password: '' })
async function onLogin() {
  if (!form.username.trim() || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    const res: any = await adminLogin({ username: form.username.trim(), password: form.password })
    userStore.setSession(res.data.token, res.data)
    const admin = (res.data.roles || []).includes('ADMIN')
    ElMessage.success(admin ? '管理员登录成功' : '用户登录成功')
    router.replace(admin ? '/admin' : String(route.query.redirect || '/'))
  } finally { loading.value = false }
}
</script>
<style scoped>
.gov-page{min-height:100vh;background:#f7f8fa;color:#333;display:flex;flex-direction:column}.gov-header{height:108px;background:#fff;border-bottom:2px solid #175a9e;display:flex;align-items:center;justify-content:space-between;padding:0 max(5vw,60px)}.brand{display:flex;align-items:center;gap:16px;text-decoration:none;color:#bd1d22}.emblem{width:62px;height:62px;border-radius:50%;background:linear-gradient(145deg,#e52b2f,#a90005);color:#ffd565;display:grid;place-items:center;font:700 24px serif;border:4px solid #f4c73b}.brand b{font-size:28px;letter-spacing:2px}.brand small{display:block;color:#8a8a8a;font-size:11px;margin-top:6px;letter-spacing:1px}.gov-header nav{display:flex;gap:16px;align-items:center;color:#555}.gov-header a{color:inherit;text-decoration:none}.gov-header i{height:16px;border-left:1px solid #999}.content{flex:1;padding:50px 8%;background:linear-gradient(135deg,#f9fbfd 0,#eef5fb 100%)}.login-panel{max-width:1100px;min-height:510px;margin:auto;background:#fff;border:1px solid #d9e0e7;box-shadow:0 8px 30px rgba(28,71,112,.08);display:grid;grid-template-columns:1fr 1fr}.intro{position:relative;color:white;padding:72px 58px;background:linear-gradient(rgba(0,45,82,.42),rgba(0,45,82,.58)),url('@/assets/login-government.png') center/cover no-repeat;display:flex;align-items:flex-end;overflow:hidden}.intro::after{content:'';position:absolute;inset:0;background:linear-gradient(180deg,transparent 42%,rgba(0,30,58,.65) 100%)}.intro-title{position:relative;z-index:1;padding-bottom:14px}.tag{padding:6px 16px;border:1px solid rgba(255,255,255,.75);border-radius:20px;background:rgba(0,70,120,.24);backdrop-filter:blur(3px)}.intro h1{font-size:40px;margin:24px 0 0;text-shadow:0 2px 10px rgba(0,0,0,.5)}.login-form{padding:60px 64px}.login-form h2{color:#075a9f;font-size:26px;margin:0}.title-line{width:56px;border-bottom:3px solid #0c68ad;margin:12px 0 30px}.login-form label{display:block;margin:18px 0 8px;font-weight:600}.form-links{display:flex;justify-content:space-between;margin:22px 0;color:#888;font-size:14px}.form-links a{color:#0767ab;text-decoration:none}.primary-btn{width:100%;height:48px;font-size:18px;background:#0862a5}.notice{text-align:center;color:#999;font-size:13px;margin-top:24px}footer{height:75px;background:#fff;border-top:1px solid #d7dce1;display:grid;place-items:center;color:#777}@media(max-width:800px){.gov-header{padding:0 20px}.gov-header nav{display:none}.brand b{font-size:20px}.content{padding:20px}.login-panel{grid-template-columns:1fr}.intro{display:none}.login-form{padding:38px 26px}}
</style>
