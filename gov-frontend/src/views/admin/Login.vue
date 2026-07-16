<template>
  <div class="login-bg">
    <div class="login-box">
      <div style="text-align:center;font-size:48px;">🔒</div>
      <h2>政府管理端</h2>
      <div style="color:rgba(255,255,255,.5);text-align:center;font-size:12px;margin-bottom:24px;">admin.gov.cn · 政务内网隔离区域</div>
      <el-alert type="warning" :closable="false" style="margin-bottom:16px;">
        需通过政务VPN接入，并完成双因素认证后方可访问。
      </el-alert>
      <el-input v-model="form.username" placeholder="工号/用户名" style="margin-bottom:16px;" />
      <el-input v-model="form.password" type="password" placeholder="密码" style="margin-bottom:16px;" />
      <el-input v-model="form.code" placeholder="短信验证码(双因素)" style="margin-bottom:16px;">
        <template #append><el-button>获取验证码</el-button></template>
      </el-input>
      <el-button type="primary" style="width:100%;" @click="onLogin" :loading="loading">安全登录</el-button>
      <div style="text-align:center;margin-top:12px;">
        <a href="/" style="color:rgba(255,255,255,.4);font-size:12px;">← 返回群众客户端</a>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '', code: '' })

async function onLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入工号和密码')
    return
  }
  loading.value = true
  try {
    const res: any = await adminLogin({ username: form.username, password: form.password })
    userStore.setToken(res.data.token)
    userStore.setUser(res.data.username, [res.data.role])
    router.push('/admin')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.login-bg { position:fixed;inset:0;background:linear-gradient(135deg,#001529,#003a5c);display:flex;align-items:center;justify-content:center; }
.login-box { width:400px;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.15);border-radius:12px;padding:40px; }
.login-box h2 { text-align:center;color:#fff;margin:8px 0; }
</style>
