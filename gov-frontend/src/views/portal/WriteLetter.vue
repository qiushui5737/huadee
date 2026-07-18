<template>
  <div class="write-letter-page">
    <div class="page-header">
      <div class="header-content">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item><a href="/">首页</a></el-breadcrumb-item>
          <el-breadcrumb-item><a href="/interaction">互动交流</a></el-breadcrumb-item>
          <el-breadcrumb-item>网上信访</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">网上信访</h1>
      </div>
    </div>

    <div class="page-content">
      <el-card shadow="hover" class="letter-card">
        <template #header>
          <div class="card-header">
            <el-icon><MessageSquare /></el-icon>
            <span>填写信访信息</span>
          </div>
        </template>

        <el-form ref="formRef" :model="formData" label-width="120px" :rules="formRules">
          <el-form-item label="来信类型" prop="type">
            <el-select v-model="formData.type" placeholder="请选择来信类型">
              <el-option label="投诉建议" value="complaint" />
              <el-option label="咨询求助" value="consult" />
              <el-option label="意见反馈" value="feedback" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>

          <el-form-item label="主题" prop="title">
            <el-input v-model="formData.title" placeholder="请输入来信主题" />
          </el-form-item>

          <el-form-item label="姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入您的姓名" />
          </el-form-item>

          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入联系电话" />
          </el-form-item>

          <el-form-item label="电子邮箱">
            <el-input v-model="formData.email" placeholder="请输入电子邮箱（选填）" />
          </el-form-item>

          <el-form-item label="来信内容" prop="content">
            <el-input v-model="formData.content" type="textarea" :rows="6" placeholder="请详细描述您的诉求..." />
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button @click="handleCancel">
                <el-icon><ArrowLeft /></el-icon> 返回
              </el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                <el-icon><Send /></el-icon> 提交来信
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { MessageSquare, ArrowLeft, Send } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const formData = reactive({
  type: '',
  title: '',
  name: '',
  phone: '',
  email: '',
  content: ''
})

const formRules = {
  type: [
    { required: true, message: '请选择来信类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入来信主题', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入来信内容', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      ElMessage.success('来信提交成功，我们会尽快处理')
      setTimeout(() => {
        router.push('/interaction')
      }, 1500)
    } catch (error) {
      ElMessage.error('提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const handleCancel = () => {
  router.push('/interaction')
}
</script>

<style scoped>
.write-letter-page {
  min-height: calc(100vh - 100px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.page-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 30px 40px;
  color: #fff;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-top: 16px;
  margin-bottom: 0;
}

.page-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 40px;
}

.letter-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>