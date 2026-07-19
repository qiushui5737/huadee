<template>
  <div class="complaint-mgmt">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>投诉管理</span>
          <div class="header-actions">
            <el-input v-model="filterKeyword" placeholder="搜索姓名/单号/标题..." size="small" style="width: 220px; margin-right: 10px;" clearable @clear="loadList" @keyup.enter="loadList">
              <template #append><el-button @click="loadList">搜索</el-button></template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width: 120px; margin-right: 10px;" @change="loadList">
              <el-option label="待受理" value="待受理" />
              <el-option label="处理中" value="处理中" />
              <el-option label="已答复" value="已答复" />
              <el-option label="已办结" value="已办结" />
            </el-select>
            <el-select v-model="filterCity" placeholder="市州筛选" clearable size="small" style="width: 140px;" @change="loadList">
              <el-option v-for="c in cityOptions" :key="c" :label="c" :value="c" />
            </el-select>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" style="margin-bottom: 20px;">
        <el-col :span="5" v-for="item in statusCards" :key="item.label">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num" :class="item.color">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num danger">{{ overdueCount }}</div>
            <div class="stat-label">超时未答复</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 投诉列表 -->
      <el-table :data="listData" stripe style="width: 100%">
        <el-table-column prop="complaintNo" label="投诉单号" width="180" />
        <el-table-column prop="realName" label="投诉人" width="90" />
        <el-table-column prop="title" label="投诉标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="投诉单位" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ [row.complaintUnitL1, row.complaintUnitL2].filter(Boolean).join(' / ') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="问题类型" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ [row.problemTypeL1, row.problemTypeL2].filter(Boolean).join(' / ') || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="答复期限" width="120">
          <template #default="{ row }">
            <span :style="{ color: isOverdue(row) ? '#f56c6c' : '' }">{{ row.deadline || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button link type="warning" size="small" @click="showStatusEdit(row)">进度</el-button>
            <el-button v-if="row.status !== '已答复' && row.status !== '已办结'" link type="success" size="small" @click="showReply(row)">答复</el-button>
            <el-button v-if="row.status === '已答复'" link type="info" size="small" @click="handleFinish(row)">办结</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadList" />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="投诉详情" width="700px">
      <el-descriptions :column="2" border v-if="currentItem">
        <el-descriptions-item label="投诉单号" :span="2">{{ currentItem.complaintNo }}</el-descriptions-item>
        <el-descriptions-item label="投诉人">{{ currentItem.realName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentItem.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentItem.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电子邮箱">{{ currentItem.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="投诉标题" :span="2">{{ currentItem.title }}</el-descriptions-item>
        <el-descriptions-item label="投诉单位" :span="2">
          {{ [currentItem.complaintUnitL1, currentItem.complaintUnitL2, currentItem.complaintUnitL3].filter(Boolean).join(' / ') || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="问题类型" :span="2">
          {{ [currentItem.problemTypeL1, currentItem.problemTypeL2, currentItem.problemTypeL3].filter(Boolean).join(' / ') || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentItem.status)">{{ currentItem.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="答复期限">{{ currentItem.deadline || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系地址" :span="2">{{ currentItem.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="事件发生地" :span="2">
          {{ [currentItem.province, currentItem.city, currentItem.district].filter(Boolean).join(' ') || '-' }}
          {{ currentItem.detailAddress || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="是否公开">{{ currentItem.isPublic ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="是否保密">{{ currentItem.isSecret ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="投诉内容" :span="2">
          <div style="white-space: pre-wrap;">{{ currentItem.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="答复内容" :span="2" v-if="currentItem.replyContent">
          <div style="white-space: pre-wrap; color: #67c23a;">{{ currentItem.replyContent }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="答复人" v-if="currentItem.replyBy">{{ currentItem.replyBy }}</el-descriptions-item>
        <el-descriptions-item label="答复时间" v-if="currentItem.replyTime">{{ currentItem.replyTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 答复弹窗 -->
    <el-dialog v-model="replyVisible" title="答复投诉" width="550px">
      <el-form label-width="80px">
        <el-form-item label="投诉单号"><span>{{ currentItem?.complaintNo }}</span></el-form-item>
        <el-form-item label="投诉标题"><span>{{ currentItem?.title }}</span></el-form-item>
        <el-form-item label="投诉内容"><div class="preview">{{ currentItem?.content }}</div></el-form-item>
        <el-form-item label="答复内容" required>
          <el-input v-model="replyContent" type="textarea" :rows="5" placeholder="请输入答复内容..." />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="replyOperator" placeholder="请输入操作人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="success" :loading="replying" @click="handleReply">确认答复</el-button>
      </template>
    </el-dialog>

    <!-- 状态/进度编辑弹窗 -->
    <el-dialog v-model="statusEditVisible" title="修改投诉进度" width="400px">
      <el-form label-width="80px">
        <el-form-item label="投诉单号"><span>{{ statusEditForm.complaintNo }}</span></el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="statusTagType(currentItem?.status || '')">{{ currentItem?.status }}</el-tag>
        </el-form-item>
        <el-form-item label="修改为" required>
          <el-select v-model="statusEditForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="待受理" value="待受理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已答复" value="已答复" />
            <el-option label="已办结" value="已办结" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusEditing" @click="handleStatusEdit">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { complaintList, replyComplaint, updateComplaintStatus, finishComplaint, complaintStats } from '@/api/interaction'

const cityOptions = [
  '成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市',
  '广元市', '遂宁市', '内江市', '乐山市', '南充市', '眉山市',
  '宜宾市', '广安市', '达州市', '雅安市', '巴中市', '资阳市',
  '阿坝州', '甘孜州', '凉山州'
]

const filterKeyword = ref('')
const filterStatus = ref('')
const filterCity = ref('')
const listData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const statsData = ref<any>({})
const overdueCount = ref(0)
const statusCards = computed(() => [
  { label: '待受理', value: statsData.value['待受理'] || 0, color: 'warning' },
  { label: '处理中', value: statsData.value['处理中'] || 0, color: 'info' },
  { label: '已答复', value: statsData.value['已答复'] || 0, color: 'success' },
  { label: '已办结', value: statsData.value['已办结'] || 0, color: '' }
])

const detailVisible = ref(false)
const replyVisible = ref(false)
const currentItem = ref<any>(null)
const replyContent = ref('')
const replyOperator = ref('系统管理员')
const replying = ref(false)

const statusEditVisible = ref(false)
const statusEditForm = reactive({ complaintNo: '', status: '' })
const statusEditing = ref(false)

const loadList = async () => {
  try {
    const res: any = await complaintList({
      keyword: filterKeyword.value || undefined,
      status: filterStatus.value || undefined,
      city: filterCity.value || undefined,
      page: currentPage.value,
      size: pageSize
    })
    if (res.code === 200) {
      listData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) { /* ignore */ }
}

const loadStats = async () => {
  try {
    const res: any = await complaintStats()
    if (res.code === 200) {
      statsData.value = res.data.statusStats || {}
      overdueCount.value = res.data.overdueCount || 0
    }
  } catch (e) { /* ignore */ }
}

const showDetail = (row: any) => {
  currentItem.value = row
  detailVisible.value = true
}

const showReply = (row: any) => {
  currentItem.value = row
  replyContent.value = ''
  replyOperator.value = '系统管理员'
  replyVisible.value = true
}

const handleReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入答复内容')
    return
  }
  replying.value = true
  try {
    const res: any = await replyComplaint(currentItem.value.complaintNo, {
      replyContent: replyContent.value,
      operator: replyOperator.value
    })
    if (res.code === 200) {
      ElMessage.success('答复成功')
      replyVisible.value = false
      loadList()
      loadStats()
    }
  } catch (e) {
    ElMessage.error('答复失败')
  } finally {
    replying.value = false
  }
}

const handleFinish = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认将投诉「${row.complaintNo}」办结？`, '办结确认')
    const res: any = await finishComplaint(row.complaintNo)
    if (res.code === 200) {
      ElMessage.success('已办结')
      loadList()
      loadStats()
    }
  } catch (e) { /* cancel */ }
}

const showStatusEdit = (row: any) => {
  statusEditForm.complaintNo = row.complaintNo
  statusEditForm.status = row.status
  statusEditVisible.value = true
}

const handleStatusEdit = async () => {
  if (!statusEditForm.status) {
    ElMessage.warning('请选择状态')
    return
  }
  statusEditing.value = true
  try {
    const res: any = await updateComplaintStatus(statusEditForm.complaintNo, { status: statusEditForm.status })
    if (res.code === 200) {
      ElMessage.success('进度修改成功')
      statusEditVisible.value = false
      loadList()
      loadStats()
    }
  } catch (e) {
    ElMessage.error('进度修改失败')
  } finally {
    statusEditing.value = false
  }
}

const isOverdue = (row: any) => {
  if (!row.deadline || row.status === '已答复' || row.status === '已办结') return false
  return new Date(row.deadline) < new Date()
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '待受理': 'warning', '处理中': '', '已答复': 'success', '已办结': 'info' }
  return map[status] || ''
}

onMounted(() => { loadList(); loadStats() })
</script>

<style scoped lang="scss">
.complaint-mgmt { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; font-size: 16px; }
.header-actions { display: flex; align-items: center; }
.stat-card { text-align: center; padding: 10px;
  .stat-num { font-size: 32px; font-weight: 700; color: #333;
    &.warning { color: #e6a23c; } &.info { color: #909399; } &.success { color: #67c23a; } &.danger { color: #f56c6c; }
  }
  .stat-label { font-size: 13px; color: #999; margin-top: 4px; }
}
.preview { background: #f5f7fa; padding: 10px; border-radius: 4px; font-size: 13px; color: #666; max-height: 80px; overflow-y: auto; }
</style>
