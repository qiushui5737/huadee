<template>
  <div class="collection-list">
    <div class="page-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <h1>意见征集</h1>
    </div>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>征集列表</span>
          <div class="header-actions">
            <el-input v-model="filterKeyword" placeholder="搜索征集主题..." size="small" style="width: 200px; margin-right: 10px;" clearable @clear="loadList" @keyup.enter="loadList">
              <template #append>
                <el-button @click="loadList">搜索</el-button>
              </template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width: 120px;" @change="loadList">
              <el-option label="进行中" value="进行中" />
              <el-option label="已结束" value="已结束" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="listData" stripe style="width: 100%">
        <el-table-column prop="title" label="征集主题" min-width="300" show-overflow-tooltip />
        <el-table-column prop="deptName" label="征集单位" width="150" />
        <el-table-column label="征集时间" width="220">
          <template #default="{ row }">
            {{ row.startDate }} 至 {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">查看</el-button>
            <el-button v-if="row.status === '进行中'" link type="success" size="small" @click="participate(row)">我要参与</el-button>
            <el-button v-if="row.status === '已结束' && row.feedback" link type="warning" size="small" @click="showFeedback(row)">结果反馈</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadList" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { collectionPublicList } from '@/api/interaction'

const router = useRouter()

const listData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterStatus = ref('')
const filterKeyword = ref('')

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '进行中': 'success', '已结束': 'info', '未开始': 'warning' }
  return map[status] || ''
}

const goBack = () => {
  router.push('/interaction')
}

const loadList = async () => {
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize
    }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterStatus.value) params.status = filterStatus.value

    const res: any = await collectionPublicList(params)
    if (res.code === 200) {
      listData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const showDetail = (row: any) => {
  router.push({ name: 'collection-detail', params: { id: row.id } })
}

const participate = (row: any) => {
  router.push({ name: 'submit-opinion', params: { id: row.id } })
}

const showFeedback = (row: any) => {
  router.push({ name: 'collection-feedback', params: { id: row.id } })
}

onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.collection-list {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

</style>
