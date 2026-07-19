<template>
  <div class="interaction-mgmt">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>留言工作台</span>
          <div class="header-actions">
            <el-input v-model="filterKeyword" placeholder="搜索标题/内容..." size="small" style="width: 200px; margin-right: 10px;" clearable @clear="loadMessages" @keyup.enter="loadMessages">
              <template #append>
                <el-button @click="loadMessages">搜索</el-button>
              </template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width: 120px;" @change="loadMessages">
              <el-option label="待分派" value="待分派" />
              <el-option label="已分派" value="已分派" />
              <el-option label="已回复" value="已回复" />
              <el-option label="已办结" value="已办结" />
            </el-select>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" style="margin-bottom: 20px;">
        <el-col :span="5">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num warning">{{ stats.pending }}</div>
            <div class="stat-label">待分派</div>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num info">{{ stats.dispatched }}</div>
            <div class="stat-label">已分派</div>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num success">{{ stats.replied }}</div>
            <div class="stat-label">已回复</div>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num">{{ stats.finished }}</div>
            <div class="stat-label">已办结</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never" class="stat-card" style="border: 2px solid #f56c6c;">
            <div class="stat-num danger">{{ stats.overdue }}</div>
            <div class="stat-label">超时提醒</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 留言表格 -->
      <el-table :data="messageList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ row.type || '咨询' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="留言人" width="100" />
        <el-table-column prop="deptCode" label="部门" width="100">
          <template #default="{ row }">
            {{ deptNameMap[row.deptCode] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="督办" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.supervise" type="danger" size="small">已督办</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="处理期限" width="180">
          <template #default="{ row }">
            <span :style="{ color: isOverdue(row) ? '#f56c6c' : '' }">{{ row.deadline || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === '待分派'" link type="warning" size="small" @click="showDispatch(row)">分派</el-button>
            <el-button v-if="row.status !== '已办结'" link type="success" size="small" @click="showReply(row)">回复</el-button>
            <el-button v-if="row.status === '已回复'" link type="info" size="small" @click="handleFinish(row)">办结</el-button>
            <el-button v-if="!row.supervise && row.status !== '已办结'" link type="danger" size="small" @click="handleSupervise(row)">督办</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top: 15px; justify-content: flex-end;"
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        v-model:current-page="currentPage"
        @current-change="loadMessages"
      />
    </el-card>

    <!-- 留言详情弹窗 -->
    <el-dialog v-model="detailVisible" title="留言详情" width="700px">
      <el-descriptions :column="1" border v-if="currentMessage">
        <el-descriptions-item label="标题">{{ currentMessage.title }}</el-descriptions-item>
        <el-descriptions-item label="留言人">{{ currentMessage.userName }}</el-descriptions-item>
        <el-descriptions-item label="目标部门">{{ deptNameMap[currentMessage.deptCode] || '未指定' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentMessage.status)">{{ currentMessage.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="留言内容" style="white-space: pre-wrap;">{{ currentMessage.content }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentMessage.createTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 对话记录 -->
      <div v-if="messageReplies.length > 0" class="conversation-section">
        <h4>交流记录</h4>
        <div class="conversation-list">
          <div
            v-for="reply in messageReplies"
            :key="reply.id"
            class="conversation-item"
            :class="{ 'is-admin': reply.userType === 'ADMIN', 'is-user': reply.userType === 'USER' }"
          >
            <div class="conversation-avatar">
              <el-avatar :size="32" :style="{ background: reply.userType === 'ADMIN' ? '#409eff' : '#67c23a' }">
                {{ reply.userType === 'ADMIN' ? '官' : '民' }}
              </el-avatar>
            </div>
            <div class="conversation-body">
              <div class="conversation-header">
                <span class="conversation-name">{{ reply.userName }}</span>
                <el-tag size="small" :type="reply.userType === 'ADMIN' ? '' : 'success'">
                  {{ reply.userType === 'ADMIN' ? '管理员' : '群众' }}
                </el-tag>
                <span class="conversation-time">{{ reply.createTime }}</span>
              </div>
              <div class="conversation-content">{{ reply.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 分派弹窗 -->
    <el-dialog v-model="dispatchVisible" title="分派留言" width="400px">
      <el-form label-width="80px">
        <el-form-item label="留言标题">
          <span>{{ currentMessage?.title }}</span>
        </el-form-item>
        <el-form-item label="目标部门">
          <el-select v-model="dispatchDept" placeholder="请选择部门" style="width: 100%;">
            <el-option label="教育部" value="EDU" />
            <el-option label="卫健委" value="HEA" />
            <el-option label="住建局" value="HOU" />
            <el-option label="人社局" value="SOC" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDispatch">确认分派</el-button>
      </template>
    </el-dialog>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyVisible" title="回复留言" width="500px">
      <el-form label-width="80px">
        <el-form-item label="留言标题">
          <span>{{ currentMessage?.title }}</span>
        </el-form-item>
        <el-form-item label="留言内容">
          <div class="msg-content-preview">{{ currentMessage?.content }}</div>
        </el-form-item>
        <el-form-item label="回复内容" required>
          <el-input v-model="replyContent" type="textarea" :rows="5" placeholder="请输入回复内容..." />
        </el-form-item>
        <el-form-item label="回复人">
          <el-input v-model="replyOperator" placeholder="请输入回复人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="handleReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { messageList as fetchMessageList, replyMessage, messageStats, superviseMessage, messageDetail } from '@/api/interaction'
import request from '@/utils/request'

// 部门编码映射
const deptNameMap: Record<string, string> = {
  EDU: '教育部', HEA: '卫健委', HOU: '住建局', SOC: '人社局'
}

// 统计数据
const stats = reactive({ pending: 0, dispatched: 0, replied: 0, finished: 0, overdue: 0 })

// 列表数据
const messageList = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterStatus = ref('')
const filterKeyword = ref('')

// 弹窗状态
const detailVisible = ref(false)
const dispatchVisible = ref(false)
const replyVisible = ref(false)
const currentMessage = ref<any>(null)
const messageReplies = ref<any[]>([])
const dispatchDept = ref('')
const replyContent = ref('')
const replyOperator = ref('')
const replying = ref(false)

// 加载留言列表
const loadMessages = async () => {
  try {
    const res: any = await fetchMessageList({
      keyword: filterKeyword.value || undefined,
      status: filterStatus.value || undefined,
      page: currentPage.value,
      size: pageSize
    })
    if (res.code === 200) {
      messageList.value = res.data.records
      total.value = res.data.total
      loadStats()
    }
  } catch (e) {
    console.error('加载留言列表失败', e)
  }
}

// 显示详情
const showDetail = async (row: any) => {
  currentMessage.value = row
  messageReplies.value = []
  detailVisible.value = true
  // 加载对话记录
  try {
    const res: any = await messageDetail(row.id)
    if (res.code === 200) {
      messageReplies.value = res.data.replies || []
    }
  } catch (e) {
    console.error('加载对话记录失败', e)
  }
}

// 显示分派弹窗
const showDispatch = (row: any) => {
  currentMessage.value = row
  dispatchDept.value = row.deptCode || ''
  dispatchVisible.value = true
}

// 执行分派
const handleDispatch = async () => {
  if (!dispatchDept.value) {
    ElMessage.warning('请选择目标部门')
    return
  }
  try {
    const res: any = await request.post(`/interaction/message/${currentMessage.value.id}/dispatch`, {
      deptCode: dispatchDept.value
    })
    if (res.code === 200) {
      ElMessage.success('分派成功')
      dispatchVisible.value = false
      loadMessages()
    }
  } catch (e) {
    ElMessage.error('分派失败')
  }
}

// 显示回复弹窗
const showReply = (row: any) => {
  currentMessage.value = row
  replyContent.value = ''
  replyOperator.value = '系统管理员'
  replyVisible.value = true
}

// 执行回复
const handleReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    const res: any = await replyMessage(currentMessage.value.id, {
      content: replyContent.value,
      operator: replyOperator.value
    })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyVisible.value = false
      loadMessages()
    }
  } catch (e) {
    ElMessage.error('回复失败')
  } finally {
    replying.value = false
  }
}

// 办结
const handleFinish = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认将「${row.title}」标记为已办结？`, '办结确认')
    const res: any = await request.post(`/interaction/message/${row.id}/finish`)
    if (res.code === 200) {
      ElMessage.success('已办结')
      loadMessages()
    }
  } catch (e) {
    // 用户取消
  }
}

// 加载统计
const loadStats = async () => {
  try {
    const res: any = await messageStats()
    if (res.code === 200) {
      const ss = res.data.statusStats
      stats.pending = ss['待分派'] || 0
      stats.dispatched = ss['已分派'] || 0
      stats.replied = ss['已回复'] || 0
      stats.finished = ss['已办结'] || 0
      stats.overdue = res.data.overdueCount || 0
    }
  } catch (e) { /* ignore */ }
}

// 是否超时
const isOverdue = (row: any) => {
  if (!row.deadline || row.status === '已办结') return false
  return new Date(row.deadline) < new Date()
}

// 督办
const handleSupervise = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认对「${row.title}」进行督办？`, '督办确认')
    const res: any = await superviseMessage(row.id, { operator: '系统管理员' })
    if (res.code === 200) {
      ElMessage.success('督办成功')
      loadMessages()
    }
  } catch (e) { /* 取消 */ }
}

// 类型标签
const typeTagType = (type: string) => {
  const map: Record<string, string> = { '咨询': '', '投诉': 'danger', '建议': 'success', '求助': 'warning' }
  return map[type] || ''
}

// 状态标签颜色
const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    '待分派': 'warning',
    '已分派': '',
    '已回复': 'success',
    '已办结': 'info'
  }
  return map[status] || ''
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped lang="scss">
.interaction-mgmt {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.stat-card {
  text-align: center;
  padding: 10px;
  .stat-num {
    font-size: 32px;
    font-weight: 700;
    color: #333;
    &.warning { color: #e6a23c; }
    &.info { color: #909399; }
    &.success { color: #67c23a; }
    &.danger { color: #f56c6c; }
  }
  .stat-label {
    font-size: 13px;
    color: #999;
    margin-top: 4px;
  }
}

.msg-content-preview {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
  max-height: 100px;
  overflow-y: auto;
}

// 对话记录
.conversation-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;

  h4 {
    font-size: 15px;
    color: #303133;
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 3px solid #409eff;
  }
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.conversation-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-radius: 6px;
  background: #f5f7fa;

  &.is-admin {
    background: #ecf5ff;
    border-left: 3px solid #409eff;
  }

  &.is-user {
    background: #f0f9eb;
    border-left: 3px solid #67c23a;
  }

  .conversation-body {
    flex: 1;
  }

  .conversation-header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 6px;
    font-size: 13px;

    .conversation-name {
      font-weight: 600;
      color: #303133;
    }

    .conversation-time {
      font-size: 12px;
      color: #909399;
      margin-left: auto;
    }
  }

  .conversation-content {
    white-space: pre-wrap;
    font-size: 13px;
    color: #303133;
    line-height: 1.5;
  }
}
</style>
