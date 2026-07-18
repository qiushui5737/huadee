<template>
  <div class="ai-audit-page">
    <div class="page-header">
      <div class="header-title">
        <h2>AI审核管理</h2>
        <p>智能审核敏感内容，保障平台安全</p>
      </div>
    </div>

    <div class="page-content">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Search /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.total }}</span>
            <span class="stat-label">审核总量</span>
          </div>
        </div>
        <div class="stat-card warning">
          <div class="stat-icon">
            <el-icon><AlertTriangle /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.sensitive }}</span>
            <span class="stat-label">敏感内容</span>
          </div>
        </div>
        <div class="stat-card success">
          <div class="stat-icon">
            <el-icon><CheckCircle /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.passed }}</span>
            <span class="stat-label">已通过</span>
          </div>
        </div>
        <div class="stat-card danger">
          <div class="stat-icon">
            <el-icon><CloseCircle /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.rejected }}</span>
            <span class="stat-label">已拒绝</span>
          </div>
        </div>
      </div>

      <el-card shadow="hover" class="content-card">
        <template #header>
          <div class="card-header">
            <span>审核记录</span>
            <el-select v-model="statusFilter" placeholder="全部状态" style="width:150px;">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="passed" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </div>
        </template>

        <el-table :data="auditRecords" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="type" label="内容类型" width="120">
            <template #default="scope">
              <el-tag size="small">{{ getTypeName(scope.row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="300">
            <template #default="scope">
              <span :title="scope.row.content">{{ scope.row.content.length > 50 ? scope.row.content.substring(0, 50) + '...' : scope.row.content }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="riskLevel" label="风险等级" width="100">
            <template #default="scope">
              <el-tag :type="getRiskType(scope.row.riskLevel)" size="small">
                {{ getRiskName(scope.row.riskLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusName(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditTime" label="审核时间" width="180" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button v-if="scope.row.status === 'pending'" size="small" type="primary" @click="handleApprove(scope.row)">通过</el-button>
              <el-button v-if="scope.row.status === 'pending'" size="small" type="danger" @click="handleReject(scope.row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Search, AlertTriangle, CheckCircle, CloseCircle } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const statusFilter = ref('')

const stats = reactive({
  total: 1258,
  sensitive: 234,
  passed: 892,
  rejected: 132
})

const auditRecords = ref([
  { id: 1, type: 'message', content: '这是一条测试消息内容，用于测试AI审核功能', riskLevel: 'low', status: 'passed', auditTime: '2026-07-18 10:30:00' },
  { id: 2, type: 'letter', content: '涉及敏感关键词的信访内容，需要人工审核确认', riskLevel: 'high', status: 'pending', auditTime: '' },
  { id: 3, type: 'suggest', content: '用户提交的意见建议，内容正常无敏感信息', riskLevel: 'none', status: 'passed', auditTime: '2026-07-18 09:15:00' },
  { id: 4, type: 'chat', content: '聊天消息中包含疑似广告内容', riskLevel: 'medium', status: 'rejected', auditTime: '2026-07-18 08:45:00' }
])

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    message: '互动消息',
    letter: '信访来信',
    suggest: '意见建议',
    chat: '聊天内容'
  }
  return map[type] || type
}

const getRiskType = (level: string) => {
  switch (level) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return ''
    default: return 'success'
  }
}

const getRiskName = (level: string) => {
  const map: Record<string, string> = {
    high: '高风险',
    medium: '中风险',
    low: '低风险',
    none: '无风险'
  }
  return map[level] || level
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'passed': return 'success'
    case 'rejected': return 'danger'
    default: return ''
  }
}

const getStatusName = (status: string) => {
  const map: Record<string, string> = {
    pending: '待审核',
    passed: '已通过',
    rejected: '已拒绝'
  }
  return map[status] || status
}

const handleApprove = (row: any) => {
  row.status = 'passed'
  row.auditTime = new Date().toLocaleString('zh-CN')
  ElMessage.success('审核通过')
}

const handleReject = (row: any) => {
  row.status = 'rejected'
  row.auditTime = new Date().toLocaleString('zh-CN')
  ElMessage.success('审核拒绝')
}
</script>

<style scoped>
.ai-audit-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.page-header {
  margin-bottom: 24px;
}

.header-title h2 {
  font-size: 24px;
  margin: 0 0 8px 0;
}

.header-title p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.page-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.stats-row {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  color: #fff;
}

.stat-card.warning {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.stat-card.success {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.stat-card.danger {
  background: linear-gradient(135deg, #f56c6c 0%, #cf1322 100%);
}

.stat-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.content-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>