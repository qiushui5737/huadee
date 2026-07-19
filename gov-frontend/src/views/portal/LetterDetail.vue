<template>
  <div class="letter-detail">
    <div class="page-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <h1>信件详情</h1>
    </div>

    <el-card v-loading="loading" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>信件单号：{{ detail.consultNo || detail.id }}</span>
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-tag v-if="!detail.isPublic" type="warning" size="small">私密</el-tag>
            <el-tag :type="statusTagType(detail.status)" size="large">{{ detail.status || '未知' }}</el-tag>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ detail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="留言人">{{ detail.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="信件类型">
          <el-tag :type="typeTagType(detail.type)" size="small">{{ detail.type || '咨询' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标部门">{{ deptLabel(detail.deptCode) }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="statusTagType(detail.status)" size="small">{{ detail.status || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理期限">{{ detail.deadline || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 信件内容 -->
      <div class="content-section">
        <h3>信件内容</h3>
        <div class="content-text" v-html="detail.content || '-'"></div>
      </div>

      <!-- 对话记录 -->
      <div v-if="replies && replies.length > 0" class="conversation-section">
        <h3>交流记录</h3>
        <div class="conversation-list">
          <div
            v-for="reply in replies"
            :key="reply.id"
            class="conversation-item"
            :class="{ 'is-admin': reply.userType === 'ADMIN', 'is-user': reply.userType === 'USER' }"
          >
            <div class="conversation-avatar">
              <el-avatar :size="36" :style="{ background: reply.userType === 'ADMIN' ? '#409eff' : '#67c23a' }">
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
              <div class="conversation-content" v-html="reply.content"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 评价信息 -->
      <div v-if="detail.rating" class="reply-section">
        <h3 style="border-left-color: #e6a23c;">群众评价</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评分">
            <el-rate v-model="detail.rating" disabled show-text />
          </el-descriptions-item>
          <el-descriptions-item label="评价内容">{{ detail.ratingContent || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 用户回复输入（仅信件本人可回复） -->
    <el-card v-if="isOwner && (detail.status === '已回复' || detail.status === '已分派')" shadow="hover" class="reply-input-card">
      <h3>回复信件</h3>
      <el-input
        v-model="userReplyContent"
        type="textarea"
        :rows="4"
        placeholder="请输入您的回复..."
      />
      <div class="reply-actions">
        <el-button type="primary" @click="submitUserReply" :loading="replySubmitting">提交回复</el-button>
      </div>
    </el-card>

    <!-- 评价按钮 -->
    <div v-if="(detail.status === '已回复' || detail.status === '已办结') && !detail.rating" class="action-bar">
      <el-button type="primary" @click="showRateDialog">评价此信件</el-button>
    </div>

    <!-- 评价弹窗 -->
    <el-dialog v-model="rateVisible" title="信件评价" width="450px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="rateForm.rating" :max="5" show-text />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="rateForm.content" type="textarea" :rows="3" placeholder="请输入评价内容（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRate" :loading="rateSubmitting">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { messageDetail, rateMessage, userReplyMessage } from '@/api/interaction'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const letterId = Number(route.params.id)

const loading = ref(false)
const detail = ref<any>({})
const replies = ref<any[]>([])

// 判断是否为信件本人
const isOwner = computed(() => {
  return userStore.isLoggedIn && userStore.profile.id && detail.value.userId
    && Number(userStore.profile.id) === Number(detail.value.userId)
})

// 用户回复
const userReplyContent = ref('')
const replySubmitting = ref(false)

// 评价
const rateVisible = ref(false)
const rateSubmitting = ref(false)
const rateForm = reactive({ rating: 5, content: '' })

// 部门映射
const deptOptions: Record<string, string> = {
  GOV: '省政府办公厅', EDU: '教育厅', HEA: '卫健委', HOU: '住建厅',
  SOC: '人社厅', POL: '公安厅', FIN: '财政厅', TRA: '交通厅'
}
const deptLabel = (code: string) => {
  if (!code) return '未指定'
  return code.split(',').map(c => deptOptions[c] || c).join('、')
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '待分派': 'warning', '已分派': '', '已回复': 'success', '已办结': 'info' }
  return map[status] || ''
}

const typeTagType = (type: string) => {
  const map: Record<string, string> = { '咨询': '', '投诉': 'danger', '建议': 'success', '求助': 'warning' }
  return map[type] || ''
}

const goBack = () => {
  router.push('/interaction')
}

const loadDetail = async () => {
  if (!letterId) {
    ElMessage.error('信件ID无效')
    return
  }
  loading.value = true
  try {
    const res: any = await messageDetail(letterId)
    if (res.code === 200) {
      detail.value = res.data
      replies.value = res.data.replies || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

const submitUserReply = async () => {
  if (!userReplyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再回复')
    router.push({ name: 'portal-login', query: { redirect: route.fullPath } })
    return
  }
  replySubmitting.value = true
  try {
    const res: any = await userReplyMessage(letterId, {
      userId: userStore.userId,
      userName: userStore.username || '市民用户',
      content: userReplyContent.value
    })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      userReplyContent.value = ''
      loadDetail() // 重新加载以显示新回复
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  } finally {
    replySubmitting.value = false
  }
}

const showRateDialog = () => {
  rateForm.rating = 5
  rateForm.content = ''
  rateVisible.value = true
}

const submitRate = async () => {
  rateSubmitting.value = true
  try {
    const res: any = await rateMessage(letterId, { rating: rateForm.rating, content: rateForm.content })
    if (res.code === 200) {
      ElMessage.success('评价成功')
      rateVisible.value = false
      detail.value.rating = rateForm.rating
      detail.value.ratingContent = rateForm.content
    } else {
      ElMessage.error(res.message || '评价失败')
    }
  } catch (e) {
    ElMessage.error('网络异常')
  } finally {
    rateSubmitting.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.letter-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  h1 {
    font-size: 24px;
    color: #1a3a5c;
    margin: 0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.content-section,
.conversation-section {
  margin-top: 24px;

  h3 {
    font-size: 18px;
    color: #1a3a5c;
    margin-bottom: 16px;
    padding-left: 12px;
    border-left: 3px solid #409eff;
  }
}

.content-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #303133;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;

  :deep(img) {
    max-width: 100%;
    height: auto;
  }
}

// 对话列表
.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
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
    gap: 8px;
    margin-bottom: 8px;

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
    line-height: 1.6;
    color: #303133;

    :deep(img) {
      max-width: 100%;
      height: auto;
    }
  }
}

.reply-section {
  margin-top: 24px;

  h3 {
    font-size: 18px;
    color: #1a3a5c;
    margin-bottom: 16px;
    padding-left: 12px;
    border-left: 3px solid #67c23a;
  }
}

.reply-input-card {
  margin-top: 20px;

  h3 {
    font-size: 18px;
    color: #1a3a5c;
    margin-bottom: 16px;
    padding-left: 12px;
    border-left: 3px solid #409eff;
  }

  .reply-actions {
    margin-top: 12px;
    text-align: right;
  }
}

.action-bar {
  margin-top: 20px;
  text-align: right;
}
</style>
