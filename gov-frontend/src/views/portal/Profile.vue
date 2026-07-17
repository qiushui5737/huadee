<template>
  <div class="profile-page">
    <div class="profile-heading">
      <div class="profile-avatar">{{ avatarText }}</div>
      <div><h1>{{ userStore.username }}</h1><p>账号：{{ userStore.profile.username }}</p></div>
    </div>

    <div class="profile-layout">
      <nav class="profile-nav" aria-label="个人中心">
        <button :class="{ active: activeTab === 'info' }" @click="activeTab = 'info'">
          <el-icon><User /></el-icon><span>个人信息</span>
        </button>
        <button :class="{ active: activeTab === 'password' }" @click="activeTab = 'password'">
          <el-icon><Lock /></el-icon><span>修改密码</span>
        </button>
        <button class="logout-item" @click="logout">
          <el-icon><SwitchButton /></el-icon><span>退出登录</span>
        </button>
      </nav>

      <section class="profile-content">
        <template v-if="activeTab === 'info'">
          <div class="section-title"><div><h2>个人信息</h2><p>维护用于政务服务联系的个人资料</p></div></div>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top">
            <div class="form-grid">
              <el-form-item label="用户名"><el-input :model-value="userStore.profile.username" disabled /></el-form-item>
              <el-form-item label="所属部门"><el-input :model-value="userStore.profile.deptCode" disabled /></el-form-item>
              <el-form-item label="真实姓名" prop="realName"><el-input v-model="profileForm.realName" /></el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-select v-model="profileForm.gender" style="width:100%"><el-option label="男" value="男"/><el-option label="女" value="女"/><el-option label="其他" value="其他"/></el-select>
              </el-form-item>
              <el-form-item label="手机号码" prop="phone"><el-input v-model="profileForm.phone" maxlength="11" /></el-form-item>
              <el-form-item label="电子邮箱" prop="email"><el-input v-model="profileForm.email" /></el-form-item>
              <el-form-item label="身份证号"><el-input :model-value="maskedIdCard" disabled /></el-form-item>
              <el-form-item label="联系地址" prop="address"><el-input v-model="profileForm.address" /></el-form-item>
            </div>
            <div class="form-actions"><el-button type="primary" :loading="savingProfile" @click="saveProfile">保存修改</el-button></div>
          </el-form>
        </template>

        <template v-else>
          <div class="section-title"><div><h2>修改密码</h2><p>修改成功后需要使用新密码重新登录</p></div></div>
          <el-form ref="passwordFormRef" class="password-form" :model="passwordForm" :rules="passwordRules" label-position="top">
            <el-form-item label="原密码" prop="oldPassword"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码" prop="newPassword"><el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少8位，包含字母和数字" /></el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
            <div class="form-actions"><el-button type="primary" :loading="savingPassword" @click="savePassword">确认修改</el-button></div>
          </el-form>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, SwitchButton, User } from '@element-plus/icons-vue'
import { adminLogout, changePassword, updateProfile } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref<'info' | 'password'>('info')
const savingProfile = ref(false)
const savingPassword = ref(false)
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const avatarText = computed(() => (userStore.username || '用户').slice(0, 1))
const maskedIdCard = computed(() => {
  const value = userStore.profile.idCard || ''
  return value.length === 18 ? value.slice(0, 6) + '********' + value.slice(-4) : value
})
const profileForm = reactive({ realName: '', gender: '', phone: '', email: '', address: '' })
watch(() => userStore.profile, (value) => {
  Object.assign(profileForm, {
    realName: value.realName || '', gender: value.gender || '', phone: value.phone || '',
    email: value.email || '', address: value.address || ''
  })
}, { immediate: true, deep: true })
const profileRules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, pattern: /^1[0-9]{10}$/, message: '请输入有效的手机号', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入有效的邮箱', trigger: 'blur' }],
  address: [{ required: true, message: '请输入联系地址', trigger: 'blur' }]
}
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$/, message: '至少8位且包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (_rule, value, callback) => value === passwordForm.newPassword ? callback() : callback(new Error('两次输入的新密码不一致')), trigger: 'blur' }
  ]
}
async function saveProfile() {
  if (!profileFormRef.value || !await profileFormRef.value.validate().catch(() => false)) return
  savingProfile.value = true
  try {
    const res: any = await updateProfile(profileForm)
    userStore.setUser(res.data)
    ElMessage.success('个人资料已保存')
  } finally { savingProfile.value = false }
}
async function savePassword() {
  if (!passwordFormRef.value || !await passwordFormRef.value.validate().catch(() => false)) return
  savingPassword.value = true
  try {
    await changePassword(passwordForm)
    userStore.clearSession('portal')
    ElMessage.success('密码修改成功，请重新登录')
    router.replace('/login')
  } finally { savingPassword.value = false }
}
async function logout() {
  await ElMessageBox.confirm('确定退出当前账号吗？', '退出登录', { type: 'warning', confirmButtonText: '退出', cancelButtonText: '取消' })
  try { await adminLogout() } finally {
    userStore.clearSession('portal')
    router.replace('/')
  }
}
</script>

<style scoped>
.profile-page{width:min(1120px,92%);margin:0 auto;padding:38px 0 64px}.profile-heading{display:flex;align-items:center;gap:18px;padding:0 0 28px;border-bottom:1px solid #dbe3ea}.profile-avatar{width:64px;height:64px;display:grid;place-items:center;border-radius:50%;background:#176ead;color:#fff;font-size:26px;font-weight:700}.profile-heading h1{margin:0 0 7px;font-size:25px;color:#263746}.profile-heading p{margin:0;color:#74808a}.profile-layout{display:grid;grid-template-columns:210px minmax(0,1fr);gap:42px;padding-top:28px}.profile-nav{display:flex;flex-direction:column;border-right:1px solid #dbe3ea;padding-right:20px}.profile-nav button{height:48px;display:flex;align-items:center;gap:12px;padding:0 15px;border:0;border-left:3px solid transparent;background:transparent;color:#455563;font-size:15px;cursor:pointer;text-align:left}.profile-nav button:hover,.profile-nav button.active{background:#eef6fc;color:#1269a9;border-left-color:#1684d5}.profile-nav .logout-item{margin-top:18px;color:#b4232a}.profile-content{min-width:0}.section-title{display:flex;justify-content:space-between;align-items:start;margin-bottom:26px}.section-title h2{margin:0 0 7px;color:#263746;font-size:22px}.section-title p{margin:0;color:#7b8791}.form-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:0 28px}.form-actions{padding-top:8px;border-top:1px solid #e4e9ed}.form-actions .el-button{min-width:120px;height:42px}.password-form{max-width:520px}@media(max-width:760px){.profile-layout{grid-template-columns:1fr;gap:24px}.profile-nav{display:grid;grid-template-columns:repeat(3,1fr);border-right:0;border-bottom:1px solid #dbe3ea;padding:0 0 16px}.profile-nav button{justify-content:center;padding:0 8px}.profile-nav .logout-item{margin-top:0}.form-grid{grid-template-columns:1fr}}
</style>
