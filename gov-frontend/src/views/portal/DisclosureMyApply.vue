<template>
  <div class="my-apply-page">
    <div class="page-header">
      <h1>我的申请</h1>
      <p class="subtitle">查看您的政府信息公开申请记录</p>
    </div>

    <el-card shadow="hover" style="max-width: 900px; margin: 0 auto;">
      <template #header>
        <div class="card-header">
          <span>申请查询</span>
          <el-button link type="primary" @click="$router.push('/disclosure')">
            <el-icon><Back /></el-icon> 返回在线申请
          </el-button>
        </div>
      </template>

      <!-- 查询区域 -->
      <div class="query-section">
        <el-input v-model="idCard" placeholder="请输入身份证号查询我的申请记录" style="max-width: 400px;" clearable @keyup.enter="handleQuery">
          <template #prepend><el-icon><User /></el-icon></template>
          <template #append>
            <el-button @click="handleQuery" :loading="loading">查询</el-button>
          </template>
        </el-input>
      </div>

      <!-- 申请列表 -->
      <div v-if="applications.length > 0" class="apply-list">
        <el-card v-for="item in applications" :key="item.id" shadow="never" class="apply-item">
          <div class="apply-header">
            <div class="apply-no">
              <el-icon><Document /></el-icon>
              <span>申请编号：{{ item.applyNo }}</span>
            </div>
            <el-tag :type="statusTagType(item.status)" size="large">{{ item.status }}</el-tag>
          </div>

          <el-descriptions :column="2" border size="small" style="margin-top: 12px;">
            <el-descriptions-item label="申请人">{{ item.applicant }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ item.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请文件">
              <span v-if="item.fileInfo">
                <el-icon><Document /></el-icon> {{ item.fileInfo.fileName }}
                <el-tag size="small" type="info" style="margin-left: 4px;">{{ item.fileInfo.deptName }}</el-tag>
              </span>
              <span v-else style="color: #999;">未指定</span>
            </el-descriptions-item>
            <el-descriptions-item label="获取方式">{{ item.acquireMethod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ item.createTime }}</el-descriptions-item>
            <el-descriptions-item label="答复期限">
              <span :style="{ color: isOverdue(item) ? '#f56c6c' : '' }">{{ item.deadline || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="申请内容" :span="2" style="white-space: pre-wrap;">{{ item.content }}</el-descriptions-item>
            <el-descriptions-item v-if="item.replyContent" label="答复内容" :span="2" style="white-space: pre-wrap;">{{ item.replyContent }}</el-descriptions-item>
            <el-descriptions-item v-if="item.replyBy" label="答复人">{{ item.replyBy }}</el-descriptions-item>
            <el-descriptions-item v-if="item.replyTime" label="答复时间">{{ item.replyTime }}</el-descriptions-item>
          </el-descriptions>

          <!-- 审批流程 -->
          <div v-if="item._auditRecords && item._auditRecords.length > 0" style="margin-top: 12px;">
            <el-button link type="primary" size="small" @click="toggleRecords(item)">
              {{ item._showRecords ? '收起' : '查看' }}审批流程
              <el-icon><ArrowDown v-if="!item._showRecords" /><ArrowUp v-else /></el-icon>
            </el-button>
            <el-timeline v-if="item._showRecords" style="margin-top: 10px;">
              <el-timeline-item v-for="record in item._auditRecords" :key="record.id" :timestamp="record.operateTime" placement="top" :type="record.action === 'approve' ? 'success' : record.action === 'reject' ? 'danger' : 'primary'">
                <div>{{ record.nodeName }} - {{ record.operatorName }}</div>
                <div v-if="record.opinion" style="color: #666; font-size: 12px;">{{ record.opinion }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>

          <!-- 操作按钮 -->
          <div class="apply-actions">
            <el-button v-if="item.status === '已答复' && item.fileId" type="primary" size="small" @click="handleViewFile(item)">
              <el-icon><Download /></el-icon> 查看授权文件
            </el-button>
            <el-button link type="info" size="small" @click="loadAuditRecordsForItem(item)">
              <el-icon><List /></el-icon> 审批记录
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="queried && applications.length === 0" description="未找到申请记录" />
      <el-empty v-if="!queried" description="输入身份证号查询您的申请记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, User, Back, Download, List, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { myDisclosureApplications, disclosureAuditRecords, disclosureFileAccess } from '@/api/interaction'

const router = useRouter()
const idCard = ref('')
const applications = ref<any[]>([])
const loading = ref(false)
const queried = ref(false)

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '已受理': 'warning', '审核中': '', '已答复': 'success', '已驳回': 'danger' }
  return map[status] || ''
}

const isOverdue = (item: any) => {
  if (!item.deadline || item.status === '已答复' || item.status === '已驳回') return false
  return new Date(item.deadline) < new Date()
}

const handleQuery = async () => {
  if (!idCard.value.trim()) {
    ElMessage.warning('请输入身份证号')
    return
  }
  loading.value = true
  queried.value = true
  try {
    const res: any = await myDisclosureApplications(idCard.value.trim())
    if (res.code === 200) {
      applications.value = res.data.map((item: any) => ({
        ...item,
        _auditRecords: null,
        _showRecords: false
      }))
      // 自动加载每个申请的审批记录
      for (const item of applications.value) {
        await loadAuditRecordsForItem(item)
      }
    } else {
      ElMessage.error(res.message || '查询失败')
      applications.value = []
    }
  } catch (e) {
    ElMessage.error('查询失败')
    applications.value = []
  } finally {
    loading.value = false
  }
}

const loadAuditRecordsForItem = async (item: any) => {
  if (item._auditRecords) return
  try {
    const res: any = await disclosureAuditRecords(item.applyNo)
    if (res.code === 200) {
      item._auditRecords = res.data
    }
  } catch (e) {
    console.error('加载审批记录失败', e)
  }
}

const toggleRecords = (item: any) => {
  item._showRecords = !item._showRecords
}

const handleViewFile = async (item: any) => {
  if (!item.fileId) {
    ElMessage.warning('没有关联文件')
    return
  }
  try {
    const res: any = await disclosureFileAccess(item.applyNo, item.fileId)
    if (res.code === 200) {
      const fileData = res.data
      if (fileData.fileUrl) {
        window.open(fileData.fileUrl, '_blank')
      } else {
        ElMessage.warning('文件链接不可用，可能文件尚未上传')
      }
    } else {
      ElMessage.error(res.message || '获取文件失败')
    }
  } catch (e) {
    ElMessage.error('获取文件失败')
  }
}
</script>

<style scoped lang="scss">
.my-apply-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  text-align: center;
  margin-bottom: 30px;
  h1 { font-size: 28px; color: #1a3a5c; margin: 0; }
  .subtitle { color: #888; margin-top: 8px; font-size: 14px; }
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}
.query-section {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.apply-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.apply-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }
}
.apply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  .apply-no {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }
}
.apply-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
