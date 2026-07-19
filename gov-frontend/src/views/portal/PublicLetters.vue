<template>
  <div class="public-letters">
    <div class="page-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <h1>公开信件</h1>
    </div>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>所有公开信件</span>
          <div class="header-actions">
            <el-input v-model="filterKeyword" placeholder="搜索标题/内容..." size="small" style="width: 200px; margin-right: 10px;" clearable @clear="loadLetters" @keyup.enter="loadLetters">
              <template #append>
                <el-button @click="loadLetters">搜索</el-button>
              </template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width: 120px;" @change="loadLetters">
              <el-option label="待分派" value="待分派" />
              <el-option label="已分派" value="已分派" />
              <el-option label="已回复" value="已回复" />
              <el-option label="已办结" value="已办结" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="letterList" stripe style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userName" label="留言人" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ row.type || '咨询' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" style="margin-top: 15px; justify-content: flex-end;" background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadLetters" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { messageList as fetchMessageList } from '@/api/interaction'

const router = useRouter()

const letterList = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterStatus = ref('')
const filterKeyword = ref('')

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

const loadLetters = async () => {
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize,
      isPublic: true
    }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterStatus.value) params.status = filterStatus.value

    const res: any = await fetchMessageList(params)
    if (res.code === 200) {
      letterList.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const showDetail = (row: any) => {
  router.push({ name: 'letter-detail', params: { id: row.id } })
}

onMounted(() => {
  loadLetters()
})
</script>

<style scoped lang="scss">
.public-letters {
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
