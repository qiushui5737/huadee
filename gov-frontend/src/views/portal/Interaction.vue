<template>
  <div class="interaction-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>政民互动</h1>
      <p class="subtitle">您的声音，我们倾听</p>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：留言表单 -->
      <el-col :span="14">
        <el-card class="form-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>我要留言</span>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="留言标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入留言标题（20字以内）" maxlength="20" show-word-limit />
            </el-form-item>

            <el-form-item label="留言内容" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="6"
                placeholder="请详细描述您的问题或建议..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="联系姓名" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入您的姓名" />
            </el-form-item>

            <el-form-item label="目标部门">
              <el-select v-model="form.targetDept" placeholder="请选择部门（选填）" clearable>
                <el-option label="教育部" value="EDU" />
                <el-option label="卫健委" value="HEA" />
                <el-option label="住建局" value="HOU" />
                <el-option label="人社局" value="SOC" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">
                提交留言
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 留言列表 -->
        <el-card class="list-card" shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>留言列表</span>
              <div>
                <el-select v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width: 120px; margin-right: 10px;" @change="loadMessages">
                  <el-option label="待分派" value="待分派" />
                  <el-option label="已分派" value="已分派" />
                  <el-option label="已回复" value="已回复" />
                  <el-option label="已办结" value="已办结" />
                </el-select>
                <el-input v-model="filterKeyword" placeholder="搜索留言..." size="small" style="width: 200px;" clearable @clear="loadMessages" @keyup.enter="loadMessages">
                  <template #append>
                    <el-button @click="loadMessages">搜索</el-button>
                  </template>
                </el-input>
              </div>
            </div>
          </template>

          <el-table :data="messageList" stripe style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="userName" label="留言人" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
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
      </el-col>

      <!-- 右侧：热门留言 -->
      <el-col :span="10">
        <el-card class="hot-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>热门留言</span>
              <el-icon><Star /></el-icon>
            </div>
          </template>

          <el-empty v-if="hotList.length === 0" description="暂无热门留言" />

          <div v-for="item in hotList" :key="item.id" class="hot-item">
            <div class="hot-title">{{ item.title }}</div>
            <div class="hot-meta">
              <el-tag :type="statusTagType(item.status)" size="small">{{ item.status }}</el-tag>
              <span class="hot-time">{{ item.createTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 留言详情弹窗 -->
    <el-dialog v-model="detailVisible" title="留言详情" width="600px">
      <el-descriptions :column="1" border v-if="currentMessage">
        <el-descriptions-item label="标题">{{ currentMessage.title }}</el-descriptions-item>
        <el-descriptions-item label="留言人">{{ currentMessage.userName }}</el-descriptions-item>
        <el-descriptions-item label="目标部门">{{ currentMessage.deptCode || '未指定' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentMessage.status)">{{ currentMessage.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="留言内容">{{ currentMessage.content }}</el-descriptions-item>
        <el-descriptions-item label="回复内容" v-if="currentMessage.replyContent">
          {{ currentMessage.replyContent }}
        </el-descriptions-item>
        <el-descriptions-item label="回复人" v-if="currentMessage.replyBy">
          {{ currentMessage.replyBy }}
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentMessage.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { submitMessage, messageList as fetchMessageList, hotMessages } from '@/api/interaction'

// 表单数据
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  title: '',
  content: '',
  contactName: '',
  targetDept: ''
})

const rules = {
  title: [{ required: true, message: '请输入留言标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }]
}

// 留言列表
const messageList = ref<any[]>([])
const hotList = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const filterStatus = ref('')
const filterKeyword = ref('')

// 详情弹窗
const detailVisible = ref(false)
const currentMessage = ref<any>(null)

// 提交留言
const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const res: any = await submitMessage(form)
    if (res.code === 200) {
      ElMessage.success('留言提交成功')
      resetForm()
      loadMessages()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，请重试')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
}

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
    }
  } catch (e) {
    console.error('加载留言列表失败', e)
  }
}

// 加载热门留言
const loadHotMessages = async () => {
  try {
    const res: any = await hotMessages()
    if (res.code === 200) {
      hotList.value = res.data
    }
  } catch (e) {
    console.error('加载热门留言失败', e)
  }
}

// 显示详情
const showDetail = (row: any) => {
  currentMessage.value = row
  detailVisible.value = true
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
  loadHotMessages()
})
</script>

<style scoped lang="scss">
.interaction-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  h1 { font-size: 28px; color: #1a3a5c; margin: 0; }
  .subtitle { color: #888; margin-top: 8px; font-size: 14px; }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.hot-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  &:last-child { border-bottom: none; }
  .hot-title {
    font-size: 14px;
    color: #333;
    margin-bottom: 6px;
    cursor: pointer;
    &:hover { color: #409eff; }
  }
  .hot-meta {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 12px;
    color: #999;
  }
}
</style>
