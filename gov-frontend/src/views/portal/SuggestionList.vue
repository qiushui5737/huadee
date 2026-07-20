<template>
  <div class="suggestion-list-page">
    <div class="page-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <h1>建议展示</h1>
    </div>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>建议列表</span>
          <div class="header-actions">
            <el-input v-model="filterKeyword" placeholder="搜索建议标题..." size="small" style="width: 200px; margin-right: 10px;" clearable @clear="loadList" @keyup.enter="loadList">
              <template #append>
                <el-button @click="loadList">搜索</el-button>
              </template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width: 120px;" @change="loadList">
              <el-option label="待受理" value="待受理" />
              <el-option label="处理中" value="处理中" />
              <el-option label="已答复" value="已答复" />
              <el-option label="已办结" value="已办结" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="listData" stripe style="width: 100%">
        <el-table-column prop="title" label="建议标题" min-width="300" show-overflow-tooltip />
        <el-table-column prop="type" label="建议类型" width="120" />
        <el-table-column prop="realName" label="建议人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">查看</el-button>
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
import { suggestionList } from '@/api/interaction'

const router = useRouter()

const listData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterStatus = ref('')
const filterKeyword = ref('')

const statusTagType = (status: string) => {
  const map: Record<string, string> = { '待受理': 'warning', '处理中': '', '已答复': 'success', '已办结': 'info' }
  return map[status] || ''
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
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

    const res: any = await suggestionList(params)
    if (res.code === 200) {
      listData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const showDetail = (row: any) => {
  router.push({ name: 'suggestion-detail', params: { id: row.id } })
}

onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.suggestion-list-page {
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
