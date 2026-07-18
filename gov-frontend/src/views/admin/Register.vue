<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <h2>用户注册</h2>
          <p>创建您的账号</p>
        </div>

        <el-form ref="formRef" :model="formData" label-width="0" :rules="formRules" class="register-form">
          <el-form-item prop="username">
            <el-input v-model="formData.username" placeholder="用户名" prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="formData.password" type="password" placeholder="密码" prefix-icon="Lock" />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input v-model="formData.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" />
          </el-form-item>

          <el-form-item prop="realName">
            <el-input v-model="formData.realName" placeholder="真实姓名" prefix-icon="UserFilled" />
          </el-form-item>

          <el-form-item prop="phone">
            <el-input v-model="formData.phone" placeholder="联系电话" prefix-icon="Phone" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleRegister" :loading="loading" class="primary-btn">
              注册
            </el-button>
          </el-form-item>

          <div class="register-footer">
            <span>已有账号？</span>
            <a href="/login">立即登录</a>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, UserFilled, Phone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/admin'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: ''
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule: any, value: string, callback: any) => {
      if (value !== formData.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    loading.value = true
    
    try {
      const res = await register(formData)
      if (res.code === 200) {
        ElMessage.success(res.message)
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('注册失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-container {
  width: 400px;
}

.register-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
}

.register-header p {
  color: #999;
  margin: 0;
}

.register-form {
  width: 100%;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.primary-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
}

.register-footer a {
  color: #1890ff;
  text-decoration: none;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>