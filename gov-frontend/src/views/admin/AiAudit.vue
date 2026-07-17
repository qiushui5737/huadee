<template>
  <div class="audit-page">
    <div class="audit-head">
      <div>
        <h2>AI 问答审计</h2>
        <p>查看群众智能问答记录、敏感命中状态和知识库回答内容。</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索问题或回答" clearable @keyup.enter="load" />
      <el-select v-model="status" clearable placeholder="审计状态">
        <el-option label="正常" value="normal" />
        <el-option label="敏感" value="sensitive" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-table :data="records" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sessionId" label="会话ID" width="180" show-overflow-tooltip />
      <el-table-column prop="question" label="群众问题" min-width="220" show-overflow-tooltip />
      <el-table-column prop="answer" label="AI回答" min-width="280" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.auditStatus === 'sensitive' ? 'danger' : 'success'">
            {{ row.auditStatus === 'sensitive' ? '敏感' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { chatAudit } from '@/api/ai'

const records = ref<any[]>([])
const keyword = ref('')
const status = ref('')
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res: any = await chatAudit({ keyword: keyword.value, status: status.value, page: page.value, size })
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.audit-page { padding: 20px; }
.audit-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.audit-head h2 { margin: 0; font-size: 22px; color: #172033; }
.audit-head p { margin: 6px 0 0; color: #667085; }
.toolbar { display: grid; grid-template-columns: minmax(220px, 1fr) 180px 88px; gap: 10px; margin-bottom: 14px; }
.el-pagination { margin-top: 14px; justify-content: flex-end; }
@media (max-width: 760px) {
  .toolbar { grid-template-columns: 1fr; }
}
</style>
