<template>
  <div class="suggestion-detail">
    <div class="page-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <h1>建议详情</h1>
    </div>

    <el-card v-loading="loading" shadow="hover">
      <div v-if="detail" class="detail-content">
        <!-- 基本信息 -->
        <div class="info-section">
          <h3 class="section-title">建议标题</h3>
          <p class="section-content">{{ detail.title }}</p>
        </div>

        <div class="info-row">
          <div class="info-item">
            <h3 class="section-title">建议类型</h3>
            <p class="section-content">{{ detail.type }}</p>
          </div>
          <div class="info-item">
            <h3 class="section-title">当前状态</h3>
            <p class="section-content">
              <el-tag :type="statusTagType(detail.status)" size="large">{{ detail.status }}</el-tag>
            </p>
          </div>
        </div>

        <div class="info-section">
          <h3 class="section-title">建议内容</h3>
          <p class="section-content content-text">{{ detail.content }}</p>
        </div>

        <div class="info-row">
          <div class="info-item">
            <h3 class="section-title">建议人</h3>
            <p class="section-content">{{ detail.realName }}</p>
          </div>
          <div class="info-item">
            <h3 class="section-title">提交时间</h3>
            <p class="section-content">{{ formatTime(detail.createTime) }}</p>
          </div>
        </div>

        <!-- 答复信息 -->
        <div v-if="detail.replyContent" class="reply-section">
          <h3 class="section-title reply-title">官方答复</h3>
          <div class="reply-content">
            <p>{{ detail.replyContent }}</p>
            <div class="reply-meta">
              <span>答复人：{{ detail.replyBy || '-' }}</span>
              <span v-if="detail.replyTime">答复时间：{{ formatTime(detail.replyTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 未答复提示 -->
        <div v-else class="no-reply">
          <el-empty description="暂无答复" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { suggestionPublicDetail } from '@/api/interaction'

const route = useRoute()
const router = useRouter()
const suggestionId = Number(route.params.id)

const loading = ref(false)
const detail = ref<any>(null)

const goBack = () => {
  router.push({ name: 'interaction' })
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '待受理': 'warning', '处理中': '', '已答复': 'success', '已办结': 'info' }
  return map[status] || ''
}

const loadDetail = async () => {
  if (!suggestionId) {
    ElMessage.error('建议ID无效')
    return
  }
  loading.value = true
  try {
    const res: any = await suggestionPublicDetail(suggestionId)
    if (res.code === 200) {
      detail.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.suggestion-detail {
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

.detail-content {
  .info-section {
    margin-bottom: 24px;
  }

  .info-row {
    display: flex;
    gap: 40px;
    margin-bottom: 24px;

    .info-item {
      flex: 1;
    }
  }

  .section-title {
    font-size: 16px;
    color: #606266;
    font-weight: 500;
    margin-bottom: 8px;
  }

  .section-content {
    font-size: 16px;
    color: #303133;
    line-height: 1.6;
    margin: 0;

    &.content-text {
      white-space: pre-wrap;
      line-height: 1.8;
    }
  }

  .reply-section {
    margin-top: 32px;
    padding-top: 24px;
    border-top: 2px solid #f0f0f0;

    .reply-title {
      color: #409eff;
      font-size: 18px;
      margin-bottom: 16px;
    }

    .reply-content {
      background: #f5f7fa;
      padding: 20px;
      border-radius: 8px;

      p {
        font-size: 16px;
        color: #303133;
        line-height: 1.8;
        margin: 0 0 16px 0;
        white-space: pre-wrap;
      }

      .reply-meta {
        display: flex;
        gap: 24px;
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .no-reply {
    margin-top: 24px;
    padding: 40px 0;
  }
}
</style>
