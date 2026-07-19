<template>
  <div class="questionnaire-page">
    <div class="page-header">
      <div class="header-content">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item><a href="/">首页</a></el-breadcrumb-item>
          <el-breadcrumb-item>问卷调查</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">问卷调查</h1>
      </div>
    </div>

    <div class="page-content">
      <el-card shadow="hover" class="question-card">
        <template #header>
          <div class="card-header">
            <el-icon><List /></el-icon>
            <span>满意度调查</span>
          </div>
        </template>

        <el-form ref="formRef" :model="formData" label-width="0" :rules="formRules">
          <div class="question-item">
            <h3>1. 您对政务服务的整体满意度如何？</h3>
            <el-radio-group v-model="formData.q1">
              <el-radio value="very_satisfied">非常满意</el-radio>
              <el-radio value="satisfied">满意</el-radio>
              <el-radio value="general">一般</el-radio>
              <el-radio value="dissatisfied">不满意</el-radio>
            </el-radio-group>
          </div>

          <div class="question-item">
            <h3>2. 您对办事效率的评价？</h3>
            <el-radio-group v-model="formData.q2">
              <el-radio value="very_satisfied">非常满意</el-radio>
              <el-radio value="satisfied">满意</el-radio>
              <el-radio value="general">一般</el-radio>
              <el-radio value="dissatisfied">不满意</el-radio>
            </el-radio-group>
          </div>

          <div class="question-item">
            <h3>3. 您对工作人员服务态度的评价？</h3>
            <el-radio-group v-model="formData.q3">
              <el-radio value="very_satisfied">非常满意</el-radio>
              <el-radio value="satisfied">满意</el-radio>
              <el-radio value="general">一般</el-radio>
              <el-radio value="dissatisfied">不满意</el-radio>
            </el-radio-group>
          </div>

          <div class="question-item">
            <h3>4. 您对网站界面的评价？</h3>
            <el-radio-group v-model="formData.q4">
              <el-radio value="very_satisfied">非常满意</el-radio>
              <el-radio value="satisfied">满意</el-radio>
              <el-radio value="general">一般</el-radio>
              <el-radio value="dissatisfied">不满意</el-radio>
            </el-radio-group>
          </div>

          <div class="question-item">
            <h3>5. 您的其他建议（选填）</h3>
            <el-input v-model="formData.suggestion" type="textarea" :rows="4" placeholder="请输入您的建议..." />
          </div>

          <div class="form-actions">
            <el-button @click="handleCancel">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              <el-icon><Check /></el-icon> 提交问卷
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { List, ArrowLeft, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const formData = reactive({
  q1: '',
  q2: '',
  q3: '',
  q4: '',
  suggestion: ''
})

const formRules = {
  q1: [
    { required: true, message: '请选择您的评价', trigger: 'change' }
  ],
  q2: [
    { required: true, message: '请选择您的评价', trigger: 'change' }
  ],
  q3: [
    { required: true, message: '请选择您的评价', trigger: 'change' }
  ],
  q4: [
    { required: true, message: '请选择您的评价', trigger: 'change' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      ElMessage.success('问卷提交成功，感谢您的参与')
      setTimeout(() => {
        router.push('/')
      }, 1500)
    } catch (error) {
      ElMessage.error('提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const handleCancel = () => {
  router.push('/')
}
</script>

<style scoped>
.questionnaire-page {
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

.question-card {
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

.question-item {
  margin: 30px 0;
  padding-bottom: 20px;
  border-bottom: 1px dashed #e8e8e8;
}

.question-item:last-child {
  border-bottom: none;
}

.question-item h3 {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

:deep(.el-radio) {
  margin-right: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>