<template>
  <div class="approval-page">
    <div class="page-header">
      <div class="header-title">
        <h2>办事审批工作台</h2>
        <p>高效处理群众办事申请，提升政务服务效率</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
        <div class="stat-item processing">
          <span class="stat-value">{{ stats.processing }}</span>
          <span class="stat-label">审核中</span>
        </div>
        <div class="stat-item approved">
          <span class="stat-value">{{ stats.approved }}</span>
          <span class="stat-label">已审批</span>
        </div>
        <div class="stat-item completed">
          <span class="stat-value">{{ stats.completed }}</span>
          <span class="stat-label">已办结</span>
        </div>
        <div class="stat-item pending-inquiry">
          <span class="stat-value">{{ pendingInquiries }}</span>
          <span class="stat-label">待回复询问</span>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="card">
      <el-tab-pane label="审批管理" name="approval">
        <div class="page-content">
          <div class="filter-bar">
            <el-select v-model="statusFilter" placeholder="全部状态" style="width:150px;">
              <el-option label="全部" value="" />
              <el-option label="提交申请" value="提交申请" />
              <el-option label="审核中" value="审核中" />
              <el-option label="已审批" value="已审批" />
              <el-option label="已办结" value="已办结" />
              <el-option label="已驳回" value="已驳回" />
            </el-select>
            <el-input v-model="keyword" placeholder="搜索受理号或申请人" style="width:250px; margin-left:10px;" @keyup.enter="loadRecords">
              <template #append>
                <el-button @click="loadRecords"><el-icon><Search /></el-icon></el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="loadRecords">查询</el-button>
            <el-button @click="handleBatchApprove" :disabled="selectedRows.length === 0">批量通过</el-button>
            <el-button type="danger" @click="handleBatchReject" :disabled="selectedRows.length === 0">批量驳回</el-button>
          </div>

          <el-card class="record-card">
            <el-table :data="records" border @selection-change="handleSelectionChange" :loading="loading">
              <el-table-column type="selection" width="50" />
              <el-table-column prop="acceptNo" label="受理号" width="180" />
              <el-table-column prop="itemName" label="事项名称" min-width="150" />
              <el-table-column prop="category" label="分类" width="100">
                <template #default="scope">
                  <el-tag size="small">{{ getCategoryName(scope.row.category) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="userName" label="申请人" width="100" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="提交时间" width="180" />
              <el-table-column prop="finishTime" label="办结时间" width="180" />
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
                  <el-button size="small" @click="handleApprove(scope.row)" v-if="scope.row.status !== '已办结'">通过</el-button>
                  <el-button type="danger" size="small" @click="handleReject(scope.row)" v-if="scope.row.status === '提交申请' || scope.row.status === '审核中' || scope.row.status === '已审批'">驳回</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="询问管理" name="inquiry">
        <div class="page-content">
          <div class="filter-bar">
            <el-select v-model="inquiryFilter" placeholder="全部状态" style="width:150px;">
              <el-option label="全部" value="" />
              <el-option label="待回复" value="pending" />
              <el-option label="已回复" value="replied" />
            </el-select>
            <el-input v-model="inquiryKeyword" placeholder="搜索受理号或申请人" style="width:250px; margin-left:10px;" @keyup.enter="loadInquiries">
              <template #append>
                <el-button @click="loadInquiries"><el-icon><Search /></el-icon></el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="loadInquiries">查询</el-button>
          </div>

          <el-card class="record-card">
            <el-table :data="inquiries" border :loading="inquiryLoading">
              <el-table-column prop="acceptNo" label="受理号" width="180" />
              <el-table-column prop="userName" label="申请人" width="100" />
              <el-table-column prop="content" label="询问内容" min-width="300">
                <template #default="scope">
                  <span :title="scope.row.content">{{ scope.row.content.length > 50 ? scope.row.content.substring(0, 50) + '...' : scope.row.content }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="reply" label="回复内容" min-width="300">
                <template #default="scope">
                  <span v-if="scope.row.reply" :title="scope.row.reply">{{ scope.row.reply.length > 50 ? scope.row.reply.substring(0, 50) + '...' : scope.row.reply }}</span>
                  <span v-else class="text-danger">待回复</span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="询问时间" width="180" />
              <el-table-column prop="replyTime" label="回复时间" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="handleReply(scope.row)" v-if="!scope.row.reply">回复</el-button>
                  <el-tag size="small" type="success" v-else>已回复</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="detailDialogVisible" title="办件详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="受理号" :span="2">{{ detailRecord.acceptNo }}</el-descriptions-item>
        <el-descriptions-item label="事项名称">{{ detailRecord.itemName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryName(detailRecord.category) }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailRecord.userName }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(detailRecord.status)">{{ detailRecord.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailRecord.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="办结时间">{{ detailRecord.finishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批意见">{{ detailRecord.comment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="表单数据" :span="2">
          <pre style="max-height:200px; overflow-y:auto; background:#f5f5f5; padding:10px; border-radius:4px;">{{ formatFormData(detailRecord.formData) }}</pre>
        </el-descriptions-item>
      </el-descriptions>

      <div style="margin-top:20px;">
        <el-steps :active="getStepIndex(detailRecord.status)" align-center>
          <el-step title="提交申请" description="群众提交申请" />
          <el-step title="材料审核" description="审核员审核材料" />
          <el-step title="审批决定" description="领导审批" />
          <el-step title="办结通知" description="办理完成" />
        </el-steps>
      </div>

      <div style="margin-top:20px;">
        <h4>询问记录</h4>
        <div v-if="detailInquiries.length > 0" class="inquiry-list">
          <div v-for="inq in detailInquiries" :key="inq.id" class="inquiry-item">
            <div class="inquiry-header">
              <span class="inquiry-time">{{ inq.createTime }}</span>
              <el-tag :type="inq.reply ? 'success' : 'warning'" size="small">{{ inq.reply ? '已回复' : '待回复' }}</el-tag>
            </div>
            <div class="inquiry-content">
              <span class="label">询问：</span>
              <span>{{ inq.content }}</span>
            </div>
            <div v-if="inq.reply" class="inquiry-reply">
              <span class="label">回复：</span>
              <span>{{ inq.reply }}</span>
              <span class="reply-time">{{ inq.replyTime }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-empty description="暂无询问记录" :image-size="80" />
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleApprove(detailRecord)" v-if="detailRecord.status !== '已办结'">通过</el-button>
        <el-button type="danger" @click="handleReject(detailRecord)" v-if="detailRecord.status === '提交申请' || detailRecord.status === '审核中' || detailRecord.status === '已审批'">驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveDialogVisible" :title="isReject ? '驳回申请' : '通过申请'" width="500px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见（选填）" />
        </el-form-item>
        <el-form-item v-if="!isReject">
          <el-checkbox v-model="approveForm.completeDirectly">直接办结</el-checkbox>
          <span class="form-tip">勾选后将跳过中间步骤，直接完成审批流程</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmApprove">{{ isReject.value ? '确认驳回' : '确认通过' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="replyDialogVisible" title="回复询问" width="500px">
      <el-form :model="replyForm" label-width="100px">
        <el-form-item label="询问内容">
          <el-input :value="currentInquiry.content" type="textarea" :rows="3" readonly />
        </el-form-item>
        <el-form-item label="回复内容" required>
          <el-input v-model="replyForm.reply" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReply">确认回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { approvalRecords, approve, batchApprove, getInquiries, getAllInquiries, replyInquiry } from '@/api/service'

const loading = ref(false)
const records = ref<any[]>([])
const statusFilter = ref('')
const keyword = ref('')
const selectedRows = ref<any[]>([])

const activeTab = ref('approval')
const inquiryLoading = ref(false)
const inquiries = ref<any[]>([])
const inquiryFilter = ref('')
const inquiryKeyword = ref('')

const detailDialogVisible = ref(false)
const detailRecord = reactive<any>({})
const detailInquiries = ref<any[]>([])

const approveDialogVisible = ref(false)
const isReject = ref(false)
const currentRecord = ref<any>(null)
const approveForm = reactive({
  comment: '',
  completeDirectly: false
})

const replyDialogVisible = ref(false)
const currentInquiry = reactive<any>({})
const replyForm = reactive({
  reply: ''
})

const stats = reactive({
  pending: 0,
  processing: 0,
  approved: 0,
  completed: 0
})

const pendingInquiries = computed(() => inquiries.value.filter(i => !i.reply).length)

const categoryMap: Record<string, string> = {
  'CAT01': '户籍办理',
  'CAT02': '社保服务',
  'CAT03': '医保服务',
  'CAT04': '教育服务',
  'CAT05': '住房服务',
  'CAT06': '就业服务'
}

const getCategoryName = (code: string) => categoryMap[code] || code
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

const getStepIndex = (status: string) => {
  switch (status) {
    case '提交申请': return 0
    case '审核中': return 1
    case '已审批': return 2
    case '已办结': return 3
    default: return 0
  }
}

const formatFormData = (formData: string) => {
  try {
    const data = JSON.parse(formData)
    return JSON.stringify(data, null, 2)
  } catch {
    return formData
  }
}

const loadRecords = async () => {
  loading.value = true
  const params: any = {}
  if (statusFilter.value) params.status = statusFilter.value
  if (keyword.value) params.keyword = keyword.value
  
  const res = await approvalRecords(params)
  records.value = res.data || []
  
  stats.pending = records.value.filter(r => r.status === '提交申请').length
  stats.processing = records.value.filter(r => r.status === '审核中').length
  stats.approved = records.value.filter(r => r.status === '已审批').length
  stats.completed = records.value.filter(r => r.status === '已办结').length
  
  loading.value = false
}

const loadInquiries = async () => {
  inquiryLoading.value = true
  const res = await getAllInquiries()
  let list = res.data || []
  
  if (inquiryFilter.value === 'pending') {
    list = list.filter((i: any) => !i.reply)
  } else if (inquiryFilter.value === 'replied') {
    list = list.filter((i: any) => i.reply)
  }
  
  if (inquiryKeyword.value) {
    const kw = inquiryKeyword.value.toLowerCase()
    list = list.filter((i: any) => 
      (i.acceptNo && i.acceptNo.toLowerCase().includes(kw)) ||
      (i.userName && i.userName.toLowerCase().includes(kw))
    )
  }
  
  inquiries.value = list
  inquiryLoading.value = false
}

const handleTabChange = (tab: string) => {
  if (tab === 'inquiry') {
    loadInquiries()
  }
}

const handleSelectionChange = (val: any[]) => {
  selectedRows.value = val
}

const viewDetail = async (row: any) => {
  Object.assign(detailRecord, row)
  detailDialogVisible.value = true
  
  const res = await getInquiries(row.acceptNo)
  detailInquiries.value = res.data || []
}

const handleApprove = (row: any) => {
  currentRecord.value = row
  isReject.value = false
  approveForm.comment = ''
  approveForm.completeDirectly = false
  approveDialogVisible.value = true
}

const handleReject = (row: any) => {
  currentRecord.value = row
  isReject.value = true
  approveForm.comment = ''
  approveForm.completeDirectly = false
  approveDialogVisible.value = true
}

const confirmApprove = async () => {
  const res = await approve(currentRecord.value.acceptNo, {
    action: isReject.value ? 'reject' : 'approve',
    comment: approveForm.comment,
    completeDirectly: approveForm.completeDirectly
  })
  
  if (res.code === 200) {
    ElMessage.success(res.message)
    approveDialogVisible.value = false
    detailDialogVisible.value = false
    loadRecords()
  }
}

const handleBatchApprove = () => {
  const acceptNos = selectedRows.value.map((r: any) => r.acceptNo)
  batchApprove({ acceptNos, action: 'approve' }).then(res => {
    if (res.code === 200) {
      ElMessage.success(res.message)
      loadRecords()
    }
  })
}

const handleBatchReject = () => {
  const acceptNos = selectedRows.value.map((r: any) => r.acceptNo)
  batchApprove({ acceptNos, action: 'reject' }).then(res => {
    if (res.code === 200) {
      ElMessage.success(res.message)
      loadRecords()
    }
  })
}

const handleReply = (row: any) => {
  Object.assign(currentInquiry, row)
  replyForm.reply = ''
  replyDialogVisible.value = true
}

const confirmReply = async () => {
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  const res = await replyInquiry(currentInquiry.id, replyForm.reply)
  if (res.code === 200) {
    ElMessage.success(res.message)
    replyDialogVisible.value = false
    loadInquiries()
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.approval-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.page-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
}

.header-title h2 {
  font-size: 24px;
  margin: 0 0 8px 0;
}

.header-title p {
  font-size: 14px;
  opacity: 0.85;
  margin: 0;
}

.header-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-item .stat-value {
  display: block;
  font-size: 32px;
  font-weight: bold;
}

.stat-item .stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.pending-inquiry .stat-value {
  color: #ffd666;
}

.page-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar :deep(.el-button) {
  margin-left: 10px;
}

.record-card {
  border-radius: 8px;
}

.record-card :deep(.el-table) {
  border-radius: 8px;
}

.inquiry-list {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
}

.inquiry-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.inquiry-item:last-child {
  border-bottom: none;
}

.inquiry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.inquiry-time {
  font-size: 12px;
  color: #999;
}

.inquiry-content, .inquiry-reply {
  font-size: 14px;
  line-height: 1.6;
}

.inquiry-content .label, .inquiry-reply .label {
  font-weight: bold;
  color: #666;
}

.inquiry-reply {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #e8e8e8;
  color: #52c41a;
}

.reply-time {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}

.text-danger {
  color: #f56c6c;
}

.empty-state {
  padding: 20px;
  text-align: center;
}
</style>