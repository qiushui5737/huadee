<template>
  <div class="collection-detail">
    <div class="page-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <h1>意见征集详情</h1>
    </div>

    <el-card v-loading="loading" shadow="hover">
      <div v-if="detail" class="detail-content">
        <!-- 左侧信息 -->
        <div class="detail-left">
          <div class="info-section">
            <h3 class="section-title">征集主题</h3>
            <p class="section-content">{{ detail.title }}</p>
          </div>

          <div class="info-section">
            <h3 class="section-title">征集时间</h3>
            <p class="section-content">{{ detail.startDate }} 至 {{ detail.endDate }}</p>
          </div>

          <div class="info-section">
            <h3 class="section-title">征集单位</h3>
            <p class="section-content">{{ detail.deptName || '-' }}</p>
          </div>

          <div class="info-section" v-if="detail.attachmentName">
            <h3 class="section-title">说明材料</h3>
            <p class="section-content">
              <el-icon><Document /></el-icon>
              {{ detail.attachmentName }}
            </p>
          </div>

          <!-- 我要参与按钮 -->
          <el-button v-if="detail.status === '进行中'" type="danger" size="large" style="width: 100%; margin-top: 20px;" @click="participate">
            我要参与
          </el-button>
          <el-tag v-else :type="detail.status === '已结束' ? 'info' : 'warning'" size="large" style="width: 100%; margin-top: 20px; text-align: center; padding: 12px;">
            {{ detail.status }}
          </el-tag>
        </div>

        <!-- 右侧说明 -->
        <div class="detail-right">
          <div class="description" v-if="detail.description" v-html="detail.description"></div>

          <div class="contact-info" v-if="detail.contactName || detail.contactPhone">
            <p>联系人：{{ detail.deptName || '' }} {{ detail.contactName || '' }}</p>
            <p>联系电话：{{ detail.contactPhone || '-' }}</p>
            <p style="text-align: right; margin-top: 20px;">{{ detail.deptName || '' }}</p>
            <p style="text-align: right;">{{ detail.createTime ? detail.createTime.split(' ')[0] : '' }}</p>
          </div>

          <div class="attachment" v-if="detail.attachmentName">
            <p>附件：{{ detail.attachmentName }}</p>
          </div>
        </div>
      </div>
    </el-card>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { collectionPublicDetail } from '@/api/interaction'

const route = useRoute()
const router = useRouter()
const collectionId = Number(route.params.id)

const loading = ref(false)
const detail = ref<any>(null)

const goBack = () => {
  router.push({ name: 'collection-list' })
}

const participate = () => {
  router.push({ name: 'submit-opinion', params: { id: collectionId } })
}

const loadDetail = async () => {
  if (!collectionId) {
    ElMessage.error('征集ID无效')
    return
  }
  loading.value = true
  try {
    const res: any = await collectionPublicDetail(collectionId)
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
.collection-detail {
  max-width: 1400px;
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
  display: flex;
  gap: 40px;

  .detail-left {
    flex: 1;
    max-width: 400px;
  }

  .detail-right {
    flex: 2;
  }
}

.info-section {
  margin-bottom: 24px;

  .section-title {
    font-size: 18px;
    color: #c00;
    font-weight: 600;
    margin-bottom: 12px;
    padding-left: 12px;
    border-left: 4px solid #c00;
  }

  .section-content {
    font-size: 16px;
    color: #303133;
    line-height: 1.6;
    padding-left: 16px;

    .el-icon {
      margin-right: 8px;
    }
  }
}

.description {
  font-size: 16px;
  line-height: 2;
  color: #303133;
  text-indent: 2em;
  margin-bottom: 24px;
}

.contact-info {
  font-size: 16px;
  line-height: 2;
  color: #303133;
  margin-top: 40px;
}

.attachment {
  margin-top: 20px;
  font-size: 14px;
  color: #606266;
}


</style>
