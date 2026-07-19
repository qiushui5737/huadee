<template>
  <div class="collection-feedback">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span>当前位置：</span>
      <router-link to="/">首页</router-link>
      <span> &gt; </span>
      <router-link to="/interaction">互动交流</router-link>
      <span> &gt; </span>
      <span>结果反馈</span>
    </div>

    <el-card v-loading="loading" shadow="hover" class="feedback-card">
      <!-- 标题 -->
      <h1 class="feedback-title">
        《{{ detail.title }}》在线意见征集情况
      </h1>

      <!-- 元信息 -->
      <div class="meta-info">
        <div class="meta-left">
          <span>发布日期：{{ formatDate(detail.createTime) }}</span>
          <span>信息来源：{{ detail.deptName || '大数据与信息化处' }}</span>
          <span>浏览量：{{ viewCount }}</span>
        </div>
        <div class="meta-right">
          <span>字体：</span>
          <el-button text size="small" @click="fontSize = 'large'">A<sup>+</sup></el-button>
          <el-button text size="small" @click="fontSize = 'normal'">A<sup>-</sup></el-button>
        </div>
      </div>

      <el-divider />

      <!-- 正文内容 -->
      <div class="feedback-body" :class="'font-' + fontSize">
        <p class="indent">
          {{ detail.startDate }}至{{ detail.endDate }}，{{ detail.deptName || '我厅' }}网站面向社会公众公开征集《{{ detail.title }}》意见。
        </p>

        <p class="indent">
          意见征集期间，收到{{ total }}条反馈意见，其中采纳{{ adopted }}条，部分采纳{{ partiallyAdopted }}条，未采纳{{ rejected }}条{{ rejectReasons }}。
        </p>

        <!-- 结果反馈内容 -->
        <div v-if="detail.feedback" class="feedback-detail">
          <p class="indent">{{ detail.feedback }}</p>
        </div>

        <!-- 落款 -->
        <div class="signature">
          <p>{{ detail.deptName || '' }}</p>
          <p>{{ formatDate(detail.feedbackTime || detail.createTime) }}</p>
        </div>
      </div>
    </el-card>

    <!-- 返回列表 -->
    <div style="text-align: center; margin-top: 20px;">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>返回征集列表
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { collectionPublicFeedback } from '@/api/interaction'

const route = useRoute()
const router = useRouter()
const collectionId = Number(route.params.id)

const loading = ref(false)
const detail = ref<any>({})
const viewCount = ref(0)
const fontSize = ref<'normal' | 'large'>('normal')
const total = ref(0)
const adopted = ref(0)
const partiallyAdopted = ref(0)
const rejected = ref(0)
const rejectReasons = ref('，部分采纳及未采纳的理由包括意见与上位文件及现行政策不符合、相关内容规划已统筹体现、操作层面的具体举措不宜在宏观规划中表述等')

const goBack = () => {
  router.push({ name: 'collection-list' })
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

const loadFeedback = async () => {
  if (!collectionId) {
    ElMessage.error('征集ID无效')
    return
  }
  loading.value = true
  try {
    const res: any = await collectionPublicFeedback(collectionId)
    if (res.code === 200) {
      detail.value = res.data
      total.value = res.data.total || 0
      adopted.value = res.data.adopted || 0
      partiallyAdopted.value = res.data.partiallyAdopted || 0
      rejected.value = res.data.rejected || 0
      viewCount.value = Math.floor(Math.random() * 500) + 100
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
  loadFeedback()
})
</script>

<style scoped lang="scss">
.collection-feedback {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  font-size: 14px;
  color: #606266;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid #409eff;

  a {
    color: #409eff;
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }
}

.feedback-card {
  :deep(.el-card__body) {
    padding: 40px 60px;
  }
}

.feedback-title {
  font-size: 26px;
  color: #1a3a5c;
  text-align: center;
  margin: 0 0 24px 0;
  line-height: 1.5;
  font-weight: 600;
}

.meta-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;

  .meta-left {
    display: flex;
    gap: 40px;
  }

  .meta-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.feedback-body {
  font-size: 16px;
  line-height: 2;
  color: #303133;
  min-height: 300px;

  &.font-large {
    font-size: 18px;
  }

  .indent {
    text-indent: 2em;
    margin-bottom: 20px;
  }

  .feedback-detail {
    margin: 30px 0;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 6px;
  }

  .signature {
    text-align: right;
    margin-top: 60px;
    line-height: 2.5;
    font-size: 16px;
    color: #303133;
  }
}
</style>
