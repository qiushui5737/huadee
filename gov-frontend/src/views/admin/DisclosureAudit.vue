<template>
  <div class="disclosure-audit">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>依申请公开审核</span>
          <div class="header-actions">
            <el-input v-model="filterKeyword" placeholder="搜索申请人/编号..." size="small" style="width: 200px; margin-right: 10px;" clearable @clear="loadList" @keyup.enter="loadList">
              <template #append><el-button @click="loadList">搜索</el-button></template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width: 120px;" @change="loadList">
              <el-option label="已受理" value="已受理" />
              <el-option label="审核中" value="审核中" />
              <el-option label="已答复" value="已答复" />
              <el-option label="已驳回" value="已驳回" />
            </el-select>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" style="margin-bottom: 20px;">
        <el-col :span="6" v-for="item in statusCards" :key="item.label">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num" :class="item.color">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 申请列表 -->
      <el-table :data="listData" stripe style="width: 100%">
        <el-table-column prop="applyNo" label="申请编号" width="160" />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="content" label="申请内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="acquireMethod" label="获取方式" width="100" />
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
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status !== '已答复' && row.status !== '已驳回'" link type="success" size="small" @click="showAudit(row, 'approve')">答复</el-button>
            <el-button v-if="row.status !== '已答复' && row.status !== '已驳回'" link type="danger" size="small" @click="showAudit(row, 'reject')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadList" />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="600px">
      <el-descriptions :column="1" border v-if="currentItem">
        <el-descriptions-item label="申请编号">{{ currentItem.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentItem.applicant }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentItem.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentItem.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="获取方式">{{ currentItem.acquireMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusTagType(currentItem.status)">{{ currentItem.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="答复期限">{{ currentItem.deadline || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请内容" style="white-space: pre-wrap;">{{ currentItem.content }}</el-descriptions-item>
        <el-descriptions-item label="用途说明" v-if="currentItem.purpose">{{ currentItem.purpose }}</el-descriptions-item>
        <el-descriptions-item label="答复内容" v-if="currentItem.replyContent" style="white-space: pre-wrap;">{{ currentItem.replyContent }}</el-descriptions-item>
        <el-descriptions-item label="答复人" v-if="currentItem.replyBy">{{ currentItem.replyBy }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" :title="auditAction === 'approve' ? '答复申请' : '驳回申请'" width="500px">
      <el-form label-width="80px">
        <el-form-item label="申请编号"><span>{{ currentItem?.applyNo }}</span></el-form-item>
        <el-form-item label="申请内容"><div class="preview">{{ currentItem?.content }}</div></el-form-item>
        <el-form-item label="答复内容" required>
          <el-input v-model="auditReply" type="textarea" :rows="5" :placeholder="auditAction === 'approve' ? '请输入答复内容...' : '请输入驳回原因...'" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="auditOperator" placeholder="请输入操作人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button :type="auditAction === 'approve' ? 'success' : 'danger'" :loading="auditing" @click="handleAudit">
          {{ auditAction === 'approve' ? '确认答复' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { disclosureList, auditDisclosure, disclosureStats } from '@/api/interaction'

const filterKeyword = ref('')
const filterStatus = ref('')
const listData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const statsData = ref<any>({})
const statusCards = computed(() => [
  { label: '已受理', value: statsData.value['已受理'] || 0, color: 'warning' },
  { label: '审核中', value: statsData.value['审核中'] || 0, color: 'info' },
  { label: '已答复', value: statsData.value['已答复'] || 0, color: 'success' },
  { label: '已驳回', value: statsData.value['已驳回'] || 0, color: 'danger' }
])

const detailVisible = ref(false)
const auditVisible = ref(false)
const currentItem = ref<any>(null)
const auditAction = ref('approve')
const auditReply = ref('')
const auditOperator = ref('系统管理员')
const auditing = ref(false)

const loadList = async () => {
  try {
    const res: any = await disclosureList({
      keyword: filterKeyword.value || undefined,
      status: filterStatus.value || undefined,
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
    const res: any = await disclosureStats()
    if (res.code === 200) {
      statsData.value = res.data.statusStats || {}
    }
  } catch (e) { /* ignore */ }
}

const showDetail = (row: any) => {
  currentItem.value = row
  detailVisible.value = true
}

const showAudit = (row: any, action: string) => {
  currentItem.value = row
  auditAction.value = action
  auditReply.value = ''
  auditOperator.value = '系统管理员'
  auditVisible.value = true
}

const handleAudit = async () => {
  if (!auditReply.value.trim()) {
    ElMessage.warning('请输入答复内容')
    return
  }
  auditing.value = true
  try {
    const res: any = await auditDisclosure(currentItem.value.applyNo, {
      action: auditAction.value,
      replyContent: auditReply.value,
      operator: auditOperator.value
    })
    if (res.code === 200) {
      ElMessage.success(auditAction.value === 'approve' ? '答复成功' : '已驳回')
      auditVisible.value = false
      loadList()
      loadStats()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    auditing.value = false
  }
}

const isOverdue = (row: any) => {
  if (!row.deadline || row.status === '已答复' || row.status === '已驳回') return false
  return new Date(row.deadline) < new Date()
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '已受理': 'warning', '审核中': '', '已答复': 'success', '已驳回': 'danger' }
  return map[status] || ''
}

onMounted(() => { loadList(); loadStats() })
</script>

<style scoped lang="scss">
.disclosure-audit { padding: 20px; }
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
<template>
  <div style="padding:20px;">
    <h2>依申请审核 - 审核工作台与时限</h2>
    <el-empty description="页面骨架 - 待实现" />
  </div>
</template>
<script setup lang="ts"></script>
