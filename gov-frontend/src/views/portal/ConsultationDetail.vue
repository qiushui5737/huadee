<template>
  <div class="consultation-detail">
    <div class="page-header">
      <el-button @click="$router.back()" icon="ArrowLeft">返回</el-button>
      <h1>咨询详情</h1>
    </div>

    <el-card v-loading="loading" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>咨询单号：{{ detail.consultNo }}</span>
          <el-tag :type="statusTagType(detail.status)" size="large">{{ detail.status }}</el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="咨询人">{{ detail.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号码">{{ detail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话号码">{{ detail.telephone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电子邮箱">{{ detail.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="咨询主题" :span="2">{{ detail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="咨询市州">{{ detail.city || '-' }}</el-descriptions-item>
        <el-descriptions-item label="事件发生地">
          {{ [detail.district, detail.street, detail.town].filter(Boolean).join(' / ') || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="具体地址" :span="2">{{ detail.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="是否公开">{{ detail.isPublic ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="是否保密">{{ detail.isConfidential ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="答复期限">{{ detail.deadline || '-' }}</el-descriptions-item>
        <el-descriptions-item label="咨询内容" :span="2">
          <div class="content-text">{{ detail.content || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="附件" :span="2" v-if="detail.attachment">
          <div class="attachment-list">
            <template v-for="(file, idx) in parseAttachments(detail.attachment)" :key="idx">
              <div v-if="file.isImage" class="attachment-image" @click="previewImage(file.url)">
                <img :src="file.url" :alt="file.name" />
              </div>
              <div v-else class="attachment-file">
                <el-icon><Document /></el-icon>
                <span>{{ file.name }}</span>
              </div>
            </template>
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 答复信息 -->
      <div v-if="detail.replyContent" class="reply-section">
        <h3>答复信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="答复人">{{ detail.replyBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答复时间">{{ detail.replyTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答复内容" :span="2">
            <div class="content-text">{{ detail.replyContent }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 图片预览 -->
    <el-image-viewer v-if="imagePreviewVisible" :url-list="[previewImageUrl]" @close="imagePreviewVisible = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document } from '@element-plus/icons-vue'
import { consultationProgress } from '@/api/interaction'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const consultNo = route.params.consultNo as string

const loading = ref(false)
const detail = ref<any>({})
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '待受理': 'warning', '处理中': '', '已答复': 'success', '已办结': 'info' }
  return map[status] || ''
}

const loadDetail = async () => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后查看咨询详情')
    router.push({ name: 'portal-login', query: { redirect: route.fullPath } })
    return
  }
  if (!consultNo) {
    ElMessage.error('咨询单号不存在')
    return
  }
  loading.value = true
  try {
    const res: any = await consultationProgress(consultNo)
    if (res.code === 200) {
      detail.value = res.data
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const parseAttachments = (attachmentStr: string) => {
  try {
    return JSON.parse(attachmentStr)
  } catch {
    return []
  }
}

const previewImage = (url: string) => {
  previewImageUrl.value = url
  imagePreviewVisible.value = true
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.consultation-detail {
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

.content-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #303133;
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

.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  .attachment-image {
    cursor: pointer;
    img {
      width: 120px;
      height: 120px;
      object-fit: cover;
      border-radius: 6px;
      border: 1px solid #e4e7ed;
      transition: transform 0.2s;
      &:hover { transform: scale(1.05); }
    }
  }

  .attachment-file {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 13px;
    color: #606266;
  }
}
</style>
