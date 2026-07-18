<template>
  <div class="profile-page">
    <div class="page-header">
      <div class="header-content">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item><a href="/">首页</a></el-breadcrumb-item>
          <el-breadcrumb-item>个人中心</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">个人中心</h1>
      </div>
    </div>

    <div class="page-content">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar">
            <el-avatar :size="120" icon="User" />
          </div>
          <div class="user-info">
            <h2>{{ userInfo.name || '用户' }}</h2>
            <p class="user-id">用户ID: {{ userInfo.id }}</p>
          </div>
        </div>

        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="基本信息" name="basic">
            <el-form :model="userInfo" label-width="120px">
              <el-form-item label="用户名">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
              <el-form-item label="姓名">
                <el-input v-model="userInfo.name" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="userInfo.phone" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userInfo.email" />
              </el-form-item>
              <el-form-item label="地址">
                <el-input v-model="userInfo.address" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-form ref="passwordFormRef" :model="passwordForm" label-width="120px" :rules="passwordRules">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="我的办件" name="records">
            <el-table :data="records" border>
              <el-table-column prop="acceptNo" label="受理号" />
              <el-table-column prop="itemName" label="事项名称" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="提交时间" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" @click="viewRecord(scope.row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')
const passwordFormRef = ref()

const userInfo = reactive({
  id: 'U001',
  username: 'user001',
  name: '',
  phone: '',
  email: '',
  address: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (rule: any, value: string, callback: any) => {
      if (value !== passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

const records = ref([
  { acceptNo: 'SL20260718001', itemName: '户籍迁移', status: '审核中', submitTime: '2026-07-18 10:30:00' },
  { acceptNo: 'SL20260718002', itemName: '社保缴纳', status: '已办结', submitTime: '2026-07-18 09:15:00' }
])

const getStatusType = (status: string) => {
  switch (status) {
    case '提交申请': return 'warning'
    case '审核中': return ''
    case '已审批': return 'primary'
    case '已办结': return 'success'
    case '已驳回': return 'danger'
    default: return ''
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate((valid: boolean) => {
    if (!valid) return
    ElMessage.success('密码修改成功，请重新登录')
  })
}

const viewRecord = (row: any) => {
  ElMessage.info(`查看办件: ${row.acceptNo}`)
}
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - 100px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.page-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 30px 40px;
  color: #fff;
}

.header-content {
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-top: 16px;
  margin-bottom: 0;
}

.page-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 40px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 30px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 20px;
}

.avatar {
  flex-shrink: 0;
}

.user-info h2 {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #333;
}

.user-id {
  color: #999;
  font-size: 14px;
  margin: 0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-tabs__content) {
  padding: 20px 0;
}
</style>