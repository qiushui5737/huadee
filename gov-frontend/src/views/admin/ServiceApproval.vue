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
              <el-table-column label="申请材料" min-width="200">
                <template #default="scope">
                  <div v-if="scope.row.materials && scope.row.materials.length > 0" class="materials-tags">
                    <el-tag v-for="(m, idx) in scope.row.materials" :key="idx" size="small" type="info" class="material-tag">{{ m }}</el-tag>
                  </div>
                  <span v-else style="color:#999;">无材料要求</span>
                </template>
              </el-table-column>
              <el-table-column label="缴费" width="120" align="center">
                <template #default="scope">
                  <div v-if="scope.row.payAmount && scope.row.payAmount > 0">
                    <span style="color: #f56c6c; font-weight: bold; font-size: 13px;">¥{{ scope.row.payAmount }}</span>
                    <el-tag :type="scope.row.payStatus === '已支付' ? 'success' : 'warning'" size="small" style="margin-left: 4px;">{{ scope.row.payStatus || '待支付' }}</el-tag>
                  </div>
                  <el-tag v-else size="small" type="success">免费</el-tag>
                </template>
              </el-table-column>
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
        <el-descriptions-item label="缴费信息">
          <template v-if="detailRecord.payAmount && detailRecord.payAmount > 0">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ detailRecord.payAmount }}</span>
            <el-tag :type="detailRecord.payStatus === '已支付' ? 'success' : 'warning'" size="small" style="margin-left: 8px;">{{ detailRecord.payStatus || '待支付' }}</el-tag>
            <span v-if="detailRecord.payTime" style="color: #999; font-size: 12px; margin-left: 8px;">缴费时间: {{ detailRecord.payTime }}</span>
          </template>
          <el-tag v-else size="small" type="success">免费办理</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请材料" :span="2">
          <div v-if="detailMaterials.length > 0" class="materials-require">
            <div v-for="(m, idx) in detailMaterials" :key="idx" class="material-req-item">
              <el-icon><Document /></el-icon>
              <span>{{ m }}</span>
              <span v-if="getMaterialValue(idx)" class="material-submitted">已提交: {{ getMaterialValue(idx) }}</span>
              <span v-else class="material-not-submitted">未提交</span>
            </div>
          </div>
          <span v-else style="color:#999;">该事项无材料要求</span>
        </el-descriptions-item>
        <el-descriptions-item label="填写内容" :span="2">
          <div v-if="parsedFormData.length > 0" class="form-data-table">
            <div v-for="field in parsedFormData" :key="field.key" class="form-data-row">
              <span class="form-data-label">{{ field.label }}</span>
              <span class="form-data-value">{{ field.value || '-' }}</span>
            </div>
          </div>
          <span v-else style="color:#999;">无提交内容</span>
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
        <el-button type="primary" @click="confirmApprove">{{ isReject ? '确认驳回' : '确认通过' }}</el-button>
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
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { Search, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { approvalRecords, approve, batchApprove, getInquiries, getAllInquiries, replyInquiry, recordDetail } from '@/api/service'

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
  'education': '教育服务',
  'housing': '住房保障',
  'health': '医疗卫生',
  'employment': '就业创业',
  'social': '社会保障',
  'traffic': '交通出行',
  'tax': '税费办理',
  'certificate': '证件办理'
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

const fieldLabelMap: Record<string, string> = {
  userName: '申请人姓名',
  idCard: '身份证号',
  phone: '联系电话',
  address: '联系地址',
  reason: '申请事由'
}

// 从事项formSchema中解析材料名称列表
const getMaterialLabels = (formSchema: string): string[] => {
  if (!formSchema) return []
  try {
    const schema = JSON.parse(formSchema)
    if (schema.materials && Array.isArray(schema.materials)) {
      return schema.materials
    }
  } catch { /* ignore */ }
  return []
}

// 详情弹窗中的材料要求列表
const detailMaterials = computed(() => {
  return getMaterialLabels(detailRecord.formSchema)
})

// 获取用户提交的某个材料的值
const getMaterialValue = (idx: number): string => {
  if (!detailRecord.formData) return ''
  try {
    const data = JSON.parse(detailRecord.formData)
    const val = data[`material_${idx}`]
    return val ? String(val) : ''
  } catch {
    return ''
  }
}

const parsedFormData = computed(() => {
  if (!detailRecord.formData) return []
  try {
    const data = JSON.parse(detailRecord.formData)
    // 获取事项的材料名称列表
    const materialLabels = getMaterialLabels(detailRecord.formSchema)
    // 过滤掉系统字段，只展示用户填写的业务字段
    const systemFields = ['itemId', 'acceptNo', 'itemName', 'formData']
    return Object.entries(data)
      .filter(([key]) => !systemFields.includes(key))
      .map(([key, value]) => {
        // 如果是material_开头的字段，用材料名称作为label
        let label = fieldLabelMap[key] || key
        if (key.startsWith('material_')) {
          const idx = parseInt(key.replace('material_', ''))
          if (!isNaN(idx) && idx < materialLabels.length) {
            label = materialLabels[idx]
          }
        }
        return { key, label, value: value != null ? String(value) : '-' }
      })
  } catch {
    return []
  }
})

const getFormSummary = (formData: string) => {
  if (!formData) return ''
  try {
    const data = JSON.parse(formData)
    const parts: string[] = []
    if (data.userName) parts.push(data.userName)
    if (data.phone) parts.push(data.phone)
    if (data.reason) parts.push(data.reason)
    // 显示自定义材料字段摘要
    Object.keys(data).forEach(key => {
      if (key.startsWith('material_') && data[key]) {
        const val = String(data[key])
        parts.push(val.length > 10 ? val.substring(0, 10) + '...' : val)
      }
    })
    return parts.join(' | ')
  } catch {
    return formData.length > 30 ? formData.substring(0, 30) + '...' : formData
  }
}

const formatFormData = (formData: string) => {
  try {
    const data = JSON.parse(formData)
    return JSON.stringify(data, null, 2)
  } catch {
    return formData || ''
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

const loadPendingInquiryCount = async () => {
  try {
    const res = await getAllInquiries()
    inquiries.value = res.data || []
  } catch (e) {
    // ignore
  }
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
  
  // 通过API获取完整记录详情（包含formData）
  try {
    const res = await recordDetail(row.acceptNo)
    if (res.data) {
      Object.assign(detailRecord, res.data)
    }
  } catch (e) {
    // ignore
  }
  
  const inqRes = await getInquiries(row.acceptNo)
  detailInquiries.value = inqRes.data || []
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
    loadPendingInquiryCount()
  } else {
    ElMessage.error(res.message || '操作失败')
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

let inquiryTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  loadRecords()
  loadPendingInquiryCount()
  inquiryTimer = setInterval(() => {
    loadPendingInquiryCount()
  }, 10000)
})

onBeforeUnmount(() => {
  if (inquiryTimer) clearInterval(inquiryTimer)
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

.form-summary {
  font-size: 12px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.materials-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.material-tag {
  margin: 2px 0;
}

.materials-require {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.material-req-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
}

.material-req-item :deep(.el-icon) {
  color: #1890ff;
}

.material-submitted {
  margin-left: auto;
  color: #52c41a;
  font-size: 12px;
}

.material-not-submitted {
  margin-left: auto;
  color: #f56c6c;
  font-size: 12px;
}

.empty-state {
  padding: 20px;
  text-align: center;
}

.form-data-table {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
}

.form-data-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  line-height: 36px;
}

.form-data-row:last-child {
  border-bottom: none;
}

.form-data-row:nth-child(even) {
  background: #fafafa;
}

.form-data-label {
  width: 120px;
  min-width: 120px;
  padding: 0 12px;
  font-weight: bold;
  color: #606266;
  background: #f5f7fa;
  border-right: 1px solid #ebeef5;
  font-size: 14px;
}

.form-data-value {
  flex: 1;
  padding: 0 12px;
  color: #303133;
  font-size: 14px;
  word-break: break-all;
}
</style>