<template>
  <div class="service-apply-page">
    <div class="apply-header">
      <div class="header-content">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item><a href="/">首页</a></el-breadcrumb-item>
          <el-breadcrumb-item><a href="/service">办事服务</a></el-breadcrumb-item>
          <el-breadcrumb-item>{{ itemName }}</el-breadcrumb-item>
          <el-breadcrumb-item>在线申报</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="apply-title">{{ itemName }} - 在线申报</h1>
      </div>
    </div>

    <div class="apply-content">
      <div class="form-section">
        <el-card shadow="hover" class="form-card">
          <template #header>
            <div class="card-header">
              <el-icon><EditPen /></el-icon>
              <span>填写申请表单</span>
            </div>
          </template>

          <el-form ref="formRef" :model="formData" label-width="120px" :rules="formRules">
            <el-form-item label="受理号" prop="acceptNo">
              <el-input v-model="formData.acceptNo" disabled />
            </el-form-item>

            <el-form-item label="事项名称" prop="itemName">
              <el-input v-model="formData.itemName" disabled />
            </el-form-item>

            <el-form-item label="申请人姓名" prop="userName">
              <el-input v-model="formData.userName" placeholder="请输入申请人姓名" />
            </el-form-item>

            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>

            <el-form-item label="联系地址" prop="address">
              <el-input v-model="formData.address" type="textarea" :rows="2" placeholder="请输入联系地址" />
            </el-form-item>

            <el-form-item label="申请事由" prop="reason">
              <el-input v-model="formData.reason" type="textarea" :rows="3" placeholder="请详细说明申请事由" />
            </el-form-item>

            <el-form-item label="附件材料">
              <el-upload
                class="upload-demo"
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :file-list="fileList"
                :limit="5"
              >
                <el-button type="primary">
                  <el-icon><Upload /></el-icon> 点击上传
                </el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 PDF、图片、Word 等格式，最多上传5个文件</div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item>
              <div class="form-actions">
                <el-button @click="handleCancel">
                  <el-icon><ArrowLeft /></el-icon> 返回
                </el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitting">
                  <el-icon><Check /></el-icon> 提交申请
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" class="preview-card">
          <template #header>
            <div class="card-header">
              <el-icon><View /></el-icon>
              <span>表单预览</span>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="受理号">{{ formData.acceptNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="事项名称">{{ formData.itemName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请人">{{ formData.userName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ formData.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系地址">{{ formData.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请事由">{{ formData.reason || '-' }}</el-descriptions-item>
            <el-descriptions-item label="附件数量">{{ fileList.length }} 个</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ new Date().toLocaleString('zh-CN') }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>

      <div class="sidebar-section">
        <el-card shadow="hover" class="tips-card">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>温馨提示</span>
            </div>
          </template>

          <ul class="tips-list">
            <li>请确保填写的信息真实准确</li>
            <li>提交后可在进度查询中查看办理状态</li>
            <li>如有疑问可拨打咨询电话</li>
            <li>材料不全可能会影响审批进度</li>
          </ul>
        </el-card>

        <el-card shadow="hover" class="guide-card">
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>办理流程</span>
            </div>
          </template>

          <el-steps :active="0" align-center>
            <el-step title="提交申请" description="填写表单并提交" />
            <el-step title="材料审核" description="工作人员审核材料" />
            <el-step title="审批决定" description="领导审批" />
            <el-step title="办结通知" description="办理完成" />
          </el-steps>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { EditPen, Upload, ArrowLeft, Check, View, InfoFilled, List } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { submitForm } from '@/api/service'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const itemName = ref('')
const fileList = ref<any[]>([])

const formData = reactive({
  acceptNo: '',
  itemName: '',
  userName: '',
  phone: '',
  address: '',
  reason: ''
})

const formRules = {
  userName: [
    { required: true, message: '请输入申请人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入联系地址', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入申请事由', trigger: 'blur' }
  ]
}

const handleFileChange = (file: any) => {
  if (file.status === 'ready') {
    fileList.value.push(file)
  } else if (file.status === 'removed') {
    fileList.value = fileList.value.filter(f => f.uid !== file.uid)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      const res = await submitForm({
        itemId: route.query.itemId,
        userName: formData.userName,
        phone: formData.phone,
        address: formData.address,
        reason: formData.reason,
        formData: JSON.stringify(formData)
      })
      
      if (res.code === 200) {
        ElMessage.success(res.message)
        setTimeout(() => {
          router.push('/service')
        }, 1500)
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const handleCancel = () => {
  router.push('/service')
}

onMounted(() => {
  formData.acceptNo = 'SL' + Date.now() + Math.floor(Math.random() * 1000)
  formData.itemName = route.query.itemName as string || '在线申报事项'
  itemName.value = formData.itemName
})
</script>

<style scoped>
.service-apply-page {
  min-height: calc(100vh - 100px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.apply-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 30px 40px;
  color: #fff;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
}

.apply-title {
  font-size: 28px;
  font-weight: bold;
  margin-top: 16px;
  margin-bottom: 0;
}

.apply-content {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 40px;
}

.form-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-card, .preview-card {
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

.tips-card, .guide-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.tips-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.tips-list li {
  padding: 10px 0;
  border-bottom: 1px dashed #f0f0f0;
  font-size: 14px;
  color: #666;
  position: relative;
  padding-left: 20px;
}

.tips-list li:last-child {
  border-bottom: none;
}

.tips-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #52c41a;
  font-weight: bold;
}

:deep(.el-steps) {
  margin-top: 20px;
}

:deep(.el-step__title) {
  font-size: 12px;
}

:deep(.el-step__description) {
  font-size: 10px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload-dragger) {
  border-radius: 8px;
}
</style>