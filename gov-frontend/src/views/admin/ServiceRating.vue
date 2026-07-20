<template>
  <div class="rating-page">
    <div class="page-header">
      <div class="header-title">
        <h2>服务评价管理</h2>
        <p>查看和管理群众对政务服务的评价反馈</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">总评价数</span>
        </div>
        <div class="stat-item good">
          <span class="stat-value">{{ stats.avgRating }}</span>
          <span class="stat-label">平均评分</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.fiveStar }}</span>
          <span class="stat-label">五星好评</span>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="filter-bar">
        <el-select v-model="ratingFilter" placeholder="全部评分" style="width:150px;" @change="filterRatings">
          <el-option label="全部" value="" />
          <el-option label="5星" :value="5" />
          <el-option label="4星" :value="4" />
          <el-option label="3星" :value="3" />
          <el-option label="2星" :value="2" />
          <el-option label="1星" :value="1" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索受理号或申请人" style="width:250px; margin-left:10px;" @keyup.enter="filterRatings">
          <template #append>
            <el-button @click="filterRatings"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="loadRatings">查询</el-button>
      </div>

      <el-card class="rating-card">
        <el-table :data="filteredList" border :loading="loading">
          <el-table-column prop="acceptNo" label="受理号" width="200" />
          <el-table-column prop="itemName" label="服务事项" min-width="150" />
          <el-table-column prop="userName" label="评价人" width="100" />
          <el-table-column label="评分" width="180">
            <template #default="scope">
              <el-rate :model-value="scope.row.rating" disabled show-score text-color="#ff9900" />
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评价内容" min-width="250">
            <template #default="scope">
              <span>{{ scope.row.content || '无评价内容' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评价时间" width="180" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getAllRatings } from '@/api/service'

const loading = ref(false)
const ratingsList = ref<any[]>([])
const ratingFilter = ref('')
const keyword = ref('')

const stats = reactive({
  total: 0,
  avgRating: '0.0',
  fiveStar: 0
})

const filteredList = computed(() => {
  let list = ratingsList.value
  if (ratingFilter.value) {
    list = list.filter(r => r.rating === Number(ratingFilter.value))
  }
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(r =>
      (r.acceptNo && r.acceptNo.toLowerCase().includes(kw)) ||
      (r.userName && r.userName.toLowerCase().includes(kw))
    )
  }
  return list
})

const loadRatings = async () => {
  loading.value = true
  try {
    const res = await getAllRatings()
    if (res.code === 200) {
      ratingsList.value = res.data || []
      stats.total = ratingsList.value.length
      stats.fiveStar = ratingsList.value.filter(r => r.rating === 5).length
      if (stats.total > 0) {
        const sum = ratingsList.value.reduce((acc: number, r: any) => acc + r.rating, 0)
        stats.avgRating = (sum / stats.total).toFixed(1)
      }
    }
  } catch (error) {
    console.error('加载评价列表失败', error)
  }
  loading.value = false
}

const filterRatings = () => {
  // filteredList is computed, no action needed
}

onMounted(() => {
  loadRatings()
})
</script>

<style scoped>
.rating-page {
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

.good .stat-value {
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

.rating-card {
  border-radius: 8px;
}

.rating-card :deep(.el-table) {
  border-radius: 8px;
}
</style>
